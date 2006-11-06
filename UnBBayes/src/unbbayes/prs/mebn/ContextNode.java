package unbbayes.prs.mebn;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import unbbayes.gui.draw.DrawEllipse;
import unbbayes.gui.draw.DrawRectangleTwo;

public class ContextNode extends MultiEntityNode {
 
	/**
	 * 
	 */
	
	private static final long serialVersionUID = 8186266877724939663L;

	private DomainMFrag mFrag;
	
	/**
	 * All innner terms of this node.
	 */
	private List<ContextNode> innerTermOfList;
	 
	/**
	 * All nodes where this node is an inner term.
	 */
	private List<ContextNode> innerTermFromList;
	
	private static Color color = Color.yellow;
	
    private DrawRectangleTwo drawContextNode;
    
    public ContextNode(String name, DomainMFrag mFrag) {
    	
    	super(); 
    	
    	setName(name); 
    	this.name = name; 
    	
    	this.mFrag = mFrag; 
    	
    	innerTermOfList = new ArrayList<ContextNode>();
    	innerTermFromList = new ArrayList<ContextNode>();
    	// Here it is defined how this node is going to be drawn.
        // In the superclass, Node, it was already definied to draw text, here
        // we add the draw ellipse.
    	size.x = 100;
    	size.y = 20; 
    	drawContextNode = new DrawRectangleTwo(position, size);
        drawElement.add(drawContextNode);

    }
	
	/**
	 * Method responsible for deleting this context node. It makes sure to clean 
	 * the innerTermFromList and the innerTermOfList.
	 *
	 */
	public void delete() {
		for (ContextNode node: innerTermFromList) {
			node.removeInnerTermOfList(node);
		}
		for (ContextNode node: innerTermOfList) {
			// If this context node is not part of the MFrag, it means it is 
			// being used just by the context node being delete. So it can also 
			// be deleted.
			if (!mFrag.containsContextNode(node))
				node.delete();
			else {
				node.removeInnerTermFromList(node);
			}
		}
		mFrag.removeContextNode(this);
	}
	
	/**
	 * Method responsible for removing the given context node from the list of 
	 * inner terms this context node has.
	 * @param contextNode The context node to remove.
	 */
	public void removeInnerTermOfList(ContextNode contextNode) {
		innerTermOfList.remove(contextNode);
	}
	
	/**
	 * Method responsible for removing the given context node from the list of 
	 * nodes where this context node is an inner term.
	 * @param contextNode The context node to remove.
	 */
	public void removeInnerTermFromList(ContextNode contextNode) {
		innerTermFromList.remove(contextNode);
	}
	
	/**
	 * 
	 */
	public void addInnerTermOfList(ContextNode contextNode){
		innerTermOfList.add(contextNode); 
	}
	
	public void addInnerTermFromList(ContextNode contextNode){
		innerTermFromList.add(contextNode); 
	}
	
	//public List<ContextNode> getInnerTermOfList(){
	//	return innerTermOfList; 
	//}
	
	//public List<ContextNode> getInnerTermFromList(){
	//	return innerTermFromList; 
	//}	
		
	/**
     *  Gets all context node's color.
     *
     * @return The color of all context node's color.
     */
    public static Color getColor() {
        return color;
    }

    /**
     *  Sets the new color for all context node.
     *
     * @return The new color of all context node in RGB.
     */
    public static void setColor(int c) {
        color = new Color(c);
    }
	 
	@Override
	public void setSelected(boolean b) {
		drawContextNode.setSelected(b);
		super.setSelected(b);
	}
	
	@Override
	public void paint(Graphics2D graphics) {
		drawContextNode.setFillColor(getColor());
		super.paint(graphics);
	}

	/**
	 * Set the MFrag where this node resides.
	 * @param frag The MFrag where this node resides.
	 */
	public void setMFrag(DomainMFrag frag) {
		mFrag = frag;
	}
	
	public DomainMFrag getMFrag(){
		return mFrag; 
	}
}
 
