package unbbayes.io.umpst.implementation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import unbbayes.model.umpst.entity.EntityModel;
import unbbayes.model.umpst.entity.RelationshipModel;
import unbbayes.model.umpst.implementation.CauseVariableModel;
import unbbayes.model.umpst.implementation.EffectVariableModel;
import unbbayes.model.umpst.implementation.EventNCPointer;
import unbbayes.model.umpst.implementation.EventType;
import unbbayes.model.umpst.implementation.EventVariableObjectModel;
import unbbayes.model.umpst.implementation.NecessaryConditionVariableModel;
import unbbayes.model.umpst.implementation.NodeFormulaTreeUMP;
import unbbayes.model.umpst.implementation.OrdinaryVariableModel;
import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.model.umpst.rule.RuleModel;
import unbbayes.prs.mebn.BuiltInRV;
import unbbayes.prs.mebn.context.EnumSubType;
import unbbayes.prs.mebn.context.EnumType;

/**
 * Builder to load rule implementation nodes.
 * 
 * @author Diego Marques
 *
 */
public class FileLoadRuleImplementation {
	
	private List<OrdinaryVariableModel> ordinaryVariableList;
	private List<CauseVariableModel> causeVariableList;
	private List<EffectVariableModel> effectVariableList;
	private List<NecessaryConditionVariableModel> necessaryConditionList;
	
	private List<EventVariableObjectModel> othersEventVariableList;
	private List<RelationshipModel> relationshipModelList;
	
	private NodeFormulaTreeUMP rootFormula;
	private List<NodeFormulaTreeUMP> nodeFormulaFather;
	private BuiltInRV builtInRV = null;
	private int index;

	public void loadOVNode (RuleModel rule, NodeList ovNodeList) {
		
		ordinaryVariableList = new ArrayList<OrdinaryVariableModel>();
		
		NodeList btOVNodeList = ovNodeList.item(0).getChildNodes();
		Element btOVElemList = (Element) btOVNodeList;
		
		NodeList listOV = btOVElemList.getElementsByTagName("ordinaryVariable");
		for (int j = 0; j < listOV.getLength(); j++) {
			
			NodeList ovNode = listOV.item(j).getChildNodes();
			Element btOVElem = (Element) ovNode;
			
			String ovId = btOVElem.getElementsByTagName("ovId").item(0).getTextContent();
			String ovInstance = btOVElem.getElementsByTagName("ovInstance").item(0).
					getTextContent();
			String entityId = btOVElem.getElementsByTagName("entityId").item(0).
					getTextContent();
						
			EntityModel entity = searchEntityModel(rule, entityId);
			OrdinaryVariableModel ov = new OrdinaryVariableModel(ovId, ovInstance,
					entity.getName(), entity);
			ordinaryVariableList.add(ov);
		}
		
	}
	
