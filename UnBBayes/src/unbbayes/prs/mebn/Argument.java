package unbbayes.prs.mebn;

import java.util.ArrayList;
import java.util.List;

import unbbayes.prs.mebn.exception.ArgumentNodeAlreadySetException;
import unbbayes.prs.mebn.exception.ArgumentOVariableAlreadySetException;

public class Argument {
	
	private String name; 
 
	private MultiEntityNode multiEntityNode; 
	
	//TODO this is necessary or the arg number is better
	private List<Argument> argumentOfList;  
	private List<Argument> argumentFromList;
	
	private int argNumber; 

    /* Single argument */
	private OrdinaryVariable oVariable; 

	/* Complex argument */
	private MultiEntityNode argumentTerm; 
	
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
	
	public void setArgumentTerm(MultiEntityNode node) throws ArgumentOVariableAlreadySetException{
		
		if (oVariable != null){
			ArgumentOVariableAlreadySetException e = new ArgumentOVariableAlreadySetException(); 
			throw e; 
		}
		
		argumentTerm = node; 
		
	}
	
	public void setOVariable(OrdinaryVariable oVariable) throws ArgumentNodeAlreadySetException{
		
		if (argumentTerm != null){
			ArgumentNodeAlreadySetException e = new ArgumentNodeAlreadySetException(); 
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
 
