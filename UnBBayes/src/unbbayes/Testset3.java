package unbbayes;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import unbbayes.datamining.classifiers.Classifier;
import unbbayes.datamining.classifiers.DistributionClassifier;
import unbbayes.datamining.classifiers.Evaluation;
import unbbayes.datamining.classifiers.NaiveBayes;
import unbbayes.datamining.classifiers.decisiontree.C45;
import unbbayes.datamining.datamanipulation.ArffLoader;
import unbbayes.datamining.datamanipulation.ArffSaver;
import unbbayes.datamining.datamanipulation.Instance;
import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.datamanipulation.Loader;
import unbbayes.datamining.datamanipulation.TxtLoader;
import unbbayes.datamining.preprocessor.imbalanceddataset.ClusterBasedSmote;
import unbbayes.datamining.preprocessor.imbalanceddataset.Sampling;
import unbbayes.datamining.preprocessor.imbalanceddataset.Smote;

/**
 * Class for testing the formalism Cluster-Based SMOTE.
 * 
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 24/08/2006
 */
public class Testset3 {
	
	float maxSE = 0;
	String maxSEHeader;
	String header = "";
	Smote smote;
	private int classIndex;
	private int counterIndex;
	private String trainFileName;
	private int numInstances;
	private int minorityClassQtd;
	private int minorityClass;
	private InstanceSet instanceSet;
	private String extension;
	private String testFileName;
	
	public Testset3() {
		try {
			run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		new Testset3();
	}

	public void run() throws Exception {
		/* Data set characteristics */
		String inputFileName = "c:/dados/m1Original - var59 num.arff";
		trainFileName = "c:/dados/sampleados/m1t";
		testFileName = "c:/dados/sampleados/m1av";
		extension = ".arff";
		classIndex = 10;
		counterIndex = 11;
		
		/* Opens the input set */
		instanceSet = openFile(inputFileName, counterIndex);
		if (instanceSet == null) {
			String exceptionMsg = "Couldn't open training data " 
				+ trainFileName;
			throw new Exception(exceptionMsg);
		}
		instanceSet.setClassIndex(classIndex);
		
		/* Discover the minority class */
		int numClasses = instanceSet.numClasses();
		numInstances = instanceSet.numInstances;
		int[] count = new int[numClasses];
		for (int classValue, inst = 0; inst < numInstances; inst++) {
			classValue = (int) instanceSet.instances[inst].data[classIndex];
			count[classValue] += instanceSet.instances[inst].data[counterIndex];
		}
		minorityClassQtd = Integer.MAX_VALUE;
		for (int c = 0; c < numClasses; c++) {
			if (count[c] < minorityClassQtd) {
				minorityClassQtd = count[c];
				minorityClass = c;
			}
		}
		
		/* Loop through all sample strategies */
		for (int i = 10000; i <= 100000; i += 10000) {
			/* Sample training data */
			sampling(i);
		}
	}
	
	private void sampling(int i) throws IOException {
		/* Create the new instanceSet */
		int newInstanceSetSize = minorityClassQtd + i;
		InstanceSet newInstanceSet = new InstanceSet(instanceSet, newInstanceSetSize);
		
		/* Fill with the minority class examples */
		float[] data;
		int inst = 0;
		int counter = 0;
		while (counter < minorityClassQtd) {
			data = instanceSet.instances[inst].data;
			if (data[classIndex] == minorityClass) {
				Instance newInstance = instanceSet.instances[inst].clone();
				newInstanceSet.insertInstance(newInstance);
				++counter;
			}
			++inst;
		}
		
		/* Randomly samples the majority class */
		Random randomizer = new Random();
		for (int x = 0; x < i; x++) {
			inst = randomizer.nextInt(numInstances);
			data = instanceSet.instances[inst].data;
			if (data[classIndex] != minorityClass) {
				Instance newInstance = instanceSet.instances[inst].clone();
				newInstanceSet.insertInstance(newInstance);
			}
		}
		
		/* Select all attributes to be saved */
		int[] selectedAttributes = new int[newInstanceSet.numAttributes];
		for (int att = 0; att < newInstanceSet.numAttributes; att++) {
			selectedAttributes[att] = att;
		}
		
		/* Build the test and training instanceSets */
		float testProportion = (float) 1 / 3;
		InstanceSet testData = newInstanceSet.buildTrainTestSet(testProportion, false);
		InstanceSet trainData = newInstanceSet;
		
		/* Save the sampled instanceSet */
		saveFile(trainFileName + i + extension, trainData, selectedAttributes, true);
		saveFile(testFileName + i + extension, testData, selectedAttributes, true);
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

}