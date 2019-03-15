/**
 * 
 */
package unbbayes.datamining.discretize;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import unbbayes.datamining.datamanipulation.Attribute;
import unbbayes.datamining.datamanipulation.Instance;
import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.datamanipulation.Utils;

/**
 * 
 * Range discretization that will generate bins in following pattern:
 * <br/>
 * <br/>
 * 0, 0toX1, X1toX2, ... , Xn-1toXn.
 * <br/>
 * <br/>
 * For the bin X1toX2, X1 is non-inclusive, and X2 is inclusive.
 * The first bin will always be "0.0".
 * This class expects data to be non-negative.
 * @author Shou Matsumoto
 */
public class RangeDiscretizationWithZero extends RangeDiscretization {
	

	private NumberFormat numberFormatter = null;
	private String binSplitter = "_to_";

	/**
	 * @param inst
	 */
	public RangeDiscretizationWithZero(InstanceSet inst) {
		super(inst);
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.datamining.discretize.RangeDiscretization#discretizeAttribute(unbbayes.datamining.datamanipulation.Attribute, int)
	 */
	public void discretizeAttribute(Attribute att,int numThresholds) throws Exception {	
		
		// nothing to do if no attribute was specified
		if (att == null) {
			return;
		}
		
		// do not consider the case in which number of bins is 1 or less (because we need at least a bin for 0, and another bin for the rest	)
		if (numThresholds <= 1) {
			super.discretizeAttribute(att, numThresholds);
		}
		
		// this is the data set
		InstanceSet dataSet = getInstances();
		if (dataSet == null) {
			throw new NullPointerException("Failed to extract instance set associated with attribute " + att.getAttributeName());
		}
		
		// basic assetions
		if (!att.isNumeric()) { 
			throw new IllegalArgumentException("Attribute not numeric");
		}
		
		// do nothing if there is no data
		if (dataSet.numInstances() == 0) {	
			return;
		}
		

		// backup values from old attribute
		float[] dataBackup = new float[dataSet.numInstances()];
      	@SuppressWarnings("rawtypes")
		Enumeration enumInst = dataSet.enumerateInstances();
      	for (int i = 0; enumInst.hasMoreElements(); i++) {
      		Instance instance = (Instance)enumInst.nextElement();
      		dataBackup[i] = instance.getValue(att);
		}
		
		
		// create new attribute which will substitute the one we're discretizing
		int attributePosition = att.getIndex();
		Attribute newAttribute = new Attribute(att.getAttributeName(),
					Attribute.NOMINAL,
					true,
					numThresholds,
					attributePosition
				);
		
		// get min and max in range
		float min = Float.MAX_VALUE;
		float max = Float.MIN_VALUE;
		for (int i = 0; i < dataBackup.length; i++) {
      		float dataValue = dataBackup[i];
      		if (dataValue > max) {
				max = dataValue;
			}
			if (dataValue < min) {
				min = dataValue;
			}
		}
      	
      	if (min < 0) {
      		throw new UnsupportedOperationException("This method only supports non-negative numbers.");
      	}
      	if (min > max) {
      		throw new RuntimeException("Data overflow or underflow detected. This version only supports 32bit float numbers as data.");
      	}
		
		
		// size of each bin should be proportional to distance between min and max
      	float sizeBin = (max - min)/(numThresholds - 1);	// -1 because there will be a special bin for zero
		
      	
		// formatter to be used for numbers
		NumberFormat formatter = getNumberFormatter();
		
		// add discrete states based on breakpoints
		
		// this will keep track of what were the breaks.
		// This will be used later to retrieve the index of the bin from a data value.
		List<Float> upperBounds = new ArrayList<Float>(numThresholds);
		
		// handle zero as a special bin
		newAttribute.addValue(formatter.format(0f));
		upperBounds.add(0f);
		
		// create breakpoints from min to max
		for (float currentBreak = sizeBin, previousBreak = 0f; currentBreak <= max; currentBreak += sizeBin) {
//			if (currentBreak <= 0f) {
//				// zero was handled already
//				continue;
//			}
			
			// this bin starts from previous number and goes until current number (inclusive)
			newAttribute.addValue( formatter.format(previousBreak) + getBinSplitter() + formatter.format(currentBreak) );
			upperBounds.add(currentBreak);
			
			// next iteration will go from current bin number to next bin number.
			previousBreak = currentBreak;
		}
		
		// insert attribute
		dataSet.setAttributeAt( newAttribute, attributePosition );
		
		// insert the discretized values accordingly to original data
		for (int dataIndex = 0; dataIndex < dataBackup.length; dataIndex++) {
			
			// extract the original data value
      		float dataValue = dataBackup[dataIndex];
      		
      		// zeros should always go to 1st bin (bin at index 0)
      		if (dataValue <= 0f) {
      			dataSet.getInstance(dataIndex).setValue( attributePosition, 0 );
      			continue;
      		}

			// find first breakpoint that includes the original value
      		for ( int binIndex = 0; binIndex < upperBounds.size(); binIndex++ ) {
      			// breakpoints are sorted in ascending order, 
				// so 1st breakpoint larger than or equal to original value is the bin we want
				if ( upperBounds.get(binIndex) >= dataValue ) {
					dataSet.getInstance(dataIndex).setValue( attributePosition, binIndex );
					break;
				}
			}
      		
		}
		
	}

	/**
	 * @return formatter used in {@link #discretizeAttribute(Attribute, int)}
	 * in order to generate a label of the discrete bin in a format X1toX2.
	 * In this case, the formatter will be applied to X1 and X2.
	 */
	public NumberFormat getNumberFormatter() {
		synchronized (this) {
			if (numberFormatter == null) {
				DecimalFormatSymbols dfs = new DecimalFormatSymbols();
				dfs.setDecimalSeparator('.');
				numberFormatter = new DecimalFormat("0.00#", dfs);
			}
			return numberFormatter;
		}
	}

	/**
	 * @param numberFormatter 
	 * formatter used in {@link #discretizeAttribute(Attribute, int)}
	 * in order to generate a label of the discrete bin in a format X1toX2.
	 * In this case, the formatter will be applied to X1 and X2.
	 */
	public void setNumberFormatter(NumberFormat numberFormatter) {
		this.numberFormatter = numberFormatter;
	}

	/**
	 * @return 
	 * 			String to be used to split two numbers in the name of discretized states.
	 * 			For instance, the discretized states will have names in the format
	 * 			X1toX2 in which X1 and X2 are numbers formatted with {@link #getNumberFormatter()}
	 * 			and "to" is the string returned by this method.
	 * @see #discretizeAttribute(Attribute)
	 */
	public String getBinSplitter() {
		return binSplitter;
	}

	/**
	 * @param binSplitter 
	 * 			String to be used to split two numbers in the name of discretized states.
	 * 			For instance, the discretized states will have names in the format
	 * 			X1toX2 in which X1 and X2 are numbers formatted with {@link #getNumberFormatter()}
	 * 			and "to" is the string returned by this method.
	 * @see #discretizeAttribute(Attribute)
	 */
	public void setBinSplitter(String binSplitter) {
		this.binSplitter = binSplitter;
	}

}
