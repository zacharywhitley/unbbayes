package unbbs.persistence.model.websphere_deploy;
/**
 * Injector interface for Enterprise Bean: Model
 * @generated
 */
public interface ModelBeanInjector_102b0683
	extends com.ibm.ws.ejbpersistence.beanextensions.EJBInjector {
	/**
	 * ejbFindByNameOrDescription
	 * @generated
	 */
	public void ejbFindByNameOrDescription(
		java.lang.String search,
		javax.resource.cci.IndexedRecord record);
	/**
	 * findModelsByDomainKey_Local
	 * @generated
	 */
	public void findModelsByDomainKey_Local(
		unbbs.persistence.model.DomainKey key,
		javax.resource.cci.IndexedRecord record);
}
