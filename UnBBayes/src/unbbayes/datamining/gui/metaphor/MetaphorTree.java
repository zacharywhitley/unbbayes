package unbbayes.datamining.gui.metaphor;

import javax.swing.*;
import javax.swing.tree.*;
import unbbayes.jprs.jbn.*;
import unbbayes.util.*;

/**
 * @author Paulo F. Duarte
 */
public class MetaphorTree extends JTree {
	private class StateObject {
	    private int stateIndex = -1;
	    private int check = 0;
	    
	    public StateObject() {
	    }
	
	    public StateObject(Node node, int stateIndex, int check, boolean showProbability) {
	        this.node = node;
	        this.stateIndex = stateIndex;
	        this.check = check;
	        this.showProbability = showProbability;
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
	    
	    public String toString() {
	        if (node != null && stateIndex > -1) {
	            String result = node.getStateAt(stateIndex);
	            if (showProbability) {
	                result += ": " + NumberFormat.getInstance(Locale.US).format(((TreeVariable)node).getMarginalAt(stateIndex) * 100.0) + "%";
	            }
	            return result;
	        }
	        return "";
	    }
	}

    public static final int CHECK_YES = 1;
    public static final int CHECK_NO = -1;
    public static final int CHECK_EMPTY = 0;

	private DefaultMutableTreeNode root = null;
	private ProbabilisticNetwork net = null;
    boolean showProbability = false;

	public MetaphorTree() {
		super(new DefaultMutableTreeNode(null));
		root = (DefaultMutableTreeNode)getModel().getRoot();
        setShowsRootHandles(true);
        setSelectionModel(null);
        setRootVisible(false);
	}

	public MetaphorTree(ProbabilisticNetwork net) {
		this();
		setProbabilisticNetwork(net);
	}

	public void setProbabilisticNetwork(ProbabilisticNetwork net) {
		if (net != null && net.equals(this.net)) {
			this.net = net;
	        NodeList nos = net.getCopiaNos();
	        root.removeAllChildren();
	        for (int i = 0; i < nos.size(); i++) {
	            Node node = (Node) nos.get(i);
	            DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(node);
	            for (int j = 0; j < node.getStatesSize(); j++) {
	                treeNode.add(new DefaultMutableTreeNode(new StateObject(node, j, CHECK_EMPTY, true)));
	            }
	            root.add(treeNode);
	        }
	        ((DefaultTreeModel)getModel()).reload(root);
		}
	}

	public ProbabilisticNetwork getProbabilisticNetwork() {
		return net;
	}
}