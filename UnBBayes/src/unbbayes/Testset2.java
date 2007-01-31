package unbbayes;

import unbbayes.datamining.datamanipulation.InstanceSet;

/**
 * Class for testing the formalism Cluster-Based SMOTE.
 * 
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 24/08/2006
 */
public class Testset2 {
	float maxSE = 0;
	String maxSEHeader;
	String header = "";
	private TestsetUtils testsetUtils;
	
	public Testset2() {
		try {
			run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		new Testset2();
	}

	public void run() throws Exception {
		/* Data set characteristics */
//		String trainFileName = "c:/dados/m1t.arff";
//		String testFileName = "c:/dados/m1av.arff";
//		String trainFileName = "c:/dados/m1tOriginal.arff";
//		String testFileName = "c:/dados/m1avOriginal.arff";
//		String trainFileName = "c:/dados/m1tOriginal - nao compactado.arff";
//		String testFileName = "c:/dados/m1avOriginal - nao compactado.arff";
//		String trainFileName = "c:/dados/m1tOriginal - var59 num.arff";
//		String testFileName = "c:/dados/m1avOriginal - var59 num.arff";
		String trainFileName = "c:/dados/outros/pima/pima-indians-diabetes-training.arff";
		String testFileName = "c:/dados/outros/pima/pima-indians-diabetes-test.arff";
//		String trainFileName = "c:/dados/sampleados/m1t" + i + ".arff";
//		String testFileName = "c:/dados/sampleados/m1av" + i + ".arff";
//		String testFileName = "c:/dados/m1avOriginal - var59 num.arff";
//		int classIndex = 10;
//		int counterIndex = 11;
		int classIndex = 8;
		int counterIndex = 9;
		int interestingClass = 1;
		
		/* Create the utils */
		testsetUtils = new TestsetUtils();
		testsetUtils.setInterestingClass(interestingClass);
		
		runAux(trainFileName, testFileName, classIndex, counterIndex);
	}

	private void runAux(String trainFileName, String testFileName,
			int classIndex, int counterIndex)
	throws Exception {
		float[] newDist;
		InstanceSet trainData;
		String results;
		
		/* Opens the training set */
		InstanceSet data = TestsetUtils.openFile(trainFileName, counterIndex);
		if (data == null) {
			String exceptionMsg = "Couldn't open training data " 
				+ trainFileName;
			throw new Exception(exceptionMsg);
		}
		data.setClassIndex(classIndex);
		float[] originalDist = TestsetUtils.distribution(data);
		
		/* Opens the test set */
		InstanceSet testData = TestsetUtils.openFile(testFileName, counterIndex);
		if (testData == null) {
			String exceptionMsg = "Couldn't open test data " + testFileName;
			throw new Exception(exceptionMsg);
		}
		testData.setClassIndex(classIndex);
		
		/* Compute original results */
		String originalResults = testsetUtils.classifyEvaluate(data, testData, originalDist);
		
		/* Loop through all sample strategies */
		for (int sampleID = 0; sampleID < 5; sampleID++) {
			/* Print header */
			testsetUtils.printHeader(sampleID);

			/* Print original results */
			System.out.print("original	");
			System.out.println(originalResults);
			
			/* Change the testsetUtils.distribution, run the models and evaluate */
			for (int i = 1; i <= 9; i++) {
				trainData = new InstanceSet(data);
				
				/* Sample training data */
				trainData = testsetUtils.sample(trainData, sampleID, i, originalDist);
				
				/* Check if the trainData has been sampled */
				if (trainData == null) {
					/* Skip */
					continue;
				}
				
				/* Distribution of the sampled training data */
				newDist = TestsetUtils.distribution(trainData);
				float percentage = (float) newDist[1] / (newDist[0] + newDist[1]);
				percentage = (100 * percentage);

				/* Internal header */
				System.out.print("     " + (int) percentage + "% 	");

				/* Classify, evaluate and print the results */
				results = testsetUtils.classifyEvaluate(trainData, testData, newDist);
				System.out.print(results);

				System.out.println("");
				
				trainData = null;
				System.gc();
			}
			System.out.println("");
			System.out.println("");

			trainData = null;
			System.gc();
		}
		
		System.out.println("\n\n");
		System.out.println("Maximum SE: " + maxSE);
		System.out.println("With:\n" + maxSEHeader);
	}
	
}