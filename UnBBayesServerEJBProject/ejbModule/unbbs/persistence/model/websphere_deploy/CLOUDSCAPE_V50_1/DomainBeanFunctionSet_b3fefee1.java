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
 * DomainBeanFunctionSet_b3fefee1
 * @generated
 */
public class DomainBeanFunctionSet_b3fefee1
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
					"INSERT INTO DOMAIN1 (ID, NAME) VALUES (?, ?)");

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
				prepareStatement(
					connection,
					"DELETE FROM DOMAIN1  WHERE ID = ?");

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
					"UPDATE DOMAIN1  SET NAME = ? WHERE ID = ?");

			// For column ID
			{
				Integer tempInteger;

				tempInteger = (Integer) inputRecord.get(0);
				if (tempInteger == null)
					pstmt.setNull(2, java.sql.Types.INTEGER);
				else
					pstmt.setInt(2, tempInteger.intValue());
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
					"UPDATE DOMAIN1  SET NAME = ? WHERE ID = ? AND NAME = ?");

			// For column ID
			{
				Integer tempInteger;

				tempInteger = (Integer) inputRecord.get(0);
				if (tempInteger == null)
					pstmt.setNull(2, java.sql.Types.INTEGER);
				else
					pstmt.setInt(2, tempInteger.intValue());
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
			IndexedRecord oldRecord = interactionSpec.getOldRecord();
			// For column NAME
			{
				String tempString;

				tempString = (String) oldRecord.get(1);
				if (tempString == null)
					pstmt.setNull(3, java.sql.Types.VARCHAR);
				else
					pstmt.setString(3, tempString);
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
					"SELECT T1.ID, T1.NAME FROM DOMAIN1  T1 WHERE T1.ID = ?");

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
					"SELECT T1.ID, T1.NAME FROM DOMAIN1  T1 WHERE T1.ID = ? FOR UPDATE ");

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
	 * DomainBeanFunctionSet_b3fefee1
	 * @generated
	 */
	public DomainBeanFunctionSet_b3fefee1() {
		functionHash = new java.util.HashMap(8);

		functionHash.put("Create", new Integer(0));
		functionHash.put("Remove", new Integer(1));
		functionHash.put("Store", new Integer(2));
		functionHash.put("StoreUsingOCC", new Integer(3));
		functionHash.put("FindAll", new Integer(4));
		functionHash.put("FindAllForUpdate", new Integer(5));
		functionHash.put("FindByPrimaryKey", new Integer(6));
		functionHash.put("FindByPrimaryKeyForUpdate", new Integer(7));
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
					FindAll(inputRecord, connection, interactionSpec);
				break;
			case 5 :
				outputRecord =
					FindAllForUpdate(inputRecord, connection, interactionSpec);
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
		}
		return outputRecord;
	}
	/**
	 * FindAll
	 * @generated
	 */
	public javax.resource.cci.Record FindAll(
		IndexedRecord inputRecord,
		Object connection,
		WSInteractionSpec interactionSpec)
		throws DataStoreAdapterException {
		PreparedStatement pstmt = null;
		ResultSet result = null;
		try {
			/* select object(o) from Domain o */
			pstmt =
				prepareStatement(
					connection,
					"select  q1.\"ID\",  q1.\"NAME\" from DOMAIN1 q1");

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
	 * FindAllForUpdate
	 * @generated
	 */
	public javax.resource.cci.Record FindAllForUpdate(
		IndexedRecord inputRecord,
		Object connection,
		WSInteractionSpec interactionSpec)
		throws DataStoreAdapterException {
		PreparedStatement pstmt = null;
		ResultSet result = null;
		try {
			/* select object(o) from Domain o */
			pstmt =
				prepareStatement(
					connection,
					"select  q1.\"ID\",  q1.\"NAME\" from DOMAIN1 q1 for update");

			result = pstmt.executeQuery();

		} catch (SQLException e) {
			throw new DataStoreAdapterException(
				"DSA_ERROR",
				e,
				this.getClass());
		}
		return createCCIRecord(connection, result);
	}
}
