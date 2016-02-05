package unbbayes.io.umpst.implementation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import com.ibm.icu.lang.UCharacter.SentenceBreak;

import unbbayes.model.umpst.implementation.CauseVariableModel;
import unbbayes.model.umpst.project.UMPSTProject;

public class FileLoadRuleDefinition {
	
//	private String line;
	private UMPSTProject umpstProject;
	private List<String> fileObj;
	private HashMap<String, CauseVariableModel> mapCauseVariable;

	public UMPSTProject loadTestFile(File file, UMPSTProject umpstProject)
			throws IOException {
		
		this.setUmpstProject(umpstProject);
		
		FileInputStream fis = new FileInputStream(file);
		Scanner input = new Scanner(fis);
		ArrayList<ArrayList<String>> ruleList = new ArrayList<ArrayList<String>>();
		int access = 0;
		
		while (input.hasNext()) {
//			String token = input.next();
			
			String line = input.nextLine();
			if(line.contains("&R")) {				
//				char[] sentenceList = new char[line.length()];
//				int pointer = 0;				
//				for (int i = 0; i < line.length(); i++) {
//					char symbol = line.charAt(i);
//					
//					if ((line.charAt(i) == '(') && (line.charAt(i+1) == '{')) {
//						access = 1; // begin of sentences
//					} else if((access == 1) && (symbol == ')')) {
//						access = 2; // has possibility to end sentences
//					} else if((access == 2) && (symbol == '}')) {
//						access = 3; // end of sentences
//					}
//										
//					// Start read sentences
//					if((access == 1) || (access == 2)) {
//						sentenceList[pointer] = symbol;
//					}
//				}
				
				access = 1; // Access the line
			} else if (access == 1) {
				access = loadOrdinaryVariable(line, access);
			} else if (access == 2) {
				access= loadNecessaryCondition(line, access);
			}	
		}		
		return null;
	}
	
	public int loadNecessaryCondition(String line, int access) {
			String variable = null;
			
			// Necessary Condition
			if (line.contains("{")) {
				
				int end = line.indexOf("(");
				variable = line.substring(1, end);					
				if(isOperation(variable)) {
					// TODO keep operation
				} else if (variable.equals("")){
					
				} else {
					// TODO keep relationship
				}
				
			} else if (line.contains("}")) {
				
			} else {
				
			}
		return 3;
	}

	public int loadOrdinaryVariable(String line, int access) {
		
			// begin of sentence
			if ((line.contains("{")) && (line.contains("isA"))) {
				int begin = line.lastIndexOf("isA");
				int end = line.indexOf(",");
				String variable = line.substring(begin+4, end-1);
				
				begin = line.indexOf(",");
				end = line.lastIndexOf(")");						
				String type = line.substring(begin+1, end);
				access = 1;
				//System.out.println(type);			
			}
			
			// end of sentence
			else if((line.contains("}")) && (line.contains("isA"))) {
				int begin = line.lastIndexOf("isA");
				int end = line.indexOf(",");
				String variable = line.substring(begin+4, end-1);
				
				begin = line.indexOf(",");
				end = line.lastIndexOf(")");
				String type = line.substring(begin+1, end);
				
				//System.out.println(type);
				access = 2;
			} else if(line.contains("isA")) {
				int begin = line.lastIndexOf("isA");
				int end = line.indexOf(",");
				String variable = line.substring(begin+4, end-1);
				
				begin = line.indexOf(",");
				end = line.lastIndexOf(")");
				String type = line.substring(begin+1, end);
				access = 1;
				//System.out.println(type);
			}
		return access;
	}
	
	public boolean isOperation(String opr) {
		Boolean flag = false;
		
		if (opr.equals("¬")) {
			flag = true;
		} else if (opr.equals("=")) {
			flag = true;
		} else if (opr.equals("^")) {
			flag = true;
		} else if (opr.equals("v")) {
			flag = true;
		} else {
			flag = false;
		}
		
		return flag;
	}
	
	/**
	 * @return the mapCauseVariable
	 */
	public HashMap<String, CauseVariableModel> getMapCauseVariable() {
		return mapCauseVariable;
	}

	/**
	 * @param mapCauseVariable the mapCauseVariable to set
	 */
	public void setMapCauseVariable(HashMap<String, CauseVariableModel> mapCauseVariable) {
		this.mapCauseVariable = mapCauseVariable;
	}

	/**
	 * @return the umpstProject
	 */
	public UMPSTProject getUmpstProject() {
		return umpstProject;
	}

	/**
	 * @param umpstProject the umpstProject to set
	 */
	public void setUmpstProject(UMPSTProject umpstProject) {
		this.umpstProject = umpstProject;
	}

}
