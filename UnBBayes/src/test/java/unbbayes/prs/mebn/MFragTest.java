/**
 * 
 */
package unbbayes.prs.mebn;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import unbbayes.util.NodeList;

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
		MFrag mfrag1 = new MFrag("mfrag1",mebn);
		MFrag mfrag2 = new MFrag("mfrag2",mebn);
		
		ResidentNode resident1 = new ResidentNode("resident1",mfrag1);
		mfrag1.addResidentNode(resident1);
		ResidentNode resident2 = new ResidentNode("resident2",mfrag2);
		mfrag2.addResidentNode(resident2);
		InputNode input1 = new InputNode("resident1",mfrag2);
		mfrag2.addInputNode(input1);
		try {
			input1.setInputInstanceOf(resident1);
		} catch (Exception e) {
			fail("A cycle has been found");
		}
		resident2.addParent(input1);
		
		NodeList temp = mfrag1.getNodeList();
		for (int i = 0; i < temp.size(); i++) {
			System.out.println(temp.get(i).getName());
			System.out.println(temp.get(i).getClass());
		}
		System.out.println("===============");
		temp = mfrag2.getNodeList();
		for (int i = 0; i < temp.size(); i++) {
			System.out.println(temp.get(i).getName());
			System.out.println(temp.get(i).getClass());
		}
		
		assertTrue(mfrag1.containsNode(resident1));
		assertNotNull(mfrag1.containsNode("resident1"));
		assertTrue(!mfrag1.containsNode(resident2));
		assertNull(mfrag1.containsNode("resident2"));
		//assertTrue(!(mfrag1.containsNode(input1))); // This is searching by node name
		
		assertTrue(mfrag2.containsNode(resident2));
		assertNotNull(mfrag2.containsNode("resident2"));
		//assertTrue(!mfrag2.containsNode(resident1));
		assertNotNull(mfrag2.containsNode("resident1"));
		assertTrue(mfrag2.containsNode(input1));
		
	}

	/**
	 *  Use this for test suite creation
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite("Test for unbbayes.prs.mebn.test.MFragTest");
		//$JUnit-BEGIN$
		suite.addTest(DomainMFragTest.suite());
		suite.addTest(new TestSuite(MFragTest.class));
		return suite;
	}
}
