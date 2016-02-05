package unbbayes.model.umpst.implementation;

import java.util.ArrayList;
import java.util.List;

public class BuiltInRV {
 
	private String name; 
	private String mnemonic; 
	
	private List<EventVariableObjectModel> eventInstanceFromList;
	
	// It is the same thing as necessary condition
	private List<NecessaryConditionVariableModel> contextInstanceFromList; 
	
	protected int numOperandos; 
	
	public BuiltInRV(String name, String mnemonic){
		this.name = name; 
		this.mnemonic = mnemonic; 
		eventInstanceFromList = new ArrayList<EventVariableObjectModel>();
		contextInstanceFromList = new ArrayList<NecessaryConditionVariableModel>();		
	}
	
	public String getName(){
		return name; 
	}
	
	public void addEventInstance(EventVariableObjectModel input){
		eventInstanceFromList.add(input); 
	}	
	
	public void addContextInstance(NecessaryConditionVariableModel context){
		contextInstanceFromList.add(context); 
	}
	
	public List<EventVariableObjectModel> getEventInstanceFromList(){
		return eventInstanceFromList; 
	}	
	
	public List<NecessaryConditionVariableModel> getContextFromList(){
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
 
