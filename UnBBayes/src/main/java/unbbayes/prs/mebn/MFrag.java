/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package unbbayes.prs.mebn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import unbbayes.prs.Edge;
import unbbayes.prs.Graph;
import unbbayes.prs.Node;
import unbbayes.prs.mebn.exception.CycleFoundException;
import unbbayes.prs.mebn.exception.MEBNConstructionException;
import unbbayes.prs.mebn.ssbn.OVInstance;

/**
 * This class represents a MEBN Fragment. 
 * MEBN Fragments (MFrags) are the basic structure of any MEBN logic 
 * model. MFrags represent influences among clusters of related RVs 
 * and can portray repeated patters using ordinary variables as 
 * placeholders in to which entity identifiers can be substituted.  
 * 
 * @version 1.0 (26/11/06)
 */

public class MFrag implements Graph{

	private MultiEntityBayesianNetwork multiEntityBayesianNetwork;

	private List<ContextNode> contextNodeList;
	private List<ResidentNode> residentNodeList;
	private List<InputNode> inputNodeList;

	private List<OrdinaryVariable> ordinaryVariableList;
	private List<OVInstance> ovInstancesList; 
	
	/**
	 *  List of nodes that this MFrag has.
	 */
	private ArrayList<Node> nodeList;
	
	/** 
	 * List of edges that this MFrag has.
	 */
	
	private ArrayList<Edge> edgeList;
	
	protected Map<String,Integer> nodeIndexes;	
	
	private String name; 
	private String description; 
	
	/* 
	 * Estes contadores indicam qual deve ser o numero do proximo
	 * no criado caso os nomes estejam sendo gerados automaticamente
	 */
	private int ordinaryVariableNum = 1; 	
	
	
	/*
	 * When creating SSBN, this state tells us if any context node has failed somewhere inside this MFrag
	 */
	private boolean isUsingDefaultCPT = false;

	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle(
  			unbbayes.prs.mebn.resources.Resources.class.getName());  		
	
	/**
	 * Contructs a new MFrag with empty node's list.
	 * @param name The name of the MFrag.
	 */
	public MFrag(String name, MultiEntityBayesianNetwork mebn) {

		this.multiEntityBayesianNetwork = mebn;
		residentNodeList = new ArrayList<ResidentNode>();
		inputNodeList = new ArrayList<InputNode>();
		ordinaryVariableList = new ArrayList<OrdinaryVariable>();
		contextNodeList = new ArrayList<ContextNode>();
		ovInstancesList = new ArrayList<OVInstance>();
		
		setName(name); 
		
		nodeList = new ArrayList<Node>();
		edgeList = new ArrayList<Edge>(); 
        nodeIndexes = new HashMap<String,Integer>();	
        
	}

	/**
	 * Set the MFrag's name.
	 * @argument name The MFrag's name.
	 */
	public void setName(String name){
		this.name = name; 
	}
	
	/**
	 * Get the MFrag's name.
	 * @return The MFrag's name.
	 */
	public String getName() {
		return name;
	}	
	
