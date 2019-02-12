/*
 *  UnBBayes
 *  Copyright (C) 2002, 2009 Universidade de Brasilia - http://www.unb.br
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
import java.awt.Cursor;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;

import unbbayes.prs.Node;

public class UShapeState extends UShape implements MouseMotionListener, MouseListener, Cloneable
{       
	/**
	 * 
	 */
	private static final long serialVersionUID = -6856101647287130364L;
	
	protected Rectangle2D rect;
	protected Rectangle   rectTextArea;
	protected float marginal;
	private float standardDeviation = 0f;
	private NumberFormat nf;

	private float stdevConfidenceIntervalMultiplier = 1.96f;
	
	public UShapeState(UCanvas s, Node pNode, int x, int y, int w, int h) 
	{
		super(s, pNode, x, y, w, h);
        
		setOpaque(false);
  
		
		nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(2);
		
		rectTextArea = new Rectangle(0,0,(int)(w*(0.70)), h );
		 
		InitShape() ;
    }    
	  
	public void	setMarginal( float d )
	{
		marginal = d;
	}
	
	public void InitShape() 
	{
		rect = new Rectangle2D.Double(0, 0,getWidth()-1,getHeight()-1);
	} 
	
	public void paintComponent(Graphics g) 
	{
		InitShape();
		
        Graphics2D g2 = (Graphics2D)g;
  
	    g2.setPaint( new GradientPaint( (int)(rectTextArea.getWidth()), 
	    								(int)(getHeight()/2), 
	    								Color.white, 
	    								(int)((getWidth())),
	    								(int)(getHeight()/2), 
	    								getBackColor(), 
	    								false));
	    
	    int marginalWidth = (int)((getWidth() -rectTextArea.getWidth())*marginal);
	    
	    g2.fillRect( (int)rectTextArea.getWidth(), 0,marginalWidth, getHeight());
	     
	    
	    // half of the confidence interval, assuming that marginals are normally distributed 
	    // interval is +- this value
	 	// (this is technically wrong, but it's reasonable quick'n'dirty approximation)
	    float confidenceInterval = getStdevConfidenceIntervalMultiplier()*getStandardDeviation();
	    
		int upperIntervalWidth = (int)((getWidth() -rectTextArea.getWidth())*confidenceInterval );
	    int lowerIntervalWidth = upperIntervalWidth;
	    if (lowerIntervalWidth > marginalWidth) {
	    	lowerIntervalWidth = marginalWidth;
	    }
	    g2.setPaint( new GradientPaint( (int)(rectTextArea.getWidth()) + marginalWidth, 
	    		(int)(getHeight()/2), 
	    		Color.white, 
	    		(int)(rectTextArea.getWidth()) + marginalWidth + upperIntervalWidth,  // (int)((getWidth())) - upperIntervalWidth,
	    		(int)(getHeight()/2), 
	    		Color.RED, 
	    		false));
		g2.fillRect( (int)rectTextArea.getWidth() + marginalWidth, 
	    			 0,
	    			 upperIntervalWidth, 
	    			 getHeight());
		g2.setPaint( new GradientPaint( (int)(rectTextArea.getWidth()) + marginalWidth - lowerIntervalWidth, 
				(int)(getHeight()/2), 
				Color.RED, 
				(int)(rectTextArea.getWidth()) + marginalWidth, // (int)((getWidth())) - lowerIntervalWidth,
				(int)(getHeight()/2), 
				Color.white, 
				false));
		g2.fillRect( (int)rectTextArea.getWidth() + (marginalWidth - lowerIntervalWidth), 
				0,
				lowerIntervalWidth, 
				getHeight());
	    
	    g2.setPaint( Color.BLUE );
	    	
	    g2.drawLine( (int)rectTextArea.getWidth(), (int)0, (int)rectTextArea.getWidth(), (int)getHeight());
	    g2.drawLine( (int)0, (int)0, (int)rect.getWidth(), (int)0);
  	 
	    g2.setPaint( getDrawColor() );
  	  	drawText(g, rectTextArea, getName(), TTYPE_LEFT);
	 
  	  	String probValueLabel = nf.format(marginal * 100.0);
  	  	
  	  	// append ± <some value> to the label, if applicable
  	  	if (getStandardDeviation() > 0f) {
			// append ± assuming that marginals are normally distributed 
			// (this is technically wrong, but it's reasonable quick'n'dirty approximation)
			probValueLabel += " � " + nf.format(confidenceInterval * 100f);
		}
  	  	
		drawText(g, rectTextArea, probValueLabel  + "%", TTYPE_RIGHT);
  	}
	
	public void mouseDragged(MouseEvent arg0) 
	{
//		Debug.println("UShapeSizeBtn_mouseDragged"); 
	}

	public void mouseMoved(MouseEvent arg0) 
	{
		setCursor(new Cursor(getCursorStyle()));
	}


	public void mouseClicked(MouseEvent arg0) { 
		getCanvas().onShapeChanged(this);
	}
 

	public void mousePressed(MouseEvent arg0) {

	}

	/**
	 * @return multiplier to be multiplied to standard deviation
	 * in order to calculate upper and lower bounds of confidence interval
	 * of marginal probabilities.
	 * For instance, 1.96 is used for 95% confidence interval of normal distributions.
	 * @see #paintComponent(Graphics)
	 */
	public float getStdevConfidenceIntervalMultiplier() {
		return stdevConfidenceIntervalMultiplier;
	}

	/**
	 * @param stdevConfidenceIntervalMultiplier :
	 * multiplier to be multiplied to standard deviation
	 * in order to calculate upper and lower bounds of confidence interval
	 * of marginal probabilities.
	 * For instance, 1.96 is used for 95% confidence interval of normal distributions.
	 */
	public void setStdevConfidenceIntervalMultiplier(
			float stdevConfidenceIntervalMultiplier) {
		this.stdevConfidenceIntervalMultiplier = stdevConfidenceIntervalMultiplier;
	}

	public float getStandardDeviation() {
		return standardDeviation;
	}

	public void setStandardDeviation(float standardDeviation) {
		this.standardDeviation = standardDeviation;
	}

 
}

