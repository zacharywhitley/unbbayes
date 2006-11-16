package unbbayes.prs.mebn;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import unbbayes.gui.draw.DrawRectangle;

public class DomainResidentNode extends ResidentNode {
 
	private List<GenerativeInputNode> inputInstanceFromList;
	
	private List<GenerativeInputNode> inputNodeFatherList;
	 
	private List<DomainResidentNode> residentNodeFatherList;
	 
	private List<DomainResidentNode> residentNodeChildList;
	 
	private DomainMFrag mFrag;
	
	private DrawRectangle drawResidentNode; 
	
	private static Color color = new Color(176, 252, 131); 	
	
	public DomainResidentNode(String name, DomainMFrag mFrag){
		
		super(); 
		setName(name); 
		
		this.mFrag = mFrag; 
		
		inputInstanceFromList = new ArrayList<GenerativeInputNode>(); 
		inputNodeFatherList = new ArrayList<GenerativeInputNode>();
		residentNodeFatherList = new ArrayList<DomainResidentNode>();	
		residentNodeChildList = new ArrayList<DomainResidentNode>();	
	
    	size.x = 100;
    	size.y = 20; 
    	drawResidentNode = new DrawRectangle(position, size);
        drawElement.add(drawResidentNode);
	}
	
	/**
     *  Gets all domain resident node's color.
     *
     * @return The color of all domain resident node's color.
     */
    public static Color getColor() {
        return color;
    }

    /**
     *  Sets the new color for all domain resident node.
     *
     * @return The new color of all domain resident node in RGB.
     */
    public static void setColor(int c) {
        color = new Color(c);
    }	
	
	@Override
	public void setSelected(boolean b) {
		drawResidentNode.setSelected(b);
		super.setSelected(b);
	}    
    
	public void addResidentNodeFather(DomainResidentNode father){
		residentNodeFatherList.add(father); 
	}
	
	public void addInputNodeFather(GenerativeInputNode father){
		inputNodeFatherList.add(father); 
	}	 
	
	public void addInputInstanceFromList(GenerativeInputNode instance){
		inputInstanceFromList.add(instance); 
	}
	
	
	public DomainMFrag getMFrag(){
		return mFrag; 
	}
	
	public List<DomainResidentNode> getResidentNodeFatherList(){
		return this.residentNodeFatherList; 
	}
	
	public List<GenerativeInputNode> getInputNodeFatherList(){
		return this.inputNodeFatherList; 
	}	
	
	public List<DomainResidentNode> getResidentNodeChildList(){
		return this.residentNodeChildList; 
	}
	
	public List<GenerativeInputNode> getInputInstanceFromList(){
		return this.inputInstanceFromList; 
	}	
	
	@Override
	public void paint(Graphics2D graphics) {
		drawResidentNode.setFillColor(getColor());
		super.paint(graphics);
	}	
	
	
}
 
