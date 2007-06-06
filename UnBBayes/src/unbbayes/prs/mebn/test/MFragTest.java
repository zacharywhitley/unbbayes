/**
 * 
 */
package unbbayes.prs.mebn.test;

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
public class MFragTest extends TestCase {

	
	/**
	 * @param arg0
	 */
	public MFragTest(String arg0) {
		super(arg0);
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
