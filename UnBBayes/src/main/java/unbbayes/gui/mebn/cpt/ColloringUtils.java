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
package unbbayes.gui.mebn.cpt;

import java.util.ArrayList;
import java.util.List;

import javax.swing.text.StyledDocument;

import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.entity.StateLink;

/**
 * Utilities for set the style of a word.  
 * 
 * @author Laecio Santos (laecio@gmail.com)
 */
public class ColloringUtils {
	
	private List<String> fatherList; 
	private List<String> statesFatherList; 
	private List<String> argumentsList; 
	
	private StyleTable styleTable; 
	
	private ResidentNode residentNode; 
	
	public ColloringUtils(ResidentNode _residentNode, StyleTable _styleTable){
		residentNode = _residentNode; 
		styleTable = _styleTable;  
		buildAuxiliaryLists(); 
	}	
	
	/**
	 * Write the text in the pane of edition using the collor defined
	 * for the differents types of tokens
	 * @param text Text that will be write
	 * @param initialPosition Position where the text will be write
	 */
	public void turnTextColor(String text, int initialPosition, StyledDocument doc){
		int i;
		int length; 
		char charRead; 
		int start;
		String subString; 
		
		i = 0; 
		length = text.length(); 
		
		while(i < length){
			charRead = text.charAt(i);
			
			/*identifiers */
			if(Character.isLetter(charRead)){
				start = i; 
				i++;
				
				if(i < length){
					charRead = text.charAt(i); 
					while((Character.isLetter(charRead)) 
							|| (Character.isDigit(charRead))){
						i++;
						if (i >= length) break; 
						charRead = text.charAt(i); 
					}
				}
				
				/* verification */
				subString = text.substring(start, i);
				writeWord(subString, initialPosition + start, doc); 
				
			}
			else{ /* numbers */
				if(Character.isDigit(charRead)){
					start = i; 
					i++; 
					if (i < length){
						charRead = text.charAt(i); 
						while(Character.isDigit(charRead) || (charRead == '.')){
							i++;
							if (i >= length) break; 
							charRead = text.charAt(i); 
						}
					}
					
					subString = text.substring(start, i);
					writeNumber(subString, initialPosition + start, doc); 
				}
				else{ /* others: logic simbols */
					writeCharacter(charRead, initialPosition + i, doc); 
					i++;
					
				}
			}
		} /* while */
		
	}
	
	private void writeWord(String word, int position, StyledDocument doc){
		
		try{
			
			if(isIfWord(word)){
				doc.insertString(position, word, styleTable.getIfStyle());
				return; 
			}
			
			if(isAnyWord(word)){
				doc.insertString(position, word, styleTable.getAnyStyle());
				return; 
			}			
			
			if(isFunctionWord(word)){
				doc.insertString(position, word, styleTable.getFunctionStyle());
				return; 
			}	
			
			if(isExpansionWord(word)){
				doc.insertString(position, word, styleTable.getDescriptionStyle());
				return; 
			}				
			
			if(isStateNode(word)){
				doc.insertString(position, word, styleTable.getStateNodeStyle());
				return; 
			}	
			
			if(isFather(word)){
				doc.insertString(position, word, styleTable.getFatherStyle());
				return; 
			}				
			
			if(isStateFather(word)){
				doc.insertString(position, word, styleTable.getStateFatherStyle());
				return; 
			}	
			
			if(isArgument(word)){
				doc.insertString(position, word, styleTable.getArgumentStyle());
				return; 
			}				
			
			doc.insertString(position, word, styleTable.getDefaultStype());
			
		}
		catch(Exception e){
			e.printStackTrace(); 
		}
		
	}
	
	private boolean isLogicCharacter(Character c){
		if(c.equals('&')) return true; 
		if(c.equals('|')) return true; 
		if(c.equals('~')) return true; 
		return false; 
	}
	
	private boolean isIfWord(String word){
		if (word.compareTo("if") == 0) return true; 
		if (word.compareTo("else") == 0) return true; 
		if (word.compareTo("then") == 0) return true;								
		return false; 
	}
	
	private boolean isAnyWord(String word){
		if (word.compareTo("any") == 0) return true; 
		if (word.compareTo("all") == 0) return true; 
		if (word.compareTo("have") == 0) return true; 									
		return false;		
	}
	
	private boolean isFunctionWord(String word){
		if (word.compareTo("max") == 0) return true; 
		if (word.compareTo("min") == 0) return true; 
		if (word.compareTo("cardinality") == 0) return true;
		if (word.compareTo("number") == 0) return true;		
		return false;		
	}
	
	private boolean isExpansionWord(String word){
		if (word.compareTo("node") == 0) return true; 
		if (word.compareTo("nodestate") == 0) return true; 
		if (word.compareTo("formula") == 0) return true;
		if (word.compareTo("paramSubSet") == 0) return true;
		if (word.compareTo("booleanFunction") == 0) return true;
		if (word.compareTo("op1") == 0) return true;
		if (word.compareTo("op2") == 0) return true;
		if (word.compareTo("op") == 0) return true;		
		return false;		
	}	
	
	private boolean isStateNode(String word){
		return residentNode.hasPossibleValue(word);
	}
	
	private boolean isStateFather(String word){
		for(String state: statesFatherList){
			if(state.compareTo(word) == 0){
				return true; 
			}
		}
		return false; 
	}
	
	private boolean isArgument(String word){
		for(String argument: argumentsList){
			if(argument.compareTo(word) == 0){
				return true; 
			}
		}
		return false; 
	}
	
	private boolean isFather(String word){
		for(String father: fatherList){
			if(father.compareTo(word) == 0){
				return true; 
			}
		}
		return false;  
	}
	
	private void writeNumber(String number, int position, StyledDocument doc){
		try{
			doc.insertString(position, number, styleTable.getNumberStyle());
		}
		catch(Exception e){
			e.printStackTrace(); 
		}		
	}
	
	private void writeCharacter(char character, int position, StyledDocument doc){
		try{
			if(isLogicCharacter(character)){
				doc.insertString(position, "" + character , styleTable.getBooleanStyle());		
			}
			else{
			    doc.insertString(position, "" + character , styleTable.getDefaultStype());
			}
		}
		catch(Exception e){
			e.printStackTrace(); 
		}	
	}	
	
	/**
	 * Update de lists of fathers, states and arguments of the node. This lists
	 * are used for verifies if the color os a word is the default color for
	 * fathers, states or arguments. 
	 */
	private void buildAuxiliaryLists(){
		
		fatherList = new ArrayList<String>(); 
		statesFatherList = new ArrayList<String>();
		argumentsList = new ArrayList<String>();
		
		for(ResidentNode resident : residentNode.getResidentNodeFatherList()){
			fatherList.add(resident.getName());
			ResidentNode domainResidentNode = resident; 
			
			for(StateLink state: domainResidentNode.getPossibleValueLinkList()){
				statesFatherList.add(state.getState().getName()); 
			}
		}
		
		for(InputNode input : residentNode.getInputNodeFatherList()){
			if(input.getInputInstanceOf() instanceof ResidentNode){
				ResidentNode resident = (ResidentNode)input.getInputInstanceOf(); 
				
				if(resident != null){
					fatherList.add(resident.getName());
					
					for(StateLink state: resident.getPossibleValueLinkList()){
						statesFatherList.add(state.getState().getName()); 
					}
				}
				
			}else{
				//Do Nothing
			}
		}
		
		for(OrdinaryVariable ov: residentNode.getOrdinaryVariableList()){
			argumentsList.add(ov.getName()); 
		}
	}
}
