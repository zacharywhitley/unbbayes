package unbbayes.prs.mebn;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import unbbayes.draw.DrawRoundedRectangle;
import unbbayes.prs.mebn.entity.Entity;
import unbbayes.prs.mebn.entity.StateLink;
import unbbayes.prs.mebn.exception.ArgumentNodeAlreadySetException;
import unbbayes.prs.mebn.exception.OVariableAlreadyExistsInArgumentList;

//TODO Pull up everything to ResidentNode, because we do not have FindingResidentNode anymore.

/**
 * 
 */
public class DomainResidentNode extends ResidentNode {
 
	private static final long serialVersionUID = 1L;

	private List<GenerativeInputNode> inputInstanceFromList;
	
	private List<GenerativeInputNode> inputNodeFatherList;
	 
	/**
	 * List of fathers of this node
	 */
	private List<DomainResidentNode> residentNodeFatherList;
	 
	/**
	 * List of children of this node
	 */
	private List<DomainResidentNode> residentNodeChildList;
	 
	private List<RandomVariableFinding> randomVariableFindingList; 
	
	private DomainMFrag mFrag;

	private String tableFunction;
	
	private List<StateLink> possibleValueList; 
	
	//Graphics informations 
	
	private DrawRoundedRectangle drawResidentNode; 
	
