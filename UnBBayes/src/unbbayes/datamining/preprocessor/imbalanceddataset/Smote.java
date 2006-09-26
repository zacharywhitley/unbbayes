package unbbayes.datamining.preprocessor.imbalanceddataset;

import java.util.Locale;

import unbbayes.Testset;
import unbbayes.datamining.datamanipulation.Attribute;
import unbbayes.datamining.datamanipulation.Instance;
import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.datamanipulation.mtree.MTree;

/**
 * SMOTE oversamples a specified class of a dataset. It creates new cases
 * based on the existing ones. These new cases are randomly interpolated
 * between each instance and its k nearest neighbors, also chosen by random.
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 18/08/2006
 */
public class Smote {
	
	private float nearesNeighborsIDs[][];
	
	/** The number of attributes of the dataset */
	private int numAttributes;
	
	/** The current dataset that will be smoted */
	private InstanceSet instanceSet;
	
	/** Number of instances in the instance set */
	private int numInstances;

	/** Index used to find nearest neighbor */
	private MTree mTree;
	
	/** Number of neighbors utilized in SMOTE*/
	private int k;
	
//	/** Stores a new instances created */
//	private short newInstance[];
	
	/** Stores all new instances created */
	private short newInstanceSet[][];
	
	/** Stores the weight of each new instances created */
	private float weight[];
	
	/** The number of synthetic created instances */
	private int numNewInstances = 0;

	/** The number of current created new instance */
	private int newInstanceCounter;
	
	/** Set the type of an attribute
	 * 0 - Nominal
	 * 1 - Numeric
	 * 2 - Numeric Cyclic
	 */
	private short attType[];
	
	/** Minimum value of an attribute. Used for circular attributes */
	private float attMinValue[];
	
	/** Maximum value of an attribute. Used for circular attributes */
	private float attMaxValue[];
	
	/** Range value of an attribute. Used for circular attributes */
	private float attRangeValue[];
	
	/** Mean value of an attribute. Used for circular attributes */
	private float attMeanValue[];

	private boolean discretize;

	/** Used in SMOTE
	 * 0: copy nominal attributes from the current instance
	 * 1: copy from the nearest neighbors
	 */
	private int nominalOption;

	/** Distance function
	 * 0: Hamming
	 * 1: HVDM
	 */
	private int distanceFunction;
	
	/** The gap is a random number between 0 and 1 wich tells how far from the
	 * current instance and how near from its nearest neighbor the new instance
	 * will be interpolated.
	 * The fixedGap, if true, determines that the gap will be fix for all
	 * attributes. If set to false, a new one will be drawn each attribute
	 */
	private boolean fixedGap;
	
	/**
	 * SMOTE oversamples a specified class of a dataset. It creates new cases
	 * based on the existing ones. These new cases are randomly interpolated
	 * between each instance and its k nearest neighbors, also chosen by random.
	 * 
	 * @param instancesIDs[]: The chosen subset of instances to be smoted
	 * @param mTree: index used to find nearest neighbors
	 */
	public Smote(InstanceSet instanceSet, MTree mTree) {
		this.instanceSet = instanceSet;
		this.mTree = mTree;
		numInstances = instanceSet.numInstances();
	}

	/**
	 * SMOTE oversamples a specified class of a dataset. It creates new cases
	 * based on the existing ones. These new cases are randomly interpolated
	 * between each instance and its k nearest neighbors, also chosen by random.
	 * 
	 * @param proportion: Desired proportion of new instances
	 * @param classIndex: class of desired nearest neighbors
	 * @return
	 */
	public InstanceSet run(InstanceSet instanceSet, float proportion, int classValue,
			boolean discretize, int nominalOption) {
		this.instanceSet = instanceSet;
		int counter = 0;
		int instancesIDsTmp[] = new int[numInstances];
		
		for (int i = 0; i < numInstances; i++) {
			if (instanceSet.getInstance(i).classValue() == classValue) {
				instancesIDsTmp[counter] = i;
				++counter;
			}
		}
		int instancesIDs[] = new int[counter];
		for (int i = 0; i < counter; i++) {
			instancesIDs[i] = instancesIDsTmp[i];
		}
		
		return run(instancesIDs, proportion, classValue, discretize, nominalOption);
	}

