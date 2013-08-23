package unbbayes.prs.bn.valueTree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import unbbayes.prs.INode;
import unbbayes.prs.Node.NodeNameChangedEvent;
import unbbayes.prs.Node.NodeNameChangedListener;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNode;

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
//	private float initialAssets;
	
//	private boolean isToChangeAssets = false;
	
	private Map<String, Integer> nameIndex = null;
	
	private List<IValueTreeNode> shadowNodeList = null;

	private boolean isToAdaptFaction = false;

	private ArrayList<IValueTreeFactionChangeListener> factionChangeListeners;

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
	public IValueTreeNode addNode(String nameOfNodeToAdd, IValueTreeNode parent, float faction) {
		// instantiate the new node
		final IValueTreeNode newVTNode = ValueTreeNode.getInstance(nameOfNodeToAdd, this);
		newVTNode.setFaction(faction);
		newVTNode.setParent(parent);	// Note: parent == null means this node shall be connected to root.
		this.addNode(newVTNode);
		return newVTNode;
	}
	
	/**
	 * @see unbbayes.prs.bn.valueTree.IValueTree#addNode(unbbayes.prs.bn.valueTree.IValueTreeNode)
	 */
	public int addNode(final IValueTreeNode nodeToAdd){
		if (nodeToAdd == null) {
			throw new NullPointerException("Cannot add null to value tree.");
		}
			
		if (nodeToAdd.getFaction() < 0 || nodeToAdd.getFaction() > 1) {
			throw new IllegalArgumentException(nodeToAdd.getFaction() + " is not a valid faction (conditional probability given parent).");
		}
		
		nodeToAdd.setValueTree(this);
		// listener which updates index of names when names are changed
		List<NodeNameChangedListener> nameChangeListeners = nodeToAdd.getNameChangeListeners();
		if (nameChangeListeners != null) {
//			nameChangeListeners.clear();
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
		}
		
		
		// Set the new node as child of the specified parent (if null, then parent is the root). Also adjust the factions of other nodes proportionally
		List<IValueTreeNode> whereToAddChild = null;
		if (nodeToAdd.getParent() == null) {
			// make sure the list of children of root is initialized
			if (get1stLevelNodes() == null) {
				set1stLevelNodes(new ArrayList<IValueTreeNode>(1));
			}
			whereToAddChild = get1stLevelNodes();
			
			
		} else {
			// make sure the list of children of parent is initialized
			if (nodeToAdd.getParent().getChildren() == null) {
				nodeToAdd.getParent().setChildren(new ArrayList<IValueTreeNode>(1));
			}
			whereToAddChild = nodeToAdd.getParent().getChildren();
			
		}
		// adapt faction
		if (isToAdaptFaction()) {
			if (whereToAddChild.isEmpty()) {
				// in this case, the new node is the only child, so its faction is actually 100%
				nodeToAdd.setFaction(1f);
			} else {
				// in this case, the faction of other children shall be adapted proportionally
				float factionComplement = 1f-nodeToAdd.getFaction();
				for (IValueTreeNode child : whereToAddChild) {
					// if faction of new node is 100%, then the other states will be set to 0%
					// otherwise, adapt proportionally so that the sum of other states is 1-faction
					child.setFaction((nodeToAdd.getFaction()>=1f)?0f:(child.getFaction()*factionComplement));
				}
			}
		}
		
		if (!whereToAddChild.contains(nodeToAdd)) {
			whereToAddChild.add(nodeToAdd);
		}
		
		// add to the end of the list of all nodes
		if (nodes == null) {
			nodes = new ArrayList<IValueTreeNode>();
		}
		if (!nodes.contains(nodeToAdd)) {
			nodes.add(nodeToAdd);
			// update index of names
			if (getNameIndex() == null) {
				setNameIndex(new HashMap<String, Integer>());
			}
			getNameIndex().put(nodeToAdd.getName(), nodes.size()-1);
		}
		
		
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
//		return (nodes);
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
		if (node == null && anchor != null) {
			// we reached root without passing though anchor. This means anchor is not in the path between node and root (so anchor is not ancestor of node)
			throw new IllegalArgumentException("The anchor (reference) " + anchor + " is expected to be an ancestor of node " + node + ", but it was not.");
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.valueTree.IValueTree#changeProb(unbbayes.prs.bn.valueTree.IValueTreeNode, unbbayes.prs.bn.valueTree.IValueTreeNode, float, java.util.List)
	 */
	public float changeProb(IValueTreeNode node, IValueTreeNode ancestorAnchor, float prob, List<IValueTreeNode> otherAnchors) {
		if (node == null) {
			throw new NullPointerException("Attempted to change the probability of a null node to " + prob + " given node " + ancestorAnchor);
		}
		if (prob < 0 || prob > 1) {
			throw new IllegalArgumentException(prob + " is not a valid probability for " + node + " given " + ancestorAnchor);
		}
		if (otherAnchors == null) {
			otherAnchors = Collections.emptyList();
		}
		
		// TODO do not change child nodes if parent is 0%
		
		// Obtain the current probability, which will be used for probability ratio and to return
		float prevProb = this.getProb(node, ancestorAnchor);
		
		// if the node itself is in the anchor list, then nothing will be changed...
//		if (otherAnchors.contains(node)) {
//			return prevProb;
//		}
		
		// the complementary ratio to be used for the relative sets (i.e. siblings of ancestors)
		float complementaryRatio = (1-prob)/(1-prevProb);
		
//		// this map will store what were the factions before the edit, so that we can restore to it in case of any problem
//		Map<IValueTreeNode, Float> factionBackup = new HashMap<IValueTreeNode, Float>();
		
		// this list will hold what nodes have had factions changes, so that we can notify listeners afterwards
		List<IValueTreeFactionChangeEvent> factionChanges = new ArrayList<IValueTreeFactionChangeEvent>();
		
		try {
			// change the faction of the node by using the ratio
//			factionBackup.put(node, node.getFaction());	// backup faction before change
			float oldFaction = node.getFaction();
			node.setFaction(oldFaction*(prob/prevProb));
			factionChanges.add(ValueTreeFactionChangeEvent.getInstance(node, oldFaction, node.getFaction()));
			
			// iterate towards anchor (or root) to set factions of other nodes
			float probCurrentNode = prob;	// this will hold probability of target initially, and then of its ancestors during the loop
			List<IValueTreeNode> children = null;	//this will hold what are the siblings (of currently managed node) now. 
			do {
				IValueTreeNode parent = node.getParent();
				
				// 1 - adjust faction of siblings in the relative set by using a complementary ratio (1-prob)/(1-prevProb)
				if (parent != null) {
					children = parent.getChildren();
				} else {
					children = get1stLevelNodes();	//Top level (children of root)
				}
				
				// also extract sum of child probability, because it will be used to adjust the faction of ancestors
				float sumProbChildren = 0f;		// this will hold the sum of child (conditional) probability, which will be the parent's new (conditional) probability
				float sumFactionChildren = 0f;	// this will hold the sum of factions of children (so that we can normalize later).
				for (IValueTreeNode child : children) {
					// note: at this point, parent.getChildren() cannot be null, because at least "node" is a child of "parent"
					if (!child.equals(node)) {	
						if (!otherAnchors.contains(child)) {
							// only change siblings here, so not the node itself
//							factionBackup.put(child, child.getFaction());	// backup faction before change
							oldFaction = child.getFaction();
							child.setFaction(oldFaction*complementaryRatio);
							factionChanges.add(ValueTreeFactionChangeEvent.getInstance(child, oldFaction, child.getFaction()));
						}
						sumProbChildren += getProb(child, ancestorAnchor);
					} else {
						// this is either the target node or its ancestor. We don't need to call getProb(child, anchor) again
						sumProbChildren += probCurrentNode;
					}
					sumFactionChildren += child.getFaction();
				}
				
				// get the current probability of the ancestor, so that we can use its ratio with the sum of children to adjust faction
				if (parent != null && !parent.equals(ancestorAnchor)) {
					if (!otherAnchors.contains(parent)) {
						// 2 - adjust the faction of the ancestor in the path between anchor and node by using the prob of children
//						factionBackup.put(parent, parent.getFaction());	// backup faction before change
						oldFaction = parent.getFaction();
						parent.setFaction(oldFaction*(sumProbChildren/getProb(parent, ancestorAnchor)));
						factionChanges.add(ValueTreeFactionChangeEvent.getInstance(parent, oldFaction, parent.getFaction()));
					}
					
					// parent will be handled in the next iteration, so set nodeProb to the probability of node of next iteration (i.e. parent) too
					probCurrentNode = sumProbChildren;
				}
				
				
				// 3 - normalize the factions of children (current node and siblings)
				for (IValueTreeNode child : children) {
					// Note: I could not do this in the previous "for", because getProb was needed after the last "for", and getProb is sensitive to factions
					if (!otherAnchors.contains(child)) {
//						factionBackup.put(child, child.getFaction());	// backup faction before change
						oldFaction = child.getFaction();
						child.setFaction(oldFaction/sumFactionChildren);
						factionChanges.add(ValueTreeFactionChangeEvent.getInstance(child, oldFaction, child.getFaction()));
					}
				}

				// parent will be the node to be handled in the next iteration.
				node = parent;
			} while (node != null && !node.equals(ancestorAnchor));
		} catch (Throwable t) {
			// restore factions of all affected nodes
//			for (Entry<IValueTreeNode, Float> entry : factionBackup.entrySet()) {
//				entry.getKey().setFaction(entry.getValue());
//			}
			for (IValueTreeFactionChangeEvent changeEvent : factionChanges) {
				changeEvent.getNode().setFaction(changeEvent.getFactionBefore());
			}
			throw new RuntimeException(t);
		}
		
		if (this.factionChangeListeners != null) {
			for (IValueTreeFactionChangeListener listener : factionChangeListeners) {
				listener.onFactionChange(factionChanges);
			}
		}
		
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

//	/* (non-Javadoc)
//	 * @see unbbayes.prs.bn.IValueTree#setInitialAssets(float)
//	 */
//	public void setInitialAssets(float initialAssets) {
//		this.initialAssets = initialAssets;
//	}

	/**
	 * @param root the root to set
	 */
	public void setRoot(INode root) {
		this.root = root;
	}

//	/**
//	 * @return the initialAssets
//	 */
//	public float getInitialAssets() {
//		return this.initialAssets;
//	}

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

//	/**
//	 * @return if true, then {@link #changeProb(IValueTreeNode, IValueTreeNode, float)} will also attempt to change assets
//	 */
//	public boolean isToChangeAssets() {
//		return isToChangeAssets;
//	}
//
//	/**
//	 * @param isToChangeAssets the isToChangeAssets to set
//	 */
//	public void setToChangeAssets(boolean isToChangeAssets) {
//		this.isToChangeAssets = isToChangeAssets;
//	}

	/**
	 * @see unbbayes.prs.bn.valueTree.IValueTree#deleteNode(unbbayes.prs.bn.valueTree.IValueTreeNode)
	 */
	public int deleteNode(IValueTreeNode nodeToDelete) {
		return this.deleteNode(nodeToDelete, true, true);
	}
	
	/**
	 * Does the same of {@link #deleteNode(IValueTreeNode)},
	 * but here we can specify whether we want to rebuild mapping of names {@link #getNameIndex()}
	 * @param nodeToDelete : node to be deleted
	 * @param isToRebuildIndexMap : if true, then {@link #getNameIndex()} will be rebuild
	 * @param isToNormalizeSiblingFaction : if true, factions of siblings of deleted node will be normalized.
	 * @return index of nodeToDelete before it was deleted.
	 */
	protected int deleteNode(IValueTreeNode nodeToDelete, boolean isToRebuildIndexMap, boolean isToNormalizeSiblingFaction) {
		if (nodeToDelete == null) {
			return -1;
		}
		int ret = getIndex(nodeToDelete);
		if (ret < 0) {
			return ret;
		}
		
		// delete from lists managed by this class
		if (nodes != null) {
			nodes.remove(nodeToDelete);
		}
		if (shadowNodeList != null) {
			shadowNodeList.remove(nodeToDelete);
		}
		if (children != null) {
			children.remove(nodeToDelete);
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
				this.deleteNode(child, false, false);	// do not rebuild mapping or normalize here, because we will rebuild it at once at the end of this method
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
		
		// also normalize factions of siblings
		if (isToNormalizeSiblingFaction) {
			// extract the list of siblings of the node which was deleted 
			List<IValueTreeNode> listOfSiblings = null;
			if (nodeToDelete.getParent() == null){
				// parent is the root (the BN node), so siblings are in get1stLevelNodes
				listOfSiblings = get1stLevelNodes();
			} else if (nodeToDelete.getParent().getChildren() != null) {
				// siblings are children of parent
				listOfSiblings = nodeToDelete.getParent().getChildren();
			}
			// get the sum of factions of children, so that we can use it for normalization
			float sum = 0f;
			for (IValueTreeNode sibling : listOfSiblings) {
				sum += sibling.getFaction();
			}
			// normalize now by dividing the factions with sum.
			for (IValueTreeNode sibling : listOfSiblings) {
				sibling.setFaction(sibling.getFaction()/sum);
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
		
		if (this.getRoot().getStatesSize() != this.shadowNodeList.size()) {
			throw new IllegalStateException("Desync of shadow nodes found. Shadow nodes expected to be " + shadowNode + ", but the actual number of states in the BN node was " + this.getRoot().getStatesSize());
		}
		
		
		// adjust the CPT and also adjust the marginal probability
		if (this.getRoot() instanceof ProbabilisticNode) {
			ProbabilisticNode probNode = (ProbabilisticNode) this.getRoot();
			// adjust the cpt first
			PotentialTable table = probNode.getProbabilityFunction();
			// obtain the values to be used to fill CPT
			List<Float> probShadowNodes = new ArrayList<Float>(shadowNodeList.size());
			for (int i = 0; i < shadowNodeList.size(); i++) {
				probShadowNodes.add(this.getProb(this.shadowNodeList.get(i),null));
			}
			// fill table
			for (int i = 0; i < table.tableSize(); i++) {
				table.setValue(i, probShadowNodes.get(i % shadowNodeList.size()));
			}
			// then adjust the marginal (not necessary if junction tree will be compiled next)
			probNode.initMarginalList();
			for (int i = 0; i < this.shadowNodeList.size(); i++) {
				probNode.setMarginalAt(i, probShadowNodes.get(i));
			}
		}
		
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

	/**
	 * @return if true, then {@link #addNode(IValueTreeNode)} and {@link #addNode(String, IValueTreeNode, float)} will
	 * adapt (normalize) the faction of siblings proportionally.
	 */
	public boolean isToAdaptFaction() {
		return isToAdaptFaction;
	}

	/**
	 * @param isToAdaptFaction : if true, then {@link #addNode(IValueTreeNode)} and {@link #addNode(String, IValueTreeNode, float)} will
	 * adapt (normalize) the faction of siblings proportionally.
	 */
	public void setToAdaptFaction(boolean isToAdaptFaction) {
		this.isToAdaptFaction = isToAdaptFaction;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.valueTree.IValueTree#addFactionChangeListener(unbbayes.prs.bn.valueTree.IValueTreeFactionChangeListener)
	 */
	public void addFactionChangeListener(
			IValueTreeFactionChangeListener listener) {
		if (this.factionChangeListeners == null) {
			this.factionChangeListeners = new ArrayList<IValueTreeFactionChangeListener>();
		}
		this.factionChangeListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.valueTree.IValueTree#removeFactionChangeListener(unbbayes.prs.bn.valueTree.IValueTreeFactionChangeListener)
	 */
	public void removeFactionChangeListener(
			IValueTreeFactionChangeListener listener) {
		if (this.factionChangeListeners != null) {
			this.factionChangeListeners.remove(listener);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.valueTree.IValueTree#removeAllFactionChangeListener()
	 */
	public void removeAllFactionChangeListener() {
		if (this.factionChangeListeners != null) {
			this.factionChangeListeners.clear();
		}
	}

	/**
	 * @return the factionChangeListeners
	 */
	public ArrayList<IValueTreeFactionChangeListener> getFactionChangeListeners() {
		return factionChangeListeners;
	}

	/**
	 * @param factionChangeListeners the factionChangeListeners to set
	 */
	public void setFactionChangeListeners(ArrayList<IValueTreeFactionChangeListener> factionChangeListeners) {
		this.factionChangeListeners = factionChangeListeners;
	}

	/**
	 * this won't clone {@link #getFactionChangeListeners()}
	 * @see java.lang.Object#clone()
	 */
	public Object clone() throws CloneNotSupportedException {
		ValueTree clone = (ValueTree) ValueTree.getInstance(this.getRoot());
		// index of nodes created already
		Map<String, IValueTreeNode> clonedNodes = new HashMap<String, IValueTreeNode>();
		// make sure factions are not modified during cloning
		clone.setToAdaptFaction(false);
		// build hierarchy
		for (IValueTreeNode child : get1stLevelNodes()) {
			// use null as parent, because their parents are the root
			clonedNodes.putAll(this.cloneAndLinkValueSubTreeRecursive(clone, child, null));
		}
		
		// set shadow nodes
		for (int i = 0; i < this.getShadowNodeSize(); i++) {
			IValueTreeNode originalShadowNode = this.getShadowNode(i);
			IValueTreeNode cloneShadowNode = clonedNodes.get(originalShadowNode.getName());
			clone.setAsShadowNode(cloneShadowNode);
		}
		
		clone.setToAdaptFaction(this.isToAdaptFaction());
		return clone;
	}

	/**
	 * Recursively clones value tree nodes and inserts to clonedNodes
	 * @param treeToAdd : value tree to insert cloned nodes
	 * @param originalRoot : root of the sub-tree to clone
	 * @param cloneParent : node which will be set as the parent of the new clone
	 * @return map filled with nodes created by this method.
	 */
	protected Map<String, IValueTreeNode> cloneAndLinkValueSubTreeRecursive(ValueTree treeToAdd, IValueTreeNode originalRoot, IValueTreeNode cloneParent) {
		if (treeToAdd == null || originalRoot == null) {
			throw new NullPointerException("The value tree and the node to clone must be specified.");
		}
		// initialize the object to return
		Map<String, IValueTreeNode> mapToReturn = new HashMap<String, IValueTreeNode>();
		// clone the node of this level
		IValueTreeNode clonedNode = treeToAdd.addNode(originalRoot.getName(), cloneParent, originalRoot.getFaction());
		mapToReturn.put(clonedNode.getName(), clonedNode);
		// recursively create descendants
		if (originalRoot.getChildren() != null) {
			for (IValueTreeNode originalChild : originalRoot.getChildren()) {
				// the new node is the parent in next recursion
				mapToReturn.putAll(this.cloneAndLinkValueSubTreeRecursive(treeToAdd, originalChild, clonedNode));
			}
		}
		return mapToReturn;
	}

	
	

}
