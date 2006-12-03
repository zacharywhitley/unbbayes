package unbbayes.prs.mebn;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import unbbayes.gui.draw.DrawRectangle;
import unbbayes.gui.draw.DrawRoundedRectangle;

public class DomainResidentNode extends ResidentNode {
 
	private List<GenerativeInputNode> inputInstanceFromList;
	
	private List<GenerativeInputNode> inputNodeFatherList;
	 
	private List<DomainResidentNode> residentNodeFatherList;
	 
	private List<DomainResidentNode> residentNodeChildList;
	 
	private DomainMFrag mFrag;
	
	private DrawRoundedRectangle drawResidentNode; 
	
	private static Color color = new Color(254, 250, 158); 	
	
	public DomainResidentNode(String name, DomainMFrag mFrag){
		
		super();
		
		setName(name); 
		updateLabel(); 
		
		this.mFrag = mFrag; 
		
		inputInstanceFromList = new ArrayList<GenerativeInputNode>(); 
		inputNodeFatherList = new ArrayList<GenerativeInputNode>();
		residentNodeFatherList = new ArrayList<DomainResidentNode>();	
		residentNodeChildList = new ArrayList<DomainResidentNode>();	
	
    	size.x = 100;
    	size.y = 20; 
    	drawResidentNode = new DrawRoundedRectangle(position, size);
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
    
	public DomainMFrag getMFrag(){
		return mFrag; 
	}
	
	private void addResidentNodeFather(DomainResidentNode father){
		residentNodeFatherList.add(father);
		father.addResidentNodeChild(this); 
		
		mFrag.removeEdgeByNodes(this, father);		
	}
	
	public void addInputNodeFather(GenerativeInputNode father){
		inputNodeFatherList.add(father); 
		father.addResidentNodeChild(this); 
	}	 
	
	public void addInputInstanceFromList(GenerativeInputNode instance){
		inputInstanceFromList.add(instance);
		instance.setInputInstanceOfNode(this); 
	}
	
	/**
	 * Add a node in the list of childs resident nodes of this node. In the node 
	 * child add this node in the list of fathers resident nodes.  
	 * @param node: the node that is child of this. 
	 */
	
	public void addResidentNodeChild(DomainResidentNode node){
		residentNodeChildList.add(node); 
		node.addResidentNodeFather(this); 
		
		mFrag.removeEdgeByNodes(this, node);
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
	
	public void removeResidentNodeFather(DomainResidentNode node){
		residentNodeFatherList.remove(node);
		node.removeResidentNodeChildList(this); 
	}
	
	public void removeInputNodeFatherList(GenerativeInputNode node){
		inputNodeFatherList.remove(node); 
		node.removeResidentNodeChild(this); 
	}	
	
	public void removeResidentNodeChildList(DomainResidentNode node){
		residentNodeChildList.remove(node); 
		node.removeResidentNodeFather(this); 
	}
	
	public void removeInputInstanceFromList(GenerativeInputNode node){
		inputInstanceFromList.remove(node);
		node.setInputInstanceOfNode(null); 
	}		
	
	@Override
	public void paint(Graphics2D graphics) {
		drawResidentNode.setFillColor(getColor());
		super.paint(graphics);
	}	
	
	public void setName(String name){
		
		super.setName(name); 
		updateLabel(); 
		
	}
	
    public void updateLabel(){
    	String newLabel; 
    	newLabel = name + "("; 
    	for(OrdinaryVariable ov: super.getOrdinaryVariableList() ){
    		newLabel = newLabel + ov.getName() + ", "; 
    	}
    	
    	newLabel = newLabel.substring(0, newLabel.length() - 2); //retirar a virgula
    	
    	newLabel = newLabel + ")"; 
    	setLabel(newLabel); 
    }
    
	public void addOrdinaryVariable(OrdinaryVariable ov){
		super.addOrdinaryVariable(ov);
		updateLabel(); 
	}   
	
	public void delete(){
		
		for (GenerativeInputNode inputNode: inputInstanceFromList){
			inputInstanceFromList.remove(inputNode); 
		}
		
		for(GenerativeInputNode inputNode: inputNodeFatherList){
			inputNodeFatherList.remove(inputNode); 
		}
		
		for(DomainResidentNode residentNode: residentNodeFatherList){
			inputNodeFatherList.remove(residentNode); 
		}
		
		for(DomainResidentNode residentNode: residentNodeChildList){
			inputNodeFatherList.remove(residentNode); 
		}				
		
		mFrag.removeDomainResidentNode(this); 
		
		
	}
	
}
 
