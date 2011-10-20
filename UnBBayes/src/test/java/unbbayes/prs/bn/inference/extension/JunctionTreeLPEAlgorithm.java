/**
 * 
 */
package unbbayes.prs.bn.inference.extension;

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

	
}
