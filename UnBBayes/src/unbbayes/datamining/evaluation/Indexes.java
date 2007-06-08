package unbbayes.datamining.evaluation;

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
	private int[][][][][] confusionMatrix;

	public Indexes(int numSamplings, int numClassifiers, int numRoundsTotal,
			int numClasses) {
		confusionMatrix = new int[numSamplings]
		                         [numClassifiers]
		                         [numRoundsTotal]
		                         [numClasses]
		                         [numClasses];
		
	}

	public void insert(int samplingID, int classfID, int pos, int actualClass,
			int predictedClass, int weight) {
		confusionMatrix[samplingID]
			               [classfID]
			               [pos]
			               [actualClass]
			               [predictedClass] += weight;
	}
}