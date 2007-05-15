package unbbayes.prs.mebn.compiler.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for unbbayes.prs.mebn.compiler.test");
		//$JUnit-BEGIN$
		suite.addTestSuite(CompilerTest.class);
		suite.addTestSuite(CompilerUtilTest.class);
		//$JUnit-END$
		return suite;
	}

}
