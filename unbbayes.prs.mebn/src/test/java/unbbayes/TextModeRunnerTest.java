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
import unbbayes.io.mebn.UbfIO;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.kb.powerloom.PowerLoomKB;

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


}
