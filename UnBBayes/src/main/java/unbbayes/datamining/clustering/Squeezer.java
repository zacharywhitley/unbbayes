package unbbayes.datamining.clustering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import unbbayes.datamining.datamanipulation.InstanceSet;

/**
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 02/11/2006
 */
public class Squeezer extends Clustering {
	/** 
	 * Stores the summaries. Each row is a summaries of a cluster. Each column
	 * holds a VS for each nominal attribute. A VS is an array list of one
	 * dimensional float vectors. Each one of these float vectors hold two
	 * values: an attribute value and the frequency that it occurs in the
	 * cluster.
	 */
	private ArrayList<float[]>[][] summaries;

	/** 
	 * Set the weight of each nominal attribute. Each position refers to a an
	 * attribute.
	 */
	double[] weight;

	private double s;

	/**
	 * Set the similarity threshold automatically:
	 * True - set automatically;
	 * False - set manually.
	 */
	private boolean useAverageSimilarity;

	/**
	 * Default constructor for this class.
	 * 
	 * @param instanceSet
	 */
	public Squeezer(InstanceSet instanceSet) {
		this.instanceSet = instanceSet;
	}

	protected void run() throws Exception {
		/* Check if there is at least one nominal attribute */
		if (numNominalAttributes < 1 &&
				numCyclicAttributes < 1) {
			String exceptionMsg;
			exceptionMsg = "Squeezer needs at least one nominal attribute";
			throw new Exception(exceptionMsg);
		}
		
		assignmentMatrix = new int[instanceSet.numInstances];
		summaries = new ArrayList[numInstances][numNominalAttributes];
		clustersSize = new double[numInstances];
		
		/* Check if the attributes' weight has been set */
		if (weight == null) {
			weight = new double[numNominalAttributes];
			for (int att = 0; att < numNominalAttributes; att++) {
				weight[att] = 1;
			}
		}
		
		/* Check if the similarity threshold is to set automatically */
		if (useAverageSimilarity) {
			int max = 1000;
			if (numInstances < max) {
				max = numInstances;
			}
			
			s = averageSimilarity((double) max / numInstances) + 1.5;
		}
		
		/* Initialize the assignment matrix */
		Arrays.fill(assignmentMatrix, -1);
		
		/* Add the first instance to the first cluster */
		numClusters = 0;
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
		
		/* Correct the clustersSize array */
		double[] clustersSizeTmp = new double[numClusters];
		System.arraycopy(clustersSize, 0, clustersSizeTmp, 0, numClusters);
		clustersSize = clustersSizeTmp;
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
		clustersSize[clusterID] += instances[inst].data[counterIndex];
		
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
		clustersSize[numClusters] = instances[inst].data[counterIndex];
		
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
						match += weight[attIndex] * vS[attIndex].get(v)[1];
					}
				}
				sim += (float) (match / (double) clustersSize[clusterID]);
				++attIndex;
			}
		}
		
		return sim;
	}
	
	/**
	 * Computes the average similarity among a sample of the data. This value
	 * can be used as guide for choosing the similarity threshold that will be
	 * used in the clusterization of the instanceSet. The idea is very simple:
	 * 1 - Get a sample of the instanceSet;
	 * 2 - Compute the similarity between each pair of this subset;
	 * 3 - Return the average of all similarities calculated in step 2.
	 * 
	 * @param percentage Desired percentage of the instanceSet utilized in this
	 * calculation. 
	 * @return Average similarity.
	 */
	public double averageSimilarity(double proportion) {
		int numCandidates = (int) Math.round(numInstances * proportion);
		int candidates[] = new int[numCandidates];
		Random randomizer = new Random();
		int counter = 0;
		int inst;
		boolean[] exists = new boolean[instanceSet.numInstances];
		
		Arrays.fill(exists, false);

		/* Randomly get a sample of the instanceSet */ 
		while (counter < numCandidates) {
			/* Get the candidate */
			inst = instancesIDs[randomizer.nextInt(numInstances)];

			/* Test if the current candidate instance has been used */
			/* Add the current candidate */
			if (!exists[inst]) {
				candidates[counter] = inst;
				exists[inst] = true;
				counter++;
			}
		}
		
		/* Get the similarity average of each tuple of instances */
		float[] instanceI;
		float[] instanceJ;
		int similarity = 0;
		for (int i = 0; i < numCandidates; i++) {
			instanceI = instances[candidates[i]].data;
			for (int j = 0; j != i && j < numCandidates; j++) {
				instanceJ = instances[candidates[j]].data;
				for (int att = 0; att < numAttributes; att++) {
					if (instanceSet.attributeType[att] == InstanceSet.NOMINAL
							&& instanceI[att] == instanceJ[att]) {
						++similarity;
					}
				}
			}
		}
		
		double avgSimilarity = similarity;
		avgSimilarity /= (double) numCandidates * (numCandidates - 1);
		
		return avgSimilarity ;
	}
	
	/**
	 * Set the weight of each nominal attribute. Each position refers to a an
	 * attribute.
	 * 
	 * @param weight
	 */
	public void setWeight(double[] weight) {
		this.weight = weight;
	}
	
	public void setS(double s) {
		this.s = s;
	}

	/**
	 * Set the similarity threshold automatically:
	 * True - set automatically;
	 * False - set manually.
	 * 
	 * @param useAverageSimilarity
	 */
	public void setUseAverageSimilarity(boolean useAverageSimilarity) {
		this.useAverageSimilarity = useAverageSimilarity;
	}
	
}