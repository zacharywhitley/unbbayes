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
import java.awt.Graphics2D;

import unbbayes.draw.DrawRectangle;
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
	
    /**
	 * 
	 */
	private static final long serialVersionUID = -4746720812779139329L;
	
	private static Color color = Color.orange;
	
	private DrawRectangle drawRectangle;

	/**
     * Constructs a DecisionNode with an incremented DrawElement.
     */
    public DecisionNode() {
    	// Here it is defined how this node is going to be drawn.
        // In the superclass, Node, it was already definied to draw text, here
        // we add the draw rectangle.
        drawRectangle = new DrawRectangle(position, size);
        drawElement.add(drawRectangle);
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
     *  Get the node's color.
     *	@return The node's color.
     */
    public static Color getColor() {
        return color;
    }
    
    /**
     *  Set the node's color.
     *
     *@param rgb The node's RGB color.
     */
    public static void setColor(int rgb) {
        color = new Color(rgb);
    }
    
    @Override
	public void setSelected(boolean b) {
		// Update the DrawEllipse selection state
		drawRectangle.setSelected(b);
		super.setSelected(b);
	}
    
    @Override
    public void paint(Graphics2D graphics) {
    	drawRectangle.setFillColor(getColor());
    	super.paint(graphics);
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
    
}