	public void loadNCNode (UMPSTProject umpstProject, RuleModel rule, NodeList ncNodeList) {
		
		necessaryConditionList = new ArrayList<NecessaryConditionVariableModel>();
		createOthersEventVariable(umpstProject, rule);
		
		NodeList btNCNodeList = ncNodeList.item(0).getChildNodes();
		Element btNCElemList = (Element) btNCNodeList;
		
		NodeList listNC = btNCElemList.getElementsByTagName("necessaryCondition");
		for (int j = 0; j < listNC.getLength(); j++) {
			rootFormula = null;
			nodeFormulaFather = new ArrayList<NodeFormulaTreeUMP>();
			index = -1;
			
			NodeList ncNode = listNC.item(j).getChildNodes();
			Element btNCElem = (Element) ncNode;
			
			String ncId = btNCElem.getElementsByTagName("ncId").item(0).
					getTextContent();
			
			NodeList listNCNodeFormulaTreeUMP = btNCElem.getElementsByTagName("ncNodeFormulaTreeUMP");
			NodeList ncNodeFormulaTreeUMP = listNCNodeFormulaTreeUMP.item(0).getChildNodes();
			Element btNCNodeFormulaTreeUMPElem = (Element) ncNodeFormulaTreeUMP;
			
			// Root
			NodeList listRootNode = btNCNodeFormulaTreeUMPElem.getElementsByTagName("ncNode");
			for (int i = 0; i < listRootNode.getLength(); i++) {
				NodeList listRootChild = listRootNode.item(i).getChildNodes();
				
				NodeFormulaTreeUMP nodeFormula = loaderNCNode(rule, listRootChild);
				if (nodeFormula.getNodeVariable() instanceof BuiltInRV) {
					nodeFormulaFather.add(nodeFormula);
					index += 1;
					
				} else {
					
					if (nodeFormulaFather.size() > 0) {	
						if (((BuiltInRV)nodeFormulaFather.get(index).getNodeVariable()).getNumOperandos() ==
								nodeFormulaFather.get(index).getChildrenUMP().size()) {
							index -= 1;							
						}
						addChildNode(nodeFormula);
					} else {
						rootFormula = nodeFormula;
					}
				}
			}
			buildTree();
			NecessaryConditionVariableModel necessaryCondition = new NecessaryConditionVariableModel(ncId, rootFormula);
			necessaryCondition.setFormula(rootFormula.getFormulaViewText());
			necessaryConditionList.add(necessaryCondition);
		}
	}
	
	public void buildTree() {	
		for (int i = 0; i < nodeFormulaFather.size(); i++) {
			if (rootFormula == null) {
				rootFormula = nodeFormulaFather.get(i);
			} else {
				rootFormula.addChild(nodeFormulaFather.get(i));
			}
		}
	}
	
	public void addChildNode(NodeFormulaTreeUMP nodeFormula) {
		if ((nodeFormulaFather.size() > 0) && (index > -1)) {
			if (index < nodeFormulaFather.size()-1) {
				if (((BuiltInRV)nodeFormulaFather.get(index).getNodeVariable()).getNumOperandos() ==
						nodeFormulaFather.get(index).getChildrenUMP().size()+1) {
					index -= 1;
					addChildNode(nodeFormula);
				} else {
					nodeFormulaFather.get(index).addChild(nodeFormula);
				}
			} else {
				if (((BuiltInRV)nodeFormulaFather.get(index).getNodeVariable()).getNumOperandos() ==
						nodeFormulaFather.get(index).getChildrenUMP().size()) {
					nodeFormulaFather.get(index).addChild(nodeFormula);
					index -= 1;
					addChildNode(nodeFormula);
				} else {
					nodeFormulaFather.get(index).addChild(nodeFormula);
				}
			}
		}
	}
	
