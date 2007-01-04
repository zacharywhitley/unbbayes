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
 * @date 01/12/2006
 */
public class TestDistribution {
	public TestDistribution() {
		try {
			run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new TestDistribution();
	}
	
	public void run() throws Exception {
		/* Data set characteristics */
		String inputFileName = "c:/m1av.arff";
		byte counterIndex = 11;
		int classIndex = 10;
	
		/* Opens the input dataset */
		InstanceSet inputData = openFile(inputFileName, counterIndex);
		if (inputData == null) {
			String exceptionMsg = "Couldn't open input data " + inputFileName;
			throw new Exception(exceptionMsg);
		}

		inputData.setClassIndex(classIndex);
		float originalDist[] = distribution(inputData);

		@SuppressWarnings("unused")
		int ema = 0;
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
