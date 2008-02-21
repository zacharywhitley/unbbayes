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

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;

// TODO estudar no livro de design patter se o composite � realmente o melhor
// para essa situa��o que desejo!

/**
 * This class is the generic class for drawing an element (single or composite) 
 * in a Graphics2D.
 */
public abstract class DrawElement {
	
	private ArrayList<DrawElement> elements = new ArrayList<DrawElement>();
	private Color fillColor;
	private static Color outlineColor = Color.black;
	private static Color selectionColor = Color.red;
	private boolean bSelected;

	/**
	 * This method is responsible for drawing this element and any other element 
	 * related to this one (all elements).
	 * @param graphics Where the element is going to be drawn.
	 */	
	public void paint(Graphics2D graphics) {
		for (DrawElement element : elements) {
			element.paint(graphics);
		}
	}
	
	/**
	 * This method is responsible for adding an element to the set of 
	 * elements related to this one. It is to be used when dealing with 
	 * composite elements.
	 * @param element The element to be added.
	 */
	public void add(DrawElement element) {
		elements.add(element);
	}
	
	/**
	 * This method is responsible for removing an element from the set of 
	 * elements related to this one. It is to be used when dealing with 
	 * composite elements.
	 * @param element The element to be removed.
	 */	
	public void remove(DrawElement element) {
		elements.remove(element);
	}
	
	public boolean isSelected() {
		return bSelected;
	}

	public void setSelected(boolean selected) {
		bSelected = selected;
	}

	public Color getFillColor() {
		return fillColor;
	}

	public void setFillColor(Color fillColor) {
		this.fillColor = fillColor;
	}

	public static Color getOutlineColor() {
		return DrawElement.outlineColor;
	}

	public static void setOutlineColor(Color outlineColor) {
		DrawElement.outlineColor = outlineColor;
	}

	public static Color getSelectionColor() {
		return DrawElement.selectionColor;
	}

	public static void setSelectionColor(Color selectionColor) {
		DrawElement.selectionColor = selectionColor;
	}

}
