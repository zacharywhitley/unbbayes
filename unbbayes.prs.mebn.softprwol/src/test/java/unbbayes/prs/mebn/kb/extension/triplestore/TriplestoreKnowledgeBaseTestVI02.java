package unbbayes.prs.mebn.kb.extension.triplestore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

public class TriplestoreKnowledgeBaseTestVI02 {

	static TriplestoreKnowledgeBase triplestoreKB; 
	static TriplestoreController kbController; 
	static MultiEntityBayesianNetwork mebn; 
	
	static final String REPOSITORY_URL = "http://localhost:8080/graphdb-workbench-free/"; 
	static final String REPOSITORY_NAME = "VI03"; 
	static final String ONTOLOGY_FILE = "VehicleIdentification5.ubf"; 
	
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
			System.out.println(TriplestoreKnowledgeBaseTestVI02.class.getResource(ONTOLOGY_FILE).toURI());
			graph = io.load(new File(TriplestoreKnowledgeBaseTestVI02.class.getResource(ONTOLOGY_FILE).toURI()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		assertNotNull(graph);
		assertTrue(graph instanceof MultiEntityBayesianNetwork);
		
		mebn = (MultiEntityBayesianNetwork) graph;
		
	}
	
	public static void turnOnKnowledgeBase(){
		System.out.println("Knwoledge Base Turn On");
		
		triplestoreKB = (TriplestoreKnowledgeBase)TriplestoreKnowledgeBase.getInstance(mebn); 
				
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
		System.out.println("Test 001");
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
	public void testSearchFindingBooleanWithOneArgument(){
		//ReportedObject(Rpt2) = Obj2
		System.out.println("Test 002");
		ResidentNode residentNode = mebn.getDomainResidentNode("FastVehicle"); 
		OrdinaryVariable ov = residentNode.getOrdinaryVariableByIndex(0); 
		
		OVInstance ovInstance = OVInstance.getInstance(
				ov, 
				LiteralEntityInstance.getInstance(OBJ_1, ov.getValueType()));
		
		List<OVInstance> argumentList = new ArrayList<OVInstance>(); 
		argumentList.add(ovInstance); 
		
		StateLink stateLink = triplestoreKB.searchFinding(residentNode, argumentList); 
		
		assertNotNull(stateLink);
		assertEquals("true", stateLink.getState().getName());
	}
	
	
	@Test
	public void testSearchFindingBooleanWithOneArgument2(){
		//ReportedObject(Rpt2) = Obj2
		System.out.println("Test 003");
		ResidentNode residentNode = mebn.getDomainResidentNode("FastVehicle"); 
		OrdinaryVariable ov = residentNode.getOrdinaryVariableByIndex(0); 
		
		OVInstance ovInstance = OVInstance.getInstance(
				ov, 
				LiteralEntityInstance.getInstance(OBJ_2, ov.getValueType()));
		
		List<OVInstance> argumentList = new ArrayList<OVInstance>(); 
		argumentList.add(ovInstance); 
		
		StateLink stateLink = triplestoreKB.searchFinding(residentNode, argumentList); 
		
		assertNotNull(stateLink);
		assertEquals("false", stateLink.getState().getName());
	}
	
	@Test
	public void testSearchFindingBooleanWithOneArgument3(){
		//ReportedObject(Rpt2) = Obj2
		System.out.println("Test 015");
		ResidentNode residentNode = mebn.getDomainResidentNode("FastVehicle"); 
		OrdinaryVariable ov = residentNode.getOrdinaryVariableByIndex(0); 
		
		OVInstance ovInstance = OVInstance.getInstance(
				ov, 
				LiteralEntityInstance.getInstance(OBJ_3, ov.getValueType()));
		
		List<OVInstance> argumentList = new ArrayList<OVInstance>(); 
		argumentList.add(ovInstance); 
		
		StateLink stateLink = triplestoreKB.searchFinding(residentNode, argumentList); 
		
		assertNotNull(stateLink);
		assertEquals("false", stateLink.getState().getName());
	}
	
	@Test
	public void testSearchFindingBooleanWithTwoArgument1(){
		//ReportedObject(Rpt2) = Obj2
		System.out.println("Test 004");
		ResidentNode residentNode = mebn.getDomainResidentNode("FastVehicleTime"); 
		
		List<OVInstance> argumentList = new ArrayList<OVInstance>(); 
		
		OrdinaryVariable ov = residentNode.getOrdinaryVariableByIndex(1); 
		
		OVInstance ovInstance = OVInstance.getInstance(
				ov, 
				LiteralEntityInstance.getInstance(OBJ_1, ov.getValueType()));

		argumentList.add(ovInstance); 
		
		ov = residentNode.getOrdinaryVariableByIndex(0); 
		
		ovInstance = OVInstance.getInstance(
				ov, 
				LiteralEntityInstance.getInstance(T_0, ov.getValueType()));

		argumentList.add(ovInstance); 
		
		StateLink stateLink = triplestoreKB.searchFinding(residentNode, argumentList); 
		
		assertNotNull(stateLink);
		assertEquals("true", stateLink.getState().getName());
	}	
	
	@Test
	public void testContextSearchBooleanWithOneArgument(){
		
		//FastVehicle(obj)
		//obj = ??? 

		//Return: OBJ_1
		
		System.out.println("Test 005");
		ContextNode contextNode = mebn.getContextNode("CX9"); 
		
		List<OVInstance> argumentList = new ArrayList<OVInstance>(); 

		SearchResult searchResult = triplestoreKB.evaluateSearchContextNodeFormula(contextNode, argumentList); 
		
		assertNotNull(searchResult);
		assertEquals(1, searchResult.getValuesResultList().size());
		
		String[] resultLine = searchResult.getValuesResultList().get(0); 
		assertEquals(1, resultLine.length);
		
		assertEquals(OBJ_1, resultLine[0]);
		
	}
	
	@Test
	public void testContextSearchBooleanWithtTwoArgument(){
		
		//FastVehicleTime(obj, t)
		//obj = ???  t = ???

		//Return: <OBJ_1 , T_0> 
		//Return: <OBJ_2, T_1> 
		
		System.out.println("Test 006");
		ContextNode contextNode = mebn.getContextNode("CX10"); 
		
		List<OVInstance> argumentList = new ArrayList<OVInstance>(); 

		SearchResult searchResult = triplestoreKB.evaluateSearchContextNodeFormula(contextNode, argumentList); 
		
		assertNotNull(searchResult);
		assertEquals(2, searchResult.getValuesResultList().size());
		
		String[] resultLine = searchResult.getValuesResultList().get(0); 
		assertEquals(2, resultLine.length);
		
		assertEquals(OBJ_1, resultLine[1]);
		assertEquals(T_0, resultLine[0]);
		
		resultLine = searchResult.getValuesResultList().get(1); 
		assertEquals(2, resultLine.length);
		
		assertEquals(OBJ_2, resultLine[1]);
		assertEquals(T_1, resultLine[0]);
		
	}
	
	@Test
	public void testContextSearchBooleanWithtTwoArgument02(){
		
		//FastVehicleTime(obj, t)
		//obj = OBJ_1 

		//Return: <T_0>  
		
		System.out.println("Test 007");
		ContextNode contextNode = mebn.getContextNode("CX10"); 
		
		List<OVInstance> argumentList = new ArrayList<OVInstance>(); 
		
		OrdinaryVariable ovObj = null; 
		for(OrdinaryVariable ov: contextNode.getOrdinaryVariablesInArgument()){
			if(ov.getName().equals("obj")){
				ovObj = ov; 
			}
		}
		
		OVInstance ovInstance = OVInstance.getInstance(
				ovObj, 
				LiteralEntityInstance.getInstance(OBJ_1, ovObj.getValueType()));

		argumentList.add(ovInstance); 
		
		SearchResult searchResult = triplestoreKB.evaluateSearchContextNodeFormula(contextNode, argumentList); 
		
		assertNotNull(searchResult);
		assertEquals(1, searchResult.getValuesResultList().size());
		
		String[] resultLine = searchResult.getValuesResultList().get(0); 
		assertEquals(1, resultLine.length);
		
		assertEquals(T_0, resultLine[0]);
		
	}
	
	@Test
	public void testContextSearchBooleanWithtTwoArgument03(){
		
		//FastVehicleTime(obj, t)
		//obj = OBJ_1 

		//Return: <T_0>  
		
		System.out.println("Test 009");
		ContextNode contextNode = mebn.getContextNode("CX10"); 
		
		List<OVInstance> argumentList = new ArrayList<OVInstance>(); 
		
		OrdinaryVariable ovObj = null; 
		for(OrdinaryVariable ov: contextNode.getOrdinaryVariablesInArgument()){
			if(ov.getName().equals("t")){
				ovObj = ov; 
			}
		}
		
		OVInstance ovInstance = OVInstance.getInstance(
				ovObj, 
				LiteralEntityInstance.getInstance(T_0, ovObj.getValueType()));

		argumentList.add(ovInstance); 
		
		SearchResult searchResult = triplestoreKB.evaluateSearchContextNodeFormula(contextNode, argumentList); 
		
		assertNotNull(searchResult);
		assertEquals(1, searchResult.getValuesResultList().size());
		
		String[] resultLine = searchResult.getValuesResultList().get(0); 
		assertEquals(1, resultLine.length);
		
		assertEquals(OBJ_1, resultLine[0]);
		
	}	
	
	@Test
	public void testEvaluateBooleanWithOneArgument(){
		
		//FastVehicle(obj)
		//obj = OBJ_1 
		//return = true; 
		
		System.out.println("Test 010");
		
		ContextNode contextNode = mebn.getContextNode("CX9"); 
		
		OrdinaryVariable ovObj  = null; 
		
		for(OrdinaryVariable ov: contextNode.getOrdinaryVariablesInArgument()){
			if(ov.getName().equals("obj")){
				ovObj = ov; 
			}
		}
		
		assertNotNull(ovObj);
		
		OVInstance ovInstance = OVInstance.getInstance(
				ovObj, 
				LiteralEntityInstance.getInstance(OBJ_1, ovObj.getValueType()));
		
		List<OVInstance> argumentList = new ArrayList<OVInstance>(); 
		argumentList.add(ovInstance); 
		
		Boolean result = triplestoreKB.evaluateContextNodeFormula(contextNode, argumentList); 
		
		assertNotNull(result);
		assertEquals("true", result.toString());
	}
	
	@Test
	public void testEvaluateBooleanWithOneArgument02(){
		
		//FastVehicle(obj)
		//obj = OBJ_2 
		//Return: false   //in base
		
		System.out.println("Test 011");
		
		ContextNode contextNode = mebn.getContextNode("CX9"); 
		
		OrdinaryVariable ovObj  = null; 
		
		for(OrdinaryVariable ov: contextNode.getOrdinaryVariablesInArgument()){
			if(ov.getName().equals("obj")){
				ovObj = ov; 
			}
		}
		
		assertNotNull(ovObj);
		
		OVInstance ovInstance = OVInstance.getInstance(
				ovObj, 
				LiteralEntityInstance.getInstance(OBJ_2, ovObj.getValueType()));
		
		List<OVInstance> argumentList = new ArrayList<OVInstance>(); 
		argumentList.add(ovInstance); 
		
		Boolean result = triplestoreKB.evaluateContextNodeFormula(contextNode, argumentList); 
		
		assertNotNull(result);
		assertEquals("false", result.toString());
	}
	
	
	@Test
	public void testEvaluateBooleanWithOneArgument03(){
		
		//FastVehicle(obj)
		//obj = OBJ_3 
		//Return: false (unknown) 
		
		System.out.println("Test 012");
		
		ContextNode contextNode = mebn.getContextNode("CX9"); 
		
		OrdinaryVariable ovObj  = null; 
		
		for(OrdinaryVariable ov: contextNode.getOrdinaryVariablesInArgument()){
			if(ov.getName().equals("obj")){
				ovObj = ov; 
			}
		}
		
		assertNotNull(ovObj);
		
		OVInstance ovInstance = OVInstance.getInstance(
				ovObj, 
				LiteralEntityInstance.getInstance(OBJ_3, ovObj.getValueType()));
		
		List<OVInstance> argumentList = new ArrayList<OVInstance>(); 
		argumentList.add(ovInstance); 
		
		Boolean result = triplestoreKB.evaluateContextNodeFormula(contextNode, argumentList); 
		
		assertNotNull(result);
		assertEquals("false", result.toString());
	}
	
	@Test
	public void testEvaluateBooleanWithTwoArgument01(){
		
		//FastVehicleTime(obj, t)
		//obj = OBJ_1  t = T0 
		//Return: true 
		
		System.out.println("Test 013");
		
		ContextNode contextNode = mebn.getContextNode("CX10"); 
		
		OrdinaryVariable ovObj  = null; 
		OrdinaryVariable ovT = null; 
		
		for(OrdinaryVariable ov: contextNode.getOrdinaryVariablesInArgument()){
			if(ov.getName().equals("obj")){
				ovObj = ov; 
			}
			if(ov.getName().equals("t")){
				ovT = ov; 
			}
		}
		
		assertNotNull(ovObj);
		assertNotNull(ovT);
		
		List<OVInstance> argumentList = new ArrayList<OVInstance>(); 
		
		OVInstance ovInstance = OVInstance.getInstance(
				ovT, 
				LiteralEntityInstance.getInstance(T_0, ovT.getValueType()));		
		argumentList.add(ovInstance); 
		
		ovInstance = OVInstance.getInstance(
				ovObj, 
				LiteralEntityInstance.getInstance(OBJ_1, ovObj.getValueType()));		
		argumentList.add(ovInstance); 
		
		Boolean result = triplestoreKB.evaluateContextNodeFormula(contextNode, argumentList); 
		
		assertNotNull(result);
		assertEquals("true", result.toString());
	}
	
	@Test
	public void testEvaluateBooleanWithTwoArgument02(){
		
		//FastVehicleTime(obj, t)
		//obj = OBJ_1  t = T1 
		//Return: false
		
		System.out.println("Test 014");
		
		ContextNode contextNode = mebn.getContextNode("CX10"); 
		
		OrdinaryVariable ovObj  = null; 
		OrdinaryVariable ovT = null; 
		
		for(OrdinaryVariable ov: contextNode.getOrdinaryVariablesInArgument()){
			if(ov.getName().equals("obj")){
				ovObj = ov; 
			}
			if(ov.getName().equals("t")){
				ovT = ov; 
			}
		}
		
		assertNotNull(ovObj);
		assertNotNull(ovT);
		
		List<OVInstance> argumentList = new ArrayList<OVInstance>(); 
		
		OVInstance ovInstance = OVInstance.getInstance(
				ovObj, 
				LiteralEntityInstance.getInstance(OBJ_1, ovObj.getValueType()));		
		argumentList.add(ovInstance); 
		
		ovInstance = OVInstance.getInstance(
				ovT, 
				LiteralEntityInstance.getInstance(T_1, ovObj.getValueType()));		
		argumentList.add(ovInstance); 
		
		Boolean result = triplestoreKB.evaluateContextNodeFormula(contextNode, argumentList); 
		
		assertNotNull(result);
		assertEquals("false", result.toString());
	}
	
	@Test
	public void testEvaluateAndBooleanWithTwoArgument01(){
		
		//FastVehicleTime(obj, t)^FastVehicle(obj)
		//obj = OBJ_1  t = T0 
		//Return: false
		
		System.out.println("Test 016");
		
		ContextNode contextNode = mebn.getContextNode("CX11"); 
		
		OrdinaryVariable ovObj  = null; 
		OrdinaryVariable ovT = null; 
		
		for(OrdinaryVariable ov: contextNode.getOrdinaryVariablesInArgument()){
			if(ov.getName().equals("obj")){
				ovObj = ov; 
			}
			if(ov.getName().equals("t")){
				ovT = ov; 
			}
		}
		
		assertNotNull(ovObj);
		assertNotNull(ovT);
		
		List<OVInstance> argumentList = new ArrayList<OVInstance>(); 
		
		OVInstance ovInstance = OVInstance.getInstance(
				ovObj, 
				LiteralEntityInstance.getInstance(OBJ_1, ovObj.getValueType()));		
		argumentList.add(ovInstance); 
		
		ovInstance = OVInstance.getInstance(
				ovT, 
				LiteralEntityInstance.getInstance(T_0, ovObj.getValueType()));		
		argumentList.add(ovInstance); 
		
		Boolean result = triplestoreKB.evaluateContextNodeFormula(contextNode, argumentList); 
		
		assertNotNull(result);
		assertEquals("true", result.toString());
	}
	
	@Test
	public void testEvaluateAndBooleanWithTwoArgument02(){
		
		//FastVehicleTime(obj, t)^FastVehicle(obj)
		//obj = OBJ_1  t = T1 
		//Return: false
		
		System.out.println("Test 017");
		
		ContextNode contextNode = mebn.getContextNode("CX11"); 
		
		OrdinaryVariable ovObj  = null; 
		OrdinaryVariable ovT = null; 
		
		for(OrdinaryVariable ov: contextNode.getOrdinaryVariablesInArgument()){
			if(ov.getName().equals("obj")){
				ovObj = ov; 
			}
			if(ov.getName().equals("t")){
				ovT = ov; 
			}
		}
		
		assertNotNull(ovObj);
		assertNotNull(ovT);
		
		List<OVInstance> argumentList = new ArrayList<OVInstance>(); 
		
		OVInstance ovInstance = OVInstance.getInstance(
				ovObj, 
				LiteralEntityInstance.getInstance(OBJ_1, ovObj.getValueType()));		
		argumentList.add(ovInstance); 
		
		ovInstance = OVInstance.getInstance(
				ovT, 
				LiteralEntityInstance.getInstance(T_1, ovObj.getValueType()));		
		argumentList.add(ovInstance); 
		
		Boolean result = triplestoreKB.evaluateContextNodeFormula(contextNode, argumentList); 
		
		assertNotNull(result);
		assertEquals("false", result.toString());
	}	
	
	@Test
	public void testEvaluateOrBooleanWithTwoArgument01(){
		
		//FastVehicleTime(obj, t)^FastVehicle(obj)
		//obj = OBJ_1  t = T0 
		//Return: false
		
		System.out.println("Test 018");
		
		ContextNode contextNode = mebn.getContextNode("CX12"); 
		
		OrdinaryVariable ovObj  = null; 
		OrdinaryVariable ovT = null; 
		
		for(OrdinaryVariable ov: contextNode.getOrdinaryVariablesInArgument()){
			if(ov.getName().equals("obj")){
				ovObj = ov; 
			}
			if(ov.getName().equals("t")){
				ovT = ov; 
			}
		}
		
		assertNotNull(ovObj);
		assertNotNull(ovT);
		
		List<OVInstance> argumentList = new ArrayList<OVInstance>(); 
		
		OVInstance ovInstance = OVInstance.getInstance(
				ovObj, 
				LiteralEntityInstance.getInstance(OBJ_1, ovObj.getValueType()));		
		argumentList.add(ovInstance); 
		
		ovInstance = OVInstance.getInstance(
				ovT, 
				LiteralEntityInstance.getInstance(T_0, ovObj.getValueType()));		
		argumentList.add(ovInstance); 
		
		Boolean result = triplestoreKB.evaluateContextNodeFormula(contextNode, argumentList); 
		
		assertNotNull(result);
		assertEquals("true", result.toString());
	}
	
	@Test
	public void testEvaluateOrBooleanWithTwoArgument02(){
		
		//FastVehicleTime(obj, t)^FastVehicle(obj)
		//obj = OBJ_1  t = T1 
		//Return: false
		
		System.out.println("Test 019");
		
		ContextNode contextNode = mebn.getContextNode("CX12"); 
		
		OrdinaryVariable ovObj  = null; 
		OrdinaryVariable ovT = null; 
		
		for(OrdinaryVariable ov: contextNode.getOrdinaryVariablesInArgument()){
			if(ov.getName().equals("obj")){
				ovObj = ov; 
			}
			if(ov.getName().equals("t")){
				ovT = ov; 
			}
		}
		
		assertNotNull(ovObj);
		assertNotNull(ovT);
		
		List<OVInstance> argumentList = new ArrayList<OVInstance>(); 
		
		OVInstance ovInstance = OVInstance.getInstance(
				ovObj, 
				LiteralEntityInstance.getInstance(OBJ_1, ovObj.getValueType()));		
		argumentList.add(ovInstance); 
		
		ovInstance = OVInstance.getInstance(
				ovT, 
				LiteralEntityInstance.getInstance(T_1, ovObj.getValueType()));		
		argumentList.add(ovInstance); 
		
		Boolean result = triplestoreKB.evaluateContextNodeFormula(contextNode, argumentList); 
		
		assertNotNull(result);
		assertEquals("true", result.toString());
	}		
	//TODO test boolean node with only one argument x rel t/f
	
	
	
	//TODO test boolean node with two arguments x rel y 
	
	//TODO test boolean node with more than two arguments x rel y, y rel z
	
}
