package unbbayes.datamining.gui.metaphor;

import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;
import unbbayes.jprs.jbn.*;
import unbbayes.util.*;

/**
 * @author Paulo F. Duarte
 */
public class MetaphorTree extends JTree {
    public static final int CHECK_YES = 1;
    public static final int CHECK_NO = -1;
    public static final int CHECK_EMPTY = 0;

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

	private DefaultMutableTreeNode root = null;
	private ProbabilisticNetwork net = null;
    private boolean showProbability = false;
    private ArrayList rowObjectsList = new ArrayList();

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
			int row = 1;
	        NodeList nos = net.getCopiaNos();
	        root.removeAllChildren();
	        for (int i = 0; i < nos.size(); i++) {
	            Node node = (Node) nos.get(i);
	            DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(node);
	            for (int j = 0; j < node.getStatesSize(); j++) {
	                treeNode.add(new DefaultMutableTreeNode(new StateObject(j, CHECK_EMPTY)));
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