/**
 * 
 */
package unbbayes.datamining.discretize.sample;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.commons.math3.distribution.TriangularDistribution;
import org.apache.log4j.Logger;

import unbbayes.datamining.datamanipulation.Attribute;
import unbbayes.datamining.datamanipulation.Instance;
import unbbayes.datamining.datamanipulation.InstanceSet;

/**
 *  Performs the inverse of discretization
 * (i.e. generates numeric samples from discretized states),
 * based on uniform distribution.
 * @author Shou Matsumoto
 */
public class UniformDistributionSampler implements ISampler {

	private String prefix= "_";
	private String splitter = "_to_";
	private String suffix = "";
	
	private InstanceSet inst;

	private Logger logger = Logger.getLogger(getClass());

	/**
	 * Default constructor kept protected for easy extension
	 */
	protected UniformDistributionSampler() {}
	
	/**
	 * Constructor initializing fields
	 * @param inst
	 */
	public UniformDistributionSampler(InstanceSet inst) {
		this();
		this.inst = new InstanceSet(inst);
	}
	


	/* (non-Javadoc)
	 * @see unbbayes.datamining.discretize.IDiscretization#discretizeAttribute(unbbayes.datamining.datamanipulation.Attribute, int)
	 */
	public void discretizeAttribute(Attribute originalAttribute, int numThresholds) throws Exception {
		
		// nothing to do if no attribute was specified
		if (originalAttribute == null) {
			return;
		}
		
		// this is the data set
		InstanceSet dataSet = getInstances();
		if (dataSet == null) {
			throw new NullPointerException("Failed to extract instance set associated with attribute " + originalAttribute.getAttributeName());
		}
		
		// basic assetions
		if (!originalAttribute.isNominal()) { 
			throw new IllegalArgumentException("Attribute not nominal");
		}
		
		// do nothing if there is no data
		if (dataSet.numInstances() == 0) {	
			return;
		}
		

		// backup values from old attribute
		List<String> dataBackup = new ArrayList<String>(dataSet.numInstances());
      	@SuppressWarnings("rawtypes")
		Enumeration enumInst = dataSet.enumerateInstances();
      	while (enumInst.hasMoreElements()) {
      		Instance instance = (Instance)enumInst.nextElement();
      		// data contains index of nominal values
      		int index = (int) instance.getValue(originalAttribute);
      		// extract the string value and add to backup
      		String value = originalAttribute.getDistinticNominalValues()[index];
			dataBackup.add(value);
		}
		
		
		// create new attribute which will substitute the one we're sampling
		Attribute newAttribute = new Attribute(
				originalAttribute.getAttributeName(), // reuse same name
				Attribute.NUMERIC, 					  // samples will be numbers
				false, 								  // not string
				dataBackup.size(), 					  // same number of data
				originalAttribute.getIndex()		  // insert to same index
			);
		
		// insert new attribute
		dataSet.setAttributeAt( newAttribute, newAttribute.getIndex() );
		
		
		// sample the numeric values accordingly to original data
		for (int dataIndex = 0; dataIndex < dataBackup.size(); dataIndex++) {
			
			// extract the original data value
      		String stateLabel = dataBackup.get(dataIndex);
      		
      		float sample = generateSample(stateLabel);
      		
      		getLogger().trace(newAttribute.getAttributeName() + ", sample = " + sample);
      		
      		// add the sample to data set
			dataSet.getInstance(dataIndex).setValue( newAttribute.getIndex(), sample);
      		
		}	// end loop for sampling
		
	}

