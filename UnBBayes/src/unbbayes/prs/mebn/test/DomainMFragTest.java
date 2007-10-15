/**
 * 
 */
package unbbayes.prs.mebn.test;

import java.util.ArrayList;
import java.util.List;

import unbbayes.prs.Edge;
import unbbayes.prs.Node;
import unbbayes.prs.mebn.Argument;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.DomainMFrag;
import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.GenerativeInputNode;
import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.builtInRV.test.BuiltInRVAndTest;
import unbbayes.prs.mebn.entity.CategoricalStatesEntity;
import unbbayes.prs.mebn.entity.Type;
import unbbayes.prs.mebn.entity.TypeContainer;
import unbbayes.prs.mebn.entity.exception.TypeAlreadyExistsException;
import unbbayes.prs.mebn.exception.ArgumentNodeAlreadySetException;
import unbbayes.prs.mebn.exception.MEBNConstructionException;
import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Shou
 *
 */
public class DomainMFragTest extends TestCase {

	MultiEntityBayesianNetwork mebn = null;
	DomainMFrag mfrag = null;
	
	/**
	 * @param arg0
	 */
	public DomainMFragTest(String arg0) {
		super(arg0);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		mebn = new MultiEntityBayesianNetwork("DomainMFragTestMEBN");
		mfrag = new DomainMFrag("DomainMFragTestMFrag",mebn);
		
		
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		mebn = null;
		
	}
	
	
	
