/**
 * 
 */
package unbbayes.prs.mebn.ssbn.test;

import java.io.File;
import java.io.IOException;

import unbbayes.io.mebn.UbfIO;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.mebn.DomainMFrag;
import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.entity.Entity;
import unbbayes.prs.mebn.ssbn.SSBNNode;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;
import junit.framework.TestCase;

/**
 * @author shou
 *
 */
public class SSBNNodeTest extends TestCase {

	private MultiEntityBayesianNetwork mebn = null;
	private  DomainMFrag mfrag = null;
	private DomainResidentNode resident = null;
	
	private SSBNNode ssbnnode = null;
	/**
	 * @param arg0
	 */
	public SSBNNodeTest(String arg0) {
		super(arg0);
		
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		UbfIO ubf = UbfIO.getInstance();
		try {
			mebn = ubf.loadMebn(new File("examples/mebn/SSBNNodeTest.ubf"));
		} catch (Exception e) {
			fail(e.getMessage());
			return;
		}
		
		if (this.mebn == null) {
			fail("Unable to load file");
			return;
		}
		this.resident = mebn.getDomainResidentNode("HarmPotential");
		
		if (this.resident == null) {
			fail("Unable to retreve resident node");
			return;
		}
		
		this.ssbnnode = SSBNNode.getInstance(this.resident);
		
		
		
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.ssbn.SSBNNode#getInstance(unbbayes.prs.mebn.DomainResidentNode, unbbayes.prs.bn.ProbabilisticNode)}.
	 */
	public void testGetInstanceDomainResidentNodeProbabilisticNode() {
		ProbabilisticNode pnode = new ProbabilisticNode();
		SSBNNode node = SSBNNode.getInstance(this.resident,pnode);
		assertNotNull(node);
		assertEquals(node.getResident(), this.resident);
		assertEquals(node.getProbNode(), pnode);
		assertEquals(3,node.getActualValues().size()); // contains true, false and absurd
		assertEquals(node.getParents().size(),0);
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.ssbn.SSBNNode#getInstance(unbbayes.prs.mebn.DomainResidentNode)}.
	 */
	public void testGetInstanceDomainResidentNode() {
		SSBNNode node = SSBNNode.getInstance(resident);
		assertNotNull(node);
		assertEquals(node.getResident(), resident);
		assertNotNull(node.getProbNode());
		assertEquals(node.getActualValues().size(),3);
		assertEquals(node.getParents().size(),0);
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.ssbn.SSBNNode#getOVs()}.
	 */
	public void testGetOVs() {
		for (OrdinaryVariable ov : this.resident.getOrdinaryVariableList()) {
			try { 
				this.ssbnnode.addArgument(ov, ov.getName());
			} catch (SSBNNodeGeneralException e) {
				fail(e.getMessage());
			}
		}
		assertTrue(this.resident.getOrdinaryVariableList().containsAll(this.ssbnnode.getOVs()));
		assertTrue(this.ssbnnode.getOVs().containsAll(this.resident.getOrdinaryVariableList()));
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.ssbn.SSBNNode#hasOV(unbbayes.prs.mebn.OrdinaryVariable)}.
	 */
	public void testHasOVOrdinaryVariable() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.ssbn.SSBNNode#hasOV(java.lang.String, boolean)}.
	 */
	public void testHasOVStringBoolean() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.ssbn.SSBNNode#hasAllOVs(java.util.Collection)}.
	 */
	public void testHasAllOVsCollectionOfOrdinaryVariable() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.ssbn.SSBNNode#hasAllOVs(unbbayes.prs.mebn.OrdinaryVariable[])}.
	 */
	public void testHasAllOVsOrdinaryVariableArray() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.ssbn.SSBNNode#hasAllOVs(boolean, java.lang.String[])}.
	 */
	public void testHasAllOVsBooleanStringArray() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.ssbn.SSBNNode#addArgument(unbbayes.prs.mebn.OrdinaryVariable, java.lang.String)}.
	 */
	public void testAddArgumentOrdinaryVariableString() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.ssbn.SSBNNode#addArgument(unbbayes.prs.mebn.OrdinaryVariable, java.lang.String, int)}.
	 */
	public void testAddArgumentOrdinaryVariableStringInt() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.ssbn.SSBNNode#setNodeAsFinding(unbbayes.prs.mebn.entity.Entity)}.
	 */
	public void testSetNodeAsFinding() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.ssbn.SSBNNode#fillProbabilisticTable()}.
	 */
	public void testFillProbabilisticTable() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.ssbn.SSBNNode#addParent(unbbayes.prs.mebn.ssbn.SSBNNode)}.
	 */
	public void testAddParent() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.ssbn.SSBNNode#removeParent(unbbayes.prs.mebn.ssbn.SSBNNode)}.
	 */
	public void testRemoveParent() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.ssbn.SSBNNode#removeParentByName(java.lang.String)}.
	 */
	public void testRemoveParentByName() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.ssbn.SSBNNode#getParentSetByStrongOV(java.lang.String[])}.
	 */
	public void testGetParentSetByStrongOVStringArray() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.ssbn.SSBNNode#getParentSetByStrongOV(java.util.Collection)}.
	 */
	public void testGetParentSetByStrongOVCollectionOfOrdinaryVariable() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.ssbn.SSBNNode#getParentSetByStrongOV(unbbayes.prs.mebn.OrdinaryVariable[])}.
	 */
	public void testGetParentSetByStrongOVOrdinaryVariableArray() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.ssbn.SSBNNode#getParentMapByWeakOV(unbbayes.prs.mebn.OrdinaryVariable[])}.
	 */
	public void testGetParentMapByWeakOV() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.ssbn.SSBNNode#getName()}.
	 */
	public void testGetName() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.ssbn.SSBNNode#getActualValues()}.
	 */
	public void testGetActualValues() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.ssbn.SSBNNode#setActualValues(java.util.Collection)}.
	 */
	public void testSetActualValues() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.ssbn.SSBNNode#getArguments()}.
	 */
	public void testGetArguments() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.ssbn.SSBNNode#setArguments(java.util.List)}.
	 */
	public void testSetArguments() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.ssbn.SSBNNode#isUsingDefaultCPT()}.
	 */
	public void testIsUsingDefaultCPT() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.ssbn.SSBNNode#setUsingDefaultCPT(boolean)}.
	 */
	public void testSetUsingDefaultCPT() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.ssbn.SSBNNode#getParents()}.
	 */
	public void testGetParents() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.ssbn.SSBNNode#setParents(java.util.Collection)}.
	 */
	public void testSetParents() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.ssbn.SSBNNode#getProbNode()}.
	 */
	public void testGetProbNode() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.ssbn.SSBNNode#setProbNode(unbbayes.prs.bn.ProbabilisticNode)}.
	 */
	public void testSetProbNode() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.ssbn.SSBNNode#getResident()}.
	 */
	public void testGetResident() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.ssbn.SSBNNode#setResident(unbbayes.prs.mebn.DomainResidentNode)}.
	 */
	public void testSetResident() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.ssbn.SSBNNode#getStrongOVSeparator()}.
	 */
	public void testGetStrongOVSeparator() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.ssbn.SSBNNode#setStrongOVSeparator(java.lang.String)}.
	 */
	public void testSetStrongOVSeparator() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.ssbn.SSBNNode#getCompiler()}.
	 */
	public void testGetCompiler() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.ssbn.SSBNNode#setCompiler(unbbayes.prs.mebn.compiler.ICompiler)}.
	 */
	public void testSetCompiler() {
		fail("Not yet implemented"); // TODO
	}

}
