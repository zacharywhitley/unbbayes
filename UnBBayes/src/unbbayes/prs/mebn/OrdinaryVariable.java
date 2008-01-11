package unbbayes.prs.mebn;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import unbbayes.draw.DrawFlatPentagon;
import unbbayes.prs.Node;
import unbbayes.prs.mebn.entity.Type;

/**
 * Ordinary Variables are place holders used in MFrag to refer to 
 * non-specific entities as arguments in given MFrag's RVs. As this OV has a 
 * Type, it has to represent a isA(Type) context node, therefore, it extends 
 * the Node class.
 * @see Node
 * @see Type
 * @see ContextNode
 */
public class OrdinaryVariable extends Node{
 
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private MFrag mFrag;
	
	private Type type; 
	
	/* 
	 * An OV can be used as argument in a resident node 
	 * or in a pointer to resident nodes (input nodes and context nodes). 
	 * The isOVariableOfList contains the nodes for the first case and the 
	 * isArgumentOfList for the latter. This lists are necessary to keep the 
	 * consistency when this OV is removed. 
	 */
	
	private List<Node> isOVariableOfList; 
	
	private List<ResidentNodePointer> isArgumentOfList; 
	
	/* draw begin */ 
	
	private static Color color = new Color(176, 252, 131); 
	
    private DrawFlatPentagon drawContextNode;
    
	/* draw end */ 
	
    /**
     * Creates the OV with its given Type and name for the given MFrag. It also 
     * creates the respective isA(Type) context node.
     * @see Type
     * @see ContextNode
     */
	public OrdinaryVariable(String name, Type type, MFrag mFrag){
		
		this.name = name; 
		this.mFrag = (MFrag)mFrag; 
		
		this.type = type; 
		type.addUserObject(this);
		
		isOVariableOfList = new ArrayList<Node>(); 
		isArgumentOfList = new  ArrayList<ResidentNodePointer>(); 
		
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
	 * @param type the type of the ordinary variable.
	 */
	public void setValueType(Type _type){
		
		type.removeUserObject(this); 
		
		type = _type; 
		type.addUserObject(this); 
		
	}
	
	/**
	 * Method responsible for returning the type of the OV. 
	 */
	public Type getValueType(){
		return type; 
	}
	
	/**
	 * Add a node in the list of nodes (if the node already is in the list, 
	 * don't do nothing). 
	 * @param node Node to be added.
	 */
	protected void addIsOVariableOfList(Node node){
	   if(!isOVariableOfList.contains(node)){
	      isOVariableOfList.add(node);
	   }
	}
	
	/**
	 * Remove a node of the IsOVariableList. 
	 * @param node Node to be removed.
	 */
	public void removeIsOVariableOfList(Node node){
		isOVariableOfList.remove(node);
	}
	
	protected void addIsArgumentOfList(ResidentNodePointer pointer){
		if(!isArgumentOfList.contains(pointer)){
			isArgumentOfList.add(pointer); 
		}
	}
	
	protected void removeIsArgumentOfList(ResidentNodePointer pointer){
		isArgumentOfList.remove(pointer); 
	}	
	
	/**
	 * Remove the OV from the respective MFrag.
	 */
	public void removeFromMFrag(){
		mFrag = null; 
	}
	
	/**
	 * Delete the ordinary variable, removing it from the respective MFrag.
	 */
	public void delete(){
		
    	mFrag.removeOrdinaryVariable(this); 
	    type.removeUserObject(this); 
	    
	    for(Node node: isOVariableOfList){
	    	if(node instanceof ResidentNode){
	    		((ResidentNode) node).removeArgument(this); 
	    	}else{
	    		if(node instanceof InputNode){
	    			
	    		}
	    	}
	    }
	    
	    for(ResidentNodePointer pointer: this.isArgumentOfList){
	    	pointer.removeOrdinaryVariable(this);  	
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
	 * Update the label of this node, representing the isA(Type) context node. 
	 * @see Type
	 * @see ContextNode  
	 */
    public String updateLabel(){
    	
    	String label; 
    	label = "isA(" + name + "," + type + ")"; 
    	setLabel(label);
    	
    	return label; 
    	
    }	
    
    /**
     * Returns the node's type, that is different from the OC Type.
     * @see Node#getType()
     */
	@Override
	public int getType() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		
		if (obj == this) {
			return true;
		}
		
		if((obj != null)&&(obj instanceof OrdinaryVariable)){
		   OrdinaryVariable node = (OrdinaryVariable) obj;
		   return (node.name.equals(this.name));
		}
		
		return false; //obj == null && this != null 
		
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString(){
		return name; 
	}
	
}
 
