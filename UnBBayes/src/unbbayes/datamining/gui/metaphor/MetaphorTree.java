package unbbayes.datamining.gui.metaphor;

import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;

import sun.security.provider.JavaKeyStore;
import unbbayes.jprs.jbn.*;
import unbbayes.util.*;

/**
 * @author Paulo F. Duarte
 */
public class MetaphorTree extends JTree {
	private class StateObject {
	    private int stateIndex = -1;
	    private int check = CHECK_EMPTY;

	    public StateObject(int stateIndex, int check) {
	        this.stateIndex = stateIndex;
	        this.check = check;
	    }

	    public int getStateIndex() {
	        return stateIndex;
	    }

	    public void setStateIndex(int stateIndex) {
	        this.stateIndex = stateIndex;
	    }

	    public int getCheck() {
	        return check;
	    }

	    public void setCheck(int check) {
	        this.check = check;
	    }
	}

	public class MetaphorTreeCellRenderer extends javax.swing.tree.DefaultTreeCellRenderer {
	    ImageIcon yesIcon = new ImageIcon(getClass().getResource("/icones/yesState.gif"));
	    ImageIcon noIcon = new ImageIcon(getClass().getResource("/icones/noState.gif"));
	    ImageIcon emptyIcon = new ImageIcon(getClass().getResource("/icones/emptyState.gif"));

	    public java.awt.Component getTreeCellRendererComponent(JTree tree, Object value,
	                            boolean sel, boolean expanded, boolean leaf,
	                            int row, boolean hasFocus) {

	        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
	        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)value;
	        if (leaf) {
	            Object obj = objectsMap.get(treeNode);
	            if (obj instanceof StateObject) {
	                StateObject stateObject = (StateObject)obj;
	                int check = stateObject.getCheck();
	                setIcon((check == CHECK_YES) ? yesIcon : ((check == CHECK_NO) ? noIcon : emptyIcon));
	            }
	            else
	            {
	                setIcon(getClosedIcon());
	            }
	        }
	        return this;
	    }
	}

    public static final int CHECK_YES = 1;
    public static final int CHECK_NO = -1;
    public static final int CHECK_EMPTY = 0;

	private DefaultMutableTreeNode root = null;
	private ProbabilisticNetwork net = null;
    private boolean showProbability = false;
    private ArrayMap objectsMap = new ArrayMap();

	public MetaphorTree() {
		super(new DefaultMutableTreeNode(null));
		root = (DefaultMutableTreeNode)getModel().getRoot();
        setShowsRootHandles(true);
        setSelectionModel(null);
        setRootVisible(false);
        setCellRenderer(new MetaphorTreeCellRenderer());
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                methaphorTreeMouseClicked(evt);
            }
        });
	}

	public MetaphorTree(boolean showProbability) {
		this();
		this.showProbability = showProbability;
	}

	public MetaphorTree(ProbabilisticNetwork net) {
		this();
		setProbabilisticNetwork(net);
	}

	public MetaphorTree(ProbabilisticNetwork net, boolean showProbability) {
		this(net);
		this.showProbability = showProbability;
	}

	public void setProbabilisticNetwork(ProbabilisticNetwork net) {
		if (net != null) {
			if (!net.equals(this.net)) {
				this.net = net;
		        root.removeAllChildren();
		        objectsMap.clear();
		        NodeList nos = net.getCopiaNos();
		        for (int i = 0; i < nos.size(); i++) {
		            Node node = (Node) nos.get(i);
		            DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(node.getDescription());
		            objectsMap.put(treeNode, node);
		            for (int j = 0; j < node.getStatesSize(); j++) {
		            	DefaultMutableTreeNode stateNode = new DefaultMutableTreeNode(node.getStateAt(j) + (showProbability ? " " + ((TreeVariable)node).getMarginalAt(j) * 100.0 + "%" : ""));
		                treeNode.add(stateNode);
		            	objectsMap.put(stateNode,new StateObject(j, CHECK_EMPTY));
		            }
		            root.add(treeNode);
		        }
		        ((DefaultTreeModel)getModel()).reload(root);
			}
		}
		else {
			this.net = null;
	        root.removeAllChildren();
	        objectsMap.clear();
		}
	}

	public ProbabilisticNetwork getProbabilisticNetwork() {
		return net;
	}

	public void setShowProbability(boolean showProbability) {
		if (showProbability != this.showProbability) {
			this.showProbability = showProbability;
			ProbabilisticNetwork temp = net;
			setProbabilisticNetwork(null);
			setProbabilisticNetwork(temp);
		}
	}

	public boolean getShowProbability() {
		return showProbability;
	}

	public void propagate() {
		int count = getRowCount();
		for (int i = 0; i < count; i++) {
			TreePath path = getPathForRow(i);
			DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)path.getLastPathComponent();
			Object obj = objectsMap.get(treeNode);
			if (obj instanceof StateObject) {
				StateObject stateObject = (StateObject)obj;
				PotentialTable table = ((ITabledVariable)objectsMap.get(treeNode.getParent())).getPotentialTable();
				if (stateObject.getCheck() == CHECK_YES) {
					table.setValue(stateObject.getStateIndex(), 1);
				}
				else if(stateObject.getCheck() == CHECK_NO) {
					table.setValue(stateObject.getStateIndex(), 0);
				}
			}
		}
		try {
			net.updateEvidences();
		}
        catch (Exception e) {
            System.err.print(e.getMessage());
            e.printStackTrace();
        }
		if (showProbability) {
			ProbabilisticNetwork temp = net;
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
}