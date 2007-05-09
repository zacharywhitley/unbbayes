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
