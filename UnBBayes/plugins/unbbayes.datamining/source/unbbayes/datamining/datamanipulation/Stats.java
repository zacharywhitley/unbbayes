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

/**
 * A class to store and calculate simple statistics
 *
 * @author M�rio Henrique Paes Vieira (mariohpv@bol.com.br)
 * @version $1.0 $ (16/02/2002)
 * edited by Emerson Lopes Machado (emersoft@conectanet.com.br)
 * (29/SEP/2006)
 */
public class Stats {
	/** The number of values seen */
	private float count;

	/** The sum of values seen */
	private double sum;

	/** The sum of values squared seen */
	private float sumSq;

	/** The standard deviation of values */		
	private double stdDev;

	/** The mean of values */		
	private double mean;

	/** The minimum value seen */
	private float min;

	/** The maximum value seen */
	private float max;
	
	/** The current instanceSet */
	private InstanceSet instanceSet;
	
	/** The current instanceSet */
	private int attributeIndex;
	
	/**
	 * Constructor for the Stats class.
	 * 
	 * @param instanceSet
	 */
	public Stats(InstanceSet instanceSet, int attributeIndex) {
		this.instanceSet = instanceSet;
		this.attributeIndex = attributeIndex;
		
		compute();
	}
	/**
	 * Calculates the standard deviation of an array of numbers.
	 * See Knuth's The Art Of Computer Programming Volume II: Seminumerical
	 * Algorithms.
	 * This algorithm is slower, but more resistant to error propagation.
	 * The input dataset must contain at least two values. <br>
	 * M(1) = x(1), M(k) = M(k-1) + (x(k) - M(k-1) / k<br>
	 * S(1) = 0, S(k) = S(k-1) + (x(k) - M(k-1)) * (x(k) - M(k))<br>
	 * for 2 <= k <= n, then<br>
	 * sigma = sqrt(S(n) / (n - 1))
	 *
	 * @param dataset Sample to compute the standard deviation of.
	 * @param att The attribute's index of the sample.
	 * @return standard deviation estimate of the sample.
	 */
	private void compute() {
		float MISSING_VALUE = Instance.MISSING_VALUE;
		int counterIndex = instanceSet.counterIndex;
		count = instanceSet.numInstances;
		
		if (count < 2) {
			stdDev = Float.NaN;
			return;
		}
		sum = 0;
		sumSq = 0;
		min = Float.MAX_VALUE;
		max = -Float.MAX_VALUE;
		double weight;
		double avg = 0;
		double aux = 0;
		double newavg;
		double value;
		int inst = 0;
		int w;

		for (int i = 0; i < count; i++) {
			value = instanceSet.instances[i].data[attributeIndex];
			
			/* Check if current value is a missing value */
			if (value == MISSING_VALUE) {
				/* Skip to the next value */
				continue;
			}
			
			if (i == 0) {
				/* First loop. Initiate 'avg' with first value and skip it */
				avg = instanceSet.instances[0].data[attributeIndex];
				w = 1;
			} else {
				w = 0;
			}
			weight = instanceSet.instances[i].data[counterIndex];
			for (; w < weight; w++) {
				newavg = avg + (value - avg) / (inst + 1);
				aux += (value - avg) * (value - newavg);
				avg = newavg;
				++inst;
				

				/* Others statistics */
				if (value < min) {
					min = (float) value;
				}
				if (value > max) {
					max = (float) value;
				}
				sum += value;
				sumSq += value * value;
			}
		}
		mean = sum / inst;
		stdDev = Math.sqrt(aux / (inst - 1));
		count = inst;
	}

	private void compute2() {
		float MISSING_VALUE = Instance.MISSING_VALUE;
		int counterIndex = instanceSet.counterIndex;
		count = instanceSet.numInstances;
		
		if (count < 2) {
			stdDev = Float.NaN;
			return;
		}
		sum = 0;
		sumSq = 0;
		min = Float.MAX_VALUE;
		max = -Float.MAX_VALUE;
		float weight;
		float value;
		int inst = 0;
		int w;

		for (int i = 0; i < count; i++) {
			value = instanceSet.instances[i].data[attributeIndex];
			
			/* Check if current value is a missing value */
			if (value == MISSING_VALUE) {
				/* Skip to the next value */
				continue;
			}
			
			weight = instanceSet.instances[i].data[counterIndex];
			for (w = 0; w < weight; w++) {
				sum += value;
				sumSq += value * value;
				++inst;
			}
		}
		mean = sum / inst;
		for (int i = 0; i < count; i++) {
			value = instanceSet.instances[i].data[attributeIndex];
			
			/* Check if current value is a missing value */
			if (value == MISSING_VALUE) {
				/* Skip to the next value */
				continue;
			}
			
			weight = instanceSet.instances[i].data[counterIndex];
			for (w = 0; w < weight; w++) {
				stdDev += (value - mean) * (value - mean);
				
				/* Others statistics */
				if (value < min) {
					min = value;
				}
				if (value > max) {
					max = value;
				}
			}
		}
		
		stdDev = (float) Math.sqrt(stdDev / (inst - 1));
		count = inst;
	}


	/** 
	 * Returns the number of values seen
	 * 
	 * @return Number of values seen
	 */
	public float getCount() {
		return count;
	}
	
	/** 
	 * Returns the sum of values seen
	 * 
	 * @return Sum
	 */
	public double getSum() {
		return sum;
	}
	
	/** 
	 * Returns the sum of values squared seen
	 * 
	 * @return Sum of values squared
	 */
	public float getSumSq() {
		return sumSq;
	}
	
	/** 
	 * Return the standard deviation of values
	 * 
	 * @return Standard deviation of values
	 */
	public double getStdDev() {
		return stdDev;
	}
	
	/** 
	 * Returns the mean of values
	 * 
	 * @return Mean of values
	 */
	public double getMean() {
		return mean;
	}
	
	/** 
	 * Returns the minimum value seen, or Double.NaN if no values seen
	 * 
	 * @return Minimum value seen
	 */
	public float getMin() {
		return min;
	}
	
	/** 
	 * Returns the maximum value seen
	 * 
	 * @return Maximum value seen
	 */
	public float getMax() {
		return max;
	}
	
	/**
	 * Returns a string summarising the stats so far.
	 *
	 * @return The summary string
	 */
	public String toString() {
		return
			"Count	 " + Utils.doubleToString(count, 8) + '\n'
			+ "Min		 " + Utils.doubleToString(min, 8) + '\n'
			+ "Max		 " + Utils.doubleToString(max, 8) + '\n'
			+ "Sum		 " + Utils.doubleToString(sum, 8) + '\n'
			+ "SumSq	 " + Utils.doubleToString(sumSq, 8) + '\n'
			+ "Mean		" + Utils.doubleToString(mean, 8) + '\n'
			+ "StdDev	" + Utils.doubleToString(stdDev, 8) + '\n';
	}
} 