	public NodeFormulaTreeUMP loaderNCNode(RuleModel rule, NodeList listNCNode) {
		Element btNCNodeElem = (Element) listNCNode;		
		
		String ncNodeName = btNCNodeElem.getElementsByTagName("ncNodeName").item(0).
				getTextContent();
		
		NodeList ncNodeMnemonicList = btNCNodeElem.getElementsByTagName("ncNodeMnemonic");
		String ncNodeMnemonic = null;
		if (ncNodeMnemonicList.getLength() > 0) {
			ncNodeMnemonic = btNCNodeElem.getElementsByTagName("ncNodeMnemonic").
					item(0).getTextContent();
		}
		
		NodeList ncChildNodeVariable = null;
		Object nodeVariableObject = null;
		ArrayList<OrdinaryVariableModel> ovArgumentList = null;
		EventNCPointer eventPointer = null;
		
		Node ncNodeVariable = btNCNodeElem.getElementsByTagName("ncNodeVariable").item(0);
		if (ncNodeVariable.getChildNodes().getLength() > 1) {
			
			// Variable or Operand Node
			ncChildNodeVariable = ncNodeVariable.getChildNodes();
			Element ncVariableElem = (Element) ncChildNodeVariable;
			Node nodeVariable = ncVariableElem.getElementsByTagName("ncNodeVariableOV").item(0);
			if (nodeVariable != null) {
				// OV
				Element nodeVariableElem = (Element) nodeVariable;
				String typeNode = nodeVariableElem.getTextContent();
				nodeVariableObject = searchOrdinaryVariableModel(ncNodeName, typeNode);
				
			} else {
				// Relationship Node
				nodeVariable = ncVariableElem.getElementsByTagName("ncNodeVariableEventId").
						item(0);
				Element nodeVariableElem = (Element) nodeVariable;
				String eventId = nodeVariableElem.getTextContent();
				
				Node nodeVariableEventType = ncVariableElem.getElementsByTagName(
						"ncNodeVariableEventType").item(0);
				Element eventTypeElem = (Element) nodeVariableEventType;
				String eventType = eventTypeElem.getTextContent();
				
				Node ncNodeVariableEventOV = ncVariableElem.getElementsByTagName("ncNodeVariableEventOVList").item(0);
				Element nodeVariableOVElem = (Element) ncNodeVariableEventOV;
				
				ovArgumentList = new ArrayList<OrdinaryVariableModel>();
				NodeList ncNodeVariableEventOVIdList = nodeVariableOVElem.getElementsByTagName("ncNodeVariableEventOVId");
				for (int i = 0; i < ncNodeVariableEventOVIdList.getLength(); i++) {
					Node nodeOvId = ncNodeVariableEventOVIdList.item(i);
					Element ovIdElem = (Element) nodeOvId;
					String ovId = ovIdElem.getTextContent();					
					ovArgumentList.add(searchOrdinaryVariableModel(ovId));
				}
				
				EventVariableObjectModel eventVariable = searchEventVariableModel(rule, eventId, eventType);
				eventPointer = new EventNCPointer(null, null, eventVariable);
				eventPointer.setOvArgumentList(ovArgumentList);
				
				nodeVariableObject = eventPointer;
			}
			
		} else {
			builtInRV = new BuiltInRV(ncNodeName, ncNodeMnemonic);
			nodeVariableObject = builtInRV;
		}
		
		String ncNodeNumOperands = null;
		Node nodeNumOperands = btNCNodeElem.getElementsByTagName("ncNodeVariableNumOperands").item(0);
		if (nodeNumOperands != null) {
			ncNodeNumOperands = btNCNodeElem.getElementsByTagName("ncNodeVariableNumOperands").item(0).
					getTextContent();
		}
		String ncNodeTypeNode = btNCNodeElem.getElementsByTagName("ncNodeTypeNode").item(0).
				getTextContent();		
		String ncNodeSubTypeNode = btNCNodeElem.getElementsByTagName("ncNodeSubTypeNode").item(0).
				getTextContent();
		
		
		NodeFormulaTreeUMP nodeFormula;
		if (nodeVariableObject instanceof BuiltInRV) {
			nodeFormula = new NodeFormulaTreeUMP(ncNodeName, EnumType.valueOf(ncNodeTypeNode),
					EnumSubType.valueOf(ncNodeSubTypeNode), nodeVariableObject);
			nodeFormula.setMnemonic(builtInRV.getMnemonic());
			((BuiltInRV)nodeFormula.getNodeVariable()).setNumOperandos(
					Integer.parseInt(ncNodeNumOperands));
		} else {
			nodeFormula = new NodeFormulaTreeUMP(ncNodeName, EnumType.valueOf(ncNodeTypeNode),
					EnumSubType.valueOf(ncNodeSubTypeNode), nodeVariableObject);
		}
		
		return nodeFormula;
	}
	
