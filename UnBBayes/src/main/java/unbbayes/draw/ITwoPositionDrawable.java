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
