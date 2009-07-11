 
package unbbayes.draw;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import unbbayes.prs.Node;
  

public class UShapeResidentNode extends UShape  
{       
	protected RoundRectangle2D roundRect;
	
	public UShapeResidentNode(UCanvas c, Node pNode, int x, int y, int w, int h)
	{
		super(c, pNode, x, y, w, h);  
		 
		
		InitShape();

    }    
	
	public void InitShape() 
	{
		roundRect = new RoundRectangle2D.Double(GAP ,GAP,getWidth()-GAP*2-1,getHeight()-GAP*2-1, getHeight()/2,getHeight()/2 );
	} 
	 
	public void paintComponent(Graphics g) 
	{
		super.paintComponent(g); 
		
		InitShape();
		
		Graphics2D g2 = (Graphics2D) g;
	 
		g2.setPaint( new GradientPaint( getWidth()/2, getHeight(),  getBackColor(), 
										getWidth()/2, 0, 			Color.white, false));
		
	   
		g2.fill(roundRect);
	 	g2.setPaint(Color.black);
 		g2.draw(roundRect);
 		
 		g2.setPaint(Color.black);
 		drawText(g);
	    
	}	    
	
	public boolean contain(double x, double y) 
	{
		return roundRect.contains((double)(x-getGlobalX()), (double)(y-getGlobalY()));
	}
}

