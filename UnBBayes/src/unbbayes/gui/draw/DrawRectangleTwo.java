package unbbayes.gui.draw;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

/**
 * Construct a rectangle stylized for the context node 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 * @version 1.0 (06/10/31)
 */

public class DrawRectangleTwo extends DrawElement{
	
	private Point2D.Double position;
	private Point2D.Double size;
	
	/**
	 * @param position The (x,y) position representing the center of the rectangle.
	 * @param size The width and height of the rectangle.
	 */
	
	public DrawRectangleTwo(Point2D.Double position, Point2D.Double size) {
		this.position = position;
		this.size = size;
	}

	@Override
	public void paint(Graphics2D graphics) {
		
		graphics.setColor(getFillColor());
		
		GeneralPath parallelogram = new GeneralPath();

		parallelogram.moveTo((float)(position.x - size.x/2), (float)(position.y));
		parallelogram.lineTo((float)(position.x - (size.x*0.8)/2), (float)(position.y + size.y/2));  
		parallelogram.lineTo((float)(position.x + (size.x*0.8)/2), (float)(position.y + size.y/2));
		parallelogram.lineTo((float)(position.x + size.x/2), (float)(position.y));
		parallelogram.lineTo((float)(position.x + (size.x*0.8)/2), (float)(position.y - size.y/2));
		parallelogram.lineTo((float)(position.x - (size.x*0.8)/2), (float)(position.y - size.y/2));
		parallelogram.lineTo((float)(position.x - size.x/2), (float)(position.y));		
		
		graphics.fill(parallelogram);
		
		
		if (isSelected()) {
			graphics.setColor(getSelectionColor());
			graphics.setStroke(new BasicStroke(2));
		} else {
			graphics.setColor(getOutlineColor());
		}
		
		graphics.draw(parallelogram);
		graphics.setStroke(new BasicStroke(1));
		
		super.paint(graphics);
	}
	
} 
