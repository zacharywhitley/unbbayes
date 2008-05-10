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
package unbbayes.io.mebn.exceptions;

public class IOMebnException extends Exception{
	
	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;	

	String object; /* what thing is the causing of the exception */
	
	public IOMebnException (String e){
		super(e); 
	}
	
	public IOMebnException (String e, String extra){
		super(e + ": " + extra); 
		this.object = extra; 
	}
	
	public IOMebnException (Exception e) {
		super(e);
	}
	
	/**
	 * Verify if the exception have an extra description
	 * @return true if exist description extra or false otherwise. 
	 */
	public boolean hasDescriptionExtra(){
		if (object == null){
			return false; 
		}
		else{
			return true; 
		}
	}
	
	public String getDescriptionExtra(){
		return object;
	}
	
	public String setDescriptionExtra(String extra){
		return this.object = extra; 
	}
	
	

}
