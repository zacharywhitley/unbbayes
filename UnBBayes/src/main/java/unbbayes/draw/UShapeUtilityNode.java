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
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.id.UtilityNode;

public class UShapeUtilityNode extends UShape  
{       
	protected GeneralPath parallelogram; 
	
	public UShapeUtilityNode(UCanvas c, Node pNode, int x, int y, int w, int h)
	{
		super(c, pNode, x, y, w, h);  
		  
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
		 
		g2.setPaint( new GradientPaint( getWidth()/2, getHeight(),  getBackColor(), 
				getWidth()/2, 0,Color.white, false));
 
		g2.fill(parallelogram);
		g2.setColor(getLineColor());
 		g2.draw(parallelogram);
 		drawText(g);
		 
	}	    
	
	public boolean contain(double x, double y) 
	{
		return parallelogram.contains((double)(x-getGlobalX()), (double)(y-getGlobalY()));
	}
}

