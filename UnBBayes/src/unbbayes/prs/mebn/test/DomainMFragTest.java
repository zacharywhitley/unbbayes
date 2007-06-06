/**
 * 
 */
package unbbayes.prs.mebn.test;

import unbbayes.prs.Edge;
import unbbayes.prs.Node;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.DomainMFrag;
import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.builtInRV.test.BuiltInRVAndTest;
import unbbayes.prs.mebn.entity.CategoricalStatesEntity;
import unbbayes.prs.mebn.entity.Type;
import unbbayes.prs.mebn.exception.MEBNConstructionException;
import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author user
 *
 */
public class DomainMFragTest extends TestCase {

	MultiEntityBayesianNetwork mebn = null;
	
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
		DomainMFrag mfrag = new DomainMFrag("TestConstructor",mebn);
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
		DomainMFrag mfrag = new DomainMFrag("testGetMultiEntityBayesianNetwork",mebn);
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
		DomainMFrag mfrag = new DomainMFrag("testAddNode",mebn);
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
		DomainMFrag mfrag = new DomainMFrag("testAddNode",mebn);
		Node node = new ResidentNode();
		Node node2 = new InputNode();
		Node node3 = new ContextNode("testAddNode",mfrag);
		
		mfrag.addNode(node);
		mfrag.addNode(node2);
		mfrag.addNode(node3);
		
		// Test
		
		mfrag.removeNode(null);
		assertEquals(mfrag.getNodeCount(), 3);	
		assertEquals(mfrag.getNodeList().size(), 3);
		assertTrue(!mfrag.getNodeList().contains(null));
		assertTrue(mfrag.getNodeList().contains(node));
		assertTrue(mfrag.getNodeList().contains(node2));
		assertTrue(mfrag.getNodeList().contains(node3));
		
		mfrag.removeNode(node);
		assertEquals(mfrag.getNodeCount(), 2);	
		assertEquals(mfrag.getNodeList().size(), 2);
		assertTrue(!mfrag.getNodeList().contains(null));
		assertTrue(!mfrag.getNodeList().contains(node));
		assertTrue(mfrag.getNodeList().contains(node2));
		assertTrue(mfrag.getNodeList().contains(node3));
		
		mfrag.removeNode(node2);
		assertEquals(mfrag.getNodeCount(), 1);	
		assertEquals(mfrag.getNodeList().size(), 1);
		assertTrue(!mfrag.getNodeList().contains(null));
		assertTrue(!mfrag.getNodeList().contains(node));
		assertTrue(!mfrag.getNodeList().contains(node2));
		assertTrue(mfrag.getNodeList().contains(node3));
		
