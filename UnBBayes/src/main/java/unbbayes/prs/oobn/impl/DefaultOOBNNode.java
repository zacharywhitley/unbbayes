/**
 * 
 */
package unbbayes.prs.oobn.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import unbbayes.prs.INode;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.prs.oobn.IOOBNClass;
import unbbayes.prs.oobn.IOOBNNode;

/**
 * @author Shou Matsumoto
 *
 */
public class DefaultOOBNNode implements IOOBNNode {
	
	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle(
  			unbbayes.prs.oobn.resources.Resources.class.getName());  		
	
  	/** name of states. Please, use an implementation which uses equals() to compare elements or you'll experience trouble at inner instance input nodes */
	private List<String> stateNames = null;
	
	private Set<IOOBNNode> innerNodes = null;
	
	
	private IOOBNClass parentClass = null;
	
	private IOOBNNode upperInstance = null;
	
	
	private String name = null;
	
	
	private int type = 	TYPE_OUTPUT;
	
	private IOOBNNode originalClassNode = null;
	
	private Set<IOOBNNode> parents = null;
	private Set<IOOBNNode> children = null;
	
	private String description = null;
	
	
	/**
	 * 
	 */
	protected DefaultOOBNNode() {
		this.innerNodes = new HashSet<IOOBNNode>();
		// I'm using 
		this.stateNames = new ArrayList<String>();
		this.parents = new HashSet<IOOBNNode>();
		this.children = new HashSet<IOOBNNode>();
	}

	
	public static DefaultOOBNNode newInstance() {
		return new DefaultOOBNNode();
	}
	
	

	/* (non-Javadoc)
	 * @see unbbayes.prs.oobn.IOOBNNode#getParentClass()
	 */
	public IOOBNClass getParentClass() {
		// TODO Auto-generated method stub
		return this.parentClass;
	}





	

