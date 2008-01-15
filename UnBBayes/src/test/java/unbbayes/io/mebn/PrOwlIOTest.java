/**
 * 
 */
package unbbayes.io.mebn;

import unbbayes.prs.mebn.ArgumentTest;
import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author shou
 *
 */
public class PrOwlIOTest extends TestCase {

	/**
	 * @param arg0
	 */
	public PrOwlIOTest(String arg0) {
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
	 * Test method for {@link unbbayes.io.mebn.PrOwlIO#loadMebn(java.io.File)}.
	 */
	public void testLoadMebn() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.io.mebn.PrOwlIO#saveMebn(java.io.File, unbbayes.prs.mebn.MultiEntityBayesianNetwork)}.
	 */
	public void testSaveMebn() {
		fail("Not yet implemented"); // TODO
	}
	
	/**
	 *  Use this for test suite creation
	 */
	public static Test suite() {
		return new TestSuite(PrOwlIOTest.class);
	}
}
