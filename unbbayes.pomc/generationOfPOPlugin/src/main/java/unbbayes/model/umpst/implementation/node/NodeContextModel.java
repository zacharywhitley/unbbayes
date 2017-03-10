package unbbayes.model.umpst.implementation.node;

import java.util.ArrayList;
import java.util.List;

/**
 * Context Node. Can be OV or NecessaryCondition.
 * 
 * @author Diego Marques
 *
 */
public class NodeContextModel extends NodeObjectModel {
	
	private String id;
	private String name;
	
	private NodeType nodeType;
	
	// Can be OV, NecessaryCondition.
	private Object eventVariable;

	public NodeContextModel(String id, String name, NodeType nodeType, Object eventVariable) {
		
		super(id, name, NodeType.CONTEXT, eventVariable);
		
		this.id = id;
		this.name = name;
		this.nodeType = nodeType;
		this.eventVariable = eventVariable;
		
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
