/*
 *  UnbBayes
 *  Copyright (C) 2002 Universidade de Bras�lia
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
 *  Representa uma rede probabil�stica.
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
	 * N�s de decis�o utilizado no processo de transforma��o.
	 */
	private NodeList decisionNodes;

	private String nome = "";

	private HierarchicTree hierarchicTree;

	private boolean firstInitialization;

	/**
	 *  Cria uma nova rede probabil�stica. Limpa o arquivo de log e inicializa o
	 *  vetor da ordem de elimina��o.
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
	 *  Verifica integridade como grafo direcionado ac�clico / conexo e coes�o.
	 *  Com a sa�da � poss�vel saber quais erros especificamente ocorreram caso algum ocorra.
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
	 *  Faz o processo de triangula��o da rede.
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
	 *  Liga as duas vari�veis e toma as devidas provid�ncias.
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
	 *  Returna o n�mero de vari�veis da rede.
	 *
	 *@return    n�mero de vari�veis da rede.
	 */
	public int noVariaveis() {
		return nos.size();
	}

	/**
	 * Retorna o vetor de c�pia dos n�s
	 * (sem as vari�veis de utilidade).
	 *
	 * @return vetor de c�pia dos n�s sem as vari�veis de utilidade.
	 */
	public NodeList getCopiaNos() {
		return copiaNos;
	}

	/**
	 *  Retorna a probabilidade estimada total da �rvore de jun��o associada.
	 *
	 *@return    probabilidade estimada total da �rvore de jun��o associada.
	 */
	public float PET() {
		return junctionTree.getN();
	}

	/**
	 * Realiza todos os passos necess�rios para compilar uma rede em �rvore de jun��o. <br><br>
	 * Realiza os seguintes passos: <br>
	 * Verifica a consist�ncia. <br>
	 * Moraliza. <br>
	 * Triangula. <br>
	 * Compila �rvore de Jun��o.
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
	 *  Chama o m�todo da �rvore de jun��o para atualizar evid�ncias.
	 *  @return             consist�ncia da �rvore atualizada.
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
	 * Inicia as cren�as da �rvore de jun��o.
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
	 *  SUB-FUN��O do m�todo verificaConsist�ncia que verifica se
	 *  existe uma ordena��o total das decis�es. Isto �, se existe
	 *  um caminho orientado entre as decis�es.
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
	 *  SUB-FUN��O do m�todo verificaConsist�ncia que verifica se todos os
	 *  n�s de utilidade n�o cont�m filhos.
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
	 *  SUB-FUN��O do m�todo verificaConsist�ncia que verifica a consistencia
	 *  das tabelas de potenciais dos n�s do grafo.
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
	 *  SUB-FUN��O do m�todo triangula.
	 *
	 *@param  no  n�.
	 *@return     true - caso haja necessidade de inserir corda para poder eliminar
	 *      o n�. false - caso contr�rio.
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
	 *  SUB-FUN��O do m�todo pesoMinimo que utiliza a her�stica do peso m�nimo.
	 *
	 * @param  auxNos  n�s.
	 * @return         n� cujo conjunto formado por adjacentes possui peso m�nimo.
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
	 *  SUB-FUN��O do procedimento triangula que elimina n� e reduz o grafo. Inclui
	 *  cordas necess�rias para eliminar n�. Retira-o e aos adjacentes.
	 *
	 *@param  no      n� a ser eliminado
	 *@param  auxNos  lista de n�s
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
	 *  Sub-rotina do m�todo triangula.
	 *  Elimina os n�s do grafo utilizando a heur�stica do peso m�nimo.
	 *  Primeiramente elimina os n�s cujos adjacentes est�o ligados dois a dois.
	 *  Depois se ainda houver n�s no grafo, elimina-os aplicando a heur�stica
	 *  do peso m�nimo.
	 *
	 * @param  auxNos  Vetor de n�s.
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
					//N�o tem cordas necess�rias:teste pr�ximo.
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
			auxNo = peso(auxNos); //auxNo: clique de peso m�nimo.
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