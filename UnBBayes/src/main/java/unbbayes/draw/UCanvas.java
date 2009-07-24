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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Line2D;

import javax.swing.JLayeredPane;

import unbbayes.prs.Node;
 
 
 
public class UCanvas extends JLayeredPane implements MouseMotionListener, MouseListener, ComponentListener, KeyListener

{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3124079297866124183L;
	protected int layerID = 0;
	UShape rootShape; 
	
 	public static final String STATE_NONE 			= "None";
	public static final String STATE_RESIZE_COMP 	= "ResizeComponents"; 
	public static final String STATE_CONNECT_COMP 	= "ConnectComponents";
	public static final String STATE_UPDATE 		= "Update"; 
	public static final String STATE_CTRL			= "Ctrl";
	
	
	public static final String NODE_ALIGN_BOTTOM	= "NodeAlignBottom";
	public static final String NODE_ALIGN_TOP		= "NodeAlignTop";
	public static final String NODE_ALIGN_LEFT		= "NodeAlignLeft";
	public static final String NODE_ALIGN_RIGHT		= "NodeAlignRight";	
 
	public String m_state;
	public Rectangle resizeRect;
	public Rectangle dragRect;
	public Rectangle selectRect;
	public Point dragPoint;
    public String strMode;
   
    public static final String MODE_NONE		= "None";
	public static final String MODE_USE_NAME	= "UseName";
	public static final String MODE_USE_DESC	= "UseDescription";

	//Test
	public UShape shapeTest;
	
	public UCanvas() 
	{
		super(); 
		
		this.setBackground(Color.white);
		this.setOpaque(true);
		 
		addMouseMotionListener(this);
		addMouseListener(this); 
		addComponentListener(this);
		addKeyListener(this); 
		 
		setFocusable(true); 
		
		setState(STATE_NONE); 
		
		dragPoint = new Point(0,0);
		dragRect = new Rectangle();
		selectRect= new Rectangle(); 
		
	
  	}
	
	public void setMode( String str )
	{
		strMode = str;
	}
	
	public String getMode()
	{
		return strMode;
	}
	
	public void setState(String s) 
	{ 		  
		m_state = s;
	}	
	
	public String getState() 
	{
		return m_state;
	} 
		
	public UShape getNodeUShape(Node n) 
	{
	 	int size = this.getComponentCount();
    	for( int i = 0; i < size; i++ )
    	{
    		UShape shapeResult = null;
    		UShape shape = (UShape)this.getComponent(i);
    		
    		shapeResult = getNodeUShape( shape, n );
    		if( shapeResult != null )
    			return shapeResult;
    		
    		if( shape.getNode() != null && shape.getNode() == n )
    			return shape;			
    	}
    	
    	return null;
	}
	
	public UShape getNodeUShape(UShape parent, Node n) 
	{
		if( parent == null ) return null;
		
	 	int size = parent.getComponentCount();
    	for( int i = 0; i < size; i++ )
    	{
    		UShape shape = (UShape)parent.getComponent(i);

    		if( shape != null )
    		if( shape.getNode() != null && shape.getNode() == n )
    			return shape;			
    	}
    	
    	return null;
	}
		
	public void fitCanvasSizeToAllUShapes()
	{
		Dimension s; 
    	Rectangle rc;
    	UShape shape;
    	int size;
		
		s = getSize();
	 	size = this.getComponentCount();
	 	
    	for( int i = 0; i < size; i++ )
    	{
    		shape = (UShape)this.getComponent(i);
    		
    		rc = shape.getBounds();
    		
    		s.width= Math.max(s.width, rc.x + rc.width);
    		s.height = Math.max(s.height, rc.y + rc.height);
    	}
    	
    	setPreferredSize(s);
		setSize(s);
	}
	
	public void DeleteSelectedShape()
	{
		int n = this.getComponentCount();
		
    	for( int i = 0; i < n; i++ )
    	{
    		UShape shape = (UShape)this.getComponent(i);
    		
    		if( shape.getState() == UShape.STATE_SELECTED )
    		{
    			onShapeDeleted(shape);
    			this.remove(shape);
    			DeleteSelectedShape();
    			repaint();
    			break;
    		}
    	}    
    
    	update(); 
	}
	 
	public void update()
	{
		
	}
	
	public void onResized( UShape s )
	{
		
	}
	
