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
import unbbayes.learning.ConstructionController;

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
	private String binPrefix = "_";
	private String missingValueToken = ConstructionController.DEFAULT_MISSING_VALUE_TOKEN;

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
		// also make a backup of original values, because we need to keep ordering when re-adding new discretized data
		float[] originalValuesBkp = new float[dataSet.numInstances()];
      	Enumeration enumInst = dataSet.enumerateInstances();
      	for (int i = 0; enumInst.hasMoreElements(); i++) {
      		Instance instance = (Instance)enumInst.nextElement();
      		originalValuesBkp[i] = instance.getValue(att);
      		// check that it's non-negative
    		if (originalValuesBkp[i]  < 0) {
    			throw new IllegalArgumentException("This discretization method assumes non-negative numbers, but found " + originalValuesBkp[i] + " at entry " + i);
    		}
      		sortedValues[i] = originalValuesBkp[i];
      		if (Float.isNaN(sortedValues[i])) {
      			// temporary substitute with negative value, so that it comes in the beggining of sorted data
      			sortedValues[i] = Float.NEGATIVE_INFINITY;
      		}
		}
      	
		
		// sort values in ascending order and also get array of sum of equal distinct values
		Arrays.sort(sortedValues);
		
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
		
		// if 1st distinct value is negative infinite, then we have negative infinite in the data
		int missingValueStartIndex = -1;
		for (int distinctIndex = 0; distinctIndex < distinctValuesStartIndex.size(); distinctIndex++) {
			if (sortedValues[distinctValuesStartIndex.get(distinctIndex)] == Float.NEGATIVE_INFINITY) {
				missingValueStartIndex = distinctIndex;
				break;
			} else if (sortedValues[distinctValuesStartIndex.get(distinctIndex)] > 0f) {
				// data does not contain the value
				break;
			}
		}
		boolean hasMissingValue = (missingValueStartIndex >= 0);
		
		// check if data has zeros
		int zeroStartIndex = -1;
		for (int distinctIndex = missingValueStartIndex + 1; distinctIndex < distinctValuesStartIndex.size(); distinctIndex++) {
			if (sortedValues[distinctValuesStartIndex.get(distinctIndex)] == 0f) {
				zeroStartIndex = distinctIndex;
				break;
			} else if (sortedValues[distinctValuesStartIndex.get(distinctIndex)] > 0f) {
				// we got a value larger than 0 in a sorted list before finding zero. So, data has no zeros
				break;
			}
		}
		boolean hasZero = zeroStartIndex >= 0;
		
		// 1st bin is reserved for the zero, so we need this much thresholds (bins) for other values
//		int numThresholdWithout0 = numThresholds;
//		if (hasZero) {
//			numThresholdWithout0 -= 1;
//		}
		
		// count how many non-zero and non-negative-infinite values we have
		int numOrdinaryValues = sortedValues.length;
		if (missingValueStartIndex >= 0) {
			numOrdinaryValues -= distinctValuesCount.get(missingValueStartIndex);	// this index contain counts of missing values
		}
		if (zeroStartIndex >= 0) {
			numOrdinaryValues -= distinctValuesCount.get(zeroStartIndex);	// this index contain counts of zeros
		}
		
		// each non-zero bin should contain this number of elements
		// -1 in order to count for the 0 bin (always present).
		// - ((missingValueStartIndex >= 0)?1:0) to count for missing values' bin if present
		int expectedCountsPerBin = (int)((float)numOrdinaryValues / (numThresholds - 1f - (hasMissingValue?1:0) ) );	
        
		// list of inclusive breakpoint. E.g. if interval is X1toX2, then it will contain index to X2.
		List<Integer> breakpointIndices = new ArrayList<Integer>(numThresholds);
		if (hasMissingValue) {
			// add missing value as breakpoint by default
			breakpointIndices.add(distinctValuesStartIndex.get(missingValueStartIndex));
		}
		if (hasZero) {
			// add zero as breakpoint by default
			breakpointIndices.add(distinctValuesStartIndex.get(zeroStartIndex));
		}
		// iterate on other (non-zero) values in order to determine other breakpoints.
		// non-zero ordinary values start right after the index of zero values
		for (int i = (hasZero?1:0) + (hasMissingValue?1:0) , cumulativeCount = 0; i < distinctValuesStartIndex.size(); i++) {
			// these lists are synchronized by index
			cumulativeCount += distinctValuesCount.get(i);
			if ( ( cumulativeCount >= expectedCountsPerBin )
					|| ( i == ( distinctValuesStartIndex.size() - 1 ) ) ) {
				// add breakpoint if number of elements in current bin reached the expected counts
				// also force breakpoint if this is the last value
				breakpointIndices.add(distinctValuesStartIndex.get(i));
				cumulativeCount = 0;	// reset counts
			}
			if (breakpointIndices.size() == (numThresholds - (hasZero?0:1) - (hasMissingValue?0:1))) {
				// reached desired number of threshold.
				// Include rest of data to last bin
				breakpointIndices.set(breakpointIndices.size() -1, 
						distinctValuesStartIndex.get(distinctValuesStartIndex.size() - 1));
				break;
			}
		}
		
		// assertion. Breakpoint will not contain zero if data did not contain zero