		mfrag.removeNode(node3);
		assertEquals(mfrag.getNodeCount(), 0);	
		assertEquals(mfrag.getNodeList().size(), 0);
		assertTrue(!mfrag.getNodeList().contains(null));
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
		fail("addResidentNode is not visible. Impossible to test removeResidentNode"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MFrag#removeInputNode(unbbayes.prs.mebn.InputNode)}.
	 */
	public void testRemoveInputNode() {
		fail("addInputNode is not visible. Impossible to test removeResidentNode"); // TODO
	}

	

	/**
	 * Test method for {@link unbbayes.prs.mebn.MFrag#getOrdinaryVariableNum()}.
	 * Test method for {@link unbbayes.prs.mebn.MFrag#addOrdinaryVariable(OrdinaryVariable)}.
	 * Test method for {@link unbbayes.prs.mebn.MFrag#removeOrdinaryVariable(OrdinaryVariable)}.
	 * Test method for {@link unbbayes.prs.mebn.MFrag#containsOrdinaryVariable(unbbayes.prs.mebn.OrdinaryVariable)}.
	 */
	public void testAddRemoveOrdinaryVariableNum() {
		DomainMFrag mfrag = new DomainMFrag("testGetOrdinaryVariableNum", mebn);
		OrdinaryVariable ov1 = new OrdinaryVariable("ov1",Type.typeCategoryLabel,mfrag);
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
		
		
		
	}

	

	

	

	/**
	 * Test method for {@link unbbayes.prs.mebn.MFrag#getOrdinaryVariableList()}.
	 */
	public void testGetOrdinaryVariableList() {
		DomainMFrag mfrag = new DomainMFrag("testGetOrdinaryVariableNum", mebn);
		OrdinaryVariable ov1 = new OrdinaryVariable("ov1",Type.typeCategoryLabel,mfrag);
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
		
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MFrag#containsNode(unbbayes.prs.Node)}.
	 */
	public void testContainsNode() {
		DomainMFrag mfrag = new DomainMFrag("testAddNode",mebn);
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
		
		DomainMFrag mfrag = new DomainMFrag("testAddNode",mebn);
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
		DomainMFrag mfrag = new DomainMFrag("testAddNode",mebn);
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
		
		DomainMFrag mfrag = new DomainMFrag("testAddNode",mebn);
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
	 */
	public void testHasEdge() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MFrag#getInputNodeList()}.
	 */
	public void testGetInputNodeList() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MFrag#setInputNodeList(java.util.List)}.
	 */
	public void testSetInputNodeList() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MFrag#getResidentNodeList()}.
	 */
	public void testGetResidentNodeList() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MFrag#setResidentNodeList(java.util.List)}.
	 */
	public void testSetResidentNodeList() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MFrag#setOrdinaryVariableNum(int)}.
	 */
	public void testSetOrdinaryVariableNum() {
		fail("Not yet implemented"); // TODO
	}

	
	

	/**
	 * Test method for {@link unbbayes.prs.mebn.DomainMFrag#delete()}.
	 */
	public void testDelete() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.DomainMFrag#addOrdinaryVariable(unbbayes.prs.mebn.OrdinaryVariable)}.
	 */
	public void testAddOrdinaryVariable() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.DomainMFrag#removeOrdinaryVariable(unbbayes.prs.mebn.OrdinaryVariable)}.
	 */
	public void testRemoveOrdinaryVariable() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.DomainMFrag#addEdge(unbbayes.prs.Edge)}.
	 */
	public void testAddEdge() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.DomainMFrag#DomainMFrag(java.lang.String, unbbayes.prs.mebn.MultiEntityBayesianNetwork)}.
	 */
	public void testDomainMFrag() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.DomainMFrag#addContextNode(unbbayes.prs.mebn.ContextNode)}.
	 */
	public void testAddContextNode() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.DomainMFrag#addGenerativeInputNode(unbbayes.prs.mebn.GenerativeInputNode)}.
	 */
	public void testAddGenerativeInputNode() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.DomainMFrag#addDomainResidentNode(unbbayes.prs.mebn.DomainResidentNode)}.
	 */
	public void testAddDomainResidentNode() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.DomainMFrag#removeContextNode(unbbayes.prs.mebn.ContextNode)}.
	 */
	public void testRemoveContextNode() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.DomainMFrag#removeGenerativeInputNode(unbbayes.prs.mebn.GenerativeInputNode)}.
	 */
	public void testRemoveGenerativeInputNode() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.DomainMFrag#removeDomainResidentNode(unbbayes.prs.mebn.DomainResidentNode)}.
	 */
	public void testRemoveDomainResidentNode() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.DomainMFrag#getGenerativeInputNodeList()}.
	 */
	public void testGetGenerativeInputNodeList() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.DomainMFrag#getDomainResidentNodeList()}.
	 */
	public void testGetDomainResidentNodeList() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.DomainMFrag#getContextNodeList()}.
	 */
	public void testGetContextNodeList() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.DomainMFrag#getDomainResidentNodeCount()}.
	 */
	public void testGetDomainResidentNodeCount() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.DomainMFrag#getContextNodeCount()}.
	 */
	public void testGetContextNodeCount() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.DomainMFrag#getGenerativeInputNodeCount()}.
	 */
	public void testGetGenerativeInputNodeCount() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.DomainMFrag#getDomainResidentNodeNum()}.
	 */
	public void testGetDomainResidentNodeNum() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.DomainMFrag#getContextNodeNum()}.
	 */
	public void testGetContextNodeNum() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.DomainMFrag#getGenerativeInputNodeNum()}.
	 */
	public void testGetGenerativeInputNodeNum() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.DomainMFrag#containsContextNode(unbbayes.prs.mebn.ContextNode)}.
	 */
	public void testContainsContextNode() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.DomainMFrag#containsDomainResidentNode(unbbayes.prs.mebn.DomainResidentNode)}.
	 */
	public void testContainsDomainResidentNode() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.DomainMFrag#containsGenerativeInputNode(unbbayes.prs.mebn.GenerativeInputNode)}.
	 */
	public void testContainsGenerativeInputNode() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.DomainMFrag#containsOrdinaryVariableDomain(unbbayes.prs.mebn.OrdinaryVariable)}.
	 */
	public void testContainsOrdinaryVariableDomain() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 *  Use this for test suite creation
	 */
	public static Test suite() {
		return new TestSuite(DomainMFragTest.class);
	}
}
