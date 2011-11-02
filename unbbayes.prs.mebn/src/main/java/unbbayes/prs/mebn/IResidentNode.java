package unbbayes.prs.mebn;

import java.util.List;

import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.mebn.compiler.ICompiler;
import unbbayes.prs.mebn.entity.Entity;
import unbbayes.prs.mebn.entity.StateLink;
import unbbayes.prs.mebn.exception.ArgumentNodeAlreadySetException;
import unbbayes.prs.mebn.exception.OVariableAlreadyExistsInArgumentList;

public interface IResidentNode {

	public static final int OBJECT_ENTITY = 0;
	public static final int CATEGORY_RV_STATES = 1;
	public static final int BOOLEAN_RV_STATES = 2;

	/**
	 * Update the label of this node. 
	 * The label is: 
	 *    LABEL := "name" "(" LIST_ARGS ")"
	 *    LIST_ARGS:= NAME_ARG "," LIST_ARGS | VAZIO 
	 *    
	 *  update too the copies of this labels in input nodes. 
	 */

	//by young
	public String updateLabel();

	public void setName(String name);

	/**
	 *@see unbbayes.prs.bn.IRandomVariable#getProbabilityFunction()
	 */
	public PotentialTable getProbabilityFunction();

	public MFrag getMFrag();

	public String getTableFunction();

	public void setTableFunction(String table);

	public void addResidentNodePointer(ResidentNodePointer pointer);

	public void removeResidentNodePointer(ResidentNodePointer pointer);

	/**
	 * Add a node in the list of childs resident nodes of this node. In the node 
	 * child add this node in the list of fathers resident nodes.  
	 * @param node: the node that is child of this. 
	 */

	public void addResidentNodeChild(ResidentNode node);

	public List<ResidentNode> getResidentNodeFatherList();

	public List<InputNode> getParentInputNodesList();

	public List<ResidentNode> getResidentNodeChildList();

	public List<InputNode> getInputInstanceFromList();

	/**
	 * Remove a node of the list of childs of this node. 
	 * 
	 * @param node
	 */
	public void removeResidentNodeChildList(ResidentNode node);

	public void removeInputInstanceFromList(InputNode node);

	public void addRandomVariableFinding(RandomVariableFinding finding);

	public void removeRandomVariableFinding(RandomVariableFinding finding);

	public boolean containsRandomVariableFinding(RandomVariableFinding finding);

	public void cleanRandomVariableFindingList();

	public List<RandomVariableFinding> getRandomVariableFindingList();

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
	public void addArgument(OrdinaryVariable ov, boolean addArgument)
			throws ArgumentNodeAlreadySetException,
			OVariableAlreadyExistsInArgumentList;

	public void removeArgument(OrdinaryVariable ov);

	/**
	 * 
	 * @param ov
	 * @return
	 */
	public boolean containsArgument(OrdinaryVariable ov);

	public List<OrdinaryVariable> getOrdinaryVariableList();

	public OrdinaryVariable getOrdinaryVariableByName(String name);

	/**
	 * 
	 * @param ov
	 * @return indice or -1 if the ov don't is an argument. 
	 */
	public int getOrdinaryVariableIndex(OrdinaryVariable ov);

	/**
	 * Recover the ordinary variable in the index position, or null if the 
	 * position don't exists. 
	 * 
	 * @param index Index of the ordinary variable to recover
	 */
	public OrdinaryVariable getOrdinaryVariableByIndex(int index);

	/**
	 * @return A list with all the ordinary variables ordereables present in this node.
	 */
	public List<OrdinaryVariable> getOrdinaryVariablesOrdereables();

	public int getTypeOfStates();

	/**
	 * @deprecated because the type of the states are unpredictable (they are more
	 * than mere booleans, categoricals and etc.), this kind of subdivision should perish.
	 * @param typeOfStates
	 */
	public void setTypeOfStates(int typeOfStates);

	/**
	 * Add a possible value to the list of possible values of
	 * the domain resident node. 
	 */
	public StateLink addPossibleValueLink(Entity possibleValue);

	/**
	 * Remove the possible value with the name 
	 * @param possibleValue name of the possible value
	 */
	public void removePossibleValueByName(String possibleValue);

	/**
	 * Remove all possible values of the node
	 */
	public void removeAllPossibleValues();

	/**
	 * Verifies if the possible value is on the list of possible values
	 * of the node. 
	 * @param possibleValue name of the possible value
	 * @return true if it is present or false otherside
	 */
	public boolean existsPossibleValueByName(String possibleValue);

	/**
	 * Verify if the entity is a state of the node 
	 * Warning: the search will be for the entity and not for the
	 * name of entity.
	 * @param entity The entity 
	 * @return true if the entity is a state, false otherside
	 */
	public boolean hasPossibleValue(Entity entity);

	/**
	 * Return the possible value of the residente node with the name
	 * (return null if don't exist a possible value with this name)
	 */
	public StateLink getPossibleValueByName(String possibleValue);

	/**
	 * getter for possibleValueList
	 * @return
	 */
	public List<StateLink> getPossibleValueLinkList();

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.MultiEntityNode#getPossibleValueList()
	 */
	public List<Entity> getPossibleValueList();

	/** 
	 * Overrides unbbayes.prs.mebn.MultiEntityNode#getPossibleValueIndex(java.lang.String),
	 * but also considers the entity instances (calls 
	 * unbbayes.prs.mebn.DomainResidentNode#getPossibleValueListIncludingEntityInstances() internally.
	 * @see unbbayes.prs.mebn.MultiEntityNode#getPossibleValueIndex(java.lang.String)
	 * @see unbbayes.prs.mebn.DomainResidentNode#getPossibleValueListIncludingEntityInstances()
	 */
	public int getPossibleValueIndex(String stateName);

	/**
	 * This is identical to unbbayes.prs.mebn.DomainResidentNode#getPossibleValueList() but
	 * the returned list also includes the entity instances from object entities.
	 * This would be useful when retrieving instances on SSBN generation step.
	 * @return a list containing entities and, when instances are present, those instances. When
	 * retrieving instances, the ObjectEntity itself (the instance container) is not retrieved
	 * @see unbbayes.prs.mebn.DomainResidentNode#getPossibleValueList()
	 */
	public List<Entity> getPossibleValueListIncludingEntityInstances();

	/**
	 * Delete the extern references for this node
	 * 
	 * - Ordinary Variables
	 * - Fathers nodes (and edges) 
	 * - Child nodes (and edges)
	 */
	public void delete();

	public String toString();

	/**
	 * Obtains the CPT compiler for this node.
	 * This compiler will be used by SSBN generation algorithm in order to generate CPTs.
	 * @return a non null value
	 */
	public ICompiler getCompiler();

	/**
	 * Sets the CPT compiler for this node.
	 * This compiler will be used by SSBN generation algorithm in order to generate CPTs.
	 * @param compiler
	 */
	public void setCompiler(ICompiler compiler);

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.MultiEntityNode#hasPossibleValue(java.lang.String)
	 */
	public boolean hasPossibleValue(String stateName);

}