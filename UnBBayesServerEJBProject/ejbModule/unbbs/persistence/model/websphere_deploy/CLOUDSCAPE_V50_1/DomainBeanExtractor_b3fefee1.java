package unbbs.persistence.model.websphere_deploy.CLOUDSCAPE_V50_1;
import com.ibm.ejs.persistence.*;
import javax.ejb.EntityBean;
import java.sql.*;
import java.text.*;
import com.ibm.vap.converters.*;
import com.ibm.vap.converters.streams.*;
/**
 * DomainBeanExtractor_b3fefee1
 * @generated
 */
public class DomainBeanExtractor_b3fefee1
	extends com.ibm.ws.ejbpersistence.dataaccess.AbstractEJBExtractor {
	/**
	 * DomainBeanExtractor_b3fefee1
	 * @generated
	 */
	public DomainBeanExtractor_b3fefee1() {
		int[] pkCols = { 1 };
		setPrimaryKeyColumns(pkCols);

		int[] dataCols = { 1, 2 };
		setDataColumns(dataCols);
	}
	/**
	 * extractData
	 * @generated
	 */
	public com.ibm.ws.ejbpersistence.cache.DataCacheEntry extractData(
		com.ibm.ws.ejbpersistence.dataaccess.RawBeanData dataRow)
		throws
			com.ibm.ws.ejbpersistence.utilpm.ErrorProcessingResultCollectionRow,
			com.ibm.ws.ejbpersistence.utilpm.PersistenceManagerInternalError {
		int[] dataColumns = getDataColumns();

		unbbs
			.persistence
			.model
			.websphere_deploy
			.CLOUDSCAPE_V50_1
			.DomainBeanCacheEntryImpl_b3fefee1 entry =
			new unbbs
				.persistence
				.model
				.websphere_deploy
				.CLOUDSCAPE_V50_1
				.DomainBeanCacheEntryImpl_b3fefee1();

		entry.setDataForID(dataRow.getInt(dataColumns[0]));
		entry.setDataForNAME(dataRow.getString(dataColumns[1]));

		return entry;
	}
	/**
	 * extractPrimaryKey
	 * @generated
	 */
	public Object extractPrimaryKey(
		com.ibm.ws.ejbpersistence.dataaccess.RawBeanData dataRow)
		throws
			com.ibm.ws.ejbpersistence.utilpm.ErrorProcessingResultCollectionRow,
			com.ibm.ws.ejbpersistence.utilpm.PersistenceManagerInternalError {
		int[] primaryKeyColumns = getPrimaryKeyColumns();

		unbbs.persistence.model.DomainKey key =
			new unbbs.persistence.model.DomainKey();

		key.id = dataRow.getInt(primaryKeyColumns[0]);

		return key;
	}
	/**
	 * getHomeName
	 * @generated
	 */
	public String getHomeName() {
		return "Domain";
	}
	/**
	 * getChunkLength
	 * @generated
	 */
	public int getChunkLength() {
		return 2;
	}
}
