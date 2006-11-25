package unbbayes.datamining.preprocessor.imbalanceddataset;

import java.io.File;
import java.io.RandomAccessFile;

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
	
	/** The current instanceSet that will be smoted */
	private InstanceSet instanceSet;

	/** The set of instances of the current instanceSet that will be smoted */
	private Instance[] instances;
	
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
	private byte HAMMING = 0;
	private byte HVDM = 1;
	
	/** 
	 * The gap is a random number between 0 and 1 wich tells how far from the
	 * current instance and how near from its nearest neighbor the new instance
	 * will be interpolated.
	 * The optionFixedGap, if true, determines that the gap will be fix for all
	 * attributes. If set to false, a new one will be drawn for each attribute.
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
		instances = instanceSet.instances;
		numInstances = instanceSet.numInstances();
		counterIndex = instanceSet.counterIndex;
		classIndex = instanceSet.classIndex;
		optionDistanceFunction = HAMMING;
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
	 * between each instance and its k nearest neighbors, also chosen by random.
	 * 
	 * @param proportion: Desired proportion of new instances
	 * @param classValue: class of desired nearest neighbors
	 */
	public void run(double proportion, int classValue) {
		int counter = 0;
		int instancesIDsTmp[] = new int[numInstances];
		
		for (int inst = 0; inst < numInstances; inst++) {
			if (instances[inst].data[classIndex] == classValue) {
				instancesIDsTmp[counter] = inst;
				++counter;
			}
		}
		int instancesIDs[] = new int[counter];
		for (int i = 0; i < counter; i++) {
			instancesIDs[i] = instancesIDsTmp[i];
		}
		
		run(instancesIDs, proportion);
	}

	/**
	 * SMOTE oversamples a specified class of a dataset. It creates new cases
	 * based on the existing ones. These new cases are randomly interpolated
	 * between each instance and its k nearest neighbors, also chosen by random.
	 * 
	 * @param instancesIDs[]: The chosen subset of instances to be smoted
	 * @param proportion: Desired proportion of new instances
	 */
	public void run(int instancesIDs[], double proportion) {
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
		numNewInstances = 0;	// Java requires initialization
		
		/* The number of created instances for each instace */
		int numNewInstancesPerInstance;
		
		/* 
		 * If proportion is less than 1, only a subset of the dataset will be
		 * used to oversample the data
		 */
		if (proportion < 1) {
			numNewInstances = Math.round(numInstancesIDs * (float) proportion);
			instancesIDs = chooseInstances(instancesIDs, numNewInstances);
			numInstancesIDs = instancesIDs.length;
			numNewInstancesPerInstance = 1;
		} else {
			/* All the subset will be used to created new instances */
			numNewInstancesPerInstance = Math.round((float) proportion);
			numNewInstances = numInstancesIDs * numNewInstancesPerInstance;
		}
		
		/* The matrix which will hold the new instances and their weights */
		int inst;
		int numNewInstancesWeighted = 0;
		for (int i = 0; i < numInstancesIDs; i++) {
			inst = instancesIDs[i];
			numNewInstancesWeighted += (int) instances[inst].data[counterIndex];
		}
		numNewInstancesWeighted *= numNewInstancesPerInstance;
		newInstanceSet = new float[numNewInstancesWeighted][numAttributes + 1];
		newInstanceCounter = 0;
		
		/* 
		 * Loop through all chosen instances and create 'numNewInstancesPerInstance'
		 * new instances for each of them
		 */
		for (int i = 0; i < numInstancesIDs; i++) {
			inst = instancesIDs[i];
//			nearestNeighbors = mTree.nearestNeighborIDs(i, k);
			nearestNeighbors = nearestNeighborIDs(i, instancesIDs);
			for (int w = 0; w < instances[inst].data[counterIndex]; w++) {
				populate(inst, nearestNeighbors, numNewInstancesPerInstance);
			}
		}
		
		/* Insert the created instances in the instanceSet */
		instanceSet.insertInstances(newInstanceSet);
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

			/* Chooses randomly one of the k nearest neighbor */
			chosenNN = (int) Math.round((Math.random() * (double) (k - 1)));
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
	private int[] chooseInstances(int instancesIDs[], int numNewInstances) {
		int index = -10;
		int indexesChosen[];
		int numInstances;
		boolean equal;

		indexesChosen = new int[numNewInstances];
		numInstances = instancesIDs.length;

		/* Choose the first instace's index */
		index = Math.round((float) Math.random() * (numInstances - 1));
		index = instancesIDs[index];
		indexesChosen[0] = index;

		/* Chooses the next instances' indexes */
		for (int i = 1; i < numNewInstances; i++) {
			equal = true;

			/* Loop while 'index' is equal to any previous chosen one */
			while (equal) {
				index = Math.round((float) Math.random() * (numInstances - 1));
				index = instancesIDs[index];
				equal = false;

				/* Compares 'index' to all chosen previous indexes*/
				for (int j = 0; j < i; j++) {
					if (index == indexesChosen[j]) {
						equal = true;
						break;
					}
				}
			}
			indexesChosen[i] = index;
		}
		return indexesChosen;
	}
	
	/**
	 * Construct the array of the indexes of the nearest neighbors of the
	 * input instance.
	 * 
	 * @param instanceID The index of the input instance.
	 * @return
	 */
	private int[] nearestNeighborIDs(int instanceID, int[] instancesIDs) {
		int nearestNeighborIDs[] = new int[k];
		int index;
		int idx = instancesIDs[instanceID];
		
		for (int i = 0; i < k; i++) {
			index = (int) nearesNeighborsIDs[idx][2 * i];
			if (index == -1) {
				index = -1;
			}
			nearestNeighborIDs[i] = index;
		}
		return nearestNeighborIDs;
	}

	public void buildNN(int k, int classValue) throws Exception {
		this.k = k;
		/* 
		 * 'nearesNeighborsIDs[i][j]' - j:
		 * evens - nn ID
		 * odds - nn distance
		 */
		nearesNeighborsIDs = new float[numInstances][2 * k];
		
		/* File with nearest neighbors to open */
		File fileTest;
		if (optionDistanceFunction == HAMMING) {
			fileTest = new File("c:/nearestHamming" + classValue + ".idx");
		} else {
			fileTest = new File("c:/nearestHVDM" + classValue + ".idx");
		}
		RandomAccessFile file;
		
		if(fileTest.exists()) {
			/* Initialize from existing file */
			file = new RandomAccessFile(fileTest, "r");
			for (int i = 0; i < numInstances; i++) {
				for (int d = 0; d < 2 * k; d += 2) {
					nearesNeighborsIDs[i][d] = file.readInt();
					nearesNeighborsIDs[i][d + 1] = file.readFloat();
				}
			}
			/* Return with the nearest neighbors read from file */
			return;
		}

		/* Initialize 'nearesNeighborsIDs */
		for (int i = 0; i < numInstances; i++) {
			for (int j = 0; j < 2 * k; j += 2) {
				nearesNeighborsIDs[i][j] = -1;
				nearesNeighborsIDs[i][j + 1] = Float.POSITIVE_INFINITY;
			}
		}
		
		/* Get distance function */
		Distance distance;
		int normFactor = 4;
		distance = new HVDM(instanceSet, normFactor);
		((HVDM) distance).setOptionDistanceFunction(optionDistanceFunction);
		
		float dist = 0;
		int distGreaterID = 0;
		float distGreater;
		for (int i = 0; i < numInstances; i++) {
			if (instances[i].data[classIndex] == classValue) {
				distGreater = Float.POSITIVE_INFINITY;
				for (int j = 0; j < numInstances; j++) {
					if (i != j && instances[j].data[classIndex] == classValue) {
						dist = distance.distanceValue(instances[i].data, instances[j].data);
						if (dist < distGreater) {
							nearesNeighborsIDs[i][distGreaterID + 1] = dist;
							nearesNeighborsIDs[i][distGreaterID] = j;
							distGreater = 0;
							
							/* Find the greatest distance */
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
		
		/* Save nearestneighbors */
		file = new RandomAccessFile(fileTest, "rw");
		for (int i = 0; i < numInstances; i++) {
			for (int d = 0; d < 2 * k; d += 2) {
				file.writeInt((int) nearesNeighborsIDs[i][d]);
				file.writeFloat(nearesNeighborsIDs[i][d + 1]);
			}
		}
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
	
}