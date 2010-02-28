package unbbayes.prs.mebn.context;

import java.io.File;
import java.util.Set;

import junit.framework.TestCase;
import unbbayes.io.mebn.PrOwlIO;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.OrdinaryVariable;

public class NodeFormulaTreeTest extends TestCase {
	
	MultiEntityBayesianNetwork mebn = null; 
	
	public NodeFormulaTreeTest(String arg0) {
		super(arg0);
	}
	
	
	
	protected void setUp() throws Exception {
			
		super.setUp();

		PrOwlIO io = new PrOwlIO(); 
		mebn = io.loadMebn(new File("src/test/resources/mebn/StarTrek.owl"));
		
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

//	public static void main(String[] args){
//		NodeFormulaTreeTest test = new NodeFormulaTreeTest("Teste"); 
//		try {
//			test.setUp();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} 
//		test.testGetVariableList(); 
//	}

	/*
	 * Test method for 'unbbayes.prs.mebn.context.NodeFormulaTree.getVariableList()'
	 */
	public void testGetVariableList() {

		System.out.println(" ");
		
		MFrag mFrag = mebn.getMFragByName("DangerToOthers_MFrag"); 
		ContextNode node = mFrag.getContextNodeByName("CX1");
		Set<OrdinaryVariable> list = node.getVariableList();
		
		System.out.println("result CX1:" + node); 
		for(OrdinaryVariable ov: list){
			System.out.println("OV: " + ov.getName()); 
		}
		
		mFrag = mebn.getMFragByName("DangerToOthers_MFrag"); 
		node = mFrag.getContextNodeByName("CX4");
		list = node.getVariableList();
		
		System.out.println("result CX4:" + node); 
		for(OrdinaryVariable ov: list){
			System.out.println("OV: " + ov.getName()); 
		}

		mFrag = mebn.getMFragByName("DangerToSelf_MFrag"); 
		node = mFrag.getContextNodeByName("CX8");
		list = node.getVariableList();
		
		System.out.println("result CX8:" + node); 
		for(OrdinaryVariable ov: list){
			System.out.println("OV: " + ov.getName()); 
		}		
		
		mFrag = mebn.getMFragByName("DangerToSelf_MFrag"); 
		node = mFrag.getContextNodeByName("CX7");
		list = node.getVariableList();
		
		System.out.println("result CX7:" + node); 
		for(OrdinaryVariable ov: list){
			System.out.println("OV: " + ov.getName()); 
		}				
	}

}
