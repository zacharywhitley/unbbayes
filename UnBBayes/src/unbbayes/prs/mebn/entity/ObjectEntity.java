package unbbayes.prs.mebn.entity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.entity.exception.ObjectEntityHasInstancesException;
import unbbayes.prs.mebn.entity.exception.TypeAlreadyExistsException;
import unbbayes.prs.mebn.entity.exception.TypeDoesNotExistException;
import unbbayes.prs.mebn.entity.exception.TypeException;

/**
 * 
 * 
 *
 */
public class ObjectEntity extends Entity {

	private TypeContainer typeContainer = null; 
	
	/**
	 * This object property (subsOVar) assigns MetaEntity individuals in order 
	 * to define the type of the substituters for each MFrag ordinary variable. 
	 * Its inverse property is the functional isSubsBy.
	 */
	private List<OrdinaryVariable> listSubstitute;
	
	/**
	 * This is a list of instances of a given object entity. For instance, ST0 
	 * and ST1 can be instances of the object entity Starship that has type 
	 * Starship_Label.
	 */
	private Set<ObjectEntityInstance> listObjectEntityInstance;
	
	private boolean isOrdereable; 
	
	protected ObjectEntity(String name, TypeContainer container) throws TypeException {
		
		super(name, container.createType(name + "_label")); 
		typeContainer = container; 
		
		listObjectEntityInstance = new HashSet<ObjectEntityInstance>(); 
	}
	
	/**
	 * Create a entity instance of a object entity. 
	 * @param name
	 * @throws TypeException
	 */
	private ObjectEntity(String name, Type _type) throws TypeException {
		
		super(name, _type);   
		_type.addUserObject(this); 
		typeContainer = null; 
		
	}
	
	public void addSubstitute(OrdinaryVariable oVar) {
		listSubstitute.add(oVar);
	}
	
	public void removeSubstitute(OrdinaryVariable oVar) {
		listSubstitute.remove(oVar);
	}
	
	/**
	 * Add an instance of the given Object Entity with the same type and returns 
	 * the instance (another object entity) created. For instance, we have the 
	 * Object Entity Starship with type Starship_Label and we want to create an 
	 * instance ST0, so we call this method passing "ST0" as the param and  
	 * the created instance will be created with the name given and the type 
	 * Starship_Label. This instance will be added to the list of Starship's 
	 * instances and it will be returned so you can do whatever you need with 
	 * this just created entity (that here it is dealt as an Starship's  
	 * instance). 
	 * @param name The Starship's instance to be created.
	 * @return The Starship's instance created (an object entity).
	 * @throws TypeException Thrown if there is any problem concerning the type 
	 * setting.
	 */
	public ObjectEntityInstance addInstance(String name) throws TypeException {
		
		ObjectEntityInstance instance = null; 
		
		if(!isOrdereable){
		    instance = new ObjectEntityInstance(name, this);
		}else{
			instance = new ObjectEntityInstanceOrdereable(name, this); 
		}
		
		listObjectEntityInstance.add(instance);
		
		return instance;
		
	}
	
	public Set<ObjectEntityInstance> getInstanceList(){
		return this.listObjectEntityInstance; 
	}
	
	public void removeInstance(ObjectEntityInstance instance) {
		listObjectEntityInstance.remove(instance);
	}
	
	public void removeAllInstances(){
		listObjectEntityInstance.clear(); 
	}
	
	protected void delete() throws TypeDoesNotExistException{
		
		getType().removeUserObject(this); 
		typeContainer.removeType(getType());
		
	}	
	
	/**
	 * Sets the entity's type.
	 * 
	 * @param type
	 *            The entity's new type.
	 * @throws TypeDoesNotExistException
	 *             Thrown if the type to be set does not exist.
	 */
	public void setType(Type _type) throws TypeException {
		
		if (typeContainer.hasType(_type)){
			if(type != null){
				type.removeUserObject(this); 
			}
			this.type = _type;
		}
		else{
			throw new TypeDoesNotExistException();
	
		}
		
	}

	/**
	 * Set the entity's name. 
	 * @param name The entity's name.
	 */
	public void setName(String name) throws TypeAlreadyExistsException{
		
		if(type != null){
			type.renameType(name + "_label"); 
		}
		else{
			type =  typeContainer.createType(name + "_label"); 
		}
		
		this.name = name;
		
	}
	
	public String toString(){
		String ret = name; 
		if(isOrdereable){
			ret+=" [Ord]";
		}
		return ret; 		
	}
	
	public boolean equals(Object obj){ 
		if(obj instanceof ObjectEntity){
			return this.getName().equals(((ObjectEntity)obj).getName()); 
		}else{
			return false; 
		}
	}



	public boolean isOrdereable() {
		return isOrdereable;
	}
	
	/**
	 * Set the isOrdereable property. This property only shoud be setted if don't 
	 * have any instances of this class.
	 *  
	 * @param isOrdereable
	 * @throws ObjectEntityHasInstancesException 
	 */
	public void setOrdereable(boolean isOrdereable) throws ObjectEntityHasInstancesException{
		
		if(!listObjectEntityInstance.isEmpty()){
			throw new ObjectEntityHasInstancesException(); 
		}
		
		this.isOrdereable = isOrdereable;
	
	}
	
}
