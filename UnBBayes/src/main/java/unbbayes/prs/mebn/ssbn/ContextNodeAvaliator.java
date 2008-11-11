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

import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.ssbn.exception.InvalidContextNodeFormulaException;
import unbbayes.prs.mebn.ssbn.exception.OVInstanceFaultException;
import unbbayes.util.Debug;

/**
 * Class that contains methods for evaluate the context nodes of a MFrag. 
 * 
 * @author Laecio Santos (laecio@gmail.com)
 */
public class ContextNodeAvaliator {

	private KnowledgeBase kb; 
	
	public ContextNodeAvaliator(KnowledgeBase kb){
		
		this.kb = kb; 
		
	}

	/**
	 * Evaluate a context node. 
	 * 
	 * @param node
	 * @param ovInstances
	 * @return the evaluation of context node
	 * @throws OVInstanceFaultException One or more ordinary variable of the context 
	 *                                  node don't have one associated OVInstance
	 */
	public boolean evaluateContextNode(ContextNode node, List<OVInstance> ovInstances) 
	         throws OVInstanceFaultException{
		
		boolean isDebug = Debug.isDebugMode(); 
		Debug.setDebug(false); 
		
		List<OrdinaryVariable> ovFaultList = node.getOVFaultForOVInstanceSet(ovInstances); 

		Debug.setDebug(isDebug); 
		if(!ovFaultList.isEmpty()){
			throw new OVInstanceFaultException(ovFaultList); 
		}else{
            boolean result = kb.evaluateContextNodeFormula(node, ovInstances);
			Debug.setDebug(isDebug); 
			return result; 
		}
		
	}
	
	/**
	 * Evaluate a search context node. A search context node is a node that return
	 * instances of the Knowledge Base that satisfies a restriction. 
	 * 
	 * Ex.: z = StarshipZone(st). 
	 * -> return all the z's. 
	 * 
	 * @param context
	 * @param ovInstances
	 * @return
	 * @throws InvalidContextNodeFormulaException
	 * @throws OVInstanceFaultException 
	 */
	public List<String> evalutateSearchContextNode(ContextNode context, List<OVInstance> ovInstances) 
	        throws InvalidContextNodeFormulaException, OVInstanceFaultException{
		
			List<String> entitiesResult = kb.evaluateSearchContextNodeFormula(context, ovInstances); 
			return entitiesResult;
	}

	/**
	 * Return all the entities of one type. 
	 * @param type
	 * @return
	 */
	public List<String> getEntityByType(String type){
		return kb.getEntityByType(type); 
	}
	
}
