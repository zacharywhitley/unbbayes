package unbbayes.prs.bn;

import java.awt.*;
import java.awt.Component;
import java.awt.dnd.*;
import java.awt.datatransfer.*;

import java.io.IOException;

import java.util.*;

import javax.swing.*;
import javax.swing.tree.*;

import unbbayes.prs.*;

import unbbayes.util.*;

/**
 * @author Mário Henrique Paes Vieira
 * @version 1.0
 */

public class HierarchicTree extends JTree implements DropTargetListener, DragSourceListener, DragGestureListener
{
  private Network net;
  private NodeList nodes;
  private ArrayMap objectsMap = new ArrayMap();
  public static final boolean EXPLANATION_TYPE = true;
  public static final boolean DESCRIPTION_TYPE = false;
  /** enables this component to be a dropTarget */
  private DropTarget dropTarget = null;
  /** enables this component to be a Drag Source */
  private DragSource dragSource = null;

  public HierarchicTree(DefaultTreeModel model)
  {   super(model);

      // set up node icons
      setCellRenderer(new HierarchicTreeCellRenderer());

      // initializes the DropTarget and DragSource.
      dropTarget = new DropTarget (this, this);
      dragSource = new DragSource();
      dragSource.createDefaultDragGestureRecognizer( this, DnDConstants.ACTION_MOVE, this);

      // initializes other features
      this.setRootVisible(false);
      this.setEditable(false);
      this.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
  }

  private class HierarchicTreeCellRenderer extends DefaultTreeCellRenderer
  {   private ImageIcon folderSmallIcon = new ImageIcon(getClass().getResource("/icons/folder-small.gif"));
      private ImageIcon yellowBallIcon = new ImageIcon(getClass().getResource("/icons/yellow-ball.gif"));
      private ImageIcon greenBallIcon = new ImageIcon(getClass().getResource("/icons/green-ball.gif"));

      public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
      {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        if (leaf)
        {
          Object obj = objectsMap.get((DefaultMutableTreeNode)value);
          if (obj != null)
          {
            Node node = (Node)obj;
            if (node.getInformationType()==Node.DESCRIPTION_TYPE)
            {
              setIcon(yellowBallIcon);
            }
            else
            {
              setIcon(greenBallIcon);
            }

          }
          else
          {
            setIcon(folderSmallIcon);
          }
        }
        else
        {   this.setOpenIcon(folderSmallIcon);
            this.setClosedIcon(folderSmallIcon);
        }
        return this;
      }
  }

  public void setProbabilisticNetwork(Network net,boolean nodeType)
  {   DefaultMutableTreeNode root = (DefaultMutableTreeNode) getModel().getRoot();

      if (net != null)
      {
        this.net = net;
        objectsMap.clear();
        NodeList nos;
        if (nodeType == EXPLANATION_TYPE)
        {
          nos = net.getExplanationNodes();
        }
        else
        {
          nos = net.getDescriptionNodes();
        }
        int size = nos.size();
        for (int i = 0; i < size; i++)
        {
          Node node = (Node) nos.get(i);
          DefaultMutableTreeNode treeNode = findUserObject(node.getDescription(),root);
          if (treeNode != null)
          {
            objectsMap.put(treeNode, node);
          }
          else
          {
            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(node.getDescription());
            objectsMap.put(newNode, node);
            root.add(newNode);
          }
        }
        ((DefaultTreeModel)getModel()).reload(root);
      }
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

    public JTree copyTree()
    {
      Stack stack = new Stack();
      DefaultMutableTreeNode root = (DefaultMutableTreeNode)getModel().getRoot();
      DefaultMutableTreeNode newRoot = new DefaultMutableTreeNode(root.toString());
      JTree jTree = new JTree(newRoot);
      stack.push(root);
      int i;
      while (!stack.empty())
      {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)stack.pop();
        int size = node.getChildCount();
        TreeNode child;
        for (i=0;i<size;i++)
        {
          child = node.getChildAt(i);
          DefaultMutableTreeNode mutableTreeNode = findUserObject(node.toString(),newRoot);
          if (mutableTreeNode != null)
            ((DefaultTreeModel)jTree.getModel()).insertNodeInto(new DefaultMutableTreeNode(child.toString()),mutableTreeNode,mutableTreeNode.getChildCount());
          stack.push(child);
        }
      }
      return jTree;
    }

