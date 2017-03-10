package unbbayes.model.umpst.implementation.algorithm;

import java.security.acl.Group;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.hp.hpl.jena.shared.RulesetNotFoundException;

import unbbayes.controller.umpst.MappingController;
import unbbayes.model.umpst.entity.RelationshipModel;
import unbbayes.model.umpst.group.GroupModel;
import unbbayes.model.umpst.implementation.CauseVariableModel;
import unbbayes.model.umpst.implementation.EffectVariableModel;
import unbbayes.model.umpst.implementation.EventNCPointer;
import unbbayes.model.umpst.implementation.NecessaryConditionVariableModel;
import unbbayes.model.umpst.implementation.NodeFormulaTreeUMP;
import unbbayes.model.umpst.implementation.OrdinaryVariableModel;
import unbbayes.model.umpst.implementation.node.InputNodeExtension;
import unbbayes.model.umpst.implementation.node.MFragExtension;
import unbbayes.model.umpst.implementation.node.NodeInputModel;
import unbbayes.model.umpst.implementation.node.NodeType;
import unbbayes.model.umpst.implementation.node.ResidentNodeExtension;
import unbbayes.model.umpst.implementation.node.UndefinedNode;
import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.model.umpst.rule.RuleModel;
import unbbayes.prs.Edge;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.context.EnumType;
import unbbayes.prs.mebn.exception.ArgumentNodeAlreadySetException;
import unbbayes.prs.mebn.exception.CycleFoundException;
import unbbayes.prs.mebn.exception.MEBNConstructionException;
import unbbayes.prs.mebn.exception.OVDontIsOfTypeExpected;
import unbbayes.prs.mebn.exception.OVariableAlreadyExistsInArgumentList;
import unbbayes.util.Debug;

/**
 * This class deals with causal relation related to rule. This rule is in a group.
 * @param rule
 * @param group
 */
public class DefineDependenceRelation {
	
	private RuleModel rule;
	private GroupModel group;
	private GroupModel groupRelatedToEffect;
	private EffectVariableModel effectRelatedToCause;
	private Map<String, GroupModel> mapGroup;
	
	private MFragExtension mfragExtensionActive;	
	private MappingController mappingController;
	private SecondCriterionOfSelection secondCriterion;
	
	private UMPSTProject umpstProject;
	
