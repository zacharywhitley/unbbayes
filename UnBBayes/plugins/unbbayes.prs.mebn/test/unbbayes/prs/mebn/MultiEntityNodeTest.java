/**
 * 
 */
package unbbayes.prs.mebn;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import unbbayes.prs.mebn.entity.TypeContainer;
import unbbayes.prs.mebn.entity.exception.TypeAlreadyExistsException;
import unbbayes.prs.mebn.exception.ArgumentNodeAlreadySetException;
import unbbayes.prs.mebn.exception.ArgumentOVariableAlreadySetException;

/**
 * @author user
 *
 */
public class MultiEntityNodeTest extends TestCase {

	MultiEntityBayesianNetwork mebn = null;
	MFrag mfrag = null;
	
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
		mfrag = new MFrag("MultiEntityNodeTestMFrag",mebn);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
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
		
		
		MultiEntityNode resident = new ResidentNode("resident_st_t",mfrag);
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
		
		mfrag.addResidentNode((ResidentNode)resident);
		
		
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
