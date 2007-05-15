package unbbayes.prs.mebn.table.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for unbbayes.prs.mebn.table.test");
		//$JUnit-BEGIN$
		suite.addTestSuite(StateFunctionTest.class);
		suite.addTestSuite(ProbabilityFunctionTest.class);
		suite.addTestSuite(TableFunctionTest.class);
		suite.addTestSuite(MinMaxFunctionTest.class);
		suite.addTestSuite(BooleanFunctionTest.class);
		suite.addTestSuite(TableParserTest.class);
		suite.addTestSuite(IfClauseTest.class);
		suite.addTestSuite(ProbabilityFunctionOperatorTest.class);
		//$JUnit-END$
		return suite;
	}

}
