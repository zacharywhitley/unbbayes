package unbbayes;

import java.io.File;
import java.io.IOException;

import unbbayes.datamining.datamanipulation.ArffLoader;
import unbbayes.datamining.datamanipulation.ArffSaver;
import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.datamanipulation.Loader;
import unbbayes.datamining.datamanipulation.TxtLoader;

/**
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 21/11/2006
 */
public class TrainingTestSetCreator {
	public TrainingTestSetCreator() throws Exception {
		run();
	}

	public static void main(String[] args) throws Exception {
		new TrainingTestSetCreator();
	}
	
	public void run() throws Exception {
		/* Data set characteristics */
		String inputFileName = "c:/dados/m1Original.arff";
		String trainFileName = "c:/dados/m1tOriginal.arff";
		String testFileName = "c:/dados/m1avOriginal.arff";
		byte counterIndex = -1;
	
		/* Opens the input dataset */
		InstanceSet inputData = openFile(inputFileName, counterIndex);
		if (inputData == null) {
			String exceptionMsg = "Couldn't open input data " + inputFileName;
			throw new Exception(exceptionMsg);
		}

		/* Compact? */
		boolean compact = true;
		
		/* Build the test and training instanceSets */
		float testProportion = (float) 1 / 3;
		InstanceSet testData = inputData.buildTrainTestSet(testProportion, false);
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


}