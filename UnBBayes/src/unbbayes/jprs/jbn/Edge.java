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

/**
 *  Classe que representa um arco entre n�s.
 *
 *@author     Michael e Rommel
 */
public class Edge implements java.io.Serializable {
    /**
     *  Guarda o primeiro n�. Quando h� orienta��o assume sem�ntica como origem.
     */
    private Node no1;

    /**
     *  Guarda o segundo n�. Quando h� orienta��o assume sem�ntica como destino.
     */
    private Node no2;

    /**
     *  Status de sele��o. Utilizado pela interface.
     */
    private boolean selecionado;


    /**
     *  Constr�i um arco que vai de no1 a no2.
     *
     *@param  no1  N� origem
     *@param  no2  N� destino
     */
    public Edge(Node no1, Node no2) {
        this.no1 = no1;
        this.no2 = no2;
    }


    /**
     *  Modifica o status de sele��o do arco.
     *
     *@param  selecionado  status de sele��o desejado.
     */
    public void setSelecionado(boolean selecionado) {
        this.selecionado = selecionado;
    }


    /**
     *  Retorna o primeiro n� associado ao arco.
     *
     *@return    o primeiro n� associado ao arco.
     */
    public Node getOriginNode() {
        return no1;
    }


    /**
     *  Retorna o segundo n� associado ao arco.
     *
     *@return    o segundo n� associado ao arco.
     */
    public Node getDestinationNode() {
        return no2;
    }


    /**
     *  Retorna o status de sele��o do arco.
     *
     *@return    status de sele��o.
     */
    public boolean isSelecionado() {
        return selecionado;
    }
}

