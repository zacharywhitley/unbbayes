package unbbayes.io.umpst;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import unbbayes.model.umpst.entity.AttributeModel;
import unbbayes.model.umpst.entity.EntityModel;
import unbbayes.model.umpst.entity.RelationshipModel;
import unbbayes.model.umpst.goal.GoalModel;
import unbbayes.model.umpst.goal.HypothesisModel;
import unbbayes.model.umpst.project.UMPSTProject;

/**
 * Get all node of relationship in the file and put them into a map
 * 
 * @author Diego Marques
 *
 */

public class FileLoadNodeRelationship {

	private GoalModel goal;
	private HypothesisModel hypothesis;	
	private EntityModel entity;
	private RelationshipModel relationship;
	private AttributeModel attribute;

	/* Get all node of relationship and put into a map of relationships */
	public Map<String, RelationshipModel> getMapRelationship(NodeList list, UMPSTProject umpstProject) {
		Element elem = null;
		NodeList repeatNodes = null;

		Map<String, RelationshipModel> mapRelationship = umpstProject.getMapRelationship();		

		for (int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);

			if (node.getNodeType() == Node.ELEMENT_NODE) {				
				elem = (Element) node;
//				String id = elem.getElementsByTagName("id").item(0).getTextContent();
				String relationshipId = elem.getElementsByTagName("relationshipId").item(0).getTextContent();
//				String relationshipName = elem.getElementsByTagName("relationshipName").item(0).getTextContent();
				String name = elem.getElementsByTagName("name").item(0).getTextContent();
				String comments = elem.getElementsByTagName("comments").item(0).getTextContent();
				String author = elem.getElementsByTagName("author").item(0).getTextContent();
				String date = elem.getElementsByTagName("date").item(0).getTextContent();
				
				relationship = new RelationshipModel(relationshipId, name, comments, author, date, 
						null, null, null, null, null, null);				
				
				/* Add all backtracking goal related to relationship */		
//				repeatNodes = elem.getElementsByTagName("backtrackingGoalsList");
				NodeList btGoalNodes = elem.getElementsByTagName("backtrackingGoalsList");
				if (btGoalNodes.getLength() > 0) {
					NodeList goalIdNodes = btGoalNodes.item(0).getChildNodes();
					Element goalIdElem = (Element) goalIdNodes;								
					repeatNodes = goalIdElem.getElementsByTagName("goalId");
					for (int j = 0; j < repeatNodes.getLength(); j++) {					
						goal = umpstProject.getMapGoal().get(repeatNodes.item(j).getTextContent());						
						relationship.getBacktrackingGoal().add(goal);
					}
				}

				/* Add all backtracking hypothesis related to relationship */				
//				repeatNodes = elem.getElementsByTagName("backtrackingHypothesisList");
				NodeList btHypothesisNodes = elem.getElementsByTagName("backtrackingHypothesisList");
				if (btHypothesisNodes.getLength() > 0) {
					NodeList hypothesisIdNodes = btHypothesisNodes.item(0).getChildNodes();
					Element hypothesisIdElem = (Element) hypothesisIdNodes;								
					repeatNodes = hypothesisIdElem.getElementsByTagName("hypothesisId");
					for (int j = 0; j < repeatNodes.getLength(); j++) {
						hypothesis = umpstProject.getMapHypothesis().get(repeatNodes.item(j).getTextContent());						
						relationship.getBacktrackingHypothesis().add(hypothesis);
					}
				}
				
				/* Add all backtracking entity related to relationship */				
//				repeatNodes = elem.getElementsByTagName("backtrackingEntity");
//				repeatNodes = elem.getElementsByTagName("backtrackingEntitiesList");
				NodeList btEntityNodes = elem.getElementsByTagName("backtrackingEntitiesList");
				if (btEntityNodes.getLength() > 0) {
					NodeList entityIdNodes = btEntityNodes.item(0).getChildNodes();
					Element entityIdElem = (Element) entityIdNodes;								
					repeatNodes = entityIdElem.getElementsByTagName("entityId");
					for (int j = 0; j < repeatNodes.getLength(); j++) {
						entity = umpstProject.getMapEntity().get(repeatNodes.item(j).getTextContent());						
						relationship.getEntityList().add(entity);
						
						/* Forward tracking of entity related to relationship */
						entity.getFowardTrackingRelationship().add(relationship);
						
						/* Indirect dependency with forward tracking attribute */
						Set<String> keys = entity.getMapAtributes().keySet();
						for(String attributeId : keys) {
							attribute = entity.getMapAtributes().get(attributeId);
							attribute.getFowardTrackingRelationship().add(relationship);
						}
					}
				}				
				mapRelationship.put(relationship.getId(), relationship);				
			}
		}		
		return mapRelationship;
	}
}