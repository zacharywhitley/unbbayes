package unbbayes.datamining.classifiers;

import unbbayes.datamining.datamanipulation.*;
import unbbayes.datamining.evaluation.*;

/**
 *  Abstract classifier. All schemes for numeric or nominal prediction extends this class.
 *
 *  @author Mário Henrique Paes Vieira (mariohpv@bol.com.br)
 *  @version $1.0 $ (17/02/2002)
 */
public abstract class Classifier
{	/**
   	* Build a classifier.
   	*
   	* @param data The training set
	  @exception Exception If classifier can't be build sucessfully.
   	*/
  	public abstract void buildClassifier(InstanceSet data) throws Exception;

  	public void buildClassifier(InstanceSet data,ITrainingMode mode) throws Exception {
  		if (mode == null || mode instanceof TrainingSet) {
  			this.buildClassifier(data);
  		} else if (mode instanceof CrossValidation) {
  			CrossValidation xval = (CrossValidation)mode;
  			xval.crossValidateModel(data,this);
  		} else {
  			assert false : "Training Mode not used"; 
  		}
  	}

  	/**
  	* Classifies a given test instance.
  	*
  	* @param instance the instance to be classified
  	* @return the classification
	* @exception Exception if an error occurred during the prediction
  	*/
  	public abstract int classifyInstance(Instance instance) throws Exception;
}