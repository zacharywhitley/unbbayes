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

import java.util.List;
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

	private Map<OrdinaryVariable, List<LiteralEntityInstance>> instanciatedArguments; 
	
	private List<SSBNNode> nodeList; 
	
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
		
	}
	
	// GET AND SET'S METHODS
	
	public boolean isUsingDefaultDistribution() {
		return useDefaultDistribution;
	}

	public void setUseDefaultDistribution(boolean useDefaultDistribution) {
		this.useDefaultDistribution = useDefaultDistribution;
	}
	
}
