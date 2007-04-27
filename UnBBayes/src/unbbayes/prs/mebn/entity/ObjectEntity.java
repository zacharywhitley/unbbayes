package unbbayes.prs.mebn.entity;

import java.util.ArrayList;
import java.util.List;

import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.entity.exception.TypeDoesNotExistException;
import unbbayes.prs.mebn.entity.exception.TypeException;

public class ObjectEntity extends Entity {
	
	private static List<ObjectEntity> listEntity = new ArrayList<ObjectEntity>();

	private static int entityNum = 1; 
	
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
	private List<ObjectEntity> listObjectEntityInstance;
	
	public ObjectEntity(String name, String type) throws TypeException {
		this.name = name;
		setType(type);
		ObjectEntity.addEntity(this);
		plusEntityNum(); 
	}
	
	private static void addEntity(ObjectEntity entity) {
		ObjectEntity.listEntity.add(entity);
	}
	
	public static void removeEntity(ObjectEntity entity){
		
		try{
		   Type.removeType(entity.getType());
		}
		catch(TypeDoesNotExistException e){
				
		}	
			
		ObjectEntity.listEntity.remove(entity);

	}
	
	public static List<ObjectEntity> getListEntity(){
		return ObjectEntity.listEntity; 
	}
	
	/**
	 * Returns the object entity with the name. Return null if 
	 * the object entity not exists. 
	 * @param name
	 * @return
	 */
	public static ObjectEntity getObjectEntity(String name){
		for(ObjectEntity oe: listEntity){
			if (oe.getName().compareTo(name) == 0){
				return oe; 
			}
		}
		
		return null; 
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
	public ObjectEntity addInstance(String name) throws TypeException {
		ObjectEntity instance = new ObjectEntity(name, type);
		listObjectEntityInstance.add(instance);
		return instance;
	}
	
	public void removeInstance(ObjectEntity instance) {
		listObjectEntityInstance.remove(instance);
	}
	
	public static int getEntityNum() {
		return entityNum;
	}

	public void setEntityNum(int entityNum) {
		this.entityNum = entityNum;
	}
	
	public void plusEntityNum(){
		entityNum++; 
	}
}
