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

/**
 *  Classe que representa um arco entre nós.
 *
 *@author     Michael e Rommel
 */
public class Edge implements java.io.Serializable {
    /**
     *  Guarda o primeiro nó. Quando há orientação assume semântica como origem.
     */
    private Node no1;

    /**
     *  Guarda o segundo nó. Quando há orientação assume semântica como destino.
     */
    private Node no2;

    /**
     *  Status de seleção. Utilizado pela interface.
     */
    private boolean selecionado;


    /**
     *  Constrói um arco que vai de no1 a no2.
     *
     *@param  no1  Nó origem
     *@param  no2  Nó destino
     */
    public Edge(Node no1, Node no2) {
        this.no1 = no1;
        this.no2 = no2;
    }


    /**
     *  Modifica o status de seleção do arco.
     *
     *@param  selecionado  status de seleção desejado.
     */
    public void setSelecionado(boolean selecionado) {
        this.selecionado = selecionado;
    }


    /**
     *  Retorna o primeiro nó associado ao arco.
     *
     *@return    o primeiro nó associado ao arco.
     */
    public Node getOriginNode() {
        return no1;
    }


    /**
     *  Retorna o segundo nó associado ao arco.
     *
     *@return    o segundo nó associado ao arco.
     */
    public Node getDestinationNode() {
        return no2;
    }


    /**
     *  Retorna o status de seleção do arco.
     *
     *@return    status de seleção.
     */
    public boolean isSelecionado() {
        return selecionado;
    }
}

