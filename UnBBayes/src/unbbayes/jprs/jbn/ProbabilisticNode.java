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
 *  Representa vari�vel probabil�stica.
 *
 *@author     Michael e Rommel
 */
public class ProbabilisticNode extends TreeVariable implements ITabledVariable {

    private ProbabilisticTable tabelaPot;
    private static Color color = new Color(Color.yellow.getRGB());

    /**
     * Inicializa a tabela de potenciais.
     */
    public ProbabilisticNode() {
        tabelaPot = new ProbabilisticTable();
    }

    /**
     *  Copia as caracter�sticas principais para o n� desejado
     *@param raio raio do n�.
     *@return c�pia do n�
     */
    public ProbabilisticNode clone(double raio) {
        ProbabilisticNode no = new ProbabilisticNode();

        for (int i = 0; i < getStatesSize(); i++) {
            no.appendState(getStateAt(i));
        }
        no.setAltura(this.getAltura());
        no.setColor(this.getColor().getRGB());
        no.setLargura(this.getLargura());
        no.setPosicao(this.getPosicao().getX() + 1.3 * raio, this.getPosicao().getY() + 1.3 * raio);
        no.setName("C�pia do " + this.getName());
        no.setDescription("C�pia do " + this.getDescription());
        no.tabelaPot = (ProbabilisticTable)this.tabelaPot.clone();
        return no;
    }


    /**
     *  Retorna a tabela de potencial desta variavel.
     *
     *@return    tabela de potencial
     */
    public PotentialTable getPotentialTable() {
        return tabelaPot;
    }


    /**
     * Calcula a marginal deste n�.
     */
    void marginal() {
        marginais = new double[getStatesSize()];
        PotentialTable auxTab = (PotentialTable) cliqueAssociado.getPotentialTable().clone();
        int index = auxTab.indexOfVariable(this);
        for (int i = 0; i < cliqueAssociado.getPotentialTable().variableCount(); i++) {
            if (i != index) {
                auxTab.removeVariable((Node)cliqueAssociado.getPotentialTable().getVariableAt(i));
            }
        }

        int tableSize = auxTab.tableSize();
        for (int i = 0; i < tableSize; i++) {
            marginais[i] = auxTab.getValue(i);
        }

        /*
        int[] coord;
        int c;
        PotentialTable auxTab;

        marginais = new double[getStatesSize()];

        auxTab = cliqueAssociado.getTabelaPot();

        int index = auxTab.indexOfVariable(this);
        int tableSize = auxTab.tableSize();
        for (c = 0; c < tableSize; c++) {
            coord = auxTab.voltaCoord(c);
            marginais[coord[index]] += auxTab.getValue(c);
        }
        */
    }


    /**
     * Insere um novo estado e atualiza as tabelas afetadas.
     * Sobrescreve o m�todo da superclasse Node.
     *
     * @param estado estado a ser adicionado
     */
    public void appendState(String estado) {
        atualizaEstado(estado, true);
    }

    /**
     *  Retira o estado criado mais recentemente e
     *  atualiza as tabelas afetadas. Sobrescreve o m�todo
     *  da superclasse Node.
     */
    public void removeLastState() {
        if (states.size() > 1) {
            super.removeLastState();
            atualizaEstado(null, false);
        }
    }

    /**
     *  Utilizado para atualizar as tabelas afetadas
     *  ao inserir e remover novos estados.
     *
     *@param  estado  estado a ser inserido / removido.
     *@param  insere  true se for para inserir e false se for para remover.
     */
    private void atualizaEstado(String estado, boolean insere) {
        int d = getStatesSize();
        if (d > 0) {
            while (d <= tabelaPot.tableSize()) {
                if (insere) {
                    tabelaPot.addValueAt(d++, 0.0);
                } else {
                    tabelaPot.removeValueAt(d);
                }
                d += getStatesSize();
            }
        }
        if (insere) {
            super.appendState(estado);
        }

        for (int c = 0; c < getChildren().size(); c++) {
            PotentialTable auxTab = ((ProbabilisticNode)getChildren().get(c)).getPotentialTable();
            int l = auxTab.indexOfVariable(this);
            List auxList = (List) auxTab.cloneVariables();
            for (int k = auxList.size() - 1; k >= l; k--) {
                auxTab.removeVariable((ProbabilisticNode) auxList.get(k));
            }
            for (int k = l; k < auxList.size(); k++) {
                auxTab.addVariable((ProbabilisticNode) auxList.get(k));
            }
            auxList.clear();
        }
    }

    /**
     *  Retorna a cor do n�.
     *
     * @return cor dos n�s probabil�sticos.
     */
    public static Color getColor() {
        return color;
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
