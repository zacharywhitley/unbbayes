package unbbayes.prs.bn.valueTree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import unbbayes.prs.INode;
import unbbayes.prs.Node.NodeNameChangedEvent;
import unbbayes.prs.Node.NodeNameChangedListener;

/**
 * This is the default implementation of {@link IValueTree}
 * @author Shou Matsumoto
 */
public class ValueTree implements IValueTree {

	/** Error margin used in probability comparison */
	public static final float PROB_ERROR_MARGIN = 0.00001f;
	
	/** The children of root */
	private List<IValueTreeNode> children = new ArrayList<IValueTreeNode>();
	/** All nodes */
	private List<IValueTreeNode> nodes = new ArrayList<IValueTreeNode>();
	private INode root =null;
	private float initialAssets;
	
	private boolean isToChangeAssets = false;
	
	private Map<String, Integer> nameIndex = null;
	
	private List<IValueTreeNode> shadowNodeList = null;

	/**
	 * Default constructor is made protected to restrict access and
	 * yet allow inheritance.
	 */
	protected ValueTree() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * This is the default constructor method.
	 * @param root : will represent the root of this value tree, which is the node in BN itself.
	 * The states of this node are the states in this value tree which is exposed to other nodes in
	 * the BN.
	 * @return a new instance of the value tree.
	 * @see #getRoot()
	 */
	public static IValueTree getInstance (INode root) {
		ValueTree ret = new ValueTree();
		ret.setRoot(root);
		return ret;
	}
	

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.IValueTree#getName()
	 */
	public String getName() {
		return this.getRoot().getName();
	}



	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.IValueTree#getRoot()
	 */
	public INode getRoot() {
		return this.root;
	}

