/**
 * 
 */
package unbbayes.prs.mebn;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import unbbayes.prs.Node;
import unbbayes.prs.mebn.builtInRV.BuiltInRVAnd;
import unbbayes.prs.mebn.builtInRV.BuiltInRVEqualTo;

/**
 * @author user
 *
 */
public class MultiEntityBayesianNetworkTest extends TestCase {

	MultiEntityBayesianNetwork mebn = null;
	MultiEntityBayesianNetwork tempMebn = null;
	
	/**
	 * @param arg0
	 */
	public MultiEntityBayesianNetworkTest(String arg0) {
		super(arg0);
		
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		this.mebn = new MultiEntityBayesianNetwork("TestMEBN");
		this.tempMebn = new MultiEntityBayesianNetwork("TempMEBN");
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {		
		super.tearDown();
		this.mebn = null;
		this.tempMebn = null;
	}

	

	/**
	 * Test method for {@link unbbayes.prs.mebn.MultiEntityBayesianNetwork#addDomainMFrag(unbbayes.prs.mebn.DomainMFrag)}.
	 */
	public void testAddDomainMFrag() {
		try {
			
			MFrag mfrag = new MFrag("testAddDomainMFrag",tempMebn);
			assertEquals(mfrag.getMultiEntityBayesianNetwork(),tempMebn);
			assertEquals(mfrag,tempMebn.getMFragList().get(0));
			assertTrue(tempMebn.getMFragList().contains(mfrag));
			
			this.mebn.addDomainMFrag(mfrag);
			List<MFrag> list = mebn.getMFragList();
			
			assertEquals(list.get(list.indexOf(mfrag)),mfrag);
			assertEquals(this.mebn,mfrag.getMultiEntityBayesianNetwork());
			assertTrue(mebn.getMFragList().contains(mfrag));
			
			List<MFrag> tempList = tempMebn.getMFragList();
			assertTrue(!tempList.contains(mfrag));
			assertTrue(tempList.isEmpty());
			
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MultiEntityBayesianNetwork#removeDomainMFrag(unbbayes.prs.mebn.DomainMFrag)}.
	 */
	public void testRemoveDomainMFrag() {
		try {
			MFrag mfrag = new MFrag("testRemoveDomainMFrag",mebn);
			assertEquals(mebn.getMFragCount(),1);
			assertEquals(mebn.getMFragList().get(0),mfrag);
			assertEquals(mebn,mfrag.getMultiEntityBayesianNetwork());
			
			mebn.removeDomainMFrag(mfrag);
			assertEquals(mebn.getMFragCount(),0);
			assertTrue(!mebn.getMFragList().contains(mfrag));
			assertTrue(mebn.getMFragList().isEmpty());
			assertNull(mfrag.getMultiEntityBayesianNetwork());			
			
			
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MultiEntityBayesianNetwork#getMFragList()}.
	 */
	public void testGetMFragList() {
		MFrag mfrag1 = new MFrag("mfrag",mebn);
		MFrag mfrag3 = new MFrag("mfrag",mebn);
		
		
		assertTrue(mebn.getMFragList().contains(mfrag1));
		assertEquals(mebn.getMFragList().get(mebn.getMFragList().indexOf(mfrag1)),mfrag1);
		
		
		assertTrue(mebn.getMFragList().contains(mfrag3));
		assertEquals(mebn.getMFragList().get(mebn.getMFragList().indexOf(mfrag3)),mfrag3);
		
		
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MultiEntityBayesianNetwork#getDomainMFragList()}.
	 */
	public void testGetDomainMFragList() {
		MFrag mfrag1 = new MFrag("mfrag",mebn);
		MFrag mfrag3 = new MFrag("mfrag",mebn);
		
		assertTrue(mebn.getDomainMFragList().contains(mfrag1));
		assertEquals(mebn.getDomainMFragList().get(mebn.getMFragList().indexOf(mfrag1)),mfrag1);
		
		assertTrue(mebn.getDomainMFragList().contains(mfrag3));
		assertEquals(mebn.getDomainMFragList().get(mebn.getDomainMFragList().indexOf(mfrag3)),mfrag3);
		
	}


	/**
	 * Test method for {@link unbbayes.prs.mebn.MultiEntityBayesianNetwork#getMFragCount()}.
	 */
	public void testGetMFragCount() {
		MFrag mfrag1 = new MFrag("mfrag",mebn);
		MFrag mfrag3 = new MFrag("mfrag",mebn);
		
		assertEquals(mebn.getMFragCount(),2);
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MultiEntityBayesianNetwork#getCurrentMFrag()}.
	 */
	public void testGetCurrentMFrag() {
		assertNull(mebn.getCurrentMFrag());
		
		MFrag mfrag = new MFrag("mfrag",this.tempMebn);
		
		mebn.addDomainMFrag(mfrag);		
		assertEquals(mebn.getCurrentMFrag(),mfrag);
		
		mebn.removeDomainMFrag(mfrag);
		assertNull(mebn.getCurrentMFrag());
		
		mebn.setCurrentMFrag(mfrag);
		assertEquals(mebn.getCurrentMFrag(),mfrag);
	}

	

	/**
	 * Test method for {@link unbbayes.prs.mebn.MultiEntityBayesianNetwork#getBuiltInRVList()}.
	 */
	public void testGetBuiltInRVList() {
		BuiltInRV built1 = new BuiltInRVAnd();
		BuiltInRV built2 = new BuiltInRVEqualTo();

		assertTrue(!mebn.getBuiltInRVList().contains(built1));
				
		
		mebn.addBuiltInRVList(built1);
		mebn.addBuiltInRVList(built2);
		
		assertTrue(mebn.getBuiltInRVList().contains(built1));
		assertTrue(mebn.getBuiltInRVList().contains(built2));
		
		assertEquals(mebn.getBuiltInRVList().get(mebn.getBuiltInRVList().indexOf(built1)),built1);
		assertEquals(mebn.getBuiltInRVList().get(mebn.getBuiltInRVList().indexOf(built2)),built2);
		
		
		
	}

	
	/**
	 * Test method for {@link unbbayes.prs.mebn.MultiEntityBayesianNetwork#getNodeList()}.
	 */
	public void testGetNodeList() {
		MFrag dmfrag = new MFrag("mfrag",mebn);
		
		Node node1 = new ResidentNode("resident",(MFrag)dmfrag);
		Node node2 = new InputNode();
		mebn.addNode(node2);
		Node node3 = new ContextNode("context",(MFrag)dmfrag);
		
		assertNotNull(mebn.getNodeList());
		assertTrue(mebn.getNodeList().contains(node1));
		assertTrue(mebn.getNodeList().contains(node2));
		assertTrue(mebn.getNodeList().contains(node3));
		
		
		MFrag dmfrag2 = new MFrag("mfrag",tempMebn);
		Node node4 = new ResidentNode("resident",(MFrag)dmfrag2);
		assertTrue(!mebn.getNodeList().contains(node4));
		
		dmfrag.addNode(node4);
		assertTrue(mebn.getNodeList().contains(node4));
		
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MultiEntityBayesianNetwork#getDomainMFragNum()}.
	 */
	public void testGetDomainMFragNum() {
	}
	
	
	public void testGetDomainMFragByNodeName() {
		
		MultiEntityBayesianNetwork mebn = new MultiEntityBayesianNetwork("testGetDomainMFragNodeNameMEBN");
		
		MFrag mfrag1 = new MFrag("mfrag1",mebn);
		MFrag mfrag2 = new MFrag("mfrag2",mebn);
		MFrag mfrag3 = new MFrag("mfrag3",mebn);
		MFrag mfrag4 = new MFrag("mfrag4",mebn);
		
		// This is odd! Why should we add a mfrag when we just added them at its constructor?!
		mebn.addDomainMFrag(mfrag1);
		mebn.addDomainMFrag(mfrag2);
		mebn.addDomainMFrag(mfrag3);
		mebn.addDomainMFrag(mfrag4);
		
		mfrag1.addResidentNode(new ResidentNode("node1",mfrag1));
		mfrag2.addResidentNode(new ResidentNode("node2",mfrag2));
		mfrag3.addResidentNode(new ResidentNode("node3",mfrag3));
		mfrag4.addResidentNode(new ResidentNode("node4",mfrag4));
		
		mfrag2.addInputNode(new InputNode("input2",mfrag2));
		mfrag3.addInputNode(new InputNode("input3",mfrag3));
		mfrag4.addInputNode(new InputNode("input4",mfrag4));
		
		mfrag3.addContextNode(new ContextNode("context3",mfrag3));
		mfrag4.addContextNode(new ContextNode("context4",mfrag4));
		
		
		assertEquals(mebn.getDomainMFragByNodeName("node1"),mfrag1);
		assertEquals(mebn.getDomainMFragByNodeName("node2"),mfrag2);
		assertEquals(mebn.getDomainMFragByNodeName("node3"),mfrag3);
		assertEquals(mebn.getDomainMFragByNodeName("node4"),mfrag4);
		
		assertTrue(mebn.getDomainMFragByNodeName("Auszeichnung") == null);
		assertTrue(mebn.getDomainMFragByNodeName("") == null);
		assertTrue(mebn.getDomainMFragByNodeName(null) == null);
		
		assertTrue(mebn.getDomainMFragByNodeName("input2") == null);
		assertTrue(mebn.getDomainMFragByNodeName("input3") == null);
		assertTrue(mebn.getDomainMFragByNodeName("input4") == null);
		
		assertTrue(mebn.getDomainMFragByNodeName("context3") == null);
		assertTrue(mebn.getDomainMFragByNodeName("context4") == null);
	}

	// Getters, setters and simple integer adders are not tested

	/**
	 *  Use this for test suite creation
	 */
	public static Test suite() {
		return new TestSuite(MultiEntityBayesianNetworkTest.class);
	}
}
