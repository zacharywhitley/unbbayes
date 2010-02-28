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

import unbbayes.prs.mebn.entity.Entity;
import unbbayes.prs.mebn.exception.ArgumentNodeAlreadySetException;
import unbbayes.prs.mebn.exception.ArgumentOVariableAlreadySetException;

/**
 * Argument of a node
 */
public class Argument {
	
	public static final int ORDINARY_VARIABLE = 0; 
	public static final int RESIDENT_NODE = 1; 
	public static final int CATEGORICAL_STATE = 2;
	public static final int SKOLEN = 3; 
	public static final int CONTEXT_NODE = 4; 
	public static final int BOOLEAN_STATE = 5; 
	
	private String name; 
 
	private MultiEntityNode multiEntityNode; 
	
	private int argNumber; 

    /* Single argument */
	private OrdinaryVariable oVariable; 

	/* Complex argument */
	private MultiEntityNode argumentTerm; 
	private Entity entityTerm; 
	
	private int type; 
	
	/**
	 * Constructs a new Argument.
	 * @param name The name of the Argument
	 * @param multiEntityNode The node where the argument is in.
	 */	
	public Argument(String name, MultiEntityNode multiEntityNode){
		
		this.name = name; 
		this.multiEntityNode = multiEntityNode; 
	}
	
	public void setArgumentTerm(MultiEntityNode node) throws ArgumentOVariableAlreadySetException{
		
		if (oVariable != null){
			ArgumentOVariableAlreadySetException e = new ArgumentOVariableAlreadySetException(); 
			throw e; 
		}
		
		argumentTerm = node; 
		
	}
	
	public void setOVariable(OrdinaryVariable oVariable) throws ArgumentNodeAlreadySetException{
		
		if (argumentTerm != null){
			ArgumentNodeAlreadySetException e = new ArgumentNodeAlreadySetException(); 
			throw e; 
		}
		
		this.oVariable = oVariable; 
		
	}	
	
	public String getName(){
		return name; 
	}
	
	public MultiEntityNode getArgumentTerm(){
		
		return argumentTerm; 
		
	}
	
	public OrdinaryVariable getOVariable(){
		
		return oVariable; 
		
	}	
	
	public MultiEntityNode getMultiEntityNode(){
		return multiEntityNode; 
	}
	
	public boolean isSimpleArgRelationship(){
		if ( oVariable != null ){
			return true; 
		}
		else{
			return false; 
		}
	}

	/**
	 * Return the number of the argument in the argument list of the node. 
	 * Be careful because the PR-OWL has the first argument number "1" not "0".
	 * 
	 * @return The number of the argument. (ArgNumber >= 1)
	 */
	public int getArgNumber() {
		return argNumber;
	}

	/**
	 * Set the number of the argument in the argument list of the node. 
	 * Be careful because the PR-OWL has the first argument number "1" and
	 * not "0".
	 * @param argNumber The number of the argument
	 */
	public void setArgNumber(int argNumber) {
		this.argNumber = argNumber;
	}

	public Entity getEntityTerm() {
		return entityTerm;
	}

	public void setEntityTerm(Entity entityTerm) {
		this.entityTerm = entityTerm;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
}
 
