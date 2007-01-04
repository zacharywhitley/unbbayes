package unbbayes;

import java.io.File;
import java.io.IOException;

import unbbayes.datamining.classifiers.Classifier;
import unbbayes.datamining.classifiers.CombinatorialNeuralModel;
import unbbayes.datamining.classifiers.DistributionClassifier;
import unbbayes.datamining.classifiers.Evaluation;
import unbbayes.datamining.classifiers.NaiveBayes;
import unbbayes.datamining.classifiers.decisiontree.C45;
import unbbayes.datamining.datamanipulation.ArffLoader;
import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.datamanipulation.Loader;
import unbbayes.datamining.datamanipulation.TxtLoader;
import unbbayes.datamining.preprocessor.imbalanceddataset.Sampling;
import unbbayes.datamining.preprocessor.imbalanceddataset.Smote;

/**
 * Class for testing the formalism Cluster-Based SMOTE.
 * 
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 24/08/2006
 */
public class Testset {
	
	float maxSE = 0;
	String maxSEHeader;
	String header = "";
	Smote smote;
//	File saida = new File("c:/saida.txt");
//	FileInputStream saidaX = new FileInputStream(saida);
	private byte[] attributeType;
	private boolean[] attributeIsString;
	private byte NOMINAL;
	
	public Testset() {
		try {
			run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		new Testset();
	}

	public void run() throws Exception {
		/* Data set characteristics */
//		String trainFileName = "c:/dados/m1t.arff";
//		String testFileName = "c:/dados/m1av.arff";
		String trainFileName = "c:/dados/m1tOriginal.arff";
		String testFileName = "c:/dados/m1avOriginal.arff";
		int classIndex = 10;
		int counterIndex = 11;
		
		
		/* Options for SMOTE - START *****************/
		
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
		
		/* Computes the k nearest neighbors of each instance */
//		InstanceSet trainData = openFile(trainFileName, counterIndex);
//		trainData.setClassIndex(classIndex);
//		smote = new Smote(trainData, null);
//		
//		/* Set SMOTE options */
//		smote.setOptionDiscretize(optionDiscretize);
//		smote.setOptionDistanceFunction(optionDistanceFunction);
//		smote.setOptionFixedGap(optionFixedGap);
//		smote.setOptionNominal(optionNominal);
		
		/* Options for SMOTE - END *****************/

		
		/* Limit applied to the counter */
		int weightLimit = 00000;
		
		/* Set relative probabilities - probabilistic models */
		boolean relativeProb = true;
		
		/* Number of different sampling strategies */
		int sampleQtd = 5;
		
		/* 
		 * Type of classifier
		 * 0 - Naive Bayes
		 * 1 - C4.5
		 * 2 - CNM
		 */
		int classifierID = 0;

		/*
		 * The maximum order of combinations for CNM  
		 */
		int maxOrderCNM = 3;
		
		runAux3(trainFileName, testFileName, classIndex, counterIndex,
				weightLimit, sampleQtd, optionDiscretize, optionNominal,
				maxOrderCNM);
		
//		classifierID = 0;
//		relativeProb = false;
//		runAux(trainFileName, testFileName, classIndex, counterIndex,
//				weightLimit, relativeProb, sampleQtd, classifierID,
//				optionDiscretize, optionNominal, maxOrderCNM);
//
//		classifierID = 0;
//		relativeProb = true;
//		runAux(trainFileName, testFileName, classIndex, counterIndex,
//				weightLimit, relativeProb, sampleQtd, classifierID,
//				optionDiscretize, optionNominal, maxOrderCNM);
//
//		classifierID = 1;
//		runAux(trainFileName, testFileName, classIndex, counterIndex,
//				weightLimit, relativeProb, sampleQtd, classifierID,
//				optionDiscretize, optionNominal, maxOrderCNM);
		
		/**************************************************************/
		
//		/* Opens the test set */
//		InstanceSet testData = openFile(testFileName, counterIndex);
//		if (testData == null) {
//			String exceptionMsg = "Couldn't open test data " + testFileName;
//			throw new Exception(exceptionMsg);
//		}
//		testData.setClassIndex(classIndex);
//		
//		/* Naive Bayes */
//		classifierID = 0;
//		relativeProb = false;
//		runAux2(trainFileName, testFileName, classIndex, counterIndex,
//				weightLimit, relativeProb, classifierID, testData, trainData,
//				maxOrderCNM);
//
//		/* Naive Bayes with Relative Prob */
//		classifierID = 0;
//		relativeProb = true;
//		runAux2(trainFileName, testFileName, classIndex, counterIndex,
//				weightLimit, relativeProb, classifierID, testData, trainData,
//				maxOrderCNM);
//
//		/* C4.5 */
//		classifierID = 1;
//		runAux2(trainFileName, testFileName, classIndex, counterIndex,
//				weightLimit, relativeProb, classifierID, testData, trainData,
//				maxOrderCNM);

//		/* CNM */
//		classifierID = 2;
//		runAux2(trainFileName, testFileName, classIndex, counterIndex,
//				weightLimit, relativeProb, classifierID, testData, trainData,
//				maxOrderCNM);

		
		
		/*************************************************************
		 *                        A N T I G O                        *
		 *************************************************************/
		
//		for (weightLimit = 4970; weightLimit < 10000; weightLimit += 100) {
//			runAux(trainFileName, testFileName, classIndex, counterIndex,
//					weightLimit, relativeProb, sampleQtd, classifierID,
//					optionDiscretize, optionNominal, maxOrderCNM);
//		}
	}

	private void runAux(String trainFileName, String testFileName, int classIndex,
			int counterIndex, int weightLimit, boolean relativeProb, int sampleQtd,
			int classifierID, boolean optionDiscretize, int optionNominal,
			int maxOrderCNM) throws Exception {
		
		/* Opens the test set */
		InstanceSet testData = openFile(testFileName, counterIndex);
		if (testData == null) {
			String exceptionMsg = "Couldn't open test data " + testFileName;
			throw new Exception(exceptionMsg);
		}
		testData.setClassIndex(classIndex);
		
		/* Choose classifier */
		Classifier classifier = null;
		if (classifierID == 0) {
			classifier = new NaiveBayes();
		} else if (classifierID == 1) {
			classifier = new C45();
		} else if (classifierID == 2) {
			classifier = new CombinatorialNeuralModel(maxOrderCNM);
		}

		/* Loop through all sample strategies */
//		for (int sampleID = 0; sampleID < sampleQtd; sampleID++) {
//		for (int sampleID = 0; sampleID < 4; sampleID++) {
		for (int sampleID = 0; sampleID < 3; sampleID++) {
			/* Print header */
			printHeader(sampleID, weightLimit, relativeProb, classifierID);

			/* Change the distribution, run the models and evaluate */
			for (int i = 1; i <= 7; i++) {
				/* Opens the training set */
				InstanceSet trainData = openFile(trainFileName, counterIndex);
				if (trainData == null) {
					String exceptionMsg = "Couldn't open training data " 
						+ trainFileName;
					throw new Exception(exceptionMsg);
				}
				trainData.setClassIndex(classIndex);
				
				/* Limit the weigth */
				if (weightLimit != 0) {
					Sampling.limitWeight(trainData, weightLimit, 0);
				}
				
				/* Sample training data */
				trainData = sample(trainData, sampleID, i,	optionDiscretize,
						optionNominal);
				
				/* Distribution of the training data */
				float[] originalDist = distribution(trainData);
	
				/* Build model */
				classifier = buildModel(trainData, originalDist, classifier);
				
				/* Evaluate model */
				evaluate(classifier, testData, relativeProb, i, originalDist);
			}
			System.out.println("");
			System.out.println("");
		}
		
		System.out.println("\n\n");
		System.out.println("Maximum SE: " + maxSE);
		System.out.println("With:\n" + maxSEHeader);
	}
	
	private void runAux2(String trainFileName, String testFileName,
			int classIndex,	int counterIndex, int weightLimit,
			boolean relativeProb, int classifierID, InstanceSet testData,
			InstanceSet trainData, int maxOrderCNM) throws Exception {
		
		/* Choose classifier */
		Classifier classifier = null;
		if (classifierID == 0) {
			classifier = new NaiveBayes();
		} else if (classifierID == 1) {
			classifier = new C45();
		} else if (classifierID == 2) {
			classifier = new CombinatorialNeuralModel(maxOrderCNM);
		}

		/* No sample strategy */
		int sampleID = -1;
		
		printHeader(sampleID, weightLimit, relativeProb, classifierID);

		trainData.setClassIndex(classIndex);
		
		/* Limit the weigth */
		if (weightLimit != 0) {
			Sampling.limitWeight(trainData, weightLimit, 0);
		}
		
		/* Distribution of the training data */
		float[] originalDist = distribution(trainData);

		/* Build model */
		classifier = buildModel(trainData, originalDist, classifier);
		
		/* Evaluate model */
		evaluate(classifier, testData, relativeProb, 1, originalDist);
		System.out.println("");
		System.out.println("");

		System.out.println("\n\n");
		System.out.println("Maximum SE: " + maxSE);
		System.out.println("With:\n" + maxSEHeader);
	}
	
	private void runAux3(String trainFileName, String testFileName,
			int classIndex, int counterIndex, int weightLimit, int sampleQtd,
			boolean optionDiscretize, int optionNominal, int maxOrderCNM)
	throws Exception {
		boolean relativeProb;
		InstanceSet trainData;
//		int classifierID;
		
		/* Opens the training set */
		InstanceSet data = openFile(trainFileName, counterIndex);
		if (data == null) {
			String exceptionMsg = "Couldn't open training data " 
				+ trainFileName;
			throw new Exception(exceptionMsg);
		}
		data.setClassIndex(classIndex);
		
		/* Opens the test set */
		InstanceSet testData = openFile(testFileName, counterIndex);
		if (testData == null) {
			String exceptionMsg = "Couldn't open test data " + testFileName;
			throw new Exception(exceptionMsg);
		}
		testData.setClassIndex(classIndex);
		
		/* Choose classifier */
		Classifier classifier = null;

		/* Loop through all sample strategies */
//		for (int sampleID = 0; sampleID < sampleQtd; sampleID++) {
//		for (int sampleID = 0; sampleID < 4; sampleID++) {
		for (int sampleID = 0; sampleID < 3; sampleID++) {
			trainData = new InstanceSet(data);
			
			/* Print header */
//			printHeader(sampleID, weightLimit, relativeProb, classifierID);

			/* Change the distribution, run the models and evaluate */
			for (int i = 1; i <= 7; i++) {
				/* Limit the weigth */
				if (weightLimit != 0) {
					Sampling.limitWeight(trainData, weightLimit, 0);
				}
				
				/* Sample training data */
				trainData = sample(trainData, sampleID, i,	optionDiscretize,
						optionNominal);
				
				/* Distribution of the training data */
				float[] originalDist = distribution(trainData);
				
				/************** Naive Bayes **************/

				/* Build model */
				classifier = new NaiveBayes();
				relativeProb = false;
				classifier = buildModel(trainData, originalDist, classifier);
				
				/* Evaluate model */
				evaluate(classifier, testData, relativeProb, i, originalDist);
				
				/************** Naive Bayes Relative Prob **************/

				/* Build model */
				classifier = new NaiveBayes();
				relativeProb = true;
				classifier = buildModel(trainData, originalDist, classifier);
				
				/* Evaluate model */
				evaluate(classifier, testData, relativeProb, i, originalDist);
				
				/************** C4.5 **************/

				/* Build model */
				classifier = new C45();
				classifier = buildModel(trainData, originalDist, classifier);
				
				/* Evaluate model */
				evaluate(classifier, testData, relativeProb, i, originalDist);
				
//				/************** CNM **************/
//
//				/* Build model */
//				classifier = new CombinatorialNeuralModel(maxOrderCNM);
//				classifier = buildModel(trainData, originalDist, classifier);
//				
//				/* Evaluate model */
//				evaluate(classifier, testData, relativeProb, i, originalDist);

				System.out.println("");
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
	
	private InstanceSet sample(InstanceSet trainData, int sampleID, int i,
			boolean optionDiscretize, int optionNominal) throws Exception {
		/* Get current class distribution  - Two class problem */
		float[] originalDist = distribution(trainData);
		float proportion = originalDist[0] * (float) i;
		proportion = proportion / (originalDist[1] * (float) (10 - i));
		switch (sampleID) {
			case 0:
				/* Undersampling */
				Sampling.undersampling(trainData,(float) (1 / proportion), 0);

				break;
			case 1:
				/* Oversampling */
				Sampling.oversampling(trainData, proportion, 1);
				
				break;
			case 2:
				/* Oversampling */
				Sampling.oversampling(trainData,
						(float) Math.sqrt(proportion), 1);
				
				/* Undersampling */
				Sampling.undersampling(trainData,
						(float) Math.sqrt((float) (1 / proportion)), 0);
				
				break;
			case 3:
				/* SMOTE */
				smote.setInstanceSet(trainData);
				smote.buildNN(5, 1);
				smote.run(1, (float) proportion);
				
				break;
			case 4:
				/* Undersampling */
				Sampling.undersampling(trainData,
						(float) Math.sqrt((float) (1 / proportion)), 0);

				/* Cluster-Based SMOTE */
				smote.setInstanceSet(trainData);
				smote.buildNN(5, 1);
				smote.run(1, (float) Math.sqrt(proportion));

				break;
		}
		return trainData;
	}
	
	private void printHeader(int sampleID, int weightLimit,
			boolean relativeProb, int classifierID) {
		
		if (classifierID == 0) {
			header = "Naive Bayes";
		} else if (classifierID == 1) {
			header = "C4.5";
		} else if (classifierID == 2) {
			header = "CNM";
		}
		
		/* Sampling strategy */
		if (sampleID == 0) {
			header = header + ": Undersampling";
		} else if (sampleID == 1) {
			header = header + ": Oversampling";
		} else if (sampleID == 2) {
			header = header + ": Oversampling with Undersampling";
		} else if (sampleID == 3) {
//			header = header + ": SMOTE with Undersampling";
			header = header + ": SMOTE";
		} else if (sampleID == 4) {
			header = header + ": Cluster-Based SMOTE with Undersampling";
		}

		if (weightLimit > 0) header = header + " " + "Weight limited to " + weightLimit; 
		if (relativeProb && classifierID == 0) header = header + "\n" + "Relative Probabilities"; 
		System.out.print("---------------------------------");
		System.out.println("---------------------------------");
		System.out.println("");
		System.out.println(header);
		System.out.println("");
		System.out.println("i - %	S	E	SE");
		
//		saida.
	}

	private Classifier buildModel(InstanceSet trainData, float originalDist[],
			Classifier classifier) throws Exception {
		/* Train the net */
		if (classifier instanceof DistributionClassifier) {
			((DistributionClassifier) classifier).setOriginalDistribution(originalDist);
		}
		classifier.buildClassifier(trainData);
		
		return classifier;
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

	private void evaluate(Classifier classifier, InstanceSet testData,
			boolean relativeProb, int i, float[] originalDist) throws Exception {
		float percentage = (float) originalDist[1] / (originalDist[0] + originalDist[1]);
		percentage = (100 * percentage);
		
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
		
		if (SE > maxSE) {
			maxSE = SE;
			maxSEHeader = header + "\nQuantity of fraud: "  + (int) percentage + "%" + "\nSE: " + maxSE;
		}
		
		System.out.print(i + " - " + (int) percentage + "	");
		System.out.print(sensibility + "	");
		System.out.print(specificity + "	");
		System.out.println(SE);
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

//	private void ema() {
// 	// hugo
//	attributeType = new byte[6];
//	attributeIsString = new boolean[6];
//	attributeType[0] = NUMERIC;
//	attributeType[1] = NUMERIC;
//	attributeType[2] = NUMERIC;
//	attributeType[3] = NUMERIC;
//	attributeType[4] = NUMERIC;
//	attributeType[5] = NUMERIC;
//	attributeIsString[0] = false;
//	attributeIsString[1] = false;
//	attributeIsString[2] = false;
//	attributeIsString[3] = false;
//	attributeIsString[4] = false;
//	attributeIsString[5] = false;
//}

private void ema() {
	// banco do brasil
	attributeType = new byte[11];
	attributeIsString = new boolean[11];
	attributeType[0] = NOMINAL;
	attributeType[1] = NOMINAL;
	attributeType[2] = NOMINAL;
//	attributeType[3] = NUMERIC;
	attributeType[3] = NOMINAL;
	attributeType[4] = NOMINAL;
	attributeType[5] = NOMINAL;
	attributeType[6] = NOMINAL;
	attributeType[7] = NOMINAL;
	attributeType[8] = NOMINAL;
//	attributeType[9] = CYCLIC;
	attributeType[9] = NOMINAL;
//	attributeType[9] = NUMERIC;
	attributeType[10] = NOMINAL;
	attributeIsString[0] = false;
	attributeIsString[1] = false;
	attributeIsString[2] = false;
	attributeIsString[3] = false;
	attributeIsString[4] = false;
	attributeIsString[5] = false;
	attributeIsString[6] = false;
	attributeIsString[7] = false;
	attributeIsString[8] = false;
	attributeIsString[9] = false;
	attributeIsString[10] = false;
}

//private void ema() {
//	// creditApproval
//	attributeType = new byte[16];
//	attributeIsString = new boolean[16];
//	attributeType[0] = NOMINAL;
//	attributeType[1] = NUMERIC;
//	attributeType[2] = NUMERIC;
//	attributeType[3] = NOMINAL;
//	attributeType[4] = NOMINAL;
//	attributeType[5] = NOMINAL;
//	attributeType[6] = NOMINAL;
//	attributeType[7] = NUMERIC;
//	attributeType[8] = NOMINAL;
//	attributeType[9] = NOMINAL;
//	attributeType[9] = NOMINAL;
//	attributeType[10] = NUMERIC;
//	attributeType[11] = NOMINAL;
//	attributeType[12] = NOMINAL;
//	attributeType[13] = NUMERIC;
//	attributeType[14] = NUMERIC;
//	attributeType[15] = NOMINAL;
//	attributeIsString[0] = true;
//	attributeIsString[1] = true;
//	attributeIsString[2] = true;
//	attributeIsString[3] = true;
//	attributeIsString[4] = true;
//	attributeIsString[5] = true;
//	attributeIsString[6] = true;
//	attributeIsString[7] = true;
//	attributeIsString[8] = true;
//	attributeIsString[9] = true;
//	attributeIsString[10] = true;
//	attributeIsString[11] = true;
//	attributeIsString[12] = true;
//	attributeIsString[13] = true;
//	attributeIsString[14] = true;
//	attributeIsString[15] = false;
//	counterIndex = -1;
//}


//private void ema() {
//	attributeType = new byte[5];
//	attributeIsString = new boolean[5];
//	attributeType[0] = NOMINAL;
//	attributeIsString[0] = true;
//	attributeType[1] = NUMERIC;
//	attributeIsString[1] = false;
//	attributeType[2] = NUMERIC;
//	attributeIsString[2] = false;
//	attributeType[3] = NOMINAL;
//	attributeIsString[3] = true;
//	attributeType[4] = NOMINAL;
//	attributeIsString[4] = true;
//}

//private void ema() {
//	attributeType = new byte[23];
//	attributeIsString = new boolean[23];
//	for (int i = 0; i < 23; i++) {
//		attributeType[i] = NOMINAL;
//		attributeIsString[i] = true;
//	}
//}

}