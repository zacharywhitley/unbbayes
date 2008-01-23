package unbbayes.prs.mebn;

import unbbayes.prs.mebn.entity.Entity;
import unbbayes.prs.mebn.exception.ArgumentNodeAlreadySetException;
import unbbayes.prs.mebn.exception.ArgumentOVariableAlreadySetException;

/**
 * Used only in the process of loading and saving PR-OWL. 
 */
public class Argument {
	
	public static final int ORDINARY_VARIABLE = 0; 
	public static final int RESIDENT_NODE = 1; 
	public static final int CATEGORICAL_STATE = 2;
	public static final int SKOLEN = 3; 
	public static final int CONTEXT_NODE = 4; 
	public static final int BOOLEAN_STATE = 5; 
	
	private String name; 
 
	private MultiEntityNode multiEntityNode; 
	
	private int argNumber; 

    /* Single argument */
	private OrdinaryVariable oVariable; 

	/* Complex argument */
	private MultiEntityNode argumentTerm; 
	private Entity entityTerm; 
	
	private int type; 
	
	/**
	 * Constructs a new Argument.
	 * @param name The name of the Argument
	 * @param multiEntityNode The node where the argument is in.
	 */	
	public Argument(String name, MultiEntityNode multiEntityNode){
		
		this.name = name; 
		this.multiEntityNode = multiEntityNode; 
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

	/**
	 * Return the number of the argument in the argument list of the node. 
	 * Be careful because the PR-OWL has the first argument number "1" not "0".
	 * 
	 * @return The number of the argument. (ArgNumber >= 1)
	 */
	public int getArgNumber() {
		return argNumber;
	}

	/**
	 * Set the number of the argument in the argument list of the node. 
	 * Be careful because the PR-OWL has the first argument number "1" and
	 * not "0".
	 * @param argNumber The number of the argument
	 */
	public void setArgNumber(int argNumber) {
		this.argNumber = argNumber;
	}

	public Entity getEntityTerm() {
		return entityTerm;
	}

	public void setEntityTerm(Entity entityTerm) {
		this.entityTerm = entityTerm;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
}
 