 
package unbbayes.draw;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;

public class UShapeRoundRect extends UShape  
{     
	protected RoundRectangle2D roundRect;
	
	public UShapeRoundRect(UCanvas c, int x, int y, int w, int h)
	{
		super(c, null, x, y, w, h);
		
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
		g2.draw (roundRect);
	    drawText(g);
	}	   
	
	public boolean contain(double x, double y) 
	{
		return roundRect.contains((double)(x-getGlobalX()), (double)(y-getGlobalY()));
	}
}