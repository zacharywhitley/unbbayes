/**
 * 
 */
package unbbayes.prs.prm;

import unbbayes.prs.INode;

/**
 * This interface represents a value of a object's attribute,
 * that is, if an entity is a table and an object is a line,
 * this interface represents a single cell containing a value.
 * @author Shou Matsumoto
 *
 */
public interface IAttributeValue extends INode {
	
	/**
	 * Gets the PRM object where this attribute value belongs.
	 * @return
	 */
	public IPRMObject getContainerObject();
	
	/**
	 * Sets the PRM object where this attribute value belongs.
	 * @param prmObject
	 */
	public void setContainerObject(IPRMObject prmObject);
	
	/**
	 * Obtains the descriptor of this attribute
	 * (e.g. if this attribute is a primary key, the type of this attribute, etc.)
	 * @return
	 */
	public IAttributeDescriptor getAttributeDescriptor();

	/**
	 * Sets the descriptor of this attribute
	 * (e.g. if this attribute is a primary key, the type of this attribute, etc.).
	 * Note that this descriptor must be compatible with the list of descriptors
	 * of {@link #getContainerObject()}, since {@link IAttributeValue} represents a value
	 * of an attribute of {@link IPRMObject}.
	 * @param attributeDescriptor
	 */
	public void setAttributeDescriptor(IAttributeDescriptor attributeDescriptor);
	/**
	 * Obtains the object to be used by this attribute value to solve
	 * the {@link INode#getParentNodes()} and {@link INode#getChildNodes()}
	 * @return
	 */
	public IDependencyChainSolver getDependencyChainSolver();
	
	/**
	 * Sets the object to be used by this attribute value to solve
	 * the {@link INode#getParentNodes()} and {@link INode#getChildNodes()}
	 * @param solver
	 */
	public void setDependencyChainSolver(IDependencyChainSolver solver);
	
	/**
	 * Obtains the actual value stored by this object.
	 * We are using textual references to allow flexibility.
	 * You may also want to reuse {@link INode#getName()} 
	 * or {@link INode#getStateAt(int)} to
	 * act like a value.
	 * @return
	 */
	public String getValue();
	
	/**
	 * 
	 * Obtains the actual value stored by this object.
	 * We are using textual references to allow flexibility.
	 * You may also want to reuse {@link INode#setName(String)} 
	 * or {@link INode#setStates(java.util.List)} to
	 * act like a value.
	 * @param value
	 */
	public void setValue(String value);
}
