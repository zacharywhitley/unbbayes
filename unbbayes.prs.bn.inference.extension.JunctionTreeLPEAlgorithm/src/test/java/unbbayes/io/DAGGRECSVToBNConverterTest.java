/**
 * 
 */
package unbbayes.io;

import java.io.File;
import java.io.IOException;
import java.util.List;

import edu.gmu.ace.daggre.io.DAGGRECSVToBNConverter;
import edu.gmu.ace.daggre.io.IDAGGRECSVNodeCreationListener;

import junit.framework.TestCase;
import unbbayes.prs.Edge;
import unbbayes.prs.Graph;
import unbbayes.prs.INode;
import unbbayes.prs.Node;
import unbbayes.prs.bn.JunctionTreeAlgorithm;
import unbbayes.prs.bn.ProbabilisticNetwork;

/**
 * This test case is going to create a BN from a DAGGRE csv file.
 * @author Shou Matsumoto
 *
 */
public class DAGGRECSVToBNConverterTest extends TestCase {
	
	private DAGGRECSVToBNConverter converter;
	private BaseIO netSaver;

	/**
	 * @param name
	 */
	public DAGGRECSVToBNConverterTest(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		this.converter = new DAGGRECSVToBNConverter();
		this.netSaver = new NetIO();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	
	
	/**
	 * Requirements (from Dr. Charles Twardy's e-mail ctwardy@c4i.gmu.edu)
	 * I have no idea how (if?) this file represents edits to multiple-choice questions.  Just assume everything is binary for a first test.
	 * The columns are: QuestionID, Timestamp, UserID, new Probability.  All probabilities started at uniform. 
	 * Output should be a set of probabilities for each question, the number of edits processed, and a runtime.  

	 * The second test should arbitrarily divide them into 20ish groups of five.
	 * To control for background procesess, do 3-5 runs each and take the minimum time.
	 * 
	 * The file contains at least 196 nodes and 3148 users.
	 * 
	 */
	public void test5per5NetCreation() {
		assertNotNull(this.converter);
		
		// adjust converter to create 5per5 node network
		this.converter.setNodeCreationListener(new IDAGGRECSVNodeCreationListener() {
			public void setNumberOfNewNodesToBeGeneratedBeforeCall(long nodesQuantity) {}
			public long getNumberOfNewNodesToBeGeneratedBeforeCall() {
				return 5;
			}
			/**
			 * Generate a network like the following:
			 * <br/>
			 * 1	<br/>
			 * | \	<br/>
			 * V V	<br/>
			 * 2->5	<br/>
			 * | \	<br/>
			 * V V	<br/>
			 * 3 4	<br/>
			 * <br/>
			 * If there is not enough nodes, then:
			 * 
			 */
			public void doCommand(Graph graph, List<INode> lastNodesCreated) {
				if (lastNodesCreated.size() <= 1) {
					// nothing to connect
					return;
				}
				// there are at least 2 nodes from now on
				// add edge 1->2
				Edge edge = new Edge((Node)lastNodesCreated.get(0), (Node)lastNodesCreated.get(1));
				try {
					graph.addEdge(edge);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				if (lastNodesCreated.size() == 2) {
					// impossible to add more edges 
					return;
				}
				// add edge 1->5 (1->4 or 1->3 if less than 5 nodes)
				edge = new Edge((Node)lastNodesCreated.get(0), (Node)lastNodesCreated.get(lastNodesCreated.size()-1));
				try {
					graph.addEdge(edge);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				// add edge 2->5 (2->4 or 2->3 if less than 5 nodes)
				edge = new Edge((Node)lastNodesCreated.get(1), (Node)lastNodesCreated.get(lastNodesCreated.size()-1));
				try {
					graph.addEdge(edge);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				if (lastNodesCreated.size() == 3) {
					// impossible to add more edges 
					return;
				}
				
				// add edge 2->3
				edge = new Edge((Node)lastNodesCreated.get(1), (Node)lastNodesCreated.get(2));
				try {
					graph.addEdge(edge);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				if (lastNodesCreated.size() == 4) {
					// impossible to add more edges 
					return;
				}
				
				// add edge 2->4
				edge = new Edge((Node)lastNodesCreated.get(1), (Node)lastNodesCreated.get(3));
				try {
					graph.addEdge(edge);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});
		
		
		assertNotNull(this.netSaver);
		File csvFile = new File("examples/DAGGRE.csv");
		assertNotNull(csvFile);
		assertTrue(csvFile.exists());
		Graph net = null;
		try {
			long time = System.currentTimeMillis();
			net = this.converter.load(csvFile);
			System.out.println("[5x5] Time to load " + csvFile.getName() + " and generate network = " 
					+ (System.currentTimeMillis() - time) + " ms.");
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		assertNotNull(net);
		assertTrue(net.getNodes().size() > 0);
		
		// test consistency
		JunctionTreeAlgorithm algorithm = new JunctionTreeAlgorithm((ProbabilisticNetwork) net);
		try {
			algorithm.run();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		File output = new File(csvFile.getParent(),"DAGGRE5p5.net");
		try {
			this.netSaver.save(output, net);
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	/**
	 * Requirements (from Dr. Charles Twardy's e-mail ctwardy@c4i.gmu.edu)
	 * I have no idea how (if?) this file represents edits to multiple-choice questions.  Just assume everything is binary for a first test.
	 * The columns are: QuestionID, Timestamp, UserID, new Probability.  All probabilities started at uniform. 
	 * Output should be a set of probabilities for each question, the number of edits processed, and a runtime.  

	 * The first test should use only disconnected nodes.  So 100+ nodes and no arcs.
	 * To control for background procesess, do 3-5 runs each and take the minimum time.
	 * 
	 * The file contains at least 196 nodes and 3148 users.
	 * 
	 */
	public void testDisconnectedNetCreation() {
		assertNotNull(this.converter);
		assertNotNull(this.netSaver);
		File csvFile = new File("examples/DAGGRE.csv");
		assertNotNull(csvFile);
		assertTrue(csvFile.exists());
		Graph net = null;
		try {
			long time = System.currentTimeMillis();
			net = this.converter.load(csvFile);
			System.out.println("[Disconnected] Time to load " + csvFile.getName() + " and generate network = " 
					+ (System.currentTimeMillis() - time) + " ms.");
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		assertNotNull(net);
		assertTrue(net.getNodes().size() > 0);
		
		// assert that all nodes are boolean [false, true]
		for (Node node : net.getNodes()) {
			assertEquals(2, node.getStatesSize());
			assertEquals("false", node.getStateAt(0));
			assertEquals("true", node.getStateAt(1));
		}
		
		// test consistency
		JunctionTreeAlgorithm algorithm = new JunctionTreeAlgorithm((ProbabilisticNetwork) net);
		try {
			algorithm.run();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		File output = new File(csvFile.getParent(),"DAGGRE1.net");
		try {
			this.netSaver.save(output, net);
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
