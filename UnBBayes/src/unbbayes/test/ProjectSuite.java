package unbbayes.test;

import junit.framework.*;

import unbbayes.datamining.datamanipulation.test.*;
import unbbayes.prs.bn.test.*;

public class ProjectSuite extends TestCase {

  public ProjectSuite(String s) {
    super(s);
  }

  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite(TestProbabilisticNetwork.class);
    suite.addTestSuite(TestUtils.class);
    return suite;
  }

  public static void main(String[] args)
  {
    new junit.swingui.TestRunner().run(ProjectSuite.class);
  }
}
