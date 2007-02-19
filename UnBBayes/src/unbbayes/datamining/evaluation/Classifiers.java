package unbbayes.datamining.evaluation;

import unbbayes.datamining.classifiers.Classifier;
import unbbayes.datamining.classifiers.DistributionClassifier;
import unbbayes.datamining.classifiers.NaiveBayes;
import unbbayes.datamining.classifiers.decisiontree.C45;
import unbbayes.datamining.classifiers.decisiontree.DecisionTreeLearning;
import unbbayes.datamining.datamanipulation.InstanceSet;

/**
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 16/02/2007
 */
public class Classifiers {

	private static String[] classifierNames = {
		"naive"
		,"c45"
//		,"ann"
		};
	
	private static int numClassifiers = 2;
	
	public static Classifier buildClassifier(InstanceSet train, int classifierID,
			float[] distribution, int positiveClass, float learningRate,
			float momentum, float threshold)
	throws Exception {
		Classifier classifier = null;
		
	    switch (classifierID) {
			case 0:
				classifier = new NaiveBayes();
				((DistributionClassifier)classifier).setRelativeClassification();
				((DistributionClassifier) classifier).setOriginalDistribution(
						distribution);
				break;
				
			case 1:
				classifier = new C45();
				((DecisionTreeLearning) classifier).setPositiveClass(positiveClass);
				((DecisionTreeLearning) classifier).setThreshold(threshold);
				break;
				
//			case 3:
//				classifier = new NeuralNetwork(learningRate, false, momentum,
//						-1, 0, 100, 1, 1, -2);
		}
		classifier.buildClassifier(train);
		
		return classifier;
	}

	public static String getClassifierName(int classifierID) {
		return classifierNames[classifierID];
	}
	
	public static int getNumClassifiers() {
		return numClassifiers;
	}
	
}