	/**
	 * @see unbbayes.prs.bn.valueTree.IValueTree#addNode(java.lang.String, unbbayes.prs.bn.valueTree.IValueTreeNode, float)
	 */
	public int addNode(String nameOfNodeToAdd, IValueTreeNode parent, float faction) {
		if (faction < 0 || faction > 1) {
			throw new IllegalArgumentException(faction + " is not a valid faction (conditional probability given parent).");
		}
		
		
		// listener which updates index of names when names are changed
		List<NodeNameChangedListener> nameChangeListeners = new ArrayList();
		nameChangeListeners.add(new NodeNameChangedListener() {
			public void nodeNameChanged(NodeNameChangedEvent event) {
				// extract index of names, so that we can reset it
				Map<String, Integer> map = getNameIndex();
				if ( map == null ) {
					// instantiate if it was never instantiated previously
					map = new HashMap<String, Integer>();
					setNameIndex(map);
//				} else {
//					// reset if instance already exists.
//					map.clear();
				}
				
				Integer index = map.remove(event.getOldName());
				if (index != null) {
					map.put(event.getNewName(), index);
				} else {
					throw new IllegalStateException("The index of the name of node " + event.getOldName() + " not found in " + this + ", so the name could not be changed to " + event.getNewName());
				}
				
			}
		});
		
		// instantiate the new node
		final IValueTreeNode newVTNode = ValueTreeNode.getInstance(nameOfNodeToAdd, this, nameChangeListeners);
		newVTNode.setFaction(faction);
		newVTNode.setParent(parent);	// Note: parent == null means this node shall be connected to root.
		
		
		// Set the new node as child of the specified parent (if null, then parent is the root). Also adjust the factions of other nodes proportionally
		List<IValueTreeNode> whereToAddChild = null;
		if (parent == null) {
			// make sure the list of children of root is initialized
			if (get1stLevelNodes() == null) {
				set1stLevelNodes(new ArrayList<IValueTreeNode>(1));
			}
			whereToAddChild = get1stLevelNodes();
			
			
		} else {
			// make sure the list of children of parent is initialized
			if (parent.getChildren() == null) {
				parent.setChildren(new ArrayList<IValueTreeNode>(1));
			}
			whereToAddChild = parent.getChildren();
			
		}
		// adapt faction
		if (whereToAddChild.isEmpty()) {
			// in this case, the new node is the only child, so its faction is actually 100%
			newVTNode.setFaction(1f);
		} else {
			// in this case, the faction of other children shall be adapted proportionally
			float factionComplement = 1f-faction;
			for (IValueTreeNode child : whereToAddChild) {
				// if faction of new node is 100%, then the other states will be set to 0%
				// otherwise, adapt proportionally so that the sum of other states is 1-faction
				child.setFaction((faction>=1f)?0f:(child.getFaction()*factionComplement));
			}
		}
		
		whereToAddChild.add(newVTNode);
		
		// add to the end of the list of all nodes
		if (nodes == null) {
			nodes = new ArrayList<IValueTreeNode>();
		}
		nodes.add(newVTNode);
		
		// update index of names
		if (getNameIndex() == null) {
			setNameIndex(new HashMap<String, Integer>());
		}
		getNameIndex().put(newVTNode.getName(), nodes.size()-1);
		
		return nodes.size()-1;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.IValueTree#getIndex(java.lang.String)
	 */
	public int getIndex(String nodeName) {
		if (nameIndex != null) {
			Integer index = nameIndex.get(nodeName);
			if (index != null) {
				return index;
			}
		}
		return -1;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.IValueTree#getNodes()
	 */
	public List<IValueTreeNode> getNodes() {
		return new ArrayList<IValueTreeNode>(nodes);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.IValueTree#getNode(java.lang.String)
	 */
	public IValueTreeNode getNode(String name) {
		int index = this.getIndex(name);
		if (index < 0) {
			return null;
		}
		return this.getNodes().get(index);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.IValueTree#getIndex(unbbayes.prs.bn.IValueTreeNode)
	 */
	public int getIndex(IValueTreeNode node) {
		if (node == null || node.getName() == null) {
			return -1;
		}
		// getIndex(String) is supposedly using a hash map, so it's supposedly faster than ArrayList#getIndex(Object), which is a linear search.
		return this.getIndex(node.getName());
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.IValueTree#getProb(unbbayes.prs.bn.IValueTreeNode, unbbayes.prs.bn.IValueTreeNode)
	 */
	public float getProb(IValueTreeNode node, IValueTreeNode anchor) {
		// the probability is the product of the factions in the path between anchor and node.
		float ret = 1f;
		while (node != null && !node.equals(anchor)) {
			// multiply the factions from node to anchor (anchor is supposedly null - root - or above node)
			ret *= node.getFaction();
			node = node.getParent();
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.IValueTree#changeProb(unbbayes.prs.bn.IValueTreeNode, unbbayes.prs.bn.IValueTreeNode, float)
	 */
	public float changeProb(IValueTreeNode node, IValueTreeNode anchor, float prob) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented yet");
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.IValueTree#getHighestRelativeSet(unbbayes.prs.bn.IValueTreeNode, unbbayes.prs.bn.IValueTreeNode)
	 */
	public List<IValueTreeNode> getHighestRelativeSet(IValueTreeNode node, IValueTreeNode anchor) {
		// the list to return
		List<IValueTreeNode> ret = new ArrayList<IValueTreeNode>();
		
		// go up in the hierarchy until we reach anchor (or root)
		while (node != null && !node.equals(anchor)) {
			
			// add siblings to the list to return
			if (node.getParent() != null && node.getParent().getChildren() != null) {
				for (IValueTreeNode sibling : node.getParent().getChildren()) {
					if (!node.equals(sibling)) {
						ret.add(sibling);
					}
				}
			}
			
			node = node.getParent();
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.IValueTree#initAssets(unbbayes.prs.bn.IValueTreeNode)
	 */
	public float initAssets(IValueTreeNode node) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented yet");
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.IValueTree#setInitialAssets(float)
	 */
	public void setInitialAssets(float initialAssets) {
		this.initialAssets = initialAssets;
	}

	/**
	 * @param root the root to set
	 */
	public void setRoot(INode root) {
		this.root = root;
	}

	/**
	 * @return the initialAssets
	 */
	public float getInitialAssets() {
		return this.initialAssets;
	}

	/**
	 * @param nodes the nodes to set
	 * @see #getNodes()
	 * @see #getNode(String)
	 * @see #getIndex(String)
	 */
	protected void setNodes(List<IValueTreeNode> nodes) {
		this.nodes = nodes;
	}

	/**
	 * @see unbbayes.prs.bn.valueTree.IValueTree#get1stLevelNodes()
	 */
	public List<IValueTreeNode> get1stLevelNodes() {
		return children;
	}


	/**
	 * @param children the children to set
	 */
	protected void set1stLevelNodes(List<IValueTreeNode> children) {
		this.children = children;
	}

	/**
	 * @return the nameIndex
	 * @see #getIndex(String)
	 */
	protected Map<String, Integer> getNameIndex() {
		return nameIndex;
	}

	/**
	 * @param nameIndex the nameIndex to set
	 * @see #getIndex(String)
	 */
	protected void setNameIndex(Map<String, Integer> nameIndex) {
		this.nameIndex = nameIndex;
	}

	/**
	 * @return if true, then {@link #changeProb(IValueTreeNode, IValueTreeNode, float)} will also attempt to change assets
	 */
	public boolean isToChangeAssets() {
		return isToChangeAssets;
	}

	/**
	 * @param isToChangeAssets the isToChangeAssets to set
	 */
	public void setToChangeAssets(boolean isToChangeAssets) {
		this.isToChangeAssets = isToChangeAssets;
	}

	/**
	 * @see unbbayes.prs.bn.valueTree.IValueTree#deleteNode(unbbayes.prs.bn.valueTree.IValueTreeNode)
	 */
	public int deleteNode(IValueTreeNode nodeToDelete) {
		return this.deleteNode(nodeToDelete, true);
	}
	
	/**
	 * Does the same of {@link #deleteNode(IValueTreeNode)},
	 * but here we can specify whether we want to rebuild mapping of names {@link #getNameIndex()}
	 * @param nodeToDelete : node to be deleted
	 * @param isToRebuildIndexMap : if true, then {@link #getNameIndex()} will be rebuild
	 * @return index of nodeToDelete before it was deleted.
	 */
	protected int deleteNode(IValueTreeNode nodeToDelete, boolean isToRebuildIndexMap) {
		int ret = getIndex(nodeToDelete);
		if (ret < 0) {
			return ret;
		}
		
		// delete from lists managed by this class
		if (nodes != null) {
			nodes.remove(ret);
		}
		if (shadowNodeList != null) {
			shadowNodeList.remove(ret);
		}
		
		// disconnect from parent
		if (nodeToDelete.getParent() != null) {
			if (nodeToDelete.getParent().getChildren() != null) {
				nodeToDelete.getParent().getChildren().remove(nodeToDelete);
			}
		}
		
		// also delete all descendants of the deleted node
		if (nodeToDelete.getChildren() != null) {
			for (IValueTreeNode child : nodeToDelete.getChildren()) {
				this.deleteNode(child, false);	// do not rebuild mapping here, because we will rebuild it at once at the end of this method
			}
		}
		
		// also rebuild index of names
		if (isToRebuildIndexMap) {
			Map<String, Integer> map = getNameIndex();
			if (map == null) {
				return -1;
			}
			map.clear();
			int i = 0;
			for (IValueTreeNode vtNode : getNodes()) {
				map.put(vtNode.getName(), i);
				i++;
			}
		}
		return ret;
	}

	/**
	 * @see unbbayes.prs.bn.valueTree.IValueTree#setAsShadowNode(unbbayes.prs.bn.valueTree.IValueTreeNode)
	 */
	public int setAsShadowNode(IValueTreeNode shadowNode) {
		// initial assertions
		if (this.getRoot() == null || shadowNode == null || shadowNode.getName() == null || shadowNode.getName().isEmpty()) {
			// impossible to add nodes
			return -1;
		}
		
		// add this to list of shadow nodes
		if (this.shadowNodeList == null) {
			this.shadowNodeList = new ArrayList<IValueTreeNode>(1);
		}
		int indexOf = this.shadowNodeList.indexOf(shadowNode);
		if (indexOf >= 0) {
			// shadow node is already there, so don't add again
			return indexOf;
		}
		this.shadowNodeList.add(shadowNode);
		
		// At this point, root is non-null. Add node as state of the root node
		this.getRoot().appendState(shadowNode.getName());
		
		return this.shadowNodeList.size()-1;
	}
	
	/**
	 * @see unbbayes.prs.bn.valueTree.IValueTree#removeLastShadowNode()
	 */
	public IValueTreeNode removeLastShadowNode() {
		if (this.shadowNodeList == null) {
			return null;
		}
		
		// Remove last state from the root node (the ordinal bayes net node)
		if (this.getRoot() != null) {
			this.getRoot().removeLastState();
		}
		
		// Note: at this point, this.shadowNodeList != null. 
		// Remove last element and return the removed element
		return this.shadowNodeList.remove(this.shadowNodeList.size()-1);
	}

	/**
	 * @see unbbayes.prs.bn.valueTree.IValueTree#getShadowNodeStateIndex(unbbayes.prs.bn.valueTree.IValueTreeNode)
	 */
	public int getShadowNodeStateIndex(IValueTreeNode shadowNode) {
		if (this.shadowNodeList == null) {
			return -1;
		}
		return this.shadowNodeList.indexOf(shadowNode);
	}

	/**
	 * @return the shadowNodeList
	 */
	protected List<IValueTreeNode> getShadowNodeList() {
		return shadowNodeList;
	}

	/**
	 * @param shadowNodeList the shadowNodeList to set
	 */
	protected void setShadowNodeList(List<IValueTreeNode> shadowNodeList) {
		this.shadowNodeList = shadowNodeList;
	}

	
	

}
