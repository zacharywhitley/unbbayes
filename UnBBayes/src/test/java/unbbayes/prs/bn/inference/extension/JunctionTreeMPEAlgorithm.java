/**
 * 
 */
package unbbayes.prs.bn.inference.extension;

import java.util.Locale;
import java.util.ResourceBundle;

import unbbayes.controller.INetworkMediator;
import unbbayes.prs.Node;
import unbbayes.prs.bn.DefaultJunctionTreeBuilder;
import unbbayes.prs.bn.IJunctionTreeBuilder;
import unbbayes.prs.bn.JunctionTreeAlgorithm;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.TreeVariable;
import unbbayes.util.extension.bn.inference.InferenceAlgorithmOptionPanel;

/**
 * This algorithm is a Juction tree algorithm for MPE.
 * @author Shou Matsumoto
 *
 */
public class JunctionTreeMPEAlgorithm extends JunctionTreeAlgorithm {

	private InferenceAlgorithmOptionPanel optionPanel;
	
	private static ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
			unbbayes.controller.resources.ControllerResources.class.getName(),
			Locale.getDefault(),
			JunctionTreeMPEAlgorithm.class.getClassLoader());
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
		if (this.getMediator() != null) {
			// if we have access to the controller, update status label
			float totalEstimateProb = this.getNet().PET();
			this.getMediator().getScreen().setStatus(this.getResource()
					.getString("statusEvidenceProbabilistic")
					+ (totalEstimateProb * 100.0));
		}
		// convert the most probable state ("marginal" value) to 100%
		// and change other states proportionally
		for (Node node : this.getNetwork().getNodes()) {
			// only consider nodes with a value in the JTree at the left side of BN compilation pane
			// (UnBBayes calls this a "marginal")
			if (node instanceof TreeVariable) {
				TreeVariable nodeWithMarginal = (TreeVariable) node;
				
				// these vars store the probability of the most probable state of this node 
				float greatestMarginal = Float.MIN_VALUE;
				for (int i = 0; i < nodeWithMarginal.getStatesSize(); i++) {
					// extract "marginals"
					float currentMarginal = nodeWithMarginal.getMarginalAt(i);
					if (currentMarginal > greatestMarginal) {
						greatestMarginal = currentMarginal;
					}
				}
				
				// use greatestMarginal to set probability of most probable state to 1 and alter others proportionally
				// this is equivalent to finding the value of X inthe following proportional equation:
				//		greatestMarginal = 100% :  currentMarginal = X
				//	(thus, X = 100%*currentMarginal/greatestMarginal)
				if (greatestMarginal > 0) {
					for (int i = 0; i < nodeWithMarginal.getStatesSize(); i++) {
						// if nodeWithMarginal.getMarginalAt(i) == greatestMarginal, then it sets to 1 (100%).
						// if not, it sets to X = 100%*currentMarginal/greatestMarginal (100% = 1)
						nodeWithMarginal.setMarginalAt(i, nodeWithMarginal.getMarginalAt(i)/greatestMarginal);
					}
				} else {
					throw new IllegalStateException("MPE = 0");
				}
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
	 * @return the resource
	 */
	public static ResourceBundle getResource() {
		return resource;
	}

	/**
	 * @param resource the resource to set
	 */
	public static void setResource(ResourceBundle resource) {
		JunctionTreeMPEAlgorithm.resource = resource;
	}

	/**
	 * @return the mediator
	 */
	public INetworkMediator getMediator() {
		if (this.getOptionPanel() != null) {
			return this.getOptionPanel().getMediator();
		}
		return null;
	}


	/**
	 * @return the optionPanel
	 */
	public InferenceAlgorithmOptionPanel getOptionPanel() {
		return optionPanel;
	}

	/**
	 * @param optionPanel the optionPanel to set
	 */
	public void setOptionPanel(InferenceAlgorithmOptionPanel optionPanel) {
		this.optionPanel = optionPanel;
	}
	
	
	
}
