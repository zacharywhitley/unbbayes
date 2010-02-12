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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import unbbayes.prs.Edge;
import unbbayes.prs.Node;
import unbbayes.util.GeometricUtil;


public class UShapeLine extends UShape  implements IEdgeHolderShape
{    
	/**
	 * 
	 */
	private static final long serialVersionUID = -783952885037303240L;
	
	protected Line2D line;
	protected UShape shapeSource;	
	protected UShape shapeTarget;
	protected Point2D.Double pSource;
	protected Point2D.Double pTarget;
	protected boolean useSelection;
	private int direction = 0;
	protected Edge edge;
	protected JPopupMenu popupLine = new JPopupMenu();
	protected GeneralPath parallelogram;
	
	public UShapeLine(UCanvas c, UShape source, UShape target)
	{
		super(c, null, 0,0,0,0); 
		
		setUseSelection(false);
				
		shapeSource = source;
		shapeTarget = target;
		
		source.addFriend( this );
		target.addFriend( this );
		
		pSource = new Point2D.Double(0,0);
		pTarget = new Point2D.Double(0,0);
		
		line = new Line2D.Double(0,0,0,0);
		
		update();
		
		JMenuItem item = new JMenuItem("Line Size: 1");
		item.addActionListener
		(	
			new ActionListener() 
			{
				public void actionPerformed(ActionEvent ae)
				{   
					setStroke(stroke1);
					repaint();
				}
			}
		);
		
 	    JMenuItem item1 = new JMenuItem("Line Size: 2");
		item1.addActionListener
		(	
			new ActionListener() 
			{
				public void actionPerformed(ActionEvent ae)
				{   
					setStroke(stroke2);
					repaint();
				}
			}
		);
		
		JMenuItem item2 = new JMenuItem("Line Size: 3");
		item2.addActionListener
		(	
			new ActionListener() 
			{
				public void actionPerformed(ActionEvent ae)
				{    
					setStroke(stroke3);
					repaint();
				}
			}
		);
		 
		
		JMenuItem item4 = new JMenuItem("Line Shape: Dash");
		item4.addActionListener
		(	
			new ActionListener() 
			{
				public void actionPerformed(ActionEvent ae)
				{   
 
					setStroke(dashed);
					repaint();
				}
			}
		);
		
		JMenuItem item5 = new JMenuItem("Line Shape: Dash2");
		item5.addActionListener
		(	
			new ActionListener() 
			{
				public void actionPerformed(ActionEvent ae)
				{    
					setStroke(dashed2);
					repaint();
				}
			}
		);
		  
		popupLine.add(item);
		popupLine.add(item1);
		popupLine.add(item2); 
		popupLine.add(item4); 
		popupLine.add(item5); 
		
    }
	
	public void finalize() 
	{
		shapeSource.removeFriend( this );
		shapeTarget.removeFriend( this );
    }

	
	public UShapeLine(UCanvas c, int x, int y, int w, int h)
	{
		super(c, null, x, y, w, h);
    }     
 
	public void setEdge(Edge n) 
	{
		edge = n;
	}	
	
	public Edge getEdge() 
	{
		return edge;
	}
	
	public void setUseSelection(boolean b) 
	{
		useSelection = b;
	}	
	
	public boolean getUseSelection()  
	{
		return useSelection;
	}
	
	public UShape getSource()
	{
		return shapeSource;
	}
	
	public UShape getTarget()
	{
		return shapeTarget;
	}
	
