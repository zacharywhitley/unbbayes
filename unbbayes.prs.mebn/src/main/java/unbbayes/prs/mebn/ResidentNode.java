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
import java.util.ArrayList;
import java.util.List;

import unbbayes.prs.bn.IRandomVariable;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.mebn.compiler.Compiler;
import unbbayes.prs.mebn.compiler.ICompiler;
import unbbayes.prs.mebn.entity.Entity;
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.entity.ObjectEntityConteiner;
import unbbayes.prs.mebn.entity.ObjectEntityInstance;
import unbbayes.prs.mebn.entity.StateLink;
import unbbayes.prs.mebn.exception.ArgumentNodeAlreadySetException;
import unbbayes.prs.mebn.exception.OVariableAlreadyExistsInArgumentList;

/**
 * This class represents a resident node.
 */
public class ResidentNode extends MultiEntityNode 
         implements IRandomVariable {
	
	private static final long serialVersionUID = 1L;

	private List<OrdinaryVariable> ordinaryVariableList; 
	
	private List<ResidentNodePointer> listPointers; 

	private List<InputNode> inputInstanceFromList;
	
	private List<InputNode> parentInputNodeList;
	 
	/**
	 * List of fathers of this node
	 */
	private List<ResidentNode> residentNodeFatherList;
	 
	/**
	 * List of children of this node
	 */
	private List<ResidentNode> residentNodeChildList;
	 
	private List<RandomVariableFinding> randomVariableFindingList; 
	
	private MFrag mFrag;

	private String tableFunction;
	
	/** 
	 * TODO the possible values (states) of this class does not conform to unbbayes.prs.Node and
	 * even {@link MultiEntityNode} sometimes,
	 *  mainly on methods for state (and size) retrieval, because states in unbbayes.prs.Node
	 *  is represented as string and in this class it is represented as StateLink. Methods
	 *  updating possibleValueList should also update super.states in order to archive such
	 *  compatibility or this class should overwrite  methods in Node.
	 */
	private List<StateLink> possibleValueList; 
	
	//Graphics informations 
	
	private static Color color = new Color(254, 250, 158); 	
	
	
	public static final int OBJECT_ENTITY = 0; 
	public static final int CATEGORY_RV_STATES = 1; 
	public static final int BOOLEAN_RV_STATES = 2; 
	
	private int typeOfStates = CATEGORY_RV_STATES; 
	
	private ICompiler compiler = null;
	
	//DON'T USE THIS CONSTRUCTOR! IS ONLY TEMPORARY FOR CLEAR THE TESTS
	public ResidentNode(){
	}
	
	public ResidentNode(String name, MFrag mFrag){
		super(); 
		listPointers = new ArrayList<ResidentNodePointer>(); 
		ordinaryVariableList = new ArrayList<OrdinaryVariable>(); 
        this.mFrag = mFrag; 
		
		inputInstanceFromList = new ArrayList<InputNode>(); 
		parentInputNodeList = new ArrayList<InputNode>();
		residentNodeFatherList = new ArrayList<ResidentNode>();	
		residentNodeChildList = new ArrayList<ResidentNode>();	
		randomVariableFindingList = new ArrayList<RandomVariableFinding>(); 
		possibleValueList = new ArrayList<StateLink>(); 
		
		setName(name); 
		updateLabel(); 		
		//by young
		setColor(new Color(254, 250, 158));
		
		// now, the resident node stores what kind of compiler the user has chosen to compile CPT
		this.compiler = Compiler.getInstance(this);
	}
	
	

	
	//------------------------GRAPHICS METHODS----------------------------------
	
	/**
     *  Gets all domain resident node's color.
     *
     * @return The color of all domain resident node's color.
     */
	//by young
   // public static Color getColor() {
    //    return color;
    //}

    /**
     *  Sets the new color for all domain resident node.
     *
     * @return The new color of all domain resident node in RGB.
     */
    //by young
//	public static void setColor(int c) {
 //       color = new Color(c);
  //  }	
    
	/**
	 * Update the label of this node. 
	 * The label is: 
	 *    LABEL := "name" "(" LIST_ARGS ")"
	 *    LIST_ARGS:= NAME_ARG "," LIST_ARGS | VAZIO 
	 *    
	 *  update too the copies of this labels in input nodes. 
	 */
	
	//by young
    public String updateLabel(){
    	
    	String newLabel; 
    	List<OrdinaryVariable> ordinaryVariableList = getOrdinaryVariableList(); 
    	
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
    	
    	for(InputNode inputNode: inputInstanceFromList){
    		inputNode.updateLabel(); 
    	}
    	
    	return newLabel;
    	
    }
	
	public void setName(String name){
		
		super.setName(name); 
		updateLabel(); 
		
	}
    
    
	
	
	
	
	/**
	 *@see unbbayes.prs.bn.IRandomVariable#getProbabilityFunction()
	 */
	public PotentialTable getProbabilityFunction() {
		return null;
	}
    
	
	
	
	
	//------------------------ SETS E GETS ----------------------------------

	public MFrag getMFrag(){
		return mFrag; 
	}
	
	public String getTableFunction(){
		return tableFunction; 
	}
	
	
	
	
	
	//------------------------ LISTS ----------------------------------
	
	public void setTableFunction(String table){
		tableFunction = table;
	}
	
    public void addResidentNodePointer(ResidentNodePointer pointer){
		listPointers.add(pointer); 
	}
	
	public void removeResidentNodePointer(ResidentNodePointer pointer){
		listPointers.remove(pointer); 
	}
	
	/**
	 * Add a node in the list of childs resident nodes of this node. In the node 
	 * child add this node in the list of fathers resident nodes.  
	 * @param node: the node that is child of this. 
	 */
	
	public void addResidentNodeChild(ResidentNode node){
		residentNodeChildList.add(node); 
		node.addResidentNodeFather(this); 
	}		
	
	/**
	 * @see <code>addResidentNodeChild</code> 
	 * @param father
	 */
	private void addResidentNodeFather(ResidentNode father){
		residentNodeFatherList.add(father);
	}
	
	/**
	 * Add a node in the list of input nodes fathers of this node. In the node 
	 * father add this node in the list of child resident nodes.  
	 * @param father
	 */
	protected void addInputNodeFather(InputNode father){
		parentInputNodeList.add(father); 
	}	 
	
	protected void addInputInstanceFromList(InputNode instance){
		inputInstanceFromList.add(instance);
	}
	

	public List<ResidentNode> getResidentNodeFatherList(){
		return this.residentNodeFatherList; 
	}
	
	public List<InputNode> getParentInputNodesList(){
		return this.parentInputNodeList; 
	}	
	
	public List<ResidentNode> getResidentNodeChildList(){
		return this.residentNodeChildList; 
	}
	
	public List<InputNode> getInputInstanceFromList(){
		return this.inputInstanceFromList; 
	}	
	
	/**
	 * Don't use this method! Use removeResidentNodeChildList
	 */
	private void removeResidentNodeFather(ResidentNode node){
		residentNodeFatherList.remove(node); 
	}
	
	protected void removeInputNodeFatherList(InputNode node){
		parentInputNodeList.remove(node); 
	}	
	
	/**
	 * Remove a node of the list of childs of this node. 
	 * 
	 * @param node
	 */
	public void removeResidentNodeChildList(ResidentNode node){
		residentNodeChildList.remove(node);
		node.removeResidentNodeFather(this); 
	}
	
	public void removeInputInstanceFromList(InputNode node){
		if (inputInstanceFromList.remove(node)) {
			// the following code initiates an infinite loop when inputInstanceFromList does not contain node...
			// that's why I included a test to avoid such situation...
			try{
				node.setInputInstanceOf((ResidentNode)null); 
			}
			catch(Exception e){
				e.printStackTrace(); 
			}
		}
	}		
	
	
	/*
	 * FINDINGS METHODS
	 */
	
	public void addRandomVariableFinding(RandomVariableFinding finding){
		randomVariableFindingList.add(finding); 
	}
	
	public void removeRandomVariableFinding(RandomVariableFinding finding){
		randomVariableFindingList.remove(finding); 
	}
	
	public boolean containsRandomVariableFinding(RandomVariableFinding finding){
		return randomVariableFindingList.contains(finding); 
	}
	
	public void cleanRandomVariableFindingList(){
		randomVariableFindingList.clear(); 
	}
	
	public List<RandomVariableFinding> getRandomVariableFindingList() {
		return randomVariableFindingList;
	}
	
	
	
	
	/*-------------------------- ARGUMENTS ----------------------------------*/
	
	/**
	 * Add a ov in the list of arguments in this resident node
	 * 
	 * @param ov
	 * 
	 * @param addArgument true if a Argument object shoud be create for the ov. 
	 *        Otherside the method addArgumenet(Argument) should to be called 
	 *        for mantain the consistency of the structure. 
	 * 
	 * @throws ArgumentNodeAlreadySetException
	 * 
	 * @throws OVariableAlreadyExistsInArgumentList
	 */
	public void addArgument(OrdinaryVariable ov, boolean addArgument) throws ArgumentNodeAlreadySetException, 
	OVariableAlreadyExistsInArgumentList{
		
		if(ordinaryVariableList.contains(ov)){
			throw new OVariableAlreadyExistsInArgumentList(); 
		}
		else{
			int position = ordinaryVariableList.size();
			
			ordinaryVariableList.add(ov); 
			ov.addIsOVariableOfList(this); 
			
			//update the argument list
			if(addArgument){
				Argument argument = new Argument("", this); 
				argument.setArgNumber(position + 1);
				argument.setOVariable(ov); 
				this.addArgument(argument); 
			}
			
			for(InputNode inputNode: inputInstanceFromList){
				inputNode.updateResidentNodePointer(); 
			}
			
			updateLabel(); 
		}
	}
	
	public void removeArgument(OrdinaryVariable ov){
		
		ordinaryVariableList.remove(ov);

		int indexOfArgumentRemoved = 0; 
		for(Argument argument: super.getArgumentList()){
			if(argument.getOVariable() == ov){
				indexOfArgumentRemoved = argument.getArgNumber(); 
				super.removeArgument(argument); 
				break;  
			}
		}
		
		//Update the position of the arguments
		//This can be simplified with the ordenation of the list of arguments
		//how a pre-requisite
		for(Argument argument: super.getArgumentList()){
			if(argument.getArgNumber() > indexOfArgumentRemoved){
				argument.setArgNumber(argument.getArgNumber() - 1); 
			}
		}
		
		updateLabel(); 
	}
	
	/**
	 * 
	 * @param ov
	 * @return
	 */
	public boolean containsArgument(OrdinaryVariable ov){
		return ordinaryVariableList.contains(ov); 
	}
	
	public List<OrdinaryVariable> getOrdinaryVariableList(){
		return ordinaryVariableList; 
	}

	public OrdinaryVariable getOrdinaryVariableByName(String name){
		for(OrdinaryVariable ov: ordinaryVariableList){
			if(ov.getName().equals(name)){
				return ov; 
			}
		}
		return null; 
	}
	
	/**
	 * 
	 * @param ov
	 * @return indice or -1 if the ov don't is an argument. 
	 */
	public int getOrdinaryVariableIndex(OrdinaryVariable ov){
		for(int i= 0; i < ordinaryVariableList.size(); i++){
			if(ordinaryVariableList.get(i).equals(ov)){
				return i; 
			}
		}
		return -1; 
	}
	
	/**
	 * Recover the ordinary variable in the index position, or null if the 
	 * position don't exists. 
	 * 
	 * @param index Index of the ordinary variable to recover
	 */
	public OrdinaryVariable getOrdinaryVariableByIndex(int index){
		
		if((index < 0 )||(index > ordinaryVariableList.size())){
			return null; 
		}else{
			return ordinaryVariableList.get(index); 
		}
		
	}
	
	/**
	 * @return A list with all the ordinary variables ordereables present in this node.
	 */
	public List<OrdinaryVariable> getOrdinaryVariablesOrdereables(){
		
		List<OrdinaryVariable> ovOrdereableList = new ArrayList<OrdinaryVariable>();
		ObjectEntityConteiner oeConteiner = this.getMFrag().getMultiEntityBayesianNetwork().getObjectEntityContainer();
		
		for(OrdinaryVariable ov: this.getOrdinaryVariableList()){
			ObjectEntity oe = oeConteiner.getObjectEntityByType(ov.getValueType()); 
			if(oe.isOrdereable()){
				ovOrdereableList.add(ov);
			}
		}
		
		return ovOrdereableList;
	}
	
	
	
	
	/*-------------------------- STATES ----------------------------------*/
	
	public int getTypeOfStates() {
		return typeOfStates;
	}

	public void setTypeOfStates(int typeOfStates) {
		this.typeOfStates = typeOfStates;
	}
	
	
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
		for(Entity value : this.getPossibleValueListIncludingEntityInstances()){
			if (value.getName().equalsIgnoreCase(possibleValue)){
				/* 
				 * TODO since this.getPossibleValueListIncludingEntityInstances() searches for 
				 * StateLink (but converts it to Entity) and the code below turns it back to StateLink again,
				 * we might optimize this.
				 */
				return new StateLink(value); 
			}
		}
		
		return null;
	}
	
	public List<StateLink> getPossibleValueLinkList(){
		return possibleValueList; 
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
	
	
	
    /** 
	 * Overrides unbbayes.prs.mebn.MultiEntityNode#getPossibleValueIndex(java.lang.String),
	 * but also considers the entity instances (calls 
	 * unbbayes.prs.mebn.DomainResidentNode#getPossibleValueListIncludingEntityInstances() internally.
	 * @see unbbayes.prs.mebn.MultiEntityNode#getPossibleValueIndex(java.lang.String)
	 * @see unbbayes.prs.mebn.DomainResidentNode#getPossibleValueListIncludingEntityInstances()
	 */
	@Override
	public int getPossibleValueIndex(String stateName) {
		int index = 0;
		for (Entity entity : this.getPossibleValueListIncludingEntityInstances()) {
			if (entity.getName().equalsIgnoreCase(stateName)) {
				return index;
			}
			index++;
		}
		return -1;
	}

	
	/**
	 * This is identical to unbbayes.prs.mebn.DomainResidentNode#getPossibleValueList() but
	 * the returned list also includes the entity instances from object entities.
	 * This would be useful when retrieving instances on SSBN generation step.
	 * @return a list containing entities and, when instances are present, those instances. When
	 * retrieving instances, the ObjectEntity itself (the instance container) is not retrieved
	 * @see unbbayes.prs.mebn.DomainResidentNode#getPossibleValueList()
	 */
	public List<Entity> getPossibleValueListIncludingEntityInstances() {
		List<Entity> ret = new ArrayList<Entity>();
		for (StateLink link : this.possibleValueList) {
			if (link.getState() instanceof ObjectEntity) {
				// TODO the above "instanceof" is a serious indication of a refactoring necessity
				for (ObjectEntityInstance instance : ((ObjectEntity)link.getState()).getInstanceList()) {
					ret.add(instance);
				}
			} else {
				ret.add(link.getState());
			}
		}
		return ret;
	}
	
	
	/*-------------------------- GENERAL METHODS ----------------------------------*/
	
	/**
	 * Delete the extern references for this node
	 * 
	 * - Ordinary Variables
	 * - Fathers nodes (and edges) 
     * - Child nodes (and edges)
	 */
	public void delete(){
		
		while(!ordinaryVariableList.isEmpty()){
			ordinaryVariableList.remove(0).removeIsOVariableOfList(this); 
		}
		
		while(!inputInstanceFromList.isEmpty()){
			inputInstanceFromList.remove(0).setInputInstanceOf(); 
		}
		
		while(!parentInputNodeList.isEmpty()){
			parentInputNodeList.remove(0).removeResidentNodeChild(this); 
		}
		
		while(!residentNodeFatherList.isEmpty()){
			ResidentNode father = residentNodeFatherList.get(0); 
			father.removeResidentNodeChildList(this); 
			mFrag.removeEdgeByNodes(father, this);
		}
		
		while(!residentNodeChildList.isEmpty()){
			ResidentNode child = residentNodeChildList.get(0); 
			this.removeResidentNodeChildList(child);
			mFrag.removeEdgeByNodes(this, child); 
		}
		
		mFrag.removeResidentNode(this); 
		
	}
	
	public String toString() {
		return name;
	}

	/**
	 * Obtains the CPT compiler for this node.
	 * This compiler will be used by SSBN generation algorithm in order to generate CPTs.
	 * @return a non null value
	 */
	public ICompiler getCompiler() {
		if (this.compiler == null) {
			ICompiler comp = Compiler.getInstance(this);
			this.setCompiler(comp);
			return comp;
		}
		return compiler;
	}

	/**
	 * Sets the CPT compiler for this node.
	 * This compiler will be used by SSBN generation algorithm in order to generate CPTs.
	 * @param compiler
	 */
	public void setCompiler(ICompiler compiler) {
		this.compiler = compiler;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.MultiEntityNode#hasPossibleValue(java.lang.String)
	 */
	@Override
	public boolean hasPossibleValue(String stateName) {
		for(StateLink value : this.getPossibleValueLinkList()){
			if (value.getState() != null && stateName.equals(value.getState().getName())){
				return true; 
			}
		}
		return false;
	}
	
}

