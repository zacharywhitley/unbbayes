package unbbayes.datamining.classifiers.decisiontree;

import java.util.ArrayList;

import javax.swing.*;

import unbbayes.datamining.classifiers.Classifier;
import unbbayes.datamining.datamanipulation.*;

/**
 *  Abstract Decision Tree. All schemes that works with decision trees extends this class.
 *
 *  @author Mário Henrique Paes Vieira (mariohpv@bol.com.br)
 *  @version $1.0 $ (24/12/2001)
 */
public abstract class DecisionTreeLearning extends Classifier
{	/**
   	* Method that build a tree
   	*
   	* @param data The training set
   	* @exception Exception If the can't be build sucessfully.
   	*/
  	protected abstract void makeTree(InstanceSet data) throws Exception;

  	public abstract JTree getTree();

	public abstract String toString();

  	/**
  	 * Return the probability of the input instance being of the positive
  	 * class.
  	 * 
  	 * @param instance
  	 * @param positiveClass
  	 * @return
  	 */
	public abstract float positiveClassProb(Instance instance, int positiveClass,
			int numClasses);

  	/**
  	 * Descend the tree updating the input parameters with information for
  	 * building a ROC curve.
  	 * 
  	 * @param positiveClass
  	 */
	public abstract void descendTree(Node treeNode, int positiveClass,
			int[] count, ArrayList<float[]> positivePoints,
			ArrayList<float[]> negativePoints,
			ArrayList<float[]> probs);
}

