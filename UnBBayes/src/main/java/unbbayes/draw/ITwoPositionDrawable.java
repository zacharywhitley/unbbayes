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

public interface ITwoPositionDrawable extends IDrawable {

	/**
	 * Set the origin position of the object.
	 * @param x The x position representing the origin of the object.
	 * @param y The y position representing the origin of the object.
	 */
	public void setOriginPosition(double x, double y);
	
	/**
	 * Get the origin position of the object.
	 * @return The origin position of the object.
	 */
	public Point2D.Double getOriginPosition();
	
	/**
	 * Set the destination position of the object.
	 * @param x The x position representing the destination of the object.
	 * @param y The y position representing the destination of the object.
	 */
	public void setDestinationPosition(double x, double y);
	
	/**
	 * Get the destination position of the object.
	 * @return The destination position of the object.
	 */
	public Point2D.Double getDestinationPosition();
	
	/**
	 * This method is responsible for telling the object that it is still being
	 * defined. Usually it is used because the destination point is not defined 
	 * yet, it is still changing.
	 * @param bNew True if it is new and false otherwise.
	 */
	public void setNew(boolean bNew);
	
}
