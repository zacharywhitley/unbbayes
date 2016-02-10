package unbbayes.io.umpst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.tree.DefaultMutableTreeNode;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import unbbayes.io.umpst.implementation.FileLoadRuleImplementation;
import unbbayes.model.umpst.entity.AttributeModel;
import unbbayes.model.umpst.entity.EntityModel;
import unbbayes.model.umpst.entity.RelationshipModel;
import unbbayes.model.umpst.goal.GoalModel;
import unbbayes.model.umpst.goal.HypothesisModel;
import unbbayes.model.umpst.group.GroupModel;
import unbbayes.model.umpst.implementation.EventType;
import unbbayes.model.umpst.implementation.EventVariableObjectModel;
import unbbayes.model.umpst.implementation.OrdinaryVariableModel;
import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.model.umpst.rule.RuleModel;

/**
 * Get all node of rule in the file and put them into a map
 * 
 * @author Diego Marques
 *
 */

public class FileLoadNodeRule {

	private RuleModel rule, childrenRule;
	private GoalModel goal;
	private HypothesisModel hypothesis;	
	private EntityModel entity;
	private AttributeModel attribute;
	private RelationshipModel relationship;
	private GroupModel group;
	
	/**
	 * Get all node of rule and put into a map of rules
	 *  
	 * @param list
	 * @param umpstProject
	 * @return mapRule
	 */

