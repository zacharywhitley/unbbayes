package unbbayes.prs.oobn.compiler;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import unbbayes.io.exception.LoadException;
import unbbayes.io.oobn.IObjectOrientedBayesianNetworkIO;
import unbbayes.io.oobn.impl.DefaultOOBNIO;
import unbbayes.prs.Graph;
import unbbayes.prs.Network;
import unbbayes.prs.Node;
import unbbayes.prs.bn.TreeVariable;
import unbbayes.prs.msbn.AbstractMSBN;
import unbbayes.prs.msbn.SubNetwork;
import unbbayes.prs.oobn.IOOBNClass;
import unbbayes.prs.oobn.IObjectOrientedBayesianNetwork;
import unbbayes.prs.oobn.compiler.impl.OOBNToSingleAgentMSBNCompiler;
import unbbayes.prs.oobn.impl.ObjectOrientedBayesianNetwork;
import junit.framework.TestCase;

public class OOBNToSingleAgentMSBNCompilerTest extends TestCase {

	public OOBNToSingleAgentMSBNCompilerTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public final void testCompile() throws Exception {
		IOOBNCompiler compiler = OOBNToSingleAgentMSBNCompiler.newInstance();
		assertNotNull(compiler);
		
		IObjectOrientedBayesianNetworkIO io = DefaultOOBNIO.newInstance(ObjectOrientedBayesianNetwork.newInstance(""));
		assertNotNull(io);
		
		assertNotNull(getClass().getResource("/StarTrek/HarmPotentialST4T3.oobn"));
		assertTrue(new File(getClass().getResource("/StarTrek/HarmPotentialST4T3.oobn").toURI()).isFile());
		assertTrue(new File(getClass().getResource("/StarTrek/HarmPotentialST4T3.oobn").toURI()).exists());
		
		IObjectOrientedBayesianNetwork oobn = (IObjectOrientedBayesianNetwork) io.load(new File(getClass().getResource("/StarTrek/HarmPotentialST4T3.oobn").toURI()));
		assertNotNull(oobn);
		
		// find HarmPotentialST4T3
		IOOBNClass harmPotential = null;
		for (IOOBNClass oobnClass : oobn.getOOBNClassList()) {
			if (oobnClass.getClassName().equalsIgnoreCase("HarmPotentialST4T3")) {
				harmPotential = oobnClass;
			}
		}
		
		assertNotNull(harmPotential);
		
		AbstractMSBN msbn = (AbstractMSBN) compiler.compile(oobn, harmPotential);
		assertNotNull(msbn);
		
		msbn.compile();
		
		for (int subnetIndex = 0; subnetIndex < msbn.getNetCount(); subnetIndex++) {
			SubNetwork subnet = msbn.getNetAt(subnetIndex);
			assertNotNull(subnet);
			System.out.println("Subnet: " + subnet.getName());
			for (Node node : subnet.getNodes()) {
				if (node instanceof TreeVariable) {
					System.out.print("\t Marginal probability of node " + node.getName() + ": ");
					for (int stateIndex = 0; stateIndex < node.getStatesSize(); stateIndex++) {
						System.out.print(node.getStateAt(stateIndex) + "=" + ((TreeVariable) node).getMarginalAt(stateIndex) + ", ");
					}
					System.out.println();
				}
			}
		}
		
	}

}
