
package unbbayes.test;

import junit.framework.*;
import unbbayes.prs.bn.test.*;
import unbbayes.prs.msbn.test.TestTopologicalTransformation;

public class ProjectSuite extends TestCase {

  public ProjectSuite(String s) {
    super(s);
  }

  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite(TestProbabilisticNetwork.class);
    suite.addTestSuite(TestTopologicalTransformation.class);
    return suite;
  }
}
