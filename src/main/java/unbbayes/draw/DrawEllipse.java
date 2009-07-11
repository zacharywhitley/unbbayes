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
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.io.Serializable;

public class DrawEllipse extends DrawElement implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Point2D.Double position;
	private Point2D.Double size;
	
	/**
	 * Constructs an DrawEllipse that will draw the elipse 
	 * in the (x,y) position given. The (x,y) position represents the
	 * center of the ellipse.
	 * @param position The (x,y) position representing the center of the ellipse.
	 * @param size The width and height of the ellipse.
	 */
	public DrawEllipse(Point2D.Double position, Point2D.Double size) {
		this.position = position;
		this.size = size;
	}

	@Override
	public void paint(Graphics2D graphics) {
		
		graphics.setColor(getFillColor());
		graphics.fill(new Ellipse2D.Double(position.x - size.x/2, position.y - size.y/2, size.x, size.y));
		
		if (isSelected()) {
			graphics.setColor(getSelectionColor());
			graphics.setStroke(new BasicStroke(2));
		} else {
			graphics.setColor(getOutlineColor());
		}
		
		graphics.draw(new Ellipse2D.Double(position.x - size.x/2, position.y - size.y/2, size.x, size.y));
		graphics.setStroke(new BasicStroke(1));
		
		super.paint(graphics);
	}
	
}