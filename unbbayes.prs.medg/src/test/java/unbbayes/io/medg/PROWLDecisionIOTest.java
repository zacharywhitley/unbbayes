/**
 * 
 */
package unbbayes.io.medg;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import junit.framework.TestCase;
import unbbayes.io.exception.LoadException;
import unbbayes.prs.Graph;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.medg.MultiEntityUtilityNode;
import unbbayes.prs.medg.compiler.MultiEntityUtilityFunctionCompiler;

/**
 * @author Shou Matsumoto
 *
 */
public class PROWLDecisionIOTest extends TestCase {

	public PROWLDecisionIOTest(String name) {
		super(name);
	}

	/**
	 * @throws java.lang.Exception
	 */
	protected void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	protected void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link unbbayes.io.mebn.PrOwlIO#save(java.io.File, unbbayes.prs.Graph)}
	 * to check if a bug which was happening when renaming an utility node
	 * loaded from file will cause bug after saving new file and loading it again.
	 * @throws URISyntaxException 
	 * @throws IOException 
	 * @throws LoadException 
	 */
	public final void testSaveRenamedUtilityNode() throws LoadException, IOException, URISyntaxException {
		PROWLDecisionIO io = (PROWLDecisionIO) PROWLDecisionIO.getInstance();
		
		MultiEntityBayesianNetwork medg = (MultiEntityBayesianNetwork) io.load(new File(getClass().getResource("/utilityRenameTest.owl").toURI()));
		assertNotNull(medg);
		
		assertEquals(1, medg.getMFragCount());	// it supposedly have only 1 MFrag
		// MFrag supposedly have only 1 utility node
		assertNotNull(medg.getDomainMFragList().get(0).getResidentNodeList());
		assertEquals(1, medg.getDomainMFragList().get(0).getResidentNodeList().size());
		assertTrue(medg.getDomainMFragList().get(0).getResidentNodeList().get(0) instanceof MultiEntityUtilityNode);
		
		// rename the utility node
		MultiEntityUtilityNode utilityNode = (MultiEntityUtilityNode) medg.getDomainMFragList().get(0).getResidentNodeList().get(0);
		assertFalse(utilityNode.getName().equalsIgnoreCase("SomeArbitraryName"));
		utilityNode.setName("SomeArbitraryName");
		
		// save MTheory as new file
		File tempFile = File.createTempFile(getClass().getName(), "_Test.owl", new File(getClass().getResource("/").toURI()));
		io.save(tempFile, medg);
		
		
		// loading should work fine now
		Graph loadedGraph = io.load(tempFile);
		assertNotNull(loadedGraph);
		assertTrue(loadedGraph instanceof MultiEntityBayesianNetwork);
		
		// sanity check
		medg = (MultiEntityBayesianNetwork) loadedGraph;
		assertEquals(1, medg.getMFragCount());	// it supposedly have only 1 MFrag
		// MFrag supposedly have only 1 utility node
		assertNotNull(medg.getDomainMFragList().get(0).getResidentNodeList());
		assertEquals(1, medg.getDomainMFragList().get(0).getResidentNodeList().size());
		assertTrue(medg.getDomainMFragList().get(0).getResidentNodeList().get(0) instanceof MultiEntityUtilityNode);
		
		
	}
	
	/**
	 * Test method for {@link PROWLDecisionIO#load(File)} check that a bug which was not properly setting 
	 * LPD compiler of utility nodes to be set up when loading new prowl decision files.
	 * @throws URISyntaxException 
	 * @throws IOException 
	 * @throws LoadException 
	 */
	public final void testLoadUtilityNodeLPDCompiler() throws LoadException, IOException, URISyntaxException {
		PROWLDecisionIO io = (PROWLDecisionIO) PROWLDecisionIO.getInstance();
		
		MultiEntityBayesianNetwork medg = (MultiEntityBayesianNetwork) io.load(new File(getClass().getResource("/utilityRenameTest.owl").toURI()));
		assertNotNull(medg);
		
		assertEquals(1, medg.getMFragCount());	// it supposedly have only 1 MFrag
		// MFrag supposedly have only 1 utility node
		assertNotNull(medg.getDomainMFragList().get(0).getResidentNodeList());
		assertEquals(1, medg.getDomainMFragList().get(0).getResidentNodeList().size());
		assertTrue(medg.getDomainMFragList().get(0).getResidentNodeList().get(0) instanceof MultiEntityUtilityNode);
		
		// check type of LPD compiler
		MultiEntityUtilityNode utilityNode = (MultiEntityUtilityNode) medg.getDomainMFragList().get(0).getResidentNodeList().get(0);
		assertNotNull(utilityNode);
		assertNotNull(utilityNode.getCompiler());
		assertTrue(utilityNode.getCompiler() instanceof MultiEntityUtilityFunctionCompiler);
		
		
	}

}
