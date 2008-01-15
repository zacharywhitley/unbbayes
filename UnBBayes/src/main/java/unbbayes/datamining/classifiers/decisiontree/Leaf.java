package unbbayes.datamining.classifiers.decisiontree;

import java.io.Serializable;

import unbbayes.datamining.datamanipulation.Attribute;
import unbbayes.datamining.datamanipulation.Instance;
import unbbayes.datamining.datamanipulation.Utils;

/**
 * Class representing the leaf of the decision tree
 * 
 * @author Danilo Balby Silva Castanheira (danbalby@yahoo.com)
 */
public class Leaf implements Serializable {
	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;		
	
	/** Class attribute of dataset */
	private Attribute classAttribute;
	
	/** Leaf's class value */
	private float classValue;
	
	/** Used in binary class problems. It's the leaf's class complementary */
	private int notClassValue;
	
	/** Leaf's weight class distribution. */
	private float[] distribution;
	
	//-----------------------------CONSTRUCTOR---------------------------//
	
	/** General use constructor 
	 *
	 * @param classAttribute new class attribute of dataset 
	 * @param weightDistribution new weight distribution
	 * @param threshold
	 * @param positiveClass
	 */
	public Leaf(Attribute classAttribute, float[] distribution, float threshold,
			int positiveClass) {
		this.classAttribute = classAttribute;
		this.distribution = distribution;

		computeClass(threshold, positiveClass);
	}
  	
	//---------------------------BASIC FUNCIONS---------------------------//
  	
	private void computeClass(float threshold, int positiveClass) {
		int negativeClass = Math.abs(1 - positiveClass);
		
		if (threshold > 0) {
			/* With Laplace estimate */
			double positiveRate = distribution[positiveClass] + 1;
			positiveRate /= (distribution[positiveClass] +
					distribution[negativeClass] + 2);
			if (positiveRate > threshold) {
				classValue = positiveClass;
			} else {
				classValue = negativeClass;
			}
		} else {
			classValue = Utils.maxIndex(distribution);
		}
		
		notClassValue = (int) Math.abs(1 - classValue);
	}

  	/**
  	 * Returns the leaf's class value
  	 * 
  	 * @return the leaf's class value
  	 */
	public int getClassValue() {
		return (int) classValue;
	}
	
	/**
	 * Returns the weight distribution
	 * 
	 * @return the weight distribution
	 */
	public float[] getDistribution() {
		if (distribution == null) {
			return null;
		} else {
			float[] arrayCopy = new float[distribution.length];
			System.arraycopy(distribution, 0, arrayCopy, 0,
					distribution.length);
			return arrayCopy;
		}
	}
	
	/**
	 * Returns the string representing the node on the tree
	 * 
	 * @return string representing the node on the tree
	 */
	public String toString() {
		if (Instance.isMissingValue(classValue)) {	
			return "NULL";
		} else {
			float numberInst = 0;
			float numberInstNonClass = 0;
			int weightDistributionSize = distribution.length;
			for (int i = 0; i < weightDistributionSize; i++) {
				numberInst += distribution[i];
				if (i != classValue) {
					numberInstNonClass += distribution[i]; 
				}
			}
							
			String text = classAttribute.getAttributeName() + " = " +
				classAttribute.value((int) classValue) + 
				" (" + Utils.keep2DigitsAfterDot(numberInst);
			if (numberInstNonClass != 0) {
				text = text + "|" + Utils.keep2DigitsAfterDot(numberInstNonClass) + ")";
			} else {
				text = text + ")";
			}
			
			return text;
		}
	}
	
	/**
	 * @return the matched
	 */
	public int getMatched() {
		return (int) distribution[(int) classValue];
	}

	/**
	 * @return the errors
	 */
	public int getErrors() {
		return (int) distribution[notClassValue];
	}
	
}