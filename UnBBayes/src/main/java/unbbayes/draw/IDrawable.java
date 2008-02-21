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

import java.awt.Graphics2D;

/**
 * Every class that is going to be drawn in any way must implement this interface.
 * @author Rommel Carvalho
 *
 */
public interface IDrawable {
	
	/**
	 * This method is responsible for drawing the object.
	 * @param graphics
	 */
	public void paint(Graphics2D graphics);
	
	/**
	 * Set the object as selected.
	 * @param selected True if the object is selected and false otherwise.
	 */
	public void setSelected(boolean selected);
	
	/**
	 * This method states if this object is selected.
	 * @return true if selected and false otherwise.
	 */
	public boolean isSelected();
	
	/**
	 * This method is responsible to verify if the given point is inside the 
	 * drawable area of the drawable object.
	 * @param x The position's x value.
	 * @param y The position's y value.
	 * @return True if the point is inside the drawable area and false otherwise.
	 */
	public boolean isPointInDrawableArea(int x, int y);

	
}
