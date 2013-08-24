package unbbayes.prs.bn.valueTree;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import unbbayes.prs.Node.NodeNameChangedListener;

/**
 * Classes implementing this interface represent a tree data structure of value-tree algorithm, 
 * that has root node as the name of the value-tree node.  Each node in the tree can only
 * have one parent, and store any kind of data. The root of the tree is a privileged
 * node that has no parents and no siblings.  Nodes are mainly accessed through
 * their index and method to ﬁnd node index by its name should be implemented
 * in the class.  Initially, the index of a node is returned when it is added to the
 * tree, and should corresponds to the order of addition.
 * @author Shou Matsumoto
 * @see ValueTreeNode
 * @see IValueTree
 */
public interface IValueTreeNode extends Serializable  {

	/**
	 * @return the name of the current node.
	 */
	public String getName();
	
	/**
	 * @param name : the name of the value-tree node being exposed to external network	
	 */
	public void setName(String name);
	
	
	/**
	 * @return the children of this state node
	 */
	public List<IValueTreeNode> getChildren();
	
	/**
	 * @param children : the children of this state node
	 */
	public void setChildren(List<IValueTreeNode> children);
	
	/**
	 * @return the parent of this value tree node. If null, then this node is a root.
	 */
	public IValueTreeNode getParent();
	
	/**
	 * @param parent : the parent of this value tree node. If null, then this node is a root.
	 */
	public void setParent(IValueTreeNode parent);
	
	
	/**
	 * @return the faction of a node represents the conditional probability of node given its parent.
	 */
	public float getFaction();

	/**
	 * @param faction of a node represents the conditional probability of node given its parent.
	 */
	public void setFaction(float faction);
	
//	/**
//	 * @return the cached probability value of this value tree node.
//	 * {@link Float#NaN} means there is no cache yet,
//	 * so {@link #setProbCache(float)} shall be called.
//	 */
//	public float getProbCache();
//	
//	/**
//	 * @param cachedProb : the cached probability value of this value tree node.
//	 *  {@link Float#NaN} means cache will be reset.
//	 *  @see #getProbCache()
//	 */
//	public void setProbCache(float cachedProb);
	
	/**
	 * @return the value tree where this node belongs.
	 * @see #setValueTree(IValueTree)
	 */
	public IValueTree getValueTree();
	
	/**
	 * @param valueTree : the value tree to set.
	 * @see #getValueTree()
	 */
	public void setValueTree(IValueTree valueTree);
	
	/**
	 * @return the date when {@link #setFaction(float)} was changed last time.
	 */
	public Date getDateLastChange();
	
	/**
	 * @return the nameChangeListeners: listeners to be invoked when {@link #setName(String)} is called
	 */
	public List<NodeNameChangedListener> getNameChangeListeners();
	
	/**
	 * @param node : node to test.
	 * @return true if the current node is an ancestor of the node provided in the argument.
	 */
	public boolean isAncestorOf(IValueTreeNode node);
}
