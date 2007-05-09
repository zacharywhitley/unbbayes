package unbbayes.draw;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

public class DrawRectangle extends DrawElement {
	
	private Point2D.Double position;
	private Point2D.Double size;
	
	/**
	 * Constructs an DrawRectangle that will draw the rectangle 
	 * in the (x,y) position given. The (x,y) position represents the
	 * center of the rectangle.
	 * @param position The (x,y) position representing the center of the rectangle.
	 * @param size The width and height of the rectangle.
	 */
	public DrawRectangle(Point2D.Double position, Point2D.Double size) {
		this.position = position;
		this.size = size;
	}

	@Override
	public void paint(Graphics2D graphics) {
		
		graphics.setColor(getFillColor());
		graphics.fillRect((int)(position.x - size.x/2), (int)(position.y - size.y/2), (int)size.x, (int)size.y);
		
		if (isSelected()) {
			graphics.setColor(getSelectionColor());
			graphics.setStroke(new BasicStroke(2));
		} else {
			graphics.setColor(getOutlineColor());
		}
		graphics.drawRect((int)(position.x - size.x/2), (int)(position.y - size.y/2), (int)size.x, (int)size.y);
		graphics.setStroke(new BasicStroke(1));
        
		super.paint(graphics);
	}
	
}