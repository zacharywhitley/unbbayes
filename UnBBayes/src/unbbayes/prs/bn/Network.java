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

import java.io.IOException;
import java.util.*;

import javax.swing.JTree;
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
 *  Classe que representa um Grafo genérico.
 *
 *@author     Michael e Rommel
 *@version    21 de Setembro de 2001
 */
public class Network implements java.io.Serializable {

	/** Load resource file from this package */
  	protected static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.prs.bn.resources.BnResources");
  	
  	protected HierarchicTree hierarchicTree;
  	
  	protected boolean firstInitialization;
  	
  	/**
	 * Nós de decisão utilizado no processo de transformação.
	 */
	protected NodeList decisionNodes;
	
	protected String id;

	protected String name;

	protected double radius;

    /**
     *  Lista de nós que compõem o grafo.
     */
    protected NodeList nos;
    //protected NodeList descriptionNodes;
    //protected NodeList explanationNodes;

    /**
     *  Lista de arcos que compõem o grafo.
     */
    protected List arcos;
    
    
    /**
	 * Faz o processamento do log de compilação.
	 */
	protected LogManager logManager;

	/**
	 *  Lista de arcos utilizada no processo de transformação.
	 */
	protected List arcosMarkov;
	
	/**
	 * Indica se o log deve ser criado ou não.
	 */
	protected boolean createLog;
           
    /**
	 *  Ordem de eliminação dos nós.
	 */
	protected NodeList oe;

	/**
	 * Cópia dos nós sem os nós de utilidade. Utilizado no processo
	 * de transformação.
	 */
	protected NodeList copiaNos;
	
	protected List copiaArcos;

	/**
	 *  Armazena handle do objeto Árvore de Junção associado ao Grafo.
	 */
	protected JunctionTree junctionTree;	
    
    private Map nodeIndexes;


    /**
     *  Constrói um novo grafo sem nós nem arcos.
     */
    public Network(String id) {
        nos = new NodeList();
        assert ! id.trim().equals("");
        this.id = id;
        //descriptionNodes = new NodeList();
        //explanationNodes = new NodeList();
        arcos = new ArrayList();
        arcosMarkov = new ArrayList();
        logManager = new LogManager();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
		DefaultTreeModel model = new DefaultTreeModel(root);
        hierarchicTree = new HierarchicTree(model);        
        nodeIndexes = new HashMap();
    }


    /**
     *  Retorna os arcos do grafo.
     *
     *@return    arcos do grafo.
     */
    public List getArcos() {
        return this.arcos;
    }


    /**
     *  Retorna os nós do grafo.
     *
     *@return    nós do grafo.
     * 
     * @todo Eliminar esse metodo! eh utilizado na classe NetWindow
     */
    public NodeList getNos() {
        return this.nos;
    }
    
    /**
	 *  Returna o número de variáveis da rede.
	 *
	 *@return    número de variáveis da rede.
	 */
    public int getNodeCount() {
    	return nos.size();    	
    }


    /**
     *  Retorna o nó do grafo com o respectivo índice.
     *
     *@param  index  índice do nó.
     *@return	nó com respectivo índice no List.
     */
    public Node getNodeAt(int index) {
        return nos.get(index);
    }


    public NodeList getDescriptionNodes()
    {   NodeList descriptionNodes = new NodeList();
        int size = nos.size();
        for (int i=0;i<size;i++)
        {   Node node = getNodeAt(i);
            if ((node.getType() == Node.PROBABILISTIC_NODE_TYPE) && (node.getInformationType() == Node.DESCRIPTION_TYPE))
            {   descriptionNodes.add(node);
            }
        }
        return descriptionNodes;
    }

    public NodeList getExplanationNodes()
    {   NodeList explanationNodes = new NodeList();
        int size = nos.size();
        for (int i=0;i<size;i++)
        {   Node node = getNodeAt(i);
            if ((node.getType() == Node.PROBABILISTIC_NODE_TYPE) && (node.getInformationType() == Node.EXPLANATION_TYPE))
            {   explanationNodes.add(node);
            }
        }
        return explanationNodes;
    }

    /**
     *  Retorna o nó do grafo com a respectiva sigla.
     *
     *@param  name  nome do nó.
     *@return       nó com a respectiva sigla.
     */
    public Node getNode(String name) {
    	int index = getNodeIndex(name);
    	if (index == -1) return null;
    	return nos.get(index);
    }
    
    /**
     * @todo prever o caso de mudanca de nome de nos.
     */

    public int getNodeIndex(String name) {
    	Object index = nodeIndexes.get(name);
    	if (index == null) {
    		return -1;    		
    	}
    	return ((Integer) index).intValue();
    	/*
    	int size = nos.size();
        for (int qnos = 0; qnos < size; qnos++) {
            if (((nos.get(qnos))).getName().equals(name)) {
                return qnos;
            }
        }
        return -1;*/
    }

