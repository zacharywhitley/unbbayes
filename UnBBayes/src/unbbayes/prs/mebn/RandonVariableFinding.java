package unbbayes.prs.mebn;


import unbbayes.prs.mebn.entity.Entity;
import unbbayes.prs.mebn.entity.ObjectEntityInstance;

/**
 * 
 * 
 * @author Laecio
 *
 */
public class RandonVariableFinding {

	private DomainResidentNode node; 
	
	private ObjectEntityInstance[] arguments;
	
	//boolean, categorical or object entity
	private Entity state;
	
	/**
	 * 
	 * @param node
	 * @param arguments
	 * @param state
	 */
	public RandonVariableFinding(DomainResidentNode node, ObjectEntityInstance[] arguments, Entity state){
		
		this.node = node; 
		this.arguments = arguments; 
		this.state = state; 
		
	}

	public ObjectEntityInstance[] getArguments() {
		return arguments;
	}

	public DomainResidentNode getNode() {
		return node;
	}

	public Entity getState() {
		return state;
	}
	
}
