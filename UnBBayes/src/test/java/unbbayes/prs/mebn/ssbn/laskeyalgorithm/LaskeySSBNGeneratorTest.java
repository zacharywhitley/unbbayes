package unbbayes.prs.mebn.ssbn.laskeyalgorithm;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import junit.framework.TestCase;
import unbbayes.io.ILogManager;
import unbbayes.io.TextLogManager;
import unbbayes.io.mebn.UbfIO;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.exception.MEBNException;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.kb.powerloom.PowerLoomKB;
import unbbayes.prs.mebn.ontology.HeparIITestSet;
import unbbayes.prs.mebn.ssbn.LiteralEntityInstance;
import unbbayes.prs.mebn.ssbn.MFragInstance;
import unbbayes.prs.mebn.ssbn.OVInstance;
import unbbayes.prs.mebn.ssbn.Query;
import unbbayes.prs.mebn.ssbn.SSBN;
import unbbayes.prs.mebn.ssbn.SimpleSSBNNode;
import unbbayes.prs.mebn.ssbn.exception.ImplementationRestrictionException;
import unbbayes.prs.mebn.ssbn.exception.OVInstanceFaultException;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;

/**
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 */
public class LaskeySSBNGeneratorTest  extends TestCase {

	HeparIITestSet hepparTestSet; 

	private ResourceBundle resourceFiles = 
		ResourceBundle.getBundle("unbbayes.prs.mebn.resources.ResourceFiles");

	public LaskeySSBNGeneratorTest(String arg0) {
		super(arg0);

	}


	protected void setUp() throws Exception {

	}

	protected void tearDown() throws Exception {
	}

	public void _testInitializationWithHepparSet(){

		LaskeyAlgorithmParameters parameters = new LaskeyAlgorithmParameters(); 
		parameters.setParameterValue(LaskeyAlgorithmParameters.DO_INITIALIZATION, "true");
		parameters.setParameterValue(LaskeyAlgorithmParameters.DO_BUILDER, "false"); 
		parameters.setParameterValue(LaskeyAlgorithmParameters.DO_PRUNE, "false"); 
		parameters.setParameterValue(LaskeyAlgorithmParameters.DO_CPT_GENERATION, "false"); 

		hepparTestSet = new HeparIITestSet(new LaskeySSBNGenerator(parameters)); 

		SSBN ssbn = hepparTestSet.executeTestCase1(); 
		System.out.println("HeparTestSet TestCase 01");
		for(SimpleSSBNNode s: ssbn.getSimpleSsbnNodeList()){
			System.out.println(s);
		}
		System.out.println("Size=" + ssbn.getSimpleSsbnNodeList().size());

		System.out.println("....\n\n");

		assertEquals(ssbn.getSimpleSsbnNodeList().size(), 1);

		ssbn = hepparTestSet.executeTestCase18(); 
		System.out.println("HeparTestSet TestCase 18");
		for(SimpleSSBNNode s: ssbn.getSimpleSsbnNodeList()){
			System.out.println(s);
		}
		System.out.println("Size=" + ssbn.getSimpleSsbnNodeList().size());

		assertEquals(ssbn.getSimpleSsbnNodeList().size(), 2); 

		ssbn = hepparTestSet.executeTestCase24(); 
		System.out.println("HeparTestSet TestCase 24");
		for(SimpleSSBNNode s: ssbn.getSimpleSsbnNodeList()){
			System.out.println(s);
		}
		System.out.println("Size=" + ssbn.getSimpleSsbnNodeList().size());

		assertEquals(ssbn.getSimpleSsbnNodeList().size(), 3); 

	}

