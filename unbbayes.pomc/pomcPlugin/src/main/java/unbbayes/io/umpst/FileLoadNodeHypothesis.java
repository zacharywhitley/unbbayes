package unbbayes.io.umpst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import unbbayes.model.umpst.group.GroupModel;
import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.model.umpst.rule.RuleModel;

/**
 * Get all hypothesis nodes in the file and put them into a map
 * 
 * @author Diego Marques
 *
 */

public class FileLoadNodeHypothesis {

	private GoalModel goalRelated;
	private HypothesisModel hypothesis, subHypothesis;

	/* Get all hypothesis nodes and put into a map hypothesis */
	public Map<String, HypothesisModel> getMapHypothesis(NodeList list, UMPSTProject umpstProject) {
		Element elem = null;
		NodeList repeatNodes = null;

		Map<String, HypothesisModel> mapHypothesis = umpstProject.getMapHypothesis();		
		ArrayList<FileIndexChildNode> listOfHypothesisNode = new ArrayList<FileIndexChildNode>();

		for (int i = 0; i < list.getLength(); i++) {
			ArrayList<String> subHypothesisList = new ArrayList<String>();
			Node node = list.item(i);

			if (node.getNodeType() == Node.ELEMENT_NODE) {				
				elem = (Element) node;				
				String id = elem.getElementsByTagName("id").item(0).getTextContent();
				String hypothesisName = elem.getElementsByTagName("hypothesisName").item(0).getTextContent();
				String comments = elem.getElementsByTagName("comments").item(0).getTextContent();
				String author = elem.getElementsByTagName("author").item(0).getTextContent();
				String date = elem.getElementsByTagName("date").item(0).getTextContent();

				/* Set goal related to hypothesis */				
				String goalRelatedId = elem.getElementsByTagName("goalRelated").item(0).getTextContent();
				goalRelated = umpstProject.getMapGoal().get(goalRelatedId);

				/* Put all subHypothesis related to hypothesis into a list of index */				
				repeatNodes = elem.getElementsByTagName("hypothesisList");
				if (repeatNodes.getLength() > 0) {
					for (int j = 0; j < repeatNodes.getLength(); j++) {						
						subHypothesisList.add(repeatNodes.item(j).getTextContent());

					}
					FileIndexChildNode iHypothesis = new FileIndexChildNode(id, subHypothesisList);
					listOfHypothesisNode.add(iHypothesis);					
				}

				hypothesis = new HypothesisModel(id, hypothesisName, comments, author, date, null, 
						null, null);

				/* add goal related to hypothesis */
				hypothesis.getGoalRelated().add(goalRelated);

				/* relate hypothesis object model to goal */
				goalRelated.getMapHypothesis().put(id, hypothesis);

				mapHypothesis.put(hypothesis.getId(), hypothesis);				
			}
		}

		/* Verify list of subHypothesis and put then into a mapHypothesis */		
		for (int j = 0; j < listOfHypothesisNode.size(); j++) {			
			String hypothesisId = listOfHypothesisNode.get(j).getIndex();

			if (listOfHypothesisNode.get(j).getListOfNodes() != null) {
				Map<String, HypothesisModel> mapSubHypothesis = new HashMap<String, HypothesisModel>();

				for (int i = 0; i < listOfHypothesisNode.get(j).getListOfNodes().size(); i++) {
					String subHypothesisId = listOfHypothesisNode.get(j).getListOfNodes().get(i);
					subHypothesis = umpstProject.getMapHypothesis().get(subHypothesisId);
					mapSubHypothesis.put(subHypothesisId, subHypothesis);

					/* Set hypothesis father */
					mapHypothesis.get(subHypothesisId).setFather(mapHypothesis.get(hypothesisId));

				}
				mapHypothesis.get(hypothesisId).setMapSubHypothesis(mapSubHypothesis);
			}
		}
		return mapHypothesis;
	}

}