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
