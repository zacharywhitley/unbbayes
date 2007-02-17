package unbbayes;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.evaluation.Classifiers;
import unbbayes.datamining.evaluation.CrossValidation;
import unbbayes.datamining.evaluation.Samples;
import unbbayes.datamining.evaluation.TestFold;
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
	private TestsetUtils testsetUtils;
	private String rocFileNameExtension;
	private int numFolds;
	String outputFileName;
	private String aucFileName;
	private int positiveClass;
	private int negativeClass;
	private String outputFilePath;
	private String hullFileName;
	private int classIndex;
	private int counterIndex;
	private String inputFilePath;
	private String inputFileName;
	private String trainingFileName;
	private String testFileName;
	private String aucAnalysisFileName;
	private double alfa = 0.05;
	private int numRounds;
	private boolean cross;
	private int k;
	
	public TestROC() {
		try {
			for (int x = 6; x < 7; x++) {
					run(x);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		new TestROC();
	}

	public void dataset(int x) {
		/* Data set characteristics */
		switch (x) {
			case 0:
				inputFilePath = "d:/dados/";
				inputFileName = "m1t.arff";
				trainingFileName = "m1t.arff";
				testFileName = "m1av.arff";
				classIndex = 10;
				counterIndex = 11;
				positiveClass = 1;
				cross = false;
				numFolds = 10;
				numRounds = 1;
				k = 15;
				break;
			case 1:
				inputFilePath = "d:/dados/outros/pima/";
				inputFileName = "pima-indians-diabetes.arff";
				trainingFileName = "pima-indians-diabetes-training.arff";
				testFileName = "pima-indians-diabetes-test.arff";
				classIndex = 8;
				counterIndex = 9;
				positiveClass = 1;
				cross = true;
				numFolds = 10;
				numRounds = 100;
				k = 15;
				break;
			case 2:
				inputFilePath = "D:/dados/outros/phoneme/";
				inputFileName = "phoneme.arff";
				trainingFileName = "phoneme-train.arff";
				testFileName = "phoneme-test.arff";
				classIndex = 5;
				counterIndex = -1;
				positiveClass = 1;
				cross = true;
				numFolds = 10;
				numRounds = 20;
				k = 15;
				break;
			case 3:
				inputFilePath = "d:/dados/outros/satimage/";
				inputFileName = "satimage - smote.arff";
				trainingFileName = "satimage - smote-train.arff";
				testFileName = "satimage - smote-test.arff";
				classIndex = 36;
				counterIndex = -1;
				positiveClass = 1;
				cross = true;
				numFolds = 10;
				numRounds = 20;
				k = 15;
				break;
			case 4:
				inputFilePath = "D:/dados/outros/adult - numeric/";
				inputFileName = "Adult - numeric.arff";
				trainingFileName = "Adult - numeric-train.arff";
				testFileName = "Adult - numeric-test.arff";
				classIndex = 6;
				counterIndex = -1;
				positiveClass = 1;
				cross = true;
				numFolds = 10;
				numRounds = 5;
				k = 15;
				break;
			case 5:
				inputFilePath = "D:/dados/outros/adult - complete/";
				inputFileName = "Adult - complete - no null.arff";
				trainingFileName = "Adult - complete - no null-train.arff";
				testFileName = "Adult - complete - no null-test.arff";
				classIndex = 14;
				counterIndex = -1;
				positiveClass = 1;
				cross = true;
				numFolds = 10;
				numRounds = 5;
				k = 15;
				break;
			case 6:
				inputFilePath = "d:/dados/outros/forest/";
				inputFileName = "cover type - smote.arff";
				trainingFileName = "cover type - smote-train.arff";
				testFileName = "cover type - smote-test.arff";
				classIndex = 54;
				counterIndex = -1;
				positiveClass = 1;
				cross = true;
				numFolds = 10;
				numRounds = 5;
				k = 15;
				break;
			case 7:
				inputFilePath = "";
				inputFileName = "";
				trainingFileName = "";
				testFileName = "";
				classIndex = 0;
				counterIndex = -1;
				positiveClass = 1;
				cross = false;
				numFolds = 10;
				numRounds = 100;
				k = 15;
				break;
		}
	}
	
	public void run(int x) throws Exception {
		/*
		 * 0: banco do brasil
		 * 1: pima
		 * 3: phoneme
		 * 5: satimage
		 * 2: adult - numeric
		 * 4: adult - complete - no null
		 * 6: forest cover
		 */
		dataset(x);
		
		negativeClass = Math.abs(1 - positiveClass);
		aucFileName = "auc.txt";
		aucAnalysisFileName = "aucAnalysis.txt";
		rocFileNameExtension = ".roc";
		hullFileName = "hull.txt";
		
		/* Add paths to the file names */
		outputFilePath = "results/";
		inputFileName = inputFilePath + inputFileName;
		trainingFileName = inputFilePath + trainingFileName;
		testFileName = inputFilePath + testFileName;
		outputFilePath = inputFilePath + outputFilePath;
		
		/* Create the utils */
		testsetUtils = new TestsetUtils();
		testsetUtils.setInterestingClass(positiveClass);
		testsetUtils.setK(k);
		
		/**** C4.5 options ****/
		testsetUtils.setIfUsingPrunning(false);
		testsetUtils.setConfidenceLevel(0.01f);
		
		if (numRounds <= 0) {
			throw new IllegalArgumentException("numRounds " +
					"must be greater than zero!");
		}
		
		if (cross) {
			runCross();
		} else {
			run();
		}
	}

	private void runCross() throws Exception {
		/* Opens the input set */
		InstanceSet instanceSet;
		instanceSet = TestsetUtils.openFile(inputFileName, counterIndex);
		if (instanceSet == null) {
			String exceptionMsg = "Couldn't open training instanceSet " 
				+ inputFileName;
			throw new Exception(exceptionMsg);
		}
		instanceSet.setClassIndex(classIndex);
		
		/* Run baby, run! */
		TestFold testFold;
		testFold = CrossValidation.getEvaluatedProbabilities(instanceSet,
				numFolds, numRounds, testsetUtils);
	
		saveResults(testFold);
	}
	
	private void run() throws Exception {
		/* Opens the training set */
		InstanceSet trainingSet;
		trainingSet = TestsetUtils.openFile(trainingFileName, counterIndex);
		if (trainingSet == null) {
			String exceptionMsg = "Couldn't open training instanceSet " 
				+ trainingFileName;
			throw new Exception(exceptionMsg);
		}
		trainingSet.setClassIndex(classIndex);
		
		/* Opens the test set */
		InstanceSet testSet;
		testSet = TestsetUtils.openFile(testFileName, counterIndex);
		if (testSet == null) {
			String exceptionMsg = "Couldn't open training instanceSet " 
				+ testFileName;
			throw new Exception(exceptionMsg);
		}
		testSet.setClassIndex(classIndex);
		
		/* Run baby, run! */
		TestFold testFold;
		testFold = CrossValidation.getEvaluatedProbabilities(trainingSet,
				testSet, numRounds, testsetUtils);
	
		saveResults(testFold);
	}
	
	private void saveResults(TestFold testFold) throws IOException {
//		selectBestAUC(testFold);
		saveAUCResults(testFold);
//		saveROCResults(testFold);
//		analyzeAUC(testFold);
//		computeHullResults(testFold);
	}

	
	private void saveROCResults(TestFold testFold) throws IOException {
		PrintWriter writer;
		File output;
		String fileName;
		String classifierName;
		String sampleName;
		
		int numClassifiers = Classifiers.getNumClassifiers();
		int numSamples = Samples.getNumSamples();
		
		int numROCPoints;
		float[][] rocPoints;
		
		/* Save roc points */
		for (int i = 0; i < numClassifiers; i++) {
			classifierName = Classifiers.getClassifierName(i);
			
			for (int sampleID = 0; sampleID < numSamples; sampleID++) {
				/* Create roc output file */
				sampleName = Samples.getSampleName(sampleID);
				fileName = outputFilePath + "/" + classifierName;
				fileName += "/" + sampleName + rocFileNameExtension;
				output = new File(fileName);
				writer = new PrintWriter(new FileWriter(output), true);
				
				/* Get roc values */
				rocPoints = testFold.getRocPointsProbs(sampleID, i);
				
				/* Save roc values */
				numROCPoints = rocPoints.length;
				writer.println("FP\tTP");
				for (int k = 0; k < numROCPoints; k++) {
					writer.println(rocPoints[k][0] + "\t" + rocPoints[k][1]);
				}
				writer.flush();
				writer.close();
			}
		}
	}
	
//	private void computeHullResults(ArrayList<Object> hullPointsAux)
//	throws IOException {
//		PrintWriter writer;
//		File output;
//		String fileName;
//		String classifierName;
//		String sampleName;
//		
//		int numClassifiers = testsetUtils.getNumClassifiers();
//		int numSamples = testsetUtils.getNumSamples();
//		
//		int numROCPoints;
//		float[][] rocPoints;
//		
//		/* Save roc points */
//		for (int i = 0; i < numClassifiers; i++) {
//			classifierName = Classifiers.getClassifierName(i);
//			
//			for (int j = 0; j < numSamples; j++) {
//				/* Create roc output file */
//				sampleName = Samples.getSampleName(j);
//				fileName = outputFilePath + "/" + classifierName;
//				fileName += "/" + sampleName;
//				output = new File(fileName);
//				writer = new PrintWriter(new FileWriter(output), true);
//				
//				/* Get roc values */
//				rocPoints = testFold.getRocPointsProbs(i, j);
//				
//				/* Save roc values */
//				numROCPoints = rocPoints.length;
//				writer.println("FP\tTP");
//				for (int k = 0; k < numROCPoints; k++) {
//					writer.println(rocPoints[k][0] + "\t" + rocPoints[k][1]);
//				}
//				writer.flush();
//				writer.close();
//			}
//		}
//		/* Merge all rocPoints */
//		int size = hullPointsAux.size();
//		ArrayList<float[]> hullPoints = new ArrayList<float[]>();
//		float[][] aux;
//		int numRocPoints;
//		for (int i = 0; i < size; i++) {
//			aux = (float[][]) hullPointsAux.get(i);
//			numRocPoints = aux.length;
//			for (int j = 0; j < numRocPoints; j++) {
//				hullPoints.add(aux[j]);
//			}
//		}
//		
//		/* Sort ascending by FP and TP */
//		ROCAnalysis.sort(hullPoints);
//		
//		/* Remove repeated points */
//		ROCAnalysis.removeRepeated(hullPoints);
//		
//		/* Compute the convex hull */
//		hullPoints = ROCAnalysis.computeConvexHull(hullPoints);
//		
//		/* Create convex hull output file */
//		PrintWriter writer;
//		File output;
//		output = new File(outputFilePath + hullFileName);
//		writer = new PrintWriter(new FileWriter(output), true);
//		
//		/* Save convex hull points on disk */
//		writer.println("FP\tTP");
//		int numHullResults = hullPoints.size();
//		for (int n = 0; n < numHullResults; n++) {
//			writer.println(hullPoints.get(n)[0] + "\t" + hullPoints.get(n)[1]);
//		}
//		writer.flush();
//		writer.close();
//	}
//	
	private void saveAUCResults(TestFold testFold)
	throws IOException {
		PrintWriter writer;
		File output;
		String fileName;
		String classifierName;

		int numClassifiers = Classifiers.getNumClassifiers();
		int numSamples = Samples.getNumSamples();
		
		double value;
		double stdDev;
		
		/* Save auc values on disk */
		for (int i = 0; i < numClassifiers; i++) {
			classifierName = Classifiers.getClassifierName(i);

			/* Create AUC output file */
			fileName = outputFilePath + classifierName + "/" + aucFileName;
			output = new File(fileName);
			writer = new PrintWriter(new FileWriter(output), true);
			
			for (int sampleID = 0; sampleID < numSamples; sampleID++) {
				writer.print(Samples.getSampleName(sampleID));
	
				/* AUC */
				value = testFold.getAuc(sampleID, i)[0];
				stdDev = testFold.getAuc(sampleID, i)[1];
				writer.print("\t" + String.format(Locale.FRANCE, "%.2f", value));
				writer.print("\t" + String.format(Locale.FRANCE, "%.2f", stdDev));
				
				/* Sensitivity */
				value = testFold.getSensitivity(sampleID, i)[0];
				stdDev = testFold.getSensitivity(sampleID, i)[1];
				writer.print("\t" + String.format(Locale.FRANCE, "%.2f", value));
				writer.print("\t" + String.format(Locale.FRANCE, "%.2f", stdDev));
				
				/* Specificity */
				value = testFold.getSpecificity(sampleID, i)[0];
				stdDev = testFold.getSpecificity(sampleID, i)[1];
				writer.print("\t" + String.format(Locale.FRANCE, "%.2f", value));
				writer.print("\t" + String.format(Locale.FRANCE, "%.2f", stdDev));
				
				/* SE */
				value = testFold.getSE(sampleID, i)[0];
				stdDev = testFold.getSE(sampleID, i)[1];
				writer.print("\t" + String.format(Locale.FRANCE, "%.2f", value));
				writer.print("\t" + String.format(Locale.FRANCE, "%.2f", stdDev));
				
				writer.println();
			}
			writer.flush();
			writer.close();
		}
	}
	
//	private void analyzeAUC(ArrayList<Object> aucResultsAux, boolean cross)
//	throws IOException {
//		int size = aucResultsAux.size();
//		double f0;
//		
//		/*
//		 * Sample ID:
//		 * 0 - Undersampling
//		 * 1 - Oversampling
//		 * 2 - SMOTE
//		 * 3 - Cluster-Based Oversampling (flattens clusters' distribution)
//		 * 4 - Cluster-Based SMOTE (flattens clusters' distribution)
//		 * 5 - Cluster-Based Oversampling modified (proportional to its clusters)
//		 * 6 - Cluster-Based SMOTE modified (proportional to its clusters)
//		 */
//		
//		double[][] aucResults = new double[size / 2][2];
//		
//		for (int i = 1; i < size / 2; i += 2) {
//			aucResults[i] = (double[]) aucResultsAux.get(i);
//		}
//		
//		if (cross) {
//			f0 = analyzeANOVA(aucResults);
//		} else {
//			f0 = analyzeChiSquare(aucResults);
//		}
//		
//		/* Number of groups being compared */
//		int k = aucResults.length;
//		
//		/* Size of the groups been compared */
//		int n = numFolds * numRounds;
//		
//		/* Compute probability of the null hypothesis */
//		int dfB = k - 1;
//		int dfW = n * dfB;
//		double fProb = Statistics.computeFProb(dfB, dfW, f0);
//		
//		/* Create auc analysis output file */
//		File output = new File(outputFilePath + aucAnalysisFileName);
//		PrintWriter writer = new PrintWriter(new FileWriter(output), true);
//		
//		/* Save auc analysis on disk */
//		writer.println("F:\t" + f0);
//		writer.println("df B:\t" + dfB);
//		writer.println("df W:\t" + dfW);
//		writer.println("Null hypothesis probability:\t" + (int) (fProb * 100)
//				+ "%");
//		if (fProb > alfa) {
//			writer.println("Accepted at " + (1 - alfa) * 100 + "% (they don't " +
//					"differ)");
//		} else {
//			writer.println("Rejected at " + (1 - alfa) * 100 + "% (they are " +
//					"significantly different!)");
//		}
//		writer.flush();
//		writer.close();
//	}
	
//	private double analyzeChiSquare(double[][] aucResults) {
//		int numAucResults = aucResults.size();
//
//		for (int n = 1; n < numAucResults; n += 2) {
//			mean = ((double []) aucResults.get(n))[0];
//			sum += mean;
//			sumSquared += mean * mean;
//		}
//		return 0;
//	}

//	private double analyzeANOVA(double[][] aucResults)
//	throws IOException {
//		/* Number of groups being compared */
//		int k = aucResults.length;
//		
//		/* Size of the groups been compared */
//		int n = numFolds * numRounds;
//
//		/* Sum of means */
//		double sum = 0;
//		
//		/* Sum of squared means */
//		double sumSquared = 0;
//		
//		/* Compute sum of means and sum of squared means */
//		double mean;
//		for (int i = 0; i < k; i++) {
//			mean = aucResults[i][0];
//			sum += mean;
//			sumSquared += mean * mean;
//		}
//		
//		/* Variance of the means */
//		double sX2;
//		sX2 = (sumSquared - (sum * sum / k)) / (k - 1);
//		
//		/* Variance between groups */
//		double sB2;
//		sB2 = sX2 * n;
//		
//		/* Variance within groups */
//		double sW2 = 0;
//		double s2Aux;
//		for (int i = 0; i < k; i++) {
//			s2Aux = aucResults[i][1];
//			sW2 += s2Aux * s2Aux;
//		}
//		sW2 /= k;
//		
//		/* Compute F0 */
//		double f0 = sB2 / sW2;
//		
//		return f0;
//	}


}