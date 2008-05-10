/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package unbbayes.prs.mebn.entity;

import java.util.ArrayList;
import java.util.List;

import unbbayes.prs.mebn.entity.exception.EntityInstanceAlreadyExistsException;
import unbbayes.prs.mebn.entity.exception.TypeException;

/**
 * Contains the Object entities of a MEBN. 
 * 
 * Note: this container should not be a singleton, since an user might edit
 * two MTheories simultaneously; naturally, using different sets of entities.
 * 
 * @author Laecio Lima dos Santos
 * @author Shou Matsumoto
 * @version 1.1 02/25/2008
 * 
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
	
	public ObjectEntity getObjectEntityByType(Type type){
		for(ObjectEntity oe: listEntity){
			if (oe.getType() == type){
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
		ObjectEntity object = entityInstance.getInstanceOf();
		object.removeInstance(entityInstance); 
		this.listEntityInstances.remove(entityInstance); 
	}
	
	/**
	 * clears all instances of a particular ObjectEntity
	 * @param entity: ObjectEntityInstances of this entity will be cleared.
	 */
	public void clearAllInstances(ObjectEntity entity) {
		this.listEntityInstances.removeAll(entity.getInstanceList());
		entity.removeAllInstances();
	}
	
	public ObjectEntityInstance getEntityInstanceByName(String name){
		for(ObjectEntityInstance entity: listEntityInstances){
			if(entity.getName().equalsIgnoreCase(name)){
				return entity; 
			}
		}
		return null; 
	}
}
