package unbbs.persistence.model.websphere_deploy.CLOUDSCAPE_V50_1;
import com.ibm.ejs.persistence.*;
import javax.ejb.EntityBean;
import java.sql.*;
import java.text.*;
import com.ibm.vap.converters.*;
import com.ibm.vap.converters.streams.*;
/**
 * DomainBeanInjectorImpl_b3fefee1
 * @generated
 */
public class DomainBeanInjectorImpl_b3fefee1
	implements unbbs.persistence.model.websphere_deploy.DomainBeanInjector_b3fefee1 {
	/**
	 * ejbCreate
	 * @generated
	 */
	public void ejbCreate(
		com.ibm.ws.ejbpersistence.beanextensions.ConcreteBean cb,
		javax.resource.cci.IndexedRecord record) {
		unbbs.persistence.model.ConcreteDomain_b3fefee1 concreteBean =
			(unbbs.persistence.model.ConcreteDomain_b3fefee1) cb;
		record.set(0, new Integer(concreteBean.getId()));
		record.set(1, concreteBean.getName());
	}
	/**
	 * ejbStore
	 * @generated
	 */
	public void ejbStore(
		com.ibm.ws.ejbpersistence.beanextensions.ConcreteBean cb,
		javax.resource.cci.IndexedRecord record) {
		unbbs.persistence.model.ConcreteDomain_b3fefee1 concreteBean =
			(unbbs.persistence.model.ConcreteDomain_b3fefee1) cb;
		record.set(0, new Integer(concreteBean.getId()));
		record.set(1, concreteBean.getName());
	}
	/**
	 * ejbRemove
	 * @generated
	 */
	public void ejbRemove(
		com.ibm.ws.ejbpersistence.beanextensions.ConcreteBean cb,
		javax.resource.cci.IndexedRecord record) {
		unbbs.persistence.model.ConcreteDomain_b3fefee1 concreteBean =
			(unbbs.persistence.model.ConcreteDomain_b3fefee1) cb;
		record.set(0, new Integer(concreteBean.getId()));
	}
	/**
	 * ejbFindByPrimaryKey
	 * @generated
	 */
	public void ejbFindByPrimaryKey(
		Object pkeyObject,
		javax.resource.cci.IndexedRecord record) {
		unbbs.persistence.model.DomainKey pkey =
			(unbbs.persistence.model.DomainKey) pkeyObject;
		record.set(0, new Integer(pkey.id));
	}
	/**
	 * findDomainByModelsKey_Local
	 * @generated
	 */
	public void findDomainByModelsKey_Local(
		unbbs.persistence.model.ModelKey fkey,
		javax.resource.cci.IndexedRecord record) {
		record.set(0, new Integer(fkey.id));
	}
}
