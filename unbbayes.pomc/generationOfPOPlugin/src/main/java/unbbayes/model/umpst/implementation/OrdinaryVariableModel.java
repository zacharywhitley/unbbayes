/**
 * 
 */
package unbbayes.model.umpst.implementation;

import unbbayes.model.umpst.entity.EntityModel;


/**
 * Ordinary variable object
 * @author Diego Marques
 */
public class OrdinaryVariableModel {
	
	private String id;
	private String variable;
	private String typeEntity;
	private EntityModel entityObject;

	/**
	 * Constructor to ordinary variable object
	 */
	public OrdinaryVariableModel(String id,
			String variable,
			String typeEntity,
			EntityModel entityObject) {
		
		this.id = id;
		this.variable = variable;
		this.typeEntity = typeEntity;
		this.entityObject = entityObject;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the variable
	 */
	public String getVariable() {
		return variable;
	}

	/**
	 * @param variable the variable to set
	 */
	public void setVariable(String variable) {
		this.variable = variable;
	}

	/**
	 * @return the typeEntity
	 */
	public String getTypeEntity() {
		return typeEntity;
	}

	/**
	 * @param typeEntity the typeEntity to set
	 */
	public void setTypeEntity(String typeEntity) {
		this.typeEntity = typeEntity;
	}

	/**
	 * @return the entityObject
	 */
	public EntityModel getEntityObject() {
		return entityObject;
	}

	/**
	 * @param entityObject the entityObject to set
	 */
	public void setEntityObject(EntityModel entityObject) {
		this.entityObject = entityObject;
	}

}
