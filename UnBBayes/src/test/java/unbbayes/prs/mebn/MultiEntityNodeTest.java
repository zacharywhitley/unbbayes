/**
 * 
 */
package unbbayes.prs.mebn;

import com.hp.hpl.jena.reasoner.rdfsReasoner1.AssertFRule;

import unbbayes.prs.mebn.Argument;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.DomainMFrag;
import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.MultiEntityNode;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.builtInRV.BuiltInRVAndTest;
import unbbayes.prs.mebn.entity.TypeContainer;
import unbbayes.prs.mebn.entity.exception.TypeAlreadyExistsException;
import unbbayes.prs.mebn.exception.ArgumentNodeAlreadySetException;
import unbbayes.prs.mebn.exception.ArgumentOVariableAlreadySetException;
import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author user
 *
 */
public class MultiEntityNodeTest extends TestCase {

	MultiEntityBayesianNetwork mebn = null;
	DomainMFrag mfrag = null;
	
	/**
	 * @param arg0
	 */
	public MultiEntityNodeTest(String arg0) {
		super(arg0);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		mebn = new MultiEntityBayesianNetwork("MultiEntityNodeTestMEBN");
		mfrag = new DomainMFrag("MultiEntityNodeTestMFrag",mebn);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MultiEntityNode#getType()}.
	 */
	public void testGetType() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MultiEntityNode#MultiEntityNode()}.
	 */
	public void testMultiEntityNode() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MultiEntityNode#getMFrag()}.
	 */
	public void testGetMFrag() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MultiEntityNode#getColor()}.
	 */
	public void testGetColor() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MultiEntityNode#setColor(int)}.
	 */
	public void testSetColor() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MultiEntityNode#removeFromMFrag()}.
	 */
	public void testRemoveFromMFrag() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MultiEntityNode#addArgument(unbbayes.prs.mebn.Argument)}.
	 */
	public void testAddArgument() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MultiEntityNode#removeArgument(unbbayes.prs.mebn.Argument)}.
	 */
	public void testRemoveArgument() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MultiEntityNode#addInnerTermOfList(unbbayes.prs.mebn.MultiEntityNode)}.
	 */
	public void testAddInnerTermOfList() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MultiEntityNode#addInnerTermFromList(unbbayes.prs.mebn.MultiEntityNode)}.
	 */
	public void testAddInnerTermFromList() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MultiEntityNode#addPossibleValue(unbbayes.prs.mebn.entity.Entity)}.
	 */
	public void testAddPossibleValue() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MultiEntityNode#removePossibleValueByName(java.lang.String)}.
	 */
	public void testRemovePossibleValueByName() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MultiEntityNode#existsPossibleValueByName(java.lang.String)}.
	 */
	public void testExistsPossibleValueByName() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MultiEntityNode#getArgumentList()}.
	 */
	public void testGetArgumentList() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MultiEntityNode#getInnerTermOfList()}.
	 */
	public void testGetInnerTermOfList() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MultiEntityNode#getInnerTermFromList()}.
	 */
	public void testGetInnerTermFromList() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MultiEntityNode#getPossibleValueList()}.
	 */
	public void testGetPossibleValueList() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MultiEntityNode#hasPossibleValue(unbbayes.prs.mebn.entity.Entity)}.
	 */
	public void testHasPossibleValueEntity() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MultiEntityNode#hasPossibleValue(java.lang.String)}.
	 */
	public void testHasPossibleValueString() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.MultiEntityNode#getPossibleValueIndex(java.lang.String)}.
	 */
	public void testGetPossibleValueIndex() {
		fail("Not yet implemented"); // TODO
	}
	
	public void testHasAllOVs_getAllOVCount() {
		TypeContainer tcontainer = new TypeContainer();
		try {
			tcontainer.createType("Starship");
			tcontainer.createType("Zone");
			tcontainer.createType("Timestep");
			tcontainer.createType("asdfasdf");
		} catch (TypeAlreadyExistsException e) {
			fail(e.getMessage());
		}
		
		OrdinaryVariable st = new OrdinaryVariable("st",tcontainer.getType("Starship"),mfrag);
		OrdinaryVariable z = new OrdinaryVariable("z",tcontainer.getType("Zone"),mfrag);
		OrdinaryVariable t = new OrdinaryVariable("t",tcontainer.getType("Timestep"),mfrag);
		
		
		MultiEntityNode resident = new DomainResidentNode("resident_st_t",mfrag);
		Argument arg =  new Argument("arg1",resident);
		arg.setArgNumber(1);
		try{
			arg.setOVariable(st);
		} catch (ArgumentNodeAlreadySetException e) {
			fail(e.getMessage());
		}
		resident.addArgument(arg);
		arg =  new Argument("arg2",resident);
		arg.setArgNumber(2);
		try{
			arg.setOVariable(t);
		} catch (ArgumentNodeAlreadySetException e) {
			fail(e.getMessage());
		}
		resident.addArgument(arg);
		
		mfrag.addDomainResidentNode((DomainResidentNode)resident);
		
		
		MultiEntityNode context1 = new ContextNode("context_st_t",mfrag);
		arg =  new Argument("arg1",context1);
		arg.setArgNumber(1);
		try{
			arg.setOVariable(st);
		} catch (ArgumentNodeAlreadySetException e) {
			fail(e.getMessage());
		}
		context1.addArgument(arg);
		arg =  new Argument("arg2",context1);
		arg.setArgNumber(2);
		try{
			arg.setOVariable(t);
		} catch (ArgumentNodeAlreadySetException e) {
			fail(e.getMessage());
		}
		context1.addArgument(arg);
		
		mfrag.addContextNode((ContextNode)context1);
		
		MultiEntityNode context2 = new ContextNode("context_resident_st_t",mfrag);
		arg =  new Argument("arg1",context2);
		arg.setArgNumber(1);
		try{
			arg.setArgumentTerm(resident);
		} catch (ArgumentOVariableAlreadySetException e) {
			fail(e.getMessage());
		}
		context2.addArgument(arg);
		
		mfrag.addContextNode((ContextNode)context2);
		
		
		assertTrue(resident.hasAllOVs(st,t));
		assertTrue(context1.hasAllOVs(st,t));
		assertTrue(context2.hasAllOVs(st,t));
		
		assertTrue(resident.hasAllOVs(st));
		assertTrue(context1.hasAllOVs(st));
		assertTrue(context2.hasAllOVs(st));
		
		assertTrue(resident.hasAllOVs(t));
		assertTrue(context1.hasAllOVs(t));
		assertTrue(context2.hasAllOVs(t));
		
		assertTrue(!resident.hasAllOVs(new OrdinaryVariable("error",tcontainer.getType("asdfasdf"),mfrag)));
		assertTrue(!context1.hasAllOVs(new OrdinaryVariable("error",tcontainer.getType("asdfasdf"),mfrag)));
		assertTrue(!context2.hasAllOVs(new OrdinaryVariable("error",tcontainer.getType("asdfasdf"),mfrag)));
		
		assertEquals(resident.getAllOVCount(),2);
		assertEquals(context1.getAllOVCount(),2);
		assertEquals(context2.getAllOVCount(),2);
	}

	/**
	 *  Use this for test suite creation
	 */
	public static Test suite() {
		return new TestSuite(MultiEntityNodeTest.class);
	}
}
