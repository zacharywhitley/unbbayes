package unbbayes.prs.mebn.ssbn;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import unbbayes.io.mebn.UbfIO;
import unbbayes.prs.INode;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.exception.MEBNException;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.kb.powerloom.PowerLoomKB;
import unbbayes.prs.mebn.ontology.HeparIITestSet;
import unbbayes.prs.mebn.ssbn.exception.ImplementationRestrictionException;
import unbbayes.prs.mebn.ssbn.exception.MFragContextFailException;
import unbbayes.prs.mebn.ssbn.exception.OVInstanceFaultException;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;
import unbbayes.prs.mebn.ssbn.laskeyalgorithm.LaskeyAlgorithmParameters;
import unbbayes.prs.mebn.ssbn.laskeyalgorithm.LaskeySSBNGenerator;

/**
 * Tests of the BuilderStructureImpl. 
 *  
 *  Note: is very hard automatize the analize of the results of the tests of 
 *  generate the SSBN. Because this, in many of this test methods, the analisy
 *  of the results should be do for the user.  
 *  
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 */
public class BuilderStructureImplTest extends TestCase{

	private ResourceBundle resourceFiles = 
		ResourceBundle.getBundle("unbbayes.prs.mebn.resources.ResourceFiles");
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testBuildStructure() {
	}

	public void terstBuildWithHepparSet(){
		
		HeparIITestSet hepparTestSet; 
		
		System.out.println("\n\n---------------------------------------------------\n");
		System.out.println("                 BUILD WITH HEPPAR SET                 ");
		System.out.println("-------------------------------------------------------");
		
		LaskeyAlgorithmParameters parameters = new LaskeyAlgorithmParameters(); 
		parameters.setParameterValue(LaskeyAlgorithmParameters.DO_INITIALIZATION, "true");
		parameters.setParameterValue(LaskeyAlgorithmParameters.DO_BUILDER, "true"); 
		parameters.setParameterValue(LaskeyAlgorithmParameters.DO_PRUNE, "false"); 
		parameters.setParameterValue(LaskeyAlgorithmParameters.DO_CPT_GENERATION, "false"); 
		
		hepparTestSet = new HeparIITestSet(new LaskeySSBNGenerator(parameters)); 
		
		/* 
		 * Case 1: Expected
		 * AST((p,maria)) 
		 * ToxicHepatitis((p,maria))
		 * HistoryAlcAbuse((p,maria))
		 * HepatoxicMeds((p,maria))
		 */
		SSBN ssbn = hepparTestSet.executeTestCase1(); 
		System.out.println("HeparTestSet TestCase 01");
		for(SimpleSSBNNode s: ssbn.getSsbnNodeList()){
			System.out.println(s);
		}
		System.out.println("Size=" + ssbn.getSsbnNodeList().size());
		assertEquals(ssbn.getSsbnNodeList().size(), 4); 
		
		System.out.println("\n---------------------------------------------------\n");
		
		/* 
		 * Case 18: Expected
		 * ToxicHepatitis((p,maria))
		 * AST((p,maria))=a700_400 [F] 
		 * HistoryAlcAbuse((p,maria))
		 * HepatoxicMeds((p,maria))
		 */
		ssbn = hepparTestSet.executeTestCase18(); 
		System.out.println("HeparTestSet TestCase 18");
		for(SimpleSSBNNode s: ssbn.getSsbnNodeList()){
			System.out.println(s);
		}
		System.out.println("Size=" + ssbn.getSsbnNodeList().size());
		assertEquals(ssbn.getSsbnNodeList().size(), 4); 
		
		System.out.println("\n---------------------------------------------------\n");
		
		/*
		 * Test 24: Expected
		 * ToxicHepatitis((p,maria))
		 * Fatigue((p,maria))=true [F] 
		 * AST((p,maria))=a700_400 [F] 
		 * HistoryAlcAbuse((p,maria))
		 * HepatoxicMeds((p,maria))
		 */
		ssbn = hepparTestSet.executeTestCase24(); 
		System.out.println("HeparTestSet TestCase 24");
		for(SimpleSSBNNode s: ssbn.getSsbnNodeList()){
			System.out.println(s);
		}
		System.out.println("Size=" + ssbn.getSsbnNodeList().size());
		assertEquals(ssbn.getSsbnNodeList().size(), 5); 
		
		System.out.println("\n---------------------------------------------------\n");
		
		/*
		 * Test 28: Expected (11 nodes)
		 * 
		 * AST
		 * Fatigue[F]
		 * EnlargedSpleen[F]
		 * ALT[F]
		 * INR[F]
		 * 
		 * ToxicHepatitis((p,maria))
		 * HistoryAlcAbuse((p,maria))
		 * HepatoxicMeds((p,maria))
		 * 
		 * FunctionalHiperbilirium
		 * Sex
		 * Age
		 * 
		 */
		ssbn = hepparTestSet.executeTestCase28(); 
		System.out.println("HeparTestSet TestCase 28");
		for(SimpleSSBNNode s: ssbn.getSsbnNodeList()){
			System.out.println(s);
		}
		System.out.println("Size=" + ssbn.getSsbnNodeList().size());
		assertEquals(ssbn.getSsbnNodeList().size(), 11); 
		 
		/*
		 * Test 63: Expected (7 nodes)
		 * 
		 * INR
		 * HistoryAlcAbuse((p,maria)) [F]
		 * HepatoxicMeds((p,maria)) [F]
		 * Sex [F]
		 * 
		 * ToxicHepatits
		 * FunctionalHiperbilirium
		 * Age
		 * 
		 */
		ssbn = hepparTestSet.executeTestCase63(); 
		System.out.println("HeparTestSet TestCase 63");
		for(SimpleSSBNNode s: ssbn.getSsbnNodeList()){
			System.out.println(s);
		}
		System.out.println("Size=" + ssbn.getSsbnNodeList().size());
		assertEquals(ssbn.getSsbnNodeList().size(), 11); 
		
	}

