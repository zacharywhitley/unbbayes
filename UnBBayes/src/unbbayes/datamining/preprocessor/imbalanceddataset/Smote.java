package unbbayes.datamining.preprocessor.imbalanceddataset;

import java.util.Date;
import java.util.Random;

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
	
	private int nearestNeighborsIDs[][];
	
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

	private Random random;

	/**
	 * SMOTE oversamples a specified class of a dataset. It creates new cases
	 * based on the existing ones. These new cases are randomly interpolated
	 * between each instance and its k nearest neighbors, also chosen by random.
	 * 
	 * @param instancesIDs[]: The chosen subset of instances to be smoted
	 * @param mTree: index used to find nearest neighbors
	 */
	public Smote(MTree mTree) {
		this.mTree = mTree;
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
	 * between each instance and its k nearest neighbors, also chosen by
	 * random.
	 * 
	 * @param proportion: Desired proportion of new instances
	 * @param classValue: class of desired nearest neighbors
	 */
	public void run(InstanceSet instanceSet, int classValue, double proportion) {
		setInstanceSet(instanceSet);
		
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
	 * SMOTE oversamples a complete dataset. It creates new cases
	 * based on the existing ones. These new cases are randomly interpolated
	 * between each instance and its k nearest neighbors, also chosen by random.
	 * 
	 * @param proportion: Desired proportion of new instances
	 */
	public void run(double proportion) {
		int instancesIDs[] = new int[numInstances];
		
		for (int inst = 0; inst < numInstances; inst++) {
			instancesIDs[inst] = inst;
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
	public int[] run(int[] instancesIDs, double proportion) {
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
		if (k < 0) exceptionMsg = "k must be greater than 0!";
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
			int aux = (int) (numInstancesIDs * proportion) + 1;
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
//			nearestNeighborsIDs = mTree.nearestNeighborIDs(i, k);
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

			/* Chooses randomly one of the k nearest neighbor */
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
		int indexesChosen[] = new int[numNewInstances];

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
		
		return indexesChosen;
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
		
		for (int i = 0; i < k; i++) {
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
		for (int i = 0; i < k; i++) {
			index = nearestNeighborsIDs[instanceID][i];
			if (index > 0) {
				nearestNeighborIDs[count] = index;
				++count;
			}
		}

		return nearestNeighborIDs;
	}

	public void buildNN(int k, int classValue) throws Exception {
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
		
		buildNN(instancesIDs, k);
	}

	/**
	 * SMOTE oversamples a complete dataset. It creates new cases
	 * based on the existing ones. These new cases are randomly interpolated
	 * between each instance and its k nearest neighbors, also chosen by random.
	 * 
	 * @param proportion: Desired proportion of new instances
	 * @throws Exception 
	 */
	public void buildNN(int k) throws Exception {
		int instancesIDs[] = new int[numInstances];
		
		for (int inst = 0; inst < numInstances; inst++) {
			instancesIDs[inst] = inst;
		}
		
		buildNN(instancesIDs, k);
	}

	public int[][] buildNN(int[] instancesIDs, int k)
	throws Exception {
		this.k = k;
		int numInstancesIDs = instancesIDs.length;

		nearestNeighborsIDs = new int[numInstancesIDs][k];
		float[][] nearestNeighborsDistance = new float[numInstancesIDs][k];
		
//		/* File with nearest neighbors to open */
//		File fileTest;
//		if (optionDistanceFunction == HAMMING) {
//			fileName = fileName + "_nearestHamming_" + classValue + ".idx";
//		} else {
//			fileName = fileName + "_nearestHVDM_" + classValue + ".idx";
//		}
//		fileTest = new File(fileName);
//		RandomAccessFile file;
//		
//		if(fileTest.exists()) {
//			/* Initialize from existing file */
//			file = new RandomAccessFile(fileTest, "r");
//			for (int i = 0; i < numInstancesIDs; i++) {
//				for (int d = 0; d < 2 * k; d += 2) {
//					nearestNeighborsIDs[i][d] = file.readInt();
//					nearestNeighborsIDs[i][d + 1] = file.readFloat();
//				}
//			}
//			/* Return with the nearest neighbors read from file */
//			return;
//		}

		/* Initialize 'nearestNeighborsIDs */
		for (int i = 0; i < numInstancesIDs; i++) {
			for (int j = 0; j < k; j++) {
				nearestNeighborsIDs[i][j] = -1;
				nearestNeighborsDistance[i][j] = Float.POSITIVE_INFINITY;
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
		int instI;
		int instJ;
		for (int i = 0; i < numInstancesIDs; i++) {
			instI = instancesIDs[i];
			distGreater = Float.POSITIVE_INFINITY;
			for (int j = 0; j < numInstancesIDs; j++) {
				instJ = instancesIDs[j];
				dist = distance.distanceValue(instances[instI].data,
						instances[instJ].data);
				/* Check if the 'instJ' is a nearest neighbor */
				if (dist < distGreater) {
					/* 
					 * We've found a new nearest neighbor. Pop the farthest
					 * neighbor up and insert this new nearest neighbor.
					 */
					nearestNeighborsIDs[i][distGreaterID] = instJ;
					nearestNeighborsDistance[i][distGreaterID] = dist;
					distGreater = 0;
					
					/* 
					 * Find the new farthest neighbor and its distance from
					 * 'intI'.
					 */
					for (int d = 0; d < k; d++) {
						if (distGreater < nearestNeighborsDistance[i][d]) {
							distGreater = nearestNeighborsDistance[i][d];
							nearestNeighborsIDs[i][distGreaterID] = instJ;
							distGreaterID = d;
						}
					}
				}
			}
		}
		
//		/* Save nearestneighbors */
//		file = new RandomAccessFile(fileTest, "rw");
//		for (int i = 0; i < numInstancesIDs; i++) {
//			for (int d = 0; d < 2 * k; d += 2) {
//				file.writeInt((int) nearestNeighborsIDs[i][d]);
//				file.writeFloat(nearestNeighborsIDs[i][d + 1]);
//			}
//		}
		return nearestNeighborsIDs;
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
	 * @param nearestNeighborsIDs the nearestNeighborsIDs to set
	 */
	public void setNearestNeighborsIDs(int[][] nearestNeighborsIDs) {
		this.nearestNeighborsIDs = nearestNeighborsIDs;
	}

}