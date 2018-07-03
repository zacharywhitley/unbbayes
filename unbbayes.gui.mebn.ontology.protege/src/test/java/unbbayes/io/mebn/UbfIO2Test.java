package unbbayes.io.mebn;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import unbbayes.prs.Graph;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.entity.ObjectEntityContainer;
import unbbayes.prs.mebn.entity.ObjectEntityInstance;
import unbbayes.prs.mebn.entity.exception.EntityInstanceAlreadyExistsException;
import unbbayes.prs.mebn.entity.exception.TypeException;
import unbbayes.util.Debug;

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
	 * This will check if creating {@link ObjectEntity} and {@link ObjectEntityInstance}
	 * from a newly created {@link MultiEntityBayesianNetwork}, saving it as PR-OWL 2, and then loading
	 * it again won't sweep  {@link ObjectEntity} and {@link ObjectEntityInstance}.
	 * @throws TypeException 
	 * @throws EntityInstanceAlreadyExistsException 
	 * @throws IOException 
	 */
	public final void testSaveEntitiesFromScratch() throws TypeException, EntityInstanceAlreadyExistsException, IOException {
		MultiEntityBayesianNetwork mebn = new MultiEntityBayesianNetwork("MEBN_From_Scratch");
		
		// create object entities
		ObjectEntityContainer entityContainer = mebn.getObjectEntityContainer();
		
		ObjectEntity myEntity = entityContainer.createObjectEntity("MyEntity");
		ObjectEntity siblingEntity = entityContainer.createObjectEntity("SiblingEntity");
		ObjectEntity childEntity = entityContainer.createObjectEntity("ChildEntity", myEntity);
		
		// create one instance for each entity
		ObjectEntityInstance myInstance = myEntity.addInstance("myInstance");
		mebn.getObjectEntityContainer().addEntityInstance(myInstance);
		ObjectEntityInstance siblingInstance = siblingEntity.addInstance("siblingInstance");
		mebn.getObjectEntityContainer().addEntityInstance(siblingInstance);
		ObjectEntityInstance childInstance = childEntity.addInstance("childInstance");
		mebn.getObjectEntityContainer().addEntityInstance(childInstance);
		
		int numEntities = mebn.getObjectEntityContainer().getListEntity().size();
		int numInstances = mebn.getObjectEntityContainer().getListEntityInstances().size();
		
		// save the project as ubf + PR-OWL 2 file
		
		UbfIO2 io = UbfIO2.getInstance();
		
		File tempFolder = Files.createTempDirectory(Paths.get(new File("./").toURI()), mebn.getName()).toFile();
		tempFolder.deleteOnExit();
		File tempFile = File.createTempFile(mebn.getName(), ".ubf", tempFolder);
		tempFile.deleteOnExit();
		
		io.save(tempFile, mebn);
		
		// load the project again
		mebn = (MultiEntityBayesianNetwork) io.load(tempFile);
		assertNotNull(mebn);
		assertNotNull(mebn.getObjectEntityContainer());
		
		// check if entities are still there
		assertEquals(numEntities, mebn.getObjectEntityContainer().getListEntity().size());
		assertTrue(mebn.getObjectEntityContainer().getObjectEntityByName(myEntity.getName()) != null);
		assertEquals(mebn.getObjectEntityContainer().getObjectEntityByName(myEntity.getName()).getName(), myEntity.getName());
		assertTrue(mebn.getObjectEntityContainer().getObjectEntityByName(siblingEntity.getName()) != null);
		assertEquals(mebn.getObjectEntityContainer().getObjectEntityByName(siblingEntity.getName()).getName(), siblingEntity.getName());
		assertTrue(mebn.getObjectEntityContainer().getObjectEntityByName(childEntity.getName()) != null);
		assertEquals(mebn.getObjectEntityContainer().getObjectEntityByName(childEntity.getName()).getName(), childEntity.getName());
		
		// check if instances are still there
		assertEquals(numInstances, mebn.getObjectEntityContainer().getListEntityInstances().size());
		assertTrue(mebn.getObjectEntityContainer().getEntityInstanceByName(myInstance.getName()) != null);
		assertEquals(mebn.getObjectEntityContainer().getEntityInstanceByName(myInstance.getName()).getName(), myInstance.getName());
		assertEquals(mebn.getObjectEntityContainer().getObjectEntityByName(myInstance.getInstanceOf().getName()).getInstanceByName( myInstance.getName()).getName(), myInstance.getName());
		assertTrue(mebn.getObjectEntityContainer().getEntityInstanceByName(siblingInstance.getName()) != null);
		assertEquals(mebn.getObjectEntityContainer().getEntityInstanceByName(siblingInstance.getName()).getName(), siblingInstance.getName());
		assertEquals(mebn.getObjectEntityContainer().getObjectEntityByName(siblingInstance.getInstanceOf().getName()).getInstanceByName( siblingInstance.getName()).getName(), siblingInstance.getName());
		assertTrue(mebn.getObjectEntityContainer().getEntityInstanceByName(childInstance.getName()) != null);
		assertEquals(mebn.getObjectEntityContainer().getEntityInstanceByName(childInstance.getName()).getName(), childInstance.getName());
		assertEquals(mebn.getObjectEntityContainer().getObjectEntityByName(childInstance.getInstanceOf().getName()).getInstanceByName( childInstance.getName()).getName(), childInstance.getName());
		
		
		tempFile.delete();
		tempFolder.delete();
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
		if (mebn.getObjectEntityContainer().getObjectEntityByName(PROWLModelUser.OBJECT_ENTITY) != null) {
			// if there is an object entity which can be retrieved by searching for ObjectEntity, then such object needs to be the root object entity (owl:Thing)
			assertEquals("Thing", mebn.getObjectEntityContainer().getObjectEntityByName(PROWLModelUser.OBJECT_ENTITY).getName());
		} else {
			// or else, the system shall not return anything with the name ObjectEntity
			assertNull(mebn.getObjectEntityContainer().getObjectEntityByName(PROWLModelUser.OBJECT_ENTITY));
		}
		assertTrue(mebn.getObjectEntityContainer().getListEntity().size() > 2);
		
		assertFalse(mebn.getMFragList().isEmpty());
		assertTrue(mebn.getMFragCount() > 0);
		for (MFrag mfrag : mebn.getMFragList()) {
			assertFalse(mfrag.getName(), mfrag.getResidentNodeList().isEmpty());
			assertFalse(mfrag.getName(), mfrag.getOrdinaryVariableList().isEmpty());
		}
		
	}
	
	/**
	 * New versions of eclipse seem to cause conflicts between multi-thread instances of protege/owlapi
	 * when executing JUnit.
	 * Please, run this suite (see {@link #main(String[])}) in case you find such problems.
	 * @return
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite(UbfIO2Test.class.getName());
		//$JUnit-BEGIN$
		suite.addTestSuite(UbfIO2Test.class);
		//$JUnit-END$
		return suite;
	}
	
	public static void main(String[] args) {
		TestRunner runner = new TestRunner(System.out);
		runner.run(suite());
	}

}
