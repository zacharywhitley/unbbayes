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
public class C45 extends DecisionTreeLearning implements Serializable{

	/** Load resources file for internacionalization */
	private transient ResourceBundle resource;
	
	/** The root of the tree generated by the constructor */
	private JTree tree ;
	
	public void buildClassifier(InstanceSet data) throws Exception
	{	
		//internacionalization
		resource = ResourceBundle.getBundle("unbbayes.datamining.classifiers.resources.ClassifiersResource");
         
		//preliminary tests 
		if (!data.getClassAttribute().isNominal())
		{
			throw new Exception(resource.getString("exception1"));
		}
         		
		Enumeration enum = data.enumerateInstances();
		while (enum.hasMoreElements())
		{
			Instance instance = (Instance)enum.nextElement();
			Enumeration enumAtt = data.enumerateAttributes();
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
		//Root of the tree, is constant during the function(isn't part of the final tree)
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("root"); 
		//Tree node variable used to keep the current one
		DefaultMutableTreeNode treeNode = root;
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

		//////////////////////////
		//Node xRootNode = new Node(null);
		//Node xNode = xRootNode;
		//////////////////////////

		//start stack with the initial data
		queue.add(new QueueComponent(treeNode,split));
		//queue.add(new QueueComponent(treeNode,split,xNode));

		QueueComponent queueComponent;
		Leaf leaf;
		double[] infoGains;
		int attributeIndex;
		double[] distribution;		
		Instance inst;
		SplitObject[] splitData;
		DefaultMutableTreeNode treeNodeNew;
		Node node;
		Attribute att;
		double meanInfoGains;
		Attribute splitAttribute;
		double splitValue;
		
		double[] splitValues;
		ArrayList numericDataList;
		
		//start recursive code
		while ((!queue.isEmpty()))
		{
			//gets the objects relative to the iteration  
			queueComponent = (QueueComponent)queue.remove(0);
			///////////////////////
			//xNode = queueComponent.getNodeParent();
			///////////////////////
			treeNode = queueComponent.getParent();
			split = queueComponent.splitObject;
			actualInst = split.getInstances();
			numInstances = actualInst.size();
			actualAtt = split.getAttributes();
			numAttributes = actualAtt.length;

			//if no instance has reached this node - is leaf 
			if (numInstances == 0)
			{
				leaf = new Leaf(Instance.missingValue(),new double[numClasses]);
				treeNode.add(new DefaultMutableTreeNode(leaf));
				///////////////////
				//xNode.add(leaf);
				///////////////////
			}
			else
			{
				// compute array with the gain of each attribute.
				// splitValues and numericDataList are initialized here
				splitValues = new double[actualAtt.length];
				numericDataList = new ArrayList();
				infoGains = utils.computeInfoGain(split, splitValues, numericDataList);
				
				//applies gain ratio if user chooses it
				if(Options.getInstance().getIfUsingGainRatio())
				{
					//applies gain ratio to attribute if its gain is greater than mean
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
					
				//sets a node for treeNode if it is the root
				if(treeNode.getLevel()==0)
				{
					treeNode.setUserObject(new Node(data.getAttribute(actualAtt[realIndex].intValue())));
				}
			
				//add data relative to infogain calculus into the actual node
				node = (Node)treeNode.getUserObject();
				node.setInstrumentationData(split,infoGains,numericDataList);
								
				//make leaf if information gain is zero....
				if (Utils.eq(infoGains[attributeIndex], 0))
				{
					//computes the number of instances for each class
					distribution = new double[numClasses];
					for (int i=0;i<numInstances;i++)
					{
						inst = data.getInstance(((Integer)actualInst.get(i)).intValue());				
						distribution[(int) inst.classValue()] += inst.getWeight();
					}
           			
					leaf = new Leaf(classAttribute,distribution);
					treeNode.add(new DefaultMutableTreeNode(leaf));
					////////////////
					//xNode.add(leaf);
					////////////////
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
							treeNodeNew = new DefaultMutableTreeNode(new NominalNode(splitAttribute,j));
							treeNode.add(treeNodeNew);
							//xNode.add(new NominalNode(splitAttribute,j));
							queue.add(new QueueComponent(treeNodeNew,splitData[j]));
							//queue.add(new QueueComponent(treeNodeNew,splitData[j],new NominalNode(splitAttribute,j)));
						}
					}
					//if split attribute is numeric....
					else
					{
						splitValue = splitValues[realIndex];
						splitData = utils.splitNumericData(split,realIndex, splitValue);
						
						treeNodeNew = new DefaultMutableTreeNode(new NumericNode(splitAttribute,splitValue,true));
						treeNode.add(treeNodeNew);
						//xNode.add(new NumericNode(splitAttribute,splitValue,true));
						queue.add(new QueueComponent(treeNodeNew,splitData[0]));
						//queue.add(new QueueComponent(treeNodeNew,splitData[0],new NumericNode(splitAttribute,splitValue,true)));
						
						treeNodeNew = new DefaultMutableTreeNode(new NumericNode(splitAttribute,splitValue,false));
						treeNode.add(treeNodeNew);
						//xNode.add(new NumericNode(splitAttribute,splitValue,false));
						queue.add(new QueueComponent(treeNodeNew,splitData[1]));
						//queue.add(new QueueComponent(treeNodeNew,splitData[1],new NumericNode(splitAttribute,splitValue,false)));
					}
				}
			}
		}

		if(root.isLeaf())
		{
			 root.setUserObject("NULL");
		}
		
		tree = new JTree(root);				
	}
	
