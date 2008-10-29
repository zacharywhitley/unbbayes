package unbbayes.prs.bn;

import unbbayes.prs.Node;

// FIXME We have to refactor the Node inheritance to separate discrete from coninous and other messy things!
// FIXME GATO no continuous node
public class ContinuousNode extends ProbabilisticNode {

	private static final long serialVersionUID = 1L;
	
	private CNNormalDistribution cnNormalDistribution;
	
	@Override
	public int getType() {
		return Node.CONTINUOUS_NODE_TYPE;
	}
	
	public ContinuousNode() {
		cnNormalDistribution = new CNNormalDistribution(this);
	}
	
	
	

}
