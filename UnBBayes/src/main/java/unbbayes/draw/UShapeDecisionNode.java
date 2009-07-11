 
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
import unbbayes.prs.id.DecisionNode;

public class UShapeDecisionNode extends UShape  
{       
	protected Rectangle2D rect; 
	
	public UShapeDecisionNode(UCanvas c, Node pNode, int x, int y, int w, int h)
	{
		super(c, pNode, x, y, w, h);  
		 		
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
	 	    
		g2.setPaint( new GradientPaint( getWidth()/2, getHeight(),  getBackColor(), 
				getWidth()/2, 0,Color.white, false));

		
		g2.fillRect(GAP ,GAP,getWidth()-GAP*2-1,getHeight()-GAP*2-1 );
		g2.setColor(getLineColor());
  	    g2.draw (rect);
  	    
  	    drawText(g);
	    
	}	    
	
	public boolean contain(double x, double y) 
	{
		return rect.contains((double)(x-getGlobalX()), (double)(y-getGlobalY()));
	}
}

