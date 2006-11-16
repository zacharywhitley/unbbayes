package unbbayes.prs.mebn;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import unbbayes.gui.draw.DrawRectangleThree;

public class GenerativeInputNode extends InputNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7377146558744109802L;
	
	private List<DomainResidentNode> residentNodeChildList;
	
	/*
	 * These two variables (inputInstanceOfRV and inputInstanceOfNode) have an
	 * 'or' relationship. That means that if this input node is an input
	 * instance of RV, than it is not from a node. The oposite is also true. In
	 * other words, if one is not null the other must be null.
	 */
	//already inherit from superclass
	//private BuiltInRV inputInstanceOfRV; 
	//is it possible to have the same name as in the superclass?
	
	private DomainResidentNode inputInstanceOfNode; 

	private DomainMFrag mFrag;
	
	private DrawRectangleThree drawInputNode; 
	
	private static Color color = new Color(187, 247, 253); 		
	
	public GenerativeInputNode(String name, DomainMFrag mFrag){
		
	   super(); 
	   setName(name); 
	   
	   this.mFrag = mFrag;
	   
	   residentNodeChildList = new ArrayList<DomainResidentNode>(); 
	
	   size.x = 100;
	   size.y = 20; 
	   drawInputNode = new DrawRectangleThree(position, size);
	   drawElement.add(drawInputNode);	
	}
	
	/**
     *  Gets all generative input node node's color.
     *
     * @return The color of all generative input node's color.
     */
    public static Color getColor() {
        return color;
    }

    /**
     *  Sets the new color for all generative input node node.
     *
     * @return The new color of all generative input node in RGB.
     */
    public static void setColor(int c) {
        color = new Color(c);
    }		
	
	
	public void addResidentNodeChild(DomainResidentNode resident){
		residentNodeChildList.add(resident); 
	}
	
	public List<DomainResidentNode> getResidentNodeChildList(){
		return residentNodeChildList; 
	}
	
	public DomainResidentNode getInputInstanceOfNode(){
		return inputInstanceOfNode; 
	}
	
	public DomainMFrag getMFrag(){
		return mFrag; 
	}

	@Override
	public void setSelected(boolean b) {
		drawInputNode.setSelected(b);
		super.setSelected(b);
	}    	
	
	@Override
	public void paint(Graphics2D graphics) {
		drawInputNode.setFillColor(getColor());
		super.paint(graphics);
	}	
	
}
