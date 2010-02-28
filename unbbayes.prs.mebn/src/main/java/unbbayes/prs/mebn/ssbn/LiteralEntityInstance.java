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

import unbbayes.prs.mebn.entity.Type;

/**
 * @author Shou Matsumoto
 *
 */
public class LiteralEntityInstance {
	private String instanceName = null;
	private Type type = null;
	
	private LiteralEntityInstance () {
		super();
	}
	
	public static LiteralEntityInstance getInstance ( String instanceName , Type type) {
		LiteralEntityInstance ei = new LiteralEntityInstance();
		ei.setInstanceName(instanceName);
		ei.setType(type);
		return ei;
	}

	/**
	 * @return the instanceName
	 */
	public String getInstanceName() {
		return instanceName;
	}

	/**
	 * @param instanceName the instanceName to set
	 */
	public void setInstanceName(String instanceName) {
		this.instanceName = instanceName;
	}

	/**
	 * @return the type
	 */
	public Type getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(Type type) {
		this.type = type;
	}
	
	public boolean equals(Object obj) {
		
		if (obj == this) {
			return true;
		}
		
		if((obj != null)&&(obj instanceof LiteralEntityInstance)){
			LiteralEntityInstance entityInstance = (LiteralEntityInstance) obj;
		   return ((entityInstance.getInstanceName().equalsIgnoreCase(this.getInstanceName())) && //The knowledge information is upper case
		            (entityInstance.getType().equals(this.getType())));
		}else{
			return false; //obj == null && this != null 
		}
		
	}
	
	public String toString(){
		return "LitEntInst:" + instanceName + "[" + type + "]";
	}
	
	
}
