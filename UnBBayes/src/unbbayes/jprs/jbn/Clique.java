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

package unbbayes.jprs.jbn;

import java.util.*;

import unbbayes.util.NodeList;

/**
 *  Classe que representa um Clique na �rvore de Jun��o (JunctionTree).
 *
 *@author    Michael e Rommel
 *@version   27 de Junho de 2001
 */
public class Clique implements ITabledVariable {

    /**
     *  Identifica unicamente o n�.
     */
    private int index;

    /**
     *  Refer�ncia para o clique pai.
     */
    private Clique parent;

    /**
     *  Lista de n�s filhos.
     */
    private List children;

    /**
     *  Tabela de Potencial Associada ao Clique.
     */
    private PotentialTable tabelaPot;

    /**
     *  Tabela de Utilidade Associada ao Clique.
     */
    private PotentialTable utilityTable;

    /**
     *  Lista de N�s Clusterizados.
     */
    private NodeList nos;

    /**
     *  Lista de N�s Probabil�sticos associados ao Clique.
     */
    private NodeList nosAssociados;

    /**
     *  Lista de N�s de Utilidade associados ao Clique.
     */
    private NodeList associatedUtilNodes;


    /**
     *  Constr�i um novo clique. Inicializa o vetor de filhos, de n�s clusterizados
     *  e de n�s associados. Inicializa o status associado para false.
     */
    public Clique() {
        children = new ArrayList();
        nos = new NodeList();
        nosAssociados = new NodeList();
        associatedUtilNodes = new NodeList();
        tabelaPot = new ProbabilisticTable();
        utilityTable = new UtilityTable();
    }


    /**
     *  Normaliza um clique da �rvore.
     *
     *@param  ok      vetor boolean de tamanho 1 para passar parametro por refer�ncia.
     *@return         constante de normaliza��o.
     */
    protected double normalize(boolean[] ok) {
        ok[0] = true;
        boolean fixo[] = new boolean[nos.size()];
        NodeList decisoes = new NodeList();
        for (int i = 0; i < nos.size(); i++) {
            if (nos.get(i) instanceof DecisionNode) {
                decisoes.add(nos.get(i));
                fixo[i] = true;
            }
        }

        if (decisoes.size() == 0) {
            return normalizeBN(ok);
        }

        int index[] = new int[decisoes.size()];
        for (int i = 0; i < index.length; i++) {
            index[i] = nos.indexOf(decisoes.get(i));
        }
        normalizeID(0, decisoes, fixo, index, new int[nos.size()], ok);
        /** @todo retornar a constante de normalizacao correta */
        return 0.0;
    }

    private double normalizeBN(boolean[] ok) {
        double n = 0.0;
        double valor;

        int sizeDados = tabelaPot.tableSize();
        for (int c = 0; c < sizeDados; c++) {
            n += tabelaPot.getValue(c);
        }
        if (Math.abs(n - 1.0) > 0.001) {
            for (int c = 0; c < sizeDados; c++) {
                valor = tabelaPot.getValue(c);
                if (n == 0.0) {
                    ok[0] = false;
                } else {
                    valor /= n;
                }

                tabelaPot.setValue(c, valor);
            }
        }
        return n;
    }


    private void normalizeID (int control,
                             NodeList decisoes,
                             boolean fixo[],
                             int index[],
                             int coord[],
                             boolean ok[]) {

        if (control == decisoes.size()) {
            double soma = sum(0, fixo, coord);
            if (soma == 0.0) {
//                ok[0] = false;
            } else {
                div(0, fixo, coord, soma);
            }
            return;
        }

        Node node = decisoes.get(control);
        for (int i = 0; i < node.getStatesSize(); i++) {
            coord[index[control]] = i;
            normalizeID(control+1, decisoes, fixo, index, coord, ok);
        }
    }

    private double sum(int control, boolean fixo[], int coord[]) {
        if (control == nos.size()) {
            return tabelaPot.getValue(coord);
        }

        if (fixo[control]) {
            return sum(control+1, fixo, coord);
        }

        Node node = nos.get(control);
        double retorno = 0.0;
        for (int i = 0; i < node.getStatesSize(); i++) {
            coord[control] = i;
            retorno += sum(control+1, fixo, coord);
        }
        return retorno;
    }

    private void div(int control, boolean fixo[], int coord[], double soma) {
        if (control == nos.size()) {
            int cLinear = tabelaPot.getLinearCoord(coord);
            tabelaPot.setValue(cLinear, tabelaPot.getValue(cLinear) / soma);
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
     * Retorna o �ndice associado ao clique.
     *
     * @return �ndice associado ao clique.
     */
    public int getIndex() {
        return index;
    }

    /**
     * Muda o �ndice do clique.
     *
     * @param indice frag que indica o status de associa��o
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
     *  Retorna o filho na posi��o especificada.
     *
     *@return    filho na posi��o especificada
     */
    public Clique getChildAt(int index) {
        return (Clique)children.get(index);
    }


    /**
     *  Retorna o vetor de n�s clusterizados.
     *
     *@return    vetor de n�s clusterizados.
     */
    public NodeList getNos() {
        return nos;
    }


    /**
     *  Retorna o vetor de n�s probabil�sticos associados.
     *
     *@return    vetor de n�s probabil�sticos associados.
     */
    public NodeList getAssociatedProbabilisticNodes() {
        return nosAssociados;
    }

    /**
     *  Retorna o vetor de n�s de utilidade associados.
     *
     *@return    vetor de n�s de utilidade associados.
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
        return tabelaPot;
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