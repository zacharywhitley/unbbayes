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
package unbbayes.datamining.preprocessor.imbalanceddataset;

import java.util.Date;
import java.util.Random;

import unbbayes.datamining.datamanipulation.AttributeStats;
import unbbayes.datamining.datamanipulation.Instance;
import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.datamanipulation.NearestNeighbors;
import unbbayes.datamining.datamanipulation.Stats;
import unbbayes.datamining.datamanipulation.mtree.MTree;
import unbbayes.datamining.evaluation.batchEvaluation.GlobalBatchParameters;
import unbbayes.datamining.evaluation.batchEvaluation.PreprocessorParameters;

/**
 * SMOTE oversamples a specified class of a dataset. It creates new cases
 * based on the existing ones. These new cases are randomly interpolated
 * between each instance and its numNN nearest neighbors, also chosen by random.
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 18/08/2006
 */
public class Smote extends Batch {
	
	private int nearestNeighborsIDs[][];
	
	/** The number of attributes of the dataset */
	private int numAttributes;
	
	/** The set of instances of the current instanceSet that will be smoted */
	private Instance[] instances;
	
	/** Number of instances in the instance set */
	private int numInstances;

	/** Index used to find nearest neighbor */
	private MTree mTree;
	
	/** Stores all new instances created */
	private float newInstanceSet[][];
	
	/** The number of current created new instance */
	private int newInstanceCounter;
	
	/** 
	 * The index of the dataset's column that represents the class attribute.
	 * Assume the value -1 in case there is no class attribute.
	 */
	private int classIndex;

	/** 
	 * The index of the dataset's column that represents the counter attribute.
	 */
	private int counterIndex;

	/** 
	 * Stores the type of an attribute:
	 * 0 - Numeric
	 * 1 - Nominal
	 * 2 - Numeric Cyclic
	 */
	private byte attributeType[];
	
	/** Constant set for numeric attributes. */
	private final static byte NUMERIC = 0;

	/** Constant set for nominal attributes. */
	private final static byte NOMINAL = 1;

	/** Constant set for cyclic numeric attributes. */
	private final static byte CYCLIC = 2;

	/** Minimum value of an attribute. Used for cyclic attributes */
	private float attMinValue[];
	
	/** Maximum value of an attribute. Used for cyclic attributes */
	private float attMaxValue[];
	
	/** Range value of an attribute. Used for cyclic attributes */
	private float attRangeValue[];
	
	/** 
	 * Half of the range value. Used to speed up calculations in cyclic
	 * attributes
	 */
	private float attHalfRangeValue[];

	/** Number of neighbors utilized in SMOTE */
	private int numNN = 5;
	
	/**
	 * Set it <code>true</code> to discretize the synthetic value created for
	 * the new instance. 
	 */
	private boolean optionDiscretize;

	/** 
	 * Used in the creation of the new instance.
	 * <ul>
	 * <li>	0: copy nominal attributes from the smoted instance </li>
	 * <li>	1: copy nominal attributes from the chosen nearest neighbor </li>
	 * </ul>
	 */
	private byte optionNominal;

	/** 
	 * The gap is a random number between 0 and 1 wich tells how far from the
	 * current instance and how near from its nearest neighbor the new instance
	 * will be interpolated.
	 * The optionFixedGap, if true, determines that the gap will be fix for all
	 * attributes. If set to false, a new one will be drawn for each attribute.
	 */
	private boolean optionFixedGap;

	private Random random;

	private NearestNeighbors nearestNeighbors;
	
	private static boolean useRatio = true;
	private static boolean useK = false;
	private static boolean useOverThresh = false;
	private static boolean usePosThresh = false;
	private static boolean useNegThresh = false;
	private static boolean useCleaning = false;
	
	public Smote(InstanceSet instanceSet) {
		this(instanceSet, null);
	}

	public Smote(InstanceSet instanceSet, PreprocessorParameters parameters) {
		super(useRatio, useK, useOverThresh, usePosThresh, useNegThresh,
				useCleaning, instanceSet, parameters);
		preprocessorName = "Smote";
		initialize();
	}

