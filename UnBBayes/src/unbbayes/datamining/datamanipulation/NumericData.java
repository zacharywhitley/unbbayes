package unbbayes.datamining.datamanipulation;

import java.util.*;

/** Encapsulates data obtained on a split value search for a numeric attribute 
 * 
 * @author Danilo Balby Silva Castanheira (danbalby@yahoo.com)
 * */
public class NumericData
{
	/** attribute index relative to a splitObject's attributes */
	private int index;
	/** list of double values representing the values tested */
	private ArrayList cuts;
	/** list of double values representing the gains obtained */
	private ArrayList gains;
	/** list of int arrays representing the distributions above of each cut  */
	private ArrayList instancesAbove;
	/** list of int arrays representing the distributions below of each cut  */
	private ArrayList instancesBelow;
	
	//OBS: 	the elements on the same position on the four 
	//		ArrayLists relates to the same cut evaluation 
	
	//-----------------------------CONSTRUCTORS---------------------------//
	
	/**
	 * Default constructor
	 * 
	 * @param index attribute index
	 */
	public NumericData(int index)
	{
		this.index = index;
		cuts = new ArrayList();
		gains = new ArrayList();
		instancesAbove = new ArrayList();
		instancesBelow = new ArrayList();
	}
	
	//---------------------------------SETS-------------------------------//
	
	/**
	 * Adds data about a new cut evaluation
	 * 
	 * @param cut value tested
	 * @param gain gain calculated
	 * @param instancesBelow distribution of the instances whose value is below the cut  
	 * @param instancesAbove distribution of the instances whose value is above the cut
	 */
	public void addData(double cut, double gain, int[] instancesBelow, int[] instancesAbove)
	{
		cuts.add(new Double(cut));
		gains.add(new Double(gain));
		this.instancesBelow.add(instancesBelow);
		this.instancesAbove.add(instancesAbove);
	}
	
	//---------------------------------GETS-------------------------------//
	
	/**
	 * Returns the number of cuts added
	 * 
	 * @return number of cuts added
	 */
	public int getNumberOfCuts()
	{
		return cuts.size();
	}
	
	/**
	 * Returns the index of the attribute relative to the numeric data
	 * 
	 * @return index of the attribute relative to the numeric data
	 */
	public int getIndex()
	{
		return index;
	}
		
	/**
	 * Returns the cut on a given position
	 * 
	 * @param index cut's position
	 * @return cut on the given position, Double.MIN_VALUE if there isn't
	 * a cut on the position specified.   
	 */
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
	
	/**
	 * Returns the gain relative to the cut on a given position
	 * 
	 * @param index cut's position
	 * @return the gain relative to the cut on the given position, Double.MIN_VALUE 
	 * if there isn't a cut on the position specified.   
	 */
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
	
	/**
	 * Returns the distribution below the cut on a given position
	 * 
	 * @param index cut's position
	 * @return the distribution below the cut on the given position, null 
	 * if there isn't a cut on the position specified.   
	 */	
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
	
	/**	
	 * Returns the distribution above the cut on a given position
	 * 
	 * @param index cut's position
	 * @return the distribution above the cut on the given position, null 
	 * if there isn't a cut on the position specified.   
	 */
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
