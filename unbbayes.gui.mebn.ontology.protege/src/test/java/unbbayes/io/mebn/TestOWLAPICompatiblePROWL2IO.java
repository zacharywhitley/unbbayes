/**
 * 
 */
package unbbayes.io.mebn;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import unbbayes.io.exception.LoadException;
import unbbayes.io.mebn.owlapi.OWLAPICompatiblePROWL2IO;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.entity.ObjectEntity;

/**
 * @author MImran
 *
 */
public class TestOWLAPICompatiblePROWL2IO extends TestCase {
	
	private OWLAPICompatiblePROWL2IO io = null;

	/**
	 * @param name
	 */
	public TestOWLAPICompatiblePROWL2IO(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		io = (OWLAPICompatiblePROWL2IO) OWLAPICompatiblePROWL2IO.newInstance();
		assertNotNull(io);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Test method for {@link unbbayes.io.mebn.owlapi.OWLAPICompatiblePROWL2IO#loadObjectEntity(org.semanticweb.owlapi.model.OWLOntology, unbbayes.prs.mebn.MultiEntityBayesianNetwork)}
	 * to see if the hierarchy of object entities are properly loaded.
	 * @throws URISyntaxException 
	 * @throws IOException 
	 * @throws LoadException 
	 */
	public void testLoadObjectEntityHierarchy() throws LoadException, IOException, URISyntaxException {
		MultiEntityBayesianNetwork mebn = (MultiEntityBayesianNetwork) io.load(new File(getClass().getResource("/VehicleIdentificationTBox/VehicleIdentificationTBoxHierarchySample.owl").toURI()));
		
		// check those entities with no parent/children
		ObjectEntity entity = mebn.getObjectEntityContainer().getObjectEntityByName("Report");
		assertNotNull(entity);
		List<ObjectEntity> parents = mebn.getObjectEntityContainer().getParentsOfObjectEntity(entity);
		assertNotNull(parents);
		assertEquals(1, parents.size());
		assertTrue(parents.contains(mebn.getObjectEntityContainer().getRootObjectEntity()));
		List<ObjectEntity> children = mebn.getObjectEntityContainer().getChildrenOfObjectEntity(entity);
		assertNotNull(children);
		assertEquals(0, children.size());
		
		entity = mebn.getObjectEntityContainer().getObjectEntityByName("TimeStep");
		assertNotNull(entity);
		parents = mebn.getObjectEntityContainer().getParentsOfObjectEntity(entity);
		assertNotNull(parents);
		assertEquals(1, parents.size());
		assertTrue(parents.contains(mebn.getObjectEntityContainer().getRootObjectEntity()));
		children = mebn.getObjectEntityContainer().getChildrenOfObjectEntity(entity);
		assertNotNull(children);
		assertEquals(0, children.size());
		
		entity = mebn.getObjectEntityContainer().getObjectEntityByName("Region");
		assertNotNull(entity);
		parents = mebn.getObjectEntityContainer().getParentsOfObjectEntity(entity);
		assertNotNull(parents);
		assertEquals(1, parents.size());
		assertTrue(parents.contains(mebn.getObjectEntityContainer().getRootObjectEntity()));
		children = mebn.getObjectEntityContainer().getChildrenOfObjectEntity(entity);
		assertNotNull(children);
		assertEquals(0, children.size());
		
		
		// check object entity with children
		
		ObjectEntity objectEntity = mebn.getObjectEntityContainer().getObjectEntityByName("Object");
		assertNotNull(objectEntity);
		ObjectEntity vehicleEntity = mebn.getObjectEntityContainer().getObjectEntityByName("Vehicle");
		assertNotNull(vehicleEntity);
		ObjectEntity trackedVehicleEntity = mebn.getObjectEntityContainer().getObjectEntityByName("TrackedVehicle");
		assertNotNull(trackedVehicleEntity);
		ObjectEntity wheeledVehicleEntity = mebn.getObjectEntityContainer().getObjectEntityByName("WheeledVehicle");
		assertNotNull(wheeledVehicleEntity);
		
		entity = objectEntity;
		parents = mebn.getObjectEntityContainer().getParentsOfObjectEntity(entity);
		assertNotNull(parents);
		assertEquals(1, parents.size());
		assertTrue(parents.contains(mebn.getObjectEntityContainer().getRootObjectEntity()));
		
		children = mebn.getObjectEntityContainer().getChildrenOfObjectEntity(entity);
		assertNotNull(children);
		assertEquals(1, children.size());
		assertTrue(children.contains(vehicleEntity));
		
		entity = vehicleEntity;
		parents = mebn.getObjectEntityContainer().getParentsOfObjectEntity(entity);
		assertNotNull(parents);
		parents.remove(mebn.getObjectEntityContainer().getRootObjectEntity());	// ignore root entity
		assertEquals(1, parents.size());
		assertTrue(parents.contains(objectEntity));
		
		children = mebn.getObjectEntityContainer().getChildrenOfObjectEntity(entity);
		assertNotNull(children);
		assertEquals(2, children.size());
		assertTrue(children.contains(trackedVehicleEntity));
		assertTrue(children.contains(wheeledVehicleEntity));
		
		entity = trackedVehicleEntity;
		parents = mebn.getObjectEntityContainer().getParentsOfObjectEntity(entity);
		assertNotNull(parents);
		parents.remove(mebn.getObjectEntityContainer().getRootObjectEntity());	// ignore root entity
		assertEquals(1, parents.size());
		assertTrue(parents.contains(vehicleEntity));
		
		children = mebn.getObjectEntityContainer().getChildrenOfObjectEntity(entity);
		assertNotNull(children);
		assertEquals(0, children.size());
		
		entity = wheeledVehicleEntity;
		parents = mebn.getObjectEntityContainer().getParentsOfObjectEntity(entity);
		assertNotNull(parents);
		parents.remove(mebn.getObjectEntityContainer().getRootObjectEntity());	// ignore root entity
		assertEquals(1, parents.size());
		assertTrue(parents.contains(vehicleEntity));
		
		children = mebn.getObjectEntityContainer().getChildrenOfObjectEntity(entity);
		assertNotNull(children);
		assertEquals(0, children.size());
		
		
	}
	

	/**
	 * New versions of eclipse seem to cause conflicts between multi-thread instances of protege/owlapi
	 * when executing JUnit.
	 * Please, run this suite (see {@link #main(String[])}) in case you find such problems.
	 * @return
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite(TestOWLAPICompatiblePROWL2IO.class.getName());
		//$JUnit-BEGIN$
		suite.addTestSuite(TestOWLAPICompatiblePROWL2IO.class);
		//$JUnit-END$
		return suite;
	}
	
	public static void main(String[] args) {
		TestRunner runner = new TestRunner(System.out);
		runner.run(suite());
		System.exit(0);
	}

}
