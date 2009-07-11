 
package unbbayes.draw;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;

public class UShapeDiamond extends UShape  
{    
	protected GeneralPath parallelogram;
	
	public UShapeDiamond(UCanvas c, int x, int y, int w, int h)
	{
		super(c, null, x, y, w, h);
		
		InitShape();
    }     
	
	public void InitShape() 
	{
		parallelogram = new GeneralPath(); 

		parallelogram.moveTo((float)((getWidth())/2),		(float)(GAP));
		parallelogram.lineTo((float)(-GAP + getWidth()), 	(float)(getHeight()/2));  
		parallelogram.lineTo((float)((getWidth())/2 ), 		(float)(getHeight()-GAP));  
		parallelogram.lineTo((float)(GAP ), 				(float)(getHeight()/2));
		parallelogram.closePath(); 
	}
	
	public void paintComponent(Graphics g) 
	{
		super.paintComponent(g); 
		
		InitShape();
		
		Graphics2D g2 = (Graphics2D) g;
		
		g.setColor(getLineColor()); 
		 	
 		g2.draw(parallelogram);
 	    drawText(g);
	}	   
	
	public boolean contain(double x, double y) 
	{
		return parallelogram.contains((double)(x-getGlobalX()), (double)(y-getGlobalY()));
	}
	
}