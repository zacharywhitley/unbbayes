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
	private static String DB_URL = "jdbc:mysql://localhost:3306/PathTesting?user=root&password=fds";

	IDBController relSchemaLoader;

	private Database db;

	private PathFinderAlgorithm pf;

	@Before
	public void setUp() throws Exception {
		relSchemaLoader = new DBControllerImp();
		relSchemaLoader.init(DB_URL);

		db = relSchemaLoader.getRelSchema();
		pf = new PathFinderAlgorithm();
	}

	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test the path finder algorithm when a table has two references to itself.
	 */
	@Test
	public void testDoubleReferenceToSameTable() {

		// Table PERSON
		Table tablePerson = db.getTable(6);
		// Column blood type
		Column column = tablePerson.getColumn(4);

		// Parent and child are the same but there are three paths.
		Attribute parent = new Attribute(tablePerson, column);
		Attribute child = new Attribute(tablePerson, column);

		// Path finder
		List<Attribute[]> possiblePaths = pf
				.getPossiblePaths(db, child, parent);

		assertTrue(possiblePaths.size() == 5);

		// First path
		validatePath(possiblePaths.get(0),"PERSON.BLOODTYPE PERSON.MOTHER PERSON.ID PERSON.BLOODTYPE ");
		validatePath(possiblePaths.get(1),"PERSON.BLOODTYPE PERSON.FATHER PERSON.ID PERSON.BLOODTYPE ");
		validatePath(possiblePaths.get(2),"PERSON.BloodType PERSON.id PERSON.Mother PERSON.BloodType ");
		validatePath(possiblePaths.get(3),"PERSON.BloodType PERSON.id PERSON.FATHER PERSON.BloodType ");
		validatePath(possiblePaths.get(4),"PERSON.BloodType PERSON.BloodType ");
				
	}

	private void validatePath(Attribute[] path1, String stringPath) {
		String path1String = pathToString(path1);
		System.out.println(path1String);
		assertTrue(path1String.equalsIgnoreCase(stringPath));
		
	}

	/**
	 * This evaluates a case when the relationships is a cascade. A->B->C. Only
	 * many-to-one and one-to-one relationships.
	 */
	@Test
	public void testDirectPath() {
		// Table A
		Table tableA = db.getTable(0);
		// Initial Column
		Column columnI = tableA.getColumn(0);

		// Table C
		Table tableC = db.getTable(2);
		// Ending Column
		Column columnE = tableC.getColumn(1);

		// First we evaluate A.I as a parent
		// Parent and child are the same but there are three paths.
		Attribute parent = new Attribute(tableA, columnI);
		Attribute child = new Attribute(tableC, columnE);

		// Apply the algorithm
		List<Attribute[]> possiblePaths = pf
				.getPossiblePaths(db, child, parent);

		assertTrue(possiblePaths.size() == 1);

		validatePath(possiblePaths.get(0), "A.Init A.BFK B.id B.CFK C.id C.End ");
		
	}

	/**
	 * This evaluates a case when the relationships is a cascade. A->B->C, but
	 * in this case, the path begins in C.
	 */
	@Test
	public void testInversePath() {

		// Table A
		Table tableA = db.getTable(0);
		// Initial Column
		Column columnI = tableA.getColumn(0);

		// Table C
		Table tableC = db.getTable(2);
		// Ending Column
		Column columnE = tableC.getColumn(1);

		// Second we evaluate A.I as a child.
		// Parent and child are the same but there are three paths.
		Attribute child1 = new Attribute(tableA, columnI);
		Attribute parent1 = new Attribute(tableC, columnE);

		List<Attribute[]> possiblePaths2 = pf.getPossiblePaths(db, child1,
				parent1);
		assertTrue(possiblePaths2.size() == 1);

		validatePath(possiblePaths2.get(0),"C.End C.id B.CFK B.id A.BFK A.Init ");
	}

	/**
	 * This evaluates the one-to-Many relationship. H->I<-J
	 */
	@Test
	public void testIndirectPath() {
		// Table H
		Table tableH = db.getTable(3);
		// Initial Column
		Column columnI = tableH.getColumn(0);

		// Table C
		Table tableJ = db.getTable(5);
		// Ending Column
		Column columnE = tableJ.getColumn(1);

		// Second we evaluate A.I as a child.
		// Parent and child are the same but there are three paths.
		Attribute parent1 = new Attribute(tableH, columnI);
		Attribute child1 = new Attribute(tableJ, columnE);

		List<Attribute[]> possiblePaths2 = pf.getPossiblePaths(db, child1,
				parent1);
		assertTrue(possiblePaths2.size() == 1);

		validatePath(possiblePaths2.get(0),"H.Init H.IFK I.id J.IFK J.End ");

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
