package unbbayes.datamining.preprocessor.imbalanceddataset;

import unbbayes.datamining.datamanipulation.AttributeStats;
import unbbayes.datamining.datamanipulation.Instance;
import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.datamanipulation.Stats;
import unbbayes.datamining.datamanipulation.mtree.MTree;
import unbbayes.datamining.distance.Distance;
import unbbayes.datamining.distance.HVDM;

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
	
	/** Stores all new instances created */
	private float newInstanceSet[][];
	
	/** The number of synthetic created instances */
	private int numNewInstances = 0;

	/** The number of current created new instance */
	private int newInstanceCounter;
	
	/** The dataset values. */
	private float[][] dataset;
	
	/** 
	 * The index of the dataset's column that represents the class attribute.
	 * Assume the value -1 in case there is no class attribute.
	 */
	private byte classIndex;

	/** 
	 * The index of the dataset's column that represents the counter attribute.
	 * Assumes always the last column of the internal dataset as the counter
	 * attribute.
	 */
	private byte counterIndex;

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
	 * Distance function desired.
	 * <ul>
	 * <li> 0: Hamming
	 * <li> 1: HVDM
	 * </ul>
	 */
	private byte optionDistanceFunction;
	
	/** 
	 * The gap is a random number between 0 and 1 wich tells how far from the
	 * current instance and how near from its nearest neighbor the new instance
	 * will be interpolated.
	 * The optionFixedGap, if true, determines that the gap will be fix for all
	 * attributes. If set to false, a new one will be drawn each attribute
	 */
	private boolean optionFixedGap;
	
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
		dataset = instanceSet.dataset;
		counterIndex = instanceSet.counterIndex;
		classIndex = instanceSet.classIndex;
	}

	public void setInstanceSet(InstanceSet instanceSet) {
		this.instanceSet = instanceSet;
	}
	
	public void setOptionDiscretize(boolean optionDiscretize) {
		this.optionDiscretize = optionDiscretize;
	}
	
	public void setOptionNominal(byte optionNominal) {
		this.optionNominal = optionNominal;
	}
	
	public void setOptionDistanceFunction(byte optionDistanceFunction) {
		this.optionDistanceFunction = optionDistanceFunction;
	}
	
	public void setOptionFixedGap(boolean optionFixedGap) {
		this.optionFixedGap = optionFixedGap;
	}
	
	/**
	 * SMOTE oversamples a specified class of a dataset. It creates new cases
	 * based on the existing ones. These new cases are randomly interpolated
	 * between each instance and its k nearest neighbors, also chosen by random.
	 * 
	 * @param proportion: Desired proportion of new instances
	 * @param classValue: class of desired nearest neighbors
	 * @return
	 */
	public InstanceSet run(float proportion, int classValue) {
		int counter = 0;
		int instancesIDsTmp[] = new int[numInstances];
		
		for (int inst = 0; inst < numInstances; inst++) {
			if (dataset[inst][classIndex] == classValue) {
				instancesIDsTmp[counter] = inst;
				++counter;
			}
		}
		int instancesIDs[] = new int[counter];
		for (int i = 0; i < counter; i++) {
			instancesIDs[i] = instancesIDsTmp[i];
		}
		
		return run(instancesIDs, proportion, classValue);
	}

	/**
	 * SMOTE oversamples a specified class of a dataset. It creates new cases
	 * based on the existing ones. These new cases are randomly interpolated
	 * between each instance and its k nearest neighbors, also chosen by random.
	 * 
	 * @param instancesIDs[]: The chosen subset of instances to be smoted
	 * @param proportion: Desired proportion of new instances
	 * @param classValue: class of desired nearest neighbors
	 * @return
	 */
	public InstanceSet run(int instancesIDs[], float proportion, int classValue) {
		/* The number of instances of the chosen subset of instances to be smoted */
		int numInstancesIDs;
		int nearestNeighbors[];
		
		numAttributes = instanceSet.numAttributes();
		
		/* Compute the statistics for all attributes */
		AttributeStats[] attributeStats = instanceSet.computeAttributeStats();
		
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
		
		/* 
		 * Set the array of the mininum and maximum attributes' values used to
		 * smote cyclic attributes.
		 */
		attributeType = instanceSet.attributeType;
		attMinValue = new float[numAttributes];
		attMaxValue = new float[numAttributes];
		attRangeValue = new float[numAttributes];
		attHalfRangeValue = new float[numAttributes];
		
		Stats stats;
		
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
		
		/* Set the number of instances that will be used to create new ones */
		numInstancesIDs = instancesIDs.length;
		
		/* The number of synthetic created instances */
		numNewInstances = 0;	// Java requires an initialization
		
		/* The number of created instances for each instace */
		int numNewInstancesPerInstance;
		
		/* 
		 * If proportion is less than 1, only a subset of the dataset will be
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
		
		/* The matrix which will hold the new instances and their weights */
		newInstanceSet = new float[numNewInstances][numAttributes + 1];
		newInstanceCounter = 0;
		
		/* 
		 * Loop through all chosen instances and create 'numNewInstancesPerInstance'
		 * new instances for each of them
		 */
		for (int i = 0; i < numInstancesIDs; i++) {
//			nearestNeighbors = mTree.nearestNeighborIDs(i, k, classValue);
			nearestNeighbors = nearestNeighborIDs(i);
			populate(instancesIDs[i], nearestNeighbors, numNewInstancesPerInstance);
		}
		
		/* Merge the newInstanceSet with the original one and return the result */
		return doMerge();
	}
	 
	private void populate(int inst, int nearestNeighborsIDs[],
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
		float dif;
		
		/* Fractionary part of 'dif' */
		float frac;
		
		/* 
		 * The gap is a random number between 0 and 1 wich tells how far from the
		 * current instance (gap -> 1) and how near from its nearest neighbor
		 * (gap -> 0) the new instance will be interpolated
		 */
		float gap;
		
		/* Auxiliar variable for computing the values of the new instance */
		float newAttValue;
		
		/* 
		 * This loop creates 'numNewInstancesPerInstance' new instances based on the current
		 * instance
		 */
		for (int i = 0; i < numNewInstancesPerInstance; i++) {
			/* Alocate space for the new instance and its weight */
			float newInstance[] = new float[numAttributes + 1];

			/* Chooses randomly one of the k nearest neighbor */
			chosenNN = Math.round((float) (Math.random() * (k - 1)));
			nearestNeighborIndex = nearestNeighborsIDs[chosenNN];

			gap = (float) Math.random();
			
			/* 
			 * This loop interpolates a new instance between the current
			 * instance and its randomly chosen nearest neighbor
			 */
			for (int att = 0; att < numAttributes; att++) {
				/* Skip the class attribute */
				if (classIndex == att) {
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
						newInstance[att] = dataset[inst][att];
					} else if (optionNominal == 1) {
						/* 
						 * Copy nominal attributes from the chosen nearest
						 * neighbor
						 */
						newInstance[att] = dataset[nearestNeighborIndex][att];
					}

					/* Skip to the next attribute */
					continue;
				} 
				
				/* Create the new instance's 'att' atribute's value */
				attValue = dataset[inst][att];
				dif = dataset[nearestNeighborIndex][att] - attValue;

				/* 
				 * If optionFixedGap is false choose a different gap to each
				 * attribute
				 */
				if (!optionFixedGap) {
					gap = (float) Math.random();
				}
				dif = dif * gap;

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
				
				newAttValue = attValue + dif;

				/* Discretize the fractionary part */
				if (optionDiscretize) {
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
				if (attributeType[att] == CYCLIC) {
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
				
				/* Stores the attribute's value of this new instance */
				newInstance[att] = newAttValue;
			}
			
			/* Set the class of the new instance if the instanceSet contains one */
			if (classIndex != -1) {
				newInstance[classIndex] = dataset[inst][classIndex];
			}
			
			/* Copy the weight value of the smoted instance */
			newInstance[counterIndex] = dataset[inst][counterIndex];
			
			/* Add this new instance to the new instanceSet */
			newInstanceSet[newInstanceCounter] = newInstance;
			++newInstanceCounter;
		}
	}
	 
	/**
	 * Randomly chooses a subset of input dataset and returns its indexes' array.
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
		int newInstanceSize;
		
		compactMatrix(newInstanceSet);

		/* Computes the final instance set zise */
		newInstanceSize = 0;
		for (int i = 0; i < newInstanceCounter; i++) {
			if (newInstanceSet[i][counterIndex] != 0) {
				++newInstanceSize;
			}
		}
		newInstanceSize += numInstances;
		
		/* Creates new instance set with sufficient space for doing the merge */
		InstanceSet result = new InstanceSet(instanceSet, newInstanceSize);
		
		/* Copies the original instances to the result instance set */
		instanceSet.copyInstances(0, result, numInstances);

		/* Creates new instances and add them to the result instance set */
		float weight;
		int existIndex;
		for (int i = 0; i < newInstanceCounter; i++) {
			if (newInstanceSet[i][counterIndex] == 0) {
				continue;
			}
			existIndex = instanceSet.find(newInstanceSet[i]);
			if (existIndex != -1) {
				weight = newInstanceSet[i][counterIndex];
				dataset[existIndex][counterIndex] += weight;
			} else {
				result.insertInstance(newInstanceSet[i]);
			}
		}
		return result;
	}

	private void compactMatrix(float[][] matrix) {
		int index[] = sort(newInstanceSet, numAttributes);
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
			weightSum = matrix[firstMatchIndex][counterIndex];
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
					weightSum += matrix[lastMatchIndex][counterIndex];
					thereAreMatches = true;
				}
				++i;
			}
			/* If there are matches compact them */
			if (thereAreMatches) {
				/* The weight of the first element will receive the counter of
				 * all elements
				 */
				matrix[firstMatchIndex][counterIndex] = weightSum;
				/* The weight of the others elements that match will be set to 0 */
				for (int j = firstMatch + 1; j <= lastMatch; j++) {
					idx = index[j];
					matrix[idx][counterIndex] = 0;
				}
			}
		}
	}

	/**
	 * Construct the array of the indexes of the nearest neighbors of the
	 * input instance.
	 * 
	 * @param instanceID The index of the input instance.
	 * @return
	 */
	private int[] nearestNeighborIDs(int instanceID) {
		int nearestNeighborIDs[] = new int[k];
		
		for (int i = 0; i < k; i++) {
			nearestNeighborIDs[i] = (int) nearesNeighborsIDs[instanceID][2 * i];
		}
		return nearestNeighborIDs;
	}

	public void buildNN(int k, int classValue) {
		this.k = k;
		
		/* 
		 * 'nearesNeighborsIDs[i][j]' - j:
		 * evens - nn ID
		 * odds - nn distance
		 */
		nearesNeighborsIDs = new float[numInstances][2 * k];
		
		/* Initialize 'nearesNeighborsIDs */
		for (int i = 0; i < numInstances; i++) {
			for (int j = 1; j < 2 * k; j += 2) {
				nearesNeighborsIDs[i][j] = Float.POSITIVE_INFINITY;
			}
		}
		
		/* Get distance function */
		Distance distance;
//		optionDistanceFunction;
		int normFactor = 4;
		distance = new HVDM(instanceSet, normFactor);
		
		float dist = 0;
		int distGreaterID = 0;
		float distGreater = 1000000;
		float[] instanceI; 
		float[] instanceJ; 
		for (int i = 0; i < numInstances; i++) {
			instanceI = dataset[i];
			if (instanceI[classIndex] == classValue) {
				for (int j = 0; j < numInstances; j++) {
					instanceJ = dataset[j];
					if (instanceJ[classIndex] == classValue) {
						dist = distance.distanceValue(instanceI, instanceJ);
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

	/**
	 * Sorts a given matrix of floats in ascending order and returns an
	 * matrix of integers with the positions of the elements of the
	 * original matrix in the sorted matrix. It doesn't use safe floating-point
	 * comparisons.
	 *
	 * @param matrix This matrix is not changed by the method!
	 * @return An matrix of integers with the positions in the sorted
	 * matrix.
	 */
	public static int[] sort(float[][] matrix, int numAttributes) {
		int[] index = new int[matrix.length];

		for (int i = 0; i < index.length; i++) {
			index[i] = i;
		}
		quickSort(matrix, index, 0, matrix.length - 1, numAttributes);
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
	private static void quickSort(float[][] matrix, int[] index, int lo0,
			int hi0, int num) {
		int lo = lo0;
		int hi = hi0;
		float mid[];
		int aux;

		if (hi0 > lo0) {
			/* 
			 * Arbitrarily establishing partition element as the midpoint of
			 * the matrix
			 */
			mid = matrix[index[(lo0 + hi0) / 2]];
	
			/* loop through the matrix until indices cross */
			while (lo <= hi) {
				/* 
				 * find the first element that is greater than or equal to
				 * the partition element starting from the left Index
				 */
				while (lessThan(matrix[index[lo]], mid, num) && lo < hi0) {
					++lo;
				}
	
				/* 
				 * find an element that is smaller than or equal to
				 * the partition element starting from the right Index
				 */
				while (greaterThan(matrix[index[hi]], mid, num) && hi > lo0) {
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
	
			/* 
			 * If the right index has not reached the left side of matrix
			 * must now sort the left partition
			 */
			if (lo0 < hi) {
				quickSort(matrix, index, lo0, hi, num);
			}
	
			/* 
			 * If the left index has not reached the right side of matrix
			 * must now sort the right partition
			 */
			if (lo < hi0) {
				quickSort(matrix, index, lo, hi0, num);
			}
		}
	}

	/**
	 * Compares two arrays of float and returns true if the first is greater
	 * than the second
	 * @param v1 The first array
	 * @param v2 The second array
	 * @param num The array size
	 * @return
	 * <li>true if v1 > v2
	 * <li>false otherwise
	 */
	private static boolean greaterThan(float[] v1, float[] v2, int num) {
		for (int i = 0; i < num; i++) {
			if (v1[i] > v2 [i]) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Compares two arrays of float and returns true if the first is less
	 * than the second
	 * @param v1 The first array
	 * @param v2 The second array
	 * @param num The array size
	 * @return
	 * <li>true if v1 < v2
	 * <li>false otherwise
	 */
	private static boolean lessThan(float[] v1, float[] v2, int num) {
		for (int i = 0; i < num; i++) {
			if (v1[i] < v2 [i]) {
				return true;
			}
		}
		return false;
	}
}

