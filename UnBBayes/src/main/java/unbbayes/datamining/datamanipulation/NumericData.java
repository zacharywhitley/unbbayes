/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package unbbayes.datamining.datamanipulation;

import java.io.Serializable;
import java.util.ArrayList;

/** Encapsulates data obtained on a split value search for a numeric attribute 
 * 
 * @author Danilo Balby Silva Castanheira (danbalby@yahoo.com)
 * */
public class NumericData implements Serializable {
	/** attribute index relative to a splitObject's attributes */
	private int index;
	
	/** list of double values representing the values tested */
	private ArrayList<Double> cuts;
	
	/** list of double values representing the gains obtained */
	private ArrayList<Double> gains;
	
	/** list of float arrays representing the distributions above of each cut  */
	private ArrayList<float[]> instancesAbove;
	
	/** list of float arrays representing the distributions below of each cut  */
	private ArrayList<float[]> instancesBelow;
	
	/** the missing values distribution */
	private float[] missingValuesDistribution;

	
	//OBS: 	the elements on the same position on the four 
	//		ArrayLists relates to the same cut evaluation 
	
	//-----------------------------CONSTRUCTORS---------------------------//
	
	/**
	 * Default constructor
	 * 
	 * @param index attribute index
	 * @param missingValuesDistribution the missing values distribution
	 */
	public NumericData(int index, float[] missingValuesDistribution)
	{
		this.index = index;
		this.missingValuesDistribution = missingValuesDistribution;
		cuts = new ArrayList<Double>();
		gains = new ArrayList<Double>();
		instancesAbove = new ArrayList<float[]>();
		instancesBelow = new ArrayList<float[]>();
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
	public void addData(double cut, double gain, float[] instancesBelow, float[] instancesAbove)
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
	 * Returns the missing values distribution
	 * 
	 * @return the missing values distribution
	 */
	public float[] getMissingValuesDistribution()
	{
		return missingValuesDistribution;
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
			return (cuts.get(index)).doubleValue();
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
			return (gains.get(index)).doubleValue();
		}
	}
	
	/**
	 * Returns the distribution below the cut on a given position
	 * 
	 * @param index cut's position
	 * @return the distribution below the cut on the given position, null 
	 * if there isn't a cut on the position specified.   
	 */	
	public float[] getInstancesBelow(int index)
	{
		if(index>getNumberOfCuts())
		{
			return null;				
		}
		else
		{
			return (instancesBelow.get(index));
		}
	}
	
	/**	
	 * Returns the distribution above the cut on a given position
	 * 
	 * @param index cut's position
	 * @return the distribution above the cut on the given position, null 
	 * if there isn't a cut on the position specified.   
	 */
	public float[] getInstancesAbove(int index)
	{
		if(index>getNumberOfCuts())
		{
			return null;				
		}
		else
		{
			return (instancesAbove.get(index));
		}
	}
}
