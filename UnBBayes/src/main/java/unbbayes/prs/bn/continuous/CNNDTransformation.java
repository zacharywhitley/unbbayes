package unbbayes.prs.bn.continuous;

import unbbayes.prs.Node;

/**
 * This class is responsible for the Continuous Node Normal Distribution transformations.
 * @author Rommel Carvalho (rommel.carvalho@gmail.com)
 *
 */
public class CNNDTransformation {
	
	/**
	 * http://mathworld.wolfram.com/NormalSumDistribution.html
	 * @param cNode
	 * @return
	 * @throws Exception
	 */
	public static double getMean(ContinuousNode cNode) throws Exception {
		CNNormalDistribution cDistribution = cNode.getCnNormalDistribution();
		double mean = 0.0;
		if (cDistribution.getDiscreteParentList().size() > 0) {
			throw new Exception("Not implemented yet!");
		}
		
		Node node;
		// Since we are supposing we just have continuous nodes as parents.
		int ndfIndex = 0;
		mean += cDistribution.getMean(ndfIndex);
		for (int i = 0; i < cDistribution.getContinuousParentList().size(); i++) {
			node = cDistribution.getContinuousParentList().get(i);
			mean += cDistribution.getConstantAt(i, ndfIndex) * CNNDTransformation.getMean((ContinuousNode)node);
		}
		
		return mean;
	}
	
	/**
	 * http://mathworld.wolfram.com/NormalSumDistribution.html
	 * @param cNode
	 * @return
	 * @throws Exception
	 */
	public static double getVariance(ContinuousNode cNode) throws Exception {
		CNNormalDistribution cDistribution = cNode.getCnNormalDistribution();
		double variance = 0.0;
		if (cDistribution.getDiscreteParentList().size() > 0) {
			throw new Exception("Not implemented yet!");
		}
		
		Node node;
		// Since we are supposing we just have continuous nodes as parents.
		int ndfIndex = 0;
		variance += cDistribution.getVariance(ndfIndex);
		for (int i = 0; i < cDistribution.getContinuousParentList().size(); i++) {
			node = cDistribution.getContinuousParentList().get(i);
			variance += Math.pow(cDistribution.getConstantAt(i, ndfIndex), 2) * CNNDTransformation.getVariance((ContinuousNode)node);
		}
		
		return variance;
	}

}
