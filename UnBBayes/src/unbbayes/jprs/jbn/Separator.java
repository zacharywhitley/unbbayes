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

import unbbayes.util.NodeList;

/**
 *  Representa um separador na Árvore de Junção (JunctionTree) entre cliques.
 *
 *@author     Michael e Rommel
 */
public class Separator implements ITabledVariable, java.io.Serializable {

    private PotentialTable tabelaPot;
    private PotentialTable utilityTable;
    private NodeList nos;

    /**
     *  Guarda o primeiro clique, quando há orientação assume semântica como origem.
     */
    private Clique clique1;

    /**
     *  Guarda o segundo clique, quando há orientação assume semântica como destino.
     */
    private Clique clique2;


    /**
     *  Constructor for the TSep object
     *
     * @param clique1 the origin clique
     * @param clique2 the destination clique
     */
    public Separator(Clique clique1, Clique clique2) {
        nos = new NodeList();
        tabelaPot = new ProbabilisticTable();
        utilityTable = new UtilityTable();
        this.clique1 = clique1;
        this.clique2 = clique2;
        clique2.setParent(clique1);
        clique1.addChild(clique2);
    }


    /**
     *  Insere um novo nó 1.
     *
     *@param  no1  novo nó 1.
     */
     /*
    public void setNo1(Clique no1) {
        this.no1 = no1;
    }


    /**
     *  Insere um novo nó 2.
     *
     *@param  no2  novo nó 2
     */
     /*
    public void setNo2(Clique no2) {
        this.no2 = no2;
    }
    */


    /**
     *  Insere uma nova lista de nós clusterizados.
     *
     *@param  nos  lista de nós clusterizados.
     */
    public void setNos(NodeList nos) {
        this.nos = nos;
    }


    /**
     *  Retorna a tabela de potencial associada ao separador.
     *
     *@return    tabela de potencial associada ao separador
     */
    public PotentialTable getPotentialTable() {
        return tabelaPot;
    }

    /**
     *  Retorna a tabela de utilidade associada ao separador.
     *
     *@return    tabela de utilidade associada ao separador
     */
    public PotentialTable getUtilityTable() {
        return utilityTable;
    }


    /**
     *  Retorna a lista de nós clusterizados.
     *
     *@return    nós clusterizados
     */
    public NodeList getNos() {
        return nos;
    }


    /**
     *  Retorna o primeiro nó.
     *
     *@return    nó 1
     */
    public Clique getNo1() {
        return clique1;
    }


    /**
     *  Retorna o segundo nó.
     *
     *@return    nó 2
     */
    public Clique getNo2() {
        return clique2;
    }

}

