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
package unbbayes.io.mebn;

import unbbayes.prs.mebn.ArgumentTest;
import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author shou
 *
 */
public class PrOwlIOTest extends TestCase {

	/**
	 * @param arg0
	 */
	public PrOwlIOTest(String arg0) {
		super(arg0);
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
	 * Test method for {@link unbbayes.io.mebn.PrOwlIO#loadMebn(java.io.File)}.
	 */
	public void testLoadMebn() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.io.mebn.PrOwlIO#saveMebn(java.io.File, unbbayes.prs.mebn.MultiEntityBayesianNetwork)}.
	 */
	public void testSaveMebn() {
		fail("Not yet implemented"); // TODO
	}
	
	/**
	 *  Use this for test suite creation
	 */
	public static Test suite() {
		return new TestSuite(PrOwlIOTest.class);
	}
}
