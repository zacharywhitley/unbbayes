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

import unbbayes.prs.Node;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;

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

	public enum ContextNodeEvaluationState{
		EVALUATION_OK, // Evaluation OK - Context node evaluated true with all the arguments filled
		EVALUATION_FAIL, 	// Evaluation fail - Context node evaluated false with all the arguments filled
		EVALUATION_SEARCH, 	// Evaluation search - Context node result in a list of ord. variables that fill the argument
		NOT_EVALUATED_YET	// Not evaluated yet- Context node not evaluated yet. 
	}
	
	private MFrag mFragOrigin; 
	
	private boolean useDefaultDistribution; 
	
	//Teorem: For the same set of ordinary variables instance values, 
	//        the evaluation of the context node should be the same. 
	private OrdinaryVariable[] ovList; 
	private List<LiteralEntityInstance>[] instanceList; 
	//if is a context related with a ordinary variable, then the unreferenced macanism 
	//was used to recover the ordinary variable fault. Else, the instanceList 
	//is the normal findings. 
	private SimpleContextNodeFatherSSBNNode[] contextForOVList; 
	
	private List<SimpleSSBNNode> nodeList; 
	
	private ContextNodeAvaliator contextNodeAvaliator; 
	
	private ContextNode[] contextNodeList; 
	private ContextNodeEvaluationState[] contextNodeEvaluationStateList; 
	
	private boolean evaluated = false; 
	
	public MFragInstance(MFrag mFragOrigin){
		
		int index = 0; 
		int size = 0; 
		
		this.mFragOrigin = mFragOrigin; 
		
		//Create the list of states of evaluation of the ordinary variables.
		size = mFragOrigin.getOrdinaryVariableList().size(); index = 0; 
		this.ovList = new OrdinaryVariable[size];  
		for(OrdinaryVariable ov: mFragOrigin.getOrdinaryVariableList()){
			ovList[index] = ov; 
			instanceList[index] = new ArrayList<LiteralEntityInstance>(); 
			contextForOVList[index] = null; 
			index++; 
		}
		
		//Create the list of states of evaluation of the context nodes. 
		size = mFragOrigin.getContextNodeList().size(); index = 0;  
		contextNodeList = new ContextNode[size]; 
		contextNodeEvaluationStateList = new ContextNodeEvaluationState[size]; 
		for(ContextNode context: mFragOrigin.getContextNodeList()){
			contextNodeList[index] = context; 
			contextNodeEvaluationStateList[index] = ContextNodeEvaluationState.NOT_EVALUATED_YET; 
			index++; 
		}
		
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

	/**
	 * Return true if the operation is OK or false otherside. 
	 */
	public boolean addInstanciatedOV(OrdinaryVariable ov, LiteralEntityInstance lei){
		int index = -1; 
		
		for(int i = 0; i < ovList.length; i++){
			if(ovList[i] == ov){
				index = i; 
				break; 
			}
		}
		
		if(index < 0){
			return false; 
		}else{
			instanceList[index].add(lei);
			return true; 
		}
		
	}
	
	public List<LiteralEntityInstance> getInstanciatedOVValue(OrdinaryVariable ov){
		for(int i = 0; i < ovList.length; i++){
			if(ovList[i] == ov){
				return instanceList[i];  
			}
		}
		return null; 
	}
	
	/**
	 * @return All the ordinary variable that don't are instanciated yet. 
	 */
	public List<OrdinaryVariable> getListNotInstanciatedOV(){
		List<OrdinaryVariable> ovNotInstanciatedList = new ArrayList<OrdinaryVariable>(); 
		for(int i = 0; i < ovList.length; i++){
			if(instanceList[i].size() <= 0){
				ovNotInstanciatedList.add(ovList[i]);  
			}
		}
		return ovNotInstanciatedList; 
	}
	
	public SimpleContextNodeFatherSSBNNode getContextNodeFather(OrdinaryVariable ov){
		for(int i = 0; i < ovList.length; i++){
			if(ovList[i] == ov){
				return contextForOVList[i];  
			}
		}
		return null; 
	}
	
	/**
	 * @return All OV of the MFrag don't instanciated yet. 
	 */
	public List<OrdinaryVariable> getOVFaultList(){

		List<OrdinaryVariable> ovFaultList = new ArrayList<OrdinaryVariable>(); 
		
		for(int i = 0; i < instanceList.length; i++){
			if(instanceList[i].size() == 0){
				ovFaultList.add(ovList[i]); 
			}
		}
		
		return ovFaultList; 
	}

	public List<OVInstance> getOVInstanceList(){
		
		return null; 
		
	}
	
	
	/**
	 * 
	 * @param node               Input or Resident Node parent
	 * @param ovInstanceList     Arguments of the parent node
	 * @return
	 */
	public List<SimpleSSBNNode> getSSBNNodeForNode(Node node, List<OVInstance> ovInstanceList){
		
		List<SimpleSSBNNode> listSSBNNode = new ArrayList<SimpleSSBNNode>(); 
		
		ResidentNode residentNode; 
		
		//Bad form to do this. 
		if(node instanceof InputNode){
			residentNode = ((InputNode)node).getResidentNodePointer().getResidentNode();  
		}else{
			residentNode = (ResidentNode) node; 
		}
		
		for(SimpleSSBNNode ssbnNode: listSSBNNode){
			
			if(ssbnNode.getResidentNode().equals(residentNode)){
				if(isOVInstanceListEquivalent(ovInstanceList, ssbnNode.getArgumentList())){
					listSSBNNode.add(ssbnNode); 
				}
			}
			
		}
		
		return listSSBNNode; 
		
	}
	
	private boolean isOVInstanceListEquivalent(List<OVInstance> ovInstanceList1, List<OVInstance> ovInstanceList2){
		//TODO implement this method
		return true; 
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
	
	public List<ContextNode> getContextNodeList(){
		return Arrays.asList(contextNodeList); 
	}
	
	
	public ContextNodeAvaliator getContextNodeAvaliator() {
		return contextNodeAvaliator;
	}

	public void setContextNodeAvaliator(ContextNodeAvaliator contextNodeAvaliator) {
		this.contextNodeAvaliator = contextNodeAvaliator;
	}

	public boolean isUseDefaultDistribution() {
		return useDefaultDistribution;
	}

	public List<SimpleSSBNNode> getNodeList() {
		return nodeList;
	}

	public void setNodeList(List<SimpleSSBNNode> nodeList) {
		this.nodeList = nodeList;
	}

	/*
	 * When two mFragInstances are equals? 
	 * 
	 * 1. The two are referenced to the same MFrag
	 * 2. For each ordinary variable OVia and OVib
	 *   2.1. If OVia == !c then OVib == !c 
	 *   2.2. If   OVia == uncertainty by context node ctx1, 
	 *        then OVib == uncertainty by context node ctx1
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object mFragInstance){
		MFragInstance node = (MFragInstance)mFragInstance;
		
		return true; 
	}

	public boolean isEvaluated() {
		return evaluated;
	}

	public void setEvaluated(boolean evaluated) {
		this.evaluated = evaluated;
	}
	
}
