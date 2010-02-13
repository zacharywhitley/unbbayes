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
package unbbayes.prs.mebn.entity;

/**
 * This class link a resident node to a state. This is necessary because the state
 * have special attributes for each node where it is a state: 
 * - The global exclusive attribute 
 * 
 * @author Laecio Lima dos Santos
 */
public class StateLink{
	
    private Entity state; 
	private boolean globallyExclusive = false;
	
	public StateLink(Entity state){
		this.state = state; 
	}

	public boolean isGloballyExclusive() {
		return globallyExclusive;
	}

	public void setGloballyExclusive(boolean globallyExclusive) {
		this.globallyExclusive = globallyExclusive;
	}

	public Entity getState() {
		return state;
	}

	public void setState(Entity state) {
		this.state = state;
	}
	
	public String toString(){
		return state.getName(); 
	}
	
}