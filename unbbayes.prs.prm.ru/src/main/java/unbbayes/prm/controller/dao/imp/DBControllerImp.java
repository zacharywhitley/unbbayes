package unbbayes.prm.controller.dao.imp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.beanutils.DynaBean;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformFactory;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.apache.log4j.Logger;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import unbbayes.prm.controller.dao.IDBController;
import unbbayes.prm.model.Attribute;
import unbbayes.prm.model.AttributeStates;
import unbbayes.prm.model.ParentRel;
import unbbayes.prm.util.helper.DBSchemaHelper;

/**
 * Get a relational schema from a DB.
 * 
 * @author David Saldaña
 * 
 */
public class DBControllerImp implements IDBController {
	/**
	 * Logger
	 */
	Logger log = Logger.getLogger(DBControllerImp.class);

	Platform platform;

	/**
	 * @see unbbayes.prm.controller.dao.IDBController
	 */
	public Database getRelSchema() {

		return platform.readModelFromDatabase("model");
	}

	/**
	 * @see unbbayes.prm.controller.dao.IDBController
	 */
	public void init(String URL) throws Exception {
		// FIXME this must not be mandatory.
		int dbEnd = URL.indexOf("?") > 0 ? URL.indexOf("?") : URL.length() - 1;

		// FIXME it must be a general DataSource for any type of DB.
		if (URL.contains("derby")) {
			String dbname = URL.substring(URL.lastIndexOf(":") + 1, dbEnd);
			EmbeddedDataSource ds;
			ds = new EmbeddedDataSource();
			ds.setDatabaseName(dbname);

			platform = PlatformFactory.createNewPlatformInstance(ds);
		} else if (URL.contains("mysql")) {

			// Eg. jdbc:mysql://localhost:3306/MDA?user=root&password=unb
			int tmpEnd = URL.lastIndexOf(":");
			String host = URL.substring(URL.indexOf("//") + 2, tmpEnd);
			String tmp = URL.substring(tmpEnd);
			tmpEnd = tmp.indexOf("/");
			String port = tmp.substring(1, tmpEnd);
			tmp = tmp.substring(tmpEnd);
			tmpEnd = tmp.indexOf("?");
			String dbname = tmp.substring(1, tmpEnd);
			tmp = tmp.substring(tmpEnd);
			tmpEnd = tmp.indexOf("&");
			String user = tmp.substring(tmp.indexOf("=") + 1, tmpEnd);
			tmp = tmp.substring(tmpEnd);
			String pass = tmp.substring(tmp.indexOf("=") + 1);

			MysqlDataSource ds2 = new MysqlDataSource();
			// ds2.setUser(user);
			// ds2.setPassword(pass);
			// ds2.setDatabaseName(dbname);
			ds2.setURL(URL);
			platform = PlatformFactory.createNewPlatformInstance(ds2);
		} else {
			throw new Exception("Database is not supported");
		}

	}

	/**
	 * @see unbbayes.prm.controller.dao.IDBController
	 */
	public String[] getPossibleValues(Table t, Column[] cols) {
		List<String> possibleValues = new ArrayList<String>();

		// Possible values for each column.
		for (Column column : cols) {
			String colName = column.getName();

			// SQL query.
			String sqlQuery = "SELECT " + colName + " FROM " + t.getName()
					+ " GROUP by " + colName;
			log.debug("Sql query: " + sqlQuery);

			// Query to DB.
			Iterator<?> it = platform.query(getRelSchema(), sqlQuery);

			// Get results
			while (it.hasNext()) {
				DynaBean bean = (DynaBean) it.next();
				String val = String.valueOf(bean.get(colName));

				if (val.equalsIgnoreCase("null")) {
					continue;
				}

				possibleValues.add(val);
			}

		}

		return possibleValues.toArray(new String[0]);
	}

	public String[] getPossibleValues(Attribute attribute) {
		return getPossibleValues(attribute.getTable(),
				new Column[] { attribute.getAttribute() });
	}

