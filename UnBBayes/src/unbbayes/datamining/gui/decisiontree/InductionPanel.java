package unbbayes.datamining.gui.decisiontree;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import unbbayes.controller.IconController;
import unbbayes.datamining.classifiers.*;

public class InductionPanel extends JPanel
{
  BorderLayout borderLayout1 = new BorderLayout();
  JSplitPane jSplitPane = new JSplitPane();
  JPanel messagesPanel = new JPanel();
  JTree jTree = new JTree();
  JPanel leftSplitPanel = new JPanel();
  JPanel rightSplitPanel = new JPanel();
  BorderLayout borderLayout2 = new BorderLayout();
  JPanel messageBodyPanel = new JPanel();
  JPanel messageTitlePanel = new JPanel();
  JLabel messageLabel = new JLabel();
  GridLayout gridLayout1 = new GridLayout();
  JScrollPane jScrollPane1 = new JScrollPane();
  JTextArea messageTextArea = new JTextArea();
  BorderLayout borderLayout3 = new BorderLayout();
  JScrollPane jScrollPane2 = new JScrollPane();
  BorderLayout borderLayout4 = new BorderLayout();
  BorderLayout borderLayout5 = new BorderLayout();
  JPanel nodetitlePanel = new JPanel();
  JLabel nodeLabel = new JLabel();
  ButtonGroup buttonGroup = new ButtonGroup();
  JScrollPane jScrollPane3 = new JScrollPane();
  JPanel nodePanel = new JPanel();
  GridLayout gridLayout2 = new GridLayout();
  TreeModel model;
  Object actualRoot;
  Object root;
  DefaultTreeModel defaultModel;
  JRadioButton jRadioButton;
  private static final int DONT_EXPAND = 0;
  private static final int EXPAND = 1;
  private int controller = DONT_EXPAND;
  private ResourceBundle resource;

  public InductionPanel()
  {	resource = ResourceBundle.getBundle("unbbayes.datamining.gui.decisiontree.resources.DecisiontreeResource");
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
  {
    this.setLayout(borderLayout1);
    messagesPanel.setLayout(borderLayout2);
    messageLabel.setText(resource.getString("messages"));
    messageTitlePanel.setLayout(gridLayout1);
    messageBodyPanel.setLayout(borderLayout3);
    messageTextArea.setEditable(false);
    messageTextArea.setRows(5);
    leftSplitPanel.setLayout(borderLayout4);
    rightSplitPanel.setLayout(borderLayout5);
    nodeLabel.setToolTipText("");
    nodePanel.setLayout(gridLayout2);
    gridLayout2.setRows(5);
    this.add(jSplitPane, BorderLayout.CENTER);
    jSplitPane.add(leftSplitPanel, JSplitPane.TOP);
    leftSplitPanel.add(jScrollPane2,  BorderLayout.CENTER);
    jSplitPane.add(rightSplitPanel, JSplitPane.BOTTOM);
    rightSplitPanel.add(nodetitlePanel,  BorderLayout.NORTH);
    nodetitlePanel.add(nodeLabel, null);
    rightSplitPanel.add(jScrollPane3, BorderLayout.CENTER);
    jScrollPane3.getViewport().add(nodePanel, null);
    this.add(messagesPanel,  BorderLayout.SOUTH);
    messagesPanel.add(messageBodyPanel,  BorderLayout.CENTER);
    messageBodyPanel.add(jScrollPane1, BorderLayout.CENTER);
    jScrollPane1.getViewport().add(messageTextArea, null);
    messagesPanel.add(messageTitlePanel, BorderLayout.NORTH);
    messageTitlePanel.add(messageLabel, null);
  }

  public void setInstances(Id3 id3)
  {   jTree = id3.getTree();
      expand(jTree);
      jTree.addTreeWillExpandListener(new javax.swing.event.TreeWillExpandListener()
      {   public void treeWillExpand(TreeExpansionEvent e) throws ExpandVetoException
          {   jTree_treeWillExpand(e);
          }
          public void treeWillCollapse(TreeExpansionEvent e) throws ExpandVetoException
          {   jTree_treeWillCollapse(e);
          }
      });
      jTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener()
      {   public void valueChanged(TreeSelectionEvent e)
          {   jTree_valueChanged(e);
          }
      });
      TreeSelectionModel tsm = jTree.getSelectionModel();
      tsm.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
      jScrollPane2.getViewport().add(jTree, null);
      model = jTree.getModel();
      actualRoot = model.getRoot();
      root = model.getRoot();
      defaultModel = new DefaultTreeModel((TreeNode)root);
      jTree.setSelectionRow(0);
      //createButtonGroup(actualRoot);
  }

