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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JTree;
import javax.swing.tree.*;

import unbbayes.io.LogManager;
import unbbayes.util.NodeList;
import unbbayes.util.SetToolkit;

/**
 *  Representa uma rede probabilística.
 *
 *@author     michael
 *@author     rommel
 */
public class ProbabilisticNetwork extends Network implements java.io.Serializable {

	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.jprs.jbn.resources.JbnResources");

    /**
     * Faz o processamento do log de compilação.
     */
    private LogManager logManager;

    /**
     *  Armazena handle do objeto Árvore de Junção associado ao Grafo.
     */
    private JunctionTree junctionTree;

    /**
     *  Lista de arcos utilizada no processo de transformação.
     */
    private List arcosMarkov;

    private double radius;

    /**
     * Nós de decisão utilizado no processo de transformação.
     */
    private NodeList decisionNodes;

    /**
     * Cópia dos nós sem os nós de utilidade. Utilizado no processo
     * de transformação.
     */
    private NodeList copiaNos;

    private String nome = "";

    /**
     *  Ordem de eliminação dos nós.
     */
    private NodeList oe;

    private HierarchicTree hierarchicTree;
    /**
     *  Cria uma nova rede probabilística. Limpa o arquivo de log e inicializa o
     *  vetor da ordem de eliminação.
     */
    public ProbabilisticNetwork() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        DefaultTreeModel model = new DefaultTreeModel(root);
        hierarchicTree = new HierarchicTree(model);
        logManager = new LogManager();
        arcosMarkov = new ArrayList();
        oe = new NodeList();
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

    public void setHierarchicTree(HierarchicTree hierarchicTree)
    {   this.hierarchicTree = hierarchicTree;
    }

    public HierarchicTree getHierarchicTree()
    {   return hierarchicTree;
    }


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

    private void resetEvidences() {
        for (int i = 0; i < copiaNos.size(); i++) {
            ((TreeVariable) copiaNos.get(i)).resetEvidence();
        }
    }


