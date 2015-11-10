package unbbayes.model.umpst.rule;

import java.util.ArrayList;
import java.util.List;

import unbbayes.model.umpst.ObjectModel;
import unbbayes.model.umpst.entity.AttributeModel;
import unbbayes.model.umpst.entity.EntityModel;
import unbbayes.model.umpst.entity.RelationshipModel;
import unbbayes.model.umpst.goal.GoalModel;
import unbbayes.model.umpst.goal.HypothesisModel;
import unbbayes.model.umpst.group.GroupModel;


public class RuleModel extends ObjectModel{
	
	private static final long serialVersionUID = 1L;
	
	private String ruleType;
	
	private List<EntityModel>       entityList;
	private List<AttributeModel>    attributeList;
	private List<RelationshipModel> relationshipList;	
	
	private List<RuleModel>          fatherRuleList;	// Rule that appear at traceability painel
	private List<RuleModel>          childrenRuleList; 
	private List<GroupModel>         groupList;

	private List<GoalModel> backtrackingGoalsList;
	private List<HypothesisModel> backtrackingHypothesisList;
	
	public RuleModel(String id,
			String rulesName,
			String ruleType, 
			String comments,
			String author, 
			String date) {
		
		super(id, rulesName, comments, author, date); 
		
		this.ruleType = ruleType;
		
		entityList       = new ArrayList<EntityModel>(); 
		attributeList    = new ArrayList<AttributeModel>(); 
		relationshipList = new ArrayList<RelationshipModel>(); 
		groupList        = new ArrayList<GroupModel>(); 
		fatherRuleList   = new ArrayList<RuleModel>();
		childrenRuleList = new ArrayList<RuleModel>(); 
		
		backtrackingGoalsList = new ArrayList<GoalModel>();
		backtrackingHypothesisList = new ArrayList<HypothesisModel>();
		
		super.setType("Rule");
	}

	public List<EntityModel> getEntityList() {
		return entityList;
	}

	public List<AttributeModel> getAttributeList() {
		return attributeList;
	}

	public List<RelationshipModel> getRelationshipList() {
		return relationshipList;
	}

	public List<GroupModel> getGroupList() {
		if(groupList == null){
			groupList = new ArrayList<GroupModel>(); 
		}
		return groupList;
	}
	
	public List<RuleModel> getFatherRuleList() {
		if(fatherRuleList == null){
			fatherRuleList = new ArrayList<RuleModel>(); 
		}
		return fatherRuleList;
	}
	
	public List<RuleModel> getChildrenRuleList() {
		if(childrenRuleList == null){
			childrenRuleList = new ArrayList<RuleModel>(); 
		}
		return childrenRuleList;
	}
	
    public void addBacktrackingEntity(EntityModel entity){
		this.entityList.add(entity); 
	}
    
    public void removeBacktrackingEntity(EntityModel entity){
    	this.entityList.remove(entity); 
    }
	
    public void addBacktrackingAttibute(AttributeModel attribute){
		this.attributeList.add(attribute); 
	}
    
    public void removeBacktrackingAttibute(AttributeModel attribute){
    	this.attributeList.remove(attribute); 
    }
    
    public void addBacktrackingRelationship(RelationshipModel relationship){
		this.relationshipList.add(relationship); 
	}
    
    public void removeBacktrackingRelationship(RelationshipModel relationship){
    	this.relationshipList.remove(relationship); 
    }
    
    public void addGroup(GroupModel group){
		this.groupList.add(group); 
	}
    
    public void removeGroup(GroupModel group){
    	this.groupList.remove(group); 
    }
    
	
	public List<GoalModel> getBacktrackingGoalList() {
		return backtrackingGoalsList;
	}

	public void setBacktrackingGoalsList(List<GoalModel> backtrackingGoalList) {
		this.backtrackingGoalsList = backtrackingGoalsList;
	}

	public List<HypothesisModel> getBacktrackingHypothesis() {
		return backtrackingHypothesisList;
	}

	public void setBacktrackingHypothesis(
			List<HypothesisModel> backtrackingHypothesisList) {
		this.backtrackingHypothesisList = backtrackingHypothesisList;
	}

	/**
	 * @return the ruleType
	 */
	public String getRuleType() {
		return ruleType;
	}

	/**
	 * @param ruleType the ruleType to set
	 */
	public void setRuleType(String ruleType) {
		this.ruleType = ruleType;
	}

}
