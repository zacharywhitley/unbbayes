package unbbayes.datamining.classifiers;

import unbbayes.datamining.datamanipulation.*;

public class Leaf
{
	/** Leaf's class value */
	private byte classValue;

	/** Leaf's class distribution. */
	private double[] distribution;
  	
	/** Counts the classified instances??? */
	private int numeroInstClass;
	
	/** Class attribute of dataset.??? */
	private Attribute classAttribute;
  	
	/** Constructor used in the case of there is no instances */
	public Leaf(byte newClassValue, double[] newDistribution)
	{
		//internacionalization
		classValue = newClassValue;
		distribution = newDistribution;
	}
  	
	/** General use constructor */
	public Leaf(Attribute newClassAttribute,double[] newDistribution)
	{
		//internacionalization
		classAttribute = newClassAttribute;
		distribution = newDistribution;
		numeroInstClass=(int)distribution[Utils.maxIndex(distribution)];
		Utils.normalize(distribution);
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
			return classAttribute.getAttributeName()+" = "+classAttribute.value((int) classValue);
		}
	}
}



