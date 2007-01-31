package unbbayes;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.evaluation.ROCAnalysis;
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
	
	public TestROC() {
		try {
			run();
//			testComputeHullResults();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		new TestROC();
	}

	public void run() throws Exception {
		/* Data set characteristics */
		String inputFilePath = "d:/dados/outros/pima/";
		outputFilePath = "results/";
		String inputFileName = "pima-indians-diabetes.arff";
		String trainFileName = "pima-indians-diabetes-training.arff";
		String testFileName = "pima-indians-diabetes-test.arff";
		int classIndex = 8;
		int counterIndex = 9;
		positiveClass = 1;
		negativeClass = 1 - positiveClass;
		aucFileName = "auc.txt";
		rocFileNameExtension = ".roc";
		hullFileName = "hull.txt";
		
		/* Cross validation */
		boolean cross = true;
		numFolds = 10;

		/* Add paths to the file names */
		inputFileName = inputFilePath + inputFileName;
		trainFileName = inputFilePath + trainFileName;
		testFileName = inputFilePath + testFileName;
		outputFilePath = inputFilePath + outputFilePath;
		
		/* Create the utils */
		testsetUtils = new TestsetUtils();
		testsetUtils.setInterestingClass(positiveClass);
		
		if (cross) {
			trainFileName = inputFileName;
		}
		
		runROCAUC(trainFileName, testFileName, classIndex, counterIndex, cross);
	}

	private void runROCAUC(String inputFileName, String testFileName,
			int classIndex, int counterIndex, boolean cross) throws Exception {
		float[] newDist;
		InstanceSet trainData;
		ArrayList<String[]> aucResults = new ArrayList<String[]>();
		ArrayList<float[]> hullResults = new ArrayList<float[]>();
		
		/* Opens the input set */
		InstanceSet instanceSet;
		instanceSet = TestsetUtils.openFile(inputFileName, counterIndex);
		if (instanceSet == null) {
			String exceptionMsg = "Couldn't open training instanceSet " 
				+ inputFileName;
			throw new Exception(exceptionMsg);
		}
		instanceSet.setClassIndex(classIndex);
		float[] originalDist = TestsetUtils.distribution(instanceSet);
		
		InstanceSet testData = null;
		if (!cross) {
			/* Opens the test set */
			testData = TestsetUtils.openFile(testFileName, counterIndex);
			if (testData == null) {
				String exceptionMsg = "Couldn't open test data " + testFileName;
				throw new Exception(exceptionMsg);
			}
			testData.setClassIndex(classIndex);

			trainData = new InstanceSet(instanceSet);
		} else {
			trainData = instanceSet;
		}

		/* Get original results */
		ArrayList<Object> results;
		results = testsetUtils.generateAUCValues(instanceSet, testData,
				originalDist, numFolds, -1, -1, cross);
		
		/* Save results */
		getAUCResults(aucResults, results);
		saveROCResults(hullResults, results);

		/* Loop through all sample strategies */
		for (int sampleID = 0; sampleID < 7; sampleID++) {
			/* Change the distribution, run the models and evaluate */
			for (int i = 1; i <= 9; i++) {
				/* Get current class distribution  - Two class problem */
				float proportion = originalDist[negativeClass] * (float) i;
				proportion /= originalDist[positiveClass] * (float) (10 - i);
				
				if (proportion < 1) {
					continue;
				}
				
				if (!cross) {
					trainData = new InstanceSet(instanceSet);
					
					/* Sample training instanceSet */
					trainData = testsetUtils.sample(trainData, sampleID, i,
							originalDist);
					
					/* Check if the trainData has been sampled */
					if (trainData == null) {
						/* Skip */
						continue;
					}
					
					/* Distribution of the sampled training instanceSet */
					newDist = TestsetUtils.distribution(trainData);
				} else {
					trainData = instanceSet;
					newDist = originalDist;
				}
				
				/* Generate results */
				results = testsetUtils.generateAUCValues(trainData, testData,
						newDist, numFolds, sampleID, i, cross);

				if (results != null) {
					/* Save results */
					getAUCResults(aucResults, results);
					saveROCResults(hullResults, results);
				}
				
				trainData = null;
				System.gc();
			}
			trainData = null;
			System.gc();
		}
		saveAUCResults(aucResults);
		computeHullResults(hullResults);
	}

	private void getAUCResults(ArrayList<String[]> aucResults,
			ArrayList<Object> results) {
		String[][] newResults = (String[][]) results.get(0);
		int numResults = newResults.length;
		
		for (int i = 0; i < numResults; i++) {
			aucResults.add(newResults[i]);
		}
	}

	private void saveROCResults(ArrayList<float[]> hullResults,
			ArrayList<Object> results) throws IOException {
		PrintWriter writer;
		File output;
		String fileName;

		/* Get roc results */
		ArrayList<Object> rocResults = (ArrayList<Object>) results.get(1);
		int numROCResults = rocResults.size();
		
		/* Save roc results */
		float[][] rocPoints;
		int numROCPoints;
		for (int n = 0; n < numROCResults; n += 2) {
			/* Get roc values */
			rocPoints = (float[][]) rocResults.get(n + 1);
			fileName = (String) rocResults.get(n) + rocFileNameExtension;
			
			/* Create roc output file */
			output = new File(outputFilePath + fileName);
			writer = new PrintWriter(new FileWriter(output), true);
			
			/* Save roc values */
			numROCPoints = rocPoints.length;
			writer.println("FP\tTP");
			for (int i = 0; i < numROCPoints; i++) {
				writer.println(rocPoints[i][0] + "\t" + rocPoints[i][1]);
				hullResults.add(rocPoints[i]);
			}
			writer.flush();
			writer.close();
		}
	}
	
	private void saveAUCResults(ArrayList<String[]> aucResults)
	throws IOException {
		PrintWriter writer;
		File output;

		/* Create AUC output file */
		output = new File(outputFilePath + aucFileName);
		writer = new PrintWriter(new FileWriter(output), true);
		
		/* Save auc values on disk */
		int numAUCResults = aucResults.size();
		for (int n = 0; n < numAUCResults; n++) {
			writer.println(aucResults.get(n)[0] + "\t" + aucResults.get(n)[1]);
		}
		writer.flush();
		writer.close();
	}
	
	private void computeHullResults(ArrayList<float[]> hullPoints)
	throws IOException {
		/* Sort ascending by FP and TP */
		ROCAnalysis.sort(hullPoints);
		
		/* Remove repeated points */
		ROCAnalysis.removeRepeated(hullPoints);
		
		/* Create convex hull output file */
		PrintWriter writer1;
		File output1;
		output1 = new File(outputFilePath + "rocPoints.txt");
		writer1 = new PrintWriter(new FileWriter(output1), true);
		
		/* Save convex hull points on disk */
		writer1.println("FP\tTP");
		int numHullResults1 = hullPoints.size();
		for (int n = 0; n < numHullResults1; n++) {
			writer1.println(hullPoints.get(n)[0] + "\t" + hullPoints.get(n)[1]);
		}
		writer1.flush();
		writer1.close();

		/* Compute the convex hull */
		hullPoints = ROCAnalysis.computeConvexHull(hullPoints);
		
		/* Create convex hull output file */
		PrintWriter writer;
		File output;
		output = new File(outputFilePath + hullFileName);
		writer = new PrintWriter(new FileWriter(output), true);
		
		/* Save convex hull points on disk */
		writer.println("FP\tTP");
		int numHullResults = hullPoints.size();
		for (int n = 0; n < numHullResults; n++) {
			writer.println(hullPoints.get(n)[0] + "\t" + hullPoints.get(n)[1]);
		}
		writer.flush();
		writer.close();
	}
	
}