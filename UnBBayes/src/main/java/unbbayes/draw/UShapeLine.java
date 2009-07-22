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
import unbbayes.util.GeometricUtil;


public class UShapeLine extends UShape  
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
	protected int direction = 0;
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
			
		/*	parallelogram = new GeneralPath();
			parallelogram.moveTo((float)(pSource.x), (float)(pSource.y));
			parallelogram.lineTo((float)(-GAP + getWidth() - getWidth()*0.2), 	(float)(GAP));  
			parallelogram.lineTo((float)(getWidth()-GAP ), 					(float)(getHeight()-GAP));  
			parallelogram.lineTo((float)(GAP ), 							(float)(getHeight()-GAP));
			parallelogram.closePath();*/ 
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
		
		g2.setStroke(getStroke());
	  	g2.draw(line);
	  	
 		if( getUseSelection() == true )
 		{
		  	if( direction != 0 )
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
		return line.contains((double)(x-getGlobalX()), (double)(y-getGlobalY()));
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
	
	public void mouseClicked(MouseEvent arg0) 
	{ 
		System.out.println("Line mouseClicked"); 

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
					
					if ((direction == 0) || (direction == 1)) {
			    		direction++;
			    		edge.setDirection(true);
			    		edge.changeDirection();
			    		changeDirection();
			    	} else if (direction == 2) {
			    		direction = 0;
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
	       	popupLine.show(arg0.getComponent(),arg0.getX(),arg0.getY());
	    }
	} 
}
