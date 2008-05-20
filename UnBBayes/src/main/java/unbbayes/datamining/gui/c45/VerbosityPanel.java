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
package unbbayes.datamining.gui.c45;

import java.util.ArrayList;
import java.util.Stack;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import unbbayes.datamining.classifiers.decisiontree.C45;
import unbbayes.datamining.classifiers.decisiontree.Leaf;
import unbbayes.datamining.classifiers.decisiontree.Node;
import unbbayes.datamining.classifiers.decisiontree.NominalNode;
import unbbayes.datamining.classifiers.decisiontree.NumericNode;
import unbbayes.datamining.classifiers.decisiontree.SplitObject;
import unbbayes.datamining.datamanipulation.Attribute;
import unbbayes.datamining.datamanipulation.Instance;
import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.datamanipulation.NumericData;
import unbbayes.datamining.datamanipulation.Options;
import unbbayes.datamining.datamanipulation.Utils;

/**
 * Class implementing a window to show data about a decision tree
 * 
 * @author Danilo Balby Silva Castanheira (danbalby@yahoo.com) 
 */
public class VerbosityPanel extends JScrollPane
{
	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;		
	
	/** text area where data is written */
	private JTextArea textArea;
	
	//--------------------------------------------------------------------//
	
	/** Default constructor */
	public VerbosityPanel()
	{
		textArea = new JTextArea();
		this.getViewport().add(textArea, null);
		textArea.setEditable(false);
	}
	
	//-------------------------------------------------------------------//
	
	/** 
	 * Writes data about a decision tree on textArea
	 * 
	 * @param c45 object storing the decision tree
	 * @param data instance set used to create the decision tree  
	 */
	public void writeVerbosityText(C45 c45,InstanceSet data)
	{
		Node node;
		Node childNode;
		NumericNode numericNode;
		NominalNode nominalNode;
		Leaf leaf;
		int treeLevel;
		String space;
		float[][] distribution;
		float[] missingDistribution;
		int attIndex;
		int gainIndex;
		float numInstances;
						
		//options
		int verbosityLevel = Options.getInstance().getVerbosityLevel();
		boolean prunned = Options.getInstance().getIfUsingPrunning();
				
		//start tree and stack with root
		node = c45.getInfoRootNode();
		Stack<Object> stack = new Stack<Object>();
		stack.add(node);
		
		//data relative to node
		SplitObject splitData;
		ArrayList instancesIndexes;
		Integer attributesIndexes;
		double[] infoGains;
		ArrayList numericDataList;
		NumericData numericData;
		Integer[] attIndexes;
		ArrayList instIndexes;
		ArrayList children;
		double cut;
								
		//data relative to instance set
		InstanceSet instances = data;
		Attribute[] attributes = instances.getAttributes();
		Attribute classAttribute = instances.getClassAttribute();
		Instance instance;
		Attribute attribute;
		String[] attributeValues;
		String[] classValues = classAttribute.getDistinticNominalValues();
		int classSize = classValues.length;
								
		//start string buffer
		StringBuffer text = new StringBuffer();
		text.append("DECISION TREE:");
		text.append(c45.toString());
		if(prunned)
		{
			text.append("\n\nDECISION TREE BEFORE PRUNNING:");
			text.append(c45.getStringInfoTree()+"\n");
		}
		text.append("\nTOTAL: "+instances.numWeightedInstances()+" instances\n");
		if(verbosityLevel>1) 
		{
			text.append("\nSPLIT DETAILS:\n");
		}
		
		//for each node...					
		while (!stack.empty())
		{
			node = (Node)stack.pop();
			children = node.getChildren();					
			if(!(children.get(0) instanceof Leaf))
			{
				if(verbosityLevel>1)
				{
					//get instrumentation data
					infoGains = node.getInfoGains();
					splitData = node.getSplitData();
					attIndexes = splitData.getAttributes();
					instIndexes = splitData.getInstances();
					numericDataList = node.getNumericDataList();
										
					//computes space
					treeLevel = attributes.length-attIndexes.length;
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
					
					//gets number of instances 
					numInstances = 0;
					for(int i=0;i<instIndexes.size();i++)
					{
						//gets the next instance
						if(instIndexes.get(i) instanceof Integer)
						{
							instance = instances.getInstance(((Integer)instIndexes.get(i)).intValue());
						}
						else
						{
							instance = (Instance)instIndexes.get(i); 
						}
						numInstances += instance.getWeight();
					}
										
					text.append(space+Utils.keep2DigitsAfterDot(numInstances)+" instances\n");
										
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
									distribution = new float[attribute.getDistinticNominalValues().length][classSize];
									missingDistribution = new float[classSize];
									for(int x=0;x<instIndexes.size();x++)
									{
										//gets the next instance
										if(instIndexes.get(x) instanceof Integer)
										{
											instance = instances.getInstance(((Integer)instIndexes.get(x)).intValue());
										}
										else
										{
											instance = (Instance)instIndexes.get(x); 
										}
										
										if(instance.isMissing(attribute))
										{
											missingDistribution[(int) instance.getValue(instance.getClassIndex())]+=instance.getWeight();
										}
										else
										{
											distribution[(int) instance.getValue(attribute)][(int) instance.getValue(instance.getClassIndex())]+=instance.getWeight();
										}
									}
																  
									text.append(space);
									for(int x=0; x<classSize; x++)
									{
										text.append("	"+classValues[x]);
									}
									text.append("\n");
									
									attributeValues = attribute.getDistinticNominalValues();
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
											text.append("	"+Utils.keep2DigitsAfterDot(distribution[x][y]));
										}
										text.append("]\n");
									}
									
									//unknown values
									text.append(space+"[unkn.:");
									for(int x=0; x<classSize; x++)
									{
										text.append("	"+Utils.keep2DigitsAfterDot(missingDistribution[x]));
									}
									text.append("]\n");
									
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
											text.append("	"+Utils.keep2DigitsAfterDot(numericData.getInstancesBelow(x)[y]));
										}
										text.append("]\n");
										
										text.append(space+"[above:");
										for(int y=0; y<classSize; y++)
										{
											text.append("	"+Utils.keep2DigitsAfterDot(numericData.getInstancesAbove(x)[y]));
										}
										text.append("]\n");
										
										text.append(space+"[unkn.:");
										for(int y=0; y<classSize; y++)
										{
											text.append("	"+Utils.keep2DigitsAfterDot(numericData.getMissingValuesDistribution()[y]));
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
					childNode = (Node)children.get(0);
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
				
				//add children on stack						
				for(int i=children.size()-1;i>=0;i--)
				{
					stack.add(children.get(i));
				}
			}
		}
		
		textArea.setText(text.toString());
		textArea.setCaretPosition(0);
	}
}
