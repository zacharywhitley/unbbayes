package unbbayes.datamining.gui.explanation;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
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
  private DefaultTreeModel model;
  private ProbabilisticNetwork net;
  private JPanel jPanel5 = new JPanel();
  private GridLayout gridLayout1 = new GridLayout();
  private ImageIcon greenBallIcon;
  private ImageIcon expandIcon;
  private ImageIcon collapseIcon;
  private JToolBar jToolBar1 = new JToolBar();
  private JButton jButton1 = new JButton();
  private JButton jButton2 = new JButton();
  private JButton jButton3 = new JButton();
  private JButton jButton4 = new JButton();
  private JButton jButton5 = new JButton();
  private JButton jButton6 = new JButton();

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
  { greenBallIcon = new ImageIcon(getClass().getResource("/icones/green-ball.gif"));
    expandIcon = new ImageIcon(getClass().getResource("/icones/expandir.gif"));
    collapseIcon = new ImageIcon(getClass().getResource("/icones/contrair.gif"));
    this.setLayout(borderLayout1);
    jPanel2.setLayout(borderLayout2);
    jPanel1.setLayout(borderLayout3);
    jPanel3.setLayout(borderLayout4);
    jPanel5.setLayout(gridLayout1);
    jPanel5.setBackground(new Color(255, 251, 240));
    jToolBar1.setBorder(null);
    jToolBar1.setFloatable(false);
    jButton2.setToolTipText("Expand Tree");
    jButton2.setIcon(expandIcon);
    jButton2.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButton2_actionPerformed(e);
      }
    });
    jButton1.setToolTipText("Collapse Tree");
    jButton1.setIcon(collapseIcon);
    jButton1.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButton1_actionPerformed(e);
      }
    });
    jButton3.setToolTipText("Add Top Folder");
    jButton3.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButton3_actionPerformed(e);
      }
    });
    jButton4.setToolTipText("Add Child Folder");
    jButton4.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButton4_actionPerformed(e);
      }
    });
    jButton5.setToolTipText("Rename Folder");
    jButton5.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButton5_actionPerformed(e);
      }
    });
    jButton6.setToolTipText("Delete Folder");
    jButton6.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButton6_actionPerformed(e);
      }
    });
    this.add(jPanel2, BorderLayout.CENTER);
    jPanel2.add(jSplitPane1,  BorderLayout.CENTER);
    jSplitPane1.add(jPanel1, JSplitPane.TOP);
    jPanel1.add(jScrollPane1,  BorderLayout.CENTER);
    jSplitPane1.add(jPanel3, JSplitPane.BOTTOM);
    jPanel3.add(jScrollPane2,  BorderLayout.CENTER);
    jScrollPane2.getViewport().add(jPanel5, null);
    this.add(jToolBar1, BorderLayout.NORTH);
    jToolBar1.add(jButton1, null);
    jToolBar1.add(jButton2, null);
    jToolBar1.addSeparator();
    jToolBar1.add(jButton3, null);
    jToolBar1.add(jButton4, null);
    jToolBar1.addSeparator();
    jToolBar1.add(jButton5, null);
    jToolBar1.add(jButton6, null);
  }

  /*void addChildButton_actionPerformed(ActionEvent e)
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
  }*/


  void applyButton_actionPerformed(ActionEvent e)
  {   net.setHierarchicTree(hierarchicTree);
  }

  public void setHierarchicTree(ProbabilisticNetwork net)
  {   this.net = net;
      this.hierarchicTree = net.getHierarchicTree();
      model = (DefaultTreeModel)hierarchicTree.getModel();
      jScrollPane1.getViewport().removeAll();
      jScrollPane1.getViewport().add(hierarchicTree, null);
      jSplitPane1.setDividerLocation(0.5);
      updateScreen();
  }

  private void updateScreen()
  {   int size = net.getExplanationNodes().size();
      if (size != 0)
          gridLayout1.setRows(size);
      jPanel5.removeAll();
      for(int i=0;i<size;i++)
      {   JRadioButton radio = new JRadioButton(net.getExplanationNodes().get(i).getDescription(),greenBallIcon);
          radio.setBackground(new Color(255,251,240));
          jPanel5.add(radio);
      }
  }

  void jButton2_actionPerformed(ActionEvent e)
  {   hierarchicTree.expandTree();
  }

  void jButton1_actionPerformed(ActionEvent e)
  {   hierarchicTree.collapseTree();
  }

  void jButton3_actionPerformed(ActionEvent e)
  {   String result = JOptionPane.showInternalInputDialog(this,"","Add Top Folder",JOptionPane.QUESTION_MESSAGE);
      if ((result != null)&&(!result.equals("")))
      {   DefaultMutableTreeNode root = ((DefaultMutableTreeNode)model.getRoot());
          DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(result);
          model.insertNodeInto(newNode,root,root.getChildCount());
          showNewNode(newNode);
      }
  }

  private void showNewNode(DefaultMutableTreeNode newNode)
  {   TreeNode[] nodes = model.getPathToRoot(newNode);
      TreePath path = new TreePath(nodes);
      hierarchicTree.scrollPathToVisible(path);
  }

  void jButton6_actionPerformed(ActionEvent e)
  {   DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)hierarchicTree.getLastSelectedPathComponent();
      if (selectedNode == null)
          return;
      else
      {   model.removeNodeFromParent(selectedNode);
      }
  }

  void jButton5_actionPerformed(ActionEvent e)
  {   DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)hierarchicTree.getLastSelectedPathComponent();
      if (selectedNode == null)
          return;
      else
      {   String result = JOptionPane.showInternalInputDialog(this,"","New Folder Name",JOptionPane.QUESTION_MESSAGE);
          selectedNode.setUserObject(result);
          model.reload(selectedNode);
      }
  }

  void jButton4_actionPerformed(ActionEvent e)
  {   DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)hierarchicTree.getLastSelectedPathComponent();
      if (selectedNode == null)
          return;
      else
      {   String result = JOptionPane.showInternalInputDialog(this,"","Add Child Folder",JOptionPane.QUESTION_MESSAGE);
          if ((result != null)&&(!result.equals("")))
          {   DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(result);
              model.insertNodeInto(newNode,selectedNode,selectedNode.getChildCount());
              showNewNode(newNode);
          }
      }
  }
}