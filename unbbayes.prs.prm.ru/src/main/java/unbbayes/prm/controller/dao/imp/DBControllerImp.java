package unbbayes.prm.controller.dao.imp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
	public String[] getPossibleValues(Database db, Table t, Column[] cols) {
		List<String> possibleValues = new ArrayList<String>();

		// Possible values for each column.
		for (Column column : cols) {
			String colName = column.getName();

			// SQL query.
			String sqlQuery = "SELECT " + colName + " FROM " + t.getName()
					+ " GROUP by " + colName;
			log.debug("Sql query: " + sqlQuery);

			// Query to DB.
			Iterator<?> it = platform.query(db, sqlQuery);

			// Get results
			while (it.hasNext()) {
				DynaBean bean = (DynaBean) it.next();
				possibleValues.add(String.valueOf(bean.get(colName)));
			}

		}

		return possibleValues.toArray(new String[0]);
	}

	/**
	 * @see unbbayes.prm.controller.dao.IDBController
	 */
	public Iterator<DynaBean> getTableValues(Database db, Table t) {
		// SQL query.
		String sqlQuery = "SELECT * FROM " + t.getName();

		// Query to DB.
		Iterator<DynaBean> it1 = platform.query(db, sqlQuery);

		return it1;
	}

	public void end() {
		// TODO close connection.
	}

	@Override
	public String[] getPossibleValues(Database db, Attribute attribute) {
		return getPossibleValues(db, attribute.getTable(),
				new Column[] { attribute.getAttribute() });
	}

}
