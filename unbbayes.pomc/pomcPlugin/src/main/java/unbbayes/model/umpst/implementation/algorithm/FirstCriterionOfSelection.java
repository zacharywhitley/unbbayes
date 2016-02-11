package unbbayes.model.umpst.implementation.algorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import unbbayes.controller.umpst.GenerateMTheoryController;
import unbbayes.model.umpst.entity.RelationshipModel;
import unbbayes.model.umpst.group.GroupModel;
import unbbayes.model.umpst.implementation.node.NodeObjectModel;
import unbbayes.model.umpst.implementation.node.NodeResidentModel;
import unbbayes.model.umpst.implementation.node.NodeType;
import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.util.ArrayMap;

/**
 * Separate resident nodes according the relationship presence in a groupList.
 * If relationshipModel is in just one group then it is a resident node.
 * @author Diego Marques
 */
public class FirstCriterionOfSelection {
	
	private UMPSTProject umpstProject;
	private Map<String, RelationshipModel> mapRelationship;
	private Map<String, GroupModel> mapGroup;
	
	private List<NodeResidentModel> nodeResidentList;
	
	private GenerateMTheoryController generateMTheoryController;

	public FirstCriterionOfSelection(UMPSTProject umpstProject, GenerateMTheoryController generateMTheoryController) {
		
		this.umpstProject = umpstProject;
		this.generateMTheoryController = generateMTheoryController;
		
		mapRelationship = new ArrayMap<String, RelationshipModel>();
		mapGroup = new ArrayMap<String, GroupModel>();
		nodeResidentList = new ArrayList<NodeResidentModel>();
		
		createMfrags();
		searchResidentNode();
	}
	
	public void searchResidentNode() {
		
		mapRelationship = umpstProject.getMapRelationship();
		Set<String> keys = mapRelationship.keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);
		
		for (String key : sortedKeys) {
			RelationshipModel relationship = mapRelationship.get(key);
			String id = relationship.getId();
			String name = relationship.getName();
			
			Set<GroupModel> setGroups = relationship.getFowardtrackingGroups();
			if (setGroups.size() == 1) {
				
				NodeResidentModel nodeResident = new NodeResidentModel(
						id, name, NodeType.RESIDENT, relationship);
				
				for (GroupModel group : setGroups) {
					generateMTheoryController.addNodeResidentInMFrag(group.getId(), nodeResident);
				}	
				
			} else {
				
				NodeObjectModel nodeNotDefined = new NodeObjectModel(
						id, name, NodeType.NOT_DEFINED, relationship);
				
				for (GroupModel group : setGroups) {					
					generateMTheoryController.addNotDefinedNodeInMFrag(group.getId(), nodeNotDefined);
				}				
			}
		}
	}
	
	public void createMfrags() {
		
		mapGroup = umpstProject.getMapGroups();
		Set<String> keys = mapGroup.keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);
		
		for (String key : sortedKeys) {
			GroupModel group = mapGroup.get(key);
			
			String id = group.getId();
			String name = group.getName();
			
			MFragModel mfrag = new MFragModel(id, name);
			generateMTheoryController.addMFrag(mfrag);			
		}
	}

	/**
	 * @return the nodeResidentList
	 */
	public List<NodeResidentModel> getNodeResidentList() {
		return nodeResidentList;
	}


	/**
	 * @param nodeResidentList the nodeResidentList to set
	 */
	public void setNodeResidentList(List<NodeResidentModel> nodeResidentList) {
		this.nodeResidentList = nodeResidentList;
	}

}
