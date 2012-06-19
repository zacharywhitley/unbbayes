/**
 * 
 */
package unbbayes.prs.bn.inference.extension;

import java.util.HashMap;
import java.util.Map;

import unbbayes.prs.Graph;
import unbbayes.prs.INode;
import unbbayes.prs.Node;
import unbbayes.prs.bn.Clique;
import unbbayes.prs.bn.DefaultJunctionTreeBuilder;
import unbbayes.prs.bn.IJunctionTree;
import unbbayes.prs.bn.IJunctionTreeBuilder;
import unbbayes.prs.bn.IRandomVariable;
import unbbayes.prs.bn.JunctionTreeAlgorithm;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.SingleEntityNetwork;
import unbbayes.prs.bn.TreeVariable;
import unbbayes.util.Debug;

/**
 * This algorithm is a Juction tree algorithm for MPE.
 * @author Shou Matsumoto
 *
 */
public class JunctionTreeMPEAlgorithm extends JunctionTreeAlgorithm {

	
	private boolean isToCalculateProbOfNonMPE = true;
	
	private IJunctionTreeBuilder defaultJunctionTreeBuilder;
	
	/**
	 * 
	 */
	public JunctionTreeMPEAlgorithm() {
		this(null);
	}

	/**
	 * @param net
	 */
	public JunctionTreeMPEAlgorithm(ProbabilisticNetwork net) {
		super(net);
		try {
			this.setDefaultJunctionTreeBuilder(new DefaultJunctionTreeBuilder(MaxProductJunctionTree.class));
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.JunctionTreeAlgorithm#run()
	 */
	public void run() throws IllegalStateException {
		if (this.getNet() != null) {
			IJunctionTreeBuilder backup = this.getNet().getJunctionTreeBuilder();	// store previous JT builder
			// indicate the network (consequently, the superclass) to use MaxProductJunctionTree instead of default junction tree.
			this.getNet().setJunctionTreeBuilder(this.getDefaultJunctionTreeBuilder());
			super.run();	// run with new JT builder
			this.propagate();
			this.getNet().setJunctionTreeBuilder(backup);	// revert change
		} else {
			// run anyway
			super.run();
		}
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.JunctionTreeAlgorithm#propagate()
	 */
	public void propagate() {
		super.propagate();
		this.updateMarginals(this.getNetwork());
		this.markMPEAs100Percent(this.getNetwork(), this.isToCalculateProbOfNonMPE());
	}
	
	/**
	 * This method customizes {@link SingleEntityNetwork#updateMarginais()},
	 * which is protected and thus not visible from this class.
	 * The modification disables marginalization by sum and enables a max product
	 * instead
	 * @param graph
	 */
	protected void updateMarginals(Graph graph) {
		for (Node node : graph.getNodes()) {
			/* Check if the node represents a numeric attribute */
			if (node.getStatesSize() == 0) {
				/* 
				 * The node represents a numeric attribute which has no
				 * potential table. Just skip it.
				 */
				continue;
			}
			this.updateMarginal(node);
		}
	}
	
	/**
	 * Updates the marginal of a node.
	 * This method customizes {@link TreeVariable#marginal()}, which is not visible.
	 * @param node
	 */
	protected void updateMarginal(INode node) {
		if ((node != null) && (node instanceof TreeVariable)) {
			TreeVariable treeVariable = (TreeVariable) node;
			
			// ensure marginal list is initialized
			treeVariable.initMarginalList();
			// obtain clique where node belongs and the probability distribution
			IRandomVariable relatedClique = null;
			try {
				for (Clique clique : ((SingleEntityNetwork)getNet()).getJunctionTree().getCliques()) {
					if (clique.getAssociatedProbabilisticNodes().contains(treeVariable)) {
						relatedClique = clique;
						break;
					}
				}
			} catch (Exception e) {
				throw new IllegalStateException("Could not extract clique from " + treeVariable + " in network " + getNet(), e);
			}
			if (relatedClique == null) {
				throw new IllegalStateException("Could not extract clique from " + treeVariable + " in network " + getNet());
			}
			PotentialTable auxTab = (PotentialTable) ((PotentialTable)relatedClique.getProbabilityFunction()).clone();
			
			// iterate over nodes in clique and start doing "max-marginalization"
			int index = auxTab.indexOfVariable(treeVariable);
			for (int i = 0; i < relatedClique.getProbabilityFunction().variableCount(); i++) {
				if (i != index) {
					// extract operation of the default junction tree
					IJunctionTree jt;
					try {
						jt = this.getDefaultJunctionTreeBuilder().buildJunctionTree(getNetwork());
					} catch (InstantiationException e) {
						throw new IllegalStateException("Could not extract junction tree from builder", e);
					} catch (IllegalAccessException e) {
						throw new IllegalStateException("Could not extract junction tree from builder", e);
					}
					if ((jt != null) 
							&& (jt instanceof IPropagationOperationHolder)) {
						IPropagationOperationHolder tree = (IPropagationOperationHolder) jt;
						
						PotentialTable.ISumOperation backupOp = auxTab.getSumOperation();	// backup real op
						auxTab.setSumOperation(tree.getMaxOperation());	// substitute op w/ operator for comparison (max) instead of sum (marginal)
						// remove maxout (this will automatically marginalize)
						auxTab.removeVariable(relatedClique.getProbabilityFunction().getVariableAt(i));
						auxTab.setSumOperation(backupOp);	// restore previous op
					} else {
						try {
							Debug.println(this.getClass(), "Unknown junction tree: " + jt);
						} catch (Throwable e) {
							e.printStackTrace();
						}
						// remove variable using default operation
						auxTab.removeVariable(relatedClique.getProbabilityFunction().getVariableAt(i));
					}
				}
			}
			
			// the table will contain the marginals. Copy values.
			for (int i = 0; i < treeVariable.getStatesSize(); i++) {
				treeVariable.setMarginalAt(i, auxTab.getValue(i));
			}
		}
	}
	
	

	/**
	 * convert the most probable state ("marginal" value) to 100%
	 * @param network : set of nodes whose most probable states will be set to 100%
	 * @param isToCalculateRelativeProb : if true, then this method will try to update
	 * the probabilities of the states other than MPE to be proportional to the new values
	 * of the MPE. For example:
	 * 		Initial: most possible state = 70%, other state = 30%
	 * 		Posterior (most possible is set to 100%): most possible state = 100%, other state = 30/70%
	 * 
	 */
	protected void markMPEAs100Percent(Graph network, boolean isToCalculateRelativeProb) {
		// The change bellow is to adhere to feature request #3314855
		// Save the list of evidence entered
		Map<String, Integer> evidenceMap = new HashMap<String, Integer>();
		// Mapping likelihood also to fix bug #3316285
		Map<String, Float[]> likelihoodMap = new HashMap<String, Float[]>();
		
		for (Node node : network.getNodes()) {
			// only consider nodes with a value in the JTree at the left side of BN compilation pane
			// (UnBBayes calls this a "marginal")
			if (node instanceof TreeVariable) {
				TreeVariable nodeWithMarginal = (TreeVariable) node;
				
				// remember evidence of this node, because we are going to clear them and restore them again
				if (nodeWithMarginal.hasEvidence() && !isToCalculateRelativeProb) {
					if (nodeWithMarginal.hasLikelihood()) {
						Float[] likelihood = new Float[nodeWithMarginal.getStatesSize()];
						for (int i = 0; i < nodeWithMarginal.getStatesSize(); i++) {
				            likelihood[i] = nodeWithMarginal.getMarginalAt(i);
				        }
						likelihoodMap.put(nodeWithMarginal.getName(), likelihood);
					} else {
						evidenceMap.put(nodeWithMarginal.getName(), nodeWithMarginal.getEvidence());
					}
				}
				
				// these vars store the probability of the most probable state of this node 
				float greatestMarginal = Float.MIN_VALUE;
				int index = 0;
				for (int i = 0; i < nodeWithMarginal.getStatesSize(); i++) {
					// extract "marginals"
					float currentMarginal = nodeWithMarginal.getMarginalAt(i);
					if (currentMarginal > greatestMarginal) {
						greatestMarginal = currentMarginal;
						index = i;
					}
				}
				
				if (greatestMarginal <= 0) {
					throw new IllegalStateException("MPE = 0");
				}
				
				if (isToCalculateRelativeProb) {
					// use greatestMarginal to set probability of most probable state to 1 and alter others proportionally
					// this is equivalent to finding the value of X inthe following proportional equation:
					//		greatestMarginal = 100% :  currentMarginal = X
					//	(thus, X = 100%*currentMarginal/greatestMarginal)
					for (int i = 0; i < nodeWithMarginal.getStatesSize(); i++) {
						// if nodeWithMarginal.getMarginalAt(i) == greatestMarginal, then it sets to 1 (100%).
						// if not, it sets to X = 100%*currentMarginal/greatestMarginal (100% = 1)
						nodeWithMarginal.setMarginalAt(i, nodeWithMarginal.getMarginalAt(i)/greatestMarginal);
					}
				}
				evidenceMap.put(nodeWithMarginal.getName(), index);
			}
		}
		// recalculate joint probability
		if ((network instanceof SingleEntityNetwork) && !isToCalculateRelativeProb) {
			try {
				SingleEntityNetwork singleEntityNetwork = (SingleEntityNetwork) network;
				// Reset evidence in order to allow changes in node which already had a different evidence set
				this.reset();
				// Enter the list of evidence again
				for (String name : evidenceMap.keySet()) {
					((TreeVariable)singleEntityNetwork.getNode(name)).addFinding(evidenceMap.get(name));
				}
				// Enter the likelihood 
				for (String name : likelihoodMap.keySet()) {
					float[] likelihood = new float[likelihoodMap.get(name).length];
					for (int i = 0; i < likelihood.length; i++) {
						likelihood[i] = likelihoodMap.get(name)[i];
					}
					((TreeVariable)singleEntityNetwork.getNode(name)).addLikeliHood(likelihood);
				}
				// Finally propagate evidence
				singleEntityNetwork.updateEvidences();
				// update status
				if (this.getMediator() != null) {
					// if we have access to the controller, update status label
					float totalEstimateProb = this.getNet().PET();
					this.getMediator().getScreen().setStatus(this.getResource()
							.getString("statusEvidenceProbabilistic")
							+ (totalEstimateProb * 100.0) + "%");
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.JunctionTreeAlgorithm#getDescription()
	 */
	@Override
	public String getDescription() {
		return "Most Probable Explanation with Junction Tree";
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.JunctionTreeAlgorithm#getName()
	 */
	@Override
	public String getName() {
		return "Junction Tree MPE";
	}


	/**
	 * If this is true, then {@link #markMPEAs100Percent(Graph, boolean)}
	 *  will try to calculate
	 * the probability of the states that is not part of MPE as well.
	 * Caution: the joint probability may not be consistent if this is set to true.
	 * @return the isToCalculateProbOfNonMPE
	 */
	public boolean isToCalculateProbOfNonMPE() {
		return isToCalculateProbOfNonMPE;
	}

	/**
	 * If this is true, then {@link #markMPEAs100Percent(Graph, boolean)}
	 *  will try to calculate
	 * the probability of the states that is not part of MPE as well.
	 * Caution: the joint probability may not be consistent if this is set to true.
	 * @param isToCalculateProbOfNonMPE the isToCalculateProbOfNonMPE to set
	 */
	public void setToCalculateProbOfNonMPE(boolean isToCalculateProbOfNonMPE) {
		this.isToCalculateProbOfNonMPE = isToCalculateProbOfNonMPE;
	}

	/**
	 * This builder will be passed to {@link #getNet()} during execution of {@link #run()},
	 * so that the {@link SingleEntityNetwork#compileJT(IJunctionTree)} can use the correct 
	 * junction tree.
	 * 
	 * @return the defaultJunctionTreeBuilder
	 */
	public IJunctionTreeBuilder getDefaultJunctionTreeBuilder() {
		return defaultJunctionTreeBuilder;
	}

	/**
	 * This builder will be passed to {@link #getNet()} during execution of {@link #run()},
	 * so that the {@link SingleEntityNetwork#compileJT(IJunctionTree)} can use the correct 
	 * junction tree.
	 * @param defaultJunctionTreeBuilder the defaultJunctionTreeBuilder to set
	 */
	public void setDefaultJunctionTreeBuilder(
			IJunctionTreeBuilder defaultJunctionTreeBuilder) {
		this.defaultJunctionTreeBuilder = defaultJunctionTreeBuilder;
	}

	/**
	 * @return if true, this indicates that this junction tree algorithm performs
	 * normalization on clique tables in order to maintain consistency.
	 * If false, this junction tree algorithm does not perform normalization
	 * (e.g. it may be a junction tree algorithm for explanation, instead of probability propagation).
	 * By default, a MPE algorithm will return false, because the sum of the probabilities
	 * is not 1.
	 * @see JunctionTreeAlgorithm#isAlgorithmWithNormalization()
	 */
	public boolean isAlgorithmWithNormalization() {
		return false;
	}
	
}
