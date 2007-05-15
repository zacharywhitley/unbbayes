package unbbayes.prs.mebn.entity.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for unbbayes.prs.mebn.entity.test");
		//$JUnit-BEGIN$
		suite.addTestSuite(EntityTest.class);
		suite.addTestSuite(BooleanStatesEntityTest.class);
		suite.addTestSuite(ObjectEntityTest.class);
		suite.addTestSuite(CategoricalStatesEntityTest.class);
		suite.addTestSuite(TypeTest.class);
		//$JUnit-END$
		return suite;
	}

}
