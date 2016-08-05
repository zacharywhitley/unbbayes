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
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import unbbayes.io.mebn.PROWLModelUser;
import unbbayes.prs.mebn.entity.exception.EntityInstanceAlreadyExistsException;
import unbbayes.prs.mebn.entity.exception.TypeAlreadyExistsException;
import unbbayes.prs.mebn.entity.exception.TypeDoesNotExistException;
import unbbayes.prs.mebn.entity.exception.TypeException;
import unbbayes.util.Debug;

/**
 * Contains the Object entities of a MEBN. 
 * 
 * Note: this container should not be a singleton, since an user might edit
 * two MTheories simultaneously; naturally, using different sets of entities.
 * 
 * TODO create a method which can disconnect an entity from its children or its parents without deleting the entire subtree. 
 * 
 * @author Laecio Lima dos Santos
 * @author Shou Matsumoto
 * @author Guilherme Carvalho Torres
 * @version 2.0 Hierarchy of object entities
 * @version 1.1 02/25/2008
 * 
 *
 */
public class ObjectEntityContainer {

	// Using List<ObjectEntity> Implementation
	private List<ObjectEntity> listEntity;
	
	// Using Tree Implementation
	// TODO remove swing classes from API
	private DefaultTreeModel entityTreeModel;
	private ObjectEntity rootObjectEntity;
	private Map<String,ObjectEntity> mapObjectEntity = new HashMap<String,ObjectEntity>();
	private Map<ObjectEntity,List<ObjectEntity>> mapObjectChildren = new HashMap<ObjectEntity,List<ObjectEntity>>();
	private Map<ObjectEntity,List<ObjectEntity>> mapObjectParents = new HashMap<ObjectEntity,List<ObjectEntity>>();
	
	/** 
	 * This will be the initial value of {@link #getDefaultRootEntityName()} 
	 * @see #getDefaultRootEntityName()
	 * @deprecated use {@link PROWLModelUser#OBJECT_ENTITY} instead.
	 */
	public static final String OBJECT_ENTITY = PROWLModelUser.OBJECT_ENTITY;
	
	private String defaultRootEntityName = PROWLModelUser.OBJECT_ENTITY;
	
	private IObjectEntityBuilder objectEntityBuilder = null;
	
	private List<ObjectEntityInstance> listEntityInstances; 

	private TypeContainer typeContainer; 
	
	private int entityNum; 
	
	/**
	 * @param _typeConteiner : the object responsible for managing types (types are not necessarily equal to entities).
	 * @see #ObjectEntityContainer(TypeContainer, IObjectEntityBuilder)
	 */
	public ObjectEntityContainer(TypeContainer _typeConteiner){
		this(_typeConteiner, ObjectEntityBuilder.getInstance(_typeConteiner));
	}
	
