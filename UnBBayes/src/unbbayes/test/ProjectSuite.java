
package unbbayes.test;

import junit.framework.*;
import unbbayes.prs.bn.test.*;

public class ProjectSuite extends TestCase {

  public ProjectSuite(String s) {
    super(s);
  }

  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite(TestProbabilisticNetwork.class);
    return suite;
  }
}
