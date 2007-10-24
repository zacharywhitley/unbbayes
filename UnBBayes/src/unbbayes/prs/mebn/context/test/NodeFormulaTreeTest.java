package unbbayes.prs.mebn.context.test;

import java.io.File;
import java.util.Set;

import junit.framework.TestCase;
import unbbayes.io.mebn.PrOwlIO;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.DomainMFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.OrdinaryVariable;

public class NodeFormulaTreeTest extends TestCase {
	
	MultiEntityBayesianNetwork mebn = null; 
	
	public NodeFormulaTreeTest(String arg0) {
		super(arg0);
	}
	
	public static void main(String[] args){
		NodeFormulaTreeTest test = new NodeFormulaTreeTest("Teste"); 
		try {
			test.setUp();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		test.testGetVariableList(); 
	}
	
	protected void setUp() throws Exception {
			
		super.setUp();

		PrOwlIO io = new PrOwlIO(); 
		mebn = io.loadMebn(new File("examples/mebn/StarTrek30.owl"));
		
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/*
	 * Test method for 'unbbayes.prs.mebn.context.NodeFormulaTree.NodeFormulaTree(String, enumType, enumSubType, Object)'
	 */
	public void testNodeFormulaTree() {

	}

	/*
	 * Test method for 'unbbayes.prs.mebn.context.NodeFormulaTree.getVariableList()'
	 */
	public void testGetVariableList() {

		System.out.println(" ");
		
		DomainMFrag mFrag = mebn.getMFragByName("DangerToOthers_MFrag"); 
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
//
//	/*
//	 * Test method for 'unbbayes.prs.mebn.context.NodeFormulaTree.getExemplarList()'
//	 */
//	public void testGetExemplarList() {
//
//	}
//
//	/*
//	 * Test method for 'unbbayes.prs.mebn.context.NodeFormulaTree.addChild(NodeFormulaTree)'
//	 */
//	public void testAddChild() {
//
//	}
//
//	/*
//	 * Test method for 'unbbayes.prs.mebn.context.NodeFormulaTree.removeChild(NodeFormulaTree)'
//	 */
//	public void testRemoveChild() {
//
//	}
//
//	/*
//	 * Test method for 'unbbayes.prs.mebn.context.NodeFormulaTree.removeAllChildren()'
//	 */
//	public void testRemoveAllChildren() {
//
//	}
//
//	/*
//	 * Test method for 'unbbayes.prs.mebn.context.NodeFormulaTree.getChildren()'
//	 */
//	public void testGetChildren() {
//
//	}
//
//	/*
//	 * Test method for 'unbbayes.prs.mebn.context.NodeFormulaTree.toString()'
//	 */
//	public void testToString() {
//
//	}
//
//	/*
//	 * Test method for 'unbbayes.prs.mebn.context.NodeFormulaTree.getName()'
//	 */
//	public void testGetName() {
//
//	}
//
//	/*
//	 * Test method for 'unbbayes.prs.mebn.context.NodeFormulaTree.setName(String)'
//	 */
//	public void testSetName() {
//
//	}
//
//	/*
//	 * Test method for 'unbbayes.prs.mebn.context.NodeFormulaTree.getTypeNode()'
//	 */
//	public void testGetTypeNode() {
//
//	}
//
//	/*
//	 * Test method for 'unbbayes.prs.mebn.context.NodeFormulaTree.setTypeNode(enumType)'
//	 */
//	public void testSetTypeNode() {
//
//	}
//
//	/*
//	 * Test method for 'unbbayes.prs.mebn.context.NodeFormulaTree.getSubTypeNode()'
//	 */
//	public void testGetSubTypeNode() {
//
//	}
//
//	/*
//	 * Test method for 'unbbayes.prs.mebn.context.NodeFormulaTree.setSubTypeNode(enumSubType)'
//	 */
//	public void testSetSubTypeNode() {
//
//	}
//
//	/*
//	 * Test method for 'unbbayes.prs.mebn.context.NodeFormulaTree.getNodeVariable()'
//	 */
//	public void testGetNodeVariable() {
//
//	}
//
//	/*
//	 * Test method for 'unbbayes.prs.mebn.context.NodeFormulaTree.setNodeVariable(Object)'
//	 */
//	public void testSetNodeVariable() {
//
//	}
//
//	/*
//	 * Test method for 'unbbayes.prs.mebn.context.NodeFormulaTree.getMnemonic()'
//	 */
//	public void testGetMnemonic() {
//
//	}
//
//	/*
//	 * Test method for 'unbbayes.prs.mebn.context.NodeFormulaTree.setMnemonic(String)'
//	 */
//	public void testSetMnemonic() {
//
//	}
//
//	/*
//	 * Test method for 'unbbayes.prs.mebn.context.NodeFormulaTree.getFormulaViewText()'
//	 */
//	public void testGetFormulaViewText() {
//
//	}
//
//	/*
//	 * Test method for 'java.lang.Object.Object()'
//	 */
//	public void testObject() {
//
//	}
//
//	/*
//	 * Test method for 'java.lang.Object.getClass()'
//	 */
//	public void testGetClass() {
//
//	}
//
//	/*
//	 * Test method for 'java.lang.Object.hashCode()'
//	 */
//	public void testHashCode() {
//
//	}
//
//	/*
//	 * Test method for 'java.lang.Object.equals(Object)'
//	 */
//	public void testEquals() {
//
//	}
//
//	/*
//	 * Test method for 'java.lang.Object.clone()'
//	 */
//	public void testClone() {
//
//	}
//
//	/*
//	 * Test method for 'java.lang.Object.toString()'
//	 */
//	public void testToString1() {
//
//	}
//
//	/*
//	 * Test method for 'java.lang.Object.notify()'
//	 */
//	public void testNotify() {
//
//	}
//
//	/*
//	 * Test method for 'java.lang.Object.notifyAll()'
//	 */
//	public void testNotifyAll() {
//
//	}
//
//	/*
//	 * Test method for 'java.lang.Object.wait(long)'
//	 */
//	public void testWaitLong() {
//
//	}
//
//	/*
//	 * Test method for 'java.lang.Object.wait(long, int)'
//	 */
//	public void testWaitLongInt() {
//
//	}
//
//	/*
//	 * Test method for 'java.lang.Object.wait()'
//	 */
//	public void testWait() {
//
//	}
//
//	/*
//	 * Test method for 'java.lang.Object.finalize()'
//	 */
//	public void testFinalize() {
//
//	}

}
