package unbbayes.prs.mebn.kb.extension.triplestore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import unbbayes.io.mebn.PROWLModelUser;
import unbbayes.io.mebn.UbfIO2;
import unbbayes.prs.Graph;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.entity.StateLink;
import unbbayes.prs.mebn.kb.SearchResult;
import unbbayes.prs.mebn.ssbn.LiteralEntityInstance;
import unbbayes.prs.mebn.ssbn.OVInstance;
import unbbayes.triplestore.SAILTriplestoreParameters;
import unbbayes.triplestore.Triplestore;
import unbbayes.triplestore.TriplestoreController;
import unbbayes.triplestore.exception.TriplestoreException;
import unbbayes.util.Debug;
import unbbayes.util.Parameters;

public class TriplestoreKnowledgeBaseTestVI01 {

	static TriplestoreKnowledgeBase triplestoreKB; 
	static TriplestoreController kbController; 
	static MultiEntityBayesianNetwork mebn; 
	
	static final String REPOSITORY_URL = "http://localhost:8080/graphdb-workbench-free/"; 
	static final String REPOSITORY_NAME = "VI03"; 
	static final String ONTOLOGY_FILE = "VehicleIdentification2.ubf"; 
	
	static final String RGN_1 = "http://www.pr-owl.org/examples/pr-owl2/VehicleIdentification/VehicleIdentification2.owl#Rgn1"; 
	static final String RGN_2 = "http://www.pr-owl.org/examples/pr-owl2/VehicleIdentification/VehicleIdentification2.owl#Rgn2"; 
	
	static final String OBJ_1 = "http://www.pr-owl.org/examples/pr-owl2/VehicleIdentification/VehicleIdentification2.owl#Obj1"; 
	static final String OBJ_2 = "http://www.pr-owl.org/examples/pr-owl2/VehicleIdentification/VehicleIdentification2.owl#Obj2"; 
	static final String OBJ_3 = "http://www.pr-owl.org/examples/pr-owl2/VehicleIdentification/VehicleIdentification2.owl#Obj3"; 
	
	static final String T_0 = "http://www.pr-owl.org/examples/pr-owl2/VehicleIdentification/VehicleIdentification2.owl#T0"; 
	static final String T_1 = "http://www.pr-owl.org/examples/pr-owl2/VehicleIdentification/VehicleIdentification2.owl#T1"; 
	static final String T_2 = "http://www.pr-owl.org/examples/pr-owl2/VehicleIdentification/VehicleIdentification2.owl#T2"; 
	
	static final String RPT_1 = "http://www.pr-owl.org/examples/pr-owl2/VehicleIdentification/VehicleIdentification2.owl#Rpt1"; 
	static final String RPT_2 = "http://www.pr-owl.org/examples/pr-owl2/VehicleIdentification/VehicleIdentification2.owl#Rpt2"; 
	static final String RPT_3 = "http://www.pr-owl.org/examples/pr-owl2/VehicleIdentification/VehicleIdentification2.owl#Rpt3"; 
	
	static final String ROAD = "http://www.pr-owl.org/examples/pr-owl2/VehicleIdentification/VehicleIdentification2.owl#Road"; 
	static final String VERYROUGH = "http://www.pr-owl.org/examples/pr-owl2/VehicleIdentification/VehicleIdentification2.owl#VeryRough"; 
	
	@BeforeClass
	public static void prepareEnvironment(){
		testLoadFile(); 
		turnOnKnowledgeBase(); 
	}
	
