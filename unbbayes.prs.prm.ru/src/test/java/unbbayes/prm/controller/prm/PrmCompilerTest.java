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
import unbbayes.prm.model.AggregateFunctionName;
import unbbayes.prm.model.Attribute;
import unbbayes.prm.model.ParentRel;
import unbbayes.prs.Node;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.exception.InvalidParentException;

public class PrmCompilerTest {

	Logger log = Logger.getLogger(PrmCompilerTest.class);

	private IDBController dbController;
	private PrmCompiler compiler;
	private IPrmController prmController;

	private static String DB_URL = "jdbc:mysql://localhost:3306/MDA?user=root&password=fds";

	Attribute attIsOfInterest;
	private Attribute idCol;

	private Attribute htcAttribute;

	private Attribute attRoute;

	private int idRelationship = 0;

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

		// Query Attributes.
		attIsOfInterest = getAttribute("SHIP", "isOfInterest");
		idCol = getAttribute("SHIP", "id");
		htcAttribute = getAttribute("SHIP", "hasTerroristCrew");
		attRoute = getAttribute("ROUTE", "name");

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
				.compile(attIsOfInterest.getTable(), idCol.getAttribute(), "1",
						attIsOfInterest.getAttribute(), "N");

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
				.compile(attIsOfInterest.getTable(), idCol.getAttribute(), "2",
						attIsOfInterest.getAttribute(), "N");

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

		// MEETING
		createNewRelationShipForMeeting();
		// HAS TERRORIST
		createIntrisecRelForHasTerroristCrew();

		System.out.println("Compiling");

		String[] nodeNames = { "SHIP 1 isOfInterest",
				"SHIP 1 hasTerroristCrew", "SHIP 2 hasTerroristCrew",
				"SHIP 2 isOfInterest" };

		// FISRT CASE: The query is on the ship with id=1
		// attribute=isOfInterest.
		ProbabilisticNetwork resultNetwork = (ProbabilisticNetwork) compiler
				.compile(attIsOfInterest.getTable(), idCol.getAttribute(), "1",
						attIsOfInterest.getAttribute(), "N");

		// Validate Result
		validateResult(resultNetwork, nodeNames);

		// SECOND CASE: The query is on the ship with id=1
		// attribute=hasTerroristCrew.
		resultNetwork = (ProbabilisticNetwork) compiler.compile(
				attIsOfInterest.getTable(), idCol.getAttribute(), "1",
				htcAttribute.getAttribute(), "N");
		validateResult(resultNetwork, nodeNames);

		// THIRD CASE: The query is on the ship with id=1
		// attribute=hasTerroristCrew.
		resultNetwork = (ProbabilisticNetwork) compiler.compile(
				attIsOfInterest.getTable(), idCol.getAttribute(), "2",
				htcAttribute.getAttribute(), "N");
		validateResult(resultNetwork, nodeNames);

		// FOURTH CASE: The query is on the ship with id=2
		// attribute=isOfInterest.
		resultNetwork = (ProbabilisticNetwork) compiler.compile(
				attIsOfInterest.getTable(), idCol.getAttribute(), "2",
				attIsOfInterest.getAttribute(), "N");

