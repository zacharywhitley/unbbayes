package unbbayes.model.umpst.entities;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JList;

import unbbayes.model.umpst.groups.GroupsModel;
import unbbayes.model.umpst.rules.RulesModel;

public class EntityModel {
	
	private String id;
	private String entityName;
	private String author;
	private String date;
	private String comments;
	
	private Map<String,AttributeModel> mapAtributes  = new HashMap<String, AttributeModel>();
	private JList backtrackingGoalsList = new JList();
	private JList backtrackingHypothesisList = new JList();
	private Set<RulesModel> fowardTrackingRules = new HashSet<RulesModel>();
	private Set<GroupsModel> fowardTrackingGroups = new HashSet<GroupsModel>();
	private Set<RelationshipModel> fowardTrackingRelationship = new HashSet<RelationshipModel>();
	
	public EntityModel(String id,
			String entityName, 
			String comments,
			String author, 
			String date) {
		
		this.id           = id;
		this.entityName   = entityName;
		this.comments     = comments;
		this.author       = author;
		this.date         = date;
	}
	
	public EntityModel(String id) {
		
		this(id,"","","","");
		
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
	 * @return the backtrackingHypothesis
	 */
	public JList getBacktrackingHypothesis() {
		return backtrackingHypothesisList;
	}


	/**
	 * @param backtrackingHypothesis the backtrackingHypothesis to set
	 */
	public void setBacktrackingHypothesis(JList backtrackingHypothesis) {
		this.backtrackingHypothesisList = backtrackingHypothesis;
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
	 * @return the mapAtributes
	 */
	public Map<String, AttributeModel> getMapAtributes() {
		return mapAtributes;
	}


	/**
	 * @param mapAtributes the mapAtributes to set
	 */
	public void setMapAtributes(Map<String, AttributeModel> mapAtributes) {
		this.mapAtributes = mapAtributes;
	}


	/**
	 * @return the backtracking
	 */
	public JList getBacktracking() {
		return backtrackingGoalsList;
	}


	/**
	 * @param backtracking the backtracking to set
	 */
	public void setBacktracking(JList backtracking) {
		this.backtrackingGoalsList = backtracking;
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
	 * @return the entityName
	 */
	public String getEntityName() {
		return entityName;
	}


	/**
	 * @param entityName the entityName to set
	 */
	public void setEntityName(String entityName) {
		this.entityName = entityName;
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

	public String toString(){
		return this.entityName; 
	}
	
	
}