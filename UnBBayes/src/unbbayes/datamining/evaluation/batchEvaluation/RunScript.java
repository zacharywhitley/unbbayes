package unbbayes.datamining.evaluation.batchEvaluation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;

import unbbayes.datamining.datamanipulation.ArffLoader;
import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.datamanipulation.Loader;
import unbbayes.datamining.datamanipulation.TxtLoader;
import unbbayes.datamining.datamanipulation.Utils;
import unbbayes.datamining.evaluation.Folds;
import unbbayes.datamining.evaluation.ROCAnalysis;
import unbbayes.datamining.evaluation.batchEvaluation.model.Datasets;
import unbbayes.datamining.evaluation.batchEvaluation.model.Evaluations;
import unbbayes.datamining.evaluation.batchEvaluation.model.Preprocessors;
import unbbayes.datamining.gui.evaluation.batchEvaluation.controllers.LogsTabController;

/**
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 29/09/2007
 */
public class RunScript {

	private int numRounds = 1;
	private int numFolds = 10;
	private Datasets datasets;
	private Preprocessors preprocessors;
	private Evaluations evaluations;
	private boolean computeAUC;
	private boolean buildROC;
	private String inputFilePath;
	private String outputFilePath;
	private String aucFileName;
	private String rocFileNameExtension;
	private String hullFileNameExtension;
	private int numberFractionDigits = 2;
	private LogsTabController logsWindowController;
	
	public RunScript(Datasets dataset, Preprocessors preprocessors,
			Evaluations evaluations, LogsTabController logsWindowController) {
		this.datasets = dataset;
		this.preprocessors = preprocessors;
		this.evaluations = evaluations;
		this.logsWindowController = logsWindowController;
		computeAUC = evaluations.isComputeAUC();
		buildROC = evaluations.isBuildROC();
		outputFilePath = "results\\";
		aucFileName = "auc.txt";
		rocFileNameExtension = " - roc.txt";
		rocFileNameExtension = " - hull.txt";
	}
	
	public void run() throws Exception {
		InstanceSet instanceSet;
		String originalOutputFilePath = outputFilePath;
		
		/* Get Datasets */
		int numActiveDatasets = datasets.getNumActiveData();
		FoldEvaluation testFold;
		for (int i = 0; i < numActiveDatasets; i++) {
			/* Get instance set */
			instanceSet = getInstanceSet(i);
			
			/* Create output path */
			outputFilePath = inputFilePath + originalOutputFilePath;
			createPath(outputFilePath);
			
			/* Run baby, run! */
			testFold = getEvaluatedProbabilities(instanceSet);
			
			/* Save results */
			saveResults(instanceSet, testFold);
			
			/* Write to log window */
			String log = "Dataset: " + datasets.getDatasetName(i);
			logsWindowController.insertData(log);
		}
	}

	private FoldEvaluation getEvaluatedProbabilities(InstanceSet instanceSet)
	throws Exception {
		/* Check if the instanceSet is compacted */
		if (instanceSet.isCompacted()) {
			throw new IllegalArgumentException("cross validation works " +
					"only on non compacted instanceSet!");
		}
		
		int positiveClass = instanceSet.getPositiveClass();

		InstanceSet train;
		InstanceSet test;
		PreprocessorParameters[] preprocessors = this.preprocessors.getActivePreprocessors();
		FoldEvaluation testFold = new FoldEvaluation(numFolds, numRounds,
				preprocessors, evaluations);

		for (int round = 0; round < numRounds; round++) {
			Folds folds = new Folds(instanceSet, numFolds);
			for (int fold = 0; fold < numFolds; fold++) {
				train = folds.getTrain(fold);
				test = folds.getTest(fold);
				testFold.run(train, test, positiveClass);
			}
		}
		
		return testFold;
	}

	private InstanceSet getInstanceSet(int i) throws Exception {
		String fileName = datasets.getDatasetFullName(i);
		int counterIndex = datasets.getCounterIndex(i);
		int classIndex = datasets.getClassIndex(i);
		
		/* Opens the input set */
		return openFile(fileName, counterIndex, classIndex);
	}
	
