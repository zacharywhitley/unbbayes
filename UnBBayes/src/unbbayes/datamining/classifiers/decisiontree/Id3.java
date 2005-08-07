package unbbayes.datamining.classifiers.decisiontree;

import java.io.*;
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
 * @author M�rio Henrique Paes Vieira (mariohpv@bol.com.br)
 * @author Danilo Balby Silva Castanheira (danbalby@yahoo.com)
 * @version $1.0 $ (24/12/2001)
 */
public class Id3 extends DecisionTreeLearning implements Serializable{

	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;
	/** Load resources file for internacionalization */
	private transient ResourceBundle resource;

	/** The root of the tree generated by the constructor */
        private Node xRootNode;

        //---------------------------------------------------------------------//

        /**
        * Generates the decision tree relative to an instance set
        *
        * @param data instance set used for the decision tree generation
        */
	public void buildClassifier(InstanceSet data) throws Exception
	{
		//internacionalization
		resource = ResourceBundle.getBundle("unbbayes.datamining.classifiers.resources.ClassifiersResource");

		// Test if the class is nominal
		if (!data.getClassAttribute().isNominal())
		{
			throw new Exception(resource.getString("exception1"));
		}

		// Test if there are numeric attributes
		Enumeration enumAtt = data.enumerateAttributes();
		while (enumAtt.hasMoreElements())
		{
			if (data.getAttribute(((Attribute)enumAtt.nextElement()).getIndex()).isNumeric())
			{
				throw new Exception(resource.getString("exception1"));
			}
		}

		// Test if there are missing values
		Enumeration enumeration = data.enumerateInstances();
		while (enumeration.hasMoreElements())
		{
			Instance instance = (Instance)enumeration.nextElement();
			enumAtt = data.enumerateAttributes();
			while (enumAtt.hasMoreElements())
			{
				if (instance.isMissing((Attribute)enumAtt.nextElement()))
				{
					throw new Exception(resource.getString("exception3"));
				}
			}
		}

		//tree generation
		makeTree(data);
	}

        //--------------------------------------------------------------------//

        /**
         *  Internal function that generates the decision tree
         *
         * 	@param data instance set used for the decision tree generation
         */
	protected void makeTree(InstanceSet data) throws Exception
	{
		int numInstances = data.numInstances();
		int numAttributes = data.numAttributes();
		int numClasses = data.numClasses();
		Attribute classAttribute = data.getClassAttribute();
		int classIndex = classAttribute.getIndex();

		//Contains methods to compute information gain and related actions
		ClassifierUtils utils = new ClassifierUtils(data);
		//Queue to use this function recursively,keeping stack components (see below)
		ArrayList queue = new ArrayList();
		//list containing indexes of all the current instances
		ArrayList actualInst = new ArrayList(numInstances);
		//array containing indexes of attributes used currently
		Integer[] actualAtt = new Integer[numAttributes];

		//starts lists of indexes with indexes of entire instanceSet
		for (int i=0;i<numInstances;i++)
		{
			actualInst.add(new Integer(i));
		}
		for (int i=0;i<numAttributes;i++)
		{
			actualAtt[i] = new Integer(i);
		}

		//object that contains lists of indexes
		SplitObject split = new SplitObject(actualInst,actualAtt);

		xRootNode = new Node(null);
		Node xNode = xRootNode;

		//start queue with the initial data
		queue.add(new QueueComponent(xNode,split));

		QueueComponent queueComponent;
		Leaf leaf;
		double[] infoGains;
		int attributeIndex;
		float[] distribution;
		Instance inst;
		SplitObject[] splitData;
		Attribute splitAttribute;

		double[] splitValues;
		ArrayList numericDataList = new ArrayList();

		//start recursive code
		while ((!queue.isEmpty()))
		{
			//gets the objects relative to the iteration
			queueComponent = (QueueComponent)queue.remove(0);

			xNode = queueComponent.getNodeParent();
			split = queueComponent.splitObject;
			actualInst = split.getInstances();
			numInstances = actualInst.size();
			actualAtt = split.getAttributes();
			numAttributes = actualAtt.length;

			//if no instance has reached this node - is leaf
			if (numInstances == 0)
			{
				leaf = new Leaf();
				xNode.add(leaf);
			}
			else
			{
				// compute array with the gain of each attribute.
				// splitValues and numericDataList are initialized here
				splitValues = new double[actualAtt.length];
				infoGains = utils.computeInfoGain(split, splitValues, numericDataList);

				//gets attribute with maximum gain
				attributeIndex = Utils.maxIndex(infoGains);

				//attributeIndex is the infoGains index for the split attribute
				//realIndex is the actualApp index for the same attribute
				int realAttribute = -1;
				int realIndex;
				for (realIndex=0;realIndex<numAttributes;realIndex++)
				{
					if (actualAtt[realIndex].intValue()!=classIndex)
					{
						realAttribute++;
						if (realAttribute==attributeIndex)
						{
							break;
						}
					}
				}

				//make leaf if information gain is zero....
				if (Utils.eq(infoGains[attributeIndex], 0))
				{
					//computes the number of instances for each class
					distribution = new float[numClasses];
					for (int i=0;i<numInstances;i++)
					{
						inst = data.getInstance(((Integer)actualInst.get(i)).intValue());
						distribution[(int) inst.classValue()] += inst.getWeight();
					}

					leaf = new Leaf(classAttribute,distribution);
					xNode.add(leaf);
				}

				//...Otherwise create successors.
				else
				{
					//puts children on queue
					splitAttribute = data.getAttribute(actualAtt[realIndex].intValue());

					//if split attribute is nominal...
					splitData = utils.splitData(split, realIndex);
					for (int j=0;j<splitData.length;j++)
					{
                                                NominalNode nominalNode = new NominalNode(splitAttribute,j);
						xNode.add(nominalNode);
						queue.add(new QueueComponent(nominalNode,splitData[j]));
					}
				}
			}
		}
	}

