package unbbayes.prs.mebn;

import java.util.ArrayList;
import java.util.List;

public class BuiltInRV {
 
	private String name; 
	private String mnemonic; 
	
	// TODO Verify if it is ever used. It seams that UnBBayes is not ready for this.
	private List<GenerativeInputNode> inputInstanceFromList;
	
	private List<ContextNode> contextInstanceFromList; 
	
	protected int numOperandos; 
	
	public BuiltInRV(String name, String mnemonic){
		this.name = name; 
		this.mnemonic = mnemonic; 
		inputInstanceFromList = new ArrayList<GenerativeInputNode>();
		contextInstanceFromList = new ArrayList<ContextNode>();		
	}
	
	public String getName(){
		return name; 
	}
	
	public void addInputInstance(GenerativeInputNode input){
		inputInstanceFromList.add(input); 
	}
	
	public void addContextInstance(ContextNode context){
		contextInstanceFromList.add(context); 
	}
	
	public List<GenerativeInputNode> getInputInstanceFromList(){
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
 
