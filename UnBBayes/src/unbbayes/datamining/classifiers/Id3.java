package unbbayes.datamining.classifiers;

import java.io.*; //Used StringBuffer in method toString(int level)
import java.util.*;

import javax.swing.*;
import javax.swing.tree.*;

import unbbayes.datamining.datamanipulation.*;

/**
 * Class implementing an Id3 decision tree classifier. For more
 * information, see<p>
 *
 * R. Quinlan (1986). <i>Induction of decision
 * trees</i>. Machine Learning. Vol.1, No.1, pp. 81-106.<p>
 *
 * @author Mário Henrique Paes Vieira (mariohpv@bol.com.br)
 * @version $1.0 $ (24/12/2001)
 */
public class Id3 extends DecisionTreeLearning implements Serializable
{	/** Load resources file for internacionalization */
	private ResourceBundle resource;

	/** The node's successors. */
  	private Id3[] successors;

  	/** Attribute used for splitting. */
  	private Attribute splitAttribute;

  	/** Class value if node is leaf. */
  	private short classValue;

  	/** Class distribution if node is leaf. */
  	private double[] distribution;

  	/** Class attribute of dataset. */
  	private Attribute classAttribute;

	/** This variable counts the classified instances */
	private int numeroInstClass;

	/**
   	* Builds Id3 decision tree classifier.
	*
   	* @param data The training data
   	* @exception Exception if classifier can't be built successfully
   	*/
  	public void buildClassifier(InstanceSet data) throws Exception
	{	resource = ResourceBundle.getBundle("unbbayes.datamining.classifiers.resources.ClassifiersResource");
		if (!data.getClassAttribute().isNominal())
		{	throw new Exception(resource.getString("exception1"));
    	}
    	Enumeration enumAtt = data.enumerateAttributes();
    	while (enumAtt.hasMoreElements())
		{	Attribute attr = (Attribute) enumAtt.nextElement();
      		if (!attr.isNominal())
			{	throw new Exception(resource.getString("exception2"));
      		}
      		Enumeration enum = data.enumerateInstances();
      		while (enum.hasMoreElements())
			{	if (((Instance) enum.nextElement()).isMissing(attr))
				{	throw new Exception(resource.getString("exception3"));
				}
      		}
    	}
    	data = new InstanceSet(data);
    	data.deleteWithMissingClass();
    	makeTree(data);
  	}

	/**
   	* Method building Id3 tree.
   	*
   	* @param data The training data
   	* @exception Exception if decision tree can't be built successfully
   	*/
  	protected void makeTree(InstanceSet data) throws Exception
	{	// Check if no instances have reached this node.
    	if (data.numInstances() == 0)
		{	splitAttribute = null;
      		classValue = Instance.missingValue();
      		distribution = new double[data.numClasses()];
      		return;
    	}

    	// Compute attribute with maximum information gain.
    	double[] infoGains = new double[data.numAttributes()];
    	Enumeration attEnum = data.enumerateAttributes();
    	while (attEnum.hasMoreElements())
		{	Attribute att = (Attribute) attEnum.nextElement();
      		infoGains[att.getIndex()] = Utils.computeInfoGain(data, att);
		}

		// Compute the information gain mean
		double meanInfoGains = Utils.sum(infoGains)/(double)(infoGains.length);

		for (int i=0; i<data.numAttributes(); i++)
			if (infoGains[i] > meanInfoGains)
			{	Attribute att = (Attribute) data.getAttribute(i);
				infoGains[att.getIndex()] = Utils.computeGainRatio(data, att);
			}

		splitAttribute = data.getAttribute(Utils.maxIndex(infoGains));

    	// Make leaf if information gain is zero.
    	// Otherwise create successors.
    	if (Utils.eq(infoGains[splitAttribute.getIndex()], 0))
		{	splitAttribute = null;
      		distribution = new double[data.numClasses()];
      		Enumeration instEnum = data.enumerateInstances();
      		while (instEnum.hasMoreElements())
			{	Instance inst = (Instance) instEnum.nextElement();
				distribution[(int) inst.classValue()] += inst.getWeight();
      		}
      		int m = Utils.maxIndex(distribution);
			numeroInstClass=(int)distribution[m];
			Utils.normalize(distribution);
      		classValue = (short)Utils.maxIndex(distribution);
      		classAttribute = data.getClassAttribute();
    	}
		else
		{	InstanceSet[] splitData = Utils.splitData(data, splitAttribute);
      		successors = new Id3[splitAttribute.numValues()];
      		for (int j = 0; j < splitAttribute.numValues(); j++)
			{	successors[j] = new Id3();
				successors[j].buildClassifier(splitData[j]);
      		}
    	}
  }

  /**
   * Classifies a given test instance using the decision tree.
   *
   * @param instance the instance to be classified
   * @return the classification
   */
  public short classifyInstance(Instance instance)
  {	if (splitAttribute == null)
  	{	return classValue;
    }
	else
	{	return successors[(int) instance.getValue(splitAttribute)].
	  	classifyInstance(instance);
    }
  }

  /**
   * Prints the decision tree using the private toString method from below.
   *
   * @return a textual description of the classifier
   */
  public String toString()
  {	if ((distribution == null) && (successors == null))
  	{	return resource.getString("toStringException1");
    }
    return resource.getString("toStringException2") + toString(0);
  }

  /**
   * Outputs a tree at a certain level.
   *
   * @param level Level at which the tree is to be printed
   */
  private String toString(int level)
  {	StringBuffer text = new StringBuffer();

    if (splitAttribute == null)
	{	if (Instance.isMissingValue(classValue))
		{	text.append(": "+resource.getString("null"));
      	}
		else
		{	text.append(": "+classAttribute.getAttributeName()+" = "+classAttribute.value((int) classValue)+" ( "+numeroInstClass+" ) ");
      	}
    }
	else
	{	for (int j = 0; j < splitAttribute.numValues(); j++)
		{	text.append("\n");
        	for (int i = 0; i < level; i++)
			{	text.append("|  ");
			}
        	text.append(splitAttribute.getAttributeName() + " = " + splitAttribute.value(j));
        	text.append(successors[j].toString(level + 1));
      	}
    }
    return text.toString();
  }

  /**
   * Get the tree build by id3 classifier using the private getTree method from below.
   *
   * @return a JTree object build by id3
   */
  public JTree getTree()
  {	DefaultMutableTreeNode root;
  	if (splitAttribute == null)
  	{	root = new DefaultMutableTreeNode(resource.getString("NULL"));
  	}
	else
	{	root = new DefaultMutableTreeNode(splitAttribute.getAttributeName());
		getTree(0,root);
	}
	JTree tree = new JTree(root);
	return tree;
  }

  /**
   * Outputs a tree at a certain level.
   *
   * @param level Level at which the tree is to be build
     @param node Actual node that will be build
   */
  private void getTree(int level,DefaultMutableTreeNode node)
  {	if (splitAttribute == null)
	{	if (Instance.isMissingValue(classValue))
		{	DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(resource.getString("NULL"));
			node.add(newNode);
      	}
		else
		{	DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(classAttribute.getAttributeName()+" = "+classAttribute.value((int) classValue));
			node.add(newNode);
      	}
    }
	else
	{	for (int j = 0; j < splitAttribute.numValues(); j++)
		{	DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(splitAttribute.getAttributeName() + " = " + splitAttribute.value(j));
        	node.add(newNode);
			successors[j].getTree(level + 1,newNode);
      	}
    }
  }

}