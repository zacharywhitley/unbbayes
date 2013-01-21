package unbbayes.prm.controller.dao;

import static org.junit.Assert.assertTrue;

import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import unbbayes.prm.controller.dao.imp.DBControllerImp;

/**
 * 
 * @author David Saldaña
 * 
 */
public class ReadDBSchema {
	/**
	 * URL to connect. include ";create=true" if the db does not exist.
	 */
	private static String DB_URL = "jdbc:mysql://localhost:3306/MDA?user=root&password=unb";
	private static String[] tableNames = { "PERSON" };

	IDBController relSchemaLoader;

	@Before
	public void setUp() throws Exception {
		relSchemaLoader = new DBControllerImp();
		relSchemaLoader.init(DB_URL);

	}

	@After
	public void tearDown() throws Exception {
		relSchemaLoader.end();
	}

	@Test
	public void test() {

		Database db = relSchemaLoader.getRelSchema();

		Table[] tables = db.getTables();
		for (int i = 0; i < tableNames.length; i++) {
			System.out.println("Table: " + tables[i]);
			assertTrue(tables[i].getName().equals(tableNames[i]));

			// Columns
			Column[] columns = tables[i].getColumns();
			for (int j = 0; j < columns.length; j++) {
				System.out.println(" Column: " + columns[j].getName());
			}
		}

	}
}
