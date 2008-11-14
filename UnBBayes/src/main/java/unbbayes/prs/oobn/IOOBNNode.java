/**
 * 
 */
package unbbayes.prs.oobn;

import java.util.Collection;
import java.util.List;

/**
 * @author Shou Matsumoto
 *
 */
public interface IOOBNNode {

	public static int TYPE_OUTPUT = 1;
	public static int TYPE_PRIVATE = 2;
	public static int TYPE_INPUT = 4;
	public static int TYPE_INSTANCE = 8;
	public static int TYPE_INSTANCE_INPUT = TYPE_INSTANCE | TYPE_INPUT;
	public static int TYPE_INSTANCE_OUTPUT= TYPE_INSTANCE | TYPE_OUTPUT;
	
	/**
	 * Obtains the type of this node
	 * @return  TYPE_OUTPUT if this is output node
	 * 			TYPE_PRIVATE if this is private node
	 * 			TYPE_INPUT if this is input node
	 * 			TYPE_INSTANCE if this is an instance node
	 * 			TYPE_INSTANCE_INPUT if this is an input node within instance node
	 * 			TYPE_INSTANCE_OUTPUT if this is an output node within instance node
	 */
	public int getType();

	
	/**
	 * Sets the type of this node
	 * @param type:  TYPE_OUTPUT if this is output node
	 * 			TYPE_PRIVATE if this is private node
	 * 			TYPE_INPUT if this is input node
	 * 			TYPE_INSTANCE if this is an instance node
	 * 			TYPE_INSTANCE_INPUT if this is an input node within instance node
	 * 			TYPE_INSTANCE_OUTPUT if this is an output node within instance node
	 */
	public void setType(int type);
	
	/**
	 * Obtains the oobn class which this node resides
	 * If this is an instance node, obtains the class it represents
	 * @return
	 */
	public IOOBNClass getParentClass();
	
	/**
	 * sets the oobn class which this node resides.
	 * If this is an instance node, obtains the class it represents
	 */
	public void setParentClass(IOOBNClass parentClass);
	
	/**
	 * If node is an inner node within an instance node, we may
	 * want to obtain that instance node
	 */
	public IOOBNNode getUpperInstanceNode();
	
	/**
	 * If node is an inner node within an instance node, we may
	 * want to set that instance node
	 */
	public void setUpperInstanceNode(IOOBNNode upper);
	
	/**
	 * If node must have inner nodes within an instance node, we may
	 * want to add more
	 */
	public void addInnerNode(IOOBNNode inner);
	
	
	/**
	 * If node is has inner nodes within an instance node, we may
	 * want to obtain that instance nodes
	 */
	public Collection<IOOBNNode> getInnerNodes(IOOBNNode upperInstanceNode);
	
	/**
	 * Gets the name of this node
	 * @return
	 */
	public String getName();
	
	/**
	 * sets the name of this node
	 * @param name
	 */
	public void setName(String name);
	
	/**
	 * Obtains an order-sensitive list of states that this node possesses
	 * @return
	 */
	public List<String> getStateNames();
	
	/**
	 * Sets an order-sensitive list of states that this node possesses
	 * @return
	 */
	public  void setStateNames(List<String> states);
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public IOOBNNode clone() throws CloneNotSupportedException;
	
	/**
	 * It might be useful to trace the original node from class,
	 * if node's type is instancial
	 */
	public void setOriginalClassNode(IOOBNNode original);
	
	/**
	 * It might be useful to trace the original node from class,
	 * if node's type is instancial
	 */
	public IOOBNNode getOriginalClassNode();
}
