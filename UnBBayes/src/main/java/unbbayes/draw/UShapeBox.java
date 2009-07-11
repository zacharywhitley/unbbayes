package unbbayes.draw;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

public class UShapeBox extends UShape  
{     
	protected Rectangle2D rect;
	
	
	public UShapeBox(UCanvas c, int x, int y, int w, int h)
	{
		super(c, null, x, y, w, h);
		
		InitShape();
    }     
	
	public void InitShape() 
	{
		rect = new Rectangle2D.Double(GAP ,GAP,getWidth()-GAP*2-1,getHeight()-GAP*2-1 );
	}
	
	public void paintComponent(Graphics g) 
	{
		super.paintComponent(g); 
	 
		InitShape();
		
	    Graphics2D g2 = (Graphics2D) g;
   	    g2.draw (rect);
  	    drawText(g);
	}	    
	
	public boolean contain(double x, double y) 
	{
		return rect.contains((double)(x-getGlobalX()), (double)(y-getGlobalY()));
	}   
}