	/**
	 * Template method to be extended by implementations
	 * that samples from distributions other than uniform
	 * @param stateLabel
	 * @return a sample from state, based on {@link #parseLowerUpperBin(String)}
	 */
	protected float generateSample(String stateLabel) {
		// parse the label in order to extract range (lower and upper bounds);
  		Entry<Float, Float> lowerUpperBin = this.parseLowerUpperBin(stateLabel);
  		
  		// sample from triangular dist based on extracted range
  		if (lowerUpperBin != null 
  				&& !( lowerUpperBin.getKey() <= 0 && lowerUpperBin.getValue() <= 0 ) ) {
  			
  			// use uniform distribution if lower/upper bins are consistent
  			getLogger().debug("Sampling from uniform distribution. Lower = " 
  					+ lowerUpperBin.getKey() 
  					+ ", upper = " + lowerUpperBin.getValue());
  			
  			// sample uniformly from lower to upper bounds
  			float range = lowerUpperBin.getValue() - lowerUpperBin.getKey();
  			
  			return lowerUpperBin.getKey() + (new Random().nextFloat() * range);
  		}
  		
  		throw new RuntimeException("Could not extract reasonable upper/lower bounds from " + stateLabel + ". Extracted: " + lowerUpperBin);
  		
	}
	
	/**
	 * It will use {@link #getPrefix()}, {@link #getSplitter()},
	 * and {@link #getSuffix()} to parse a state label and
	 * obtain lower and upper numeric values to sample from.
	 * @param state : state label
	 * @return : {@link Entry} in which {@link Entry#getKey()} is the lower
	 * bound and {@link Entry#getValue()} is the upper bound.
	 */
	protected Map.Entry<Float, Float> parseLowerUpperBin(String state) {
		getLogger().trace("Parsing state " + state);
		
		// remove prefix
		if (state.startsWith(getPrefix())) {
			state = state.substring(getPrefix().length());
			getLogger().debug("Removed prefix " + getPrefix() + ". Result: " + state);
		}
		
		// remove suffix
		if (state.endsWith(getSuffix())) {
			state = state.substring(0, state.length() - getSuffix().length());
			getLogger().debug("Removed suffix " + getSuffix() + ". Result: " + state);
		}
		
		// split to 2 substrings with splitter
		String[] split = state.split(getSplitter());
		
		// parse the substrings
		float lower = Float.MAX_VALUE;
		float upper = Float.MIN_VALUE;
		try {
			lower = Float.parseFloat(split[0]);
			upper = lower;
			if (split.length >= 2 ) {
				upper = Float.parseFloat(split[1]);
			}
		} catch (Exception e) {
			getLogger().warn("Could not parse " + state + " with splitter " + getSplitter());
			getLogger().warn("Obtained split was: ");
			for (String string : split) {
				getLogger().warn(string);
			}
		}
		
		getLogger().trace("Parsed " + state + ". Lower = " + lower + ", upper = " + upper);
		
		return Collections.singletonMap(lower, upper).entrySet().iterator().next();
	}

	/* (non-Javadoc)
	 * @see unbbayes.datamining.discretize.IDiscretization#getName()
	 */
	public String getName() {
		return "Uniform distribution";
	}

	/* (non-Javadoc)
	 * @see unbbayes.datamining.discretize.sample.ISampler#getPrefix()
	 */
	public String getPrefix() {
		return prefix;
	}

	/* (non-Javadoc)
	 * @see unbbayes.datamining.discretize.sample.ISampler#setPrefix(java.lang.String)
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	/* (non-Javadoc)
	 * @see unbbayes.datamining.discretize.sample.ISampler#getSplitter()
	 */
	public String getSplitter() {
		return splitter;
	}

	/* (non-Javadoc)
	 * @see unbbayes.datamining.discretize.sample.ISampler#setSplitter(java.lang.String)
	 */
	public void setSplitter(String splitter) {
		this.splitter = splitter;
	}

	/* (non-Javadoc)
	 * @see unbbayes.datamining.discretize.sample.ISampler#getSuffix()
	 */
	public String getSuffix() {
		return suffix;
	}

	/* (non-Javadoc)
	 * @see unbbayes.datamining.discretize.sample.ISampler#setSuffix(java.lang.String)
	 */
	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.datamining.discretize.IDiscretization#getInstances()
	 */
	public InstanceSet getInstances() {
		return inst;
	}
	
	/**
	 * Set the instance set
	 * @param inst
	 * @see #getInstances()
	 */
	protected void setInstances(InstanceSet inst) {
		this.inst = inst;
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

}
