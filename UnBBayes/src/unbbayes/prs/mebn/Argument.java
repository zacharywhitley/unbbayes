package unbbayes.prs.mebn;

import java.util.ArrayList;
import java.util.List;

public class Argument {
	
	private String name; 
 
	private MultiEntityNode multiEntityNode; 
	 
	private List<Argument> argumentOfList; 
	 
	private List<Argument> argumentFromList; 

	private MultiEntityNode argumentTerm; 
	
	private OrdinaryVariable oVariable; 

	/**
	 * Contructs a new Argument.
	 * @param name The name of the Argument
	 * @param multiEntityNode The node where the argument is in.
	 */	
	public Argument(String name, MultiEntityNode multiEntityNode){
		this.name = name; 
		this.multiEntityNode = multiEntityNode; 
		argumentOfList = new ArrayList<Argument>(); 
		argumentFromList = new ArrayList<Argument>(); 
	}
	
	public void setArgumentTerm(MultiEntityNode node) throws Exception{
		
		if (oVariable != null){
			Exception e = new Exception(); 
			throw e; 
		}
		
		argumentTerm = node; 
		
	}
	
	public void setOVariable(OrdinaryVariable oVariable) throws Exception{
		
		if (argumentTerm != null){
			Exception e = new Exception(); 
			throw e; 
		}
		
		this.oVariable = oVariable; 
		
	}	
	
	public String getName(){
		return name; 
	}
	
	public MultiEntityNode getArgumentTerm(){
		
		return argumentTerm; 
		
	}
	
	public OrdinaryVariable getOVariable(){
		
		return oVariable; 
		
	}	
	
	public MultiEntityNode getMultiEntityNode(){
		return multiEntityNode; 
	}
	
	public boolean isSimpleArgRelationship(){
		if ( oVariable != null ){
			return true; 
		}
		else{
			return false; 
		}
	}
	
}
 