	/**
	 * @see unbbayes.prm.controller.dao.IDBController
	 */
	public Iterator<DynaBean> getTableValues(Table t) {
		// SQL query.
		String sqlQuery = "SELECT * FROM " + t.getName();

		// Query to DB.
		Iterator<DynaBean> it1 = platform.query(getRelSchema(), sqlQuery);

		return it1;
	}

	public void end() {
		// Close the connection.
		platform.shutdownDatabase();
	}

	/**
	 * 
	 * @return String[][] with two columns. The first column is the id and the
	 *         second is the value.
	 */
	public String[][] getParentRelatedInstances(ParentRel relationship,
			final String queryIndex2) {
		Attribute[] path = relationship.getPath();
		// To create a list of non duplicated table names.
		Set<String> tableNames = new HashSet<String>();

		// If it is a char type
		String queryIndex = path[path.length - 1].getAttribute().getType()
				.contains("CHAR") ? "'" + queryIndex2 + "'" : queryIndex2;

		// Path example: PERSON.BLOODTYPE -> PERSON.MOTHER -> PERSON.ID ->
		// PERSON.BLOODTYPE. Child to -> parent.
		// Then we have path[0]=PERSON.BLOODTYPE, path[1]=PERSON.MOTHER, etc.

		// The fist FK is the query.
		String where = " WHERE " + path[2] + "=" + queryIndex;

		// Slot chain.
		for (int i = 2; i < path.length - 1; i += 2) {
			// If i is even then is a remote index.
			Attribute attributeRemoteId = path[i];
			// If i is odd then is a local FK.
			Attribute attributeLocalFk = path[i + 1]; // Be careful with this +1

			// Validate Fk - Fk
			boolean remoteIdIsFK = DBSchemaHelper
					.isAttributeFK(attributeRemoteId);
			boolean localFkIsFK = DBSchemaHelper
					.isAttributeFK(attributeLocalFk);

			// TODO: EL PROBLEMA ESTÁ AQUÍ EN FK-FK
			// FK-FK then it moves one place the slot chain.
			if (remoteIdIsFK && localFkIsFK) {
				i--;
				continue;
//				attributeLocalFk = path[i + 2];
			}

			// Table names
			String remoteIdName = attributeRemoteId.getTable().getName();
			String localFkName = attributeLocalFk.getTable().getName();

			// If the table names are different, then add the cross.
			if (!remoteIdName.equals(localFkName)) {
				where = where + " AND " + attributeRemoteId + "="
						+ attributeLocalFk;
			}

			// add if it is not duplicated.
			tableNames.add(remoteIdName);
			tableNames.add(localFkName);
		}

		// Related tables
		String queryTables = "";
		for (String tableName : tableNames) {
			queryTables = queryTables
					+ (queryTables.length() == 0 ? tableName : "," + tableName);
		}

		// Parent information.
		String parentAttName = relationship.getParent().getAttribute()
				.getName();

		// Column Index.
		String columnId;
		String table;
		// If this is the primary key
		if (path[path.length - 2].getAttribute().isPrimaryKey()) {
			columnId = path[path.length - 2].getAttribute().getName();
			table = path[path.length - 2].getTable().getName();
		} else {
			// Maybe this is not the best way but it works.
			columnId = path[path.length - 2].getTable().getPrimaryKeyColumns()[0]
					.getName();
			table = path[path.length - 2].getTable().getName();
		}

		// SQL query.
		String sqlQuery = "SELECT " + table + "." + columnId + ", "
				+ relationship.getParent() + " FROM " + queryTables + where;
		log.debug("SQL query = " + sqlQuery);

		// Query to DB.
		Iterator<DynaBean> it1 = platform.query(getRelSchema(), sqlQuery);

		// Convert to string[]
		List<String[]> instances = new ArrayList<String[]>();
		while (it1.hasNext()) {
			DynaBean dynaBean = (DynaBean) it1.next();
			String inst = String.valueOf(dynaBean.get(parentAttName));
			String id = String.valueOf(dynaBean.get(columnId));

			instances.add(new String[] { id, inst });

		}

		return instances.toArray(new String[0][0]);
	}

