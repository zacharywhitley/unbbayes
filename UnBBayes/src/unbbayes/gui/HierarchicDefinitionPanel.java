package unbbayes.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.tree.*;

import unbbayes.prs.bn.*;
import unbbayes.util.*;

/**
 * @author Mário Henrique Paes Vieira (mariohpv@bol.com.br)
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
  private TreePath oldPath;
  private JTree explanationTree;
  private NetWindow netWindow;
  private JPanel bottomPanel;
  private JPanel topPanel;
  private JButton jButton6;
  private JButton jButton5;
  private JButton jButton4;
  private JToolBar jToolBar;
  private JButton expand;
  private JButton edit;
  private JButton collapse;
  private JLabel statusBar;

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
    statusBar      = new JLabel("Definição da hierarquia");

    greenBallIcon = new ImageIcon(getClass().getResource("/icons/green-ball.gif"));

    //cria botões que serão usados nos toolbars
    jButton6     = new JButton(new ImageIcon(getClass().getResource("/icons/delete-folder.gif")));
    jButton5     = new JButton(new ImageIcon(getClass().getResource("/icons/rename-folder.gif")));
    jButton4     = new JButton(new ImageIcon(getClass().getResource("/icons/add-folder.gif")));
    expand       = new JButton(new ImageIcon(getClass().getResource("/icons/expand-nodes.gif")));
    edit         = new JButton(new ImageIcon(getClass().getResource("/icons/edit.gif")));
    collapse     = new JButton(new ImageIcon(getClass().getResource("/icons/contract-nodes.gif")));

    //seta tooltip para esses botões
    jButton6.setToolTipText("Delete Folder");
    jButton5.setToolTipText("Rename Folder");
    jButton4.setToolTipText("Add Folder");
    expand.setToolTipText(resource.getString("expandToolTip"));
    edit.setToolTipText(resource.getString("editToolTip"));
    collapse.setToolTipText(resource.getString("collapseToolTip"));

    jButton6.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButton6_actionPerformed(e);
      }
    });

    jButton5.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButton5_actionPerformed(e);
      }
    });

    jButton4.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButton4_actionPerformed(e);
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

    //coloca botões no toolbar e edte no painel principal
    jToolBar.add(expand);
    jToolBar.add(collapse);

    jToolBar.addSeparator();

    jToolBar.add(jButton4, null);

    jToolBar.addSeparator();

    jToolBar.add(jButton5, null);
    jToolBar.add(jButton6, null);

    jToolBar.addSeparator();

    jToolBar.add(edit, null);
    topPanel.add(jToolBar, BorderLayout.CENTER);

    //adiciona panels ao centerPanel
    descriptionScrollPane = new JScrollPane();
    explanationScrollPane = new JScrollPane();
    centerPanel.add(descriptionScrollPane,JSplitPane.TOP);
    centerPanel.add(explanationScrollPane,JSplitPane.BOTTOM);

    // Definição da árvore de explanação
    DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
    DefaultTreeModel model = new DefaultTreeModel(root);
    explanationTree = new HierarchicTree(model);
    explanationTree.setRootVisible(false);
    explanationTree.setEditable(false);
    explanationTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

    DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
    renderer.setIcon(greenBallIcon);
    renderer.setClosedIcon(greenBallIcon);
    renderer.setLeafIcon(greenBallIcon);
    renderer.setOpenIcon(greenBallIcon);
    explanationTree.setCellRenderer(renderer);

    explanationScrollPane.getViewport().add(explanationTree, null);

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

    //trata os eventos de mouse para a árvore de hierarquia
    hierarchicTree.addMouseListener(new MouseAdapter()
    {
      public void mousePressed(MouseEvent e)
      {
        int selRow = hierarchicTree.getRowForLocation(e.getX(), e.getY());
        if (selRow == -1)
        {
          return;
        }
        else
        {
          TreePath selPath = hierarchicTree.getPathForLocation(e.getX(), e.getY());
          if (oldPath != null && oldPath.equals(selPath))
          {
            hierarchicTree.clearSelection();
            oldPath = null;
          }
          else
          {
            oldPath = selPath;
          }
        }
      }
    });

    model = (DefaultTreeModel)hierarchicTree.getModel();
    descriptionScrollPane.getViewport().removeAll();
    descriptionScrollPane.getViewport().add(hierarchicTree, null);

    NodeList explanationNodes = net.getExplanationNodes();
    int size = explanationNodes.size();
    DefaultMutableTreeNode root = (DefaultMutableTreeNode)explanationTree.getModel().getRoot();
    root.removeAllChildren();
    for(int i=0;i<size;i++)
    {
      Node node = explanationNodes.get(i);
      DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(node.getDescription());
      root.add(newNode);
    }
    explanationScrollPane.getViewport().removeAll();
    ((DefaultTreeModel)explanationTree.getModel()).reload(root);
    explanationScrollPane.getViewport().add(explanationTree, null);
  }

  void jButton6_actionPerformed(ActionEvent e)
  {   DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)hierarchicTree.getLastSelectedPathComponent();
      if (selectedNode == null)
          return;
      else
      {   model.removeNodeFromParent(selectedNode);
          Enumeration enum = selectedNode.breadthFirstEnumeration();
          while (enum.hasMoreElements())
          {   DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)enum.nextElement();
              Node node = hierarchicTree.getNodeInformation(treeNode);
              if (node != null)
              {   node.setInformationType(Node.EXPLANATION_TYPE);
              }
          }
          updateExplanationTree();
      }
  }

  void jButton5_actionPerformed(ActionEvent e)
  {   DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)hierarchicTree.getLastSelectedPathComponent();
      if (selectedNode == null)
          return;
      else
      {   String result = JOptionPane.showInternalInputDialog(this,"","New Folder Name",JOptionPane.QUESTION_MESSAGE);
          Node node = hierarchicTree.getNodeInformation(selectedNode);
          if (node != null)
          {   node.setDescription(result);
          }
          selectedNode.setUserObject(result);
          model.reload(selectedNode);
      }
  }

  void jButton4_actionPerformed(ActionEvent e)
  {   DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)hierarchicTree.getLastSelectedPathComponent();
      Node node = hierarchicTree.getNodeInformation(selectedNode);
      if (selectedNode == null && node == null)
      {   String result = JOptionPane.showInternalInputDialog(this,"","Add Top Folder",JOptionPane.QUESTION_MESSAGE);
          if ((result != null)&&(!result.equals("")))
          {   DefaultMutableTreeNode root = ((DefaultMutableTreeNode)model.getRoot());
              DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(result);
              model.insertNodeInto(newNode,root,root.getChildCount());
              showNewNode(newNode);
          }
      }
      else if (selectedNode != null && node == null)
      {   String result = JOptionPane.showInternalInputDialog(this,"","Add Child Folder",JOptionPane.QUESTION_MESSAGE);
          if ((result != null)&&(!result.equals("")))
          {   DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(result);
              model.insertNodeInto(newNode,selectedNode,selectedNode.getChildCount());
              showNewNode(newNode);
          }
      }
  }

  private void showNewNode(DefaultMutableTreeNode newNode)
  {   TreeNode[] nodes = model.getPathToRoot(newNode);
      TreePath path = new TreePath(nodes);
      hierarchicTree.scrollPathToVisible(path);
  }

  void edit_actionPerformed(ActionEvent e)
  {   netWindow.changeToNetEdition();
  }
}