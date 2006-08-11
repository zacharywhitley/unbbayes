package unbbayes.gui.draw;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;

// TODO estudar no livro de design patter se o composite é realmente o melhor
// para essa situação que desejo!

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
