package unbbayes.draw;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

/**
 * Representa um pentagono formado por um retangulo na base
 * e um triangulo encima:
 *              
 *             .
 *           .    .
 *           .    .
 *           ......
 *  
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 * @version 1.0 (06/10/31)
 */

public class DrawFlatPentagon extends DrawElement{
	
	private Point2D.Double position;
	private Point2D.Double size;
	
	/**
	 * @param position The (x,y) position representing the center of the rectangle (the triangle 
	 * have 1/3 of the higth of the retangle).
	 * @param size The width and height of the rectangle.
	 */
	
	public DrawFlatPentagon(Point2D.Double position, Point2D.Double size) {
		this.position = position;
		this.size = size;
	}

	@Override
	public void paint(Graphics2D graphics) {
		
		graphics.setColor(getFillColor());
		
		GeneralPath parallelogram = new GeneralPath();

		parallelogram.moveTo((float)(position.x - size.x/2), (float)(position.y + size.y/2));
		parallelogram.lineTo((float)(position.x - size.x/2), (float)(position.y - size.y/2));  
		parallelogram.lineTo((float)(position.x), (float)(position.y - (0.90)*size.y));  
		parallelogram.lineTo((float)(position.x + size.x/2), (float)(position.y - size.y/2));  
		parallelogram.lineTo((float)(position.x + size.x/2), (float)(position.y + size.y/2));  		
		parallelogram.closePath(); 
		
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
