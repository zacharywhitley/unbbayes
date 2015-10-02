package unbbayes.io.umpst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import unbbayes.model.umpst.entity.AttributeModel;
import unbbayes.model.umpst.entity.EntityModel;
import unbbayes.model.umpst.goal.GoalModel;
import unbbayes.model.umpst.goal.HypothesisModel;
import unbbayes.model.umpst.project.UMPSTProject;

/**
 * Get all node of entity in the file and put them into a map
 * 
 * @author Diego Marques
 *
 */

public class FileLoadNodeEntity {

	private GoalModel goal;
	private HypothesisModel hypothesis;	
	private AttributeModel attribute;
	private EntityModel entity;
	
	private Map<String, EntityModel> mapEntity;
	private Set<EntityModel> fowardTrackingEntity;
	
	/**
	 * Get all node of entity and put into a map of entities
	 * 
	 * @param list
	 * @param umpstProject
	 * @return mapEntity
	 */
	
	public Map<String, EntityModel> getMapEntity(NodeList list, UMPSTProject umpstProject) {
		Element elem = null;
		NodeList repeatNodes = null;

		mapEntity = umpstProject.getMapEntity();
		ArrayList<FileIndexChildNode> listOfAttributeNode = new ArrayList<FileIndexChildNode>();

		for (int i = 0; i < list.getLength(); i++) {
			ArrayList<String> attributeList = new ArrayList<String>();
			Node node = list.item(i);

			if (node.getNodeType() == Node.ELEMENT_NODE) {				
				elem = (Element) node;				
				String id = elem.getElementsByTagName("id").item(0).getTextContent();
				String entityName = elem.getElementsByTagName("entityName").item(0).getTextContent();
				String comments = elem.getElementsByTagName("comments").item(0).getTextContent();
				String author = elem.getElementsByTagName("author").item(0).getTextContent();
				String date = elem.getElementsByTagName("date").item(0).getTextContent();
				
				entity = new EntityModel(id, entityName, comments, author, date);

				/* Put all attribute related to entity into a list of index */
				/* It is necessary because entity methods only accept map of 
				 * attributes as argument*/
				repeatNodes = elem.getElementsByTagName("atributesList");
				if (repeatNodes.getLength() > 0) {
					for (int j = 0; j < repeatNodes.getLength(); j++) {						
						attributeList.add(repeatNodes.item(j).getTextContent());
					}
					FileIndexChildNode iEntity = new FileIndexChildNode(id, attributeList);
					listOfAttributeNode.add(iEntity);					
				}
				
				/* Add all backtracking goal related to entity */				
				repeatNodes = elem.getElementsByTagName("backtrackingGoalsList");
				if (repeatNodes.getLength() > 0) {
					for (int j = 0; j < repeatNodes.getLength(); j++) {					
						goal = umpstProject.getMapGoal().get(repeatNodes.item(j).getTextContent());						
						entity.getBacktrackingGoalList().add(goal);
						
						/* Forward tracking of entity related to goal */
						goal.getFowardTrackingEntity().add(entity);
					}
				}

				/* Add all backtracking hypothesis related to entity */				
				repeatNodes = elem.getElementsByTagName("backtrackingHypothesisList");
				if (repeatNodes.getLength() > 0) {
					for (int j = 0; j < repeatNodes.getLength(); j++) {		
						hypothesis = umpstProject.getMapHypothesis().get(repeatNodes.item(j).getTextContent());						
						entity.getBacktrackingHypothesis().add(hypothesis);
						
						/* Forward tracking of entity related to hypothesis */
						hypothesis.getFowardTrackingEntity().add(entity);
					}
				}
				mapEntity.put(entity.getId(), entity);				
			}
		}

		/* Verify list of attributes and put then into a mapAttribute */		
		for (int j = 0; j < listOfAttributeNode.size(); j++) {			
			String entityId = listOfAttributeNode.get(j).getIndex();

			if (listOfAttributeNode.get(j).getListOfNodes() != null) {
				Map<String, AttributeModel> mapAttribute = new HashMap<String, AttributeModel>();

				for (int i = 0; i < listOfAttributeNode.get(j).getListOfNodes().size(); i++) {
					String attributeId = listOfAttributeNode.get(j).getListOfNodes().get(i);
					attribute = umpstProject.getMapAtribute().get(attributeId);
					mapAttribute.put(attributeId, attribute);
				}
				mapEntity.get(entityId).setMapAtributes(mapAttribute);
				
			}
		}
		
		return mapEntity;
	}	
}