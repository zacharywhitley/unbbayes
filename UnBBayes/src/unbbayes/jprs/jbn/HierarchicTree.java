package unbbayes.jprs.jbn;

import java.awt.Component;
import java.util.*;

import javax.swing.*;
import javax.swing.tree.*;

import unbbayes.jprs.jbn.*;
import unbbayes.util.*;

/**
 * @author Mário Henrique Paes Vieira
 * @version 1.0
 */

public class HierarchicTree extends JTree
{ private ProbabilisticNetwork net;
  private ArrayMap objectsMap = new ArrayMap();

  public HierarchicTree(DefaultTreeModel model)
  {   super(model);

      // set up node icons
      setCellRenderer(new HierarchicTreeCellRenderer());

      this.setRootVisible(false);
      this.setEditable(true);
      this.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
  }

  public class HierarchicTreeCellRenderer extends DefaultTreeCellRenderer
  {   private ImageIcon folderSmallIcon = new ImageIcon(getClass().getResource("/icones/folderSmall.gif"));
      private ImageIcon yellowBallIcon = new ImageIcon(getClass().getResource("/icones/yellow-ball.gif"));

      public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
      {   super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
          if (leaf)
          {   DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)value;
              Object obj = objectsMap.get(treeNode);
              if (obj instanceof Node)
              {   setIcon(yellowBallIcon);
              }
              else
              {   setIcon(folderSmallIcon);
              }
          }
          else
          {   this.setOpenIcon(folderSmallIcon);
              this.setClosedIcon(folderSmallIcon);
          }
          return this;
      }
  }

  public void setProbabilisticNetwork(ProbabilisticNetwork net)
  {   DefaultMutableTreeNode root = (DefaultMutableTreeNode) getModel().getRoot();

      if (net != null)
      {   if (!net.equals(this.net))
          {   this.net = net;
              objectsMap.clear();
              NodeList nos = net.getDescriptionNodes();
              int size = nos.size();
              for (int i = 0; i < size; i++)
              {   Node node = (Node) nos.get(i);
                  DefaultMutableTreeNode treeNode = findUserObject(node.getDescription(),root);
                  if (treeNode != null)
                  {   objectsMap.put(treeNode, node);
                  }
                  else
                  {   DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(node.getDescription());
                      objectsMap.put(newNode, node);
                      root.add(newNode);
                  }
              }
          }
      }
      ((DefaultTreeModel)getModel()).reload(root);
  }

  private DefaultMutableTreeNode findUserObject(String treeNode,DefaultMutableTreeNode root)
  {   Enumeration e = root.breadthFirstEnumeration();
      while (e.hasMoreElements())
      {   DefaultMutableTreeNode node = (DefaultMutableTreeNode)e.nextElement();
          if (node.getUserObject().toString().equals(treeNode))
              return node;
      }
      return null;
  }

  /**
     *  Expande todos os nós da árvore.
     *
     * @see            JTree
     */
    public void expandTree()
    {   for (int i = 0; i < getRowCount(); i++)
        {   expandRow(i);
        }
    }

    /**
     *  Retrai todos os nós da árvore.
     *
     * @see            JTree
     */
    public void collapseTree()
    {   for (int i = 0; i < getRowCount(); i++)
        {   collapseRow(i);
        }
    }

    public Node getNodeInformation(DefaultMutableTreeNode treeNode)
    {   return (Node)objectsMap.get(treeNode);
    }
}