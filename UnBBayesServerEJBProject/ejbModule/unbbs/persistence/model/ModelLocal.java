package unbbs.persistence.model;
/**
 * Local interface for Enterprise Bean: Model
 */
public interface ModelLocal extends javax.ejb.EJBLocalObject {
	/**
	 * Get accessor for persistent attribute: name
	 */
	public java.lang.String getName();
	/**
	 * Set accessor for persistent attribute: name
	 */
	public void setName(java.lang.String newName);
	/**
	 * Get accessor for persistent attribute: description
	 */
	public java.lang.String getDescription();
	/**
	 * Set accessor for persistent attribute: description
	 */
	public void setDescription(java.lang.String newDescription);
	/**
	 * Get accessor for persistent attribute: model
	 */
	public java.lang.String getModel();
	/**
	 * Set accessor for persistent attribute: model
	 */
	public void setModel(java.lang.String newModel);
	
	/**
	 * This method was generated for supporting the relationship role named domain.
	 * It will be deleted/edited when the relationship is deleted/edited.
	 */
	public unbbs.persistence.model.DomainLocal getDomain();
	/**
	 * This method was generated for supporting the relationship role named domain.
	 * It will be deleted/edited when the relationship is deleted/edited.
	 */
	public void setDomain(unbbs.persistence.model.DomainLocal aDomain);
}
