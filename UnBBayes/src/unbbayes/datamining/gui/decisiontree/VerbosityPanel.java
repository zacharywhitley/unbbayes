package unbbayes.datamining.gui.decisiontree;

import javax.swing.*;
import javax.swing.tree.*;
import java.util.*;

import unbbayes.datamining.datamanipulation.*;
import unbbayes.datamining.classifiers.decisiontree.*;

public class VerbosityPanel extends JScrollPane
{
	private JTextArea textArea;
	
	public VerbosityPanel()
	{
		textArea = new JTextArea();
		this.getViewport().add(textArea, null);
		textArea.setEditable(false);
	}
	
	public void writeVerbosityText(C45 c45,InstanceSet data)
	{
		Node node;
		Node childNode;
		NumericNode numericNode;
		NominalNode nominalNode;
		Leaf leaf;
		int treeLevel;
		DefaultMutableTreeNode treeNodeTemp;
		String space;
		double[][] distribution;
		int attIndex;
		int gainIndex;
						
		//verbosity level
		int verbosityLevel = Options.getInstance().getVerbosityLevel();
				
		//start tree and stack with root
		JTree tree = c45.getTree();
		DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)tree.getModel().getRoot();
		Stack stack = new Stack();
		stack.add(treeNode);
		
		//data relative to node
		SplitObject splitData;
		ArrayList instancesIndexes;
		Integer attributesIndexes;
		double[] infoGains;
		ArrayList numericDataList;
		NumericData numericData;
		Integer[] attIndexes;
		ArrayList instIndexes;
		double cut;
								
		//data relative to instance set
		//InstanceSet instances = c45.getInstances();
		InstanceSet instances = data;
		Attribute[] attributes = instances.getAttributes();
		Attribute classAttribute = instances.getClassAttribute();
		Instance instance;
		Attribute attribute;
		String[] attributeValues;
		String[] classValues = classAttribute.getAttributeValues();
		int classSize = classValues.length;
		
						
		//start string buffer
		StringBuffer text = new StringBuffer();
		text.append(c45.toString());
		text.append("\nTOTAL: "+instances.numWeightedInstances()+" cases\n");
		if(verbosityLevel>1) text.append("\nSPLIT DETAILS:\n");
			
		while (!stack.empty())
		{
			treeNode = (DefaultMutableTreeNode)stack.pop();
										
			if(!treeNode.isLeaf()&&(!treeNode.getFirstChild().isLeaf()))
			{
				if(verbosityLevel>1)
				{
					node = (Node)treeNode.getUserObject();
					infoGains = node.getInfoGains();
					splitData = node.getSplitData();
					attIndexes = splitData.getAttributes();
					instIndexes = splitData.getInstances();
					numericDataList = node.getNumericDataList();
										
					//computes space
					treeLevel = treeNode.getLevel();
					space = "";
					for(int i=0;i<treeLevel;i++)
					{
						space+="|	";
					}
				
					//starts writing for a node				
					if(treeLevel>0)
					{
						text.append(space+node.toString().toUpperCase()+"\n");
					}					
					text.append(space+instIndexes.size()+" instances\n");
										
					if(verbosityLevel>2)
					{
						for(int i=0,j=0,k=0;i<infoGains.length;i++,j++)
						{
							attribute = attributes[attIndexes[j].intValue()];
							
							//fix index for attIndexes
							if(attribute.getIndex()==classAttribute.getIndex())
							{
								j++;
							}
							
							text.append(space+"Attribute "+attribute.getAttributeName()+": ");
							
							if(attribute.isNumeric())
							{
								numericData = (NumericData)numericDataList.get(k);
								cut = 0;
								for(int x=0;x<numericData.getNumberOfCuts();x++)
								{
									if(infoGains[i]==numericData.getGain(x))
									{
										cut = numericData.getCut(x);
										break;
									}
								}
												
								text.append("gain = "+infoGains[i]+", cut = "+cut+".\n");
							}
							else
							{
								text.append("gain = "+infoGains[i]+".\n");
							}
							
							if(verbosityLevel>3)
							{
								if(attribute.isNominal())
								{
									//computes the distribution
									distribution = new double[attribute.getAttributeValues().length][classSize];
									for(int x=0;x<instIndexes.size();x++)
									{
										instance = instances.getInstance(((Integer)instIndexes.get(x)).intValue());
										for(int y=0;y<attribute.getAttributeValues().length;y++)
										{
											if(instance.getValue(attribute)==y)
											{
												distribution[y][instance.getValue(instance.getClassIndex())]+=instance.getWeight();
											}
										}
									}
																  
									text.append(space);
									for(int x=0; x<classSize; x++)
									{
										text.append("	"+classValues[x]);
									}
									text.append("\n");
									
									attributeValues = attribute.getAttributeValues();
									for(int x=0;x<attributeValues.length;x++)
									{
										if(attributeValues[x].length()>5)
										{
											text.append(space+"["+attributeValues[x].substring(0,4).toLowerCase()+".:");
										}
										else
										{
											text.append(space+"["+attributeValues[x].toLowerCase()+":");
										} 
										
										for(int y=0; y<classSize; y++)
										{
											text.append("	"+distribution[x][y]);
										}
										text.append("]\n");
									}
									text.append(space);
								}
								else
								{
									numericData = (NumericData)numericDataList.get(k);
									for(int x=0;x<numericData.getNumberOfCuts();x++)
									{
										text.append(space);
										for(int y=0; y<classSize; y++)
										{
											text.append("	"+classValues[y]);
										}
										text.append("\n");
										
										text.append(space+"[below:");
										for(int y=0; y<classSize; y++)
										{
											text.append("	"+(double)numericData.getInstancesBelow(x)[y]);
										}
										text.append("]\n");
										
										text.append(space+"[above:");
										for(int y=0; y<classSize; y++)
										{
											text.append("	"+(double)numericData.getInstancesAbove(x)[y]);
										}
										text.append("]\n");
										
										text.append(space+"cut: "+numericData.getCut(x)+". gain: "+numericData.getGain(x)+"\n");
									}
								}
								text.append("\n");
							}
							
							if(attribute.isNumeric())
							{
								k++;
							}
						}
					}
					
					//print best attribute
					treeNodeTemp = (DefaultMutableTreeNode)treeNode.getNextNode();
					childNode = (Node)treeNodeTemp.getUserObject();
					attIndex = childNode.getAttribute().getIndex();
					gainIndex = Utils.maxIndex(infoGains);
					if(childNode instanceof NominalNode)
					{
						text.append(space+"Best attribute: "+childNode.getAttributeName()+"(gain = "+infoGains[gainIndex]+")\n\n");
					}
					else
					{
						numericNode = (NumericNode)childNode;
						cut = numericNode.getSplitValue();
						text.append(space+"Best attribute: "+childNode.getAttributeName()+" (gain = "+infoGains[gainIndex]+", cut = "+cut+")\n\n");
					}
					
					if(verbosityLevel>3)
					{
						text.append("-------------------------------------------------\n\n");
					}
					
				}
										
				for(int i=treeNode.getChildCount()-1;i>=0;i--)
				{
					stack.add(treeNode.getChildAt(i));
				}
			}
		}
		
		textArea.setText(text.toString());
		textArea.setCaretPosition(0);
	}
}
