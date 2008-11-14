/**
 * 
 */
package unbbayes.prs.oobn.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import unbbayes.prs.oobn.IOOBNClass;
import unbbayes.prs.oobn.IOOBNNode;

/**
 * @author Shou Matsumoto
 *
 */
public class DefaultOOBNNode implements IOOBNNode {

	private List<String> stateNames = null;
	
	private Set<IOOBNNode> innerNodes = null;
	
	
	private IOOBNClass parentClass = null;
	
	private IOOBNNode upperInstance = null;
	
	
	private String name = null;
	
	
	private int type = 	TYPE_OUTPUT;
	
	private IOOBNNode originalClassNode = null;
	
	/**
	 * 
	 */
	protected DefaultOOBNNode() {
		this.innerNodes = new HashSet<IOOBNNode>();
		this.stateNames = new ArrayList<String>();
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
	 * @param type the type to set
	 */
	public void setType(int type) {
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
	 * @see unbbayes.prs.oobn.IOOBNNode#getInnerNodes(unbbayes.prs.oobn.IOOBNNode)
	 */
	public Collection<IOOBNNode> getInnerNodes(IOOBNNode upperInstanceNode) {
		return this.innerNodes;
	}


	/* (non-Javadoc)
	 * @see unbbayes.prs.oobn.IOOBNNode#getName()
	 */
	public String getName() {
		if ( ( this.getType() == this.TYPE_INSTANCE_INPUT ) || ( this.getType() == this.TYPE_INSTANCE_OUTPUT ) ) {
			try{
				return this.getUpperInstanceNode().getName() + "_" + this.getOriginalClassNode().getName();
			} catch (Exception e) {
				return this.getUpperInstanceNode().getName() + "_" + this.name;
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


	
	
	
	
	
}
