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
import unbbayes.io.msbn.IMSBNIO;
import unbbayes.io.msbn.impl.DefaultMSBNIO;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.TreeVariable;
import unbbayes.prs.msbn.SingleAgentMSBN;
import unbbayes.prs.msbn.SubNetwork;

import junit.framework.TestCase;

/**
 * @author Michael Onishi
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class TestCompiled extends TestCase {
	public static final File FILE_5PARTC = new File("examples/msbn/5partc");
	public static final File FILE_5PARTCMONO = new File("examples/msbn/5partcmono.net");
	public static final double DELTA = 0.001;
	
	public TestCompiled(String msg) {
		super(msg);
	}
	
	public void testTopological() throws Exception {
		IMSBNIO loader = DefaultMSBNIO.newInstance();
		SingleAgentMSBN msbn =  loader.loadMSBN(FILE_5PARTC);
		ProbabilisticNetwork net = (ProbabilisticNetwork)loader.load(FILE_5PARTCMONO);
		net.compile(); 
		msbn.compile();
		
		for (int i = 0; i < msbn.getNetCount(); i++) {
			SubNetwork sub = msbn.getNetAt(i);
			for (int j = 0; j < sub.getNodeCount(); j++) {
				TreeVariable var = (TreeVariable) sub.getNodeAt(j);
				TreeVariable varMono = (TreeVariable) net.getNode(var.getName());
				for (int k = 0; k < var.getStatesSize(); k++) {
					assertEquals(varMono.getMarginalAt(k), var.getMarginalAt(k), DELTA);					
				}
			}	
		}
	}
		
}