		// Validate Result
		validateResult(resultNetwork, nodeNames);
	}

	/**
	 * This is to test for Route.
	 * 
	 * Relationship 1: SHIP.isOfInterest SHIP.route ROUTE.id ROUTE.name
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRoute() throws Exception {

		// MEETING
		// createNewRelationShipForMeeting();
		// HAS TERRORIST
		// createIntrisecRelForHasTerroristCrew();
		// ROUTE
		createRouteRel();

		System.out.println("Compiling");

		// The query is on the ship with id=1
		ProbabilisticNetwork resultNetwork = (ProbabilisticNetwork) compiler
				.compile(attIsOfInterest.getTable(), idCol.getAttribute(), "1",
						attIsOfInterest.getAttribute(), "N");

		// Validate Result
		String[] nodeNames = { "SHIP 1 isOfInterest", "ROUTE 0 name" };
		validateResult(resultNetwork, nodeNames);
	}

	/**
	 * Test route with multiples parents.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRouteMultiParents() throws Exception {
		createRouteRel();

		System.out.println("Compiling");

		// The query is on the ship with id=4
		ProbabilisticNetwork resultNetwork = (ProbabilisticNetwork) compiler
				.compile(attIsOfInterest.getTable(), idCol.getAttribute(), "4",
						attIsOfInterest.getAttribute(), "N");

		// Validate Result
		String[] nodeNames = new String[] { "SHIP 4 isOfInterest",
				"SHIP 5 isOfInterest", "ROUTE 1 name" };
		validateResult(resultNetwork, nodeNames);

	}

	private void createRouteRel() throws Exception {
		Attribute[] path = createRoutePath();

		System.out.println("Creating route relationship");
		String idRel = idRelationship++ + "";

		// Parent rel
		createRel(idRel, path, attIsOfInterest, attRoute);
	}

	private void createIntrisecRelForHasTerroristCrew() throws Exception {
		Attribute[] path = createHasTerroristPath();

		System.out.println("Creating hasTerroristCrew relationship");
		String idRel = idRelationship++ + "";

		createRel(idRel, path, attIsOfInterest, htcAttribute);
	}

	private void createRel(String idRel, Attribute[] path, Attribute parent,
			Attribute child) throws InvalidParentException {
		// Parent rel
		ParentRel newRel = new ParentRel(parent, child);
		newRel.setAggregateFunction(AggregateFunctionName.add);
		newRel.setPath(path);
		newRel.setIdRelationsShip(idRel);
		prmController.addParent(newRel);

		// Nodes for cpts
		ProbabilisticNode parentNode = new ProbabilisticNode();
		parentNode.setDescription(idRel);
		ProbabilisticNode childNode = new ProbabilisticNode();
		childNode.setDescription(idRel);

		// CPT for parent
		if (prmController.getCPD(parent) == null) {
			prmController.setCPD(parent,
					createIsOfInterestParentTable(parentNode));
		} else {
			// Parent States
			String[] states = dbController.getPossibleValues(parent);
			for (String state : states) {
				parentNode.appendState(state);
			}
		}

		// Child States
		String[] childStates = dbController.getPossibleValues(child);
		for (String state : childStates) {
			childNode.appendState(state);
		}

		// Child
		childNode.addParent(parentNode);

		// Create the CPTs
		PotentialTable childTable = childNode.getProbabilityFunction();
		childTable.addVariable(childNode);
		childTable.addVariable(parentNode);

		// Init CPTs
		childTable.setValue(0, 0.5f);
		childTable.setValue(1, 0.5f);
		childTable.setValue(2, 0.5f);
		childTable.setValue(3, 0.5f);

		PotentialTable cpts[] = { childTable };
		// CPTs
		System.out.println("CPTs");
		prmController.setCPD(child, cpts);

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
		String idRel = idRelationship++ + "";
		ParentRel newRel = new ParentRel(attIsOfInterest, attIsOfInterest);
		newRel.setPath(path);
		newRel.setIdRelationsShip(idRel);
		prmController.addParent(newRel);

		// Nodes for cpts
		ProbabilisticNode parentNode = new ProbabilisticNode();
		parentNode.setDescription(idRel);
		ProbabilisticNode childNode = new ProbabilisticNode();
		childNode.setDescription(idRel);

		PotentialTable parentTable = createIsOfInterestParentTable(parentNode);

		// States
		String[] states = dbController.getPossibleValues(attIsOfInterest);
		for (String state : states) {
			childNode.appendState(state);
		}
		// Parent
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

		PotentialTable cpts[] = { parentTable, childTable };
		// CPTs
		System.out.println("CPTs");
		prmController.setCPD(attIsOfInterest, cpts);
	}

	private PotentialTable createIsOfInterestParentTable(
			ProbabilisticNode parentNode) {
		// States
		String[] states = dbController.getPossibleValues(attIsOfInterest);
		for (String state : states) {
			parentNode.appendState(state);
		}

		// Create the CPTs
		PotentialTable parentTable = parentNode.getProbabilityFunction();
		parentTable.addVariable(parentNode);

		// Init cpts
		parentTable.setValue(0, 0.5f);
		parentTable.setValue(1, 0.5f);

		return parentTable;
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
		Attribute pt1 = attIsOfInterest;
		Attribute pt2 = idCol;
		Attribute pt3 = getAttribute("MEETING", "ship1");
		Attribute pt4 = getAttribute("MEETING", "ship2");
		Attribute pt5 = idCol;
		Attribute pt6 = attIsOfInterest;
		return new Attribute[] { pt1, pt2, pt3, pt4, pt5, pt6 };
	}

	/**
	 * Create a path for route: SHIP.isOfInterest SHIP.route ROUTE.id ROUTE.name
	 * 
	 * @return
	 * @throws Exception
	 */

	private Attribute[] createRoutePath() throws Exception {
		Attribute pt1 = attIsOfInterest;
		Attribute pt2 = getAttribute("SHIP", "route");
		Attribute pt3 = getAttribute("ROUTE", "id");
		Attribute pt4 = attRoute;
		return new Attribute[] { pt1, pt2, pt3, pt4 };
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
		Attribute pt1 = attIsOfInterest;
		Attribute pt2 = htcAttribute;
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
