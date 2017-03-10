package unbbayes.model.umpst.implementation.node;

import java.util.ArrayList;
import java.util.List;

/**
 * Node model. It can be Cause or Effect event
 * 
 * @author Diego Marques
 *
 */
public class NodeInputModel extends NodeObjectModel {
	
	private String id;
	private String name;
	
	private NodeType nodeType;
	private List<NodeObjectModel> childrenNode;
	
	// Can be Cause or Effect
	private Object eventVariable;

	public NodeInputModel(String id, String name, NodeType nodeType, Object eventVariable) {
		
		super(id, name, NodeType.INPUT, eventVariable);
		
		this.id = id;
		this.name = name;
		this.nodeType = nodeType;
		this.eventVariable = eventVariable;
		
		childrenNode = new ArrayList<NodeObjectModel>();
	}
	
	public void addNode(NodeInputModel node) {		
		if (node.getNodeType() != NodeType.CONTEXT) {
			childrenNode.add(node);
		} else {
			System.err.println("Error addiction. Incorret node type.");
		}
		
	}
	
	public void removeNode(NodeInputModel node) {
		if (node.getNodeType() != NodeType.CONTEXT) {
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

}
