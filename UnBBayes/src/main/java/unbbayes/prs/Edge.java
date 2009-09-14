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
package unbbayes.prs;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import sun.java2d.loops.DrawLine;
import unbbayes.util.GeometricUtil;


/**
 *  This class represents an edge between two nodes.
 *
 *@author Michael Onishi
 *@author Rommel Carvalho
 */
public class Edge implements java.io.Serializable {
	
	private static Color color = Color.black;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3912210617648282346L;

	/**
     *  Guarda o primeiro n�. Quando h� orienta��o assume sem�ntica como origem.
     */
    private Node node1;

    /**
     *  Guarda o segundo n�. Quando h� orienta��o assume sem�ntica como destino.
     */
    private Node node2;

    /**
     *  Status de sele��o. Utilizado pela interface.
     */
    private boolean bSelected;
    
    /**
     *  Status que indica se existe ou n�o dire��o no arco. Utilizado pela interface.
     */
    private boolean direction;


    /**
     *  Creates an edge from node1 to node2.
     *
     *@param  node1  starting node
     *@param  node2  destination node
     */
    public Edge(Node no1, Node no2) {
        this.node1 = no1;
        this.node2 = no2;
        
        // assert node1 != node2 : "arco malfeito";
        direction = true;
    }

    
    /**
     *  Modifica o status de dire��o do arco.
     *
     *@param  direction  status de exist�ncia de dire��o.
     */
    public void setDirection(boolean direction) {
        this.direction = direction;
    }
  
    /**
     *  Modified by Young
     */
    public boolean getDirection() {
        return this.direction;
    }
    
    /**
     *  Retorna o primeiro n� associado ao arco.
     *
     *@return    o primeiro n� associado ao arco.
     */
    public Node getOriginNode() {
        return node1;
    }


    /**
     *  Retorna o segundo n� associado ao arco.
     *
     *@return    o segundo n� associado ao arco.
     */
    public Node getDestinationNode() {
        return node2;
    }
    
    /**
     *  Retorna o status de dire��o do arco.
     *
     *@return    status de dire��o.
     */
    public boolean hasDirection() {
        return direction;
    }
 
 	/**
 	 *  Muda a dire��o do arco. O pai vira filho e o filho vira pai
 	 */   
    public void changeDirection() {
    	// Faz a troca na lista de pais e filhos de node1 e node2
    	node1.getChildren().remove(node2);
	    node2.getParents().remove(node1);
	    node1.getParents().add(node2);
	    node2.getChildren().add(node1);
	    
	    // Faz a troca no pr�prio Edge
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
    
	public boolean isPointInDrawableArea(int x, int y) {
		double x1 = node1.getPosition().getX();
        double y1 = node1.getPosition().getY();
        double x2 = node2.getPosition().getX();
        double y2 = node2.getPosition().getY();;
        
        double yTeste = ((y2 - y1) / (x2 - x1)) * x + (y1 - x1 * ((y2 - y1) / (x2 - x1)));
        double xTeste = (y - (y1 - x1 * ((y2 - y1) / (x2 - x1)))) / ((y2 - y1) / (x2 - x1));

        //by young
        Point2D.Double ponto1 = GeometricUtil.getCircunferenceTangentPoint(node1.getPosition(), node2.getPosition(), (node1.getWidth() + node1.getHeight())/4);
        Point2D.Double ponto2 = GeometricUtil.getCircunferenceTangentPoint(node2.getPosition(), node1.getPosition(), (node1.getWidth() + node1.getHeight())/4);

        if (ponto1.getX() < ponto2.getX()) {
            if (((y <= yTeste + 5) && (y >= yTeste - 5)) || ((x <= xTeste + 5) && (x >= xTeste - 5))) {
                if (ponto1.getY() < ponto2.getY()) {
                    if ((y >= ponto1.getY() - 5) && (y <= ponto2.getY() + 5) && (x >= ponto1.getX() - 5) && (x <= ponto2.getX() + 5)) {
                        return true;
                    }
                }
                else {
                    if ((y >= ponto2.getY() - 5) && (y <= ponto1.getY() + 5) && (x >= ponto1.getX() - 5) && (x <= ponto2.getX() + 5)) {
                        return true;
                    }
                }
            }
        }
        else {
            if (((y <= yTeste + 5) && (y >= yTeste - 5)) || ((x <= xTeste + 5) && (x >= xTeste - 5))) {
                if (ponto1.getY() < ponto2.getY()) {
                    if ((y >= ponto1.getY() - 5) && (y <= ponto2.getY() + 5) && (x >= ponto2.getX() - 5) && (x <= ponto1.getX() + 5)) {
                        return true;
                    }
                }
                else {
                    if ((y >= ponto2.getY() - 5) && (y <= ponto1.getY() + 5) && (x >= ponto2.getX() - 5) && (x <= ponto1.getX() + 5)) {
                        return true;
                    }
                }
            }
        }
		return false;
	}
	
	public boolean isSelected() {
        return bSelected;
    }
	
	public Point2D.Double getOriginPosition() {
		return node1.getPosition();
	}
	
	public void setOriginPosition(double x, double y) {
		node1.setPosition(x, y);		
	}
	
	public Point2D.Double getDestinationPosition() {
		return node2.getPosition();
	}
	
	public void setDestinationPosition(double x, double y) {
		node2.setPosition(x, y);
		
	}

}

