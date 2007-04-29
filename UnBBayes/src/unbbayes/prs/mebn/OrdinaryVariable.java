package unbbayes.prs.mebn;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import unbbayes.gui.draw.DrawFlatPentagon;
import unbbayes.gui.mebn.auxiliary.ToolKitForGuiMebn;
import unbbayes.prs.Node;
import unbbayes.prs.mebn.context.NodeFormulaTree;
import unbbayes.prs.mebn.entity.ObjectEntity;

/**
 * Ordinary Variables are placeholders used in MFrag to refer to 
 * non-specific entities as arguments in given MFrag's RVs. 
 */

/* Tentativa de fazer o ordinary variable ser visto como um node */

public class OrdinaryVariable extends Node{
 
	private MFrag mFrag;
	
	private String type; 
	
	private List<Node> isOVariableOfList; 
	
	/* draw */ 
	
	private static Color color = new Color(176, 252, 131); 
	
    private DrawFlatPentagon drawContextNode;	
	
	/*
	 * Nesta versão de teste corresponde ao elemento que preenche a posicao 
	 * da variavel ordinária. 
	 */
	private ObjectEntity entity; 
	
	public OrdinaryVariable(String name, String type, MFrag mFrag){
		
		this.name = name; 
		this.mFrag = (MFrag)mFrag; 
		this.type = type; 
		
		isOVariableOfList = new ArrayList<Node>(); 
		
    	/* draw */
    	updateLabel(); 
    	size.x = 100;
    	size.y = 20; 
    	drawContextNode = new DrawFlatPentagon(position, size);
        drawElement.add(drawContextNode);
		
	}
	
	/**
	 * Method responsible for return the MFrag where the Ordinary 
	 * Variable are inside.
	 */	
	
	public MFrag getMFrag(){
		return mFrag; 
	}
	 
	/**
	 * Method responsible for return the name of the OV. 
	 */	
	
	public String getName(){
		return name; 
	
	}
	
	/**
	 * Turn the name of the Ordinary Variable 
	 * @param name The new name 
	 */
	
	public void setName(String name){
		this.name = name; 
	
	    for(Node node: isOVariableOfList){
	    	if(node instanceof DomainResidentNode){
	    		((DomainResidentNode)node).updateLabel(); 
	    	}
	    }
	}
	
	/**
	 * Set the type.
	 * Nota: this method don't verify if the string is a type valid.  
	 * @param type
	 */
	public void setValueType(String type){
		
		this.type = new String(type); 
		
		System.out.println("-> Type of ov " + this.name + " turned for " + type); 
		
	}
	
	/**
	 * Method responsible for return the type of the OV. 
	 */
	
	public String getValueType(){
		return type; 
	}
	
	/**
	 * Add a node in the list of nodes when this o variable is
	 * present (if the node alredy is in the list, don't do nothing). 
	 * @param node
	 */
	
	protected void addIsOVariableOfList(Node node){
	   if(!isOVariableOfList.contains(node)){
	      isOVariableOfList.add(node);
	   }
	}
	
	/**
	 * Remove a node of the IsOVariableList. 
	 * @param node
	 */
	
	protected void removeIsOVariableOfList(Node node){
		isOVariableOfList.remove(node);
	}
	
	public String toString(){
		return name; 
	}

	public ObjectEntity getEntity() {
		return entity;
	}

	public void setEntity(ObjectEntity entity) {
		this.entity = entity;
	}
	
	public void delete(){
		
    	mFrag.removeOrdinaryVariable(this); 
	
	    for(Node node: this.isOVariableOfList){
	    	//TODO fazer remocao
	    }
	}
	
	/* draw */
	/*-------------------------------------------------------------*/
	
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
	 * update the label of this node. 
	 * The label is the formula that represents this context node.  
	 */
	
    public String updateLabel(){
    	
    	String label; 
    	label = "isA(" + name + "," + type + ")"; 
    	setLabel(label);
    	
    	return label; 
    	
    }	
    
	@Override
	public int getType() {
		// TODO Auto-generated method stub
		return 0;
	}
	
}
 
