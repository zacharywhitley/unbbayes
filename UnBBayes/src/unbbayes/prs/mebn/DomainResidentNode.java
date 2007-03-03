package unbbayes.prs.mebn;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import unbbayes.gui.draw.DrawRoundedRectangle;
import unbbayes.prs.mebn.exception.ArgumentNodeAlreadySetException;
import unbbayes.prs.mebn.exception.OVariableAlreadyExistsInArgumentList;

public class DomainResidentNode extends ResidentNode {
 
	private List<GenerativeInputNode> inputInstanceFromList;
	
	private List<GenerativeInputNode> inputNodeFatherList;
	 
	private List<DomainResidentNode> residentNodeFatherList;
	 
	private List<DomainResidentNode> residentNodeChildList;
	 
	private DomainMFrag mFrag;
	
	private DrawRoundedRectangle drawResidentNode; 
	
	private String tableFunction;
	
	private static Color color = new Color(254, 250, 158); 	
	
	
	public DomainResidentNode(String name, DomainMFrag mFrag){
		
		super();
		
		this.mFrag = mFrag; 
		
		inputInstanceFromList = new ArrayList<GenerativeInputNode>(); 
		inputNodeFatherList = new ArrayList<GenerativeInputNode>();
		residentNodeFatherList = new ArrayList<DomainResidentNode>();	
		residentNodeChildList = new ArrayList<DomainResidentNode>();	
		
		setName(name); 
		updateLabel(); 		
		
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
	
	/*------------------------------------------------------------*/
	
	/**
	 * Add a node in the list of childs resident nodes of this node. In the node 
	 * child add this node in the list of fathers resident nodes.  
	 * @param node: the node that is child of this. 
	 */
	
	public void addResidentNodeChild(DomainResidentNode node){
		residentNodeChildList.add(node); 
		node.addResidentNodeFather(this); 
	}		
	
	private void addResidentNodeFather(DomainResidentNode father){
		residentNodeFatherList.add(father);
	}
	
	/**
	 * Add a node in the list of input nodes fathers of this node. In the node 
	 * father add this node in the list of child resident nodes.  
	 * @param father
	 */
	protected void addInputNodeFather(GenerativeInputNode father){
		inputNodeFatherList.add(father); 
	}	 
	
	protected void addInputInstanceFromList(GenerativeInputNode instance){
		inputInstanceFromList.add(instance);
	}
	
	public void addArgument(OrdinaryVariable ov) throws OVariableAlreadyExistsInArgumentList, 
	                                                    ArgumentNodeAlreadySetException{
		super.addArgument(ov);
		ov.addIsOVariableOfList(this); 
		updateLabel(); 
		
	}   
	
	public void removeArgument(OrdinaryVariable ov){
		super.removeArgument(ov);
		ov.removeIsOVariableOfList(this); 
		updateLabel(); 
	}
	
	
	
	/*------------------------------------------------------------*/
	
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
	
	/*------------------------------------------------------------*/
	
	public void removeResidentNodeFather(DomainResidentNode node){
		residentNodeFatherList.remove(node);
		//node.removeResidentNodeChildList(this); 
	}
	
	public void removeInputNodeFatherList(GenerativeInputNode node){
		inputNodeFatherList.remove(node); 
		//node.removeResidentNodeChild(this); 
	}	
	
	public void removeResidentNodeChildList(DomainResidentNode node){
		residentNodeChildList.remove(node); 
		node.removeResidentNodeFather(this); 
	}
	
	public void removeInputInstanceFromList(GenerativeInputNode node){
		inputInstanceFromList.remove(node);
		node.setInputInstanceOf((DomainResidentNode)null); 
	}		
	
	
	
	
	/*------------------------------------------------------------*/
	
	@Override
	public void paint(Graphics2D graphics) {
		drawResidentNode.setFillColor(getColor());
		super.paint(graphics);
	}	
	
	public void setName(String name){
		
		super.setName(name); 
		updateLabel(); 
		
	}
	
	/**
	 * update the label of this node. 
	 * The label is: 
	 *    LABEL := "name" "(" LIST_ARGS ")"
	 *    LIST_ARGS:= NAME_ARG "," LIST_ARGS | VAZIO 
	 *    
	 *  update too the copies of this labels in input nodes. 
	 */
	
    public void updateLabel(){
    	
    	String newLabel; 
    	List<OrdinaryVariable> ordinaryVariableList = super.getOrdinaryVariableList(); 
    	
    	newLabel = name + "("; 
    	
    	for(OrdinaryVariable ov: ordinaryVariableList ){
    		newLabel = newLabel + ov.getName() + ", "; 
    	}
    	
        // retirar a virgula desnecessaria caso ela exista
    	if(ordinaryVariableList.size() > 0){
    	   newLabel = newLabel.substring(0, newLabel.length() - 2); 
    	}
    	
    	newLabel = newLabel + ")"; 
    	
    	setLabel(newLabel); 
    	
    	/* referencias a este label */
    	
    	for(GenerativeInputNode inputNode: inputInstanceFromList){
    		inputNode.updateLabel(); 
    	}
    	
    }
	
    /**
     * retira todas as referencias exteriores a este nodo para que este
     * possa ser removido corretamente.
     */
    
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
	
	public String getTableFunction(){
		return tableFunction; 
	}
	
	public void setTableFunction(String table){
		tableFunction = table;
	}
	
}
 
