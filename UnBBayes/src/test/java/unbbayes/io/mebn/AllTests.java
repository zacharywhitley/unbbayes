package unbbayes.io.mebn;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for unbbayes.io.mebn.test");
		//$JUnit-BEGIN$
		suite.addTest(UbfIoTest.suite());
		suite.addTest(LoaderPrOwlIOTest.suite());
		suite.addTest(SaverPrOwlIOTest.suite());
		suite.addTest(PrOwlIOTest.suite());
		//$JUnit-END$
		return suite;
	}

}
