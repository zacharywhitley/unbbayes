package unbbayes.datamining.evaluation;

import unbbayes.TestsetUtils;
import unbbayes.datamining.classifiers.Classifier;
import unbbayes.datamining.classifiers.DistributionClassifier;
import unbbayes.datamining.classifiers.NaiveBayes;
import unbbayes.datamining.classifiers.NaiveScaleBayes;
import unbbayes.datamining.classifiers.decisiontree.C45;
import unbbayes.datamining.classifiers.decisiontree.DecisionTreeLearning;
import unbbayes.datamining.datamanipulation.InstanceSet;

/**
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 16/02/2007
 */
public class Classifiers {

	private static String[] classifierNames = new String[20];
	
	private static int numClassifiers = 1;
	
	public static Classifier newClassifier(int classifierID)
	throws Exception {
		Classifier classifier = null;

	    switch (classifierID) {
			case 1:
				classifier = new NaiveBayes();
				classifierNames[classifierID] = "naive";
				break;
				
			case 0:
				classifier = new C45();
				classifierNames[classifierID] = "c45";
				break;
				
//			case 1:
//				classifier = new NaiveScaleBayes();
//				classifierNames[classifierID] = "naiveScale";
//				break;
		}

		return classifier;
	}

	public static void buildClassifier(InstanceSet train,
			Classifier classifier, float[] distribution,
			TestsetUtils testsetUtils)
	throws Exception {
		int positiveClass = testsetUtils.getPositiveClass();
		if (classifier instanceof DecisionTreeLearning) {
			((DecisionTreeLearning) classifier).setPositiveClass(positiveClass);
		} else if (classifier instanceof DistributionClassifier) {
			((DistributionClassifier) classifier).setOriginalDistribution(
			distribution);
			if (classifier instanceof NaiveScaleBayes) {
				((NaiveScaleBayes) classifier).setTestsetUtils(testsetUtils);
			}
		}
		classifier.buildClassifier(train);
	}

	public static String getClassifierName(int classifierID) {
		return classifierNames[classifierID];
	}
	
	public static int getNumClassifiers() {
		return numClassifiers;
	}
	
}

