package unbbs.persistence.model;
/**
 * Local Home interface for Enterprise Bean: Domain
 */
public interface DomainLocalHome extends javax.ejb.EJBLocalHome {
	/**
	 * Creates an instance from a key for Entity Bean: Domain
	 */
	public unbbs.persistence.model.DomainLocal create(int id, String name)
		throws javax.ejb.CreateException;
	/**
	 * Finds an instance using a key for Entity Bean: Domain
	 */
	public unbbs.persistence.model.DomainLocal findByPrimaryKey(
		unbbs.persistence.model.DomainKey primaryKey)
		throws javax.ejb.FinderException;

	public java.util.Collection findAll() throws javax.ejb.FinderException;
}
