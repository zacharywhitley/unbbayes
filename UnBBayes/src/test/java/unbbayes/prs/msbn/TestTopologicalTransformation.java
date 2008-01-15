package unbbayes.prs.msbn;

import java.io.File;

import unbbayes.io.NetIO;
import unbbayes.prs.msbn.SingleAgentMSBN;

import junit.framework.TestCase;

/**
 * @author Michael S. Onishi
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class TestTopologicalTransformation extends TestCase {
	private static File CYCLE_FILE = new File("examples/msbn/cycle-test");
	private static File TOPOLOGICAL_FILE = new File("examples/msbn/topological-test");
	
	public TestTopologicalTransformation(String m) {
		super(m);
	}
	
	public void testCycle() throws Exception {
		NetIO loader = new NetIO();
		SingleAgentMSBN msbn =  loader.loadMSBN(CYCLE_FILE);
		boolean cycleDetected = false;		
		try {
			msbn.compile();
		} catch (Exception e) {
			cycleDetected = true;
		}
		assertTrue("Cycle not detected", cycleDetected);
	}
	
	public void testTopological() throws Exception {
		NetIO loader = new NetIO();
		SingleAgentMSBN msbn =  loader.loadMSBN(TOPOLOGICAL_FILE);
		msbn.compile();	
	}
}
