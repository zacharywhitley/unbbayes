 
package unbbayes.gui;
 
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.util.ResourceBundle;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import unbbayes.draw.UShape;
import unbbayes.draw.UShapeLine;
import unbbayes.draw.UShapeProbabilisticNode;
import unbbayes.prs.Edge;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.prs.hybridbn.ContinuousNode;

 
public class LearningPNEditionPane extends GraphPane implements MouseListener, MouseMotionListener 
{ 
	/** Load resource file from this package */
	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.gui.resources.GuiResources");

	
	public LearningPNEditionPane(JDialog dlg, ProbabilisticNetwork n) 
	{    	
		super(dlg, n);
	}
	
	public boolean insertEdge(Edge edge) 
	{		
		 
        try {
        	net.addEdge(edge);
        	
		} catch (InvalidParentException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), resource
					.getString("statusError"), JOptionPane.ERROR_MESSAGE);
			return false;
		}
				 
		return true;
	}
	    
	public void update()
	{
		this.removeAll();
		
		Node n; 
		Edge e;
		UShape shape = null;
		Point2D defaultStartPos = new Point2D.Double(0,0);
		
		// Load all nodes.
		for (int i = 0; i < nodeList.size(); i++) 
		{
			n = nodeList.get(i);
			n.updateLabel();
				
			//MODE_LEANRING_PANE 
			n.setPosition( defaultStartPos.getX(), defaultStartPos.getY());
			defaultStartPos.setLocation(defaultStartPos.getX() + 120, ( i%2 == 1 ? 10 : 100)  );
						
			createNode( n );
												
			if(n instanceof ContinuousNode || n instanceof ProbabilisticNode) 
			{
				shape = getNodeUShape(n);
				
				if( shape != null )
				{
					shape.shapeTypeChange(UShapeProbabilisticNode.STYPE_NONE);				
					shape.setState(UShape.STATE_RESIZED);
				}
			}
		}	
		
		// Load all Edges
		for (int i = 0; i < edgeList.size(); i++) 
		{
			e = edgeList.get(i);
			
			if(getNodeUShape(e.getOriginNode()) != null && getNodeUShape(e.getDestinationNode()) != null )
			{
				UShapeLine line = new UShapeLine(this, getNodeUShape(e.getOriginNode()), getNodeUShape(e.getDestinationNode()) );
				line.setEdge(e);
				line.setUseSelection(false);
				addShape( line );
			}
		}	
		
		setShapeStateAll(UShape.STATE_NONE);
		fitCanvasSizeToAllUShapes();
	} 
	   
    public UShapeLine onDrawConnectLineReleased(UShape shapeParent, int x, int y)
    {
    	UShapeLine line = super.onDrawConnectLineReleased( shapeParent, x, y );
    	
    	if( line != null )
    	{
	    	Edge e = new Edge(line.getSource().getNode(), line.getTarget().getNode());
	    	if( e != null )
	    	{
	    		if( insertEdge(e) == true )
	    		{
	    			//MODE_LEANRING_PANE 
	    			line.setUseSelection( true );
	    			
	    			line.setEdge(e);	    						
	    		}
	    		else
	    		{
	    			delShape(line);
	    			repaint();
	    		}
	    	}
    	}
    	
    	return line;
    }   
}