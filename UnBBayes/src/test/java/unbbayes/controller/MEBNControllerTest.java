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
package unbbayes.controller;

import java.io.File;

import unbbayes.gui.NetworkWindow;
import unbbayes.io.mebn.UbfIO;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.util.Debug;
import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author shou
 * 
 * FIXME this is not testing properly. Fix it.
 *
 *
 */
public class MEBNControllerTest extends TestCase {

	private MEBNController controller = null;
	private MultiEntityBayesianNetwork mebn = null;
	
	private String plmFileName = "src/test/resources/testCases/mebn/knowledgeBase/KnowledgeBaseWithStarshipZoneST4ver2.plm";
	private String owlFileName = "src/test/resources/testCases/mebn/StarTrek.ubf";
	
	/**
	 * @param arg0
	 */
	public MEBNControllerTest(String arg0) {
		super(arg0);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		Debug.setDebug(false);
		UbfIO io = UbfIO.getInstance();
		mebn = io.loadMebn(new File(owlFileName));
		this.controller = new MEBNController(mebn,new NetworkWindow(mebn));
		//PowerLoomKB.getInstanceKB().loadModule(new File(plmFileName));
		Debug.setDebug(true);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		Debug.setDebug(false);
	}


	/**
	 * Test method for {@link unbbayes.controller.MEBNController#loadFindingsFile(java.io.File file)}.
	 */
	public void testLoadFindingsFile() {
		try {
			this.controller.loadFindingsFile(new File(plmFileName));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	/**
	 *  Use this for test suite creation
	 */
	public static Test suite() {
		return new TestSuite(MEBNControllerTest.class);
	}
}
