package unbbayes.prm.util;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import unbbayes.prm.controller.dao.IDBController;
import unbbayes.prm.controller.dao.imp.DBControllerImp;
import unbbayes.prm.model.Attribute;

/**
 * 
 * @author David Saldaña
 * 
 */
public class PathFinderTest {
	/**
	 */
	private static String DB_URL = "jdbc:derby:/home/dav/workspace-unb/unbbayes.prs.prm2/examples/bloodType/BloodType.db";

	IDBController relSchemaLoader;

	@Before
	public void setUp() throws Exception {
		relSchemaLoader = new DBControllerImp();
		relSchemaLoader.init(DB_URL);

	}

	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test the path finder algorithm when a table has two references to itself.
	 */
	@Test
	public void testDoubleReferenceToSameTable() {

		Database db = relSchemaLoader.getRelSchema();

		// Table PERSON
		Table tablePerson = db.getTable(0);
		// Column blood type
		Column column = tablePerson.getColumn(3);

		// Parent and child are the same but there are three paths.
		Attribute parent = new Attribute(tablePerson, column);
		Attribute child = new Attribute(tablePerson, column);

		// Path finder
		PathFinderAlgorithm pf = new PathFinderAlgorithm();
		List<Attribute[]> possiblePaths = pf.getPossiblePaths(parent, parent);

		assertTrue(possiblePaths.size() == 2);

		Attribute[] path1 = possiblePaths.get(0);
		String path1String = pathToString(path1);
		System.out.println(path1String);
		assertTrue(path1String.equals(
				"PERSON.BLOODTYPE PERSON.MOTHER PERSON.ID PERSON.BLOODTYPE "));

		Attribute[] path2 = possiblePaths.get(1);
		String path2String = pathToString(path2);
		System.out.println(path2String);
		assertTrue(path2String.equals(
				"PERSON.BLOODTYPE PERSON.FATHER PERSON.ID PERSON.BLOODTYPE "));
	}

	private String pathToString(Attribute[] possiblePath) {
		String path = "";

		for (Attribute attribute : possiblePath) {
			path = path
					+ (attribute.getTable().getName() + "."
							+ attribute.getAttribute().getName() + " ");
		}
		return path;

	}
}
