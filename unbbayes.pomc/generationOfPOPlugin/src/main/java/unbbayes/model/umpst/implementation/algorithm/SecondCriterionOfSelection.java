package unbbayes.model.umpst.implementation.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import unbbayes.controller.umpst.MappingController;
import unbbayes.model.umpst.ObjectModel;
import unbbayes.model.umpst.entity.EntityModel;
import unbbayes.model.umpst.entity.RelationshipModel;
import unbbayes.model.umpst.exception.IncompatibleEventException;
import unbbayes.model.umpst.exception.IncompatibleRuleForGroupException;
import unbbayes.model.umpst.group.GroupModel;
import unbbayes.model.umpst.implementation.CauseVariableModel;
import unbbayes.model.umpst.implementation.OrdinaryVariableModel;
import unbbayes.model.umpst.implementation.node.InputNodeExtension;
import unbbayes.model.umpst.implementation.node.MFragExtension;
import unbbayes.model.umpst.implementation.node.NodeObjectModel;
import unbbayes.model.umpst.implementation.node.ResidentNodeExtension;
import unbbayes.model.umpst.implementation.node.UndefinedNode;
import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.model.umpst.rule.RuleModel;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.exception.ArgumentNodeAlreadySetException;
import unbbayes.prs.mebn.exception.OVDontIsOfTypeExpected;
import unbbayes.prs.mebn.exception.OVariableAlreadyExistsInArgumentList;

/**
 * This criterion classifies not defined nodes present in MFrags as context, input
 * or resident nodes and set the dependencies between each other.  
 * @author Diego Marques
 */
public class SecondCriterionOfSelection {
	
	private UMPSTProject umpstProject;
	private MappingController mappingController;	
	
	private Map<String, GroupModel> mapGroup;
	private Map<String, RuleModel> mapRule;
	private Map<String, NodeObjectModel> mapDoubtNodes;
	private List<ObjectModel> objectModel;
	
	
	public SecondCriterionOfSelection(MappingController mappingController, MultiEntityBayesianNetwork mebn, UMPSTProject umpstProject)
			throws IncompatibleRuleForGroupException {
		
		this.mappingController = mappingController;
		this.umpstProject = umpstProject;
		secondSelection(mebn);

	}
	
	/**
	 * Second Selection based on Second Criterion Of Selection algorithm.
	 * @throws IncompatibleRuleForGroupException 
	 */
	public void secondSelection(MultiEntityBayesianNetwork mebn) throws IncompatibleRuleForGroupException {		
		
		Map<String, MFragExtension> mapMFragExtension = mappingController.getMapMFragExtension();
		Set<String> keys = mapMFragExtension.keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);
		
		for (String groupId : sortedKeys) {	
			MFragExtension mfragExtension = mapMFragExtension.get(groupId);
			GroupModel group = mfragExtension.getGroupRelated();
			
			/**
			 * == VERIFY RULES
			 * Verify list of rules related to each group and map the causal relation defined
			 */
			defineMfragRelation(group, mfragExtension);			
			
			/**
			 * == RELATIONSHIP WITHOUT RULE
			 * Map relationships that are not defined in a rule. This kind of relationship can be in a group that
			 * has rules.
			 */
			defineMfragInfo(group, mfragExtension);			
		}
		
		/**
		 * == TREAT THE LIST OF DOUBTS 
		 * Verify if there is a list of undefined nodes and map them to resident or input nodes
		 */
		
