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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import unbbayes.prs.mebn.entity.exception.EntityInstanceAlreadyExistsException;
import unbbayes.prs.mebn.entity.exception.TypeAlreadyExistsException;
import unbbayes.prs.mebn.entity.exception.TypeDoesNotExistException;
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
public class ObjectEntityContainer {

	// Using List<ObjectEntity> Implementation
	private List<ObjectEntity> listEntity;
	
	// Using Tree Implementation
	private DefaultTreeModel entityTreeModel;
	private ObjectEntity rootObjectEntity;
	private HashMap<String,ObjectEntity> mapObjectEntity = new HashMap<String,ObjectEntity>();
	private HashMap<ObjectEntity,List<ObjectEntity>> mapObjectChilds = new HashMap<ObjectEntity,List<ObjectEntity>>();
	private HashMap<ObjectEntity,List<ObjectEntity>> mapObjectParents = new HashMap<ObjectEntity,List<ObjectEntity>>();
	
	private final String OBJECT_ENTITY = "ObjectEntity";
	
	private IObjectEntityBuilder objectEntityBuilder = null;
	
	private List<ObjectEntityInstance> listEntityInstances; 

	private TypeContainer typeContainer; 
	
	private int entityNum; 
	
	public ObjectEntityContainer(TypeContainer _typeConteiner){
		
		setTypeContainer(_typeConteiner); 
		entityNum = 1;
		
		// Implementation using List<ObjectEntity>
		listEntity = new ArrayList<ObjectEntity>(); 
		listEntityInstances = new ArrayList<ObjectEntityInstance>();
		
		createRootObjectEntity();
	}

