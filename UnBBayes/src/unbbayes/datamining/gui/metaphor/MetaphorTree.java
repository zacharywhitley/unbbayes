package unbbayes.datamining.gui.metaphor;

import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import java.util.*;

import javax.swing.*;
import javax.swing.tree.*;

import unbbayes.jprs.jbn.*;
import unbbayes.util.*;

/**
 * @author Paulo F. Duarte
 */
public class MetaphorTree extends JTree
{
	private class StateObject
        {   private int stateIndex = -1;
	    private int check = CHECK_EMPTY;

	    public StateObject(int stateIndex, int check)
            {   this.stateIndex = stateIndex;
	        this.check = check;
	    }

	    public int getStateIndex()
            {   return stateIndex;
	    }

	    public void setStateIndex(int stateIndex)
            {   this.stateIndex = stateIndex;
	    }

	    public int getCheck()
            {   return check;
	    }

	    public void setCheck(int check)
            {   this.check = check;
	    }
	}

	public class MetaphorTreeCellRenderer extends javax.swing.tree.DefaultTreeCellRenderer
        {   ImageIcon yesIcon = new ImageIcon(getClass().getResource("/icones/yesState.gif"));
	    ImageIcon noIcon = new ImageIcon(getClass().getResource("/icones/noState.gif"));
	    ImageIcon emptyIcon = new ImageIcon(getClass().getResource("/icones/emptyState.gif"));
            ImageIcon evidenciasIcon = new ImageIcon(getClass().getResource("/icones/evidencias.gif"));

	    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
            {   super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
	        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)value;
	        if (leaf)
                {   Object obj = objectsMap.get(treeNode);
	            if (obj instanceof StateObject)
                    {   StateObject stateObject = (StateObject)obj;
	                int check = stateObject.getCheck();
	                setIcon((check == CHECK_YES) ? yesIcon : ((check == CHECK_NO) ? noIcon : emptyIcon));
	            }
	        }
                else
                {   this.setOpenIcon(evidenciasIcon);
                    this.setClosedIcon(evidenciasIcon);
                }
	        return this;
	    }
	}

        public static final int CHECK_YES = 1;
        public static final int CHECK_NO = -1;
        public static final int CHECK_EMPTY = 0;

	//private DefaultMutableTreeNode root = null;
	private ProbabilisticNetwork net = null;
        private boolean showProbability = false;
        private ArrayMap objectsMap = new ArrayMap();
        private NumberFormat nf;

	protected MetaphorTree()
        {   //super();
            /*super(new DefaultMutableTreeNode());
            root = (DefaultMutableTreeNode)getModel().getRoot();*/
            setShowsRootHandles(true);
            setSelectionModel(null);
            setRootVisible(false);
            this.setAutoscrolls(true);
            setCellRenderer(new MetaphorTreeCellRenderer());
            addMouseListener(new MouseAdapter()
            {   public void mouseClicked(java.awt.event.MouseEvent evt)
                {   methaphorTreeMouseClicked(evt);
                }
            });
            nf = NumberFormat.getInstance(Locale.US);
            nf.setMaximumFractionDigits(4);
	}

	public MetaphorTree(ProbabilisticNetwork net)
        {	this(net,false);
	}

	public MetaphorTree(ProbabilisticNetwork net, boolean showProbability)
        {	this();
		this.showProbability = showProbability;
                setProbabilisticNetwork(net);
	}

	public void setProbabilisticNetwork(ProbabilisticNetwork net)
        {	DefaultMutableTreeNode root = (DefaultMutableTreeNode) getModel().getRoot();
                if (net != null)
                {	if (!net.equals(this.net))
                        {	this.net = net;
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
                {	this.net = null;
	                root.removeAllChildren();
	                objectsMap.clear();
                        System.out.println("problem");
		}
                ((DefaultTreeModel)getModel()).reload(root);
	}

	public ProbabilisticNetwork getProbabilisticNetwork() {
		return net;
	}

	public void setShowProbability(boolean showProbability)
        {	if (showProbability != this.showProbability)
                {	this.showProbability = showProbability;
			ProbabilisticNetwork temp = net;
			setProbabilisticNetwork(null);
			setProbabilisticNetwork(temp);
		}
	}

	public boolean getShowProbability()
        {	return showProbability;
	}

	public void propagate()
        {	int count = getRowCount();
		for (int i = 0; i < count; i++)
                {	TreePath path = getPathForRow(i);
			DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)path.getLastPathComponent();
			Object obj = objectsMap.get(treeNode);
			if (obj instanceof StateObject)
                        {	StateObject stateObject = (StateObject)obj;
				PotentialTable table = ((ITabledVariable)objectsMap.get(treeNode.getParent())).getPotentialTable();
				if (stateObject.getCheck() == CHECK_YES)
                                {	table.setValue(stateObject.getStateIndex(), 1);
				}
				else if(stateObject.getCheck() == CHECK_NO)
                                {	table.setValue(stateObject.getStateIndex(), 0);
				}
			}
		}
		try
                {	net.updateEvidences();
		}
                catch (Exception e)
                {   System.err.print(e.getMessage());
                    e.printStackTrace();
                }
		if (showProbability)
                {	ProbabilisticNetwork temp = net;
			setProbabilisticNetwork(null);
			setProbabilisticNetwork(temp);
		}
	}

	private void methaphorTreeMouseClicked(java.awt.event.MouseEvent evt) {
        TreePath clickedPath = getPathForLocation(evt.getX(), evt.getY());
        if (clickedPath != null) {
            DefaultMutableTreeNode clickedNode = (DefaultMutableTreeNode)(clickedPath.getLastPathComponent());
            if (clickedNode != null && clickedNode.isLeaf()) {
                Object obj = objectsMap.get(clickedNode);
                if (obj instanceof StateObject) {
                    DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode)clickedNode.getParent();
                    Enumeration childrenEnum = parentNode.children();
                    StateObject yesChecked = null;
                    ArrayList noCheckeds = new ArrayList(),
                              emptyCheckeds = new ArrayList();
                    while (childrenEnum.hasMoreElements()) {
                        DefaultMutableTreeNode child = (DefaultMutableTreeNode)childrenEnum.nextElement();
                        if (!child.equals(clickedNode)) {
                            if (((StateObject)objectsMap.get(child)).getCheck() == CHECK_YES) {
                                yesChecked = (StateObject)objectsMap.get(child);
                            }
                            else if (((StateObject)objectsMap.get(child)).getCheck() == CHECK_NO) {
                                noCheckeds.add(objectsMap.get(child));
                            }
                            else {
                                emptyCheckeds.add(objectsMap.get(child));
                            }
                        }
                    }
                    if (SwingUtilities.isLeftMouseButton(evt)) {
                        if (((StateObject)obj).getCheck() == CHECK_YES) {
                            ((StateObject)obj).setCheck(CHECK_EMPTY);
                            for (int i = 0; i < noCheckeds.size(); i++) {
                                ((StateObject)noCheckeds.get(i)).setCheck(CHECK_EMPTY);
                            }
                        }
                        else {
                            ((StateObject)obj).setCheck(CHECK_YES);
                            if (yesChecked != null) {
                                yesChecked.setCheck(CHECK_NO);
                            }
                            for (int i = 0; i < emptyCheckeds.size(); i++) {
                                ((StateObject)emptyCheckeds.get(i)).setCheck(CHECK_NO);
                            }
                        }
                    }
                    if (SwingUtilities.isRightMouseButton(evt)) {
                        if (((StateObject)obj).getCheck() == CHECK_NO) {
                            ((StateObject)obj).setCheck(CHECK_EMPTY);
                            if (yesChecked != null) {
                                yesChecked.setCheck(CHECK_EMPTY);
                            }
                        }
                        else if (noCheckeds.size() < (parentNode.getChildCount() - 1)) {
                            ((StateObject)obj).setCheck(CHECK_NO);
                            if (noCheckeds.size() == (parentNode.getChildCount() - 2)) {
                                ((StateObject)emptyCheckeds.get(0)).setCheck(CHECK_YES);
                            }
                        }
                    }
                    repaint();
                }
            }
        }
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

    /**
     * Modifica o formato de números
     *
     * @param local localidade do formato de números.
     */
    public void setNumberFormat(Locale local)
    {   nf = NumberFormat.getInstance(local);
    }

}