	public void onPositionChanged( UShape s )
	{
		Dimension dim; 
		dim = getSize();
	 	
		if(!(s instanceof UShapeLine))
		{
			if( s.getCenterX()+s.getWidth() > dim.width )
			{
				System.out.println(s.ID + "getCenterX=" + s.getCenterX());
				setPreferredSize(new Dimension( s.getCenterX()+s.getWidth(), dim.height));
				setSize(new Dimension( s.getCenterX()+s.getWidth(), dim.height));
			} 
			
			if( s.getCenterY()+s.getHeight() > dim.height )
			{
				System.out.println(s.ID + "getCenterY="+s.getCenterY());
				setPreferredSize(new Dimension( dim.width, s.getCenterY()+s.getHeight()));
				setSize(new Dimension( dim.width, s.getCenterY()+s.getHeight()));
			}
		}
	}
	 
	public void onResizeToFitText()
	{
		int n = this.getComponentCount();
		
    	for( int i = 0; i < n; i++ )
    	{
    		UShape shape = (UShape)this.getComponent(i);
    		
    		if( shape.getState() == UShape.STATE_SELECTED )
    		{
    			shape.resizeToFitText();
    			shape.repaint();
    		}
    	}
	}
	
	public void onShapeColorChanged( Color c )
	{
		int n = this.getComponentCount();
		
    	for( int i = 0; i < n; i++ )
    	{
    		UShape shape = (UShape)this.getComponent(i);
    		
    		if( shape.getState() == UShape.STATE_SELECTED )
    		{
    			shape.setBackColor(c);
    			shape.repaint();
    		}
    	}
	}
	 
	public void onShapeTypeChanged( String s )
	{
		int n = this.getComponentCount();
		
    	for( int i = 0; i < n; i++ )
    	{
    		UShape shape = (UShape)this.getComponent(i);
    		
    		if( shape.getState() == UShape.STATE_SELECTED )
    		{
    			shape.shapeTypeChange(s);
    			shape.repaint();
    		}
    	}
	}
		
	public void onShapeDeleted( UShape s )
	{
		
	}
	
	public void onShapeChanged( UShape s )
	{
		
	}
	
    public void onSelectionChanged()
    {
    }    
	     
	public void onAlignNodes(String str) 
	{		
		int left   = 1000;//this.getBounds().width;
		int right  = 0;
		int top    = this.getBounds().height;
		int bottom = 0;
		
		int n = this.getComponentCount();
    	for( int i = 0; i < n; i++ )
    	{
    		UShape shape = (UShape)this.getComponent(i);
    		
    		if( shape.getState() == UShape.STATE_SELECTED )
    		{
    			if( str == NODE_ALIGN_LEFT )
    				left = Math.min(left, shape.getCenterX());
    			else
    			if( str == NODE_ALIGN_RIGHT )
    				right = Math.max(right, shape.getCenterX());
    			else
    			if( str == NODE_ALIGN_TOP )
    				top = Math.min(top, shape.getCenterY());
    			else
    			if( str == NODE_ALIGN_BOTTOM )
    				bottom = Math.max(bottom, shape.getCenterY());    			
    		}
    	}
    	
    	for( int i = 0; i < n; i++ )
    	{
    		UShape shape = (UShape)this.getComponent(i);
    		
    		if( shape.getState() == UShape.STATE_SELECTED )
    		{
    			if( str == NODE_ALIGN_LEFT )
    				shape.move(left-shape.getWidth()/2, shape.getY());
    			else
    			if( str == NODE_ALIGN_RIGHT )
    				shape.move(right-shape.getWidth()/2, shape.getY());
    			else
    			if( str == NODE_ALIGN_TOP )
    				shape.move(shape.getX(), top-shape.getHeight()/2);
    			else
    			if( str == NODE_ALIGN_BOTTOM )
    				shape.move(shape.getX(), bottom-shape.getHeight()/2);    			
    		}
    	}
	 
	}
	
	public Node getSelectedShapesNode() 
	{
		UShape shape = getSelectedShape();
		
		if( shape != null )
			return shape.getNode();
			
    	return null;
	}
	
