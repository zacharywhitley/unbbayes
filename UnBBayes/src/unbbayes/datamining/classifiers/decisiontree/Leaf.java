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
public class Leaf implements Serializable
{
	/** Class attribute of dataset */
	private Attribute classAttribute;
	
	/** Leaf's class value */
	private short classValue;

	/** Leaf's weight class distribution. */
	private float[] weightDistribution;
	
	//-----------------------------CONSTRUCTORS---------------------------//
	
	/** Constructor used in the case of there is no instances */
	public Leaf()
	{
		classValue = Instance.missingValue();
	}
  	
	/** General use constructor 
	 *
	 * @param classAttribute new class attribute of dataset 
	 * @param weightDistribution new weight distribution
	 */
	public Leaf(Attribute classAttribute, float[] weightDistribution)
	{
		this.classAttribute = classAttribute;
		this.weightDistribution = weightDistribution;
		classValue = (byte)Utils.maxIndex(weightDistribution);
	}
	
	//---------------------------BASIC FUNCIONS---------------------------//
  	
  	/**
  	 * Returns the leaf's class value
  	 * 
  	 * @return the leaf's class value
  	 */
	public short getClassValue()
	{
		return classValue;
	}
	
	/**
	 * Returns the weight distribution
	 * 
	 * @return the weight distribution
	 */
	public float[] getDistribution()
	{
		if(weightDistribution==null)
		{
			return null;
		}
		else
		{
			float[] arrayCopy = new float[weightDistribution.length];
			System.arraycopy(weightDistribution,0,arrayCopy,0,weightDistribution.length);
			return arrayCopy;
		}
	}
	
	/**
	 * Returns the string representing the node on the tree
	 * 
	 * @return string representing the node on the tree
	 */
	public String toString()
	{
		if (Instance.isMissingValue(classValue))
		{	
			return "NULL";
		}
		else
		{
			float numberInst = 0;
			float numberInstNonClass = 0; 
			for(int i=0;i<weightDistribution.length;i++)
			{
				numberInst += weightDistribution[i];
				if(i!=classValue)
				{
					numberInstNonClass += weightDistribution[i]; 
				}
			}
							
			String text =  classAttribute.getAttributeName()+" = "+classAttribute.value((int) classValue)+" ("+Utils.keep2DigitsAfterDot(numberInst);
			if(numberInstNonClass!=0)
			{
				text = text+"|"+Utils.keep2DigitsAfterDot(numberInstNonClass)+")";
			}
			else
			{
				text = text+")";
			}
			
			return text;
		}
	}
}



