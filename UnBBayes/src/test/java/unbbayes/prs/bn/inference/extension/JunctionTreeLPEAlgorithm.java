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

/**
 * @author Shou Matsumoto
 *
 */
public class JunctionTreeLPEAlgorithm extends JunctionTreeAlgorithm {

	private INetworkMediator mediator;
	
	private static ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
			unbbayes.controller.resources.ControllerResources.class.getName(),
			Locale.getDefault(),
			JunctionTreeMPEAlgorithm.class.getClassLoader());
	
	/**
	 * 
	 */
	public JunctionTreeLPEAlgorithm() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param net
	 */
	public JunctionTreeLPEAlgorithm(ProbabilisticNetwork net) {
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
			this.getNet().setJunctionTreeBuilder(new DefaultJunctionTreeBuilder(MinProductJunctionTree.class));
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
				float minimum = Float.MAX_VALUE;
				int indexOfMinimum = -1;
				for (int i = 0; i < nodeWithMarginal.getStatesSize(); i++) {
					// extract "marginals"
					float currentMarginal = nodeWithMarginal.getMarginalAt(i);	// use complement (1 - prob) to consider least probable values
					if (currentMarginal < minimum) {
						minimum = currentMarginal;
						indexOfMinimum = i;
					}
				}
				
				// use greatestMarginal to set probability of least probable state to 1 and others to 0
				if (indexOfMinimum >= 0) {	// if found
					// set everything to zero
					for (int i = 0; i < nodeWithMarginal.getStatesSize(); i++) {
						nodeWithMarginal.setMarginalAt(i, 0);
					}
					// set minimum to 1
					nodeWithMarginal.setMarginalAt(indexOfMinimum, 1);
				} else {
					throw new IllegalStateException("LPE not found.");
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
	 * @see unbbayes.prs.bn.inference.extension.JunctionTreeMPEAlgorithm#getName()
	 */
	@Override
	public String getName() {
		return "Junction Tree Least PE";
	}

	/**
	 * @return the mediator
	 */
	public INetworkMediator getMediator() {
		return mediator;
	}

	/**
	 * @param mediator the mediator to set
	 */
	public void setMediator(INetworkMediator mediator) {
		this.mediator = mediator;
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
		JunctionTreeLPEAlgorithm.resource = resource;
	}

	
}
