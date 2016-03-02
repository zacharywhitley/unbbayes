package unbbayes.io.mebn;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import unbbayes.io.exception.LoadException;
import unbbayes.prs.Graph;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.entity.ObjectEntityContainer;
import unbbayes.util.Debug;
import junit.framework.TestCase;

public class UbfIO2Test extends TestCase {


	public UbfIO2Test(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Test method for {@link UbfIO2#load(java.io.File)}
	 */
	public final void testLoadFile() {
		Debug.setDebug(true);
		
		UbfIO2 io = UbfIO2.getInstance();
		assertNotNull(io);
		
		Graph graph;
		try {
			graph = io.load(new File(getClass().getResource("VehicleIdentification.ubf").toURI()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		assertNotNull(graph);
		assertTrue(graph instanceof MultiEntityBayesianNetwork);
		
		MultiEntityBayesianNetwork mebn = (MultiEntityBayesianNetwork) graph;
		
		assertNotNull(mebn.getObjectEntityContainer().getRootObjectEntity());
		assertEquals(mebn.getObjectEntityContainer().getRootObjectEntity().getName(), "Thing");
		assertNull(mebn.getObjectEntityContainer().getObjectEntityByName(ObjectEntityContainer.OBJECT_ENTITY));
		assertTrue(mebn.getObjectEntityContainer().getListEntity().size() > 2);
		
		assertFalse(mebn.getMFragList().isEmpty());
		assertTrue(mebn.getMFragCount() > 0);
		for (MFrag mfrag : mebn.getMFragList()) {
			assertFalse(mfrag.getName(), mfrag.getResidentNodeList().isEmpty());
			assertFalse(mfrag.getName(), mfrag.getOrdinaryVariableList().isEmpty());
		}
		
	}

}
