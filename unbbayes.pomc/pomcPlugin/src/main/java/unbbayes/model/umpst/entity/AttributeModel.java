package unbbayes.model.umpst.entity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import unbbayes.model.umpst.ObjectModel;
import unbbayes.model.umpst.group.GroupModel;
import unbbayes.model.umpst.rule.RuleModel;


public class AttributeModel extends ObjectModel {
	
	private static final long serialVersionUID = 1L;
	
	private AttributeModel father;
	private Set<EntityModel> entityRelated;
	private Map<String, AttributeModel> mapSubAtributes;
	private Set<RelationshipModel> fowardTrackingRelationship;
	private Set<RuleModel> fowardTrackingRules;
	private Set<GroupModel> fowardTrackingGroups;
	
	public AttributeModel(String id,
			String atributeName, 
			String comments,
			String author, 
			String date,
			Set<EntityModel> entityRelated,
			AttributeModel father,
			Map<String,AttributeModel> mapSubAtributes,
			Set<RelationshipModel> fowardTrackingRelationship,
			Set<RuleModel> fowardTrackingRules , 
			Set<GroupModel> fowardTrackingGroups) {

		super(id, atributeName, comments, author, date); 
		
		this.father = father;
		
		this.entityRelated=entityRelated;
		
		if(entityRelated==null){
			this.setEntityRelated(new HashSet<EntityModel>());
		}
		
		this.mapSubAtributes=mapSubAtributes;
		
		if (mapSubAtributes==null){
			this.setMapSubAtributes(new HashMap<String, AttributeModel>());
		}
		
		this.fowardTrackingRelationship=fowardTrackingRelationship;
		
		if(fowardTrackingRelationship==null){
			this.setFowardTrackingRelationship(new java.util.HashSet<RelationshipModel>());
		}
		
		this.fowardTrackingRules=fowardTrackingRules;
		
		if(fowardTrackingRules==null){
			this.setFowardTrackingRules(new HashSet<RuleModel>());
		}
		
		this.fowardTrackingGroups=fowardTrackingGroups;
		
		if(fowardTrackingGroups==null){
			this.setFowardTrackingGroups(new HashSet<GroupModel>());
		}	
	}


	/**
	 * @return the fowardTrackingRules
	 */
	public Set<RuleModel> getFowardTrackingRules() {
		return fowardTrackingRules;
	}
	

	/**
	 * @param fowardTrackingRules the fowardTrackingRules to set
	 */
	public void setFowardTrackingRules(Set<RuleModel> fowardTrackingRules) {
		this.fowardTrackingRules = fowardTrackingRules;
	}


	/**
	 * @return the fowardTrackingGroups
	 */
	public Set<GroupModel> getFowardTrackingGroups() {
		return fowardTrackingGroups;
	}




	/**
	 * @param fowardTrackingGroups the fowardTrackingGroups to set
	 */
	public void setFowardTrackingGroups(Set<GroupModel> fowardTrackingGroups) {
		this.fowardTrackingGroups = fowardTrackingGroups;
	}




	/**
	 * @return the fowardTrackingRelationship
	 */
	public Set<RelationshipModel> getFowardTrackingRelationship() {
		return fowardTrackingRelationship;
	}






	/**
	 * @param fowardTrackingRelationship the fowardTrackingRelationship to set
	 */
	public void setFowardTrackingRelationship(
			Set<RelationshipModel> fowardTrackingRelationship) {
		this.fowardTrackingRelationship = fowardTrackingRelationship;
	}






	/**
	 * @return the entityRelated
	 */
	public Set<EntityModel> getEntityRelated() {
		return entityRelated;
	}


	/**
	 * @param entityRelated the entityRelated to set
	 */
	public void setEntityRelated(Set<EntityModel> entityRelated) {
		this.entityRelated = entityRelated;
	}


	/**
	 * @return the mapSubAtributes
	 */
	public Map<String, AttributeModel> getMapSubAtributes() {
		return mapSubAtributes;
	}


	/**
	 * @param mapSubAtributes the mapSubAtributes to set
	 */
	public void setMapSubAtributes(Map<String, AttributeModel> mapSubAtributes) {
		this.mapSubAtributes = mapSubAtributes;
	}


	/**
	 * @return the father
	 */
	public AttributeModel getFather() {
		return father;
	}


	/**
	 * @param father the father to set
	 */
	public void setFather(AttributeModel father) {
		this.father = father;
	}
	
	public String toString(){
		return this.getName(); 
	}
	
}