	public void testBuildWithStarTrekSet(){

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
		parameters.setParameterValue(LaskeyAlgorithmParameters.DO_BUILDER, "true"); 
		parameters.setParameterValue(LaskeyAlgorithmParameters.DO_PRUNE, "false"); 
		parameters.setParameterValue(LaskeyAlgorithmParameters.DO_CPT_GENERATION, "false"); 
		
		LaskeySSBNGenerator algorithm = new LaskeySSBNGenerator(parameters); 
		

		
//		assertEquals(ssbn.getSsbnNodeList().size(), 11); 
		
		/*
		 * Test 2: 
		 * HarmPotential(ST4, T3)
		 * Result: 
		 * this is the network of the monograph
		 */
		starTrekCase2(ssbn, mebn, kb, algorithm);
		
	}


	/*
	 * Query: HarmPotential(ST0, T0)
	 * KnowledgeBase: StarTrekKB_Situation1
	 * Result: fail because the context node not IsOwnStarship(ST0) fail. 
	 */
	private SSBN starTrekCase1(SSBN ssbn, MultiEntityBayesianNetwork mebn,
			KnowledgeBase kb, LaskeySSBNGenerator algorithm) {
		
		MFrag mFrag = mebn.getMFragByName("Starship_MFrag"); 
		
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
		
		ssbn = executeAlgorithm(ssbn, kb, algorithm, queryList); 
		
		printSSBNNodeList(ssbn);
		return ssbn;
	}


	
	/*
	 * Query: HarmPotential(ST4, T3)
	 * KnowledgeBase: StarTrekKB_Situation1
	 * Result: this is the network of the monograph
	 */
	private void starTrekCase2(SSBN ssbn, MultiEntityBayesianNetwork mebn,
			KnowledgeBase kb, LaskeySSBNGenerator algorithm) {
		
		MFrag mFrag;
		ResidentNode resident;
		List<OVInstance> ovInstanceList;
		OrdinaryVariable ov;
		OVInstance ovInstance;
		Query query;
		List<Query> queryList;
		
		System.out.println("\n\n\n\nTest 2: HarmPotential(ST4, T3)");
		
		mFrag = mebn.getMFragByName("Starship_MFrag"); 
		
		resident = mFrag.getDomainResidentNodeByName("HarmPotential");
		
		ovInstanceList = new ArrayList<OVInstance>(); 
		ov = mFrag.getOrdinaryVariableByName("st"); 
		ovInstance = OVInstance.getInstance(ov, 
				LiteralEntityInstance.getInstance("ST4", ov.getValueType())); 
		ovInstanceList.add(ovInstance); 
		ov = mFrag.getOrdinaryVariableByName("t"); 
		ovInstance = OVInstance.getInstance(ov, 
				LiteralEntityInstance.getInstance("T3", ov.getValueType())); 
		ovInstanceList.add(ovInstance); 
		
		query = new Query(resident, ovInstanceList); 
		
		queryList = new ArrayList<Query>();
		queryList.add(query); 
		
		ssbn = executeAlgorithm(ssbn, kb, algorithm, queryList); 
		
		printSSBNNodeList(ssbn); 
	}

	private SSBN executeAlgorithm(SSBN ssbn, KnowledgeBase kb,
			LaskeySSBNGenerator algorithm, List<Query> queryList) {
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
		return ssbn;
	}

