package unbbayes.prs.mebn;

import java.util.ArrayList;
import java.util.List;

public class DomainMFrag extends MFrag {

	private List<ContextNode> contextNodeList;

	private List<GenerativeInputNode> generativeInputNodeList;

	private List<DomainResidentNode> domainResidentNodeList;

	/**
	 * Contructs a new DomainMFrag with empty node's list.
	 * @param name The name of the DomainMFrag.
	 */
	public DomainMFrag(String name, MultiEntityBayesianNetwork mebn) {
		super(name, mebn);
		contextNodeList = new ArrayList<ContextNode>();
		generativeInputNodeList = new ArrayList<GenerativeInputNode>();
		domainResidentNodeList = new ArrayList<DomainResidentNode>();
	}

	/**
	 * Method responsible for adding the given node in its context node list. It
	 * is also responsible to add the same node to the node list.
	 * 
	 * @param contextNode
	 *            The node to be added in the context node list.
	 */
	public void addContextNode(ContextNode contextNode) {
		contextNodeList.add(contextNode);
		addNode(contextNode);
		contextNode.setMFrag(this);
	}

	/**
	 * Method responsible for adding the given node in its generative input node
	 * list. It is also responsible to add the same node to the input node list.
	 * 
	 * @param generativeInputNode
	 *            The node to be added in the generative input node list.
	 */
	public void addGenerativeInputNode(GenerativeInputNode generativeInputNode) {
		generativeInputNodeList.add(generativeInputNode);
		addInputNode(generativeInputNode);
	}

	/**
	 * Method responsible for adding the given node in its domain resident node
	 * list. It is also responsible to add the same node to the resident node
	 * list.
	 * 
	 * @param domainResidentNode
	 *            The node to be added in the domain resident node list.
	 */
	public void addDomainResidentNode(DomainResidentNode domainResidentNode) {
		domainResidentNodeList.add(domainResidentNode);
		addResidentNode(domainResidentNode);
	}

	/**
	 * Method responsible for removing the given node from its context node
	 * list. It is also responsible to remove the same node from the node list.
	 * 
	 * @param contextNode
	 *            The node to be removed from the context node list.
	 */
	public void removeContextNode(ContextNode contextNode) {
		contextNodeList.remove(contextNode);
		removeNode(contextNode);
	}

	/**
	 * Method responsible for removing the given node from its generative input
	 * node list. It is also responsible to remove the same node from the input
	 * node list.
	 * 
	 * @param generativeInputNode
	 *            The node to be removed from the generative input node list.
	 */
	public void removeGenerativeInputNode(
			GenerativeInputNode generativeInputNode) {
		generativeInputNodeList.remove(generativeInputNode);
		removeInputNode(generativeInputNode);
	}

	/**
	 * Method responsible for removing the given node from its domain resident
	 * node list. It is also responsible to remove the same node from the
	 * resident node list.
	 * 
	 * @param domainResidentNode
	 *            The node to be removed from the domain resident node list.
	 */
	public void removeDomainResidentNode(DomainResidentNode domainResidentNode) {
		domainResidentNodeList.remove(domainResidentNode);
		removeResidentNode(domainResidentNode);
	}
	
	/**
	 * Method responsible for adding the given ordinary variable in its ordinary variable list.
	 * 
	 * @param ordinaryVariable
	 *            The ordinary variable to be added in the ordinary variable list.
	 */	
	public void addOrdinaryVariableDomain(OrdinaryVariable ordinaryVariable){
		addOrdinaryVariable(ordinaryVariable);
	}
	
	/**
	 * Method responsible for removing the given ordinary variable from its ordinary variable list.
	 * 
	 * @param ordinary variable
	 *            The ordinary variable to be removed from the ordinary variable list.
	 */	
	protected void removeOrdinaryVariableDomain(OrdinaryVariable ordinaryVariable){
		removeOrdinaryVariable(ordinaryVariable); 
	}		
	
	/**
	 * Gets the list of generative input nodes in this DomainMFrag.
	 * @return The list of generative input nodes in this DomainMFrag.
	 */
	public List<GenerativeInputNode> getGenerativeInputNodeList() {
		return generativeInputNodeList;
	}
	
	/**
	 * Gets the list of domain resident nodes in this DomainMFrag.
	 * @return The list of domain resident nodes in this DomainMFrag.
	 */
	public List<DomainResidentNode> getDomainResidentNodeList() {
		return domainResidentNodeList;
		
	}
	
	/**
	 * Gets the list of context nodes in this DomainMFrag.
	 * @return The list of context nodes in this DomainMFrag.
	 */
	public List<ContextNode> getContextNodeList(){
		return contextNodeList;
	}
	
	/**
	 * Gets the total number of context nodes in this DomainMFrag.
	 * @return The total number of context nodes in this DomainMFrag.
	 */
	public int getContextNodeCount() {
		return contextNodeList.size();
	}	
	
	/**
	 * Gets the total number of generative input nodes in this DomainMFrag.
	 * @return The total number of generative input nodes in this DomainMFrag.
	 */
	public int getGenerativeInputNodeCount() {
		return generativeInputNodeList.size();
	}
	
	/**
	 * Gets the total number of domain resident nodes in this DomainMFrag.
	 * @return The total number of domain resident nodes in this DomainMFrag.
	 */
	public int getDomainResidentNodeCount() {
		return domainResidentNodeList.size();
		
	}	
	
	/**
	 * Method responsible to tell if the context node list contains the given 
	 * context node.
	 * @param contextNode The context node to check.
	 * @return True if the list contais this node and false otherwise.
	 */
	public boolean containsContextNode(ContextNode contextNode) {
		return contextNodeList.contains(contextNode);
	}
	
	/**
	 * Method responsible to tell if the domain resident node list contains the given 
	 * context node.
	 * @param residentNode The context node to check.
	 * @return True if the list contais this node and false otherwise.
	 */
	public boolean containsDomainResidentNode(DomainResidentNode residentNode) {
		return domainResidentNodeList.contains(residentNode);
	}	
	
	/**
	 * Method responsible to tell if the generative input node list contains the given 
	 * generative input node.
	 * @param generativeInputNode The generative input node to check.
	 * @return True if the list contais this node and false otherwise.
	 */
	public boolean containsGenerativeInputNode(GenerativeInputNode generativeInputNode) {
		return generativeInputNodeList.contains(generativeInputNode);
	}	

	/**
	 * Method responsible to tell if the ordinary variable list contains the given 
	 * ordinary variable.
	 * @param ordinaryVariable The ordinary variable to check.
	 * @return True if the list contais this ordinary variable and false otherwise.
	 */
	public boolean containsOrdinaryVariableDomain(OrdinaryVariable ordinaryVariable) {
		return containsOrdinaryVariable(ordinaryVariable);
	}			
	
	/**
	 * Method responsible for deleting this DomainMFrag but not its nodes and 
	 * variables. Only their relationship.
	 * This method is overwritten because its superclass does not eleminate the
	 * relationship with the context nodes, that just exist in this class.
	 *
	 */
	public void delete() {
		super.delete();
		for (MultiEntityNode node : contextNodeList) {
			node.removeFromMFrag();
		}
	}

}
