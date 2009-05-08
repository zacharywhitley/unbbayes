/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package unbbayes.prs.mebn;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import unbbayes.draw.DrawFlatPentagon;
import unbbayes.prs.Node;
import unbbayes.prs.mebn.entity.Type;
import unbbayes.util.SerializablePoint2D;

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
    
    
	private static final Point DEFAULT_MEBN_SIZE = new Point(100,20); 
	
	protected static SerializablePoint2D size = new SerializablePoint2D(
			DEFAULT_MEBN_SIZE.getX(), DEFAULT_MEBN_SIZE.getY());

	
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
	    	if(node instanceof ResidentNode){
	    		((ResidentNode)node).updateLabel(); 
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
 // Please, avoid using @Override annotation, since it makes interface extraction (refactor) very difficult,
	// because it supposes a inherited method is declared always inside a class, 
	// and fails if it becomes declared inside an interface.
//	@Override
	public int getType() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	/**
	 * Two ordinary variables are equals if: <br>
	 * - Have the same name <br> 
	 * - Have the same type <br>
	 * - Have the same MFrag <br> 
	 */
	@Override
	public boolean equals(Object obj) {
		
		boolean result = false; 
		
		if (obj == this) {
			result = true;
		}else{
			if((obj != null)&&(obj instanceof OrdinaryVariable)){
				OrdinaryVariable node = (OrdinaryVariable) obj;
				if((node.getMFrag()!=null)&&(node.getValueType()!=null)){
					if(     (node.name.equals(this.name) &&
							(node.getValueType().equals(this.getValueType()))) &&
							(node.getMFrag().equals(this.mFrag))){

						result = true;

					}
				}else{
					if(node.name.equals(this.name)){

						result = true;

					}					
				}
			}
		}
		
		return result; //obj == null && this != null 
		
	}
	
	/**
	 * Returns the node's size (x,y) where x = width and y = height.
	 * 
	 * @return The node's size.
	 */
	public static Point2D.Double getSize() {

		return size;

	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString(){
		return name; 
	}
	
}
 
