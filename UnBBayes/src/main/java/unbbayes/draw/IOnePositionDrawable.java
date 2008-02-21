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

import java.awt.geom.Point2D;

public interface IOnePositionDrawable extends IDrawable {
	
	/**
	 * Set the center position of the object.
	 * @param x The x position representing the center of the object.
	 * @param y The y position representing the center of the object.
	 */
	public void setPosition(double x, double y);
	
	/**
	 * Get the center position of the object.
	 * @return The center position of the object.
	 */
	public Point2D.Double getPosition();

}
