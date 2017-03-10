package unbbayes.model.umpst.group;

import java.util.ArrayList;
import java.util.List;

import unbbayes.model.umpst.ObjectModel;
import unbbayes.model.umpst.entity.AttributeModel;
import unbbayes.model.umpst.entity.EntityModel;
import unbbayes.model.umpst.entity.RelationshipModel;
import unbbayes.model.umpst.goal.GoalModel;
import unbbayes.model.umpst.goal.HypothesisModel;
import unbbayes.model.umpst.rule.RuleModel;

public class GroupModel extends ObjectModel {
	
	private static final long serialVersionUID = 1L;
	
	List<GoalModel> backtrackingGoal;
	List<HypothesisModel> backtrackingHypothesis;
	List<RuleModel> backtrackingRules;
	
	List<EntityModel> backtrackingEntities;
	List<AttributeModel> backtrackingAtributes;
	List<RelationshipModel> backtrackingRelationship;
	
	public GroupModel(String id,String groupName,
			String comments,
			String author,
			String date){
		
		super(id, groupName, comments, author, date); 
		
		backtrackingGoal = new ArrayList<GoalModel>();
		backtrackingHypothesis = new ArrayList<HypothesisModel>();
		
		backtrackingRules = new ArrayList<RuleModel>();
		
		backtrackingEntities = new ArrayList<EntityModel>();
		backtrackingAtributes = new ArrayList<AttributeModel>();
		backtrackingRelationship = new ArrayList<RelationshipModel>();	
		
		super.setType("Group");
	}
	
	/**
	 * @return the backtrackingEntities
	 */
	public List<EntityModel> getBacktrackingEntities() {
		return backtrackingEntities;
	}

	/**
	 * @return the backtrackingAtributes
	 */
	public List<AttributeModel> getBacktrackingAtributes() {
		return backtrackingAtributes;
	}

	/**
	 * @return the backtrackingRelationship
	 */
	public List<RelationshipModel> getBacktrackingRelationship() {
		return backtrackingRelationship;
	}

	/**
	 * @return the backtrackingRules
	 */
	public List<RuleModel> getBacktrackingRules() {
		return backtrackingRules;
	}

	/**
	 * @return the backtrackingGoal
	 */
	public List<GoalModel> getBacktrackingGoal() {
		return backtrackingGoal;
	}

	/**
	 * @return the backtrackingHypothesis
	 */
	public List<HypothesisModel> getBacktrackingHypothesis() {
		return backtrackingHypothesis;
	}

	
}