//		if (breakpointIndices.size() != (numThresholds - (hasZero?0:1))) {
//			throw new RuntimeException("Number of breakpoints generated with non-supervised discretization was " 
//								+ (breakpointIndices.size() + (hasZero?0:1)) + ", but required number was " + (numThresholds)
//								+ ". Please, change the number of thresholds.");
//		}
		
		// formatter to be used for numbers
		NumberFormat formatter = getNumberFormatter();
		
		// add discrete states based on breakpoints
		// handle missing value as a special bin
		if (hasMissingValue) {
			newAttribute.addValue(getMissingValueToken());
		}
		// handle zero as a special bin and bin to start frequency discretization
		float previousBinNumber = 0f;
		newAttribute.addValue(getBinPrefix() + formatter.format(previousBinNumber));
		// build labels of discrete bins
		// breakpoint[0], breakpoint[0]-to-breakpoint[1], breakpoint[1]-to-breakpoint[2], ...
		// start from breakpoint of ordinary values (i.e. after missing value and zero)
		for (int i = (hasMissingValue?1:0) + (hasZero?1:0); i < breakpointIndices.size(); i++) {
			// extract value referred by the breakpoint
			float currentBinNumber = sortedValues[breakpointIndices.get(i)];
			// this bin starts from previous number and goes until current number (inclusive)
			newAttribute.addValue( getBinPrefix() + formatter.format(previousBinNumber) + getBinSplitter() + formatter.format(currentBinNumber));
			// next iteration will go from current bin number to next bin number.
			previousBinNumber = currentBinNumber;
		}

        
		// insert attribute
		dataSet.setAttributeAt( newAttribute, attributePosition );
		
		// insert the discretized values accordingly to original data
		for (int dataIndex = 0; dataIndex < dataSet.numInstances(); dataIndex++) {	
			
			// original value to be substituted with discretized value
			float originaValue = originalValuesBkp[dataIndex];
			
			if (Float.isNaN(originaValue)) {
				// this is a missing value
				if (!hasMissingValue) {
					// inconsistency
					throw new RuntimeException("Found missing value in data which was not detected previously during discretization process. This can be caused in multithreading.");
				}
				// missing value is supposedly the 1st bin
				dataSet.getInstance(dataIndex).setValue( attributePosition, breakpointIndices.get(0) );
			} else {
				// find first breakpoint that includes the original value
				// missing value should not be compared, so ignore from comparison (by starting from index 1 if missing values are present)
				for (int breakIndex = (hasMissingValue?1:0); breakIndex < breakpointIndices.size(); breakIndex++) {
					// breakpoints are sorted in ascending order, 
					// so 1st breakpoint larger than or equal to original value is the bin we want
					if ( sortedValues[breakpointIndices.get(breakIndex)] >= originaValue ) {	
						// if there is no zero values in data, breakIndex will be pointing only to non-zero bins
						// so we need to add 1 to index considering that index 0 is always present and represents zeros in the attribute
						dataSet.getInstance(dataIndex).setValue( attributePosition, (breakIndex + (hasZero?0:1)) );
						break;
					}
				}
				
			}	// end of else (i.e. not a missing value)
			
		}	// end of for each data entry
		
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
				numberFormatter = new DecimalFormat("0.0###", dfs);
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

	/**
	 * @return 
	 * this prefix will be included at beginning of names
	 * of discretized states 
	 */
	public String getBinPrefix() {
		return binPrefix;
	}

	/**
	 * @param binPrefix 
	 * this prefix will be included at beginning of names
	 * of discretized states 
	 */
	public void setBinPrefix(String binPrefix) {
		this.binPrefix = binPrefix;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.datamining.discretize.IDiscretization#getName()
	 */
	public String getName() {
		return "Frequency (single bin for zeros)";
	}

	/**
	 * @return symbol to be used to represent a missing value in the data. 
	 * @see ConstructionController#DEFAULT_MISSING_VALUE_TOKEN
	 */
	public String getMissingValueToken() {
		return missingValueToken;
	}

	/**
	 * @param missingValueToken :
	 * symbol to be used to represent a missing value in the data. 
	 * @see ConstructionController#DEFAULT_MISSING_VALUE_TOKEN
	 */
	public void setMissingValueToken(String missingValueToken) {
		this.missingValueToken = missingValueToken;
	}


}
