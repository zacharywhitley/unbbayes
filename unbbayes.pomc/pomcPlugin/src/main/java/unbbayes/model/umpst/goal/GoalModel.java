package unbbayes.model.umpst.goal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import unbbayes.model.umpst.ObjectModel;
import unbbayes.model.umpst.entity.EntityModel;
import unbbayes.model.umpst.group.GroupModel;
import unbbayes.model.umpst.rule.RuleModel;

public class GoalModel extends ObjectModel{
	
	private static final long serialVersionUID = 1L;
	
	private GoalModel                   goalFather;
	
	private Map<String, GoalModel>      subgoals ;
	
	private Map<String, HypothesisModel> mapHypothesis;
	
	private Set<EntityModel>            fowardTrackingEntity;
	private Set<GroupModel>             fowardTrackingGroups;
	private Set<RuleModel>              fowardTrackingRules; 
	
	private Set<GoalModel>              goalsRelated;
	
	public GoalModel(String id,
			String goalName, 
			String comments,
			String author, 
			String date, 
			GoalModel father) {
		
		super(id, goalName, comments, author, date);
		
		this.goalFather = father;
				
		this.setSubgoals(new HashMap<String, GoalModel>()); //List of Subgoals
		
		this.setMapHypothesis(new HashMap<String, HypothesisModel>()); //List of hypothesis
		
		this.setFowardTrackingEntity(new HashSet<EntityModel>()); // Traceability
		
		this.setFowardTrackingGroups(new HashSet<GroupModel>()); // Traceability
		
		this.setFowardTrackingRules(new HashSet<RuleModel>()); // Traceability
		
		this.setGoalsRelated(new HashSet<GoalModel>());
		
		super.setType("Goal");
		
	}

	/**
	 * @return the goalsRelated
	 */
	public Set<GoalModel> getGoalsRelated() {
		return goalsRelated;
	}


	/**
	 * @param goalsRelated the goalsRelated to set
	 */
	public void setGoalsRelated(Set<GoalModel> goalsRelated) {
		this.goalsRelated = goalsRelated;
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
	 * @return the fowardTrackingEntity
	 */
	public Set<EntityModel> getFowardTrackingEntity() {
		return fowardTrackingEntity;
	}


	/**
	 * @param the fowardTrackingEntity the fowardTrackingEntity to set
	 */
	public void setFowardTrackingEntity(Set<EntityModel> fowardTrackingEntity) {
		this.fowardTrackingEntity = fowardTrackingEntity;
	}


	/**
	 * @return the fowardTrackingRules
	 */
	public Set<RuleModel> getFowardTrackingRules() {
		return fowardTrackingRules;
	}


	/** 
	 * @param fowardTrackingRule
	 */
	public void setFowardTrackingRules(Set<RuleModel> fowardTrackingRule) {
		this.fowardTrackingRules = fowardTrackingRule;
	}
	
	/**
	 * @return the goalFather
	 */
	public GoalModel getGoalFather() {
		return goalFather;
	}


	/**
	 * @param goalFather the goalFather to set
	 */
	public void setGoalFather(GoalModel goalFather) {
		this.goalFather = goalFather;
	}

	/**
	 * @return the subgoals
	 */
	public Map<String,GoalModel> getSubgoals() {
		return subgoals;
	}

	/**
	 * @param subgoals the subgoals to set
	 */
	public void setSubgoals(Map<String,GoalModel> subgoals) {
		this.subgoals = subgoals;
	}

	/**
	 * @return the mapHypothesis
	 */
	public Map<String, HypothesisModel> getMapHypothesis() {
		return mapHypothesis;
	}


	/**
	 * @param mapHypothesis the mapHypothesis to set
	 */
	public void setMapHypothesis(Map<String, HypothesisModel> mapHypothesis) {
		this.mapHypothesis = mapHypothesis;
	}

	public String toString(){
		return getName(); 
	}

}
