package unbbayes.model.umpst.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import unbbayes.model.umpst.ObjectModel;
import unbbayes.model.umpst.groups.GroupModel;
import unbbayes.model.umpst.requirements.GoalModel;
import unbbayes.model.umpst.requirements.HypothesisModel;
import unbbayes.model.umpst.rules.RuleModel;

public class EntityModel extends ObjectModel{
	
	private Map<String,AttributeModel> mapAtributes  = new HashMap<String, AttributeModel>();
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
		
		backtrackingGoalsList = new ArrayList<GoalModel>();
		backtrackingHypothesisList = new ArrayList<HypothesisModel>();
		fowardTrackingRules = new HashSet<RuleModel>();
		fowardTrackingGroups = new HashSet<GroupModel>();
		fowardTrackingRelationship = new HashSet<RelationshipModel>();
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


//	/**
//	 * @param backtrackingHypothesis the backtrackingHypothesis to set
//	 */
//	public void setBacktrackingHypothesis(JList backtrackingHypothesis) {
//		this.backtrackingHypothesisList = backtrackingHypothesis;
//	}


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


//	/**
//	 * @param backtracking the backtracking to set
//	 */
//	public void setBacktracking(JList backtracking) {
//		this.backtrackingGoalsList = backtracking;
//	}

	public String toString(){
		return getName(); 
	}
	
	
}