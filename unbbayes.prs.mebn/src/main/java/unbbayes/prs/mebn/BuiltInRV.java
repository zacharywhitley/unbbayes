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

import java.util.ArrayList;
import java.util.List;

public class BuiltInRV {
 
	private String name; 
	private String mnemonic; 
	
	// TODO Verify if it is ever used. It seams that UnBBayes is not ready for this.
	private List<InputNode> inputInstanceFromList;
	
	private List<ContextNode> contextInstanceFromList; 
	
	protected int numOperandos; 
	
	public BuiltInRV(String name, String mnemonic){
		this.name = name; 
		this.mnemonic = mnemonic; 
		inputInstanceFromList = new ArrayList<InputNode>();
		contextInstanceFromList = new ArrayList<ContextNode>();		
	}
	
	public String getName(){
		return name; 
	}
	
	public void addInputInstance(InputNode input){
		inputInstanceFromList.add(input); 
	}
	
	public void addContextInstance(ContextNode context){
		contextInstanceFromList.add(context); 
	}
	
	public List<InputNode> getInputInstanceFromList(){
		return inputInstanceFromList; 
	}
	
	public List<ContextNode> getContextFromList(){
		return contextInstanceFromList; 
	}
	
	public int getNumOperandos(){
	   return numOperandos; 	
	}
	
	public void setNumOperandos(int num){
		numOperandos = num;
	}

	public String getMnemonic() {
		return mnemonic;
	}

	public void setMnemonic(String mnemonic) {
		this.mnemonic = mnemonic;
	}
	
}
 
