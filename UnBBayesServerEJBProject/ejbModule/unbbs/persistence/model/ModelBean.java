package unbbs.persistence.model;
/**
 * Bean implementation class for Enterprise Bean: Model
 */
public abstract class ModelBean implements javax.ejb.EntityBean {

	private javax.ejb.EntityContext myEntityCtx;
	/**
	 * setEntityContext
	 */
	public void setEntityContext(javax.ejb.EntityContext ctx) {
		myEntityCtx = ctx;
	}
	/**
	 * getEntityContext
	 */
	public javax.ejb.EntityContext getEntityContext() {
		return myEntityCtx;
	}
	/**
	 * unsetEntityContext
	 */
	public void unsetEntityContext() {
		myEntityCtx = null;
	}

	public unbbs.persistence.model.ModelKey ejbCreate(
		int id,
		String name,
		String description,
		String model,
		DomainLocal domain)
		throws javax.ejb.CreateException {
		setId(id);
		setName(name);
		setDescription(description);
		setModel(model);
		return null;
	}
	
	public void ejbPostCreate(
		int id,
		String name,
		String description,
		String model,
		DomainLocal domain)
		throws javax.ejb.CreateException {
			
		setDomain(domain);
	}
	
	/**
	 * ejbActivate
	 */
	public void ejbActivate() {
	}
	/**
	 * ejbLoad
	 */
	public void ejbLoad() {
	}
	/**
	 * ejbPassivate
	 */
	public void ejbPassivate() {
	}
	/**
	 * ejbRemove
	 */
	public void ejbRemove() throws javax.ejb.RemoveException {
	}
	/**
	 * ejbStore
	 */
	public void ejbStore() {
	}
	/**
	 * Get accessor for persistent attribute: id
	 */
	public abstract int getId();
	/**
	 * Set accessor for persistent attribute: id
	 */
	public abstract void setId(int newId);
	/**
	 * Get accessor for persistent attribute: name
	 */
	public abstract java.lang.String getName();
	/**
	 * Set accessor for persistent attribute: name
	 */
	public abstract void setName(java.lang.String newName);
	/**
	 * Get accessor for persistent attribute: description
	 */
	public abstract java.lang.String getDescription();
	/**
	 * Set accessor for persistent attribute: description
	 */
	public abstract void setDescription(java.lang.String newDescription);
	/**
	 * Get accessor for persistent attribute: model
	 */
	public abstract java.lang.String getModel();
	/**
	 * Set accessor for persistent attribute: model
	 */
	public abstract void setModel(java.lang.String newModel);
	
	/**
	 * ejbCreate
	 */
	public unbbs.persistence.model.ModelKey ejbCreate(int id)
		throws javax.ejb.CreateException {
		setId(id);
		return null;
	}
	/**
	 * ejbPostCreate
	 */
	public void ejbPostCreate(int id) throws javax.ejb.CreateException {
	}
	/**
	 * This method was generated for supporting the relationship role named domain.
	 * It will be deleted/edited when the relationship is deleted/edited.
	 */
	public abstract unbbs.persistence.model.DomainLocal getDomain();
	/**
	 * This method was generated for supporting the relationship role named domain.
	 * It will be deleted/edited when the relationship is deleted/edited.
	 */
	public abstract void setDomain(unbbs.persistence.model.DomainLocal aDomain);
}
