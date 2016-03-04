package unbbayes.model.umpst.implementation.algorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import unbbayes.controller.umpst.GenerateMTheoryController;
import unbbayes.model.umpst.ObjectModel;
import unbbayes.model.umpst.entity.EntityModel;
import unbbayes.model.umpst.entity.RelationshipModel;
import unbbayes.model.umpst.group.GroupModel;
import unbbayes.model.umpst.implementation.CauseVariableModel;
import unbbayes.model.umpst.implementation.EffectVariableModel;
import unbbayes.model.umpst.implementation.NecessaryConditionVariableModel;
import unbbayes.model.umpst.implementation.OrdinaryVariableModel;
import unbbayes.model.umpst.implementation.node.NodeContextModel;
import unbbayes.model.umpst.implementation.node.NodeInputModel;
import unbbayes.model.umpst.implementation.node.NodeResidentModel;
import unbbayes.model.umpst.implementation.node.NodeType;
import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.model.umpst.rule.RuleModel;
import unbbayes.util.ArrayMap;

/**
 * This criterion classifies not defined nodes present in MFrags in context, input
 * or resident nodes and set the dependencies between each other.  
 * @author Diego Marques
 */
public class SecondCriterionOfSelection {
	
	private UMPSTProject umpstProject;
	private GenerateMTheoryController generateMTheoryController;
	
	private Map<String, GroupModel> mapGroup;
	private Map<String, RuleModel> mapRule;
	private List<ObjectModel> objectModel;
	
	public SecondCriterionOfSelection(UMPSTProject umpstProject,
			GenerateMTheoryController generateMTheoryController) {
		
		this.umpstProject = umpstProject;
		this.generateMTheoryController = generateMTheoryController;
		
		mapGroup = new ArrayMap<String, GroupModel>();
		mapRule = new ArrayMap<String, RuleModel>();
		
		mainSelection();		
	}
	
	/**
	 * Main Selection.
	 */
	public void mainSelection() {
		mapGroup = umpstProject.getMapGroups();
		Set<String> keys = mapGroup.keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);
		
