package unbbayes.io.umpst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import unbbayes.model.umpst.entity.AttributeModel;
import unbbayes.model.umpst.entity.EntityModel;
import unbbayes.model.umpst.goal.HypothesisModel;
import unbbayes.model.umpst.project.UMPSTProject;

/**
 * Get all attribute node in the file and put them into a map
 * 
 * @author Diego Marques
 *
 */

public class FileLoadNodeAttribute {

	private EntityModel entityRelated;
	private AttributeModel attribute, subAttribute;

	/* Get all attribute nodes and put into a map of attributes */
	public Map<String, AttributeModel> getMapAttribute(NodeList list, UMPSTProject umpstProject) {
		Element elem = null;
		NodeList repeatNodes = null;

		Map<String, AttributeModel> mapAttribute = umpstProject.getMapAtribute();		
		ArrayList<FileIndexChildNode> listOfSubAttributeNode = new ArrayList<FileIndexChildNode>();

		for (int i = 0; i < list.getLength(); i++) {
			ArrayList<String> subAttribute = new ArrayList<String>();
			Node node = list.item(i);

			if (node.getNodeType() == Node.ELEMENT_NODE) {				
				elem = (Element) node;
//				String id = elem.getElementsByTagName("id").item(0).getTextContent();
				String attributeId = elem.getElementsByTagName("attributeId").item(0).getTextContent();
//				String atributeName = elem.getElementsByTagName("atributeName").item(0).getTextContent();
				String name = elem.getElementsByTagName("name").item(0).getTextContent();
				String comments = elem.getElementsByTagName("comments").item(0).getTextContent();
				String author = elem.getElementsByTagName("author").item(0).getTextContent();
				String date = elem.getElementsByTagName("date").item(0).getTextContent();
				
				/* Set entity related to attribute */				
				String entityRelatedId = elem.getElementsByTagName("entityRelated").item(0).getTextContent();
				entityRelated = umpstProject.getMapEntity().get(entityRelatedId);
				
				/* Put all subAttributes from attribute into a list of index */				
				repeatNodes = elem.getElementsByTagName("subAtributesList");
				if (repeatNodes.getLength() > 0) {
					for (int j = 0; j < repeatNodes.getLength(); j++) {						
						subAttribute.add(repeatNodes.item(j).getTextContent());
						
					}
					FileIndexChildNode iAttribute = new FileIndexChildNode(attributeId, subAttribute);
					listOfSubAttributeNode.add(iAttribute);
					
		
				}
				attribute = new AttributeModel(attributeId, name, comments, author, date, null, null, 
						null, null, null, null);
				
				/* add entity related to attribute */
				attribute.getEntityRelated().add(entityRelated);
				
				/* relate attribute object model to entity */
				entityRelated.getMapAtributes().put(attributeId, attribute);
				
				mapAttribute.put(attribute.getId(), attribute);				
			}
		}

		/* Verify list of subAttributes and put then into a mapAttribute */		
		for (int j = 0; j < listOfSubAttributeNode.size(); j++) {
			String _attributeId = listOfSubAttributeNode.get(j).getIndex();
			Map<String, AttributeModel> mapSubAttribute = new HashMap<String, AttributeModel>();

			if (listOfSubAttributeNode.get(j).getListOfNodes() != null) {			
				for (int i = 0; i < listOfSubAttributeNode.get(j).getListOfNodes().size(); i++) {
					String subAttributeId = listOfSubAttributeNode.get(j).getListOfNodes().get(i);
					subAttribute = umpstProject.getMapAtribute().get(subAttributeId);
					mapSubAttribute.put(subAttributeId, subAttribute);
					
				}
				mapAttribute.get(_attributeId).setMapSubAtributes(mapSubAttribute);
			}		
		}		
		
		return mapAttribute;
	}
}