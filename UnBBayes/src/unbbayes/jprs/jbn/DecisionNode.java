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
import java.awt.Color;

/**
 *  Variavel de decisao
 *
 *@author     Michael e Rommel
 */
public class DecisionNode extends TreeVariable {

    private static Color color = new Color(Color.orange.getRGB());

    /**
     * Inicializa a cor do n�.
     */
    public DecisionNode() {
    }

    /**
     *  Retorna a cor do n�.
     *
     */
    public static Color getColor() {
        return color;
    }

    void marginal() {
        marginais = new double[getStatesSize()];
        PotentialTable auxTab = (PotentialTable)((Clique)cliqueAssociado).getUtilityTable().clone();
        auxTab.directOpTab(cliqueAssociado.getPotentialTable(), PotentialTable.PRODUCT_OPERATOR);
        int index = auxTab.indexOfVariable(this);
        for (int i = 0; i < cliqueAssociado.getPotentialTable().variableCount(); i++) {
            if (i != index) {
                auxTab.removeVariable((Node)cliqueAssociado.getPotentialTable().getVariableAt(i));
            }
        }

        if (hasEvidence()) {
            marginais[getEvidence()] = auxTab.getValue(getEvidence());
        } else {
            int tableSize = auxTab.tableSize();
            for (int i = 0; i < tableSize; i++) {
                marginais[i] = auxTab.getValue(i);
            }
        }
    }

    /**
     *  Modifica a cor do n�.
     *
     *@param c O novo RGB da cor do n�.
     */
    public static void setColor(int c) {
        color = new Color(c);
    }
}