	/**
	 * 
	 * @return String[][] with two columns. The first column is the id and the
	 *         second is the value.
	 */
	public String[][] getChildRelatedInstances(ParentRel relationship,
			final String queryIndex2) {
		Attribute[] path = relationship.getPath();
		// To create a list of non duplicated table names.
		Set<String> tableNames = new HashSet<String>();

		// If it is a char type
		String queryIndex = path[1].getAttribute().getType().contains("CHAR") ? "'"
				+ queryIndex2 + "'"
				: queryIndex2;

		// Path example: PERSON.BLOODTYPE -> PERSON.MOTHER -> PERSON.ID ->
		// PERSON.BLOODTYPE. Child to -> parent.
		// Then we have path[0]=PERSON.BLOODTYPE, path[1]=PERSON.MOTHER, etc.

		// Direction
		boolean directionFkToId = !path[path.length - 2].getAttribute()
				.isPrimaryKey();

		// Index position depends on the direction
		int indexPosition = path.length - (directionFkToId ? 2 : 3);
		// The fist FK is the query.
		String where = " WHERE " + path[indexPosition] + "=" + queryIndex;

		// Slot chain. 
		for (int i = path.length - 3; i > 0; i -= 2) {
			
			
			// If i is even then is a remote index.
			Attribute attributeRemoteId = path[i];
			// If i is odd then is a local FK.
			Attribute attributeLocalFk = path[i -1]; 

			// Validate FK-FK relationship.
			boolean remoteIdIsFK = DBSchemaHelper.isAttributeFK(attributeRemoteId);
			boolean localFkIsFK = DBSchemaHelper.isAttributeFK(attributeLocalFk);

			// FK-FK then it moves one place the slot chain.
			if (remoteIdIsFK && localFkIsFK) {
				i++;
				continue;	
			}	
			
			// Table names
			String remoteIdTableName = attributeRemoteId.getTable().getName();
			String localFkTableName = attributeLocalFk.getTable().getName();

			// If the table names are different, then add the cross.
			if (!remoteIdTableName.equals(localFkTableName)) {
				where += " AND " + attributeRemoteId + "=" + attributeLocalFk;
			}

			// add if it is not duplicated.
			tableNames.add(remoteIdTableName);
			tableNames.add(localFkTableName);
		}

		// Related tables
		String queryTables = "";
		for (String tableName : tableNames) {
			queryTables = queryTables
					+ (queryTables.length() == 0 ? tableName : "," + tableName);
		}

		// Parent information.
		String parentAttName = relationship.getParent().getAttribute()
				.getName();

		// Column Index.
		String tableId;

		// If this is the primary key
		if (directionFkToId) {
			// Maybe this is not the best way but it works.
			tableId = path[1].getAttribute().getName();
		} else {
			tableId = path[1].getTable().getPrimaryKeyColumns()[0].getName();
		}

		String tableIdName = path[1].getTable().getName() + "." + tableId;

		// SQL query.
		String sqlQuery = "SELECT " + tableIdName + ", "
				+ relationship.getChild() + " FROM " + queryTables + where;
		log.debug("SQL query = " + sqlQuery);

		// Query to DB.
		Iterator<DynaBean> it1 = platform.query(getRelSchema(), sqlQuery);

		// Convert to string[]
		List<String[]> instances = new ArrayList<String[]>();
		while (it1.hasNext()) {
			DynaBean dynaBean = (DynaBean) it1.next();
			String id = String.valueOf(dynaBean.get(tableId));
			String inst = String.valueOf(dynaBean.get(relationship.getChild()
					.getAttribute().getName()));

			instances.add(new String[] { id, inst });
		}

		return instances.toArray(new String[0][0]);
	}

