package unbbayes.gui.draw;


import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Representa um retangulo com as bordas arredondadas. 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 * @version 1.0 (06/10/31)
 */

public class DrawRoundedRectangle extends DrawElement{
	
	private Point2D.Double position;
	private Point2D.Double size;

	/**
	 * @param position The (x,y) position representing the center of the rectangle.
	 * @param size The width and height of the rectangle.
	 */	
	
	public DrawRoundedRectangle(Point2D.Double position, Point2D.Double size) {
		this.position = position;
		this.size = size;
	}

	@Override
	public void paint(Graphics2D graphics) {
		
		graphics.setColor(getFillColor());
		
		Rectangle2D.Double rectangle = new Rectangle2D.Double((double)(position.x - (size.x*0.8)/2), (double)(position.y - (size.y)/2), (double)(0.8*size.x), (double)(size.y)); 
		Ellipse2D.Double leftBorder = new Ellipse2D.Double((double)(position.x - size.x/2), (double)(position.y - size.y/2), (double)(0.2*size.x), (double)(size.y)); 
		Ellipse2D.Double rightBorder = new Ellipse2D.Double((double)(position.x + (size.x*0.8)/2) - (size.x*0.1), (double)(position.y - size.y/2), (double)(0.2*size.x), (double)(size.y)); 
				
		Area area = new Area(rectangle);
		area.add(new Area(leftBorder));
		area.add(new Area(rightBorder)); 

		graphics.fill(area); 
		
		if (isSelected()) {
			graphics.setColor(getSelectionColor());
			graphics.setStroke(new BasicStroke(2));
		} else {
			graphics.setColor(getOutlineColor());
		}
		
		graphics.draw(area);
		graphics.setStroke(new BasicStroke(1));
		
		super.paint(graphics);
	}
	
} 
