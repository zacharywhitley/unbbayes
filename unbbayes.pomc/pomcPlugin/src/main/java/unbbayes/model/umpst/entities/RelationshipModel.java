package unbbayes.model.umpst.entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import unbbayes.model.umpst.groups.GroupModel;
import unbbayes.model.umpst.requirements.GoalModel;
import unbbayes.model.umpst.requirements.HypothesisModel;
import unbbayes.model.umpst.rules.RuleModel;


public class RelationshipModel {
	
	private String id;
	private String comments;
	private String author;
	private String date;
	
	private List<EntityModel>     entityList;
	private List<AttributeModel>  atributeList;
	private List<GoalModel>       goalList;
	private List<HypothesisModel> hypothesisList;
	
	private Set<RuleModel> fowardtrackingRulesSet;
	private Set<GroupModel> fowardtrackingGroupsSet;

	
	public RelationshipModel(String id,
			String relationshipName, 
			String comments,
			String author, 
			String date,
			List<EntityModel> backtrackingEntity, 
			List<AttributeModel> backtrackingAtribute,
			List<GoalModel> backtrackingGoal,
			List<HypothesisModel> backtrackingHypothesis,
			Set<RuleModel> fowardtrackingRules,
			Set<GroupModel> fowardtrackingGroups) {
		
		this.id=id;
		this.relationshipName = relationshipName;
		this.comments = comments;
		this.author = author;
		this.date = date;
		this.atributeList=backtrackingAtribute;
		
		if(backtrackingAtribute==null){
			this.setBacktrackingAtribute(new ArrayList<AttributeModel>());
		}
		this.entityList=backtrackingEntity;
		if(backtrackingEntity==null){
			this.setBacktrackingEntity(new ArrayList<EntityModel>());
		}
		this.goalList=backtrackingGoal;
		if(backtrackingGoal==null){
			this.setBacktrackingGoal(new ArrayList<GoalModel>());
		}
		this.hypothesisList=backtrackingHypothesis;
		if(backtrackingHypothesis==null){
			this.setBacktrackingHypothesis(new ArrayList<HypothesisModel>());
		}
		this.fowardtrackingRulesSet=fowardtrackingRules;
		if(fowardtrackingRules==null){
			this.setFowardtrackingRules(new HashSet<RuleModel>());
		}
		this.fowardtrackingGroupsSet=fowardtrackingGroups;
		if(fowardtrackingGroups==null){
			this.setFowardtrackingGroups(new HashSet<GroupModel>());
		}
	}


	/**
	 * @return the fowardtrackingRules
	 */
	public Set<RuleModel> getFowardtrackingRules() {
		return fowardtrackingRulesSet;
	}

	/**
	 * @param fowardtrackingRules the fowardtrackingRules to set
	 */
	public void setFowardtrackingRules(Set<RuleModel> fowardtrackingRules) {
		this.fowardtrackingRulesSet = fowardtrackingRules;
	}

	/**
	 * @return the fowardtrackingGroups
	 */
	public Set<GroupModel> getFowardtrackingGroups() {
		return fowardtrackingGroupsSet;
	}

	/**
	 * @param fowardtrackingGroups the fowardtrackingGroups to set
	 */
	public void setFowardtrackingGroups(Set<GroupModel> fowardtrackingGroups) {
		this.fowardtrackingGroupsSet = fowardtrackingGroups;
	}

	/**
	 * @return the backtrackingGoal
	 */
	public List<GoalModel> getBacktrackingGoal() {
		return goalList;
	}

	/**
	 * @param backtrackingGoal the backtrackingGoal to set
	 */
	public void setBacktrackingGoal(List<GoalModel> backtrackingGoal) {
		this.goalList = backtrackingGoal;
	}

	/**
	 * @return the backtrackingHypothesis
	 */
	public List<HypothesisModel> getBacktrackingHypothesis() {
		return hypothesisList;
	}

	/**
	 * @param backtrackingHypothesis the backtrackingHypothesis to set
	 */
	public void setBacktrackingHypothesis(List<HypothesisModel> backtrackingHypothesis) {
		this.hypothesisList = backtrackingHypothesis;
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
	 * @return the backtrackingEntity
	 */
	public List<EntityModel> getBacktrackingEntityList() {
		return entityList;
	}

	/**
	 * @param backtrackingEntity the backtrackingEntity to set
	 */
	public void setBacktrackingEntity(List<EntityModel> backtrackingEntity) {
		this.entityList = backtrackingEntity;
	}

	/**
	 * @return the backtrackingAtribute
	 */
	public List<AttributeModel> getBacktrackingAtribute() {
		return atributeList;
	}


	/**
	 * @param backtrackingAtribute the backtrackingAtribute to set
	 */
	public void setBacktrackingAtribute(List<AttributeModel> backtrackingAtribute) {
		this.atributeList = backtrackingAtribute;
	}

	private String relationshipName;
	/**
	 * @return the relationshipName
	 */
	public String getRelationshipName() {
		return relationshipName;
	}

	/**
	 * @param relationshipName the relationshipName to set
	 */
	public void setRelationshipName(String relationshipName) {
		this.relationshipName = relationshipName;
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
		return this.relationshipName; 
	}
	
}