package unbbayes.datamining.classifiers.decisiontree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Stack;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import unbbayes.datamining.datamanipulation.Attribute;
import unbbayes.datamining.datamanipulation.ClassifierUtils;
import unbbayes.datamining.datamanipulation.Instance;
import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.datamanipulation.NumericData;
import unbbayes.datamining.datamanipulation.Options;
import unbbayes.datamining.datamanipulation.PrunningUtils;
import unbbayes.datamining.datamanipulation.Utils;

/**
 * Class implementing an C4.5 decision tree classifier. For more
 * information, see<p>
 *
 * R. Quinlan (1993). <i>C4.5: Programs for Machine Learning </i><p>
 *
 * @author M�rio Henrique Paes Vieira (mariohpv@bol.com.br)
 * @author Danilo Balby Silva Castanheira (danbalby@yahoo.com)
 * @version $1.0 $ (11/07/2003)
 */
public class C45 extends DecisionTreeLearning implements Serializable
{
	/** Load resources file for internacionalization */
	private transient ResourceBundle resource;

	/** The root of the tree generated by the constructor */
	private Node xRootNode;

	/** The root of the tree generated without prunning and containing construction data
	 * (same of xRootNode if there wasn't prune) */
	private Node infoRootNode;

	//-----------------------------------------------------------------------//

	/**
	* Generates the decision tree relative to an instance set
	*
	* @param data instance set used for the decision tree generation
	*/
	public void buildClassifier(InstanceSet data) throws Exception
	{
		//internacionalization
		resource = ResourceBundle.getBundle("unbbayes.datamining.classifiers.resources.ClassifiersResource");

		//preliminary tests
		if (!data.getClassAttribute().isNominal())
		{
			throw new Exception(resource.getString("exception1"));
		}

		//tree generation
		makeTree(data);
	}

	//-----------------------------------------------------------------------//

	/**
	 *	Internal function that generates the decision tree
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
		ArrayList<QueueComponent> queue = new ArrayList<QueueComponent>();
		//list containing indexes of all the current instances
		ArrayList<Integer> actualInst = new ArrayList<Integer>(numInstances);
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
		// N� ativo da �rvore
		Node xNode = xRootNode;


		//start stack with the initial data
		//queue.add(new QueueComponent(treeNode,split));
		queue.add(new QueueComponent(xNode,split));

		QueueComponent queueComponent;
		Leaf leaf;
		double[] infoGains;
		int attributeIndex;
		float[] distribution;
		Instance inst;
		SplitObject[] splitData;
		Node node;
		Attribute att;
		double meanInfoGains;
		Attribute splitAttribute;
		double splitValue;

		double[] splitValues;
		ArrayList<NumericData> numericDataList;

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
				numericDataList = new ArrayList<NumericData>();
				infoGains = utils.computeInfoGain(split, splitValues, numericDataList);

				//applies gain ratio if user chooses it
				if(Options.getInstance().getIfUsingGainRatio())
				{
					//applies gain ratio to attributes with gains greater than mean
					meanInfoGains = Utils.sum(infoGains)/(double)(infoGains.length);
					for (int i=0,decr=0; i<numAttributes;i++)
					{
						if(actualAtt[i].intValue()==classIndex)
						{
							decr++;		//from now on, infoGains[i] corresponds to actualAtt[1+i]
						}
						else if (infoGains[i-decr] > meanInfoGains)
						{
							att = (Attribute) data.getAttribute(actualAtt[i].intValue());
							if(att.isNominal())
							{
								infoGains[i-decr] /= utils.computeSplitInformation(split,i);
							}
						}
					}
				}

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

				//add data relative to infogain calculus into the actual node
				xNode.setInstrumentationData(split,infoGains,numericDataList);

				//computes the number of instances for each class
				distribution = new float[numClasses];
				int distributionClass;
				for (int i=0;i<numInstances;i++)
				{
					  inst = utils.getInstance(actualInst,i);
					  distribution[(int) inst.classValue()] += inst.getWeight();
				}
				distributionClass = Utils.maxIndex(distribution);
				
				//make leaf if information gain is zero....
				if ((Utils.eq(infoGains[attributeIndex], 0))||(ClassifierUtils.sumNonClassDistribution(distribution,distributionClass)<1))
				{
					leaf = new Leaf(classAttribute,distribution);
					xNode.add(leaf);
				}
				
				//...Otherwise create successors.
				else
				{
					//puts children on queue
					splitAttribute = data.getAttribute(actualAtt[realIndex].intValue());
					//if split attribute is nominal...
					if(splitAttribute.isNominal())
					{
						splitData = utils.splitData(split, realIndex);
						for (int j=0;j<splitData.length;j++)
						{
							NominalNode nominalNode = new NominalNode(splitAttribute,j);
							xNode.add(nominalNode);
							queue.add(new QueueComponent(nominalNode,splitData[j]));
						}
					}
					//if split attribute is numeric....
					else
					{
						splitValue = splitValues[realIndex];
						splitData = utils.splitNumericData(split,realIndex, splitValue);

						NumericNode numericNode = new NumericNode(splitAttribute,splitValue,true);
						xNode.add(numericNode);
						queue.add(new QueueComponent(numericNode,splitData[0]));

						numericNode = new NumericNode(splitAttribute,splitValue,false);
						xNode.add(numericNode);
						queue.add(new QueueComponent(numericNode,splitData[1]));
					}
				}
			}
		}

             infoRootNode = xRootNode;

         //prune tree if this option was enabled
         if(Options.getInstance().getIfUsingPrunning())
         {
           PrunningUtils pruner = new PrunningUtils();
           xRootNode = pruner.pruneTree(xRootNode, classAttribute);
         }

	}

	//--------------------------------------------------------------------------//

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
          Stack<Object> stackObj = new Stack<Object>();
          Stack<DefaultMutableTreeNode> stackTree = new Stack<DefaultMutableTreeNode>();
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

	//-------------------------------------------------------------------------//

	/**
	 * Returns a string describing the decision tree created
	 *	
	 * 	@return string representation of a decision tree
	 */
	public String toString()
	{
		return getStringTree(xRootNode);
    }

