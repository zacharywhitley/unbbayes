package unbbayes.prm.controller.prm;

import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import unbbayes.prm.controller.dao.IDBController;
import unbbayes.prm.controller.dao.imp.DBControllerImp;
import unbbayes.prm.model.Attribute;
import unbbayes.prm.model.ParentRel;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNetwork;

public class PrmCompilerTest {

	Logger log = Logger.getLogger(PrmCompilerTest.class);

	private IDBController dbController;
	private PrmCompiler compiler;
	private IPrmController prmController;

	private static String DB_URL = "jdbc:mysql://localhost:3306/MDA?user=root&password=fds";

	Attribute att;
	private Attribute idCol;

	@Before
	public void setUp() throws Exception {
		System.out.println("Start");

		// DB Controller.
		dbController = new DBControllerImp();
		dbController.init(DB_URL);

		// PRM Controller
		prmController = new PrmController();

		// PRM compiler.
		compiler = new PrmCompiler(prmController, dbController);

		att = getAttribute("SHIP", "isOfInterest");
		idCol = getAttribute("SHIP", "id");

		System.out.println("Seted up");
	}

	@After
	public void tearDown() throws Exception {
		dbController.end();
	}

	@Test
	public void test() throws Exception {
		// PATH
		// SHIP.isOfInterest SHIP.id MEETING.ship1 MEETING.ship2 SHIP.id
		// SHIP.isOfInterest
		Attribute pt1 = att;
		Attribute pt2 = idCol;
		Attribute pt3 = getAttribute("MEETING", "ship1");
		Attribute pt4 = getAttribute("MEETING", "ship2");
		Attribute pt5 = pt2;
		Attribute pt6 = pt1;

		Attribute[] path = { pt1, pt2, pt3, pt4, pt5, pt6 };

		System.out.println("Creating relationships");
		// Registry relationships.
		// MEETING Relationship.
		ParentRel newRel = new ParentRel(att, att);
		newRel.setPath(path);
		prmController.addParent(newRel);

		// TODO create the cpts
		PotentialTable parentTable = null;
		PotentialTable childTable = null;

		PotentialTable cpts[] = { parentTable, childTable };
		// CPTs
		System.out.println("CPTs");
		prmController.setCPD(att, cpts);

		System.out.println("Compiling");

		ProbabilisticNetwork resultNetwork = (ProbabilisticNetwork) compiler
				.compile(att.getTable(), idCol.getAttribute(), "1",
						att.getAttribute(), "N");

	}

	/**
	 * Fin an attribute in a DB.
	 * 
	 * @param tableName
	 * @param colName
	 * @return
	 * @throws Exception
	 */
	private Attribute getAttribute(String tableName, String colName)
			throws Exception {
		Database relSchema = dbController.getRelSchema();
		Table[] tables = relSchema.getTables();

		for (Table table : tables) {
			if (table.getName().equals(tableName)) {
				Column[] columns = table.getColumns();

				for (Column column : columns) {
					if (column.getName().equals(colName)) {
						return new Attribute(table, column);
					}
				}
			}
		}
		throw new Exception("Atribute not fund." + tableName + "." + colName);
	}

}
