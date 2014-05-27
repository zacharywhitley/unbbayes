package unbbayes.model.umpst.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import unbbayes.model.umpst.ObjectModel;
import unbbayes.model.umpst.goal.GoalModel;
import unbbayes.model.umpst.goal.HypothesisModel;
import unbbayes.model.umpst.group.GroupModel;
import unbbayes.model.umpst.rule.RuleModel;


public class RelationshipModel extends ObjectModel{
	
	private static final long serialVersionUID = 1L;
	
	private List<EntityModel>     entityList;
	
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
		
		super(id, relationshipName, comments, author, date); 
		
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
	 * toString: relationship(EntityA, EntityB)
	 *           relationship(EntityA)
	 *           relationship()
	 *           relationship(EntityA, EntityB, EntityC)
	 *           
	 * Is the name of the relationchip more the names of the entities between 
	 * brackets and separated by virgles. 
	 */
	public String toString(){
		String relationshipName = getName(); 
		
		relationshipName+= "("; 
		
		if(relationshipName != ""){
			if (getBacktrackingEntityList().size() != 0){
				
				for (int i = 0; i < getBacktrackingEntityList().size(); i++){
					relationshipName+= getBacktrackingEntityList().get(i);
					relationshipName+=" , "; 
				}
				relationshipName = relationshipName.subSequence(0, relationshipName.length()-3).toString(); 
				
			}
		}
		
		relationshipName+= ")"; 
		
		return relationshipName;
	}
	
}