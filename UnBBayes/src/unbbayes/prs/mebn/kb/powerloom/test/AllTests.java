package unbbayes.prs.mebn.kb.powerloom.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for unbbayes.prs.mebn.kb.powerloom");
		//$JUnit-BEGIN$
		suite.addTestSuite(PowerLoomKBTest.class);
		//$JUnit-END$
		return suite;
	}

}
