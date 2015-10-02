package unbbayes.io.umpst;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import unbbayes.model.umpst.entity.AttributeModel;
import unbbayes.model.umpst.entity.EntityModel;
import unbbayes.model.umpst.entity.RelationshipModel;
import unbbayes.model.umpst.goal.GoalModel;
import unbbayes.model.umpst.goal.HypothesisModel;
import unbbayes.model.umpst.group.GroupModel;
import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.model.umpst.rule.RuleModel;

/**
 * Build hierarchy of nodes according to type node distribution
 * 
 * @author Diego Marques
 */

public class FileBuildNodeHierarchy implements IBuildTypeNodeHierarchy{
	
	private Set<String> keys;
	private Set<String> subKeys;
	private TreeSet<String> sortedKeys;
	private TreeSet<String> subSortedKeys;
	
	private GoalModel goal, subGoal;
	private HypothesisModel hypothesis, subHypothesis;
	private EntityModel entity;
	private AttributeModel attribute, subAttribute;
	private RelationshipModel relationship;
	private RuleModel rule;
	private GroupModel group;
	
	FileBuildNode bn = new FileBuildNode();
	
	public void goalNodeHierarchy(Document doc, Element parent, UMPSTProject umpstProject) {
		Element node = null;
	
		if (umpstProject.getMapGoal().size() > 0) {
			keys = umpstProject.getMapGoal().keySet();		
			sortedKeys = new TreeSet<String>(keys);
			
			bn.setNameNode("goal");
			bn.setNameObject("goalName");
			
			for (String key : sortedKeys) {
				goal = umpstProject.getMapGoal().get(key);
				node = bn.buildNode(doc, parent, goal);
				
				/* Subgoals list */
				if(!goal.getSubgoals().isEmpty()) {
					subKeys = goal.getSubgoals().keySet();
					subSortedKeys = new TreeSet<String>(subKeys);
					
					for (String subKey : subSortedKeys) {
						subGoal = umpstProject.getMapGoal().get(subKey);
						
						Element subgoals = doc.createElement("subgoals");
						subgoals.appendChild(doc.createTextNode(subGoal.getId()));
						node.appendChild(subgoals);
					}					
				}
				
				/* Hypothesis List*/				
				if (!umpstProject.getMapHypothesis().isEmpty()) {
					subKeys = umpstProject.getMapHypothesis().keySet();
					subSortedKeys = new TreeSet<String>(subKeys);
					
					for (String subKey : subSortedKeys) {
						hypothesis = umpstProject.getMapHypothesis().get(subKey);
						
						if (hypothesis.getGoalRelated().contains(goal)) {
							Element hypothesisList = doc.createElement("hypothesisList");
							hypothesisList.appendChild(doc.createTextNode(hypothesis.getId()));
							node.appendChild(hypothesisList);
						}
					}
				}
				
				/* Goal father */
//				if (!goal.getGoalsRelated().isEmpty()) {
//					Element goalFather = doc.createElement("father");
//					goalFather.appendChild(doc.createTextNode(goal.getGoalFather().getId()));
//					node.appendChild(goalFather);
//					
//				}				
				parent.appendChild(node);
			}
		}		
	}	
	
	public void hypothesisNodeHierarchy(Document doc, Element parent, UMPSTProject umpstProject) {
		Element node = null;
		
		if (umpstProject.getMapHypothesis().size() > 0) {
			keys = umpstProject.getMapHypothesis().keySet();
			sortedKeys = new TreeSet<String>(keys);
					
			bn.setNameNode("hypothesis");
			bn.setNameObject("hypothesisName");
			
			for(String key : sortedKeys) {
				hypothesis = umpstProject.getMapHypothesis().get(key);
				node = bn.buildNode(doc, parent, hypothesis);
				
				/* Goals related to hypothesis */
				subKeys = umpstProject.getMapGoal().keySet();
				subSortedKeys = new TreeSet<String>(subKeys);
				
				for (String subKey : subSortedKeys) {
					goal = umpstProject.getMapGoal().get(subKey);
					
					if (hypothesis.getGoalRelated().contains(goal)) {
						Element goalRelatedList = doc.createElement("goalRelated");
						goalRelatedList.appendChild(doc.createTextNode(goal.getId()));
						node.appendChild(goalRelatedList);
					}
				}
				
				/* Sub hypothesis list*/
				if (!hypothesis.getMapSubHypothesis().isEmpty()) {
					subKeys = hypothesis.getMapSubHypothesis().keySet();
					subSortedKeys = new TreeSet<String>(subKeys);
					
					for (String subKey : subSortedKeys) {
						subHypothesis = umpstProject.getMapHypothesis().get(subKey);
						
						Element subHypothesisList = doc.createElement("hypothesisList");
						subHypothesisList.appendChild(doc.createTextNode(subHypothesis.getId()));
						node.appendChild(subHypothesisList);
					}					
				}
				parent.appendChild(node);
			}
		}
	}
		
