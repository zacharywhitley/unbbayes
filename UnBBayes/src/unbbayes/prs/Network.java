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

package unbbayes.prs;

import java.util.*;

import unbbayes.prs.bn.ITabledVariable;
import unbbayes.prs.bn.PotentialTable;
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
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.prs.bn.resources.BnResources");

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
    
    
    private Map nodeIndexes;


    /**
     *  Constrói um novo grafo sem nós nem arcos.
     */
    public Network() {
        nos = new NodeList();
        //descriptionNodes = new NodeList();
        //explanationNodes = new NodeList();
        arcos = new ArrayList();
        
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
}

