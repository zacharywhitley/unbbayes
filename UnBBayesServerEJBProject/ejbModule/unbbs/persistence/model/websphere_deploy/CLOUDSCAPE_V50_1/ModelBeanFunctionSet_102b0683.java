package unbbs.persistence.model.websphere_deploy.CLOUDSCAPE_V50_1;
import com.ibm.ws.rsadapter.cci.WSRdbResultSetImpl;
import com.ibm.ws.rsadapter.cci.WSRdbConnectionImpl;
import com.ibm.websphere.rsadapter.WSInteractionSpec;
import com.ibm.ws.rsadapter.exceptions.DataStoreAdapterException;
import javax.resource.cci.Record;
import javax.resource.cci.IndexedRecord;
import java.sql.*;
import java.text.*;
import com.ibm.vap.converters.*;
import com.ibm.vap.converters.streams.*;
/**
 * ModelBeanFunctionSet_102b0683
 * @generated
 */
public class ModelBeanFunctionSet_102b0683
	extends com.ibm.ws.rsadapter.cci.WSResourceAdapterBase
	implements com.ibm.websphere.rsadapter.DataAccessFunctionSet {
	/**
	 * @generated
	 */
	private java.util.HashMap functionHash;
	/**
	 * Create
	 * @generated
	 */
	public void Create(
		IndexedRecord inputRecord,
		Object connection,
		WSInteractionSpec interactionSpec)
		throws DataStoreAdapterException {
		PreparedStatement pstmt = null;
		try {
			pstmt =
				prepareStatement(
					connection,
					"INSERT INTO MODEL (ID, NAME, DESCRIPTION, MODEL, DOMAIN_ID) VALUES (?, ?, ?, ?, ?)");

			// For column ID
			{
				Integer tempInteger;

				tempInteger = (Integer) inputRecord.get(0);
				if (tempInteger == null)
					pstmt.setNull(1, java.sql.Types.INTEGER);
				else
					pstmt.setInt(1, tempInteger.intValue());
			}
			// For column NAME
			{
				String tempString;

				tempString = (String) inputRecord.get(1);
				if (tempString == null)
					pstmt.setNull(2, java.sql.Types.VARCHAR);
				else
					pstmt.setString(2, tempString);
			}
			// For column DESCRIPTION
			{
				String tempString;

				tempString = (String) inputRecord.get(2);
				if (tempString == null)
					pstmt.setNull(3, java.sql.Types.VARCHAR);
				else
					pstmt.setString(3, tempString);
			}
			// For column MODEL
			{
				String tempString;

				tempString = (String) inputRecord.get(3);
				if (tempString == null)
					pstmt.setNull(4, java.sql.Types.VARCHAR);
				else
					pstmt.setString(4, tempString);
			}
			// For column DOMAIN_ID
			{
				Integer tempInteger;

				tempInteger = (Integer) inputRecord.get(4);
				if (tempInteger == null)
					pstmt.setNull(5, java.sql.Types.INTEGER);
				else
					pstmt.setInt(5, tempInteger.intValue());
			}
			if (pstmt.executeUpdate() == 0)
				throw new DataStoreAdapterException(
					"DSA_ERROR",
					new javax.ejb.NoSuchEntityException(),
					this.getClass());

		} catch (SQLException e) {
			throw new DataStoreAdapterException(
				"DSA_ERROR",
				e,
				this.getClass());
		} finally {
			try {
				if (pstmt != null) {
					returnPreparedStatement(connection, pstmt);
				}
			} catch (SQLException ignored) {
			}
		}
	}
	/**
	 * Remove
	 * @generated
	 */
	public void Remove(
		IndexedRecord inputRecord,
		Object connection,
		WSInteractionSpec interactionSpec)
		throws DataStoreAdapterException {
		PreparedStatement pstmt = null;
		try {
			pstmt =
				prepareStatement(connection, "DELETE FROM MODEL  WHERE ID = ?");

			// For column ID
			{
				Integer tempInteger;

				tempInteger = (Integer) inputRecord.get(0);
				if (tempInteger == null)
					pstmt.setNull(1, java.sql.Types.INTEGER);
				else
					pstmt.setInt(1, tempInteger.intValue());
			}
			if (pstmt.executeUpdate() == 0)
				throw new DataStoreAdapterException(
					"DSA_ERROR",
					new javax.ejb.NoSuchEntityException(),
					this.getClass());

		} catch (SQLException e) {
			throw new DataStoreAdapterException(
				"DSA_ERROR",
				e,
				this.getClass());
		} finally {
			try {
				if (pstmt != null) {
					returnPreparedStatement(connection, pstmt);
				}
			} catch (SQLException ignored) {
			}
		}
	}
	/**
	 * Store
	 * @generated
	 */
	public void Store(
		IndexedRecord inputRecord,
		Object connection,
		WSInteractionSpec interactionSpec)
		throws DataStoreAdapterException {
		PreparedStatement pstmt = null;
		try {
			pstmt =
				prepareStatement(
					connection,
					"UPDATE MODEL  SET NAME = ?, DESCRIPTION = ?, MODEL = ?, DOMAIN_ID = ? WHERE ID = ?");

			// For column ID
			{
				Integer tempInteger;

				tempInteger = (Integer) inputRecord.get(0);
				if (tempInteger == null)
					pstmt.setNull(5, java.sql.Types.INTEGER);
				else
					pstmt.setInt(5, tempInteger.intValue());
			}
			// For column NAME
			{
				String tempString;

				tempString = (String) inputRecord.get(1);
				if (tempString == null)
					pstmt.setNull(1, java.sql.Types.VARCHAR);
				else
					pstmt.setString(1, tempString);
			}
			// For column DESCRIPTION
			{
				String tempString;

				tempString = (String) inputRecord.get(2);
				if (tempString == null)
					pstmt.setNull(2, java.sql.Types.VARCHAR);
				else
					pstmt.setString(2, tempString);
			}
			// For column MODEL
			{
				String tempString;

				tempString = (String) inputRecord.get(3);
				if (tempString == null)
					pstmt.setNull(3, java.sql.Types.VARCHAR);
				else
					pstmt.setString(3, tempString);
			}
			// For column DOMAIN_ID
			{
				Integer tempInteger;

				tempInteger = (Integer) inputRecord.get(4);
				if (tempInteger == null)
					pstmt.setNull(4, java.sql.Types.INTEGER);
				else
					pstmt.setInt(4, tempInteger.intValue());
			}
			if (pstmt.executeUpdate() == 0)
				throw new DataStoreAdapterException(
					"DSA_ERROR",
					new javax.ejb.NoSuchEntityException(),
					this.getClass());

		} catch (SQLException e) {
			throw new DataStoreAdapterException(
				"DSA_ERROR",
				e,
				this.getClass());
		} finally {
			try {
				if (pstmt != null) {
					returnPreparedStatement(connection, pstmt);
				}
			} catch (SQLException ignored) {
			}
		}
	}
	/**
	 * StoreUsingOCC
	 * @generated
	 */
	public void StoreUsingOCC(
		IndexedRecord inputRecord,
		Object connection,
		WSInteractionSpec interactionSpec)
		throws DataStoreAdapterException {
		PreparedStatement pstmt = null;
		try {
			pstmt =
				prepareStatement(
					connection,
					"UPDATE MODEL  SET NAME = ?, DESCRIPTION = ?, MODEL = ?, DOMAIN_ID = ? WHERE ID = ? AND NAME = ? AND DESCRIPTION = ? AND MODEL = ? AND DOMAIN_ID = ?");

			// For column ID
			{
				Integer tempInteger;

				tempInteger = (Integer) inputRecord.get(0);
				if (tempInteger == null)
					pstmt.setNull(5, java.sql.Types.INTEGER);
				else
					pstmt.setInt(5, tempInteger.intValue());
			}
			// For column NAME
			{
				String tempString;

				tempString = (String) inputRecord.get(1);
				if (tempString == null)
					pstmt.setNull(1, java.sql.Types.VARCHAR);
				else
					pstmt.setString(1, tempString);
			}
			// For column DESCRIPTION
			{
				String tempString;

				tempString = (String) inputRecord.get(2);
				if (tempString == null)
					pstmt.setNull(2, java.sql.Types.VARCHAR);
				else
					pstmt.setString(2, tempString);
			}
			// For column MODEL
			{
				String tempString;

				tempString = (String) inputRecord.get(3);
				if (tempString == null)
					pstmt.setNull(3, java.sql.Types.VARCHAR);
				else
					pstmt.setString(3, tempString);
			}
			// For column DOMAIN_ID
			{
				Integer tempInteger;

				tempInteger = (Integer) inputRecord.get(4);
				if (tempInteger == null)
					pstmt.setNull(4, java.sql.Types.INTEGER);
				else
					pstmt.setInt(4, tempInteger.intValue());
			}
			IndexedRecord oldRecord = interactionSpec.getOldRecord();
			// For column NAME
			{
				String tempString;

				tempString = (String) oldRecord.get(1);
				if (tempString == null)
					pstmt.setNull(6, java.sql.Types.VARCHAR);
				else
					pstmt.setString(6, tempString);
			}
			// For column DESCRIPTION
			{
				String tempString;

				tempString = (String) oldRecord.get(2);
				if (tempString == null)
					pstmt.setNull(7, java.sql.Types.VARCHAR);
				else
					pstmt.setString(7, tempString);
			}
			// For column MODEL
			{
				String tempString;

				tempString = (String) oldRecord.get(3);
				if (tempString == null)
					pstmt.setNull(8, java.sql.Types.VARCHAR);
				else
					pstmt.setString(8, tempString);
			}
			// For column DOMAIN_ID
			{
				Integer tempInteger;

				tempInteger = (Integer) oldRecord.get(4);
				if (tempInteger == null)
					pstmt.setNull(9, java.sql.Types.INTEGER);
				else
					pstmt.setInt(9, tempInteger.intValue());
			}
			if (pstmt.executeUpdate() == 0)
				throw new DataStoreAdapterException(
					"DSA_ERROR",
					new javax.ejb.NoSuchEntityException(),
					this.getClass());

		} catch (SQLException e) {
			throw new DataStoreAdapterException(
				"DSA_ERROR",
				e,
				this.getClass());
		} finally {
			try {
				if (pstmt != null) {
					returnPreparedStatement(connection, pstmt);
				}
			} catch (SQLException ignored) {
			}
		}
	}
	/**
	 * FindByNameOrDescription
	 * @generated
	 */
	public javax.resource.cci.Record FindByNameOrDescription(
		IndexedRecord inputRecord,
		Object connection,
		WSInteractionSpec interactionSpec)
		throws DataStoreAdapterException {
		PreparedStatement pstmt = null;
		ResultSet result = null;
		try {
			/* select object(o) from Model o where o.name  like ?1 or o.description like ?1 */
			pstmt =
				prepareStatement(
					connection,
					"select  q1.\"ID\",  q1.\"NAME\",  q1.\"DESCRIPTION\",  q1.\"MODEL\",  q1.\"DOMAIN_ID\" from MODEL q1 where  ( q1.\"NAME\" LIKE ? or  q1.\"DESCRIPTION\" LIKE ?)");

			// For ?1 (search)
			{
				String tempString;

				tempString = (String) inputRecord.get(0);
				if (tempString == null)
					pstmt.setNull(1, java.sql.Types.VARCHAR);
				else
					pstmt.setString(1, tempString);
			}
			// For ?1 (search)
			{
				String tempString;

				tempString = (String) inputRecord.get(0);
				if (tempString == null)
					pstmt.setNull(2, java.sql.Types.VARCHAR);
				else
					pstmt.setString(2, tempString);
			}
			result = pstmt.executeQuery();

		} catch (SQLException e) {
			throw new DataStoreAdapterException(
				"DSA_ERROR",
				e,
				this.getClass());
		}
		return createCCIRecord(connection, result);
	}
	/**
	 * FindByNameOrDescriptionForUpdate
	 * @generated
	 */
	public javax.resource.cci.Record FindByNameOrDescriptionForUpdate(
		IndexedRecord inputRecord,
		Object connection,
		WSInteractionSpec interactionSpec)
		throws DataStoreAdapterException {
		PreparedStatement pstmt = null;
		ResultSet result = null;
		try {
			/* select object(o) from Model o where o.name  like ?1 or o.description like ?1 */
			pstmt =
				prepareStatement(
					connection,
					"select  q1.\"ID\",  q1.\"NAME\",  q1.\"DESCRIPTION\",  q1.\"MODEL\",  q1.\"DOMAIN_ID\" from MODEL q1 where  ( q1.\"NAME\" LIKE ? or  q1.\"DESCRIPTION\" LIKE ?)  for update");

			// For ?1 (search)
			{
				String tempString;

				tempString = (String) inputRecord.get(0);
				if (tempString == null)
					pstmt.setNull(1, java.sql.Types.VARCHAR);
				else
					pstmt.setString(1, tempString);
			}
			// For ?1 (search)
			{
				String tempString;

				tempString = (String) inputRecord.get(0);
				if (tempString == null)
					pstmt.setNull(2, java.sql.Types.VARCHAR);
				else
					pstmt.setString(2, tempString);
			}
			result = pstmt.executeQuery();

		} catch (SQLException e) {
			throw new DataStoreAdapterException(
				"DSA_ERROR",
				e,
				this.getClass());
		}
		return createCCIRecord(connection, result);
	}
	/**
	 * FindByPrimaryKey
	 * @generated
	 */
	public javax.resource.cci.Record FindByPrimaryKey(
		IndexedRecord inputRecord,
		Object connection,
		WSInteractionSpec interactionSpec)
		throws DataStoreAdapterException {
		PreparedStatement pstmt = null;
		ResultSet result = null;
		try {
			pstmt =
				prepareStatement(
					connection,
					"SELECT T1.ID, T1.NAME, T1.DESCRIPTION, T1.MODEL, T1.DOMAIN_ID FROM MODEL  T1 WHERE T1.ID = ?");

			// For column ID
			{
				Integer tempInteger;

				tempInteger = (Integer) inputRecord.get(0);
				if (tempInteger == null)
					pstmt.setNull(1, java.sql.Types.INTEGER);
				else
					pstmt.setInt(1, tempInteger.intValue());
			}
			result = pstmt.executeQuery();

		} catch (SQLException e) {
			throw new DataStoreAdapterException(
				"DSA_ERROR",
				e,
				this.getClass());
		}
		return createCCIRecord(connection, result);
	}
	/**
	 * FindByPrimaryKeyForUpdate
	 * @generated
	 */
	public javax.resource.cci.Record FindByPrimaryKeyForUpdate(
		IndexedRecord inputRecord,
		Object connection,
		WSInteractionSpec interactionSpec)
		throws DataStoreAdapterException {
		PreparedStatement pstmt = null;
		ResultSet result = null;
		try {
			pstmt =
				prepareStatement(
					connection,
					"SELECT T1.ID, T1.NAME, T1.DESCRIPTION, T1.MODEL, T1.DOMAIN_ID FROM MODEL  T1 WHERE T1.ID = ? FOR UPDATE ");

			// For column ID
			{
				Integer tempInteger;

				tempInteger = (Integer) inputRecord.get(0);
				if (tempInteger == null)
					pstmt.setNull(1, java.sql.Types.INTEGER);
				else
					pstmt.setInt(1, tempInteger.intValue());
			}
			result = pstmt.executeQuery();

		} catch (SQLException e) {
			throw new DataStoreAdapterException(
				"DSA_ERROR",
				e,
				this.getClass());
		}
		return createCCIRecord(connection, result);
	}
	/**
	 * findModelsByDomainKey_Local
	 * @generated
	 */
	public javax.resource.cci.Record findModelsByDomainKey_Local(
		IndexedRecord inputRecord,
		Object connection,
		WSInteractionSpec interactionSpec)
		throws DataStoreAdapterException {
		PreparedStatement pstmt = null;
		ResultSet result = null;
		try {
			pstmt =
				prepareStatement(
					connection,
					"SELECT T1.ID, T1.NAME, T1.DESCRIPTION, T1.MODEL, T1.DOMAIN_ID FROM MODEL  T1 WHERE T1.DOMAIN_ID = ?");

			// For column DOMAIN_ID
			{
				Integer tempInteger;

				tempInteger = (Integer) inputRecord.get(0);
				if (tempInteger == null)
					pstmt.setNull(1, java.sql.Types.INTEGER);
				else
					pstmt.setInt(1, tempInteger.intValue());
			}
			result = pstmt.executeQuery();

		} catch (SQLException e) {
			throw new DataStoreAdapterException(
				"DSA_ERROR",
				e,
				this.getClass());
		}
		return createCCIRecord(connection, result);
	}
	/**
	 * findModelsByDomainKey_LocalForUpdate
	 * @generated
	 */
	public javax.resource.cci.Record findModelsByDomainKey_LocalForUpdate(
		IndexedRecord inputRecord,
		Object connection,
		WSInteractionSpec interactionSpec)
		throws DataStoreAdapterException {
		PreparedStatement pstmt = null;
		ResultSet result = null;
		try {
			pstmt =
				prepareStatement(
					connection,
					"SELECT T1.ID, T1.NAME, T1.DESCRIPTION, T1.MODEL, T1.DOMAIN_ID FROM MODEL  T1 WHERE T1.DOMAIN_ID = ? FOR UPDATE ");

			// For column DOMAIN_ID
			{
				Integer tempInteger;

				tempInteger = (Integer) inputRecord.get(0);
				if (tempInteger == null)
					pstmt.setNull(1, java.sql.Types.INTEGER);
				else
					pstmt.setInt(1, tempInteger.intValue());
			}
			result = pstmt.executeQuery();

		} catch (SQLException e) {
			throw new DataStoreAdapterException(
				"DSA_ERROR",
				e,
				this.getClass());
		}
		return createCCIRecord(connection, result);
	}
	/**
	 * ModelBeanFunctionSet_102b0683
	 * @generated
	 */
	public ModelBeanFunctionSet_102b0683() {
		functionHash = new java.util.HashMap(10);

		functionHash.put("Create", new Integer(0));
		functionHash.put("Remove", new Integer(1));
		functionHash.put("Store", new Integer(2));
		functionHash.put("StoreUsingOCC", new Integer(3));
		functionHash.put("FindByNameOrDescription", new Integer(4));
		functionHash.put("FindByNameOrDescriptionForUpdate", new Integer(5));
		functionHash.put("FindByPrimaryKey", new Integer(6));
		functionHash.put("FindByPrimaryKeyForUpdate", new Integer(7));
		functionHash.put("findModelsByDomainKey_Local", new Integer(8));
		functionHash.put(
			"findModelsByDomainKey_LocalForUpdate",
			new Integer(9));
	}
	/**
	 * execute
	 * @generated
	 */
	public Record execute(
		WSInteractionSpec interactionSpec,
		IndexedRecord inputRecord,
		Object connection)
		throws javax.resource.ResourceException {
		String functionName = interactionSpec.getFunctionName();
		Record outputRecord = null;
		int functionIndex =
			((Integer) functionHash.get(functionName)).intValue();

		switch (functionIndex) {
			case 0 :
				Create(inputRecord, connection, interactionSpec);
				break;
			case 1 :
				Remove(inputRecord, connection, interactionSpec);
				break;
			case 2 :
				Store(inputRecord, connection, interactionSpec);
				break;
			case 3 :
				StoreUsingOCC(inputRecord, connection, interactionSpec);
				break;
			case 4 :
				outputRecord =
					FindByNameOrDescription(
						inputRecord,
						connection,
						interactionSpec);
				break;
			case 5 :
				outputRecord =
					FindByNameOrDescriptionForUpdate(
						inputRecord,
						connection,
						interactionSpec);
				break;
			case 6 :
				outputRecord =
					FindByPrimaryKey(inputRecord, connection, interactionSpec);
				break;
			case 7 :
				outputRecord =
					FindByPrimaryKeyForUpdate(
						inputRecord,
						connection,
						interactionSpec);
				break;
			case 8 :
				outputRecord =
					findModelsByDomainKey_Local(
						inputRecord,
						connection,
						interactionSpec);
				break;
			case 9 :
				outputRecord =
					findModelsByDomainKey_LocalForUpdate(
						inputRecord,
						connection,
						interactionSpec);
				break;
		}
		return outputRecord;
	}
}
