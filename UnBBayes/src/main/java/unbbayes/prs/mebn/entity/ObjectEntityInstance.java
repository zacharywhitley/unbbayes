package unbbayes.prs.mebn.entity;

import unbbayes.prs.mebn.OrdinaryVariable;

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
	
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		
		if((obj != null)&&(obj instanceof ObjectEntityInstance)){
			ObjectEntityInstance node = (ObjectEntityInstance) obj;
		   return (node.name.equals(this.name));
		}
		
		return false; //obj == null && this != null 
		
	}
}
