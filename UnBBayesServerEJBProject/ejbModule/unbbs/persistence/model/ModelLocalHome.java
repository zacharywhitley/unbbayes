package unbbs.persistence.model;
/**
 * Local Home interface for Enterprise Bean: Model
 */
public interface ModelLocalHome extends javax.ejb.EJBLocalHome {

	/**
	 * Creates an instance from a key for Entity Bean: Model
	 */
	public unbbs.persistence.model.ModelLocal create(
		int id,
		String name,
		String description,
		String model,
		DomainLocal domain)
		throws javax.ejb.CreateException;
	/**
	 * Finds an instance using a key for Entity Bean: Model
	 */
	public unbbs.persistence.model.ModelLocal findByPrimaryKey(
		unbbs.persistence.model.ModelKey primaryKey)
		throws javax.ejb.FinderException;
	
	public java.util.Collection findByNameOrDescription(java.lang.String search) throws javax.ejb.FinderException;
	/**
	 * Creates an instance from a key for Entity Bean: Model
	 */
	public unbbs.persistence.model.ModelLocal create(int id)
		throws javax.ejb.CreateException;
}
