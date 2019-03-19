package unbbayes.simulation.montecarlo.sampling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.log4j.Logger;

import unbbayes.prs.Node;
import unbbayes.prs.bn.IterativeSecondOrderJunctionTreeAlgorithm;
import unbbayes.prs.bn.JunctionTreeAlgorithm;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithm;
import unbbayes.util.longtask.ILongTaskProgressObserver;
import unbbayes.util.longtask.LongTaskProgressChangedEvent;

/**
 * This is a monte-carlo sampler
 * which support second order Bayes net 
 * (Bayes net with CPTs representing a probability distribution
 * instead of a single conditional probability),
 * by delegating inference to {@link IterativeSecondOrderJunctionTreeAlgorithm}.
 * The sampling process is as simple as 
 * calculating probability based on {@link IterativeSecondOrderJunctionTreeAlgorithm},
 * sampling a node based on such probability, setting evidence 
 * of a node in {@link IterativeSecondOrderJunctionTreeAlgorithm}
 * (accordingly to the value we just sampled),
 * propagate ({@link IterativeSecondOrderJunctionTreeAlgorithm#propagate()}), 
 * sample another node, and so on.
 * @author Shou Matsumoto
 * @see unbbayes.io.CountCompatibleNetIO
 */
public class IterativeSecondOrderJunctionTreeSampler implements IMonteCarloSampling {
	
	private List<ILongTaskProgressObserver> observers = new ArrayList<ILongTaskProgressObserver>();
	
	private IterativeSecondOrderJunctionTreeAlgorithm algorithm = null;
	
	private Logger logger = Logger.getLogger(getClass());

	private boolean isToAssertEvidenceBackup = false;

	private byte[][] sampledStatesMatrix;
	

	/**
	 * Default constructor kept visible for easy inheritance
	 */
	public IterativeSecondOrderJunctionTreeSampler() {}

//	/**
//	 * Default constructor initializing fields.
//	 * @param algorithm
//	 * inference algorithm to delegate inference to.
//	 */
//	public IterativeSecondOrderJunctionTreeSampler(IterativeSecondOrderJunctionTreeAlgorithm algorithm) {
//		super();
//		this.algorithm = algorithm;
//	}


	/**
	 * @return 
	 * inference algorithm to delegate inference to.
	 */
	public IterativeSecondOrderJunctionTreeAlgorithm getAlgorithm() {
		synchronized (this) {
			if (algorithm == null) {
				// use 1 iteration by default
				algorithm = new IterativeSecondOrderJunctionTreeAlgorithm(null, 1, Long.MAX_VALUE);
				algorithm.setToPropagateOriginalTableAfterSecondOrderPropagation(false);
				algorithm.addInferencceAlgorithmListener(JunctionTreeAlgorithm.BACKUP_EVIDENCES_BEFORE_RUN_RESTORE_AFTER_RUN);
			}
			return algorithm;
		}
	}

