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
import java.util.List;
import java.util.StringTokenizer;

import junit.framework.TestCase;
import unbbayes.io.mebn.UbfIO;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.RandomVariableFinding;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.util.Debug;

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
		Debug.setDebug(true);
		kb = PowerLoomKB.getNewInstanceKB();
		kb.loadModule(new File("examples/mebn/KnowledgeBase/KnowledgeBaseWithStarshipZoneST4ver2.plm"));
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		Debug.setDebug(false);
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
		try{
			result = kb.executeCommand(" ( retrieve all  ( EXISTS0 ?x666 ) ) ");
		} catch (Exception e){
			e.printStackTrace();
			fail(e.getMessage());
		}
		assertNotNull(result);
		System.out.println(result);	
	}


	/**
	 * Test method for {@link unbbayes.prs.mebn.kb.powerloom.parsePLIStringAndFillFinding(ResidentNode resident , String strPLI)}
	 * and its equivalent for boolean expression.
	 */
	/*
	public void testparsePLIStringAndFillFinding() {
		UbfIO io = UbfIO.getInstance();
		MultiEntityBayesianNetwork mebn = null;
		try {
			mebn = io.loadMebn(new File("examples/mebn/StarTrek51.ubf"));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		assertNotNull(kb);
		assertNotNull(mebn);
		assertNotNull(mebn.getDomainResidentNode("ISOWNSTARSHIP"));
		
		List<RandomVariableFinding> findings = null;
		
		String query = "(ST1 ST2 ST3 ST4)";
		Debug.println(query + ":");
		try{
			findings = kb.parsePLIStringAndFillBooleanFinding(mebn.getDomainResidentNode("ISOWNSTARSHIP"),
												   query, true, false);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		assertNotNull(findings);
		for (RandomVariableFinding randomVariableFinding : findings) {
			Debug.println(randomVariableFinding.toString());
		}
		
		findings = null;
		query = "(ST0)";
		Debug.println(query + ":");
		try{
			findings = kb.parsePLIStringAndFillBooleanFinding(mebn.getDomainResidentNode("ISOWNSTARSHIP"),
												   query, true, true);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		assertNotNull(findings);
		for (RandomVariableFinding randomVariableFinding : findings) {
			Debug.println(randomVariableFinding.toString());
		}
		
		
		
		findings = null;
		query = "((ST1 T0 PHASER2RANGE) \n (ST3 T0 TORPEDORANGE) \n (ST4 T0 PHASER1RANGE))";
		Debug.println(query + ":");
		try{
			findings = kb.parsePLIStringAndFillBooleanFinding(mebn.getDomainResidentNode("DISTFROMOWN"),
												   query, false, false);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		assertNotNull(findings);
		for (RandomVariableFinding randomVariableFinding : findings) {
			Debug.println(randomVariableFinding.toString());
		}
		
	}
	*/
	
	/**
	 * Test method for {@link unbbayes.prs.mebn.kb.powerloom.fillFindings(DomainResidentNode resident)}.
	 */
	/*
	public void testFillFindings() {
		UbfIO io = UbfIO.getInstance();
		MultiEntityBayesianNetwork mebn = null;
		try {
			mebn = io.loadMebn(new File("examples/mebn/StarTrek51.ubf"));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		assertNotNull(kb);
		assertNotNull(mebn);
		
		assertNotNull(mebn.getDomainResidentNode("DISTFROMOWN"));
		try{
		kb.fillFindings(mebn.getDomainResidentNode("DISTFROMOWN"));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}		
		assertEquals(4, mebn.getDomainResidentNode("DISTFROMOWN").getRandonVariableFindingList().size());
		
		
		assertNotNull(mebn.getDomainResidentNode("ISOWNSTARSHIP"));
		try{
		kb.fillFindings(mebn.getDomainResidentNode("ISOWNSTARSHIP"));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		assertEquals(5, mebn.getDomainResidentNode("ISOWNSTARSHIP").getRandonVariableFindingList().size());
		
		assertNotNull(mebn.getDomainResidentNode("HARMPOTENTIAL"));
		try{
		kb.fillFindings(mebn.getDomainResidentNode("HARMPOTENTIAL"));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		assertEquals(0, mebn.getDomainResidentNode("HARMPOTENTIAL").getRandonVariableFindingList().size());
		
		assertNotNull(mebn.getDomainResidentNode("EXISTS"));
		try{
		kb.fillFindings(mebn.getDomainResidentNode("EXISTS"));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		assertEquals(5, mebn.getDomainResidentNode("EXISTS").getRandonVariableFindingList().size());
		
	}
	*/
	
}