	public void update()
	{
		//by young4
		super.update();
		
		pSource.x = 0;
		pSource.y = 0;
		pTarget.x = 0;
		pTarget.y = 0;
		
		if( shapeSource.checkExactEdge( null, shapeTarget, pSource, pTarget ) )
		{
			setBounds(  (int)Math.min(pSource.x, pTarget.x)-GAP, 
						(int)Math.min(pSource.y, pTarget.y)-GAP, 
						(int)Math.abs(pTarget.x-pSource.x)+GAP*2, 
						(int)Math.abs(pTarget.y-pSource.y)+GAP*2 );
			
			changeToLocalPosition(pSource);
			changeToLocalPosition(pTarget);
			
	
			//by young4
			//create wrap of line
		    double x1 = shapeSource.getCenterX()- getX();
		    double y1 = shapeSource.getCenterY()- getY();
		    double x2 = shapeTarget.getCenterX()- getX();
		    double y2 = shapeTarget.getCenterY()- getY();
		      
		    double newX =  5*Math.cos(Math.atan2(y2-y1, x2-x1))+x1;
			double newY =  5*Math.sin(Math.atan2(y2-y1, x2-x1))+y1;
					
		    double x4 = (-x1+newX)*Math.cos(-Math.PI/2) - (-y1+newY)*Math.sin(-Math.PI/2) + x1; 
		    double y4 = (-x1+newX)*Math.sin(-Math.PI/2) + (-y1+newY)*Math.cos(-Math.PI/2) + y1;
		    double x5 = (-x1+newX)*Math.cos(Math.PI/2) - (-y1+newY)*Math.sin(Math.PI/2) + x1; 
		    double y5 = (-x1+newX)*Math.sin(Math.PI/2) + (-y1+newY)*Math.cos(Math.PI/2) + y1;
		    	     
		    newX =  5*Math.cos(Math.atan2(y1-y2, x1-x2))+x2;
			newY =  5*Math.sin(Math.atan2(y1-y2, x1-x2))+y2;
					
		    double x6 = (-x2+newX)*Math.cos(-Math.PI/2) - (-y2+newY)*Math.sin(-Math.PI/2) + x2; 
		    double y6 = (-x2+newX)*Math.sin(-Math.PI/2) + (-y2+newY)*Math.cos(-Math.PI/2) + y2;
		    double x7 = (-x2+newX)*Math.cos(Math.PI/2) - (-y2+newY)*Math.sin(Math.PI/2) + x2; 
		    double y7 = (-x2+newX)*Math.sin(Math.PI/2) + (-y2+newY)*Math.cos(Math.PI/2) + y2;
		    
			parallelogram = new GeneralPath();
			parallelogram.moveTo((float)x4, (float)y4);
			parallelogram.lineTo((float)x5, (float)y5);  
			parallelogram.lineTo((float)x6, (float)y6);  
			parallelogram.lineTo((float)x7, (float)y7);
			parallelogram.closePath();  
			 
		}
		
		repaint();
	}
	 
	public void receiveMessage( String Msg )
	{ 
		if( Msg == STATE_MOVE )
		{		
			update();
		}
	} 
		
	public void paintComponent(Graphics g) 
	{
		super.paintComponent(g); 
		
		Graphics2D g2 = (Graphics2D) g;
				
		line.setLine(pSource.x, pSource.y, pTarget.x, pTarget.y);
		
		//g2.draw(parallelogram);
		
		g2.setStroke(getStroke());
	  	g2.draw(line);
	  	
	  	
 		if( getUseSelection() == true )
 		{
		  	if( this.getDirection() != 0 )
		  	{
		  		drawArrow(g2);
		  	}
 		}
	  	else
 		if( getUseSelection() == false )	  	
	  	{
	  		drawArrow(g2);
	  	}
		 
	}	
	
