package unbbayes.test;

import junit.framework.*;

import unbbayes.datamining.datamanipulation.test.*;
import unbbayes.prs.bn.test.*;
import unbbayes.prs.msbn.test.TestCompiled;
import unbbayes.prs.msbn.test.TestTopologicalTransformation;

public class ProjectSuite extends TestCase {

  public ProjectSuite(String s) {
    super(s);
  }

  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite(TestProbabilisticNetwork.class);
    suite.addTestSuite(TestTopologicalTransformation.class);
    suite.addTestSuite(TestCompiled.class);
    suite.addTestSuite(TestUtils.class); 
    return suite;
  }

  public static void main(String[] args)
  {
    new junit.swingui.TestRunner().run(ProjectSuite.class);
  }
}
