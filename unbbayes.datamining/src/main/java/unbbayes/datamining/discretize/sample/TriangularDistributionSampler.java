package unbbayes.datamining.discretize.sample;

import java.util.Map.Entry;

import org.apache.commons.math3.distribution.TriangularDistribution;

import unbbayes.datamining.datamanipulation.InstanceSet;

/**
 * Performs the inverse of discretization
 * (i.e. generates numeric samples from discretized states),
 * based on triangular distribution.
 * @author Shou Matsumoto
 */
public class TriangularDistributionSampler extends UniformDistributionSampler {


	/**
	 * Default constructor kept protected for easy extension
	 */
	protected TriangularDistributionSampler() {}
	
	/**
	 * Constructor initializing fields
	 * @param inst
	 */
	public TriangularDistributionSampler(InstanceSet inst) {
		super(inst);
	}
	
	/**
	 * Sample from triangular distribution rather than uniform distribution.
	 * @see unbbayes.datamining.discretize.sample.UniformDistributionSampler#generateSample(java.lang.String)
	 */
	@Override
	protected float generateSample(String stateLabel) {
		// parse the label in order to extract range (lower and upper bounds);
  		Entry<Float, Float> lowerUpperBin = getStateIntervalParser().parseLowerUpperBin(stateLabel);
  		
  		// sample from triangular dist based on extracted range
  		if (lowerUpperBin != null ) {
  			if (lowerUpperBin.getKey() <= 0 && lowerUpperBin.getValue() <= 0 ) {
  				return 0f;
  			}
  			
  			// use triangular distribution if lower/upper bins are consistent
  			getLogger().debug("Sampling from triangular distribution. Lower = " 
  					+ lowerUpperBin.getKey() 
  					+ ", upper & mode = " + lowerUpperBin.getValue());
  			
  			TriangularDistribution distribution = new TriangularDistribution (
  					lowerUpperBin.getKey(), 	// lower bound
  					lowerUpperBin.getValue(), 	// upper bound
  					lowerUpperBin.getValue() 	// use upper bound as mode
					);
  			
  			return (float)distribution.sample();
  		}
  		
  		throw new RuntimeException("Could not extract reasonable upper/lower bounds from " + stateLabel + ". Extracted: " + lowerUpperBin);
  		
	}

	/* (non-Javadoc)
	 * @see unbbayes.datamining.discretize.IDiscretization#getName()
	 */
	public String getName() {
		return "Triangular distribution";
	}


}