	private void printSSBNNodeList(SSBN ssbn) {
		System.out.println("\n\n\n Result");
		for(SimpleSSBNNode s: ssbn.getSsbnNodeList()){
			System.out.println(s);
			for(INode sChild: s.getChildNodes()){
				System.out.println("   " + sChild);
			}
		}
		System.out.println("Size=" + ssbn.getSsbnNodeList().size());
		System.out.println("---------------------------------------------------\n\n");
	}
	
//	@Test
	public void terstEvaluateMFragContextNodes(){
		
		MFragInstance mFragInstance = null; 
		SSBN ssbn = new SSBN(); 
		MultiEntityBayesianNetwork mebn; 
		KnowledgeBase kb; 
		
		mebn = loadStarTrekOntologyExample(); 
		
		kb = PowerLoomKB.getNewInstanceKB(); 
		kb.createGenerativeKnowledgeBase(mebn); 
		
		loadFindingsSet(mebn, kb, resourceFiles.getString("StarTrekKB_Situation1")); 
		
		BuilderStructureImpl builderStructure = BuilderStructureImpl.newInstance(kb); 
		
		/* 
		 * Test 1
		 * MFrag = Starship_MFrag
		 * st = ST1
		 */
		
		System.out.println("Test 1");
		MFrag mFrag = mebn.getMFragByName("Starship_MFrag"); 
		mFragInstance = MFragInstance.getInstance(mFrag); 
		
		OrdinaryVariable st = mFrag.getOrdinaryVariableByName("st"); 
		
		try {
			mFragInstance.addOVValue(st, "ST1");
		} catch (MFragContextFailException e1) {
			e1.printStackTrace();
			fail("Error addin ST1 to the MFragInstance."); 
		} 
		
		try {
			builderStructure.evaluateMFragContextNodes(mFragInstance);
		} catch (ImplementationRestrictionException e) {
			e.printStackTrace();
			fail(); 
		} catch (SSBNNodeGeneralException e) {
			e.printStackTrace();
			fail(); 
		} catch (OVInstanceFaultException e) {
			e.printStackTrace();
			fail(); 
		} 
		
		for(ContextNode context: mFragInstance.getContextNodeList()){
			assertEquals(ContextNodeEvaluationState.EVALUATION_OK, 
					mFragInstance.getStateEvaluationOfContextNode(context)); 
		}
		
		System.out.println("End the evaluation");
		System.out.println("-------------------------------------------------\n\n");
		
		
		/* 
		 * Test 2
		 * MFrag = Starship_MFrag
		 * st = ST5
		 * -> Should evaluate false the mFrag: don't have a finding for StarshipZone(ST5)
		 */
		
		System.out.println("Test 2");
		mFrag = mebn.getMFragByName("Starship_MFrag"); 
		mFragInstance = MFragInstance.getInstance(mFrag); 
		
		st = mFrag.getOrdinaryVariableByName("st"); 
		
		try {
			mFragInstance.addOVValue(st, "ST5");
		} catch (MFragContextFailException e1) {
			e1.printStackTrace();
			fail("Error addin ST1 to the MFragInstance."); 
		} 
		
		try {
			builderStructure.evaluateMFragContextNodes(mFragInstance);
		} catch (ImplementationRestrictionException e) {
			e.printStackTrace();
			fail(); 
		} catch (SSBNNodeGeneralException e) {
			e.printStackTrace();
			fail(); 
		} catch (OVInstanceFaultException e) {
			e.printStackTrace();
			fail(); 
		} 
		
		assertEquals(true, mFragInstance.isUseDefaultDistribution()); 
		
		System.out.println("End the evaluation");
		System.out.println("-------------------------------------------------\n\n");
		
		/* 
		 * Test 3
		 * MFrag = Starship_MFrag
		 * st = ST0
		 * -> Should evaluate false the mFrag: don't have a finding for StarshipZone(ST5)
		 */
		
		System.out.println("Test 3");
		mFrag = mebn.getMFragByName("Starship_MFrag"); 
		mFragInstance = MFragInstance.getInstance(mFrag); 
		
		st = mFrag.getOrdinaryVariableByName("st"); 
		
		try {
			mFragInstance.addOVValue(st, "ST0");
		} catch (MFragContextFailException e1) {
			e1.printStackTrace();
			fail("Error addin ST1 to the MFragInstance."); 
		} 
		
		try {
			builderStructure.evaluateMFragContextNodes(mFragInstance);
		} catch (ImplementationRestrictionException e) {
			e.printStackTrace();
			fail(); 
		} catch (SSBNNodeGeneralException e) {
			e.printStackTrace();
			fail(); 
		} catch (OVInstanceFaultException e) {
			e.printStackTrace();
			fail(); 
		} 
		
		assertEquals(true, mFragInstance.isUseDefaultDistribution()); 
		
		System.out.println("End the evaluation");
		System.out.println("-------------------------------------------------\n\n");
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
