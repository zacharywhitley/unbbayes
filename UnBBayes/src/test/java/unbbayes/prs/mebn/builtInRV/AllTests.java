package unbbayes.prs.mebn.builtInRV;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for unbbayes.prs.mebn.builtInRV.test");
		//$JUnit-BEGIN$
		suite.addTest(BuiltInRVEqualToTest.suite());
		suite.addTest(BuiltInRVIffTest.suite());
		suite.addTest(BuiltInRVNotTest.suite());
		suite.addTest(BuiltInRVOrTest.suite());
		suite.addTest(BuiltInRVForAllTest.suite());
		suite.addTest(BuiltInRVAndTest.suite());
		suite.addTest(BuiltInRVImpliesTest.suite());
		suite.addTest(BuiltInRVExistsTest.suite());
		//$JUnit-END$
		return suite;
	}

}
