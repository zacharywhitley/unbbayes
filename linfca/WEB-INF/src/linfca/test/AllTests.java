
package linfca.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for linfca.test");
		//$JUnit-BEGIN$
		suite.addTest(new TestSuite(ValidarUsuarioFeatureTest.class));
		//$JUnit-END$
		return suite;
	}
}
