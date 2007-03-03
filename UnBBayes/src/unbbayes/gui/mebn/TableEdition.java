package unbbayes.gui.mebn;

import java.util.ArrayList;
import java.util.List;

import javax.swing.text.StyledDocument;

import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.entity.Entity;

public class TableEdition {
	
	private List<String> fatherList; 
	private List<String> statesFatherList; 
	private List<String> argumentsList; 
	
	private ToolKitForTableEdition toolKit; 
	
	private DomainResidentNode residentNode; 
	
	public TableEdition(DomainResidentNode _residentNode, ToolKitForTableEdition _toolKit){
		
		residentNode = _residentNode; 
		toolKit = _toolKit;  
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
		char teste; 
		int start;
		String subString; 
		
		i = 0; 
		length = text.length(); 
		
		while(i < length){
			teste = text.charAt(i);
			
			/*idenficadores */
			if(Character.isLetter(teste)){
				start = i; 
				i++;
				
				if(i < length){
					teste = text.charAt(i); 
					while((Character.isLetter(teste)) 
							|| (Character.isDigit(teste))){
						i++;
						if (i >= length) break; 
						teste = text.charAt(i); 
					}
				}
				
				/* verificação */
				subString = text.substring(start, i);
				writeWord(subString, initialPosition + start, doc); 
				
			}
			else{ /* numeros */
				if(Character.isDigit(teste)){
					start = i; 
					i++; 
					if (i < length){
						teste = text.charAt(i); 
						while(Character.isDigit(teste) || (teste == '.')){
							i++;
							if (i >= length) break; 
							teste = text.charAt(i); 
						}
					}
					
					subString = text.substring(start, i);
					writeNumber(subString, initialPosition + start, doc); 
				}
				else{ /* outros */
					writeCharacter(teste, initialPosition + i, doc); 
					i++;
					
				}
			}
		} /* while */
		
	}
	
	private void writeWord(String word, int position, StyledDocument doc){
		
		try{
			
			if(isIfWord(word)){
				doc.insertString(position, word, toolKit.getIfStyle());
				return; 
			}
			
			if(isAnyWord(word)){
				doc.insertString(position, word, toolKit.getAnyStyle());
				return; 
			}			
			
			if(isFunctionWord(word)){
				doc.insertString(position, word, toolKit.getFunctionStyle());
				return; 
			}	
			
			if(isExpansionWord(word)){
				doc.insertString(position, word, toolKit.getDescriptionStyle());
				return; 
			}				
			
			if(isStateNode(word)){
				doc.insertString(position, word, toolKit.getStateNodeStyle());
				return; 
			}	
			
			if(isFather(word)){
				doc.insertString(position, word, toolKit.getFatherStyle());
				return; 
			}				
			
			if(isStateFather(word)){
				doc.insertString(position, word, toolKit.getStateFatherStyle());
				return; 
			}	
			
			if(isArgument(word)){
				doc.insertString(position, word, toolKit.getArgumentStyle());
				return; 
			}				
			
			doc.insertString(position, word, toolKit.getDefaultStype());
			
		}
		catch(Exception e){
			e.printStackTrace(); 
		}
		
	}
	
	private boolean isIfWord(String word){
		if (word.compareTo("if") == 0) return true; 
		if (word.compareTo("else") == 0) return true; 
		if (word.compareTo("then") == 0) return true; 
		if (word.compareTo("or") == 0) return true; 
		if (word.compareTo("and") == 0) return true; 									
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
			doc.insertString(position, number, toolKit.getNumberStyle());
		}
		catch(Exception e){
			e.printStackTrace(); 
		}		
	}
	
	private void writeCharacter(char character, int position, StyledDocument doc){
		try{
			doc.insertString(position, "" + character , toolKit.getDefaultStype());
		}
		catch(Exception e){
			
			e.printStackTrace(); 
		}	
	}	
	
	private void buildAuxiliaryLists(){
		
		fatherList = new ArrayList<String>(); 
		statesFatherList = new ArrayList<String>(); 
		
		for(ResidentNode resident : residentNode.getResidentNodeFatherList()){
			fatherList.add(resident.getName());
			for(Entity state: resident.getPossibleValueList()){
				statesFatherList.add(state.getName()); 
			}
		}
		
		for(InputNode input : residentNode.getInputNodeFatherList()){
			if(input.getInputInstanceOf() instanceof ResidentNode){
				ResidentNode resident = (ResidentNode)input.getInputInstanceOf(); 
				if(resident != null){
					fatherList.add(resident.getName());
					for(Entity state: resident.getPossibleValueList()){
						statesFatherList.add(state.getName()); 
					}
				}
			}
		}
		
		argumentsList = new ArrayList<String>();
		for(OrdinaryVariable ov: residentNode.getOrdinaryVariableList()){
			argumentsList.add(ov.getName()); 
		}
	}
}
