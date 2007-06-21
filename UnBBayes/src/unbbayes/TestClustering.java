package unbbayes;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import unbbayes.datamining.clustering.CEBMDC;
import unbbayes.datamining.clustering.Kmeans;
import unbbayes.datamining.clustering.Squeezer;
import unbbayes.datamining.datamanipulation.ArffLoader;
import unbbayes.datamining.datamanipulation.ArffSaver;
import unbbayes.datamining.datamanipulation.Instance;
import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.datamanipulation.Loader;
import unbbayes.datamining.datamanipulation.TxtLoader;
import unbbayes.datamining.distance.Euclidean;
import unbbayes.datamining.distance.IDistance;

/**
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 11/10/2006
 */
public class TestClustering {
	public TestClustering() {
		try {
			run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new TestClustering();
	}

	public void run() throws Exception {
		/* Data set characteristics */
		String inputFileName = "c:/dados/m1Original.arff";
		String trainFileName = "c:/dados/sampleados/m1tClusterized.arff";
		String testFileName = "c:/dados/sampleados/m1avClusterized.arff";
		byte classIndex = 10;
		byte counterIndex = 11;

		/* Opens the training set */
		InstanceSet instanceSet = openFile(inputFileName, counterIndex);
		if (instanceSet == null) {
			String exceptionMsg = "Couldn't open test data " + inputFileName;
			throw new Exception(exceptionMsg);
		}
		
		instanceSet.setClassIndex(classIndex);
		
		/* Number of clusters desired with numeric clusterization */ 
		int k = 5;

		/* 
		 * The minimum accepted % of change in each iteration in the numeric
		 * clusterization.
		 */
		double kError = 1.001f;

		boolean numeric = false;
		boolean nominal = false;
		boolean mixed = false;
		
		/* Algorithm for clustering numeric attributes */
		if (instanceSet.numNumericAttributes > 0 || 
				instanceSet.numCyclicAttributes > 0) {
			numeric = true;
		}

		/* Check if there are nominal attributes */
		if (instanceSet.numNominalAttributes > 0 &&
				instanceSet.attributes[classIndex].isNominal()) {
			nominal = true;
		} else if (instanceSet.numNominalAttributes > 1) {
			nominal = true;
		}

		/* Algorithm for clustering nominal attributes */
		if (nominal && numeric) {
			mixed = true;
		}
		
		Squeezer squeezer = null;
		Kmeans kmeans = null;
		
		/* Algorithm for clustering mixed attributes */
		/* Set the options for the CEBMDC algorithm */
		CEBMDC cebmdc = new CEBMDC(instanceSet);
		double[] weight = {instanceSet.numNumericAttributes,
				instanceSet.numNominalAttributes};
		cebmdc.setWeight(weight);
		cebmdc.setUseAverageSimilarity(true);

		int[] numericClusters = null;
		int[] nominalClusters = null;
		
		int numClusters = 0;
		int[][] clusters = null;
		double[] clustersSize = null;
		int[] assignmentMatrix = null;

		/* Discover the majority class */
		int numClasses = instanceSet.numClasses();
		int numInstances = instanceSet.numInstances;
		double[] count = new double[numClasses];
		for (int classValue, inst = 0; inst < numInstances; inst++) {
			classValue = (int) instanceSet.instances[inst].data[classIndex];
			count[classValue] += instanceSet.instances[inst].data[counterIndex];
		}
		double majorityClassQtd = 0;
		int majorityClass = 0;
		for (int c = 0; c < numClasses; c++) {
			if (count[c] > majorityClassQtd) {
				majorityClassQtd = count[c];
				majorityClass = c;
			}
		}
		
		/* Clusterize the numeric attributes */
		numericClusters = null;
		if (numeric) {
			/* Set the options for the Kmeans algorithm */
			int normFactor = 4;
			IDistance distance = new Euclidean(instanceSet, normFactor, false);
			kmeans = new Kmeans(instanceSet);
			kmeans.setOptionDistance(distance);
			kmeans.setError(kError);
			kmeans.setNumClusters(k);
			kmeans.clusterize(majorityClass);
			numericClusters = kmeans.getAssignmentMatrix();
		}

		/* Clusterize the nominal attributes */
		nominalClusters = null;
		if (nominal) {
			/* Set the options for the Squeezer algorithm */
			squeezer = new Squeezer(instanceSet);
			squeezer.setUseAverageSimilarity(true);
			squeezer.clusterize(majorityClass);
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

		/* Pick the biggest cluster of the majority class */
		int biggestClusterIndex = 0;
		double biggestClusterSize = 0;
		for (int clusterID = 0; clusterID < numClusters; clusterID++) {
			if (clustersSize[clusterID] > biggestClusterSize) {
				biggestClusterSize = clustersSize[clusterID];
				biggestClusterIndex = clusterID;
			}
		}
		
		/* Create the new instanceSet */
		int minorityClassQtd = instanceSet.numWeightedInstances - (int) majorityClassQtd;
		int newInstanceSetSize = minorityClassQtd + (int) biggestClusterSize;
		InstanceSet newInstanceSet = new InstanceSet(instanceSet, newInstanceSetSize);
		int index;
		for (int inst = 0; inst < biggestClusterSize; inst++) {
			index = clusters[biggestClusterIndex][inst];
			Instance newInstance = new Instance(instanceSet.instances[index]);
			newInstanceSet.insertInstance(newInstance);
		}
		float[] data;
		for (int inst = 0; inst < numInstances; inst++) {
			data = instanceSet.instances[inst].data;
			if (data[classIndex] != majorityClass) {
				Instance newInstance = new Instance(instanceSet.instances[inst]);
				newInstanceSet.insertInstance(newInstance);
			}
		}
		
		/* Build the test and training instanceSets */
		float testProportion = (float) 1 / 3;
		InstanceSet testData = newInstanceSet.buildTrainTestSet(testProportion, false, classIndex);
		InstanceSet trainData = newInstanceSet;
		
		/* Select all attributes to be saved */
		int[] selectedAttributes = new int[newInstanceSet.numAttributes];
		for (int att = 0; att < newInstanceSet.numAttributes; att++) {
			selectedAttributes[att] = att;
		}
		
		/* Save the training instance */
		saveFile(trainFileName, trainData, selectedAttributes, true);
		
		/* Save the test instance */
		saveFile(testFileName, testData, selectedAttributes, true);
	}
	
	private InstanceSet openFile(String fileName, int counterIndex) throws IOException {
		File file = new File(fileName);
		Loader loader = null;
		
		if (fileName.regionMatches(true, fileName.length() - 5, ".arff", 0, 5)) {
        	loader = new ArffLoader(file, -1);
        } else if (fileName.regionMatches(true, fileName.length() - 4, ".txt", 0, 4)) {
        	loader = new TxtLoader(file, -1);
        }

		/* If the dataset is compacted */
		if (counterIndex != -1) {
			loader.setCounterAttribute(counterIndex);
		}
		
		while (loader.getInstance()) {
			/* Wait while instances are loaded */
		}
		if (loader != null) {
			InstanceSet instanceSet = loader.getInstanceSet();

			return instanceSet;
		}
		
		return null;
	}

	private void saveFile(String fileName, InstanceSet instanceSet,
			int[] selectedAttributes, boolean compacted) 
	throws IOException {
		File file = new File(fileName);
		ArffSaver saver = new ArffSaver(file, instanceSet, selectedAttributes, 
				compacted);
		
		/* Save the instances one by one */
		while (saver.setInstance()) {
			/* Wait while instances are saved */
		}
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
		IDistance distance = new Euclidean(instanceSet, normFactor, false);
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