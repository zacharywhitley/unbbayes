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

import java.awt.Color;

/**
 *  Classe que representa variável de utilidade.
 *
 *@author     Michael e Rommel
 */
public class UtilityNode extends Node implements ITabledVariable {

    private PotentialTable utilTable;

    private static Color color = new Color(Color.green.getRGB());

    /**
     *  Construtor. Inicializa a tabela de potencial
     */
    public UtilityNode() {
        utilTable = new UtilityTable();
        states.add("Utility");
    }

    /**
     * Não faz nada ao se tentar inserir um estado, pois
     * variáveis de utilidade só aceitam 1 estado.
     */
    public void insereEstado() { }

    /**
     * Não faz nada ao se tentar inserir um estado, pois
     * variáveis de utilidade só aceitam 1 estado.
     */
    public void removeLastState() { }

    /**
     *  Gets the tabelaPot attribute of the TVU object
     *
     *@return    The tabelaPot value
     */
    public PotentialTable getPotentialTable() {
        return this.utilTable;
    }

    /**
     *  Retorna a cor do nó.
     *
     */
    public static Color getColor() {
        return color;
    }

    /**
     *  Modifica a cor do nó.
     *
     *@param c O novo RGB da cor do nó.
     */
    public static void setColor(int c) {
        color = new Color(c);
    }
}