    /**
     *  Retira do grafo o arco especificado.
     *
     *@param  arco  arco a ser retirado.
     */
    public void removeEdge(Edge arco) {
        arco.getOriginNode().getChildren().remove(arco.getDestinationNode());
        arco.getDestinationNode().getParents().remove(arco.getOriginNode());
        removeArco(arco);
    }


    /**
     *  Adiciona novo nó ao grafo.
     *
     *@param  no  nó a ser inserido.
     */
    public void addNode(Node no) {
        nos.add(no);
        nodeIndexes.put(no.getName(), new Integer(nos.size()-1));
        /*if (no.getInformationType() == Node.EXPLANATION_TYPE)
        {   explanationNodes.add(no);
        }
        else
        {   descriptionNodes.add(no);
        }*/
    }


    /**
     *  Adiciona o arco à rede.
     *
     *@param  arco  arco a ser inserido.
     */
    public void addEdge(Edge arco) {
        arco.getOriginNode().getChildren().add(arco.getDestinationNode());
        arco.getDestinationNode().getParents().add(arco.getOriginNode());
        arcos.add(arco);
        if (arco.getDestinationNode() instanceof ITabledVariable) {
			ITabledVariable v2 = (ITabledVariable) arco.getDestinationNode();
			PotentialTable auxTab = v2.getPotentialTable();
			auxTab.addVariable(arco.getOriginNode());
		}
    }


    /**
     *  Remove nó do grafo.
     *
     *@param  elemento  no a ser removido.
     */
    public void removeNode(Node elemento) {
        int c;
        Node auxNo;
        Edge auxArco;
        
        nos.remove(elemento);
        
        nodeIndexes.remove(elemento.getName());
        /*if (elemento.getInformationType() == Node.EXPLANATION_TYPE)
        {   explanationNodes.remove(elemento);
        }
        else
        {   descriptionNodes.remove(elemento);
        }*/
        for (c = 0; c < nos.size(); c++) {
            auxNo = nos.get(c);
            auxNo.getParents().remove(elemento);
            auxNo.getChildren().remove(elemento);
        }
        if (!arcos.isEmpty()) {
            auxArco = (Edge) arcos.get(0);
            c = 0;
            while (auxArco != arcos.get(arcos.size() - 1)) {
                if ((auxArco.getOriginNode() == elemento) || (auxArco.getDestinationNode() == elemento)) {
                    removeArco(auxArco);
                }
                else {
                    c++;
                }
                auxArco = (Edge) arcos.get(c);
            }
            if ((auxArco.getOriginNode() == elemento) || (auxArco.getDestinationNode() == elemento)) {
                removeArco(auxArco);
            }
        }
    }


    /**
     *  Remove arco do grafo.
     *
     *@param  elemento  arco a ser removido
     */
    private void removeArco(Edge elemento) {
        Node auxNo;
        ITabledVariable auxVTab;
        PotentialTable auxTP;

        arcos.remove(elemento);

        auxNo = elemento.getDestinationNode();
        if (auxNo instanceof ITabledVariable) {
            auxVTab = (ITabledVariable)auxNo;
            auxTP = auxVTab.getPotentialTable();
            auxTP.removeVariable(elemento.getOriginNode());
        }
    }


    /**
     *  Percorre lista de nós e em cada nó faz uma cópia das referências dos pais e
     *  filhos para uma lista de adjacentes do nó.
     */
    protected void montaAdjacentes() {
        this.desmontaAdjacentes();
        for (int qnos = 0; qnos < nos.size(); qnos++) {
            nos.get(qnos).montaAdjacentes();
        }
    }
    
    
    protected void makeAdjacents() {
    	desmontaAdjacentes();
    	for (int z = arcosMarkov.size() - 1; z >= 0; z--) {
			Edge auxArco = (Edge) arcosMarkov.get(z);
			auxArco.getOriginNode().getAdjacents().add(
				auxArco.getDestinationNode());
			auxArco.getDestinationNode().getAdjacents().add(
				auxArco.getOriginNode());
		}
	
		for (int z = copiaArcos.size() - 1; z >= 0; z--) {
			Edge auxArco = (Edge) copiaArcos.get(z);
			if (auxArco.getDestinationNode().getType()
				== Node.UTILITY_NODE_TYPE) {
				copiaArcos.remove(z);
			} else {
				auxArco.getOriginNode().getAdjacents().add(
					auxArco.getDestinationNode());
				auxArco.getDestinationNode().getAdjacents().add(
					auxArco.getOriginNode());
			}
		}
//		arcosMarkov = SetToolkit.union(arcosMarkov, edges);    	
    }


