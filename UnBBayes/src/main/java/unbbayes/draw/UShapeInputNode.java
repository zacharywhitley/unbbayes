 
 
package unbbayes.draw;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

import unbbayes.prs.Node;
  

public class UShapeInputNode extends UShape  
{       
	protected GeneralPath parallelogram;
	
	public UShapeInputNode(UCanvas c, Node pNode, int x, int y, int w, int h)
	{
		super(c, pNode, x, y, w, h);  
		 		
		InitShape();
 	
    }    
	
	public void InitShape() 
	{
		parallelogram = new GeneralPath();

		parallelogram.moveTo((float)(getWidth()-GAP ),					 (float)(GAP));
		parallelogram.lineTo((float)(GAP ), 							 (float)(GAP));  
		parallelogram.lineTo((float)(GAP + 10 ), 			 (float)(getHeight()-GAP));  
		parallelogram.lineTo((float)(-GAP + getWidth() - 10),(float)(getHeight()-GAP));
		parallelogram.closePath(); 
	} 
	 
	 
	public void paintComponent(Graphics g) 
	{
		super.paintComponent(g); 
		
		InitShape();
		
		Graphics2D g2 = (Graphics2D) g;
		 
		g2.setPaint( new GradientPaint( getWidth()/2, getHeight(),  getBackColor(), 
										getWidth()/2, 0, 			Color.white, false));
		
		g2.fill(parallelogram);
		
		g2.setPaint(Color.black);
 		g2.draw(parallelogram);
 		
 		g2.setPaint(Color.black);
 		drawText(g);
	    
	}	    
	
	public boolean contain(double x, double y) 
	{
		return parallelogram.contains((double)(x-getGlobalX()), (double)(y-getGlobalY()));
	}
}

