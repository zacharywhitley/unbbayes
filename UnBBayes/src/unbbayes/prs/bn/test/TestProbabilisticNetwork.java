package unbbayes.prs.bn.test;

import unbbayes.io.*;
import unbbayes.prs.bn.*;
import junit.framework.*;
import java.io.*;

public class TestProbabilisticNetwork extends TestCase {

    public static File ASIA_FILE = new File("examples/asia.net");
    public static double DELTA = 0.0001;

    private ProbabilisticNetwork net;

    public TestProbabilisticNetwork(String s) {
        super(s);
    }

    public void setUp() throws Exception {
        BaseIO io = new NetIO();
        net = io.load(ASIA_FILE);
    }

    public void testCompileAsia() throws Exception {
        net.compile();
        ProbabilisticNode temp = (ProbabilisticNode)net.getNode("D");
        assertEquals(temp.getMarginalAt(0), 0.4360, DELTA);

        temp = (ProbabilisticNode)net.getNode("B");
        assertEquals(temp.getMarginalAt(0), 0.45, DELTA);

        temp = (ProbabilisticNode)net.getNode("L");
        assertEquals(temp.getMarginalAt(0), 0.055, DELTA);

        temp = (ProbabilisticNode)net.getNode("L");
        assertEquals(temp.getMarginalAt(0), 0.055, DELTA);

        temp = (ProbabilisticNode)net.getNode("T");
        assertEquals(temp.getMarginalAt(0), 0.0104, DELTA);

        temp = (ProbabilisticNode)net.getNode("X");
        assertEquals(temp.getMarginalAt(0), 0.1103, DELTA);

        temp = (ProbabilisticNode)net.getNode("S");
        assertEquals(temp.getMarginalAt(0), 0.50, DELTA);

        temp = (ProbabilisticNode)net.getNode("E");
        assertEquals(temp.getMarginalAt(0), 0.0648, DELTA);

        temp = (ProbabilisticNode)net.getNode("A");
        assertEquals(temp.getMarginalAt(0), 0.01, DELTA);
    }
}
