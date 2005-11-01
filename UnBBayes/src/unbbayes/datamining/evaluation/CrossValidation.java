package unbbayes.datamining.evaluation;

import java.util.Random;

import unbbayes.datamining.classifiers.Classifier;
import unbbayes.datamining.datamanipulation.InstanceSet;

public class CrossValidation implements ITrainingMode {
	
	protected int numFolds;
	
	public CrossValidation(int folds) {
		this.numFolds = folds;
	}
	
	public int getFolds() {
		return numFolds;
	}

	  /**
	   * Performs a (stratified if class is nominal) cross-validation
	   * for a classifier on a set of instances.
	   *
	   * @exception Exception if a classifier could not be generated
	   * successfully or the class is not defined
	   */
	  public void crossValidateModel(InstanceSet data,Classifier classifier) throws Exception
	  {   	  
		  data.randomize(new Random(42));
		  if (data.getClassAttribute().isNominal()) {   
			  data.stratify();
		  }
		  System.out.println(data);
		  // Do the folds
		//for (int i = 0; i < numFolds; i++) {	
			/*InstanceSet train = trainCV(data,numFolds, i);
			classifier.buildClassifier(train);
			InstanceSet test = testCV(data,numFolds, i);
			evaluateModel(classifier, test);*/
		//}
	  }

	  /**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
