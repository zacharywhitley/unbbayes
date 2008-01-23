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
package unbbayes.prs.bn;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ResourceBundle;

import unbbayes.draw.DrawEllipse;
import unbbayes.prs.Node;
import unbbayes.util.NodeList;
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
    private DrawEllipse drawEllipse;

    /** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.prs.bn.resources.BnResources");

    /**
     * Constructs a ProbabilisticNode with an initialized table and 
     * an incremented DrawElement.
     */
    public ProbabilisticNode() {
        tabelaPot = new ProbabilisticTable();
        // Here it is defined how this node is going to be drawn.
        // In the superclass, Node, it was already definied to draw text, here
        // we add the draw ellipse.
        drawEllipse = new DrawEllipse(position, size);
        drawElement.add(drawEllipse);
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
        float[] marginais = new float[super.marginais.length];
        System.arraycopy(super.marginais, 0, marginais, 0, marginais.length);
        cloned.marginais = marginais;
        
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
        
		NodeList clones[] = new NodeList[getChildren().size()];
		int indexes[] = new int[getChildren().size()];
        for (int i = 0; i < getChildren().size(); i++) {
        	if (getChildren().get(i).getType() == Node.DECISION_NODE_TYPE) {
        		continue;
        	}
       		PotentialTable auxTab = ((ITabledVariable) getChildren().get(i)).getPotentialTable();
            clones[i] = auxTab.cloneVariables();
            indexes[i] = auxTab.indexOfVariable(this);     
        }
        
        for (int i = 0; i < getChildren().size(); i++) {
        	if (getChildren().get(i).getType() == Node.DECISION_NODE_TYPE) {
        		continue;
        	}
            PotentialTable auxTab = ((ITabledVariable) getChildren().get(i)).getPotentialTable();
            int l = indexes[i];
            NodeList auxList = clones[i];            
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
        	if (getChildren().get(i).getType() == Node.DECISION_NODE_TYPE) {
        		continue;
        	}
            PotentialTable auxTab = ((ITabledVariable) getChildren().get(i)).getPotentialTable();
            int l = indexes[i];
            NodeList auxList = clones[i];         
            for (int k = l; k < auxList.size(); k++) {
                auxTab.addVariable(auxList.get(k));
            }
        }
    }

    /**
     *  Returns node's color.
     *
     * @return color of the probabilisti node.
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
	
	@Override
	public void setSelected(boolean b) {
		// Update the DrawEllipse selection state
		drawEllipse.setSelected(b);
		super.setSelected(b);
	}
	
	@Override
	public void paint(Graphics2D graphics) {
		if (getInformationType() == Node.DESCRIPTION_TYPE) {
			drawEllipse.setFillColor(getDescriptionColor());
    	} else if (getInformationType() == Node.EXPLANATION_TYPE) {
    		drawEllipse.setFillColor(getExplanationColor());
    	}
		super.paint(graphics);
	}

}