package unbbayes.gui.draw;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

public class DrawDashRectangle extends DrawElement {

	private Point2D.Double startPosition;
	private Point2D.Double endPosition;
	
	/**
	 * Constructs an DrawDashRectangle that will draw a dash rectangle starting 
	 * at the begin position and ending at the end position.
	 * @param startPosition The (x,y) rectangle's start position.
	 * @param endPosition The (x,y) rectangle's end position.
	 */
	public DrawDashRectangle(Point2D.Double beginSelectionPoint, Point2D.Double endSelectionPoint) {
		this.startPosition = beginSelectionPoint;
		this.endPosition = endSelectionPoint;
	}

	@Override
	public void paint(Graphics2D graphics) {
		
		graphics.setColor(getFillColor());

		float [] dash = {10f, 10f};
		graphics.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 10f, dash, 10f));
		
        if ((startPosition.getX() <= endPosition.getX()) && (startPosition.getY() <= endPosition.getY())) {
        	graphics.drawRect((int)startPosition.getX(), (int)startPosition.getY(), (int)(endPosition.getX()-startPosition.getX()), (int)(endPosition.getY()-startPosition.getY()));
        } else {
            if ((startPosition.getX() > endPosition.getX()) && (startPosition.getY() <= endPosition.getY())) {
            	graphics.drawRect((int)endPosition.getX(), (int)startPosition.getY(), (int)(startPosition.getX()-endPosition.getX()), (int)(endPosition.getY()-startPosition.getY()));
            } else {
                if ((startPosition.getX() <= endPosition.getX()) && (startPosition.getY() > endPosition.getY())) {
                	graphics.drawRect((int)startPosition.getX(), (int)endPosition.getY(), (int)(endPosition.getX()-startPosition.getX()), (int)(startPosition.getY()-endPosition.getY()));
                } else {
                	graphics.drawRect((int)endPosition.getX(), (int)endPosition.getY(), (int)(startPosition.getX()-endPosition.getX()), (int)(startPosition.getY()-endPosition.getY()));
                }
            }
        }
        graphics.setStroke(new BasicStroke(1));
        
		super.paint(graphics);
	}
}