	/**
	 * @param algorithm 
	 * inference algorithm to delegate inference to.
	 */
	public void setAlgorithm(IterativeSecondOrderJunctionTreeAlgorithm algorithm) {
		synchronized (this) {
			this.algorithm = algorithm;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.util.longtask.ILongTaskProgressObservable#registerObserver(unbbayes.util.longtask.ILongTaskProgressObserver)
	 */
	public void registerObserver(ILongTaskProgressObserver observer) {
		getObservers().add(observer); 
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.util.longtask.ILongTaskProgressObservable#removeObserver(unbbayes.util.longtask.ILongTaskProgressObserver)
	 */
	public void removeObserver(ILongTaskProgressObserver observer) {
		getObservers().remove(observer); 
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.util.longtask.ILongTaskProgressObservable#notityObservers(unbbayes.util.longtask.LongTaskProgressChangedEvent)
	 */
	public void notityObservers(LongTaskProgressChangedEvent event) {
		for(ILongTaskProgressObserver observer: getObservers()){
			observer.update(event); 
		}
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.longtask.ILongTaskProgressObservable#getMaxProgress()
	 */
	@Deprecated
	public int getMaxProgress() {
//		try {
//			return getAlgorithm().getMaxIterations() * getAlgorithm().getNet().getNodeCount();
//		} catch (Exception e) {
//			getLogger().warn("Attempted to retrieve max progress before initialization of algorithm.", e);
//		}
//		return getCurrentProgress() + 1;
		
		throw new UnsupportedOperationException("Should use observables instead of this deprecated method.");
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.longtask.ILongTaskProgressObservable#getCurrentProgress()
	 */
	@Deprecated
	public int getCurrentProgress() {
//		synchronized (this) {
//			return currentProgress;
//		}

		throw new UnsupportedOperationException("Should use observables instead of this deprecated method.");
	}


	/* (non-Javadoc)
	 * @see unbbayes.util.longtask.ILongTaskProgressObservable#getPercentageDone()
	 */
	@Deprecated
	public int getPercentageDone() {
//		return Math.round((float)getCurrentProgress() / getMaxProgress() * 10000f);

		throw new UnsupportedOperationException("Should use observables instead of this deprecated method.");
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.longtask.ILongTaskProgressObservable#getCurrentProgressStatus()
	 */
	@Deprecated
	public String getCurrentProgressStatus() {
//		return currentProgressStatus;
		throw new UnsupportedOperationException("Should use observables instead of this deprecated method.");
	}
	


	/* (non-Javadoc)
	 * @see unbbayes.simulation.montecarlo.sampling.IMonteCarloSampling#getSamplingNodeOrderQueue()
	 */
	@SuppressWarnings("unchecked")
	public List<Node> getSamplingNodeOrderQueue() {
		try {
			return getAlgorithm().getNet().getNodes();
		} catch (Exception e) {
			getLogger().warn("Failed to get ordering of nodes. Probably the algorithm was not initialized yet.", e);
		}
		return Collections.EMPTY_LIST;
	}

	/* (non-Javadoc)
	 * @see unbbayes.simulation.montecarlo.sampling.IMonteCarloSampling#start(unbbayes.prs.bn.ProbabilisticNetwork, int)
	 */
	public void start(ProbabilisticNetwork pn, int nTrials) {
		
		// initial assertions
		if (pn == null) {
			throw new NullPointerException("Null probabilistic network not allowed.");
		}
		if (nTrials <= 0) {
			getLogger().warn("Skipped an attempt to start " + nTrials + " simulations.");
			return;
		}
		
		// make sure the algorithm is dealing with this network
		getAlgorithm().setNet(pn);
		
		if (isToAssertEvidenceBackup()) {
			// make sure evidences are backed up before run
			// avoid inserting duplicates
			getAlgorithm().removeInferencceAlgorithmListener(JunctionTreeAlgorithm.BACKUP_EVIDENCES_BEFORE_RUN_RESTORE_AFTER_RUN);
			getAlgorithm().addInferencceAlgorithmListener(JunctionTreeAlgorithm.BACKUP_EVIDENCES_BEFORE_RUN_RESTORE_AFTER_RUN);
		}
		
		// init/run algorithm to calculate probability;
		getAlgorithm().run();
		
		// propagate base evidences;
		getAlgorithm().propagate();
		
		// get only the probabilistic nodes in network
		List<ProbabilisticNode> probNodes = new ArrayList<ProbabilisticNode>(getAlgorithm().getNet().getNodeCount());
		for (Node node : getAlgorithm().getNet().getNodes()) {
			if (node == null 
					|| !(node instanceof ProbabilisticNode)) {
				// ignore non-probabilistic nodes
				continue;	
			}
			probNodes.add((ProbabilisticNode) node);
		}
		
		// initialize matrix that will store samples
		sampledStatesMatrix = new byte[nTrials][probNodes.size()];
		// initialize with invalid value, so that we can detect unhandled cells
		for (int i = 0; i < sampledStatesMatrix.length; i++) {
			Arrays.fill(sampledStatesMatrix[i], ((byte)-1) );
		}
		
		int currentProgress = 0;
		
		// start sampling
		for (int sampleIndex = 0; sampleIndex < nTrials; sampleIndex++) {
			
			// keep track of evidences inserted from sampling, so that we can undo them in next trial
			List<ProbabilisticNode> auxiliaryEvidenceNodes = new ArrayList<ProbabilisticNode>(probNodes.size());
			
			// sample a single node
			for (int nodeIndex = 0; nodeIndex < probNodes.size(); nodeIndex++) {
				ProbabilisticNode node = probNodes.get(nodeIndex);
				
				// check if node already have hard evidence
				int state = node.getEvidence();
				if ( ( state >= 0 )
						&& ( node.getMarginalAt(state) <= 0.00005 ) ) {
					// this is a negative evidence.
					getLogger().info("Negative evidence found for " + node + " at state " + state);
					// mark as if evidence was not present, so that we can sample normally
					state = -1;
				}
				if ( state < 0 ) {
					// node did not have evidence
					// sample a state based on mean;
					state = sample(node.getMean());
					
					getLogger().debug("Sample of node " + node + " = " + node.getStateAt(state));
					
					// set evidence of sampled node
					node.addFinding(state);
					// keep track of evidences inserted by sampling, 
					// because we need to undo them in next iteration
					auxiliaryEvidenceNodes.add(node);	
					
					// propagate new evidence
					getAlgorithm().propagate(false);
				} else {
					getLogger().debug("Evidence of node " + node + " = " + node.getStateAt(state));
				}
				
				// store sample;
				sampledStatesMatrix[sampleIndex][nodeIndex] = (byte) state;
				currentProgress++;
				
				try {
					// notify observers
					notityObservers(new LongTaskProgressChangedEvent("Sampling", (int)(( currentProgress * 10000f ) / ( nTrials * probNodes.size() ) ) ) );
				} catch (Exception e) {
					getLogger().warn("Failed to wake observers", e);
				}
			}
			
			for (ProbabilisticNode node : auxiliaryEvidenceNodes) {
				node.resetEvidence();
				node.restoreMarginal();
			}
			getAlgorithm().run();
			
		}

	}
	
	/**
	 * Returns an index of provided array 
	 * by sampling from a probability density function 
	 * of discrete states.
	 * @param dist : probability density function
	 * in which index represent a state of a discrete random variable
	 * and values represent probability density.
	 * @return sampled index in probability density function
	 */
	public int sample(double[] dist) {
		if (dist == null || dist.length <= 0) {
			throw new IllegalArgumentException("Cannot sample from null or empty distribution.");
		}
		
		// use a copy, because it will be normalized
		dist = Arrays.copyOf(dist, dist.length);
		
		// normalize it, so that it will become a density function
		normalize(dist);	
		
		// get a value from 0 to 1
		double randomNumber = new Random().nextDouble();
		
		// calculate cumulative function of dist, and at same time 
		// search for state in dist which matches with random number
		double cumulative = 0;
		for (int state = 0; state < dist.length; state++) {
			cumulative += dist[state];
			getLogger().debug("Sampling from cumulative dist. Current bin = " + state 
					+ ", prob = " + dist[state]
							+ ", cumulative = " + cumulative);
			if (cumulative >= randomNumber) {
				return state;
			}
		}
		
		throw new RuntimeException("Cumulative distribution did not end at 100%. "
				+ "This can be due to a bug or inconsistent probability distribution.");
//		return dist.length - 1;
		
	}

	/**
	 * Just an ordinary normalization method
	 */
	public void normalize(double[] dist) {
		if (dist == null || (dist.length <= 0)) {
			return;
		}
		double sum = 0;
		for (double val : dist) {
			if (val < 0) {
				throw new IllegalArgumentException("Negative value found in distribution.");
			}
			sum += val;
		}
		for (int i = 0; i < dist.length; i++) {
			dist[i] /= sum;
		}
	}

	/* (non-Javadoc)
	 * @see unbbayes.simulation.montecarlo.sampling.IMonteCarloSampling#getSampledStatesMatrix()
	 */
	public byte[][] getSampledStatesMatrix() {
		return sampledStatesMatrix;
	}

	/* (non-Javadoc)
	 * @see unbbayes.simulation.montecarlo.sampling.IMonteCarloSampling#getSampledStatesCompactMatrix()
	 */
	@Deprecated
	public byte[][] getSampledStatesCompactMatrix() {
		throw new UnsupportedOperationException("This is an unused deprecated operation not supported in this version anymore.");
//		return sampledStatesMatrix;
	}

	/* (non-Javadoc)
	 * @see unbbayes.simulation.montecarlo.sampling.IMonteCarloSampling#getStatesSetTimesSampled()
	 */
	@Deprecated
	public int[] getStatesSetTimesSampled() {
		throw new UnsupportedOperationException("This is an unused deprecated operation not supported in this version anymore.");
//		int[] ret = new int[sampledStatesMatrix.length];
//		Arrays.fill(ret, 1);
//		return ret;
	}

	/* (non-Javadoc)
	 * @see unbbayes.simulation.montecarlo.sampling.IMonteCarloSampling#getSampledStatesMap()
	 */
	@Deprecated
	public Map<Integer, Integer> getSampledStatesMap() {
		throw new UnsupportedOperationException("This is an unused deprecated operation not supported in this version anymore.");
//		Map<Integer, Integer> ret = new HashMap<Integer, Integer>();
//		int[] statesSetTimesSampled = getStatesSetTimesSampled();
//		for (int i = 0; i < statesSetTimesSampled.length; i++) {
//			ret.put(i, statesSetTimesSampled[i]);
//		}
//		return ret;
	}

	/* (non-Javadoc)
	 * @see unbbayes.simulation.montecarlo.sampling.IMonteCarloSampling#getLinearCoord(int[])
	 */
	@Deprecated
	public int getLinearCoord(int[] multidimensionalCoord) {
		throw new UnsupportedOperationException("This is an unused deprecated operation not supported in this version anymore.");
	}

	/* (non-Javadoc)
	 * @see unbbayes.simulation.montecarlo.sampling.IMonteCarloSampling#getMultidimensionalCoord(int)
	 */
	@Deprecated
	public byte[] getMultidimensionalCoord(int linearCoord) {
		throw new UnsupportedOperationException("This is an unused deprecated operation not supported in this version anymore.");
	}


	/**
	 * @return the observers
	 * of {@link #registerObserver(ILongTaskProgressObserver)}
	 * @see #removeObserver(ILongTaskProgressObserver)
	 * @see #notityObservers(LongTaskProgressChangedEvent)
	 */
	public List<ILongTaskProgressObserver> getObservers() {
		return observers;
	}

	/**
	 * @param observers 
	 * the observers
	 * of {@link #registerObserver(ILongTaskProgressObserver)}
	 * @see #removeObserver(ILongTaskProgressObserver)
	 * @see #notityObservers(LongTaskProgressChangedEvent)
	 */
	public void setObservers(List<ILongTaskProgressObserver> observers) {
		this.observers = observers;
	}

	/**
	 * @return the logger
	 */
	public Logger getLogger() {
		return logger;
	}

	/**
	 * @param logger the logger to set
	 */
	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	/**
	 * @return 
	 * if true, then {@link #start(ProbabilisticNetwork, int)}
	 * will force inclusion of {@link JunctionTreeAlgorithm#BACKUP_EVIDENCES_BEFORE_RUN_RESTORE_AFTER_RUN}
	 * to {@link #getAlgorithm()} before 
	 * initializing inference algorithm (i.e. before running {@link IInferenceAlgorithm#run()}).
	 * In other words, it will be asserted that {@link #start(ProbabilisticNetwork, int)}
	 * will backup evidences before algorithm initialization.
	 * Setting this to true isn't necessary if {@link #getAlgorithm()} already have
	 * {@link JunctionTreeAlgorithm#BACKUP_EVIDENCES_BEFORE_RUN_RESTORE_AFTER_RUN}
	 * in {@link JunctionTreeAlgorithm#getInferenceAlgorithmListeners()}.
	 */
	public boolean isToAssertEvidenceBackup() {
		return isToAssertEvidenceBackup;
	}

	/**
	 * @param isToAssertEvidenceBackup 
	 * if true, then {@link #start(ProbabilisticNetwork, int)}
	 * will force inclusion of {@link JunctionTreeAlgorithm#BACKUP_EVIDENCES_BEFORE_RUN_RESTORE_AFTER_RUN}
	 * to {@link #getAlgorithm()} before 
	 * initializing inference algorithm (i.e. before running {@link IInferenceAlgorithm#run()}).
	 * In other words, it will be asserted that {@link #start(ProbabilisticNetwork, int)}
	 * will backup evidences before algorithm initialization.
	 * Setting this to true isn't necessary if {@link #getAlgorithm()} already have
	 * {@link JunctionTreeAlgorithm#BACKUP_EVIDENCES_BEFORE_RUN_RESTORE_AFTER_RUN}
	 * in {@link JunctionTreeAlgorithm#getInferenceAlgorithmListeners()}.
	 */
	public void setToAssertEvidenceBackup(boolean isToAssertEvidenceBackup) {
		this.isToAssertEvidenceBackup = isToAssertEvidenceBackup;
	}


	

}
