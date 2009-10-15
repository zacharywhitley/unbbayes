package unbbayes.msbn;
import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import unbbayes.io.BaseIO;
import unbbayes.io.NetIO;
import unbbayes.io.exception.LoadException;
import unbbayes.io.msbn.impl.DefaultMSBNIO;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.msbn.SingleAgentMSBN;
import unbbayes.prs.msbn.SubNetwork;


/**
 * @author Shou Matsumoto
 * This class does the same as MSBNExample would do.
 * TODO we should actually test the result.
 */
public class MSBNExampleTest extends TestCase {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}
	
	/**
	 * A sample for MSBN API
	 * @author Michael
	 * @author Rommel
	 * @author Shou Matsumoto
	 */
	@Test
	public final void testMsbnExample()  {
		SingleAgentMSBN msbn = null;
		
		BaseIO io = DefaultMSBNIO.newInstance();
		try {
			msbn = (SingleAgentMSBN)io.load(new File("src/test/resources/testCases/msbn/5partc/"));
		} catch (LoadException e) {
			e.printStackTrace();
			fail (e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			fail (e.getMessage());
		} 
		
		try {
			msbn.compile();
		} catch (Exception e) {
			e.printStackTrace();
			fail (e.getMessage());
		}
		
		SubNetwork net = msbn.getNetAt(0);
		ProbabilisticNode node = (ProbabilisticNode) net.getNode("var_1");
		node.addFinding(1);
		try {
			net.updateEvidences();
		} catch (Exception e) {
			e.printStackTrace();
			fail (e.getMessage());
		}
		
		net = msbn.getNetAt(2);
		try {
			msbn.shiftAttention(net);
		} catch (Exception e) {
			e.printStackTrace();
			fail (e.getMessage());
		}
	}

}
