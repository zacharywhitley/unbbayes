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

import java.util.HashMap;
import java.util.Map;

import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.OrdinaryVariable;

/**
 * Represent a MFrag instanciate for a set of entities and encapsule the state
 * of evaluation of the context nodes of this MFrag for this set of entities. 
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 */
public class MFragInstance {

	private MFrag mFragOrigin; 
	
	private boolean useDefaultDistribution; 

	//Ordinary variables
	
	private Map<OrdinaryVariable, LiteralEntityInstance> ordinaryVariableEvaluationState;
	
	private Map<ContextNode, ContextNodeEvaluationState> contextNodeEvaluationState; 
	
	
	public enum ContextNodeEvaluationState{
		EVALUATION_OK, 
		EVALUATION_FAIL, 
		EVALUATION_SEARCH, 
		NOT_EVALUATED_YET
	}
	
	public MFragInstance(MFrag mFragOrigin){
		this.mFragOrigin = mFragOrigin; 
		
		contextNodeEvaluationState = new HashMap<ContextNode, ContextNodeEvaluationState>(); 
		for(ContextNode contextNode: mFragOrigin.getContextNodeList()){
			contextNodeEvaluationState.put(contextNode, ContextNodeEvaluationState.NOT_EVALUATED_YET); 
		}
		
		ordinaryVariableEvaluationState = new HashMap<OrdinaryVariable, LiteralEntityInstance>(); 
		for(OrdinaryVariable ordinaryVariable: mFragOrigin.getOrdinaryVariableList()){
			ordinaryVariableEvaluationState.put(ordinaryVariable, null); 
		}
	
	}
	
	public void setContextNodeEvaluationState(ContextNode context, ContextNodeEvaluationState state){
		contextNodeEvaluationState.put(context, state); 
	}

	public void setOrdinaryVariableEvaluationState(OrdinaryVariable ov, LiteralEntityInstance instance){
		ordinaryVariableEvaluationState.put(ov, instance); 
	}
	
	public boolean isUsingDefaultDistribution() {
		return useDefaultDistribution;
	}

	public void setUseDefaultDistribution(boolean useDefaultDistribution) {
		this.useDefaultDistribution = useDefaultDistribution;
	}
	
}