	/**
	 * Default constructor initializing fields.
	 * This will also call {@link #createRootObjectEntity()} in order to initialize {@link #getRootObjectEntity()}
	 * @param _typeConteiner : the object responsible for managing types (types are not necessarily equal to entities).
	 * @param objectEntityBuilder : builder to be used in order to create new instances of {@link ObjectEntity}
	 * This should not be null.
	 * @see #setObjectEntityBuilder(IObjectEntityBuilder)
	 */
	public ObjectEntityContainer(TypeContainer _typeConteiner, IObjectEntityBuilder objectEntityBuilder){
		
		
		setTypeContainer(_typeConteiner); 
		
		this.setObjectEntityBuilder(objectEntityBuilder);
		
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
				
				rootObjectEntity = getObjectEntityByName(getDefaultRootEntityName());
				
				if(rootObjectEntity == null) {
					Type objectEntityType = getTypeContainer().getType(getDefaultRootEntityName() + "_label");
					if (objectEntityType != null) {
						// remove existing type
						getTypeContainer().removeType(objectEntityType);
					}
					
//					rootObjectEntity = new ObjectEntity(OBJECT_ENTITY, getTypeContainer()); 
					// the above line was substituted with the following
					rootObjectEntity = getObjectEntityBuilder().getObjectEntity(getDefaultRootEntityName()) ;
					rootObjectEntity.getType().addUserObject(rootObjectEntity); 
				
					plusEntityNum();
					
				}
				
				addEntity(rootObjectEntity, null);
	
//				this.mapObjectChildren.put(rootObjectEntity, new ArrayList<ObjectEntity>());	// this is redundant
				this.mapObjectParents.put(rootObjectEntity, (List) Collections.emptyList());	// this reduces memory usage
				
				refreshTreeModel();
				
			} catch (TypeException e) {
				// TODO Auto-generated catch block
				throw new RuntimeException(e);
			}
		}
		
	}
	
	private void refreshChildren(ObjectEntity parentObjectEntity,DefaultMutableTreeNode parentNode) {

		for(ObjectEntity childObjectEntity: this.mapObjectChildren.get(parentObjectEntity)) {
			
			DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(childObjectEntity);
			entityTreeModel.insertNodeInto(childNode, parentNode, parentNode.getChildCount());
			
			refreshChildren(childObjectEntity,childNode);
		}
		
	}
	
	public void refreshTreeModel() {
		
		ObjectEntity rootObjectEntity = this.mapObjectEntity.get(this.getRootObjectEntity().getName());
		if(rootObjectEntity == null){
			// Maybe raise some Exception here
			return;
		}
		// TODO remove swing classes from dependence.
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(rootObjectEntity);
		
		entityTreeModel = new DefaultTreeModel(rootNode);
		
		for(ObjectEntity childObjectEntity: this.mapObjectChildren.get(rootObjectEntity)) {
			
			DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(childObjectEntity);
			entityTreeModel.insertNodeInto(childNode, rootNode, rootNode.getChildCount());
			refreshChildren(childObjectEntity,childNode);
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
	 * This method updates the content of {@link #listEntity}, {@link #mapObjectEntity},{@link #mapObjectChildren}, {@link #mapObjectParents}.
	 * @param entity : This will be included into {@link #listEntity}, and also will be used as key in 
	 * {@link #mapObjectEntity},{@link #mapObjectChildren}, {@link #mapObjectParents}.
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
			
			entity.addNameChangeListener(new IEntityNameChangeListener() {
				/** This should update map of names whenever object entity's name is changed*/
				public void onNameChange(String oldName, String newName, Entity entity) {
					getMapObjectEntity().remove(oldName);
					try {
						getMapObjectEntity().put(newName, (ObjectEntity)entity);
					} catch (ClassCastException e) {
						Debug.println(getClass(), "Could not update entity name mapping of " + entity, e);
					}
					
				}
			});
		}
		
		if(parentObjectEntity == null) {
			
			this.mapObjectChildren.put(entity, new ArrayList<ObjectEntity>());
			this.mapObjectParents.put(entity, new ArrayList<ObjectEntity>());

			return;
		}
		
		List<ObjectEntity> mappedList = this.mapObjectChildren.get(parentObjectEntity);
		
		if(mappedList == null) {
			mappedList = new ArrayList<ObjectEntity>();
			this.mapObjectChildren.put(parentObjectEntity,mappedList);
		}
		
		if(!this.mapObjectChildren.get(parentObjectEntity).contains(entity)) {
			this.mapObjectChildren.get(parentObjectEntity).add(entity);
		}
		
		if(!this.mapObjectChildren.containsKey(entity)){
			this.mapObjectChildren.put(entity, new ArrayList<ObjectEntity>());
		}
		
		mappedList = this.mapObjectParents.get(entity);
		
		if(mappedList == null) {
			mappedList = new ArrayList<ObjectEntity>();
			this.mapObjectParents.put(entity,mappedList);
		}

		if (!this.mapObjectParents.get(entity).contains(parentObjectEntity)) {	// avoid duplicates
			this.mapObjectParents.get(entity).add(parentObjectEntity);
		}
		
		
		
	}
	
	// Using Tree Implementation
	/**
	 * 
	 * @param name
	 * @param parentObjectEntity : if this is null then a new entity will be created as a child of {@link #getRootObjectEntity()}
	 * @return
	 * @throws TypeException
	 * 
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
	
	/**
	 * @param entity : entity to rename
	 * @param name : new name
	 * @throws TypeAlreadyExistsException
	 * @see {@link ObjectEntity#setName(String)}
	 * @see IEntityNameChangeListener#onNameChange(String, String, Entity)
	 */
	public void renameEntity(ObjectEntity entity, String name) throws TypeAlreadyExistsException {
		
		// the code to update mapObjectEntity and tree model were migrated to ObjectEntity#EntityNameChangeListener
//		this.mapObjectEntity.remove(entity.getName());
		
		entity.setName(name);
		
		// the code to update mapObjectEntity and tree model were migrated to ObjectEntity#EntityNameChangeListener
//		this.mapObjectEntity.put(name,entity);
		
		refreshTreeModel();
	}
	
	// Using Tree Implementation 
	private void removeSelfAndChildEntities(ObjectEntity entity) throws TypeDoesNotExistException {
		
		ObjectEntity parent;
		
		while(this.mapObjectChildren.get(entity).size() != 0){
			
			removeSelfAndChildEntities(this.mapObjectChildren.get(entity).get(0));
		}
		
		entity.delete();
				
		this.listEntity.remove(entity);
		this.mapObjectChildren.remove(entity);
		
		while(this.mapObjectParents.get(entity).size() != 0){
			
			parent = this.mapObjectParents.get(entity).get(0);
			this.mapObjectChildren.get(parent).remove(entity);
			this.mapObjectParents.get(entity).remove(parent);
		}
		this.mapObjectParents.remove(entity);
	
		// Clear map of entity's name and the ObjectEntity 
		this.mapObjectEntity.remove(entity.getName());
	}
	
	// Using Tree Implementation 
	public void removeEntity(ObjectEntity entity) throws TypeDoesNotExistException,Exception {
		
		removeSelfAndChildEntities(entity);
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
	
	public List<ObjectEntity> getChildrenOfObjectEntity(ObjectEntity entity) {
		return this.mapObjectChildren.get(entity);
	}
	
	private List<ObjectEntity> getDescendantsAndSelfRecursive(List<ObjectEntity> descendents, ObjectEntity entity) {
		
		for(ObjectEntity child: this.mapObjectChildren.get(entity)) {
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
			throw new EntityInstanceAlreadyExistsException(entityInstance.toString()); 
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
	 * @return {@link ObjectEntity} that is the root of {@link DefaultTreeModel}
	 */
	protected void setRootObjectEntity(ObjectEntity root) {
		rootObjectEntity = root;
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

	/**
	 * @return the mapObjectChildren
	 */
	protected Map<ObjectEntity, List<ObjectEntity>> getMapObjectChildren() {
		return this.mapObjectChildren;
	}

	/**
	 * @param mapObjectChildren the mapObjectChildren to set
	 */
	protected void setMapObjectChildren(
			Map<ObjectEntity, List<ObjectEntity>> mapObjectChildren) {
		this.mapObjectChildren = mapObjectChildren;
	}

	/**
	 * @return the mapObjectParents
	 */
	protected Map<ObjectEntity, List<ObjectEntity>> getMapObjectParents() {
		return this.mapObjectParents;
	}

	/**
	 * @param mapObjectParents the mapObjectParents to set
	 */
	protected void setMapObjectParents(
			Map<ObjectEntity, List<ObjectEntity>> mapObjectParents) {
		this.mapObjectParents = mapObjectParents;
	}

	/**
	 * @return the mapObjectEntity
	 */
	protected Map<String, ObjectEntity> getMapObjectEntity() {
		return this.mapObjectEntity;
	}

	/**
	 * @param mapObjectEntity the mapObjectEntity to set
	 */
	protected void setMapObjectEntity(HashMap<String, ObjectEntity> mapObjectEntity) {
		this.mapObjectEntity = mapObjectEntity;
	}

	/**
	 * @return the defaultRootEntityName : this will be used in {@link #createRootObjectEntity()}
	 * as the name of the root object entity.
	 * @see #getRootObjectEntity()
	 */
	public String getDefaultRootEntityName() {
		return defaultRootEntityName;
	}

	/**
	 * @param defaultRootEntityName the defaultRootEntityName to set : this will be used in {@link #createRootObjectEntity()}
	 * as the name of the root object entity.
	 * @see #getRootObjectEntity()
	 */
	public void setDefaultRootEntityName(String defaultRootEntityName) {
		this.defaultRootEntityName = defaultRootEntityName;
	}
}
