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
import java.util.List;
import java.util.ResourceBundle;

import unbbayes.util.NodeList;

/**
 *  Classe que representa um Clique na Árvore de Junção (JunctionTree).
 *
 *@author    Michael e Rommel
 *@version   27 de Junho de 2001
 */
public class Clique implements ITabledVariable, java.io.Serializable {
	
	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.prs.bn.resources.BnResources");

    /**
     *  Identifica unicamente o nó.
     */
    private int index;

    /**
     *  Referência para o clique pai.
     */
    private Clique parent;

    /**
     *  Lista de nós filhos.
     */
    private List children;

    /**
     *  Tabela de Potencial Associada ao Clique.
     */
    private PotentialTable potentialTable;
    
    /**
     *  Tabela de Utilidade Associada ao Clique.
     */
    private PotentialTable utilityTable;    

    /**
     *  Lista de Nós Clusterizados.
     */
    private NodeList nos;

    /**
     *  Lista de Nós Probabilísticos associados ao Clique.
     */
    private NodeList nosAssociados;

    /**
     *  Lista de Nós de Utilidade associados ao Clique.
     */
    private NodeList associatedUtilNodes;


    /**
     *  Constrói um novo clique. Inicializa o vetor de filhos, de nós clusterizados
     *  e de nós associados. Inicializa o status associado para false.
     */
    public Clique() {
        children = new ArrayList();
        nos = new NodeList();
        nosAssociados = new NodeList();
        associatedUtilNodes = new NodeList();
        potentialTable = new ProbabilisticTable();
        utilityTable = new UtilityTable();
    }


    /**
     *  Normaliza um clique da árvore.
     *
     *@param  ok      vetor boolean de tamanho 1 para passar parametro por referência.
     *@return         constante de normalização.
     */
    protected double normalize() throws Exception {
        boolean fixo[] = new boolean[nos.size()];
        NodeList decisoes = new NodeList();
        for (int i = 0; i < nos.size(); i++) {        	
            if (nos.get(i).getType() == Node.DECISION_NODE_TYPE) {
                decisoes.add(nos.get(i));
                fixo[i] = true;
            }
        }

        if (decisoes.size() == 0) {
            return normalizeBN();
        }

        int index[] = new int[decisoes.size()];
        for (int i = 0; i < index.length; i++) {
            index[i] = nos.indexOf(decisoes.get(i));
        }
        normalizeID(0, decisoes, fixo, index, new int[nos.size()]);
        /** @todo retornar a constante de normalizacao correta */
        return 0.0;
    }

    private float normalizeBN() throws Exception {
        float n = 0;
        float valor;

        int sizeDados = potentialTable.tableSize();
        for (int c = 0; c < sizeDados; c++) {
            n += potentialTable.getValue(c);
        }
        if (Math.abs(n - 1.0) > 0.001) {
            for (int c = 0; c < sizeDados; c++) {
                valor = potentialTable.getValue(c);
                if (n == 0.0) {
                    throw new Exception(resource.getString("InconsistencyUnderflowException"));
                }
                valor /= n;
                potentialTable.setValue(c, valor);
            }
        }
        return n;
    }


    private void normalizeID (int control,
                             NodeList decisoes,
                             boolean fixo[],
                             int index[],
                             int coord[]) throws Exception {

        if (control == decisoes.size()) {
            float soma = sum(0, fixo, coord);
            if (soma == 0.0) {
            	for (int k = 0; k < decisoes.size(); k++) {
					System.out.println(decisoes.get(k) + " - " + decisoes.get(k).getStateAt(coord[index[k]]));
            	}
            	return;
//                throw new Exception(resource.getString("InconsistencyUnderflowException"));
            }
            div(0, fixo, coord, soma);            
            return;
        }

        DecisionNode node = (DecisionNode) decisoes.get(control);
        
        if (node.hasEvidence()) {
        	coord[index[control]] = node.getEvidence();
            normalizeID(control+1, decisoes, fixo, index, coord);
        	return;        	
        }
                
        for (int i = 0; i < node.getStatesSize(); i++) {        	
            coord[index[control]] = i;
			normalizeID(control+1, decisoes, fixo, index, coord);
        }
    }

    private float sum(int control, boolean fixo[], int coord[]) {
        if (control == nos.size()) {
            return potentialTable.getValue(coord);
        }

        if (fixo[control]) {
            return sum(control+1, fixo, coord);
        }

        Node node = nos.get(control);
        float retorno = 0;
        for (int i = 0; i < node.getStatesSize(); i++) {
            coord[control] = i;
            retorno += sum(control+1, fixo, coord);
        }
        return retorno;
    }

    private void div(int control, boolean fixo[], int coord[], float soma) {
        if (control == nos.size()) {
            int cLinear = potentialTable.getLinearCoord(coord);
            potentialTable.setValue(cLinear, potentialTable.getValue(cLinear) / soma);
            return;
        }

        if (fixo[control]) {
            div(control+1, fixo, coord, soma);
            return;
        }

        Node node = nos.get(control);
        for (int i = 0; i < node.getStatesSize(); i++) {
            coord[control] = i;
            div(control+1, fixo, coord, soma);
        }
    }


    /**
     *  Seta o pai deste clique
     *
     *@param  pai  o pai do clique
     */
    public void setParent(Clique pai) {
        this.parent = pai;
    }


    /**
     * Retorna o índice associado ao clique.
     *
     * @return índice associado ao clique.
     */
    public int getIndex() {
        return index;
    }

    /**
     * Muda o índice do clique.
     *
     * @param indice frag que indica o status de associação
     */
    public void setIndex(int indice) {
        this.index = indice;
    }


    /**
     *  Retorna o tamanho da lista de  filhos.
     *
     *@return    vetor de filhos.
     */
    public int getChildrenSize() {
        return children.size();
    }

    /**
     *  Adiciona um filho no final da lista de filhos.
     *
     *@param  child filho a ser inserido.
     */
    public void addChild(Clique child) {
        children.add(child);
    }

    /**
     *  Retorna o filho na posição especificada.
     *
     *@return    filho na posição especificada
     */
    public Clique getChildAt(int index) {
        return (Clique)children.get(index);
    }


    /**
     *  Retorna o vetor de nós clusterizados.
     *
     *@return    vetor de nós clusterizados.
     */
    public NodeList getNos() {
        return nos;
    }


    /**
     *  Retorna o vetor de nós probabilísticos associados.
     *
     *@return    vetor de nós probabilísticos associados.
     */
    public NodeList getAssociatedProbabilisticNodes() {
        return nosAssociados;
    }

    /**
     *  Retorna o vetor de nós de utilidade associados.
     *
     *@return    vetor de nós de utilidade associados.
     */
    public NodeList getAssociatedUtilityNodes() {
        return associatedUtilNodes;
    }

    /**
     *  Retorna a tabela de potencial.
     *
     *@return    tabela de potencial.
     */
    public PotentialTable getPotentialTable() {
        return potentialTable;
    }

    /**
     *  Retorna a tabela de utilidade associada ao clique.
     *
     *@return    tabela de utilidade associada ao clique
     */
    public PotentialTable getUtilityTable() {
        return utilityTable;
    }
}