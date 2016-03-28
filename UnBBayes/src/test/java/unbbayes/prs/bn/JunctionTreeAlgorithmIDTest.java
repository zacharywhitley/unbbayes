/**
 * 
 */
package unbbayes.prs.bn;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import unbbayes.io.NetIO;
import unbbayes.prs.Edge;
import unbbayes.prs.Node;
import unbbayes.prs.bn.JunctionTreeAlgorithm.ProbabilisticNetworkClone;
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
	
	
	public final void testCloneProbabilisticNetwork() {
		ProbabilisticNetwork net = null;
		try {
			net = (ProbabilisticNetwork) new NetIO().load(new File(getClass().getClassLoader().getResource("./testCases/asia.net").toURI()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		assertNotNull(net);
		assertFalse(net.getNodes().isEmpty());
		assertEquals(net.getNodes().size(), net.getNodeCount());
		
		JunctionTreeAlgorithm algorithm = new JunctionTreeAlgorithm(net);
		algorithm.run();
		
		assertNotNull(algorithm.getJunctionTree());
		assertFalse(algorithm.getJunctionTree().getCliques().isEmpty());
		
		Map<String, float[]> marginals = new HashMap<String, float[]>();
		
		for (Node node : net.getNodes()) {
			if (node instanceof ProbabilisticNode) {
				ProbabilisticNode probabilisticNode = (ProbabilisticNode) node;
				float[] marginal = new float[probabilisticNode.getStatesSize()];
				for (int i = 0; i < probabilisticNode.getStatesSize(); i++) {
					marginal[i] = probabilisticNode.getMarginalAt(i);
				}
				marginals.put(probabilisticNode.getName(), marginal);
			}
		}
		
		assertEquals(net.getNodeCount(), marginals.size());
		
		ProbabilisticNetworkClone clone = algorithm.new ProbabilisticNetworkClone(net);
		assertNotNull(clone);
		assertEquals(clone.getNodes().size(), clone.getNodeCount());
		assertEquals(net.getNodeCount(), clone.getNodeCount());
		assertFalse(net == clone);
		
		for (Node node : net.getNodes()) {
			if (node instanceof ProbabilisticNode) {
				ProbabilisticNode oldNode = (ProbabilisticNode) node;
				ProbabilisticNode newNode = (ProbabilisticNode) clone.getNode(oldNode.getName());
				assertNotNull(newNode);
				for (int i = 0; i < oldNode.getProbabilityFunction().getVariablesSize(); i++) {
					assertEquals("[" + i + "]", oldNode.getProbabilityFunction().getVariableAt(i).getName(), newNode.getProbabilityFunction().getVariableAt(i).getName());
				}
			}
		}
		
		JunctionTreeAlgorithm algorithm2 = new JunctionTreeAlgorithm(clone);
		algorithm2.run();
		
		assertNotNull(algorithm2.getJunctionTree());
		assertFalse(algorithm2.getJunctionTree().getCliques().isEmpty());
		assertEquals(algorithm.getJunctionTree().getCliques().size(), algorithm2.getJunctionTree().getCliques().size());
		assertFalse(algorithm.getJunctionTree() == algorithm2.getJunctionTree());
		
		for (Node node : clone.getNodes()) {
			if (node instanceof ProbabilisticNode) {
				ProbabilisticNode probabilisticNode = (ProbabilisticNode) node;
				float[] marginal = marginals.get(probabilisticNode.getName());
				assertNotNull(probabilisticNode.getName() , marginal);
				for (int i = 0; i < marginal.length; i++) {
					assertEquals(probabilisticNode.getName() , marginal[i], probabilisticNode.getMarginalAt(i), 0.000005);
				}
			}
		}
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
