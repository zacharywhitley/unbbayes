package unbbayes.model.umpst.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import unbbayes.model.umpst.ObjectModel;
import unbbayes.model.umpst.entities.AttributeModel;
import unbbayes.model.umpst.entities.EntityModel;
import unbbayes.model.umpst.entities.RelationshipModel;
import unbbayes.model.umpst.groups.GroupModel;


public class RuleModel extends ObjectModel{
	
	private static final long serialVersionUID = 1L;
	
	private String ruleType;
	
	private List<EntityModel>       entityList;
	private List<AttributeModel>    attributeList;
	private List<RelationshipModel> relationshipList;	
	private Set<GroupModel>         groupSet;
	
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
		groupSet         = new HashSet<GroupModel>(); 
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

	public Set<GroupModel> getGroupSet() {
		return groupSet;
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
		this.groupSet.add(group); 
	}
    
    public void removeGroup(GroupModel group){
    	this.groupSet.remove(group); 
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
