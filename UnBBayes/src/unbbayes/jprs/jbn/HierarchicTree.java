package unbbayes.jprs.jbn;

import java.awt.Component;

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


      /*DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
      renderer.setClosedIcon(folderSmallIcon);
      renderer.setOpenIcon(folderSmallIcon);
      renderer.setLeafIcon(yellowBallIcon);
      this.setCellRenderer(renderer);*/

      this.setRootVisible(false);
      this.setEditable(true);
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
  {   /*DefaultMutableTreeNode root = (DefaultMutableTreeNode) getModel().getRoot();
      if (net != null)
      {   if (!net.equals(this.net))
          {   this.net = net;
              root.removeAllChildren();
              objectsMap.clear();
              NodeList nos = net.getNos();
              int size = nos.size();
              for (int i = 0; i < size; i++)
              {   Node node = (Node) nos.get(i);
                  DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(node.getDescription());
                  objectsMap.put(treeNode, node);
                  int statesSize = node.getStatesSize();
                  for (int j = 0; j < statesSize; j++)
                  {   DefaultMutableTreeNode stateNode = new DefaultMutableTreeNode(node.getStateAt(j) + (showProbability ? " " + nf.format(((TreeVariable)node).getMarginalAt(j) * 100.0) + "%" : ""));
                      treeNode.add(stateNode);
                      objectsMap.put(stateNode,new StateObject(j, CHECK_EMPTY));
                  }
                  root.add(treeNode);
              }
          }
      }
      else
      {   this.net = null;
          root.removeAllChildren();
          objectsMap.clear();
      }
      ((DefaultTreeModel)getModel()).reload(root);*/
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
}