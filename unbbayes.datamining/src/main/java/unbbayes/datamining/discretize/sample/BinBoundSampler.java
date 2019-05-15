package unbbayes.datamining.discretize.sample;

import java.util.Map.Entry;
import java.util.Random;

import unbbayes.datamining.datamanipulation.InstanceSet;

/**
 * Performs the inverse of discretization
 * (i.e. generates numeric samples from discretized states),
 * by sampling either the upper or lower bound
 * of the bins.
 * @author Shou Matsumoto
 *
 */
public class BinBoundSampler extends UniformDistributionSampler {

	private float lowerBinSampleProb = 0f;
	
	private Random rand = new Random();
	
	/**
	 * Default constructor kept protected for easy extension
	 */
	protected BinBoundSampler() {}
	
	/**
	 * Constructor initializing fields
	 * @param inst
	 */
	public BinBoundSampler(InstanceSet inst) {
		super(inst);
	}
	

	/**
	 * Sample upper or lower bin rather than uniform distribution.
	 * @see unbbayes.datamining.discretize.sample.UniformDistributionSampler#generateSample(java.lang.String)
	 */
	@Override
	protected float generateSample(String stateLabel) {
		// parse the label in order to extract range (lower and upper bounds);
  		Entry<Float, Float> lowerUpperBin = getStateIntervalParser().parseLowerUpperBin(stateLabel);
  		
  		// sample from triangular dist based on extracted range
  		if (lowerUpperBin != null ) {
  			if (lowerUpperBin.getKey() >= lowerUpperBin.getValue()) {
  				// this should also handle interval [0,0]
  				return lowerUpperBin.getKey();
  			}
  			
  			// use triangular distribution if lower/upper bins are consistent
  			getLogger().debug("Sampling upper or lower bin. Lower = " 
  					+ lowerUpperBin.getKey() 
  					+ ", upper = " + lowerUpperBin.getValue()
  					+ ", probability of picking upper bin = " + getLowerBinSampleProb());
  			
  			if (getRandom().nextFloat() < getLowerBinSampleProb()) {
  				// return lower bin
  				return lowerUpperBin.getKey();
  			} else {
  				// return upper bin
  				return lowerUpperBin.getValue();
  			}
  			
  		}
  		
  		throw new RuntimeException("Could not extract reasonable upper/lower bounds from " + stateLabel + ". Extracted: " + lowerUpperBin);
  		
	}

	/* (non-Javadoc)
	 * @see unbbayes.datamining.discretize.IDiscretization#getName()
	 */
	public String getName() {
		return "Bin corner";
	}

	/**
	 * @return
	 * Probability of sampling the lower bin rather than upper bin.
	 * Set this value to something between 0 and 1.
	 * If 0, all samples will be from upper bound.
	 * If 1, all samples will be from lower bound.
	 */
	public float getLowerBinSampleProb() {
		return lowerBinSampleProb;
	}

	/**
	 * @param lowerBinSampleProb 
	 * Probability of sampling the lower bin rather than upper bin.
	 * Set this value to something between 0 and 1.
	 * If 0, all samples will be from upper bound.
	 * If 1, all samples will be from lower bound.
	 */
	public void setLowerBinSampleProb(float lowerBinSampleProb) {
		this.lowerBinSampleProb = lowerBinSampleProb;
	}

	/**
	 * @return random number generator used
	 * for sampling upper or lower bin.
	 * @see #getLowerBinSampleProb()
	 */
	public Random getRandom() {
		return rand;
	}

	/**
	 * @param rand : random number generator used
	 * for sampling upper or lower bin.
	 * @see #getLowerBinSampleProb()
	 */
	public void setRandom(Random rand) {
		this.rand = rand;
	}


}
