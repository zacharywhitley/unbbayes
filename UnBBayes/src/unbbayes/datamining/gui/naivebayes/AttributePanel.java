package unbbayes.datamining.gui.naivebayes;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.util.*;
import unbbayes.datamining.datamanipulation.*;
import java.awt.event.*;

public class AttributePanel extends JPanel
{
  BorderLayout borderLayout1 = new BorderLayout();
  JTree leftTree;
  JTree rightTree;
  JSplitPane jSplitPane = new JSplitPane();
  JPanel rigthPanel = new JPanel();
  JScrollPane rightScrollPane = new JScrollPane();
  BorderLayout borderLayout2 = new BorderLayout();
  JPanel leftPanel = new JPanel();
  JScrollPane leftScrollPane = new JScrollPane();
  BorderLayout borderLayout3 = new BorderLayout();
  JPanel classPanel = new JPanel();
  JComboBox classComboBox = new JComboBox();
  JLabel classLabel = new JLabel();
  InstanceSet instances;

  public AttributePanel()
  { try
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
    classLabel.setText("Select Class =");
    classComboBox.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        classComboBox_actionPerformed(e);
      }
    });
    classComboBox.setEnabled(false);
    this.add(jSplitPane,  BorderLayout.CENTER);
    jSplitPane.add(rigthPanel, JSplitPane.BOTTOM);
    rigthPanel.add(rightScrollPane, BorderLayout.CENTER);
    jSplitPane.add(leftPanel, JSplitPane.TOP);
    leftPanel.add(leftScrollPane, BorderLayout.CENTER);
    this.add(classPanel, BorderLayout.NORTH);
    classPanel.add(classLabel, null);
    classPanel.add(classComboBox, null);
  }

  public void setInstances(InstanceSet instances)
  {   this.instances = instances;
      int numAtt = instances.numAttributes();
      classComboBox.removeAllItems();
      for(int i=0; i<numAtt; i++)
      {   classComboBox.addItem(instances.getAttribute(i).getAttributeName());
          if(i==(numAtt-1))
              classComboBox.setSelectedItem(instances.getAttribute(i).getAttributeName());
      }
      buildTrees(numAtt-1);
  }

  private void buildTrees(int index)
  {   instances.setClass(instances.getAttribute(index));
      DefaultTreeModel treeModel = new DefaultTreeModel(mountClassTree());
      leftTree = new JTree(treeModel);
      leftScrollPane.getViewport().add(leftTree, null);
      treeModel = new DefaultTreeModel(mountAttributeTree());
      rightTree = new JTree(treeModel);
      rightScrollPane.getViewport().add(rightTree, null);
  }

  private DefaultMutableTreeNode mountClassTree()
  {   Attribute att = instances.getClassAttribute();
      DefaultMutableTreeNode root = new DefaultMutableTreeNode("Class = "+att.getAttributeName());
      insertAttributeTree(att,root);
      return root;
  }

  private DefaultMutableTreeNode mountAttributeTree()
  {   int numAtt = instances.numAttributes();
      int numClass = instances.getClassAttribute().getIndex();
      DefaultMutableTreeNode root = new DefaultMutableTreeNode("Attributes = ");
      DefaultMutableTreeNode leaf;
      for(int i=0; i<numAtt; i++)
      {   if (i!=numClass)
          {   leaf = new DefaultMutableTreeNode(instances.getAttribute(i).getAttributeName());
              root.add(leaf);
              Attribute att = instances.getAttribute(i);
              insertAttributeTree(att,leaf);
          }
      }
      return root;
  }

  private void insertAttributeTree(Attribute att, DefaultMutableTreeNode node)
  {   int numClasses = att.numValues();
      if (numClasses==0)
      {   DefaultMutableTreeNode leaf = new DefaultMutableTreeNode("numeric");
          node.add(leaf);
      }
      else
      {   Enumeration enum = att.enumerateValues();
          DefaultMutableTreeNode leaf;
          while (enum.hasMoreElements())
          {	leaf = new DefaultMutableTreeNode(enum.nextElement());
                node.add(leaf);
          }
      }
  }

  void classComboBox_actionPerformed(ActionEvent e)
  {   if(classComboBox.getSelectedIndex()>=0)
          buildTrees(classComboBox.getSelectedIndex());
  }

  public void enableComboBox(boolean bool)
  {   classComboBox.setEnabled(bool);
  }

}