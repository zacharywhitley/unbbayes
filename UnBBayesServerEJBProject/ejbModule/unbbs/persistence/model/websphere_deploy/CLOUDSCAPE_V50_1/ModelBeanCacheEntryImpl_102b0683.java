package unbbs.persistence.model.websphere_deploy.CLOUDSCAPE_V50_1;
import com.ibm.ejs.persistence.*;
import javax.ejb.EntityBean;
import java.sql.*;
import java.text.*;
import com.ibm.vap.converters.*;
import com.ibm.vap.converters.streams.*;
/**
 * ModelBeanCacheEntryImpl_102b0683
 * @generated
 */
public class ModelBeanCacheEntryImpl_102b0683
	extends com.ibm.ws.ejbpersistence.cache.DataCacheEntry
	implements unbbs.persistence.model.websphere_deploy.ModelBeanCacheEntry_102b0683 {
	/**
	 * @generated
	 */
	private int ID_Data;
	/**
	 * @generated
	 */
	private String NAME_Data;
	/**
	 * @generated
	 */
	private String DESCRIPTION_Data;
	/**
	 * @generated
	 */
	private String MODEL_Data;
	/**
	 * @generated
	 */
	private int DOMAIN_ID_Data;
	/**
	 * @generated
	 */
	private boolean DOMAIN_ID_IsNull;
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
	/**
	 * getDescription
	 * @generated
	 */
	public java.lang.String getDescription() {
		return DESCRIPTION_Data;
	}
	/**
	 * setDataForDESCRIPTION
	 * @generated
	 */
	public void setDataForDESCRIPTION(String data) {
		this.DESCRIPTION_Data = data;
	}
	/**
	 * getModel
	 * @generated
	 */
	public java.lang.String getModel() {
		return MODEL_Data;
	}
	/**
	 * setDataForMODEL
	 * @generated
	 */
	public void setDataForMODEL(String data) {
		this.MODEL_Data = data;
	}
	/**
	 * getDomain_id
	 * @generated
	 */
	public int getDomain_id() {
		return DOMAIN_ID_Data;
	}
	/**
	 * setDataForDOMAIN_ID
	 * @generated
	 */
	public void setDataForDOMAIN_ID(int data, boolean isNull) {
		this.DOMAIN_ID_Data = data;
		this.DOMAIN_ID_IsNull = isNull;
	}
	/**
	 * getDomainKey
	 * @generated
	 */
	public unbbs.persistence.model.DomainKey getDomainKey() {
		if (DOMAIN_ID_IsNull)
			return null;
		unbbs.persistence.model.DomainKey key =
			new unbbs.persistence.model.DomainKey();
		key.id = DOMAIN_ID_Data;
		return key;
	}
	/**
	 * getForeignKey
	 * @generated
	 */
	public Object getForeignKey(String role) {
		if (role.equals("domain"))
			return getDomainKey();
		else
			return null;
	}
}
