package unbbayes.prs.mebn;

import java.util.ArrayList;
import java.util.List;

import unbbayes.prs.Node;
import unbbayes.util.NodeList;

public class MFrag {

	private MultiEntityBayesianNetwork multiEntityBayesianNetwork;

	private List<ResidentNode> residentNodeList;

	private List<InputNode> inputNodeList;

	private List<OrdinaryVariable> ordinaryVariableList;

	private String name;

	private NodeList nodeList;
	
	/**
	 * Method responsible for deleting this MFrag but not its nodes and 
	 * variables. Only their relationship.
	 *
	 */
	public void delete() {
		nodeList.clear();
		for (MultiEntityNode node : residentNodeList) {
			node.removeFromMFrag();
		}
		for (MultiEntityNode node : inputNodeList) {
			node.removeFromMFrag();
		}
		for (OrdinaryVariable variable : ordinaryVariableList) {
			variable.removeFromMFrag();
		}
	}


	/**
	 * Contructs a new MFrag with empty node's list.
	 * @param name The name of the MFrag.
	 */
	protected MFrag(String name) {
		this.name = name;
		residentNodeList = new ArrayList<ResidentNode>();
		inputNodeList = new ArrayList<InputNode>();
		nodeList = new NodeList();
	}

	/**
	 * Method responsible for adding the given node in its node list. This list
	 * contains all resident and input nodes. Besides that, it contains all
	 * nodes that can be added in subclasses of this class. For instance, the
	 * DomainMFrag contains context nodes also.
	 * 
	 * @param node
	 *            The node to be added in the node list.
	 */
	protected void addNode(Node node) {
		nodeList.add(node);
		multiEntityBayesianNetwork.addNode(node);
	}

	/**
	 * Method responsible for adding the given node in its resident node list.
	 * It is also responsible to add the same node to the node list.
	 * 
	 * @param residentNode
	 *            The node to be added in the resident node list.
	 */
	protected void addResidentNode(ResidentNode residentNode) {
		residentNodeList.add(residentNode);
		addNode(residentNode);
	}

	/**
	 * Method responsible for adding the given node in its input node list. It
	 * is also responsible to add the same node to the node list.
	 * 
	 * @param inputNode
	 *            The node to be added in the input node list.
	 */
	protected void addInputNode(InputNode inputNode) {
		inputNodeList.add(inputNode);
		addNode(inputNode);
	}

	/**
	 * Method responsible for removing the given node from its node list. This
	 * list contains all resident and input nodes. Besides that, it contains all
	 * nodes that can be added in subclasses of this class. For instance, the
	 * DomainMFrag contains context nodes also.
	 * 
	 * @param node
	 *            The node to be added in the node list.
	 */
	protected void removeNode(Node node) {
		nodeList.remove(node);
		multiEntityBayesianNetwork.removeNode(node);
	}

	/**
	 * Method responsible for removing the given node from its resident node
	 * list. It is also responsible to remove the same node from the node list.
	 * 
	 * @param residentNode
	 *            The node to be removed from the resident node list.
	 */
	protected void removeResidentNode(ResidentNode residentNode) {
		residentNodeList.remove(residentNode);
		removeNode(residentNode);
	}

	/**
	 * Method responsible for removing the given node from its input node list.
	 * It is also responsible to remove the same node from the node list.
	 * 
	 * @param inputNode
	 *            The node to be removed from the input node list.
	 */
	protected void removeInputNode(InputNode inputNode) {
		inputNodeList.remove(inputNode);
		removeNode(inputNode);
	}

	/**
	 * Gets the node list. List of all nodes in this MFrag.
	 * 
	 * @return The list of all nodes in this MFrag.
	 */
	public NodeList getNodeList() {
		return nodeList;
	}

}
