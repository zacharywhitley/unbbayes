package unbbayes.prs.mebn.entity;

/**
 * A object entity instance ordereable is a entity instance that 
 * have a predecessor. Are instances of Object Entities objects where
 * the isOrdereable property is true; 
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 */
public class ObjectEntityInstanceOrdereable extends ObjectEntityInstance{

	private ObjectEntityInstanceOrdereable prev; 
	private ObjectEntityInstanceOrdereable proc; 
	
	public ObjectEntityInstanceOrdereable(String name, ObjectEntity instanceOf){
		super(name, instanceOf); 
	}

	public ObjectEntityInstanceOrdereable getPrev() {
		return prev;
	}

	public void setPrev(ObjectEntityInstanceOrdereable prev) {
		this.prev = prev;
	}

	public ObjectEntityInstanceOrdereable getProc() {
		return proc;
	}

	public void setProc(ObjectEntityInstanceOrdereable proc) {
		this.proc = proc;
	}
	
	public boolean equals(Object obj){ 
		if(obj instanceof ObjectEntityInstanceOrdereable){
			return this.getName().equals(((ObjectEntityInstanceOrdereable)obj).getName()); 
		}else{
			return false; 
		}
	}
	
}
