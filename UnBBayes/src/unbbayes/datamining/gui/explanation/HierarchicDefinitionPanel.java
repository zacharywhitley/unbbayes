package unbbayes.datamining.gui.explanation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.tree.*;

import unbbayes.jprs.jbn.*;

public class HierarchicDefinitionPanel extends JPanel
{
  private BorderLayout borderLayout1 = new BorderLayout();
  private JMenuBar jMenuBar1 = new JMenuBar();
  private JPanel jPanel2 = new JPanel();
  private BorderLayout borderLayout2 = new BorderLayout();
  private JSplitPane jSplitPane1 = new JSplitPane();
  private JPanel jPanel1 = new JPanel();
  private JPanel jPanel3 = new JPanel();
  private JScrollPane jScrollPane1 = new JScrollPane();
  private JScrollPane jScrollPane2 = new JScrollPane();
  private BorderLayout borderLayout3 = new BorderLayout();
  private BorderLayout borderLayout4 = new BorderLayout();
  private HierarchicTree hierarchicTree;
  private JPanel jPanel4 = new JPanel();
  private JButton applyButton = new JButton();
  private JButton addChildButton = new JButton();
  private DefaultTreeModel model;
  private JTextField jTextField1 = new JTextField();
  private JList jList1 = new JList();
  private ProbabilisticNetwork net;

  public HierarchicDefinitionPanel()
  {
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
    jPanel2.setLayout(borderLayout2);
    jPanel1.setLayout(borderLayout3);
    jPanel3.setLayout(borderLayout4);
    applyButton.setText("Apply Changes");
    applyButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        applyButton_actionPerformed(e);
      }
    });
    addChildButton.setText("add child");
    addChildButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        addChildButton_actionPerformed(e);
      }
    });
    jTextField1.setColumns(11);
    this.add(jPanel2, BorderLayout.CENTER);
    jPanel2.add(jSplitPane1,  BorderLayout.CENTER);
    jSplitPane1.add(jPanel1, JSplitPane.TOP);
    jPanel1.add(jScrollPane1,  BorderLayout.CENTER);
    jSplitPane1.add(jPanel3, JSplitPane.BOTTOM);
    jPanel3.add(jScrollPane2,  BorderLayout.CENTER);
    jScrollPane2.getViewport().add(jList1, null);
    this.add(jPanel4, BorderLayout.NORTH);
    jPanel4.add(jTextField1, null);
    jPanel4.add(addChildButton, null);
    jPanel4.add(applyButton, null);
  }

  void addChildButton_actionPerformed(ActionEvent e)
  {   DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)hierarchicTree.getLastSelectedPathComponent();

      if (selectedNode == null)
      {   return;
      }
      else if (e.getSource().equals(addChildButton))
      {   // add new node child
          String newNodeName = "New";
          if (!jTextField1.getText().equals(""))
              newNodeName = jTextField1.getText();
          DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(newNodeName);
          model.insertNodeInto(newNode, selectedNode, selectedNode.getChildCount());

          // now display new node
          TreeNode[] nodes = model.getPathToRoot(newNode);
          TreePath path = new TreePath(nodes);
          hierarchicTree.scrollPathToVisible(path);
      }
  }


  void applyButton_actionPerformed(ActionEvent e)
  {   net.setHierarchicTree(hierarchicTree);
  }

  public void setHierarchicTree(ProbabilisticNetwork net)
  {   this.net = net;
      this.hierarchicTree = net.getHierarchicTree();
      model = new DefaultTreeModel((TreeNode)hierarchicTree.getModel().getRoot());
      jScrollPane1.getViewport().add(hierarchicTree, null);
      jSplitPane1.setDividerLocation(0.5);
  }
}