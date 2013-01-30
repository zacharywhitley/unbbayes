package unbbayes.prm.controller.prm;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

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
import unbbayes.prs.Node;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;

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

	/**
	 * This is to test the FK to FK relationship, specifically for MEETING
	 * table. In this case, the query is on the parent.
	 * 
	 * Relationship 1: SHIP.isOfInterest SHIP.id MEETING.ship1 MEETING.ship2
	 * SHIP.id SHIP.isOfInterest
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSimpleFKtoFKParentRelationship() throws Exception {

		createNewRelationShipForMeeting();

		System.out.println("Compiling");

		// The query is on the ship with id=1.
		ProbabilisticNetwork resultNetwork = (ProbabilisticNetwork) compiler
				.compile(att.getTable(), idCol.getAttribute(), "1",
						att.getAttribute(), "N");

		// Validate Result
		String[] nodeNames = { "SHIP 1 isOfInterest", "SHIP 2 isOfInterest" };
		validateResult(resultNetwork, nodeNames);
	}

	/**
	 * This is to test the FK to FK relationship, specifically for MEETING
	 * table. In this case, the query is on the child.
	 * 
	 * Relationship 1: SHIP.isOfInterest SHIP.id MEETING.ship1 MEETING.ship2
	 * SHIP.id SHIP.isOfInterest
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSimpleFKtoFKChildRelationship() throws Exception {

		createNewRelationShipForMeeting();

		System.out.println("Compiling");

		// The query is on the ship with id=2
		ProbabilisticNetwork resultNetwork = (ProbabilisticNetwork) compiler
				.compile(att.getTable(), idCol.getAttribute(), "2",
						att.getAttribute(), "N");

		// Validate Result
		String[] nodeNames = { "SHIP 1 isOfInterest", "SHIP 2 isOfInterest" };
		validateResult(resultNetwork, nodeNames);

	}

	/**
	 * This is to test the FK to FK relationship and a intrinsic relationship.
	 * 
	 * Relationship 1: SHIP.isOfInterest SHIP.id MEETING.ship1 MEETING.ship2
	 * SHIP.id SHIP.isOfInterest
	 * 
	 * Relationship 2: SHIP.isOfInterest SHIP.hasTerrowristCrew
	 * 
	 * @throws Exception
	 */
	@Test
	public void testIntrinsicRel() throws Exception {

		createNewRelationShipForMeeting();
		createIntrisecRelForHasTerroristCrew();

		System.out.println("Compiling");

		// The query is on the ship with id=1 attribute=isOfInterest.
		ProbabilisticNetwork resultNetwork = (ProbabilisticNetwork) compiler
				.compile(att.getTable(), idCol.getAttribute(), "1",
						att.getAttribute(), "N");

		// Validate Result
		String[] nodeNames = { "SHIP 1 isOfInterest",
				"SHIP 1 hasTerroristCrew", "SHIP 2 hasTerroristCrew",
				"SHIP 2 isOfInterest" };
		validateResult(resultNetwork, nodeNames);

	}

	private void createIntrisecRelForHasTerroristCrew() throws Exception {
		Attribute[] path = createHasTerroristPath();

		System.out.println("Creating hasTerroristCrew relationship");
		String idRel = "1";

		// Has terrorist crew Attribute.
		Attribute htcAttribute = getAttribute("SHIP", "hasTerroristCrew");

		// Parent rel
		ParentRel newRel = new ParentRel(att, htcAttribute);
		newRel.setPath(path);
		newRel.setIdRelationsShip(idRel);
		prmController.addParent(newRel);

		// Nodes for cpts
		ProbabilisticNode parentNode = new ProbabilisticNode();
		parentNode.setDescription(idRel);
		ProbabilisticNode childNode = new ProbabilisticNode();
		childNode.setDescription(idRel);

		// Parent States
		String[] states = dbController.getPossibleValues(att);
		for (String state : states) {
			parentNode.appendState(state);
		}
		// Child States
		String[] childStates = dbController.getPossibleValues(htcAttribute);
		for (String state : childStates) {
			childNode.appendState(state);
		}

		// Child
		childNode.addParent(parentNode);

		// Create the CPTs
		PotentialTable childTable = childNode.getProbabilityFunction();
		childTable.addVariable(childNode);
		childTable.addVariable(parentNode);

		// Init cpts
		childTable.setValue(0, 0.5f);
		childTable.setValue(1, 0.5f);
		childTable.setValue(2, 0.5f);
		childTable.setValue(3, 0.5f);

		PotentialTable cpts[] = { childTable };
		// CPTs
		System.out.println("CPTs");
		prmController.setCPD(htcAttribute, cpts);
	}

	/**
	 * Validate if every node name has a node in the network.
	 * 
	 * @param resultNetwork
	 * @param nodeNames
	 */
	private void validateResult(ProbabilisticNetwork resultNetwork,
			String[] nodeNames) {
		ArrayList<Node> nodes = new ArrayList<Node>(resultNetwork.getNodes());

		assertTrue(nodes.size() == nodeNames.length);

		for (String nodeName : nodeNames) {
			boolean nodeExists = false;

			for (Node node : nodes) {
				if (node.getName().equals(nodeName)) {
					nodeExists = true;

					// To make the algorithm faster.
					nodes.remove(node);
					break;
				}
			}
			assertTrue(nodeExists);
		}
	}

	private void createNewRelationShipForMeeting() throws Exception {
		Attribute[] path = createMeetingPath();

		System.out.println("Creating meeting relationship");
		// Registry relationships.
		// MEETING Relationship.
		String idRel = "0";
		ParentRel newRel = new ParentRel(att, att);
		newRel.setPath(path);
		newRel.setIdRelationsShip(idRel);
		prmController.addParent(newRel);

		// Nodes for cpts
		ProbabilisticNode parentNode = new ProbabilisticNode();
		parentNode.setDescription(idRel);
		ProbabilisticNode childNode = new ProbabilisticNode();
		childNode.setDescription(idRel);

		// States
		String[] states = dbController.getPossibleValues(att);
		for (String state : states) {
			parentNode.appendState(state);
			childNode.appendState(state);
		}
		// Parent
		childNode.addParent(parentNode);

		// Create the CPTs
		PotentialTable parentTable = parentNode.getProbabilityFunction();
		parentTable.addVariable(parentNode);
		PotentialTable childTable = childNode.getProbabilityFunction();
		childTable.addVariable(childNode);
		childTable.addVariable(parentNode);

		// Init cpts
		parentTable.setValue(0, 0.5f);
		parentTable.setValue(1, 0.5f);
		childTable.setValue(0, 0.5f);
		childTable.setValue(1, 0.5f);
		childTable.setValue(2, 0.5f);
		childTable.setValue(3, 0.5f);

		PotentialTable cpts[] = { parentTable, childTable };
		// CPTs
		System.out.println("CPTs");
		prmController.setCPD(att, cpts);
	}

	/**
	 * Create a path for meeting: SHIP.isOfInterest SHIP.id MEETING.ship1
	 * MEETING.ship2 SHIP.id SHIP.isOfInterest
	 * 
	 * @return
	 * @throws Exception
	 */
	private Attribute[] createMeetingPath() throws Exception {
		// PATH
		Attribute pt1 = att;
		Attribute pt2 = idCol;
		Attribute pt3 = getAttribute("MEETING", "ship1");
		Attribute pt4 = getAttribute("MEETING", "ship2");
		Attribute pt5 = pt2;
		Attribute pt6 = pt1;
		return new Attribute[] { pt1, pt2, pt3, pt4, pt5, pt6 };
	}

	/**
	 * Create a path for meeting: SHIP.isOfInterest SHIP.hasTerrorisCrew.
	 * 
	 * @return
	 * @throws Exception
	 */
	private Attribute[] createHasTerroristPath() throws Exception {
		// PATH
		// PATH
		Attribute pt1 = att;
		Attribute pt2 = getAttribute("SHIP", "hasTerroristCrew");
		return new Attribute[] { pt1, pt2 };
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
