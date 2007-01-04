package unbbayes;

import java.io.File;
import java.io.IOException;

import unbbayes.datamining.classifiers.Classifier;
import unbbayes.datamining.classifiers.DistributionClassifier;
import unbbayes.datamining.classifiers.Evaluation;
import unbbayes.datamining.classifiers.NaiveBayes;
import unbbayes.datamining.classifiers.decisiontree.C45;
import unbbayes.datamining.datamanipulation.ArffLoader;
import unbbayes.datamining.datamanipulation.AttributeStats;
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
public class TestROC {
	
	float maxSE = 0;
	String maxSEHeader;
	String header = "";
	Smote smote;
	private int i;
//	File saida = new File("c:/saida.txt");
//	FileInputStream saidaX = new FileInputStream(saida);
	
	public TestROC() {
		try {
			for (i = 10000; i <= 10000; i += 10000) {
				run(i);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		new TestROC();
	}

	public void run(int i) throws Exception {
		/* Data set characteristics */
		String trainFileName = "c:/dados/m1t.arff";
		String testFileName = "c:/dados/m1av.arff";
//		String trainFileName = "c:/dados/m1tOriginal.arff";
//		String testFileName = "c:/dados/m1avOriginal.arff";
//		String trainFileName = "c:/dados/m1tOriginal - var59 num.arff";
//		String testFileName = "c:/dados/m1avOriginal - var59 num.arff";
//		String trainFileName = "c:/dados/outros/pima/pima-indians-diabetes-training.arff";
//		String testFileName = "c:/dados/outros/pima/pima-indians-diabetes-test.arff";
//		String trainFileName = "c:/dados/sampleados/m1t" + i + ".arff";
//		String testFileName = "c:/dados/sampleados/m1av" + i + ".arff";
//		String testFileName = "c:/dados/m1avOriginal - var59 num.arff";
//		int classIndex = 10;
//		int counterIndex = 11;
		int classIndex = 8;
		int counterIndex = 9;
		
		
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
		
		/* Set SMOTE options */
		smote = new Smote(null);
		smote.setOptionDiscretize(optionDiscretize);
		smote.setOptionDistanceFunction(optionDistanceFunction);
		smote.setOptionFixedGap(optionFixedGap);
		smote.setOptionNominal(optionNominal);
		
		/* Options for SMOTE - END *****************/

		
		/* Number of different sampling strategies */
		int sampleQtd = 5;
		
		/*
		 * The maximum order of combinations for CNM  
		 */
		int maxOrderCNM = 3;
		
		runAux(trainFileName, testFileName, classIndex, counterIndex,
				sampleQtd, optionDiscretize, optionNominal,	maxOrderCNM);
	}

	private void runAux(String trainFileName, String testFileName,
			int classIndex, int counterIndex, int sampleQtd,
			boolean optionDiscretize, int optionNominal, int maxOrderCNM)
	throws Exception {
		boolean relativeProb = false;
		float[] newDist = new float[2];
		InstanceSet trainData;
		Classifier classifier = null;
		
		/* Opens the training set */
		InstanceSet data = openFile(trainFileName, counterIndex);
		if (data == null) {
			String exceptionMsg = "Couldn't open training data " 
				+ trainFileName;
			throw new Exception(exceptionMsg);
		}
		data.setClassIndex(classIndex);
		float[] originalDist = distribution(data);
		
		/* Opens the test set */
		InstanceSet testData = openFile(testFileName, counterIndex);
		if (testData == null) {
			String exceptionMsg = "Couldn't open test data " + testFileName;
			throw new Exception(exceptionMsg);
		}
		testData.setClassIndex(classIndex);
		
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.print("---------------------------------");
		System.out.println("---------------------------------");
		System.out.print("---------------------------------");
		System.out.println("---------------------------------");
		System.out.println(i);
		System.out.print("---------------------------------");
		System.out.println("---------------------------------");
		System.out.print("---------------------------------");
		System.out.println("---------------------------------");
		System.out.println("");
		System.out.println("");
		
		/* Loop through all sample strategies */
		for (int sampleID = 0; sampleID < 5; sampleID++) {
//		for (int sampleID = 0; sampleID < 4; sampleID++) {
//		for (int sampleID = 0; sampleID < 3; sampleID++) {
//		for (int sampleID = 3; sampleID < 5; sampleID++) {
//		for (int sampleID = 4; sampleID < 5; sampleID++) {
			/* Print header */
			printHeader(sampleID);

			/* Change the distribution, run the models and evaluate */
//			for (int i = 1; i <= 7; i++) {
//			for (int i = 0; i <= 7; i++) {
//			for (int i = 0; i <= 1; i++) {
			for (int i = 0; i <= 100; i =+ 10) {
				trainData = data;
				newDist[0] = 100 - i;
				newDist[1] = i;
				
				float percentage = i;

				/* Internal header */
				System.out.print("     " + (int) percentage + "% 	");

				/************** Naive Bayes **************/
				/* Build model */
				classifier = new NaiveBayes();
				relativeProb = false;
				classifier = buildModel(trainData, newDist, classifier);
				
				/* Evaluate model */
				evaluate(classifier, testData, relativeProb, i, originalDist);
				

//				/************** Naive Bayes Relative Prob **************/
//				/* Build model */
//				classifier = new NaiveBayes();
//				relativeProb = true;
//				classifier = buildModel(trainData, newDist, classifier);
//				
//				/* Evaluate model */
//				evaluate(classifier, testData, relativeProb, i, originalDist);
//				
//
//				/************** C4.5 **************/
//				/* Build model */
//				classifier = new C45();
//				classifier = buildModel(trainData, newDist, classifier);
//				
//				/* Evaluate model */
//				evaluate(classifier, testData, relativeProb, i, originalDist);
//				

//				/************** CNM **************/
//				/* Build model */
//				classifier = new CombinatorialNeuralModel(maxOrderCNM);
//				classifier = buildModel(trainData, newDist, classifier);
//				
//				/* Evaluate model */
//				evaluate(classifier, testData, relativeProb, i, originalDist);

				System.out.println("");
				
				classifier = null;
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
	
	private InstanceSet sample(InstanceSet trainData, int sampleID, int i,
			boolean optionDiscretize, int optionNominal, float[] originalDist)
	throws Exception {
		/* Get current class distribution  - Two class problem */
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
//				/* SMOTE */
//				smote.setInstanceSet(trainData);
//				smote.buildNN(5, 1);
//				smote.run((float) proportion, 1);
				
				/* Undersampling */
				Sampling.undersampling(trainData,
						(float) Math.sqrt((float) (1 / proportion)), 0);

				originalDist = distribution(trainData);

				/* Cluster-Based SMOTE */
				smote.setInstanceSet(trainData);
				smote.buildNN(5, 1);
				smote.run(1, (float) Math.sqrt(proportion));

				break;
			case 4:
				/* Undersampling */
				Sampling.undersampling(trainData,
						(float) Math.sqrt((float) (1 / proportion)), 0);

				/* Cluster-Based SMOTE */
				ClusterBasedSmote cbs = new ClusterBasedSmote(trainData);
				cbs.setOptionDiscretize(false);
				cbs.setOptionDistanceFunction((byte) 1);
				cbs.setOptionFixedGap(true);
				cbs.setOptionNominal((byte) 0);
				trainData = cbs.run();

				break;
		}
		return trainData;
	}
	
	private void printHeader(int sampleID) {
		/* Sampling strategy */
		if (sampleID == 0) {
			header = "Undersampling";
		} else if (sampleID == 1) {
			header = "Oversampling";
		} else if (sampleID == 2) {
			header = "Oversampling with Undersampling";
		} else if (sampleID == 3) {
			header = "SMOTE with Undersampling";
		} else if (sampleID == 4) {
			header = "Cluster-Based SMOTE with Undersampling";
		}

		System.out.print("---------------------------------");
		System.out.println("---------------------------------");
		System.out.println("");
		System.out.println(header);
		System.out.println("");
		System.out.println("			NB			NBRP			C4.5");
		System.out.println("% de fraude	S	E	SE	S	E	SE	S	E	SE");
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
		
		System.out.print(sensibility + "	");
		System.out.print(specificity + "	");
		System.out.print(SE + "	");
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

}