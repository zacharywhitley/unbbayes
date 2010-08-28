/**
 * 
 */
package unbbayes.prs.prm;

import java.util.Map;

/**
 * This interface represents an entry of a table
 * (that is, an instance of an entity, or a line in a database table)
 * @author Shou Matsumoto
 *
 */
public interface IPRMObject {
	
	/**
	 * Obtains a map pointing from an attribute's descriptor (column) to its
	 * value (cell).
	 * @return
	 */
	public Map<IAttributeDescriptor, IAttributeValue> getAttributeValueMap();
	

	/**
	 * Sets a map pointing from an attribute's descriptor (column) to its
	 * value (cell).
	 * @param attributeValueMap
	 */
	public void setAttributeValueMap(Map<IAttributeDescriptor, IAttributeValue> attributeValueMap);
	
	/**
	 * Gets the class (entity) where this object (instance) belongs.
	 * @return
	 */
	public IPRMClass getPRMClass();
	

	/**
	 * Sets the class (entity) where this object (instance) belongs.
	 * @param prmClass
	 */
	public void setPRMClass(IPRMClass prmClass);
	
}
