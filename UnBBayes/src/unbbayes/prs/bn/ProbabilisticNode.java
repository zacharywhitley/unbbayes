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
package unbbayes.prs.bn;

import java.util.*;
import java.awt.Color;

import unbbayes.util.NodeList;
import unbbayes.util.SetToolkit;

/**
 *  Representa vari�vel probabil�stica.
 *
 *@author     Michael e Rommel
 */
public class ProbabilisticNode extends TreeVariable implements ITabledVariable, java.io.Serializable {

    private ProbabilisticTable tabelaPot;
    private static Color descriptionColor = new Color(Color.yellow.getRGB());
    private static Color explanationColor = new Color(Color.green.getRGB());

    /** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.prs.bn.resources.BnResources");

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
        no.setDescriptionColor(this.getDescriptionColor().getRGB());
        no.setExplanationColor(this.getExplanationColor().getRGB());
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
		cloned.setDescriptionColor(this.getDescriptionColor().getRGB());
		cloned.setExplanationColor(this.getExplanationColor().getRGB());
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
        float[] marginais = new float[super.marginais.length];
        System.arraycopy(super.marginais, 0, marginais, 0, marginais.length);
        cloned.marginais = marginais;
        
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
     * Calcula a marginal deste n�.
     */
    protected void marginal() {
        marginais = new float[getStatesSize()];
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
//            super.removeLastState();
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
                    tabelaPot.addValueAt(d++, 0);
                } else {
                    tabelaPot.removeValueAt(d);
                }
                d += getStatesSize();
            }
        }        
        
		NodeList clones[] = new NodeList[getChildren().size()];
		int indexes[] = new int[getChildren().size()];
        for (int i = 0; i < getChildren().size(); i++) {
        	PotentialTable auxTab = ((ProbabilisticNode)getChildren().get(i)).getPotentialTable();           
            clones[i] = auxTab.cloneVariables();
            indexes[i] = auxTab.indexOfVariable(this);     
        }
        
        for (int c = 0; c < getChildren().size(); c++) {
            PotentialTable auxTab = ((ProbabilisticNode)getChildren().get(c)).getPotentialTable();
            int l = indexes[c];
            NodeList auxList = clones[c];            
            for (int k = auxList.size() - 1; k >= l; k--) {
                auxTab.removeVariable(auxList.get(k));
            }
        }
        
        if (insere) {
          	super.appendState(estado);
        } else {
        	super.removeLastState();        	
        }
       
        
        for (int c = 0; c < getChildren().size(); c++) {
            PotentialTable auxTab = ((ProbabilisticNode)getChildren().get(c)).getPotentialTable();
            int l = indexes[c];
            NodeList auxList = clones[c];         
            for (int k = l; k < auxList.size(); k++) {
                auxTab.addVariable(auxList.get(k));
            }
        }
    }

    /**
     *  Retorna a cor do n�.
     *
     * @return cor dos n�s probabil�sticos.
     */
    public static Color getDescriptionColor() {
        return descriptionColor;
    }

    /**
     *  Modifica a cor do n� de descri��o.
     *
     *@param c O novo RGB da cor do n�.
     */
    public static void setDescriptionColor(int c) {
        descriptionColor = new Color(c);
    }
    
    /**
     *  Modifica a cor do n� de explana��o.
     *
     *@param c O novo RGB da cor do n�.
     */
    public static void setExplanationColor(int c) {
        explanationColor = new Color(c);
    }
    

	/**
	 * Gets the explanationColor.
	 * @return Returns a Color
	 */
	public static Color getExplanationColor() {
		return explanationColor;
	}

}