	/**
	 * SMOTE oversamples a specified class of a dataset. It creates new cases
	 * based on the existing ones. These new cases are randomly interpolated
	 * between each instance and its k nearest neighbors, also chosen by random.
	 * 
	 * @param instancesIDs[]: The chosen subset of instances to be smoted
	 * @param proportion: Desired proportion of new instances
	 * @param classIndex: class of desired nearest neighbors
	 * @return
	 */
	public InstanceSet run(int instancesIDs[], float proportion, int classValue,
			boolean discretize, int nominalOption) {
		/* The number of instances of the chosen subset of instances to be smoted */
		int numInstancesIDs;
		Instance instance;
		int nearestNeighbors[];
		
		numAttributes = instanceSet.numAttributes();
		
		/* Message thrown as an exception in case of wrong arguments */ 
		String exceptionMsg = "";
		
		/* Test the parameters */
		if (instanceSet == null) exceptionMsg = "The instanceSet is null!";
		if (instancesIDs == null) exceptionMsg = "The instancesIDs is null!";
		if (proportion <= 0) exceptionMsg = "proportion must be greater than 0!";
		if (k < 0) exceptionMsg = "k must be greater than 0!";
//		if (mTree == null) exceptionMsg = "The mTree is null!";
		
		/* Throw exception if there is problems with the parameters */
		if (exceptionMsg != "") throw new IllegalArgumentException(exceptionMsg);
		
		/* Set the vector of the mininum and maximum attributes' values used to
		 * creating circular attributes of new instances 
		 */
		attType = new short[numAttributes];
		attMinValue = new float[numAttributes];
		attMaxValue = new float[numAttributes];
		attRangeValue = new float[numAttributes];
		attMeanValue = new float[numAttributes];
		for (int att = 0; att < numAttributes; att++) {
			/* Skip the class attribute */
			if (instanceSet.getClassIndex() == att) {
				continue;
			}
			
			Attribute attribute = instanceSet.getAttribute(att);
			if (attribute.isNominal()) attType[att] = 0;
			if (attribute.isNumeric()) attType[att] = 1;
			if (attribute.isCyclic()) attType[att] = 2;
			if (attType[att] == 2) {
				attMinValue[att] = attribute.getMinimumValue(); 
				attMaxValue[att] = attribute.getMaximumValue();
				attRangeValue[att] = attMaxValue[att] - attMinValue[att] + 1;
				attMeanValue[att] = attRangeValue[att] / 2;
			}
		}
		
		/* Set the number of instances that will be used to create new ones */
		numInstancesIDs = instancesIDs.length;
		
		/* The number of synthetic created instances */
		numNewInstances = 0;	/* Java requires an initialization */
		
		/* The number of created instances for each instace */
		int numNewInstancesPerInstance;
		
		/* If proportion is less than 1, only a subset of the dataset will be
		 * used to oversample the data
		 */
		if (proportion < 1) {
			numNewInstances = Math.round(numInstancesIDs * proportion);
			instancesIDs = chooseInstances(instancesIDs, numNewInstances);
			numInstancesIDs = instancesIDs.length;
			numNewInstancesPerInstance = 1;
		} else {
			/* All the subset will be used to created new instances */
			numNewInstancesPerInstance = Math.round(proportion);	/* must be int */
			numNewInstances = numInstancesIDs * numNewInstancesPerInstance;
		}
		
		/* The matrix which will hold the new instances. Allocates one vector
		 * of int for storing the weight
		 */
		newInstanceSet = new short[numNewInstances][numAttributes];
		newInstanceCounter = 0;
		weight = new float[numNewInstances];
		
		/* Loop through all chosen instances and create 'numNewInstancesPerInstance'
		 * new instances for each of them
		 */
		for (int i = 0; i < numInstancesIDs; i++) {
			instance = instanceSet.getInstance(instancesIDs[i]);
//			nearestNeighbors = mTree.nearestNeighborIDs(i, k, classValue);
			nearestNeighbors = nearestNeighborIDs(i);
			populate(instance, nearestNeighbors, numNewInstancesPerInstance);
		}
		
		/* Merge the newInstanceSet with the original one and return the result */
		return doMerge();
	}
	 
