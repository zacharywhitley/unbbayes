package unbbayes.datamining.discretize.sample;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.log4j.Logger;

import unbbayes.datamining.datamanipulation.Attribute;
import unbbayes.datamining.datamanipulation.Instance;
import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.util.DefaultStateIntervalParser;
import unbbayes.util.IStateIntervalParser;

/**
 *  Performs the inverse of discretization
 * (i.e. generates numeric samples from discretized states),
 * based on uniform distribution.
 * @author Shou Matsumoto
 */
public class UniformDistributionSampler implements ISampler {

	private IStateIntervalParser stateIntervalParser = new DefaultStateIntervalParser();
	
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
  		Entry<Float, Float> lowerUpperBin = getStateIntervalParser().parseLowerUpperBin(stateLabel);
  		
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
	

	/* (non-Javadoc)
	 * @see unbbayes.datamining.discretize.IDiscretization#getName()
	 */
	public String getName() {
		return "Uniform distribution";
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

	/**
	 * @return the stateIntervalParser
	 */
	public IStateIntervalParser getStateIntervalParser() {
		return this.stateIntervalParser;
	}

	/**
	 * @param stateIntervalParser the stateIntervalParser to set
	 */
	public void setStateIntervalParser(IStateIntervalParser stateIntervalParser) {
		this.stateIntervalParser = stateIntervalParser;
	}

}