		for (String key : sortedKeys) {
			GroupModel group = mapGroup.get(key);
			
			// This model only supports groups or domains that have just one rule definition.
			if (group.getBacktrackingRules().size() == 1) {
				
				RuleModel rule = group.getBacktrackingRules().get(0);
				if (compareElements(rule, group)) {
					
//					
//					insertContextNode(rule, group);
//					searchOVMissing(rule, group);
//					defineMFragCausal(rule, group);
					
				} else {
					System.err.println("Number of element in rule: " + rule.getId() +
							" " + "does not match with group: " + group.getId());
					
					// Elements of rule that does not check
					for (int i = 0; i < objectModel.size(); i++) {
						System.err.println(objectModel.get(i).getName());
					}
				}
				
			} else if (group.getBacktrackingRules().size() > 1) {
				System.err.println("Error Second Criterio. Number of rules off a bound.");
			} else if (group.getBacktrackingRules().size() == 0) {
				
				// Add missing OrdinaryVariables
//				insertMissingOV(group);
			}
		}
	}
	
	/**
	 * This method deals with causal relation presents in group.
	 * This method deals only with groups that has related rule.
	 * @param rule
	 * @param group
	 */
	public void defineMFragCausal(RuleModel rule, GroupModel group) {
		
		// Select MFrag related to group.
		List<MFragModel> mfrag = generateMTheoryController.getMTheory().getMFragList();
		MFragModel mfragSelected = null;
		for (int i = 0; i < mfrag.size(); i++) {
			if (mfrag.get(i).getId().equals(group.getId())) {
				mfragSelected = mfrag.get(i);
				break;
			}
		}
		
		// Resident nodes selected in first criterion of selection
		List<NodeResidentModel> residentNodeList = mfragSelected.getNodeResidentList();		
		for (int j = 0; j < rule.getCauseVariableList().size(); j++) {						
			CauseVariableModel cause = rule.getCauseVariableList().get(j);
			
			boolean isNotResident = false;
			for (int i = 0; i < residentNodeList.size(); i++) {
				
				// All Nodes selected as resident is RelationshipModel
				if (residentNodeList.get(i).getEventVariable().getClass()
						== RelationshipModel.class) {
					
					NodeResidentModel residentNodeFather = residentNodeList.get(i);
					RelationshipModel relationshipModel = (RelationshipModel)residentNodeFather.getEventVariable();
					
					// Can exist more than one cause with same name, but with different arguments.
					if (relationshipModel.equals(cause.getRelationshipModel())) {
												
						String sentence = cause.getRelationship() + "(";
						for (int k = 0; k < cause.getArgumentList().size(); k++) {				
							sentence = sentence + cause.getArgumentList().get(k) + ", ";
						}
						int index = sentence.lastIndexOf(", ");
						sentence = sentence.substring(0, index);
						sentence = sentence + ")";
						
						residentNodeFather.setName(sentence);
						residentNodeFather.setNodeType(NodeType.RESIDENT_CAUSE);
						residentNodeFather.setEventVariable(cause);
						
						for (int l = 0; l < rule.getEffectVariableList().size(); l++) {
							EffectVariableModel effect = rule.getEffectVariableList().get(l);
							String id = effect.getId();
							
							String sentenceChild = effect.getRelationship() + "(";
							for (int k = 0; k < effect.getArgumentList().size(); k++) {				
								sentenceChild = sentenceChild + effect.getArgumentList().get(k) + ", ";
							}
							int indexChild = sentenceChild.lastIndexOf(", ");
							sentenceChild = sentenceChild.substring(0, indexChild);
							sentenceChild = sentenceChild + ")";					
							
							NodeResidentModel residentNodeChild = new NodeResidentModel(id, sentenceChild, 
									NodeType.RESIDENT, effect);
							residentNodeChild.addFatherNode(residentNodeFather);
							residentNodeFather.addChildrenNode(residentNodeChild);
							
							generateMTheoryController.addNodeResidentInMFrag(group.getId(), residentNodeChild);
						}
						generateMTheoryController.updateNodeResidentInMFrag(group.getId(), residentNodeFather);
					}
				}
			}
		}
		classifyInputNode(rule, group);
	}
	
	public void classifyInputNode(RuleModel rule, GroupModel group) {
		
		List<RuleModel> causeIsEffectList = new ArrayList<RuleModel>();
		
		mapRule = umpstProject.getMapRules();
		Set<String> keys = mapRule.keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);
				
		int flag = 0;
		for (String key : sortedKeys) {
			RuleModel ruleSelected = mapRule.get(key);
			if (!(ruleSelected.getId().equals(rule.getId()))) {
				
				for (int k = 0; k < rule.getCauseVariableList().size(); k++) {
					CauseVariableModel cause = rule.getCauseVariableList().get(k);
				
					for (int j = 0; j < ruleSelected.getEffectVariableList().size(); j++) {	
						EffectVariableModel effect = ruleSelected.getEffectVariableList().get(j);
						
						if (cause.getRelationshipModel().equals(effect.getRelationshipModel())) {
							// cause is effect
							causeIsEffectList.add(ruleSelected);
							flag++;
						}
					}
				}
			}
		}
		
		if (causeIsEffectList.size() > 0) {
			mapRule = umpstProject.getMapRules();
			Set<String> keys2 = mapRule.keySet();
			TreeSet<String> sortedKeys2 = new TreeSet<String>(keys2);
					
			for (String key2 : sortedKeys2) {
				RuleModel ruleSelected = mapRule.get(key2);
			
				for (int i = 0; i < causeIsEffectList.size(); i++) {
					String idCauseIsEffect = causeIsEffectList.get(i).getId();
					
					if (!(ruleSelected.getId().equals(idCauseIsEffect))) {
						
						for (int k = 0; k < ruleSelected.getCauseVariableList().size(); k++) {
							CauseVariableModel cause = ruleSelected.getCauseVariableList().get(k);
							String id = cause.getId();
							
							String sentence = cause.getRelationship() + "(";
							for (int k1 = 0; k1 < cause.getArgumentList().size(); k1++) {				
								sentence = sentence + cause.getArgumentList().get(k1) + ", ";
							}
							int index = sentence.lastIndexOf(", ");
							sentence = sentence.substring(0, index);
							sentence = sentence + ")";
							
							NodeInputModel inputNode = new NodeInputModel(id, sentence, NodeType.INPUT, cause);
							
							List<GroupModel> idGroupList = ruleSelected.getFowardtrackingGroupList();
							if (idGroupList.size() == 1) {
								generateMTheoryController.addNodeInputInMFrag(idGroupList.get(0).getId(), inputNode);
							} else {
								System.err.println("Error classifyInputNode. Rule is in more than one group.");
							}
						}				
					}
				}
			}
		}
	}
	
	public void setOthersResidentNode() {
		
//		// Set resident node as effect event present in rule related to group.
//		for (int i = 0; i < rule.getEffectVariableList().size(); i++) {
//			
//			EffectVariableModel effect = rule.getEffectVariableList().get(i);
//			if (effect.getRelationship() != null) {
//				
//				String id = effect.getId();
//				String sentence = effect.getRelationship() + "(";
//				for (int j = 0; j < effect.getArgumentList().size(); j++) {				
//					sentence = sentence + effect.getArgumentList().get(j) + ", ";
//				}
//				int index = sentence.lastIndexOf(", ");
//				sentence = sentence.substring(0, index);
//				sentence = sentence + ")";
//			
//				NodeResidentModel residentNode = new NodeResidentModel(id, sentence,
//						NodeType.RESIDENT, effect);
//				
//				generateMTheoryController.addNodeResidentInMFrag(group.getId(), residentNode);
//				
//			} else {
//				System.err.println("Error EffectModel, RULE: "+ rule.getId() + 
//						". Effect relationship not define");
//			}
//			
//		}
	}
	
	/**
	 * Insert missing Ordinary Variables as context node in groups that
	 * do not have definition rule.
	 */
	public void insertMissingOV(GroupModel group) {
		OrdinaryVariableModel ov;
		
		for (int i = 0; i < group.getBacktrackingEntities().size(); i++) {
			EntityModel entity = group.getBacktrackingEntities().get(i);
			
			String id = Integer.toString(i);
			String variable = entity.getName().toLowerCase();
			String typeEntity = entity.getName();
			
			ov = new OrdinaryVariableModel(id, variable, typeEntity, entity);
			
			String name = "( " + variable + ", " + typeEntity + " )" ;
			
			NodeContextModel contextNode = new NodeContextModel(
					ov.getId(), name, NodeType.CONTEXT, ov);
			
			generateMTheoryController.addNodeContextInMFrag(group.getId(), contextNode);
		}
	}
	
	/**
	 * Insert missing Ordinary Variables that has rule, but it is not
	 * present.
	 * @param rule
	 * @param model
	 */
	public void searchOVMissing(RuleModel rule, GroupModel group) {
		
		int flag = 0;
		for (int j = 0; j < group.getBacktrackingEntities().size(); j++) {
			for (int i = 0; i < rule.getOrdinaryVariableList().size(); i++) {
				EntityModel entityOV = rule.getOrdinaryVariableList().get(i).getEntityObject();
				EntityModel entityGroup = group.getBacktrackingEntities().get(j);
				
				if (entityOV.getId().equals(entityGroup.getId())) {
					flag++;
					break;
				}
			}
		}
		if (flag < group.getBacktrackingEntities().size()) {
			System.err.println("Error OV definition" + "Missing Entities GROUP :" + group.getId() +
					"Missing Entities RULE :" + rule.getId());
		}
	}
	
	public void insertContextNode(RuleModel rule, GroupModel group) {
		
		// Add OrdinaryVariable as context node.
		for (int i = 0; i < rule.getOrdinaryVariableList().size(); i++) {
			OrdinaryVariableModel ov = rule.getOrdinaryVariableList().get(i);
			String id = ov.getId();		
			String name = "( " + ov.getVariable() + ", " + ov.getTypeEntity() + " )" ;
			
			NodeContextModel contextNode = new NodeContextModel(id, name, NodeType.CONTEXT, ov);
			generateMTheoryController.addNodeContextInMFrag(group.getId(), contextNode);
		}
		
		// Add NecessaryCondtion as context node.
		for (int i = 0; i < rule.getNecessaryConditionList().size(); i++) {
			NecessaryConditionVariableModel nc = rule.getNecessaryConditionList().get(i);
			String id = nc.getId();
			String name = nc.getFormula();
			
			NodeContextModel contextNode = new NodeContextModel(id, name, NodeType.CONTEXT, nc);
			generateMTheoryController.addNodeContextInMFrag(group.getId(), contextNode);
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
	
	/**
	 * Set ID according to the last necessary condition ID created.
	 */
	public String getOVId(RuleModel rule) {
		int id = 0;
		int greaterID = -1;
		boolean beginID = true; // created to set Id = 0
		for (int i = 0; i < rule.getOrdinaryVariableList().size(); i++) {
			if (greaterID < Integer.parseInt(rule.getOrdinaryVariableList().get(i).getId())) {
				greaterID = Integer.parseInt(rule.getOrdinaryVariableList().get(i).getId());
				beginID = false;
			}
		}
		if (!beginID) {
			id = greaterID+1;
			return Integer.toString(id);
		}
		return "0";
	}

}