	public void drawArrow(Graphics2D g) 
	{
		GeneralPath arrow = new GeneralPath();
		Point2D.Double point1;
		Point2D.Double point2;
		double x1 = pSource.x;
	    double y1 = pSource.y;
	    double x2 = pTarget.x;
	    double y2 = pTarget.y;
	    double x3;
	    double y3;
	    double x4;
	    double y4;
	    Point2D.Double size = new Point2D.Double(2,2);
		    
	   /* if (!isNew()) 
	    {
	    	point1 = GeometricUtil.getCircunferenceTangentPoint(pTarget, pSource, (size.x + size.y)/4 + 10);
	    	point2 = GeometricUtil.getCircunferenceTangentPoint(pTarget, pSource, (size.x + size.y)/4);
	    	x2 = point2.x;
	    	y2 = point2.y;
	        
	    }	   
	    else */
	    {
	    	point1 = GeometricUtil.getCircunferenceTangentPoint(pTarget, pSource, 10);
	    }
	
	    // Use this first 4 equations if we are in the 2o or 4o quadrant
	    if (((x1 > x2) && (y1 > y2)) || ((x1 < x2) && (y1 < y2))) {
	        x3 = point1.x + 5 * Math.abs(Math.cos(Math.atan((x2 - x1) / (y1 - y2))));
	        y3 = point1.y - 5 * Math.abs(Math.sin(Math.atan((x2 - x1) / (y1 - y2))));
	        x4 = point1.x - 5 * Math.abs(Math.cos(Math.atan((x2 - x1) / (y1 - y2))));
	        y4 = point1.y + 5 * Math.abs(Math.sin(Math.atan((x2 - x1) / (y1 - y2))));
	    }
	    // Use this last 4 equations if we are in the 1o or 3o quadrant
	    else {
	        x3 = point1.x + 5 * (Math.cos(Math.atan((x1 - x2) / (y1 - y2))));
	        y3 = point1.y - 5 * (Math.sin(Math.atan((x1 - x2) / (y1 - y2))));
	        x4 = point1.x - 5 * (Math.cos(Math.atan((x1 - x2) / (y1 - y2))));
	        y4 = point1.y + 5 * (Math.sin(Math.atan((x1 - x2) / (y1 - y2))));
	    }
	
	    // Draw the arrow path with the points obtained above.
	    arrow.moveTo((float) (x3), (float) (y3));
	    arrow.lineTo((float) (x2), (float) (y2));
	    arrow.lineTo((float) (x4), (float) (y4));
	    arrow.lineTo((float) (x3), (float) (y3));
		
	    /*
		if (isSelected()) {
			graphics.setColor(getSelectionColor());
			graphics.setStroke(new BasicStroke(2));
		} else {
			graphics.setColor(getFillColor());
		}*/
		
		g.fill(arrow);
		g.setStroke(new BasicStroke(1));
	}
	
	public boolean contain(double x, double y) 
	{		
		return parallelogram.contains((double)(x-getGlobalX()), (double)(y-getGlobalY()));
	 	 
	}
	
	public void changeDirection() 
	{ 
		UShape shapeTemp1;
		UShape shapeTemp2;
		shapeTemp1 = shapeSource;
		shapeTemp2 = shapeTarget;
		shapeSource = shapeTemp2;
		shapeTarget = shapeTemp1;
		
		update();
	}
 
	/*
	 * (non-Javadoc)
	 * @see unbbayes.draw.UShape#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent arg0) {
		super.mousePressed(arg0);
		this.getCanvas().setShapeStateAll(STATE_NONE, null);
		this.setState(STATE_SELECTED, null);
		this.getCanvas().onShapeChanged(this);
		this.getCanvas().onSelectionChanged();
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.draw.UShape#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent arg0) 
	{ 
		System.out.println("Line mouseClicked"); 
		
//		this.setState(UShape.STATE_SELECTED, null);

	 	if (SwingUtilities.isLeftMouseButton(arg0)) 
	    {
	 		if( getUseSelection() == true )
	 		{
				if( edge != null )
				{/*
					if ( edge.getDirection() == false ) 
					{
						edge.setDirection(true);
						edge.changeDirection();
						changeDirection();
					} 
					else 
					if ( edge.getDirection() == true )
					{
						edge.setDirection(false);
						edge.changeDirection();
						changeDirection();
					}*/		
					
					if ((this.getDirection() == 0) || (this.getDirection() == 1)) {
			    		this.setDirection(this.getDirection() + 1);
			    		edge.setDirection(true);
			    		edge.changeDirection();
			    		changeDirection();
			    	} else if (this.getDirection() == 2) {
			    		this.setDirection(0);
			    		edge.setDirection(false);
			    		update();
			    	} 
				}
	 		}
	    } 
		 
	    if (SwingUtilities.isRightMouseButton(arg0)) 
	    {
	       	System.out.println("Right button released.");
	        
	       	popupLine.setEnabled(true);
//	       	popupLine.show(arg0.getComponent(),arg0.getX(),arg0.getY());
	       	popupLine.show(this.getCanvas(),arg0.getX(),arg0.getY());
	    }
	}

	/**
	 * @return the direction
	 */
	public int getDirection() {
		// force sincronization of UShapeLine's direction and edge's direction, since it was not being done at TAN...
		if (this.edge.hasDirection() == false) {
			direction = 0;
		} else {
			// there is direction, force sincronization
			direction = 1;
		} 
		return direction;
	}

	/**
	 * @param direction the direction to set
	 */
	public void setDirection(int direction) {
		this.direction = direction;
	} 
}
