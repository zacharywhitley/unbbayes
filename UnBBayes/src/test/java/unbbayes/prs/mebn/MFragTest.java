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

import unbbayes.prs.Node;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.DomainMFrag;
import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.GenerativeInputNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.builtInRV.BuiltInRVAndTest;
import unbbayes.util.NodeList;
import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author user
 *
 */
public class MFragTest extends TestCase {

	
	/**
	 * @param arg0	
	 */
	public MFragTest(String arg0) {
		super(arg0);
	}
	
	
	public void testContainsNode(){
		MultiEntityBayesianNetwork mebn = new MultiEntityBayesianNetwork("teste");
		DomainMFrag mfrag1 = new DomainMFrag("mfrag1",mebn);
		DomainMFrag mfrag2 = new DomainMFrag("mfrag2",mebn);
		
		DomainResidentNode resident1 = new DomainResidentNode("resident1",mfrag1);
		mfrag1.addDomainResidentNode(resident1);
		DomainResidentNode resident2 = new DomainResidentNode("resident2",mfrag2);
		mfrag2.addDomainResidentNode(resident2);
		GenerativeInputNode input1 = new GenerativeInputNode("resident1",mfrag2);
		mfrag2.addGenerativeInputNode(input1);
		try {
			input1.setInputInstanceOf(resident1);
		} catch (Exception e) {
			fail("A cycle has been found");
		}
		resident2.addParent(input1);
		
		NodeList temp = mfrag1.getNodeList();
		for (int i = 0; i < temp.size(); i++) {
			System.out.println(temp.get(i).getName());
			System.out.println(temp.get(i).getClass());
		}
		System.out.println("===============");
		temp = mfrag2.getNodeList();
		for (int i = 0; i < temp.size(); i++) {
			System.out.println(temp.get(i).getName());
			System.out.println(temp.get(i).getClass());
		}
		
		assertTrue(mfrag1.containsNode(resident1));
		assertNotNull(mfrag1.containsNode("resident1"));
		assertTrue(!mfrag1.containsNode(resident2));
		assertNull(mfrag1.containsNode("resident2"));
		//assertTrue(!(mfrag1.containsNode(input1))); // This is searching by node name
		
		assertTrue(mfrag2.containsNode(resident2));
		assertNotNull(mfrag2.containsNode("resident2"));
		//assertTrue(!mfrag2.containsNode(resident1));
		assertNotNull(mfrag2.containsNode("resident1"));
		assertTrue(mfrag2.containsNode(input1));
		
	}

	/**
	 *  Use this for test suite creation
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite("Test for unbbayes.prs.mebn.test.MFragTest");
		//$JUnit-BEGIN$
		suite.addTest(DomainMFragTest.suite());
		suite.addTest(new TestSuite(MFragTest.class));
		return suite;
	}
}
