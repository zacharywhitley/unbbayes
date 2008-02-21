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
package unbbayes.prs.id;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ResourceBundle;

import unbbayes.draw.DrawParallelogram;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ITabledVariable;
import unbbayes.prs.bn.PotentialTable;

/**
 *  This class represents the utility node.
 *
 *@author Michael Onishi 
 *@author Rommel Carvalho
 */
public class UtilityNode extends Node implements ITabledVariable, java.io.Serializable {

	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;
	
    private PotentialTable utilTable;

    private static Color color = Color.cyan;
    
    private DrawParallelogram drawParallelogram;
    
    /** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.prs.bn.resources.BnResources");

  	/**
     * Constructs a UtilityNode with an initialized table and 
     * an incremented DrawElement.
     */
    public UtilityNode() {
        utilTable = new UtilityTable();
        states.add(resource.getString("utilityName"));
        // Here it is defined how this node is going to be drawn.
        // In the superclass, Node, it was already definied to draw text, here
        // we add the draw parallelogram.
        drawParallelogram = new DrawParallelogram(position, size);
        drawElement.add(drawParallelogram);
    }
    
    
    public int getType() {
    	return UTILITY_NODE_TYPE;    	
    }

    /**
     * N�o faz nada ao se tentar inserir um estado, pois
     * vari�veis de utilidade s� aceitam 1 estado.
     */
    public void appendState(String state) { }

    /**
     * N�o faz nada ao se tentar inserir um estado, pois
     * vari�veis de utilidade s� aceitam 1 estado.
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
    	drawParallelogram.setSelected(b);
		super.setSelected(b);
	}
    
    @Override
    public void paint(Graphics2D graphics) {
    	drawParallelogram.setFillColor(getColor());
    	super.paint(graphics);
    }
}