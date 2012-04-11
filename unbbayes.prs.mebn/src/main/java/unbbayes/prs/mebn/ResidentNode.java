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
import unbbayes.prs.mebn.entity.StateLink;
import unbbayes.prs.mebn.exception.ArgumentNodeAlreadySetException;
import unbbayes.prs.mebn.exception.OVariableAlreadyExistsInArgumentList;

/**
 * This class represents a resident node.
 */
public class ResidentNode extends MultiEntityNode 
         implements IRandomVariable, IResidentNode {
	
	private boolean isToLimitQuantityOfParentsInstances = false;
	
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
	
	
	private int typeOfStates = CATEGORY_RV_STATES; 
	
	private ICompiler compiler = null;
	
	//DON'T USE THIS CONSTRUCTOR! IS ONLY TEMPORARY FOR CLEAR THE TESTS
	public ResidentNode(){
	}
	
	public ResidentNode(String name, MFrag mFrag){
		super(); 
		setListPointers(new ArrayList<ResidentNodePointer>()); 
		setOrdinaryVariableList(new ArrayList<OrdinaryVariable>()); 
        this.setMFrag(mFrag); 
		
		setInputInstanceFromList(new ArrayList<InputNode>()); 
		setParentInputNodeList(new ArrayList<InputNode>());
		setResidentNodeFatherList(new ArrayList<ResidentNode>());	
		setResidentNodeChildList(new ArrayList<ResidentNode>());	
		setRandomVariableFindingList(new ArrayList<RandomVariableFinding>()); 
		setPossibleValueLinkList(new ArrayList<StateLink>()); 
		
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
    
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IResidentNode#updateLabel()
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
    	
    	for(InputNode inputNode: getInputInstanceFromList()){
    		inputNode.updateLabel(); 
    	}
    	
    	return newLabel;
    	
    }
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IResidentNode#setName(java.lang.String)
	 */
	public void setName(String name){
		
		super.setName(name); 
		updateLabel(); 
		
	}
    
    
	
	
	
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IResidentNode#getProbabilityFunction()
	 */
	public PotentialTable getProbabilityFunction() {
		return null;
	}
    
	
	
	
	
	//------------------------ SETS E GETS ----------------------------------

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IResidentNode#getMFrag()
	 */
	public MFrag getMFrag(){
		return mFrag; 
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IResidentNode#getTableFunction()
	 */
	public String getTableFunction(){
		return tableFunction; 
	}
	
	
	
	
	
	//------------------------ LISTS ----------------------------------
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IResidentNode#setTableFunction(java.lang.String)
	 */
	public void setTableFunction(String table){
		tableFunction = table;
	}
	
    /* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IResidentNode#addResidentNodePointer(unbbayes.prs.mebn.ResidentNodePointer)
	 */
    public void addResidentNodePointer(ResidentNodePointer pointer){
		getListPointers().add(pointer); 
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IResidentNode#removeResidentNodePointer(unbbayes.prs.mebn.ResidentNodePointer)
	 */
	public void removeResidentNodePointer(ResidentNodePointer pointer){
		getListPointers().remove(pointer); 
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IResidentNode#addResidentNodeChild(unbbayes.prs.mebn.ResidentNode)
	 */
	
	public void addResidentNodeChild(ResidentNode node){
		getResidentNodeChildList().add(node); 
		node.addResidentNodeFather(this); 
	}		
	
	/**
	 * @see <code>addResidentNodeChild</code> 
	 * @param father
	 */
	private void addResidentNodeFather(ResidentNode father){
		getResidentNodeFatherList().add(father);
	}
	
	/**
	 * Add a node in the list of input nodes fathers of this node. In the node 
	 * father add this node in the list of child resident nodes.  
	 * @param father
	 */
	protected void addInputNodeFather(InputNode father){
		getParentInputNodesList().add(father); 
	}	 
	
	protected void addInputInstanceFromList(InputNode instance){
		getInputInstanceFromList().add(instance);
	}
	

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IResidentNode#getResidentNodeFatherList()
	 */
	public List<ResidentNode> getResidentNodeFatherList(){
		return this.residentNodeFatherList; 
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IResidentNode#getParentInputNodesList()
	 */
	public List<InputNode> getParentInputNodesList(){
		return this.parentInputNodeList;
	}	
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IResidentNode#getResidentNodeChildList()
	 */
	public List<ResidentNode> getResidentNodeChildList(){
		return this.residentNodeChildList; 
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IResidentNode#getInputInstanceFromList()
	 */
	public List<InputNode> getInputInstanceFromList(){
		return this.inputInstanceFromList; 
	}	
	
	/**
	 * Don't use this method! Use removeResidentNodeChildList
	 */
	private void removeResidentNodeFather(ResidentNode node){
		getResidentNodeFatherList().remove(node); 
	}
	
	protected void removeInputNodeFatherList(InputNode node){
		getParentInputNodesList().remove(node); 
	}	
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IResidentNode#removeResidentNodeChildList(unbbayes.prs.mebn.ResidentNode)
	 */
	public void removeResidentNodeChildList(ResidentNode node){
		getResidentNodeChildList().remove(node);
		node.removeResidentNodeFather(this); 
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IResidentNode#removeInputInstanceFromList(unbbayes.prs.mebn.InputNode)
	 */
	public void removeInputInstanceFromList(InputNode node){
		if (getInputInstanceFromList().remove(node)) {
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
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IResidentNode#addRandomVariableFinding(unbbayes.prs.mebn.RandomVariableFinding)
	 */
	public void addRandomVariableFinding(RandomVariableFinding finding){
		getRandomVariableFindingList().add(finding); 
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IResidentNode#removeRandomVariableFinding(unbbayes.prs.mebn.RandomVariableFinding)
	 */
	public void removeRandomVariableFinding(RandomVariableFinding finding){
		getRandomVariableFindingList().remove(finding); 
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IResidentNode#containsRandomVariableFinding(unbbayes.prs.mebn.RandomVariableFinding)
	 */
	public boolean containsRandomVariableFinding(RandomVariableFinding finding){
		return getRandomVariableFindingList().contains(finding); 
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IResidentNode#cleanRandomVariableFindingList()
	 */
	public void cleanRandomVariableFindingList(){
		getRandomVariableFindingList().clear(); 
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IResidentNode#getRandomVariableFindingList()
	 */
	public List<RandomVariableFinding> getRandomVariableFindingList() {
		return randomVariableFindingList;
	}
	
	
	
	
	/*-------------------------- ARGUMENTS ----------------------------------*/
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IResidentNode#addArgument(unbbayes.prs.mebn.OrdinaryVariable, boolean)
	 */
	public void addArgument(OrdinaryVariable ov, boolean addArgument) throws ArgumentNodeAlreadySetException, 
	OVariableAlreadyExistsInArgumentList{
		
		if(getOrdinaryVariableList().contains(ov)){
			throw new OVariableAlreadyExistsInArgumentList(); 
		}
		else{
			int position = getOrdinaryVariableList().size();
			
			getOrdinaryVariableList().add(ov); 
			ov.addIsOVariableOfList(this); 
			
			//update the argument list
			if(addArgument){
				Argument argument = new Argument("", this); 
				argument.setArgNumber(position + 1);
				argument.setOVariable(ov); 
				this.addArgument(argument); 
			}
			
			for(InputNode inputNode: getInputInstanceFromList()){
				inputNode.updateResidentNodePointer(); 
			}
			
			updateLabel(); 
		}
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IResidentNode#removeArgument(unbbayes.prs.mebn.OrdinaryVariable)
	 */
	public void removeArgument(OrdinaryVariable ov){
		
		getOrdinaryVariableList().remove(ov);

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
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IResidentNode#containsArgument(unbbayes.prs.mebn.OrdinaryVariable)
	 */
	public boolean containsArgument(OrdinaryVariable ov){
		return getOrdinaryVariableList().contains(ov); 
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IResidentNode#getOrdinaryVariableList()
	 */
	public List<OrdinaryVariable> getOrdinaryVariableList(){
		return ordinaryVariableList; 
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IResidentNode#getOrdinaryVariableByName(java.lang.String)
	 */
	public OrdinaryVariable getOrdinaryVariableByName(String name){
		for(OrdinaryVariable ov: getOrdinaryVariableList()){
			if(ov.getName().equals(name)){
				return ov; 
			}
		}
		return null; 
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IResidentNode#getOrdinaryVariableIndex(unbbayes.prs.mebn.OrdinaryVariable)
	 */
	public int getOrdinaryVariableIndex(OrdinaryVariable ov){
		for(int i= 0; i < getOrdinaryVariableList().size(); i++){
			if(getOrdinaryVariableList().get(i).equals(ov)){
				return i; 
			}
		}
		return -1; 
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IResidentNode#getOrdinaryVariableByIndex(int)
	 */
	public OrdinaryVariable getOrdinaryVariableByIndex(int index){
		
		if((index < 0 )||(index > getOrdinaryVariableList().size())){
			return null; 
		}else{
			return getOrdinaryVariableList().get(index); 
		}
		
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IResidentNode#getOrdinaryVariablesOrdereables()
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
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IResidentNode#getTypeOfStates()
	 */
	public int getTypeOfStates() {
		return typeOfStates;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IResidentNode#setTypeOfStates(int)
	 */
	public void setTypeOfStates(int typeOfStates) {
		this.typeOfStates = typeOfStates;
	}
	
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IResidentNode#addPossibleValueLink(unbbayes.prs.mebn.entity.Entity)
	 */
	public StateLink addPossibleValueLink(Entity possibleValue){
		StateLink value = new StateLink(possibleValue); 
		possibleValueList.add(value);
		return value; 
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.mebn.MultiEntityNode#addPossibleValue(unbbayes.prs.mebn.entity.Entity)
	 */
	public void addPossibleValue(Entity possibleValue){
		this.addPossibleValueLink(possibleValue);
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IResidentNode#removePossibleValueByName(java.lang.String)
	 */
	public void removePossibleValueByName(String possibleValue){
		
		for(StateLink value : possibleValueList){
			if (value.getState().getName().equals(possibleValue)){
				possibleValueList.remove(value);
				return; 
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IResidentNode#removeAllPossibleValues()
	 */
	public void removeAllPossibleValues(){
		possibleValueList.clear(); 
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IResidentNode#existsPossibleValueByName(java.lang.String)
	 */
	public boolean existsPossibleValueByName(String possibleValue){
		
		for(StateLink value : possibleValueList){
			if (value.getState().getName().equals(possibleValue)){
				return true; 
			}
		}
		
		return false; 
	}	
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IResidentNode#hasPossibleValue(unbbayes.prs.mebn.entity.Entity)
	 */
	public boolean hasPossibleValue(Entity entity) {
		for(StateLink value : possibleValueList){
			if (value.getState() == entity){
				return true; 
			}
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IResidentNode#getPossibleValueByName(java.lang.String)
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
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IResidentNode#getPossibleValueLinkList()
	 */
	public List<StateLink> getPossibleValueLinkList(){
		return possibleValueList; 
	}
	
	/**
	 * setter for possibleValueList.
	 * @param possibleValueList
	 * @see #getPossibleValueLinkList()
	 */
	protected void setPossibleValueLinkList(List<StateLink> possibleValueList){
		this.possibleValueList = possibleValueList ; 
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.MultiEntityNode#getPossibleValueList()
	 */
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IResidentNode#getPossibleValueList()
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
	
	
	
    /* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IResidentNode#getPossibleValueIndex(java.lang.String)
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

	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IResidentNode#getPossibleValueListIncludingEntityInstances()
	 */
	public List<Entity> getPossibleValueListIncludingEntityInstances() {
		List<Entity> ret = new ArrayList<Entity>();
		for (StateLink link : this.possibleValueList) {
			Entity state = link.getState();
			if (state instanceof ObjectEntity) {
				// TODO the above "instanceof" is a serious indication of a refactoring necessity
				ret.addAll(((ObjectEntity)state).getInstanceList());
			} else {
				ret.add(state);
			}
		}
		return ret;
	}
	
	
	/*-------------------------- GENERAL METHODS ----------------------------------*/
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IResidentNode#delete()
	 */
	public void delete(){
		
		while(!getOrdinaryVariableList().isEmpty()){
			getOrdinaryVariableList().remove(0).removeIsOVariableOfList(this); 
		}
		
		while(!getInputInstanceFromList().isEmpty()){
			getInputInstanceFromList().remove(0).setInputInstanceOf(); 
		}
		
		while(!getParentInputNodesList().isEmpty()){
			getParentInputNodesList().remove(0).removeResidentNodeChild(this); 
		}
		
		while(!getResidentNodeFatherList().isEmpty()){
			ResidentNode father = getResidentNodeFatherList().get(0); 
			father.removeResidentNodeChildList(this); 
			getMFrag().removeEdgeByNodes(father, this);
		}
		
		while(!getResidentNodeChildList().isEmpty()){
			ResidentNode child = getResidentNodeChildList().get(0); 
			this.removeResidentNodeChildList(child);
			getMFrag().removeEdgeByNodes(this, child); 
		}
		
		getMFrag().removeResidentNode(this); 
		
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IResidentNode#toString()
	 */
	public String toString() {
		return name;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IResidentNode#getCompiler()
	 */
	public ICompiler getCompiler() {
		if (this.compiler == null) {
			ICompiler comp = Compiler.getInstance(this);
			this.setCompiler(comp);
			return comp;
		}
		return compiler;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IResidentNode#setCompiler(unbbayes.prs.mebn.compiler.ICompiler)
	 */
	public void setCompiler(ICompiler compiler) {
		this.compiler = compiler;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.MultiEntityNode#hasPossibleValue(java.lang.String)
	 */
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IResidentNode#hasPossibleValue(java.lang.String)
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

	/**
	 * @param listPointers the listPointers to set
	 */
	protected void setListPointers(List<ResidentNodePointer> listPointers) {
		this.listPointers = listPointers;
	}

	/**
	 * @return the listPointers
	 */
	protected List<ResidentNodePointer> getListPointers() {
		return listPointers;
	}

	/**
	 * @param ordinaryVariableList the ordinaryVariableList to set
	 */
	protected void setOrdinaryVariableList(List<OrdinaryVariable> ordinaryVariableList) {
		this.ordinaryVariableList = ordinaryVariableList;
	}

	/**
	 * @param mFrag the mFrag to set
	 */
	protected void setMFrag(MFrag mFrag) {
		this.mFrag = mFrag;
	}


	/**
	 * @param inputInstanceFromList the inputInstanceFromList to set
	 */
	protected void setInputInstanceFromList(List<InputNode> inputInstanceFromList) {
		this.inputInstanceFromList = inputInstanceFromList;
	}

	/**
	 * @param parentInputNodeList the parentInputNodeList to set
	 */
	protected void setParentInputNodeList(List<InputNode> parentInputNodeList) {
		this.parentInputNodeList = parentInputNodeList;
	}


	/**
	 * @param residentNodeFatherList the residentNodeFatherList to set
	 */
	protected void setResidentNodeFatherList(List<ResidentNode> residentNodeFatherList) {
		this.residentNodeFatherList = residentNodeFatherList;
	}

	/**
	 * @param residentNodeChildList the residentNodeChildList to set
	 */
	protected void setResidentNodeChildList(List<ResidentNode> residentNodeChildList) {
		this.residentNodeChildList = residentNodeChildList;
	}

	/**
	 * @param randomVariableFindingList the randomVariableFindingList to set
	 */
	protected void setRandomVariableFindingList(
			List<RandomVariableFinding> randomVariableFindingList) {
		this.randomVariableFindingList = randomVariableFindingList;
	}

	/**
	 * @return 
	 * If this value is true, the following behavior will happen at the SSBN generator. <br/>
	 * If a node is going to have too many parents, and the LPD of node can be represented as a chain like the following network: <br/>
	 * Suppose E is a boolean OR: <br/>
	 * Parents: A B C D	<br/>
	 * Child: E                  <br/>
	 *                      <br/>
	 * It may be represented as:<br/>
	 *  <br/>
	 * A B <br/>
	 * | / <br/>
	 * Y C <br/>
	 * | / <br/>
	 * X D <br/>
	 * | / <br/>
	 * E <br/>
	 *              <br/><br/>
	 * Note: X and Y have the same LPD of E (they are also boolean OR) <br/>
	 * <br/>                                    
	 * This value indicates the maximum quantity of parents for nodes
	 * E, X and Y in the above example.
	 */
	public boolean isToLimitQuantityOfParentsInstances() {
		return isToLimitQuantityOfParentsInstances;
	}

	/**
	 * @param isToLimit : 
	 * If this value is true, the following behavior will happen at the SSBN generator. <br/>
	 * If a node is going to have too many parents, and the LPD of node can be represented as a chain like the following network: <br/>
	 * Suppose E is a boolean OR: <br/>
	 * Parents: A B C D	<br/>
	 * Child: E                  <br/>
	 *                      <br/>
	 * It may be represented as:<br/>
	 *  <br/>
	 * A B <br/>
	 * | / <br/>
	 * Y C <br/>
	 * | / <br/>
	 * X D <br/>
	 * | / <br/>
	 * E <br/>
	 *              <br/><br/>
	 * Note: X and Y have the same LPD of E (they are also boolean OR) <br/>
	 * <br/>                                    
	 * This value indicates the maximum quantity of parents for nodes
	 * E, X and Y in the above example.
	 */
	public void setToLimitQuantityOfParentsInstances(boolean isToLimit) {
		if (isToLimit && this.getPossibleValueByName("true") == null) {
			throw new IllegalStateException("Limitation on quantity of parents available only for boolean nodes.");
		}
		this.isToLimitQuantityOfParentsInstances = isToLimit;
	}
	
}

