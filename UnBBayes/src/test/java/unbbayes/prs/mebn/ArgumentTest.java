/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package unbbayes.prs.mebn;

import unbbayes.prs.mebn.Argument;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.MultiEntityNode;
import unbbayes.prs.mebn.builtInRV.BuiltInRVAndTest;
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
