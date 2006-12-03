package unbbayes.gui.draw;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

/**
 * Representa um quadrilatero com a base inferior correspondendo 
 * a 70% do tamanho da base superior. 
 *  
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 * @version 1.0 (06/10/31)
 */


public class DrawTwoBaseRectangle extends DrawElement{
	private Point2D.Double position;
	private Point2D.Double size;
	
	/**
	 * @param position The (x,y) position representing the center of the rectangle (the triangle 
	 * have 1/3 of the higth of the retangle).
	 * @param size The width and height of the rectangle.
	 */
	
	public DrawTwoBaseRectangle(Point2D.Double position, Point2D.Double size) {
		this.position = position;
		this.size = size;
	}

	@Override
	public void paint(Graphics2D graphics) {
		
		graphics.setColor(getFillColor());
		
		GeneralPath parallelogram = new GeneralPath();

		parallelogram.moveTo((float)(position.x - 0.5*size.x), (float)(position.y - 0.5*size.y));
		parallelogram.lineTo((float)(position.x - 0.35*size.x),  (float)(position.y + 0.5*size.y)); 
		parallelogram.lineTo((float)(position.x + 0.35*size.x),  (float)(position.y + 0.5*size.y));  
		parallelogram.lineTo((float)(position.x + 0.5*size.x), (float)(position.y - 0.5*size.y));  		
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
