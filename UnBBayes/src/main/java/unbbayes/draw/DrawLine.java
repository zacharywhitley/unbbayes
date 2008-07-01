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
import java.awt.geom.Point2D;

import unbbayes.util.GeometricUtil;

public class DrawLine extends DrawElement {

	private Point2D.Double startPosition;
	private Point2D.Double endPosition;
	private Point2D.Double size;
	private boolean bNew;
	
	/**
	 * Constructs an DrawLine that will draw the line 
	 * in the given start and end positions.
	 * @param startPosition The (x,y) start position of the line.
	 * @param endPosition The (x,y) end position of the line.
	 * @param size The width and height (x,y) of the circunference to calculate the tangent point.
	 */
	public DrawLine(Point2D.Double startPosition, Point2D.Double endPosition, Point2D.Double size) {
		this.startPosition = startPosition;
		this.endPosition = endPosition;
		this.size = size;
	}

	@Override
	public void paint(Graphics2D graphics) {
		
		Point2D.Double point1 = GeometricUtil.getCircunferenceTangentPoint(startPosition, endPosition, (size.x + size.y)/4);
		Point2D.Double point2;
		if (!isNew()) {
			point2 = GeometricUtil.getCircunferenceTangentPoint(endPosition, startPosition, (size.x + size.y)/4);
		} else {
			point2 = endPosition;
		}
				
		if (isSelected()) {
			graphics.setColor(getSelectionColor());
			graphics.setStroke(new BasicStroke(2));
		} else {
			graphics.setColor(getFillColor());
		}
		graphics.drawLine((int)point1.x, (int)point1.y, (int)point2.x, (int)point2.y);
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