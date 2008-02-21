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
package unbbayes.draw;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import unbbayes.util.GeometricUtil;

public class DrawArrow extends DrawElement {

	private Point2D.Double startPosition;
	private Point2D.Double endPosition;
	private Point2D.Double size;
	private boolean bNew;
	
	/**
	 * Constructs an DrawArrow that will draw the arrow  
	 * given start and end positions of a line.
	 * @param startPosition The (x,y) start position of the line.
	 * @param endPosition The (x,y) end position of the line.
	 * @param size The width and height (x,y) of the circunference to calculate the tangent point.
	 */
	public DrawArrow(Point2D.Double startPosition, Point2D.Double endPosition, Point2D.Double size) {
		this.startPosition = startPosition;
		this.endPosition = endPosition;
		this.size = size;
	}

	@Override
	public void paint(Graphics2D graphics) {
		
		GeneralPath arrow = new GeneralPath();
		Point2D.Double point1;
		Point2D.Double point2;
		double x1 = startPosition.x;
        double y1 = startPosition.y;
        double x2 = endPosition.x;
        double y2 = endPosition.y;
        double x3;
        double y3;
        double x4;
        double y4;

        // ponta da seta = ponto correspondente na circunfer�ncia do n�, caso a seta j� esteja inserida - base da seta deslecada de 10 do centro do n�
        if (!isNew()) {
        	point1 = GeometricUtil.getCircunferenceTangentPoint(endPosition, startPosition, (size.x + size.y)/4 + 10);
        	point2 = GeometricUtil.getCircunferenceTangentPoint(endPosition, startPosition, (size.x + size.y)/4);
        	x2 = point2.x;
        	y2 = point2.y;
            
        }
        //ponta do seta = ponto na ponta do mouse - base da seta deslecada de 10 da ponta do mouse
        else {
        	point1 = GeometricUtil.getCircunferenceTangentPoint(endPosition, startPosition, 10);
        }

        // Use this first 4 equations if we are in the 2o or 4o quadrant
        if (((x1 > x2) && (y1 > y2)) || ((x1 < x2) && (y1 < y2))) {
            x3 = point1.x + 5 * Math.abs(Math.cos(Math.atan((x2 - x1) / (y1 - y2))));
            y3 = point1.y - 5 * Math.abs(Math.sin(Math.atan((x2 - x1) / (y1 - y2))));
            x4 = point1.x - 5 * Math.abs(Math.cos(Math.atan((x2 - x1) / (y1 - y2))));
            y4 = point1.y + 5 * Math.abs(Math.sin(Math.atan((x2 - x1) / (y1 - y2))));
        }
        // Use this last 4 equations if we are in the 1o or 3o quadrant
        else {
            x3 = point1.x + 5 * (Math.cos(Math.atan((x1 - x2) / (y1 - y2))));
            y3 = point1.y - 5 * (Math.sin(Math.atan((x1 - x2) / (y1 - y2))));
            x4 = point1.x - 5 * (Math.cos(Math.atan((x1 - x2) / (y1 - y2))));
            y4 = point1.y + 5 * (Math.sin(Math.atan((x1 - x2) / (y1 - y2))));
        }

        // Draw the arrow path with the points obtained above.
        arrow.moveTo((float) (x3), (float) (y3));
        arrow.lineTo((float) (x2), (float) (y2));
        arrow.lineTo((float) (x4), (float) (y4));
        arrow.lineTo((float) (x3), (float) (y3));
		
		if (isSelected()) {
			graphics.setColor(getSelectionColor());
			graphics.setStroke(new BasicStroke(2));
		} else {
			graphics.setColor(getFillColor());
		}
		graphics.fill(arrow);
		graphics.setStroke(new BasicStroke(1));
        
		super.paint(graphics);
	}
	
	/**
	 * Set the arrow as a new one.
	 * @param bNew True if it is a new arrow or false if it is an existing one.
	 */
	public void setNew(boolean bNew) {
		this.bNew = bNew;
	}
	
	/**
	 * Returns if the arrow is a new one.
	 * @return True if it is a new arrow or false if it is an existing one.
	 */
	public boolean isNew() {
		return bNew;
	}
	
}