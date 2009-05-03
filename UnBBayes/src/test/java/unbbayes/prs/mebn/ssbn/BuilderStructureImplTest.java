package unbbayes.prs.mebn.ssbn;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BuilderStructureImplTest extends TestCase{

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testBuildStructure() {
	}

	@Test
	public void testGetPossiblesCombinationsOfArgumentsForSSBNNode() {
		
//		List<LiteralEntityInstance> instanceListForOViVector[]; 
//		
//		BuilderStructureImpl builder = new BuilderStructureImpl(); 	
//		
//		LiteralEntityInstance instance;
//		List<LiteralEntityInstance> instanceListForOVi; 
//		
//		//Case 1: Node(x1, x2, x3)
//		//X1 = 1A
//		//X2 = 2A
//		//X3 = 3A
//		//Result expected = [1A, 2A, 3A]
//		
//		instanceListForOViVector = new ArrayList[3]; 
//		
//		instanceListForOVi = new ArrayList<LiteralEntityInstance>(); 
//		instanceListForOVi.add(LiteralEntityInstance.getInstance("1A", null)); 
//		instanceListForOViVector[0] = instanceListForOVi; 
//
//		instanceListForOVi = new ArrayList<LiteralEntityInstance>(); 
//		instanceListForOVi.add(LiteralEntityInstance.getInstance("2A", null)); 
//		instanceListForOViVector[1] = instanceListForOVi; 
//		
//		instanceListForOVi = new ArrayList<LiteralEntityInstance>(); 
//		instanceListForOVi.add(LiteralEntityInstance.getInstance("3A", null)); 
//		instanceListForOViVector[2] = instanceListForOVi; 		
//		
//		List<LiteralEntityInstance[]> result = builder.getPossiblesCombinationsOfArgumentsForSSBNNode(instanceListForOViVector); 
//		assertEquals(result.size(), 1); 
//		assertEquals(result.get(0).length, 3); 
//		
//		int nodeNumber = 0; 
//		System.out.println("Case 1: Result size = " + result.size());
//		for(LiteralEntityInstance[] vectorNodeI: result){
//			System.out.print("Node " + nodeNumber + ":");
//			for(LiteralEntityInstance lei: vectorNodeI){
//				System.out.print(lei + " ");
//			}
//			nodeNumber++; 
//			System.out.println("");
//		}
//		
//		//Case 2: Node(x1, x2, x3)
//		//X1 = 1A, 1B
//		//X2 = 2A
//		//X3 = 3A
//		//Result expected = [1A, 2A, 3A]
//		//                  [1B, 2A, 3A]		
//		
//		instanceListForOViVector = new ArrayList[3]; 
//		
//		instanceListForOVi = new ArrayList<LiteralEntityInstance>(); 
//		instanceListForOVi.add(LiteralEntityInstance.getInstance("1A", null)); 
//		instanceListForOVi.add(LiteralEntityInstance.getInstance("1B", null)); 
//		instanceListForOViVector[0] = instanceListForOVi; 
//
//		instanceListForOVi = new ArrayList<LiteralEntityInstance>(); 
//		instanceListForOVi.add(LiteralEntityInstance.getInstance("2A", null)); 
//		instanceListForOViVector[1] = instanceListForOVi; 
//		
//		instanceListForOVi = new ArrayList<LiteralEntityInstance>(); 
//		instanceListForOVi.add(LiteralEntityInstance.getInstance("3A", null)); 
//		instanceListForOViVector[2] = instanceListForOVi; 		
//		
//		result = builder.getPossiblesCombinationsOfArgumentsForSSBNNode(instanceListForOViVector); 
//		assertEquals(result.size(), 2); 
//		assertEquals(result.get(0).length, 3); 
//		assertEquals(result.get(1).length, 3); 
//		
//		nodeNumber = 0; 
//		System.out.println("Case 2: Result size = " + result.size());
//		for(LiteralEntityInstance[] vectorNodeI: result){
//			System.out.print("Node " + nodeNumber + ":");
//			for(LiteralEntityInstance lei: vectorNodeI){
//				System.out.print(lei + " ");
//			}
//			nodeNumber++; 
//			System.out.println("");
//		}		
//		
//		//Case 3: Node(x1, x2, x3)
//		//X1 = 1A, 1B, 2C
//		//X2 = 2A, 2B
//		//X3 = 3A, 3B
//		//Result expected = [1A, 2A, 3A]
//		//                  [1A, 2A, 3B]		
//		//                  [1A, 2B, 3A]
//		//                  [1A, 2B, 3B]
//		//                  [1A, 2C, 3A]		
//		//                  [1A, 2C, 3B]
//		//Result expected = [1B, 2A, 3A]
//		//                  [1B, 2A, 3B]		
//		//                  [1B, 2B, 3A]
//		//                  [1B, 2B, 3B]
//		//                  [1B, 2C, 3A]		
//		//                  [1B, 2C, 3B]
//		//Result expected = [1C, 2A, 3A]
//		//                  [1C, 2A, 3B]		
//		//                  [1C, 2B, 3A]
//		//                  [1C, 2B, 3B]
//		//                  [1C, 2C, 3A]		
//		//                  [1C, 2C, 3B]		
//		
//		instanceListForOViVector = new ArrayList[3]; 
//		
//		instanceListForOVi = new ArrayList<LiteralEntityInstance>(); 
//		instanceListForOVi.add(LiteralEntityInstance.getInstance("1A", null)); 
//		instanceListForOVi.add(LiteralEntityInstance.getInstance("1B", null)); 
//		instanceListForOVi.add(LiteralEntityInstance.getInstance("1C", null)); 
//		instanceListForOViVector[0] = instanceListForOVi; 
//
//		instanceListForOVi = new ArrayList<LiteralEntityInstance>(); 
//		instanceListForOVi.add(LiteralEntityInstance.getInstance("2A", null)); 
//		instanceListForOVi.add(LiteralEntityInstance.getInstance("2B", null)); 
//		instanceListForOViVector[1] = instanceListForOVi; 
//		
//		instanceListForOVi = new ArrayList<LiteralEntityInstance>(); 
//		instanceListForOVi.add(LiteralEntityInstance.getInstance("3A", null)); 
//		instanceListForOVi.add(LiteralEntityInstance.getInstance("3B", null)); 
//		instanceListForOViVector[2] = instanceListForOVi; 		
//		
//		result = builder.getPossiblesCombinationsOfArgumentsForSSBNNode(instanceListForOViVector); 
//		assertEquals(result.size(), 12); 
//		assertEquals(result.get(0).length, 3); 
//		assertEquals(result.get(1).length, 3); 
//		
//		nodeNumber = 0; 
//		System.out.println("Case 3: Result size = " + result.size());
//		for(LiteralEntityInstance[] vectorNodeI: result){
//			System.out.print("Node " + nodeNumber + ":");
//			for(LiteralEntityInstance lei: vectorNodeI){
//				System.out.print(lei + " ");
//			}
//			nodeNumber++; 
//			System.out.println("");
//		}				
		
	}

	@Test
	public void testCreateEdgesForMFragInstance() {
	}

}
