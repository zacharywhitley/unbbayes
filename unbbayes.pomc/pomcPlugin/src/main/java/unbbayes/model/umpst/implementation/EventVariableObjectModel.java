/**
 * 
 */
package unbbayes.model.umpst.implementation;

import java.util.ArrayList;

import unbbayes.model.umpst.entity.AttributeModel;
import unbbayes.model.umpst.entity.RelationshipModel;


/**
 * Random Variable Model (Cause and Effect Variable)
 * @author Diego Marques
 */
public class EventVariableObjectModel {
	
	private String id;
	private EventType typeEvent;
	private String relationship;
	private ArrayList<String> argumentList;
	private AttributeModel attributeModel;
	private RelationshipModel relationshipModel;
	private String attribute;

	/**
	 * Constructor of cause variable object
	 */
	public EventVariableObjectModel(String id, EventType typeEvent) {		
		this.id = id;
		this.typeEvent = typeEvent; 
		this.relationship = null;
		this.attribute = null;
		
		argumentList = new ArrayList<String>();
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
	 * @return the relationship
	 */
	public String getRelationship() {
		return relationship;
	}

	/**
	 * @param relationship the relationship to set
	 */
	public void setRelationship(String relationship) {
		this.relationship = relationship;
	}	

	/**
	 * @return the attribute
	 */
	public String getAttribute() {
		return attribute;
	}

	/**
	 * @param attribute the attribute to set
	 */
	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}

	/**
	 * @return the argumentList
	 */
	public ArrayList<String> getArgumentList() {
		return argumentList;
	}

	/**
	 * @param argumentList the argumentList to set
	 */
	public void setArgumentList(ArrayList<String> argumentList) {
		this.argumentList = argumentList;
	}

	/**
	 * @return the relationshipModel
	 */
	public RelationshipModel getRelationshipModel() {
		return relationshipModel;
	}

	/**
	 * @param relationshipModel the relationshipModel to set
	 */
	public void setRelationshipModel(RelationshipModel relationshipModel) {
		this.relationshipModel = relationshipModel;
	}

	/**
	 * @return the attributeModel
	 */
	public AttributeModel getAttributeModel() {
		return attributeModel;
	}

	/**
	 * @param attributeModel the attributeModel to set
	 */
	public void setAttributeModel(AttributeModel attributeModel) {
		this.attributeModel = attributeModel;
	}

	/**
	 * @return the typeEvent
	 */
	public EventType getTypeEvent() {
		return typeEvent;
	}

	/**
	 * @param typeEvent the typeEvent to set
	 */
	public void setTypeEvent(EventType typeEvent) {
		this.typeEvent = typeEvent;
	}	

}