        //--------------------------------------------------------------------//

          /**
           * Returns the decision tree created previously on JTree format
           *
           *  @return decision tree on a JTree format
         */
	public JTree getTree()
	{
          Leaf leaf;
          Node node;
          Object obj;
          Stack stackObj = new Stack();
          Stack stackTree = new Stack();
          DefaultMutableTreeNode text2 = new DefaultMutableTreeNode("root");
          DefaultMutableTreeNode treeNode = text2;


          ArrayList root = xRootNode.children;
          int size = root.size();
          for (int i=0;i<size;i++)
          {
            stackObj.push(root.get(i));
            stackTree.push(treeNode);
          }

          while (!stackObj.empty())
          {
            obj = stackObj.pop();
            treeNode = (DefaultMutableTreeNode)stackTree.pop();

            if(obj instanceof Leaf)
            {
              leaf = (Leaf)obj;
              treeNode.add(new DefaultMutableTreeNode(leaf.toString()));
            }
            else
            {
              node = (Node)obj;
              DefaultMutableTreeNode treeNodeChild = new DefaultMutableTreeNode(node.toString());
              treeNode.add(treeNodeChild);
              treeNode = treeNodeChild;

              ArrayList children = node.children;
              size = children.size();
              for (int i=0;i<size;i++)
              {
                stackObj.push(children.get(i));
                stackTree.push(treeNode);
              }
            }
          }
          return new JTree(text2);
	}

        //--------------------------------------------------------------------//

        /**
         * Returns a string describing the decision tree created
         *
         * 	@return string representation of a decision tree
         */
	public String toString()
	{
          Leaf leaf;
          Node node;
          Object obj;
          Integer level;
          Stack stackObj = new Stack();
          Stack stackLevel = new Stack();
          StringBuffer text = new StringBuffer();

          ArrayList root = xRootNode.children;
          int size = root.size();
          for (int i=0;i<size;i++)
          {
            stackObj.push(root.get(i));
            stackLevel.push(new Integer(1));
          }

          while (!stackObj.empty())
          {
            obj = stackObj.pop();
            level = (Integer)stackLevel.pop();

            if(obj instanceof Leaf)
            {
              leaf = (Leaf)obj;
              text.append(": "+leaf.toString());
            }
            else
            {
              node = (Node)obj;

              if(level.intValue()!=0)
              {
                text.append("\n");
                for (int i=0;i<level.intValue()-1;i++)
                {
                  text.append("| ");
                }
                text.append(node.toString());
              }

              ArrayList children = node.children;
              size = children.size();
              for (int i=0;i<size;i++)
              {
                stackObj.push(children.get(i));
                stackLevel.push(new Integer(level.intValue()+1));
              }
            }
          }
          return  text.toString();
        }

	//-----------------------------------------------------------------------//

	/**
	 * Classifies a given test instance using the decision tree.
	 *
	 * @param instance the instance to be classified
	 * @return the classification
	 */
	public int classifyInstance(Instance instance)
	{
          Leaf leaf;
          NominalNode node;
          Node treeNode = xRootNode;

          while (!(treeNode.children.get(0) instanceof Leaf))
          {
            node = (NominalNode)treeNode.children.get(0);
            // Atributo nominal
            treeNode = (NominalNode)treeNode.children.get(instance.getValue(node.getAttribute()));
          }

          leaf = (Leaf)treeNode.children.get(0);
          return leaf.getClassValue();
        }

	/*************************************************************************/

        /**
         * Component used to store temporary data on an queue used to build the decision tree
         * on the makeTree procedure
         *
         * @author Danilo Balby Silva Castanheira (danbalby@yahoo.com)
         */
	private class QueueComponent
	{
          /** data about an instance set */
          private SplitObject splitObject;
          /** tree node relative to the splitObject */
          private Node nodeParent;

          //--------------------------CONSTRUCTORS--------------------------//

          /**
           * Default constructor
           *
           * @param nodeParent node relative to the splitObject
           * @param splitObject data about an instance set
           */
          public QueueComponent(Node newParent, SplitObject splitObject)
          {
            nodeParent = newParent;
            this.splitObject = splitObject;
          }

          //-----------------------------GETS------------------------------//

          /**
           * Returns the node set to the component
           *
           * @return node set to the component
           */
          public Node getNodeParent()
          {
            return nodeParent;
          }

          /**
          * Returns the intance set data set to the component
          *
          * @return instance set data set to the component
          */
         public SplitObject getSplitObject()
         {
           return splitObject;
         }
       }
}