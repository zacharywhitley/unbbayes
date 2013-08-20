package unbbayes.prs.bn.valueTree;

import java.util.List;

import unbbayes.prs.INode;

/**
 * Classes implementing this interface represents a tree of {@link IValueTreeNode},
 * that is, possible states of nodes in Bayes nets represented in a hierarchical tree
 * instead of a simple list of states.
 * @author Shou Matsumoto
 * @see IValueTreeNode
 */
public interface IValueTree {

	/**
	 * @return the name of the value tree as a whole.
	 * This can be the name of the root node.
	 */
	public String getName();
	
	
	
	/**
	 * @return the root node in the value tree,
	 * which is an ordinal BN node.
	 * The exposed states are states of this node.
	 */
	public INode getRoot();
	
	/**
	 *  Adds a state node as a child of a given parent node. 
	 * @param nameOfNodeToAdd : name of the state node to be included.
	 * @param parent : the parent of nodeToAdd
	 * @param faction: probability of nodeToAdd given parent.
	 * @return negative if failed. Position index/offset if successful.
	 * @see #getNodes()
	 */
	public int addNode(String nameOfNodeToAdd, IValueTreeNode parent, float faction);
	
	/**
	 * Deletes a value tree node from the value tree.
	 * Implementations may choose whether to delete the whole subtree or not.
	 * @param nodeToDelete : node to be deleted
	 * @return : index of node where it was present.
	 * @see #getIndex(IValueTreeNode)
	 */
	public int deleteNode(IValueTreeNode nodeToDelete);
	
	/**
	 * @param nodeName : name of the node to search
	 * @return the index of the node in the tree;
	 * @see #getNodes()
	 */
	public int getIndex(String nodeName);
	
	/**
	 * @return all value tree nodes in this tree. This is probably a copy of the original. Use {@link #addNode(String, IValueTreeNode, float)}
	 * to include new elements to it.
	 */
	public List<IValueTreeNode> getNodes();
	
	/**
	 * @return all value tree nodes immediately below root (i.e. children of root node).
	 */
	public List<IValueTreeNode> get1stLevelNodes();
	
	/**
	 * @param name : name of the node to search
	 * @return the node identified by the name
	 * @see #getNodes()
	 * @see #getIndex(IValueTreeNode)
	 */
	public IValueTreeNode getNode(String name);
	
	/**
	 * @param node : node to search.
	 * @return the index of the node in {@link #getNodes()};
	 */
	public int getIndex(IValueTreeNode node);
	
	/**
	 * 
	 * @param node : node in the value tree (so, it is equivalent to a possible state of a random variable)
	 * to look for the probability.
	 * @param anchor : the ancestor to consider. If non-null, then the returned
	 * probability is conditional to such anchor. If null, then the returned probability
	 * is absolute probability.
	 * @return  probability of node given one particular ancestor
	 */
	public float getProb(IValueTreeNode node, IValueTreeNode anchor);
	
	/**
	 *  Changes the probability of node from the current to the provided value, conditioning on reference node anchor (which
	 *  means the probability of anchor remains no change). We require to update the tree after the edit, in particular,
	 *  <br/>
	 *  <br/>
	 *  1.  ﬁnd the set of highest relatives according to node, anchor, and update their probabilities including the faction;
	 *  <br/>
	 *  2.  update probabilities and F for the set of parents of node.
	 *  <br/>
	 *  3.  update probabilities and F for all the descendant of node and descendant for the member node in the 
	 *  highest relatives set of (node,anchor).
	 *  <br/>
	 *  4.  output the probabilities of exposing state if there is any change, and signal the update for the whole network.
	 *  <br/>
	 * @param node : node to change probability
	 * @param anchor : anchor (ancestor of node) not to change probability
	 * @param prob : probability value to set
	 * @return : the old probability.
	 */
	public float changeProb(IValueTreeNode node, IValueTreeNode anchor, float prob);
	
	/**
	 * Definition of highest relative set: Let a t be the set of all ancestors of a node t, i.e., the 
	 * parent of t, the parent of that parent, etc.  to the root.  Let s t be the set
	 * of all siblings of a node t.  Given a target node t and a reference node r
	 * somewhere above t, we deﬁne a minimal highest (most toward root) set
	 * of relatives nodes o(r, t) that covers the rest of the tree below r and above
	 * or equal to the level of t, called the highest relative set according to r, t,
	 * and given by o(r, t) = s t + (union g in(a i −a r ) s g ). In another word, highest
	 * relatives set is the set of all siblings of the target node t, and all siblings
	 * of parents (including parent of parent, etc.). 
	 * @param node : target node to consider
	 * @param anchor : reference node to consider
	 * @return  the  highest  relatives  set  according  to node and anchor
	 */
	public List<IValueTreeNode> getHighestRelativeSet(IValueTreeNode node, IValueTreeNode anchor);
	
	/**
	 *  Initialize asset for node and all its siblings. There is no need to assign any asset to
	 *  levels below or other branches in the tree.
	 * @param node: node to initialize assets
	 * @return :  value of the assets used for initialization.
	 */
	public float initAssets(IValueTreeNode node);
	
	/**
	 * Changes the value of default initial assets to be used in {@link #initAssets(IValueTreeNode)}.
	 * @param initialAssets: the assets to set
	 */
	public void setInitialAssets(float initialAssets);
	
	/**
	 * Converts a value tree node to a shadow node (a node visible to the rest of the Bayes net).
	 * @param shadowNode : the node to be converted to a shadow node. This node must be present in 
	 * {@link #getNodes()}.
	 * @return index of state of {@link #getRoot()} where the shadow node was mapped.
	 */
	public int setAsShadowNode(IValueTreeNode shadowNode);
	
	/**
	 * Removes the last shadow node (state of {@link #getRoot()}) which was
	 * included by {@link #setAsShadowNode(IValueTreeNode)}
	 * @return the removed shadow node
	 */
	public IValueTreeNode removeLastShadowNode();
	
	/**
	 * @param shadowNode : the shadow node to find.
	 * @return the index of state of {@link #getRoot()} where the shadow node was mapped
	 */
	public int getShadowNodeStateIndex(IValueTreeNode shadowNode);
	
	/**
	 * @param index : the index (offset) of the shadow node.
	 * @return : the shadow node identified by index
	 * @see #getShadowNodeStateIndex(IValueTreeNode)
	 * @see #setAsShadowNode(IValueTreeNode)
	 */
	public IValueTreeNode getShadowNode(int index);
	
	/**
	 * @return : how many shadow nodes there are.
	 */
	public int getShadowNodeSize();
	
}
