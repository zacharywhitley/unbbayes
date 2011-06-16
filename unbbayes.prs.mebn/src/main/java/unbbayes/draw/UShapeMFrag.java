/*
 *  UnBBayes
 *  Copyright (C) 2002, 2009, 2011 Universidade de Brasilia - http://www.unb.br
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

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import unbbayes.prs.Node;

/**
 * Shape used for showing MFrag's content. This is used to show all MFrags in a 
 * given MTheory.
 * 
 * @author Rommel Carvalho (rommel.carvalho@gmail.com)
 * @version 1.0 06/18/2011 - (feature:3317031) First version of UShapeMFrag
 */
public class UShapeMFrag extends UShape implements INodeHolderShape {
	
	private static final long serialVersionUID = -4179123071455600926L;
	
	protected Rectangle2D rect;
	protected RoundRectangle2D roundRect;
	protected Rectangle rectTitle;
	protected RoundRectangle2D roundRectTitle;
	protected Line2D lineTitle;
	public int heightTitle = 30;
	
	public boolean showTitleBorder = true;
	public boolean showRoundBorder = true;
	public boolean showBodyBorder = true;
	
	protected BasicStroke thinStroke = new BasicStroke(1.0f);
	protected BasicStroke thickStroke = new BasicStroke(3.0f);
	
	public void setShowAll(boolean show) {
		showTitleBorder = show;
		showBodyBorder = show;
	}

	public UShapeMFrag(UCanvas c, Node n, int x, int y, int w, int h) {
		super(c, n, x, y, w, h);

		InitShape();
	}

	public void InitShape() {
		rect = new Rectangle2D.Double(GAP, GAP, getWidth() - GAP * 2 - 1,
				getHeight() - GAP * 2 - 1);
		
		roundRect = new RoundRectangle2D.Double(GAP, GAP, getWidth() - GAP * 2 - 1,
				getHeight() - GAP * 2 - 1, 2 * GAP, 2 * GAP);

		rectTitle = new Rectangle(GAP, GAP, getWidth() - GAP * 2 - 1,
				heightTitle);
		
		roundRectTitle = new RoundRectangle2D.Double(GAP, GAP, getWidth() - GAP * 2 - 1,
				heightTitle, 2 * GAP, 2 * GAP);
		
		lineTitle = new Line2D.Double(GAP, GAP + heightTitle, getWidth() - GAP - 1, GAP + heightTitle);
	}

	Font font1 = new Font("Helvetica", Font.PLAIN, 22);
	Font font2 = new Font("TimesRoman", Font.PLAIN, 20);
	Font font3 = new Font("Courier", Font.PLAIN, 18);

	Font font4 = new Font("Helvetica", Font.BOLD, 16);
	Font font5 = new Font("Helvetica", Font.ITALIC, 16);
	Font font6 = new Font("Helvetica", Font.BOLD + Font.ITALIC, 16);

	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		InitShape();

		Graphics2D g2 = (Graphics2D) g;
		g2.setStroke(thickStroke);
		if (showBodyBorder) {
			if (showRoundBorder) {
				g2.draw(roundRect);
			} else {
				g2.draw(rect);
			}
		}
		if (showTitleBorder) {
			g2.draw(lineTitle);
		}
		drawText(g, rectTitle);
		g2.setStroke(thinStroke);
		
	}

	public void update() {
		super.update();
		updateNodeInformation();
		InitShape();
		repaint();
	}

	public boolean contain(double x, double y) {
		return rectTitle.contains((double) (x - getGlobalX()),
				(double) (y - getGlobalY()));
	}
	
	public String toString() {
		return getNode().getName();
	}
}