	public void setMTree(MTree mTree) {
		this.mTree = mTree;
	}

	public void setInstanceSet(InstanceSet instanceSet) {
		this.instanceSet = instanceSet;
		instances = instanceSet.instances;
		numInstances = instanceSet.numInstances();
		counterIndex = instanceSet.counterIndex;
		classIndex = instanceSet.classIndex;
	}
	
	/**
	 * SMOTE oversamples a specified class of a dataset. It creates new cases
	 * based on the existing ones. These new cases are randomly interpolated
	 * between each instance and its numNN nearest neighbors, also chosen by
	 * random.
	 * 
	 * @param proportion: Desired proportion of new instances
	 * @param interestingClass: class of desired nearest neighbors
	 */
	protected void run() {
		int counter = 0;
		int instancesIDsTmp[] = new int[numInstances];
		
		for (int inst = 0; inst < numInstances; inst++) {
			if (instances[inst].data[classIndex] == interestingClass) {
				instancesIDsTmp[counter] = inst;
				++counter;
			}
		}
		int instancesIDs[] = new int[counter];
		for (int i = 0; i < counter; i++) {
			instancesIDs[i] = instancesIDsTmp[i];
		}
		
		run(instancesIDs);
	}

	/**
	 * SMOTE oversamples a complete dataset. It creates new cases
	 * based on the existing ones. These new cases are randomly interpolated
	 * between each instance and its numNN nearest neighbors, also chosen by random.
	 * 
	 * @param proportion: Desired proportion of new instances
	 */
	public void runAll() {
		int instancesIDs[] = new int[numInstances];
		
		for (int inst = 0; inst < numInstances; inst++) {
			instancesIDs[inst] = inst;
		}
		
		run(instancesIDs);
	}

