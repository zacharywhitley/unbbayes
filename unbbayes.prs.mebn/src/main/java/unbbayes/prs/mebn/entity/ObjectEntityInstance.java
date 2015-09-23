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

import unbbayes.prs.mebn.ssbn.ILiteralEntityInstance;


/**
 * Instance of one Object Entity. 
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 */
public class ObjectEntityInstance extends Entity implements ILiteralEntityInstance{

	private ObjectEntity instanceOf; 
	
	/**
	 * Note: this constructor doesn't add the new instance into the ObjectEntity's list
	 * of instances.
	 * @param name
	 * @param instanceOf
	 */
	public ObjectEntityInstance(String name, ObjectEntity instanceOf){
		super(name, instanceOf.getType()); 
		this.instanceOf = instanceOf; 
	}
	
	public String toString(){
	    return name + " (" + instanceOf.getName() + ")";
	}

	public ObjectEntity getInstanceOf() {
		return instanceOf;
	}
	
	public void setName(String name){
		this.name = name; 
	}
	
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		
		if((obj != null)&&(obj instanceof ObjectEntityInstance)){
			ObjectEntityInstance node = (ObjectEntityInstance) obj;
		   return (node.name.equals(this.name));
		}
		
		return false; //obj == null && this != null 
		
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.mebn.ssbn.ILiteralEntityInstance#getInstanceName()
	 */
	public String getInstanceName() {
		return this.getName();
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.mebn.ssbn.ILiteralEntityInstance#setInstanceName(java.lang.String)
	 */
	public void setInstanceName(String instanceName) {
		this.setName(instanceName);
	}

}
