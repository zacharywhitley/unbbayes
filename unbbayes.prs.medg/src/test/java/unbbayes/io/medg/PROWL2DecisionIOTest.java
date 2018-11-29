/**
 * 
 */
package unbbayes.io.medg;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import junit.framework.TestCase;
import unbbayes.io.exception.LoadException;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;

/**
 * @author Shou Matsumoto
 *
 */
public class PROWL2DecisionIOTest extends TestCase {

	public PROWL2DecisionIOTest(String name) {
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
	 * Test method for {@link PROWL2DecisionIO#load(File)}, to check that punning individuals (individuals with
	 * same URI of the class it belongs to) is supported.
	 * @throws LoadException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public final void testLoadPunningIndividual() throws IOException, URISyntaxException {
		PROWL2DecisionIO io = (PROWL2DecisionIO) PROWL2DecisionIO.getInstance();
		
		MultiEntityBayesianNetwork medg = (MultiEntityBayesianNetwork) io.load(new File(getClass().getResource("/PunningTest.owl").toURI()));
		assertNotNull(medg);
		
		// make sure the entity exists
		assertEquals(2, medg.getObjectEntityContainer().getListEntity().size());	// it supposedly have only 1 entity (and extra owl:Thing entity)
		assertNotNull(medg.getObjectEntityContainer().getObjectEntityByName("PunningTestEntity"));
		
		// make sure the individual exists
		assertEquals(1, medg.getObjectEntityContainer().getObjectEntityByName("PunningTestEntity").getInstanceList().size());
		assertNotNull(medg.getObjectEntityContainer().getObjectEntityByName("PunningTestEntity").getInstanceByName("PunningTestEntity"));
		assertNotNull(medg.getObjectEntityContainer().getEntityInstanceByName("PunningTestEntity"));
		
	}

}
