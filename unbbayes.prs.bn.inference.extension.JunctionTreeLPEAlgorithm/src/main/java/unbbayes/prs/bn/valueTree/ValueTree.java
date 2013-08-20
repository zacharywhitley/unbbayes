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
//		if (anchor == null && !Float.isNaN(node.getProbCache())) {
//			// inconditional probability can be retrieved from cache
//			return node.getProbCache();
//		}
		// the probability is the product of the factions in the path between anchor and node.
		float ret = 1f;
		while (node != null && !node.equals(anchor)) {
			// multiply the factions from node to anchor (anchor is supposedly null - root - or above node)
			ret *= node.getFaction();
			node = node.getParent();
		}
		// update cache if we calculated inconditional prob
//		if (anchor == null) {
//			node.setProbCache(ret);
//		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.IValueTree#changeProb(unbbayes.prs.bn.IValueTreeNode, unbbayes.prs.bn.IValueTreeNode, float)
	 */
	public float changeProb(IValueTreeNode node, IValueTreeNode anchor, float prob) {
		if (node == null) {
			throw new NullPointerException("Attempted to change the probability of a null node to " + prob + " given node " + anchor);
		}
		if (prob < 0 || prob > 1) {
			throw new IllegalArgumentException(prob + " is not a valid probability for " + node + " given " + anchor);
		}
		
		// Obtain the current probability, which will be used for probability ratio and to return
		float prevProb = this.getProb(node, anchor);
		
		// the complementary ratio to be used for the relative sets (i.e. siblings of ancestors)
		float complementaryRatio = (1-prob)/(1-prevProb);
		
		// change the faction of the node by using the ratio
		node.setFaction(node.getFaction()*prob*prevProb);
		
		// iterate towards anchor (or root) to set factions of other nodes
		float probCurrentNode = prob;	// this will hold probability of target initially, and then of its ancestors during the loop
		do {
			IValueTreeNode parent = node.getParent();
			
			// 1 - adjust faction of siblings in the relative set by using a complementary ratio (1-prob)/(1-prevProb)
			if (parent != null) {
				// also extract sum of child probability, because it will be used to adjust the faction of ancestors
				float sumProbChildren = 0f;		// this will hold the sum of child (conditional) probability, which will be the parent's new (conditional) probability
				float sumFactionChildren = 0f;	// this will hold the sum of factions of children (so that we can normalize later).
				for (IValueTreeNode child : parent.getChildren()) {
					// note: at this point, parent.getChildren() cannot be null, because at least "node" is a child of "parent"
					if (!child.equals(node)) {	
						// only change siblings here, so not the node itself
						child.setFaction(child.getFaction()*complementaryRatio);
						sumProbChildren += getProb(child, anchor);
					} else {
						// this is either the target node or its ancestor. We don't need to call getProb(child, anchor) again
						sumProbChildren += probCurrentNode;
					}
					sumFactionChildren += child.getFaction();
				}
				
				// get the current probability of the ancestor, so that we can use its ratio with the sum of children to adjust faction
				probCurrentNode = getProb(parent, anchor);	// parent will be handled in the next iteration, so set nodeProb to the probability of node of next iteration (i.e. parent) too
				
				// 2 - adjust the faction of the ancestor in the path between anchor and node by using the prob of children
				parent.setFaction(parent.getFaction()*(sumProbChildren/probCurrentNode));
				
				// 3 - normalize the factions of children (current node and siblings)
				for (IValueTreeNode child : parent.getChildren()) {
					// Note: I could not do this in the previous "for", because getProb was needed after the last "for", and getProb is sensitive to factions
					child.setFaction(child.getFaction()/sumFactionChildren);
				}
				
			}

			// parent will be the node to be handled in the next iteration.
			node = parent;
		} while (node != null && !node.equals(anchor));
		
		// simply return what was the probability before edit
		return prevProb;
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
	 * @param index : the index (offset) of the shadow node.
	 * @return : the shadow node identified by index
	 * @see #getShadowNodeStateIndex(IValueTreeNode)
	 * @see #setAsShadowNode(IValueTreeNode)
	 */
	public IValueTreeNode getShadowNode(int index) {
		if (getShadowNodeList() == null) {
			return null;
		}
		return getShadowNodeList().get(index);
	}
	
	/**
	 * @see unbbayes.prs.bn.valueTree.IValueTree#getShadowNodeSize()
	 */
	public int getShadowNodeSize() {
		if (getRoot() == null || getShadowNodeList() == null) {
			return 0;
		}
		int size = getRoot().getStatesSize();
		if (size != getShadowNodeList().size()) {
			throw new IllegalStateException("Shadow node is desynchronized with the BN node. The BN node has " + size + " states, but shadow nodes are " + getShadowNodeList());
		}
		return size;
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