	public UShape getSelectedShape() 
	{
		int n = this.getComponentCount();
    	for( int i = 0; i < n; i++ )
    	{
    		UShape shape = (UShape)this.getComponent(i);
    		
    		if( shape.getState() == UShape.STATE_SELECTED )
    		{
    			return shape; 
    		}
    	}
    	
    	return null;
	}
 
	
    public void paintComponent(Graphics g) 
	{
		super.paintComponent(g);
	    Graphics2D g2 = (Graphics2D) g;
	    
	    //Test
	    //g.drawString(getState(),10,10);
	    //
	      
	    if(getState() == STATE_UPDATE)
		{    
	    	update(); 
	    	setState(STATE_NONE);
		}  		
		else
		if(getState() == STATE_RESIZE_COMP)
		{   
	    	int n = this.getComponentCount();
	    	for( int i = 0; i < n; i++ )
	    	{
	    		UShape shape = (UShape)this.getComponent(i);
	    		
	    		if( shape.getState() == UShape.STATE_SELECTED )
	    		{
	    			if( shape.checkLimitSize(resizeRect) )
	    			{	    			    
	    				g.setColor(new Color(200,200,250)); 
	    				g.fillRect(shape.getX()+resizeRect.x, shape.getY()+resizeRect.y, shape.getWidth()+ resizeRect.width, shape.getHeight()+ resizeRect.height);
	    			}
	    		}
	    	}
		}  		
		else
		if(getState() == STATE_CONNECT_COMP)
		{
			int n = this.getComponentCount();
			
	    	for( int i = 0; i < n; i++ )
	    	{
	    		UShape shape = (UShape)this.getComponent(i);
	    		
	    	 	if( shape.getState() == UShape.STATE_SELECTED && dragPoint.x > 0 && dragPoint.x > 0 )
	    		{ 	    			
	    			Line2D line = new Line2D.Double(shape.getCenterX(), shape.getCenterY(), dragPoint.x, dragPoint.y);
	    			
	    		    g2.setStroke(UShape.dashed);
	    		    g2.setColor(new Color(200,200,250)); 
    			  	g2.draw(line);
    			  	g2.setStroke(UShape.stroke1);
    			  	
	    		}
	    	}
		}
		else
		if(getState() == STATE_NONE)
		{   
		   	int n = this.getComponentCount();
		   	for( int i = 0; i < n; i++ )
		   	{
		   		UShape shape = (UShape)this.getComponent(i);
		    		
		   		if( shape.getState() == UShape.STATE_SELECTED )
		   		{
					shape.sendMessageToFriends(UShape.STATE_MOVE);
				}
			}
		
		   	//Draw Drag Box
			if( dragRect.x != 0 && dragRect.y != 0 )
			{ 
				selectRect.setBounds( (int)dragRect.getX(), (int)dragRect.getY(), (int)dragRect.getWidth(), (int)dragRect.getHeight() );
				 
				if( dragRect.getWidth() < 0 )  { selectRect.x = (int)(dragRect.getX()+ dragRect.getWidth());  selectRect.width  = (int)Math.abs( dragRect.getWidth() );}
				if( dragRect.getHeight() < 0 ) { selectRect.y = (int)(dragRect.getY()+ dragRect.getHeight()); selectRect.height = (int)Math.abs( dragRect.getHeight() );}
				 
				g.setColor(new Color(200,200,250));
				g.drawRect( (int)selectRect.getX(), (int)selectRect.getY(), (int)selectRect.getWidth(), (int)selectRect.getHeight() );
			}

		}  		
	}
     
    public void setShapeStateAll(String s) 
	{
    	int n = this.getComponentCount();
    	for( int i = 0; i < n; i++ )
    	{
    		UShape shape = (UShape)this.getComponent(i);
    		shape.setState(s);	
    		
    		setShapeStateAll(shape, s);
    	}
    	
    	repaint();
	}   
     
    public void setShapeStateAll(UShape parent, String s) 
	{
    	int n = parent.getComponentCount();
    	for( int i = 0; i < n; i++ )
    	{
    		UShape shape = (UShape)parent.getComponent(i);
    		shape.setState(s);			
    	}
    	
    	repaint();
	}   
    
    public void drawResizeRectEnter(Rectangle rc) 
	{
    	resizeRect = rc;
    	
    	setState(STATE_RESIZE_COMP);
    	 
    	repaint();
	}   
	  
