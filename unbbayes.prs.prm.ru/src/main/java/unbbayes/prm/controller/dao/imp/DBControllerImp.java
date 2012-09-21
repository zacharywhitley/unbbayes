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

import unbbayes.prm.controller.dao.IDBController;
import unbbayes.prm.model.Attribute;
import unbbayes.prm.model.ParentRel;

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
	public void init(String URL) {
		// FIXME this must not be mandatory.
		String dbname = URL.substring(URL.lastIndexOf(":") + 1);

		// FIXME it must be a general DataSource for any type of DB.
		EmbeddedDataSource ds;
		ds = new EmbeddedDataSource();
		ds.setDatabaseName(dbname);

		platform = PlatformFactory.createNewPlatformInstance(ds);

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
				possibleValues.add(String.valueOf(bean.get(colName)));
			}

		}

		return possibleValues.toArray(new String[0]);
	}

	@Override
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
		// TODO close connection.
	}

	/**
	 * 
	 */
	@Override
	public String[] getRelatedInstances(ParentRel relationship,
			String queryIndex) {
		Attribute[] path = relationship.getPath();
		// To create a list of non duplicated table names.
		Set<String> tableNames = new HashSet<String>();

		// Path example: PERSON.BLOODTYPE -> PERSON.MOTHER -> PERSON.ID ->
		// PERSON.BLOODTYPE. Child to -> parent

		// The fist FK is the query.
		String where = " WHERE " + path[1].getTable().getName() + "."
				+ path[1].getAttribute().getName() + "=" + queryIndex;

		// Slot chain.
		for (int i = 2; i < path.length - 1; i += 2) {
			// If i is even then is a remote index.
			Attribute attributeRemoteId = path[i];
			// If i is odd then is a local FK.
			Attribute attributeLocalFk = path[i + 1]; // Be careful with this +1

			// Table names
			String remoteIdName = attributeRemoteId.getTable().getName();
			String localFkName = attributeLocalFk.getTable().getName();

			// If the table names are different, then add the cross.
			if (!remoteIdName.equals(localFkName)) {
				where = where + " " + remoteIdName + "."
						+ attributeRemoteId.getAttribute().getName() + "="
						+ localFkName + "."
						+ attributeLocalFk.getAttribute().getName();
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
		String parentTableName = relationship.getParent().getTable().getName();
		String parentAttName = relationship.getParent().getAttribute()
				.getName();

		// SQL query.
		String sqlQuery = "SELECT " + parentTableName + "." + parentAttName
				+ " FROM " + queryTables + where;

		log.debug("SQL query = " + sqlQuery);

		// Query to DB.
		Iterator<DynaBean> it1 = platform.query(getRelSchema(), sqlQuery);

		// Convert to string[]
		List<String> instances = new ArrayList<String>();
		while (it1.hasNext()) {
			DynaBean dynaBean = (DynaBean) it1.next();
			String inst = String.valueOf(dynaBean.get(parentAttName));
			instances.add(inst);

		}

		return instances.toArray(new String[0]);
	}

	@Override
	public String getSpecificValue(Column queryColumn, Attribute attribute,
			String instanceId) {
		// SQL query.
		String sqlQuery = "SELECT " + queryColumn.getName() + " FROM "
				+ attribute.getTable().getName() + " WHERE "
				+ attribute.getAttribute().getName() + "=" + instanceId;

		log.debug("SQL for specific value=" + sqlQuery);

		// Query to DB.
		Iterator<DynaBean> it1 = platform.query(getRelSchema(), sqlQuery);

		DynaBean bean = it1.next();

		return bean.get(queryColumn.getName()).toString();
	}
}