	public void entityNodeHierarchy(Document doc, Element parent, UMPSTProject umpstProject) {		
		Element node = null;
		EntityModel entity;

		if (umpstProject.getMapEntity().size() > 0) {
			keys = umpstProject.getMapEntity().keySet();
			sortedKeys = new TreeSet<String>(keys);
			
			for (String key : sortedKeys) {
				bn.setNameNode("entity");
				bn.setNameObject("entityName");

				entity = umpstProject.getMapEntity().get(key);
				node = bn.buildNode(doc, parent, entity);				
				parent.appendChild(node);
	
				/* verify node dependencies */
//				buildAttributesDependency(umpstProject, doc, node, entity);
				
				/* Attributes List */
				if (!umpstProject.getMapAtribute().isEmpty()) {
					subKeys = umpstProject.getMapAtribute().keySet();
					subSortedKeys = new TreeSet<String>(subKeys);
					
					for (String subKey : subSortedKeys) {
						attribute = umpstProject.getMapAtribute().get(subKey);
						
						if (attribute.getEntityRelated().contains(entity)) {
							Element attributeList = doc.createElement("atributesList");
							attributeList.appendChild(doc.createTextNode(attribute.getId()));
							node.appendChild(attributeList);
						}
					}
				}
				
				/* backtracking goals */
				if(!entity.getBacktrackingGoalList().isEmpty()) {
					java.util.List<GoalModel> listGoals = entity.getBacktrackingGoalList();
					
					for(GoalModel elem : listGoals) {
						Element goalId = doc.createElement("backtrackingGoalsList");
						goalId.appendChild(doc.createTextNode(elem.getId()));
						node.appendChild(goalId);
					}					
				}
				
				/* backtracking hypothesis */
				if(!entity.getBacktrackingHypothesis().isEmpty()) {
					java.util.List<HypothesisModel> listHypothesis = entity.getBacktrackingHypothesis();
					
					for(HypothesisModel elem : listHypothesis) {
						Element hipothesisId = doc.createElement("backtrackingHypothesisList");
						hipothesisId.appendChild(doc.createTextNode(elem.getId()));
						node.appendChild(hipothesisId);
					}					
				}
			}
		}		
	}
	