    public void drawResizeRectReleased(Rectangle rc) 
	{
    	resizeRect = rc;
    	
    	int n = this.getComponentCount();
    	for( int i = 0; i < n; i++ )
    	{
    		UShape shape = (UShape)this.getComponent(i);
    		
    		if( shape.getState() == UShape.STATE_SELECTED )
    		{
    			if( shape.checkLimitSize(resizeRect) )
    			{
    				shape.setNewSize(shape.getX()+resizeRect.x, shape.getY()+resizeRect.y, shape.getWidth()+ resizeRect.width, shape.getHeight()+ resizeRect.height);
    			}
    		}
    	}
    	 
    	setState(STATE_NONE);
    	 
    	repaint();
	}   
    
    public void onShapeMoved( UShape shapeMoved, int x, int y)
    {
    	int n = this.getComponentCount();
    	for( int i = 0; i < n; i++ )
    	{
    		UShape shape = (UShape)this.getComponent(i);
    		
    		if( shape.getState() == UShape.STATE_SELECTED )
    		{ 
    			
    			if( shapeMoved != shape )
    			{
    				Point loc = shape.getLocation(); 
    				
    				shape.setNewSize(loc.x + x, loc.y + y, shape.getWidth(), shape.getHeight());
    			}
    		}
    	}
    }
    
    public void onDrawConnectLineEnter( int x, int y)
    {
		if(getState() == STATE_CONNECT_COMP)
		{
			dragPoint.x = x;
			dragPoint.y = y;
			repaint();
		}    
    }
    
    public UShapeLine onDrawConnectLineReleased( UShape shapeParent, int x, int y)
    {
    	UShapeLine line = null;
    	
    	if(getState() == STATE_CONNECT_COMP)
		{ 		
    		UShape shapeTarget;
    		
    		shapeTarget = GetShape(x, y);
    		
    		if( shapeTarget != null )
    		{    		
    			line = connectLinesForAllSelectedShape( null, shapeTarget );
    		}
    		
    		dragPoint.x = 0;
    		dragPoint.y = 0;    		 
		 }  
    	
    	repaint();
    	
    	return line;
    }
    
    public UShapeLine connectLinesForAllSelectedShape( UShape shapeParent, UShape shapeTarget )
    {
    	UShapeLine line = null;
    	
		int n;
		
		if( shapeParent != null )
			n = shapeParent.getComponentCount();
		else
			n = this.getComponentCount();
		
    	for( int i = 0; i < n; i++ )
    	{
    		UShape shape;
    		
    		if( shapeParent != null )
    			shape = (UShape)shapeParent.getComponent(i);
    		else
    			shape = (UShape)this.getComponent(i);
    		
    		line = connectLinesForAllSelectedShape(shape, shapeTarget );
    		
    		if( line != null )
    			return line;
    		
    		if( shape.getState() == UShape.STATE_SELECTED )
    		{
    			line = new UShapeLine(this, shape, shapeTarget);
    			addShape(line);
    			return line;
    		}
    	}	
    	
    	return line;
    }
      
    public UShape GetShape( int x, int y )
    {    	
    	UShape shapeResult; 
    		
    	shapeResult = GetShape( null, x, y);
    		
    	if( shapeResult != null )
    		return shapeResult;  		
       	
    	return null;
    }
    

	public UShape GetShape( UShape parent, int x, int y )
    {    	
    	int n;
    	
    	if( parent == null )
    		n = this.getComponentCount();
    	else
    		n = parent.getComponentCount();
    	
    	for( int i = 0; i < n; i++ )
    	{
    		UShape shape; 
    		UShape shapeChild; 
    	
    		if( parent == null )
    			shape = (UShape)this.getComponent(i);
        	else
        		shape = (UShape)parent.getComponent(i);
    		
    		shapeChild = GetShape( shape, x, y );
    		
    		if( shapeChild != null )
    			return shapeChild;
    		    		
    		if( shape.getState() == UShape.STATE_NONE )
    		{
    			if(shape.contain(x, y))   			
    				return shape;
    		}
       	}
    	
    	return null;
    }
    
	public void checkSelectArea() 
	{ 
	  	int n = this.getComponentCount();
    	for( int i = 0; i < n; i++ )
    	{
    		UShape shape = (UShape)this.getComponent(i);
    		
    		if( shape.getState() == UShape.STATE_NONE )
    		{
    			if(shape.isContained(selectRect))  
    			{
    				shape.setState(UShape.STATE_SELECTED);
    			}
    		}
       	}
	} 
	
