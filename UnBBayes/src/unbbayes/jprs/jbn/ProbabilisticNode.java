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
import java.awt.Color;

import unbbayes.util.SetToolkit;

/**
 *  Representa variável probabilística.
 *
 *@author     Michael e Rommel
 */
public class ProbabilisticNode extends TreeVariable implements ITabledVariable, java.io.Serializable {

    private ProbabilisticTable tabelaPot;
    private static Color color = new Color(Color.yellow.getRGB());

    /** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.jprs.jbn.resources.JbnResources");

    /**
     * Inicializa a tabela de potenciais.
     */
    public ProbabilisticNode() {
        tabelaPot = new ProbabilisticTable();
    }


    public int getType() {
    	return PROBABILISTIC_NODE_TYPE;
    }

    /**
     *  Copia as características principais para o nó desejado
     *@param raio raio do nó.
     *@return cópia do nó
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
        no.setName(resource.getString("copyName") + this.getName());
        no.setDescription(resource.getString("copyName") + this.getDescription());
        no.tabelaPot = (ProbabilisticTable)this.tabelaPot.clone();
        return no;
    }
    
    public Object clone() {
    	ProbabilisticNode cloned = new ProbabilisticNode();
    	cloned.tabelaPot = (ProbabilisticTable)this.tabelaPot.clone();
		cloned.setColor(this.getColor().getRGB());
		cloned.setDescription(this.getDescription());
		cloned.setName(this.getName());
		cloned.setPosicao(this.getPosicao().getX(), this.getPosicao().getY());
		cloned.setParents(SetToolkit.clone(parents));
		cloned.setChildren(SetToolkit.clone(this.getChildren()));
		cloned.setStates(SetToolkit.clone(states));
		cloned.setAdjacents(SetToolkit.clone(this.getAdjacents()));
		cloned.setSelected(this.isSelecionado());
		cloned.setAltura(this.getAltura());
        cloned.setLargura(this.getLargura());
        cloned.setExplanationDescription(this.getExplanationDescription());
        cloned.setPhrasesMap(this.getPhrasesMap());
        cloned.setInformationType(this.getInformationType());
        double[] marginais = new double[this.getMarginais().length];
        System.arraycopy(this.getMarginais(), 0, marginais, 0, marginais.length);
        cloned.setMarginais(marginais);
        
        return cloned;
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
     * Calcula a marginal deste nó.
     */
    void marginal() {
        marginais = new double[getStatesSize()];
        PotentialTable auxTab = (PotentialTable) cliqueAssociado.getPotentialTable().clone();
        int index = auxTab.indexOfVariable(this);
        int size = cliqueAssociado.getPotentialTable().variableCount();
        for (int i = 0; i < size; i++) {
            if (i != index) {
                auxTab.removeVariable(cliqueAssociado.getPotentialTable().getVariableAt(i));
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
     * Sobrescreve o método da superclasse Node.
     *
     * @param estado estado a ser adicionado
     */
    public void appendState(String estado) {
        atualizaEstado(estado, true);
    }

    /**
     *  Retira o estado criado mais recentemente e
     *  atualiza as tabelas afetadas. Sobrescreve o método
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
     *  Retorna a cor do nó.
     *
     * @return cor dos nós probabilísticos.
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
