/**
 * 
 */
package unbbayes;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import junit.framework.TestCase;

import org.semanticweb.owlapi.reasoner.OWLReasoner;

import unbbayes.io.mebn.MebnIO;
import unbbayes.io.mebn.owlapi.OWLAPIStorageImplementorDecorator;
import unbbayes.io.mebn.protege.Protege41CompatiblePROWL2IO;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.TreeVariable;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.RandomVariableFinding;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.entity.CategoricalStateEntity;
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.entity.ObjectEntityInstance;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.kb.extension.ontology.protege.PROWL2KnowledgeBase;
import unbbayes.prs.mebn.kb.extension.ontology.protege.PROWL2KnowledgeBaseBuilder;
import unbbayes.util.Debug;

/**
 * @author Shou Matsumoto
 *
 */
public class TestPROWL2TextModeRunner extends TestCase {

	/**
	 * @param name
	 */
	public TestPROWL2TextModeRunner(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.kb.extension.ontology.protege.OWL2KnowledgeBase#saveFindings(unbbayes.prs.mebn.MultiEntityBayesianNetwork, java.io.File)}
	 * to save KB in a file other than the one containing the MTheory.
	 * @throws Exception 
	 */
	public final void testSaveFindingsSeparateOWLFile() throws Exception {
		
		Debug.setDebug(true);
		Debug.println("Enabled debug mode");
		PROWL2TextModeRunner textModeRunner = new PROWL2TextModeRunner();
		
		// load owl (content in ubf is not needed when not using GUI)
		MebnIO mebnIO = Protege41CompatiblePROWL2IO.newInstance();
		File owlFile = new File(getClass().getResource("/VehicleIdentificationTBox/VehicleIdentificationTBox.owl").toURI());
		
		// JUnit assertions. Ignore them if not using JUnit
		assertNotNull(mebnIO);
		assertNotNull(owlFile);
		assertTrue(owlFile.exists());
		
		// load TBox (MTheory without findings nor entity instances)
		MultiEntityBayesianNetwork mebn = mebnIO.loadMebn(owlFile);

		// copy blank ABox file (to be filled later with evidences and entity instances) to same directory of ubf file
		Files.copy(
				getClass().getResourceAsStream("/VehicleIdentificationTBox/bkp/VehicleIdentificationABoxBlank.owl"), // blank source to copy
				new File(owlFile.getParentFile(), "VehicleIdentificationABox.owl").toPath(), 	// destination directory and new file name
				StandardCopyOption.REPLACE_EXISTING);	// overwrite existing
		
		// get java.io.File instance of the ABox file I just copied
		File aboxFile = new File(owlFile.getParentFile(), "VehicleIdentificationABox.owl");
		
		// JUnit assertions. Ignore them if not using JUnit
		assertNotNull(aboxFile);
		assertTrue(aboxFile.exists());
		
		// initialize kb
		KnowledgeBase knowledgeBase = new PROWL2KnowledgeBaseBuilder().buildKB(mebn, null);
		knowledgeBase = textModeRunner.createKnowledgeBase(knowledgeBase, mebn);
		
		// JUnit assertions. Ignore them if not using JUnit
		assertTrue(knowledgeBase instanceof PROWL2KnowledgeBase);
		
		// associate kb with the blank abox file
		knowledgeBase.loadModule(aboxFile, true);
		
		// this is a tweak to make sure the mebn instance is associated with new OWL reasoner instance
		OWLReasoner reasoner = ((PROWL2KnowledgeBase)knowledgeBase).getDefaultOWLReasoner();
		OWLAPIStorageImplementorDecorator storageImpl = OWLAPIStorageImplementorDecorator.newInstance(reasoner.getRootOntology());
		storageImpl.setOWLReasoner(reasoner);
		mebn.setStorageImplementor(storageImpl);
		
		// (optional) copy findings from blank KB to mebn instance
		knowledgeBase = textModeRunner.fillFindings(mebn,knowledgeBase);
		
		// create new instance of entity "Object"
		ObjectEntity entity = mebn.getObjectEntityContainer().getObjectEntityByName("Object");
		String instanceName = "O1";	// I'm calling it O1 (this is o-one, not zero-one). Instance names must not start with numbers.
		ObjectEntityInstance instance = entity.addInstance(instanceName);
		mebn.getObjectEntityContainer().addEntityInstance(instance);
		mebn.getNamesUsed().add(instanceName); 	// (optional) keep track of names that are already present in mebn
		
		
		// create a finding ObjectType(O1) == Tracked
		ResidentNode residentNode = mebn.getDomainResidentNode("ObjectType");
		CategoricalStateEntity categoricalState = mebn.getCategoricalStatesEntityContainer().getCategoricalState("Tracked");
		ObjectEntityInstance[] arguments = {instance};
		RandomVariableFinding finding = new RandomVariableFinding(
				residentNode, 
				arguments , 
				categoricalState, 
				mebn);
		residentNode.addRandomVariableFinding(finding); 
		
		// save the KB (it's associated with a separate file already).
		// this should save O1 and ObjectType(O1) == Tracked in VehicleIdentificationABox.owl
		textModeRunner.saveKnowledgeBase(aboxFile, knowledgeBase, mebn);
		
		
		
		// Query ObjectType(O1) just for the purpose of testing a query (to make sure that the finding is considered)
		String[] queryParam = {"O1"};
		ProbabilisticNetwork net = textModeRunner.callLaskeyAlgorithm(
					mebn, 
					knowledgeBase, 
					"ObjectType", 
					queryParam
				);
		
		// JUnit assertions. Ignore them if not using JUnit
		assertNotNull(net);
		assertEquals(1, net.getNodeCount());
		assertNotNull(net.getNode("ObjectType__O1"));
		assertEquals("ObjectType__O1", net.getNodeAt(0).getName());
		int indexTrackedState = 0;
		for (indexTrackedState = 0; indexTrackedState < net.getNodeAt(0).getStatesSize(); indexTrackedState++) {
			if (net.getNodeAt(0).getStateAt(indexTrackedState).equalsIgnoreCase("Tracked")) {
				break;
			}
		}
		assertTrue(indexTrackedState < net.getNodeAt(0).getStatesSize());
		assertEquals(1f, ((TreeVariable)net.getNodeAt(0)).getMarginalAt(indexTrackedState));
		
		// print probabilities
		for (Node node : net.getNodes()) {
			System.out.print(node + " : ");
			for (int state = 0; state < node.getStatesSize(); state++) {
				System.out.print(" " + node.getStateAt(state));
				if (node instanceof TreeVariable) {
					System.out.print(" " + ((TreeVariable)node).getMarginalAt(state) + ", ");
				}
			}
			System.out.println();
		}
		
	}

}