	//-----------------------------------------------------------------------//
	
	/**
	 * Returns a string describing the decision tree before prunning
	 * (the entire tree if the tree wasn't prunned)
	 *	
	 * 	@return string representation of a decision tree before prunned
	 */
	public String getStringInfoTree()
	{
		return getStringTree(infoRootNode);
	}

	//-----------------------------------------------------------------------//
	
	/**
	 * Returns a string describing a decision tree 
	 * 
	 * @param rootNode tree's root
	 * @return string representing the tree
	 */
	private String getStringTree(Node rootNode)
	{
		Leaf leaf;
		Node node;
		Object obj;
		Integer level;
		Stack<Object> stackObj = new Stack<Object>();
		Stack<Integer> stackLevel = new Stack<Integer>();
		StringBuffer text = new StringBuffer();

		ArrayList root = rootNode.children;
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
	public short classifyInstance(Instance instance)
	{
          Leaf leaf;
          Node node;
          Attribute att;
          double splitValue;
          String[] attValues;
          Node treeNode = xRootNode;

          while (!(treeNode.children.get(0) instanceof Leaf))
          {
            node = (Node)treeNode.children.get(0);
            att = node.getAttribute();
            if (att.isNominal())
            {
              // Atributo nominal
              treeNode = (NominalNode)treeNode.children.get(instance.getValue(node.getAttribute()));
            }
            else
            {
              // Atributo num�rico
              attValues = att.getAttributeValues();
              splitValue = ((NumericNode)node).getSplitValue();

              if(Double.parseDouble(attValues[instance.getValue(att.getIndex())])>=splitValue)
              {
                treeNode = (NumericNode)treeNode.children.get(0);
              }
              else
              {
                treeNode = (NumericNode)treeNode.children.get(1);
              }
            }
          }

          leaf = (Leaf)treeNode.children.get(0);
          return leaf.getClassValue();
	}
	
	//-----------------------------------------------------------------------//
	
	/**
	 * Returns the root of the tree created
	 * 
	 * @return the root of the tree created
	 */
	public Node getRootNode()
	{
		return xRootNode; 
	}
	
	//-----------------------------------------------------------------------//
	
	/**
	 * Returns the root of the tree before prunning 
	 * (the entire tree if it wasn't prunned)
	 * 
	 * @return the root of the tree before prunning
	 */
	public Node getInfoRootNode()
	{
		return infoRootNode;
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
