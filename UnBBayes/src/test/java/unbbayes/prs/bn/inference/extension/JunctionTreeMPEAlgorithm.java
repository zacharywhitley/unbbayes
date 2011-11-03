/**
 * 
 */
package unbbayes.prs.bn.inference.extension;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import unbbayes.prs.Graph;
import unbbayes.prs.Node;
import unbbayes.prs.bn.DefaultJunctionTreeBuilder;
import unbbayes.prs.bn.IJunctionTreeBuilder;
import unbbayes.prs.bn.JunctionTreeAlgorithm;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.SingleEntityNetwork;
import unbbayes.prs.bn.TreeVariable;

/**
 * This algorithm is a Juction tree algorithm for MPE.
 * @author Shou Matsumoto
 *
 */
public class JunctionTreeMPEAlgorithm extends JunctionTreeAlgorithm {

	
	private boolean isToCalculateProbOfNonMPE = false;
	
	/**
	 * 
	 */
	public JunctionTreeMPEAlgorithm() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param net
	 */
	public JunctionTreeMPEAlgorithm(ProbabilisticNetwork net) {
		super(net);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.JunctionTreeAlgorithm#run()
	 */
	@Override
	public void run() throws IllegalStateException {
		if (this.getNet() != null) {
			IJunctionTreeBuilder backup = this.getNet().getJunctionTreeBuilder();	// store previous JT builder
			// indicate the network (consequently, the superclass) to use MaxProductJunctionTree instead of default junction tree.
			this.getNet().setJunctionTreeBuilder(new DefaultJunctionTreeBuilder(MaxProductJunctionTree.class));
			super.run();	// run with new JT builder
			this.getNet().setJunctionTreeBuilder(backup);	// revert change
		} else {
			// run anyway
			super.run();
		}
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.JunctionTreeAlgorithm#propagate()
	 */
	@Override
	public void propagate() {
		super.propagate();
		this.markMPEAs100Percent(this.getNetwork(), this.isToCalculateProbOfNonMPE());
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
				} else {
					evidenceMap.put(nodeWithMarginal.getName(), index);
				}
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

	
	
}
