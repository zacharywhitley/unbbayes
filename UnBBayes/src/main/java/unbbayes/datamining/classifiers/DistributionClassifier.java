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
package unbbayes.datamining.classifiers;

import java.util.ResourceBundle;

import unbbayes.datamining.datamanipulation.Instance;
import unbbayes.datamining.datamanipulation.Utils;

/**
 * Abstract Bayesian Classifier. All schemes that works with bayesian
 * classifiers extends this class.
 *
 * @author M�rio Henrique Paes Vieira (mariohpv@bol.com.br)
 * @version $1.0 $ (17/02/2002)
 */
public abstract class DistributionClassifier extends Classifier {
	/**
	 * Predicts the class memberships for a given instance. If
	 * an instance is unclassified, the returned array elements
	 * must be all zero. If the class is numeric, the array
	 * must consist of only one element, which contains the
	 * predicted value.
	 *
	 * @param instance the instance to be classified
	 * @return an array containing the estimated membership
	 * probabilities of the test instance in each class (this
	 * should sum to at most 1)
	 * @exception Exception if distribution could not be
	 * computed successfully
	 */
	public abstract float[] distributionForInstance(Instance instance)
		throws Exception;

	/** Load resources file from this package */
	protected static ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
			unbbayes.datamining.resources.ClassifiersResource.class.getName());

	protected float[] originalDistribution;
	private int[] classValues;
	private float[] probabilities;
	public static final int NORMAL_CLASSIFICATION = 0;
	public static final int RELATIVE_FREQUENCY_CLASSIFICATION = 1;
	public static final int ABSOLUTE_FREQUENCY_CLASSIFICATION = 2;
	public static final int BINARY_CLASSIFICATION = 3;
	private int classificationType = 0;

	private int interestingClass;

	private double threshold;

	public float[] getOriginalDistribution() {
		return originalDistribution;
	}

	/**
	 * Classifies the given test instance. The instance has to belong to a
	 * dataset when it's being classified.
	 *
	 * @param instance the instance to be classified
	 * @return the predicted most likely class for the instance or
	 * Instance.missingValue() if no prediction is made
	 * @exception Exception if an error occurred during the prediction
	 */
	public int classifyInstance(Instance instance) throws Exception {
		float[] dist = distributionForInstance(instance);
		
		if (dist == null) {
			throw new Exception(resource.getString("nullPrediction"));
		}
		
		switch (classificationType) {
			case NORMAL_CLASSIFICATION:
				return Utils.maxIndex(dist);
				
			case RELATIVE_FREQUENCY_CLASSIFICATION:
				int maxIndex = -1;
				float max = Float.MIN_VALUE;
				float aux;
				
				for (int i = 0; i < dist.length; i++) {
					aux = dist[i] / originalDistribution[i];
					if (aux > max) {
						max = aux;
						maxIndex = i;
					}
				}
				return maxIndex;
				
			case ABSOLUTE_FREQUENCY_CLASSIFICATION:
				int actualValue;
				
				for (int j = 0; j<classValues.length; j++)  {
					actualValue = classValues[j];
					if (dist[actualValue] >= probabilities[j]) {
						return actualValue;
					}
				}
				return (int) Instance.MISSING_VALUE;
				
			default:
				throw new Exception("Classification type not known");
		}
	}

	public int classifyInstance(float[] dist) throws Exception { 
		if (dist == null) {
			throw new Exception(resource.getString("nullPrediction"));
		}
		
		switch (classificationType) {
			case NORMAL_CLASSIFICATION:
				return Utils.maxIndex(dist);
				
			case RELATIVE_FREQUENCY_CLASSIFICATION:
				int maxIndex = -1;
				float max = Float.MIN_VALUE;
				float aux;
				
				for (int i = 0; i < dist.length; i++) {
					aux = dist[i] / originalDistribution[i];
					if (aux > max) {
						max = aux;
						maxIndex = i;
					}
				}
				return maxIndex;
				
			case ABSOLUTE_FREQUENCY_CLASSIFICATION:
				int actualValue;
				
				for (int j = 0; j<classValues.length; j++)  {
					actualValue = classValues[j];
					if (dist[actualValue] >= probabilities[j]) {
						return actualValue;
					}
				}
				return (int) Instance.MISSING_VALUE;
			
			case BINARY_CLASSIFICATION:
				float prob;
				
				for (int i = 0; i < dist.length; i++) {
					dist[i] = dist[i] / originalDistribution[i];
				}
				
				prob = dist[interestingClass] / dist[1 - interestingClass];
				if (prob >= threshold) {
					return interestingClass;
				} else {
					return 1 - interestingClass;
				}
					
			default:
				throw new Exception("Classification type not known");
		}
	}

	public void setNormalClassification() {
		classificationType = NORMAL_CLASSIFICATION;
	}

	public void setRelativeClassification() {
		classificationType = RELATIVE_FREQUENCY_CLASSIFICATION;
	}

	public void setAbsoluteClassification(int[] classValues,
			float[] probabilities) {
		classificationType = ABSOLUTE_FREQUENCY_CLASSIFICATION;
		this.classValues = classValues;
		this.probabilities = probabilities;
	}
	
	public void setOriginalDistribution(float[] originalDistribution) {
		this.originalDistribution = originalDistribution;
	}

	public void setInterestingClass(int interestingClass) {
		this.interestingClass = interestingClass;
	}

	public void setThreshold(float threshold) {
		this.threshold = threshold;
	}

	public void setBinaryClassification() {
		classificationType = BINARY_CLASSIFICATION;
	}

}
