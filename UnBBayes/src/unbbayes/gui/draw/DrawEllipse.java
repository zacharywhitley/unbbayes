package unbbayes.gui.draw;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

public class DrawEllipse extends DrawElement {
	
	private Point2D.Double position;
	private Point2D.Double size;
	
	/**
	 * Constructs an DrawEllipse that will draw the elipse 
	 * in the (x,y) position given. The (x,y) position represents the
	 * center of the ellipse.
	 * @param position The (x,y) position representing the center of the ellipse.
	 * @param size The width and height of the ellipse.
	 */
	public DrawEllipse(Point2D.Double position, Point2D.Double size) {
		this.position = position;
		this.size = size;
	}

	@Override
	public void paint(Graphics2D graphics) {
		
		graphics.setColor(getFillColor());
		graphics.fill(new Ellipse2D.Double(position.x - size.x/2, position.y - size.y/2, size.x, size.y));
		
		if (isSelected()) {
			graphics.setColor(getSelectionColor());
			graphics.setStroke(new BasicStroke(2));
		} else {
			graphics.setColor(getOutlineColor());
		}
		
		graphics.draw(new Ellipse2D.Double(position.x - size.x/2, position.y - size.y/2, size.x, size.y));
		graphics.setStroke(new BasicStroke(1));
		
		super.paint(graphics);
	}
	
}
