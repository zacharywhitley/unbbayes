package unbbayes.datamining.classifiers.decisiontree;

import unbbayes.datamining.datamanipulation.*;
import java.io.Serializable;
import java.util.*;

public class Node implements Serializable
{
	/** Attribute used for splitting. */
	protected Attribute splitAttribute;

	//instrumentation
	/** keep attributes and instances used */
	protected SplitObject split;
	/** info gains */
	protected double[] infoGains;
	/** data obtained from numeric attributes */
	protected ArrayList numericDataList;

	protected ArrayList children = new ArrayList();

	public Node(Attribute splitAttribute)
	{
		this.splitAttribute = splitAttribute;
	}

	public Attribute getAttribute()
	{
		return splitAttribute;
	}

	public String getAttributeName()
	{
		return splitAttribute.getAttributeName();
	}

	public String toString()
	{
		return getAttributeName();
	}

	public void setInstrumentationData(SplitObject split, double[] infoGains, ArrayList numericDataList)
	{
		this.split = split;
		this.infoGains = infoGains;
		this.numericDataList = numericDataList;
	}

	public SplitObject getSplitData()
	{
		return split;
	}

	public double[] getInfoGains()
	{
		return infoGains;
	}

	public ArrayList getNumericDataList()
	{
		return numericDataList;
	}

	public void add(Object obj)
	{
		children.add(obj);
	}
}

