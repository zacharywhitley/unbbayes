package unbbayes.datamining.gui.decisiontree;

import javax.swing.*;
import javax.swing.tree.*;
import java.util.*;

import unbbayes.datamining.datamanipulation.*;
import unbbayes.datamining.classifiers.*;

public class VerbosityPanel extends JScrollPane
{
	private JTextArea textArea;
	
	public VerbosityPanel()
	{
		textArea = new JTextArea();
		this.getViewport().add(textArea, null);
		textArea.setEditable(false);
	}
	
	public void writeVerbosityText(C45 id3)
	{
/*		Node node;
		Leaf leaf;
		int treeLevel;
		Enumeration enum;
		DefaultMutableTreeNode treeNodeTemp;
		double[] infoGains;
		double[] distribution;
		String space;
		String[] values;
		
		int verbosityLevel = Options.getInstance().getVerbosityLevel();
		StringBuffer text = new StringBuffer();
		text.append(id3.toString());
		if(verbosityLevel>1) text.append("\n\nSPLIT DETAILS:\n");
		
		JTree tree = id3.getTree();
		DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)tree.getModel().getRoot();
		Stack stack = new Stack();
		stack.add(treeNode);
		
		InstanceSet instances = id3.getInstances();
		Attribute classAttribute = instances.getClassAttribute();
		int distSize = classAttribute.getAttributeValues().length;
		Attribute[] attributes = instances.getAttributes();		
				
		while (!stack.empty())
		{
			treeNode = (DefaultMutableTreeNode)stack.pop();
							
			if(!treeNode.isLeaf()&&(!treeNode.getFirstChild().isLeaf()))
			{
				infoGains = id3.getInfoGains(treeNode);
				
				enum = treeNode.depthFirstEnumeration();
				distribution = new double[distSize];
				Arrays.fill(distribution,0);
				while(enum.hasMoreElements())
				{
					treeNodeTemp = (DefaultMutableTreeNode)enum.nextElement(); 
					if(treeNodeTemp.isLeaf())
					{
						leaf = (Leaf)treeNodeTemp.getUserObject();
						distribution = Utils.arraysSum(distribution,leaf.getDistribution());												
					}
				}
				
				if(verbosityLevel>1)
				{
					treeLevel = treeNode.getLevel();
					space = "";
					for(int i=0;i<treeLevel;i++)
					{
						space+="|	";
					}
				
									
					if(treeLevel>0)
					{
						node = (Node)treeNode.getUserObject();	
						text.append(space+node.toString()+"\n");
					}					
					text.append(space+Utils.sum(distribution)+" instances\n");
					
					treeNodeTemp = (DefaultMutableTreeNode)treeNode.getNextNode();
					node = (Node)treeNodeTemp.getUserObject();
					
					if(verbosityLevel>2)
					{
						for(int i=0;i<infoGains.length;i++)
						{
							text.append(space+"Attribute "+attributes[i].getAttributeName()+": ");
							/*if(verbosityLevel>3)
							{
								
								text.append("\n");
								if(attributes[i].isNominal())
								{
									values = attributes[i].getAttributeValues();
									for(int j=0;j<values.length;j++)
									{
										text.append(space+"["+values[i]+"	:");
									}
								}
								else
								{
									
								}
						
							}*/
/*							text.append("gain = "+infoGains[i]+"\n");
						}
					}
					
					text.append(space+"Best attribute: "+node.getAttributeName()+". Gain: "+infoGains[node.getAttribute().getIndex()]+"\n\n");
				}
										
				for(int i=treeNode.getChildCount()-1;i>=0;i--)
				{
					stack.add(treeNode.getChildAt(i));
				}
			}
		}
		
		textArea.setText(text.toString());*/
	}
}