    /**
     *  Destrói a lista de adjacentes de cada nó do grafo.
     */
    protected void desmontaAdjacentes() {
    	int size = nos.size();
        for (int qnos = 0; qnos < size; qnos++) {
            nos.get(qnos).desmontaAdjacentes();
        }
    }


    /**
     *  Limpa a lista de nós.
     */
    protected void limpaNos() {
        nos.clear();
        //explanationNodes.clear();
        //descriptionNodes.clear();
    }


    /**
     *  Limpa a lista de arcos.
     */
    protected void limpaArcos() {
        arcos.clear();
    }
    
    
    
    /*
     *  Verificação através da eliminação dos nós que não são pais e filhos
     *  de alguém ao mesmo tempo. Há a necessidade de trabalhar com uma cópia
     *  da lista de nós da rede.
     *
     *@throws Exception se a rede possui ciclo.
    public final void vC1() throws Exception {
        Node auxNo1;
        Node auxNo2;
        boolean existeRetirada;
        int i;
        int j;
        int n;
        int l;
        int m;
        List listaPais = new ArrayList();
        List listaFilhos = new ArrayList();
        NodeList listaCloneNos = SetToolkit.clone(nos);

        if (listaCloneNos.size() != 0) {
            for (i = 0; i < listaCloneNos.size(); i++) {
                auxNo1 = listaCloneNos.get(i);
                listaPais.add(SetToolkit.clone(auxNo1.getParents()));
                listaFilhos.add(SetToolkit.clone(auxNo1.getChildren()));
            }
            auxNo1 = listaCloneNos.get(0);
            existeRetirada = false;
            if (auxNo1 != null) {
                existeRetirada = true;
            }
            while (existeRetirada) {
                l = 0;
                m = 0;
                while (auxNo1 != null) {
                    existeRetirada = false;
                    if ((auxNo1.getParents().size() == 0) || (auxNo1.getChildren().size() == 0)) {
                        listaCloneNos.remove(auxNo1);
                        existeRetirada = true;
                        for (j = 0; j < listaCloneNos.size(); j++) {
                            auxNo2 = listaCloneNos.get(j);
                            if (auxNo2.getParents().contains(auxNo1)) {
                                auxNo2.getParents().remove(auxNo1);
                                auxNo1.getChildren().remove(auxNo2);
                            }
                            if (auxNo2.getChildren().contains(auxNo1)) {
                                auxNo2.getChildren().remove(auxNo1);
                                auxNo1.getParents().remove(auxNo2);
                            }
                        }
                    }
                    n = listaCloneNos.size();
                    if (!existeRetirada) {
                        m++;
                    } else {
                        m = 0;
                    }
                    if (m > n) {
                        auxNo1 = null;
                        existeRetirada = false;
                        break;
                    }
                    if (l < n - 1) {
                        l++;
                    }
                    else {
                        l = 0;
                    }
                    if (n > 0) {
                        auxNo1 = listaCloneNos.get(l);
                    }
                    else {
                        auxNo1 = null;
                        existeRetirada = false;
                    }
                }
            }

            for (i = 0; i < nos.size(); i++) {
                auxNo1 = nos.get(i);
                auxNo1.setParents((NodeList) listaPais.get(i));
                auxNo1.setChildren((NodeList) listaFilhos.get(i));
            }
            listaPais.clear();
            listaFilhos.clear();

            if (listaCloneNos.size() != 0) {
                StringBuffer sb = new StringBuffer(resource.getString("CicleNetException"));
                for (i = 0; i < listaCloneNos.size(); i++) {
                   auxNo1 = listaCloneNos.get(i);
                   sb.append(" " + auxNo1.getName());
                }
                throw new Exception(sb.toString());
            }
        }
    }
    */
    
    /**
     *  Verify if this network has cycle.
     *
     *@throws Exception If this network has a cycle.
     */
    public final void verifyCycles() throws Exception {
    	int nodeSize = nos.size();
    	char[] visited = new char[nodeSize];
    	int[] pi = new int[nodeSize];
    	
    	for (int i = 0; i < nodeSize; i++) {
    		dfsCycle(i, visited, pi);
    	}
    }
    
    /**
     * Depth first search to verify cycle.
     */
    private void dfsCycle(int nodeIndex, char[] visited, int[] pi) throws Exception {
    	if (visited[nodeIndex] != 0) { 			
 			// Back edge. Has cycle!
    		if (visited[nodeIndex] == 1) {
                throw new Exception(resource.getString("CicleNetException") 
                					 + " " + createPath(nodeIndex, nodeIndex, pi, true));
    		}
    		return;    		
    	}
    	
    	visited[nodeIndex] = 1;    	
    	Node node = nos.get(nodeIndex);    	
    	for (int i = node.getChildren().size()-1; i >= 0; i--) {
    		int newIndex = getNodeIndex(node.getChildren().get(i).getName());
    		pi[newIndex] = nodeIndex; 
    		dfsCycle(newIndex, visited, pi);
    	}
    	visited[nodeIndex] = 2;
    }
    
