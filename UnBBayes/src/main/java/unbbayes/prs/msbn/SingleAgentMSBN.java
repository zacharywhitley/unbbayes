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

import java.util.List;

/**
 * A multi-sectioned network. 
 * @author Michael S. Onishi
 */
public class SingleAgentMSBN extends AbstractMSBN {
	protected SubNetwork activeNet;
	
	/**
	 * Creates a new multi-sectioned network with the specified id.
	 * @param id	the id of this multi-sectioned network
	 */
	public SingleAgentMSBN(String id) {
		super(id);
	}
	
	protected void initBeliefs() throws Exception {
		SubNetwork raiz = (SubNetwork) nets.get(0);		
		coletBeliefs(raiz);
		distributeBelief(raiz);
	}

	
	/**
	 * Shifts attention from the active sub-network to the specified sub-network.
	 * @param net	the subnetwork to shift attention.
	 */
	public void shiftAttention(SubNetwork net) throws Exception {
		List caminho = activeNet.makePath(net);		
		for (int i = 1; i < caminho.size(); i++) {
			SubNetwork netAux = (SubNetwork) caminho.get(i);
			updateBelief(netAux, activeNet);
			activeNet = netAux;
		}
		
//		assert activeNet == net;
	}
}
