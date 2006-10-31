package unbbayes.gui.draw;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

public class DrawRectangleTwo extends DrawElement{
	
	private Point2D.Double position;
	private Point2D.Double size;
	
	/**
	 * 
	 */
	
	public DrawRectangleTwo(Point2D.Double position, Point2D.Double size) {
		this.position = position;
		this.size = size;
		this.setFillColor(Color.YELLOW); 
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