    /**
     * Auxiliary method for dfsCycle() to construct the path of the cycle detected. 
     */
    private String createPath(int currentIndex, int nodeIndex, int[] pi, boolean first) {
    	if (currentIndex == nodeIndex && ! first) {
			return nos.get(currentIndex).getName();
    	}
    	return createPath(pi[currentIndex], nodeIndex, pi, false) + " " + nos.get(currentIndex).getName();
    }
    

    /**
     *  Verifica a conectividade da rede.
     *
     *  @throws Exception se a rede for disconexa.
     */
    public final void verifyConectivity() throws Exception {
        List visitados = new ArrayList(nos.size());
        if (nos.size() <= 1) {
            return;
        }
        montaAdjacentes();
        dfsConnectivity(nos.get(0), visitados);
        desmontaAdjacentes();
        if (visitados.size() != nos.size()) {
            throw new Exception(resource.getString("DisconectedNetException"));
        }
    }

    /**
     * Depth first search to verify conectivity.
     */
    private void dfsConnectivity(Node no, List visitados) {
        visitados.add(no);
        for (int i = 0; i < no.getAdjacents().size(); i++) {
            Node aux = no.getAdjacents().get(i);
            if (! visitados.contains(aux)) {
                dfsConnectivity(aux, visitados);
            }
        }
    }

	/**
	 *  Faz o processo de moralização da rede.
	 */
	protected void moraliza() {
		Node auxNo;
		Node auxPai1;
		Node auxPai2;
		Edge auxArco;
		
		desmontaAdjacentes();
	
		if (createLog) {
			logManager.append(resource.getString("moralizeLabel"));
		}
		arcosMarkov.clear();
		copiaArcos = SetToolkit.clone(arcos);
	
		//retira os arcos de informação
		int sizeArcos = copiaArcos.size() - 1;
		for (int i = sizeArcos; i >= 0; i--) {
			auxArco = (Edge) copiaArcos.get(i);
			if (auxArco.getDestinationNode().getType()
				== Node.DECISION_NODE_TYPE) {
				copiaArcos.remove(i);
			}
		}
	
		int sizeNos = nos.size();
		for (int n = 0; n < sizeNos; n++) {
			auxNo = nos.get(n);
			if (!(auxNo.getType() == Node.DECISION_NODE_TYPE)
				&& auxNo.getParents().size() > 1) {
				int sizePais = auxNo.getParents().size();
				for (int j = 0; j < sizePais - 1; j++) {
					auxPai1 = auxNo.getParents().get(j);
					for (int k = j + 1; k < sizePais; k++) {
						auxPai2 = auxNo.getParents().get(k);
						if ((existeArco(auxPai1, auxPai2, copiaArcos) == -1)
							&& (existeArco(auxPai1, auxPai2, arcosMarkov) == -1)) {
							auxArco = new Edge(auxPai1, auxPai2);
							if (createLog) {
								logManager.append(
									auxPai1.getName()
										+ " - "
										+ auxPai2.getName()
										+ "\n");
							}
							arcosMarkov.add(auxArco);
						}
					}
				}
			}
		}
		
		makeAdjacents();
		
		if (createLog) {
			logManager.append("\n");
		}
	}

	/**
	 *  Verifica existência de determinado arco.
	 *
	 *@param  no1  nó origem.
	 *@param  no2  nó destino.
	 *@return      posição do arco no vetor ou -1 caso não exista tal arco.
	 */
	public int existeArco(Node no1, Node no2) {
		return existeArco(no1, no2, arcos);
	}