	//--------------------------------------------------------------------------//

	public JTree getTree()
	{
		return tree;
	}
	
	//-------------------------------------------------------------------------//

	public String toString()
	{
		Leaf leaf;
		Node node;
		Stack stack = new Stack();
		StringBuffer text = new StringBuffer();
		DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)tree.getModel().getRoot();
	
		stack.push(treeNode);
	 
		while (!stack.empty())
		{
			treeNode = (DefaultMutableTreeNode)stack.pop();
				
			if(treeNode.isLeaf())
			{
				leaf = (Leaf)treeNode.getUserObject();
				text.append(": "+leaf.toString());
			}
			else
			{
				if(treeNode.getLevel()!=0)
				{
					text.append("\n"); 
					for (int i=0;i<treeNode.getLevel()-1;i++)
					{
						text.append("|	");
					}
					node = (Node)treeNode.getUserObject();
					text.append(node.toString());
				}
						
				for(int i=treeNode.getChildCount()-1;i>=0;i--)
				{
					stack.push(treeNode.getChildAt(i));
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
	public byte classifyInstance(Instance instance)
	{		
	  Leaf leaf;
	  NominalNode node;
	  Attribute att;
	  double splitValue;
	  String[] attValues;
	  DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)tree.getModel().getRoot();
	  DefaultMutableTreeNode treeNodeTemp;
		
	  if (treeNode.isLeaf())
	  {
		  return -1;
	  }
			
	  while(!treeNode.getFirstChild().isLeaf())
	  {
		  treeNodeTemp = (DefaultMutableTreeNode)treeNode.getFirstChild();
		  node = (NominalNode)treeNodeTemp.getUserObject();
		  att = node.getAttribute();
		  if (att.isNominal())
		  {
			  treeNode = (DefaultMutableTreeNode)treeNode.getChildAt(instance.getValue(node.getAttribute()));
		  }
		  else
		  {
			  attValues = att.getAttributeValues();
			  splitValue = node.getAttributeValue();
			
			  if(Double.parseDouble(attValues[instance.getValue(att.getIndex())])>=splitValue)
			  {
				  treeNode = (DefaultMutableTreeNode)treeNode.getChildAt(0);
			  }
			  else
			  {
				  treeNode = (DefaultMutableTreeNode)treeNode.getChildAt(1);
			  }
		  }
	  }
    
	  treeNode = (DefaultMutableTreeNode)treeNode.getFirstChild();
	  leaf = (Leaf)treeNode.getUserObject();
	  return leaf.getClassValue();
	}
	
	/*************************************************************************/

	private class QueueComponent
	{
		private DefaultMutableTreeNode parent;
		private SplitObject splitObject;
		
		////////////////////
		/*private Node nodeParent;
		public QueueComponent(Node newParent, SplitObject splitObject)
		{
			nodeParent = newParent;
			this.splitObject = splitObject;
		}
		public Node getNodeParent()
		{
			return nodeParent;
		}
		public QueueComponent(DefaultMutableTreeNode newParent, SplitObject splitObject, Node nParent)
		{
			parent = newParent;
			this.splitObject = splitObject;
			nodeParent = nParent;
		}*/
		///////////////////
	
		public QueueComponent(DefaultMutableTreeNode newParent, SplitObject splitObject)
		{
			parent = newParent;
			this.splitObject = splitObject;
		}
	
		public SplitObject getSplitObject()
		{
			return splitObject;
		}
	
		public DefaultMutableTreeNode getParent()
		{
			return parent;
		}
	}
}