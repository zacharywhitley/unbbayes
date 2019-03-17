package unbbayes.prs.bn;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.log4j.Logger;

import unbbayes.io.CountCompatibleNetIO;
import unbbayes.prs.INode;
import unbbayes.prs.Network;
import unbbayes.prs.Node;
import unbbayes.util.Debug;
import cc.mallet.types.Dirichlet;

/**
 * This is an extension to {@link JunctionTreeAlgorithm}
 * in order to support inference (calculation of joint probability and evidence propagation)
 * of second order Bayes net (Bayes net with CPTs representing a probability distribution
 * instead of a single conditional probability).
 * This version only supports discrete variables, and the higher order CPTs 
 * are expected to be count tables (parameters of Dirichlet-multinomial distribution)
 * which should be accessible from {@link unbbayes.prs.bn.ProbabilisticNetwork#getProperty(String)},
 * with the property name being {@link CountCompatibleNetIO#DEFAULT_COUNT_TABLE_PREFIX} + {@link unbbayes.prs.INode#getName()}.
 * Means and standard deviations of marginals will be stored in {@link Node#setMean(double[])}
 * and {@link Node#setStandardDeviation(double[])}.
 * @author Shou Matsumoto
 * @see CountCompatibleNetIO
 */
public class IterativeSecondOrderJunctionTreeAlgorithm extends JunctionTreeAlgorithm {

	/** 
	 * Invoking {@link #getNet()} and {@link Network#getProperty(String)} 
	 * with this parameter will return {@link Map} of {@link ProbabilisticNode} to {@link List} 
	 * (in which index is node's possible state) of {@link org.apache.commons.math3.stat.descriptive.StatisticalSummary}
	 * @see #propagate()
	 * @see Node#getStandardDeviation()
	 * @see Node#getMean()
	 */
	public static final String STATE_SUMMARY_STATISTICS_PROPERTY_NAME = IterativeSecondOrderJunctionTreeAlgorithm.class.getName()
			+ "node.state.summaryStatistics";
	
	private int maxIterations = 100;
	private long maxTimeMillis = 60000;
	
	private float initialVirtualCounts = 1;
	private boolean isToPropagateOnFirstRun = false;
	
	private Logger logger = Logger.getLogger(getClass());
	private boolean isToPropagateOriginalTableAfterSecondOrderPropagation = true;
	
	/**
	 * Default constructor made visible for easy inheritance
	 */
	public IterativeSecondOrderJunctionTreeAlgorithm() {
		super();
	}

