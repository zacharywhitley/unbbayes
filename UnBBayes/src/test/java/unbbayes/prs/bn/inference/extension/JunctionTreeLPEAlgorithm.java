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
					// use greatestMarginal to set probability of most probable state to 1 and alter others proportionally
					// this is equivalent to finding the value of X inthe following proportional equation:
					//		greatestMarginal = 100% :  currentMarginal = X
					//	(thus, X = 100%*currentMarginal/greatestMarginal)
					for (int i = 0; i < nodeWithMarginal.getStatesSize(); i++) {
						// if nodeWithMarginal.getMarginalAt(i) == greatestMarginal, then it sets to 1 (100%).
						// if not, it sets to X = 100%*currentMarginal/greatestMarginal (100% = 1)
						nodeWithMarginal.setMarginalAt(i, nodeWithMarginal.getMarginalAt(i)/smallestMarginal);
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
	public String getDescription() {
		return "Most Probable Explanation with Junction Tree";
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.JunctionTreeAlgorithm#getName()
	 */
	public String getName() {
		return "Junction Tree Least PE";
	}

	
}
