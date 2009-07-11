package unbbayes.draw;
 
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

import unbbayes.prs.Node;

public class UShapeFrame extends UShape  
{     
	protected Rectangle2D rect;
	protected Rectangle rectTitle;
	protected int heightTitle = 30;	
	
	public UShapeFrame(UCanvas c, Node n, int x, int y, int w, int h)
	{
		super(c, n, x, y, w, h);
		
		InitShape();
    }     
	
	public void InitShape() 
	{
		rect = new Rectangle2D.Double(GAP ,GAP,getWidth()-GAP*2-1,getHeight()-GAP*2-1 );
		
		rectTitle = new Rectangle(GAP, GAP, getWidth()-GAP*2-1, heightTitle );
	}
	
	public void paintComponent(Graphics g) 
	{
		super.paintComponent(g); 
	 
		InitShape();
		
	    Graphics2D g2 = (Graphics2D) g;
   	    g2.draw (rect);
   	    g2.draw (rectTitle);
   	    
   	    drawText(g, rectTitle);
	}	    
	
	public boolean contain(double x, double y) 
	{
		return rectTitle.contains((double)(x-getGlobalX()), (double)(y-getGlobalY()));
	}   
}
 