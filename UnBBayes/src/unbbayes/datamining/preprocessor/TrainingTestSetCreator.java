package unbbayes.datamining.preprocessor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;

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
//			teste();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void teste() {
		int  testSize = 2000000;
		Random randomizer = new Random();
		Hashtable<Integer, Integer> used;
		used = new Hashtable<Integer, Integer>(testSize);
		int inst;
		int[] instancesTestSet = new int[testSize];

		/* Build array with the instances' indexes of the new test set */
		int counter = 0;
		while (counter < testSize) {
			inst = randomizer.nextInt(testSize);
//			if (!used.contains(inst)) {
				instancesTestSet[counter] = inst;
//				used.put(inst, inst);
				++counter;
//			}
		}
		
		counter = 0;
		for (int i = 0; i < testSize; i++) {
			if (used.contains(i)) {
				++counter;
			}
			used.put(i, i);
		}
		int em = 0;
	}

	public void run() throws Exception {
		/* Data set characteristics */
		String inputFileName = "c:/m1t.txt";
		String trainFileName = "c:/m1t.arff";
		String testFileName = "c:/m1a.arff";
		byte classIndex = 10;
		byte counterIndex = 11;
	
		/* Opens the input dataset */
		InstanceSet inputData = openFile(inputFileName, counterIndex);
		if (inputData == null) {
			String exceptionMsg = "Couldn't open input data " + trainFileName;
			throw new Exception(exceptionMsg);
		}
		inputData.setClassIndex(classIndex);
		
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
			InstanceSet instanceSet = loader.getInstances();
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


}