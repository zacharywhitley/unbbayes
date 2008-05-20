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
package unbbayes.prs.msbn;

import java.io.File;

import unbbayes.io.NetIO;
import unbbayes.prs.msbn.SingleAgentMSBN;

import junit.framework.TestCase;

/**
 * @author Michael S. Onishi
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class TestTopologicalTransformation extends TestCase {
	private static File CYCLE_FILE = new File("examples/msbn/cycle-test");
	private static File TOPOLOGICAL_FILE = new File("examples/msbn/topological-test");
	
	public TestTopologicalTransformation(String m) {
		super(m);
	}
	
	public void testCycle() throws Exception {
		NetIO loader = new NetIO();
		SingleAgentMSBN msbn =  loader.loadMSBN(CYCLE_FILE);
		boolean cycleDetected = false;		
		try {
			msbn.compile();
		} catch (Exception e) {
			cycleDetected = true;
		}
		assertTrue("Cycle not detected", cycleDetected);
	}
	
	public void testTopological() throws Exception {
		NetIO loader = new NetIO();
		SingleAgentMSBN msbn =  loader.loadMSBN(TOPOLOGICAL_FILE);
		msbn.compile();	
	}
}