	private int existeArco(Node no1, Node no2, List vetArcos) {
		if (no1 == no2) {
			return 1;
		}
	
		int sizeArcos = vetArcos.size();
		Edge auxA;
		for (int i = 0; i < sizeArcos; i++) {
			auxA = (Edge) vetArcos.get(i);
			if ((auxA.getOriginNode() == no1)
				&& (auxA.getDestinationNode() == no2)
				|| (auxA.getOriginNode() == no2)
				&& (auxA.getDestinationNode() == no1)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Sets the createLog.
	 * @param createLog The createLog to set
	 */
	public void setCreateLog(boolean createLog) {
		this.createLog = createLog;
	}

	/**
	 *  Monta árvore de junção a partir do grafo.
	 */
	protected void compilaAJ(JunctionTree jt) throws Exception {
		int menor;
		Clique auxClique;
		Separator auxSep;
	
		resetEvidences();
		
		junctionTree = jt;	
		
		this.cliques();
		this.arvoreForte();
		this.sortCliqueNodes();
		this.associaCliques();
		junctionTree.iniciaCrencas();
	
		int sizeNos = copiaNos.size();
		for (int c = 0; c < sizeNos; c++) {
			Node auxNode = copiaNos.get(c);
			menor = Integer.MAX_VALUE;
			if (auxNode.getType() == Node.PROBABILISTIC_NODE_TYPE) {
				int sizeSeparadores = junctionTree.getSeparatorsSize();
				for (int c2 = 0; c2 < sizeSeparadores; c2++) {
					auxSep = (Separator) junctionTree.getSeparatorAt(c2);
					if (auxSep.getNos().contains(auxNode)
						&& (auxSep.getPotentialTable().tableSize() < menor)) {
						((ProbabilisticNode) auxNode).setAssociatedClique(
							auxSep);
						menor = auxSep.getPotentialTable().tableSize();
					}
				}
			}
	
			if (menor == Integer.MAX_VALUE) {
				int sizeCliques = junctionTree.getCliques().size();
				for (int c2 = 0; c2 < sizeCliques; c2++) {
					auxClique = (Clique) junctionTree.getCliques().get(c2);
					if (auxClique.getNos().contains(auxNode)
						&& (auxClique.getPotentialTable().tableSize() < menor)) {
						if (auxNode.getType()
							== Node.PROBABILISTIC_NODE_TYPE) {
							((ProbabilisticNode) auxNode).setAssociatedClique(
								auxClique);
						} else {
							((DecisionNode) auxNode).setAssociatedClique(
								auxClique);
							break;
						}
						menor = auxClique.getPotentialTable().tableSize();
					}
				}
			}
		}
	
		updateMarginais();
	
		if (createLog) {
			Thread t = new Thread(new Runnable() {
				public void run() {
					makeLog();
					System.out.println("**Log ended**");
				}
			});
			t.setPriority(Thread.MIN_PRIORITY);
			t.start();
		}
	}

	protected void resetEvidences() {
		int size = copiaNos.size();
		for (int i = 0; i < size; i++) {
			((TreeVariable) copiaNos.get(i)).resetEvidence();
		}
	}

	/**
	 * Returns true if this network is a Influence Diagram
	 * @return Returns true if this network is a Influence Diagram, false otherwise.
	 */
	public boolean isID() {
		for (int i = 0; i < nos.size(); i++) {
			if (nos.get(i).getType() == Node.DECISION_NODE_TYPE
				|| nos.get(i).getType() == Node.UTILITY_NODE_TYPE) {
	
				return true;
			}
		}
		return false;
	}

	protected void updateMarginais() {
		for (int i = 0; i < copiaNos.size(); i++) {
			TreeVariable node = (TreeVariable) copiaNos.get(i);
			node.marginal();
		}
	}

	/**
	 *  Faz o processo de identificação dos Cliques
	 */
	protected void cliques() {
		int i;
		int j;
		Node auxNo;
		Node auxNo2;
		int e;
		Clique auxClique;
		Clique auxClique2;
		List listaCliques = new ArrayList();
	
		int sizeNos = copiaNos.size();
		for (i = 0; i < sizeNos; i++) {
			auxNo = copiaNos.get(i);
			e = oe.indexOf(auxNo);
			auxClique = new Clique();
			auxClique.getNos().add(auxNo);
	
			int sizeAdjacentes = auxNo.getAdjacents().size();
			for (j = 0; j < sizeAdjacentes; j++) {
				auxNo2 = auxNo.getAdjacents().get(j);
				if (oe.indexOf(auxNo2) > e) {
					auxClique.getNos().add(auxNo2);
				}
			}
			listaCliques.add(auxClique);
		}
	
		boolean haTroca = true;
		while (haTroca) {
			haTroca = false;
			for (i = 0; i < listaCliques.size() - 1; i++) {
				auxClique = (Clique) listaCliques.get(i);
				auxClique2 = (Clique) listaCliques.get(i + 1);
				if (auxClique.getNos().size() > auxClique2.getNos().size()) {
					listaCliques.set(i + 1, auxClique);
					listaCliques.set(i, auxClique2);
					haTroca = true;
				}
			}
		}
	
		int sizeCliques = listaCliques.size();
	
		for1 : for (i = 0; i < sizeCliques; i++) {
			auxClique = (Clique) listaCliques.get(i);
			for (j = i + 1; j < sizeCliques; j++) {
				auxClique2 = (Clique) listaCliques.get(j);
	
				if (auxClique2.getNos().containsAll(auxClique.getNos())) {
					continue for1;
				}
			}
			junctionTree.getCliques().add(auxClique);
		}
		listaCliques.clear();
	}

	/**
	 * Ordena os nós dos cliques e dos separadores de acordo com a ordem de eliminação.
	 */
	protected void sortCliqueNodes() {
		List listaCliques = junctionTree.getCliques();
		for (int k = 0; k < listaCliques.size(); k++) {
			Clique clique = (Clique) listaCliques.get(k);
			NodeList nosClique = clique.getNos();
			boolean haTroca = true;
			while (haTroca) {
				haTroca = false;
				for (int i = 0; i < nosClique.size() - 1; i++) {
					Node node1 = nosClique.get(i);
					Node node2 = nosClique.get(i + 1);
					if (oe.indexOf(node1) > oe.indexOf(node2)) {
						nosClique.set(i + 1, node1);
						nosClique.set(i, node2);
						haTroca = true;
					}
				}
			}
		}
	
		for (int k = junctionTree.getSeparatorsSize() - 1; k >= 0; k--) {
			Separator separator = (Separator) junctionTree.getSeparatorAt(k);
			NodeList nosSeparator = separator.getNos();
			boolean haTroca = true;
			while (haTroca) {
				haTroca = false;
				for (int i = 0; i < nosSeparator.size() - 1; i++) {
					Node node1 = nosSeparator.get(i);
					Node node2 = nosSeparator.get(i + 1);
					if (oe.indexOf(node1) > oe.indexOf(node2)) {
						nosSeparator.set(i + 1, node1);
						nosSeparator.set(i, node2);
						haTroca = true;
					}
				}
			}
		}
	}

	protected void makeLog() {
		long in = System.currentTimeMillis();
		try {
			logManager.finishLog(junctionTree, nos);
			logManager.writeToDisk(LogManager.DEFAULT_FILENAME, false);
		} catch (java.io.IOException ioe) {
			System.err.println(ioe.getMessage());
		}
		System.out.println(
			"GERACAO DO ARQUIVO LOG em "
				+ (System.currentTimeMillis() - in)
				+ " ms");
	}

	/**
	 *  Faz a associação dos Nós a um único clique com menos espaço de est. que
	 *  contenha sua família
	 */
	protected void associaCliques() {
		int min;
		Node auxNo;
		PotentialTable auxTabPot, auxUtilTab;
		Clique auxClique;
		Clique cliqueMin = null;
	
		for (int i = junctionTree.getCliques().size() - 1; i >= 0; i--) {
			auxClique = (Clique) junctionTree.getCliques().get(i);
			auxTabPot = auxClique.getPotentialTable();
			auxUtilTab = auxClique.getUtilityTable();
	
			int sizeNos = auxClique.getNos().size();
			for (int c = 0; c < sizeNos; c++) {
				auxTabPot.addVariable(auxClique.getNos().get(c));
				auxUtilTab.addVariable(auxClique.getNos().get(c));
			}
		}
	
		for (int k = junctionTree.getSeparatorsSize() - 1; k >= 0; k--) {
			Separator auxSep = (Separator) junctionTree.getSeparatorAt(k);
			auxTabPot = auxSep.getPotentialTable();
			auxUtilTab = auxSep.getUtilityTable();
			int sizeNos = auxSep.getNos().size();
			for (int c = 0; c < sizeNos; c++) {
				auxTabPot.addVariable(auxSep.getNos().get(c));
				auxUtilTab.addVariable(auxSep.getNos().get(c));
			}
		}
	
		int sizeNos = nos.size();
		for (int n = 0; n < sizeNos; n++) {
			if (nos.get(n).getType() == Node.DECISION_NODE_TYPE) {
				continue;
			}
	
			min = Integer.MAX_VALUE;
			auxNo = nos.get(n);
	
			int sizeCliques = junctionTree.getCliques().size();
			for (int c = 0; c < sizeCliques; c++) {
				auxClique = (Clique) junctionTree.getCliques().get(c);
	
				if (auxClique.getPotentialTable().tableSize() < min
					&& auxClique.getNos().containsAll(auxNo.getParents())) {
					if (auxNo.getType() == Node.PROBABILISTIC_NODE_TYPE
						&& !auxClique.getNos().contains(auxNo)) {
						continue;
					}
					cliqueMin = auxClique;
					min = cliqueMin.getPotentialTable().tableSize();
				}
			}
	
			if (auxNo.getType() == Node.PROBABILISTIC_NODE_TYPE) {
				cliqueMin.getAssociatedProbabilisticNodes().add(auxNo);
			} else {
				cliqueMin.getAssociatedUtilityNodes().add(auxNo);
			}
		}
	}

	/**
	 *  Faz o processo de constituição da árvore de junção - Frank Jensen
	 */
	protected void arvoreForte() {
		int ndx;
		Clique auxClique;
		Clique auxClique2;
		NodeList uni;
		NodeList inter;
		NodeList auxList;
		NodeList listaNos;
		Separator sep;
		NodeList alpha = new NodeList();
	
		for (int i = oe.size() - 1; i >= 0; i--) {
			alpha.add(oe.get(i));
		}
	
		if (copiaNos.size() > 1) {
			int sizeCliques = junctionTree.getCliques().size();
			for (int i = 0; i < sizeCliques; i++) {
				auxClique = (Clique) junctionTree.getCliques().get(i);
				listaNos = SetToolkit.clone(auxClique.getNos());
	
				//calcula o índice
				while ((ndx = indice(listaNos, alpha)) <= 0
					&& listaNos.size() > 1);
				if (ndx < 0) {
					ndx = 0;
				}
				auxClique.setIndex(ndx);
				listaNos.clear();
			}
			alpha.clear();
	
			Comparator comparador = new Comparator() {
				public int compare(Object o1, Object o2) {
					Clique c1 = (Clique) o1;
					Clique c2 = (Clique) o2;
					if (c1.getIndex() > c2.getIndex()) {
						return 1;
					}
					if (c1.getIndex() < c2.getIndex()) {
						return -1;
					}
					return 0;
				}
			};
	
			Collections.sort(junctionTree.getCliques(), comparador);
	
			auxClique = (Clique) junctionTree.getCliques().get(0);
			uni = SetToolkit.clone(auxClique.getNos());
	
			int sizeCliques1 = junctionTree.getCliques().size();
			for (int i = 1; i < sizeCliques1; i++) {
				auxClique = (Clique) junctionTree.getCliques().get(i);
				inter = SetToolkit.intersection(auxClique.getNos(), uni);
	
				for (int j = 0; j < i; j++) {
					auxClique2 = (Clique) junctionTree.getCliques().get(j);
	
					if (!auxClique2.getNos().containsAll(inter)) {
						continue;
					}
	
					sep = new Separator(auxClique2, auxClique);
					sep.setNos(inter);
					junctionTree.addSeparator(sep);
	
					auxList = SetToolkit.union(auxClique.getNos(), uni);
					uni.clear();
					uni = auxList;
					break;
				}
			}
		}
	}

	/**
	 *  SUB-FUNÇÃO do método arvoreForte
	 */
	protected int indice(NodeList listaNos, NodeList alpha) {
		int ndx;
		int mx;
		Node auxNo;
		Node noMax = null;
		NodeList auxList = null;
		NodeList vizinhos;
	
		// pega o nó de índice máximo na ordem alpha (inverso da ordem de eliminição)
		mx = -1;
		int sizeNos = listaNos.size();
		for (int i = 0; i < sizeNos; i++) {
			auxNo = listaNos.get(i);
			ndx = alpha.indexOf(auxNo);
			if (mx < ndx) {
				mx = ndx;
				noMax = auxNo;
			}
		}
	
		// Retira esse nó do clique
		listaNos.remove(noMax);
	
		// Monta uma lista de vizinhos do clique
		auxNo = listaNos.get(0);
		vizinhos = SetToolkit.clone(auxNo.getAdjacents());
		int sizeNos1 = listaNos.size();
		for (int i = 1; i < sizeNos1; i++) {
			auxNo = listaNos.get(i);
			auxList = SetToolkit.intersection(vizinhos, auxNo.getAdjacents());
			vizinhos.clear();
			vizinhos = auxList;
		}
		vizinhos.remove(noMax);
	
		ndx = 0;
		int sizeVizinhos = vizinhos.size();
		for (int i = 0; i < sizeVizinhos; i++) {
			auxNo = vizinhos.get(i);
			if (listaNos.contains(auxNo) || (alpha.indexOf(auxNo) > mx)) {
				continue;
			}
			ndx = mx;
			break;
		}
	
		return ndx;
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
	protected boolean pesoMinimo(NodeList auxNos) {
		boolean algum;
		
		algum = true;
		while (algum) {
			algum = false;
	
			for (int i = auxNos.size() - 1; i >= 0; i--) {
				Node auxNo = auxNos.get(i);
	
				if (cordas(auxNo)) {
					//Não tem cordas necessárias:teste próximo.
					continue;
				}
	
				for (int j = auxNo.getAdjacents().size() - 1; j >= 0; j--) {
					Node v = auxNo.getAdjacents().get(j);
					boolean removed = v.getAdjacents().remove(auxNo);				
					assert removed;
				}
				auxNos.remove(auxNo);
				algum = true;
				oe.add(auxNo);
				if (createLog) {
					logManager.append(
						"\t" + oe.size() + " " + auxNo.getName() + "\n");
				}
			}
		}
	
		if (auxNos.size() > 0) {
			Node auxNo = peso(auxNos); //auxNo: clique de peso mínimo.
			oe.add(auxNo);
			if (createLog) {
				logManager.append(
					"\t" + oe.size() + " " + auxNo.getName() + "\n");
			}
			elimine(auxNo, auxNos); //Elimine no e reduza grafo.
			return true;
		}
		
		return false;
	}

	/**
	 *  SUB-FUNÇÃO do procedimento triangula que elimina nó e reduz o grafo. Inclui
	 *  cordas necessárias para eliminar nó. Retira-o e aos adjacentes.
	 *
	 *@param  no      nó a ser eliminado
	 *@param  auxNos  lista de nós
	 */
	private void elimine(Node no, NodeList auxNos) {	
		for (int i = no.getAdjacents().size()-1; i > 0; i--) {
			Node auxNo1 = no.getAdjacents().get(i);
	
			for (int j = i - 1; j >= 0; j--) {
				Node auxNo2 = no.getAdjacents().get(j);
				if (! auxNo2.getAdjacents().contains(auxNo1)) {
					Edge auxArco = new Edge(auxNo1, auxNo2);
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
					
					System.out.println(auxArco);
				}
			}
		}
	
		for (int i = no.getAdjacents().size() - 1; i >= 0; i--) {
			Node auxNo1 = no.getAdjacents().get(i);
			boolean removed = auxNo1.getAdjacents().remove(no);
			assert removed;
		}
		auxNos.remove(no);
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
	
		Node noMin = null;
		double pmin = Double.MAX_VALUE;

		for (int i = auxNos.size()-1; i >= 0; i--) {
			auxNo = auxNos.get(i);
			p = Math.log(auxNo.getStatesSize());
	
			for (int j = auxNo.getAdjacents().size()-1; j >= 0; j--) {
				v = auxNo.getAdjacents().get(j);
				p += Math.log(v.getStatesSize());
			}
			if (p < pmin) {
				pmin = p;
				noMin = auxNo;
			}
		}
		
		assert noMin != null;
		return noMin;
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
	
		for (int i = no.getAdjacents().size()-1; i > 0; i--) {
			Node auxNo1 = no.getAdjacents().get(i);
	
			for (int j = i - 1; j >=0; j--) {
				Node auxNo2 = no.getAdjacents().get(j);
				if (! auxNo2.getAdjacents().contains(auxNo1)) {
					return true;
				}
			}
		}
		return false;
	}
	
	
	/**
	 *  Verifica integridade como grafo direcionado acíclico / conexo e coesão.
	 *  Com a saída é possível saber quais erros especificamente ocorreram caso algum ocorra.
	 */
	protected void verifyConsistency() throws Exception {
		if (nos.size() != 0) {
			
			nodeIndexes.clear();
			for (int i = nos.size()-1; i>=0; i--) {
				nodeIndexes.put(nos.get(i).getName(), new Integer(i));				
			}
			
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

	/**
	 * Seta o nome da rede.
	 *
	 * @param name nome da rede.
	 */
	public void setName(String name) {
		this.name = name;
	}

	public HierarchicTree getHierarchicTree() {
		return hierarchicTree;
	}

	public void setHierarchicTree(HierarchicTree hierarchicTree) {
		this.hierarchicTree = hierarchicTree;
	}

	/**
	 * Retorna o nome da rede.
	 *
	 * @return nome da rede.
	 */
	public String getName() {
		return name;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public double getRadius() {
		return radius;
	}

	/**
	 *  SUB-FUNÇÃO do método verificaConsistência que verifica a consistencia
	 *  das tabelas de potenciais dos nós do grafo.
	 */
	protected void verificaTabelasPot() throws Exception {
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
	 *  SUB-FUNÇÃO do método verificaConsistência que verifica se todos os
	 *  nós de utilidade não contém filhos.
	 */
	protected void verificaUtilidade() throws Exception {
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
	 *  SUB-FUNÇÃO do método verificaConsistência que verifica se
	 *  existe uma ordenação total das decisões. Isto é, se existe
	 *  um caminho orientado entre as decisões.
	 */
	protected void sortDecisions() throws Exception {
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
	 * Retorna o vetor de cópia dos nós
	 * (sem as variáveis de utilidade).
	 *
	 * @return vetor de cópia dos nós sem as variáveis de utilidade.
	 */
	public NodeList getCopiaNos() {
		return copiaNos;
	}

	public String getLog() {
		return logManager.getLog();
	}

	/**
	 * Gets the createLog.
	 * @return Returns a boolean
	 */
	public boolean isCreateLog() {
		return createLog;
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

	protected void copyMarginal() {
		for (int i = 0; i < copiaNos.size(); i++) {
			TreeVariable node = (TreeVariable) copiaNos.get(i);
			node.copyMarginal();
		}
	}

	protected void restoreMarginais() {
		for (int i = 0; i < copiaNos.size(); i++) {
			TreeVariable node = (TreeVariable) copiaNos.get(i);
			node.restoreMarginal();
		}
	}

	/**
	 * Sets the firstInitialization.
	 * @param firstInitialization The firstInitialization to set
	 */
	public void setFirstInitialization(boolean firstInitialization) {
		this.firstInitialization = firstInitialization;
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
	 * Returns the id.
	 * @return String
	 */
	public String getId() {
		return id;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return name + '(' + id + ')';
	}
}

