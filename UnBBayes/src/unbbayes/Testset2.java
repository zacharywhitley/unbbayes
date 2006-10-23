package unbbayes;

import java.io.File;
import java.io.IOException;

import unbbayes.datamining.classifiers.Classifier;
import unbbayes.datamining.classifiers.DistributionClassifier;
import unbbayes.datamining.classifiers.Evaluation;
import unbbayes.datamining.classifiers.NaiveBayes;
import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.datamanipulation.TxtLoader;

/**
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 11/10/2006
 */
public class Testset2 {
	public Testset2() {
		try {
			run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() throws Exception {
		/* Data set characteristics */
		String trainFileName = "c:/m1t.txt";
		String testFileName = "c:/m1av.txt";
		byte classIndex = 10;
		byte counterIndex = 11;

		/* Set relative probabilities - probabilistic models */
		boolean relativeProb = true;
		
		/* Opens the training set */
		InstanceSet trainData = openFile(trainFileName, counterIndex);
		if (trainData == null) {
			String exceptionMsg = "Couldn't open test data " + testFileName;
			throw new Exception(exceptionMsg);
		}
		trainData.setClassIndex(classIndex);
		
		/* Build classifier */
		Classifier classifier = new NaiveBayes();
		classifier.buildClassifier(trainData);

		/* Opens the test set */
		InstanceSet testData = openFile(testFileName, counterIndex);
		if (testData == null) {
			String exceptionMsg = "Couldn't open test data " + testFileName;
			throw new Exception(exceptionMsg);
		}
		testData.setClassIndex(classIndex);

		if (classifier instanceof DistributionClassifier) {
			if (relativeProb) {
				((DistributionClassifier)classifier).setRelativeClassification();
			} else {
				((DistributionClassifier)classifier).setNormalClassification();
			}
		}

		/* Evaluate the model */
		Evaluation eval = new Evaluation(testData, classifier);
		eval.evaluateModel(classifier, testData);
		
		/* Print out the SE */
		float sensibility = (float) eval.truePositiveRate(1) * 1000;
		sensibility = (int) sensibility;
		sensibility = sensibility / 1000;
		float specificity = (float) eval.truePositiveRate(0) * 1000;
		specificity = (int) specificity;
		specificity = specificity / 1000;
		float SE = sensibility * specificity * 1000;
		SE = (int) SE;
		SE = SE / 1000;
		
		System.out.print(sensibility + "	");
		System.out.print(specificity + "	");
		System.out.println(SE);
	}
	
	private InstanceSet openFile(String fileName, int counterIndex) throws IOException {
		File file = new File(fileName);
		TxtLoader loader = new TxtLoader(file);
		
		/* If the dataset is compacted */
		loader.setCounterAttribute(counterIndex);
		
		while (loader.getInstance()) {
			/* Wait while instances are loaded */
		}
		if (loader != null) {
			InstanceSet instanceSet = loader.getInstances();

			return instanceSet;
		}
		
		return null;
	}

}

