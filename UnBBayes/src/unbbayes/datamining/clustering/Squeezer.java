package unbbayes.datamining.clustering;

import java.util.ArrayList;
import java.util.Arrays;

import unbbayes.datamining.datamanipulation.Instance;
import unbbayes.datamining.datamanipulation.InstanceSet;

/**
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 02/11/2006
 */
public class Squeezer {
	/** The current instanceSet */
	private InstanceSet instanceSet;

	/** The current set of input instances */
	private Instance[] instances;

	/**
	 * Data assignment to summaries. Each position is a data point in the same
	 * order as they appear in the input matrix. Its value is the cluster id.
	 */
	private int[] assignmentMatrix;
	
	/** 
	 * Stores the summaries. Each row is a summaries of a cluster. Each column
	 * holds a VS for each nominal attribute. A VS is an array list of one
	 * dimensional float vectors. Each one of these float vectors hold two
	 * values: an attribute value and the frequency that it occurs in the
	 * cluster.
	 */
	private ArrayList<float[]>[][] summaries;

	/** Number of instances in the instance set */
	private int numInstances;

	/** Number of attributes in the instance set */
	private int numAttributes;
	
	/** The index of the  counter attribute */
	private int counterIndex;

	/** Stores the IDs of the instances for clustering */
	private int[] instancesIDs;

	/** Number of clusters created */
	private int numClusters = 0;

	/** Size of each cluster created */
	private double clusterSize[];

	/**
	 * 
	 * @param instanceSet
	 */
	public Squeezer(InstanceSet instanceSet) {
		this.instanceSet = instanceSet;
		instances = instanceSet.instances;
		numInstances = instanceSet.numInstances;
		numAttributes = instanceSet.numAttributes;
		counterIndex = instanceSet.counterIndex;
	}

	/**
	 * 
	 * @param s
	 * @return
	 */
	public int[] clusterize(float s) {
		/* Choose all instances */
		instancesIDs = new int[numInstances];
		for (int i = 0; i < numInstances; i++) {
			instancesIDs[i] = i;
		}

		return clusterizeAux(s);
	}

	/**
	 * 
	 * @param s
	 * @param classValue
	 * @return
	 */
	public int[] clusterize(float s, int classValue) {
		/* Choose the instancesIDs for the clustering process */
		int counter = 0;
		int instancesIDsTmp[] = new int[numInstances];
		int classIndex = instanceSet.classIndex;
		for (int inst = 0; inst < numInstances; inst++) {
			if (instances[inst].data[classIndex] == classValue) {
				instancesIDsTmp[counter] = inst;
				++counter;
			}
		}
		instancesIDs = new int[counter];
		for (int i = 0; i < counter; i++) {
			instancesIDs[i] = instancesIDsTmp[i];
		}
		
		/* Set the total number of instances for the clustering process */
		numInstances = counter;
		
		return clusterizeAux(s);
	}
	
	/**
	 * 
	 * @param s
	 * @return
	 */
	private int[] clusterizeAux(float s) {
		assignmentMatrix = new int[instanceSet.numInstances];
		summaries = new ArrayList[numInstances][instanceSet.numNominalAttributes];
		clusterSize = new double[numInstances];
		
		/* Initialize the assignment matrix */
		Arrays.fill(assignmentMatrix, -1);
		
		/* Add the first instance to the first cluster */
		addNewClusterStructure(0);

		/* For each instance */
		float sim;
		float simMax;
		int simMaxIndex = 0;
		for (int inst = 1; inst < numInstances; inst++) {
			simMax = 0;
			for (int clusterID = 0; clusterID < numClusters; clusterID++) {
				sim = similarity(clusterID, inst);
				if (sim > simMax) {
					simMax = sim;
					simMaxIndex = clusterID;
				}
			}
		
			/* Check the maximum similarity with the threshold 's' */
			if (simMax >= s) {
				/* Add the current instance to its nearest cluster */
				addInstanceToCluster(inst, simMaxIndex );
			} else {
				/* Create a new cluster with the current instance */
				addNewClusterStructure(inst);
			}
		}
		
//		// testes
//		float[][] ema = new float[numClusters][2];
//		float[][] emaTmp = new float[numClusters][2];
//		int idx;
//		for (int i = 0; i < numInstances; i++) {
//			idx = assignmentMatrix[i];
//			++ema[idx][(int) instances[i].data[instanceSet.classIndex]];
//		}
		
		return assignmentMatrix;
	}
	
	/**
	 * 
	 * @param i
	 * @param simMaxIndex
	 */
	private void addInstanceToCluster(int i, int clusterID) {
		ArrayList<float[]>[] vS = summaries[clusterID];
		int inst = instancesIDs[i];
		boolean newValue;
		int attIndex = 0;
		int numValues;
		
		for (int att = 0; att < numAttributes; att++) {
			if (instanceSet.attributeType[att] == InstanceSet.NOMINAL) {
				numValues = vS[attIndex].size();
				newValue = true;
				for (int v = 0; v < numValues; v++) {
					if (instances[inst].data[att] == vS[attIndex].get(v)[0]) {
						/* Just update the frequency of the attribute value */
						vS[attIndex].get(v)[1] += 
							instances[inst].data[counterIndex];
						newValue = false;
						break;
					}
				}
				if (newValue) {
					/* Add this new attribute value to the summary */
					float[] vSi = new float[2];
					vSi[0] = instances[inst].data[att];
					vSi[1] = instances[inst].data[counterIndex];
					vS[attIndex].add(vSi);
				}
				++attIndex;
			}
		}
		
		/* Update the cluster size */
		clusterSize[clusterID] += instances[inst].data[counterIndex];
		
		/* Label the instance with this clusterID */
		assignmentMatrix[inst] = clusterID;
	}

	/**
	 * 
	 * @param i
	 */
	private void addNewClusterStructure(int i) {
		int inst = instancesIDs[i];
		int attIndex = 0;
		
		for (int att = 0; att < numAttributes; att++) {
			if (instanceSet.attributeType[att] == InstanceSet.NOMINAL) {
				float[] vSi = new float[2];
				vSi[0] = instances[inst].data[att];
				vSi[1] = instances[inst].data[counterIndex];
				ArrayList<float[]> vS = new ArrayList<float[]>();
				vS.add(vSi);
				summaries[numClusters][attIndex] = vS;
				++attIndex;
			}
		}

		/* Update the cluster size */
		clusterSize[numClusters] = instances[inst].data[counterIndex];
		
		/* Label the instance with their respective clusterID */
		assignmentMatrix[inst] = numClusters;
		
		++numClusters;
	}

	/**
	 * 
	 * @param clusterID
	 * @param i
	 * @return
	 */
	private float similarity(int clusterID, int i) {
		ArrayList<float[]>[] vS = summaries[clusterID];
		int inst = instancesIDs[i];
		float sim = 0;
		double match;
		int attIndex = 0;
		int numValues;
		
		for (int att = 0; att < numAttributes; att++) {
			if (instanceSet.attributeType[att] == InstanceSet.NOMINAL) {
				match = 0;
				numValues = vS[attIndex].size();
				for (int v = 0; v < numValues; v++) {
					if (instances[inst].data[att] == vS[attIndex].get(v)[0]) {
						match += vS[attIndex].get(v)[1];
					}
				}
				sim += (float) (match / (double) clusterSize[clusterID]);
				++attIndex;
			}
		}
		
		return sim;
	}
}

