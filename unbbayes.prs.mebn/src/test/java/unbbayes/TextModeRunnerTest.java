/**
 * 
 */
package unbbayes;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;
import unbbayes.TextModeRunner.QueryNodeNameAndArguments;
import unbbayes.io.NetIO;
import unbbayes.io.mebn.UbfIO;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.kb.powerloom.PowerLoomKB;
import unbbayes.prs.mebn.ssbn.ContextFatherSSBNNode;

/**
 * @author Shou Matsumoto
 *
 */
public class TextModeRunnerTest extends TestCase {

	private TextModeRunner textModeRunner;
	private KnowledgeBase kb;
	private MultiEntityBayesianNetwork mebn;

	/**
	 * @param name
	 */
	public TextModeRunnerTest(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		textModeRunner = new TextModeRunner();
		
		// load ubf/owl
		
		UbfIO ubf = UbfIO.getInstance();
		mebn = ubf.loadMebn(new File("src/test/resources/mebn/VehicleIdentification.ubf"));

		
		// initialize kb
		
		kb = PowerLoomKB.getNewInstanceKB();
		kb = textModeRunner.createKnowledgeBase(kb, mebn);
		// load kb
		kb.loadModule(new File("src/test/resources/mebn/knowledgeBase/VehicleIdentification.plm"), true);
		
		
		kb = textModeRunner.fillFindings(mebn,kb);
		
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Test method for {@link unbbayes.TextModeRunner#executeQueryLaskeyAlgorithm(java.util.List, unbbayes.prs.mebn.kb.KnowledgeBase, unbbayes.prs.mebn.MultiEntityBayesianNetwork)}.
	 */
	public final void testCallLaskeyAlgorithm() {
		String nameOfResidentNodeInQuery = "ObjectType";
		String argument = "Obj1";
		
		ProbabilisticNetwork net = null;
		try {
			// single query
			net = this.textModeRunner.callLaskeyAlgorithm(mebn, kb, Collections.singletonList(textModeRunner.new QueryNodeNameAndArguments(nameOfResidentNodeInQuery, argument)));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		// the name of generated node shall be nameOfResidentNodeInQuery + "__" + argument
		ProbabilisticNode node = (ProbabilisticNode) net.getNode(nameOfResidentNodeInQuery + "__" + argument);
		assertNotNull("Could not find node " + nameOfResidentNodeInQuery + "__" + argument, node);
		
		// check states and marginals
		assertEquals(3, node.getStatesSize());
		assertTrue(node + "." + node.getStateAt(0) + " is " +  node.getMarginalAt(0)*100 + ", but expected 1.81", 1.80f < node.getMarginalAt(0)*100 && node.getMarginalAt(0) < 1.82f);
		assertTrue(node + "." + node.getStateAt(1) + " is " +  node.getMarginalAt(1)*100 + ", but expected 3.28", 3.27f < node.getMarginalAt(1)*100 && node.getMarginalAt(1) < 3.29f);
		assertTrue(node + "." + node.getStateAt(2) + " is " +  node.getMarginalAt(2)*100 + ", but expected 94.91", 94.90f < node.getMarginalAt(2)*100 && node.getMarginalAt(2) < 94.92f);
		
	}
	
	/**
	 * Test method for {@link unbbayes.TextModeRunner#executeQueryLaskeyAlgorithm(java.util.List, unbbayes.prs.mebn.kb.KnowledgeBase, unbbayes.prs.mebn.MultiEntityBayesianNetwork)}.
	 */
	public final void testCallLaskeyAlgorithmMultipleQuery() {
		String nameOfResidentNodeInQuery = "ObjectType";	
		String argument1 = "Obj1";	// query 1 = ObjectType(Obj1)
		String argument2 = "Obj2";	// query 2 = ObjectType(Obj2); we know that this is disconnected to query 1
		
		// generate set of queries
		Set<QueryNodeNameAndArguments> queries = new HashSet<TextModeRunner.QueryNodeNameAndArguments>();
		queries.add(textModeRunner.new QueryNodeNameAndArguments(nameOfResidentNodeInQuery, argument1));	// ObjectType(Obj1)
		queries.add(textModeRunner.new QueryNodeNameAndArguments(nameOfResidentNodeInQuery, argument2));	// ObjectType(Obj2)

		// this is the return
		ProbabilisticNetwork net = null;
		try {
			// multiple query (2 query nodes)
			net = this.textModeRunner.callLaskeyAlgorithm(mebn, kb, queries);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		// check query 1
		// the name of generated node shall be nameOfResidentNodeInQuery + "__" + argument
		ProbabilisticNode node = (ProbabilisticNode) net.getNode(nameOfResidentNodeInQuery + "__" + argument1);
		assertNotNull("Could not find node " + nameOfResidentNodeInQuery + "__" + argument1, node);
		
		// check states and marginals of query 1
		assertEquals(3, node.getStatesSize());
		assertTrue(node + "." + node.getStateAt(0) + " is " +  node.getMarginalAt(0)*100 + ", but expected 1.81", 1.80f < node.getMarginalAt(0)*100 && node.getMarginalAt(0) < 1.82f);
		assertTrue(node + "." + node.getStateAt(1) + " is " +  node.getMarginalAt(1)*100 + ", but expected 3.28", 3.27f < node.getMarginalAt(1)*100 && node.getMarginalAt(1) < 3.29f);
		assertTrue(node + "." + node.getStateAt(2) + " is " +  node.getMarginalAt(2)*100 + ", but expected 94.91", 94.90f < node.getMarginalAt(2)*100 && node.getMarginalAt(2) < 94.92f);
		
		// do the same check for query 2
		node = (ProbabilisticNode) net.getNode(nameOfResidentNodeInQuery + "__" + argument2);
		assertNotNull("Could not find node " + nameOfResidentNodeInQuery + "__" + argument2, node);
		
		// check states and marginals of query 2
		assertEquals(3, node.getStatesSize());
		assertTrue(node + "." + node.getStateAt(0) + " is " +  node.getMarginalAt(0)*100 + ", but expected 86.9", 86.8f < node.getMarginalAt(0)*100 && node.getMarginalAt(0) < 87.0f);
		assertTrue(node + "." + node.getStateAt(1) + " is " +  node.getMarginalAt(1)*100 + ", but expected 11.91", 11.90f < node.getMarginalAt(1)*100 && node.getMarginalAt(1) < 11.92f);
		assertTrue(node + "." + node.getStateAt(2) + " is " +  node.getMarginalAt(2)*100 + ", but expected 1.19", 1.18f < node.getMarginalAt(2)*100 && node.getMarginalAt(2) < 1.20f);
		
		
	}
	
	/**
	 * This method checks if multiplexor nodes (which implements some sort of reference uncertainty in context nodes)
	 * are not duplicated when same context nodes happens in 2 MFrags.
	 * @throws Exception 
	 */
	public final void testDuplicateMultiplexor() throws Exception {
		TextModeRunner textModeRunner = new TextModeRunner();
		
		// load ubf/owl
		UbfIO ubf = UbfIO.getInstance();
		MultiEntityBayesianNetwork mebn = ubf.loadMebn(new File("src/test/resources/duplicateMultiplexor.ubf"));
		
		// initialize kb
		KnowledgeBase kb = PowerLoomKB.getNewInstanceKB();
		kb = textModeRunner.createKnowledgeBase(kb, mebn);
		
		// make a query to Main(E1)
		
		// check that the node and entity instance to be queried are present
		assertNotNull(mebn.getObjectEntityContainer().getEntityInstanceByName("E1"));
		assertNotNull(mebn.getDomainResidentNode("Main"));
		
		// single query
		ProbabilisticNetwork returnedNet = this.textModeRunner.callLaskeyAlgorithm(
				mebn, 
				kb, 
				Collections.singletonList(textModeRunner.new QueryNodeNameAndArguments("Main", "E1"))
			);
		assertNotNull(returnedNet);
		
		// names of the multiplexor must not be something like CX1 or CX2
		if (ContextFatherSSBNNode.isToGenerateSuggestiveProbabilisticNodeName()) {
			assertNull(returnedNet.getNode("CX2"));
			assertNull(returnedNet.getNode("CX1"));
		}
		
		
		// make sure the CPT of the multiplexed node (child of multiplexor) is correct
		
		// load the ground truth network for comparison
		ProbabilisticNetwork groundTruth = (ProbabilisticNetwork) new NetIO().load(new File("src/test/resources/singleMultiplexor.net"));
		assertNotNull(groundTruth);
		
		// extract the children of multiplexor nodes from generated network
		ProbabilisticNode node1 = (ProbabilisticNode) returnedNet.getNode("Resident1__E1");
		assertNotNull(node1);
		ProbabilisticNode node2 = (ProbabilisticNode) returnedNet.getNode("Resident2__E1");
		assertNotNull(node2);

		// extract the children of multiplexor nodes from ground truth
		ProbabilisticNode groundTruthNode1 = (ProbabilisticNode) groundTruth.getNode("Resident1__E1");
		assertNotNull(groundTruthNode1);
		ProbabilisticNode groundTruthNode2 = (ProbabilisticNode) groundTruth.getNode("Resident2__E1");
		assertNotNull(groundTruthNode2);
		
		// make sure the CPTs are the same
		assertEquals(groundTruthNode1.getProbabilityFunction().tableSize(), node1.getProbabilityFunction().tableSize());
		for (int i = 0; i < groundTruthNode1.getProbabilityFunction().tableSize(); i++) {
			// 0.00005 is the error margin for comparing two float numbers
			assertEquals("[" + i + "]", groundTruthNode1.getProbabilityFunction().getValue(i), node1.getProbabilityFunction().getValue(i), 0.00005);	
		}
		assertEquals(groundTruthNode2.getProbabilityFunction().tableSize(), node2.getProbabilityFunction().tableSize());
		for (int i = 0; i < groundTruthNode2.getProbabilityFunction().tableSize(); i++) {
			// 0.00005 is the error margin for comparing two float numbers
			assertEquals("[" + i + "]", groundTruthNode2.getProbabilityFunction().getValue(i), node2.getProbabilityFunction().getValue(i), 0.00005);
		}
		
		// if multiplexor is not duplicated, then the returned network must have 8 nodes (9 if duplicated)
		assertEquals(8, returnedNet.getNodeCount());
		
	}
	


}
