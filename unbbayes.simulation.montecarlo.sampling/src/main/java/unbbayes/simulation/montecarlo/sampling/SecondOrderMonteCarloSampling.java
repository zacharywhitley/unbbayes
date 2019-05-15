package unbbayes.simulation.montecarlo.sampling;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import unbbayes.io.CountCompatibleNetIO;
import unbbayes.prs.Node;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.util.DirichletSampler;


/**
 * This is a monte-carlo sampler
 * which support second order Bayes net, 
 * a Bayes net with CPTs representing a Dirichlet probability distribution
 * instead of a single conditional probability.
 * It samples a single conditional probability from the Dirichlet distribution,
 * then it uses forward sampling to calculate a RV's probability mass function. Based on its pmf, it 
 * calculates the cumulative density function. Finally, a random number between 0 and 1 is generated 
 * and the sampled state is defined by the state the random number relates based on its cdf. 
 * If a node contain positive hard evidence, the state specified by the evidence
 * will be sampled instead of sampling from the cdf.
 * @author Shou Matsumoto
 * @see unbbayes.io.CountCompatibleNetIO
 */
public class SecondOrderMonteCarloSampling extends MatrixMonteCarloSampling {

	private float initialVirtualCounts = 1f;
	
	private Map<ProbabilisticNode, Map<Integer, DirichletSampler>> dirichletCacheForParentStates 
								= new HashMap<ProbabilisticNode, Map<Integer,DirichletSampler>>();

	private boolean isToClearCacheOnStart = true;

	private float virtualCountsMultiplier = 1f;

	/**
	 * Default constructor kept visible for easy inheritance
	 * and to be compatible with plug-in architecture.
	 */
	public SecondOrderMonteCarloSampling() {}
	
	public void start(ProbabilisticNetwork pn , int nTrials, long elapsedTimeMillis) {
		
		if (isToClearCacheOnStart) {
			this.forceClearCache();
		}
		
		// run ordinary monte carlo after cache is clear
		super.start(pn, nTrials, elapsedTimeMillis);
	}
	
	/**
	 * Forces cache to be cleared
	 * @see #getDirichletCacheForParentStates()
	 */
	public void forceClearCache() {
		// clear cache
		Map<ProbabilisticNode, Map<Integer, DirichletSampler>> cache = getDirichletCacheForParentStates();
		if (cache != null) {
			cache.clear();
		}
	}

	/**
	 * Samples a single CPT from Dirichlet distributions
	 * of the second order bayes net, and then perform standard monte-carlo
	 * sampling.
	 * @see unbbayes.simulation.montecarlo.sampling.MatrixMonteCarloSampling#simulate(int)
	 */
	protected void simulate(int nTrial){
		
		int[] sampledStates = new int[samplingNodeOrderQueue.size()];
		
		for (int i = 0 ; i < samplingNodeOrderQueue.size(); i++) {
			
			ProbabilisticNode node = (ProbabilisticNode)samplingNodeOrderQueue.get(i);
			
			
			if (node.hasEvidence()) {
				// If it is an evidence node, then we do not need to sample it.
				sampledStates[i] = node.getEvidence();
			} else {
				List<Integer> parentsIndexes = getParentsIndexesInQueue(node);
				this.sampleCPT(node, this.pn, parentsIndexes, sampledStates);
				// sample based on probability mass function
				double[] pmf = getProbabilityMassFunction(sampledStates, parentsIndexes, node);
				sampledStates[i] = getState(pmf);
			}
			
			sampledStatesMatrix[nTrial][i] = (byte)sampledStates[i];
			
		}
		
	}