	private InstanceSet openFile(String fileName, int counterIndex,
			int classIndex)
	throws Exception {
		InstanceSet instanceSet;
		instanceSet = openFile(fileName, counterIndex);
		instanceSet.setClassIndex(classIndex);
		
		return instanceSet;
	}

	private InstanceSet openFile(String fileName, int counterIndex)
	throws Exception {
		File file = new File(fileName);
		Loader loader = null;
		
		if (fileName.regionMatches(true, fileName.length() - 5, ".arff", 0, 5)) {
        	loader = new ArffLoader(file, -1);
        } else if (fileName.regionMatches(true, fileName.length() - 4, ".txt", 0, 4)) {
        	loader = new TxtLoader(file, -1);
        }

		/* If the datasets is compacted */
		loader.setCounterAttribute(counterIndex);

		while (loader.getInstance()) {
			/* Wait while instances are loaded */
		}
		
		if (loader != null) {
			inputFilePath = file.getParent() + "\\";
			return loader.getInstanceSet();
		} else {
			String exceptionMsg;
			exceptionMsg = "Couldn't open training instanceSet " + fileName;
			throw new Exception(exceptionMsg);
		}
	}

	private void saveResults(InstanceSet instanceSet, FoldEvaluation testFold)
	throws IOException {
		if (instanceSet.isMultiClass()) {
			saveMultiClassResults(instanceSet, testFold);
		} else {
			if (computeAUC) {
				saveAUCResults(instanceSet, testFold);
			}
			if (buildROC) {
				saveROCResults(instanceSet, testFold);
				computeHullResults(instanceSet, testFold);
			}
		}
	}

	private void saveMultiClassResults(InstanceSet instanceSet,
			FoldEvaluation testFold)
	throws IOException {
		int numSamplings = testFold.getNumBatchIterations();
		int numClassifiers = testFold.getNumClassifiers();
		Indexes indexes = testFold.getIndexes();
		for (int samplingID = 0; samplingID < numSamplings; samplingID++) {
			for (int classfID = 0; classfID < numClassifiers; classfID++) {
				saveMultiClassResults(instanceSet, testFold, indexes,
						samplingID, classfID);
			}
		}
	}
	
	private void saveMultiClassResults(InstanceSet instanceSet,
			FoldEvaluation testFold, Indexes indexes, int samplingID,
			int classfierID)
	throws IOException {
		PrintWriter writer;
		File output;
		String fileName;
		int numClasses = instanceSet.numClasses();
		int classIndex = instanceSet.classIndex;
		
		/* Set output path */
		String classifierName = Classifiers.getClassifierName(classfierID);
		String samplingName = testFold.getSamplingName(samplingID).replace('\t', ' ');
		String outputFilePath = new String(this.outputFilePath) + "/";
		outputFilePath += classifierName + " - " + samplingName + " - ";
		
		/* Save confusion matrix on disk */
		fileName = outputFilePath + "confusionMatrix.txt";
		output = new File(fileName);
		writer = new PrintWriter(new FileWriter(output), true);
		for (int i = 0; i < numClasses; i++) {
			writer.print("\t");
			writer.print(instanceSet.attributes[classIndex].value(i));
		}
		writer.println();
		int[][] confusionMatrix = indexes.getConfusionMatrix(samplingID, classfierID);
		for (int i = 0; i < numClasses; i++) {
			writer.print(instanceSet.attributes[classIndex].value(i));
			for (int j = 0; j < numClasses; j++) {
				writer.print("\t");
				writer.print(confusionMatrix[i][j]);
			}
			writer.println();
		}
		writer.println();
		writer.println();
		writer.println("Class frequency");
		writer.println("\t" + "Training set" + "\t" + "Evaluation set");
		for (int i = 0; i < numClasses; i++) {
			writer.print(instanceSet.attributes[classIndex].value(i));
			writer.print("\t");
			writer.print(testFold.getClassDistribution(samplingID, i));
			writer.print("\t");
//			writer.println((int) testSet.getClassDistribution()[i]);
		}
		
		writer.flush();
		writer.close();
		
//		/* Save tp and fp on disk */
//		fileName = outputFilePath + "TP-FP.txt";
//		output = new File(fileName);
//		writer = new PrintWriter(new FileWriter(output), true);
//		writer.println("TP Rate\tFP Rate\tTN Rate\tFN Rate\tClass");
//		for (int i = 0; i < numClasses; i++) {
//			writer.print(indexes.truePositiveRate(samplingID, classfierID, i) + "\t");
//			writer.print(indexes.falsePositiveRate(samplingID, classfierID, i) + "\t");
//			writer.print(indexes.trueNegativeRate(samplingID, classfierID, i) + "\t");
//			writer.print(indexes.falseNegativeRate(samplingID, classfierID, i) + "\t");
//			writer.print(instanceSet.attributes[classIndex].value(i));
//			writer.println();
//		}
//		writer.flush();
//		writer.close();
//		
//		/* Save indexes on disk */
//		fileName = outputFilePath + "indices.txt";
//		output = new File(fileName);
//		writer = new PrintWriter(new FileWriter(output), true);
//		writer.println("Precision\tRecall\tAccuracy\tFScore\tClass");
//		for (int i = 0; i < numClasses; i++) {
//			writer.print(indexes.getPrecision(samplingID, classfierID, i) + "\t");
//			writer.print(indexes.getRecall(samplingID, classfierID, i) + "\t");
//			writer.print(indexes.getAccuracy(samplingID, classfierID, i) + "\t");
//			writer.print(indexes.getFScore(samplingID, classfierID, i) + "\t");
//			writer.print(instanceSet.attributes[classIndex].value(i));
//			writer.println();
//		}
//		writer.flush();
//		writer.close();
	}