	public void attributeNodeHierarchy(Document doc, Element parent, UMPSTProject umpstProject) {
		Element node = null;
		
		if (umpstProject.getMapAtribute().size() > 0) {
			keys = umpstProject.getMapAtribute().keySet();
			sortedKeys = new TreeSet<String>(keys);
			
			bn.setNameNode("attribute");
			bn.setNameObject("atributeName");
			
			for(String key : sortedKeys) {
				attribute = umpstProject.getMapAtribute().get(key);
				node = bn.buildNode(doc, parent, attribute);
				
				/* Entity related to attribute */
				subKeys = umpstProject.getMapEntity().keySet();
				subSortedKeys = new TreeSet<String>(subKeys);
				
				for (String subKey : subSortedKeys) {
					entity = umpstProject.getMapEntity().get(subKey);
					
					if (attribute.getEntityRelated().contains(entity)) {
						Element entityRelatedList = doc.createElement("entityRelated");
						entityRelatedList.appendChild(doc.createTextNode(entity.getId()));
						node.appendChild(entityRelatedList);
					}
				}
				
				/* Sub attribute list*/
				if (!attribute.getMapSubAtributes().isEmpty()) {
					subKeys = attribute.getMapSubAtributes().keySet();
					subSortedKeys = new TreeSet<String>(subKeys);
					
					for (String subKey : subSortedKeys) {
						subAttribute = umpstProject.getMapAtribute().get(subKey);
						
						Element subAttributeList = doc.createElement("subAtributesList");
						subAttributeList.appendChild(doc.createTextNode(subAttribute.getId()));
						node.appendChild(subAttributeList);
					}					
				}
				parent.appendChild(node);
			}
		}
	}
	
	
	public void relationshipNodeHierarchy(Document doc, Element parent, UMPSTProject umpstProject) {		
		Element node = null;
		
		keys = umpstProject.getMapRelationship().keySet();
		sortedKeys = new TreeSet<String>(keys);

		if (umpstProject.getMapRelationship().size() > 0) {
			for (String key : sortedKeys) {
				bn.setNameNode("relationship");
				bn.setNameObject("relationshipName");

				relationship = umpstProject.getMapRelationship().get(key);
				node = bn.buildNode(doc, parent, relationship);				
				parent.appendChild(node);
				
				/* backtracking entity */
				if(!relationship.getEntityList().isEmpty()) {
					List<EntityModel> listEntities = relationship.getEntityList();
					
					for(EntityModel elem : listEntities) {
						Element entityId = doc.createElement("backtrackingEntitiesList");
						entityId.appendChild(doc.createTextNode(elem.getId()));
						node.appendChild(entityId);
					}					
				}	
				
				/* backtracking goals */
				if(!relationship.getBacktrackingGoal().isEmpty()) {
					List<GoalModel> listGoals = relationship.getBacktrackingGoal();
					
					for(GoalModel elem : listGoals) {
						Element goalId = doc.createElement("backtrackingGoalsList");
						goalId.appendChild(doc.createTextNode(elem.getId()));
						node.appendChild(goalId);
					}					
				}
				
				/* backtracking hypothesis */
				if(!relationship.getBacktrackingHypothesis().isEmpty()) {
					List<HypothesisModel> listHypothesis = relationship.getBacktrackingHypothesis();
					
					for(HypothesisModel elem : listHypothesis) {
						Element hypothesislId = doc.createElement("backtrackingHypothesisList");
						hypothesislId.appendChild(doc.createTextNode(elem.getId()));
						node.appendChild(hypothesislId);
					}					
				}
			}
		}		
	}
	
	public void ruleNodeHierarchy(Document doc, Element parent, UMPSTProject umpstProject) {		
		Element node = null;
		
		keys = umpstProject.getMapRules().keySet();
		sortedKeys = new TreeSet<String>(keys);

		if (umpstProject.getMapRules().size() > 0) {
			for (String key : sortedKeys) {
				bn.setNameNode("rule");
				bn.setNameObject("rulesName");

				rule = umpstProject.getMapRules().get(key);
				node = bn.buildNode(doc, parent, rule);
				
				Element ruleType = doc.createElement("ruleType");
				ruleType.appendChild(doc.createTextNode(rule.getRuleType()));
				node.appendChild(ruleType);
				
				parent.appendChild(node);
				
				/* Entity List */
				if(!rule.getEntityList().isEmpty()) {
					List<EntityModel> listEntities = rule.getEntityList();
					
					for(EntityModel elem : listEntities) {
						Element entityId = doc.createElement("entityList");
						entityId.appendChild(doc.createTextNode(elem.getId()));
						node.appendChild(entityId);
					}					
				}
				
				/* Attribute List */
				if(!rule.getAttributeList().isEmpty()) {
					List<AttributeModel> listAttributes = rule.getAttributeList();
					
					for(AttributeModel elem : listAttributes) {
						Element attributeId = doc.createElement("attributeList");
						attributeId.appendChild(doc.createTextNode(elem.getId()));
						node.appendChild(attributeId);
					}					
				}
				
				/* Relationship List */
				if(!rule.getRelationshipList().isEmpty()) {
					List<RelationshipModel> listRelationship = rule.getRelationshipList();
					
					for(RelationshipModel elem : listRelationship) {
						Element relationshipId = doc.createElement("relationshipList");
						relationshipId.appendChild(doc.createTextNode(elem.getId()));
						node.appendChild(relationshipId);
					}					
				}
				
				/* Group List */
				if(!rule.getGroupList().isEmpty()) {
					List<GroupModel> listGroup = rule.getGroupList();
					
					for(GroupModel elem : listGroup) {
						Element groupId = doc.createElement("groupList");
						groupId.appendChild(doc.createTextNode(elem.getId()));
						node.appendChild(groupId);
					}					
				}
				
				/* Children rule List */
				if(!rule.getChildrenRuleList().isEmpty()) {
					List<RuleModel> listRuleChildren = rule.getChildrenRuleList();
					
					for(RuleModel elem : listRuleChildren) {
						Element ruleChildrenId = doc.createElement("childrenRuleList");
						ruleChildrenId.appendChild(doc.createTextNode(elem.getId()));
						node.appendChild(ruleChildrenId);
					}					
				}
				
				/* backtracking goals */
				if(!rule.getBacktrackingGoalList().isEmpty()) {
					List<GoalModel> listGoals = rule.getBacktrackingGoalList();
					
					for(GoalModel elem : listGoals) {
						Element goalId = doc.createElement("backtrackingGoalsList");
						goalId.appendChild(doc.createTextNode(elem.getId()));
						node.appendChild(goalId);
					}					
				}
				
				/* backtracking hypothesis */
				if(!rule.getBacktrackingHypothesis().isEmpty()) {
					List<HypothesisModel> listHypothesis = rule.getBacktrackingHypothesis();
					
					for(HypothesisModel elem : listHypothesis) {
						Element hypothesisId = doc.createElement("backtrackingHypothesisList");
						hypothesisId.appendChild(doc.createTextNode(elem.getId()));
						node.appendChild(hypothesisId);
					}					
				}
			}
		}		
	}
	
