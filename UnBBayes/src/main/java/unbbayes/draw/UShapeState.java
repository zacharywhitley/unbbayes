 

package unbbayes.draw;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import unbbayes.prs.Node;
import unbbayes.prs.bn.TreeVariable;
import unbbayes.prs.id.DecisionNode;

public class UShapeState extends UShape implements MouseMotionListener, MouseListener, Cloneable
{       
	protected Rectangle2D rect;
	protected Rectangle   rectTextArea;
	protected float marginal; 
	private NumberFormat nf;
	
	public UShapeState(UCanvas s, Node pNode, int x, int y, int w, int h) 
	{
		super(s, pNode, x, y, w, h);
        
		setOpaque(false);
  
		
		nf = NumberFormat.getInstance(Locale.US);
		nf.setMaximumFractionDigits(2);
		
		rectTextArea = new Rectangle(0,0,(int)(w*(0.32)), h );
		 
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
	    								(int)((getWidth() - rectTextArea.getWidth())),
	    								(int)(getHeight()/2), 
	    								getBackColor(), 
	    								false));
	    
	    g2.fillRect( (int)rectTextArea.getWidth(), 0,(int)((getWidth() -rectTextArea.getWidth())*marginal), getHeight());
	     
	    g2.setPaint( Color.BLUE );
	    	
	    g2.drawLine( (int)rectTextArea.getWidth(), (int)0, (int)rectTextArea.getWidth(), (int)getHeight());
	    g2.drawLine( (int)0, (int)0, (int)rect.getWidth(), (int)0);
  	 
	    g2.setPaint( getDrawColor() );
  	  	drawText(g, rectTextArea, getName(), TTYPE_LEFT);
	 
  	  	drawText(g, rectTextArea, nf.format(marginal * 100.0) + "%", TTYPE_RIGHT);
  	}
	
	public void mouseDragged(MouseEvent arg0) 
	{
		System.out.println("UShapeSizeBtn_mouseDragged"); 
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
 
}