	public DefineDependenceRelation(RuleModel rule, GroupModel group, MFragExtension mfrag, MappingController mappingController,
			UMPSTProject umpstProject, SecondCriterionOfSelection secondCriterion) {
		
		this.rule = rule;
		this.group = group;
		this.mfragExtensionActive = mfrag;
		this.mappingController = mappingController;
		this.umpstProject = umpstProject;
		this.secondCriterion = secondCriterion;
		
		try {
			mapCausalRelation();
		} catch (ArgumentNodeAlreadySetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OVariableAlreadyExistsInArgumentList e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidParentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		mapMissingRelationships();
	}
	
	/**
	 * For each {@link RuleModel} maps the {@link CauseVariableModel} and {@link EffectVariableModel} to 
	 * {@link ResidentNodeExtension} or {@link InputNodeExtension}.
	 * 
	 * @throws ArgumentNodeAlreadySetException
	 * @throws OVariableAlreadyExistsInArgumentList
	 * @throws InvalidParentException
	 */
	public void mapCausalRelation() throws ArgumentNodeAlreadySetException,
		OVariableAlreadyExistsInArgumentList, InvalidParentException {
		
		for (int j = 0; j < rule.getCauseVariableList().size(); j++) {
			CauseVariableModel cause = rule.getCauseVariableList().get(j);

			/**
			 * Cause was mapped as resident node in first criterion of selection. Search for a node
			 * that was mapped from cause statement
			 */
			ResidentNodeExtension residentNode = mappingController.getResidentNodeRelatedToCauseIn(
					cause, mfragExtensionActive);
			
			/**
			 * Also it is possible to exist resident nodes that were mapped in first criterion and
			 * it is effect. So, these kind of node will not have its arguments.
			 */
			if (residentNode != null) {
				
				/**
				 * The First Criterion of Selection does not add the arguments related to resident node
				 * created.
				 */
				residentNode = mappingController.mapResidentNodeArgument(cause, residentNode, mfragExtensionActive);
				mappingController.mapAllEffectsToResident(residentNode, mfragExtensionActive, rule);
			}
			/**
			 * Cause is Effect in a rule
			 */
			else if ((residentNode == null) && (containsCauseRelatedToEffect(cause, umpstProject))) {				
				
				/**
				 * Verify if the input node related to the cause has an instance related to the resident node created
				 * from the effect searched
				 */
				
				MFragExtension mfragRelatedEffect = mappingController.getMFragRelatedToGroup(
						getGroupRelatedToEffect());
				EffectVariableModel effectRelatedToCause = getEffectRelatedToCause();
				
				ResidentNodeExtension residentNodeRelated = mappingController.getResidentNodeRelatedToEffectIn(
						effectRelatedToCause, mfragRelatedEffect);
				
				if(residentNodeRelated != null) {
					/**
					 * The resident node needs to have ordinary variables because during the mapping process the residentPointer
					 * will be setted as the instance and its arguments will be the input node argument.
					 */
//					if(residentNodeRelated.getOrdinaryVariableList().size() > 0) {
						try {							
							InputNodeExtension inputNode = mappingController.mapToInputNode(cause, mfragExtensionActive, residentNodeRelated);
	//							inputNode.updateResidentNodePointer();
							mappingController.mapAllEffectsToResident(inputNode, mfragExtensionActive, rule);
							
						} catch (OVDontIsOfTypeExpected e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
//					}
				}
				else {
					// ATTENTION
					// Map the effect related to the cause to resident.
					residentNodeRelated = mappingController.mapToResidentNode(
							effectRelatedToCause.getRelationshipModel(), mfragRelatedEffect, effectRelatedToCause);
					
					System.out.println("=====");
					System.out.println("Cause->UN: "+cause.getRelationship()+ " Effect->Resident: "+residentNodeRelated.getName() +" of "+mfragRelatedEffect.getName());
					
					// Include the cause in undefinedNode list because then the node will be mapped to input.
					UndefinedNode undefinedNode = new UndefinedNode(cause, mfragExtensionActive);
					// Add the rule related to the causeVariable
					undefinedNode.setRuleRelated(rule);
					mappingController.getUndefinedNodeList().add(undefinedNode);
					
					Debug.println("[PLUG-IN EXT] UndefinedNode added "+((CauseVariableModel)undefinedNode.getEventRelated()).getRelationship()+ " at "+undefinedNode.getMfragExtension().getName());
				}
			}
			
			/**
			 * If the cause variable is not presented in group as effect, then it will be mapped to a
			 * undefined node.
			 */
			else {
				UndefinedNode undefinedNode = new UndefinedNode(cause, mfragExtensionActive);
				// Add the rule related to the causeVariable
				undefinedNode.setRuleRelated(rule);
				mappingController.getUndefinedNodeList().add(undefinedNode);
				Debug.println("[PLUG-IN EXT] UndefinedNode added "+((CauseVariableModel)undefinedNode.getEventRelated()).getRelationship()+" at "+undefinedNode.getMfragExtension().getName());
			}
//			// Verify if there are nodes that were not mapped.
//			// Usually, these are nodes that were not mapped in other rule as effect.
//			else if(searchInputNode(cause) == null) {
//				String key = mfragSelected.getId();
//				mapCauseAsNotDefined(cause);
//				
//				secondCriterion.getMapDoubtNodes().put(key, notDefinedNode);
//			}
		}
	}
	
//	public void mapMissingRelationships() {
//		List<RelationshipModel> listMissingRelationship = searchMissingRelationship();
//		
//		for (int i = 0; i < listMissingRelationship.size(); i++) {
//			System.out.println(listMissingRelationship.get(i).getName());
//		}		
//	}
	
	/**
	 * Verify if there are relationships that were not defined in rule definition as cause or effect.
	 * If it exist, then it is added in list of missing relationships.
	 * @return List<RelationshipModel>
	 */
//	public List<RelationshipModel> searchMissingRelationship() {
//		List<RelationshipModel> listMissingRelationship = new ArrayList<RelationshipModel>();
//		boolean exist;
//		
//		for (int i = 0; i < group.getBacktrackingRelationship().size(); i++) {
//			RelationshipModel relationshipOfGroup = group.
//					getBacktrackingRelationship().get(i);			
//			exist = false;
//			
//			for (int j = 0; j < rule.getCauseVariableList().size(); j++) {
//				CauseVariableModel causeVar = rule.getCauseVariableList().get(j);				
//				RelationshipModel relationshipOfCause = causeVar.getRelationshipModel();
//				
//				if (relationshipOfGroup.equals(relationshipOfCause)) {
//					exist = true;
//					break;
//				}
//			}
//			for (int j = 0; j < rule.getEffectVariableList().size(); j++) {
//				EffectVariableModel effectVar = rule.getEffectVariableList().get(j);				
//				RelationshipModel relationshipOfEffect = effectVar.getRelationshipModel();
//				
//				if (relationshipOfGroup.equals(relationshipOfEffect)) {
//					exist = true;
//					break;
//				}
//			}
//			
//			// TODO recognize structures like isProcurementFinished(proc)^(enterprise = hasWinnerOfProcurement(proc))
//			exist = existsAsNecessaryCondition(relationshipOfGroup);
//				
//			
////			for (int j = 0; j < rule.getRelationshipList().size(); j++) {								
////				RelationshipModel relationshipOfRule = rule.getRelationshipList().get(j);
////				
////				if (relationshipOfGroup.equals(relationshipOfRule)) {
////					exist = true;
////					break;
////				}
////			}
//			if (!exist) {
////				System.out.println("--"+group.getName()+"--");
////				System.out.println(relationshipOfGroup.getName());
//				listMissingRelationship.add(relationshipOfGroup);
//			}
//		}
//		return listMissingRelationship;
//	}
	
	// The algorithm maps all relationship as resident node at some MFrag related to group.
//	public boolean existsAsNecessaryCondition(RelationshipModel relationship) {
//		boolean exists = false;		
//		for (int j = 0; j < rule.getNecessaryConditionList().size(); j++) {
//			NecessaryConditionVariableModel nc = rule.getNecessaryConditionList().get(j);			
//			NodeFormulaTreeUMP formulaTree = nc.getFormulaTree();
//			
//			exists = compareNodeFormula(formulaTree, relationship);
//			if (exists) {
////				System.out.println("--"+group.getName()+"--");
////				System.out.println(relationship.getName());
//				return true;
//			}
//		}
//		return false;
//	}
	
//	public boolean compareNodeFormula(NodeFormulaTreeUMP node, RelationshipModel relationship) {
//		if (node.getTypeNode() == EnumType.OPERAND) {
//			EventNCPointer event = (EventNCPointer)node.getNodeVariable();
//			RelationshipModel ncRelationship = event.getEventVariable().getRelationshipModel();
//			if (ncRelationship.equals(relationship)) {
//				return true;
//			}
//			if (node.getChildren().size() > 0) {
//				compareNodeFormula(node.getChildrenUMP().get(0), relationship);			
//			}
//		}
//		return false;
//	}
	
	/**
	 * Keep all the {@link GroupModel} in the model and compare the {@link EffectVariableModel} present in {@link RuleModel}
	 * to the {@link CauseVariableModel}. If these events have the same {@link RelationshipModel}, then the group has one
	 * {@link EffectVariableModel} related to {@link CauseVariableModel} compared.
	 * 
	 * @param cause
	 * @param umpstProject
	 * @return
	 */
	public boolean containsCauseRelatedToEffect(CauseVariableModel cause, UMPSTProject umpstProject) {
		
		/**
		 * Keep all the groups in the project
		 */
		if(umpstProject != null) {
			Map<String, GroupModel> mapGroup = umpstProject.getMapGroups();
			Set<String> keys = mapGroup.keySet();
			TreeSet<String> sortedKeys = new TreeSet<String>(keys);
	
			for (String key : sortedKeys) {
				GroupModel groupSearched = mapGroup.get(key);
				
				/**
				 * Search in a group if there is an effect related to the cause
				 */
//				if (!groupSearched.equals(groupRelated)) {
				
					for (int i = 0; i < groupSearched.getBacktrackingRules().size(); i++) {
				
						RuleModel ruleSearched = groupSearched.getBacktrackingRules().get(i);
						
						/**
						 * Search in all rules related to the group searched if there are effects related
						 * to the cause passed as parameter.
						 */					
						List<EffectVariableModel> effectList = ruleSearched.getEffectVariableList();
						for (int j = 0; j < effectList.size(); j++) {
							
							RelationshipModel relationshipEffect = effectList.get(j).getRelationshipModel();
							RelationshipModel relationshipCause = cause.getRelationshipModel();
							if (relationshipCause.equals(relationshipEffect)) {
								
								/**
								 * Set the effect and group identified
								 */
								setEffectRelatedToCause(effectList.get(j));
								setGroupRelatedToEffect(groupSearched);
								return true;
							}
						}
					}
//				}
			}
		}
		return false;
	}
	
	
	
//	public void mapCauseAsNotDefined(CauseVariableModel cause) {
//		String sentence = cause.getRelationship() + "(";
//		for (int k = 0; k < cause.getArgumentList().size(); k++) {				
//			sentence = sentence + cause.getArgumentList().get(k) + ", ";
//		}
//		int index = sentence.lastIndexOf(", ");
//		sentence = sentence.substring(0, index);
//		sentence = sentence + ")";
//		
//		String id = cause.getId();
//		notDefinedNode = new NodeInputModel(id, sentence, NodeType.NOT_DEFINED, cause);
//	}
	
	/**
	 * Verify if rule defines cause selected as effect and returns the effect related. If rule defines, then
	 * set as resident node the relationship defined as effect.
	 * @param rule
	 * @param cause
	 * @return effect
	 */
//	public EffectVariableModel searchEffect(RuleModel rule, CauseVariableModel cause) {
//		for (int i = 0; i < rule.getEffectVariableList().size(); i++) {
//			EffectVariableModel effect = rule.getEffectVariableList().get(i);
//			if (effect.getRelationshipModel().equals(cause.getRelationshipModel())) {
////				mapEffectAsResident(effect);
//				return effect;
//			}
//		}
//		return null;
//	}
	
	/**
	 * Get {@link RuleModel} that defines as {@link EffectVariableModel} the {@link CauseVariableModel} passed as argument.
	 * Set causeWasEffect variable with the {@link EffectVariableModel} related and set groupWasEffect variable with the {@link GroupModel}
	 * in which this {@link EffectVariableModel} is presented.
	 * 
	 * @param groupSearched
	 * @param cause
	 * @return RuleModel
	 */
//	public RuleModel getRuleOfCauseAndEffectRelation(GroupModel groupSearched, CauseVariableModel cause) {
//		
//		for (int i = 0; i < groupSearched.getBacktrackingRules().size(); i++) {
//			RuleModel rule = groupSearched.getBacktrackingRules().get(i);
//			
//			for (int j = 0; j < rule.getEffectVariableList().size(); j++) {
//				
//				// If the cause it is an effect in other rule, then return the rule that this is presented
//				EffectVariableModel effect = rule.getEffectVariableList().get(j);
//				if (effect.getRelationshipModel().equals(cause.getRelationshipModel())) {
//					setCauseWasEffect(effect);
//					setGroupWasEffect(groupSearched);
//					return rule;
//				}
//			}
//		}
//		setCauseWasEffect(null);
//		setGroupWasEffect(null);
//		return null;
//	}			
	
//	public void mapEffectAsResident(EffectVariableModel effect) {
//		String sentence = effect.getRelationship() + "(";
//		for (int k = 0; k < effect.getArgumentList().size(); k++) {				
//			sentence = sentence + effect.getArgumentList().get(k) + ", ";
//		}
//		int index = sentence.lastIndexOf(", ");
//		sentence = sentence.substring(0, index);
//		sentence = sentence + ")";
//		
//		residentNodeFather.setName(sentence);
//		residentNodeFather.setNodeType(NodeType.RESIDENT);
//		residentNodeFather.setEventVariable(effect);
//	}
	
	/**
	 * @return the groupRelatedToEffect
	 */
	public GroupModel getGroupRelatedToEffect() {
		return groupRelatedToEffect;
	}

	/**
	 * @param groupRelatedToEffect the groupRelatedToEffect to set
	 */
	public void setGroupRelatedToEffect(GroupModel groupRelatedToEffect) {
		this.groupRelatedToEffect = groupRelatedToEffect;
	}

	/**
	 * @return the effectRelatedToCause
	 */
	public EffectVariableModel getEffectRelatedToCause() {
		return effectRelatedToCause;
	}

	/**
	 * @param effectRelatedToCause the effectRelatedToCause to set
	 */
	public void setEffectRelatedToCause(EffectVariableModel effectRelatedToCause) {
		this.effectRelatedToCause = effectRelatedToCause;
	}
	
	/**
	 * Verify if cause relationship defined in rule was mapped as 
	 * input node in MFrag related to rule. If it is, then return
	 * input node identified.
	 * @param cause
	 * @return inputNode
	 */
//	public NodeInputModel searchInputNode(CauseVariableModel cause) {
//		
//		List<NodeInputModel> inputNodeList = mfragSelected.getNodeInputList();
//		
//		for (int i = 0; i < inputNodeList.size(); i++) {
//			
//			// Input node is random variable related to an attribute or relationship.
//			if (inputNodeList.get(i).getEventVariable().getClass()
//					== RelationshipModel.class) {
//				
//				NodeInputModel inputNode = inputNodeList.get(i);
//				RelationshipModel relationshipModel = (RelationshipModel)inputNode.getEventVariable();
//				
//				if (relationshipModel.equals(cause.getRelationshipModel())) {
//					return inputNode;
//				}
//			}
//		}
//		return null;
//			
//	}
}
