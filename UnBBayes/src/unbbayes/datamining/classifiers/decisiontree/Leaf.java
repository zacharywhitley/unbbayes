package unbbayes.datamining.classifiers.decisiontree;

import java.io.Serializable;
import unbbayes.datamining.datamanipulation.*;

public class Leaf implements Serializable
{
	/** Leaf's class value */
	private byte classValue;

	/** Leaf's class distribution. */
	private double[] distribution;
  	
	/** Counts the classified instances */
	private int numberInstClass;
	
	/** Class attribute of dataset. */
	private Attribute classAttribute;
  	
	/** Constructor used in the case of there is no instances */
	public Leaf(byte newClassValue, double[] newDistribution)
	{
		classValue = newClassValue;
		distribution = newDistribution;
	}
  	
	/** General use constructor */
	public Leaf(Attribute newClassAttribute,double[] newDistribution)
	{
		classAttribute = newClassAttribute;
		distribution = newDistribution;
		numberInstClass=(int)distribution[Utils.maxIndex(distribution)];
		//Utils.normalize(distribution);
		classValue = (byte)Utils.maxIndex(distribution);
	}
  	
	public byte getClassValue()
	{
		return classValue;
	}
  	
	public String toString()
	{
		if (Instance.isMissingValue(classValue))
		{	
			return "NULL";
		}
		else
		{	
			return classAttribute.getAttributeName()+" = "+classAttribute.value((int) classValue)+" ("+numberInstClass+")";
		}
	}
	
	public double[] getDistribution()
	{
		double[] arrayCopy = new double[distribution.length];
		System.arraycopy(distribution,0,arrayCopy,0,distribution.length);
		return arrayCopy;
	}
}



