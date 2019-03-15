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
 * Discretizer that will generate bins in following pattern:
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
public class FrequencyDiscretizationWithZero extends FrequencyDiscretization {
	

	private NumberFormat numberFormatter = null;
	private String binSplitter = "_to_";

	/**
	 * @param inst
	 */
	public FrequencyDiscretizationWithZero(InstanceSet inst) {
		super(inst);
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.datamining.discretize.FrequencyDiscretization#discretizeAttribute(unbbayes.datamining.datamanipulation.Attribute, int)
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
		
		
		// create new attribute which will substitute the one we're discretizing
		int attributePosition = att.getIndex();
		Attribute newAttribute = new Attribute(att.getAttributeName(),
					Attribute.NOMINAL,
					true,
					numThresholds,
					attributePosition
				);
		
		// extract values from old attribute
		float[] sortedValues = new float[dataSet.numInstances()];
      	Enumeration enumInst = dataSet.enumerateInstances();
      	for (int i = 0; enumInst.hasMoreElements(); i++) {
      		Instance instance = (Instance)enumInst.nextElement();
      		sortedValues[i] = instance.getValue(att);
		}
      	
		// backup original values, because we need to keep ordering when re-adding new discretized data
		float[] originalValuesBkp = new float[dataSet.numInstances()];
		System.arraycopy( sortedValues, 0, originalValuesBkp, 0, sortedValues.length );
		
		// sort values in ascending order and also get array of sum of equal distinct values
		Arrays.sort(sortedValues);
		
		// check that it's non-negative
		if (sortedValues[0] < 0) {
			throw new IllegalArgumentException("This discretization method assumes non-negative numbers, but found " + sortedValues[0]);
		}
		
		// get indexes that start distinct values in the sorted data
		List<Integer> distinctValuesStartIndex = new ArrayList<Integer>(sortedValues.length);
		distinctValuesStartIndex.add(0);	// 1st value is always distinct from "previous" (because there's no such previous)
		for (int i = 1; i < sortedValues.length; i++) {
			if (sortedValues[i] != sortedValues[i-1]) {
				distinctValuesStartIndex.add(i);
			}
		}
		
		// extract number of distinct values 
		int numDistinctValues = distinctValuesStartIndex.size();
		if (numThresholds > numDistinctValues) {
			throw new Exception("Number of thresholds is larger than number of distinct values in the data. Number of distinct values = " + numDistinctValues); 
		}
		
		// extract number of repetitions each distinct values have
		List<Integer> distinctValuesCount = new ArrayList<Integer>(distinctValuesStartIndex.size());
		for (int i = 0; i < distinctValuesStartIndex.size() - 1; i++) {
			// Values between distinct values are repetitions. 
			// Therefore, we just need to count how many values there are between distinct values
			distinctValuesCount.add(distinctValuesStartIndex.get(i+1) - distinctValuesStartIndex.get(i));
		}
		// the last distinct value repeats until the end of the sorted data
		distinctValuesCount.add(sortedValues.length - distinctValuesStartIndex.get( distinctValuesStartIndex.size() - 1 ) );
		// these lists should be now synchronized by index
		if (distinctValuesCount.size() != distinctValuesStartIndex.size()) {
			throw new RuntimeException("Index of distinct values and list of their counts should have same size. Distinct values was " 
								+ distinctValuesStartIndex.size() + ", counts were " + distinctValuesCount.size()
								+ ". This is probably a bug.");
		}
		
		// if 1st distinct value is zero, then we have zeros in the data
		boolean hasZero = sortedValues[distinctValuesStartIndex.get(0)] == 0f;
		
		// 1st bin is reserved for the zero, so we need this much thresholds (bins) for other values
//		int numThresholdWithout0 = numThresholds;
//		if (hasZero) {
//			numThresholdWithout0 -= 1;
//		}
		
		// count how many non-zero values we have
		int numNonZeroValues = sortedValues.length;
		if (hasZero) {
			numNonZeroValues -= distinctValuesCount.get(0);	// 1st index contain counts of zeros
		}
		
		// each non-zero bin should contain this number of elements
		int expectedCountsPerBin = Math.round((float)numNonZeroValues / (numThresholds - 1));	// -1 in order to count for the 0 bin
        
		// list of inclusive breakpoint. E.g. if interval is X1toX2, then it will contain index to X2.
		List<Integer> breakpointIndices = new ArrayList<Integer>(numThresholds);
		if (hasZero) {
			// add zero as 1st breakpoint by default
			breakpointIndices.add(distinctValuesStartIndex.get(0));
		}
		// iterate on other (non-zero) values in order to determine other breakpoints
		for (int i = (hasZero?1:0), cumulativeCount = 0; i < distinctValuesStartIndex.size(); i++) {
			// these lists are synchronized by index
			cumulativeCount += distinctValuesCount.get(i);
			if ( ( cumulativeCount >= expectedCountsPerBin )
					|| ( i == ( distinctValuesStartIndex.size() - 1 ) ) ) {
				// add breakpoint if number of elements in current bin reached the expected counts
				// also force breakpoint if this is the last value
				breakpointIndices.add(distinctValuesStartIndex.get(i));
				cumulativeCount = 0;	// reset counts
			}
		}
		
		// assertion. Breakpoint will not contain zero if data did not contain zero
		if (breakpointIndices.size() != (numThresholds - (hasZero?0:1))) {
			throw new RuntimeException("Number of breakpoints generated with non-supervised discretization was " 
								+ (breakpointIndices.size() + (hasZero?0:1)) + ", but required number was " + (numThresholds)
								+ ". Please, reduce the number of thresholds.");
		}
		
		// formatter to be used for numbers
		NumberFormat formatter = getNumberFormatter();
		
		// add discrete states based on breakpoints
		// handle zero as a special bin
		float previousBinNumber = 0f;
		newAttribute.addValue(formatter.format(previousBinNumber));
		// build labels of discrete bins
		// breakpoint[0], breakpoint[0]-to-breakpoint[1], breakpoint[1]-to-breakpoint[2], ...
		for (int i = (hasZero?1:0); i < breakpointIndices.size(); i++) {
			// extract value referred by the breakpoint
			float currentBinNumber = sortedValues[breakpointIndices.get(i)];
			// this bin starts from previous number and goes until current number (inclusive)
			newAttribute.addValue( formatter.format(previousBinNumber) + getBinSplitter() + formatter.format(currentBinNumber));
			// next iteration will go from current bin number to next bin number.
			previousBinNumber = currentBinNumber;
		}

        
		// insert attribute
		dataSet.setAttributeAt( newAttribute, attributePosition );
		
		// insert the discretized values accordingly to original data
		for (int dataIndex = 0; dataIndex < dataSet.numInstances(); dataIndex++) {	
			
			// original value to be substituted with discretized value
			float originaValue = originalValuesBkp[dataIndex];
			
			// find first breakpoint that includes the original value
			for (int breakIndex = 0; breakIndex < breakpointIndices.size(); breakIndex++) {
				// breakpoints are sorted in ascending order, 
				// so 1st breakpoint larger than or equal to original value is the bin we want
				if ( sortedValues[breakpointIndices.get(breakIndex)] >= originaValue ) {	
					// if there is no zero values in data, breakIndex will be pointing only to non-zero bins
					// so we need to add 1 to index considering that index 0 represents zeros in the attribute
					dataSet.getInstance(dataIndex).setValue( attributePosition, (breakIndex + (hasZero?0:1)) );
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
