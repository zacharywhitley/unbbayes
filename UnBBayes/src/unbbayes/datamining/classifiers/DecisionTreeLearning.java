package unbbayes.datamining.classifiers;

import javax.swing.*;

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

}

