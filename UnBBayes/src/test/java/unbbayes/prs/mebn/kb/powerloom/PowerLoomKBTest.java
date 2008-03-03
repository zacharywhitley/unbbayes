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
package unbbayes.prs.mebn.kb.powerloom;

import java.io.File;

import unbbayes.io.mebn.UbfIO;
import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.kb.powerloom.PowerLoomKB;
import junit.framework.TestCase;

/**
 * @author user
 *
 */
public class PowerLoomKBTest extends TestCase {

	private PowerLoomKB kb = null;
	
	/**
	 * @param arg0
	 */
	public PowerLoomKBTest(String arg0) {
		super(arg0);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		kb = PowerLoomKB.getInstanceKB();
		kb.loadModule(new File("examples/mebn/KnowledgeBase/KnowledgeBaseWithStarshipZoneST4.plm"));
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.kb.powerloom.PowerLoomKB#executeCommand(java.lang.String)}.
	 */
	public void testExecuteCommand() {
		String result = kb.executeCommand(" ( retrieve all ( = ( STARSHIPZONE ?x1  ) ?x ) ) ");
		assertNotNull(result);
		System.out.println(result);		
		result = kb.executeCommand(" ( retrieve all (   ISOWNSTARSHIP ?x11 ) ) ");
		assertNotNull(result);
		System.out.println(result);
		result = kb.executeCommand(" ( retrieve all ( not ( ISOWNSTARSHIP ?x21 ) ) ) ");
		assertNotNull(result);
		System.out.println(result);	
	}
	
	/**
	 * Test method for {@link unbbayes.prs.mebn.kb.powerloom.fillFindings(DomainResidentNode resident)}.
	 */
	public void testFillFindings() {
		UbfIO io = UbfIO.getInstance();
		MultiEntityBayesianNetwork mebn = null;
		try {
			mebn = io.loadMebn(new File("examples/mebn/StarTrek49.ubf"));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		assertNotNull(kb);
		assertNotNull(mebn);
		assertNotNull(mebn.getDomainResidentNode("DISTFROMOWN"));
		
		
		kb.fillFindings(mebn.getDomainResidentNode("DISTFROMOWN"));
		
	}

}
