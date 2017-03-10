package unbbayes.model.umpst.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import unbbayes.model.umpst.ObjectModel;
import unbbayes.model.umpst.goal.GoalModel;
import unbbayes.model.umpst.goal.HypothesisModel;
import unbbayes.model.umpst.group.GroupModel;
import unbbayes.model.umpst.rule.RuleModel;

public class EntityModel extends ObjectModel{
	
	private static final long serialVersionUID = 1L;
	
	private Map<String,AttributeModel> mapAtributes;
	
	private List<GoalModel> backtrackingGoalsList;
	private List<HypothesisModel> backtrackingHypothesisList;
	
	private Set<RuleModel> fowardTrackingRules;
	private Set<GroupModel> fowardTrackingGroups;
	private Set<RelationshipModel> fowardTrackingRelationship;
	
	public EntityModel(String id,
			String entityName, 
			String comments,
			String author, 
			String date) {
		
		super(id, entityName, comments, author, date); 
		
		mapAtributes  = new HashMap<String, AttributeModel>();
		backtrackingGoalsList = new ArrayList<GoalModel>();
		backtrackingHypothesisList = new ArrayList<HypothesisModel>();
		fowardTrackingRules = new HashSet<RuleModel>();
		fowardTrackingGroups = new HashSet<GroupModel>();
		fowardTrackingRelationship = new HashSet<RelationshipModel>();
		
		super.setType("Entity"); 
		
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
	 * @return the backtrackingHypothesis
	 */
	public List<HypothesisModel> getBacktrackingHypothesis() {
		return backtrackingHypothesisList;
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
	public List<GoalModel> getBacktrackingGoalList() {
		return backtrackingGoalsList;
	}

	public String toString(){
		return getName(); 
	}
	
	
}