	/**
	 * SMOTE oversamples a specified class of a dataset. It creates new cases
	 * based on the existing ones. These new cases are randomly interpolated
	 * between each instance and its numNN nearest neighbors, also chosen by random.
	 * 
	 * @param instancesIDs[]: The chosen subset of instances to be smoted
	 * @param proportion: Desired proportion of new instances
	 */
	public int[] run(int[] instancesIDs) {
		if (instanceSet.numInstances < 2 || instancesIDs.length < 2) {
			return instancesIDs;
		}
		
//		System.out.print("Entrei: ");
//		System.out.println((new java.text.SimpleDateFormat("HH:mm:ss")).format(new Date()));

		instancesIDs = instancesIDs.clone();
		numAttributes = instanceSet.numAttributes();
		
		/* Message thrown as an exception in case of wrong arguments */ 
		String exceptionMsg = "";
		
		/* Test the parameters */
		if (instanceSet == null) exceptionMsg = "The instanceSet is null!";
		if (instancesIDs == null) exceptionMsg = "The instancesIDs is null!";
		if (proportion < 1) exceptionMsg = "proportion must be greater than 1!";
		if (numNN < 0) exceptionMsg = "numNN must be greater than 0!";
//		if (mTree == null) exceptionMsg = "The mTree is null!";
		
		/* Throw exception if there is problems with the parameters */
		if (exceptionMsg != "") throw new IllegalArgumentException(exceptionMsg);
		
		/* Initialize the random number generator */
		random  = new Random(new Date().getTime());

		/* 
		 * Set the array of the mininum and maximum attributes' values used to
		 * smote cyclic attributes.
		 */
		attributeType = instanceSet.attributeType;
		attMinValue = new float[numAttributes];
		attMaxValue = new float[numAttributes];
		attRangeValue = new float[numAttributes];
		attHalfRangeValue = new float[numAttributes];
		
		/* Get some statistics about the cyclics attributes */
		Stats stats;
		AttributeStats[] attributeStats = instanceSet.getAttributeStats();
		for (int att = 0; att < numAttributes; att++) {
			/* Skip the class attribute */
			if (instanceSet.getClassIndex() == att) {
				continue;
			}
			
			stats = attributeStats[att].getNumericStats();
			if (attributeType[att] == CYCLIC) {
				attMinValue[att] = stats.getMin();
				attMaxValue[att] = stats.getMax();
				attRangeValue[att] = attMaxValue[att] - attMinValue[att] + 1;
				attHalfRangeValue[att] = attRangeValue[att] / 2;
			}
		}
		
		/* Adjust proportion to count with the current number of instances */
		--proportion;
		
		/* Set the number of instances that will be used to create new ones */
		int numInstancesIDs = instancesIDs.length;
		
		/* The matrix which will hold the new instances and their weights */
		int inst;
		double currentSize = 0;
		for (int i = 0; i < numInstancesIDs; i++) {
			inst = instancesIDs[i];
			currentSize += instances[inst].data[counterIndex];
		}
		int numNewInstances = (int) (proportion * currentSize) + 1;
		newInstanceSet = new float[numNewInstances][numAttributes + 1];
		newInstanceCounter = 0;
		
		/* The number of created instances for each instace */
		int numNewInstancesPerInstance;
		
		/* 
		 * If proportion is less than 1, only a subset of the dataset will be
		 * used to oversample the data
		 */
		if (proportion < 1) {
//			int aux = (int) (numInstancesIDs * proportion) + 1;
			int aux = (int) (currentSize * proportion) + 1;
			instancesIDs = chooseInstances(instancesIDs, aux);
			numInstancesIDs = instancesIDs.length;
			numNewInstancesPerInstance = 1;
		} else {
			/* All the subset will be used to created new instances */
			numNewInstancesPerInstance = (int) proportion;
		}
		
//		System.out.print("Antes de popular: ");
//		System.out.println((new java.text.SimpleDateFormat("HH:mm:ss")).format(new Date()));

		/* Create the new instances */
		populate(numNewInstancesPerInstance, instancesIDs, numNewInstances);
		
		/* Check if desired number of new instances has been reached */
		if (newInstanceCounter < numNewInstances) {
			int rest = numNewInstances - newInstanceCounter;
			instancesIDs = chooseInstances(instancesIDs, rest);
			numInstancesIDs = instancesIDs.length;
			numNewInstancesPerInstance = 1;

			/* Create the rest of the new instances */
			populate(numNewInstancesPerInstance, instancesIDs, numNewInstances);
		}
		
//		System.out.print("Antes de mesclar: ");
//		System.out.println((new java.text.SimpleDateFormat("HH:mm:ss")).format(new Date()));

		/* 
		 * Insert the created instances in the instanceSet and return their indexes
		 */
		int[] result = instanceSet.insertInstances(newInstanceSet);
		
//		System.out.print("Depois de mesclar: ");
//		System.out.println((new java.text.SimpleDateFormat("HH:mm:ss")).format(new Date()));
//		System.out.println();
		
		return result;
	}
	
	private void populate(int numNewInstancesPerInstance, int[] instancesIDs,
			int max) {
		int numInstancesIDs = instancesIDs.length;
		int inst;
		int nearestNeighborsIDs[];
		
		/* 
		 * Loop through all chosen instances and create
		 * 'numNewInstancesPerInstance' new instances for each of them
		 */
		for (int i = 0; i < numInstancesIDs; i++) {
			inst = instancesIDs[i];
//			nearestNeighborsIDs = mTree.nearestNeighborIDs(i, numNN);
			nearestNeighborsIDs = nearestNeighborIDs(i);
			for (int w = 0; w < instances[inst].data[counterIndex]; w++) {
				populateAux(inst, nearestNeighborsIDs, numNewInstancesPerInstance);
				if (newInstanceCounter >= max) {
					break;
				}
			}
			if (newInstanceCounter >= max) {
				break;
			}
		}
		
	}
	
