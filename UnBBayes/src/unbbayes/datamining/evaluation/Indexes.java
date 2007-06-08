package unbbayes.datamining.evaluation;

import unbbayes.datamining.datamanipulation.InstanceSet;

/**
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 08/06/2007
 */
public class Indexes {
	
	/**
	 * Array for storing the confusion matrix.
	 * 
	 * first: samplingID
	 * second: classifierID
	 * third: pos
	 * fourth: true class
	 * fifth: predicted class
	 */
	private int[][][][][] confusionMatrixCross;

	/**
	 * Array for storing the confusion matrix.
	 * 
	 * first: samplingID
	 * second: classifierID
	 * third: true class
	 * fourth: predicted class
	 */
	private int[][][][] confusionMatrix;

	/**
	 * first: samplingID second: classifierID third: meand and stdDev
	 */
	private double[][][] globalError;

	private double[][][] globalErrorTemp;

	/**
	 * first: samplingID second: classifierID third: meand and stdDev
	 */
	private double[][][] sensitivity;

	private double[][][] sensitivityTemp;

	/**
	 * first: samplingID second: classifierID third: meand and stdDev
	 */
	private double[][][] specificity;

	private double[][][] specificityTemp;

	/**
	 * first: samplingID second: classifierID third: meand and stdDev
	 */
	private double[][][] SE;

	private double[][][] SETemp;
	
	private String[] samplingName;

	private int numSamplings;

	private int numClassifiers;

	private int numRoundsTotal;

	private int numClasses;

	private Evaluation eval;

	public Indexes(int numSamplings, int numClassifiers, int numRoundsTotal)
	throws Exception {
		this.numSamplings = numSamplings;
		this.numClassifiers = numClassifiers;
		this.numRoundsTotal = numRoundsTotal;
		numClasses = instanceSet.numClasses();
		
		confusionMatrix = new int[numSamplings]
		                         [numClassifiers]
		                         [numClasses]
		                         [numClasses];
		
		confusionMatrixCross = new int[numSamplings]
				                      [numClassifiers]
				                      [numRoundsTotal]
				                      [numClasses]
				                      [numClasses];
		
		eval = new Evaluation(instanceSet);
	}

	public void insert(int samplingID, int classfID, int pos, int actualClass,
			int predictedClass, int weight) {
		confusionMatrix[samplingID]
			           [classfID]
			           [actualClass]
			           [predictedClass] += weight;
		
		confusionMatrixCross[samplingID]
				            [classfID]
				            [pos]
				            [actualClass]
				            [predictedClass] += weight;
	}

	public double truePositiveRate(int samplingID, int classfID, int classID) {
		eval.setConfusionMatrix(confusionMatrix[samplingID][classfID]);
		return eval.truePositiveRate(classID);
	}

	public double falsePositiveRate(int samplingID, int classfID, int classID) {
		eval.setConfusionMatrix(confusionMatrix[samplingID][classfID]);
		return eval.falsePositiveRate(classID);
	}

	public double trueNegativeRate(int samplingID, int classfID, int classID) {
		eval.setConfusionMatrix(confusionMatrix[samplingID][classfID]);
		return eval.trueNegativeRate(classID);
	}

	public double falseNegativeRate(int samplingID, int classfID, int classID) {
		eval.setConfusionMatrix(confusionMatrix[samplingID][classfID]);
		return eval.falseNegativeRate(classID);
	}

	public double getPrecision(int samplingID, int classfID, int classID) {
		eval.setConfusionMatrix(confusionMatrix[samplingID][classfID]);
		return eval.getPrecision(classID);
	}

	public double getRecall(int samplingID, int classfID, int classID) {
		eval.setConfusionMatrix(confusionMatrix[samplingID][classfID]);
		return eval.getRecall(classID);
	}

	public double getAccuracy(int samplingID, int classfID, int classID) {
		eval.setConfusionMatrix(confusionMatrix[samplingID][classfID]);
		return eval.getAccuracy(classID);
	}

	public double getFScore(int samplingID, int classfID, int classID) {
		eval.setConfusionMatrix(confusionMatrix[samplingID][classfID]);
		return eval.getFScore(classID);
	}

	public int[][] getConfusionMatrix(int samplingID, int classfID) {
		eval.setConfusionMatrix(confusionMatrix[samplingID][classfID]);
		return eval.getConfusionMatrix();
	}
	
//	public void buildIndexes() {
//		for (int pos = 0; pos < numRoundsTotal; pos++) {
//			globalError = 
//		}
//		int[] total = new int[numClasses];
//		Arrays.fill(total, 0);
//
//		total[actualClass] += weight;
//
//		int tp = confusionMatrix[positiveClass][positiveClass];
//		int fp = confusionMatrix[negativeClass][positiveClass];
//
//		/* Global error */
//		double globalError = confusionMatrix[0][1] + confusionMatrix[1][0];
//		globalError /= test.numWeightedInstances;
//
//		/* Sensitivity */
//		double sensitivity = (double) tp / total[positiveClass];
//
//		/* Specificity */
//		double specificity = 1 - ((double) fp / total[negativeClass]);
//
//		/* SE */
//		double SE = sensitivity * specificity;
//
//		if (SE < 50) {
//			@SuppressWarnings("unused")
//			boolean fudeu = true;
//		}
//		
//		globalErrorTemp[samplingID][classfID][pos] = globalError * 100;
//		sensitivityTemp[samplingID][classfID][pos] = sensitivity * 100;
//		specificityTemp[samplingID][classfID][pos] = specificity * 100;
//		SETemp[samplingID][classfID][pos] = SE * 100;
//		
//		globalError = new double[numSamplings][numClassifiers][];
//		sensitivity = new double[numSamplings][numClassifiers][];
//		specificity = new double[numSamplings][numClassifiers][];
//		SE = new double[numSamplings][numClassifiers][];
//		
//		for (int i = 0; i < numSamplings; i++) {
//			for (int j = 0; j < numClassifiers; j++) {
//				if (numRoundsTotal > 1) {
//					/* Average the 'numFolds' aucs */
//					globalError[i][j] = Utils
//							.computeMeanStdDev(globalErrorTemp[i][j]);
//					sensitivity[i][j] = Utils
//							.computeMeanStdDev(sensitivityTemp[i][j]);
//					specificity[i][j] = Utils
//							.computeMeanStdDev(specificityTemp[i][j]);
//					SE[i][j] = Utils.computeMeanStdDev(SETemp[i][j]);
//				} else {
//					globalError[i][j] = new double[2];
//					globalError[i][j][0] = globalErrorTemp[i][j][0];
//					globalError[i][j][1] = 0;
//
//					sensitivity[i][j] = new double[2];
//					sensitivity[i][j][0] = sensitivityTemp[i][j][0];
//					sensitivity[i][j][1] = 0;
//
//					specificity[i][j] = new double[2];
//					specificity[i][j][0] = specificityTemp[i][j][0];
//					specificity[i][j][1] = 0;
//
//					SE[i][j] = new double[2];
//					SE[i][j][0] = SETemp[i][j][0];
//					SE[i][j][1] = 0;
//				}
//			}
//		}
//	}
	
}