package unbbs.persistence.model.websphere_deploy.CLOUDSCAPE_V50_1;
import com.ibm.ejs.persistence.*;
import javax.ejb.EntityBean;
import java.sql.*;
import java.text.*;
import com.ibm.vap.converters.*;
import com.ibm.vap.converters.streams.*;
/**
 * DomainBeanCacheEntryImpl_b3fefee1
 * @generated
 */
public class DomainBeanCacheEntryImpl_b3fefee1
	extends com.ibm.ws.ejbpersistence.cache.DataCacheEntry
	implements unbbs.persistence.model.websphere_deploy.DomainBeanCacheEntry_b3fefee1 {
	/**
	 * @generated
	 */
	private int ID_Data;
	/**
	 * @generated
	 */
	private String NAME_Data;
	/**
	 * getId
	 * @generated
	 */
	public int getId() {
		return ID_Data;
	}
	/**
	 * setDataForID
	 * @generated
	 */
	public void setDataForID(int data) {
		this.ID_Data = data;
	}
	/**
	 * getName
	 * @generated
	 */
	public java.lang.String getName() {
		return NAME_Data;
	}
	/**
	 * setDataForNAME
	 * @generated
	 */
	public void setDataForNAME(String data) {
		this.NAME_Data = data;
	}
}
