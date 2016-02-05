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
import unbbayes.model.umpst.implementation.CauseVariableModel;
import unbbayes.model.umpst.implementation.EffectVariableModel;
import unbbayes.model.umpst.implementation.EventVariableObjectModel;
import unbbayes.model.umpst.implementation.NecessaryConditionVariableModel;
import unbbayes.model.umpst.implementation.OrdinaryVariableModel;


public class RuleModel extends ObjectModel{
	
	private static final long serialVersionUID = 1L;
	
	private String ruleType;
	
	private List<EntityModel>       entityList;
	private List<AttributeModel>    attributeList;
	private List<RelationshipModel> relationshipList;	
	
	private List<RuleModel>          fatherRuleList;	// Rule that appears at traceability painel
	private List<RuleModel>          childrenRuleList; 
	private List<GroupModel>         groupList;

	private List<GoalModel> backtrackingGoalsList;
	private List<HypothesisModel> backtrackingHypothesisList;
	
	private List<OrdinaryVariableModel> ordinaryVariableList;
	private List<CauseVariableModel> causeVariableList;
	private List<EffectVariableModel> effectVariableList;
	private List<NecessaryConditionVariableModel> necessaryConditionList;
	private List<EventVariableObjectModel> eventVariableObjectList;
	
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
		
		ordinaryVariableList = new ArrayList<OrdinaryVariableModel>();
		causeVariableList = new ArrayList<CauseVariableModel>();
		effectVariableList = new ArrayList<EffectVariableModel>();
		necessaryConditionList = new ArrayList<NecessaryConditionVariableModel>();
		eventVariableObjectList = new ArrayList<EventVariableObjectModel>();
		
		super.setType("Rule");
	}
	
	public void changeEventVariableObject(CauseVariableModel causeVariable) {
		for (int i = 0; i < getEventVariableObjectList().size(); i++) {
			if (causeVariable.getId().equals(getEventVariableObjectList().get(i).getId())) {
				getEventVariableObjectList().get(i).setRelationshipModel(causeVariable.getRelationshipModel());
				getEventVariableObjectList().get(i).setRelationship(causeVariable.getRelationship());
				getEventVariableObjectList().get(i).setArgumentList(causeVariable.getArgumentList());
				break;
			}
		}
	}
	
	public void changeEventVariableObject(EffectVariableModel effectVariable) {
		for (int i = 0; i < getEventVariableObjectList().size(); i++) {
			if (effectVariable.getId().equals(getEventVariableObjectList().get(i).getId())) {
				getEventVariableObjectList().get(i).setRelationshipModel(effectVariable.getRelationshipModel());
				getEventVariableObjectList().get(i).setRelationship(effectVariable.getRelationship());
				getEventVariableObjectList().get(i).setArgumentList(effectVariable.getArgumentList());
				break;
			}
		}
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

	/**
	 * @return the ordinaryVariableList
	 */
	public List<OrdinaryVariableModel> getOrdinaryVariableList() {
		return ordinaryVariableList;
	}

	/**
	 * @param ordinaryVariableList the ordinaryVariableList to set
	 */
	public void setOrdinaryVariableList(List<OrdinaryVariableModel> ordinaryVariableList) {
		this.ordinaryVariableList = ordinaryVariableList;
	}

	/**
	 * @return the causeVariableList
	 */
	public List<CauseVariableModel> getCauseVariableList() {
		return causeVariableList;
	}

	/**
	 * @param causeVariableList the causeVariableList to set
	 */
	public void setCauseVariableList(List<CauseVariableModel> causeVariableList) {
		this.causeVariableList = causeVariableList;
	}

	/**
	 * @return the effectVariableList
	 */
	public List<EffectVariableModel> getEffectVariableList() {
		return effectVariableList;
	}

	/**
	 * @param effectVariableList the effectVariableList to set
	 */
	public void setEffectVariableList(List<EffectVariableModel> effectVariableList) {
		this.effectVariableList = effectVariableList;
	}

	/**
	 * @return the eventVariableObjectList
	 */
	public List<EventVariableObjectModel> getEventVariableObjectList() {
		return eventVariableObjectList;
	}

	/**
	 * @param eventVariableObjectList the eventVariableObjectList to set
	 */
	public void setEventVariableObjectList(List<EventVariableObjectModel> eventVariableObjectList) {
		this.eventVariableObjectList = eventVariableObjectList;
	}

	/**
	 * @return the necessaryConditionList
	 */
	public List<NecessaryConditionVariableModel> getNecessaryConditionList() {
		return necessaryConditionList;
	}

	/**
	 * @param necessaryConditionList the necessaryConditionList to set
	 */
	public void setNecessaryConditionList(List<NecessaryConditionVariableModel> necessaryConditionList) {
		this.necessaryConditionList = necessaryConditionList;
	}	
}
