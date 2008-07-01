/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
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
