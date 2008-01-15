package unbbayes.datamining.classifiers;

import java.util.ArrayList;

import unbbayes.datamining.classifiers.decisiontree.Node;
import unbbayes.datamining.datamanipulation.Instance;
import unbbayes.datamining.datamanipulation.InstanceSet;

/**
 *  Abstract classifier. All schemes for numeric or nominal prediction extends this class.
 *
 *  @author Mário Henrique Paes Vieira (mariohpv@bol.com.br)
 *  @version $1.0 $ (17/02/2002)
 */
public abstract class Classifier {
	/**
	 * Build a classifier.
	 *
	 * @param data The training set
	 * @exception Exception If classifier can't be build sucessfully.
	 */
  	public abstract void buildClassifier(InstanceSet data) throws Exception;

  	/**
  	 * Classifies a given test instance.
  	 *
  	 * @param instance the instance to be classified
  	 * @return the classification
	 * @exception Exception if an error occurred during the prediction
  	 */
  	public abstract int classifyInstance(Instance instance) throws Exception;

	public abstract float[] distributionForInstance(Instance instance)
	throws Exception;
  	
}