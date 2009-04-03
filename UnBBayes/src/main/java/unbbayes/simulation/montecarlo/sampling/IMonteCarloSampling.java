package unbbayes.simulation.montecarlo.sampling;

import java.util.List;
import java.util.Map;

import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNetwork;


public interface IMonteCarloSampling {
	
	/**
	 * Returns the generated sample matrix. The row represents the ith trial and the column 
	 * represents the jth node from the sampled order. The value matrix[i][j] 
	 * represents the sampled state index (respecting the node's states order) for the 
	 * jth node in the ith trial.
	 * @return The generated sample matrix.
	 */
	public byte[][] getSampledStatesMatrix();
	
	/**
	 * Returns the generated compact sample matrix. The row represents the ith sampled state set
	 * and the column represents the jth node from the sampled order. The value matrix[i][j] 
	 * represents the sampled state index (respecting the node's states order) for the 
	 * jth node in the ith sampled state set. To get the number of times the 
	 * ith set of states was sampled use <code>getStatesSetTimesSampled()</code>.
	 * @return The generated compact sample matrix.
	 */
	public byte[][] getSampledStatesCompactMatrix();
	
	/**
	 * The number of times the ith set of states was sampled. 
	 * @return The number of times the ith set of states was sampled.
	 */
	public int[] getStatesSetTimesSampled();
	
	/**
	 * Returns the generated sample map, with key = linear coord (representing the sates sampled) and 
	 * value = number of times this key was sampled.
	 * @return The generated sample map.
	 */
	public Map<Integer,Integer> getSampledStatesMap();
	
	/**
	 * Return the order the nodes are in the sampled matrix.
	 * @return The order the nodes are in the sampled matrix.
	 */
	public List<Node> getSamplingNodeOrderQueue();
	
	/**
	 * Generates the MC sample with the given size for the given probabilistic network.
	 * @param pn Probabilistic network that will be used for sampling.
	 * @param nTrials Number of trials to generate.
	 */
	public void start(ProbabilisticNetwork pn , int nTrials);
	
	/**
	 * Get the linear coordinate from the multidimensional one.
	 * LinearCoord = SumOf(StateOf[i] * FactorOf[i]), for all 
	 * possible nodes (i).
	 * 
	 * @param multidimensionalCoord Multidimensional coordinate (represented by the state for
	 * each node).
	 * @return The corresponding linear coordinate.
	 */
	public int getLinearCoord(int multidimensionalCoord[]);

	/**
	 * Get the multidimensional coordinate from the linear one.
	 * 
	 * @param linearCoord The linear coordinate.
	 * @return The corresponding multidimensional coordinate.
	 */
	public byte[] getMultidimensionalCoord(int linearCoord);

}
