/**
 * 
 */
package unbbayes.model.umpst.implementation;

import java.util.ArrayList;

import unbbayes.model.umpst.entity.AttributeModel;
import unbbayes.model.umpst.entity.RelationshipModel;


/**
 * Variable object of Effect
 * @author Diego Marques
 */
public class EffectVariableModel extends EventVariableObjectModel {
	
	private String id;
	private String relationship;
	private RelationshipModel relationshipModel;
	private ArrayList<OrdinaryVariableModel> ovArgumentList;
	private ArrayList<AttributeModel> attArgumentList;
	private AttributeModel attributeModel;

	/**
	 * Constructor of effect variable object
	 */
	public EffectVariableModel(String id) {
		super(id, EventType.EFFECT);
		this.id = id;
		this.relationship = null;
		
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
