package unbbayes.io.umpst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import unbbayes.model.umpst.goal.GoalModel;
import unbbayes.model.umpst.goal.HypothesisModel;
import unbbayes.model.umpst.project.UMPSTProject;

/**
 * Get all goal nodes in the file and put them into a map
 * 
 * @author Diego Marques
 *
 */
public class FileLoadNodeGoal {

	private GoalModel goal, subGoal;
	private HypothesisModel hypothesis;

	/* Get all goal nodes and put into a map goal */
	public Map<String, GoalModel> getMapGoals(NodeList list, UMPSTProject umpstProject) {
		Element elem = null;
		NodeList repeatNodes = null;

		Map<String, GoalModel> mapGoal = umpstProject.getMapGoal();		
		ArrayList<FileIndexChildNode> listOfSubGoalNode = new ArrayList<FileIndexChildNode>();
		ArrayList<FileIndexChildNode> listOfHypothesisNode = new ArrayList<FileIndexChildNode>();

		for (int i = 0; i < list.getLength(); i++) {
			ArrayList<String> subGoalsList = new ArrayList<String>();
			ArrayList<String> hypothesisList = new ArrayList<String>();
			Node node = list.item(i);

			if (node.getNodeType() == Node.ELEMENT_NODE) {				
				elem = (Element) node;				
//				String id = elem.getElementsByTagName("id").item(0).getTextContent();
				String goalId = elem.getElementsByTagName("goalId").item(0).getTextContent();
//				String goalName = elem.getElementsByTagName("goalName").item(0).getTextContent();
				String name = elem.getElementsByTagName("name").item(0).getTextContent();
				String comments = elem.getElementsByTagName("comments").item(0).getTextContent();
				String author = elem.getElementsByTagName("author").item(0).getTextContent();
				String date = elem.getElementsByTagName("date").item(0).getTextContent();				

				/* Put all subgoals from goal into a list of index. It is necessary because
				 * there are goals that not exists */
				NodeList subGoalsNodes = elem.getElementsByTagName("subgoals");				
				if (subGoalsNodes.getLength() > 0) {
					NodeList goalIdNodes = subGoalsNodes.item(0).getChildNodes();
					Element goalIdElem = (Element) goalIdNodes;								
					repeatNodes = goalIdElem.getElementsByTagName("goalId");
					for (int j = 0; j < repeatNodes.getLength(); j++) {				
						subGoalsList.add(repeatNodes.item(j).getTextContent());
					}
					FileIndexChildNode iSubGoal = new FileIndexChildNode(goalId, subGoalsList);
					listOfSubGoalNode.add(iSubGoal);
				}

				/* Put all hypothesis related to goal into a list of index */				
//				repeatNodes = elem.getElementsByTagName("hypothesisList");
				NodeList hypothesisNodes = elem.getElementsByTagName("hypothesisList");
				if (hypothesisNodes.getLength() > 0) {
					NodeList hypothesisIdNodes = hypothesisNodes.item(0).getChildNodes();
					Element hypothesisIdElem = (Element) hypothesisIdNodes;								
					repeatNodes = hypothesisIdElem.getElementsByTagName("hypothesisId");
					for (int j = 0; j < repeatNodes.getLength(); j++) {						
						hypothesisList.add(repeatNodes.item(j).getTextContent());

					}
					FileIndexChildNode iHypothesis = new FileIndexChildNode(goalId, hypothesisList);
					listOfHypothesisNode.add(iHypothesis);					
				}

				goal = new GoalModel(goalId, name, comments, author, date, null);
				mapGoal.put(goal.getId(), goal);				
			}
		}

		/* Verify list of subgoals and put then into a mapGoal */		
		for (int j = 0; j < listOfSubGoalNode.size(); j++) {
			String _goalId = listOfSubGoalNode.get(j).getIndex();
			Map<String, GoalModel> mapSubGoal = new HashMap<String, GoalModel>();

			if (listOfSubGoalNode.get(j).getListOfNodes() != null) {			
				for (int i = 0; i < listOfSubGoalNode.get(j).getListOfNodes().size(); i++) {
					String idSubGoal = listOfSubGoalNode.get(j).getListOfNodes().get(i);
					subGoal = umpstProject.getMapGoal().get(idSubGoal);
					mapSubGoal.put(idSubGoal, subGoal);
					
					/* Set subgoals related to goal */
//					mapGoal.get(idGoal).getGoalsRelated().add(subGoal);
				}
				mapGoal.get(_goalId).setSubgoals(mapSubGoal);
			}			
		}

		/* Verify list of hypothesis and put then into a mapHypothesis */		
		for (int j = 0; j < listOfHypothesisNode.size(); j++) {			
			String _goalId = listOfHypothesisNode.get(j).getIndex();
			Map<String, HypothesisModel> mapHypothesis = new HashMap<String, HypothesisModel>();

			if (listOfHypothesisNode.get(j).getListOfNodes() != null) {
				for (int i = 0; i < listOfHypothesisNode.get(j).getListOfNodes().size(); i++) {
					String idHypothesis = listOfHypothesisNode.get(j).getListOfNodes().get(i);
					hypothesis = umpstProject.getMapHypothesis().get(idHypothesis);
					mapHypothesis.put(idHypothesis, hypothesis);		

				}
				mapGoal.get(_goalId).setMapHypothesis(mapHypothesis);
			}
		}
		return mapGoal;
	}
}