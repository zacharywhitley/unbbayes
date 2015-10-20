/**
 * 
 */
package unbbayes.prs.bn;

import junit.framework.TestCase;
import unbbayes.prs.Edge;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.prs.id.DecisionNode;
import unbbayes.prs.id.UtilityNode;

/**
 * @author Shou Matsumoto
 *
 */
public class JunctionTreeAlgorithmIDTest extends TestCase {

	/**
	 * @param name
	 */
	public JunctionTreeAlgorithmIDTest(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	/**
	 * Test method for {@link JunctionTreeAlgorithm#sortDecisionNodes(unbbayes.prs.Graph)}
	 */
	public final void testReorderDecisionNodes() {
		ProbabilisticNetwork network = new ProbabilisticNetwork("ID");
		assertNotNull(network);
		
		/*
		 * build an ID with the following structure:
		 * 
		 * Utility(a)<-[DecisionA(x),DecisionA(y)]<-ProbabilisticNode(a)<-[DecisionB(x),DecisionB(y)]
		 * 
		 */
		
		UtilityNode util = new UtilityNode();
		util.setName("Utility__A");
		util.setDescription(util.getName());
		util.getProbabilityFunction().addVariable(util);
		network.addNode(util);
		
		DecisionNode decisionX = new DecisionNode();
		decisionX.setName("DecisionA__X");
		decisionX.setDescription(decisionX.getName());
		decisionX.appendState("choice1");
		decisionX.appendState("choice2");
		network.addNode(decisionX);
		try {
			network.addEdge(new Edge(decisionX, util));
		} catch (InvalidParentException e) {
			throw new RuntimeException(e);
		}
		
		DecisionNode decisionY = new DecisionNode();
		decisionY.setName("DecisionA__Y");
		decisionY.setDescription(decisionY.getName());
		decisionY.appendState("choice1");
		decisionY.appendState("choice2");
		network.addNode(decisionY);
		try {
			network.addEdge(new Edge(decisionY, util));
		} catch (InvalidParentException e) {
			throw new RuntimeException(e);
		}
		
		ProbabilisticNode probNode = new ProbabilisticNode();
		probNode.appendState("state1");
		probNode.setName("ProbabilisticNode__A");
		probNode.setDescription(probNode.getName());
		PotentialTable table = probNode.getProbabilityFunction();
		table.addVariable(probNode);
		table.setValue(0, 1);
		network.addNode(probNode);
		try {
			network.addEdge(new Edge(probNode, decisionX));
			network.addEdge(new Edge(probNode, decisionY));
		} catch (InvalidParentException e) {
			throw new RuntimeException(e);
		}
		
		
		decisionX = new DecisionNode();
		decisionX.setName("DecisionB__X");
		decisionX.setDescription(decisionX.getName());
		decisionX.appendState("choice1");
		decisionX.appendState("choice2");
		network.addNode(decisionX);
		try {
			network.addEdge(new Edge(decisionX, probNode));
		} catch (InvalidParentException e) {
			throw new RuntimeException(e);
		}
		
		decisionY = new DecisionNode();
		decisionY.setName("DecisionB__Y");
		decisionY.setDescription(decisionY.getName());
		decisionY.appendState("choice1");
		decisionY.appendState("choice2");
		network.addNode(decisionY);
		try {
			network.addEdge(new Edge(decisionY, probNode));
		} catch (InvalidParentException e) {
			throw new RuntimeException(e);
		}
		
		JunctionTreeAlgorithm algorithm = new JunctionTreeAlgorithm(network);
		
		// make sure the algorithm requires explicit total order of decision nodes
		algorithm.setDecisionTotalOrderRequired(true);
		assertTrue(algorithm.isDecisionTotalOrderRequired());
		
		try {
			algorithm.run();
			fail("Should throw exception because we did not guarantee total order of decision nodes");
		} catch (Exception e) {
			assertTrue(e.getMessage().toLowerCase().contains("there is no ordenation of the decision variables"));
		}
		
		// make sure the algorithm will take care of total ordering if it was not explicit
		algorithm.setDecisionTotalOrderRequired(false);
		assertFalse(algorithm.isDecisionTotalOrderRequired());
		
		
		try {
			algorithm.run();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}

}