	// Using Tree Implementation
	/**
	 * 
	 */
	protected void createRootObjectEntity() {
		
		if(rootObjectEntity == null) {
			try {
				
				rootObjectEntity = getObjectEntityByName(OBJECT_ENTITY);
				
				if(rootObjectEntity == null) {
					
					rootObjectEntity = new ObjectEntity(OBJECT_ENTITY, getTypeContainer()); 
					rootObjectEntity.getType().addUserObject(rootObjectEntity); 
				
					plusEntityNum();
					
				}
				
				addEntity(rootObjectEntity, null);
	
				this.mapObjectChilds.put(rootObjectEntity, new ArrayList<ObjectEntity>());
				this.mapObjectParents.put(rootObjectEntity, (List) Collections.emptyList());
				
				refreshTreeModel();
				
			} catch (TypeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	private void refreshChilds(ObjectEntity parentObjectEntity,DefaultMutableTreeNode parentNode) {

		for(ObjectEntity childObjectEntity: this.mapObjectChilds.get(parentObjectEntity)) {
			
			DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(childObjectEntity);
			entityTreeModel.insertNodeInto(childNode, parentNode, parentNode.getChildCount());
			
			refreshChilds(childObjectEntity,childNode);
		}
		
	}
	
	public void refreshTreeModel() {
		
		ObjectEntity rootObjectEntity = this.mapObjectEntity.get(this.getRootObjectEntity().getName());
		if(rootObjectEntity == null){
			// Maybe raise some Exception here
			return;
		}
		
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(rootObjectEntity);
		
		entityTreeModel = new DefaultTreeModel(rootNode);
		
		for(ObjectEntity childObjectEntity: this.mapObjectChilds.get(rootObjectEntity)) {
			
			DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(childObjectEntity);
			entityTreeModel.insertNodeInto(childNode, rootNode, rootNode.getChildCount());
			refreshChilds(childObjectEntity,childNode);
		}
	}

	/**
	 * Create a new Object Entity with the name specified. 
	 * Create a type for the Object Entity and this is added to the list of Type (see <Type>). 
	 * @param name The name of the entity
	 * @return the new ObjectEntity 
	 * @throws TypeException Some error when try to create a new type
	 */
	
	// Implementation using List<ObjectEntity>
	public ObjectEntity createObjectEntity(String name) throws TypeException{
		
		return this.createObjectEntity(name, null);
		
//		ObjectEntity objEntity = new ObjectEntity(name, getTypeContainer()); 
//		objEntity.getType().addUserObject(objEntity); 
//		
//		addEntity(objEntity);
//	
//		plusEntityNum(); 
//		
//		return objEntity; 
		
	}
	
	/**
	 * This method updates the content of {@link #listEntity}, {@link #mapObjectEntity},{@link #mapObjectChilds}, {@link #mapObjectParents}.
	 * @param entity : This will be included into {@link #listEntity}, and also will be used as key in 
	 * {@link #mapObjectEntity},{@link #mapObjectChilds}, {@link #mapObjectParents}.
	 * If this is null then we are creating a new root entity.
	 * @param parentObjectEntity : This will become the parent entity.
	 * @see #createObjectEntity(String, ObjectEntity)
	 * @see #getRootObjectEntity()
	 */
	protected void addEntity(ObjectEntity entity, ObjectEntity parentObjectEntity) {
				
		if(entity == null) {
			return;
		}
		
		String entityName = entity.getName();
		
		if(!this.mapObjectEntity.containsKey(entityName)){
			
			this.listEntity.add(entity);
			this.mapObjectEntity.put(entityName,entity);
			
		}
		
		if(parentObjectEntity == null) {
			
			this.mapObjectChilds.put(entity, new ArrayList<ObjectEntity>());
			this.mapObjectParents.put(entity, new ArrayList<ObjectEntity>());

			return;
		}
		
		List<ObjectEntity> mappedList = this.mapObjectChilds.get(parentObjectEntity);
		
		if(mappedList == null) {
			mappedList = new ArrayList<ObjectEntity>();
			this.mapObjectChilds.put(parentObjectEntity,mappedList);
		}
		
		if(!this.mapObjectChilds.get(parentObjectEntity).contains(entity)) {
			this.mapObjectChilds.get(parentObjectEntity).add(entity);
		}
		
		if(!this.mapObjectChilds.containsKey(entity)){
			this.mapObjectChilds.put(entity, new ArrayList<ObjectEntity>());
		}
		
		mappedList = this.mapObjectParents.get(entity);
		
		if(mappedList == null) {
			mappedList = new ArrayList<ObjectEntity>();
			this.mapObjectParents.put(entity,mappedList);
		}

		this.mapObjectParents.get(entity).add(parentObjectEntity);
		
	}
	
	// Using Tree Implementation
	/**
	 * 
	 * @param name
	 * @param parentObjectEntity : if this is null then a new entity will be created as a child of {@link #getRootObjectEntity()}
	 * @return
	 * @throws TypeException
	 */
	public ObjectEntity createObjectEntity(String name, ObjectEntity parentObjectEntity) throws TypeException{
		
		ObjectEntity objEntity = getObjectEntityByName(name);
		
		if(objEntity == null) {
			
			objEntity = this.getObjectEntityBuilder().getObjectEntity(name); 
			objEntity.getType().addUserObject(objEntity); 
		
			plusEntityNum();
			
		}
		
		// Forcing the parent to be the root if nothing was specified.
		if(parentObjectEntity == null) {
			
			parentObjectEntity = this.getRootObjectEntity();
		}
		
		addEntity(objEntity, parentObjectEntity);
		
		refreshTreeModel();
			
		return objEntity;	
	}
	
	// Implementation using List<ObjectEntity> 
//	public void removeEntity(ObjectEntity entity) throws Exception{
//
//		entity.delete(); 	
//		listEntity.remove(entity);
//
//	}
	
	public void renameEntity(ObjectEntity entity, String name) throws TypeAlreadyExistsException {
		
		this.mapObjectEntity.remove(entity.getName());
		
		entity.setName(name);
		this.mapObjectEntity.put(name,entity);
		
		refreshTreeModel();
	}
	
	// Using Tree Implementation 
	private void removeSelfAndChildsEntities(ObjectEntity entity) throws TypeDoesNotExistException {
		
		ObjectEntity parent;
		
		while(this.mapObjectChilds.get(entity).size() != 0){
			
			removeSelfAndChildsEntities(this.mapObjectChilds.get(entity).get(0));
		}
		
		entity.delete();
				
		this.listEntity.remove(entity);
		this.mapObjectChilds.remove(entity);
		
		while(this.mapObjectParents.get(entity).size() != 0){
			
			parent = this.mapObjectParents.get(entity).get(0);
			this.mapObjectChilds.get(parent).remove(entity);
			this.mapObjectParents.get(entity).remove(parent);
		}
		this.mapObjectParents.remove(entity);
	
		// Clear map of entity's name and the ObjectEntity 
		this.mapObjectEntity.remove(entity.getName());
	}
	
	// Using Tree Implementation 
	public void removeEntity(ObjectEntity entity) throws TypeDoesNotExistException,Exception {
		
		removeSelfAndChildsEntities(entity);
		refreshTreeModel();
	}
	
	// Using List<ObjectEntity> Implementation
//	public List<ObjectEntity> getListEntity() {
//		return listEntity;
//	}
	
	/**
	 * Returns a List of all elements present in entityTreeModel attribute.
	 * @return List<ObjectEntity>
	 */
	// Using Tree Implementation
	public List<ObjectEntity> getListEntity() {
		return listEntity;
	}
	
	// Using Tree Implementation
	public DefaultTreeModel getEntityTreeModel() {
		return this.entityTreeModel;
	}
	
	public List<ObjectEntity> getParentsOfObjectEntity(ObjectEntity entity) {
		return this.mapObjectParents.get(entity);
	}
	
	public List<ObjectEntity> getChildsOfObjectEntity(ObjectEntity entity) {
		return this.mapObjectChilds.get(entity);
	}
	
	private List<ObjectEntity> getDescendantsAndSelfRecursive(List<ObjectEntity> descendents, ObjectEntity entity) {
		
		for(ObjectEntity child: this.mapObjectChilds.get(entity)) {
			getDescendantsAndSelfRecursive(descendents, child);
		}
		
		descendents.add(entity);
		
		return descendents;
	}
	
	/**
	 * @param entity instance of {@link ObjectEntity}
	 * @return Return a {@link List} of {@link ObjectEntity} containing entity and all its descendants.
	 */
	public List<ObjectEntity> getDescendantsAndSelf(ObjectEntity entity) {
		return getDescendantsAndSelfRecursive(new ArrayList<ObjectEntity>(), entity);
	}
	
	// Using Tree Implementation
	public ObjectEntity getObjectEntityByName(String entityName) {
		try {
			return this.mapObjectEntity.get(entityName);
		}
		catch (Exception ex){
			return null;
		}
	}
	
	/**
	 * Returns the object entity with the name. Return null if 
	 * the object entity not exists. 
	 * @param name
	 * @return
	 */
	// Using List Implementation
//	public ObjectEntity getObjectEntityByName(String name){
//		for(ObjectEntity oe: listEntity){
//			if (oe.getName().equals(name) ){
//				return oe; 
//			}
//		}
//		
//		return null; 
//	}
	
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


	/**
	 * @return the typeContainer
	 */
	public TypeContainer getTypeContainer() {
		return typeContainer;
	}


	/**
	 * This will set the type container and also instantiate a new {@link ObjectEntityBuilder}. 
	 * @param typeContainer the typeContainer to set
	 * @see #setObjectEntityBuilder(IObjectEntityBuilder).
	 */
	protected void setTypeContainer(TypeContainer typeContainer) {
		this.typeContainer = typeContainer;
		this.setObjectEntityBuilder(ObjectEntityBuilder.getInstance(typeContainer));
	}
	
	/**
	 * Set a new name for root {@link ObjectEntity}
	 * @param name: New name of root {@link ObjectEntity}
	 * @return {@link Boolean} that indicates if the new name was set correctly
	 */
	public boolean setRootObjectEntityName(String name){
		try {
			
			this.renameEntity(rootObjectEntity, name);	
			return true;
		} catch (Exception e) {
			
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * @return {@link ObjectEntity} that is the root of {@link DefaultTreeModel}
	 */
	public ObjectEntity getRootObjectEntity() {
		return rootObjectEntity;
	}

	/**
	 * @return the objectEntityBuilder
	 * @see #createObjectEntity(String, ObjectEntity)
	 */
	public IObjectEntityBuilder getObjectEntityBuilder() {
		return objectEntityBuilder;
	}

	/**
	 * @param objectEntityBuilder the objectEntityBuilder to set
	 * @see #createObjectEntity(String, ObjectEntity)
	 */
	public void setObjectEntityBuilder(IObjectEntityBuilder objectEntityBuilder) {
		this.objectEntityBuilder = objectEntityBuilder;
	}
}
