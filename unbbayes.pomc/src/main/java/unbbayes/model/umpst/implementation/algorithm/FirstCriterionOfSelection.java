package unbbayes.model.umpst.implementation.algorithm;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.osgi.framework.debug.Debug;

import unbbayes.controller.umpst.MappingController;
import unbbayes.model.umpst.entity.RelationshipModel;
import unbbayes.model.umpst.group.GroupModel;
import unbbayes.model.umpst.implementation.node.MFragExtension;
import unbbayes.model.umpst.implementation.node.ResidentNodeExtension;
import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;

/**
 * Separate resident nodes according the relationship presence in a groupList.
 * If relationshipModel is in just one group then it is a resident node.
 * @author Diego Marques
 */
public class FirstCriterionOfSelection {
	
	private UMPSTProject umpstProject;
	private MappingController mappingController;

	public FirstCriterionOfSelection(UMPSTProject umpstProject, MappingController mappingController,
			MultiEntityBayesianNetwork mebn) {
		
		this.umpstProject = umpstProject;
		this.mappingController = mappingController;
		
		firstSelection(mebn);
		Debug.println(this.getClass() + "Fist Criterion of Selection done.");
	}
	
	/**
	 * Search {@link RelationshipModel} declared once in {@link GroupModel}
	 * and maps them to {@link ResidentNodeExtension}.
	 */
	public void firstSelection(MultiEntityBayesianNetwork mebn) {
		
		Map<String, MFragExtension> mapMFragExtension = mappingController.getMapMFragExtension();
		Set<String> keys = mapMFragExtension.keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);
		
		for (String groupId : sortedKeys) {	
			MFragExtension mfragExtension = mapMFragExtension.get(groupId);
			
			GroupModel groupRelated = mfragExtension.getGroupRelated();
			List<RelationshipModel> relationshipList = groupRelated.getBacktrackingRelationship(); 
			for (int i = 0; i < relationshipList.size(); i++) {
				
				RelationshipModel relationship = relationshipList.get(i);
				if (relationship.getFowardtrackingGroups().size() == 1) {
					
					ResidentNodeExtension residentNodeExtension = mappingController.mapToResidentNode(
							relationship, mfragExtension, null);
					
					if(residentNodeExtension == null) {
						System.err.println(this.getClass()+ "- Error in create resident node: "+relationship.getName());
					}
				}
			}
		}
	}
}