	/**
	 * Get a specific value of an instance.
	 * 
	 * @param queryColumn
	 *            query column
	 * @param localIdColum
	 *            unique index column or a FK.
	 * @param idValue
	 *            instance id.
	 * @return
	 */
	public String getSpecificValue(Column queryColumn, Attribute localIdColum,
			String instanceId) {
		// Id string.
		instanceId = localIdColum.getAttribute().getType().contains("CHAR") ? "'"
				+ instanceId + "'"
				: instanceId;

		// SQL query.
		String sqlQuery = "SELECT " + queryColumn.getName() + " FROM "
				+ localIdColum.getTable().getName() + " WHERE "
				+ localIdColum.toString() + "=" + instanceId;

		log.debug("SQL for specific value=" + sqlQuery);

		// Query to DB.
		Iterator<DynaBean> it1 = platform.query(getRelSchema(), sqlQuery);

		if (it1.hasNext()) {
			DynaBean bean = it1.next();

			return String.valueOf(bean.get(queryColumn.getName()));
		} else {
			return null;
		}
	}

	/**
	 * Get a specific value of an instance.
	 * 
	 * @param queryColumn
	 *            query column
	 * @param destinyIdAttribute
	 *            unique index column.
	 * @param idValue
	 *            instance id.
	 * @return
	 */
	public String getSpecificValue(Column queryColumn,
			Attribute destinyIdAttribute, Attribute origRefAttribute,
			Attribute origIdAttribute, String originInstanceId) {
		// id string
		originInstanceId = destinyIdAttribute.getAttribute().getType()
				.contains("CHAR") ? "'" + originInstanceId + "'"
				: originInstanceId;

		// Validate if the origin attribute exists.
		// String originAtt = origAttribute.getAttribute().getName();
		// String fkValidationQuery = "SELECT " + originAtt + " FROM "
		// + origAttribute.getTable().getName() + " WHERE " + originAtt + "="
		// + instanceId+" AND "+;
		// Iterator<DynaBean> itVal = platform.query(getRelSchema(),
		// fkValidationQuery);
		// log.debug("SQL for specific value=" + fkValidationQuery);
		// if(!itVal.hasNext()){
		// return null;
		// }
		//
		//

		//
		String fkInstanceValue = "(SELECT "
				+ origRefAttribute.getAttribute().getName() + " FROM "
				+ origRefAttribute.getTable().getName() + " WHERE "
				+ origIdAttribute.getAttribute().getName() + "="
				+ originInstanceId + ")";

		// Instance query.
		String fromTable = destinyIdAttribute.getTable().getName();
		String where = destinyIdAttribute.toString() + "=" + fkInstanceValue;

		// SQL query.
		String sqlQuery = "SELECT " + queryColumn.getName() + " FROM "
				+ fromTable + " WHERE " + where;

		log.debug("SQL for specific value=" + sqlQuery);

		// Query to DB.
		Iterator<DynaBean> it1 = platform.query(getRelSchema(), sqlQuery);

		if (it1.hasNext()) {
			DynaBean bean = it1.next();

			return String.valueOf(bean.get(queryColumn.getName()));
		} else {
			return null;
		}
	}

	public double[] getStateProbability(AttributeStates attributeStates) {
		Attribute attribute = attributeStates.getAttribute();
		String columnName = attribute.getAttribute().getName();
		String[] states = attributeStates.getStates();

		// Probabilities
		double[] probs = new double[states.length];

		for (int i = 0; i < states.length; i++) {
			// SELECT COUNT(isECMDeployed) FROM SHIP WHERE isECMDeployed='F'
			String sqlQuery = "SELECT COUNT(" + columnName + ") AS R  FROM "
					+ attribute.getTable().getName() + " WHERE " + columnName
					+ "='" + states[i] + "'";

			log.debug("query=" + sqlQuery);

			Iterator<DynaBean> it1 = platform.query(getRelSchema(), sqlQuery);
			DynaBean bean = it1.next();

			probs[i] = Double.parseDouble(bean.get("R").toString());
		}

		// sum probs
		double sum = 0;
		for (double d : probs) {
			sum += d;
		}

		// Normalize
		if (sum > 0) {
			for (int i = 0; i < probs.length; i++) {
				probs[i] /= sum;
			}
		} else {
			for (int i = 0; i < probs.length; i++) {
				probs[i] = 1 / probs.length;
			}
		}

		return probs;

	}
}
