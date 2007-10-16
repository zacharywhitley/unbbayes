package unbbayes.prs.mebn.entity;

/**
 * This class link a resident node to a state. This is necessary because the state
 * have special atributes for each node where it is a state. 
 * 
 * @author Laecio Lima dos Santos
 */
public class StateLink{
	
	private static int CATEGORICAL_STATE = 0; 
	private static int BOOLEAN_STATE = 1; 
	private static int ENTITY_STATE = 2; 
	
	private Entity state; 
	private boolean globallyExclusive = false;
	private int type; 
	
	public StateLink(Entity state){
		this.state = state; 
	}

	public boolean isGloballyExclusive() {
		return globallyExclusive;
	}

	public void setGloballyExclusive(boolean globallyExclusive) {
		this.globallyExclusive = globallyExclusive;
	}

	public Entity getState() {
		return state;
	}

	public void setState(Entity state) {
		this.state = state;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
	
	
}