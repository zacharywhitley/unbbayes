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

package unbbayes.prs;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D.Double;

import unbbayes.gui.draw.DrawArrow;
import unbbayes.gui.draw.DrawLine;
import unbbayes.gui.draw.IDrawable;


/**
 *  This class represents an edge between two nodes.
 *
 *@author Michael Onishi
 *@author Rommel Carvalho
 */
public class Edge implements java.io.Serializable, IDrawable {
	
	private static Color color = Color.black;
	private DrawLine drawLine;
	private DrawArrow drawArrow;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3912210617648282346L;

	/**
     *  Guarda o primeiro nó. Quando há orientação assume semântica como origem.
     */
    private Node node1;

    /**
     *  Guarda o segundo nó. Quando há orientação assume semântica como destino.
     */
    private Node node2;

    /**
     *  Status de seleção. Utilizado pela interface.
     */
    private boolean bSelected;
    
    /**
     *  Status que indica se existe ou não direção no arco. Utilizado pela interface.
     */
    private boolean direction;


    /**
     *  Constrói um arco que vai de node1 a node2.
     *
     *@param  node1  Nó origem
     *@param  node2  Nó destino
     */
    public Edge(Node no1, Node no2) {
        this.node1 = no1;
        this.node2 = no2;
        
        // assert node1 != node2 : "arco malfeito";
        direction = true;
        
        // Here it is defined how this edge is going to be drawn.
        drawLine = new DrawLine(node1.getPosition(), node2.getPosition(), Node.getSize());
        drawArrow = new DrawArrow(node1.getPosition(), node2.getPosition(), Node.getSize());
        drawArrow.setNew(false);
        drawLine.add(drawArrow);
    }


    /**
     *  Modifica o status de seleção do arco.
     *
     *@param  bSelected  status de seleção desejado.
     */
    public void setSelected(boolean b) {
    	// Update the DrawArrow and DrawLine selection state
		drawArrow.setSelected(b);
		drawLine.setSelected(b);
        this.bSelected = b;
    }
    
    /**
     *  Modifica o status de direção do arco.
     *
     *@param  direction  status de existência de direção.
     */
    public void setDirection(boolean direction) {
        this.direction = direction;
    }


    /**
     *  Retorna o primeiro nó associado ao arco.
     *
     *@return    o primeiro nó associado ao arco.
     */
    public Node getOriginNode() {
        return node1;
    }


    /**
     *  Retorna o segundo nó associado ao arco.
     *
     *@return    o segundo nó associado ao arco.
     */
    public Node getDestinationNode() {
        return node2;
    }


    /**
     *  Retorna o status de seleção do arco.
     *
     *@return    status de seleção.
     */
    public boolean isSelected() {
        return bSelected;
    }
    
    /**
     *  Retorna o status de direção do arco.
     *
     *@return    status de direção.
     */
    public boolean hasDirection() {
        return direction;
    }
 
 	/**
 	 *  Muda a direção do arco. O pai vira filho e o filho vira pai
 	 */   
    public void changeDirection() {
    	// Faz a troca na lista de pais e filhos de node1 e node2
    	node1.getChildren().remove(node2);
	    node2.getParents().remove(node1);
	    node1.getParents().add(node2);
	    node2.getChildren().add(node1);
	    
	    // Faz a troca no próprio Edge
    	Node aux = node1;
    	node1 = node2;
    	node2 = aux;
    }
        

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "Edge: " + node1.toString() + " -> " + node2.toString();
	}


	public Double getPosition() {return null;}
	
	public void setPosition(double x, double y) {}
	
	/**
     *  Get the edge's color.
     *	@return The edge's color.
     */
    public static Color getColor() {
        return color;
    }
    
    /**
     *  Set the edge's color.
     *
     *@param rgb The edge's RGB color.
     */
    public static void setColor(int rgb) {
        color = new Color(rgb);
    }
    
	public void paint(Graphics2D graphics) {
		drawLine.setFillColor(getColor());
		drawArrow.setFillColor(getColor());
		drawLine.paint(graphics);
	}

	/**
	 * This method is responsible for telling the drawing class, DrawArrow, 
	 * to tell if the edge being drawn is new or not. This vaies the destination 
	 * point to draw the arrow.
	 * @param bNew True if it is new and false otherwise.
	 */
	public void setDrawNew(boolean bNew) {
		drawArrow.setNew(bNew);
		drawLine.setNew(bNew);
	}
	

}

