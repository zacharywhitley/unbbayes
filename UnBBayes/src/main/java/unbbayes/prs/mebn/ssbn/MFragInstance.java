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

package unbbayes.prs.mebn.ssbn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.entity.StateLink;
import unbbayes.prs.mebn.ssbn.exception.MFragContextFailException;

/**
 * Represent a MFrag instanciated for a set of entities and encapsule the state
 * of evaluation of the context nodes of this MFrag for this set of entities. 
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 */

/*
 * Notes: 
 * The MFragInstance exist only in the builder phase of the algorithm. 
 */
public class MFragInstance {
	
	private MFrag mFragOrigin; 
	
	private SSBN ssbn; 
	
	private ContextNodeEvaluator contextNodeAvaliator; 
	
	private boolean useDefaultDistribution; 
	
	//Teorem: For the same set of ordinary variables instance values, 
	//        the evaluation of the context node should be the same. 
	private OrdinaryVariable[] ovList; 
	private SimpleContextNodeFatherSSBNNode[] contextForOVList; 
	
	private EntityTree entityTree; 
	
	//This three lists contains the evaluation of the MFrag: the nodes and the 
	//edges generateds. 
	private List<SimpleSSBNNode> ssbNodeList; 
	private List<SimpleEdge> edgeList; 
	
	private ContextNode[] contextNodeList; 
	private ContextNodeEvaluationState[] contextNodeEvaluationStateList; 
	
	private boolean evaluated = false; 
	
	private MFragInstance(MFrag mFragOrigin){
		
		int index = 0; 
		int size = 0; 
		
		this.mFragOrigin = mFragOrigin; 
		
		//Create the list of states of evaluation of the ordinary variables.
		size = mFragOrigin.getOrdinaryVariableList().size(); 
		index = 0; 
		this.ovList = new OrdinaryVariable[size];  
		this.contextForOVList = new SimpleContextNodeFatherSSBNNode[size]; 
		
		for(OrdinaryVariable ov: mFragOrigin.getOrdinaryVariableList()){
			ovList[index] = ov; 
			contextForOVList[index] = null; 
			index++; 
		}
		
		//Create the list of states of evaluation of the context nodes. 
		size = mFragOrigin.getContextNodeList().size(); 
		index = 0;  
		contextNodeList = new ContextNode[size]; 
		contextNodeEvaluationStateList = new ContextNodeEvaluationState[size]; 
		for(ContextNode context: mFragOrigin.getContextNodeList()){
			contextNodeList[index] = context; 
			contextNodeEvaluationStateList[index] = ContextNodeEvaluationState.NOT_EVALUATED_YET; 
			index++; 
		}
		
		this.entityTree = new EntityTree(); 
		
		this.ssbNodeList = new ArrayList<SimpleSSBNNode>(); 
		this.edgeList = new ArrayList<SimpleEdge>(); 
		
	}
	
	public static MFragInstance getInstance(MFrag mFragOrigin){
		return new MFragInstance(mFragOrigin); 
	}
	
	//Nodes
	
	/**
	 * Add a value of a OV that is instanciated for a unique value. 
	 */
	public void addOVValue(OrdinaryVariable ov, String entity) throws MFragContextFailException{
		
		this.entityTree.updateTreeForNewInformation(ov, entity); 
		
	}
	
	/**
	 * Update the  evaluation of the context nodes of a MFrag with the new result. 
	 * 
	 * @param ovArray               Sequence of the ordinary variables of the context node
	 * @param entityValuesArray     Sequence of possible evaluations for the OV set. 
	 * 
	 * @throws MFragContextFailException throw if the new results added 
	 *                                   don't are possible against the previous 
	 *                                   result. The context node set of the mfrag
	 *                                   fail (don't is consistent). 
	 */
	public void addOVValuesCombination(OrdinaryVariable ovArray[], List<String[]> entityValuesArray) 
 	                 throws MFragContextFailException{
		
		this.entityTree.updateTreeForNewInformation(ovArray, entityValuesArray); 
	
	}
	
	public void addOVValueCombination(OrdinaryVariable ov, List<String> entityValues) 
	                 throws MFragContextFailException{

		OrdinaryVariable ovArray[] = new OrdinaryVariable[1];
		List<String[]> entityValuesArray = new ArrayList<String[]>(); 

		ovArray[0] = ov; 

		for(String entity: entityValues){
			String[] entitiesArray = new String[1]; 
			entitiesArray[0] = entity; 
			entityValuesArray.add(entitiesArray); 
		}

		this.entityTree.updateTreeForNewInformation(ovArray, entityValuesArray); 

	}
	
	public List<OVInstance> getOVInstanceList(){
		
		return entityTree.getOVInstanceList(); 
		
	}
	
	/**
	 * Return all ov instances for the ordinary variable. If don't have ov instances
	 * return a empty list. 
	 */
	public List<OVInstance> getOVInstanceListForOrdinaryVariable(OrdinaryVariable ov){
		
		return entityTree.getOVInstanceListForOrdinaryVariable(ov); 
		
	}

	// GET AND SET'S METHODS
	
	public boolean isUsingDefaultDistribution() {
		return useDefaultDistribution;
	}

