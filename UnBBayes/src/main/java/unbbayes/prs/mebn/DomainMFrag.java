package unbbayes.prs.mebn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import unbbayes.prs.Edge;
import unbbayes.prs.Node;
import unbbayes.prs.mebn.exception.CycleFoundException;
import unbbayes.prs.mebn.exception.MEBNConstructionException;
import unbbayes.prs.mebn.ssbn.OVInstance;

/**
 * Domain-specific MFrags. 
 * Extend the MFrag for work with GenerativeInputNode, DomainResidentNode and ContextNode. 
 */

public class DomainMFrag extends MFrag {

	private List<ContextNode> contextNodeList;

	private List<GenerativeInputNode> inputNodeList;

	private List<DomainResidentNode> residentNodeList;
	
	private List<OVInstance> ovInstancesList; 

	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.prs.mebn.resources.Resources");  		
	
	/**
	 * Contructs a new DomainMFrag with empty node's list.
	 * @param name The name of the DomainMFrag.
	 */
	public DomainMFrag(String name, MultiEntityBayesianNetwork mebn) {
		super(name, mebn);
		contextNodeList = new ArrayList<ContextNode>();
		inputNodeList = new ArrayList<GenerativeInputNode>();
		residentNodeList = new ArrayList<DomainResidentNode>();
		ovInstancesList = new ArrayList<OVInstance>(); 
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
		
		super.getMultiEntityBayesianNetwork().plusContextNodeNul(); 
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
		
		super.getMultiEntityBayesianNetwork().plusGenerativeInputNodeNum();
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
		
		this.getMultiEntityBayesianNetwork().plusDomainResidentNodeNum(); 

	}

