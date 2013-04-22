/**
 * 
 */
package unbbayes.prs.bn.cpt.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import junit.framework.TestCase;
import unbbayes.gui.table.GUIPotentialTable;
import unbbayes.io.BaseIO;
import unbbayes.io.NetIO;
import unbbayes.prs.INode;
import unbbayes.prs.bn.JunctionTreeAlgorithm;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.ProbabilisticTable;

/**
 * @author Shou Matsumoto
 *
 */
public class InCliqueConditionalProbabilityExtractorTest extends TestCase {

	private ProbabilisticNetwork net;
	private JunctionTreeAlgorithm algorithm;
	private InCliqueConditionalProbabilityExtractor condProbExtractor;

	/**
	 * @param name
	 */
	public InCliqueConditionalProbabilityExtractorTest(String name) {
		super(name);
	}

	/**
	 * Load network and compile it using junction tree algorithm. The network is: <br/>
	 * D -> F <- G 	<br/>
	 * |			<br/>
	 * v			<br/>
	 * E			<br/>
	 * <br/>
	 * The cliques/separators are:  (DFG) - [D] - (DE) <br/>
	 * The conditional probability for F given D and G is: <br/>
	 * -----------------------------------------------------------	<br/>
	 * | _ __ g 1 _ _ _ _ | _ __ g 2 _ _ _ _ | _ __ g 3 _ _ _ __ |	<br/>
	 * -----------------------------------------------------------	<br/>
	 * | d1 | d 2_ | d 3_ | d1 | d 2_ | d 3_ | d 1 | d 2_  | d3  |	<br/>
	 * -----------------------------------------------------------	<br/>
	 * |0.7 | 0.05 | 0.10 | 0.1 | 0.05 | 0.60 | 0.4 | 0.05 | 0.35|	<br/>
	 * |0.2 | 0.90 | 0.30 | 0.2 | 0.90 | 0.30 | 0.2 | 0.90 | 0.30|	<br/>
	 * |0.1 | 0.05 | 0.60 | 0.7 | 0.05 | 0.10 | 0.4 | 0.05 | 0.35|	<br/>
	 * @see junit.framework.TestCase#setUp()
	 * 
	 */
	protected void setUp() throws Exception {
		super.setUp();
		// load network from file
		BaseIO io = new NetIO();
		net = (ProbabilisticNetwork) io.load(new File(this.getClass().getClassLoader().getResource("testCases/testmodel_defg.net").toURI()));
		// start up algorithm for compiling net
		algorithm = new JunctionTreeAlgorithm((ProbabilisticNetwork) net);
		// compile net
		algorithm.run();
		condProbExtractor = (InCliqueConditionalProbabilityExtractor) InCliqueConditionalProbabilityExtractor.newInstance();
		
		
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public static void intfill(int[] array, int value) {
	    int len = array.length;
	    if (len > 0)
	    array[0] = value;
	    for (int i = 1; i < len; i += i)
	        System.arraycopy( array, 0, array, i,
	            ((len - i) < i) ? (len - i) : i);
	}
	
	/**
	 * Test method for {@link unbbayes.prs.bn.cpt.impl.InCliqueConditionalProbabilityExtractor#buildCondicionalProbability(unbbayes.prs.INode, java.util.List, unbbayes.prs.Graph, unbbayes.util.extension.bn.inference.IInferenceAlgorithm)}.
	 */
	public final void testBuildCondicionalProbability() {
		int array[] = new int[10];
		Random r = new Random();
		long stamp1;
		long stamp0;
		
		
		// prepare parent nodes
		List<INode> parents = new ArrayList<INode>();
		// main node
		ProbabilisticNode nodeF = (ProbabilisticNode) net.getNode("F");
		
		// no parent
		ProbabilisticTable table = (ProbabilisticTable) condProbExtractor.buildCondicionalProbability(nodeF, parents, net, algorithm);
		assertNotNull(table);
		assertEquals(3, table.tableSize());
		assertTrue("value = " + table.getValue(0), .284 < table.getValue(0) && table.getValue(0) < .286);
		assertTrue("value = " + table.getValue(1), .429 < table.getValue(1) && table.getValue(1) < .431);
		assertTrue("value = " + table.getValue(2), .284 < table.getValue(2) && table.getValue(2) < .286);
		
		new GUIPotentialTable(table).showTable("Test");
		
		// D as parent
		parents.add(net.getNode("D"));
		table = (ProbabilisticTable) condProbExtractor.buildCondicionalProbability(nodeF, parents, net, algorithm);
		assertNotNull(table);
		assertEquals(9, table.tableSize());
		// D = d1
		assertTrue("value = " + table.getValue(0), .399 < table.getValue(0) && table.getValue(0) < .401);
		assertTrue("value = " + table.getValue(1), .199 < table.getValue(1) && table.getValue(1) < .201);
		assertTrue("value = " + table.getValue(2), .399 < table.getValue(2) && table.getValue(2) < .401);
		// D = d2
		assertTrue("value = " + table.getValue(3), .049 < table.getValue(3) && table.getValue(3) < .051);
		assertTrue("value = " + table.getValue(4), .899 < table.getValue(4) && table.getValue(4) < .901);
		assertTrue("value = " + table.getValue(5), .049 < table.getValue(5) && table.getValue(5) < .051	);
		// D = d3
		assertTrue("value = " + table.getValue(6), .349 < table.getValue(6) && table.getValue(6) < .351);
		assertTrue("value = " + table.getValue(7), .299 < table.getValue(7) && table.getValue(7) < .301);
		assertTrue("value = " + table.getValue(8), .349 < table.getValue(8) && table.getValue(8) < .351);
		
		new GUIPotentialTable(table).showTable("Test");
		
		// D and G as parents
		parents.add(net.getNode("G"));
		table = (ProbabilisticTable) condProbExtractor.buildCondicionalProbability(nodeF, parents, net, algorithm);
		assertNotNull(table);
		assertEquals(27, table.tableSize());
		for (int i = 0; i < table.tableSize(); i++) {
			assertTrue("On index " + i, nodeF.getProbabilityFunction().getValue(i) - 0.001 < table.getValue(i) && table.getValue(i) < nodeF.getProbabilityFunction().getValue(i) +  0.001);
		}
		new GUIPotentialTable(table).showTable("Test");
	}

}
