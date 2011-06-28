package unbbayes.model.umpst.entities;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import unbbayes.model.umpst.groups.GroupsModel;
import unbbayes.model.umpst.rules.RulesModel;


public class AtributeModel {
	
	private String id;
	private String atributeName;
	private String comments;
	private String author;
	private String date;
	private AtributeModel father;
	private Set<EntityModel> entityRelated;
	private Map<String, AtributeModel> mapSubAtributes;
	private Set<RelationshipModel> fowardTrackingRelationship;
	private Set<RulesModel> fowardTrackingRules;
	private Set<GroupsModel> fowardTrackingGroups;
	
	public AtributeModel(String id,String atributeName, String comments,String author, String date,Set<EntityModel> entityRelated,
			AtributeModel father,Map<String,AtributeModel> mapSubAtributes,Set<RelationshipModel> fowardTrackingRelationship,
			Set<RulesModel> fowardTrackingRules , Set<GroupsModel> fowardTrackingGroups) {
		
		this.id=id;
		this.atributeName = atributeName;
		this.comments = comments;
		this.author = author;
		this.date = date;
		this.father = father;
		this.entityRelated=entityRelated;
		if(entityRelated==null){
			this.setEntityRelated(new HashSet<EntityModel>());
		}
		this.mapSubAtributes=mapSubAtributes;
		if (mapSubAtributes==null){
			this.setMapSubAtributes(new HashMap<String, AtributeModel>());
		}
		this.fowardTrackingRelationship=fowardTrackingRelationship;
		if(fowardTrackingRelationship==null){
			this.setFowardTrackingRelationship(new java.util.HashSet<RelationshipModel>());
		}
		this.fowardTrackingRules=fowardTrackingRules;
		if(fowardTrackingRules==null){
			this.setFowardTrackingRules(new HashSet<RulesModel>());
		}
		this.fowardTrackingGroups=fowardTrackingGroups;
		if(fowardTrackingGroups==null){
			this.setFowardTrackingGroups(new HashSet<GroupsModel>());
		}
	}

	


	/**
	 * @return the fowardTrackingRules
	 */
	public Set<RulesModel> getFowardTrackingRules() {
		return fowardTrackingRules;
	}




	/**
	 * @param fowardTrackingRules the fowardTrackingRules to set
	 */
	public void setFowardTrackingRules(Set<RulesModel> fowardTrackingRules) {
		this.fowardTrackingRules = fowardTrackingRules;
	}




	/**
	 * @return the fowardTrackingGroups
	 */
	public Set<GroupsModel> getFowardTrackingGroups() {
		return fowardTrackingGroups;
	}




	/**
	 * @param fowardTrackingGroups the fowardTrackingGroups to set
	 */
	public void setFowardTrackingGroups(Set<GroupsModel> fowardTrackingGroups) {
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
	public Map<String, AtributeModel> getMapSubAtributes() {
		return mapSubAtributes;
	}


	/**
	 * @param mapSubAtributes the mapSubAtributes to set
	 */
	public void setMapSubAtributes(Map<String, AtributeModel> mapSubAtributes) {
		this.mapSubAtributes = mapSubAtributes;
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
	 * @return the atributeName
	 */
	public String getAtributeName() {
		return atributeName;
	}


	/**
	 * @param atributeName the atributeName to set
	 */
	public void setAtributeName(String atributeName) {
		this.atributeName = atributeName;
	}


	/**
	 * @return the comments
	 */
	public String getComments() {
		return comments;
	}


	/**
	 * @param comments the comments to set
	 */
	public void setComments(String comments) {
		this.comments = comments;
	}


	/**
	 * @return the author
	 */
	public String getAuthor() {
		return author;
	}


	/**
	 * @param author the author to set
	 */
	public void setAuthor(String author) {
		this.author = author;
	}


	/**
	 * @return the date
	 */
	public String getDate() {
		return date;
	}


	/**
	 * @param date the date to set
	 */
	public void setDate(String date) {
		this.date = date;
	}


	/**
	 * @return the father
	 */
	public AtributeModel getFather() {
		return father;
	}


	/**
	 * @param father the father to set
	 */
	public void setFather(AtributeModel father) {
		this.father = father;
	}
	
	
	
}