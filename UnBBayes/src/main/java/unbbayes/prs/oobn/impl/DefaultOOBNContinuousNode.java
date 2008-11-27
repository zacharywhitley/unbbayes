/**
 * 
 */
package unbbayes.prs.oobn.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import unbbayes.prs.Node;
import unbbayes.prs.bn.ContinuousNode;
import unbbayes.prs.oobn.IOOBNClass;
import unbbayes.prs.oobn.IOOBNNode;
import unbbayes.util.ArrayMap;
import unbbayes.util.Debug;

/**
 * @author Shou Matsumoto
 *
 */
public class DefaultOOBNContinuousNode extends ContinuousNode implements
		IOOBNNode {

	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.prs.oobn.resources.Resources");  		
	
  	
	private Set<IOOBNNode> innerNodes = null;
	
	
	private IOOBNClass parentClass = null;
	
	private IOOBNNode upperInstance = null;
	
	
	private String name = null;
	
	
	private int type = 	TYPE_OUTPUT;
	
	private IOOBNNode originalClassNode = null;
	
	private Set<IOOBNNode> parents = null;
	private Set<IOOBNNode> children = null;
	
	
	/**
	 * Default implementation of continuous node for OOBN
	 */
	protected DefaultOOBNContinuousNode() {
		// TODO Auto-generated constructor stub
	}
	
	public static DefaultOOBNContinuousNode newInstance() {
		Debug.println(DefaultOOBNContinuousNode.class, "Continuous node for OOBN is not implemented yet.");
		return new DefaultOOBNContinuousNode();
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
		if ( ( this.getType() == this.TYPE_INSTANCE_INPUT ) || ( this.getType() == this.TYPE_INSTANCE_OUTPUT ) ) {
		//if (  this.getType() == this.TYPE_INSTANCE_OUTPUT ) {
			try{
				return this.getUpperInstanceNode().getName() + "_" 
								+ this.getOriginalClassNode().getName();
			} catch (Exception e) {
				return this.getUpperInstanceNode().getName() + "_" 
								+ this.name;
			}
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
		return null;
	}


	/**
	 * @param stateNames the stateNames to set
	 */
	public void setStateNames(List<String> stateNames) {
		Debug.println(this.getClass(), "Attempt to set continuous node's state was detected.");
		for (String string : stateNames) {
			Debug.print(" State: " + string);
		}
		Debug.println("");
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

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public IOOBNNode clone() {
		
		// TODO Auto-generated method stub

		DefaultOOBNContinuousNode clone = this.newInstance();
		
		clone.setName(this.getName());
		clone.setOriginalClassNode(this.getOriginalClassNode());
		clone.setParentClass(this.getParentClass());
		clone.setStateNames(new ArrayList<String>());
		clone.setType(this.getType());
		clone.setUpperInstanceNode(this.getUpperInstanceNode());
		clone.setAdjacents(new ArrayList<Node>(this.getAdjacents()));
		clone.setAssociatedClique(this.getAssociatedClique());
		clone.setChildren(new ArrayList<Node>(this.getChildren()));
		clone.setDescription(this.getDescription());
		clone.setExplanationDescription(this.getExplanationDescription());
		clone.setInformationType(this.getInformationType());
		clone.setLabel(this.getLabel());
		clone.setMean(this.getMean().clone());
		clone.setParents(this.getParents());
		clone.setPhrasesMap(this.getPhrasesMap());
		clone.setPosition(this.getPosition().x, this.getPosition().y);
		clone.setSelected(this.isSelected());
		clone.setSizeIsVariable(this.sizeIsVariable);
//		clone.setSizeVariable(width, height);
		clone.setStandardDeviation(this.getStandardDeviation().clone());
		clone.setStates(new ArrayList<String>(this.states));
		
		
		return clone;
	}
	
	
	

}
