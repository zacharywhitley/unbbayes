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

package unbbayes.prs.bn;


import unbbayes.prs.Node;
import unbbayes.prs.id.JunctionTreeID;
import unbbayes.util.NodeList;
import unbbayes.util.SetToolkit;

/**
 *  Representa uma rede probabilística.
 *
 *@author     michael
 *@author     rommel
 */
public class ProbabilisticNetwork
	extends SingleEntityNetwork
	implements java.io.Serializable {

	  /** Serialization runtime version number */
	  private static final long serialVersionUID = 0;
			
	
	/**
	 *  Cria uma nova rede probabilística. Limpa o arquivo de log e inicializa o
	 *  vetor da ordem de eliminação.
	 */
	public ProbabilisticNetwork(String id) {
		super(id);							
		oe = new NodeList();
		firstInitialization = true;
	}
	

	/**
	 *  Faz o processo de triangulação da rede.
	 */
	private void triangula() {		
		Node aux;
		NodeList auxNos;

		if (createLog) {
			logManager.append(resource.getString("triangulateLabel"));
		}
		auxNos = SetToolkit.clone(nodeList);
		removeUtilityNodes(auxNos);
		copiaNos = SetToolkit.clone(auxNos);
		int sizeDecisao = decisionNodes.size();
		for (int i = 0; i < sizeDecisao; i++) {
			aux = decisionNodes.get(i);
			auxNos.remove(aux);
			auxNos.removeAll(aux.getParents());
		}

		oe = new NodeList(copiaNos.size());

		while (minimumWeightElimination(auxNos))
			;

		//        int index;
		for (int i = decisionNodes.size() - 1; i >= 0; i--) {
			aux = decisionNodes.get(i);
			oe.add(aux);
			int sizeAdjacentes = aux.getAdjacents().size();
			for (int j = 0; j < sizeAdjacentes; j++) {
				Node v = aux.getAdjacents().get(j);
				v.getAdjacents().remove(aux);
			}
			if (createLog) {
				logManager.append(
					"\t" + oe.size() + " " + aux.getName() + "\n");
			}

			auxNos = SetToolkit.clone(aux.getParents());
			auxNos.removeAll(decisionNodes);
			auxNos.removeAll(oe);
			for (int j = 0; j < i; j++) {
				Node decision = decisionNodes.get(j);
				auxNos.removeAll(decision.getParents());
			}

			while (minimumWeightElimination(auxNos)) 
				;
		}
		
		makeAdjacents();
	}

	private void removeUtilityNodes(NodeList nodes) {
		for (int i = nodes.size() - 1; i >= 0; i--) {
			if (nodes.get(i).getType() == Node.UTILITY_NODE_TYPE) {
				nodes.remove(i);
			}
		}
	}


	/**
	 * Realiza todos os passos necessários para compilar uma rede em árvore de junção. <br><br>
	 * Realiza os seguintes passos: <br>
	 * Verifica a consistência. <br>
	 * Moraliza. <br>
	 * Triangula. <br>
	 * Compila Árvore de Junção.
	 */
	public void compile() throws Exception {
		if (nodeList.size() == 0) {
			throw new Exception(resource.getString("EmptyNetException"));
		}
		if (createLog) {
			logManager.reset();
		}
		verifyConsistency();
		moralize();
		triangula();		
		
		if (isID()) {
			compileJT(new JunctionTreeID());
		} else {
			compileJT(new JunctionTree());
		}
	}

}