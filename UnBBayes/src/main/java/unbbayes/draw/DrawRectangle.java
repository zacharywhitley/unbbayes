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

public class DrawRectangle extends DrawElement {
	
	private Point2D.Double position;
	private Point2D.Double size;
	
	/**
	 * Constructs an DrawRectangle that will draw the rectangle 
	 * in the (x,y) position given. The (x,y) position represents the
	 * center of the rectangle.
	 * @param position The (x,y) position representing the center of the rectangle.
	 * @param size The width and height of the rectangle.
	 */
	public DrawRectangle(Point2D.Double position, Point2D.Double size) {
		this.position = position;
		this.size = size;
	}

	@Override
	public void paint(Graphics2D graphics) {
		
		graphics.setColor(getFillColor());
		graphics.fillRect((int)(position.x - size.x/2), (int)(position.y - size.y/2), (int)size.x, (int)size.y);
		
		if (isSelected()) {
			graphics.setColor(getSelectionColor());
			graphics.setStroke(new BasicStroke(2));
		} else {
			graphics.setColor(getOutlineColor());
		}
		graphics.drawRect((int)(position.x - size.x/2), (int)(position.y - size.y/2), (int)size.x, (int)size.y);
		graphics.setStroke(new BasicStroke(1));
        
		super.paint(graphics);
	}
	
}