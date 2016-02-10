package unbbayes.io.umpst.implementation;

import java.awt.Event;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.tree.DefaultMutableTreeNode;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import unbbayes.gui.umpst.UmpstModule;
import unbbayes.model.umpst.entity.EntityModel;
import unbbayes.model.umpst.entity.RelationshipModel;
import unbbayes.model.umpst.implementation.BuiltInRV;
import unbbayes.model.umpst.implementation.CauseVariableModel;
import unbbayes.model.umpst.implementation.EffectVariableModel;
import unbbayes.model.umpst.implementation.EnumSubType;
import unbbayes.model.umpst.implementation.EnumType;
import unbbayes.model.umpst.implementation.EventNCPointer;
import unbbayes.model.umpst.implementation.EventType;
import unbbayes.model.umpst.implementation.EventVariableObjectModel;
import unbbayes.model.umpst.implementation.NecessaryConditionVariableModel;
import unbbayes.model.umpst.implementation.NodeFormulaTree;
import unbbayes.model.umpst.implementation.OrdinaryVariableModel;
import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.model.umpst.rule.RuleModel;

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
	
	private NodeFormulaTree rootFormula;
	private List<NodeFormulaTree> nodeFormulaFather;
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
			nodeFormulaFather = new ArrayList<NodeFormulaTree>();
			index = -1;
			
			NodeList ncNode = listNC.item(j).getChildNodes();
			Element btNCElem = (Element) ncNode;
			
			String ncId = btNCElem.getElementsByTagName("ncId").item(0).
					getTextContent();
			
			NodeList listNCNodeFormulaTree = btNCElem.getElementsByTagName("ncNodeFormulaTree");
			NodeList ncNodeFormulaTree = listNCNodeFormulaTree.item(0).getChildNodes();
			Element btNCNodeFormulaTreeElem = (Element) ncNodeFormulaTree;
			
			// Root
			NodeList listRootNode = btNCNodeFormulaTreeElem.getElementsByTagName("ncNode");
			for (int i = 0; i < listRootNode.getLength(); i++) {
				NodeList listRootChild = listRootNode.item(i).getChildNodes();
				
				NodeFormulaTree nodeFormula = loaderNCNode(rule, listRootChild);
				if (nodeFormula.getNodeVariable().getClass() == BuiltInRV.class) {
					nodeFormulaFather.add(nodeFormula);
					index += 1;
					
				} else {
					
					if (nodeFormulaFather.size() > 0) {	
						if (((BuiltInRV)nodeFormulaFather.get(index).getNodeVariable()).getNumOperandos() ==
								nodeFormulaFather.get(index).getChildren().size()) {
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
	
	public void addChildNode(NodeFormulaTree nodeFormula) {
		if ((nodeFormulaFather.size() > 0) && (index > -1)) {
			if (index < nodeFormulaFather.size()-1) {
				if (((BuiltInRV)nodeFormulaFather.get(index).getNodeVariable()).getNumOperandos() ==
						nodeFormulaFather.get(index).getChildren().size()+1) {
					index -= 1;
					addChildNode(nodeFormula);
				} else {
					nodeFormulaFather.get(index).addChild(nodeFormula);
				}
			} else {
				if (((BuiltInRV)nodeFormulaFather.get(index).getNodeVariable()).getNumOperandos() ==
						nodeFormulaFather.get(index).getChildren().size()) {
					nodeFormulaFather.get(index).addChild(nodeFormula);
					index -= 1;
					addChildNode(nodeFormula);
				} else {
					nodeFormulaFather.get(index).addChild(nodeFormula);
				}
			}
		}
	}
	
	public NodeFormulaTree loaderNCNode(RuleModel rule, NodeList listNCNode) {
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
		
		
		NodeFormulaTree nodeFormula;
		if (nodeVariableObject.getClass() == BuiltInRV.class) {
			nodeFormula = new NodeFormulaTree(ncNodeName, EnumType.valueOf(ncNodeTypeNode),
					EnumSubType.valueOf(ncNodeSubTypeNode), nodeVariableObject);
			nodeFormula.setMnemonic(builtInRV.getMnemonic());
			((BuiltInRV)nodeFormula.getNodeVariable()).setNumOperandos(
					Integer.parseInt(ncNodeNumOperands));
		} else {
			nodeFormula = new NodeFormulaTree(ncNodeName, EnumType.valueOf(ncNodeTypeNode),
					EnumSubType.valueOf(ncNodeSubTypeNode), nodeVariableObject);
		}
		
		return nodeFormula;
	}
	
	public void loadCauseNode (RuleModel rule, NodeList causeNodeList) {
		
		causeVariableList = new ArrayList<CauseVariableModel>();
		ArrayList<String> argumentList;
		
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
			argumentList = new ArrayList<String>();
			for (int i = 0; i < listArgument.getLength(); i++) {		
				Node argumentNode = listArgument.item(i);
				Element btArgumentElem = (Element) argumentNode;
				
				String causeArgument = btArgumentElem.getTextContent();
				argumentList.add(causeArgument);
			}
			
			// Cause Relationship Model
			Node causeRelationshipModel = btCauseElemList.getElementsByTagName("causeRelationshipModel").item(j);
			Element btCauseRelationshipModelElem = (Element) causeRelationshipModel;
			
			String relationshipId = btCauseRelationshipModelElem.getElementsByTagName(
					"relationshipId").item(0).getTextContent();
			
			// Set CauseVariable
			CauseVariableModel causeVariable = new CauseVariableModel(causeId);
			causeVariable.setRelationship(causeRelationship);
			causeVariable.setArgumentList(argumentList);
			causeVariable.setRelationshipModel(
					searchRelationshipModel(rule, relationshipId));
			
			causeVariableList.add(causeVariable);
		}
	}
	
	public void loadEffectNode (RuleModel rule, NodeList effectNodeList) {
		
		effectVariableList = new ArrayList<EffectVariableModel>();
		ArrayList<String> argumentList;
		
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
			argumentList = new ArrayList<String>();
			for (int i = 0; i < listArgument.getLength(); i++) {		
				Node argumentNode = listArgument.item(i);
				Element btArgumentElem = (Element) argumentNode;
				
				String effectArgument = btArgumentElem.getTextContent();
				argumentList.add(effectArgument);
			}
			
			// Effect Relationship Model
			Node effectRelationshipModel = btEffectElemList.getElementsByTagName("effectRelationshipModel").item(j);
			Element btEffectRelationshipModelElem = (Element) effectRelationshipModel;
			
			String relationshipId = btEffectRelationshipModelElem.getElementsByTagName(
					"relationshipId").item(0).getTextContent();
			
			// Set EffectVariable
			EffectVariableModel effectVariable = new EffectVariableModel(effectId);
			effectVariable.setRelationship(effectRelationship);
			effectVariable.setArgumentList(argumentList);
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
	 * @param relationship
	 * @return
	 */
	public boolean canParticipate(RelationshipModel relationship, RuleModel rule) {		
		
		List<OrdinaryVariableModel> ordinaryVariableList = new ArrayList<OrdinaryVariableModel>();
		ordinaryVariableList = rule.getOrdinaryVariableList();
		
		for (int i = 0; i < relationship.getEntityList().size(); i++) {
			
			EntityModel entity = relationship.getEntityList().get(i);
			for (int j = 0; j < ordinaryVariableList.size(); j++) {
				if (!(entity.getId().equals(ordinaryVariableList.get(j).getEntityObject().getId()))) {
					return false;
				}
			}
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
	
	public RelationshipModel searchRelationshipModel(RuleModel rule, String id) {
		for (int i = 0; i < rule.getRelationshipList().size(); i++) {
			if (rule.getRelationshipList().get(i).getId().equals(id)) {
				return rule.getRelationshipList().get(i);
			}
		}
		System.err.println("Null pointer. Entity not found.");
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
