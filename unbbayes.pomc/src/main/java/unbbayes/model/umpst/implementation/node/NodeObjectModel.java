package unbbayes.model.umpst.implementation.node;

import java.util.ArrayList;
import java.util.List;

import unbbayes.model.umpst.implementation.algorithm.MFragModel;

/**
 * Node model that can be context, input or resident node. 
 * 
 * @author Diego Marques
 *
 */
public class NodeObjectModel {
	
	private String id;
	private String name;
	private String idMfragParent;
	
	private NodeType nodeType;
	private List<NodeObjectModel> childrenNode;
	private List<NodeObjectModel> fatherNode;
	private Object eventVariable;
	
	

	public NodeObjectModel(String id, String name, NodeType nodeType, Object eventVariable) {
		
		this.id = id;
		this.name = name;
		this.nodeType = nodeType;
		this.eventVariable = eventVariable;
		
		childrenNode = new ArrayList<NodeObjectModel>();
		fatherNode = new ArrayList<NodeObjectModel>();
	}
	
	public void addFatherNode(NodeObjectModel node) {		
		if (node.getNodeType() != NodeType.CONTEXT) {
			fatherNode.add(node);
		} else {
			System.err.println("Error addiction. Incorret node type.");
		}
		
	}
	
	public void removeFatherNode(NodeObjectModel node) {		
		if (node.getNodeType() != NodeType.CONTEXT) {
			fatherNode.add(node);
		} else {
			System.err.println("Error addiction. Incorret node type.");
		}
		
	}
			
	public void addChildrenNode(NodeResidentModel node) {		
		if ((node.getNodeType() != NodeType.CONTEXT) &&
				(node.getNodeType() != NodeType.INPUT)) {
			childrenNode.add(node);
		} else {
			System.err.println("Error addiction. Incorret node type.");
		}
		
	}
	
	public void removeChildrenNode(NodeResidentModel node) {
		if ((node.getNodeType() != NodeType.CONTEXT) &&
				(node.getNodeType() != NodeType.INPUT)) {
			childrenNode.remove(node);
		} else {
			System.err.println("Error addiction. Incorret node type.");
		}
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the nodeType
	 */
	public NodeType getNodeType() {
		return nodeType;
	}

	/**
	 * @param nodeType the nodeType to set
	 */
	public void setNodeType(NodeType nodeType) {
		this.nodeType = nodeType;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the childrenNode
	 */
	public List<NodeObjectModel> getChildrenNode() {
		return childrenNode;
	}

	/**
	 * @param childrenNode the childrenNode to set
	 */
	public void setChildrenNode(List<NodeObjectModel> childrenNode) {
		this.childrenNode = childrenNode;
	}

	/**
	 * @return the eventVariable
	 */
	public Object getEventVariable() {
		return eventVariable;
	}

	/**
	 * @param eventVariable the eventVariable to set
	 */
	public void setEventVariable(Object eventVariable) {
		this.eventVariable = eventVariable;
	}

	/**
	 * @return the fatherNode
	 */
	public List<NodeObjectModel> getFatherNode() {
		return fatherNode;
	}

	/**
	 * @param fatherNode the fatherNode to set
	 */
	public void setFatherNode(List<NodeObjectModel> fatherNode) {
		this.fatherNode = fatherNode;
	}

	/**
	 * @return the idMfragParent
	 */
	public String getIdMfragParent() {
		return idMfragParent;
	}

	/**
	 * @param idMfragParent the idMfragParent to set
	 */
	public void setIdMfragParent(String idMfragParent) {
		this.idMfragParent = idMfragParent;
	}

}
