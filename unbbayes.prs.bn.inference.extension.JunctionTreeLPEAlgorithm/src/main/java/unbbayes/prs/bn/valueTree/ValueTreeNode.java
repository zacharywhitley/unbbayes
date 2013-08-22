package unbbayes.prs.bn.valueTree;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import unbbayes.prs.Node.NodeNameChangedListener;
import unbbayes.prs.bn.ProbabilisticNode;

/**
 * This is a class implementing a tree data structure of value-tree algorithm, 
 * that has root node as the name of the value-tree node.  Each node in the tree can only
 * have one parent, and store any kind of data. The root of the tree is a privileged
 * node that has no parents and no siblings.  Nodes are mainly accessed through
 * their index and method to ﬁnd node index by its name should be implemented
 * in the class.  Initially, the index of a node is returned when it is added to the
 * tree, and should corresponds to the order of addition.
 * @author Shou Matsumoto
 *
 */
public class ValueTreeNode implements IValueTreeNode {
	
	private String name = "node";
	
	private List<IValueTreeNode> children = null;

	private IValueTreeNode parent = null;
	
	private float faction = 1f;
	
//	private float prob = Float.NaN;
	
	private IValueTree valueTree;

	private List<NodeNameChangedListener> nameChangeListeners;
	
	public static final Date DATE0 = new Date(0);
	
	private Date dateLastChange = DATE0;

	/**
	 * The default constructor is made protected to restrict access,
	 * but to allow inheritance.
	 * @see ValueTreeNode#getInstance(String)
	 */
	protected ValueTreeNode() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * This is the default constructor method for {@link ValueTreeNode}
	 * @param name: the name of the value-tree node being exposed to external network 
	 * @param valueTree : the value tree where this node belongs.
	 * @return : a new instance of {@link IValueTreeNode}
	 * @see #setName(String)
	 * @see #setValueTree(IValueTree)
	 */
	public static IValueTreeNode getInstance(String name, IValueTree valueTree) {
		return getInstance(name, valueTree, new ArrayList<NodeNameChangedListener>(1));
	}
	
	/**
	 * This is the default constructor method for {@link ValueTreeNode}
	 * @param name: the name of the value-tree node being exposed to external network 
	 * @param valueTree : the value tree where this node belongs.
	 * @return : a new instance of {@link IValueTreeNode}
	 * @see #setName(String)
	 * @see #setValueTree(IValueTree)
	 */
	public static IValueTreeNode getInstance(String name, IValueTree valueTree, List<NodeNameChangedListener> nameChangeListeners) {
		ValueTreeNode ret = new ValueTreeNode();
		ret.setName(name);
		ret.setValueTree(valueTree);
		// do not initialize the listeners before setting the name first
		ret.setNameChangeListeners(nameChangeListeners);
		return ret;
	}

	/**
	 * @see unbbayes.prs.bn.valueTree.IValueTreeNode#getName()
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name : the name of the value-tree node being exposed to external network	
	 */
	public void setName(String name) {
		final String oldName = this.name;
		this.name = name;
		
		// acknowledge listeners
		if (getNameChangeListeners() != null) {
			for (NodeNameChangedListener listener : getNameChangeListeners()) {
				listener.nodeNameChanged(new ProbabilisticNode().new NodeNameChangedEvent(oldName, name));
			}
		}
	}


	/**
	 * @see unbbayes.prs.bn.valueTree.IValueTreeNode#getChildren()
	 */
	public List<IValueTreeNode> getChildren() {
		return children;
	}

	/**
	 * @param children the children to set
	 */
	public void setChildren(List<IValueTreeNode> children) {
		this.children = children;
	}

	/**
	 * @see unbbayes.prs.bn.valueTree.IValueTreeNode#getParent()
	 */
	public IValueTreeNode getParent() {
		return this.parent;
	}

	/**
	 * @param parent the parent to set
	 */
	public void setParent(IValueTreeNode parent) {
		this.parent = parent;
	}

	/**
	 * @see unbbayes.prs.bn.valueTree.IValueTreeNode#getFaction()
	 */
	public float getFaction() {
		return faction;
	}

	/**
	 * @see unbbayes.prs.bn.valueTree.IValueTreeNode#setFaction(float)
	 */
	public void setFaction(float faction) {
		this.faction = faction;
		this.setDateLastChange(new Date());
//		this.setProbCache(Float.NaN);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return this.getName() + " : " + this.getFaction();
	}

	/**
	 * @return the valueTree
	 */
	public IValueTree getValueTree() {
		return valueTree;
	}

	/**
	 * @see unbbayes.prs.bn.valueTree.IValueTreeNode#setValueTree(unbbayes.prs.bn.valueTree.IValueTree)
	 */
	public void setValueTree(IValueTree valueTree) {
		this.valueTree = valueTree;
//		if (this.getChildren() != null) {
//			for (IValueTreeNode child : this.getChildren()) {
//				child.setValueTree(valueTree);
//			}
//		}
	}

	/**
	 * @return the nameChangeListeners: listeners to be invoked wheen {@link #setName(String)} is called
	 */
	public List<NodeNameChangedListener> getNameChangeListeners() {
		return nameChangeListeners;
	}

	/**
	 * @param nameChangeListeners : listeners to be invoked wheen {@link #setName(String)} is called
	 */
	public void setNameChangeListeners(List<NodeNameChangedListener> nameChangeListeners) {
		this.nameChangeListeners = nameChangeListeners;
	}

	/**
	 * @return the date when {@link #setFaction(float)} was changed last time.
	 */
	public Date getDateLastChange() {
		return dateLastChange;
	}

	/**
	 * @param dateLastChange : the date when {@link #setFaction(float)} was changed last time.
	 */
	public void setDateLastChange(Date dateLastChange) {
		this.dateLastChange = dateLastChange;
	}

//	/**
//	 * @see unbbayes.prs.bn.valueTree.IValueTreeNode#getProbCache()
//	 */
//	public float getProbCache() {
//		return this.prob;
//	}
//
//	/**
//	 * @see unbbayes.prs.bn.valueTree.IValueTreeNode#setProbCache(float)
//	 */
//	public void setProbCache(float prob) {
//		this.prob = prob;
//	}
	

}
