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

	/** Load resource file from this package */
	private static ResourceBundle resource =
		ResourceBundle.getBundle("unbbayes.prs.bn.resources.BnResources");

	private double radius;

	/**
	 * Nós de decisão utilizado no processo de transformação.
	 */
	private NodeList decisionNodes;

	private String nome = "";

	private HierarchicTree hierarchicTree;

	private boolean firstInitialization;

	/**
	 *  Cria uma nova rede probabilística. Limpa o arquivo de log e inicializa o
	 *  vetor da ordem de eliminação.
	 */
	public ProbabilisticNetwork() {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
		DefaultTreeModel model = new DefaultTreeModel(root);
		hierarchicTree = new HierarchicTree(model);				
		oe = new NodeList();
		firstInitialization = true;
	}

	/**
	 * Seta o nome da rede.
	 *
	 * @param name nome da rede.
	 */
	public void setName(String name) {
		this.nome = name;
	}

	/**
	 * Retorna o nome da rede.
	 *
	 * @return nome da rede.
	 */
	public String getName() {
		return nome;
	}

	public void setHierarchicTree(HierarchicTree hierarchicTree) {
		this.hierarchicTree = hierarchicTree;
	}

	public HierarchicTree getHierarchicTree() {
		return hierarchicTree;
	}
	
	/*
	public int size() {
		return nos.size();
	}
	*/

	/**
	 *  Verifica integridade como grafo direcionado acíclico / conexo e coesão.
	 *  Com a saída é possível saber quais erros especificamente ocorreram caso algum ocorra.
	 */
	private void verificaConsistencia() throws Exception {
		if (nos.size() != 0) {
			boolean erro = false;

			StringBuffer sb = new StringBuffer();

			try {
				verificaUtilidade();
			} catch (Exception e) {
				erro = true;
				sb.append(e.getMessage());
			}
			try {
				verifyCycles();
			} catch (Exception e) {
				erro = true;
				sb.append('\n' + e.getMessage());
			}
			try {
				verifyConectivity();
			} catch (Exception e) {
				erro = true;
				sb.append('\n' + e.getMessage());
			}
			try {
				verificaTabelasPot();
			} catch (Exception e) {
				erro = true;
				sb.append('\n' + e.getMessage());
			}
			try {
				sortDecisions();
			} catch (Exception e) {
				erro = true;
				sb.append('\n' + e.getMessage());
			}

			if (erro) {
				throw new Exception(sb.toString());
			}
		}
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

		while (auxNos.size() != 0) {
			pesoMinimo(auxNos);
		}

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

			while (auxNos.size() != 0) {
				pesoMinimo(auxNos);
			}
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
	 *  Returna o número de variáveis da rede.
	 *
	 *@return    número de variáveis da rede.
	 */
	public int noVariaveis() {
		return nos.size();
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
		verificaConsistencia();
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
	 *  SUB-FUNÇÃO do método verificaConsistência que verifica se
	 *  existe uma ordenação total das decisões. Isto é, se existe
	 *  um caminho orientado entre as decisões.
	 */
	private void sortDecisions() throws Exception {
		decisionNodes = new NodeList();
		int sizeNos = nos.size();
		for (int i = 0; i < sizeNos; i++) {
			if (nos.get(i).getType() == Node.DECISION_NODE_TYPE) {
				decisionNodes.add(nos.get(i));
			}
		}

		NodeList fila = new NodeList(nos.size());
		Node aux, aux2, aux3;

		int sizeDecisao = decisionNodes.size();
		for (int i = 0; i < sizeDecisao; i++) {
			boolean visitados[] = new boolean[nos.size()];
			aux = (Node) decisionNodes.get(i);
			fila.clear();
			fila.add(aux);

			while (fila.size() != 0) {
				aux2 = fila.remove(0);
				visitados[nos.indexOf(aux2)] = true;

				int sizeFilhos = aux2.getChildren().size();
				for (int k = 0; k < sizeFilhos; k++) {
					aux3 = (Node) aux2.getChildren().get(k);
					if (!visitados[nos.indexOf(aux3)]) {
						if (aux3.getType() == Node.DECISION_NODE_TYPE
							&& !aux.getAdjacents().contains(aux3)) {
							aux.getAdjacents().add(aux3);
						}
						fila.add(aux3);
					}
				}
			}
		}

		boolean haTroca = true;
		while (haTroca) {
			haTroca = false;
			for (int i = 0; i < decisionNodes.size() - 1; i++) {
				Node node1 = decisionNodes.get(i);
				Node node2 = decisionNodes.get(i + 1);
				if (node1.getAdjacents().size()
					< node2.getAdjacents().size()) {
					decisionNodes.set(i + 1, node1);
					decisionNodes.set(i, node2);
					haTroca = true;
				}
			}
		}

		int sizeDecisao1 = decisionNodes.size();
		for (int i = 0; i < sizeDecisao1; i++) {
			System.out.print(decisionNodes.get(i) + " ");
		}
		System.out.println();

		for (int i = 0; i < decisionNodes.size(); i++) {
			aux = decisionNodes.get(i);
			//            System.out.print(aux.getAdjacents().size() + " ");
			if (aux.getAdjacents().size() != decisionNodes.size() - i - 1) {
				throw new Exception(
					resource.getString("DecisionOrderException"));
			}
		}

		desmontaAdjacentes();
	}

	/**
	 *  SUB-FUNÇÃO do método verificaConsistência que verifica se todos os
	 *  nós de utilidade não contém filhos.
	 */
	private void verificaUtilidade() throws Exception {
		Node aux;

		int sizeNos = nos.size();
		for (int i = 0; i < sizeNos; i++) {
			aux = (Node) nos.get(i);
			if (aux.getType() == Node.UTILITY_NODE_TYPE
				&& aux.getChildren().size() != 0) {
				throw new Exception(
					resource.getString("variableName")
						+ aux
						+ resource.getString("hasChildName"));
			}
		}
	}

	/**
	 *  SUB-FUNÇÃO do método verificaConsistência que verifica a consistencia
	 *  das tabelas de potenciais dos nós do grafo.
	 */
	private void verificaTabelasPot() throws Exception {
		ProbabilisticTable auxTabPot;
		int c;
		Node auxNo;
		ProbabilisticNode auxVP;

		int sizeNos = nos.size();
		for (c = 0; c < sizeNos; c++) {
			auxNo = nos.get(c);
			if (auxNo.getType() == Node.PROBABILISTIC_NODE_TYPE) {
				auxVP = (ProbabilisticNode) auxNo;
				auxTabPot = (ProbabilisticTable) auxVP.getPotentialTable();
				auxTabPot.verificaConsistencia();
			}
		}
	}

	/**
	 *  SUB-FUNÇÃO do método triangula.
	 *
	 *@param  no  nó.
	 *@return     true - caso haja necessidade de inserir corda para poder eliminar
	 *      o nó. false - caso contrário.
	 */
	private boolean cordas(Node no) {
		if (no.getAdjacents().size() < 2) {
			return false;
		}

		int sizeAdjacentes = no.getAdjacents().size();
		for (int i = 0; i < sizeAdjacentes - 1; i++) {
			Node auxNo1 = no.getAdjacents().get(i);

			for (int j = i + 1; j < sizeAdjacentes; j++) {
				Node auxNo2 = no.getAdjacents().get(j);
				if (!auxNo2.getAdjacents().contains(auxNo1)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 *  SUB-FUNÇÃO do método pesoMinimo que utiliza a herística do peso mínimo.
	 *
	 * @param  auxNos  nós.
	 * @return         nó cujo conjunto formado por adjacentes possui peso mínimo.
	 */
	private Node peso(NodeList auxNos) {
		Node v;
		Node auxNo;
		double p;

		Node noMin = auxNos.get(0);
		double pmin = Math.log(noMin.getStatesSize());

		int sizeAdjacentes = noMin.getAdjacents().size();
		for (int i = 0; i < sizeAdjacentes; i++) {
			v = noMin.getAdjacents().get(i);
			pmin += Math.log(v.getStatesSize());
		}

		int sizeNos = auxNos.size();
		for (int i = 1; i < sizeNos; i++) {
			auxNo = auxNos.get(i);
			p = Math.log(auxNo.getStatesSize());

			int sizeAdjacentes1 = auxNo.getAdjacents().size();
			for (int j = 0; j < sizeAdjacentes1; j++) {
				v = auxNo.getAdjacents().get(j);
				p += Math.log(v.getStatesSize());
			}
			if (p < pmin) {
				pmin = p;
				noMin = auxNo;
			}
		}
		return noMin;
	}

	/**
	 *  SUB-FUNÇÃO do procedimento triangula que elimina nó e reduz o grafo. Inclui
	 *  cordas necessárias para eliminar nó. Retira-o e aos adjacentes.
	 *
	 *@param  no      nó a ser eliminado
	 *@param  auxNos  lista de nós
	 */
	private void elimine(Node no, NodeList auxNos) {
		Node auxNo1;
		Node auxNo2;
		Edge auxArco;

		int sizeAdjacentes = no.getAdjacents().size();

		for (int i = 0; i < sizeAdjacentes - 1; i++) {
			auxNo1 = no.getAdjacents().get(i);

			for (int j = i + 1; j < sizeAdjacentes; j++) {
				auxNo2 = no.getAdjacents().get(j);
				if (!auxNo2.getAdjacents().contains(auxNo1)) {
					auxArco = new Edge(auxNo1, auxNo2);
					if (createLog) {
						logManager.append(
							auxNo1.getName()
								+ resource.getString("linkedName")
								+ auxNo2.getName()
								+ "\n");
					}
					arcosMarkov.add(auxArco);
					auxNo1.getAdjacents().add(auxNo2);
					auxNo2.getAdjacents().add(auxNo1);
				}
			}
		}

		int sizeAdjacentes1 = no.getAdjacents().size();
		for (int i = 0; i < sizeAdjacentes1; i++) {
			auxNo1 = no.getAdjacents().get(i);
			auxNo1.getAdjacents().remove(no);
		}
		auxNos.remove(no);
	}

	/**
	 *  Sub-rotina do método triangula.
	 *  Elimina os nós do grafo utilizando a heurística do peso mínimo.
	 *  Primeiramente elimina os nós cujos adjacentes estão ligados dois a dois.
	 *  Depois se ainda houver nós no grafo, elimina-os aplicando a heurística
	 *  do peso mínimo.
	 *
	 * @param  auxNos  Vetor de nós.
	 */
	private void pesoMinimo(NodeList auxNos) {
		boolean algum;
		Node auxNo;
		Node v;

		algum = true;
		while (algum) {
			algum = false;

			int sizeNos = auxNos.size();
			for (int i = 0; i < sizeNos; i++) {
				auxNo = auxNos.get(i);

				if (cordas(auxNo)) {
					//Não tem cordas necessárias:teste próximo.
					continue;
				}

				int sizeAdjacentes = auxNo.getAdjacents().size();
				for (int j = 0; j < sizeAdjacentes; j++) {
					v = auxNo.getAdjacents().get(j);
					v.getAdjacents().remove(auxNo);
				}
				auxNos.remove(auxNo);
				algum = true;
				oe.add(auxNo);
				if (createLog) {
					logManager.append(
						"\t" + oe.size() + " " + auxNo.getName() + "\n");
				}
				break;
			}
		}

		if (auxNos.size() > 0) {
			auxNo = peso(auxNos); //auxNo: clique de peso mínimo.
			oe.add(auxNo);
			if (createLog) {
				logManager.append(
					"\t" + oe.size() + " " + auxNo.getName() + "\n");
			}
			elimine(auxNo, auxNos); //Elimine no e reduza grafo.
		}
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
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