package unbbayes;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import unbbayes.datamining.classifiers.Classifier;
import unbbayes.datamining.classifiers.CombinatorialNeuralModel;
import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.evaluation.Classifiers;
import unbbayes.datamining.evaluation.Evaluation;
import unbbayes.datamining.preprocessor.imbalanceddataset.Smote;

/**
 * Class for testing the formalism Cluster-Based SMOTE.
 * 
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 24/08/2006
 */
public class TestFaber {
	
	float maxSE = 0;
	String maxSEHeader;
	String header = "";
	Smote smote;
	private TestsetUtils testsetUtils;
	String outputFileName;
	private String outputFilePath;
	private int classIndex;
	private int counterIndex;
	private String inputFilePath;
	private String inputFileName;
	private String trainingFileName;
	private String testFileName;
	private Evaluation eval;
	private int numClasses;
	
	public TestFaber() {
		try {
			rodaTodos();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		new TestFaber();
	}

	private void rodaTodos() throws Exception {
		int x = 0;
		callSettings(x);
		run(x);
	}
	
	public void dataset(int x) {
		/* Data set characteristics */
		switch (x) {
			case 0:
				inputFilePath = "C:/fabricio/miner/base/NaiveBayes/";
				//trainingFileName = "treinoKdd17Full.arff";
				trainingFileName = "avaliacaoKdd17Full.arff";
				testFileName = "arquivoTeste17full.arff";
				//testFileName = "treinoKdd17Full.arff";
				classIndex = 17;
				counterIndex = 18;
				break;
				
			case 1:
				inputFilePath = "C:/Documents and Settings/alfredo/Desktop/workspace/UnBBayes/examples/";
				trainingFileName = "weather.arff";
				testFileName = "weather.arff";
				classIndex = 4;
				counterIndex = 5;
				break;
		}
	}
	
	public void run(int x) throws Exception {
		System.out.println(inputFileName);
		long start = System.currentTimeMillis();
		System.out.println(x + " - " + (new java.text.SimpleDateFormat("HH:mm:ss")).format(new Date()));

		run();

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
		 * 0: SPAM
		 */
		dataset(x);
		
		/* Add paths to the file names */
		outputFilePath = "";
		inputFileName = inputFilePath + inputFileName;
		trainingFileName = inputFilePath + trainingFileName;
		testFileName = inputFilePath + testFileName;
		outputFilePath = inputFilePath + outputFilePath;
	}
	
	private InstanceSet openFile(String fileName) throws IOException {
		InstanceSet instanceSet;
		instanceSet = TestsetUtils.openFile(fileName, counterIndex, classIndex);
		
		return instanceSet;
	}

	private void run() throws Exception {
		/* Opens the training set */
		InstanceSet trainingSet = openFile(trainingFileName);
		
		if (trainingSet == null) {
			String exceptionMsg = "Couldn't open training instanceSet " 
				+ trainingFileName;
			throw new Exception(exceptionMsg);
		}
		
		/* Opens the test set */
		InstanceSet test = openFile(testFileName);
		if (test == null) {
			String exceptionMsg = "Couldn't open training instanceSet " 
				+ testFileName;
			throw new Exception(exceptionMsg);
		}
		test.setClassIndex(classIndex);
		numClasses = test.numClasses();
		
		/* Get class distribution */
		float[] distribution = trainingSet.getClassDistribution(false);
		
		/* Build classifier */
		int classfID = 0;	// naive
//		int classfID = 1;	// c4.5
//		int classfID = 2;	// cnm
		Classifier classifier;
		classifier = Classifiers.newClassifier(classfID);
		Classifiers.buildClassifier(trainingSet, classifier, distribution, testsetUtils);

		/* If CombinatorialNeuralModel */
		int minSupport = 0;
		int minConfidence = 0; 
		if (classifier instanceof CombinatorialNeuralModel) {
			((CombinatorialNeuralModel) classifier).prunning(minSupport, minConfidence);
		}
		
		/* Evaluate the model */
		eval = new Evaluation(test, classifier);
		eval.evaluateModel(classifier, test);

		PrintWriter writer;
		File output;
		String fileName;

		/* Save tp and fp on disk */
		fileName = outputFilePath + "/TP-FP.txt";
		output = new File(fileName);
		writer = new PrintWriter(new FileWriter(output), true);
		writer.println("TP Rate\tFP Rate\tTN Rate\tFN Rate\tClass");
		for (int i = 0; i < numClasses; i++) {
			writer.print(eval.truePositiveRate(i) + "\t");
			writer.print(eval.falsePositiveRate(i) + "\t");
			writer.print(eval.trueNegativeRate(i) + "\t");
			writer.print(eval.falseNegativeRate(i) + "\t");
			writer.print(trainingSet.attributes[trainingSet.classIndex].value(i));
			writer.println();
		}
		writer.flush();
		writer.close();
		
		/* Save indexes on disk */
		fileName = outputFilePath + "/indices.txt";
		output = new File(fileName);
		writer = new PrintWriter(new FileWriter(output), true);
		writer.println("Precision\tRecall\tAccuracy\tFScore\tClass");
		for (int i = 0; i < numClasses; i++) {
			writer.print(eval.getPrecision(i) + "\t");
			writer.print(eval.getRecall(i) + "\t");
			writer.print(eval.getAccuracy(i) + "\t");
			writer.print(eval.getFScore(i) + "\t");
			writer.print(trainingSet.attributes[trainingSet.classIndex].value(i));
			writer.println();
		}
		writer.flush();
		writer.close();
		
		/* Save confusion matrix on disk */
		fileName = outputFilePath + "/confusionMatrix.txt";
		output = new File(fileName);
		writer = new PrintWriter(new FileWriter(output), true);
		for (int i = 0; i < numClasses; i++) {
			if (i > 0) writer.print("\t");
			writer.print(trainingSet.attributes[trainingSet.classIndex].value(i));
		}
		writer.println();
		int[][] confusionMatrix = eval.getConfusionMatrix();
		for (int i = 0; i < numClasses; i++) {
			for (int j = 0; j < numClasses; j++) {
				writer.print(confusionMatrix[i][j] + "\t");
			}
			writer.print(trainingSet.attributes[trainingSet.classIndex].value(i));
			writer.println();
		}
		writer.flush();
		writer.close();
	}
	
}