    public Node getNodeInformation(DefaultMutableTreeNode treeNode)
    {   return (Node)objectsMap.get(treeNode);
    }

    /** is invoked when you are dragging over the DropSite */
    public void dragEnter (DropTargetDragEvent event)
    {
      event.acceptDrag (DnDConstants.ACTION_MOVE);
    }

    /** is invoked when you are exit the DropSite without dropping */
    public void dragExit (DropTargetEvent event)
    {}

    /** is invoked when a drag operation is going on */
    public void dragOver (DropTargetDragEvent event)
    {}

    /** a drop has occurred */
    public void drop (DropTargetDropEvent event)
    {
      try
      {
        Transferable transferable = event.getTransferable();

        if (transferable.isDataFlavorSupported (DataFlavor.stringFlavor))
        {
          String s = (String)transferable.getTransferData ( DataFlavor.stringFlavor);
          Node draggedNode = net.getNode(s);
          if (draggedNode != null)
          {
            DefaultMutableTreeNode root = (DefaultMutableTreeNode) getModel().getRoot();
            DefaultMutableTreeNode treeNode = findUserObject(draggedNode.getDescription(),root);
            if (treeNode == null)
            {
              event.acceptDrop(DnDConstants.ACTION_MOVE);
              int informationType = draggedNode.getInformationType();
              if (informationType == Node.DESCRIPTION_TYPE)
              {
                draggedNode.setInformationType(Node.EXPLANATION_TYPE);
              }
              else
              {
                draggedNode.setInformationType(Node.DESCRIPTION_TYPE);
              }
              Point d = event.getLocation();
              TreePath treePath = getPathForLocation((int)d.getX(),(int)d.getY());
              if (treePath != null)
              {
                root = (DefaultMutableTreeNode)treePath.getLastPathComponent();
              }
              Node dum = getNodeInformation(root);
              DefaultTreeModel model = (DefaultTreeModel)getModel();
              DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(draggedNode.getDescription());
              objectsMap.put(newNode, draggedNode);
              if (dum != null)
              {
                root = (DefaultMutableTreeNode)root.getParent();
              }
              root.add(newNode);
              model.reload(root);
              event.getDropTargetContext().dropComplete(true);
            }
          }
          else
          {
            event.rejectDrop();
          }
        }
      }
      catch (IOException ioe)
      {
        ioe.printStackTrace();
        System.err.println( "Exception" + ioe.getMessage());
        event.rejectDrop();
      }
      catch (UnsupportedFlavorException ufe )
      {
        ufe.printStackTrace();
        System.err.println( "Exception" + ufe.getMessage());
        event.rejectDrop();
      }
    }

    /** is invoked if the use modifies the current drop gesture */
    public void dropActionChanged ( DropTargetDragEvent event )
    {}

    /** a drag gesture has been initiated */
    public void dragGestureRecognized( DragGestureEvent event)
    {
      DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)getLastSelectedPathComponent();
      if (selectedNode != null)
      {
        Node node = getNodeInformation(selectedNode);
        if (node != null)
        {
          StringSelection text = new StringSelection(node.getName());
          // as the name suggests, starts the dragging
          dragSource.startDrag (event, DragSource.DefaultMoveDrop, text, this);
        }
      }
    }

    /** this message goes to DragSourceListener, informing it that the dragging
        has ended */
    public void dragDropEnd (DragSourceDropEvent event)
    {
      if ( event.getDropSuccess())
      {
        ((DefaultTreeModel)getModel()).removeNodeFromParent((DefaultMutableTreeNode)getLastSelectedPathComponent());
      }
    }

    /** this message goes to DragSourceListener, informing it that the dragging
        has entered the DropSite */
    public void dragEnter (DragSourceDragEvent event)
    {}

    /** this message goes to DragSourceListener, informing it that the dragging
        has exited the DropSite */
    public void dragExit (DragSourceEvent event)
    {}

    /** this message goes to DragSourceListener, informing it that the dragging is currently
        ocurring over the DropSite */
    public void dragOver (DragSourceDragEvent event)
    {}

    /** is invoked when the user changes the dropAction */
    public void dropActionChanged ( DragSourceDragEvent event)
    {}
}