	private void populateAux(int inst, int[] nearestNeighborsIDs,
			int numNewInstancesPerInstance) {
		int nearestNeighborIndex;
		
		/* Randomly chosen nearest neighbor's index */
		int chosenNN;
		
		/* Stores an attribute's value of the an instance */
		float attValue;
		
		/* 
		 * Difference between an attribute of the current instance and its
		 * nearest neighbor
		 */
		double dif;
		
		/* Fractionary part of 'dif' */
		double frac;
		
		/* 
		 * The gap is a random number between 0 and 1 wich tells how far from the
		 * current instance (gap -> 1) and how near from its nearest neighbor
		 * (gap -> 0) the new instance will be interpolated
		 */
		double gap;
		
		/* Auxiliar variable for computing the values of the new instance */
		double newAttValue;
		
		/* 
		 * This loop creates 'numNewInstancesPerInstance' new instances based on the current
		 * instance
		 */
		for (int i = 0; i < numNewInstancesPerInstance; i++) {
			/* Alocate space for the new instance and its weight */
			float newInstance[] = new float[numAttributes + 1];

			/* Chooses randomly one of the numNN nearest neighbor */
			chosenNN = random.nextInt(nearestNeighborsIDs.length);
			nearestNeighborIndex = nearestNeighborsIDs[chosenNN];
			
			gap = Math.random();
			
			/* 
			 * This loop interpolates a new instance between the current
			 * instance and its randomly chosen nearest neighbor
			 */
			for (int att = 0; att < numAttributes; att++) {
				/* Just copy the class value of the smoted instance */
				if (att == classIndex) {
					newInstance[att] = instances[inst].data[classIndex];

					/* Skip to the next attribute */
					continue;
				}
				
				/* 
				 * Only numeric attributes can be interpolated. If the current
				 * attribute is nominal, just copy the attribute values from
				 * the smoted instance or from its nearest neighbor.
				 */
				if (attributeType[att] == NOMINAL) {
					if (optionNominal == 0) {
						/* Copy nominal attributes from the smoted instance */
						newInstance[att] = instances[inst].data[att];
					} else if (optionNominal == 1) {
						/* Copy nominal attributes from the nearest neighbor */
						newInstance[att] = instances[nearestNeighborIndex].data[att];
					}

					/* Skip to the next attribute */
					continue;
				} 
				
				/* Create the new instance's 'att' atribute's value */
				attValue = instances[inst].data[att];
				dif = instances[nearestNeighborIndex].data[att] - attValue;

				if (dif == 0) {
					/* Just copy the value and skip */
					newInstance[att] = instances[inst].data[att];
					continue;
				}
				
				/* If this attribute is cyclic we take special care */
				if (attributeType[att] == CYCLIC) {
					/* 
					 * If the absolute of 'dif' is greater than half way between
					 * minimun and maximum value of this attribute, we take its
					 * complementary
					 */
					if (Math.abs(dif) > attHalfRangeValue[att]) {
						if (dif > 0 ) {
							dif = dif - attRangeValue[att];
						} else {
							dif = attRangeValue[att] - dif;
						}
					}
				}
				
				/* 
				 * If optionFixedGap is false choose a different gap to each
				 * attribute
				 */
				if (!optionFixedGap) {
					gap = Math.random();
				}
				dif = dif * gap;

				/* Set new value */
				newAttValue = attValue + dif;

				/* Check if new value is inside the range of the cyclic attribute*/
				if (attributeType[att] == CYCLIC) {
					/* Check the upper bound */
					if (newAttValue > attMaxValue[att]) {
						/* Get the distance from upper bound and add it to the
						 * lower bound
						 */
						newAttValue = newAttValue - attMaxValue[att];
						newAttValue = attMinValue[att] + newAttValue - 1;
					}
					
					/* Check lower bound */
					if (newAttValue < attMinValue[att]) {
						/* Get the distance from lower bound and withdraws this
						 * amount from the upper bound
						 */
						newAttValue = attMinValue[att] - newAttValue;
						newAttValue = attMaxValue[att] - newAttValue + 1;
					}
				}
				
				/* Discretize the fractionary part */
				if (optionDiscretize) {
					frac = newAttValue - (int) newAttValue;
					if (frac >= 0 && frac < 0.125) {
						frac = 0;
					} else if (frac >= 0.125 && frac < 0.25) {
						frac = 0.25;
					} else if (frac >= 0.25 && frac < 0.5) {
						frac = 0.5;
					} else {
						frac = 0.75;
					}
					newAttValue = frac + (int) newAttValue;
				}
				
				/* Stores the attribute's value of this new instance */
				newInstance[att] = (float) newAttValue;
			}
			
			/* Set the weight value of the smoted instance */
			newInstance[counterIndex] = 1;
			
			/* Add this new instance to the new instanceSet */
			newInstanceSet[newInstanceCounter] = newInstance;
			++newInstanceCounter;
		}
	}
	 
