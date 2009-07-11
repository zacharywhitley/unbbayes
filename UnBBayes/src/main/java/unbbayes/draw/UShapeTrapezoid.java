 
package unbbayes.draw;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;

public class UShapeTrapezoid extends UShape  
{    
	protected GeneralPath parallelogram;
	
	public UShapeTrapezoid(UCanvas c, int x, int y, int w, int h)
	{
		super(c, null, x, y, w, h);
		
		InitShape();
    }     
	
	public void InitShape() 
	{
		parallelogram = new GeneralPath();
		parallelogram.moveTo((float)(GAP + getWidth()*0.2 ),					(float)(GAP));
		parallelogram.lineTo((float)(-GAP + getWidth() - getWidth()*0.2), 	(float)(GAP));  
		parallelogram.lineTo((float)(getWidth()-GAP ), 					(float)(getHeight()-GAP));  
		parallelogram.lineTo((float)(GAP ), 							(float)(getHeight()-GAP));
		parallelogram.closePath(); 
	} 
	
	public void paintComponent(Graphics g) 
	{
		super.paintComponent(g); 
		
		InitShape();
		
		Graphics2D g2 = (Graphics2D) g;
		g2.draw(parallelogram);
 	    drawText(g);
	}	
	
	public boolean contain(double x, double y) 
	{
		return parallelogram.contains((double)(x-getGlobalX()), (double)(y-getGlobalY()));
	}
}