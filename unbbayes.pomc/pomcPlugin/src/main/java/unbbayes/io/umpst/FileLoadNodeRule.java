package unbbayes.io.umpst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import unbbayes.model.umpst.entity.AttributeModel;
import unbbayes.model.umpst.entity.EntityModel;
import unbbayes.model.umpst.entity.RelationshipModel;
import unbbayes.model.umpst.goal.GoalModel;
import unbbayes.model.umpst.goal.HypothesisModel;
import unbbayes.model.umpst.group.GroupModel;
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

		for (int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);
			ArrayList<String> childrenRuleList = new ArrayList<String>();

			if (node.getNodeType() == Node.ELEMENT_NODE) {				
				elem = (Element) node;				
				String id = elem.getElementsByTagName("id").item(0).getTextContent();
				String rulesName = elem.getElementsByTagName("rulesName").item(0).getTextContent();
				String comments = elem.getElementsByTagName("comments").item(0).getTextContent();
				String author = elem.getElementsByTagName("author").item(0).getTextContent();
				String date = elem.getElementsByTagName("date").item(0).getTextContent();
				String ruleType = elem.getElementsByTagName("ruleType").item(0).getTextContent();
				
				rule = new RuleModel(id, rulesName, ruleType, comments, author, date);
				
				/* Add all backtracking entity (entityList) related to rule */				
				repeatNodes = elem.getElementsByTagName("entityList");
				if (repeatNodes.getLength() > 0) {
					for (int j = 0; j < repeatNodes.getLength(); j++) {		
						entity = umpstProject.getMapEntity().get(repeatNodes.item(j).getTextContent());						
						rule.getEntityList().add(entity);
						
						/* Forward tracking of rule related to entity */
						entity.getFowardTrackingRules().add(rule);
					}
				}
				
				/* Add all backtracking attribute (attributeList) related to rule */
				repeatNodes = elem.getElementsByTagName("attributeList");
				if (repeatNodes.getLength() > 0) {
					for (int j = 0; j < repeatNodes.getLength(); j++) {		
						attribute = umpstProject.getMapAtribute().get(repeatNodes.item(j).getTextContent());						
						rule.getAttributeList().add(attribute);
						
						/* Forward tracking of rule related to attribute */
						attribute.getFowardTrackingRules().add(rule);
					}
				}				
				
				/* Add all backtracking relationship (relationshipList) related to rule */
				repeatNodes = elem.getElementsByTagName("relationshipList");
				if (repeatNodes.getLength() > 0) {
					for (int j = 0; j < repeatNodes.getLength(); j++) {					
						relationship = umpstProject.getMapRelationship().get(repeatNodes.item(j).getTextContent());						
						rule.getRelationshipList().add(relationship);
						
						/* Forward tracking of rule related to attribute */
						relationship.getFowardtrackingRules().add(rule);
					}
				}
				
				/* Add all backtracking group (groupList) related to rule */
				repeatNodes = elem.getElementsByTagName("groupList");
				if (repeatNodes.getLength() > 0) {
					for (int j = 0; j < repeatNodes.getLength(); j++) {
						group = umpstProject.getMapGroups().get(repeatNodes.item(j).getTextContent());
						rule.getGroupList().add(group);
					}
				}
				
				/* Add all backtracking children rule (childrenRuleList) related to rule.
				 * 
				 * Needs first all rules inside map rules. Because of that, id is saved and 
				 * then is searched in rules map */
				repeatNodes = elem.getElementsByTagName("childrenRuleList");
				if (repeatNodes.getLength() > 0) {
					for (int j = 0; j < repeatNodes.getLength(); j++) {						
						childrenRuleList.add(repeatNodes.item(j).getTextContent());
					}
					FileIndexChildNode iChildrenRules = new FileIndexChildNode(id, childrenRuleList);
					listOfRuleNode.add(iChildrenRules);					
				}
				
				/* Add all backtracking goal related to rule */
				repeatNodes = elem.getElementsByTagName("backtrackingGoalsList");
				if (repeatNodes.getLength() > 0) {
					for (int j = 0; j < repeatNodes.getLength(); j++) {					
						goal = umpstProject.getMapGoal().get(repeatNodes.item(j).getTextContent());						
						rule.getBacktrackingGoalList().add(goal);
						
						/* Forward tracking of rule related to goal */
						goal.getFowardTrackingRules().add(rule);
					}
				}
				
				/* Add all backtracking hypothesis (relationshipList) related to rule */
				repeatNodes = elem.getElementsByTagName("backtrackingHypothesisList");
				if (repeatNodes.getLength() > 0) {
					for (int j = 0; j < repeatNodes.getLength(); j++) {
						hypothesis = umpstProject.getMapHypothesis().get(repeatNodes.item(j).getTextContent());						
						rule.getBacktrackingHypothesis().add(hypothesis);
						
						/* Forward tracking of rule related to hypothesis */
						hypothesis.getFowardTrackingRules().add(rule);
					}
				}
				
				mapRule.put(rule.getId(), rule);				
			}			
		}
		
		/* Verify list of attributes and put then into a mapAttribute */		
		for (int j = 0; j < listOfRuleNode.size(); j++) {			
			String ruleId = listOfRuleNode.get(j).getIndex();

			if (listOfRuleNode.get(j).getListOfNodes() != null) {
				
				/* List of children rules */
				for (int i = 0; i < listOfRuleNode.get(j).getListOfNodes().size(); i++) {
					String childrenRuleId = listOfRuleNode.get(j).getListOfNodes().get(i);					
					
					/* Add father rule in rule children */
					rule = mapRule.get(ruleId);
					mapRule.get(childrenRuleId).getFatherRuleList().add(rule);
					
					/* Add children in rule father */
					childrenRule = mapRule.get(childrenRuleId);
					mapRule.get(ruleId).getChildrenRuleList().add(childrenRule);
				}
			}
		}
		
		return mapRule;
	}
}