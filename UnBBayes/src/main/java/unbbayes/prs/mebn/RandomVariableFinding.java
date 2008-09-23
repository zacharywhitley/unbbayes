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
import unbbayes.prs.mebn.entity.ObjectEntityInstance;

/**
 * Represent a finding for a random variable (a domain resident node). 
 *
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 * @version 1.0 10/26/2007
 */
public class RandomVariableFinding {

	private ResidentNode node;

	private ObjectEntityInstance[] arguments;

	MultiEntityBayesianNetwork mebn;

	//boolean, categorical or object entity
	private Entity state;

	private String name;

	/**
	 *
	 * @param node
	 * @param arguments
	 * @param state
	 */
	public RandomVariableFinding(ResidentNode node, ObjectEntityInstance[] arguments, 
			Entity state, MultiEntityBayesianNetwork mebn){

		this.node = node;
		this.arguments = arguments;
		this.state = state;
		this.mebn = mebn;

		name = "RVF"; //this object don't is saved... 
	}

	public ObjectEntityInstance[] getArguments() {
		return arguments;
	}

	public ResidentNode getNode() {
		return node;
	}

	public Entity getState() {
		return state;
	}

	public String toString(){
		String nameFinding = node.getName();
		nameFinding+="(";
		for(int i = 0; i < arguments.length - 1; i++){
			nameFinding+=arguments[i];
			nameFinding+=",";
		}

		if(arguments.length > 0){
		   nameFinding+= arguments[arguments.length - 1];
		}

		nameFinding+=")";
		nameFinding+="=";
		nameFinding+=state.getName();

		return nameFinding;
	}

}
