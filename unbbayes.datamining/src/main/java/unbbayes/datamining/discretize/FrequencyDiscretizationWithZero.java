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
 * 
 * @author Shou Matsumoto
 */
public class FrequencyDiscretizationWithZero extends FrequencyDiscretization {
	

	private NumberFormat numberFormatter = null;

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
		
		// do not consider the case in which number of bins is 1 or less (because we need at least a bin for 0, and another bin for the rest	)
		if (numThresholds <= 1) {
			super.discretizeAttribute(att, numThresholds);
		}
		
		InstanceSet inst = getInstances();
		
		// basic assetions
		if (!att.isNumeric()) { 
			throw new IllegalArgumentException("Attribute not numeric");
		}
		
		// do nothing if there is no data
		int numInstances = inst.numInstances();
		if (numInstances == 0) {	
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
		float[] sortedValues = new float[numInstances];
      	Enumeration enumInst = inst.enumerateInstances();
      	for (int i = 0; enumInst.hasMoreElements(); i++) {
      		Instance instance = (Instance)enumInst.nextElement();
      		sortedValues[i] = instance.getValue(att);
		}
      	
		
		
		float[] originalValuesBkp = new float[numInstances];
		System.arraycopy(sortedValues,0,originalValuesBkp,0,sortedValues.length);
		
		// sort values in ascending order and also get array of sum of equal distinct values
		Arrays.sort(sortedValues);
		
		// check that it's non-negative
		if (sortedValues[0] < 0) {
			throw new IllegalArgumentException("This discretization method assumes non-negative numbers, but found " + sortedValues[0]);
		}
		
		// get indexes that start distinct values in the sorted data
		List<Integer> indicesDistinctValues = new ArrayList<Integer>(sortedValues.length);
		indicesDistinctValues.add(0);	// 1st value is always distinct from "previous" (because there's no such previous)
		for (int i = 1; i < sortedValues.length; i++) {
			if (sortedValues[i] != sortedValues[i-1]) {
				indicesDistinctValues.add(i);
			}
		}
		
		// extract number of distinct values 
		int numDistinctValues = indicesDistinctValues.size();
		if (numThresholds > numDistinctValues) {
			throw new Exception("Number of thresholds is larger than number of distinct values in the data. Number of distinct values = " + numDistinctValues); 
		}
		
		// each bin should contain this number of distinct values
		int numDistinctValuesPerBin = Math.round(numDistinctValues / (numThresholds - 1f));	// -1 because zero is always a single bin
        
		// list of inclusive breakpoint. E.g. if interval is X1toX2, then it will contain values of X2.
		List<Float> breakpoints = new ArrayList<Float>(numThresholds);
		
		// add zero to breakpoints
		boolean hasZeros = sortedValues[indicesDistinctValues.get(0)] == 0f;
		if (hasZeros) {
			// breakpoint of zero will be the last occurrence of repeated zeros (which is 1 index before the next distinct value)
			breakpoints.add(sortedValues[indicesDistinctValues.get(1) - 1]);
		}
		
		// add numDistinctValuesPerBin distinct values to breakpoints
//		for (int i = 0; i < indicesDistinctValues.size(); i++) {
//			asdf;
//		}
//
//		// build labels of discrete bins
//		// breakpoint[0], breakpoint[0]-to-breakpoint[1], breakpoint[1]-to-breakpoint[2], ...
//		
//		if (lastBreakIndex != (numThresholds-1)) {   
//			breakPoint[lastBreakIndex] = (sumEachDistinctValues[valueCurrentIndex]/freqDistinctValues[valueCurrentIndex]);
//			newAttribute.addValue(numberFormatter.format(sumEachDistinctValues[valueBeginIndex]/freqDistinctValues[valueBeginIndex])+"to"+numberFormatter.format(breakPoint[lastBreakIndex]));
//		} else {	
//			breakPoint[lastBreakIndex] = sortedValues[(sortedValues.length - 1)];
//			newAttribute.addValue(numberFormatter.format(sumEachDistinctValues[valueBeginIndex]/freqDistinctValues[valueBeginIndex])+"to"+numberFormatter.format(breakPoint[lastBreakIndex]));
//		}
//        
//		// insert attribute
//		inst.setAttributeAt(newAttribute,attributePosition);
//		
//		// insert the values
//		for (int valueIndex=0; valueIndex < numInstances; valueIndex++) {	
//			byte newValue = (byte)0;
//			for (int breakIndex=0; breakIndex < lastBreakIndex; breakIndex++) {
//				if (originalValuesBkp[valueIndex] <= breakPoint[breakIndex]) {	
//					newValue = (byte)breakIndex;
//					break;
//				}
//			}
//			inst.getInstance(valueIndex).setValue(attributePosition, newValue);
//		}
//		
	
		
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
				numberFormatter = new DecimalFormat("0.0#", dfs);
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

}
