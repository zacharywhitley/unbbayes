package unbbayes.gui.draw;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;

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
