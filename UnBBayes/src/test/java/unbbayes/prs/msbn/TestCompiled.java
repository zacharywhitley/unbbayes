package unbbayes.prs.msbn;

import java.io.File;

import unbbayes.io.NetIO;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.TreeVariable;
import unbbayes.prs.msbn.SingleAgentMSBN;
import unbbayes.prs.msbn.SubNetwork;

import junit.framework.TestCase;

/**
 * @author Michael Onishi
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class TestCompiled extends TestCase {
	public static final File FILE_5PARTC = new File("examples/msbn/5partc");
	public static final File FILE_5PARTCMONO = new File("examples/msbn/5partcmono.net");
	public static final double DELTA = 0.001;
	
	public TestCompiled(String msg) {
		super(msg);
	}
	
	public void testTopological() throws Exception {
		NetIO loader = new NetIO();
		SingleAgentMSBN msbn =  loader.loadMSBN(FILE_5PARTC);
		ProbabilisticNetwork net = loader.load(FILE_5PARTCMONO);
		net.compile(); 
		msbn.compile();
		
		for (int i = 0; i < msbn.getNetCount(); i++) {
			SubNetwork sub = msbn.getNetAt(i);
			for (int j = 0; j < sub.getNodeCount(); j++) {
				TreeVariable var = (TreeVariable) sub.getNodeAt(j);
				TreeVariable varMono = (TreeVariable) net.getNode(var.getName());
				for (int k = 0; k < var.getStatesSize(); k++) {
					assertEquals(varMono.getMarginalAt(k), var.getMarginalAt(k), DELTA);					
				}
			}	
		}
	}
		
}