	/**
	 * Test method for {@link unbbayes.prs.mebn.MFrag#MFrag(java.lang.String, unbbayes.prs.mebn.MultiEntityBayesianNetwork)}.
	 */
	public void testMFrag() {
		assertEquals(mebn.getMFragCount(),1);
		assertTrue(mebn.getMFragList().contains(mfrag));
		assertTrue(mebn.getDomainMFragList().contains(mfrag));
		assertEquals(mfrag.getMultiEntityBayesianNetwork(),mebn);
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MFrag#setName(java.lang.String)}.
	 */
	public void testName() {
		String mfragName = "testGetName";
		DomainMFrag mfrag = new DomainMFrag(mfragName,mebn);
		assertEquals(mfrag.getName(), mfragName);
		
		String mfragName2 = "testSetName";
		mfrag.setName(mfragName2);
		assertEquals(mfrag.getName(), mfragName2);
		
		mfrag.setName(null);
		assertNull(mfrag.getName());
		
	}

	

	/**
	 * Test method for {@link unbbayes.prs.mebn.MFrag#getMultiEntityBayesianNetwork()}.
	 */
	public void testGetMultiEntityBayesianNetwork() {
		MultiEntityBayesianNetwork mebn2 = new MultiEntityBayesianNetwork("testGetMultiEntityBayesianNetworkMEBN");
		DomainMFrag mfrag2 = new DomainMFrag("testGetMultiEntityBayesianNetwork",mebn2);
		
		assertNotNull(mfrag.getMultiEntityBayesianNetwork());
		assertNotNull(mfrag2.getMultiEntityBayesianNetwork());
		assertEquals(mfrag.getMultiEntityBayesianNetwork(),mebn);
		assertEquals(mfrag2.getMultiEntityBayesianNetwork(), mebn2);
		assertTrue(!mfrag.getMultiEntityBayesianNetwork().equals(mfrag2.getMultiEntityBayesianNetwork()));
		
		
	}

	

	/**
	 * Test method for {@link unbbayes.prs.mebn.MFrag#addNode(unbbayes.prs.Node)}.
	 */
	public void testAddNode() {
		Node node = new ResidentNode();
		Node node2 = new InputNode();
		Node node3 = new ContextNode("testAddNode",mfrag);
		
		mfrag.addNode(node);
		mfrag.addNode(node2);
		mfrag.addNode(node3);
		
		assertEquals(mfrag.getNodeCount(), 3);
		assertEquals(mfrag.getNodeList().size(), 3);
		assertTrue(mfrag.getNodeList().contains(node));
		assertTrue(mfrag.getNodeList().contains(node2));
		assertTrue(mfrag.getNodeList().contains(node3));
		
		/*
		mfrag.addNode(null);	
		assertEquals(mfrag.getNodeCount(), 4);
		
		assertTrue(mfrag.getNodeList().contains(null));
		*/
		
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MFrag#addResidentNode(unbbayes.prs.mebn.ResidentNode)}.
	 */
	public void testAddResidentNode() {
		fail("Not visible, method is protected");// TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MFrag#addInputNode(unbbayes.prs.mebn.InputNode)}.
	 */
	public void testAddInputNode() {
		fail("Not visible, method is protected");// TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MFrag#removeNode(unbbayes.prs.Node)}.
	 */
	public void testRemoveNode() {
		Node node = new ResidentNode();
		Node node2 = new InputNode();
		Node node3 = new ContextNode("testAddNode",mfrag);
		
		mfrag.addNode(node);
		mfrag.addNode(node2);
		mfrag.addNode(node3);
		
		// Test
		try {
		
			mfrag.removeNode(null);
			assertEquals(mfrag.getNodeCount(), 3);	
			assertEquals(mfrag.getNodeList().size(), 3);
			assertTrue(!mfrag.getNodeList().contains(null));
			assertTrue(mfrag.getNodeList().contains(node));
			assertTrue(mfrag.getNodeList().contains(node2));
			assertTrue(mfrag.getNodeList().contains(node3));
			
		} catch (NullPointerException e) {
			
		}
		mfrag.removeNode(node);
		assertEquals(mfrag.getNodeCount(), 2);	
		assertEquals(mfrag.getNodeList().size(), 2);
		//assertTrue(!mfrag.getNodeList().contains(null));
		assertTrue(!mfrag.getNodeList().contains(node));
		assertTrue(mfrag.getNodeList().contains(node2));
		assertTrue(mfrag.getNodeList().contains(node3));
		
		mfrag.removeNode(node2);
		assertEquals(mfrag.getNodeCount(), 1);	
		assertEquals(mfrag.getNodeList().size(), 1);
		//assertTrue(!mfrag.getNodeList().contains(null));
		assertTrue(!mfrag.getNodeList().contains(node));
		assertTrue(!mfrag.getNodeList().contains(node2));
		assertTrue(mfrag.getNodeList().contains(node3));
		
		mfrag.removeNode(node3);
		assertEquals(mfrag.getNodeCount(), 0);	
		assertEquals(mfrag.getNodeList().size(), 0);
		//assertTrue(!mfrag.getNodeList().contains(null));
		assertTrue(!mfrag.getNodeList().contains(node));
		assertTrue(!mfrag.getNodeList().contains(node2));
		assertTrue(!mfrag.getNodeList().contains(node3));
		
		assertNotNull(mfrag.getNodeList());
		assertEquals(mfrag.getNodeList().size(), 0);
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MFrag#removeResidentNode(unbbayes.prs.mebn.ResidentNode)}.
	 */
	public void testRemoveResidentNode() {
		DomainResidentNode resident1 = new DomainResidentNode("testRemoveResidentNode",mfrag);
		DomainResidentNode resident2 = new DomainResidentNode("testRemoveResidentNode",mfrag);
		
		assertEquals(mfrag.getNodeCount(),2);
		assertEquals(mfrag.getDomainResidentNodeCount(),2);
		assertTrue(mfrag.containsDomainResidentNode(resident1));
		assertTrue(mfrag.containsDomainResidentNode(resident2));
		assertTrue(mfrag.containsNode(resident1));
		assertTrue(mfrag.containsNode(resident2));
		
		mfrag.removeDomainResidentNode(resident1);
		assertEquals(mfrag.getNodeCount(),1);
		assertEquals(mfrag.getDomainResidentNodeCount(),1);
		assertTrue(!mfrag.containsDomainResidentNode(resident1));
		assertTrue(mfrag.containsDomainResidentNode(resident2));
		assertTrue(!mfrag.containsNode(resident1));
		assertTrue(mfrag.containsNode(resident2));
		
		mfrag.removeDomainResidentNode(resident2);
		assertEquals(mfrag.getNodeCount(),0);
		assertEquals(mfrag.getDomainResidentNodeCount(),0);
		assertTrue(!mfrag.containsDomainResidentNode(resident1));
		assertTrue(!mfrag.containsDomainResidentNode(resident2));
		assertTrue(!mfrag.containsNode(resident1));
		assertTrue(!mfrag.containsNode(resident2));
		
		//fail("addResidentNode is not visible. Impossible to test removeResidentNode"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MFrag#removeInputNode(unbbayes.prs.mebn.InputNode)}.
	 */
	public void testRemoveInputNode() {
		
		GenerativeInputNode input1 = new GenerativeInputNode("testRemoveResidentNode",mfrag);
		GenerativeInputNode input2 = new GenerativeInputNode("testRemoveResidentNode",mfrag);
		
		assertEquals(mfrag.getNodeCount(),2);
		assertEquals(mfrag.getGenerativeInputNodeCount(),2);
		assertTrue(mfrag.containsGenerativeInputNode(input1));
		assertTrue(mfrag.containsGenerativeInputNode(input2));
		assertTrue(mfrag.containsNode(input1));
		assertTrue(mfrag.containsNode(input2));
		
		mfrag.removeGenerativeInputNode(input1);
		assertEquals(mfrag.getNodeCount(),1);
		assertEquals(mfrag.getGenerativeInputNodeCount(),1);
		assertTrue(!mfrag.containsGenerativeInputNode(input1));
		assertTrue(mfrag.containsGenerativeInputNode(input2));
		assertTrue(!mfrag.containsNode(input1));
		assertTrue(mfrag.containsNode(input2));
		
		mfrag.removeGenerativeInputNode(input2);
		assertEquals(mfrag.getNodeCount(),0);
		assertEquals(mfrag.getGenerativeInputNodeCount(),0);
		assertTrue(!mfrag.containsGenerativeInputNode(input1));
		assertTrue(!mfrag.containsGenerativeInputNode(input2));
		assertTrue(!mfrag.containsNode(input1));
		assertTrue(!mfrag.containsNode(input2));
		
		//fail("addInputNode is not visible. Impossible to test removeResidentNode"); // TODO
	}

	

	/**
	 * Test method for {@link unbbayes.prs.mebn.MFrag#getOrdinaryVariableNum()}.
	 * Test method for {@link unbbayes.prs.mebn.MFrag#addOrdinaryVariable(OrdinaryVariable)}.
	 * Test method for {@link unbbayes.prs.mebn.MFrag#removeOrdinaryVariable(OrdinaryVariable)}.
	 * Test method for {@link unbbayes.prs.mebn.MFrag#containsOrdinaryVariable(unbbayes.prs.mebn.OrdinaryVariable)}.
	 */
	public void testAddRemoveOrdinaryVariableNum() {
		/*OrdinaryVariable ov1 = new OrdinaryVariable("ov1",Type.typeCategoryLabel,mfrag);
		OrdinaryVariable ov2 = new OrdinaryVariable("ov1",Type.typeCategoryLabel,mfrag);
		OrdinaryVariable ov3 = new OrdinaryVariable("ov1",Type.typeCategoryLabel,mfrag);
		
		mfrag.addOrdinaryVariable(ov1);
		mfrag.addOrdinaryVariable(ov2);
		mfrag.addOrdinaryVariable(ov3);
		
		assertEquals(mfrag.getOrdinaryVariableNum(), 3);
		assertEquals(mfrag.getOrdinaryVariableNum(), mfrag.getOrdinaryVariableList().size());
		
		assertTrue(mfrag.getOrdinaryVariableList().contains(ov1));
		assertTrue(mfrag.getOrdinaryVariableList().contains(ov2));
		assertTrue(mfrag.getOrdinaryVariableList().contains(ov3));
		
		assertTrue(mfrag.containsOrdinaryVariable(ov1));
		assertTrue(mfrag.containsOrdinaryVariable(ov2));
		assertTrue(mfrag.containsOrdinaryVariable(ov3));
		
		
		mfrag.removeOrdinaryVariable(ov1);
		assertEquals(mfrag.getOrdinaryVariableNum(), 2);
		assertTrue(!mfrag.getOrdinaryVariableList().contains(ov1));
		assertTrue(mfrag.getOrdinaryVariableList().contains(ov2));
		assertTrue(mfrag.getOrdinaryVariableList().contains(ov3));
		assertTrue(!mfrag.containsOrdinaryVariable(ov1));
		assertTrue(mfrag.containsOrdinaryVariable(ov2));
		assertTrue(mfrag.containsOrdinaryVariable(ov3));
		
		mfrag.removeOrdinaryVariable(ov2);
		assertEquals(mfrag.getOrdinaryVariableNum(), 1);
		assertTrue(!mfrag.getOrdinaryVariableList().contains(ov1));
		assertTrue(!mfrag.getOrdinaryVariableList().contains(ov2));
		assertTrue(mfrag.getOrdinaryVariableList().contains(ov3));
		assertTrue(!mfrag.containsOrdinaryVariable(ov1));
		assertTrue(!mfrag.containsOrdinaryVariable(ov2));
		assertTrue(mfrag.containsOrdinaryVariable(ov3));
		
		mfrag.removeOrdinaryVariable(ov3);
		assertNotNull(mfrag.getOrdinaryVariableList());
		assertEquals(mfrag.getOrdinaryVariableNum(), 0);
		assertTrue(!mfrag.getOrdinaryVariableList().contains(ov1));
		assertTrue(!mfrag.getOrdinaryVariableList().contains(ov2));
		assertTrue(!mfrag.getOrdinaryVariableList().contains(ov3));
		assertTrue(!mfrag.containsOrdinaryVariable(ov1));
		assertTrue(!mfrag.containsOrdinaryVariable(ov2));
		assertTrue(!mfrag.containsOrdinaryVariable(ov3));
		
		*/
		
	}

	

	

	

	/**
	 * Test method for {@link unbbayes.prs.mebn.MFrag#getOrdinaryVariableList()}.
	 */
	public void testGetOrdinaryVariableList() {
		/*OrdinaryVariable ov1 = new OrdinaryVariable("ov1",Type.typeCategoryLabel,mfrag);
		OrdinaryVariable ov2 = new OrdinaryVariable("ov1",Type.typeCategoryLabel,mfrag);
		OrdinaryVariable ov3 = new OrdinaryVariable("ov1",Type.typeCategoryLabel,mfrag);
		
		mfrag.addOrdinaryVariable(ov1);
		mfrag.addOrdinaryVariable(ov2);
		mfrag.addOrdinaryVariable(ov3);
		
		assertNotNull(mfrag.getOrdinaryVariableList());
		assertEquals(mfrag.getOrdinaryVariableList().size(), 3);
		assertEquals(mfrag.getOrdinaryVariableList().size(), mfrag.getOrdinaryVariableNum());
		
		assertTrue(mfrag.getOrdinaryVariableList().contains(ov1));
		assertTrue(mfrag.getOrdinaryVariableList().contains(ov2));
		assertTrue(mfrag.getOrdinaryVariableList().contains(ov3));
		*/
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MFrag#containsNode(unbbayes.prs.Node)}.
	 */
	public void testContainsNode() {
		Node node = new ResidentNode();
		Node node2 = new InputNode();
		Node node3 = new ContextNode("testAddNode",mfrag);
		
		mfrag.addNode(node);
		mfrag.addNode(node2);
		mfrag.addNode(node3);
		
		assertTrue(mfrag.containsNode(node));
		assertTrue(mfrag.containsNode(node2));
		assertTrue(mfrag.containsNode(node3));
		
		assertTrue(!mfrag.containsNode(new InputNode()));
		assertTrue(!mfrag.containsNode(new ResidentNode()));
		assertTrue(!mfrag.containsNode(new ContextNode("failure",mfrag)));
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MFrag#getEdges()}.
	 * Test method for {@link unbbayes.prs.mebn.MFrag#removeEdge(Edge)}.
	 * Test method for {@link unbbayes.prs.mebn.MFrag#addEdge(Edge)}.
	 */
	public void testGetEdges() {
		/*
		 * 			________________
		 * 			|	Node2		|
		 * 			-----------------
		 * 				|			\
		 * 				|		 	 \
		 * 				V			  \
		 * 		______________		   \
		 * 		|	Node1	  |			\
		 * 		--------------			|
		 * 				\				|
		 * 				 \				|
		 * 				  \				|
		 * 				  V				V
		 * 				__________________
		 * 				|	Node3		  |
		 * 				-------------------
		 * 
		 */
		
		Node node1 = new ResidentNode();
		Node node2 = new InputNode();
		Node node3 = new ResidentNode();
		
		Edge edge21 = new Edge(node2,node1);
		Edge edge23 = new Edge(node2,node3);
		Edge edge13 = new Edge(node1,node3);
		
		try {
			mfrag.addEdge(edge21);
			mfrag.addEdge(edge23);
			mfrag.addEdge(edge13);
		} catch (Exception e) {
			fail (e.getMessage());
		}
		
		assertNotNull(mfrag.getEdges());
		assertEquals(mfrag.getEdges().size(), 3);
		assertTrue(mfrag.getEdges().contains(edge21));
		assertTrue(mfrag.getEdges().contains(edge23));
		assertTrue(mfrag.getEdges().contains(edge13));
		
		mfrag.removeEdge(edge21);
		assertEquals(mfrag.getEdges().size(), 2);
		assertTrue(!mfrag.getEdges().contains(edge21));
		assertTrue(mfrag.getEdges().contains(edge23));
		assertTrue(mfrag.getEdges().contains(edge13));
		
		mfrag.removeEdge(edge23);
		assertEquals(mfrag.getEdges().size(), 2);
		assertTrue(!mfrag.getEdges().contains(edge21));
		assertTrue(!mfrag.getEdges().contains(edge23));
		assertTrue(mfrag.getEdges().contains(edge13));
		
		mfrag.removeEdge(edge13);
		assertEquals(mfrag.getEdges().size(), 2);
		assertTrue(!mfrag.getEdges().contains(edge21));
		assertTrue(!mfrag.getEdges().contains(edge23));
		assertTrue(!mfrag.getEdges().contains(edge13));
		
		
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MFrag#getNodes()}.
	 */
	public void testGetNodes() {
		Node node = new ResidentNode();
		Node node2 = new InputNode();
		Node node3 = new ContextNode("testAddNode",mfrag);
		
		mfrag.addNode(node);
		mfrag.addNode(node2);
		mfrag.addNode(node3);
		
		assertEquals(mfrag.getNodeList(), mfrag.getNodes());
	}

	

	

	/**
	 * Test method for {@link unbbayes.prs.mebn.MFrag#removeEdgeByNodes(unbbayes.prs.Node, unbbayes.prs.Node)}.
	 */
	public void testRemoveEdgeByNodes() {
		/*
		 * 			________________
		 * 			|	Node2		|
		 * 			-----------------
		 * 				|			\
		 * 				|		 	 \
		 * 				V			  \
		 * 		______________		   \
		 * 		|	Node1	  |			\
		 * 		--------------			|
		 * 				\				|
		 * 				 \				|
		 * 				  \				|
		 * 				  V				V
		 * 				__________________
		 * 				|	Node3		  |
		 * 				-------------------
		 * 
		 */
		
		Node node1 = new ResidentNode();
		Node node2 = new InputNode();
		Node node3 = new ResidentNode();
		
		Edge edge21 = new Edge(node2,node1);
		Edge edge23 = new Edge(node2,node3);
		Edge edge13 = new Edge(node1,node3);
		
		try {
			mfrag.addEdge(edge21);
			mfrag.addEdge(edge23);
			mfrag.addEdge(edge13);
		} catch (Exception e) {
			fail (e.getMessage());
		}
		
		// try to remove non existing edges
		
		mfrag.removeEdgeByNodes(null, null);
		assertTrue(mfrag.getNodeList().contains(node1));
		assertTrue(mfrag.getNodeList().contains(node2));
		assertTrue(mfrag.getNodeList().contains(node3));
		assertEquals(mfrag.getEdges().size(), 3);
		
		mfrag.removeEdgeByNodes(node1, null);
		assertTrue(mfrag.getNodeList().contains(node1));
		assertTrue(mfrag.getNodeList().contains(node2));
		assertTrue(mfrag.getNodeList().contains(node3));
		assertEquals(mfrag.getEdges().size(), 3);
		
		mfrag.removeEdgeByNodes(null, node1);
		assertTrue(mfrag.getNodeList().contains(node1));
		assertTrue(mfrag.getNodeList().contains(node2));
		assertTrue(mfrag.getNodeList().contains(node3));
		assertEquals(mfrag.getEdges().size(), 3);
		
		
		mfrag.removeEdgeByNodes(node1, node1);
		assertTrue(mfrag.getNodeList().contains(node1));
		assertTrue(mfrag.getNodeList().contains(node2));
		assertTrue(mfrag.getNodeList().contains(node3));
		assertEquals(mfrag.getEdges().size(), 3);
		
		mfrag.removeEdgeByNodes(node1, node2);
		assertTrue(mfrag.getNodeList().contains(node1));
		assertTrue(mfrag.getNodeList().contains(node2));
		assertTrue(mfrag.getNodeList().contains(node3));
		assertEquals(mfrag.getEdges().size(), 3);
		
		mfrag.removeEdgeByNodes(node3, node1);
		assertTrue(mfrag.getNodeList().contains(node1));
		assertTrue(mfrag.getNodeList().contains(node2));
		assertTrue(mfrag.getNodeList().contains(node3));
		assertEquals(mfrag.getEdges().size(), 3);
		
		mfrag.removeEdgeByNodes(node3, node2);
		assertTrue(mfrag.getNodeList().contains(node1));
		assertTrue(mfrag.getNodeList().contains(node2));
		assertTrue(mfrag.getNodeList().contains(node3));
		assertEquals(mfrag.getEdges().size(), 3);
		
		
		
		
		// remove edges
		
		mfrag.removeEdgeByNodes(node2,node1);
		assertEquals(mfrag.getEdges().size(), 2);
		assertTrue(!mfrag.getEdges().contains(edge21));
		assertTrue(mfrag.getEdges().contains(edge23));
		assertTrue(mfrag.getEdges().contains(edge13));
		assertTrue(mfrag.getNodeList().contains(node1));
		assertTrue(mfrag.getNodeList().contains(node2));
		
		mfrag.removeEdgeByNodes(node2,node3);
		assertEquals(mfrag.getEdges().size(), 2);
		assertTrue(!mfrag.getEdges().contains(edge21));
		assertTrue(!mfrag.getEdges().contains(edge23));
		assertTrue(mfrag.getEdges().contains(edge13));
		
		mfrag.removeEdgeByNodes(node1,node3);
		assertEquals(mfrag.getEdges().size(), 2);
		assertTrue(!mfrag.getEdges().contains(edge21));
		assertTrue(!mfrag.getEdges().contains(edge23));
		assertTrue(!mfrag.getEdges().contains(edge13));
	}

	

	/**
	 * Test method for {@link unbbayes.prs.mebn.MFrag#hasEdge(unbbayes.prs.Node, unbbayes.prs.Node)}.
	 * Test method for {@link unbbayes.prs.mebn.MFrag#addEdge(Edge)}.
	 *
	 */
	public void testHasEdge() {
		/*
		 * 			________________
		 * 			|	Node2		|
		 * 			-----------------
		 * 				|			\
		 * 				|		 	 \
		 * 				V			  \
		 * 		______________		   \
		 * 		|	Node1	  |			\
		 * 		--------------			|
		 * 				\				|
		 * 				 \				|
		 * 				  \				|
		 * 				  V				V
		 * 				__________________
		 * 				|	Node3		  |
		 * 				-------------------
		 * 
		 */
		
		Node node1 = new ResidentNode();
		Node node2 = new InputNode();
		Node node3 = new ResidentNode();
		
		Edge edge21 = new Edge(node2,node1);
		Edge edge23 = new Edge(node2,node3);
		Edge edge13 = new Edge(node1,node3);
		
		try {
			mfrag.addEdge(edge21);
			mfrag.addEdge(edge23);
			mfrag.addEdge(edge13);
		} catch (Exception e) {
			fail (e.getMessage());
		}
		
		int edgePos = -1;
		assertTrue((mfrag.hasEdge(node1,node1)) < 0);
		assertTrue((mfrag.hasEdge(node1,node2)) < 0);
		assertTrue((edgePos = mfrag.hasEdge(node1,node3)) >= 0);
		assertEquals(mfrag.getEdges().get(edgePos),edge13);
		
		edgePos = -1;
		assertTrue((edgePos = mfrag.hasEdge(node2,node1)) >= 0);
		assertEquals(mfrag.getEdges().get(edgePos),edge21);
		assertTrue((mfrag.hasEdge(node2,node2)) < 0);
		assertTrue((edgePos = mfrag.hasEdge(node2,node3)) >= 0);
		assertEquals(mfrag.getEdges().get(edgePos),edge23);
		
		assertTrue((mfrag.hasEdge(node3,node1)) < 0);
		assertTrue((mfrag.hasEdge(node3,node2)) < 0);
		assertTrue((mfrag.hasEdge(node3,node3)) < 0);
		
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MFrag#getInputNodeList()}.
	 */
	public void testGetInputNodeList() {
		Node node1 = new ResidentNode();
		Node node2 = new GenerativeInputNode("node2",mfrag);
		Node node3 = new ResidentNode();
		
		
		
		assertTrue(mfrag.getInputNodeList().contains(node2));
		assertTrue(!mfrag.getInputNodeList().contains(node1));
		assertTrue(!mfrag.getInputNodeList().contains(node3));
		
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MFrag#setInputNodeList(java.util.List)}.
	 */
	public void testSetInputNodeList() {
		DomainMFrag placeholder = new DomainMFrag("placeholder",mebn);
		GenerativeInputNode node1 = new GenerativeInputNode("node1",mfrag);
		GenerativeInputNode node2 = new GenerativeInputNode("node2",placeholder);
		GenerativeInputNode node3 = new GenerativeInputNode("node3",placeholder);
		List<InputNode> list = new ArrayList<InputNode>();
		
		assertTrue(mfrag.containsGenerativeInputNode(node1));
		assertTrue(!mfrag.containsGenerativeInputNode(node2));
		assertTrue(!mfrag.containsGenerativeInputNode(node3));
		
		list.add(node2);
		list.add(node3);
		mfrag.setInputNodeList(list);
		
		assertEquals(mfrag.getGenerativeInputNodeList(),list);
		
		assertTrue(!mfrag.containsGenerativeInputNode(node1));
		assertTrue(mfrag.containsGenerativeInputNode(node2));
		assertTrue(mfrag.containsGenerativeInputNode(node3));
		
		
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MFrag#getResidentNodeList()}.
	 */
	public void testGetResidentNodeList() {
		Node node1 = new ResidentNode();
		Node node2 = new GenerativeInputNode("node2",mfrag);
		Node node3 = new ResidentNode();
				
		
		assertTrue(mfrag.getResidentNodeList().contains(node1));
		assertTrue(!mfrag.getResidentNodeList().contains(node2));
		assertTrue(mfrag.getResidentNodeList().contains(node3));
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MFrag#setResidentNodeList(java.util.List)}.
	 */
	public void testSetResidentNodeList() {
		DomainMFrag placeholder = new DomainMFrag("placeholder",mebn);
		DomainResidentNode node1 = new DomainResidentNode("node1",mfrag);
		DomainResidentNode node2 = new DomainResidentNode("node2",placeholder);
		DomainResidentNode node3 = new DomainResidentNode("node3",placeholder);
		List<ResidentNode> list = new ArrayList<ResidentNode>();
		
		assertTrue(mfrag.containsDomainResidentNode(node1));
		assertTrue(!mfrag.containsDomainResidentNode(node2));
		assertTrue(!mfrag.containsDomainResidentNode(node3));
		
		list.add(node2);
		list.add(node3);
		mfrag.setResidentNodeList(list);
		
		assertEquals(mfrag.getGenerativeInputNodeList(),list);
		
		assertTrue(!mfrag.containsDomainResidentNode(node1));
		assertTrue(mfrag.containsDomainResidentNode(node2));
		assertTrue(mfrag.containsDomainResidentNode(node3));
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MFrag#setOrdinaryVariableNum(int)}.
	 */
	public void testSetOrdinaryVariableNum() {
		int i = (int)Math.random()*100;
		mfrag.setOrdinaryVariableNum(i);
		assertEquals(mfrag.getOrdinaryVariableNum(),i);
		
	}

	
	

	/**
	 * Test method for {@link unbbayes.prs.mebn.DomainMFrag#delete()}.
	 */
	public void testDelete() {
		/*DomainResidentNode resident = new DomainResidentNode("resident",mfrag);
		GenerativeInputNode input = new GenerativeInputNode("input",mfrag);
		ContextNode context = new ContextNode("context",mfrag);
		OrdinaryVariable ov1 = new OrdinaryVariable("typeBoolean",Type.typeBoolean,mfrag);
		OrdinaryVariable ov2 = new OrdinaryVariable("typeCategoryLabel",Type.typeCategoryLabel,mfrag);
		OrdinaryVariable ov3 = new OrdinaryVariable("typeLabel",Type.typeLabel,mfrag);
		
		mfrag.delete();
		
		assertNull(resident.getMFrag());
		assertNull(input.getMFrag());
		assertNull(context.getMFrag());
		assertNull(ov1.getMFrag());
		assertNull(ov2.getMFrag());
		assertNull(ov3.getMFrag());
		
		assertEquals(mfrag.getNodeCount(),0);
		assertEquals(mfrag.getDomainResidentNodeCount(),0);
		assertEquals(mfrag.getGenerativeInputNodeCount(),0);
		assertEquals(mfrag.getContextNodeCount(),0);
		assertEquals(mfrag.getOrdinaryVariableList().size(),0);
		
		assertTrue(!mfrag.containsDomainResidentNode(resident));
		assertTrue(!mfrag.containsGenerativeInputNode(input));
		assertTrue(!mfrag.containsContextNode(context));
		assertTrue(!mfrag.containsOrdinaryVariable(ov1));
		assertTrue(!mfrag.containsOrdinaryVariable(ov2));
		assertTrue(!mfrag.containsOrdinaryVariable(ov3));
		*/
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.DomainMFrag#addOrdinaryVariable(unbbayes.prs.mebn.OrdinaryVariable)}.
	 */
	public void testAddOrdinaryVariable() {
		/*DomainMFrag temp = new DomainMFrag("testAddOrdinaryVariable",mebn);
		OrdinaryVariable ov = new OrdinaryVariable("ov",Type.typeBoolean,temp);
		int ovCount = mfrag.getOrdinaryVariableNum();
		
		mfrag.addOrdinaryVariable(ov);
		assertEquals(mfrag.getOrdinaryVariableNum(),ovCount + 1);
		assertTrue(mfrag.containsOrdinaryVariable(ov));
		assertEquals(ov.getMFrag(),mfrag);
		
		*/
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.DomainMFrag#removeOrdinaryVariable(unbbayes.prs.mebn.OrdinaryVariable)}.
	 */
	public void testRemoveOrdinaryVariable() {
		/*OrdinaryVariable ov = new OrdinaryVariable("ov",Type.typeBoolean,mfrag);
		
		mfrag.removeOrdinaryVariable(ov);
		assertTrue(!mfrag.containsOrdinaryVariable(ov));
		assertTrue(!ov.getMFrag().equals(mfrag));
		*/
	}

	
	

	/**
	 * Test method for {@link unbbayes.prs.mebn.DomainMFrag#addContextNode(unbbayes.prs.mebn.ContextNode)}.
	 */
	public void testAddContextNode() {
		DomainMFrag temp = new DomainMFrag("testAddOrdinaryVariable",mebn);
		ContextNode context = new ContextNode("context",temp);
		
		mfrag.addContextNode(context);
		assertEquals(mfrag.getContextNodeCount(),1);
		assertEquals(mfrag.getContextNodeNum(),1);
		assertTrue(mfrag.containsContextNode(context));
		assertTrue(mfrag.containsNode(context));
		assertEquals(context.getMFrag(),mfrag);
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.DomainMFrag#addGenerativeInputNode(unbbayes.prs.mebn.GenerativeInputNode)}.
	 */
	public void testAddGenerativeInputNode() {

		DomainMFrag temp = new DomainMFrag("testAddOrdinaryVariable",mebn);
		GenerativeInputNode node = new GenerativeInputNode("node",temp);
		
		mfrag.addGenerativeInputNode(node);
		assertEquals(mfrag.getGenerativeInputNodeCount(),1);
		assertEquals(mfrag.getGenerativeInputNodeNum(),1);
		assertTrue(mfrag.containsGenerativeInputNode(node));
		assertTrue(mfrag.containsNode(node));
		assertEquals(node.getMFrag(),mfrag);
		
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.DomainMFrag#addDomainResidentNode(unbbayes.prs.mebn.DomainResidentNode)}.
	 */
	public void testAddDomainResidentNode() {
		DomainMFrag temp = new DomainMFrag("testAddOrdinaryVariable",mebn);
		DomainResidentNode node = new DomainResidentNode("node",temp);
		
		mfrag.addDomainResidentNode(node);
		assertEquals(mfrag.getDomainResidentNodeCount(),1);
		assertEquals(mfrag.getDomainResidentNodeNum(),1);
		assertTrue(mfrag.containsDomainResidentNode(node));
		assertTrue(mfrag.containsNode(node));
		assertEquals(node.getMFrag(),mfrag);
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.DomainMFrag#removeContextNode(unbbayes.prs.mebn.ContextNode)}.
	 */
	public void testRemoveContextNode() {
		ContextNode node = new ContextNode("node",mfrag);
		
		mfrag.removeContextNode(node);
		assertTrue(!mfrag.containsContextNode(node));
		assertTrue(!mfrag.containsNode(node));
		assertTrue(!node.getMFrag().equals(mfrag));
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.DomainMFrag#removeGenerativeInputNode(unbbayes.prs.mebn.GenerativeInputNode)}.
	 */
	public void testRemoveGenerativeInputNode() {
		GenerativeInputNode node = new GenerativeInputNode("node",mfrag);
		
		mfrag.removeGenerativeInputNode(node);
		assertTrue(!mfrag.containsGenerativeInputNode(node));
		assertTrue(!mfrag.containsNode(node));
		assertTrue(!node.getMFrag().equals(mfrag));
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.DomainMFrag#removeDomainResidentNode(unbbayes.prs.mebn.DomainResidentNode)}.
	 */
	public void testRemoveDomainResidentNode() {
		DomainResidentNode node = new DomainResidentNode("node",mfrag);
		
		mfrag.removeDomainResidentNode(node);
		assertTrue(!mfrag.containsDomainResidentNode(node));
		assertTrue(!mfrag.containsNode(node));
		assertTrue(!node.getMFrag().equals(mfrag));
	}

	
	
	/**
	 * Test method for {@link unbbayes.prs.mebn.DomainMFrag#getContextByAllOV(unbbayes.prs.mebn.OrdinaryVariable...allOVs)}.
	 */
	public void testGetContextByAllOV() {
		TypeContainer typeContainer = new TypeContainer();
		try {
			typeContainer.createType("Starship");
			typeContainer.createType("Zone");
			typeContainer.createType("Timestep");
		} catch (TypeAlreadyExistsException e) {
			this.fail(e.getMessage());
			return;
		}		
		Type tStarship = typeContainer.getType("Starship");
		Type tZone = typeContainer.getType("Zone");
		Type tTimestep = typeContainer.getType("Timestep");
		
		OrdinaryVariable st = new OrdinaryVariable("st",tStarship,this.mfrag);
		OrdinaryVariable z = new OrdinaryVariable("z",tZone,this.mfrag);
		OrdinaryVariable t = new OrdinaryVariable("z",tTimestep,this.mfrag);
		
		ContextNode context_st_t_z = new ContextNode("context_st_t_z",this.mfrag);
		Argument arg = new Argument("st",context_st_t_z);
		try{
			arg.setOVariable(st);
		} catch (ArgumentNodeAlreadySetException e) {
			this.fail(e.getMessage());
			return;
		}
		context_st_t_z.addArgument(arg);
		arg = new Argument("t",context_st_t_z);
		try{
			arg.setOVariable(t);
		} catch (ArgumentNodeAlreadySetException e) {
			this.fail(e.getMessage());
			return;
		}
		context_st_t_z.addArgument(arg);
		arg = new Argument("z",context_st_t_z);
		try{
			arg.setOVariable(t);
		} catch (ArgumentNodeAlreadySetException e) {
			this.fail(e.getMessage());
			return;
		}
		context_st_t_z.addArgument(arg);
		
		
		ContextNode context_st_z = new ContextNode("context_st_z",this.mfrag);
		
		
	}
	

	/**
	 *  Use this for test suite creation
	 */
	public static Test suite() {
		return new TestSuite(DomainMFragTest.class);
	}
}
