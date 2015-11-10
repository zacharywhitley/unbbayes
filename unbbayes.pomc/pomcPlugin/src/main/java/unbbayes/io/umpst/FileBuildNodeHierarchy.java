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

public class FileBuildNodeHierarchy implements IBuildTypeNodeHierarchy {
	
	private Set<String> keys;
	private Set<String> subKeys, _subKeys;
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
//			bn.setNameObject("goalName");
			bn.setIdNode("goalId");
			bn.setNameObject("name");
			
			for (String key : sortedKeys) {
				goal = umpstProject.getMapGoal().get(key);
				node = bn.buildNode(doc, parent, goal);
				
				/* Subgoals list */
				if(!goal.getSubgoals().isEmpty()) {
					subKeys = goal.getSubgoals().keySet();
					subSortedKeys = new TreeSet<String>(subKeys);
					
					Element subgoals = doc.createElement("subgoals");
					node.appendChild(subgoals);
					for (String subKey : subSortedKeys) {
						subGoal = umpstProject.getMapGoal().get(subKey);
						
//						Element subgoals = doc.createElement("subgoals");
						Element goalId = doc.createElement("goalId");
						goalId.appendChild(doc.createTextNode(subGoal.getId()));
						subgoals.appendChild(goalId);
//						goalId.appendChild(goalId);
					}
				}
				
				/* Hypothesis List*/				
				if (!goal.getMapHypothesis().isEmpty()) {
					subKeys = goal.getMapHypothesis().keySet();
					subSortedKeys = new TreeSet<String>(subKeys);
					
					Element hypothesisList = doc.createElement("hypothesisList");
					node.appendChild(hypothesisList);
					for (String subKey : subSortedKeys) {
//						hypothesis = umpstProject.getMapHypothesis().get(subKey);
						hypothesis = goal.getMapHypothesis().get(subKey);
						
//						if (hypothesis.getGoalRelated().contains(goal)) {
//							Element hypothesisList = doc.createElement("hypothesisList");
							Element hypothesisId = doc.createElement("hypothesisId");
							hypothesisId.appendChild(doc.createTextNode(hypothesis.getId()));
							hypothesisList.appendChild(hypothesisId);
//							node.appendChild(hypothesisId);
//						}
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
//			bn.setNameObject("hypothesisName");
			bn.setIdNode("hypothesisId");
			bn.setNameObject("name");
			
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
					
					Element subHypothesisElem = doc.createElement("subHypothesis");
					node.appendChild(subHypothesisElem);					
					for (String subKey : subSortedKeys) {
						subHypothesis = umpstProject.getMapHypothesis().get(subKey);
						
//						Element subHypothesisList = doc.createElement("hypothesisList");
						Element subHypothesisId = doc.createElement("hypothesisId");
						subHypothesisId.appendChild(doc.createTextNode(subHypothesis.getId()));
						subHypothesisElem.appendChild(subHypothesisId);
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
				bn.setIdNode("entityId");
				bn.setNameObject("name");

				entity = umpstProject.getMapEntity().get(key);
				node = bn.buildNode(doc, parent, entity);				
				parent.appendChild(node);
	
				/* verify node dependencies 
				 * OBS. This format does not need attribute list node
				 * */
//				buildAttributeDependency(entity, doc, node);
				
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

	/* Verify entity dependencies and build attribute nodes */
	/* TODO Save in format below
	 * 		<entity>
	 * 			<attribute>
	 * 
	 * */
//	public void buildAttributeDependency(EntityModel entity, Document doc, Element node) {
//		Element nodeDependencyFather = null;
//		Element nodeDependency = null;
//		AttributeModel attribute;
//		
//		if (!entity.getMapAtributes().isEmpty()) {		
//			keys = entity.getMapAtributes().keySet();
//			sortedKeys = new TreeSet<String>(keys);
//					
//			bn.setNameNode("attribute");
//			bn.setIdNode("attributeId");
//			bn.setNameObject("name");
//			
//			for(String key : sortedKeys) {
//				attribute = entity.getMapAtributes().get(key);				
//				nodeDependency = bn.buildNode(doc, node, attribute);
//				if(attribute.getFather() != null) {
//					nodeDependencyFather = bn.getNodeFather();
//					nodeDependencyFather.appendChild(nodeDependency);
//				} else {
//					node.appendChild(nodeDependency);
//				}
//				bn.setNodeFather(nodeDependency);				
//			}
//		}
//	}
	
	public void attributeNodeHierarchy(Document doc, Element parent, UMPSTProject umpstProject) {
		Element node = null;
		
		if (umpstProject.getMapAtribute().size() > 0) {
			keys = umpstProject.getMapAtribute().keySet();
			sortedKeys = new TreeSet<String>(keys);
			
			bn.setNameNode("attribute");
//			bn.setNameObject("attributeName");
			bn.setIdNode("attributeId");
			bn.setNameObject("name");
			
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
//				bn.setNameObject("relationshipName");
				bn.setIdNode("relationshipId");
				bn.setNameObject("name");

				relationship = umpstProject.getMapRelationship().get(key);
				node = bn.buildNode(doc, parent, relationship);				
				parent.appendChild(node);
				
				/* backtracking entity */
				if(!relationship.getEntityList().isEmpty()) {
					List<EntityModel> listEntities = relationship.getEntityList();
					
					Element btEntityElem = doc.createElement("backtrackingEntitiesList");
					node.appendChild(btEntityElem);
					for(EntityModel elem : listEntities) {
//						Element entityId = doc.createElement("backtrackingEntitiesList");
						Element entityId = doc.createElement("entityId");
						entityId.appendChild(doc.createTextNode(elem.getId()));
						btEntityElem.appendChild(entityId);
//						node.appendChild(entityId);
					}					
				}	
				
				/* backtracking goals */
				if(!relationship.getBacktrackingGoal().isEmpty()) {
					List<GoalModel> listGoals = relationship.getBacktrackingGoal();
					
					Element btGoalElem = doc.createElement("backtrackingGoalsList");
					node.appendChild(btGoalElem);
					for(GoalModel elem : listGoals) {
//						Element goalId = doc.createElement("backtrackingGoalsList");
						Element goalId = doc.createElement("goalId");
						goalId.appendChild(doc.createTextNode(elem.getId()));
						btGoalElem.appendChild(goalId);
					}					
				}
				
				/* backtracking hypothesis */
				if(!relationship.getBacktrackingHypothesis().isEmpty()) {
					List<HypothesisModel> listHypothesis = relationship.getBacktrackingHypothesis();
					
					Element btHypothesisElem = doc.createElement("backtrackingHypothesisList");
					node.appendChild(btHypothesisElem);
					for(HypothesisModel elem : listHypothesis) {
//						Element hypothesislId = doc.createElement("backtrackingHypothesisList");
						Element hypothesislId = doc.createElement("hypothesisId");
						hypothesislId.appendChild(doc.createTextNode(elem.getId()));
						btHypothesisElem.appendChild(hypothesislId);
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
//				bn.setNameObject("rulesName");
				bn.setIdNode("ruleId");
				bn.setNameObject("name");

				rule = umpstProject.getMapRules().get(key);
				node = bn.buildNode(doc, parent, rule);
				
				Element ruleType = doc.createElement("ruleType");
				ruleType.appendChild(doc.createTextNode(rule.getRuleType()));
				node.appendChild(ruleType);
				
				parent.appendChild(node);
				
				/* Entity List */
				if(!rule.getEntityList().isEmpty()) {
					List<EntityModel> listEntities = rule.getEntityList();
					
					Element entityListElem = doc.createElement("entityList");
					node.appendChild(entityListElem);
					for(EntityModel elem : listEntities) {
						Element entityId = doc.createElement("entityId");
						entityId.appendChild(doc.createTextNode(elem.getId()));
						entityListElem.appendChild(entityId);
//						node.appendChild(entityId);
					}					
				}
				
				/* Attribute List */
				if(!rule.getAttributeList().isEmpty()) {
					List<AttributeModel> listAttributes = rule.getAttributeList();
					
					Element attributeListElem = doc.createElement("attributeList");
					node.appendChild(attributeListElem);
					for(AttributeModel elem : listAttributes) {
						Element attributeId = doc.createElement("attributeId");
						attributeId.appendChild(doc.createTextNode(elem.getId()));
						attributeListElem.appendChild(attributeId);
//						node.appendChild(attributeId);
					}					
				}
				
				/* Relationship List */
				if(!rule.getRelationshipList().isEmpty()) {
					List<RelationshipModel> listRelationship = rule.getRelationshipList();
					
					Element relationshipListElem = doc.createElement("relationshipList");
					node.appendChild(relationshipListElem);
					for(RelationshipModel elem : listRelationship) {
						Element relationshipId = doc.createElement("relationshipId");
						relationshipId.appendChild(doc.createTextNode(elem.getId()));
						relationshipListElem.appendChild(relationshipId);
//						node.appendChild(relationshipId);
					}					
				}
				
				/* Group List */
				if(!rule.getGroupList().isEmpty()) {
					List<GroupModel> listGroup = rule.getGroupList();
					
					Element groupListElem = doc.createElement("groupList");
					node.appendChild(groupListElem);
					for(GroupModel elem : listGroup) {
						Element groupId = doc.createElement("groupId");
						groupId.appendChild(doc.createTextNode(elem.getId()));
						groupListElem.appendChild(groupId);
					}					
				}
				
				/* Children rule List */
				if(!rule.getChildrenRuleList().isEmpty()) {
					List<RuleModel> listRuleChildren = rule.getChildrenRuleList();
					
					Element chListElem = doc.createElement("childrenRuleList");
					node.appendChild(chListElem);
					for(RuleModel elem : listRuleChildren) {
						Element ruleChildrenId = doc.createElement("ruleId");
						ruleChildrenId.appendChild(doc.createTextNode(elem.getId()));
						chListElem.appendChild(ruleChildrenId);
					}					
				}
				
				/* backtracking goals */
				if(!rule.getBacktrackingGoalList().isEmpty()) {
					List<GoalModel> listGoals = rule.getBacktrackingGoalList();
					
					Element btGoalElem = doc.createElement("backtrackingGoalsList");
					node.appendChild(btGoalElem);
					for(GoalModel elem : listGoals) {
						Element goalId = doc.createElement("goalId");
						goalId.appendChild(doc.createTextNode(elem.getId()));
						btGoalElem.appendChild(goalId);
					}					
				}
				
				/* backtracking hypothesis */
				if(!rule.getBacktrackingHypothesis().isEmpty()) {
					List<HypothesisModel> listHypothesis = rule.getBacktrackingHypothesis();
					
					Element btHypothesisElem = doc.createElement("backtrackingHypothesisList");
					node.appendChild(btHypothesisElem);
					for(HypothesisModel elem : listHypothesis) {
						Element hypothesisId = doc.createElement("hypothesisId");
						hypothesisId.appendChild(doc.createTextNode(elem.getId()));
						btHypothesisElem.appendChild(hypothesisId);
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
//				bn.setNameObject("groupName");
				bn.setIdNode("groupId");
				bn.setNameObject("name");

				group = umpstProject.getMapGroups().get(key);
				node = bn.buildNode(doc, parent, group);
				parent.appendChild(node);
				
				/* backtracking goals */
				if(!group.getBacktrackingGoal().isEmpty()) {
					List<GoalModel> listGoals = group.getBacktrackingGoal();
					
					Element btGoalElem = doc.createElement("backtrackingGoalsList");
					node.appendChild(btGoalElem);
					for(GoalModel elem : listGoals) {
						Element goalId = doc.createElement("goalId");
						goalId.appendChild(doc.createTextNode(elem.getId()));
						btGoalElem.appendChild(goalId);
					}					
				}
				
				/* backtracking hypothesis */
				if(!group.getBacktrackingHypothesis().isEmpty()) {
					List<HypothesisModel> listHypothesis = group.getBacktrackingHypothesis();
					
					Element btHypothesisElem = doc.createElement("backtrackingHypothesisList");
					node.appendChild(btHypothesisElem);
					for(HypothesisModel elem : listHypothesis) {
						Element hypothesisId = doc.createElement("hypothesisId");
						hypothesisId.appendChild(doc.createTextNode(elem.getId()));
						btHypothesisElem.appendChild(hypothesisId);
					}					
				}
				
				/* backtracking rules */
				if(!group.getBacktrackingRules().isEmpty()) {
					List<RuleModel> listRules = group.getBacktrackingRules();
					
					Element btRuleElem = doc.createElement("backtrackingRulesList");
					node.appendChild(btRuleElem);
					for(RuleModel elem : listRules) {
						Element ruleId = doc.createElement("ruleId");
						ruleId.appendChild(doc.createTextNode(elem.getId()));
						btRuleElem.appendChild(ruleId);
					}					
				}
				
				/* backtracking entities */
				if(!group.getBacktrackingEntities().isEmpty()) {
					List<EntityModel> listEntity = group.getBacktrackingEntities();
					
					Element btEntityElem = doc.createElement("backtrackingEntitiesList");
					node.appendChild(btEntityElem);
					for(EntityModel elem : listEntity) {
						Element entityId = doc.createElement("entityId");
						entityId.appendChild(doc.createTextNode(elem.getId()));
						btEntityElem.appendChild(entityId);
					}					
				}
				
				/* backtracking attributes */
				if(!group.getBacktrackingAtributes().isEmpty()) {
					List<AttributeModel> listAttributes = group.getBacktrackingAtributes();
					
					Element btAttributeElem = doc.createElement("backtrackingAttributesList");
					node.appendChild(btAttributeElem);
					for(AttributeModel elem : listAttributes) {
						Element attributeId = doc.createElement("attributeId");
						attributeId.appendChild(doc.createTextNode(elem.getId()));
						btAttributeElem.appendChild(attributeId);
					}					
				}
				
				/* backtracking relationship */
				if(!group.getBacktrackingRelationship().isEmpty()) {
					List<RelationshipModel> listRelationship = group.getBacktrackingRelationship();
					
					Element btRelationshipElem = doc.createElement("backtrackingRelationshipList");
					node.appendChild(btRelationshipElem);
					for(RelationshipModel elem : listRelationship) {
						Element relationshipId = doc.createElement("relationshipId");
						relationshipId.appendChild(doc.createTextNode(elem.getId()));
						btRelationshipElem.appendChild(relationshipId);
					}					
				}
			}
		}		
	}
}