	public void groupNodeHierarchy(Document doc, Element parent, UMPSTProject umpstProject) {		
		Element node = null;
		
		keys = umpstProject.getMapGroups().keySet();
		sortedKeys = new TreeSet<String>(keys);

		if (umpstProject.getMapGroups().size() > 0) {
			for (String key : sortedKeys) {
				bn.setNameNode("group");
				bn.setNameObject("groupName");

				group = umpstProject.getMapGroups().get(key);
				node = bn.buildNode(doc, parent, group);
				parent.appendChild(node);
				
				/* backtracking goals */
				if(!group.getBacktrackingGoal().isEmpty()) {
					List<GoalModel> listGoals = group.getBacktrackingGoal();
					
					for(GoalModel elem : listGoals) {
						Element goalId = doc.createElement("backtrackingGoalsList");
						goalId.appendChild(doc.createTextNode(elem.getId()));
						node.appendChild(goalId);
					}					
				}
				
				/* backtracking hypothesis */
				if(!group.getBacktrackingHypothesis().isEmpty()) {
					List<HypothesisModel> listHypothesis = group.getBacktrackingHypothesis();
					
					for(HypothesisModel elem : listHypothesis) {
						Element hypothesisId = doc.createElement("backtrackingHypothesisList");
						hypothesisId.appendChild(doc.createTextNode(elem.getId()));
						node.appendChild(hypothesisId);
					}					
				}
				
				/* backtracking rules */
				if(!group.getBacktrackingRules().isEmpty()) {
					List<RuleModel> listRules = group.getBacktrackingRules();
					
					for(RuleModel elem : listRules) {
						Element ruleId = doc.createElement("backtrackingRulesList");
						ruleId.appendChild(doc.createTextNode(elem.getId()));
						node.appendChild(ruleId);
					}					
				}
				
				/* backtracking entities */
				if(!group.getBacktrackingEntities().isEmpty()) {
					List<EntityModel> listEntity = group.getBacktrackingEntities();
					
					for(EntityModel elem : listEntity) {
						Element entityId = doc.createElement("backtrackingEntitiesList");
						entityId.appendChild(doc.createTextNode(elem.getId()));
						node.appendChild(entityId);
					}					
				}
				
				/* backtracking attributes */
				if(!group.getBacktrackingAtributes().isEmpty()) {
					List<AttributeModel> listAttributes = group.getBacktrackingAtributes();
					
					for(AttributeModel elem : listAttributes) {
						Element attributeId = doc.createElement("backtrackingAttributesList");
						attributeId.appendChild(doc.createTextNode(elem.getId()));
						node.appendChild(attributeId);
					}					
				}
				
				/* backtracking relationship */
				if(!group.getBacktrackingRelationship().isEmpty()) {
					List<RelationshipModel> listRelationship = group.getBacktrackingRelationship();
					
					for(RelationshipModel elem : listRelationship) {
						Element relationshipId = doc.createElement("backtrackingRelationshipList");
						relationshipId.appendChild(doc.createTextNode(elem.getId()));
						node.appendChild(relationshipId);
					}					
				}
			}
		}		
	}
}