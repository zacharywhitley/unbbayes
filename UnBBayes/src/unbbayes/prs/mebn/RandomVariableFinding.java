package unbbayes.prs.mebn;


import unbbayes.prs.mebn.entity.Entity;
import unbbayes.prs.mebn.entity.ObjectEntityInstance;

/**
 * Represent a finding for a random variable (a domain resident node). 
 *
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 * @version 1.0 10/26/2007
 */
public class RandomVariableFinding {

	private DomainResidentNode node;

	private ObjectEntityInstance[] arguments;

	MultiEntityBayesianNetwork mebn;

	//boolean, categorical or object entity
	private Entity state;

	private String name;

	/**
	 *
	 * @param node
	 * @param arguments
	 * @param state
	 */
	public RandomVariableFinding(DomainResidentNode node, ObjectEntityInstance[] arguments, Entity state, MultiEntityBayesianNetwork mebn){

		this.node = node;
		this.arguments = arguments;
		this.state = state;
		this.mebn = mebn;

		//TODO name...
		name = "RVF"; //this object don't is saved... 
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

	public String toString(){
		String nameFinding = node.getName();
		nameFinding+="(";
		for(int i = 0; i < arguments.length - 1; i++){
			nameFinding+=arguments[i];
			nameFinding+=",";
		}

		if(arguments.length > 0){
		   nameFinding+= arguments[arguments.length - 1];
		}

		nameFinding+=")";
		nameFinding+="=";
		nameFinding+=state.getName();

		return nameFinding;
	}

}
