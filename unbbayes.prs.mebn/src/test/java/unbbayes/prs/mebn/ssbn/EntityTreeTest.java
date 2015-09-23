package unbbayes.prs.mebn.ssbn;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.entity.Type;
import unbbayes.prs.mebn.entity.TypeContainer;
import unbbayes.prs.mebn.ssbn.exception.MFragContextFailException;

public class EntityTreeTest extends TestCase{

	Type testType = null; 
	
	@Before
	public void setUp() throws Exception {
	
		TypeContainer typeContainer = new TypeContainer(); 
		testType = typeContainer.createType("TEST_TYPE"); 
		
	}

	@After
	public void tearDown() throws Exception {
		
	}

//	@Test
	public void tesdtUpdateTreeForNewInformationOrdinaryVariableString() {
		
		EntityTree entityTree; 
		OrdinaryVariable ov; 
		OrdinaryVariable ovArray[]; 
		String entityArray[]; 
		List<String[]> entityArrayList; 
		
		/*
		 * Test 1: 
		 * Initial tree: empty
		 * Operation: add a simple ov = OV1 = Entity1; 
		 * Expected: 
		 *       [OV1=ENT01]
		 */
		
		entityTree = new EntityTree(); 
		ov = new OrdinaryVariable("OV1", testType, null); 
		try {
			entityTree.updateTreeForNewInformation(ov, "ENT01");
		} catch (MFragContextFailException e) {
			fail("Error in the update process");
			e.printStackTrace(); 
		} 
		
		System.out.println("(Test1) EntityTree: ");
		entityTree.printTree(); 
		System.out.println("");
		
		
		/*
		 * Test 2: 
		 * Initial tree: tree with the node [[OV1=ENT01]]
		 * Operation: add a simple ov = OV2 = Entity2; 
		 * Expected: 
		 *       [OV1=ENT01]
		 *         [OV2=ENT02]
		 */
		
//		entityTree = new EntityTree(); //Use the entity result of the test 1 
		ov = new OrdinaryVariable("OV2", testType, null); 
		try {
			entityTree.updateTreeForNewInformation(ov, "ENT02");
		} catch (MFragContextFailException e) {
			fail("Error in the update process");
			e.printStackTrace(); 
		} 
		
		System.out.println("\n(Test2) EntityTree: ");
		entityTree.printTree(); 
		System.out.println("");
		
		/*
		 * Test 3: 
		 * Initial tree: empty
		 * Operation: 1) add a simple ov = OV1 = ENT01; 
		 *            2) add OV2 = ENT02, OV2 = ENT03
		 * Expected: 
		 *       [OV1=ENT01]
		 *         [OV2=ENT02]
		 *       [OV1=ENT01]  
		 *         [OV2=ENT03]
		 */
		entityTree = new EntityTree(); 
		
		ovArray = new OrdinaryVariable[2]; 
		ovArray[0] = new OrdinaryVariable("OV1", testType, null);
		ovArray[1] = new OrdinaryVariable("OV2", testType, null); 
		
		entityArrayList = new ArrayList<String[]>(); 
		
		entityArray = new String[2]; 
		entityArray[0] = new String("ENT01");
		entityArray[1] = new String("ENT02");
		entityArrayList.add(entityArray); 
		
		entityArray = new String[2]; 
		entityArray[0] = new String("ENT01");
		entityArray[1] = new String("ENT03");
		entityArrayList.add(entityArray); 
		
		try {
			entityTree.updateTreeForNewInformation(ovArray, entityArrayList);
		} catch (MFragContextFailException e) {
			fail("Error in the update process");
			e.printStackTrace(); 
		} 
		
		System.out.println("\n(Test3) EntityTree: ");
		entityTree.printTree(); 
		System.out.println("");
		
		
		/*
		 * Test 4: 
		 * 
		 * Initial tree:
		 *       [OV1=ENT01]
		 *         [OV2=ENT02]
		 *       [OV1=ENT01]  
		 *         [OV2=ENT03]
		 *         
		 * Operation: add the result [OV1=ENT01, OV2 = ENT03, OV3 = ENT04]
		 * 
		 * Expected: 
		 *       [OV1=ENT01]
		 *         [OV2=ENT03]
		 *           [OV3=ENT04]
		 */
		
		ovArray = new OrdinaryVariable[3]; 
		ovArray[0] = new OrdinaryVariable("OV1", testType, null);
		ovArray[1] = new OrdinaryVariable("OV2", testType, null); 
		ovArray[2] = new OrdinaryVariable("OV3", testType, null); 
		
		entityArrayList = new ArrayList<String[]>(); 
		
		entityArray = new String[3]; 
		entityArray[0] = new String("ENT01");
		entityArray[1] = new String("ENT03");		
		entityArray[2] = new String("ENT04");
		entityArrayList.add(entityArray); 
		
		try {
			entityTree.updateTreeForNewInformation(ovArray, entityArrayList);
		} catch (MFragContextFailException e) {
			e.printStackTrace(); 
			fail("Error in the update process");
		} 
		
		System.out.println("\n(Test4) EntityTree: ");
		entityTree.printTree(); 
		System.out.println("");
		
		/*
		 * Test 5: 
		 * 
		 * Initial tree:
		 *       [OV1=ENT01]
		 *         [OV2=ENT03]
		 *           [OV3=ENT04]
		 *         
		 * Operation: add the result [OV1=ENT01, OV2 = ENT05]
		 * 
		 * Expected: 
		 * 
		 */
		
		ovArray = new OrdinaryVariable[2]; 
		ovArray[0] = new OrdinaryVariable("OV1", testType, null);
		ovArray[1] = new OrdinaryVariable("OV2", testType, null); 
		
		entityArrayList = new ArrayList<String[]>(); 
		
		entityArray = new String[2]; 
		entityArray[0] = new String("ENT01");
		entityArray[1] = new String("ENT05");
		entityArrayList.add(entityArray); 
		
		boolean fail = false; 
		try {
			entityTree.updateTreeForNewInformation(ovArray, entityArrayList);
		} catch (MFragContextFailException e) {
			fail = true; 
			System.out.println("\n(Test5) EntityTree: ");
			entityTree.printTree(); 
			System.out.println("\nOK MFragContextFailException: inconsistent tree");
		} 
		if(!fail){
			fail(); 
		}
		
	}

