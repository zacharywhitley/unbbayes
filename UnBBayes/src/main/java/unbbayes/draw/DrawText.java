package unbbayes.draw;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.text.AttributedString;

public class DrawText extends DrawElement implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String text;
	private Point2D.Double position;
	
	/**
	 * Constructs an DrawText that will draw the text 
	 * in the (x,y) position given.
	 * @param text The text to draw.
	 * @param position The (x,y) position to draw the text.
	 */
	public DrawText(String text, Point2D.Double position) {
		this.text = text;
		this.position = position;
	}

	@Override
	public void paint(Graphics2D graphics) {
		
		super.paint(graphics);
		
		AttributedString as = new AttributedString(text);
        Font serifFont = new Font("Serif", Font.PLAIN, 12);
        FontRenderContext frc = new FontRenderContext(new AffineTransform(), true, true);
        double alt = serifFont.getStringBounds(text, frc).getHeight();
        double lar = serifFont.getStringBounds(text, frc).getWidth();
        as.addAttribute(TextAttribute.FONT, serifFont);
        as.addAttribute(TextAttribute.FOREGROUND, Color.black);
        
        graphics.setColor(getFillColor());
        graphics.drawString(as.getIterator(), (int)(position.x - lar/2), (int)(position.y + alt/2));
        
	}
	
	/**
	 * Set the text to be drawn.
	 * @param text The text to be drawn.
	 */
	public void setText(String text) {
		this.text = text;
	}
	
}
