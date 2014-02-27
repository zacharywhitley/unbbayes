package unbbayes.model.umpst.requirements;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import unbbayes.model.umpst.entities.EntityModel;
import unbbayes.model.umpst.groups.GroupModel;

public class GoalModel {
	
	private String id;
	
	private String goalName;
	private String comments;
	private String author;
	private String date;
	
	private GoalModel                   goalFather;
	private Map<String, GoalModel>      subgoals ;
	private Map<String,HypothesisModel> mapHypothesis;
	private Set<EntityModel>            fowardTrackingEntity;
	private Set<GroupModel>             fowardTrackingGroups;
	private Set<GoalModel>              goalsRelated;
	
	public GoalModel(String id,
			String goalName, 
			String comments,
			String author, 
			String date, 
			GoalModel father,
			Map<String,GoalModel> children,
			Map<String,HypothesisModel> hypothesis,
			Set<EntityModel> fowardTrackingEntity,
			Set<GroupModel> fowardTrackingGroups,
			Set<GoalModel> goalsRelated) {
		
		this.id=id;
		this.goalName = goalName;
		this.comments = comments;
		this.author = author;
		this.date = date;
		this.goalFather = father;
		this.subgoals = children;
		
		if (children==null){
			this.setSubgoals(new HashMap<String, GoalModel>());
		}
		
		this.mapHypothesis=hypothesis;
		
		if ( hypothesis==null ){
			this.setMapHypothesis(new HashMap<String, HypothesisModel>());

		}
		
		this.fowardTrackingEntity = fowardTrackingEntity;
		
		if (fowardTrackingEntity==null){
			this.setFowardTrackingEntity(new HashSet<EntityModel>());
		}
		
		this.fowardTrackingGroups = fowardTrackingGroups;
		
		if (fowardTrackingGroups==null){
			this.setFowardTrackingGroups(new HashSet<GroupModel>());
		}
		
		this.goalsRelated=goalsRelated;
		
		if (goalsRelated==null){
			this.setGoalsRelated(new HashSet<GoalModel>());
		}
		
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
	 * @param fowardTrackingEntity the fowardTrackingEntity to set
	 */
	public void setFowardTrackingEntity(Set<EntityModel> fowardTrackingEntity) {
		this.fowardTrackingEntity = fowardTrackingEntity;
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
	 * @return the name
	 */
	public String getGoalName() {
		return goalName;
	}


	/**
	 * @param name the name to set
	 */
	public void setGoalName(String goalName) {
		this.goalName = goalName;
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


	public String toString(){
		return getGoalName(); 
	}

}
