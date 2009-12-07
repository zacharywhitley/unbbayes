/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package unbbayes.prs.bn;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import unbbayes.prs.Node;
import unbbayes.util.SetToolkit;

/**
 *  Represents probabilistic variable.
 *
 *@author Michael Onishi
 *@author Rommel Carvalho
 */
public class ProbabilisticNode extends TreeVariable implements ITabledVariable, java.io.Serializable {
			
    /**
	 * 
	 */
	private static final long serialVersionUID = -8362313890037632119L;
	
	private ProbabilisticTable tabelaPot;
    private static Color descriptionColor = Color.yellow;
    private static Color explanationColor = Color.green;
    
    /** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.prs.bn.resources.BnResources");

    /**
     * Constructs a ProbabilisticNode with an initialized table and 
     * an incremented DrawElement.
     */
    public ProbabilisticNode() {
        tabelaPot = new ProbabilisticTable();
	  	//by young
		setColor(descriptionColor);
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
    	// TODO Rever esse m�todo para n�o precisar do raio.
        ProbabilisticNode no = new ProbabilisticNode();

        for (int i = 0; i < getStatesSize(); i++) {
            no.appendState(getStateAt(i));
        }
		ProbabilisticNode.setDescriptionColor(ProbabilisticNode.getDescriptionColor().getRGB());
		ProbabilisticNode.setExplanationColor(ProbabilisticNode.getExplanationColor().getRGB());
        no.setPosition(this.getPosition().getX() + 1.3 * raio, this.getPosition().getY() + 1.3 * raio);
        no.setName(resource.getString("copyName") + this.getName());
        no.setDescription(resource.getString("copyName") + this.getDescription());
        no.tabelaPot = (ProbabilisticTable)this.tabelaPot.clone();
        return no;
    }
    
    public Object clone() {
    	ProbabilisticNode cloned = new ProbabilisticNode();
    	cloned.tabelaPot = (ProbabilisticTable)this.tabelaPot.clone();
		ProbabilisticNode.setDescriptionColor(ProbabilisticNode.getDescriptionColor().getRGB());
		ProbabilisticNode.setExplanationColor(ProbabilisticNode.getExplanationColor().getRGB());
		cloned.setDescription(this.getDescription());
		cloned.setName(this.getName());
		cloned.setPosition(this.getPosition().getX(), this.getPosition().getY());
		cloned.setParents(SetToolkit.clone(parents));
		cloned.setChildren(SetToolkit.clone(this.getChildren()));
		cloned.setStates(SetToolkit.clone(states));
		cloned.setAdjacents(SetToolkit.clone(this.getAdjacents()));
		cloned.setSelected(this.isSelected());
        cloned.setExplanationDescription(this.getExplanationDescription());
        cloned.setPhrasesMap(this.getPhrasesMap());
        cloned.setInformationType(this.getInformationType());
        float[] marginais = new float[super.marginalList.length];
        System.arraycopy(super.marginalList, 0, marginais, 0, marginais.length);
        cloned.marginalList = marginais;
        
        return cloned;
    }
    
    public ProbabilisticNode basicClone() {
    	ProbabilisticNode cloned = new ProbabilisticNode();
		cloned.setDescription(this.getDescription());
		cloned.setName(this.getName());
		cloned.setPosition(this.getPosition().getX(), this.getPosition().getY());
		cloned.setStates(SetToolkit.clone(states));
        
        return cloned;
    }


    /**
     *  Returns the probabilistic table of this variable.
     *
     *@return    the CPT (potential table)
     */
    public PotentialTable getPotentialTable() {
        return tabelaPot;
    }


    /**
     * Calculates the margin of this node.
     */
    protected void marginal() {
    	initMarginalList();
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
            marginalList[i] = auxTab.getValue(i);
        }
    }


    /**
     * Inserts a new state and updates the affected tables.
     * Overrides Node's superclass method.
     *
     * @param state : a new state to be added.
     */
    public void appendState(String state) {
        updateState(state, true);
    }

    /**
     *  Removes the newest state and updates the affected tables. Overwrites a Node's
     *  superclass method.
     */
    public void removeLastState() {
        if (states.size() > 1) {
//            super.removeLastState();
            updateState(null, false);
        }
    }

    /**
     *  This method can be used to update the affected tables when inserting and removing
     *  new states.
     *
     *@param  state  state to be inserted / removed.
     *@param  isInsertion  true for insertion and false for remotion.
     */
    private void updateState(String state, boolean isInsertion) {
        int d = getStatesSize();
        if (d > 0) {
            while (d <= tabelaPot.tableSize()) {
                if (isInsertion) {
                    tabelaPot.addValueAt(d++, 0);
                } else {
                    tabelaPot.removeValueAt(d);
                }
                d += getStatesSize();
            }
        }        
        
        List<Node> clones[] = new ArrayList[getChildren().size()];
		int indexes[] = new int[getChildren().size()];
        for (int i = 0; i < getChildren().size(); i++) {
        	if (getChildren().get(i).getType() == Node.DECISION_NODE_TYPE || getChildren().get(i).getType() == Node.CONTINUOUS_NODE_TYPE) {
        		continue;
        	}
       		PotentialTable auxTab = ((ITabledVariable) getChildren().get(i)).getPotentialTable();
            clones[i] = auxTab.cloneVariables();
            indexes[i] = auxTab.indexOfVariable(this);     
        }
        
        for (int i = 0; i < getChildren().size(); i++) {
        	if (getChildren().get(i).getType() == Node.DECISION_NODE_TYPE || getChildren().get(i).getType() == Node.CONTINUOUS_NODE_TYPE) {
        		continue;
        	}
            PotentialTable auxTab = ((ITabledVariable) getChildren().get(i)).getPotentialTable();
            int l = indexes[i];
            List<Node> auxList = clones[i];            
            for (int k = auxList.size() - 1; k >= l; k--) {
                auxTab.removeVariable(auxList.get(k));
            }
        }
        
        if (isInsertion) {
          	super.appendState(state);
        } else {
        	super.removeLastState();        	
        }
       
        
        for (int i = 0; i < getChildren().size(); i++) {
        	if (getChildren().get(i).getType() == Node.DECISION_NODE_TYPE || getChildren().get(i).getType() == Node.CONTINUOUS_NODE_TYPE) {
        		continue;
        	}
            PotentialTable auxTab = ((ITabledVariable) getChildren().get(i)).getPotentialTable();
            int l = indexes[i];
            List<Node> auxList = clones[i];         
            for (int k = l; k < auxList.size(); k++) {
                auxTab.addVariable(auxList.get(k));
            }
        }
    }

    /**
     *  Returns node's color.
     *
     * @return color of the probabilistic node.
     */
    public static Color getDescriptionColor() {
        return descriptionColor;
    }

    /**
     *  Changes the description node's color.
     *
     *@param c RGB value of the new color.
     */
    public static void setDescriptionColor(int c) {
        descriptionColor = new Color(c);
    }
    
    /**
     *  Changes the explanation's node's color.
     *
     *@param c RGB value of the new color.
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
