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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import unbbayes.io.LogManager;
import unbbayes.prs.Edge;
import unbbayes.prs.Node;
import unbbayes.prs.id.DecisionNode;
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
	extends Network
	implements java.io.Serializable {

	private boolean firstInitialization;

	/**
	 *  Cria uma nova rede probabilística. Limpa o arquivo de log e inicializa o
	 *  vetor da ordem de eliminação.
	 */
	public ProbabilisticNetwork() {								
		oe = new NodeList();
		firstInitialization = true;
	}
	

	public String getLog() {
		return logManager.getLog();
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
		auxNos = SetToolkit.clone(nos);
		removeUtilityNodes(auxNos);
		copiaNos = SetToolkit.clone(auxNos);
		int sizeDecisao = decisionNodes.size();
		for (int i = 0; i < sizeDecisao; i++) {
			aux = decisionNodes.get(i);
			auxNos.remove(aux);
			auxNos.removeAll(aux.getParents());
		}

		oe = new NodeList(copiaNos.size());

		while (pesoMinimo(auxNos))
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

			while (pesoMinimo(auxNos)) 
				;
		}
	}

	private void removeUtilityNodes(NodeList nodes) {
		for (int i = nodes.size() - 1; i >= 0; i--) {
			if (nodes.get(i).getType() == Node.UTILITY_NODE_TYPE) {
				nodes.remove(i);
			}
		}
	}

	/**
	 *  Liga as duas variáveis e toma as devidas providências.
	 *
	 *@param  arco  arco a ser inserido na rede.
	 */
	public void addEdge(Edge arco) {
		super.addEdge(arco);
		if (arco.getDestinationNode() instanceof ITabledVariable) {
			ITabledVariable v2 = (ITabledVariable) arco.getDestinationNode();
			PotentialTable auxTab = v2.getPotentialTable();
			auxTab.addVariable(arco.getOriginNode());
		}
	}

	/**
	 * Retorna o vetor de cópia dos nós
	 * (sem as variáveis de utilidade).
	 *
	 * @return vetor de cópia dos nós sem as variáveis de utilidade.
	 */
	public NodeList getCopiaNos() {
		return copiaNos;
	}

	/**
	 *  Retorna a probabilidade estimada total da árvore de junção associada.
	 *
	 *@return    probabilidade estimada total da árvore de junção associada.
	 */
	public float PET() {
		return junctionTree.getN();
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
		if (nos.size() == 0) {
			throw new Exception(resource.getString("EmptyNetException"));
		}
		if (createLog) {
			logManager.reset();
		}
		verifyConsistency();
		moraliza();
		triangula();
		compilaAJ();
	}

	/**
	 *  Chama o método da árvore de junção para atualizar evidências.
	 *  @return             consistência da árvore atualizada.
	 */
	public void updateEvidences() throws Exception {
		int sizeNos = copiaNos.size();
		for (int c = 0; c < sizeNos; c++) {
			TreeVariable node = (TreeVariable) copiaNos.get(c);
			node.updateEvidences();
		}

		try {
			junctionTree.consistencia();
		} catch (Exception e) {
			initialize();
			throw e;
		}
		//        resetEvidences();
		updateMarginais();
	}

	/**
	 * Inicia as crenças da árvore de junção.
	 */
	public void initialize() throws Exception {
		resetEvidences();
		junctionTree.iniciaCrencas();
		if (firstInitialization) {
			updateMarginais();
			copyMarginal();
			firstInitialization = false;
		} else {
			restoreMarginais();
		}
	}

	private void copyMarginal() {
		for (int i = 0; i < copiaNos.size(); i++) {
			TreeVariable node = (TreeVariable) copiaNos.get(i);
			node.copyMarginal();
		}
	}

	private void restoreMarginais() {
		for (int i = 0; i < copiaNos.size(); i++) {
			TreeVariable node = (TreeVariable) copiaNos.get(i);
			node.restoreMarginal();
		}
	}

	/**
	 * Gets the createLog.
	 * @return Returns a boolean
	 */
	public boolean isCreateLog() {
		return createLog;
	}

	/**
	 * Sets the firstInitialization.
	 * @param firstInitialization The firstInitialization to set
	 */
	public void setFirstInitialization(boolean firstInitialization) {
		this.firstInitialization = firstInitialization;
	}

}