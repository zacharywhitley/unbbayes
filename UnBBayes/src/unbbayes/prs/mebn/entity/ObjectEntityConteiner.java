package unbbayes.prs.mebn.entity;

import java.util.ArrayList;
import java.util.List;

import unbbayes.prs.mebn.entity.exception.EntityInstanceAlreadyExistsException;
import unbbayes.prs.mebn.entity.exception.TypeException;

/**
 * Contains the Object entities of a MEBN. 
 * 
 * Note: Este conteiner não pode ser um singleton poque o usuario pode editar
 * duas ou mais MTheories ao mesmo tempo, acarretando conjuntos de entidades
 * diferentes. 
 * 
 * @author Laecio Lima dos Santos
 * @version 1.0 06/03/07
 *
 */
public class ObjectEntityConteiner {

	private List<ObjectEntity> listEntity;
	
	private List<ObjectEntityInstance> listEntityInstances; 

	private TypeContainer typeContainer; 
	
	private int entityNum; 
	
	public ObjectEntityConteiner(TypeContainer _typeConteiner){
		
		typeContainer = _typeConteiner; 
		entityNum = 1;
		listEntity = new ArrayList<ObjectEntity>(); 
		listEntityInstances = new ArrayList<ObjectEntityInstance>(); 
		
	}
	
	
	/**
	 * Create a new Object Entity with the name specified. 
	 * Create a type for the Object Entity and this is added to the list of Type (see <Type>). 
	 * @param name The name of the entity
	 * @return the new ObjectEntity 
	 * @throws TypeException Some error when try to create a new type
	 */
	public ObjectEntity createObjectEntity(String name) throws TypeException{
		
		ObjectEntity objEntity = new ObjectEntity(name, typeContainer); 
		objEntity.getType().addUserObject(objEntity); 
		
		addEntity(objEntity);
	
		plusEntityNum(); 
		
		return objEntity; 
		
	}
	
	private void addEntity(ObjectEntity entity) {
		listEntity.add(entity);
	}
	
	public void removeEntity(ObjectEntity entity) throws Exception{

		entity.delete(); 	
		listEntity.remove(entity);

	}
	
	public List<ObjectEntity> getListEntity(){
		return listEntity; 
	}
	
	/**
	 * Returns the object entity with the name. Return null if 
	 * the object entity not exists. 
	 * @param name
	 * @return
	 */
	public ObjectEntity getObjectEntityByName(String name){
		for(ObjectEntity oe: listEntity){
			if (oe.getName().compareTo(name) == 0){
				return oe; 
			}
		}
		
		return null; 
	}
	
	//Para gerar nomes automaticos. 
	
	public int getEntityNum() {
		return entityNum;
	}

	public void setEntityNum(int _entityNum) {
		entityNum = _entityNum;
	}
	
	public void plusEntityNum(){
		entityNum++; 
	}


	public List<ObjectEntityInstance> getListEntityInstances() {
		return listEntityInstances;
	}
	
	public void addEntityInstance(ObjectEntityInstance entityInstance) throws EntityInstanceAlreadyExistsException{
		if(!listEntityInstances.contains(entityInstance)){
		   this.listEntityInstances.add(entityInstance); 
		}
		else{
			throw new EntityInstanceAlreadyExistsException(); 
		}
	}
	
	public void removeEntityInstance(ObjectEntityInstance entityInstance){
		this.listEntityInstances.remove(entityInstance); 
	}
	
	public ObjectEntityInstance getEntityInstanceByName(String name){
		for(ObjectEntityInstance entity: listEntityInstances){
			if(entity.getName().equals(name)){
				return entity; 
			}
		}
		return null; 
	}
}
