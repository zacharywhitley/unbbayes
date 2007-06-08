package unbbayes;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.datamanipulation.Utils;
import unbbayes.datamining.evaluation.Classifiers;
import unbbayes.datamining.evaluation.CrossValidation;
import unbbayes.datamining.evaluation.ROCAnalysis;
import unbbayes.datamining.evaluation.Samplings;
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
	private String outputFilePath;
	private String hullFileName;
	private int classIndex;
	private int counterIndex;
	private String inputFilePath;
	private String inputFileName;
	private String trainingFileName;
	private String testFileName;
	private int numRounds;
	private boolean cross;
	private int k;
	private boolean simplesampling = false;
	private Samplings samplings;
	private TestFold testFold;
	private int numberFractionDigits;
	private int ratioStart;
	private int ratioEnd;
	private int kStart;
	private int kEnd;
	private boolean multiClass;
	private InstanceSet instanceSet;
	
	public TestROC() {
		try {
			rodaTodos();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		new TestROC();
	}

	private void rodaTodos() throws Exception {
		ratioStart = 4;
		ratioEnd = 5;

		kStart = 2;
//		kEnd = 3;
//		kStart = 16;
		kEnd = 3;

		for (int x = 2; x < 3; x++) {
			callSettings(x);

			numRounds = 1;
//			numFolds = 10;
//			cross = false;

			applySettings();
			try {
				run(x);
			} catch (Exception e) {
				System.out.println(e.getMessage());
				continue;
			}
			saveResults(testFold);
//			saveAUCResults2(testFold, kStart);
		}
	}
	
	public void dataset(int x) {
		/* Data set characteristics */
		switch (x) {
			case 0:
				inputFilePath = "c:/dados/outros/kdd";
				inputFileName = "kdd.arff";
				trainingFileName = "";
				testFileName = "";
				classIndex = 17;
				counterIndex = 18;
				positiveClass = 1;
				cross = true;
				numFolds = 10;
				numRounds = 1;
				k = 15;
				multiClass = true;
				break;
			case 1:
				inputFilePath = "c:/dados/outros/glass/";
				inputFileName = "glass.arff";
				trainingFileName = "glass.arff";
				testFileName = "glass.arff";
				classIndex = 9;
				counterIndex = 10;
				positiveClass = 1;
				cross = true;
				numFolds = 10;
				numRounds = 600;
				k = 3;
				break;
			case 2:
				inputFilePath = "c:/dados/outros/yeast/";
				inputFileName = "yeast.arff";
				trainingFileName = "";
				testFileName = "";
				classIndex = 8;
				counterIndex = 9;
				positiveClass = 1;
				cross = true;
				numFolds = 10;
				numRounds = 86;
				k = 15;
				break;
			case 3:
				inputFilePath = "c:/dados/outros/pima/";
				inputFileName = "pima-indians-diabetes.arff";
				trainingFileName = "";
				testFileName = "";
				classIndex = 8;
				counterIndex = 9;
				positiveClass = 1;
				cross = true;
				numFolds = 10;
				numRounds = 40;
				k = 15;
				break;
			case 4:
				inputFilePath = "c:/dados/outros/sonar/";
				inputFileName = "sonar.arff";
				trainingFileName = "";
				testFileName = "";
				classIndex = 60;
				counterIndex = 61;
				positiveClass = 1;
				cross = true;
				numFolds = 10;
				numRounds = 18;
				k = 15;
				break;
			case 5:
				inputFilePath = "c:/dados/outros/vowel/";
				inputFileName = "vowel.arff";
				trainingFileName = "";
				testFileName = "";
				classIndex = 12;
				counterIndex = 13;
				positiveClass = 0;
				cross = true;
				numFolds = 10;
				numRounds = 12;
				k = 15;
				break;
			case 6:
				inputFilePath = "c:/dados/outros/vehicle/";
				inputFileName = "vehicle.arff";
				trainingFileName = "";
				testFileName = "";
				classIndex = 18;
				counterIndex = 19;
				positiveClass = 0;
				cross = true;
				numFolds = 10;
				numRounds = 10;
				k = 15;
				break;
			case 7:
				inputFilePath = "c:/dados/outros/letter-a/";
				inputFileName = "letter-a.arff";
				trainingFileName = "";
				testFileName = "";
				classIndex = 16;
				counterIndex = 17;
				positiveClass = 1;
				cross = true;
				numFolds = 10;
				numRounds = 1;
				k = 15;
				break;
			case 8:
				inputFilePath = "c:/dados/outros/letter-vowel/";
				inputFileName = "letter-vowel.arff";
				trainingFileName = "letter-vowel.arff";
				testFileName = "letter-vowel.arff";
				classIndex = 16;
				counterIndex = 17;
				positiveClass = 1;
				cross = true;
				numFolds = 10;
				numRounds = 1;
				k = 15;
				break;
			case 9:
				inputFilePath = "c:/dados/outros/forest/";
				inputFileName = "cover type.arff";
				trainingFileName = "";
				testFileName = "";
				classIndex = 27;
				counterIndex = 28;
				positiveClass = 1;
				cross = true;
				numFolds = 10;
				numRounds = 1;
				k = 15;
				break;
			case 10:
				inputFilePath = "c:/dados/outros/german/";
				inputFileName = "german.arff";
				trainingFileName = "";
				testFileName = "";
				classIndex = 20;
				counterIndex = 21;
				positiveClass = 1;
				cross = true;
				numFolds = 10;
				numRounds = 15;
				k = 15;
				break;
			case 11:
				inputFilePath = "c:/dados/outros/phoneme/";
				inputFileName = "phoneme.arff";
				trainingFileName = "";
				testFileName = "";
				classIndex = 5;
				counterIndex = 6;
				positiveClass = 1;
				cross = true;
				numFolds = 10;
				numRounds = 1;
				k = 15;
				break;
			case 12:
				inputFilePath = "c:/dados/outros/nursery/";
				inputFileName = "nursery.arff";
				trainingFileName = "";
				testFileName = "";
				classIndex = 8;
				counterIndex = 9;
				positiveClass = 1;
				cross = true;
				numFolds = 10;
				numRounds = 1;
				k = 15;
				break;
			case 13:
				inputFilePath = "c:/dados/outros/satimage/";
				inputFileName = "satimage - smote.arff";
				trainingFileName = "";
				testFileName = "";
				classIndex = 36;
				counterIndex = 37;
				positiveClass = 1;
				cross = true;
				numFolds = 10;
				numRounds = 1;
				k = 15;
				break;
			case 14:
				inputFilePath = "c:/dados/outros/splice-ei/";
				inputFileName = "splice-ei.arff";
				trainingFileName = "";
				testFileName = "";
				classIndex = 60;
				counterIndex = 61;
				positiveClass = 0;
				cross = true;
				numFolds = 10;
				numRounds = 10;
				k = 15;
				break;
			case 15:
				inputFilePath = "c:/dados/outros/splice-ie/";
				inputFileName = "splice-ie.arff";
				trainingFileName = "";
				testFileName = "";
				classIndex = 60;
				counterIndex = 61;
				positiveClass = 0;
				cross = true;
				numFolds = 10;
				numRounds = 10;
				k = 15;
				break;
			case 16:
				inputFilePath = "c:/dados/outros/adult - complete/";
				inputFileName = "Adult - complete.arff";
				trainingFileName = "";
				testFileName = "";
				classIndex = 14;
				counterIndex = 15;
				positiveClass = 1;
				cross = true;
				numFolds = 10;
				numRounds = 1;
				k = 15;
				break;
			case 17:
				inputFilePath = "c:/dados/outros/adult - numeric/";
				inputFileName = "Adult - numeric.arff";
				trainingFileName = "";
				testFileName = "";
				classIndex = 6;
				counterIndex = 7;
				positiveClass = 1;
				cross = true;
				numFolds = 10;
				numRounds = 1;
				k = 15;
				break;
		}
	}
	
	public void run(int x) throws Exception {
		System.out.println(inputFileName);
		long start = System.currentTimeMillis();
		System.out.println(x + " - " + (new java.text.SimpleDateFormat("HH:mm:ss")).format(new Date()));

//		callSettings(x);
		
		if (cross) {
			runCross();
		} else {
			run();
		}

		long time = System.currentTimeMillis() - start;
		int min = (int) (time / 1000) / 60;
		int sec = (int) (time / 1000) - (min * 60);
		String timeMesg;
		timeMesg = "Time for " + x + ": " + min + "\"" + sec + "'";
		System.out.println(timeMesg);
		System.out.println();
	}

	private void callSettings(int x) {
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
		
		aucFileName = "auc.txt";
		rocFileNameExtension = " - roc.txt";
		hullFileName = "hull.txt";
		
		/* Add paths to the file names */
		outputFilePath = "results/";
		inputFileName = inputFilePath + inputFileName;
		trainingFileName = inputFilePath + trainingFileName;
		testFileName = inputFilePath + testFileName;
		outputFilePath = inputFilePath + outputFilePath;
		
		/* Number of fraction digits */
		numberFractionDigits = 10;
	}
	
	public void applySettings() {
		/* Create the utils */
		testsetUtils = new TestsetUtils();
		testsetUtils.setPositiveClass(positiveClass);
		testsetUtils.setK(k);
		testsetUtils.setSimplesampling(simplesampling);
		testsetUtils.setRatioStart(ratioStart);
		testsetUtils.setRatioEnd(ratioEnd);
		testsetUtils.setKstart(kStart);
		testsetUtils.setKend(kEnd);
		
		/**** C4.5 options ****/
		testsetUtils.setIfUsingPrunning(false);
		testsetUtils.setConfidenceLevel(0.25f);
		
		if (numRounds <= 0) {
			throw new IllegalArgumentException("numRounds " +
					"must be greater than zero!");
		}
		
	}

	private void runCross() throws Exception {
		/* Opens the input set */
		instanceSet = openFile(inputFileName);
		
		if (instanceSet == null) {
			String exceptionMsg = "Couldn't open training instanceSet " 
				+ inputFileName;
			throw new Exception(exceptionMsg);
		}
		
		/* Run baby, run! */
		testFold = CrossValidation.getEvaluatedProbabilities(instanceSet,
				numFolds, numRounds, testsetUtils);
	
		samplings = testFold.getSamplings();
	}
	
	private InstanceSet openFile(String fileName) throws IOException {
		InstanceSet instanceSet;
		instanceSet = TestsetUtils.openFile(fileName, counterIndex, classIndex);
		testsetUtils.setInstanceSetType(instanceSet.getInstanceSetType());
		
		return instanceSet;
	}

	private void run() throws Exception {
		/* Opens the training set */
		instanceSet = openFile(trainingFileName);
		
		if (instanceSet == null) {
			String exceptionMsg = "Couldn't open training instanceSet " 
				+ trainingFileName;
			throw new Exception(exceptionMsg);
		}
		
		/* Opens the test set */
		InstanceSet testSet;
		testSet = TestsetUtils.openFile(testFileName, counterIndex, classIndex);
		if (testSet == null) {
			String exceptionMsg = "Couldn't open training instanceSet " 
				+ testFileName;
			throw new Exception(exceptionMsg);
		}
		testSet.setClassIndex(classIndex);
		
		/* Run baby, run! */
		testFold = CrossValidation.getEvaluatedProbabilities(instanceSet,
				testSet, numRounds, testsetUtils, samplings);
	
		samplings = testFold.getSamplings();
	}
	
	private void saveResults(TestFold testFold) throws IOException {
		if (multiClass) {
			saveMultiClassResults(testFold);
		} else {
			saveAUCResults(testFold);
			saveROCResults(testFold);
			computeHullResults(testFold);
		}
	}

	private void saveMultiClassResults(TestFold testFold) {
		int numSamplings = testFold.getNumSamplings();
		int numClassifiers = testFold.getNumClassifiers();
		for (int samplingID = 0; samplingID < numSamplings; samplingID++) {
			for (int classfID = 0; classfID < numClassifiers; classfID++) {
				
			}
		}
		
	}
	
//	private void saveMultiClassResults(TestFold testFold, int samplingID, int classfID) {
//		PrintWriter writer;
//		File output;
//		String fileName;
//		int numClasses = instanceSet.numClasses();
//		int classIndex = instanceSet.classIndex;
//
//		/* Save tp and fp on disk */
//		fileName = outputFilePath + "/TP-FP.txt";
//		output = new File(fileName);
//		writer = new PrintWriter(new FileWriter(output), true);
//		writer.println("TP Rate\tFP Rate\tTN Rate\tFN Rate\tClass");
//		for (int i = 0; i < numClasses; i++) {
//			writer.print(testFold.truePositiveRate(i) + "\t");
//			writer.print(testFold.falsePositiveRate(i) + "\t");
//			writer.print(testFold.trueNegativeRate(i) + "\t");
//			writer.print(testFold.falseNegativeRate(i) + "\t");
//			writer.print(instanceSet.attributes[classIndex].value(i));
//			writer.println();
//		}
//		writer.flush();
//		writer.close();
//		
//		/* Save indexes on disk */
//		fileName = outputFilePath + "/indices.txt";
//		output = new File(fileName);
//		writer = new PrintWriter(new FileWriter(output), true);
//		writer.println("Precision\tRecall\tAccuracy\tFScore\tClass");
//		for (int i = 0; i < numClasses; i++) {
//			writer.print(testFold.getPrecision(i) + "\t");
//			writer.print(testFold.getRecall(i) + "\t");
//			writer.print(testFold.getAccuracy(i) + "\t");
//			writer.print(testFold.getFScore(i) + "\t");
//			writer.print(instanceSet.attributes[classIndex].value(i));
//			writer.println();
//		}
//		writer.flush();
//		writer.close();
//		
//		/* Save confusion matrix on disk */
//		fileName = outputFilePath + "/confusionMatrix.txt";
//		output = new File(fileName);
//		writer = new PrintWriter(new FileWriter(output), true);
//		for (int i = 0; i < numClasses; i++) {
//			if (i > 0) writer.print("\t");
//			writer.print(instanceSet.attributes[classIndex].value(i));
//		}
//		writer.println();
//		int[][] confusionMatrix = testFold.getConfusionMatrix();
//		for (int i = 0; i < numClasses; i++) {
//			for (int j = 0; j < numClasses; j++) {
//				writer.print(confusionMatrix[i][j] + "\t");
//			}
//			writer.print(instanceSet.attributes[classIndex].value(i));
//			writer.println();
//		}
//		writer.flush();
//		writer.close();
//	}

	private void saveROCResults(TestFold testFold) throws IOException {
		PrintWriter writer;
		File output;
		String fileName;
		String classifierName;
		String sampleName;
		String fpAvg;
		String tpAvg;
		String fpStdDev;
		String tpStdDev;
		
		int numClassifiers = Classifiers.getNumClassifiers();
		int numSamplings = samplings.getNumSamplings();
		
		int numROCPoints;
		float[][] rocPointsAvg;
		float[][] rocPointsStdDev;
		
		/* Save roc points */
		for (int i = 0; i < numClassifiers; i++) {
			classifierName = Classifiers.getClassifierName(i);
			
			for (int sampleID = 0; sampleID < numSamplings; sampleID++) {
				/* Create roc output file */
				sampleName = samplings.getSamplingName(sampleID);
				fileName = outputFilePath + "/" + classifierName;
//				fileName += "/" + sampleName + rocFileNameExtension;
				fileName += "/" + sampleID + sampleName + rocFileNameExtension;
				output = new File(fileName);
				writer = new PrintWriter(new FileWriter(output), true);
				
				/* Get roc values */
				rocPointsAvg = testFold.getRocPointsAvg(sampleID, i);
				rocPointsStdDev = testFold.getRocPointsStdDev(sampleID, i);
				
				/* Sort ascending by FP and TP */
				numROCPoints = rocPointsAvg.length;
				float[][] rocPointsAux = new float[numROCPoints][4];
				for (int n = 0; n < numROCPoints; n++) {
					rocPointsAux[n][0] = rocPointsAvg[n][0];
					rocPointsAux[n][1] = rocPointsAvg[n][1];
					rocPointsAux[n][2] = rocPointsStdDev[n][0];
					rocPointsAux[n][3] = rocPointsStdDev[n][1];
				}
				Utils.sort(rocPointsAux);
				
				/* Save roc values */
				numROCPoints = rocPointsAvg.length;
				writer.println();
				writer.println(sampleName);
				for (int k = 0; k < numROCPoints; k++) {
					fpAvg = toComma(rocPointsAux[k][0], numberFractionDigits);
					tpAvg = toComma(rocPointsAux[k][1], numberFractionDigits);
					fpStdDev = toComma(rocPointsAux[k][2], numberFractionDigits);
					tpStdDev = toComma(rocPointsAux[k][3], numberFractionDigits);
					writer.print(fpAvg + "\t" + tpAvg + "\t");
					writer.println(fpStdDev + "\t" + tpStdDev);
				}
				writer.flush();
				writer.close();
			}
		}
	}
	
	private void computeHullResults(TestFold testFold)
	throws IOException {
		int numClassifiers = Classifiers.getNumClassifiers();
		int numSamplings = samplings.getNumSamplings();
		
		ArrayList<float[]> hullPoints;
		float[][] rocPoints;
		int numRocPoints;

		/* Merge all rocPoints */
		hullPoints = new ArrayList<float[]>();
		for (int classID = 0; classID < numClassifiers; classID++) {
			for (int sampleID = 0; sampleID < numSamplings; sampleID++) {
				/* Get roc values */
				rocPoints = testFold.getRocPointsAvg(sampleID, classID);
				numRocPoints = rocPoints.length;
				
				for (int i = 0; i < numRocPoints; i++) {
					hullPoints.add(rocPoints[i]);
				}
			}
			computeHullResults(hullPoints, "");
		}
	}
	
	private void computeHullResults() throws IOException {
		double[][] rocPoints = {
								};
		
		/* Merge all rocPoints */
		ArrayList<float[]> hullPoints = new ArrayList<float[]>();
		int numRocPoints = rocPoints.length;
		for (int i = 0; i < numRocPoints; i++) {
			float[] ema = new float[2];
			ema[0] = (float) rocPoints[i][0];
			ema[1] = (float) rocPoints[i][1];
			hullPoints.add(ema);
		}
		/*
		 * 0: banco do brasil
		 * 1: pima
		 * 3: phoneme
		 * 5: satimage
		 * 2: adult - numeric
		 * 4: adult - complete - no null
		 * 6: forest cover
		 */
		dataset(16);
		
		aucFileName = "auc.txt";
		rocFileNameExtension = ".roc";
		hullFileName = "hull.txt";
		
		/* Add paths to the file names */
		outputFilePath = "results/";
		inputFileName = inputFilePath + inputFileName;
		trainingFileName = inputFilePath + trainingFileName;
		testFileName = inputFilePath + testFileName;
		outputFilePath = inputFilePath + outputFilePath;
		
		computeHullResults(hullPoints, "");
	}
	
	private void computeHullResults(ArrayList<float[]> hullPoints, String folder)
	throws IOException {
		/* Compute the convex hull */
		hullPoints = ROCAnalysis.computeConvexHull(hullPoints);
		
		/* Sort ascending by FP and TP */
		ROCAnalysis.sort(hullPoints);
		
		/* Create convex hull output file */
		PrintWriter writer;
		File output;
		output = new File(outputFilePath + folder + hullFileName);
		writer = new PrintWriter(new FileWriter(output), true);
		
		/* Save convex hull points on disk */
		int numHullResults = hullPoints.size();
		String fp;
		String tp;
		writer.println();
		writer.println("Hull");
		numberFractionDigits = 10;
		for (int n = 0; n < numHullResults; n++) {
			fp = toComma(hullPoints.get(n)[0], numberFractionDigits);
			tp = toComma(hullPoints.get(n)[1], numberFractionDigits);
			writer.println(fp + "\t" + tp);
		}
		writer.flush();
		writer.close();
	}
	
	private String toComma(float f, int numberFractionDigits) {
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		dfs.setDecimalSeparator(',');
		DecimalFormat format = new DecimalFormat();
	    format.setDecimalFormatSymbols(dfs);
	    format.setMinimumFractionDigits(numberFractionDigits);
	    
		return format.format(f);
	}

	private void saveAUCResults(TestFold testFold)
	throws IOException {
		PrintWriter writer;
		File output;
		String fileName;
		String classifierName;

		int numClassifiers = testFold.getNumClassifiers();
		int numSamplings = testFold.getNumSamplings();
		
		double value;
		double stdDev;
		String stringValue;
		String stringStdDev;
		
		/* Create AUC output file */
		fileName = outputFilePath + "/" + aucFileName;
		output = new File(fileName);
		writer = new PrintWriter(new FileWriter(output), true);
		
		/* Save auc values on disk */
		for (int i = 0; i < numClassifiers; i++) {
			classifierName = Classifiers.getClassifierName(i);

			for (int sampleID = 0; sampleID < numSamplings; sampleID++) {
				
				/* Write classifier name */
				writer.print(classifierName);
	
				/* Write sample name */
				writer.print("\t" + testFold.getSamplingName(sampleID));
	
				/* AUC */
				value = testFold.getAuc(sampleID, i)[0];
				stdDev = testFold.getAuc(sampleID, i)[1];
				stringValue = String.format(Locale.FRANCE, "%.2f", value);
				stringStdDev = String.format(Locale.FRANCE, "%.2f", stdDev);
				writer.print("\t" + stringValue + " (" + stringStdDev + ")");
				
//				/* Global error */
//				value = testFold.getGlobalError(sampleID, i)[0];
//				stdDev = testFold.getGlobalError(sampleID, i)[1];
//				stringValue = String.format(Locale.FRANCE, "%.2f", value);
//				stringStdDev = String.format(Locale.FRANCE, "%.2f", stdDev);
//				writer.print("\t" + stringValue + " (" + stringStdDev + ")");
//				
//				/* Sensitivity */
//				value = testFold.getSensitivity(sampleID, i)[0];
//				stdDev = testFold.getSensitivity(sampleID, i)[1];
//				stringValue = String.format(Locale.FRANCE, "%.2f", value);
//				stringStdDev = String.format(Locale.FRANCE, "%.2f", stdDev);
//				writer.print("\t" + stringValue + " (" + stringStdDev + ")");
//				
//				/* Specificity */
//				value = testFold.getSpecificity(sampleID, i)[0];
//				stdDev = testFold.getSpecificity(sampleID, i)[1];
//				stringValue = String.format(Locale.FRANCE, "%.2f", value);
//				stringStdDev = String.format(Locale.FRANCE, "%.2f", stdDev);
//				writer.print("\t" + stringValue + " (" + stringStdDev + ")");
//				
//				/* SE */
//				value = testFold.getSE(sampleID, i)[0];
//				stdDev = testFold.getSE(sampleID, i)[1];
//				stringValue = String.format(Locale.FRANCE, "%.2f", value);
//				stringStdDev = String.format(Locale.FRANCE, "%.2f", stdDev);
//				writer.print("\t" + stringValue + " (" + stringStdDev + ")");
				
				writer.println();
			}
		}
		writer.flush();
		writer.close();
	}
	
	private void saveAUCResults2(TestFold testFold, int kSeries)
	throws IOException {
		PrintWriter writer;
		File output;
		String fileName;

		int numClassifiers = Classifiers.getNumClassifiers();
		int numSamplings = testFold.getNumSamplings();
		
		double value;
		double stdDev;
		String stringValue;
		String stringStdDev;
		
		/* Create AUC output file */
		fileName = outputFilePath + "/" + kSeries + " - " + aucFileName;
		output = new File(fileName);
		writer = new PrintWriter(new FileWriter(output), true);
		
		/* Save auc values on disk */
		for (int i = 0; i < numClassifiers; i++) {
			for (int sampleID = 0; sampleID < numSamplings; sampleID++) {
				
				/* Write sample name */
				writer.print(testFold.getSamplingName(sampleID));
	
				/* AUC */
				value = testFold.getAuc(sampleID, i)[0];
				stdDev = testFold.getAuc(sampleID, i)[1];
				stringValue = String.format(Locale.FRANCE, "%.2f", value);
				stringStdDev = String.format(Locale.FRANCE, "%.2f", stdDev);
				writer.print("\t" + stringValue + " (" + stringStdDev + ")");
				
				writer.println();
			}
		}
		writer.flush();
		writer.close();
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