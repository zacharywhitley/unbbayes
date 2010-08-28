/**
 * 
 */
package unbbayes.prs.prm;

import java.util.Collection;
import java.util.List;

import unbbayes.prs.INode;
import unbbayes.prs.prm.cpt.IPRMCPT;

/**
 * This interface stores informations about an entity's attributes. That is,
 * informations about a column in a database table. In another word,
 * it represents type information or other restrictions, such as
 * if a column (attribute) is a primary key, a foreign key, datatypes,
 * etc.
 * @author Shou Matsumoto
 *
 */
public interface IAttributeDescriptor extends INode {
	
	/** Compare to {@link INode#getType()} to test if this attribute is a String (this is a stub for future release) */
	public static final int STRING_TYPE  = 1;
	/** Compare to {@link INode#getType()} to test if this attribute is a number (this is a stub for future release) */
	public static final int NUMERIC_TYPE = 2;
	/** Compare to {@link INode#getType()} to test if this attribute is a date (this is a stub for future release) */
	public static final int DATE_TYPE 	 = 3;
	
	/**
	 * Obtains the attribute values stored for this attribute.
	 * @return
	 */
	public Collection<IAttributeValue> getAttributeValues();
	
	/**
	 * Sets the attribute values stored for this attribute.
	 * @param attributeValues
	 */
	public void setAttributeValues(Collection<IAttributeValue> attributeValues);
	
	/**
	 * Obtains an object containing all informations about probabilistic
	 * dependency of this attribute (e.g. CPT, parents, children, etc)
	 * @return
	 */
	public IPRMDependency getPRMDependency();
	

	/**
	 * Sets an object containing all informations about probabilistic
	 * dependency of this attribute (e.g. CPT, parents, children, etc)
	 * @return
	 */
	public void setPRMDependency(IPRMDependency prmDependency);
	
	/**
	 * Obtains the class (entity) where this attribute belongs
	 * @return
	 */
	public IPRMClass getPRMClass();

	/**
	 * Sets the class (entity) where this attribute belongs
	 * @param prmClass
	 */
	public void setPRMClass(IPRMClass prmClass);
	
	/**
	 * Obtains the referenced attribute descriptor if this attribute is a 
	 * foreign key. It must be null if this attribute is not a foreign key.
	 * @return
	 */
	public IForeignKey getForeignKeyReference();

	/**
	 * Sets the referenced attribute descriptor if this attribute is a 
	 * foreign key. It must be null if this attribute is not a foreign key.
	 * @param foreignKey
	 */
	public void setForeignKeyReference(IForeignKey foreignKey);
	
	/**
	 * Checks if this attribute is mandatory.
	 * @return true if this attribute's value must be non-null
	 */
	public Boolean isMandatory();
	

	/**
	 * Sets the flag indicating that this attribute is mandatory.
	 * @param isMandatory : true if this attribute's value must be non-null
	 */
	public void setMandatory(Boolean isMandatory);
	
	/**
	 * Checks if this attribute is a primary key.
	 * @see IPRMClass#getPrimaryKeys()
	 * @return
	 */
	public Boolean isPrimaryKey();
	

	/**
	 * Sets the flag indicating that this attribute is a primary key.
	 * @param isPK : true if this attribute's value must be a PK
	 */
	public void setPrimaryKey(Boolean isPK);
	
	/**
	 * Checks if this attribute is a foreign key.
	 * @see IPRMClass#getForeignKeys()
	 * @return
	 */
	public Boolean isForeignKey();

//	/**
//	 * Sets the flag indicating that this attribute is a primary key.
//	 * @param isFK : true if this attribute's value must be a PK
//	 */
//	public void setForeignKey(Boolean isFK);
	
	/**
	 * Sets the value of {@link INode#getType()} using a type code.
	 * This is a placeholder for future releases allowing
	 * non-categorical types. Currently, only categorical strings are allowed
	 * as type.
	 * @param typeCode
	 */
	public void setType(int typeCode);
}