	/**
	 * Test method for {@link UbfIO2#load(java.io.File)}
	 */
	public static void testLoadFile() {
		Debug.setDebug(true);
		
		UbfIO2 io = UbfIO2.getInstance();
		assertNotNull(io);
		
		
		Graph graph;
		try {
			System.out.println(TriplestoreKnowledgeBaseTestVI01.class.getResource(ONTOLOGY_FILE).toURI());
			graph = io.load(new File(TriplestoreKnowledgeBaseTestVI01.class.getResource(ONTOLOGY_FILE).toURI()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		assertNotNull(graph);
		assertTrue(graph instanceof MultiEntityBayesianNetwork);
		
		mebn = (MultiEntityBayesianNetwork) graph;
		
	}
	
	public static void turnOnKnowledgeBase(){
		System.out.println("Knwoledge Base Turn On");
		
		triplestoreKB = (TriplestoreKnowledgeBase)TriplestoreKnowledgeBase.getInstance(mebn, new EmptyMediator()); 
				
		kbController = triplestoreKB.getTriplestoreController(); 
		
    	Parameters params = new SAILTriplestoreParameters();
    	
		params.setParameterValue(Triplestore.PARAM_URL, REPOSITORY_URL);
		params.setParameterValue(Triplestore.PARAM_REPOSITORY, REPOSITORY_NAME);
		
		try {
			kbController.startConnection(params);
		} catch (TriplestoreException e) {
			e.printStackTrace();
			assertNotNull(null);
		}
	}
	
	@Test
	public void testIsConnected(){
		System.out.println("Test 001");
		assertEquals("Connection to Triplestore Fail", kbController.isConnected(), true); 
	}
	
	@Test
	public void testMEBNLoaded(){
		System.out.println("Test 002");
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
	
	@Test
	public void testSearchFindingCategorical(){
		//TerrainType(Rgn1) = Road 
		System.out.println("Test 003");
		ResidentNode residentNode = mebn.getDomainResidentNode("TerrainType"); 
		OrdinaryVariable ov = residentNode.getOrdinaryVariableByIndex(0); 
		
		OVInstance ovInstance = OVInstance.getInstance(
				ov, 
				LiteralEntityInstance.getInstance(RGN_1, ov.getValueType()));
		
		List<OVInstance> argumentList = new ArrayList<OVInstance>(); 
		argumentList.add(ovInstance); 
		
		StateLink stateLink = triplestoreKB.searchFinding(residentNode, argumentList); 
		
		assertNotNull(stateLink);
		assertEquals(stateLink.getState().getName(), "VeryRough");
	
	}
	
	@Test
	public void testSearchFindingEntity(){
		//ReportedObject(Rpt2) = Obj2
		System.out.println("Test 004");
		ResidentNode residentNode = mebn.getDomainResidentNode("ReportedObject"); 
		OrdinaryVariable ov = residentNode.getOrdinaryVariableByIndex(0); 
		
		OVInstance ovInstance = OVInstance.getInstance(
				ov, 
				LiteralEntityInstance.getInstance(RPT_2, ov.getValueType()));
		
		List<OVInstance> argumentList = new ArrayList<OVInstance>(); 
		argumentList.add(ovInstance); 
		
		StateLink stateLink = triplestoreKB.searchFinding(residentNode, argumentList); 
		
		assertNotNull(stateLink);
		assertEquals(OBJ_2, stateLink.getState().getName());
	}
	
	@Test
	public void testSearchFindingEntity2(){
		//ReportedObject(Rpt3) = NULL
		System.out.println("Test 005");
		ResidentNode residentNode = mebn.getDomainResidentNode("ReportedObject"); 
		OrdinaryVariable ov = residentNode.getOrdinaryVariableByIndex(0); 
		
		OVInstance ovInstance = OVInstance.getInstance(
				ov, 
				LiteralEntityInstance.getInstance(RPT_3, ov.getValueType()));
		
		List<OVInstance> argumentList = new ArrayList<OVInstance>(); 
		argumentList.add(ovInstance); 
		
		StateLink stateLink = triplestoreKB.searchFinding(residentNode, argumentList); 
		
		System.out.println("StateLink = " + stateLink);
		assertNull(stateLink);
	}
	
	@Test
	public void testSearchFindingDoubleArguments(){
		//MTI(Rpt1, T0) = Slow
		System.out.println("Test 006");
		ResidentNode residentNode = mebn.getDomainResidentNode("MTI"); 
		
		List<OVInstance> argumentList = new ArrayList<OVInstance>(); 
		
		OrdinaryVariable ov = residentNode.getOrdinaryVariableByIndex(0); 
		OVInstance ovInstance = OVInstance.getInstance(ov, 
				LiteralEntityInstance.getInstance(RPT_1, ov.getValueType()));
		argumentList.add(ovInstance); 
		
		ov = residentNode.getOrdinaryVariableByIndex(1); 
		ovInstance = OVInstance.getInstance(ov, 
				LiteralEntityInstance.getInstance(T_0, ov.getValueType()));
		argumentList.add(ovInstance); 
		
		StateLink stateLink = triplestoreKB.searchFinding(residentNode, argumentList); 
		
		assertNotNull(stateLink);
		assertEquals(stateLink.getState().getName(), "Slow");
	}
	
	@Test
	public void testSearchFindingDoubleArguments02(){
		//MTI(Rpt1, T2) = null
		System.out.println("Test 007");
		ResidentNode residentNode = mebn.getDomainResidentNode("MTI"); 
		
		List<OVInstance> argumentList = new ArrayList<OVInstance>(); 
		
		OrdinaryVariable ov = residentNode.getOrdinaryVariableByIndex(0); 
		OVInstance ovInstance = OVInstance.getInstance(ov, 
				LiteralEntityInstance.getInstance(RPT_1, ov.getValueType()));
		argumentList.add(ovInstance); 
		
		ov = residentNode.getOrdinaryVariableByIndex(1); 
		ovInstance = OVInstance.getInstance(ov, 
				LiteralEntityInstance.getInstance(T_2, ov.getValueType()));
		argumentList.add(ovInstance); 
		
		StateLink stateLink = triplestoreKB.searchFinding(residentNode, argumentList); 
		
		assertNull(stateLink);
	}
	
	@Test
	public void testContextNodeCX6(){
		
		//CX6: rgn = Location(obj) 
		//     rgn = RGN1, obj = OBJ1 
		//     expected = true
		System.out.println("Test 008");
		ContextNode contextNode = mebn.getContextNode("CX6"); 
		Set<OrdinaryVariable> ovList = contextNode.getVariableList(); 
		
		List<OVInstance> argumentList = new ArrayList<OVInstance>(); 
		
		OrdinaryVariable rgnOV = null; 
		OrdinaryVariable objOV = null;
		
		for(OrdinaryVariable ov: ovList)	{
			if(ov.getName().equals("rgn")){
				rgnOV = ov; 
			}
			if(ov.getName().equals("obj")){
				objOV = ov; 
			}
		};
		
		assertNotNull(rgnOV);
		assertNotNull(objOV); 
		
		OVInstance ovInstance = OVInstance.getInstance(objOV, 
				LiteralEntityInstance.getInstance(OBJ_1, objOV.getValueType()));
		argumentList.add(ovInstance); 
		
		ovInstance = OVInstance.getInstance(rgnOV, 
				LiteralEntityInstance.getInstance(RGN_1, rgnOV.getValueType()));
		argumentList.add(ovInstance); 
		
		Boolean result = triplestoreKB.evaluateContextNodeFormula(contextNode, argumentList); 
		assertNotNull(result); 
		assertEquals(true, result.booleanValue());
		
	}
	
	@Test
	public void testContextNodeCX6_02(){
		
		//CX6: rgn = Location(obj) 
		//     rgn = RGN1, obj = OBJ2 
		//     expected = false
		System.out.println("Test 009");
		ContextNode contextNode = mebn.getContextNode("CX6"); 
		Set<OrdinaryVariable> ovList = contextNode.getVariableList(); 
		
		List<OVInstance> argumentList = new ArrayList<OVInstance>(); 
		
		OrdinaryVariable rgnOV = null; 
		OrdinaryVariable objOV = null;
		
		for(OrdinaryVariable ov: ovList)	{
			if(ov.getName().equals("rgn")){
				rgnOV = ov; 
			}
			if(ov.getName().equals("obj")){
				objOV = ov; 
			}
		};
		
		assertNotNull(rgnOV);
		assertNotNull(objOV); 
		
		OVInstance ovInstance = OVInstance.getInstance(objOV, 
				LiteralEntityInstance.getInstance(OBJ_2, objOV.getValueType()));
		argumentList.add(ovInstance); 
		
		ovInstance = OVInstance.getInstance(rgnOV, 
				LiteralEntityInstance.getInstance(RGN_1, rgnOV.getValueType()));
		argumentList.add(ovInstance); 
		
		Boolean result = triplestoreKB.evaluateContextNodeFormula(contextNode, argumentList); 
		assertNotNull(result); 
		assertEquals(false, result.booleanValue());
		
	}
	
	@Test
	public void testContextNodeCX8(){
		
		//CX6: t != tprev 
		//     t = T0, tprev = T1 
		//     expected = true
		
		System.out.println("Test 010");
		ContextNode contextNode = mebn.getContextNode("CX8"); 
		Set<OrdinaryVariable> ovList = contextNode.getVariableList(); 
		
		List<OVInstance> argumentList = new ArrayList<OVInstance>(); 
		
		OrdinaryVariable tOV = null; 
		OrdinaryVariable tprevOV = null;
		
		for(OrdinaryVariable ov: ovList)	{
			if(ov.getName().equals("t")){
				tOV = ov; 
			}
			if(ov.getName().equals("tPrev")){
				tprevOV = ov; 
			}
		};
		
		assertNotNull(tOV);
		assertNotNull(tprevOV); 
		
		OVInstance ovInstance = OVInstance.getInstance(tprevOV, 
				LiteralEntityInstance.getInstance(T_0, tprevOV.getValueType()));
		argumentList.add(ovInstance); 
		
		ovInstance = OVInstance.getInstance(tOV, 
				LiteralEntityInstance.getInstance(T_1, tOV.getValueType()));
		argumentList.add(ovInstance); 
		
		Boolean result = triplestoreKB.evaluateContextNodeFormula(contextNode, argumentList); 
		assertNotNull(result); 
		assertEquals(true, result.booleanValue());
		
	}
	
	@Test
	public void testContextNodeCX8_02(){
		
		//CX6: t != tprev 
		//     t = T0, tprev = T0 
		//     expected = true
		
		System.out.println("Test 011");
		
		ContextNode contextNode = mebn.getContextNode("CX8"); 
		Set<OrdinaryVariable> ovList = contextNode.getVariableList(); 
		
		List<OVInstance> argumentList = new ArrayList<OVInstance>(); 
		
		OrdinaryVariable tOV = null; 
		OrdinaryVariable tprevOV = null;
		
		for(OrdinaryVariable ov: ovList)	{
			if(ov.getName().equals("t")){
				tOV = ov; 
			}
			if(ov.getName().equals("tPrev")){
				tprevOV = ov; 
			}
		};
		
		assertNotNull(tOV);
		assertNotNull(tprevOV); 
		
		OVInstance ovInstance = OVInstance.getInstance(tprevOV, 
				LiteralEntityInstance.getInstance(T_0, tprevOV.getValueType()));
		argumentList.add(ovInstance); 
		
		ovInstance = OVInstance.getInstance(tOV, 
				LiteralEntityInstance.getInstance(T_0, tOV.getValueType()));
		argumentList.add(ovInstance); 
		
		Boolean result = triplestoreKB.evaluateContextNodeFormula(contextNode, argumentList); 
		assertNotNull(result); 
		assertEquals(false, result.booleanValue());
	}	
	
	@Test
	public void testSearchContextNodeCX6(){
		
		System.out.println("Test 012");
		
		//CX6: rgn = Location(obj) 
		//     obj = OBJ1 
		//     expected rgn = RGN1
		
		ContextNode contextNode = mebn.getContextNode("CX6"); 
		Set<OrdinaryVariable> ovList = contextNode.getVariableList(); 
		
		List<OVInstance> argumentList = new ArrayList<OVInstance>(); 
		
		OrdinaryVariable rgnOV = null; 
		OrdinaryVariable objOV = null;
		
		for(OrdinaryVariable ov: ovList)	{
			if(ov.getName().equals("rgn")){
				rgnOV = ov; 
			}
			if(ov.getName().equals("obj")){
				objOV = ov; 
			}
		};
		
		assertNotNull(rgnOV);
		assertNotNull(objOV); 
		
		OVInstance ovInstance = OVInstance.getInstance(objOV, 
				LiteralEntityInstance.getInstance(OBJ_1, objOV.getValueType()));
		argumentList.add(ovInstance); 
		
//		ovInstance = OVInstance.getInstance(rgnOV, 
//				LiteralEntityInstance.getInstance(RGN_1, rgnOV.getValueType()));
//		argumentList.add(ovInstance); 
		
		SearchResult result = triplestoreKB.evaluateSearchContextNodeFormula(contextNode, argumentList); 
		assertNotNull(result); 
		
		List<String[]> resultList = result.getValuesResultList();
		
		assertEquals(1, resultList.size());
		
		String[] line = resultList.get(0); 
		assertNotNull(line); 
		
		assertEquals(1, line.length);
		
		assertEquals(RGN_1, line[0]);
		
	}
	
	@Test
	public void testSearchContextNodeCX6_2(){
		
		System.out.println("Test 013");
		
		//CX6: rgn = Location(obj) 
		//     rgn = ?
		//     location = ? 
		//     expected <Obj1, Rgn1>, <Obj2, Rgn2> 
		
		ContextNode contextNode = mebn.getContextNode("CX6"); 
		Set<OrdinaryVariable> ovList = contextNode.getVariableList(); 
		
		List<OVInstance> argumentList = new ArrayList<OVInstance>(); 
		
		OrdinaryVariable rgnOV = null; 
		OrdinaryVariable objOV = null;
		
		for(OrdinaryVariable ov: ovList)	{
			if(ov.getName().equals("rgn")){
				rgnOV = ov; 
			}
			if(ov.getName().equals("obj")){
				objOV = ov; 
			}
		};
		
		assertNotNull(rgnOV);
		assertNotNull(objOV); 
		
		SearchResult result = triplestoreKB.evaluateSearchContextNodeFormula(contextNode, argumentList); 
		
		assertNotNull(result); 
		
		List<String[]> resultList = result.getValuesResultList();
		
		assertEquals(2, resultList.size());
		
		String[] line = resultList.get(0); 
		assertNotNull(line); 
		
		assertEquals(2, line.length);
		
		assertEquals(RGN_1, line[0]);
		assertEquals(OBJ_1, line[1]);
		
		line = resultList.get(1); 
		assertNotNull(line); 
		
		assertEquals(2, line.length);
		
		assertEquals(RGN_2, line[0]);
		assertEquals(OBJ_2, line[1]);		
		
	}	
	
	
	@Test
	public void testSearchContextNodeCX8_01(){
		
		//CX6: t != tprev 
		//     t = T0, tprev = ?
		//     expected = T1
		
		System.out.println("Test 014");
		
		ContextNode contextNode = mebn.getContextNode("CX8"); 
		Set<OrdinaryVariable> ovList = contextNode.getVariableList(); 
		
		List<OVInstance> ovInstanceList = new ArrayList<OVInstance>(); 
		
		OrdinaryVariable tOV = null; 
		OrdinaryVariable tprevOV = null;
		
		for(OrdinaryVariable ov: ovList)	{
			if(ov.getName().equals("t")){
				tOV = ov; 
			}
			if(ov.getName().equals("tPrev")){
				tprevOV = ov; 
			}
		};
		
		assertNotNull(tOV);
		assertNotNull(tprevOV); 
		
		OVInstance ovInstance = OVInstance.getInstance(tprevOV, 
				LiteralEntityInstance.getInstance(T_0, tprevOV.getValueType()));
		ovInstanceList.add(ovInstance); 
		
		SearchResult result = triplestoreKB.evaluateSearchContextNodeFormula(contextNode, ovInstanceList); 
		assertNotNull(result); 
		
		List<String[]> resultList = result.getValuesResultList();
		
		assertEquals(1, resultList.size());
		
		String[] line = resultList.get(0); 
		assertNotNull(line); 
		
		assertEquals(1, line.length);
		
		assertEquals(T_1, line[0]);		
	}	
	
	@Test
	public void testSearchContextNodeCX8_02(){
		
		//CX6: t != tprev 
		//     t = ?, tprev = ?
		//     <T0, T1> <T1, T0> 
		
		System.out.println("Test 015");
		
		ContextNode contextNode = mebn.getContextNode("CX8"); 
		Set<OrdinaryVariable> ovList = contextNode.getVariableList(); 
		
		List<OVInstance> ovInstanceList = new ArrayList<OVInstance>(); 
		
		OrdinaryVariable tOV = null; 
		OrdinaryVariable tprevOV = null;
		
		for(OrdinaryVariable ov: ovList)	{
			if(ov.getName().equals("t")){
				tOV = ov; 
			}
			if(ov.getName().equals("tPrev")){
				tprevOV = ov; 
			}
		};
		
		assertNotNull(tOV);
		assertNotNull(tprevOV); 
		
		SearchResult result = triplestoreKB.evaluateSearchContextNodeFormula(contextNode, ovInstanceList); 
		assertNotNull(result); 
		
		List<String[]> resultList = result.getValuesResultList();
		
		assertEquals(2, resultList.size());
		
		String[] line = resultList.get(0); 
		assertNotNull(line); 
		
		assertEquals(2, line.length);
		
		if(line[0].equals(T_0)){
			assertEquals(T_0, line[0]);	
			assertEquals(T_1, line[1]);
		}else{
			assertEquals(T_1, line[0]);	
			assertEquals(T_0, line[1]);
		}
		
		line = resultList.get(1); 
		assertNotNull(line); 
		
		assertEquals(2, line.length);
		if(line[0].equals(T_0)){
			assertEquals(T_0, line[0]);	
			assertEquals(T_1, line[1]);
		}else{
			assertEquals(T_1, line[0]);	
			assertEquals(T_0, line[1]);
		}
	}		
	
	//TODO test boolean node with only one argument x rel t/f
	
	//TODO test boolean node with two arguments x rel y 
	
	//TODO test boolean node with more than two arguments x rel y, y rel z
	
}