	public void loadCauseNode (RuleModel rule, NodeList causeNodeList, List<RuleModel> ruleChildrenList) {
		
		causeVariableList = new ArrayList<CauseVariableModel>();
		ArrayList<OrdinaryVariableModel> ovArgumentList;
		createAllRelationshipModelList(rule, ruleChildrenList);
		
		NodeList btCauseNodeList = causeNodeList.item(0).getChildNodes();
		Element btCauseElemList = (Element) btCauseNodeList;
		
		NodeList listCause = btCauseElemList.getElementsByTagName("causeVariable");
		for (int j = 0; j < listCause.getLength(); j++) {
			
			NodeList causeNode = listCause.item(j).getChildNodes();
			Element btCauseElem = (Element) causeNode;
			
			String causeId = btCauseElem.getElementsByTagName("causeId").item(0).
					getTextContent();
			String causeRelationship = btCauseElem.getElementsByTagName("causeRelationship").item(0).
					getTextContent();
			
			// Argument List
			NodeList listCauseArgumentList = btCauseElem.getElementsByTagName("causeArgumentList");
			NodeList listCauseArgument = listCauseArgumentList.item(0).getChildNodes();
			Element btListCauseElem = (Element) listCauseArgument;
			
			NodeList listArgument = btListCauseElem.getElementsByTagName("causeArgument");
			ovArgumentList = new ArrayList<OrdinaryVariableModel>();
			for (int i = 0; i < listArgument.getLength(); i++) {		
				Node argumentNode = listArgument.item(i);
				Element btArgumentElem = (Element) argumentNode;
				
				String causeArgument = btArgumentElem.getTextContent();
				
				// Select OrdinaryVariable
				for (int k = 0; k < ordinaryVariableList.size(); k++) {
					if (causeArgument.equals(ordinaryVariableList.get(k).getVariable())) {
						ovArgumentList.add(ordinaryVariableList.get(k));
					}
				}
			}
			
			// Cause Relationship Model
			Node causeRelationshipModel = btCauseElemList.getElementsByTagName("causeRelationshipModel").item(j);
			Element btCauseRelationshipModelElem = (Element) causeRelationshipModel;
			
			String relationshipId = btCauseRelationshipModelElem.getElementsByTagName(
					"relationshipId").item(0).getTextContent();			
			
			// Set CauseVariable
			CauseVariableModel causeVariable = new CauseVariableModel(causeId);
			causeVariable.setRelationship(causeRelationship);
			causeVariable.setOvArgumentList(ovArgumentList);
			causeVariable.setRelationshipModel(
					searchRelationshipModel(rule, relationshipId));
			
			causeVariableList.add(causeVariable);
		}
	}
	
	public void loadEffectNode (RuleModel rule, NodeList effectNodeList, List<RuleModel> ruleChildrenList) {
		
		effectVariableList = new ArrayList<EffectVariableModel>();		
		ArrayList<OrdinaryVariableModel> ovArgumentList;
		createAllRelationshipModelList(rule, ruleChildrenList);
		
		NodeList btEffectNodeList = effectNodeList.item(0).getChildNodes();
		Element btEffectElemList = (Element) btEffectNodeList;
		
		NodeList listEffect = btEffectElemList.getElementsByTagName("effectVariable");
		for (int j = 0; j < listEffect.getLength(); j++) {
			
			NodeList effectNode = listEffect.item(j).getChildNodes();
			Element btEffectElem = (Element) effectNode;
			
			String effectId = btEffectElem.getElementsByTagName("effectId").item(0).
					getTextContent();
			String effectRelationship = btEffectElem.getElementsByTagName("effectRelationship").item(0).
					getTextContent();
			
			// Argument List
			NodeList listCauseArgumentList = btEffectElem.getElementsByTagName("effectArgumentList");
			NodeList listCauseArgument = listCauseArgumentList.item(0).getChildNodes();
			Element btListEffectElem = (Element) listCauseArgument;
			
			NodeList listArgument = btListEffectElem.getElementsByTagName("effectArgument");
			ovArgumentList = new ArrayList<OrdinaryVariableModel>();
			for (int i = 0; i < listArgument.getLength(); i++) {		
				Node argumentNode = listArgument.item(i);
				Element btArgumentElem = (Element) argumentNode;
				
				String effectArgument = btArgumentElem.getTextContent();
				
				// Select OrdinaryVariable
				for (int k = 0; k < ordinaryVariableList.size(); k++) {
					if (effectArgument.equals(ordinaryVariableList.get(k).getVariable())) {
						ovArgumentList.add(ordinaryVariableList.get(k));
					}
				}
			}
			
			// Effect Relationship Model
			Node effectRelationshipModel = btEffectElemList.getElementsByTagName("effectRelationshipModel").item(j);
			Element btEffectRelationshipModelElem = (Element) effectRelationshipModel;
			
			String relationshipId = btEffectRelationshipModelElem.getElementsByTagName(
					"relationshipId").item(0).getTextContent();
			
			// Set EffectVariable
			EffectVariableModel effectVariable = new EffectVariableModel(effectId);
			effectVariable.setRelationship(effectRelationship);
			effectVariable.setOvArgumentList(ovArgumentList);
			effectVariable.setRelationshipModel(
					searchRelationshipModel(rule, relationshipId));
			
			effectVariableList.add(effectVariable);
		}
	}
	

