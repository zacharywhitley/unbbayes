package unbbayes.datamining.datamanipulation;

import java.util.*;

/** data used in calculus of a numeric attribute */
public class NumericData
{
	int index;
	ArrayList cuts;
	ArrayList gains;
	ArrayList instancesAbove;
	ArrayList instancesBelow;
	
	public NumericData(int index)
	{
		this.index = index;
		cuts = new ArrayList();
		gains = new ArrayList();
		instancesAbove = new ArrayList();
		instancesBelow = new ArrayList();
	}
	
	public void addData(double cut, double gain, int[] instancesBelow, int[] instancesAbove)
	{
		cuts.add(new Double(cut));
		gains.add(new Double(gain));
		this.instancesBelow.add(instancesBelow);
		this.instancesAbove.add(instancesAbove);
	}
	
	public int getNumberOfCuts()
	{
		return cuts.size();
	}
	
	public int getIndex()
	{
		return index;
	}
	
	public double getCut(int index)
	{
		if(index>getNumberOfCuts())
		{
			return Double.MIN_VALUE;				
		}
		else
		{
			return ((Double)cuts.get(index)).doubleValue();
		}
	}
	
	public double getGain(int index)
	{
		if(index+1>getNumberOfCuts())
		{
			return Double.MIN_VALUE;				
		}
		else
		{
			return ((Double)gains.get(index)).doubleValue();
		}
	}
	
	public int[] getInstancesBelow(int index)
	{
		if(index>getNumberOfCuts())
		{
			return null;				
		}
		else
		{
			return ((int[])instancesBelow.get(index));
		}
	}
	
	public int[] getInstancesAbove(int index)
	{
		if(index>getNumberOfCuts())
		{
			return null;				
		}
		else
		{
			return ((int[])instancesAbove.get(index));
		}
	}
}