    /**
     *  Monta árvore de junção a partir do grafo.
     */
    private void compilaAJ() throws Exception {
        Edge auxArc;
        int menor;
        Clique auxClique;
        Separator auxSep;

        resetEvidences();

        if (isID()) {
            junctionTree = new JunctionTreeID();
        } else {
            junctionTree = new JunctionTree();
        }
        desmontaAdjacentes();

        int sizeMarkov = arcosMarkov.size();
        for (int c = 0; c < sizeMarkov; c++) {
            auxArc = (Edge) arcosMarkov.get(c);
            auxArc.getOriginNode().getAdjacents().add(auxArc.getDestinationNode());
            auxArc.getDestinationNode().getAdjacents().add(auxArc.getOriginNode());
        }

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
                    if (auxSep.getNos().contains(auxNode) && (auxSep.getPotentialTable().tableSize() < menor)) {
                        ((ProbabilisticNode)auxNode).setAssociatedClique(auxSep);
                        menor = auxSep.getPotentialTable().tableSize();
                    }
                }
            }

            if (menor == Integer.MAX_VALUE) {
                int sizeCliques = junctionTree.getCliques().size();
                for (int c2 = 0; c2 < sizeCliques; c2++) {
                    auxClique = (Clique) junctionTree.getCliques().get(c2);
                    if (auxClique.getNos().contains(auxNode) && (auxClique.getPotentialTable().tableSize() < menor)) {
                        if (auxNode.getType() == Node.PROBABILISTIC_NODE_TYPE) {
                            ((ProbabilisticNode)auxNode).setAssociatedClique(auxClique);
                        } else {
                            ((DecisionNode)auxNode).setAssociatedClique(auxClique);
                            break;
                        }
                        menor = auxClique.getPotentialTable().tableSize();
                    }
                }
            }
        }


        updateMarginais();

        Thread t = new Thread(new Runnable() {
          public void run() {
              makeLog();
              System.out.println("**Log ended**");
          }
        });
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
    }


    private void makeLog() {
        long in = System.currentTimeMillis();
        try {
            logManager.finishLog(junctionTree, nos);
            logManager.writeToDisk(LogManager.DEFAULT_FILENAME, false);
        } catch (java.io.IOException ioe) {
            System.err.println(ioe.getMessage());
        }
        System.out.println("GERACAO DO ARQUIVO LOG em " + (System.currentTimeMillis() - in) + " ms");
    }

    public String getLog() {
        return logManager.getLog();
    }


    /**
     *  Faz o processo de moralização da rede.
     */
    private void moraliza() {
        Node auxNo;
        Node auxPai1;
        Node auxPai2;
        Edge auxArco;
        List copiaArcos;

        logManager.append(resource.getString("moralizeLabel"));
        arcosMarkov.clear();
        copiaArcos = SetToolkit.clone(arcos);

        //retira os arcos de informação
        int sizeArcos = copiaArcos.size()-1;
        for (int i = sizeArcos; i >= 0; i--) {
           auxArco = (Edge)copiaArcos.get(i);
           if (auxArco.getDestinationNode().getType() == Node.DECISION_NODE_TYPE) {
              copiaArcos.remove(i);
           }
        }

        int sizeNos = nos.size();
        for (int n = 0; n < sizeNos; n++) {
            auxNo = nos.get(n);
            if (!(auxNo.getType() == Node.DECISION_NODE_TYPE) && auxNo.getParents().size() > 1) {
                int sizePais = auxNo.getParents().size();
                for (int j = 0; j < sizePais - 1; j++) {
                    auxPai1 = auxNo.getParents().get(j);
                    for (int k = j + 1; k < sizePais; k++) {
                        auxPai2 = auxNo.getParents().get(k);
                        if ((existeArco(auxPai1, auxPai2, copiaArcos) == -1) && (existeArco(auxPai1, auxPai2, arcosMarkov) == -1)) {
                            auxArco = new Edge(auxPai1, auxPai2);
                            logManager.append(auxPai1.getName() + " - " + auxPai2.getName() + "\n");
                            arcosMarkov.add(auxArco);
                        }
                    }
                }
            }
        }

        desmontaAdjacentes();

        int sizeMarkov = arcosMarkov.size()-1;
        for (int z = sizeMarkov; z >= 0; z--) {
            auxArco = (Edge) arcosMarkov.get(z);
            auxArco.getOriginNode().getAdjacents().add(auxArco.getDestinationNode());
            auxArco.getDestinationNode().getAdjacents().add(auxArco.getOriginNode());
        }

        int sizeArcos1 = copiaArcos.size();
        for (int z = sizeArcos1-1; z >= 0; z--) {
            auxArco = (Edge) copiaArcos.get(z);
            if (auxArco.getDestinationNode().getType() == Node.UTILITY_NODE_TYPE) {
                copiaArcos.remove(z);
            } else {
                auxArco.getOriginNode().getAdjacents().add(auxArco.getDestinationNode());
                auxArco.getDestinationNode().getAdjacents().add(auxArco.getOriginNode());
            }
        }
        arcosMarkov = SetToolkit.union(arcosMarkov, copiaArcos);
        logManager.append("\n");
    }


    /**
     *  Faz o processo de triangulação da rede.
     */
    private void triangula() {
        Node aux;
        NodeList auxNos;

        logManager.append(resource.getString("triangulateLabel"));
        auxNos = SetToolkit.clone (nos);
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
        for (int i = decisionNodes.size()-1; i >= 0; i--) {
            aux = decisionNodes.get(i);
            oe.add(aux);
            int sizeAdjacentes = aux.getAdjacents().size();
            for (int j = 0; j < sizeAdjacentes; j++) {
                Node v = aux.getAdjacents().get(j);
                v.getAdjacents().remove(aux);
            }
            logManager.append("\t" + oe.size() + " " + aux.getName() + "\n");

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
        for (int i = nodes.size()-1; i >= 0; i--) {
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


    private int existeArco(Node no1, Node no2, List vetArcos) {
        if (no1 == no2) {
            return 1;
        }

        int sizeArcos = vetArcos.size();
        Edge auxA;
        for (int i = 0; i < sizeArcos; i++) {
            auxA = (Edge) vetArcos.get(i);
            if ((auxA.getOriginNode() == no1) && (auxA.getDestinationNode() == no2) ||
                    (auxA.getOriginNode() == no2) && (auxA.getDestinationNode() == no1)) {
                return i;
            }
        }
        return -1;
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


    /**
     *  Retorna a probabilidade estimada total da árvore de junção associada.
     *
     *@return    probabilidade estimada total da árvore de junção associada.
     */
    public double PET() {
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
        logManager.reset();
        verificaConsistencia();
        moraliza();
        triangula();
        compilaAJ();
    }

    private void updateMarginais() {
        for (int i = 0; i < copiaNos.size(); i++) {
            ((TreeVariable)copiaNos.get(i)).marginal();
        }
    }


    /**
     *  Chama o método da árvore de junção para atualizar evidências.
     *  @return             consistência da árvore atualizada.
     */
     public void updateEvidences() throws Exception {
        int sizeNos = copiaNos.size();
        for (int c = 0; c < sizeNos; c++) {
            TreeVariable node = (TreeVariable) copiaNos.get(c);
            if (node.hasEvidence()) {
                ITabledVariable auxObj;
                auxObj = node.getAssociatedClique();
                PotentialTable auxTab = auxObj.getPotentialTable();
                int index = auxTab.indexOfVariable(node);
                int sizeDados = auxTab.tableSize();
                int[] coord;
                for (int c2 = 0; c2 < sizeDados; c2++) {
                    double aux = auxTab.getValue(c2);
                    coord = auxTab.voltaCoord(c2);
                    aux *= node.getMarginalAt(coord[index]);
                    auxTab.setValue(c2,aux);
                }
            }
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
        updateMarginais();
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
        Node aux,aux2,aux3;

        int sizeDecisao = decisionNodes.size();
        for (int i = 0; i < sizeDecisao; i++) {
            boolean visitados[] = new boolean[nos.size()];
            aux = (Node)decisionNodes.get(i);
            fila.clear();
            fila.add(aux);

            while (fila.size() != 0) {
                aux2 =  fila.remove(0);
                visitados[nos.indexOf(aux2)] = true;

                int sizeFilhos = aux2.getChildren().size();
                for (int k = 0; k < sizeFilhos; k++) {
                    aux3 = (Node)aux2.getChildren().get(k);
                    if (! visitados[nos.indexOf(aux3)]) {
                        if (aux3.getType() == Node.DECISION_NODE_TYPE && ! aux.getAdjacents().contains(aux3)) {
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
                Node node1 =  decisionNodes.get(i);
                Node node2 =  decisionNodes.get(i + 1);
                if (node1.getAdjacents().size() < node2.getAdjacents().size()) {
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
            aux =  decisionNodes.get(i);
//            System.out.print(aux.getAdjacents().size() + " ");
            if (aux.getAdjacents().size() != decisionNodes.size()-i-1) {
                throw new Exception(resource.getString("DecisionOrderException"));
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
            aux = (Node)nos.get(i);
            if (aux.getType() == Node.UTILITY_NODE_TYPE && aux.getChildren().size() != 0) {
                throw new Exception(resource.getString("variableName") + aux + resource.getString("hasChildName"));
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
            auxNo =  nos.get(c);
            if (auxNo.getType() == Node.PROBABILISTIC_NODE_TYPE) {
                auxVP = (ProbabilisticNode) auxNo;
                auxTabPot = (ProbabilisticTable) auxVP.getPotentialTable();
                auxTabPot.verificaConsistencia();
            }
        }
    }


    /**
     *  Faz o processo de identificação dos Cliques
     */
    private void cliques() {
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
            auxNo =  copiaNos.get(i);
            e = oe.indexOf(auxNo);
            auxClique = new Clique();
            auxClique.getNos().add(auxNo);

            int sizeAdjacentes = auxNo.getAdjacents().size();
            for (j = 0; j < sizeAdjacentes; j++) {
                auxNo2 =  auxNo.getAdjacents().get(j);
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

  for1: for (i = 0; i < sizeCliques; i++) {
            auxClique = (Clique) listaCliques.get(i);
            for (j = i + 1; j < sizeCliques; j++) {
                auxClique2 = (Clique) listaCliques.get(j);

                if (auxClique2.getNos().containsAll(auxClique.getNos()) ){
                    continue for1;
                }
            }
            junctionTree.getCliques().add(auxClique);
        }
        listaCliques.clear();
    }


    /**
     *  SUB-FUNÇÃO do método arvoreForte
     */
    private int indice(NodeList listaNos, NodeList alpha) {
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
            auxNo =  listaNos.get(i);
            ndx = alpha.indexOf(auxNo);
            if (mx < ndx) {
                mx = ndx;
                noMax = auxNo;
            }
        }

        // Retira esse nó do clique
        listaNos.remove(noMax);

        // Monta uma lista de vizinhos do clique
        auxNo =  listaNos.get(0);
        vizinhos = SetToolkit.clone(auxNo.getAdjacents());
        int sizeNos1 = listaNos.size();
        for (int i = 1; i < sizeNos1; i++) {
            auxNo =  listaNos.get(i);
            auxList = SetToolkit.intersection(vizinhos, auxNo.getAdjacents());
            vizinhos.clear();
            vizinhos = auxList;
        }
        vizinhos.remove(noMax);

        ndx = 0;
        int sizeVizinhos = vizinhos.size();
        for (int i = 0; i < sizeVizinhos; i++) {
            auxNo =  vizinhos.get(i);
            if (listaNos.contains(auxNo) || (alpha.indexOf(auxNo) > mx)) {
                continue;
            }
            ndx = mx;
            break;
        }

        return ndx;
    }


    /**
     *  Faz o processo de constituição da árvore de junção - Frank Jensen
     */
    private void arvoreForte() {
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
                while ( (ndx = indice(listaNos, alpha)) <= 0 && listaNos.size() > 1)
                      ;
                if (ndx < 0) {
                   ndx = 0;
                }
                auxClique.setIndex(ndx);
                listaNos.clear();
            }
            alpha.clear();


            Comparator comparador = new Comparator() {
               public int compare(Object o1, Object o2) {
                    Clique c1 = (Clique)o1;
                    Clique c2 = (Clique)o2;
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

                    if (! auxClique2.getNos().containsAll(inter)) {
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
     * Ordena os nós dos cliques e dos separadores de acordo com a ordem de eliminação.
     */
    private void sortCliqueNodes() {
        List listaCliques = junctionTree.getCliques();
        for (int k = 0; k < listaCliques.size(); k++) {
            Clique clique = (Clique) listaCliques.get(k);
            NodeList nosClique = clique.getNos();
            boolean haTroca = true;
            while (haTroca) {
                haTroca = false;
                for (int i = 0; i < nosClique.size() - 1; i++) {
                    Node node1 =  nosClique.get(i);
                    Node node2 =  nosClique.get(i + 1);
                    if (oe.indexOf(node1) > oe.indexOf(node2)) {
                        nosClique.set(i + 1, node1);
                        nosClique.set(i, node2);
                        haTroca = true;
                    }
                }
            }
        }

        for (int k = junctionTree.getSeparatorsSize()-1; k >= 0; k--) {
            Separator separator = (Separator) junctionTree.getSeparatorAt(k);
            NodeList nosSeparator = separator.getNos();
            boolean haTroca = true;
            while (haTroca) {
                haTroca = false;
                for (int i = 0; i < nosSeparator.size() - 1; i++) {
                    Node node1 =  nosSeparator.get(i);
                    Node node2 =  nosSeparator.get(i + 1);
                    if (oe.indexOf(node1) > oe.indexOf(node2)) {
                        nosSeparator.set(i + 1, node1);
                        nosSeparator.set(i, node2);
                        haTroca = true;
                    }
                }
            }
        }
    }


    /**
     *  Faz a associação dos Nós a um único clique com menos espaço de est. que
     *  contenha sua família
     */
    private void associaCliques() {
        int min;
        Node auxNo;
        PotentialTable auxTabPot, auxUtilTab;
        Clique auxClique;
        Clique cliqueMin = null;

        for (int i = junctionTree.getCliques().size()-1; i >= 0; i--) {
            auxClique = (Clique) junctionTree.getCliques().get(i);
            auxTabPot = auxClique.getPotentialTable();
            auxUtilTab = auxClique.getUtilityTable();

            int sizeNos = auxClique.getNos().size();
            for (int c = 0; c < sizeNos; c++) {
                auxTabPot.addVariable( auxClique.getNos().get(c));
                auxUtilTab.addVariable( auxClique.getNos().get(c));
            }
        }

        for (int k = junctionTree.getSeparatorsSize()-1; k >= 0; k--) {
            Separator auxSep = (Separator) junctionTree.getSeparatorAt(k);
            auxTabPot = auxSep.getPotentialTable();
            auxUtilTab = auxSep.getUtilityTable();
            int sizeNos = auxSep.getNos().size();
            for (int c = 0; c < sizeNos; c++) {
                auxTabPot.addVariable( auxSep.getNos().get(c));
                auxUtilTab.addVariable( auxSep.getNos().get(c));
            }
        }

        int sizeNos = nos.size();
        for (int n = 0; n < sizeNos; n++) {
            if (nos.get(n).getType() == Node.DECISION_NODE_TYPE) {
                continue;
            }

            min = Integer.MAX_VALUE;
            auxNo =  nos.get(n);

            int sizeCliques = junctionTree.getCliques().size();
            for (int c = 0; c < sizeCliques; c++) {
                auxClique = (Clique) junctionTree.getCliques().get(c);

                if (auxClique.getPotentialTable().tableSize() < min && auxClique.getNos().containsAll(auxNo.getParents())) {
                    if (auxNo.getType() == Node.PROBABILISTIC_NODE_TYPE && ! auxClique.getNos().contains(auxNo)) {
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
            Node auxNo1 =  no.getAdjacents().get(i);

            for (int j = i + 1; j < sizeAdjacentes; j++) {
                Node auxNo2 =  no.getAdjacents().get(j);
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

        Node noMin =  auxNos.get(0);
        double pmin = Math.log(noMin.getStatesSize());

        int sizeAdjacentes = noMin.getAdjacents().size();
        for (int i = 0; i < sizeAdjacentes; i++) {
            v =  noMin.getAdjacents().get(i);
            pmin += Math.log(v.getStatesSize());
        }

        int sizeNos = auxNos.size();
        for (int i = 1; i < sizeNos; i++) {
            auxNo =  auxNos.get(i);
            p = Math.log(auxNo.getStatesSize());

            int sizeAdjacentes1 = auxNo.getAdjacents().size();
            for (int j = 0; j < sizeAdjacentes1; j++) {
                v =  auxNo.getAdjacents().get(j);
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
            auxNo1 =  no.getAdjacents().get(i);

            for (int j = i + 1; j < sizeAdjacentes; j++) {
                auxNo2 =  no.getAdjacents().get(j);
                if (! auxNo2.getAdjacents().contains(auxNo1)) {
                    auxArco = new Edge(auxNo1, auxNo2);
                    logManager.append(auxNo1.getName() + resource.getString("linkedName") + auxNo2.getName() + "\n");
                    arcosMarkov.add(auxArco);
                    auxNo1.getAdjacents().add(auxNo2);
                    auxNo2.getAdjacents().add(auxNo1);
                }
            }
        }

        int sizeAdjacentes1 = no.getAdjacents().size();
        for (int i = 0; i < sizeAdjacentes1; i++) {
            auxNo1 =  no.getAdjacents().get(i);
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
                logManager.append("\t" + oe.size() + " " + auxNo.getName() + "\n");
                break;
            }
        }

        if (auxNos.size() > 0) {
            auxNo = peso(auxNos); //auxNo: clique de peso mínimo.
            oe.add(auxNo);
            logManager.append("\t" + oe.size() + " " + auxNo.getName() + "\n");
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
     * Returns true if this network is a Influence Diagram
     * @return Returns true if this network is a Influence Diagram, false otherwise.
     */
    public boolean isID() {
        for (int i = 0; i < nos.size(); i++) {
            if (nos.get(i).getType() == Node.DECISION_NODE_TYPE ||
                nos.get(i).getType() == Node.UTILITY_NODE_TYPE) {

                return true;
            }
        }
        return false;
    }
}