	@Test
	public void testUpdateTreeForNewInformationOrdinaryVariableArrayListOfString() {
		
		EntityTree entityTree; 
		OrdinaryVariable ov; 
		OrdinaryVariable ovArray[]; 
		String entityArray[]; 
		List<String[]> entityArrayList; 
		
		/*
		 * Test 3: 
		 * Initial tree: empty
		 * Operation: 1) add a simple ov = OV1 = ENT01; 
		 *            2) add OV2 = ENT02, OV2 = ENT03
		 * Expected: 
		 *       [OV1=ENT01]
		 *         [OV2=ENT02]
		 *       [OV1=ENT01]  
		 *         [OV2=ENT03]
		 */
		
		entityTree = new EntityTree(); 
		
		ovArray = new OrdinaryVariable[2]; 
		ovArray[0] = new OrdinaryVariable("OV1", testType, null);
		ovArray[1] = new OrdinaryVariable("OV2", testType, null); 
		
		entityArrayList = new ArrayList<String[]>(); 
		
		entityArray = new String[2]; 
		entityArray[0] = new String("ENT01");
		entityArray[1] = new String("ENT02");
		entityArrayList.add(entityArray); 
		
		entityArray = new String[2]; 
		entityArray[0] = new String("ENT01");
		entityArray[1] = new String("ENT03");
		entityArrayList.add(entityArray); 
		
		try {
			entityTree.updateTreeForNewInformation(ovArray, entityArrayList);
		} catch (MFragContextFailException e) {
			fail("Error in the update process");
			e.printStackTrace(); 
		} 
		
		System.out.println("\n(Test 1) EntityTree: ");
		entityTree.printTree(); 
		System.out.println("");
		
		OrdinaryVariable[] knownOVArray = new OrdinaryVariable[1]; 
		knownOVArray[0] = ovArray[0]; //OV1
		
		ILiteralEntityInstance[] knownEntityArray = new ILiteralEntityInstance[1];
		knownEntityArray[0] = LiteralEntityInstance.getInstance("ENT01", ovArray[0].getValueType()); 
		
		OrdinaryVariable[] ovSearchArray = new OrdinaryVariable[1];
		ovSearchArray[0] = ovArray[1]; //OV2
		
		List<String[]> combinations = 
			entityTree.recoverCombinationsEntitiesPossibles(
					knownOVArray, knownEntityArray, ovSearchArray); 
		
		/* 
		 * Expected: (2 results)
		 * [ENT02]
		 * [ENT03]
		 */
		System.out.println("Combinations: ");
		for(String[] combination: combinations){
			System.out.print("Combination: ");
			for(String element: combination){
				System.out.println(element);
			}
		}
		
		assertEquals(2, combinations.size()); 
		
	}

	@Test
	public void testRecoverCombinationsEntitiesPossibles() {
		
	}

	@Test
	public void testGetIndexOfOv() {
	
	}

	@Test
	public void testDestroyPath() {
		
	}

	@Test
	public void testGetOvLevel() {
		
	}

	@Test
	public void testGetNodesOfLevel() {
		
	}

	@Test
	public void testGetOVInstances() {
		
	}

	@Test
	public void testGetTreeHowList() {
		
	}

	@Test
	public void testAddChildrenToList() {
		
	}

	@Test
	public void testAddLevel() {
		
	}

}
