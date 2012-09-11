package unbbayes.prm.controller.dao;

import static org.junit.Assert.*;

import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import unbbayes.prm.controller.dao.imp.DBControllerImp;
import unbbayes.prm.view.graphicator.RelationalGraphicator;

/**
 * 
 * @author David Saldaña
 *
 */
public class ReadDBSchema {
	/**
	 * URL to connect. include ";create=true" if the db does not exist.
	 */
	private static String DB_URL = "jdbc:derby:examples/movies/MovieTest.db";
	IDBController relSchemaLoader;

	@Before
	public void setUp() throws Exception {
		relSchemaLoader = new DBControllerImp();
		relSchemaLoader.init(DB_URL);
		
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		String[] tableNames = { "MOVIE", "SHOW", "THEATER" };

		Database db = relSchemaLoader.getRelSchema();

		Table[] tables = db.getTables();
		for (int i = 0; i < tables.length; i++) {
			System.out.println("Table: " + tables[i]);
			assertTrue(tables[i].getName().equals(tableNames[i]));
			
			// Columns
			Column[] columns = tables[i].getColumns();
			for (int j = 0; j < columns.length; j++) {
				System.out.println(" Column: "+ columns[j].getName());
			}
		}
		

	}
}
