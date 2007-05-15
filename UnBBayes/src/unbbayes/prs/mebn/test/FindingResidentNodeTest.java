/**
 * 
 */
package unbbayes.prs.mebn.test;

import unbbayes.prs.mebn.builtInRV.test.BuiltInRVAndTest;
import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author user
 *
 */
public class FindingResidentNodeTest extends TestCase {

	/**
	 * @param arg0
	 */
	public FindingResidentNodeTest(String arg0) {
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
	 *  Use this for test suite creation
	 */
	public static Test suite() {
		return new TestSuite(FindingResidentNodeTest.class);
	}
}
