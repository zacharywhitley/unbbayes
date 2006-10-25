package unbbayes.prs.mebn;

import java.util.ArrayList;
import java.util.List;

public class BuiltInRV {
 
	private String name; 
	
	private List<GenerativeInputNode> inputInstanceFromList;
	
	private List<ContextNode> contextInstanceFromList; 
	
	public BuiltInRV(String name){
		this.name = name; 
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
}
 
