package unbbayes.model.umpst.implementation.algorithm;

import java.util.ArrayList;
import java.util.List;

import unbbayes.model.umpst.implementation.node.NodeContextModel;
import unbbayes.model.umpst.implementation.node.NodeInputModel;
import unbbayes.model.umpst.implementation.node.NodeObjectModel;
import unbbayes.model.umpst.implementation.node.NodeResidentModel;

/**
 * MFragModel. Group of nodes that has context, input and resident nodes.
 * 
 * @author Diego Marques
 *
 */
public class MFragModel {
	
	private String id;
	private String name;
	
	private List<NodeContextModel> nodeContextList;	
	private List<NodeInputModel> nodeInputList;
	private List<NodeResidentModel> nodeResidentList;
	private List<NodeObjectModel> nodeNotDefinedList;

	public MFragModel(String id, String name) {
		
		this.id = id;
		this.name = name;
		
		nodeContextList = new ArrayList<NodeContextModel>();
		nodeInputList = new ArrayList<NodeInputModel>();
		nodeResidentList = new ArrayList<NodeResidentModel>();
		nodeNotDefinedList = new ArrayList<NodeObjectModel>();
	}
	
	public void updateResidentNode(NodeResidentModel node) {
		removeResidentNode(node);
		addResidentNode(node);
	}
	
	public void updateInputNode(NodeInputModel node) {
		removeInputNode(node);
		addInputNode(node);
	}
	
	/**
	 * Include context nodes;
	 */
	public void addContextNode(NodeContextModel node) {
		nodeContextList.add(node);		
	}
	
	/**
	 * Remove context nodes;
	 */
	public void removeContextNode(NodeContextModel node) {
		nodeContextList.remove(node);		
	}
	
	/**
	 * Include input nodes;
	 */
	public void addInputNode(NodeInputModel node) {
		nodeInputList.add(node);		
	}
	
	/**
	 * Remove input nodes;
	 */
	public void removeInputNode(NodeInputModel node) {
		nodeInputList.remove(node);		
	}
	
	/**
	 * Include resident nodes;
	 */
	public void addResidentNode(NodeResidentModel node) {
		nodeResidentList.add(node);		
	}
	
	/**
	 * Remove resident nodes;
	 */
	public void removeResidentNode(NodeResidentModel node) {
		nodeResidentList.remove(node);		
	}
	
	/**
	 * Include not defined nodes;
	 */
	public void addNotDefinedNode(NodeObjectModel node) {
		nodeNotDefinedList.add(node);		
	}
	
	/**
	 * Remove not defined nodes;
	 */
	public void removeNotDefinedNode(NodeObjectModel node) {
		nodeNotDefinedList.remove(node);		
	}

	/**
	 * @return the nodeContextList
	 */
	public List<NodeContextModel> getNodeContextList() {
		return nodeContextList;
	}

	/**
	 * @param nodeContextList the nodeContextList to set
	 */
	public void setNodeContextList(List<NodeContextModel> nodeContextList) {
		this.nodeContextList = nodeContextList;
	}

	/**
	 * @return the nodeInputList
	 */
	public List<NodeInputModel> getNodeInputList() {
		return nodeInputList;
	}

	/**
	 * @param nodeInputList the nodeInputList to set
	 */
	public void setNodeInputList(List<NodeInputModel> nodeInputList) {
		this.nodeInputList = nodeInputList;
	}

	/**
	 * @return the nodeResidentList
	 */
	public List<NodeResidentModel> getNodeResidentList() {
		return nodeResidentList;
	}

	/**
	 * @param nodeResidentList the nodeResidentList to set
	 */
	public void setNodeResidentList(List<NodeResidentModel> nodeResidentList) {
		this.nodeResidentList = nodeResidentList;
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
	 * @return the nodeNotDefinedList
	 */
	public List<NodeObjectModel> getNodeNotDefinedList() {
		return nodeNotDefinedList;
	}

	/**
	 * @param nodeNotDefinedList the nodeNotDefinedList to set
	 */
	public void setNodeNotDefinedList(List<NodeObjectModel> nodeNotDefinedList) {
		this.nodeNotDefinedList = nodeNotDefinedList;
	}

}
