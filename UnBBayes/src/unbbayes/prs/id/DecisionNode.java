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

package unbbayes.prs.id;

import java.awt.Color;

import unbbayes.prs.bn.Clique;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.TreeVariable;
import unbbayes.util.SetToolkit;

/**
 *  Variavel de decisao
 *
 *@author     Michael e Rommel
 */
public class DecisionNode extends TreeVariable implements java.io.Serializable {

	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;	
	
    private static Color color = new Color(Color.orange.getRGB());

    /**
     * Inicializa a cor do nó.
     */
    public DecisionNode() {
    }
    
    public Object clone() {
    	DecisionNode cloned = new DecisionNode();
		DecisionNode.setColor(DecisionNode.getColor().getRGB());
		cloned.setDescription(this.getDescription());
		cloned.setName(this.getName());
		cloned.setPosition(this.getPosition().getX(), this.getPosition().getY());
		cloned.setParents(SetToolkit.clone(parents));
		cloned.setChildren(SetToolkit.clone(this.getChildren()));
		cloned.setStates(SetToolkit.clone(states));
		cloned.setAdjacents(SetToolkit.clone(this.getAdjacents()));
		cloned.setSelected(this.isSelected());
		DecisionNode.setHeight(DecisionNode.getHeight());
		DecisionNode.setWidth(DecisionNode.getWidth());
        cloned.setExplanationDescription(this.getExplanationDescription());
        cloned.setPhrasesMap(this.getPhrasesMap());
        cloned.setInformationType(this.getInformationType());
        float[] marginais = new float[super.marginais.length];
        System.arraycopy(super.marginais, 0, marginais, 0, marginais.length);
        cloned.marginais = marginais;
        
        return cloned;
    }
    
    
    public int getType() {
    	return DECISION_NODE_TYPE;    	
    }

    /**
     *  Retorna a cor do nó.
     *
     */
    public static Color getColor() {
        return color;
    }

    protected void marginal() {
        marginais = new float[getStatesSize()];
        PotentialTable auxTab = (PotentialTable)((Clique)cliqueAssociado).getUtilityTable().clone();
        auxTab.directOpTab(cliqueAssociado.getPotentialTable(), PotentialTable.PRODUCT_OPERATOR);
        int index = auxTab.indexOfVariable(this);
        for (int i = 0; i < cliqueAssociado.getPotentialTable().variableCount(); i++) {
            if (i != index) {
                auxTab.removeVariable(cliqueAssociado.getPotentialTable().getVariableAt(i));
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
     *  Modifica a cor do nó.
     *
     *@param c O novo RGB da cor do nó.
     */
    public static void setColor(int c) {
        color = new Color(c);
    }
}