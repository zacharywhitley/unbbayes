package unbbs.persistence.model.websphere_deploy.CLOUDSCAPE_V50_1;
import com.ibm.ejs.persistence.*;
import javax.ejb.EntityBean;
import java.sql.*;
import java.text.*;
import com.ibm.vap.converters.*;
import com.ibm.vap.converters.streams.*;
/**
 * ModelBeanExtractor_102b0683
 * @generated
 */
public class ModelBeanExtractor_102b0683
	extends com.ibm.ws.ejbpersistence.dataaccess.AbstractEJBExtractor {
	/**
	 * ModelBeanExtractor_102b0683
	 * @generated
	 */
	public ModelBeanExtractor_102b0683() {
		int[] pkCols = { 1 };
		setPrimaryKeyColumns(pkCols);

		int[] dataCols = { 1, 2, 3, 4, 5 };
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
			.ModelBeanCacheEntryImpl_102b0683 entry =
			new unbbs
				.persistence
				.model
				.websphere_deploy
				.CLOUDSCAPE_V50_1
				.ModelBeanCacheEntryImpl_102b0683();

		entry.setDataForID(dataRow.getInt(dataColumns[0]));
		entry.setDataForNAME(dataRow.getString(dataColumns[1]));
		entry.setDataForDESCRIPTION(dataRow.getString(dataColumns[2]));
		entry.setDataForMODEL(dataRow.getString(dataColumns[3]));
		entry.setDataForDOMAIN_ID(
			dataRow.getInt(dataColumns[4]),
			dataRow.wasNull());

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

		unbbs.persistence.model.ModelKey key =
			new unbbs.persistence.model.ModelKey();

		key.id = dataRow.getInt(primaryKeyColumns[0]);

		return key;
	}
	/**
	 * getHomeName
	 * @generated
	 */
	public String getHomeName() {
		return "Model";
	}
	/**
	 * getChunkLength
	 * @generated
	 */
	public int getChunkLength() {
		return 5;
	}
}