	/**
	 * Updates {@link ProbabilisticNode#getProbabilityFunction()}
	 * based on a sample from Dirichlet distribution
	 * specified in count tables (parameters of Dirichlet-multinomial distribution)
	 * which should be accessible from {@link unbbayes.prs.bn.ProbabilisticNetwork#getProperty(String)},
	 * with the property name being {@link CountCompatibleNetIO#DEFAULT_COUNT_TABLE_PREFIX} + {@link unbbayes.prs.INode#getName()}.
	 * @param node : node of cpt to be updated
	 * @param net : net containing node and reference to count table
	 * @param parentsIndexes : indexes of parents in the current samples. It is assumed that parents were sampled already
	 * @param sampledStates : current samples. States of parents will be referred
	 */
	protected void sampleCPT(ProbabilisticNode node, ProbabilisticNetwork net, List<Integer> parentsIndexes, int[] sampledStates) {
		
		if (net == null) {
			throw new NullPointerException("Network must be specified.");
		}
		if (node == null || node.getName() == null || node.getName().isEmpty()) {
			throw new IllegalArgumentException("Node must be specified and must have a valid name/id.");
		}

		// extract count table (i.e. dirichlet alpha parameters)
		PotentialTable countTable = (PotentialTable) net.getProperty(CountCompatibleNetIO.DEFAULT_COUNT_TABLE_PREFIX + node.getName());
		if (countTable == null) {
			throw new IllegalArgumentException("Count table not found for node " + node.getName() 
					+ ". Use network property \"" + CountCompatibleNetIO.DEFAULT_COUNT_TABLE_PREFIX + node.getName() 
					+ "\" to store count tables of node " + node
					+ " in network " + net);
		}
		
		// code assumes that the variable at index 0 of PotentialTable is the node (owner) itself, 
		// and other variables are the parents of such node.
		if (countTable.indexOfVariable(node) != 0) {
			throw new IllegalArgumentException("Current version assumes that the node which owns a potential/count table is associated with index 0 of such potential/count table,"
					+ " but index was " + countTable.indexOfVariable(node));
		}
		
		// extract cpt to overwrite with new sample
		PotentialTable table = ((ProbabilisticNode) node).getProbabilityFunction();
		
		// code assumes that indexes of variables in count table and cpt are the same 
		if (table.getVariablesSize() != countTable.getVariablesSize()) {
			throw new IllegalArgumentException("Current version assumes that the node which owns a potential/count have same number of parents, but found incompatible node " + node);
		}
		for (int i = 0; i < table.getVariablesSize(); i++) {
			if (!table.getVariableAt(i).equals(countTable.getVariableAt(i))) {
				throw new IllegalArgumentException("Current version assumes that potential/count tables have variables, "
						+ "but found incompatible table for " + node
						+ " in index " + i);
			}
		}
		
		// use a coordinate to access table, 
		// because I'll only sample conditional probability given current states of parents in sample
		int[] coord = table.getMultidimensionalCoord(0);
		for(int parentInd = 0 ; parentInd < parentsIndexes.size(); parentInd++){				
			Integer nodeIndex = parentsIndexes.get(parentInd);
			coord[table.indexOfVariable(samplingNodeOrderQueue.get(nodeIndex))] = sampledStates[nodeIndex];								
		}
		
		// initialize list to be filled with dirichlet distribution of CPT (separated by each combination of states of parent nodes)
//		List<DirichletSampler> dirichletByParentState = new ArrayList<DirichletSampler>();
		
		// iterate on cells of the count table as cell = base cell + offset (the offset is relative to states of owner node).
		// Count table contains absolute frequency (as opposed to probabilities -- relative frequencies)
		// of states of owner node given states of parents
//		for (int baseCellIndex = 0; baseCellIndex < countTable.tableSize(); ) {
//			
//			// instantiate dirichlet distribution with the counts we found in current table
//			// parameters of a dirichlet distribution are usually represented with greek alphas in the literature
//			// number of parameters of dirichlet is equal to number of states of the associated random variable
//			double[] alphas = new double[node.getStatesSize()];		
//			for (int stateOffset = 0; stateOffset < alphas.length; stateOffset++) {
//				alphas[stateOffset] = getInitialVirtualCounts() + countTable.getValue(baseCellIndex + stateOffset);
//			}
//			DirichletSampler dirichlet = new DirichletSampler(alphas);
//			dirichletByParentState.add(dirichlet);
//			
//			// skip to next cell in which the state of CPT owner (current node) is 0 again
//			baseCellIndex += node.getStatesSize();
//		}
		
		// get from cache if present
		Map<ProbabilisticNode, Map<Integer, DirichletSampler>> cache = getDirichletCacheForParentStates();
		if (cache == null) {
			cache = new HashMap<ProbabilisticNode, Map<Integer,DirichletSampler>>();
			setDirichletCacheForParentStates(cache);
		}
		Map<Integer, DirichletSampler> mapInCache = cache.get(node);
		if (mapInCache == null) {
			mapInCache = new HashMap<Integer, DirichletSampler>();
			cache.put(node, mapInCache);
		}
		int firstCellInCPTColumn = table.getLinearCoord(coord);
		DirichletSampler dirichlet = mapInCache.get(firstCellInCPTColumn);
		
		if (dirichlet == null) {
			// instantiate dirichlet distribution with the counts we found in current table
			// parameters of a dirichlet distribution are usually represented with greek alphas in the literature
			// number of parameters of dirichlet is equal to number of states of the associated random variable.
			double[] alphas = new double[node.getStatesSize()];		
			// only instantiate dirichlet for current states of parents (so only instantiate 1 dirichlet dist)
			for (int state = 0; state < alphas.length; state++) {
				// index zero of coord represents the node itself. 
				// Other indexes were set with values of parents in sample
				coord[0] = state;	
				
				alphas[state] = (getInitialVirtualCounts() + countTable.getValue(coord)) * getVirtualCountsMultiplier();
				
			}
			
			dirichlet = new DirichletSampler(alphas);
			
			// update cache
			mapInCache.put(firstCellInCPTColumn, dirichlet);
		}
		
		// iterate on number of combination of states of parent nodes 
		// (this number is equal to number of Dirichlet objects in the list)
//		for (int dirichletListIndex = 0; dirichletListIndex < dirichletByParentState.size(); dirichletListIndex++) {
//			
//			// sample a conditional probability from respective dirichlet dist
//			DirichletSampler dirichlet = dirichletByParentState.get(dirichletListIndex);
//			double[] sampleConditionalProb = dirichlet.sample();
//			
//			// corresponding cell in table is the base cell index (see code below) + offset
//			int baseCellIndex = dirichletListIndex * node.getStatesSize();
//			for (int stateOffset = 0; stateOffset < sampleConditionalProb.length; stateOffset++) {
//				// overwrite cpt by accessing cells by base + offset
//				table.setValue(baseCellIndex + stateOffset, (float)sampleConditionalProb[stateOffset]);
//			}
//			
//		}	// end of iteration for each combination of states of parent nodes
		
		// update cpt with sample of dirichlet
		double[] sampleConditionalProb = dirichlet.sample();
		for (int state = 0; state < sampleConditionalProb.length; state++) {
			// index zero of coord represents the node itself. 
			// Other indexes were set with values of parents in sample
			coord[0] = state;	
			
			table.setValue(coord, (float)sampleConditionalProb[state]);
		}
		
	}

