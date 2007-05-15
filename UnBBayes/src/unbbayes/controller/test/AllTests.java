package unbbayes.controller.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for unbbayes.controller.test");
		//$JUnit-BEGIN$
		suite.addTest(MEBNControllerTest.suite());
		//$JUnit-END$
		return suite;
	}

}
