/**
 * 
 */
package unbbayes.prs.bn.inference.extension;

import java.util.Date;
import java.util.Map;

import unbbayes.prs.Graph;
import unbbayes.prs.INode;
import unbbayes.prs.Node;
import unbbayes.prs.bn.DefaultJunctionTreeBuilder;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.SingleEntityNetwork;
import unbbayes.prs.bn.SingleEntityNetworkWrapper;
import unbbayes.prs.bn.TreeVariable;
import unbbayes.util.Debug;
import unbbayes.util.extension.bn.inference.InferenceAlgorithmOptionPanel;

/**
 * @author Shou Matsumoto
 *
 */
public class JunctionTreeLPEAlgorithm extends JunctionTreeMPEAlgorithm {


	/**
	 * 
	 */
	public JunctionTreeLPEAlgorithm() {
		this(null);
	}

	/**
	 * @param net
	 */
	public JunctionTreeLPEAlgorithm(ProbabilisticNetwork net) {
		super(net);
		// initialize default junction tree with a min-product operator
		try{
			this.setDefaultJunctionTreeBuilder(new DefaultJunctionTreeBuilder(MinProductJunctionTree.class));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	/**
	 * convert the least probable state ("marginal" value) to 100%
	 * @param network : set of nodes whose most probable states will be set to 100%
	 * @param isToCalculateRelativeProb : if true, then this method will try to update
	 * the probabilities of the states other than LPE to be proportional to the new values
	 * of the LPE. For example:
	 * 		Initial: least possible state = 70%, other state = 30%
	 * 		Posterior (most possible is set to 100%): least possible state = 100%, other state = 30/70%
	 * 
	 */
	protected void markMPEAs100Percent(Graph network, boolean isToCalculateRelativeProb) {
		// The change bellow is to adhere to feature request #3314855
		// Save the list of evidence entered
//		Map<String, Integer> evidenceMap = new HashMap<String, Integer>();
		// Mapping likelihood also to fix bug #3316285
//		Map<String, Float[]> likelihoodMap = new HashMap<String, Float[]>();
		
		for (Node node : network.getNodes()) {
			// only consider nodes with a value in the JTree at the left side of BN compilation pane
			// (UnBBayes calls this a "marginal")
			if (node instanceof TreeVariable) {
				TreeVariable nodeWithMarginal = (TreeVariable) node;
				
				// remember evidence of this node, because we are going to clear them and restore them again
//				if (nodeWithMarginal.hasEvidence() && !isToCalculateRelativeProb) {
//					if (nodeWithMarginal.hasLikelihood()) {
//						Float[] likelihood = new Float[nodeWithMarginal.getStatesSize()];
//						for (int i = 0; i < nodeWithMarginal.getStatesSize(); i++) {
//				            likelihood[i] = nodeWithMarginal.getMarginalAt(i);
//				        }
//						likelihoodMap.put(nodeWithMarginal.getName(), likelihood);
//					} else {
//						evidenceMap.put(nodeWithMarginal.getName(), nodeWithMarginal.getEvidence());
//					}
//				}
				
				// these vars store the probability of the most probable state of this node 
				float smallestMarginal = Float.MAX_VALUE;
				int index = 0;
				for (int i = 0; i < nodeWithMarginal.getStatesSize(); i++) {
					// extract "marginals"
					float currentMarginal = nodeWithMarginal.getMarginalAt(i);
					if ((currentMarginal < smallestMarginal) && (currentMarginal > 0)) {
						smallestMarginal = currentMarginal;
						index = i;
					}
				}
				
				if (smallestMarginal <= 0) {
					throw new IllegalStateException("LPE = 0");
				}
				
				if (isToCalculateRelativeProb) {
					if (!nodeWithMarginal.hasEvidence()) {
						// use greatestMarginal to set probability of most probable state to 1 and alter others proportionally
						// this is equivalent to finding the value of X inthe following proportional equation:
						//		greatestMarginal = 100% :  currentMarginal = X
						//	(thus, X = 100%*currentMarginal/greatestMarginal)
						for (int i = 0; i < nodeWithMarginal.getStatesSize(); i++) {
							// if nodeWithMarginal.getMarginalAt(i) == greatestMarginal, then it sets to 1 (100%).
							// if not, it sets to X = 100%*currentMarginal/greatestMarginal (100% = 1)
							nodeWithMarginal.setMarginalAt(i, (1.0f - nodeWithMarginal.getMarginalAt(i)) / (1.0f - smallestMarginal));
							if ((nodeWithMarginal.getMarginalAt(i) < 0.0f) || (nodeWithMarginal.getMarginalAt(i) > 1.0f)) {
								try {
									Debug.println(getClass(), "The state \"" + nodeWithMarginal.getStateAt(i) + "\" of node "
											+ nodeWithMarginal  + " is inconsistent: " + nodeWithMarginal.getMarginalAt(i));
								} catch (Throwable t) {
									t.printStackTrace();
								}
								nodeWithMarginal.setMarginalAt(i, 0.0f);
							}
						}
					} else {
						nodeWithMarginal.setMarginalAt(nodeWithMarginal.getEvidence(), 1.0f);
					}
					if (this.getMediator() != null) {
						// if we have access to the controller, update status label
						this.getMediator().getScreen().setStatus(this.getResource().getString("okButtonLabel"));
					}
				} else {
//					evidenceMap.put(nodeWithMarginal.getName(), index);
				}
			}
		}
		// recalculate joint probability
		if ((network instanceof SingleEntityNetwork)) {
			try {
				SingleEntityNetwork singleEntityNetwork = (SingleEntityNetwork) network;
				IExplanationJunctionTree jt = null;
				if (singleEntityNetwork.getJunctionTree() instanceof IExplanationJunctionTree) {
					jt = (IExplanationJunctionTree) singleEntityNetwork.getJunctionTree();
				}
				Map<INode, Integer> explanation = jt.calculateExplanation(singleEntityNetwork, this);
				
				if (!isToCalculateRelativeProb) {
					// Reset evidence in order to allow changes in node which already had a different evidence set
					this.reset();
					// Enter the list of evidence again
//					for (String name : evidenceMap.keySet()) {
//						((TreeVariable)singleEntityNetwork.getNode(name)).addFinding(evidenceMap.get(name));
//					}
					for (INode inode : explanation.keySet()) {
						if (inode instanceof TreeVariable) {
							TreeVariable node = (TreeVariable) inode;
							node.addFinding(explanation.get(node));
						}
					}
					// Enter the likelihood 
//					for (String name : likelihoodMap.keySet()) {
//						float[] likelihood = new float[likelihoodMap.get(name).length];
//						for (int i = 0; i < likelihood.length; i++) {
//							likelihood[i] = likelihoodMap.get(name)[i];
//						}
//						((TreeVariable)singleEntityNetwork.getNode(name)).addLikeliHood(likelihood);
//					}
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
				} else {
					// display explanation
					String explMessage = "\n\n[" + new Date() + "] LPE: \n\n";
					String simpleMessage = "LPE = {";
					for (INode node : explanation.keySet()) {
						explMessage += "\t" + node + " = " + node.getStateAt(explanation.get(node)) + "; \n";
						simpleMessage += node.getName() + " = " + node.getStateAt(explanation.get(node)) + "; ";
					}
					explMessage = explMessage.substring(0, explMessage.lastIndexOf("\n"));	// remove last \n
					explMessage += "\n";
					simpleMessage = simpleMessage.substring(0, simpleMessage.lastIndexOf(";"));	// remove last ';'
					simpleMessage += " }";
					if (this.getMediator() != null) {
						// if we have access to the controller, update status label
						this.getMediator().getScreen().setStatus(simpleMessage);
						try {
							// use wrapper to extract the log manager and update network log.
							new SingleEntityNetworkWrapper(singleEntityNetwork).getLogManager().append(explMessage);
							// show network log (click the "show log" button)
//							this.getMediator().getScreen().getNetWindowCompilation().getLog().doClick();
						} catch (Exception e) {
							try {
								Debug.println(getClass(), "Could not update log" ,e);
							} catch (Throwable t) {
								t.printStackTrace();
							}
						}
					}
					System.out.println(explMessage);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.JunctionTreeAlgorithm#getDescription()
	 */
	public String getDescription() {
		return "Least Probable Explanation with Junction Tree";
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.JunctionTreeAlgorithm#getName()
	 */
	public String getName() {
		return "Junction Tree Least PE";
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.JunctionTreeAlgorithm#reset()
	 */
	public void reset() {
		super.reset();
		InferenceAlgorithmOptionPanel bkp = this.getOptionPanel();	// backup the option panel
		this.setOptionPanel(null);	// setting option panel to null disables access to GUI (so, log will be disabled)
		this.propagate();	// propagate with log disabled
		this.setOptionPanel(bkp);	// restore backup
	}

	
}