	/**
	 * All relationship from umpstProject that have the same entities
	 * present in rule ordinaryVariableList.
	 */
	public void createOthersEventVariable(UMPSTProject umpstProject, RuleModel rule) {
		
		Map<String, RelationshipModel> relationshipMap = new HashMap<String, RelationshipModel>(); 
		relationshipMap = umpstProject.getMapRelationship();
				
		othersEventVariableList = new ArrayList<EventVariableObjectModel>();
		
		Set<String> keys = relationshipMap.keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);
		
		for (String key : sortedKeys) {
			
			if (canParticipate(relationshipMap.get(key), rule)) {
				String id = relationshipMap.get(key).getId();
				String name = relationshipMap.get(key).getName();
			
				EventVariableObjectModel event = new EventVariableObjectModel(id, EventType.OTHER);
				event.setRelationship(name);
				event.setRelationshipModel(relationshipMap.get(key));
				
				othersEventVariableList.add(event);
			}
		}
	}
	
	/**
	 * This function evaluates if relationshipModel can be eventVariableObjectModel.
	 * If the relationship has as argument variables related to the Entities present
	 * in Ordinary Variable, so the relationship can be an event that participates in
	 * the model. 
	 * @param relationship
	 * @return
	 */
	public boolean canParticipate(RelationshipModel relationship, RuleModel rule) {		
		
		List<OrdinaryVariableModel> ordinaryVariableList = new ArrayList<OrdinaryVariableModel>();
		ordinaryVariableList = rule.getOrdinaryVariableList();
		
		int flag = 0;
		
		for (int i = 0; i < relationship.getEntityList().size(); i++) {
			EntityModel entity = relationship.getEntityList().get(i);

			for (int j = 0; j < rule.getOrdinaryVariableList().size(); j++) {			
				if (entity.getId().equals(rule.getOrdinaryVariableList().get(j).getEntityObject().getId())) {
					flag++;
				}
			}
		}
		if (flag >= relationship.getEntityList().size()) {
			return true;
		}		
		return true;
	}
	
	public EventVariableObjectModel searchEventVariableModel(RuleModel rule, String eventId, String eventType) {
		
		for (int i = 0; i < othersEventVariableList.size(); i++) {
			if ((othersEventVariableList.get(i).getId().equals(eventId)) &&
					(othersEventVariableList.get(i).getTypeEvent().equals(EventType.valueOf(eventType)))) {
				return othersEventVariableList.get(i); 
			}
		}
		System.err.println("Null pointer. Event not found.");
		return null;
	}
	
	/**
	 * Search OV by id.
	 * @param id
	 * @return
	 */
	public OrdinaryVariableModel searchOrdinaryVariableModel(String id) {
		
		for (int i = 0; i < getOrdinaryVariableList().size(); i++) {
			if (getOrdinaryVariableList().get(i).getId().equals(id)){
				return getOrdinaryVariableList().get(i);
			}
		}
		System.err.println("Null pointer. OV not found.");
		return null;
	}
	
	/**
	 * Search OV by instance and typeNode.
	 * @param instance
	 * @param typeNode
	 * @return
	 */
	public OrdinaryVariableModel searchOrdinaryVariableModel(String instance, String typeNode) {
		
		for (int i = 0; i < getOrdinaryVariableList().size(); i++) {
			if ((getOrdinaryVariableList().get(i).getVariable().equals(instance)) &&
					(getOrdinaryVariableList().get(i).getTypeEntity().equals(typeNode))){
				return getOrdinaryVariableList().get(i);
			}
		}
		System.err.println("Null pointer. OV not found.");
		return null;
	}
	
	/**
	 * All relationship model from rule and their children.
	 * @param rule
	 * @param id
	 * @return
	 */
	public void createAllRelationshipModelList(RuleModel rule, List<RuleModel> ruleChildrenList) {
		
		relationshipModelList = new ArrayList<RelationshipModel>();
		
		if (ruleChildrenList.size() > 0) {
			for (int i = 0; i < ruleChildrenList.size(); i++) {
				
				RuleModel ruleChild = ruleChildrenList.get(i);
				for (int j = 0; j < ruleChild.getRelationshipList().size(); j++) {
					relationshipModelList.add(ruleChild.getRelationshipList().get(j));
				}
			}
		}
		
		for (int i = 0; i < rule.getRelationshipList().size(); i++) {
			relationshipModelList.add(rule.getRelationshipList().get(i));
		}
	}
	
	public RelationshipModel searchRelationshipModel(RuleModel rule, String id) {
		
		for (int i = 0; i < relationshipModelList.size(); i++) {
			if (relationshipModelList.get(i).getId().equals(id)) {
				return relationshipModelList.get(i);
			}
		}
		System.err.println("Null pointer. RelatinshipModel not found." + " | Id: " + id);
		return null;
	}
	
	public EntityModel searchEntityModel(RuleModel rule, String id) {
		for (int i = 0; i < rule.getEntityList().size(); i++) {
			if (rule.getEntityList().get(i).getId().equals(id)) {
				return rule.getEntityList().get(i);
			}
		}
		System.err.println("Null pointer. Entity not found.");
		return null;
	}	

	/**
	 * @return the ordinaryVariableList
	 */
	public List<OrdinaryVariableModel> getOrdinaryVariableList() {
		return ordinaryVariableList;
	}

	/**
	 * @param ordinaryVariableList the ordinaryVariableList to set
	 */
	public void setOrdinaryVariableList(List<OrdinaryVariableModel> ordinaryVariableList) {
		this.ordinaryVariableList = ordinaryVariableList;
	}

	/**
	 * @return the causeVariableList
	 */
	public List<CauseVariableModel> getCauseVariableList() {
		return causeVariableList;
	}

	/**
	 * @param causeVariableList the causeVariableList to set
	 */
	public void setCauseVariableList(List<CauseVariableModel> causeVariableList) {
		this.causeVariableList = causeVariableList;
	}

	/**
	 * @return the effectVariableList
	 */
	public List<EffectVariableModel> getEffectVariableList() {
		return effectVariableList;
	}

	/**
	 * @param effectVariableList the effectVariableList to set
	 */
	public void setEffectVariableList(List<EffectVariableModel> effectVariableList) {
		this.effectVariableList = effectVariableList;
	}

	/**
	 * @return the necessaryConditionList
	 */
	public List<NecessaryConditionVariableModel> getNecessaryConditionList() {
		return necessaryConditionList;
	}

	/**
	 * @param necessaryConditionList the necessaryConditionList to set
	 */
	public void setNecessaryConditionList(List<NecessaryConditionVariableModel> necessaryConditionList) {
		this.necessaryConditionList = necessaryConditionList;
	}

}
