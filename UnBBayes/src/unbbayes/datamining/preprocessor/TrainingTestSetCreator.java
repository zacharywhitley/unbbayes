package unbbayes.datamining.preprocessor;

import java.io.File;
import java.io.IOException;

import unbbayes.datamining.datamanipulation.ArffSaver;
import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.datamanipulation.TxtLoader;

/**
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 21/11/2006
 */
public class TrainingTestSetCreator {
	public TrainingTestSetCreator() {
		try {
			run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new TrainingTestSetCreator();
	}
	
	public void run() throws Exception {
		/* Data set characteristics */
		String inputFileName = "c:/m1t.txt";
		String trainFileName = "c:/m1t.arff";
		String testFileName = "c:/m1av.arff";
		byte counterIndex = -1;
		int classIndex = 10;
	
		/* Opens the input dataset */
		InstanceSet inputData = openFile(inputFileName, counterIndex);
		if (inputData == null) {
			String exceptionMsg = "Couldn't open input data " + inputFileName;
			throw new Exception(exceptionMsg);
		}

		/* Build the test and training instanceSets */
		int testSize = Math.round((float) inputData.numInstances / 3);
		InstanceSet testData = inputData.buildTrainTestSet(testSize);
		InstanceSet trainData = inputData;
		
		/* Select all attributes to be saved */
		int[] selectedAttributes = new int[inputData.numAttributes];
		for (int att = 0; att < inputData.numAttributes; att++) {
			selectedAttributes[att] = att;
		}
		
		/* Save the training instance */
		saveFile(trainFileName, trainData, selectedAttributes, true);
		
		/* Save the test instance */
		saveFile(testFileName, testData, selectedAttributes, true);
	}
	
	private InstanceSet openFile(String fileName, int counterIndex) throws IOException {
		File file = new File(fileName);
		TxtLoader loader = new TxtLoader(file);
		
		/* If the dataset is compacted */
		loader.setCounterAttribute(counterIndex);

		/* Get the instances one by one */
		while (loader.getInstance()) {
			/* Wait while instances are loaded */
		}

		/* Return the instanceSet if read successfully */
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
		
		while (saver.setInstance()) {
			/* Wait while instances are loaded */
		}
	}

	private float[] distribution(InstanceSet trainData) {
		int numInstances = trainData.numInstances();
		int numClasses = trainData.numClasses();
		int classIndex = trainData.classIndex;
		int counterIndex = trainData.counterIndex;
		float distribution[] = new float[numClasses];
//		float weight = 0;
		int classValue;
		
		for (int i = 0; i < numClasses; i++) {
			distribution[i] = 0;
		}
		
		for (int i = 0; i < numInstances; i++) {
			classValue = (int) trainData.instances[i].data[classIndex];
			distribution[classValue] += trainData.instances[i].data[counterIndex];
//			weight += dataset[i][counterIndex];
		}

//		for (int i = 0; i < numClasses; i++) {
//			distribution[i] /= weight;
//		}
		return distribution;
	}


}