	/**
	 * Method responsible for adding the given ordinary variable in its ordinary variable list.
	 * 
	 * @param ordinaryVariable
	 *            The ordinary variable to be added in the ordinary variable list.
	 */	
	public void addOrdinaryVariable(OrdinaryVariable ordinaryVariable){
		
		super.addOrdinaryVariable(ordinaryVariable);
		addNode(ordinaryVariable); 
		
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
	public void removeGenerativeInputNode(GenerativeInputNode generativeInputNode) {
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
	 * Method responsible for removing the given ordinary variable from its ordinary variable list.
	 * 
	 * @param ordinary variable
	 *            The ordinary variable to be removed from the ordinary variable list.
	 */	
	public void removeOrdinaryVariable(OrdinaryVariable ordinaryVariable){
		super.getOrdinaryVariableList().remove(ordinaryVariable);
		removeNode(ordinaryVariable); 
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
	 * Gets the total number of domain resident nodes in this DomainMFrag.
	 * @return The total number of domain resident nodes in this DomainMFrag.
	 */
	public int getDomainResidentNodeCount() {
		return residentNodeList.size();
		
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
	
	/**
	 * gets the number of the next domain resident node (for uses to generate 
	 * the automatic name
	 */	
	public int getDomainResidentNodeNum(){
		return this.getMultiEntityBayesianNetwork().getDomainResidentNodeNum(); 
	}

	/**
	 * Returns the domain resident node with respective name, or null if it doesn't exist. 
	 */
	public DomainResidentNode getDomainResidentNodeByName(String name){
		for(DomainResidentNode test: residentNodeList){
			if (test.getName().equals(name)){
				return test; 
			}
		}
		return null; 
	}
	
	/**
	 * gets the number of the next context node (for uses to generate 
	 * the automatic name
	 */	
	public int getContextNodeNum(){
		return this.getMultiEntityBayesianNetwork().getContextNodeNum(); 
	}
	
	/**
	 * gets the number of the next generative input node (for uses to generate 
	 * the automatic name
	 */	
	public int getGenerativeInputNodeNum(){
		return this.getMultiEntityBayesianNetwork().getGenerativeInputNodeNum(); 
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
	 * Return the node with the name, or null if don't exists. 
	 */
	public ContextNode getContextNodeByName(String name){
		for(ContextNode test: contextNodeList){
			if (test.getName().equals(name)){
				return test; 
			}
		}
		return null; 
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
	
	
	
	/*
	 * OVInstanceList methods... 
	 */
	
	public void addOVInstance(OVInstance element){
		ovInstancesList.add(element); 
	}
	
	public List<OVInstance> getOVInstancesForOrdinaryVariable(OrdinaryVariable ov){
		List<OVInstance> ret = new ArrayList<OVInstance>(); 
		for(OVInstance ovInstance: ovInstancesList){
			if(ovInstance.getOv().equals(ov)){
				ret.add(ovInstance); 
			}
		}
		return ret; 
	}
	
	public void removeOVInstance(OVInstance element){
		ovInstancesList.remove(element); 
	}
	
	public void clearOVInstanceList(){
		ovInstancesList.clear(); 
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
		//TODO cuidado!!! analisar se a classe mae realmente esta deletando estes nodos!!! 
		
		for (MultiEntityNode node : contextNodeList) {
			node.removeFromMFrag();
		}
		contextNodeList = new ArrayList<ContextNode>();
	}
	
	/**
	 *  Add a edge in the domainMFrag. 
	 *  Valid Edges:
	 *  - Input -> Resident
	 *  - Resident -> Resident
	 *  
	 *@param  edge
	 *@throws MEBNConstructionException when the edge don't is between valid nodes. 
	 */
	
	public void addEdge(Edge edge) throws MEBNConstructionException, CycleFoundException, Exception{
		
		Node origin = edge.getOriginNode();
		Node destination = edge.getDestinationNode();
		
		if (destination instanceof DomainResidentNode){
			if (origin instanceof DomainResidentNode){
				//Case 1: DomainResidentNode -> DomainResidentNode
				super.addEdge(edge); 
			    ((DomainResidentNode)origin).addResidentNodeChild((DomainResidentNode)destination); 
			}
			else{
				if (origin instanceof GenerativeInputNode){
					//Case 2: GenerativeInputNode -> DomainResidentNode 
				   super.addEdge(edge); 
				   ((GenerativeInputNode)origin).addResidentNodeChild((DomainResidentNode)destination);
				}
				else{
					throw new MEBNConstructionException(resource.getString("InvalidEdgeException")); 
				}
			}
		}
		else{
			throw new MEBNConstructionException(resource.getString("InvalidEdgeException"));
		}
	}	
	
	
	/**
	 * 
	 * @param allOVs set of OrdinaryVariable to be looked for.
	 * @return set of ContextNodes containing all and exactly the Ordinary Variables passed by its arguments
	 */
	public Collection<ContextNode> getContextByAllOV(OrdinaryVariable...allOVs) {
		Collection<ContextNode> ret = new ArrayList<ContextNode>();
		
		if (allOVs == null) {
			return ret;
		}
		
		for (ContextNode node : this.getContextNodeList()) {
			// we suppose if all ovs are present and the number of ovs (inside arguments) are the same of the required ovs, then its exactly the same
			if (( node.getAllOVCount() == allOVs.length ) && node.hasAllOVs(allOVs)) {
				ret.add(node);	// we also suppose the contextNodeList has no redundancy
			}			
		}
		return ret;
	}
	
	/**
	 * 
	 * @param allOVs set of OrdinaryVariable to be looked for.
	 * @return set of ContextNodes containing all (but not exactly) the Ordinary Variables passed by its arguments
	 */
	public Collection<ContextNode> getContextByOV(OrdinaryVariable...allOVs) {
		Collection<ContextNode> ret = new ArrayList<ContextNode>();
		
		if (allOVs == null) {
			return ret;
		}
		
		for (ContextNode node : this.getContextNodeList()) {
			if ( node.hasAllOVs(allOVs)) {
				ret.add(node);	// we  suppose the contextNodeList has no redundancy
			}			
		}
		return ret;
	}

	/**
	 * 
	 * @param allOVs set of OrdinaryVariable to be looked for.
	 * @return set of ContextNodes containing combinations of the Ordinary Variables passed by its arguments
	 */	
	public Collection<ContextNode> getContextByOVCombination(Collection<OrdinaryVariable> allOVs) {
		
		Collection<ContextNode> ret = new ArrayList<ContextNode>();
		
		if (allOVs == null) {
			return ret;
		}
		
		for (ContextNode contextNode : this.getContextNodeList()) {
			boolean test = true; 
			Set<OrdinaryVariable> ordinaryVariableList = contextNode.getVariableList();
			
			for(OrdinaryVariable ov: ordinaryVariableList){
				if(!allOVs.contains(ov)){
					test = false; 
					break; 
				}
			}
			
			if(test) ret.add(contextNode); 
			
		}
		
		return ret;
	
	}
	
	/**
	 * Return all context nodes that have one or more ordinary variables of 
	 * the allOVs list. 
	 */
	public Collection<ContextNode> getSearchContextByOVCombination(Collection<OrdinaryVariable> allOVs) {
		
		Collection<ContextNode> ret = new ArrayList<ContextNode>();
		
		if (allOVs == null) {
			return ret;
		}
		
		for (ContextNode node : this.getContextNodeList()) {
			boolean test = false; 
			Set<OrdinaryVariable> ordinaryVariableList = node.getVariableList();
			
			for(OrdinaryVariable ov: ordinaryVariableList){
				if(allOVs.contains(ov)){
					test = true; 
					break; 
				}
			}
			
			if(test)ret.add(node); 
			
		}
		
		return ret;
	
	}
	
	
	/**
	 * 
	 * @param allOVs set of OrdinaryVariable to be looked for.
	 * @return set of ContextNodes containing all (but not exactly) the Ordinary Variables passed by its arguments
	 */
	public Collection<ContextNode> getContextByOV(Collection<OrdinaryVariable>allOVs) {
		if (allOVs == null) {
			return new ArrayList<ContextNode>();
		}
		OrdinaryVariable ovs[] = new OrdinaryVariable[allOVs.size()];
		ovs = allOVs.toArray(ovs);
		return getContextByOV(ovs);
	}
	
	public String toString(){
		return this.getName(); 
	}

}
