/**
 * 
 */
package unbbayes.prs.mebn;

import unbbayes.prs.mebn.builtInRV.BuiltInRVAndTest;
import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author user
 *
 */
public class MTheoryAlgorithmTest extends TestCase {

	/**
	 * @param arg0
	 */
	public MTheoryAlgorithmTest(String arg0) {
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
	 * Test method for {@link unbbayes.prs.mebn.MTheoryAlgorithm#generateSSBN(unbbayes.prs.mebn.MultiEntityBayesianNetwork, java.util.List, java.util.List)}.
	 */
	public void testGenerateSSBN() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 *  Use this for test suite creation
	 */
	public static Test suite() {
		return new TestSuite(MTheoryAlgorithmTest.class);
	}
}