  private void createButtonGroup(Object root)
  {   if (root != null)
      {   nodeLabel.setText(""+root);
          messageTextArea.append(resource.getString("selectedNode")+root+":\n");
          int num = model.getChildCount(root);
          nodePanel.removeAll();
          nodePanel.repaint();
          for(int i=0;i<num;i++)
          {   Object filho = model.getChild(root,i);
              boolean bool = model.isLeaf(filho);
              if (bool==true)
              {   JLabel noFolha = new JLabel();
                  noFolha.setForeground(Color.red);
                  noFolha.setText("       "+filho);
                  nodePanel.add(noFolha);
                  messageTextArea.append(resource.getString("leaf")+filho+"\n");
              }
              else
              {   addButton(""+filho);
              }
          }
          DefaultMutableTreeNode node = (DefaultMutableTreeNode)actualRoot;
          if (node.getParent()!=null)
          {   addButton(resource.getString("back"));
          }
      }
  }

  private void addButton(String buttonName)
  {   jRadioButton = new JRadioButton(buttonName,false);
      jRadioButton.addActionListener(new java.awt.event.ActionListener()
      {   public void actionPerformed(ActionEvent e)
          {   jRadioButton_actionPerformed(e);
          }
      });
      buttonGroup.add(jRadioButton);
      nodePanel.add(jRadioButton);
      if (buttonName.equals(resource.getString("back")))
      {   jRadioButton.setMnemonic(((Character)resource.getObject("backMnemonic")).charValue());
          try
          {   jRadioButton.setIcon(IconController.getInstance().getReturnIcon());
          }
          catch (Exception e)
          {}
      }
  }

  void jRadioButton_actionPerformed(ActionEvent e)
  {   if(e.getActionCommand().equals(resource.getString("back")))
      {   messageTextArea.append(resource.getString("return")+actualRoot+"\n\n");
          DefaultMutableTreeNode node = (DefaultMutableTreeNode)actualRoot;
          actualRoot = (Object)node.getParent();
          //createButtonGroup(actualRoot);
          if (node.getParent()!=null)
          {   controller = EXPAND;

              TreeNode[] nodes = defaultModel.getPathToRoot((TreeNode)actualRoot);
              TreePath path = new TreePath(nodes);
              jTree.expandPath(path);
              jTree.setSelectionPath(path);
              jTree.scrollPathToVisible(path);

              controller = DONT_EXPAND;
          }
      }
      else
      {   actualRoot = findUserObject(e.getActionCommand());
          messageTextArea.append("\t"+actualRoot+"\n\n");
          //createButtonGroup(actualRoot);
          if (!((TreeNode)actualRoot).isLeaf())
          {   controller = EXPAND;
              TreeNode[] nodes = defaultModel.getPathToRoot((TreeNode)actualRoot);
              TreePath path = new TreePath(nodes);
              jTree.expandPath(path);
              jTree.setSelectionPath(path);
              jTree.scrollPathToVisible(path);
              controller = DONT_EXPAND;
          }
      }
  }

  public Object findUserObject (String str)
  {   DefaultMutableTreeNode root = (DefaultMutableTreeNode)actualRoot;
      Enumeration e = root.breadthFirstEnumeration();
      while(e.hasMoreElements())
      {   Object node = e.nextElement();
          if(node.toString().equals(str))
              return node;
      }
      return null;
  }

  void jTree_treeWillCollapse(TreeExpansionEvent e) throws ExpandVetoException
  {   if (controller == DONT_EXPAND)
            throw new ExpandVetoException(e);
  }

  void jTree_treeWillExpand(TreeExpansionEvent e) throws ExpandVetoException
  {   if (controller == DONT_EXPAND)
            throw new ExpandVetoException(e);
  }

  private void expand(JTree arvore)
  {   for (int i = 0; i < arvore.getRowCount(); i++)
      {     arvore.expandRow(i);
      }
  }

  void jTree_valueChanged(TreeSelectionEvent e)
  {   controller = DONT_EXPAND;
      DefaultMutableTreeNode node = (DefaultMutableTreeNode)jTree.getLastSelectedPathComponent();
      if (node == null)
          return;
      else
      {   int[] num = jTree.getSelectionRows();
          if (num[0] == 0)
              messageTextArea.setText("");
          actualRoot = node;
          createButtonGroup(actualRoot);
      }
  }

}