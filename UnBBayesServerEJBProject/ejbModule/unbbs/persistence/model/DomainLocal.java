package unbbs.persistence.model;
/**
 * Local interface for Enterprise Bean: Domain
 */
public interface DomainLocal extends javax.ejb.EJBLocalObject {
	/**
	 * Get accessor for persistent attribute: name
	 */
	public java.lang.String getName();
	/**
	 * Set accessor for persistent attribute: name
	 */
	public void setName(java.lang.String newName);
	/**
	 * This method was generated for supporting the relationship role named models.
	 * It will be deleted/edited when the relationship is deleted/edited.
	 */
	public java.util.Collection getModels();
	/**
	 * This method was generated for supporting the relationship role named models.
	 * It will be deleted/edited when the relationship is deleted/edited.
	 */
	public void setModels(java.util.Collection aModels);
}
