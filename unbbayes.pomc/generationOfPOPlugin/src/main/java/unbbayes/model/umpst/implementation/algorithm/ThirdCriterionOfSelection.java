/**
 * 
 */
package unbbayes.model.umpst.implementation.algorithm;

import java.util.List;

import unbbayes.controller.umpst.MappingController;
import unbbayes.model.umpst.entity.RelationshipModel;
import unbbayes.model.umpst.exception.IncompatibleQuantityException;
import unbbayes.model.umpst.implementation.CauseVariableModel;
import unbbayes.model.umpst.implementation.EventMappingType;
import unbbayes.model.umpst.implementation.node.InputNodeExtension;
import unbbayes.model.umpst.implementation.node.MFragExtension;
import unbbayes.model.umpst.implementation.node.ResidentNodeExtension;
import unbbayes.model.umpst.implementation.node.UndefinedNode;
import unbbayes.model.umpst.rule.RuleModel;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.exception.ArgumentNodeAlreadySetException;
import unbbayes.prs.mebn.exception.OVDontIsOfTypeExpected;
import unbbayes.prs.mebn.exception.OVariableAlreadyExistsInArgumentList;
import unbbayes.util.Debug;

/**
 * Maps an event related to the model to resident or input node manually by the user.
 * Create a panel showing the undefined nodes to the user selects as resident or input node.
 * 
 * @author Diego Marques
 */
public class ThirdCriterionOfSelection {

	private MappingController mappingController;	
	
	public ThirdCriterionOfSelection(MappingController mappingController, MultiEntityBayesianNetwork mebn,
			List<UndefinedNode> undefinedNodeList) {
		
		this.mappingController = mappingController;
		thirdCriterion(mebn, undefinedNodeList);				
	}
	
	public void thirdCriterion(MultiEntityBayesianNetwork mebn, List<UndefinedNode> undefinedNodeList) {
		
//		List<UndefinedNode> undefinedNodeList = mappingController.getUndefinedNodeList();
		if(undefinedNodeList.size() > 0) {			
			mappingController.createThirdCriterionPanel(undefinedNodeList, mebn);
		}			
	}
	
	public void mapUndefinedNode(List<UndefinedNode> hypothesisListCase, MultiEntityBayesianNetwork mebn)  throws IncompatibleQuantityException,ArgumentNodeAlreadySetException,
		OVariableAlreadyExistsInArgumentList, InvalidParentException{
		
		// The nodes were mapped by the user			
		// Map to resident node
		for (int i = 0; i < hypothesisListCase.size(); i++) {
			
			UndefinedNode nodeMapped = hypothesisListCase.get(i);
			if(nodeMapped.getMappingType().equals(EventMappingType.RESIDENT)) {
				
				CauseVariableModel causeRelated = (CauseVariableModel)nodeMapped.getEventRelated();					
				RelationshipModel relationship = causeRelated.getRelationshipModel();
				MFragExtension mfragRelated = nodeMapped.getMfragExtension();
				RuleModel ruleRelated = nodeMapped.getRuleRelated();
				
				
				ResidentNodeExtension residentNode = mappingController.mapToResidentNode(
						relationship, mfragRelated, nodeMapped.getEventRelated());
				mappingController.mapAllEffectsToResident(residentNode, mfragRelated, ruleRelated);
				
				Debug.println("[PLUG-IN EXT] Mapped UndefinedNode "+ ((CauseVariableModel)nodeMapped.getEventRelated()).getRelationship()+" To Resident at "+mfragRelated.getName());
			}				
		}
		
		// Map to input node
		for (int i = 0; i < hypothesisListCase.size(); i++) {
			
			UndefinedNode nodeMapped = hypothesisListCase.get(i);
			if(nodeMapped.getMappingType().equals(EventMappingType.INPUT)) {
				
				CauseVariableModel causeRelated = (CauseVariableModel)nodeMapped.getEventRelated();
				MFragExtension mfragRelated = nodeMapped.getMfragExtension();
				RuleModel ruleRelated = nodeMapped.getRuleRelated();
				
				ResidentNodeExtension residentNodeRelated = mappingController.getResidentNodeRelatedToAny(nodeMapped.getEventRelated());				
				
				if(residentNodeRelated != null) {
					try {
						InputNodeExtension inputNode = mappingController.mapToInputNode(causeRelated, mfragRelated, residentNodeRelated);
						mappingController.mapAllEffectsToResident(inputNode, mfragRelated, ruleRelated);
						
						Debug.println("[PLUG-IN EXT] Mapped UndefinedNode "+ ((CauseVariableModel)nodeMapped.getEventRelated()).getRelationship()+"  To Input at "+mfragRelated.getName());
					} catch (OVDontIsOfTypeExpected e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ArgumentNodeAlreadySetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}				
		}			
	}
}
