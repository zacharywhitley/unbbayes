package unbbs.persistence.model.websphere_deploy.CLOUDSCAPE_V50_1;
import com.ibm.ejs.persistence.*;
import javax.ejb.EntityBean;
import java.sql.*;
import java.text.*;
import com.ibm.vap.converters.*;
import com.ibm.vap.converters.streams.*;
/**
 * ModelBeanInjectorImpl_102b0683
 * @generated
 */
public class ModelBeanInjectorImpl_102b0683
	implements unbbs.persistence.model.websphere_deploy.ModelBeanInjector_102b0683 {
	/**
	 * ejbCreate
	 * @generated
	 */
	public void ejbCreate(
		com.ibm.ws.ejbpersistence.beanextensions.ConcreteBean cb,
		javax.resource.cci.IndexedRecord record) {
		unbbs.persistence.model.DomainKey tempDomainKey;

		unbbs.persistence.model.ConcreteModel_102b0683 concreteBean =
			(unbbs.persistence.model.ConcreteModel_102b0683) cb;
		record.set(0, new Integer(concreteBean.getId()));
		record.set(1, concreteBean.getName());
		record.set(2, concreteBean.getDescription());
		record.set(3, concreteBean.getModel());
		tempDomainKey = concreteBean.getDomainKey();
		if (tempDomainKey == null)
			record.set(4, null);
		else
			record.set(4, new Integer(tempDomainKey.id));
	}
	/**
	 * ejbStore
	 * @generated
	 */
	public void ejbStore(
		com.ibm.ws.ejbpersistence.beanextensions.ConcreteBean cb,
		javax.resource.cci.IndexedRecord record) {
		unbbs.persistence.model.DomainKey tempDomainKey;

		unbbs.persistence.model.ConcreteModel_102b0683 concreteBean =
			(unbbs.persistence.model.ConcreteModel_102b0683) cb;
		record.set(0, new Integer(concreteBean.getId()));
		record.set(1, concreteBean.getName());
		record.set(2, concreteBean.getDescription());
		record.set(3, concreteBean.getModel());
		tempDomainKey = concreteBean.getDomainKey();
		if (tempDomainKey == null)
			record.set(4, null);
		else
			record.set(4, new Integer(tempDomainKey.id));
	}
	/**
	 * ejbRemove
	 * @generated
	 */
	public void ejbRemove(
		com.ibm.ws.ejbpersistence.beanextensions.ConcreteBean cb,
		javax.resource.cci.IndexedRecord record) {
		unbbs.persistence.model.ConcreteModel_102b0683 concreteBean =
			(unbbs.persistence.model.ConcreteModel_102b0683) cb;
		record.set(0, new Integer(concreteBean.getId()));
	}
	/**
	 * ejbFindByNameOrDescription
	 * @generated
	 */
	public void ejbFindByNameOrDescription(
		java.lang.String search,
		javax.resource.cci.IndexedRecord record) {
		record.set(0, search);
	}
	/**
	 * ejbFindByPrimaryKey
	 * @generated
	 */
	public void ejbFindByPrimaryKey(
		Object pkeyObject,
		javax.resource.cci.IndexedRecord record) {
		unbbs.persistence.model.ModelKey pkey =
			(unbbs.persistence.model.ModelKey) pkeyObject;
		record.set(0, new Integer(pkey.id));
	}
	/**
	 * findModelsByDomainKey_Local
	 * @generated
	 */
	public void findModelsByDomainKey_Local(
		unbbs.persistence.model.DomainKey fkey,
		javax.resource.cci.IndexedRecord record) {
		record.set(0, new Integer(fkey.id));
	}
}
