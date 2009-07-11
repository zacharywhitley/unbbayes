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
package unbbayes.example;

import java.io.File;

import unbbayes.io.BaseIO;
import unbbayes.io.NetIO;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.msbn.SingleAgentMSBN;
import unbbayes.prs.msbn.SubNetwork;

/**
 * Sample code for MSBN API
 * @author Michael
 * @author Rommel
 * @deprecated see {@link unbbayes.msbn.MSBNExampleTest} instead.
 * @see unbbayes.msbn.MSBNExampleTest
 */
public class MSBNExample {

	/**
	 * Sample code for MSBN API
	 * @author Michael
	 * @author Rommel
	 * @deprecated see {@link unbbayes.msbn.MSBNExampleTest} instead.
	 * @see unbbayes.msbn.MSBNExampleTest
	 */
	public static void main(String[] args) throws Exception {
		SingleAgentMSBN msbn = null;
		
		BaseIO io = new NetIO();
		msbn = io.loadMSBN(new File("src/test/resources/testCases/msbn/5partc/"));
		
		msbn.compile();
		
		SubNetwork net = msbn.getNetAt(0);
		ProbabilisticNode node = (ProbabilisticNode) net.getNode("var_1");
		node.addFinding(1);
		net.updateEvidences();
		
		net = msbn.getNetAt(2);
		msbn.shiftAttention(net);
	}
}