	public Map<String, RuleModel> getMapRule(NodeList list, UMPSTProject umpstProject) {
		Element elem = null;
		NodeList repeatNodes = null;

		Map<String, RuleModel> mapRule = umpstProject.getMapRules();
		ArrayList<FileIndexChildNode> listOfRuleNode = new ArrayList<FileIndexChildNode>();
		
		FileLoadRuleImplementation lri = new FileLoadRuleImplementation();

		for (int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);
			ArrayList<String> childrenRuleList = new ArrayList<String>();

			if (node.getNodeType() == Node.ELEMENT_NODE) {				
				elem = (Element) node;
//				String id = elem.getElementsByTagName("id").item(0).getTextContent();
				String ruleId = elem.getElementsByTagName("ruleId").item(0).getTextContent();
//				String rulesName = elem.getElementsByTagName("rulesName").item(0).getTextContent();
				String name = elem.getElementsByTagName("name").item(0).getTextContent();
				String comments = elem.getElementsByTagName("comments").item(0).getTextContent();
				String author = elem.getElementsByTagName("author").item(0).getTextContent();
				String date = elem.getElementsByTagName("date").item(0).getTextContent();
				String ruleType = elem.getElementsByTagName("ruleType").item(0).getTextContent();
				
				rule = new RuleModel(ruleId, name, ruleType, comments, author, date);
				
				/* Add all backtracking entity (entityList) related to rule */				
//				repeatNodes = elem.getElementsByTagName("entityList");
				NodeList entityNodes = elem.getElementsByTagName("entityList");
				if (entityNodes.getLength() > 0) {
					NodeList entityIdNodes = entityNodes.item(0).getChildNodes();
					Element entityIdElem = (Element) entityIdNodes;								
					repeatNodes = entityIdElem.getElementsByTagName("entityId");
					for (int j = 0; j < repeatNodes.getLength(); j++) {		
						entity = umpstProject.getMapEntity().get(repeatNodes.item(j).getTextContent());						
						rule.getEntityList().add(entity);
						
						/* Forward tracking of rule related to entity */
						entity.getFowardTrackingRules().add(rule);
					}
				}
				
				/* Add all backtracking attribute (attributeList) related to rule */
//				repeatNodes = elem.getElementsByTagName("attributeList");
				NodeList attributeNodes = elem.getElementsByTagName("attributeList");
				if (attributeNodes.getLength() > 0) {
					NodeList attributeIdNodes = attributeNodes.item(0).getChildNodes();
					Element attributeIdElem = (Element) attributeIdNodes;								
					repeatNodes = attributeIdElem.getElementsByTagName("attributeId");
					for (int j = 0; j < repeatNodes.getLength(); j++) {
						attribute = umpstProject.getMapAtribute().get(repeatNodes.item(j).getTextContent());						
						rule.getAttributeList().add(attribute);
						
						/* Forward tracking of rule related to attribute */
						attribute.getFowardTrackingRules().add(rule);
					}
				}				
				
				/* Add all backtracking relationship (relationshipList) related to rule */
//				repeatNodes = elem.getElementsByTagName("relationshipList");
				NodeList relationshipNodes = elem.getElementsByTagName("relationshipList");
				if (relationshipNodes.getLength() > 0) {
					NodeList relationshipIdNodes = relationshipNodes.item(0).getChildNodes();
					Element relationshipIdElem = (Element) relationshipIdNodes;
					repeatNodes = relationshipIdElem.getElementsByTagName("relationshipId");
					for (int j = 0; j < repeatNodes.getLength(); j++) {					
						relationship = umpstProject.getMapRelationship().get(repeatNodes.item(j).getTextContent());						
						rule.getRelationshipList().add(relationship);
						
						/* Forward tracking of rule related to attribute */
						relationship.getFowardtrackingRules().add(rule);
					}
				}
				
				/* Add all backtracking group (groupList) related to rule */
//				repeatNodes = elem.getElementsByTagName("groupList");
				NodeList groupNodes = elem.getElementsByTagName("groupList");
				if (groupNodes.getLength() > 0) {
					NodeList groupIdNodes = groupNodes.item(0).getChildNodes();
					Element groupIdElem = (Element) groupIdNodes;
					repeatNodes = groupIdElem.getElementsByTagName("groupId");
					for (int j = 0; j < repeatNodes.getLength(); j++) {
						group = umpstProject.getMapGroups().get(repeatNodes.item(j).getTextContent());
						rule.getGroupList().add(group);
					}
				}
				
				/* Add all backtracking children rule (childrenRuleList) related to rule.
				 * 
				 * Needs first all rules inside map rules. Because of that, id is saved and 
				 * then is searched in rules map */
//				repeatNodes = elem.getElementsByTagName("childrenRuleList");
				NodeList childrenRuleNodes = elem.getElementsByTagName("childrenRuleList");
				if (childrenRuleNodes.getLength() > 0) {
					NodeList childrenRuleIdNodes = childrenRuleNodes.item(0).getChildNodes();
					Element childrenRuleIdElem = (Element) childrenRuleIdNodes;
					repeatNodes = childrenRuleIdElem.getElementsByTagName("ruleId");
					for (int j = 0; j < repeatNodes.getLength(); j++) {
						childrenRuleList.add(repeatNodes.item(j).getTextContent());
					}
					FileIndexChildNode iChildrenRules = new FileIndexChildNode(ruleId, childrenRuleList);
					listOfRuleNode.add(iChildrenRules);
				}
				
				/* Add all backtracking goal related to rule */
//				repeatNodes = elem.getElementsByTagName("backtrackingGoalsList");
				NodeList btGoalNodes = elem.getElementsByTagName("backtrackingGoalsList");
				if (btGoalNodes.getLength() > 0) {
					NodeList btGoalIdNodes = btGoalNodes.item(0).getChildNodes();
					Element btGoalIdElem = (Element) btGoalIdNodes;
					repeatNodes = btGoalIdElem.getElementsByTagName("goalId");
					for (int j = 0; j < repeatNodes.getLength(); j++) {					
						goal = umpstProject.getMapGoal().get(repeatNodes.item(j).getTextContent());						
						rule.getBacktrackingGoalList().add(goal);
						
						/* Forward tracking of rule related to goal */
						goal.getFowardTrackingRules().add(rule);
					}
				}
				
				/* Add all backtracking hypothesis (relationshipList) related to rule */
//				repeatNodes = elem.getElementsByTagName("backtrackingHypothesisList");
				NodeList btHypothesisNodes = elem.getElementsByTagName("backtrackingHypothesisList");
				if (btHypothesisNodes.getLength() > 0) {
					NodeList btHypothesisIdNodes = btHypothesisNodes.item(0).getChildNodes();
					Element btHypothesisIdElem = (Element) btHypothesisIdNodes;
					repeatNodes = btHypothesisIdElem.getElementsByTagName("hypothesisId");
					for (int j = 0; j < repeatNodes.getLength(); j++) {
						hypothesis = umpstProject.getMapHypothesis().get(repeatNodes.item(j).getTextContent());						
						rule.getBacktrackingHypothesis().add(hypothesis);
						
						/* Forward tracking of rule related to hypothesis */
						hypothesis.getFowardTrackingRules().add(rule);
					}
				}
				
				// IMPLEMENTATION
				NodeList btImplementation = elem.getElementsByTagName("implementation");
				if (btImplementation.getLength() > 0) {
					NodeList btImplementationNodes = btImplementation.item(0).getChildNodes();
					Element btImplementationElem = (Element) btImplementationNodes;
					
					// OV
					NodeList ovNodeList = btImplementationElem.getElementsByTagName("ordinaryVariableList");
					if (ovNodeList.getLength() > 0) {
						
						lri.loadOVNode(rule, ovNodeList);
						for (int j = 0; j < lri.getOrdinaryVariableList().size(); j++) {
							rule.getOrdinaryVariableList().add(
									lri.getOrdinaryVariableList().get(j));							
						}
					}					
					
					// Cause
					NodeList causeNodeList = btImplementationElem.getElementsByTagName("causeVariableList");
					if (causeNodeList.getLength() > 0) {
						lri.loadCauseNode(rule, causeNodeList);
						for (int j = 0; j < lri.getCauseVariableList().size(); j++) {
							rule.getCauseVariableList().add(
									lri.getCauseVariableList().get(j));
							rule.getEventVariableObjectList().add(
									lri.getCauseVariableList().get(j));
						}
					}
					
					// Effect
					NodeList effectNodeList = btImplementationElem.getElementsByTagName("effectVariableList");
					if (effectNodeList.getLength() > 0) {
						lri.loadEffectNode(rule, effectNodeList);
						for (int j = 0; j < lri.getEffectVariableList().size(); j++) {
							rule.getEffectVariableList().add(
									lri.getEffectVariableList().get(j));
							rule.getEventVariableObjectList().add(
									lri.getEffectVariableList().get(j));
						}
					}
					
					// NecessaryCondition
					NodeList ncNodeList = btImplementationElem.getElementsByTagName("necessaryConditionList");
					if (ncNodeList.getLength() > 0) {
						
						lri.loadNCNode(umpstProject, rule, ncNodeList);
						for (int j = 0; j < lri.getNecessaryConditionList().size(); j++) {
							rule.getNecessaryConditionList().add(
									lri.getNecessaryConditionList().get(j));							
						}
					}
				}
				
				mapRule.put(rule.getId(), rule);				
			}			
		}
		
		/* Verify list of attributes and put then into a mapAttribute */
		for (int j = 0; j < listOfRuleNode.size(); j++) {			
			String _ruleId = listOfRuleNode.get(j).getIndex();

			if (listOfRuleNode.get(j).getListOfNodes() != null) {
				
				/* List of children rules */
				for (int i = 0; i < listOfRuleNode.get(j).getListOfNodes().size(); i++) {
					String childrenRuleId = listOfRuleNode.get(j).getListOfNodes().get(i);					
					
					/* Add father rule in rule children */
					rule = mapRule.get(_ruleId);
					mapRule.get(childrenRuleId).getFatherRuleList().add(rule);
					
					/* Add children in rule father */
					childrenRule = mapRule.get(childrenRuleId);
					mapRule.get(_ruleId).getChildrenRuleList().add(childrenRule);
				}
			}
		}		
		return mapRule;
	}	
}