/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package unbbayes.datamining.classifiers.decisiontree;

import java.util.ArrayList;

import javax.swing.JTree;

import unbbayes.datamining.classifiers.Classifier;
import unbbayes.datamining.datamanipulation.Instance;
import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.datamanipulation.Utils;

/**
 *  Abstract Decision Tree. All schemes that works with decision trees extends this class.
 *
 *  @author M�rio Henrique Paes Vieira (mariohpv@bol.com.br)
 *  @version $1.0 $ (24/12/2001)
 */
public abstract class DecisionTreeLearning extends Classifier {

	/**
	 * Factor used in the computation of the class of a leaf. If the leaf's
	 * number of positive instances divided by the number of negative instances
	 * is greater than the 'threshold' the leaf's class is positive.
	 * Otherwise, the leaf's class is negative.
	 */
	protected float threshold = -1;
	
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
	public float[] distributionForInstance(Instance instance) {
		Leaf leaf = classifyInstanceAux(instance);
		
		float[] dist = leaf.getDistribution();
		
		double aux;
		int numClasses = dist.length;
		float[] probs = new float[numClasses];
		float sum = Utils.sum(dist);
		for (int i = 0; i < numClasses; i++) {
			/* With Laplace estimate */
			aux = dist[i] + 1;
			aux /= (sum + numClasses);
			probs[i] = (float) aux;
		}
		
		return probs;
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