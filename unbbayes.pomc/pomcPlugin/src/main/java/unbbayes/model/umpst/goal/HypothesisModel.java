package unbbayes.model.umpst.goal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import unbbayes.model.umpst.ObjectModel;
import unbbayes.model.umpst.entity.EntityModel;
import unbbayes.model.umpst.group.GroupModel;
import unbbayes.model.umpst.rule.RuleModel;

public class HypothesisModel extends ObjectModel{
	
	private static final long serialVersionUID = 1L;
	
	private HypothesisModel father;
	private Set<GoalModel> goalRelated;
	private Map<String,HypothesisModel> mapSubHypothesis;
	
	private Set<EntityModel> fowardTrackingEntity;
	private Set<GroupModel> fowardTrackingGroups;
	private Set<RuleModel> fowardTrackingRules;

	
	public HypothesisModel(String id,
			String hypothesisName, 
			String comments,
			String author, 
			String date,
			Set<GoalModel> goalRelated, 
			HypothesisModel father,
			Map<String,HypothesisModel> subHypothesis) {
		
		super(id, hypothesisName, comments, author, date); 
		
		this.father = father;
		
		this.mapSubHypothesis = subHypothesis;
		if(subHypothesis==null){
			this.setMapSubHypothesis(new HashMap<String, HypothesisModel>());			
		}
		this.goalRelated = goalRelated;
		if(goalRelated==null){
			this.setGoalRelated(new HashSet<GoalModel>());
		}
		
		this.setFowardTrackingEntity(new HashSet<EntityModel>());
		
		this.setFowardTrackingGroups(new HashSet<GroupModel>());
		
		this.setFowardTrackingRules(new HashSet<RuleModel>()); 
		
		super.setType("Hypothesis");
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
	 * @return the mapSubHypothesis
	 */
	public Map<String, HypothesisModel> getMapSubHypothesis() {
		return mapSubHypothesis;
	}

	/**
	 * @param mapSubHypothesis the mapSubHypothesis to set
	 */
	public void setMapSubHypothesis(Map<String, HypothesisModel> mapSubHypothesis) {
		this.mapSubHypothesis = mapSubHypothesis;
	}

	/**
	 * @return the fowardTrackingEntity
	 */
	public Set<EntityModel> getFowardTrackingEntity() {
		return fowardTrackingEntity;
	}

	/**
	 * @param fowardTrackingEntity the fowardTrackingEntity to set
	 */
	public void setFowardTrackingEntity(Set<EntityModel> fowardTrackingEntity) {
		this.fowardTrackingEntity = fowardTrackingEntity;
	}


	/**
	 * @return the goalRelated
	 */
	public Set<GoalModel> getGoalRelated() {
		return goalRelated;
	}


	/**
	 * @param goalRelated the goalRelated to set
	 */
	public void setGoalRelated(Set<GoalModel> goalRelated) {
		this.goalRelated = goalRelated;
	}


	/**
	 * @return the children
	 */
	public Map<String,HypothesisModel> getSubHypothesis() {
		return mapSubHypothesis;
	}


	/**
	 * @param children the children to set
	 */
	public void setSubHypothesis(Map<String,HypothesisModel> subHypothesis) {
		this.mapSubHypothesis = subHypothesis;
	}

	/**
	 * @return the father
	 */
	public HypothesisModel getFather() {
		return father;
	}

	public Set<RuleModel> getFowardTrackingRules() {
		return fowardTrackingRules;
	}

	public void setFowardTrackingRules(Set<RuleModel> fowardTrackingRules) {
		this.fowardTrackingRules = fowardTrackingRules;
	}

	/**
	 * @param father the father to set
	 */
	public void setFather(HypothesisModel father) {
		this.father = father;
	}
	
	public String toString(){
		return this.getName(); 
	}
	
}