  	public UShape setRect( int x, int y, int width, int height )
  	{
  		UShapeBox shape = new UShapeBox(this, x, y, width, height);
 
  		add(shape,layerID++); 
  		
  		return shape;
  	}
  	
  	public UShape setEllipse( int x, int y, int width, int height )
  	{
  		UShapeEllipse shape = new UShapeEllipse(this, x, y, width, height);
 
  		add(shape,layerID++);   
  		
  		return shape;
  	} 
  	 
  	public UShape setFrame( int x, int y, int width, int height )
  	{
  		UShapeFrame shape = new UShapeFrame(this, null, x, y, width, height);
 
  		add(shape,layerID++);   
  		
  		return shape;
  	}
  	
  	public UShape setRoundRect( int x, int y, int width, int height )
  	{
  		UShapeRoundRect shape = new UShapeRoundRect(this, x, y, width, height);
 
  		add(shape,layerID++);   
  		
  		return shape;
  	}
  	
  	public UShape setTrapezoid( int x, int y, int width, int height )
  	{
  		UShapeTrapezoid shape = new UShapeTrapezoid(this, x, y, width, height);
 
  		add(shape,layerID++);   
  		
  		return shape;
  	}
  	
  	public UShape setPentagon( int x, int y, int width, int height )
  	{
  		UShapePentagon shape = new UShapePentagon(this, x, y, width, height);
 
  		add(shape,layerID++);  
  		
  		return shape;
  	}
  	
  	public UShape setDiamond( int x, int y, int width, int height )
  	{
  		UShapeDiamond shape = new UShapeDiamond(this, x, y, width, height);
 
  		add(shape,layerID++);  
  		
  		return shape;
  	}
  
	public void addShape( UShape shape )
  	{
  		add(shape,layerID++);   
  	}  	  	
	
	public void delShape( UShape shape )
  	{
		shape.finalize();		
		this.remove(shape);
  	}  	  	

	public void mouseDragged(MouseEvent e) 
	{	
		if( dragRect.x != 0 && dragRect.y != 0 )
		{
			dragRect.width  = ( e.getX()- dragRect.x );
			dragRect.height = ( e.getY()- dragRect.y );
			
			repaint();
		}
	}
	
	public void mouseMoved(MouseEvent e) {
		
	}

	public void mouseClicked(MouseEvent e) 
	{  
		if( isFocusable())
		{
			System.out.println("isFocusable " );
			
		}
	}

	public void mouseEntered(MouseEvent e) {
		
	}


	public void mouseExited(MouseEvent e) {
		
	}

	public void mousePressed(MouseEvent e) 
	{ 
		if( dragRect.x == 0 && dragRect.y == 0 )
		{
			dragRect.x = e.getX();
			dragRect.y = e.getY();
		}
		  
		setShapeStateAll(UShape.STATE_NONE);
		repaint();
	}

	public void mouseReleased(MouseEvent e) 
	{ 
		if( dragRect.x != 0 && dragRect.y != 0 )
		{
			checkSelectArea();
			dragRect.setBounds(0, 0, 0, 0);
			selectRect.setBounds(0, 0, 0, 0);
			
			repaint();
		}
	}

	public void componentHidden(ComponentEvent arg0) 
	{
		setShapeStateAll(UShape.STATE_NONE);
		System.out.println("componentHidden " );
		
	}

	public void componentMoved(ComponentEvent arg0) 
	{
		setShapeStateAll(UShape.STATE_NONE);
		System.out.println("componentMoved " );
		
	}

	public void componentResized(ComponentEvent arg0) 
	{ 
		System.out.println("componentResized " );	
	}

	public void componentShown(ComponentEvent arg0) 
	{ 
	 	setShapeStateAll(UShape.STATE_NONE);
		System.out.println("componentShown " );
	}

	public void keyPressed(KeyEvent arg0) 
	{  		 
	    if (arg0.isControlDown() ) 
	    {
	    	setState( STATE_CTRL ); 
	    }
	    
	    if (arg0.getKeyCode() == KeyEvent.VK_DELETE ) 
	    {
	    	DeleteSelectedShape();
	    	System.out.println("Delete" );
	    }
	}

	public void keyReleased(KeyEvent arg0) 
	{
		setState( STATE_NONE );
		System.out.println("keyReleased" );
	}

	public void keyTyped(KeyEvent arg0) 
	{
	}
}