	/**
	 * Randomly chooses a subset of input dataset and returns its indexes' array.
	 * 
	 * @param instancesIDs: The input dataset index
	 * @param numNewInstances: The size of the subset created
	 * @return
	 */
	private int[] chooseInstances(int[] instancesIDs, int numNewInstances) {
		int[] instancesIDsAux = instancesIDs.clone();

		int numInstances = instancesIDsAux.length;
		int aux = numNewInstances;
		if (numNewInstances > numInstances) {
			aux = numInstances;
		}
		int indexesChosen[] = new int[aux];

		Random random = new Random(new Date().getTime());
		int counter = 0;
		int index = 0;
		int inst;
		int instIDs;
		int last;
		while (counter < numNewInstances) {
			instIDs = random.nextInt(numInstances);
			inst = instancesIDsAux[instIDs];
			
			counter += instanceSet.instances[inst].data[counterIndex];
			indexesChosen[index] = inst;
			++index;
			
			last = instancesIDsAux[numInstances - 1];
			instancesIDsAux[instIDs] = last;
			--numInstances;
		}
		
		int[] result = new int[index];
		for (int i = 0; i < index; i++) {
			result[i] = indexesChosen[i];
		}
		
		return result;
	}
	
	/**
	 * Construct the array of the indexes of the nearest neighbors of the
	 * input instance.
	 * 
	 * @param instanceID The index of the input instance.
	 * @return
	 */
	private int[] nearestNeighborIDs(int instanceID) {
		int index = -1;
		int count = 0;
		
		for (int i = 0; i < numNN; i++) {
			try {
				index = nearestNeighborsIDs[instanceID][i];
			} catch (Exception e) {
				@SuppressWarnings("unused")
				boolean pause = true;
			}
			if (index > 0) {
				++count;
			}
		}

		if (count == 0) {
			@SuppressWarnings("unused")
			boolean fudeu = true;
		}
		
		int nearestNeighborIDs[] = new int[count];
		count = 0;
		for (int i = 0; i < numNN; i++) {
			index = nearestNeighborsIDs[instanceID][i];
			if (index > 0) {
				nearestNeighborIDs[count] = index;
				++count;
			}
		}

		return nearestNeighborIDs;
	}

	public void buildNearestNeighbors() throws Exception {
		if (nearestNeighborsIDs == null) {
			nearestNeighbors = new NearestNeighbors(instanceSet);
			nearestNeighborsIDs = nearestNeighbors.build(interestingClass);
		}
	}
	
	public void setOptionDiscretize(boolean optionDiscretize) {
		this.optionDiscretize = optionDiscretize;
	}
	
	public void setOptionNominal(byte optionNominal) {
		this.optionNominal = optionNominal;
	}
	
	public void setOptionFixedGap(boolean optionFixedGap) {
		this.optionFixedGap = optionFixedGap;
	}

	public void setNearestNeighborsIDs(int[][] nearestNeighborsIDs) {
		this.nearestNeighborsIDs = nearestNeighborsIDs;
	}

	public boolean isNearestNeighborsIDsBuilt() {
		return nearestNeighborsIDs != null;
	}

	public void initialize() {
		GlobalBatchParameters.getInstance().initializeSmote(this);
	}
	
	@Override
	protected void initializeBatch(InstanceSet instanceSet)
	throws Exception {
		initialize();
		setInstanceSet(instanceSet);
		setInterestingClass(positiveClass);
		buildNearestNeighbors();
	}

}