	private static Color color = new Color(254, 250, 158); 	
	
	
	public DomainResidentNode(String name, DomainMFrag mFrag){
		
		super();
		
		this.mFrag = mFrag; 
		
		inputInstanceFromList = new ArrayList<GenerativeInputNode>(); 
		inputNodeFatherList = new ArrayList<GenerativeInputNode>();
		residentNodeFatherList = new ArrayList<DomainResidentNode>();	
		residentNodeChildList = new ArrayList<DomainResidentNode>();	
		randomVariableFindingList = new ArrayList<RandomVariableFinding>(); 
		possibleValueList = new ArrayList<StateLink>(); 
		
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
	
	/**
	 * Add a new ordinary variable to the list of arguments of this node 
	 * and update all input nodes of this node for have the same number
	 * of arguments. 
	 */
	public void addArgument(OrdinaryVariable ov) throws OVariableAlreadyExistsInArgumentList, 
	                                                    ArgumentNodeAlreadySetException{
		super.addArgument(ov);
		ov.addIsOVariableOfList(this); 
		
		for(GenerativeInputNode inputNode: inputInstanceFromList){
			inputNode.updateResidentNodePointer(); 
		}
		
		updateLabel(); 
		
	}   
	
	public void removeArgument(OrdinaryVariable ov){
		super.removeArgument(ov);
		//ov.removeIsOVariableOfList(this); 
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
	/**
	 * Don't use this method! Use removeResidentNodeChildList
	 */
	private void removeResidentNodeFather(DomainResidentNode node){
		residentNodeFatherList.remove(node); 
	}
	
	protected void removeInputNodeFatherList(GenerativeInputNode node){
		inputNodeFatherList.remove(node); 
	}	
	
	/**
	 * Remove a node of the list of childs of this node. 
	 * 
	 * @param node
	 */
	public void removeResidentNodeChildList(DomainResidentNode node){
		residentNodeChildList.remove(node);
		node.removeResidentNodeFather(this); 
	}
	
	public void removeInputInstanceFromList(GenerativeInputNode node){
		inputInstanceFromList.remove(node);
		try{
			node.setInputInstanceOf((DomainResidentNode)null); 
		}
		catch(Exception e){
			e.printStackTrace(); 
		}
	}		
	
	/*------------------------------------------------------------*/
	
	/**
	 * Add a possible value to the list of possible values of
	 * the domain resident node. 
	 */
	public StateLink addPossibleValueLink(Entity possibleValue){
		StateLink value = new StateLink(possibleValue); 
		possibleValueList.add(value);
		return value; 
		// TODO override addPossibleValue() or call super.addPossibleValue(possibleValue) in order
		// to make compatible w/ MultiEntityNode.possibleValueList...
	}
	
	/**
	 * Remove the possible value with the name 
	 * @param possibleValue name of the possible value
	 */
	public void removePossibleValueByName(String possibleValue){
		
		for(StateLink value : possibleValueList){
			if (value.getState().getName().equals(possibleValue)){
				possibleValueList.remove(value);
				return; 
			}
		}
	}
	
	/**
	 * Remove all possible values of the node
	 */
	public void removeAllPossibleValues(){
		possibleValueList.clear(); 
	}
	
	/**
	 * Verifies if the possible value is on the list of possible values
	 * of the node. 
	 * @param possibleValue name of the possible value
	 * @return true if it is present or false otherside
	 */
	public boolean existsPossibleValueByName(String possibleValue){
		
		for(StateLink value : possibleValueList){
			if (value.getState().getName().equals(possibleValue)){
				return true; 
			}
		}
		
		return false; 
	}	
	
	/**
	 * Verify if the entity is a state of the node 
	 * Warning: the search will be for the entity and not for the
	 * name of entity.
	 * @param entity The entity 
	 * @return true if the entity is a state, false otherside
	 */
	public boolean hasPossibleValue(Entity entity) {
		for(StateLink value : possibleValueList){
			if (value.getState() == entity){
				return true; 
			}
		}
		return false;
	}
	
	/**
	 * Return the possible value of the residente node with the name
	 * (return null if don't exist a possible value with this name)
	 */
	public StateLink getPossibleValueByName(String possibleValue){
		for(StateLink value : possibleValueList){
			if (value.getState().getName().equalsIgnoreCase(possibleValue)){
				return value; 
			}
		}
		
		return null;
	}
	
	public List<StateLink> getPossibleValueLinkList(){
		return possibleValueList; 
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
	 * Update the label of this node. 
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
     * Remove the extern references to this node.
     * 
     *  - Fathers nodes (and edges) 
     *  - Child nodes (and edges)
     *  
     *  Call the method delete of the super class; 
     */
    
	public void delete(){
		
		super.delete(); 
		
		while(!inputInstanceFromList.isEmpty()){
			inputInstanceFromList.remove(0).setInputInstanceOf(); 
		}
		
		while(!inputNodeFatherList.isEmpty()){
			inputNodeFatherList.remove(0).removeResidentNodeChild(this); 
		}
		
		while(!residentNodeFatherList.isEmpty()){
			DomainResidentNode father = residentNodeFatherList.get(0); 
			father.removeResidentNodeChildList(this); 
			mFrag.removeEdgeByNodes(father, this);
		}
		
		while(!residentNodeChildList.isEmpty()){
			DomainResidentNode child = residentNodeChildList.get(0); 
			this.removeResidentNodeChildList(child);
			mFrag.removeEdgeByNodes(this, child); 
		}
		
		mFrag.removeDomainResidentNode(this); 
		
	}
	
	public String getTableFunction(){
		return tableFunction; 
	}
	
	public void setTableFunction(String table){
		tableFunction = table;
	}

	public void addRandonVariableFinding(RandomVariableFinding finding){
		randomVariableFindingList.add(finding); 
	}
	
	public void removeRandonVariableFinding(RandomVariableFinding finding){
		randomVariableFindingList.remove(finding); 
	}
	
	public boolean containsRandonVariableFinding(RandomVariableFinding finding){
		return randomVariableFindingList.contains(finding); 
	}
	
	public List<RandomVariableFinding> getRandonVariableFindingList() {
		return randomVariableFindingList;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.MultiEntityNode#getPossibleValueIndex(java.lang.String)
	 */
	@Override
	public int getPossibleValueIndex(String stateName) {
		int index = 0;
		for (StateLink link : this.possibleValueList) {
			if (link.getState().getName().equals(stateName)) {
				return index;
			}
			index++;
		}
		return -1;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.MultiEntityNode#getPossibleValueList()
	 */
	@Override
	public List<Entity> getPossibleValueList() {
		// TODO optimize this
		List<Entity> ret = new ArrayList<Entity>();
		for (StateLink link : this.possibleValueList) {
			ret.add(link.getState());
		}
		return ret;
	}
	
	
	
}
 
