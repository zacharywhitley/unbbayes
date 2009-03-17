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
import java.util.Map;

import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.OrdinaryVariable;

/**
 * Represent a MFrag instanciated for a set of entities and encapsule the state
 * of evaluation of the context nodes of this MFrag for this set of entities. 
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 */
public class MFragInstance {

	private MFrag mFragOrigin; 
	
	private boolean useDefaultDistribution; 
	
	private OrdinaryVariable[] ovList; 
	private List<LiteralEntityInstance>[] instanceList; 
	private SimpleContextNodeFatherSSBNNode[] contextList; 
	
	private List<SimpleSSBNNode> nodeList; 
	
	private ContextNodeAvaliator contextNodeAvaliator; 
	
	private Map<ContextNode, ContextNodeEvaluationState> contextNodeEvaluationState; 
	
	
	//Boolean
	// Evaluation OK - Context node evaluated true with all the arguments filled
	// Evaluation fail - Context node evaluated false with all the arguments filled
	// Evaluation search - Context node result in a list of ord. variables that fill the argument
	// Not evaluated yet- Context node not evaluated yet. 
	
	public enum ContextNodeEvaluationState{
		EVALUATION_OK, 
		EVALUATION_FAIL, 
		EVALUATION_SEARCH, 
		NOT_EVALUATED_YET
	}
	
	public MFragInstance(MFrag mFragOrigin){
		this.mFragOrigin = mFragOrigin; 
		
		this.ovList = new OrdinaryVariable[mFragOrigin.getOrdinaryVariableList().size()]; 
		int index = 0; 
		for(OrdinaryVariable ov: mFragOrigin.getOrdinaryVariableList()){
			ovList[index] = ov; 
			instanceList[index] = new ArrayList<LiteralEntityInstance>(); 
			contextList[index] = null; 
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
	
	public List<LiteralEntityInstance> getInstanciatedOV(OrdinaryVariable ov){
		for(int i = 0; i < ovList.length; i++){
			if(ovList[i] == ov){
				return instanceList[i];  
			}
		}
		return null; 
	}
	
	public SimpleContextNodeFatherSSBNNode getContextNodeFather(OrdinaryVariable ov){
		for(int i = 0; i < ovList.length; i++){
			if(ovList[i] == ov){
				return contextList[i];  
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

	public ContextNodeAvaliator getContextNodeAvaliator() {
		return contextNodeAvaliator;
	}

	public void setContextNodeAvaliator(ContextNodeAvaliator contextNodeAvaliator) {
		this.contextNodeAvaliator = contextNodeAvaliator;
	}

	public Map<ContextNode, ContextNodeEvaluationState> getContextNodeEvaluationState() {
		return contextNodeEvaluationState;
	}

	public void setContextNodeEvaluationState(
			Map<ContextNode, ContextNodeEvaluationState> contextNodeEvaluationState) {
		this.contextNodeEvaluationState = contextNodeEvaluationState;
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
	
}
