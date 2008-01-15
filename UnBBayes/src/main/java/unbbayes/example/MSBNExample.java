/*
 *  UnbBayes
 *  Copyright (C) 2002 Universidade de Brasília
 *
 *  This file is part of UnbBayes.
 *
 *  UnbBayes is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  UnbBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnbBayes; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package unbbayes.example;

import java.io.File;

import unbbayes.io.BaseIO;
import unbbayes.io.NetIO;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.msbn.SingleAgentMSBN;
import unbbayes.prs.msbn.SubNetwork;

/**
 * Exemplo de uso da API para MSBN
 * @author Michael
 * @author Rommel
 */
public class MSBNExample {

	public static void main(String[] args) throws Exception {
		SingleAgentMSBN msbn = null;
		
		BaseIO io = new NetIO();
		msbn = io.loadMSBN(new File("./examples/msbn/5partc/"));
		
		msbn.compile();
		
		SubNetwork net = msbn.getNetAt(0);
		ProbabilisticNode node = (ProbabilisticNode) net.getNode("var_1");
		node.addFinding(1);
		net.updateEvidences();
		
		net = msbn.getNetAt(2);
		msbn.shiftAttention(net);
	}
}