	public void setUseDefaultDistribution(boolean useDefaultDistribution) {
		this.useDefaultDistribution = useDefaultDistribution;
	}

	public MFrag getMFragOrigin() {
		return mFragOrigin;
	}

	public void setMFragOrigin(MFrag fragOrigin) {
		mFragOrigin = fragOrigin;
	}
	
	
	// ORDINARY VARIABLE EVALUATION STATE LISTS

	
	public SimpleContextNodeFatherSSBNNode getContextNodeFather(OrdinaryVariable ov){
		for(int i = 0; i < ovList.length; i++){
			if(ovList[i].equals(ov)){
				return contextForOVList[i];  
			}
		}
		return null; 
	}
	
	public void setContextNodeForOrdinaryVariable(OrdinaryVariable ov, 
			SimpleContextNodeFatherSSBNNode contextNode){
		
		for(int i = 0; i < ovList.length; i++){
			if(ovList[i].equals(ov)){
				contextForOVList[i] = contextNode;  
			}
		}
		
	}
	
	/**
	 * Monta uma combinação de todos os resultados possíveis para as variáveis 
	 * ordinárias contidas em ovSearchArray, utilizando a arvore de entidades onde 
	 * será utilizada os valores das variáveis já preenchidas. 
	 * 
	 * @param knownOVArray
	 * @param knownEntityArray
	 * @param ovSearchArray
	 * @return Um array com todos os resultados possíveis para a lista ovSearchArray. 
	 *         Os elementos do retorno estão na mesma ordem. 
	 */
	public List<String[]> recoverCombinationsEntitiesPossibles(
			OrdinaryVariable[] knownOVArray,
			LiteralEntityInstance[] knownEntityArray,
			OrdinaryVariable[] ovSearchArray){
		
		return entityTree.recoverCombinationsEntitiesPossibles(knownOVArray, 
				knownEntityArray, ovSearchArray); 
		
	}
	
	// CONTEXT NODE EVALUATION STATE 
	/**
	 * @param contextNode     the context node 
	 * @return                the state of the context node or null otherside (Context Node not found)
	 */
	public ContextNodeEvaluationState getStateEvaluationOfContextNode(ContextNode contextNode){
		int index = 0; 
		for(ContextNode ctx: contextNodeList){
			if(ctx == contextNode){
				return contextNodeEvaluationStateList[index];
			}else{
				index++; 
			}
		}
		return null;
	}
	
	/**
	 * @param contextNode    ContextNode to be setted
	 * @param state          New state of the context node
	 * @return true          if the state are setted, false otherside (Context Node not found). 
	 */
	public boolean setStateEvaluationOfContextNode(ContextNode contextNode, ContextNodeEvaluationState state){
		int index = 0; 
		for(ContextNode ctx: contextNodeList){
			if(ctx == contextNode){
				this.contextNodeEvaluationStateList[index] = state;
				return true; 
			}else{
				index++; 
			}
		}
		return false; 
	}
	
	
	/**
	 * Two MFrag Instances are equals if: 
	 * 
	 * 1. The two are referenced to the same MFrag
	 * 2. The entityTree of the two MFrag's are equal
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o){
		MFragInstance mFrag = (MFragInstance)o;
		
		boolean result = true; 
		
		if(this.mFragOrigin.equals(mFrag.getMFragOrigin())){

			if(!this.entityTree.equals((mFrag.getEntityTree()))){
				result = false; 
			}
		
		}else{
			result = false; 
		}
		
		return result; 
		
	}
	
	@Override
	public String toString(){
		
		String result = ""; 
		
		result += this.mFragOrigin.getName(); 
		
		return result; 
	}
	
	// GET'S AND SET'S METHODS
	
	public List<ContextNode> getContextNodeList(){
		return Arrays.asList(contextNodeList); 
	}
	
	
	public ContextNodeEvaluator getContextNodeAvaliator() {
		return contextNodeAvaliator;
	}

	public void setContextNodeAvaliator(ContextNodeEvaluator contextNodeAvaliator) {
		this.contextNodeAvaliator = contextNodeAvaliator;
	}

	public boolean isUseDefaultDistribution() {
		return useDefaultDistribution;
	}

	public List<SimpleSSBNNode> getSSBNNodeList() {
		return ssbNodeList;
	}

	public void addSSBNNode(SimpleSSBNNode ssbnNode) {
		this.ssbNodeList.add(ssbnNode);
	}
	
	public List<SimpleEdge> getEdgeList() {
		return edgeList;
	}

	public void addEdge(SimpleEdge edge) {
		this.edgeList.add(edge);
	}

	/**
	 * An MFragInstance is marked how evaluated when all nodes are marked how 
	 * evaluated and the context nodes are all evaluated. 
	 */
	public boolean isEvaluated() {
		return evaluated;
	}

	public void setEvaluated(boolean evaluated) {
		this.evaluated = evaluated;
	}

	public OrdinaryVariable[] getOvList() {
		return ovList;
	}

	public void setEntityTree(EntityTree entityTree) {
		this.entityTree = entityTree;
	}
	
	private EntityTree getEntityTree() {
		return entityTree;
	}
	
}
