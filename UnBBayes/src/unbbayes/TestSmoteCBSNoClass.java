package unbbayes;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import unbbayes.datamining.clustering.CEBMDC;
import unbbayes.datamining.clustering.Kmeans;
import unbbayes.datamining.clustering.Squeezer;
import unbbayes.datamining.datamanipulation.AttributeStats;
import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.datamanipulation.TxtLoader;
import unbbayes.datamining.distance.Euclidean;
import unbbayes.datamining.distance.IDistance;
import unbbayes.datamining.preprocessor.imbalanceddataset.Smote;

/**
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 11/10/2006
 */
public class TestSmoteCBSNoClass {
	public TestSmoteCBSNoClass() {
		try {
			run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new TestSmoteCBSNoClass();
	}

	public void run() throws Exception {
		/* Data set characteristics */
		String trainFileName = "c:/hugo.txt";
		byte classIndex = -1;
		byte counterIndex = -1;

		/* Opens the training set */
		InstanceSet trainData = openFile(trainFileName, counterIndex);
		if (trainData == null) {
			String exceptionMsg = "Couldn't open test data " + trainFileName;
			throw new Exception(exceptionMsg);
		}
		
		/* Number of clusters desired with numeric clusterization */ 
		int k = 5;

		/* 
		 * The minimum accepted % of change in each iteration in the numeric
		 * clusterization.
		 */
		double kError = 1.001f;

		/* Clusterize the training set */
		ArrayList clustersFramework = clusterize(trainData, k, kError);
		int[][] clusters = (int[][]) clustersFramework.get(0);
		int numClusters = (Integer) clustersFramework.get(1);
		double[] clustersSize = (double[]) clustersFramework.get(2);
		int[] assignmentMatrix = (int[]) clustersFramework.get(3);
		
//		/* Apply Cluster-Based SMOTE to the training data */
//		ClusterBasedSmote cbs = new ClusterBasedSmote(trainData);
//		cbs.setOptionDiscretize(false);
//		cbs.setOptionDistanceFunction((byte) 1);
//		cbs.setOptionFixedGap(true);
//		cbs.setOptionNominal((byte) 0);
//		trainData = cbs.run(clusters, numClusters, clustersSize, assignmentMatrix);
		
		/* Options for SMOTE - START *****************/
		Smote smote = new Smote(null);
		
		/*
		 * Set it <code>true</code> to optionDiscretize the synthetic value created for
		 * the new instance. 
		 */
		boolean optionDiscretize = false;
		
		/* 
		 * Used in SMOTE
		 * 0: copy nominal attributes from the current instance
		 * 1: copy from the nearest neighbors
		 */
		byte optionNominal = 0;
		
		/*
		 * The gap is a random number between 0 and 1 wich tells how far from the
		 * current instance and how near from its nearest neighbor the new instance
		 * will be interpolated.
		 * The optionFixedGap, if true, determines that the gap will be fix for all
		 * attributes. If set to false, a new one will be drawn for each attribute.
		 */
		boolean optionFixedGap = true;
		
		/* 
		 * Distance function
		 * 0: Hamming
		 * 1: HVDM
		 */
		byte optionDistanceFunction = 1;
		
		/* Set SMOTE options */
		smote.setOptionDiscretize(optionDiscretize);
		smote.setOptionDistanceFunction(optionDistanceFunction);
		smote.setOptionFixedGap(optionFixedGap);
		smote.setOptionNominal(optionNominal);
		
		smote.buildNN(5, classIndex);

		/* Options for SMOTE - END *****************/

		/* Compute the statistics for all attributes */
		AttributeStats[] attributeStats = trainData.getAttributeStats(false);

		/* SMOTE */
		smote.setInstanceSet(trainData);
		smote.run(3);

		@SuppressWarnings("unused")
		int ema = 0;
	}
	
	private InstanceSet openFile(String fileName, int counterIndex) throws IOException {
		File file = new File(fileName);
		TxtLoader loader = new TxtLoader(file, -1);
		
		/* If the dataset is compacted */
		loader.setCounterAttribute(counterIndex);
		
		while (loader.getInstance()) {
			/* Wait while instances are loaded */
		}
		if (loader != null) {
			InstanceSet instanceSet = loader.getInstanceSet();

			return instanceSet;
		}
		
		return null;
	}

	/**
	 * Return an array of integer with the clusterID of each instance of the
	 * class pointed by the parameter <code>classValue</code> in the order they
	 * appear in the instanceSet.
	 * 
	 * @param classValue
	 * @return
	 * @throws Exception 
	 */
	private ArrayList clusterize(InstanceSet instanceSet, int k, double kError)
	throws Exception {
		boolean numeric = false;
		boolean nominal = false;
		boolean mixed = false;
		
		/* Algorithm for clustering numeric attributes */
		if (instanceSet.numNumericAttributes > 0) {
			numeric = true;
		}

		/* Set the options for the Kmeans algorithm */
		int normFactor = 4;
		IDistance distance = new Euclidean(instanceSet, normFactor);
		Kmeans kmeans = new Kmeans(instanceSet);
		kmeans.setOptionDistance(distance);
		kmeans.setError(kError);
		kmeans.setNumClusters(k);
		
		/* Algorithm for clustering nominal attributes */
		if (instanceSet.numNominalAttributes > 0 || 
				instanceSet.numCyclicAttributes > 0) {
			nominal = true;
			if (numeric) mixed = true;
		}
		
		/* Set the options for the Squeezer algorithm */
		Squeezer squeezer = new Squeezer(instanceSet);
		squeezer.setUseAverageSimilarity(true);
		
		/* Algorithm for clustering mixed attributes */
		/* Set the options for the CEBMDC algorithm */
		CEBMDC cebmdc = new CEBMDC(instanceSet);
		double[] weight = {instanceSet.numNumericAttributes,
				instanceSet.numNominalAttributes};
		cebmdc.setWeight(weight);
		cebmdc.setUseAverageSimilarity(true);

		int[] numericClusters;
		int[] nominalClusters;
		
		int[][] clusters = null;
		int numClusters = 0;
		double[] clustersSize = null;
		int[] assignmentMatrix = null;

		/* Clusterize the numeric attributes */
		numericClusters = null;
		if (numeric) {
			kmeans.clusterize();
			numericClusters = kmeans.getAssignmentMatrix();
		}

		/* Clusterize the nominal attributes */
		nominalClusters = null;
		if (nominal) {
			squeezer.clusterize();
			nominalClusters = squeezer.getAssignmentMatrix();
		}
		
		/* Clusterize the both numeric and nominal attributes */
		if (mixed) {
			cebmdc.setNumericClustersInput(numericClusters);
			cebmdc.setNominalClustersInput(nominalClusters);
			cebmdc.clusterize();
			
			/* Get the cluster results */
			clusters = cebmdc.getClusters();
			numClusters = cebmdc.getNumClusters();
			clustersSize = cebmdc.getClustersSize();
			assignmentMatrix = cebmdc.getAssignmentMatrix();
		} else if (numeric) {
			/* Get the cluster results */
			clusters = kmeans.getClusters();
			numClusters = kmeans.getNumClusters();
			clustersSize = kmeans.getClustersSize();
			assignmentMatrix = kmeans.getAssignmentMatrix();
		} else if (nominal) {
			/* Get the cluster results */
			clusters = squeezer.getClusters();
			numClusters = squeezer.getNumClusters();
			clustersSize = squeezer.getClustersSize();
			assignmentMatrix = squeezer.getAssignmentMatrix();
		}
		
		ArrayList<Object> result = new ArrayList<Object>(4);
		result.add(0, clusters);
		result.add(1, numClusters);
		result.add(2, clustersSize);
		result.add(3, assignmentMatrix);
		
		return result;
	}

}