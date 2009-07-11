 
package unbbayes.draw;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;

public class UShapeEllipse extends UShape  
{       
	protected Ellipse2D ellipse;
	
	public UShapeEllipse(UCanvas c, int x, int y, int w, int h)
	{
		super(c, null, x, y, w, h);  
		
		InitShape();
    }    
	
	public void InitShape() 
	{
		ellipse = new Ellipse2D.Double(GAP,GAP,getWidth()-1-GAP*2,getHeight()-1-GAP*2);
	} 
	
	public void paintComponent(Graphics g) 
	{
		super.paintComponent(g); 
		
		InitShape();
		
		Graphics2D g2 = (Graphics2D) g;
		 
  	    g2.draw (ellipse);
  	    drawText(g);
	}	    
	
	public boolean contain(double x, double y) 
	{
		return ellipse.contains((double)(x-getGlobalX()), (double)(y-getGlobalY()));
	}
}

