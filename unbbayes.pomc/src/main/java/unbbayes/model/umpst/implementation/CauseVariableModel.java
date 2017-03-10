/**
 * 
 */
package unbbayes.model.umpst.implementation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import unbbayes.model.umpst.entity.AttributeModel;
import unbbayes.model.umpst.entity.RelationshipModel;


/**
 * Variable object of Cause
 * @author Diego Marques
 */
public class CauseVariableModel extends EventVariableObjectModel{
	
	private String id;
	private String relationship;
//	private ArrayList<String> argumentList;
	private ArrayList<OrdinaryVariableModel> ovArgumentList;
	private ArrayList<AttributeModel> attArgumentList;
//	private AttributeModel attributeModel;
	private RelationshipModel relationshipModel;
	private String attribute;

	/**
	 * Constructor of cause variable object
	 */
	public CauseVariableModel(String id) {
		super(id, EventType.CAUSE);
		this.id = id;
		this.relationship = null;
		this.attribute = null;
		
		setOvArgumentList(new ArrayList<OrdinaryVariableModel>());
		setAttArgumentList(new ArrayList<AttributeModel>());
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

//	/**
//	 * @return the argumentList
//	 */
//	public ArrayList<String> getArgumentList() {
//		return argumentList;
//	}
//
//	/**
//	 * @param argumentList the argumentList to set
//	 */
//	public void setArgumentList(ArrayList<String> argumentList) {
//		this.argumentList = argumentList;
//	}

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

//	/**
//	 * @return the attributeModel
//	 */
//	public AttributeModel getAttributeModel() {
//		return attributeModel;
//	}
//
//	/**
//	 * @param attributeModel the attributeModel to set
//	 */
//	public void setAttributeModel(AttributeModel attributeModel) {
//		this.attributeModel = attributeModel;
//	}
	
	/**
	 * @return the ovArgumentList
	 */
	public ArrayList<OrdinaryVariableModel> getOvArgumentList() {
		return ovArgumentList;
	}

	/**
	 * @param ovArgumentList the ovArgumentList to set
	 */
	public void setOvArgumentList(ArrayList<OrdinaryVariableModel> ovArgumentList) {
		this.ovArgumentList = ovArgumentList;
	}

	/**
	 * @return the attArgumentList
	 */
	public ArrayList<AttributeModel> getAttArgumentList() {
		return attArgumentList;
	}

	/**
	 * @param attArgumentList the attArgumentList to set
	 */
	public void setAttArgumentList(ArrayList<AttributeModel> attArgumentList) {
		this.attArgumentList = attArgumentList;
	}	

}