		treatDoubtCase();
		
		
	}
	
	/**
	 * Verify if there is a list of {@link UndefinedNode} and map them to {@link ResidentNodeExtension} or
	 * {@link InputNodeExtension}
	 * @throws IncompatibleEventException 
	 */
	public void treatDoubtCase() {
		if(mappingController.getUndefinedNodeList().size() > 0) {
			
			List<UndefinedNode> treatedNodeList = new ArrayList<UndefinedNode>();
			
			List<UndefinedNode> undefinedNodeList = mappingController.getUndefinedNodeList();
			for (int i = 0; i < undefinedNodeList.size(); i++) {
				
				UndefinedNode undefinedNode = undefinedNodeList.get(i);
				
				if(undefinedNode.getEventRelated().getClass().equals(CauseVariableModel.class)) {
					// The event is an cause variable
					Object eventRelated = undefinedNode.getEventRelated();
					MFragExtension mfragExtensionRelated = undefinedNode.getMfragExtension();
					
					ResidentNodeExtension residentNodeRelated = mappingController.getResidentNodeRelatedToAny(eventRelated);
					
					if(residentNodeRelated != null) {
						
						// Maps the undefinedNode to inputNode
						try {
							InputNodeExtension inputNode = mappingController.mapToInputNode((CauseVariableModel)eventRelated,
									mfragExtensionRelated, residentNodeRelated);
							
							// Map the effect event related to the cause (mapped to inputNode)
							if(undefinedNode.getRuleRelated() != null) {
								RuleModel ruleRelated = undefinedNode.getRuleRelated();
								try {
									mappingController.mapAllEffectsToResident(inputNode, mfragExtensionRelated, ruleRelated);
								} catch (OVariableAlreadyExistsInArgumentList e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (InvalidParentException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							
							// Remove the undefinedNode to the list of doubts
							treatedNodeList.add(undefinedNode);
//							mappingController.getUndefinedNodeList().remove(undefinedNode);
							
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
			mappingController.updateUndefinedNodeList(treatedNodeList);
		}
	}
	
	/**
	 * Map {@link RelationshipModel} that are not defined in a {@link RuleModel}. This kind of {@link RelationshipModel}
	 * can be in a {@link GroupModel} that has {@link RuleModel}.
	 * @param group
	 * @param mfragExtension
	 */
	public void defineMfragInfo(GroupModel group, MFragExtension mfragExtension) {
		List<RelationshipModel> relationshipModelList = group.getBacktrackingRelationship();
		for (int i = 0; i < relationshipModelList.size(); i++) {
			
			RelationshipModel relationship = relationshipModelList.get(i);
			
			if(!containsRuleRelatedTo(group, relationship)) {		
				
				 /**
				  * First verify if there any residentNode related to the relationshipModel that was
				  * created during the First Criteria of Selection. If it exists, then inserMissingOrdinaryVariable
				  * related to the node. Case it was not created, then map the relationshipModel to residentNode.
				  */
				ResidentNodeExtension residentNode = mappingController.getResidentNodeRelatedTo(relationship, mfragExtension);
				if(residentNode == null) {
					residentNode = mappingController.mapToResidentNode(relationship, mfragExtension, null);
				}
				
				// Insert ordinary variable if the resident node does not have.
				if(residentNode.getOrdinaryVariableList().size() == 0) {
					try {
						insertMissingOrdinaryVariableIn(residentNode, mfragExtension);
					} catch (ArgumentNodeAlreadySetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (OVariableAlreadyExistsInArgumentList e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	/**
	 * Verify list of {@link RuleModel} related to each {@link GroupModel} and map the causal relation defined
	 * @param group
	 * @param mfragExtension
	 * @throws IncompatibleRuleForGroupException 
	 */
	public void defineMfragRelation(GroupModel group, MFragExtension mfragExtension) 
			throws IncompatibleRuleForGroupException {
		List<RuleModel> listRules = new ArrayList<RuleModel>();
		DefineDependenceRelation ruleRelation;
		
		listRules = group.getBacktrackingRules();
		for (int i = 0; i < listRules.size(); i++) {
			
			// Compare if rule and group have the same elements
			RuleModel rule = group.getBacktrackingRules().get(i);
			
			if (compareElements(rule, group)) {
				
				ruleRelation = new DefineDependenceRelation(rule, group, mfragExtension, mappingController,
						umpstProject, this);
				
			} else {
				String msg = "Number of element in rule: " + rule.getId() + " " +
						"does not match with group: " + group.getId();
				
				throw new IncompatibleRuleForGroupException(msg);
//				for (int j = 0; j < objectModel.size(); j++) {
//					System.err.println(objectModel.get(j).getName());
//				}
			}
		}
	}
	
	/**
	 * Verify if the {@link RelationshipModel} is defined in {@link RuleModel} present in the {@link GroupModel}
	 * passed as parameter.
	 * @param group
	 * @param relationship
	 * @return
	 */
	public boolean containsRuleRelatedTo(GroupModel group, RelationshipModel relationship) {
		
		if(relationship.getFowardtrackingRules().size() == 0) {
			return false;
		}
		else {
		
			Set<RuleModel> ruleSet = relationship.getFowardtrackingRules();
			for (RuleModel ruleRelated : ruleSet) {
				
				for (int i = 0; i < group.getBacktrackingRules().size(); i++) {
					RuleModel ruleCompared = group.getBacktrackingRules().get(i);
					
					if(ruleRelated.equals(ruleCompared)) {
						return true;
					}
				}
			}
		}
		return false;
		
	}
	
	
	/**
	 * Add the {@link OrdinaryVariable} related to {@link ResidentNodeExtension} mapped from {@link RelationshipModel}
	 * that was not present in any {@link RuleModel}
	 * @param relationship
	 * @throws OVariableAlreadyExistsInArgumentList 
	 * @throws ArgumentNodeAlreadySetException 
	 */
	public void insertMissingOrdinaryVariableIn(ResidentNodeExtension residentNode, MFragExtension mfragExtension)
			throws ArgumentNodeAlreadySetException, OVariableAlreadyExistsInArgumentList {
		
		RelationshipModel relationship = (RelationshipModel)residentNode.getEventRelated();
		
		for (int i = 0; i < relationship.getEntityList().size(); i++) {
			EntityModel entity = relationship.getEntityList().get(i);
			
			String id = Integer.toString(i);
			String variable = entity.getName().toLowerCase();
			String typeEntity = entity.getName();
			
			OrdinaryVariable ov = null;
			OrdinaryVariableModel ovModel = mfragExtension.getOrdinaryVariableModelByName(variable);
			if(ovModel == null) {
			
				ovModel = new OrdinaryVariableModel(id, variable, typeEntity, entity);
				mfragExtension.getOrdinaryVariablevModelList().add(ovModel);
	//				String name = "isA( " + variable + ", " + typeEntity + " )";
	//			NodeContextModel contextNode = new NodeContextModel(
	//					ov.getId(), name, NodeType.CONTEXT, ov);
				//			mappingController.addContextNodeInMFrag(group.getId(), contextNode);
				
				// Map the ordinaryVariableModel to the ordinaryVariable and add it to the mfragExtension
				ov = mappingController.mapToOrdinaryVariable(ovModel, mfragExtension);
			}
			else {
				// Get the ordinaryVariable by the name of the variable of the ordinaryVariableModel
				ov = mfragExtension.getOrdinaryVariableByName(ovModel.getVariable());
			}
			
			// Add the ordinaryVariable to residentNode
			residentNode.addArgument(ov, true);
		}
	}
	
	/**
	 * Compare if rule is according to group related.
	 * @param rule
	 * @param group
	 * @return
	 */
	public boolean compareElements(RuleModel rule, GroupModel group) {
		int confirm = 0;
		int actflag = 0;
		int lstflag = 0;
		
		objectModel = new ArrayList<ObjectModel>();
		
		// Attributes
		for (int i = 0; i < rule.getAttributeList().size(); i++) {
			for (int j = 0; j < group.getBacktrackingAtributes().size(); j++) {
				String idRuleAtt = rule.getAttributeList().get(i).getId();
				String idGroupAtt = group.getBacktrackingAtributes().get(j).getId();
				if (idRuleAtt.equals(idGroupAtt)) {
					actflag++;
					lstflag = actflag;
					break;
				} else {
					lstflag++;
				}
			}
			if (lstflag > actflag) {
				objectModel.add(rule.getAttributeList().get(i));
			}
		}
		if (actflag >= rule.getAttributeList().size()) {
			confirm++;
		}
		
		// Entities
		actflag = 0;
		lstflag = 0;
		for (int i = 0; i < rule.getEntityList().size(); i++) {
			for (int j = 0; j < group.getBacktrackingEntities().size(); j++) {
				String idRuleEnt = rule.getEntityList().get(i).getId();
				String idGroupEnt = group.getBacktrackingEntities().get(j).getId();
				if (idRuleEnt.equals(idGroupEnt)) {
					actflag++;
					lstflag = actflag;
					break;
				} else {
					lstflag++;
				}
			}		
			if (lstflag > actflag) {
				objectModel.add(rule.getEntityList().get(i));
			}
		}
		if (actflag >= rule.getEntityList().size()) {
			confirm++;
		}
		
		// Relationship
		actflag = 0;
		lstflag = 0;
		for (int i = 0; i < rule.getRelationshipList().size(); i++) {
			for (int j = 0; j < group.getBacktrackingRelationship().size(); j++) {
				String idRuleRel = rule.getRelationshipList().get(i).getId();
				String idGroupRel = group.getBacktrackingRelationship().get(j).getId();
				if (idRuleRel.equals(idGroupRel)) {
					actflag++;
					lstflag = actflag;
					break;
				} else {
					lstflag++;
				}
			}
			if (lstflag > actflag) {
				objectModel.add(rule.getRelationshipList().get(i));
			}
		}
		if (actflag >= rule.getRelationshipList().size()) {
			confirm++;
		}
		
		if (confirm == 3) {
			return true;
		}
		
		return false;		
	}
}
