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

package unbbayes.jprs.jbn;

import java.util.*;

/**
 *  Classe que representa um Grafo genérico.
 *
 *@author     Michael e Rommel
 *@version    21 de Setembro de 2001
 */
public class Network {

    /**
     *  Lista de nós que compõem o grafo.
     */
    protected List nos;

    /**
     *  Lista de arcos que compõem o grafo.
     */
    protected List arcos;


    /**
     *  Constrói um novo grafo sem nós nem arcos.
     */
    public Network() {
        nos = new ArrayList();
        arcos = new ArrayList();
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
     */
    public List getNos() {
        return this.nos;
    }


    /**
     *  Retorna o nó do grafo com o respectivo índice.
     *
     *@param  index  índice do nó.
     *@return	nó com respectivo índice no List.
     */
    public Node getNodeAt(int index) {
        return (Node) (nos.get(index));
    }


    /**
     *  Retorna o nó do grafo com a respectiva sigla.
     *
     *@param  name  nome do nó.
     *@return       nó com a respectiva sigla.
     */
    public Node getNode(String name) {
        for (int qnos = 0; qnos < nos.size(); qnos++) {
            if (((Node) (nos.get(qnos))).getName().equals(name)) {
                return (Node) (nos.get(qnos));
            }
        }
        return null;
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
        for (c = 0; c < nos.size(); c++) {
            auxNo = (Node) nos.get(c);
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
            ((Node) nos.get(qnos)).montaAdjacentes();
        }
    }


    /**
     *  Destrói a lista de adjacentes de cada nó do grafo.
     */
    protected void desmontaAdjacentes() {
        for (int qnos = 0; qnos < nos.size(); qnos++) {
            ((Node) nos.get(qnos)).desmontaAdjacentes();
        }
    }


    /**
     *  Limpa a lista de nós.
     */
    protected void limpaNos() {
        nos.clear();
    }


    /**
     *  Limpa a lista de arcos.
     */
    protected void limpaArcos() {
        arcos.clear();
    }

    /**
     *  Verificação através da eliminação dos nós que não são pais e filhos
     *  de alguém ao mesmo tempo. Há a necessidade de trabalhar com uma cópia
     *  da lista de nós da rede.
     *
     *@throws Exception se a rede possui ciclo.
     */
    public final void verifyCycles() throws Exception {
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
        List listaCloneNos = SetToolkit.clone(nos);

        if (! listaCloneNos.isEmpty()) {
            for (i = 0; i < listaCloneNos.size(); i++) {
                auxNo1 = (Node) listaCloneNos.get(i);
                listaPais.add(SetToolkit.clone(auxNo1.getParents()));
                listaFilhos.add(SetToolkit.clone(auxNo1.getChildren()));
            }
            auxNo1 = (Node) listaCloneNos.get(0);
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
                            auxNo2 = (Node) listaCloneNos.get(j);
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
                    }
                    else {
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
                        auxNo1 = (Node) listaCloneNos.get(l);
                    }
                    else {
                        auxNo1 = null;
                        existeRetirada = false;
                    }
                }
            }

            for (i = 0; i < nos.size(); i++) {
                auxNo1 = (Node) nos.get(i);
                auxNo1.setParents((List) listaPais.get(i));
                auxNo1.setChildren((List) listaFilhos.get(i));
            }
            listaPais.clear();
            listaFilhos.clear();

            if (! listaCloneNos.isEmpty()) {
                StringBuffer sb = new StringBuffer("Rede com ciclo:");
                for (i = 0; i < listaCloneNos.size(); i++) {
                   auxNo1 = (Node)listaCloneNos.get(i);
                   sb.append(" " + auxNo1.getName());
                }
                throw new Exception(sb.toString());
            }
        }
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
        percorre((Node)nos.get(0), visitados);
        desmontaAdjacentes();
        if (visitados.size() != nos.size()) {
            throw new Exception("Rede Disconexa");
        }
    }

    /**
     * Percorrimento em profundidade.
     */
    private void percorre(Node no, List visitados) {
        visitados.add(no);
        for (int i = 0; i < no.getAdjacents().size(); i++) {
            Node aux = (Node)no.getAdjacents().get(i);
            if (! visitados.contains(aux)) {
                percorre(aux, visitados);
            }
        }
    }
}

