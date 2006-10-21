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
	 * Contructs a new MFrag with empty node's list.
	 * @param name The name of the MFrag.
	 */
	protected MFrag(String name, MultiEntityBayesianNetwork mebn) {
		this.name = name;
		this.multiEntityBayesianNetwork = mebn;
		residentNodeList = new ArrayList<ResidentNode>();
		inputNodeList = new ArrayList<InputNode>();
		ordinaryVariableList = new ArrayList<OrdinaryVariable>();
		nodeList = new NodeList();
	}

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
	 * Method responsible for adding the given ordinary variable in its ordinary variable list.
	 * 
	 * @param ordinaryVariable
	 *            The ordinary variable to be added in the ordinary variable list.
	 */	
	protected void addOrdinaryVariable(OrdinaryVariable ordinaryVariable){
		ordinaryVariableList.add(ordinaryVariable); 
	}
	
	/**
	 * Method responsible for removing the given ordinary variable from its ordinary variable list.
	 * 
	 * @param ordinary variable
	 *            The ordinary variable to be removed from the ordinary variable list.
	 */	
	protected void removeOrdinaryVariable(OrdinaryVariable ordinaryVariable){
		ordinaryVariableList.remove(ordinaryVariable); 
	}	

	/**
	 * Method responsible to tell if the ordinary variable list contains the given 
	 * ordinary variable.
	 * @param ordinaryVariable The ordinary variable to check.
	 * @return True if the list contais this ordinary variable and false otherwise.
	 */
	public boolean containsOrdinaryVariable(OrdinaryVariable ordinaryVariable) {
		return ordinaryVariableList.contains(ordinaryVariable);
	}	
	
	/**
	 * Gets the node list. List of all nodes in this MFrag.
	 * 
	 * @return The list of all nodes in this MFrag.
	 */
	public NodeList getNodeList() {
		return nodeList;
	}
	
	/**
	 * Gets the ordinary variable list.
	 * 
	 * @return The list of all nodes in this MFrag.
	 */
	public List<OrdinaryVariable> getOrdinaryVariableList() {
		return ordinaryVariableList;
	}	

	/**
	 * Get the MFrag's name.
	 * @return The MFrag's name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Method responsible to tell if the node list contains the given node.
	 * @param node The node to check.
	 * @return True if the list contais this node and false otherwise.
	 */
	public boolean containsNode(Node node) {
		return nodeList.contains(node);
	}	
		

}
