/**
 * 
 */
package unbbayes.prs.mebn.test;

import unbbayes.prs.mebn.FindingMFrag;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.builtInRV.test.BuiltInRVAndTest;
import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author user
 *
 */
public class FindingMFragTest extends TestCase {

	MultiEntityBayesianNetwork mebn = null;
	/**
	 * Test method for {@link unbbayes.prs.mebn.MFrag#MFrag(java.lang.String, unbbayes.prs.mebn.MultiEntityBayesianNetwork)}.
	 */
	public void testMFrag() {
		MFrag mfrag = new FindingMFrag("TestConstructor",mebn);
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MFrag#setName(java.lang.String)}.
	 */
	public void testSetName() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MFrag#getName()}.
	 */
	public void testGetName() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MFrag#getMultiEntityBayesianNetwork()}.
	 */
	public void testGetMultiEntityBayesianNetwork() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MFrag#delete()}.
	 */
	public void testDelete() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MFrag#addNode(unbbayes.prs.Node)}.
	 */
	public void testAddNode() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MFrag#addResidentNode(unbbayes.prs.mebn.ResidentNode)}.
	 */
	public void testAddResidentNode() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MFrag#addInputNode(unbbayes.prs.mebn.InputNode)}.
	 */
	public void testAddInputNode() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MFrag#removeNode(unbbayes.prs.Node)}.
	 */
	public void testRemoveNode() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MFrag#removeResidentNode(unbbayes.prs.mebn.ResidentNode)}.
	 */
	public void testRemoveResidentNode() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MFrag#removeInputNode(unbbayes.prs.mebn.InputNode)}.
	 */
	public void testRemoveInputNode() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MFrag#addOrdinaryVariable(unbbayes.prs.mebn.OrdinaryVariable)}.
	 */
	public void testAddOrdinaryVariable() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MFrag#getOrdinaryVariableNum()}.
	 */
	public void testGetOrdinaryVariableNum() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MFrag#removeOrdinaryVariable(unbbayes.prs.mebn.OrdinaryVariable)}.
	 */
	public void testRemoveOrdinaryVariable() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MFrag#containsOrdinaryVariable(unbbayes.prs.mebn.OrdinaryVariable)}.
	 */
	public void testContainsOrdinaryVariable() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MFrag#getNodeList()}.
	 */
	public void testGetNodeList() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MFrag#getOrdinaryVariableList()}.
	 */
	public void testGetOrdinaryVariableList() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MFrag#containsNode(unbbayes.prs.Node)}.
	 */
	public void testContainsNode() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MFrag#getEdges()}.
	 */
	public void testGetEdges() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MFrag#getNodes()}.
	 */
	public void testGetNodes() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MFrag#getNodeCount()}.
	 */
	public void testGetNodeCount() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MFrag#removeEdge(unbbayes.prs.Edge)}.
	 */
	public void testRemoveEdge() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MFrag#removeEdgeByNodes(unbbayes.prs.Node, unbbayes.prs.Node)}.
	 */
	public void testRemoveEdgeByNodes() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MFrag#addEdge(unbbayes.prs.Edge)}.
	 */
	public void testAddEdge() {
		fail("Not yet implemented"); // TODO
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
	 * @param arg0
	 */
	public FindingMFragTest(String arg0) {
		super(arg0);
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
	 * Test method for {@link unbbayes.prs.mebn.FindingMFrag#FindingMFrag(java.lang.String, unbbayes.prs.mebn.MultiEntityBayesianNetwork)}.
	 */
	public void testFindingMFrag() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 *  Use this for test suite creation
	 */
	public static Test suite() {
		return new TestSuite(FindingMFragTest.class);
	}
}
