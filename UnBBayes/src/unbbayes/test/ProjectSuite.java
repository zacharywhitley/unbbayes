
package unbbayes.test;

import junit.framework.*;
import unbbayes.jprs.jbn.test.*;

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