	public void _testInitializationWithStartrekSet(){

		MFragInstance mFragInstance = null; 
		SSBN ssbn = new SSBN(); 
		MultiEntityBayesianNetwork mebn; 
		KnowledgeBase kb; 

		mebn = loadStarTrekOntologyExample(); 

		kb = PowerLoomKB.getNewInstanceKB(); 
		kb.createGenerativeKnowledgeBase(mebn); 

		loadFindingsSet(mebn, kb, resourceFiles.getString("StarTrekKB_Situation1")); 

		LaskeyAlgorithmParameters parameters = new LaskeyAlgorithmParameters(); 
		parameters.setParameterValue(LaskeyAlgorithmParameters.DO_INITIALIZATION, "true");
		parameters.setParameterValue(LaskeyAlgorithmParameters.DO_BUILDER, "false"); 
		parameters.setParameterValue(LaskeyAlgorithmParameters.DO_PRUNE, "false"); 
		parameters.setParameterValue(LaskeyAlgorithmParameters.DO_CPT_GENERATION, "false"); 

		LaskeySSBNGenerator algorithm = new LaskeySSBNGenerator(parameters); 

		/*
		 * Test 1: 
		 * HarmPotential(ST0, T0)
		 * Result: 
		 * network with the queries and the findings
		 */
		MFrag mFrag = mebn.getMFragByName("Starship_MFrag"); 
		mFragInstance = MFragInstance.getInstance(mFrag); 

		ResidentNode resident = mFrag.getDomainResidentNodeByName("HarmPotential");

		List<OVInstance> ovInstanceList = new ArrayList<OVInstance>(); 
		OrdinaryVariable ov = mFrag.getOrdinaryVariableByName("st"); 
		OVInstance ovInstance = OVInstance.getInstance(ov, LiteralEntityInstance.getInstance("ST0", ov.getValueType())); 
		ovInstanceList.add(ovInstance); 
		ov = mFrag.getOrdinaryVariableByName("t"); 
		ovInstance = OVInstance.getInstance(ov, LiteralEntityInstance.getInstance("T0", ov.getValueType())); 
		ovInstanceList.add(ovInstance); 

		Query query = new Query(resident, ovInstanceList); 

		List<Query> queryList = new ArrayList<Query>();
		queryList.add(query); 

		try {
			ssbn = algorithm.generateSSBN(queryList, kb);
		} catch (SSBNNodeGeneralException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail(); 
		} catch (ImplementationRestrictionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail(); 
		} catch (MEBNException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail(); 
		} catch (OVInstanceFaultException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail(); 
		} catch (InvalidParentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail(); 
		} 

		for(SimpleSSBNNode s: ssbn.getSimpleSsbnNodeList()){
			System.out.println(s);
		}
		System.out.println("Size=" + ssbn.getSimpleSsbnNodeList().size());
		assertEquals(ssbn.getSimpleSsbnNodeList().size(), 37); 
		System.out.println("---------------------------------------------------\n\n");

	}
	
	public void testExecuteHepparTestSet(){
		LaskeyAlgorithmParameters parameters = new LaskeyAlgorithmParameters(); 
		parameters.setParameterValue(LaskeyAlgorithmParameters.DO_INITIALIZATION, "true");
		parameters.setParameterValue(LaskeyAlgorithmParameters.DO_BUILDER, "true"); 
		parameters.setParameterValue(LaskeyAlgorithmParameters.DO_PRUNE, "true"); 
		parameters.setParameterValue(LaskeyAlgorithmParameters.DO_CPT_GENERATION, "true"); 

		ILogManager logManager = new TextLogManager();
		hepparTestSet = new HeparIITestSet(new LaskeySSBNGenerator(parameters), logManager);
		
		hepparTestSet.executeTests(); 
		hepparTestSet.recordLog(); 
	}
	
	private MultiEntityBayesianNetwork loadStarTrekOntologyExample() {
		UbfIO io = UbfIO.getInstance(); 
		MultiEntityBayesianNetwork mebn = null; 

		try {
			mebn = io.loadMebn(new File(resourceFiles.getString("UnBBayesUBF")));
		} catch (Exception e) {
			e.printStackTrace();
			fail(); 
		}
		return mebn;
	}

	private void loadFindingsSet(MultiEntityBayesianNetwork mebn, KnowledgeBase kb, String nameOfFile) {
		try {
			kb.loadModule(new File(nameOfFile), true);

			for (ResidentNode resident : mebn.getDomainResidentNodes()) {
				try {
					kb.fillFindings(resident);
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			fail(); 
		}
	}


}
