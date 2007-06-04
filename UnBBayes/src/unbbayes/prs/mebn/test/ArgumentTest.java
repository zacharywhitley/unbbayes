/**
 * 
 */
package unbbayes.prs.mebn.test;

import unbbayes.prs.mebn.Argument;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.MultiEntityNode;
import unbbayes.prs.mebn.builtInRV.test.BuiltInRVAndTest;
import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author shou
 *
 */
public class ArgumentTest extends TestCase {
	
	MultiEntityBayesianNetwork mebn = null;
	MultiEntityNode node = null;
	Argument argument = null;
	
	
	/**
	 * @param arg0
	 */
	public ArgumentTest(String arg0) {
		super(arg0);
		this.mebn = new MultiEntityBayesianNetwork("TestMEBN");
		// TODO auto generated stub
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.Argument#Argument(java.lang.String, unbbayes.prs.mebn.MultiEntityNode)}.
	 */
	public void testArgument() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.Argument#setArgumentTerm(unbbayes.prs.mebn.MultiEntityNode)}.
	 */
	public void testSetArgumentTerm() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.Argument#setOVariable(unbbayes.prs.mebn.OrdinaryVariable)}.
	 */
	public void testSetOVariable() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.Argument#getName()}.
	 */
	public void testGetName() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.Argument#getArgumentTerm()}.
	 */
	public void testGetArgumentTerm() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.Argument#getOVariable()}.
	 */
	public void testGetOVariable() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.Argument#getMultiEntityNode()}.
	 */
	public void testGetMultiEntityNode() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.Argument#isSimpleArgRelationship()}.
	 */
	public void testIsSimpleArgRelationship() {
		fail("Not yet implemented"); // TODO
	}
	
	/**
	 *  Use this for test suite creation
	 */
	public static Test suite() {
		return new TestSuite(ArgumentTest.class);
	}
}
