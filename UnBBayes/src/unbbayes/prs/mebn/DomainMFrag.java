package unbbayes.prs.mebn;

import java.util.ArrayList;
import java.util.List;

/**
 * Domain-specifcs MFrags. 
 */

public class DomainMFrag extends MFrag {

	private List<ContextNode> contextNodeList;

	private List<GenerativeInputNode> inputNodeList;

	private List<DomainResidentNode> residentNodeList;

	/* 
	 * Estes contadores indicam qual deve ser o numero do proximo
	 * no criado caso os nomes estejam sendo gerados automaticamente
	 */
	private int generativeInputNodeNum = 1; 
	private int domainResidentNodeNum = 1; 	
	private int contextNodeNum = 1; 

	/**
	 * Contructs a new DomainMFrag with empty node's list.
	 * @param name The name of the DomainMFrag.
	 */
	public DomainMFrag(String name, MultiEntityBayesianNetwork mebn) {
		super(name, mebn);
		contextNodeList = new ArrayList<ContextNode>();
		inputNodeList = new ArrayList<GenerativeInputNode>();
		residentNodeList = new ArrayList<DomainResidentNode>();
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
		
		contextNodeNum++; 
	}

	/**
	 * Method responsible for adding the given node in its generative input node
	 * list. It is also responsible to add the same node to the input node list.
	 * 
	 * @param generativeInputNode
	 *            The node to be added in the generative input node list.
	 */
	public void addGenerativeInputNode(GenerativeInputNode generativeInputNode) {
		inputNodeList.add(generativeInputNode);
		addInputNode(generativeInputNode);
		
		generativeInputNodeNum++; 
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
		
		residentNodeList.add(domainResidentNode);
		addResidentNode(domainResidentNode);
		
		domainResidentNodeNum++; 
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
		inputNodeList.remove(generativeInputNode);
		removeNode(generativeInputNode);
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
		residentNodeList.remove(domainResidentNode);
		removeNode(domainResidentNode);
	}
	
	
	/**
	 * Gets the list of generative input nodes in this DomainMFrag.
	 * @return The list of generative input nodes in this DomainMFrag.
	 */
	public List<GenerativeInputNode> getGenerativeInputNodeList() {
		return inputNodeList;
	}
	
	/**
	 * Gets the list of domain resident nodes in this DomainMFrag.
	 * @return The list of domain resident nodes in this DomainMFrag.
	 */
	public List<DomainResidentNode> getDomainResidentNodeList() {
		return residentNodeList;
		
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
		return inputNodeList.size();
	}
	
	public int getDomainResidentNodeNum(){
		return domainResidentNodeNum; 
	}
	
	public int getContextNodeNum(){
		return contextNodeNum; 
	}
	
	public int getGenerativeInputNodeNum(){
		return generativeInputNodeNum; 
	}	
	
	
	
	/**
	 * Gets the total number of domain resident nodes in this DomainMFrag.
	 * @return The total number of domain resident nodes in this DomainMFrag.
	 */
	public int getDomainResidentNodeCount() {
		return residentNodeList.size();
		
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
		return residentNodeList.contains(residentNode);
	}	
	
	/**
	 * Method responsible to tell if the generative input node list contains the given 
	 * generative input node.
	 * @param generativeInputNode The generative input node to check.
	 * @return True if the list contais this node and false otherwise.
	 */
	public boolean containsGenerativeInputNode(GenerativeInputNode generativeInputNode) {
		return inputNodeList.contains(generativeInputNode);
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
	 * variables. Only their relationship. Retire the DomainMFrag of the MEBN 
	 * where it is inside. 
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