	/**
	 * Get the MEBN where this MFrag is inside
	 * @return the MEBN where this MFrag is inside
	 */
	public MultiEntityBayesianNetwork getMultiEntityBayesianNetwork(){
		return multiEntityBayesianNetwork;
	}
	
	
	/**
	 * Method responsible for deleting this MFrag but not its nodes and 
	 * variables. Only their relationship.
	 */
	public void delete() {
		
		nodeList.clear();
		
		for (MultiEntityNode node : residentNodeList) {
			node.removeFromMFrag();
		}
		residentNodeList = new ArrayList<ResidentNode>();
				
		
		for (MultiEntityNode node : inputNodeList) {
			node.removeFromMFrag();
		}
		
		inputNodeList = new ArrayList<InputNode>();
		
		
		for (OrdinaryVariable variable : ordinaryVariableList) {
			variable.removeFromMFrag(); 
		}
		
		ordinaryVariableList = new ArrayList<OrdinaryVariable>();
		
		//TODO cuidado!!! analisar se a classe mae realmente esta deletando estes nodos!!! 
		
		for (MultiEntityNode node : contextNodeList) {
			node.removeFromMFrag();
		}
		contextNodeList = new ArrayList<ContextNode>();
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
	
	public void addNode(Node node) {
		nodeList.add(node);
		multiEntityBayesianNetwork.addNode(node);
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
		
		getMultiEntityBayesianNetwork().plusContextNodeNul(); 
	}

	
	/**
	 * Method responsible for adding the given node in its resident node list.
	 * It is also responsible to add the same node to the node list.
	 * 
	 * @param residentNode
	 *            The node to be added in the resident node list.
	 */
	public void addResidentNode(ResidentNode residentNode) {
		residentNodeList.add(residentNode);
		addNode(residentNode);
		getMultiEntityBayesianNetwork().plusDomainResidentNodeNum(); 
	}

	/**
	 * Method responsible for adding the given node in its input node list. It
	 * is also responsible to add the same node to the node list.
	 * 
	 * @param inputNode
	 *            The node to be added in the input node list.
	 */
	public void addInputNode(InputNode inputNode) {
		inputNodeList.add(inputNode);
		addNode(inputNode);
		getMultiEntityBayesianNetwork().plusGenerativeInputNodeNum();
	}

	/**
	 * Method responsible for adding the given ordinary variable in its ordinary variable list.
	 * 
	 * @param ordinaryVariable
	 *            The ordinary variable to be added in the ordinary variable list.
	 */	
	public void addOrdinaryVariable(OrdinaryVariable ordinaryVariable){
		ordinaryVariableList.add(ordinaryVariable);
		ordinaryVariableNum++; 
		addNode(ordinaryVariable); 
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
	public void removeNode(Node node) {
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

	
	public int getOrdinaryVariableNum(){
		return ordinaryVariableNum; 
	}	
	
	public void plusOrdinaryVariableNum(){
		ordinaryVariableNum++; 
	}
	
	/**
	 * Method responsible for removing the given ordinary variable from its ordinary variable list.
	 * 
	 * @param ordinary variable
	 *            The ordinary variable to be removed from the ordinary variable list.
	 */	
	public void removeOrdinaryVariable(OrdinaryVariable ordinaryVariable){
		ordinaryVariableList.remove(ordinaryVariable);
		removeNode(ordinaryVariable); 
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
	 * Return the ordinary variable with the name, or null if don't exists. 
	 */
	public OrdinaryVariable getOrdinaryVariableByName(String name){
		for(OrdinaryVariable test: ordinaryVariableList){
			if (test.getName().equals(name)){
				return test; 
			}
		}
		return null; 
	}
	
	/**
	 * Gets the node list. List of all nodes in this MFrag.
	 * 
	 * @return The list of all nodes in this MFrag.
	 */
	public ArrayList<Node> getNodeList() {
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
	 * Method responsible to tell if the node list contains the given node.
	 * Note that this method depends on NodeList.contains(node) method and
	 * for now (September 10, 2007) it makes name comparision.
	 * @param node The node to check.
	 * @return True if the list contais this node and false otherwise.
	 */
	public boolean containsNode(Node node) {
		return nodeList.contains(node);
	}	
	
	
	/**
	 *  Retorna os edgeList do grafo.
	 *
	 *@return    edgeList do grafo.
	 */
	public List<Edge> getEdges(){
		return edgeList; 
	}

	/**
	 *  Retorna os n�s do grafo.
	 *
	 *@return    n�s do grafo.
	 * 
	 * @todo Eliminar esse metodo! eh utilizado na classe NetWindow
	 */
	public ArrayList<Node> getNodes(){
		return this.getNodeList(); 
	}

	/**
	 *@return number of nodes of this MFrag (node of all types). 
	 */
	public int getNodeCount(){
		return nodeList.size();		
	}



	/**
	 *  Remove the edge between two nodes and remove the relations
	 *  between the nodes.  
	 *
	 *  @param  arco
	 */
	
	public void removeEdge(Edge arco) {
	    
		Node origin = arco.getOriginNode();
		Node destination = arco.getDestinationNode(); 
		
		/* graph structure */
		origin.getChildren().remove(arco.getDestinationNode());
	    destination.getParents().remove(arco.getOriginNode());
	    edgeList.remove(arco);	
	    
	    /* mebn strucutre */
	    if(origin instanceof ResidentNode){
	    		((ResidentNode)origin).removeResidentNodeChildList((ResidentNode)destination); 
	    }
	    else{ //input
	    	((InputNode)origin).removeResidentNodeChild((ResidentNode)destination); 
	    }
	}


	/**
	 *  Retira do grafo o arco especificado.
	 *
	 *@param  arco  arco a ser retirado.
	 */
	public void removeEdgeByNodes(Node origin, Node destination) {
	    
		for(Edge edge: edgeList){
			if (edge.getOriginNode() == origin){
				if(edge.getDestinationNode() == destination){
					removeEdge(edge); 
					return; 
				}
			}
		}
		
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
		
		if (destination instanceof ResidentNode){
			if (origin instanceof ResidentNode){
				//Case 1: DomainResidentNode -> DomainResidentNode
				addEdgeInGraph(edge); 
			    ((ResidentNode)origin).addResidentNodeChild((ResidentNode)destination); 
			}
			else{
				if (origin instanceof InputNode){
					//Case 2: GenerativeInputNode -> DomainResidentNode 
					addEdgeInGraph(edge); 
				   ((InputNode)origin).addResidentNodeChild((ResidentNode)destination);
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
	 *  Add a edge in the graph. 
     *  @param edge
	*/
	
	private void addEdgeInGraph(Edge edge) throws Exception{
		
		Node origin = edge.getOriginNode();
		Node destination = edge.getDestinationNode();

		origin.getChildren().add(edge.getDestinationNode());
		destination.getParents().add(edge.getOriginNode());
			    
		edgeList.add(edge);

	}

	/**
	 *  Verifica exist�ncia de determinado arco.
	 *
	 *@param  no1  n� origem.
	 *@param  no2  n� destino.
	 *@return      posi��o do arco no vetor ou -1 caso n�o exista tal arco.
	 */
	public int hasEdge(Node no1, Node no2){
		if (no1 == no2) {
			return 1;
		}
	
		int sizeArcos = edgeList.size();
		Edge auxA;
		for (int i = 0; i < sizeArcos; i++) {
			auxA = (Edge) edgeList.get(i);
			if ((auxA.getOriginNode() == no1)
				&& (auxA.getDestinationNode() == no2)
				|| (auxA.getOriginNode() == no2)
				&& (auxA.getDestinationNode() == no1)) {
				return i;
			}
		}
		return -1;		
	}

	public List<InputNode> getInputNodeList() {
		return inputNodeList;
	}

	public void setInputNodeList(List<InputNode> inputNodeList) {
		this.inputNodeList = inputNodeList;
	}

	public List<ResidentNode> getResidentNodeList() {
		return residentNodeList;
	}

	public void setResidentNodeList(List<ResidentNode> residentNodeList) {
		this.residentNodeList = residentNodeList;
	}

	public void setOrdinaryVariableNum(int ordinaryVariableNum) {
		this.ordinaryVariableNum = ordinaryVariableNum;
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
	public ResidentNode getDomainResidentNodeByName(String name){
		for(ResidentNode test: residentNodeList){
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
	public boolean containsDomainResidentNode(ResidentNode residentNode) {
		return residentNodeList.contains(residentNode);
	}	
	
	/**
	 * Method responsible to tell if the generative input node list contains the given 
	 * generative input node.
	 * @param generativeInputNode The generative input node to check.
	 * @return True if the list contais this node and false otherwise.
	 */
	public boolean containsGenerativeInputNode(InputNode generativeInputNode) {
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
	 * @param allOVs set of OrdinaryVariable to be looked for.
	 * @return set of ContextNodes containing combinations of the Ordinary 
	 * Variables passed by its arguments
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
	 * Get all the context nodes that contains the ordinary variables in the list
	 * and related ov's. 
	 * 
	 * @param allOVs
	 * @return
	 */
	public Collection<ContextNode> getContextNodeByOrdinaryVariableRelated(Collection<OrdinaryVariable> allOVs){
		
		Collection<ContextNode> ret = new ArrayList<ContextNode>();
		List<OrdinaryVariable> ovList = new ArrayList<OrdinaryVariable>(); 
		boolean ordinaryVariableAdded = true; 
		
		ovList.addAll(allOVs); 
		
		/*
		 * A idéia é pegar todos os nós de contexto que contém ao menos uma 
		 * VO de allOVs e adicioná-los a lista de nós a serem retornados. As variaveis
		 * ordinárias que pertencem a estes nós e não estavam na lista inicial de
		 * VO's serão adicionadas e serão novamente procurados os nós de contexto
		 * que contém ao menos uma VO. O processo se repete até nenhuma variavel
		 * ordinária ser adicionada a lista (não foi adicionado nenhum nó de 
		 * contexto que contenha uma variável ordinária inédica). (SFTE)
		*/
		int i = 0; 
		while(ordinaryVariableAdded){
			
			ordinaryVariableAdded = false; 
			
			List<OrdinaryVariable> plusOrdinaryVariable = new ArrayList<OrdinaryVariable>(); 
			
			for(OrdinaryVariable ov: ovList){
	
				for(ContextNode ct: this.contextNodeList){
					if(ct.getVariableList().contains(ov)){
						if(!(ret.contains(ct))){
							ret.add(ct); 
							
							for(OrdinaryVariable ovContext: ct.getVariableList()){
								if(!(ovList.contains(ovContext))){
									plusOrdinaryVariable.add(ovContext); 
									ordinaryVariableAdded = true; 
								}
							}			
						}
					}
				}	
			}
			
			for(OrdinaryVariable ov: plusOrdinaryVariable){
				ovList.add(ov); 
			}
			
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
	
	
	
	
	/**
	 * When creating SSBN and making a query, this method tells us whether someone has reported this MFrag as having
	 * a failed context node (thus we should use default CPT for every). This value should be cleared every time we
	 * start a new SSBN creation.
	 * @return true if every Random Variable inside this MFrag should use default CPT (there were failing context node).
	 * Returns false elsewise.
	 * @see MFrag.setAsUsingDefaultCPT()
	 * TODO refactor. Move this method to DomainMFrag, since an input mfrag would never be concious
	 * about default CPTs
	 */
	public boolean isUsingDefaultCPT() {
		return isUsingDefaultCPT;
	}
	
	/**
	 * By setting this to true, lets a "note" informing other classes accessing this class that there were some
	 * context node failing and every Resident Node inside this MFrag must use default distribution By setting this to false,
	 * clears that information. We should set this to false everytime we start a new SSBN query.
	 * @param isUsingDefaultCPT: value to set
	 * TODO refactor. Move this method to DomainMFrag, since an input mfrag would never be concious
	 * about default CPTs
	 */
	public void setAsUsingDefaultCPT(boolean isUsingDefaultCPT) {
		this.isUsingDefaultCPT = isUsingDefaultCPT;
	}
	
	
	/**
	 * checks if there are any node with a particular name inside this mfrag.
	 * @param name: the name of a node
	 * @return The first Node found. Null elsewise.
	 */
	public Node containsNode(String name){
		ArrayList<Node> list = this.getNodeList();
		Node node = null;
		for (int i = 0 ; i < list.size() ; i++) {
			node = list.get(i);
			if (node.getName().equalsIgnoreCase(name) ) {
				return node;
			}
		}
		return null;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
