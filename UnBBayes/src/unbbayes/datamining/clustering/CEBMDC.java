package unbbayes.datamining.clustering;

import java.util.ArrayList;
import java.util.Arrays;

import unbbayes.datamining.datamanipulation.Instance;
import unbbayes.datamining.datamanipulation.InstanceSet;

/**
 * CEBMDC (Cluster Ensemble Based Mixed Data Clustering). This framework was
 * proposed by Zengyou He, Xiaofe I Xu and Shengchum Deng. It clusters both
 * nominal and numeric data. The idea behind it is to construct a meta cluster
 * of the output of two different clustering algorithm. First, the original 
 * dataset is divided in two subsets: a pure numeric dataset and a pure nominal
 * dataset. Then, these two subsets are clusterized by any of the many existing
 * clustering algorithms. Finally, the cluster results of both subsets are
 * combined and clusterized as a nominal set.
 * The authors utilized in their framework the Squeezer algorithm for clustering
 * nominal datasets. The same algorithm is used in this implementation as well.
 * More info: http://arxiv.org/ftp/cs/papers/0509/0509011.pdf
 * 
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 07/11/2006
 */
public class CEBMDC {
	/** The current instanceSet */
	private InstanceSet instanceSet;

	/** The current set of input instances */
	private Instance[] instances;

	/** The current set of input instances */
	private int[][] inputClusters;

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

	/** The index of the  counter attribute */
	private int counterIndex;

	/** Number of clusters created */
	private int numClusters = 0;

	/** Size of each cluster created */
	private double clusterSize[];
	
	/** 
	 * Weight of the attributes in the final cluster construction:
	 * <ul>
	 * <li><code>weight[0]</code>: number of numeric attributes.
	 * <li><code>weight[1]</code>: number of nominal attributes.
	 * </ul>
	 */
	float[] weight;
	
	/**
	 * 
	 * @param instanceSet
	 */
	public CEBMDC(InstanceSet instanceSet) {
		this.instanceSet = instanceSet;
		instances = instanceSet.instances;
		numInstances = instanceSet.numInstances;
		counterIndex = instanceSet.counterIndex;
	}

	/**
	 * 
	 * @param s
	 * @return
	 */
	public int[] clusterize(int[] numericClusters, int[] nominalClusters,
			float[] weight, float s) {
		/* Message thrown as an exception in case of wrong arguments */
		boolean exception = false;
		String exceptionMsg = "The numeric and nominal clusters are not from" +
				" the same instances!";

		/* Check if numeric and nominal clusters are from the same instances */
		if (numericClusters.length != nominalClusters.length) {
			exception = true;
		} else {
			for (int i = 0; i < numericClusters.length; i++) {
				if (numericClusters[i] == -1 && nominalClusters[i] != -1) {
					exception = true;
				} 
				if (numericClusters[i] != -1 && nominalClusters[i] == -1) {
					exception = true;
				}
			}
		}

		/* Throw exception if there is problems with the parameters */
		if (exception) throw new IllegalArgumentException(exceptionMsg);
		
		/* Merge numeric and nominal clusters together */
		numInstances = numericClusters.length;
		inputClusters = new int[numInstances][2];
		for (int i = 0; i < numInstances; i++) {
			inputClusters[i][0] = numericClusters[i];
			inputClusters[i][1] = nominalClusters[i];
		}

		/* Create and initialize the necessary elements */
		this.weight = weight;
		assignmentMatrix = new int[numInstances];
		summaries = new ArrayList[numInstances][2];
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
			/* Check if the current instance is valid */
			if (inputClusters[inst][0] == -1) {
				/* Skip the current instance */
				continue;
			}
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
		
		return assignmentMatrix;
	}
	
	/**
	 * 
	 * @param i
	 * @param simMaxIndex
	 */
	private void addInstanceToCluster(int inst, int clusterID) {
		ArrayList<float[]>[] vS = summaries[clusterID];
		boolean newValue;
		int numValues;
		
		for (int att = 0; att < 2; att++) {
			numValues = vS[att].size();
			newValue = true;
			for (int v = 0; v < numValues; v++) {
				if (inputClusters[inst][att] == vS[att].get(v)[0]) {
					/* Just update the frequency of the attribute value */
					vS[att].get(v)[1] += instances[inst].data[counterIndex];
					newValue = false;
					break;
				}
			}
			if (newValue) {
				/* Add this new attribute value to the summary */
				float[] vSi = new float[2];
				vSi[0] = inputClusters[inst][att];
				vSi[1] = instances[inst].data[counterIndex];
				vS[att].add(vSi);
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
	private void addNewClusterStructure(int inst) {
		for (int att = 0; att < 2; att++) {
			float[] vSi = new float[2];
			vSi[0] = inputClusters[inst][att];
			vSi[1] = instances[inst].data[counterIndex];
			ArrayList<float[]> vS = new ArrayList<float[]>();
			vS.add(vSi);
			summaries[numClusters][att] = vS;
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
	private float similarity(int clusterID, int inst) {
		ArrayList<float[]>[] vS = summaries[clusterID];
		float sim = 0;
		double match;
		int numValues;
		
		for (int att = 0; att < 2; att++) {
			match = 0;
			numValues = vS[att].size();
			for (int v = 0; v < numValues; v++) {
				if (inputClusters[inst][att] == vS[att].get(v)[0]) {
					match += weight[att] * vS[att].get(v)[1];
				}
			}
			sim += (float) (match / (double) clusterSize[clusterID]);
		}
		
		return sim;
	}

	/**
	 * Returns the number of clusters created.
	 * 
	 * @return The number of clusters created.
	 */
	public int getNumClusters() {
		return numClusters;
	}
}