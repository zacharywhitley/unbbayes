package unbbayes.datamining.clustering;

import java.util.ArrayList;

import unbbayes.datamining.datamanipulation.Instance;
import unbbayes.datamining.datamanipulation.InstanceSet;

/**
 * Implements common features for data clustering methods. All data clustering
 * methods classes must extend this class.
 * 
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 2006/10/06
 */
public abstract class Clustering {
	/** The current instanceSet */
	protected InstanceSet instanceSet;

	/** The current set of input instances */
	protected Instance[] instances;

	/** Number of instances in the instance set */
	protected int numInstances;

	/** Number of attributes in the instance set */
	protected int numAttributes;
	
	/** Number of numeric attributes in the instance set */
	protected int numNumericAttributes;
	
	/** Number of nominal attributes in the instance set */
	protected int numNominalAttributes;
	
	/** Number of cyclic attributes in the instance set */
	protected int numCyclicAttributes;
	
	/** 
	 * Stores the type of an attribute:
	 * 0 - Numeric
	 * 1 - Nominal
	 * 2 - Cyclic numeric
	 */
	protected byte[] attributeType;

	/** Constant set for numeric attributes. */
	protected final static byte NUMERIC = 0;

	/** Constant set for nominal attributes. */
	protected final static byte NOMINAL = 1;

	/** Constant set for cyclic numeric attributes. */
	protected final static byte CYCLIC = 2;

	/** The index of the  counter attribute */
	protected int counterIndex;

	/** Stores the IDs of the instances for clustering */
	protected int[] instancesIDs;

	/** Matrix of all clusters. Each row stores all instancesIDs of a cluster */
	protected int[][] clusters;
	
	/** Number of clusters created */
	protected int numClusters = 0;
	
	/** Weighted number of instances in each cluster created */
	protected double[] clustersSize;

	/**
	 * Data assignment to clusters. Each position corresponds to the cluster id
	 * of an instance in the same order it appears in the input instanceSet.
	 */
	protected int[] assignmentMatrix;

	private void initialize() {
		instances = instanceSet.instances;
		numInstances = instanceSet.numInstances;
		numAttributes = instanceSet.numAttributes;
		numNumericAttributes = instanceSet.numNumericAttributes;
		numNominalAttributes = instanceSet.numNominalAttributes;
		numCyclicAttributes = instanceSet.numCyclicAttributes;
		counterIndex = instanceSet.counterIndex;
		attributeType = instanceSet.attributeType;
		clusters = null;
		clustersSize = null;
		assignmentMatrix = null;
	}
	
	public void clusterize() throws Exception {
		/* Initialize necessary fields */
		initialize();
		
		/* Choose all instances */
		instancesIDs = new int[numInstances];
		for (int i = 0; i < numInstances; i++) {
			instancesIDs[i] = i;
		}

		run();
		buildClustersInfo();
	}

	public void clusterize(int[] instancesIDs) throws Exception {
		this.instancesIDs = instancesIDs;

		/* Initialize necessary fields */
		initialize();
		
		/* Set the total number of instances for the clustering process */
		numInstances = instancesIDs.length;
		
		run();
		buildClustersInfo();
	}

	public void clusterize(int classValue) throws Exception {
		/* Initialize necessary fields */
		initialize();
		
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
		
		run();
		buildClustersInfo();
	}

	/**
	 * Build the resulting arrays with detailed clusters information. The
	 * information are stored in the following four fields:
	 * <code>clusters,numClusters, clustersSize, assignmentMatrix</code> 
	 * @throws Exception 
	 */
	public void buildClustersInfo() throws Exception {
		if (numClusters < 1) {
			throw new Exception("Number of cluster is less than 1!"); 
		}
		
		if (assignmentMatrix == null) {
			throw new Exception("The assignmentMatrix is null!"); 
		}
		
		/* 
		 * Build the clusters matrix, wich is the matrix of all clusters. Each
		 * row stores all instancesIDs of a cluster.
		 */
		if (clusters == null) {
			clusters = new int[numClusters][];
			ArrayList<Integer>[] clustersTemp = new ArrayList[numClusters];
			for (int clusterID = 0; clusterID < numClusters; clusterID++) {
				clustersTemp[clusterID] = new ArrayList<Integer>();
			}
			int clusterID;
			for (int inst = 0; inst < assignmentMatrix.length; inst++) {
				clusterID = assignmentMatrix[inst];
				if (clusterID != -1) {
					clustersTemp[clusterID].add(inst);
				}
			}
			int clusterQtd;
			int inst;
			for (clusterID = 0; clusterID < numClusters; clusterID++) {
				clusterQtd = clustersTemp[clusterID].size();
				clusters[clusterID] = new int[clusterQtd];
				for (int i = 0; i < clusterQtd; i++) {
					inst = clustersTemp[clusterID].get(i).intValue();
					clusters[clusterID][i] = inst;
				}
			}
		}

		/* Build the array with the weighted size of all clusters */
		if (clustersSize == null) {
			clustersSize = new double[numClusters];
			int clusterSize;
			int inst;
			int clusterQtd;
			for (int clusterID = 0; clusterID < numClusters; clusterID++) {
				clusterQtd = clusters[clusterID].length;
				clusterSize = 0;
				for (int i = 0; i < clusterQtd; i++) {
					inst = clusters[clusterID][i];
					clusterSize += instances[inst].data[counterIndex];
				}
				clustersSize[clusterID] = clusterSize;
			}
		}
	}

	/**
	 * Run the clusterizing algorithm with the specified parameters set. Each
	 * algorithm has its own set of parameters (see the specific set methods
	 * for each type of algorithm).
	 */
	protected abstract void run() throws Exception;

	/**
	 * Returns a matrix containing all clusters. Each row stores all
	 * instancesIDs of a cluster.
	 * 
	 * @return The clusters created.
	 */
	public int[][] getClusters() {
		return clusters;
	}

	/**
	 * Returns the number of clusters created.
	 * 
	 * @return The number of clusters created.
	 */
	public int getNumClusters() {
		return numClusters;
	}

	/** 
	 * Returns the weighted number of instances in each cluster created.
	 * 
	 * @return The size of each cluster.
	 */
	public double[] getClustersSize() {
		return clustersSize;
	}

	/**
	 * Returns the data assignment to clusters. Each position corresponds to
	 * the cluster id of an instance in the same order it appears in the input
	 * instanceSet.
	 * 
	 * @return The number of clusters created.
	 */
	public int[] getAssignmentMatrix() {
		return assignmentMatrix;
	}
	
}