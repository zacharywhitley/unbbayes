package unbbayes.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.tree.*;

import unbbayes.prs.*;
import unbbayes.prs.bn.*;
import unbbayes.util.*;

/**
 * @author M�rio Henrique Paes Vieira (mariohpv@bol.com.br)
 * @version $1.0 $ (04/11/2002)
 */

public class HierarchicDefinitionPanel extends JPanel
{
  private JSplitPane centerPanel;
  private JScrollPane descriptionScrollPane;
  private JScrollPane explanationScrollPane;
  private HierarchicTree hierarchicTree;
  private DefaultTreeModel model;
  private ProbabilisticNetwork net;
  private ImageIcon greenBallIcon;
  private HierarchicTree explanationTree;
  private NetWindow netWindow;
  private JPanel bottomPanel;
  private JPanel topPanel;
  private JButton deleteButton;
  private JButton renameButton;
  private JButton addFolderButton;
  private JToolBar jToolBar;
  private JButton expand;
  private JButton edit;
  private JButton collapse;
  private JLabel statusBar;
  private Node draggedDescriptionNode;
  private Node draggedExplanationNode;
  private DefaultMutableTreeNode selectedNode;

  /** Load resource file from this package */
  private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.gui.resources.GuiResources");

  public HierarchicDefinitionPanel(ProbabilisticNetwork net, NetWindow netWindow)
  {
    super();
    this.net = net;
    this.netWindow = netWindow;
    this.setLayout(new BorderLayout());

    jToolBar       = new JToolBar();
    jToolBar.setBorder(null);
    jToolBar.setFloatable(false);
    topPanel       = new JPanel(new GridLayout(0,1));
    centerPanel    = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    bottomPanel    = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 1));
    statusBar      = new JLabel("Defini��o da hierarquia");
    hierarchicTree = net.getHierarchicTree();

    greenBallIcon = new ImageIcon(getClass().getResource("/icons/green-ball.gif"));

    //cria bot�es que ser�o usados nos toolbars
    deleteButton        = new JButton(new ImageIcon(getClass().getResource("/icons/delete-folder.gif")));
    renameButton        = new JButton(new ImageIcon(getClass().getResource("/icons/rename-folder.gif")));
    addFolderButton     = new JButton(new ImageIcon(getClass().getResource("/icons/add-folder.gif")));
    expand              = new JButton(new ImageIcon(getClass().getResource("/icons/expand-nodes.gif")));
    edit                = new JButton(new ImageIcon(getClass().getResource("/icons/edit.gif")));
    collapse            = new JButton(new ImageIcon(getClass().getResource("/icons/contract-nodes.gif")));

    //seta tooltip para esses bot�es
    deleteButton.setToolTipText("Delete Folder");
    renameButton.setToolTipText("Rename Folder");
    addFolderButton.setToolTipText("Add Folder");
    expand.setToolTipText(resource.getString("expandToolTip"));
    edit.setToolTipText(resource.getString("editToolTip"));
    collapse.setToolTipText(resource.getString("collapseToolTip"));

    deleteButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        deleteButton_actionPerformed(e);
      }
    });

    renameButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        renameButton_actionPerformed(e);
      }
    });

    addFolderButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        addFolderButton_actionPerformed(e);
      }
    });

    expand.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        hierarchicTree.expandTree();
      }
    });

    edit.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        edit_actionPerformed(e);
      }
    });

    collapse.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        hierarchicTree.collapseTree();
      }
    });

    //trata os eventos de mouse para a �rvore de hierarquia
    hierarchicTree.addMouseListener(new MouseAdapter()
    {
      private int oldRow = -1;

      public void mousePressed(MouseEvent e)
      {
        int selRow = hierarchicTree.getRowForLocation(e.getX(), e.getY());
        explanationTree.clearSelection();
        if (selRow == -1)
        {
          return;
        }
        else
        {
          if ((oldRow != -1) && (selRow == oldRow))
          {
            hierarchicTree.clearSelection();
            oldRow = -1;
          }
          else
          {
            oldRow = selRow;
          }
        }
      }

      public void mouseReleased(MouseEvent e)
      {
        if (draggedExplanationNode!=null)
        {
          draggedExplanationNode.setInformationType(Node.DESCRIPTION_TYPE);
          ((DefaultTreeModel)explanationTree.getModel()).removeNodeFromParent(selectedNode);
          updateExplanationTree();
          draggedExplanationNode = null;
        }
        setCursor(Cursor.getDefaultCursor());
      }

      public void mouseEntered(MouseEvent e)
      {
        mouseReleased(e);
      }

    });

    //trata os eventos de mouse para a �rvore de hierarquia para movimento do mouse
    hierarchicTree.addMouseMotionListener(new MouseMotionListener()
    {
      public void mouseDragged(MouseEvent e)
      {
        selectedNode = (DefaultMutableTreeNode)hierarchicTree.getLastSelectedPathComponent();
        if (selectedNode != null)
        {
          Enumeration enum = selectedNode.breadthFirstEnumeration();
          while (enum.hasMoreElements())
          {
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)enum.nextElement();
            Node node = hierarchicTree.getNodeInformation(treeNode);
            if (node != null)
            {
              setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
              draggedDescriptionNode = node;
            }
          }
        }
      }

      public void mouseMoved(MouseEvent e)
      {}
    });

    //coloca bot�es no toolbar e edte no painel principal
    jToolBar.add(expand);
    jToolBar.add(collapse);

    jToolBar.addSeparator();

    jToolBar.add(addFolderButton, null);

    jToolBar.addSeparator();

    jToolBar.add(renameButton, null);
    jToolBar.add(deleteButton, null);

    jToolBar.addSeparator();

    jToolBar.add(edit, null);
    topPanel.add(jToolBar, BorderLayout.CENTER);

    //adiciona panels ao centerPanel
    descriptionScrollPane = new JScrollPane();
    explanationScrollPane = new JScrollPane();
    centerPanel.add(descriptionScrollPane,JSplitPane.TOP);
    centerPanel.add(explanationScrollPane,JSplitPane.BOTTOM);

    // Defini��o da �rvore de explana��o
    DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
    DefaultTreeModel explanationModel = new DefaultTreeModel(root);
    explanationTree = new HierarchicTree(explanationModel);

    DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
    renderer.setIcon(greenBallIcon);
    renderer.setClosedIcon(greenBallIcon);
    renderer.setLeafIcon(greenBallIcon);
    renderer.setOpenIcon(greenBallIcon);
    explanationTree.setCellRenderer(renderer);

    explanationScrollPane.getViewport().add(explanationTree, null);

    //trata os eventos de mouse para a �rvore de explana��o
    explanationTree.addMouseListener(new MouseAdapter()
    {
      private int oldRow = -1;

      public void mousePressed(MouseEvent e)
      {
        int selRow = explanationTree.getRowForLocation(e.getX(), e.getY());
        hierarchicTree.clearSelection();
        if (selRow == -1)
        {
          return;
        }
        else
        {
          if ((oldRow != -1) && (selRow == oldRow))
          {
            explanationTree.clearSelection();
            oldRow = -1;
          }
          else
          {
            oldRow = selRow;
          }
        }
      }

      public void mouseReleased(MouseEvent e)
      {
        if (draggedDescriptionNode!=null)
        {
          draggedDescriptionNode.setInformationType(Node.EXPLANATION_TYPE);
          model.removeNodeFromParent(selectedNode);
          updateExplanationTree();
          draggedDescriptionNode = null;
        }
        setCursor(Cursor.getDefaultCursor());
      }

      public void mouseEntered(MouseEvent e)
      {
        mouseReleased(e);
      }

    });

    //trata os eventos de mouse para a �rvore de hierarquia para movimento do mouse
    explanationTree.addMouseMotionListener(new MouseMotionListener()
    {
      public void mouseDragged(MouseEvent e)
      {
        selectedNode = (DefaultMutableTreeNode)explanationTree.getLastSelectedPathComponent();
        if (selectedNode != null)
        {
          Enumeration enum = selectedNode.breadthFirstEnumeration();
          while (enum.hasMoreElements())
          {
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)enum.nextElement();
            Node node = explanationTree.getNodeInformation(treeNode);
            if (node != null)
            {
              setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
              draggedExplanationNode = node;
            }
          }
        }
      }

      public void mouseMoved(MouseEvent e)
      {}
    });

    // adiciona o statusBar ao bottomPanel
    bottomPanel.add(statusBar);

    //adiciona containers para o contentPane
    this.add(topPanel, BorderLayout.NORTH);
    this.add(centerPanel, BorderLayout.CENTER);
    this.add(bottomPanel, BorderLayout.SOUTH);
    setVisible(true);

  }

  public void updateExplanationTree()
  {
    this.hierarchicTree = net.getHierarchicTree();

    hierarchicTree.setProbabilisticNetwork(net,HierarchicTree.DESCRIPTION_TYPE);
    model = (DefaultTreeModel)hierarchicTree.getModel();
    descriptionScrollPane.getViewport().removeAll();
    descriptionScrollPane.getViewport().add(hierarchicTree, null);

    explanationTree.setProbabilisticNetwork(net,HierarchicTree.EXPLANATION_TYPE);
    explanationScrollPane.getViewport().removeAll();
    explanationScrollPane.getViewport().add(explanationTree, null);
  }

  private void deleteButton_actionPerformed(ActionEvent e)
  {
    selectedNode = (DefaultMutableTreeNode)hierarchicTree.getLastSelectedPathComponent();
    if (selectedNode != null)
    {
      Enumeration enum = selectedNode.breadthFirstEnumeration();
      while (enum.hasMoreElements())
      {
        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)enum.nextElement();
        Node node = hierarchicTree.getNodeInformation(treeNode);
        if (node == null)
        {
          model.removeNodeFromParent(selectedNode);
          //updateExplanationTree();
        }
      }
    }
  }

  private void renameButton_actionPerformed(ActionEvent e)
  {
    selectedNode = (DefaultMutableTreeNode)hierarchicTree.getLastSelectedPathComponent();
    if (selectedNode != null)
    {
      disableButtons();
      String newNodeName = JOptionPane.showInternalInputDialog(this,"","New Folder Name",JOptionPane.QUESTION_MESSAGE);
      enableButtons();
      Node node = hierarchicTree.getNodeInformation(selectedNode);
      if ((node != null)&&(!newNodeName.equals("")))
      {
        node.setDescription(newNodeName);
      }
      selectedNode.setUserObject(newNodeName);
      model.reload(selectedNode);
    }
    selectedNode = (DefaultMutableTreeNode)explanationTree.getLastSelectedPathComponent();
    if (selectedNode != null)
    {
      disableButtons();
      String newNodeName = JOptionPane.showInternalInputDialog(this,"","New Folder Name",JOptionPane.QUESTION_MESSAGE);
      enableButtons();
      Node node = explanationTree.getNodeInformation(selectedNode);
      if ((node != null)&&(!newNodeName.equals("")))
      {
        node.setDescription(newNodeName);
      }
      selectedNode.setUserObject(newNodeName);
      model.reload((TreeNode)model.getRoot());
    }
  }

  private void addFolderButton_actionPerformed(ActionEvent e)
  {
    selectedNode = (DefaultMutableTreeNode)hierarchicTree.getLastSelectedPathComponent();
    Node node = hierarchicTree.getNodeInformation(selectedNode);
    if (node == null)
    {
      if (selectedNode == null)
      {
        disableButtons();
        String newNodeName = JOptionPane.showInternalInputDialog(this,"","Add Top Folder",JOptionPane.QUESTION_MESSAGE);
        enableButtons();
        if ((newNodeName != null)&&(!newNodeName.equals("")))
        {
          DefaultMutableTreeNode root = ((DefaultMutableTreeNode)model.getRoot());
          insertNewNode(root,newNodeName);
        }
      }
      else
      {
        String newNodeName = JOptionPane.showInternalInputDialog(this,"","Add Child Folder",JOptionPane.QUESTION_MESSAGE);
        if ((newNodeName != null)&&(!newNodeName.equals("")))
        {
          insertNewNode(selectedNode,newNodeName);
        }
      }
    }
  }

  private void disableButtons()
  {
    deleteButton.setEnabled(false);
    renameButton.setEnabled(false);
    addFolderButton.setEnabled(false);
    expand.setEnabled(false);
    edit.setEnabled(false);
    collapse.setEnabled(false);
  }

  private void enableButtons()
  {
    deleteButton.setEnabled(true);
    renameButton.setEnabled(true);
    addFolderButton.setEnabled(true);
    expand.setEnabled(true);
    edit.setEnabled(true);
    collapse.setEnabled(true);
  }

  private void insertNewNode(DefaultMutableTreeNode node,String nodeName)
  {
    DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(nodeName);
    model.insertNodeInto(newNode,node,node.getChildCount());
    showNewNode(newNode);
  }

  private void showNewNode(DefaultMutableTreeNode newNode)
  {   TreeNode[] nodes = model.getPathToRoot(newNode);
      TreePath path = new TreePath(nodes);
      hierarchicTree.scrollPathToVisible(path);
  }

  private void edit_actionPerformed(ActionEvent e)
  {   netWindow.changeToNetEdition();
  }
}