	private void populate(Instance instance, int nearestNeighborsIDs[],
			int numNewInstancesPerInstance) {
		Instance nearestNeighbor;
		int nearestNeighborIndex;
		
		/* Randomly chosen nearest neighbor's index */
		int chosenNN;
		
		/* Stores an attribute's value of the an instance */
		float attValue;
		
		/* Difference between an attribute of the current instance and its
		 * nearest neighbor
		 */
		float dif;
		
		/* Fractionary part of 'dif' */
		float frac;
		
		/* Stores the attributes' values of the new instance */
		float newAttValue;
		
		/* Stores the attributes' values of the new instance formated as String */
		String newAttValueString;
		
		/* Index of the new attribute value */
		int newAttValueIndex;
		
		/* References an attribute of the instance set */
		Attribute attribute;
		
		/* The gap is a random number between 0 and 1 wich tells how far from the
		 * current instance (gap -> 1) and how near from its nearest neighbor
		 * (gap -> 0) the new instance will be interpolated
		 */
		float gap;
		
		/* This loop creates 'numNewInstancesPerInstance' new instances based on the current
		 * instance
		 */
		for (int i = 0; i < numNewInstancesPerInstance; i++) {
			/* Chooses randomly one of the k nearest neighbor */
			chosenNN = Math.round((float) (Math.random() * (k - 1)));
			nearestNeighborIndex = nearestNeighborsIDs[chosenNN];
			nearestNeighbor = instanceSet.getInstance(nearestNeighborIndex);
			short newInstance[] = new short[numAttributes];
			gap = (float) Math.random();
			
			/* The next loop interpolates a new instance between the current
			 * instance and its randomly chosen nearest neighbor
			 */
			for (int att = 0; att < numAttributes; att++) {
				/* Skip the class attribute */
				if (instanceSet.getClassIndex() == att) {
					continue;
				}
				
				/* Only numeric attributes can be interpolated */
				if (attType[att] == 0) {
					if (nominalOption == 0) {
						/* Stores the attribute's value of this new instance */
						newInstance[att] = instance.getValue(att);
					} else if (nominalOption == 1) {
						newInstance[att] = nearestNeighbor.getValue(att);
					}

					/* Skip to the next attribute */
					continue;
				} 
				
				attribute = instanceSet.getAttribute(att);
				
				/* Create the new instance's 'att' atribute's value */
				attValue = instance.floatValue(att);
				dif = nearestNeighbor.floatValue(att) - attValue;

				/* If fixedGap is false choose a different gap to each attribute */
				if (!fixedGap) {
					gap = (float) Math.random();
				}
				dif = dif * gap;

				/* If this attribute is circular we take special care */
				if (attType[att] == 2) {
					/* If the absolute of 'dif' is greater than half way between
					 * minimun and maximum value of this attribute, we take its
					 * complementary
					 */
					if (Math.abs(dif) > attMeanValue[att]) {
						if (dif > 0 ) {
							dif = dif - attRangeValue[att];
						} else {
							dif = attRangeValue[att] - dif;
						}
					}
				}
				
				newAttValue = attValue + dif;

				/* Discretize the fractionary part */
				if (discretize) {
					frac = newAttValue - (int) newAttValue;
					if (frac >= 0 && frac < 0.125) {
						frac = 0;
					} else if (frac >= 0.125 && frac < 0.25) {
						frac = (float) 0.25;
					} else if (frac >= 0.25 && frac < 0.5) {
						frac = (float) 0.5;
					} else if (frac >= 0.5 && frac < 0.75) {
						frac = (float) 0.75;
					} else {
						frac = 1;
					}
					newAttValue = frac + (int) newAttValue;
				}
				
				/* Check if new value is inside the range of the cyclic attribute*/
				if (attType[att] == 2) {
					/* Check the upper bound */
					if (newAttValue > attMaxValue[att]) {
						/* Get the distance from upper bound and add it to the
						 * lower bound
						 */
						newAttValue = attMaxValue[att] - newAttValue;
						newAttValue = newAttValue + attMinValue[att];
					}
					
					/* Check lower bound */
					if (newAttValue < attMinValue[att]) {
						/* Get the distance from lower bound and withdraws this
						 * amount from the upper bound
						 */
						newAttValue = attMinValue[att] - newAttValue;
						newAttValue = attMaxValue[att] - newAttValue;
					}
				}

				/* If this attribute's value is not defined yet in the instance
				 * set, it must be defined
				 */
				if (discretize) {
					newAttValueString = String.format(Locale.US, "%.2f", newAttValue);
				} else {
					newAttValueString = String.format(Locale.US, "%f", newAttValue);
				}
				newAttValueIndex = attribute.indexOfValue(newAttValueString);
				if (newAttValueIndex == -1) {
					attribute.addValue(newAttValueString);
				}
				
				/* Stores the attribute's value of this new instance */
				newInstance[att] = (short) attribute.indexOfValue(newAttValueString);
			}
			
			/* Set the class of the new instance if the instance set contains one */
			if (instanceSet.getClassIndex() > -1) {
				newInstance[instance.getClassIndex()] = instance.classValue();
			}
			
			/* Add this new instance */
			addNewInstance(newInstance, instance);
		}
	}
	 
