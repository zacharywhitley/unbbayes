package unbbayes.prs.mebn.entity;

/**
 * Instance of the Object Entity Class. 
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 */
public class ObjectEntityInstance extends Entity{

	private ObjectEntity instanceOf; 
	
	public ObjectEntityInstance(String name, ObjectEntity instanceOf){
		super(name, instanceOf.getType()); 
		this.instanceOf = instanceOf; 
	}
	
	public String toString(){
	    return name + " (" + instanceOf.getName() + ")";
	}

	public ObjectEntity getInstanceOf() {
		return instanceOf;
	}
	
	public void setName(String name){
		this.name = name; 
	}
}