	private void saveROCResults(InstanceSet instanceSet, FoldEvaluation testFold) throws IOException {
		PrintWriter writer;
		File output;
		String filePath;
		String fileName;
		String classifierName;
		String sampleName;
		String fpAvg;
		String tpAvg;
		String fpStdDev;
		String tpStdDev;
		
		int numClassifiers = Classifiers.getNumClassifiers();
		int numSamplings = testFold.getNumBatchIterations();
		
		int numROCPoints;
		float[][] rocPointsAvg;
		float[][] rocPointsStdDev;
		
		/* Save roc points */
		for (int i = 0; i < numClassifiers; i++) {
			classifierName = Classifiers.getClassifierName(i);
			
			/* Create path for the classifier results */
			filePath = outputFilePath + "/" + classifierName;
			createPath(filePath);
			
			for (int sampleID = 0; sampleID < numSamplings; sampleID++) {
				/* Create roc output file */
				sampleName = testFold.getSamplingName(sampleID);
//				fileName = filePath + "/" + sampleName + rocFileNameExtension;
				fileName = filePath + "/" + sampleID + sampleName + rocFileNameExtension;
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
					fpAvg = toComma(rocPointsAux[k][0], numberFractionDigits );
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
	
	private void computeHullResults(InstanceSet instanceSet, FoldEvaluation testFold)
	throws IOException {
		int numClassifiers = Classifiers.getNumClassifiers();
		int numSamplings = testFold.getNumBatchIterations();;
		
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
			computeHullResults(hullPoints, instanceSet.getRelationName());
		}
	}
	
	private void computeHullResults(ArrayList<float[]> hullPoints, String name)
	throws IOException {
		/* Compute the convex hull */
		hullPoints = ROCAnalysis.computeConvexHull(hullPoints);
		
		/* Sort ascending by FP and TP */
		ROCAnalysis.sort(hullPoints);
		
		/* Create convex hull output file */
		PrintWriter writer;
		File output;
		output = new File(outputFilePath + name + hullFileNameExtension);
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

	private void createPath(String path) {
		File output = new File(path);
		if (!output.exists()) {
			output.mkdir();
		}
	}

	private void saveAUCResults(InstanceSet instanceSet, FoldEvaluation testFold) throws IOException {
		PrintWriter writer;
		File output;
		String fileName;
		String classifierName;

		int numClassifiers = testFold.getNumClassifiers();
		int numSamplings = testFold.getNumBatchIterations();
		
		double value;
		double stdDev;
		String stringValue;
		String stringStdDev;
		
		/* Create AUC output file */
		fileName = outputFilePath + aucFileName;
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

