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
public abstract class DecisionTreeLearning extends Classifier {

	/**
	 * Factor used in the computation of the class of a leaf. If the leaf's
	 * number of positive instances divided by the number of negative instances
	 * is greater than the 'balanceFactor' the leaf's class is positive.
	 * Otherwise, the leaf's class is negative.
	 */
	protected float threshold = 0;
	
	/* Two class problem */
	protected int positiveClass = 1;

	/**
   	* Method that build a tree
   	*
   	* @param data The training set
 * @exception Exception If the can't be build sucessfully.
   	*/
  	protected abstract void makeTree(InstanceSet data)
  	throws Exception;

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
	public float positiveClassProb(Instance instance, int positiveClass) {
		Leaf leaf = classifyInstanceAux(instance);
		
		float[] dist = leaf.getDistribution();
		
		int negativeClass = Math.abs(positiveClass - 1);
		
		/* With Laplace estimate */
		double prob = dist[positiveClass] + 1;
		prob /= (dist[positiveClass] + dist[negativeClass] + 2);
		
		return (float) prob;
	}
	
	/**
	 * Classifies a given test instance using the decision tree. It will also
	 * update the macthed and errors counters of the leaf corresponding to the
	 * input instance. These counters will be used to build the points of a ROC
	 * curve of this classifier.
	 *
	 * @param instance the instance to be classified
	 * @return the classification
	 */
	public int classifyInstance(Instance instance) {
		return classifyInstanceAux(instance).getClassValue();
	}
	
	protected abstract Leaf classifyInstanceAux(Instance instance);

	/**
  	 * Descend the tree updating the input parameters with information for
  	 * building a ROC curve.
  	 */
	public void descendTree(Node treeNode, int[] count, ArrayList<float[]> positivePoints,
			ArrayList<float[]> negativePoints,
			ArrayList<float[]> probs) {
		int tp = 0;
		int fp = 0;
		
		if (treeNode.children.get(0) instanceof Leaf) {
			/* The node is a LEAF */
			Leaf leaf;
			int matched;
			int errors;
			int leafClass;
			float[] point;
			
			leaf = (Leaf) treeNode.children.get(0);
			matched = leaf.getMatched();
			errors = leaf.getErrors();
			tp = matched - errors;
			fp = errors;
			leafClass = leaf.getClassValue();
			count[leafClass] += tp;
			point = new float[2];
			
			if (leafClass == positiveClass) {
				point[0] = fp;
				point[1] = tp;
				positivePoints.add(point);
			} else {
				point[0] = tp;
				point[1] = fp;
				negativePoints.add(point);
			}
			
			/* Compute prob of being positive example */
			float[] prob = new float[2];
			prob[0] = (float) matched / (matched + errors);
			prob[1] = leafClass;
		} else {
			/* The node is internal node */
			int numChildren = treeNode.children.size();
			for (int i = 0; i < numChildren; i++) {
				descendTree((Node) treeNode.children.get(i), count,
						positivePoints, negativePoints, probs);
			}
		}
	}


	public void setThreshold(float threshold) {
		this.threshold = threshold;
	}

	public void setPositiveClass(int positiveClass) {
		this.positiveClass = positiveClass;
	}

}