	private void addNewInstance(short[] newInstance, Instance instance) {
		boolean instanceExist;
		int instanceExistID;
		int idx;
		Instance instanceTmp;

		newInstanceSet[newInstanceCounter] = newInstance;
		weight[newInstanceCounter] = instance.getWeight();
//		weight[newInstanceCounter] = 1;
		++newInstanceCounter;

//		/* Check if this new instance already exist in the dataset of the new instances */
//		instanceExist = false;
//		instanceExistID = -1;	/* Java requires it */
//		for (int inst = 0; inst < newInstanceCounter && !instanceExist; inst++) {
//			instanceExist = true;
//			for (int att = 0; att < numAttributes; att++) {
//				if (newInstanceSet[inst][att] != newInstance[att]) {
//					instanceExist = false;
//					break;
//				}
//			}
//			instanceExistID = inst;
//		}
//		
//		/* If the new instance already exist in the new instance set*/ 
//		if (instanceExist) {
//			/* Update the weight of the already existed instance in the new
//			 * instance set
//			 */
////			weight[instanceExistID] += instance.getWeight();
//			weight[instanceExistID] += 1;
//		} else {
//			/* Check if this new instance already exist in the original instance set */
//			idx = instanceSet.contains(newInstance);
//			if (idx != -1) {
//				/* Update the weight of the already existed instance in the
//				 * instance set
//				 */
//				instanceTmp = instanceSet.getInstance(idx);
////				instanceTmp.addWeight(instanceTmp.getWeight());
//				instanceTmp.addWeight(1);
//			} else {
//				/* Insert the new instance in the new instance set */
//				newInstanceSet[newInstanceCounter] = newInstance;
//				weight[newInstanceCounter] = instance.getWeight();
////				weight[newInstanceCounter] = 1;
//				++newInstanceCounter;
//			}
//		}
	}

	/**
	 * Randomly chooses a subset of input dataset and returns its indexes' vector.
	 * @param instancesIDs: The input dataset index
	 * @param numNewInstances: The size of the subset created
	 * @return
	 */
	private int[] chooseInstances(int instancesIDs[], int numNewInstances) {
		int index;
		int indexesChosen[];
		int numInstances;
		boolean equal;
		
		indexesChosen = new int[numNewInstances];
		numInstances = instancesIDs.length;
		
		/* Choose the first instace's index */
		indexesChosen[0] = Math.round((float)Math.random() * numInstances);
		
		/* Chooses the next instances' indexes */
		for (int i = 1; i < numNewInstances; i++) {
			equal = true;
			
			/* Loop until 'index' differs from any previous chosen one */
			while (equal) {
				index = Math.round((float)Math.random() * numInstances);
				equal = false;
				
				/* Compares 'index' to all chosen previous indexes*/
				for (int j = 0; j < i; j++) {
					if (index == indexesChosen[j]) {
						equal = true;
						break;
					}
				}
				
				/* Stores this 'index' only if not equal to any previous chosen one */
				if (!equal) indexesChosen[i] = index;
			}
		}
		
		return indexesChosen;
	}
	
	private InstanceSet doMerge() {
		Instance instanceTmp;
		int newInstanceSize;
		int ema = 0;
		
		compactMatrix(newInstanceSet);

		/* Computes the final instance set zise */
		newInstanceSize = 0;
		for (int i = 0; i < newInstanceCounter; i++) {
			if (weight[i] != 0) {
				++newInstanceSize;
				ema += weight[i];
			}
		}
		newInstanceSize = newInstanceSize + numInstances;
		
		/* Creates new instance set with sufficient space for doing the merge */
		InstanceSet result = new InstanceSet(instanceSet, newInstanceSize);
		
		/* Copies the original instances to the result instance set */
		instanceSet.copyInstances(0, result, numInstances);

		@SuppressWarnings("unused")
		float dist[] = distribution(result);
		/* Creates new instances and add them to the result instance set */
		for (int i = 0; i < newInstanceCounter; i++) {
			if (weight[i] == 0) {
				continue;
			}
			instanceTmp = instanceSet.find(newInstanceSet[i]);
			if (instanceTmp != null) {
				instanceTmp.addWeight(weight[i]);
			} else {
				Instance instance = new Instance((int) weight[i], newInstanceSet[i]);
				result.add(instance);
			}
		}
		dist = distribution(result);
		return result;
	}

