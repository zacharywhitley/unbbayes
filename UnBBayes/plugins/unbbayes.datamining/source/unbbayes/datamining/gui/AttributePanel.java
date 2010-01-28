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
package unbbayes.datamining.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.Enumeration;
import java.util.ResourceBundle;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import unbbayes.datamining.datamanipulation.Attribute;
import unbbayes.datamining.datamanipulation.InstanceSet;

public class AttributePanel extends JPanel
{
	
	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;		
	
	private BorderLayout borderLayout1 = new BorderLayout();
	private JTree leftTree;
	private JTree rightTree;
	private JSplitPane jSplitPane = new JSplitPane();
	private JPanel rigthPanel = new JPanel();
	private JScrollPane rightScrollPane = new JScrollPane();
	private BorderLayout borderLayout2 = new BorderLayout();
	private JPanel leftPanel = new JPanel();
	private JScrollPane leftScrollPane = new JScrollPane();
	private BorderLayout borderLayout3 = new BorderLayout();
	private JPanel classPanel = new JPanel();
	private JComboBox classComboBox = new JComboBox();
	private JLabel classLabel = new JLabel();
	private InstanceSet instances;
	/** Carrega o arquivo de recursos para internacionaliza��o da localidade padr�o */
	private ResourceBundle resource;

	public AttributePanel()
	{ resource = unbbayes.util.ResourceController.newInstance().getBundle(
			unbbayes.datamining.gui.resources.GuiResource.class.getName());
	try
		{
		jbInit();
		}
		catch(Exception e)
		{
		e.printStackTrace();
		}
	}
	private void jbInit() throws Exception
	{ this.setLayout(borderLayout1);
		rigthPanel.setLayout(borderLayout2);
		rigthPanel.setBackground(Color.white);
		leftPanel.setLayout(borderLayout3);
		leftPanel.setBackground(Color.white);
		classLabel.setText(resource.getString("selectClass"));
		classComboBox.addActionListener(new java.awt.event.ActionListener()
		{
		public void actionPerformed(ActionEvent e)
		{
			classComboBox_actionPerformed(e);
		}
		});
		classComboBox.setEnabled(false);
		jSplitPane.setEnabled(false);
		this.add(jSplitPane,	BorderLayout.CENTER);
		jSplitPane.add(rigthPanel, JSplitPane.BOTTOM);
		rigthPanel.add(rightScrollPane, BorderLayout.CENTER);
		jSplitPane.add(leftPanel, JSplitPane.TOP);
		leftPanel.add(leftScrollPane, BorderLayout.CENTER);
		this.add(classPanel, BorderLayout.NORTH);
		classPanel.add(classLabel, null);
		classPanel.add(classComboBox, null);
	}

	public void setInstances(InstanceSet instances)
	{	 this.instances = instances;
		int numAtt = instances.numAttributes();
		classComboBox.removeAllItems();
		for(int i=0; i<numAtt; i++)
		{	 classComboBox.addItem(instances.getAttribute(i).getAttributeName());
				if(i == (numAtt - 1))
					classComboBox.setSelectedItem(instances.getAttribute(i).getAttributeName());
		}
		buildTrees(numAtt - 1);
		jSplitPane.setEnabled(true);
		jSplitPane.setDividerLocation(0.3);
	}

	private void buildTrees(int index)
	{	 instances.setClass(instances.getAttribute(index));
		DefaultTreeModel treeModel = new DefaultTreeModel(mountClassTree());
		leftTree = new JTree(treeModel);
		leftScrollPane.getViewport().add(leftTree, null);
		treeModel = new DefaultTreeModel(mountAttributeTree());
		rightTree = new JTree(treeModel);
		rightScrollPane.getViewport().add(rightTree, null);
	}

	private DefaultMutableTreeNode mountClassTree()
	{	 Attribute att = instances.getClassAttribute();
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(resource.getString("class")+att.getAttributeName());
		insertAttributeTree(att,root);
		return root;
	}

	private DefaultMutableTreeNode mountAttributeTree()
	{	 int numAtt = instances.numAttributes();
		int numClass = instances.getClassAttribute().getIndex();
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(resource.getString("attributes"));
		DefaultMutableTreeNode leaf;
		for(int i=0; i<numAtt; i++)
		{	 if (i!=numClass)
				{	 leaf = new DefaultMutableTreeNode(instances.getAttribute(i).getAttributeName());
					root.add(leaf);
					Attribute att = instances.getAttribute(i);
					insertAttributeTree(att,leaf);
				}
		}
		return root;
	}

	private void insertAttributeTree(Attribute att, DefaultMutableTreeNode node) {
		if (!att.isNominal()) {
			DefaultMutableTreeNode leaf = new DefaultMutableTreeNode("numeric");
			node.add(leaf);
		} else {
			Enumeration enumeration = att.enumerateValues();
			DefaultMutableTreeNode leaf;
			while (enumeration.hasMoreElements()) {
				leaf = new DefaultMutableTreeNode(enumeration.nextElement());
				node.add(leaf);
			}
		}
	}

	void classComboBox_actionPerformed(ActionEvent e)
	{	 if(classComboBox.getSelectedIndex()>=0)
				buildTrees(classComboBox.getSelectedIndex());
	}

	public void enableComboBox(boolean bool)
	{	 classComboBox.setEnabled(bool);
	}

}