	/**
	 * Constructor initializing fields
	 * @param net : second order Bayes net to be compiled
	 * @param maxIterations : maximum number of samples to generate. The sampling process will stop if this number of iterations
	 * or maxTimeMillis is reached.
	 * @param maxTimeMillis : maximum time (milliseconds) to wait before stopping samplings.
	 * The sampling process will stop if this time is reached, or maxIterations numbers of simulations is reached.
	 * @see #run()
	 * @see #setNet(ProbabilisticNetwork)
	 * @see #setMaxIterations(int)
	 * @see #setMaxTimeMillis(long)
	 */
	public IterativeSecondOrderJunctionTreeAlgorithm(ProbabilisticNetwork net, int maxIterations, long maxTimeMillis) {
		this();
		this.setNet(net);
		this.maxIterations = maxIterations;
		this.maxTimeMillis = maxTimeMillis;
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.JunctionTreeAlgorithm#getName()
	 */
	public String getName() {
		return getClass().getSimpleName();
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.JunctionTreeAlgorithm#getDescription()
	 */
	public String getDescription() {
		return "Second order Bayes net inference algorithm which samples a CPT and then runs Junction Tree algorithm normally.";
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.JunctionTreeAlgorithm#run()
	 */
	public void run() throws IllegalStateException {
		super.run();
		if (isToPropagateOnFirstRun) {
			this.propagate();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.JunctionTreeAlgorithm#reset()
	 */
	public void reset() {
		super.reset();
		if (isToPropagateOnFirstRun) {
			this.propagate();
		}
	}
	
	/**
	 * Performs sampling of first-order CPTs and delegate to superclass.
	 * This will be iterated {@link #getMaxIterations()} times
	 * or until {@link #getMaxTimeMillis()} is reached.
	 * @see unbbayes.prs.bn.JunctionTreeAlgorithm#propagate()
	 */
	public void propagate() {
		
		// initialize maps of objects that calculate statistics associated with each node
		Map<ProbabilisticNode, List<DescriptiveStatistics>> mapStatisticsByState = new HashMap<ProbabilisticNode, List<DescriptiveStatistics>>();
		Map<ProbabilisticNode, List<Dirichlet>> mapDirichletCPT = new HashMap<ProbabilisticNode, List<Dirichlet>>();
		
		// backup the list of evidences, because they will be reset when JunctionTreeAlgorithm#run() is called
		Map<String, Integer> evidenceMap = new HashMap<String, Integer>();
		Set<String> negativeEvidenceNodeNames = new HashSet<String>();	// name of nodes in evidenceMap whose evidences are negative (indicate evidence NOT in a given state)
		Map<String, float[]> likelihoodMap = new HashMap<String, float[]>();	// backup plan for cases when we could not create virtual nodes
		
		
		// backup original cpt values,
		// initialize statistics of marginals of each node,
		// and initialize dirichlet distribution for each parents' state in cpt
		for (Node node : getNet().getNodes()) {
			
			if (node instanceof ProbabilisticNode) {
				
				// backup cpt
				((ProbabilisticNode) node).getProbabilityFunction().copyData();
				
				// initialize statistics of marginal prob
				List<DescriptiveStatistics> statisticsByState = new ArrayList<DescriptiveStatistics>(node.getStatesSize());
				for (int state = 0; state < node.getStatesSize(); state++) {
					statisticsByState.add(new DescriptiveStatistics());
				}
				mapStatisticsByState.put((ProbabilisticNode) node, statisticsByState);
				
				// start instantiating dirichlet distribution for cells in cpt
				
				// extract count table
				PotentialTable countTable = (PotentialTable) getNet().getProperty(CountCompatibleNetIO.DEFAULT_COUNT_TABLE_PREFIX + node.getName());
				if (countTable == null) {
					getLogger().warn("Count table not found for node " + node.getName() + ". Counts will be ignored for this node.");
					continue;
				}
				
				// code block following lines below assumes that the variable at index 0 of PotentialTable is the node (owner) itself, 
				// and other variables are the parents of such node.
				if (countTable.indexOfVariable(node) != 0) {
					throw new IllegalArgumentException("Current version assumes that the node which owns a potential/count table is associated with index 0 of such potential/count table,"
													+ " but index was " + countTable.indexOfVariable(node));
				}
				
				// initialize list to be filled with dirichlet distribution of CPT (separated by each combination of states of parent nodes)
				List<Dirichlet> dirichletByParentState = new ArrayList<Dirichlet>();
				
				// iterate on cells of the count table as cell = base cell + offset (the offset is relative to states of owner node).
				// Count table contains absolute frequency (as opposed to probabilities -- relative frequencies)
				// of states of owner node given states of parents
				for (int baseCellIndex = 0; baseCellIndex < countTable.tableSize(); ) {
					
					// extract states of all nodes corresponding to current cell in table
//					int[] statesAtCurrentCell = countTable.getMultidimensionalCoord(baseCellIndex);
//					// make sure we are always at a cell corresponding to 1st state (state 0) of owner node
//					if (statesAtCurrentCell[0] != 0) {	// index 0 of this array is supposedly associated with owner node
//						throw new RuntimeException("Failed to obtain cell representing the 1st state of the node owning the CPT. This might be a bug.");
//					}
					
					// instantiate dirichlet distribution with the counts we found in current table
					// parameters of a dirichlet distribution are usually represented with greek alphas in the literature
					// number of parameters of dirichlet is equal to number of states of the associated random variable
					double[] alphas = new double[node.getStatesSize()];		
					for (int stateOffset = 0; stateOffset < alphas.length; stateOffset++) {
						alphas[stateOffset] = getInitialVirtualCounts() + countTable.getValue(baseCellIndex + stateOffset);
					}
					Dirichlet dirichlet = new Dirichlet(alphas);
					dirichletByParentState.add(dirichlet);
					
					// skip to next cell in which the state of CPT owner (current node) is 0 again
					baseCellIndex += node.getStatesSize();
				}
				
				// keep track of the dirichlet object, because we'll use it during the sampling process
				mapDirichletCPT.put((ProbabilisticNode) node, dirichletByParentState);
				
				// keep track of evidences (note: at this point we know node is a ProbabilisticNode)
				if (((ProbabilisticNode) node).hasEvidence()) {
					if (((ProbabilisticNode) node).hasLikelihood()) {
						// the following code was added here because we need current marginal to calculate soft evidence
						// Enter the likelihood as virtual nodes
						try {
							// prepare list of nodes to add soft/likelihood evidence
							List<INode> evidenceNodes = new ArrayList<INode>();
							evidenceNodes.add(node);	// the main node is the one carrying the likelihood ratio
							// if conditional soft evidence, add all condition nodes (if non-conditional, then this will add an empty list)
							evidenceNodes.addAll(this.getLikelihoodExtractor().extractLikelihoodParents(this.getNetwork(), node));
							// create the virtual node
							INode virtual = null;
							try {
								virtual = this.addVirtualNode(this.getNetwork(), evidenceNodes);
								if (virtual != null) {
									// store the hard evidence of the new virtual node, so that it can be retrieved after reset
									// hard evidence of virtual node is never a "NOT" evidence (evidence is always about a given particular state, and never about values "NOT" in a given state)
									evidenceMap.put(virtual.getName(), ((TreeVariable) virtual).getEvidence());
								}
							} catch (Exception e) {
								Debug.println(getClass(), "Could not create virtual node for " + node, e);
								// backup plan: use old routine (although it is not entirely correct)
								// backup the likelihood values
								likelihoodMap.put(node.getName(), ((ProbabilisticNode) node).getLikelihood());
								// putting in evidenceMap will mark this node as evidence (no matter what kind of evidence it is actually)
								evidenceMap.put(node.getName(), 0);	
							}
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					
					} else {
						// store hard evidence, so that it can be retrieved after reset
						int evidenceIndex = ((ProbabilisticNode) node).getEvidence();
						if (((ProbabilisticNode) node).getMarginalAt(evidenceIndex) == 0) {
							// this is a "NOT" evidence (evidence about "NOT" in a given state)
							negativeEvidenceNodeNames.add(node.getName());
						}
						evidenceMap.put(node.getName(), evidenceIndex);
					}
				}
				
			}	// end of if (node instanceof ProbabilisticNode)
			
		}	// end of iteration for each node
		
		getLogger().debug("Starting " + getMaxIterations() 
				+ " iterations of monte-carlo simulation or for " 
				+ getMaxTimeMillis() + " milliseconds.");
		
		// track start time of sampling process
		long startTime = System.currentTimeMillis();
		
		// start sampling cpts and calculating respective marginals 
		for (int iteration = 0; iteration < getMaxIterations(); iteration++) {
			// sample CPTs with dirichlet distribution;
			for (Node node : getNet().getNodes()) {
				if (node instanceof ProbabilisticNode) {
					
					// extract cpt to overwrite with new sample
					PotentialTable table = ((ProbabilisticNode) node).getProbabilityFunction();
					
					// extract dirichlet objects
					List<Dirichlet> dirichletByParentState = mapDirichletCPT.get(node);
					
					// iterate on number of combination of states of parent nodes 
					// (this number is equal to number of Dirichlet objects in the list)
					for (int dirichletListIndex = 0; dirichletListIndex < dirichletByParentState.size(); dirichletListIndex++) {
						
						// sample a conditional probability from respective dirichlet dist
						Dirichlet dirichlet = dirichletByParentState.get(dirichletListIndex);
						double[] sampleConditionalProb = dirichlet.nextDistribution();
						
						// corresponding cell in table is the base cell index (see code below) + offset
						int baseCellIndex = dirichletListIndex * node.getStatesSize();
						for (int stateOffset = 0; stateOffset < sampleConditionalProb.length; stateOffset++) {
							// overwrite cpt by accessing cells by base + offset
							table.setValue(baseCellIndex + stateOffset, (float)sampleConditionalProb[stateOffset]);
						}
						
					}	// end of iteration for each combination of states of parent nodes
					
				}	// end of if (node instanceof ProbabilisticNode)
				
			}	// end of iteration for each node
			
			// compile/propagate with sampled cpt;
			super.run();
			
			// re-insert the evidences, because the above compilation has reset the evidences;
			for (String name : evidenceMap.keySet()) {
				// if name is in negativeEvidenceNodeNames, add as negative finding. Add as normal finding otherwise
				((TreeVariable)getNet().getNode(name)).addFinding(evidenceMap.get(name), negativeEvidenceNodeNames.contains(name));

				if (likelihoodMap.containsKey(name)) {
					// if name is in likelihoodMap, this was a likelihood/soft evidence with no virtual node (the virtual node has failed)
					// so, use old routine for likelihood evidence
					((TreeVariable)getNet().getNode(name)).setMarginalProbabilities(likelihoodMap.get(name));
				}
			}
			
			// now force global consistency and propagate the evidences
			super.propagate();
			
			// update statistics of marginals of each node
			for (Node node : getNet().getNodes()) {
				if (node instanceof ProbabilisticNode) {
					// get statistics of marginal prob
					List<DescriptiveStatistics> statisticsByState = mapStatisticsByState.get(node);
					// add marginal prob of each state to respective statistics
					for (int state = 0; state < node.getStatesSize(); state++) {
						statisticsByState.get(state).addValue(((ProbabilisticNode) node).getMarginalAt(state));
					}
				}
			}
			
			// stop sampling if reached time limit
			if ((System.currentTimeMillis() - startTime) > getMaxTimeMillis()) {
				getLogger().debug("Halting iteration because max time was reached: " + (System.currentTimeMillis() - startTime));
				break;
			}
		}
		
		// restore original cpts;
		// also update the means and standard deviations of marginals of all nodes;
		for (Node node : getNet().getNodes()) {
			if (node instanceof ProbabilisticNode) {
				// restore cpt
				((ProbabilisticNode) node).getProbabilityFunction().restoreData();
				
				// this is just to make sure array of marginals are allocated
//				((ProbabilisticNode) node).initMarginalList();	// probably, this is not needed if ran after super.run()
				
				// extract statistics of marginal prob
				List<DescriptiveStatistics> statisticsByState = mapStatisticsByState.get(node);
				// allocate array of mean and standard deviation of the marginals
				double[] means = new double[statisticsByState.size()];	// for backward compatibility
				double[] stdev = new double[statisticsByState.size()];
				// fill mean and standard deviation of the marginals
				for (int state = 0; state < statisticsByState.size(); state++) {
					means[state] = statisticsByState.get(state).getMean();
					stdev[state] = statisticsByState.get(state).getStandardDeviation();
					
					
					// by default, use mean as the best estimate of marginal probability 
//					((ProbabilisticNode) node).setMarginalAt(state, (float) means[state]);
				}
				// update the values in the nodes
				node.setMean(means);	// we know this is equal to marginal, but we keep it for backward compatibility
				node.setStandardDeviation(stdev);
				
			}
		}
		
		// also backup the summary statistics
//		if (mapStatisticsByState != null 
//				&& !mapStatisticsByState.isEmpty() 
//				&& getNet() != null
//				) {
			getNet().addProperty(STATE_SUMMARY_STATISTICS_PROPERTY_NAME, mapStatisticsByState);
//		}
		
		// run propagation with original table if prompted
		if (isToPropagateOriginalTableAfterSecondOrderPropagation()) {
			super.run();

			// re-insert the evidences, because the above compilation has reset the evidences;
			for (String name : evidenceMap.keySet()) {
				// if name is in negativeEvidenceNodeNames, add as negative finding. Add as normal finding otherwise
				((TreeVariable)getNet().getNode(name)).addFinding(evidenceMap.get(name), negativeEvidenceNodeNames.contains(name));

				if (likelihoodMap.containsKey(name)) {
					// if name is in likelihoodMap, this was a likelihood/soft evidence with no virtual node (the virtual node has failed)
					// so, use old routine for likelihood evidence
					((TreeVariable)getNet().getNode(name)).setMarginalProbabilities(likelihoodMap.get(name));
				}
			}
			
			super.propagate();
		}
	}

	/**
	 * @return  maximum number of samples to generate. The sampling process will stop if this number of iterations
	 * or maxTimeMillis is reached.
	 * @see #propagate()
	 */
	public int getMaxIterations() {
		return maxIterations;
	}

	/**
	 * @param maxIterations
	 * maximum number of samples to generate. The sampling process will stop if this number of iterations
	 * or maxTimeMillis is reached.
	 * @see #propagate()
	 */
	public void setMaxIterations(int maxIterations) {
		this.maxIterations = maxIterations;
	}

	/**
	 * @return maximum time (milliseconds) to wait before stopping samplings.
	 * The sampling process will stop if this time is reached, or maxIterations numbers of simulations is reached.
	 * @see #propagate()
	 */
	public long getMaxTimeMillis() {
		return maxTimeMillis;
	}

	/**
	 * @param maxTimeMillis : maximum time (milliseconds) to wait before stopping samplings.
	 * The sampling process will stop if this time is reached, or maxIterations numbers of simulations is reached.
	 * @see #propagate()
	 */
	public void setMaxTimeMillis(long maxTimeMillis) {
		this.maxTimeMillis = maxTimeMillis;
	}

	/**
	 * @return initial value of parameters of dirichlet distribution.
	 * The actual parameters of dirichlet distribuion to be used in the sampling process 
	 * (to sample a "first order" CPT which will be used by the superclass Junction tree algorithm)
	 * will be this value plus the counts stored at {@link ProbabilisticNetwork#getProperty(String)}
	 * with name {@link CountCompatibleNetIO#DEFAULT_COUNT_TABLE_PREFIX} + {@link Node#getName()}.
	 */
	public float getInitialVirtualCounts() {
		return initialVirtualCounts;
	}

	/**
	 * @param initialVirtualCounts : initial value of parameters of dirichlet distribution.
	 * The actual parameters of dirichlet distribuion to be used in the sampling process 
	 * (to sample a "first order" CPT which will be used by the superclass Junction tree algorithm)
	 * will be this value plus the counts stored at {@link ProbabilisticNetwork#getProperty(String)}
	 * with name {@link CountCompatibleNetIO#DEFAULT_COUNT_TABLE_PREFIX} + {@link Node#getName()}.
	 */
	public void setInitialVirtualCounts(float initialVirtualCounts) {
		this.initialVirtualCounts = initialVirtualCounts;
	}

	/**
	 * @return if true, {@link #run()} and {@link #reset()} will call {@link #propagate()} at the end.
	 */
	public boolean isToPropagateOnFirstRun() {
		return isToPropagateOnFirstRun;
	}

	/**
	 * @param isToPropagateOnFirstRun : if true, {@link #run()} and {@link #reset()} will call {@link #propagate()} at the end.
	 */
	public void setToPropagateOnFirstRun(boolean isToPropagateOnFirstRun) {
		this.isToPropagateOnFirstRun = isToPropagateOnFirstRun;
	}

	public Logger getLogger() {
		return logger;
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	/**
	 * @return
	 * 		if true, then {@link #propagate()} will run ordinary propagation
	 * 		with original CPTs at the end.
	 */
	public boolean isToPropagateOriginalTableAfterSecondOrderPropagation() {
		return isToPropagateOriginalTableAfterSecondOrderPropagation;
	}

	/**
	 * @param isToPropagateOriginalTableAfterSecondOrderPropagation :
	 * 		if true, then {@link #propagate()} will run ordinary propagation
	 * 		with original CPTs at the end.
	 */
	public void setToPropagateOriginalTableAfterSecondOrderPropagation(
			boolean isToPropagateOriginalTableAfterSecondOrderPropagation) {
		this.isToPropagateOriginalTableAfterSecondOrderPropagation = isToPropagateOriginalTableAfterSecondOrderPropagation;
	}

}
