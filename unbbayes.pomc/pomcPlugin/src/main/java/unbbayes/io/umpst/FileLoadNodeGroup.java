package unbbayes.io.umpst;

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
 * Get all node of group in the file and put them into a map
 * 
 * @author Diego Marques
 *
 */

public class FileLoadNodeGroup {

	private RuleModel rule;
	private GoalModel goal;
	private HypothesisModel hypothesis;	
	private EntityModel entity;
	private AttributeModel attribute;
	private RelationshipModel relationship;
	private GroupModel group;
	
	/**
	 * Get all node of group and put into a map of groups
	 * 
	 * @param list
	 * @param umpstProject
	 * @return mapGroup
	 */
	
	public Map<String, GroupModel> getMapGroup(NodeList list, UMPSTProject umpstProject) {
		Element elem = null;
		NodeList repeatNodes = null;

		Map<String, GroupModel> mapGroup = umpstProject.getMapGroups();

		for (int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);

			if (node.getNodeType() == Node.ELEMENT_NODE) {				
				elem = (Element) node;
//				String id = elem.getElementsByTagName("id").item(0).getTextContent();
				String groupId = elem.getElementsByTagName("groupId").item(0).getTextContent();
//				String groupName = elem.getElementsByTagName("groupName").item(0).getTextContent();
				String name = elem.getElementsByTagName("name").item(0).getTextContent();
				String comments = elem.getElementsByTagName("comments").item(0).getTextContent();
				String author = elem.getElementsByTagName("author").item(0).getTextContent();
				String date = elem.getElementsByTagName("date").item(0).getTextContent();

				group = new GroupModel(groupId, name, comments, author, date);
				
//				/* Add all backtracking goal related to group */
//				repeatNodes = elem.getElementsByTagName("backtrackingGoalsList");
//				if (repeatNodes.getLength() > 0) {
//					for (int j = 0; j < repeatNodes.getLength(); j++) {					
//						goal = umpstProject.getMapGoal().get(repeatNodes.item(j).getTextContent());						
//						group.getBacktrackingGoal().add(goal);
//					}
//				}
				
//				/* Add all backtracking hypothesis related to group */
//				repeatNodes = elem.getElementsByTagName("backtrackingHypothesisList");
//				if (repeatNodes.getLength() > 0) {
//					for (int j = 0; j < repeatNodes.getLength(); j++) {		
//						hypothesis = umpstProject.getMapHypothesis().get(repeatNodes.item(j).getTextContent());						
//						group.getBacktrackingHypothesis().add(hypothesis);
//					}
//				}				
				
				
				/* 
				 * Obs: implementation part will define this
				 * 
				 * Add all backtracking rule related to group */				
//				repeatNodes = elem.getElementsByTagName("backtrackingRulesList");
				NodeList ruleNodes = elem.getElementsByTagName("backtrackingRulesList");
				if (ruleNodes.getLength() > 0) {
					NodeList ruleIdNodes = ruleNodes.item(0).getChildNodes();
					Element ruleIdElem = (Element) ruleIdNodes;								
					repeatNodes = ruleIdElem.getElementsByTagName("ruleId");
					for (int j = 0; j < repeatNodes.getLength(); j++) {		
						rule = umpstProject.getMapRules().get(repeatNodes.item(j).getTextContent());						
						group.getBacktrackingRules().add(rule);
						
						/* Forward tracking of rule related to group */
						rule.getFowardtrackingGroupList().add(group);
					}
				}
				
				/* Add all backtracking entity related to group */				
//				repeatNodes = elem.getElementsByTagName("backtrackingEntitiesList");
				NodeList entityNodes = elem.getElementsByTagName("backtrackingEntitiesList");
				if (entityNodes.getLength() > 0) {
					NodeList entityIdNodes = entityNodes.item(0).getChildNodes();
					Element entityIdElem = (Element) entityIdNodes;								
					repeatNodes = entityIdElem.getElementsByTagName("entityId");
					for (int j = 0; j < repeatNodes.getLength(); j++) {		
						entity = umpstProject.getMapEntity().get(repeatNodes.item(j).getTextContent());						
						group.getBacktrackingEntities().add(entity);
						
						/* Forward tracking of entity related to relationship */
						entity.getFowardTrackingGroups().add(group);
						
						/* Backtracking goals and hypothesis present in backtracking entities */
						for (int j2 = 0; j2 < entity.getBacktrackingGoalList().size(); j2++) {
							goal = entity.getBacktrackingGoalList().get(j2);
							
							/* Indirect dependency with backtracking goal */
							group.getBacktrackingGoal().add(goal);
							
							/* Indirect dependency with forward tracking goal */
							goal.getFowardTrackingGroups().add(group);	
						}
						
						/* Indirect dependency with backtracking hypothesis */
						for (int j2 = 0; j2 < entity.getBacktrackingHypothesis().size(); j2++) {
							hypothesis = entity.getBacktrackingHypothesis().get(j2);
							group.getBacktrackingHypothesis().add(hypothesis);
							
							/* Indirect dependency with forward tracking hypothesis */
							hypothesis.getFowardTrackingGroups().add(group);
						}
					}
				}
				
				/* Add all backtracking attribute related to group */
//				repeatNodes = elem.getElementsByTagName("backtrackingAttributesList");
				NodeList attributeNodes = elem.getElementsByTagName("backtrackingAttributesList");
				if (attributeNodes.getLength() > 0) {
					NodeList attributeIdNodes = attributeNodes.item(0).getChildNodes();
					Element attributeIdElem = (Element) attributeIdNodes;					
					repeatNodes = attributeIdElem.getElementsByTagName("attributeId");
					for (int j = 0; j < repeatNodes.getLength(); j++) {		
						attribute = umpstProject.getMapAtribute().get(repeatNodes.item(j).getTextContent());						
						group.getBacktrackingAtributes().add(attribute);
						
						/* Forward tracking of entity related to relationship */
						attribute.getFowardTrackingGroups().add(group);
					}
				}				
				
				/* Add all backtracking relationship (relationshipList) related to rule */
//				repeatNodes = elem.getElementsByTagName("backtrackingRelationshipList");
				NodeList relationshipNodes = elem.getElementsByTagName("backtrackingRelationshipList");
				if (relationshipNodes.getLength() > 0) {
					NodeList relationshipIdNodes = relationshipNodes.item(0).getChildNodes();
					Element relationshipIdElem = (Element) relationshipIdNodes;					
					repeatNodes = relationshipIdElem.getElementsByTagName("relationshipId");
					for (int j = 0; j < repeatNodes.getLength(); j++) {					
						relationship = umpstProject.getMapRelationship().get(repeatNodes.item(j).getTextContent());						
						group.getBacktrackingRelationship().add(relationship);
						
						/* Forward tracking of entity related to relationship */
						relationship.getFowardtrackingGroups().add(group);
					}
				}
				
				mapGroup.put(group.getId(), group);				
			}			
		}		
		return mapGroup;
	}
}