	private float[] distribution(InstanceSet trainData) {
		int numInstances = trainData.numInstances();
		int numClasses = trainData.numClasses();
		int classIndex;
		float distribution[] = new float[numClasses];
		float weight = 0;
		Instance instance;
		
		for (int i = 0; i < numClasses; i++) {
			distribution[i] = 0;
		}
		
		for (int i = 0; i < numInstances; i++) {
			instance = trainData.getInstance(i);
			classIndex = instance.classValue();
			distribution[classIndex] += instance.getWeight();
			weight += instance.getWeight();
		}

//		for (int i = 0; i < numClasses; i++) {
//			distribution[i] /= weight;
//		}
		return distribution;
	}

	private void compactMatrix(short[][] matrix) {
		int index[] = sort(newInstanceSet);
		int firstMatch;
		int lastMatch;
		int firstMatchIndex;
		int lastMatchIndex;
		boolean match;
		boolean thereAreMatches;
		int i;
		int idx;
		float weightSum;
		
		i = 0;
		while (i < newInstanceCounter) {
			match = true;
			thereAreMatches = false;
			firstMatch = i;
			firstMatchIndex = index[firstMatch];
			lastMatch = i;
			weightSum = weight[firstMatchIndex];
			++i;
			while (match && i < newInstanceCounter) {
				idx = index[i];
				for (int att = 0; att < numAttributes; att++) {
					if (matrix[idx][att] != matrix[firstMatchIndex][att]) {
						match = false;
						break;
					}
				}
				if (match) {
					++lastMatch;
					lastMatchIndex = index[lastMatch];
					weightSum += weight[lastMatchIndex];
					thereAreMatches = true;
				}
				++i;
			}
			/* If there are matches compact them */
			if (thereAreMatches) {
				/* The weight of the first element will receive the counter of
				 * all elements
				 */
				weight[firstMatchIndex] = weightSum;
				/* The weight of the others elements that match will be set to 0 */
				for (int j = firstMatch + 1; j <= lastMatch; j++) {
					idx = index[j];
					weight[idx] = 0;
				}
			}
		}
	}

	private int[] nearestNeighborIDs(int instanceID) {
		int nearestNeighborIDs[] = new int[k];
		
		for (int i = 0; i < k; i++) {
			nearestNeighborIDs[i] = (int) nearesNeighborsIDs[instanceID][2 * i];
		}
		return nearestNeighborIDs;
	}

	/** Distance function
	 * 0: Hamming
	 * 1: HVDM
	 */
	public void buildNN(int k, int classIndex, int distanceFunction) {
		this.k = k;
		this.distanceFunction = distanceFunction;
		
		/* 'nearesNeighborsIDs[i][j]' - j:
		 * evens - nn ID
		 * odds - nn distance
		 */
		nearesNeighborsIDs = new float[numInstances][2 * k];
		Instance instanceI;
		Instance instanceJ;
		
		/* Start 'nearesNeighborsIDs */
		for (int i = 0; i < numInstances; i++) {
			for (int j = 1; j < 2 * k; j += 2) {
				nearesNeighborsIDs[i][j] = 1000000;
			}
		}
		
		float dist = 0;
		int distGreaterID = 0;
		float distGreater = 1000000;
		
		for (int i = 0; i < numInstances; i++) {
			instanceI = instanceSet.getInstance(i);
			if (instanceI.classValue() == classIndex) {
				for (int j = 0; j < numInstances; j++) {
					instanceJ = instanceSet.getInstance(j);
					if (instanceJ.classValue() == classIndex) {
						dist = distance(instanceI, instanceJ);
						if (dist < distGreater) {
							nearesNeighborsIDs[i][distGreaterID + 1] = dist;
							nearesNeighborsIDs[i][distGreaterID] = j;
						}
						distGreater = 0;
						for (int d = 0; d < 2 * k; d += 2) {
							if (distGreater < nearesNeighborsIDs[i][d + 1]) {
								distGreater = nearesNeighborsIDs[i][d + 1];
								distGreaterID = d;
							}
						}
					}
				}
			}
		}
	}

