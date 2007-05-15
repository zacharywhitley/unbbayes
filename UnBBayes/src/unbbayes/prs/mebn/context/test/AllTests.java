package unbbayes.prs.mebn.context.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for unbbayes.prs.mebn.context.test");
		//$JUnit-BEGIN$
		suite.addTestSuite(NodeFormulaTree.class);
		//$JUnit-END$
		return suite;
	}

}
