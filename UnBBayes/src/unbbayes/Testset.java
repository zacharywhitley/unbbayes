package unbbayes;

import java.io.File;
import java.io.IOException;

import unbbayes.datamining.classifiers.Classifier;
import unbbayes.datamining.classifiers.DistributionClassifier;
import unbbayes.datamining.classifiers.Evaluation;
import unbbayes.datamining.classifiers.NaiveBayes;
import unbbayes.datamining.classifiers.decisiontree.C45;
import unbbayes.datamining.datamanipulation.InstanceSet;
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
	
	public Testset() {
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
		InstanceSet trainData = openFile(trainFileName, counterIndex);
		trainData.setClassIndex(classIndex);
		smote = new Smote(trainData, null);
		
		/* Set SMOTE options */
		smote.setOptionDiscretize(optionDiscretize);
		smote.setOptionDistanceFunction(optionDistanceFunction);
		smote.setOptionFixedGap(optionFixedGap);
		smote.setOptionNominal(optionNominal);
		
		smote.buildNN(5, 1);

		/* Options for SMOTE - END *****************/

		
		/* Limit applied to the counter */
		int weightLimit = 404;
		
		/* Set relative probabilities - probabilistic models */
		boolean relativeProb = true;
		
		/* Number of different sampling strategies */
		int sampleQtd = 5;
		
		/* Type of classifier
		 * 0 - Naive Bayes
		 * 1 - C4.5
		 */
		int classifierID = 0;
		
		runAux(trainFileName, testFileName, classIndex, counterIndex,
				weightLimit, relativeProb, sampleQtd, classifierID,
				optionDiscretize, optionNominal);

//		for (; weightLimit < 1000; weightLimit += 10) {
//			runAux(trainFileName, testFileName, classIndex, counterIndex, weightLimit,
//					relativeProb, sampleQtd, classifierID, optionDiscretize, optionNominal);
//		}
	}

	private void runAux(String trainFileName, String testFileName, int classIndex,
			int counterIndex, int weightLimit, boolean relativeProb, int sampleQtd,
			int classifierID, boolean optionDiscretize, int optionNominal) throws Exception {
		
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
		}

		/* Loop through all sample strategies */
//		for (int sampleID = 0; sampleID < sampleQtd; sampleID++) {
		for (int sampleID = 3; sampleID < 4; sampleID++) {
			/* Print header */
			printHeader(sampleID, weightLimit, relativeProb, classifier);

			/* Change the distribution, run the models and evaluate */
			for (int i = 1; i <= 8; i++) {
				/* Opens the training set */
				InstanceSet trainData = openFile(trainFileName, counterIndex);
				if (trainData == null) {
					String exceptionMsg = "Couldn't open training data " 
						+ testFileName;
					throw new Exception(exceptionMsg);
				}
				trainData.setClassIndex(classIndex);
				
				/* Sample training data */
				trainData = sample(trainData, /*sampleID*/3, i, weightLimit,
						optionDiscretize, optionNominal);
				
				/* Distribution of the training data */
				float originalDist[] = distribution(trainData);
	
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
	
	private InstanceSet sample(InstanceSet trainData, int sampleID, int i,
			int weightLimit, boolean optionDiscretize, int optionNominal) {
		/* Limit the weigth */
		if (weightLimit != 0) {
			Sampling.limitWeight(trainData, weightLimit, 0);
		}
		
		/* Get current class distribution  - Two class problem */
		float originalDist[] = distribution(trainData);
		float proportion = originalDist[0] * (float) i;
		proportion = proportion / (originalDist[1] * (float) (10 - i));
		
		switch (sampleID) {
			case 0:
				/* Undersampling */
				Sampling.simpleSampling(trainData,
						(float) (1 / proportion), 0);
				
				break;
			case 1:
				/* Oversampling */
				Sampling.simpleSampling(trainData, proportion, 1);
				
				break;
			case 2:
				/* Oversampling */
				Sampling.simpleSampling(trainData,
						(float) Math.sqrt(proportion), 1);
				
				/* Undersampling */
				Sampling.simpleSampling(trainData,
						(float) Math.sqrt((float) (1 / proportion)), 0);
				
				break;
			case 3:
				/* SMOTE */
				smote.setInstanceSet(trainData);
				trainData = smote.run(proportion, 1);
				
				break;
			case 4:
				/* SMOTE */
				smote.setInstanceSet(trainData);
				trainData = smote.run((float) Math.sqrt(proportion), 1);

				/* Undersampling */
				Sampling.simpleSampling(trainData,
						(float) Math.sqrt((float) (1 / proportion)), 0);

				break;
		}
		return trainData;
	}
	
	private void printHeader(int sampleID, int weightLimit,
			boolean relativeProb, Classifier classifier) {
		
		if (classifier instanceof NaiveBayes) {
			header = "Naive Bayes";
		} else if (classifier instanceof C45) {
			header = "C4.5";
		}
		
		/* Sampling strategy */
		if (sampleID == 0) {
			header = header + ": Undersampling";
		} else if (sampleID == 1) {
			header = header + ": Oversampling";
		} else if (sampleID == 2) {
			header = header + ": Oversampling with Undersampling";
		} else if (sampleID == 3) {
			header = header + ": SMOTE";
		} else if (sampleID == 4) {
			header = header + ": SMOTE with Undersampling";
		}

		if (weightLimit > 0) header = header + " " + "Weight limited to " + weightLimit; 
		if (relativeProb) header = header + "\n" + "Relative Probabilities"; 
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
//		if (classifier instanceof NaiveBayes) {
//			((NaiveBayes) classifier).setOriginalDistribution(originalDist);
//		}
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
			boolean relativeProb, int i, float originalDist[]) throws Exception {
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
			maxSEHeader = header + "\ni: " + i + " - " + (int) percentage + "%";
		}
		
		System.out.print(i + " - " + (int) percentage + "	");
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