	/**
	 * @param parentClass the parentClass to set
	 */
	public void setParentClass(IOOBNClass parentClass) {
		this.parentClass = parentClass;
	}


	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}


	/**
	 * This method also tests consistency for basic node types (input, output, private)
	 * @param type the type to set
	 */
	public void setType(int type) {
		if (type == TYPE_INPUT) {
			if (this.getOOBNParents().size() > 0) {
				// input node (of a class) must not have parents
				throw new IllegalArgumentException(resource.getString("InputNodeHasNoParents"));
			}
		}
		this.type = type;
	}


	/* (non-Javadoc)
	 * @see unbbayes.prs.oobn.IOOBNNode#getUpperInstanceNode()
	 */
	public IOOBNNode getUpperInstanceNode() {
		return this.upperInstance;
	}


	/* (non-Javadoc)
	 * @see unbbayes.prs.oobn.IOOBNNode#setUpperInstanceNode(unbbayes.prs.oobn.IOOBNNode)
	 */
	public void setUpperInstanceNode(IOOBNNode upperInstanceNode) {
		
		this.upperInstance = upperInstanceNode;
		
		
	}


	/* (non-Javadoc)
	 * @see unbbayes.prs.oobn.IOOBNNode#addInnerNode(unbbayes.prs.oobn.IOOBNNode)
	 */
	public void addInnerNode(IOOBNNode inner) {
		this.innerNodes.add(inner);
	}


	/* (non-Javadoc)
	 * @see unbbayes.prs.oobn.IOOBNNode#getInnerNodes()
	 */
	public Collection<IOOBNNode> getInnerNodes() {
		return this.innerNodes;
	}


	/* (non-Javadoc)
	 * @see unbbayes.prs.oobn.IOOBNNode#getName()
	 */
	public String getName() {
		if ( ( this.getType() == this.TYPE_INSTANCE_INPUT ) 
		  || ( this.getType() == this.TYPE_INSTANCE_OUTPUT ) ) {
			
			return( this.getUpperInstanceNode().getName() 
			+ "_" + this.getOriginalClassNode().getName());	
			
		}
		return this.name;
	}
	
	


	/* (non-Javadoc)
	 * @see unbbayes.prs.oobn.IOOBNNode#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.getName();
	}


	/**
	 * @return the stateNames
	 */
	public List<String> getStateNames() {
		return stateNames;
	}


	/**
	 * @param stateNames the stateNames to set
	 */
	public void setStateNames(List<String> stateNames) {
		this.stateNames = stateNames;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public IOOBNNode clone() throws CloneNotSupportedException {
		DefaultOOBNNode clone = DefaultOOBNNode.newInstance();
		clone.setName(this.getName());
		clone.setParentClass(this.getParentClass());
		clone.setStateNames(this.getStateNames());
		clone.setType(this.getType());
		clone.setUpperInstanceNode(this.getUpperInstanceNode());
		return clone;
	}


	/**
	 * @return the originalClassNode
	 */
	public IOOBNNode getOriginalClassNode() {
		return originalClassNode;
	}


	/**
	 * @param originalClassNode the originalClassNode to set
	 */
	public void setOriginalClassNode(IOOBNNode originalClassNode) {
		this.originalClassNode = originalClassNode;
	}


	/* (non-Javadoc)
	 * @see unbbayes.prs.oobn.IOOBNNode#addParent(unbbayes.prs.oobn.IOOBNNode)
	 */
	public void addParent(IOOBNNode node) {
		
		// type consistency
		
		if ((this.getType() == this.TYPE_INPUT )) {
			// input node (of a class) must not have parents
			throw new IllegalArgumentException(resource.getString("InputNodeHasNoParents"));
		}
		
		if ((this.getType() == this.TYPE_INSTANCE_OUTPUT)) {
			// output node (of a instance) must not have parents
			throw new IllegalArgumentException(resource.getString("InstanceOutputNodeHasNoParents"));
		}
		
		if ((this.getType() == this.TYPE_INSTANCE_INPUT)) {
			// instance input node should never have 2 or more parents
			if (this.getOOBNParents().size() > 0) {
				throw new IllegalArgumentException(resource.getString("InstanceInputNodeHasNoMultipleParents"));
			}
			
			// instance input node should have type-compatible parent
			if (!this.getStateNames().equals(node.getStateNames())) {
				throw new IllegalArgumentException(resource.getString("InstanceInputTypeCompatibilityFailed"));
			}
		}
		
		if ((this.getType() == this.TYPE_INSTANCE)) {
			// instance node must not have direct children or parents.
			// Parents of instance node should be parents of instance input nodes.
			// Children of instance node should be children of instance output nodes.
			throw new IllegalArgumentException(resource.getString("PleaseAddParentToInstanceInputNodes"));
		}
		
		if ((node.getType() == node.TYPE_INSTANCE_INPUT)) {
			// no instance input node should have children
			throw new IllegalArgumentException(resource.getString("PleaseAddChildToInstanceOutputNodes"));
		}
		
		this.parents.add(node);
		try{
			node.addChild(this);
		} catch (RuntimeException e) {
			// revert undo to parent (make add child and add parent transactional)
			this.parents.remove(node);
			throw e;
		}
	}


	/* (non-Javadoc)
	 * @see unbbayes.prs.oobn.IOOBNNode#getOOBNParents()
	 */
	public Set<IOOBNNode> getOOBNParents() {
		return this.parents;
	}


	/* (non-Javadoc)
	 * @see unbbayes.prs.oobn.IOOBNNode#addChild(unbbayes.prs.oobn.IOOBNNode)
	 */
	public void addChild(IOOBNNode node) {
		
		// type consistency
		
		// a node must never be a parent of 2 or more instance input nodes
		// we are not testing it to ordinal input node because addParent is doing so
		if ((node.getType() == node.TYPE_INSTANCE_INPUT) ) {			
			if (this.getOOBNChildren() != null) {
				// check if this already has an input instance node
				for (IOOBNNode child : this.getOOBNChildren()) {
					if ((child.getType() == child.TYPE_INSTANCE_INPUT)) {
						throw new IllegalArgumentException(resource.getString("NoNodeIsParentOf2InstanceInput"));
					}
				}
			}			
			
		}
		
		
		if ((this.getType() == this.TYPE_INSTANCE)) {
			// instance node must not have direct children or parents.
			// Parents of instance node should be parents of instance input nodes.
			// Children of instance node should be children of instance output nodes.
			throw new IllegalArgumentException(resource.getString("PleaseAddChildToInstanceOutputNodes"));
		}
		
		this.children.add(node);
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		// this is not null, but if obj is null, it is not equal
		if (obj == null) {
			return false;
		}
		
		// if they are the same instances, of course they are equal
		if (this == obj) {
			return true;
		}
		
		// if obj has compatible type, compare its names
		if (obj instanceof IOOBNNode) {
			if (this.getName().equals(((IOOBNNode)obj).getName())) {
				return true;
			}
		}
		// or else, call super class...
		return super.equals(obj);
	}

	

	/* (non-Javadoc)
	 * @see unbbayes.prs.oobn.IOOBNNode#getOOBNChildren()
	 */
	public Set<IOOBNNode> getOOBNChildren() {
		return this.children;
	}


	// Methods maintained only to assure interface compatibility
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#addChildNode(unbbayes.prs.INode)
	 */
	public void addChildNode(INode child) throws InvalidParentException {
		this.addChild((IOOBNNode)child);
	}


	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#addParentNode(unbbayes.prs.INode)
	 */
	public void addParentNode(INode parent) throws InvalidParentException {
		this.addParent((IOOBNNode)parent);
	}


	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#appendState(java.lang.String)
	 */
	public void appendState(String state) {
		this.getStateNames().add(state);
	}


	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#getAdjacentNodes()
	 */
	public List<INode> getAdjacentNodes() {
		// I'm using a set just in order to assure unique elements
		Set<INode> ret = new HashSet<INode>();
		ret.addAll(this.getParentNodes());
		ret.addAll(this.getChildNodes());
		return new ArrayList<INode>(ret);
	}


	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#getChildNodes()
	 */
	public List<INode> getChildNodes() {
		return new ArrayList<INode>(this.getOOBNChildren());
	}


	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#getDescription()
	 */
	public String getDescription() {
		if (this.description == null) {
			if (this.getOriginalClassNode() != null) {
				return this.getOriginalClassNode().getDescription() + "." + this.getName();
			}
			return this.getName();
		}
		return this.description;
	}


	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#getParentNodes()
	 */
	public List<INode> getParentNodes() {
		return new ArrayList<INode>(this.getOOBNParents());
	}


	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#getStateAt(int)
	 */
	public String getStateAt(int index) {
		return this.getStateNames().get(index);
	}


	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#getStatesSize()
	 */
	public int getStatesSize() {
		return this.getStateNames().size();
	}


	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#removeChildNode(unbbayes.prs.INode)
	 */
	public void removeChildNode(INode child) {
		this.getOOBNChildren().remove(child);
	}


	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#removeLastState()
	 */
	public void removeLastState() {
		if (this.getStateNames().size() > 0) {
			this.getStateNames().remove(this.getStatesSize()-1);
		}		
	}


	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#removeParentNode(unbbayes.prs.INode)
	 */
	public void removeParentNode(INode parent) {
		this.getOOBNParents().remove(parent);
	}


	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#removeStateAt(int)
	 */
	public void removeStateAt(int index) {
		this.getStateNames().remove(index);
	}


	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#setChildNodes(java.util.List)
	 */
	public void setChildNodes(List<INode> children) {
		this.children = new HashSet(children);
	}


	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#setDescription(java.lang.String)
	 */
	public void setDescription(String text) {
		this.description = text;
	}


	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#setParentNodes(java.util.List)
	 */
	public void setParentNodes(List<INode> parents) {
		this.parents = new HashSet(parents);
	}


	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#setStateAt(java.lang.String, int)
	 */
	public void setStateAt(String state, int index) {
		this.getStateNames().set(index, state);
	}


	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#setStates(java.util.List)
	 */
	public void setStates(List<String> states) {
		this.setStateNames(states);
	}


	
	
	
}
