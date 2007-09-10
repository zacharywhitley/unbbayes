/**
 * 
 */
package unbbayes.prs.mebn.test;

import unbbayes.prs.Node;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.DomainMFrag;
import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.GenerativeInputNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.builtInRV.test.BuiltInRVAndTest;
import unbbayes.util.NodeList;
import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author user
 *
 */
public class MFragTest extends TestCase {

	
	/**
	 * @param arg0	
	 */
	public MFragTest(String arg0) {
		super(arg0);
	}
	
	
	public void testContainsNode(){
		MultiEntityBayesianNetwork mebn = new MultiEntityBayesianNetwork("teste");
		DomainMFrag mfrag1 = new DomainMFrag("mfrag1",mebn);
		DomainMFrag mfrag2 = new DomainMFrag("mfrag1",mebn);
		DomainMFrag mfrag3 = new DomainMFrag("mfrag1",mebn);
		
		DomainResidentNode resident1 = new DomainResidentNode("resident1",mfrag1);
		DomainResidentNode resident2 = new DomainResidentNode("resident2",mfrag2);
		GenerativeInputNode input1 = new GenerativeInputNode("resident1",mfrag2);
		try {
			input1.setInputInstanceOf(resident1);
		} catch (Exception e) {
			fail("A cycle has been found");
		}
		resident2.addParent(input1);
		ContextNode context1 = new ContextNode("resident1",mfrag3);
		
		assertTrue(mfrag1.containsNode(resident1));
		assertNotNull(mfrag1.containsNode("resident1"));
		assertTrue(!mfrag1.containsNode(resident2));
		assertNull(mfrag1.containsNode("resident2"));
		// TODO
	}

	
	
	/**
	 *  Use this for test suite creation
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite("Test for unbbayes.prs.mebn.test.MFragTest");
		//$JUnit-BEGIN$
		suite.addTest(FindingMFragTest.suite());
		suite.addTest(DomainMFragTest.suite());
		
		return suite;
	}
}