	private float distance(Instance instanceI, Instance instanceJ) {
		float dist = 0;
		float distTmp;
		
		for (int att = 0; att < numAttributes; att++) {
			/* Skip the class attribute */
			if (instanceSet.getClassIndex() == att) {
				continue;
			}
			
			distTmp = 0;
			if (attType[att] == 0) {
				/* Attribute is nominal */
				if (instanceI.getValue(att) != instanceI.getValue(att)) {
					if (distanceFunction == 0) {
						/* Hamming distance */
						distTmp = distTmp + 1;
					} else if (distanceFunction == 1) {
						/* HVDM distance */
						
					}
				}
			} else {
				/* Attribute is numeric or cyclic*/
				distTmp = instanceI.getValue(att) - instanceI.getValue(att);
				distTmp = Math.abs(distTmp);

				/* Attribute is cyclic */
				if (attType[att] == 2) {
					/* If the absolute of 'dist' is greater than half way between
					 * minimun and maximum value of this attribute, we take its
					 * complementary
					 */
					if (Math.abs(dist) > attMeanValue[att]) {
						distTmp = attRangeValue[att] - distTmp;
					}
				}
			}
			dist = dist + distTmp;
		}
		return dist;
	}
	
	/**
	 * Sorts a given matrix of shorts in ascending order and returns an
	 * matrix of integers with the positions of the elements of the
	 * original matrix in the sorted matrix. It doesn't use safe floating-point
	 * comparisons.
	 *
	 * @param matrix This matrix is not changed by the method!
	 * @return An matrix of integers with the positions in the sorted
	 * matrix.
	 */
	public static int[] sort(short[][] matrix) {
		int [] index = new int[matrix.length];
		for (int i = 0; i < index.length; i++) {
			index[i] = i;
		}
		quickSort(matrix, index, 0, matrix.length - 1);
		return index;
	}

	/**
	 * Implements unsafe quicksort for an matrix of indices.
	 *
	 * @param matrix The matrix of doubles to be sorted
	 * @param index The index which should contain the positions in the
	 * sorted matrix
	 * @param lo0 The first index of the subset to be sorted
	 * @param hi0 The last index of the subset to be sorted
	 */
	private static void quickSort(short[][] matrix, int[] index, int lo0, int hi0) {
		int lo = lo0;
		int hi = hi0;
		short mid[];
		int aux;

		if (hi0 > lo0) {
			/* Arbitrarily establishing partition element as the midpoint of
			 * the matrix
			 */
			mid = matrix[index[(lo0 + hi0) / 2]];
	
			/* loop through the matrix until indices cross */
			while (lo <= hi) {
				/* find the first element that is greater than or equal to
				 * the partition element starting from the left Index
				 */
					while (lessThan(matrix[index[lo]], mid) && lo < hi0) {
						++lo;
					}
		
					/* find an element that is smaller than or equal to
					 * the partition element starting from the right Index
					 */
					while (greaterThan(matrix[index[hi]], mid) && hi > lo0) {
						--hi;			
					}
		
					/* if the indexes have not crossed, swap */
					if (lo <= hi) {
						aux = index[lo];
						index[lo] = index[hi];
						index[hi] = aux;
						++lo;
						--hi;
					}
			}
	
			/* If the right index has not reached the left side of matrix
			 * must now sort the left partition
			 */
			if (lo0 < hi) {
				quickSort(matrix, index, lo0, hi);
			}
	
			/* If the left index has not reached the right side of matrix
			 * must now sort the right partition
			 */
			if (lo < hi0) {
				quickSort(matrix, index, lo, hi0);
			}
		}
	}

	/**
	 * Compares two arrays of short and returns true if the first is greater
	 * than the second
	 * @param s the first array
	 * @param mid the second array
	 * @return
	 * 		true if s > mid
	 * 		false otherwise
	 */
	private static boolean greaterThan(short[] s, short[] mid) {
		int num = s.length;
		
		for (int i = 0; i < num; i++) {
			if (s[i] > mid [i]) {
				return true;
			} else if (s[i] < mid [i]) {
				return false;
			}
		}
		return false;
	}

	/**
	 * Compares two arrays of short and returns true if the first is less
	 * than the second
	 * @param s the first array
	 * @param mid the second array
	 * @return
	 * 		true if s < mid
	 * 		false otherwise
	 */
	private static boolean lessThan(short[] s, short[] mid) {
		int num = s.length;
		
		for (int i = 0; i < num; i++) {
			if (s[i] < mid [i]) {
				return true;
			} else if (s[i] > mid [i]) {
				return false;
			}
		}
		return false;
	}
}

