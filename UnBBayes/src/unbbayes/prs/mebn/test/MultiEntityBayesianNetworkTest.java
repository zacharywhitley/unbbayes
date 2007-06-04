/**
 * 
 */
package unbbayes.prs.mebn.test;

import java.util.List;

import unbbayes.prs.mebn.DomainMFrag;
import unbbayes.prs.mebn.FindingMFrag;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
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
	 * Test method for {@link unbbayes.prs.mebn.MultiEntityBayesianNetwork#addFindingMFrag(unbbayes.prs.mebn.FindingMFrag)}.
	 */
	public void testAddFindingMFrag() {
		try {
			FindingMFrag finding = new FindingMFrag("testAddFindingMFrag",tempMebn);
			assertEquals(finding.getMultiEntityBayesianNetwork(),tempMebn);
			assertEquals(tempMebn.getMFragCount(),1);
			assertTrue(tempMebn.getFindingMFragList().contains(finding));
			assertTrue(tempMebn.getMFragList().contains(finding));
			
			
			mebn.addFindingMFrag(finding);
			assertEquals(finding.getMultiEntityBayesianNetwork(),mebn);
			assertEquals(mebn.getMFragCount(),1);
			assertTrue(mebn.getFindingMFragList().contains(finding));
			assertTrue(mebn.getMFragList().contains(finding));
			
			assertEquals(tempMebn.getMFragCount(),0);
			assertTrue(!tempMebn.getFindingMFragList().contains(finding));
			assertTrue(!tempMebn.getMFragList().contains(finding));
			assertTrue(tempMebn.getFindingMFragList().isEmpty());
			assertTrue(tempMebn.getMFragList().isEmpty());
			
		} catch (Exception e) {
			fail(e.getMessage());
		}
		
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MultiEntityBayesianNetwork#removeFindingMFrag(unbbayes.prs.mebn.FindingMFrag)}.
	 */
	public void testRemoveFindingMFrag() {
		try {
			FindingMFrag finding = new FindingMFrag("testRemoveFindingMFrag",mebn);
			assertEquals(finding.getMultiEntityBayesianNetwork(),mebn);
			assertEquals(mebn.getMFragCount(),1);
			assertTrue(mebn.getFindingMFragList().contains(finding));
			assertTrue(mebn.getMFragList().contains(finding));
			
			mebn.removeFindingMFrag(finding);
			
			assertNull(finding.getMultiEntityBayesianNetwork());
			assertEquals(mebn.getMFragCount(),0);
			assertTrue(!mebn.getFindingMFragList().contains(finding));
			assertTrue(!mebn.getMFragList().contains(finding));
			
			
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MultiEntityBayesianNetwork#getMFragList()}.
	 */
	public void testGetMFragList() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MultiEntityBayesianNetwork#getDomainMFragList()}.
	 */
	public void testGetDomainMFragList() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MultiEntityBayesianNetwork#getFindingMFragList()}.
	 */
	public void testGetFindingMFragList() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MultiEntityBayesianNetwork#getMFragCount()}.
	 */
	public void testGetMFragCount() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MultiEntityBayesianNetwork#getCurrentMFrag()}.
	 */
	public void testGetCurrentMFrag() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MultiEntityBayesianNetwork#setCurrentMFrag(unbbayes.prs.mebn.MFrag)}.
	 */
	public void testSetCurrentMFrag() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MultiEntityBayesianNetwork#getBuiltInRVList()}.
	 */
	public void testGetBuiltInRVList() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MultiEntityBayesianNetwork#addBuiltInRVList(unbbayes.prs.mebn.BuiltInRV)}.
	 */
	public void testAddBuiltInRVList() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MultiEntityBayesianNetwork#getNodeList()}.
	 */
	public void testGetNodeList() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MultiEntityBayesianNetwork#getDomainMFragNum()}.
	 */
	public void testGetDomainMFragNum() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MultiEntityBayesianNetwork#getContextNodeNum()}.
	 */
	public void testGetContextNodeNum() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MultiEntityBayesianNetwork#setContextNodeNum(int)}.
	 */
	public void testSetContextNodeNum() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MultiEntityBayesianNetwork#plusContextNodeNul()}.
	 */
	public void testPlusContextNodeNul() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MultiEntityBayesianNetwork#getDomainResidentNodeNum()}.
	 */
	public void testGetDomainResidentNodeNum() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MultiEntityBayesianNetwork#setDomainResidentNodeNum(int)}.
	 */
	public void testSetDomainResidentNodeNum() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MultiEntityBayesianNetwork#plusDomainResidentNodeNum()}.
	 */
	public void testPlusDomainResidentNodeNum() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MultiEntityBayesianNetwork#getGenerativeInputNodeNum()}.
	 */
	public void testGetGenerativeInputNodeNum() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MultiEntityBayesianNetwork#setGenerativeInputNodeNum(int)}.
	 */
	public void testSetGenerativeInputNodeNum() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MultiEntityBayesianNetwork#plusGenerativeInputNodeNum()}.
	 */
	public void testPlusGenerativeInputNodeNum() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MultiEntityBayesianNetwork#getEntityNum()}.
	 */
	public void testGetEntityNum() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MultiEntityBayesianNetwork#setEntityNum(int)}.
	 */
	public void testSetEntityNum() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MultiEntityBayesianNetwork#plusEntityNul()}.
	 */
	public void testPlusEntityNul() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MultiEntityBayesianNetwork#setDomainMFragNum(int)}.
	 */
	public void testSetDomainMFragNum() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 *  Use this for test suite creation
	 */
	public static Test suite() {
		return new TestSuite(MultiEntityBayesianNetworkTest.class);
	}
}
