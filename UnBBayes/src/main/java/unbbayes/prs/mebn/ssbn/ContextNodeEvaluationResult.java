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

import unbbayes.prs.mebn.ContextNode;

public class ContextNodeEvaluationResult {

	public static int FALSE        = 0;
	public static int TRUE         = 1; 
	public static int ENTITIES     = 2; 
	public static int ALL_ENTITIES = 3; 
	public static int UNAVALIABLE  = 4; 
	
	private ContextNode contextNode; 
	private int typeResult; 
	private Object objectResult; 

	public ContextNodeEvaluationResult(ContextNode contextNode, int typeResult, Object objectResult){
		this.contextNode = contextNode; 
		this.typeResult = typeResult; 
		this.objectResult = objectResult; 
	}
	
	public ContextNodeEvaluationResult(ContextNode contextNode, int typeResult){
		this.contextNode = contextNode; 
		this.typeResult = typeResult; 
		this.objectResult = null; 
	}

	public ContextNode getContextNode() {
		return contextNode;
	}

	public Object getObjectResult() {
		return objectResult;
	}

	public int getTypeResult() {
		return typeResult;
	}
	
}