	/**
	 * @return initial value of parameters of dirichlet distribution.
	 * The actual parameters of dirichlet distribution to be used in the sampling process 
	 * (to sample a "first order" CPT which will be used by the superclass Junction tree algorithm)
	 * will be this value plus the counts stored at {@link ProbabilisticNetwork#getProperty(String)}
	 * with name {@link CountCompatibleNetIO#DEFAULT_COUNT_TABLE_PREFIX} + {@link Node#getName()}.
	 */
	public float getInitialVirtualCounts() {
		return initialVirtualCounts;
	}

	/**
	 * @param initialVirtualCounts : initial value of parameters of dirichlet distribution.
	 * The actual parameters of dirichlet distribution to be used in the sampling process 
	 * (to sample a "first order" CPT which will be used by the superclass Junction tree algorithm)
	 * will be this value plus the counts stored at {@link ProbabilisticNetwork#getProperty(String)}
	 * with name {@link CountCompatibleNetIO#DEFAULT_COUNT_TABLE_PREFIX} + {@link Node#getName()}.
	 */
	public void setInitialVirtualCounts(float initialVirtualCounts) {
		this.initialVirtualCounts = initialVirtualCounts;
	}

	/**
	 * @return the cache of dirichlet sampler for a node 
	 * and state of parents in a sample.
	 * The integer is the 1st cell in cpt associated with a given combination
	 * of states of parents (values of such states are values in current sample).
	 * Cache is cleared in the beginning of {@link #start(ProbabilisticNetwork, int, long)}
	 * @see #sampleCPT(ProbabilisticNode, ProbabilisticNetwork, List, int[])
	 * @see #isToClearCacheOnStart()
	 */
	protected Map<ProbabilisticNode, Map<Integer, DirichletSampler>> getDirichletCacheForParentStates() {
		return dirichletCacheForParentStates;
	}

	/**
	 * @param dirichletCacheForParentStates :
	 * the cache of dirichlet sampler for a node 
	 * and state of parents in a sample.
	 * The integer is the 1st cell in cpt associated with a given combination
	 * of states of parents (values of such states are values in current sample).
	 * Cache is cleared in the beginning of {@link #start(ProbabilisticNetwork, int, long)}
	 * @see #sampleCPT(ProbabilisticNode, ProbabilisticNetwork, List, int[])
	 * @see #isToClearCacheOnStart()
	 */
	protected void setDirichletCacheForParentStates(
			Map<ProbabilisticNode, Map<Integer, DirichletSampler>> dirichletCacheForParentStates) {
		this.dirichletCacheForParentStates = dirichletCacheForParentStates;
	}

	/**
	 * @return if true, {@link #getDirichletCacheForParentStates()}
	 * will be reset at beginning of {@link #start(ProbabilisticNetwork, int, long)}
	 */
	public boolean isToClearCacheOnStart() {
		return isToClearCacheOnStart;
	}

	/**
	 * @param isToClearCacheOnStart :
	 * if true, {@link #getDirichletCacheForParentStates()}
	 * will be reset at beginning of {@link #start(ProbabilisticNetwork, int, long)}
	 */
	public void setToClearCacheOnStart(boolean isToClearCacheOnStart) {
		this.isToClearCacheOnStart = isToClearCacheOnStart;
	}

	/**
	 * @return value to be multiplied to virtual counts
	 * in order to artificially adjust variance
	 * of the Dirichlet distribution. Default is 1.
	 */
	public float getVirtualCountsMultiplier() {
		return virtualCountsMultiplier;
	}

	/**
	 * @param virtualCountsMultiplier :
	 * value to be multiplied to virtual counts
	 * in order to artificially adjust variance
	 * of the Dirichlet distribution. Default is 1.
	 */
	public void setVirtualCountsMultiplier(float virtualCountsMultiplier) {
		this.virtualCountsMultiplier = virtualCountsMultiplier;
	}

}
