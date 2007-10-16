/**
 * 
 */
package unbbayes.prs.mebn.test;

import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.reasoner.rdfsReasoner1.AssertFRule;

import unbbayes.prs.Node;
import unbbayes.prs.mebn.BuiltInRV;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.DomainMFrag;
import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.GenerativeInputNode;
import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.builtInRV.BuiltInRVAnd;
import unbbayes.prs.mebn.builtInRV.BuiltInRVEqualTo;
import unbbayes.prs.mebn.builtInRV.test.BuiltInRVAndTest;
import unbbayes.prs.mebn.exception.MEBNException;
import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

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
			
			DomainMFrag mfrag = new DomainMFrag("testAddDomainMFrag",tempMebn);
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
			DomainMFrag mfrag = new DomainMFrag("testRemoveDomainMFrag",mebn);
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
		MFrag mfrag1 = new DomainMFrag("mfrag",mebn);
		MFrag mfrag3 = new DomainMFrag("mfrag",mebn);
		
		
		assertTrue(mebn.getMFragList().contains(mfrag1));
		assertEquals(mebn.getMFragList().get(mebn.getMFragList().indexOf(mfrag1)),mfrag1);
		
		
		assertTrue(mebn.getMFragList().contains(mfrag3));
		assertEquals(mebn.getMFragList().get(mebn.getMFragList().indexOf(mfrag3)),mfrag3);
		
		
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MultiEntityBayesianNetwork#getDomainMFragList()}.
	 */
	public void testGetDomainMFragList() {
		MFrag mfrag1 = new DomainMFrag("mfrag",mebn);
		MFrag mfrag3 = new DomainMFrag("mfrag",mebn);
		
		assertTrue(mebn.getDomainMFragList().contains(mfrag1));
		assertEquals(mebn.getDomainMFragList().get(mebn.getMFragList().indexOf(mfrag1)),mfrag1);
		
		assertTrue(mebn.getDomainMFragList().contains(mfrag3));
		assertEquals(mebn.getDomainMFragList().get(mebn.getDomainMFragList().indexOf(mfrag3)),mfrag3);
		
	}


	/**
	 * Test method for {@link unbbayes.prs.mebn.MultiEntityBayesianNetwork#getMFragCount()}.
	 */
	public void testGetMFragCount() {
		MFrag mfrag1 = new DomainMFrag("mfrag",mebn);
		MFrag mfrag3 = new DomainMFrag("mfrag",mebn);
		
		assertEquals(mebn.getMFragCount(),2);
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MultiEntityBayesianNetwork#getCurrentMFrag()}.
	 */
	public void testGetCurrentMFrag() {
		assertNull(mebn.getCurrentMFrag());
		
		DomainMFrag mfrag = new DomainMFrag("mfrag",this.tempMebn);
		
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
		MFrag dmfrag = new DomainMFrag("mfrag",mebn);
		
		Node node1 = new DomainResidentNode("resident",(DomainMFrag)dmfrag);
		Node node2 = new InputNode();
		mebn.addNode(node2);
		Node node3 = new ContextNode("context",(DomainMFrag)dmfrag);
		
		assertNotNull(mebn.getNodeList());
		assertTrue(mebn.getNodeList().contains(node1));
		assertTrue(mebn.getNodeList().contains(node2));
		assertTrue(mebn.getNodeList().contains(node3));
		
		
		MFrag dmfrag2 = new DomainMFrag("mfrag",tempMebn);
		Node node4 = new DomainResidentNode("resident",(DomainMFrag)dmfrag2);
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
		
		DomainMFrag mfrag1 = new DomainMFrag("mfrag1",mebn);
		DomainMFrag mfrag2 = new DomainMFrag("mfrag2",mebn);
		DomainMFrag mfrag3 = new DomainMFrag("mfrag3",mebn);
		DomainMFrag mfrag4 = new DomainMFrag("mfrag4",mebn);
		
		// This is odd! Why should we add a mfrag when we just added them at its constructor?!
		mebn.addDomainMFrag(mfrag1);
		mebn.addDomainMFrag(mfrag2);
		mebn.addDomainMFrag(mfrag3);
		mebn.addDomainMFrag(mfrag4);
		
		mfrag1.addDomainResidentNode(new DomainResidentNode("node1",mfrag1));
		mfrag2.addDomainResidentNode(new DomainResidentNode("node2",mfrag2));
		mfrag3.addDomainResidentNode(new DomainResidentNode("node3",mfrag3));
		mfrag4.addDomainResidentNode(new DomainResidentNode("node4",mfrag4));
		
		mfrag2.addGenerativeInputNode(new GenerativeInputNode("input2",mfrag2));
		mfrag3.addGenerativeInputNode(new GenerativeInputNode("input3",mfrag3));
		mfrag4.addGenerativeInputNode(new GenerativeInputNode("input4",mfrag4));
		
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
