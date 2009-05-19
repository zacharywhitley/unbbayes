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

package unbbayes.prs.mebn.ssbn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import unbbayes.io.ILogManager;
import unbbayes.io.TextLogManager;
import unbbayes.prs.INode;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.TreeVariable;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;

/**
 * Encapsule the SSBN generated by the ISSBNGenerator. 
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 * 
 */
public class SSBN {

	private ProbabilisticNetwork probabilisticNetwork; 
	
	private KnowledgeBase knowledgeBase; 
	
	private List<SimpleSSBNNode> findingList; 
	private List<Query> queryList; 
	
	private List<SimpleSSBNNode> simpleSSBNNodeList; 
	private List<SimpleEdge> edgeList; 
	
	private List<SSBNNode> ssbnNodeList;
	
	private List<MFragInstance> mFragInstanceList; 
	
	//informations about the creation of the SSBN
	private ILogManager logManager;
	private List<SSBNWarning> warningList; 
	
	private enum State{
		INITIAL, 
		COMPILED, 
		WITH_FINDINGS, 
		FINDINGS_PROPAGATED, 
		USER_ACTION
	}
	
	private State state = State.INITIAL; 
	
	/**
	 *
	 * @param pn Probabilistic network get on the algorithm 
	 * @param findingList List of SSBNNode's where for each element the property isFinding = true
	 * @param queryList List of queries
	 */
	public SSBN(){
		findingList = new ArrayList<SimpleSSBNNode>(); 
		queryList = new ArrayList<Query>(); 
		warningList = new ArrayList<SSBNWarning>();
		simpleSSBNNodeList = new ArrayList<SimpleSSBNNode>(); 
		ssbnNodeList = new ArrayList<SSBNNode>(); 
		edgeList = new ArrayList<SimpleEdge>();
		mFragInstanceList = new ArrayList<MFragInstance>(); 
		
		logManager = new TextLogManager(); 
	}
	
	
	
	
	//---------- METHODS FOR ADD COMPONENTS TO THE ALGORITHM EVALUATION ---
	
	/**
	 * Verify if already exists a node in the list of nodes. If its exists, return
	 * the already existent node, else return added the node to the list of node 
	 * and return it; 
	 */
	public SimpleSSBNNode addSSBNNodeIfItDontAdded(SimpleSSBNNode ssbnNode){
		
		SimpleSSBNNode alreadyExistentNode = null; 
		
		for(SimpleSSBNNode n: simpleSSBNNodeList){
			if(n.equals(ssbnNode)){
				alreadyExistentNode = n;  
			}
		}
		
		if(alreadyExistentNode == null){
			alreadyExistentNode = ssbnNode; 
			this.simpleSSBNNodeList.add(ssbnNode); 
		}
		
		return alreadyExistentNode; 
	}
	
	
	//---------- METHODS FOR ITERATION WITH THE STATE OF THE CPT EVALUATION ---
	
	/**
	 * Initialize the ssbn: 
	 * 1) Compile the network
	 * 2) Add the findings
	 * 3) Propagate the findings
	 * 
	 * After this, the network is ready to show to the user
	 * @throws Exception 
	 */
	public void compileAndInitializeSSBN() throws Exception{
		compileNetwork(); 
		addFindings();
		propagateFindings(); 
	}
	
	public void reinitializeSSBN() throws Exception{
	    this.probabilisticNetwork.initialize();
		addFindings();
		propagateFindings(); 
	}
	
	private void compileNetwork() throws Exception{
		probabilisticNetwork.compile(); 
		setState(State.COMPILED); 
	}
	
	private void propagateFindings() throws Exception{
		probabilisticNetwork.updateEvidences();
		setState(State.FINDINGS_PROPAGATED); 
	}
	
	/**
	 * Propagate the findings 
	 * 
	 * Pre-Requisite: 
	 * - All the nodes of the list of findings have only one actual value
	 */
	private void addFindings() throws SSBNNodeGeneralException{
		
		for(SimpleSSBNNode findingNode: findingList){
			
			if(findingNode.getProbNode()!=null){ //Not all findings nodes are at the network. 
				TreeVariable node = findingNode.getProbNode();

				String nameState = findingNode.getState().getName(); 

				boolean ok = false; 
				for(int i = 0; i < node.getStatesSize(); i++){
					if(node.getStateAt(i).equals(nameState)){
						node.addFinding(i);
						ok = true; 
						break; 
					}
				}

				if(!ok){
					throw new SSBNNodeGeneralException(); 
				}
			}
			
		}
		
		setState(State.WITH_FINDINGS); 
		
	}
	
	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}
	

	
	// GET AND SET'S METHODS
	
	/**
	 * Add one finding to the finding list. Dont't add this node to the list of 
	 * nodes of the SSBN (use the method addSSBNNodeIfItDontAdded for this).
	 */
	public void addFindingToTheFindingList(SimpleSSBNNode finding){
		if(!this.findingList.contains(finding)){
			this.findingList.add(finding); 
		}
	}
	
	public List<SimpleSSBNNode> getFindingList() {
		return findingList;
	}

	public List<Query> getQueryList() {
		return queryList;
	}
	
	public ProbabilisticNetwork getProbabilisticNetwork() {
		return probabilisticNetwork;
	}

	public void setProbabilisticNetwork(ProbabilisticNetwork probabilisticNetwork) {
		this.probabilisticNetwork = probabilisticNetwork;
	}

	/**
	 * Add a query to the query list. Dont't add this node to the list of 
	 * nodes of the SSBN (use the method addSSBNNodeIfItDontAdded for this).
	 */
	public void addQueryToTheQueryList(Query query){
		if(!this.queryList.contains(query)){
			this.queryList.add(query); 
		}
	}
	
	public List<SimpleSSBNNode> getSimpleSsbnNodeList() {
		return simpleSSBNNodeList;
	}
	
	public List<SSBNNode> getSsbnNodeList(){
		return ssbnNodeList; 
	}
	
	public void setSsbnNodeList(List<SSBNNode> list){
		this.ssbnNodeList = list; 
	}
	
	public void addSSBNNode(SimpleSSBNNode node){
		this.simpleSSBNNodeList.add(node); 
	}

	public List<SimpleEdge> getEdgeList() {
		return edgeList;
	}
	
	public void addEdge(SimpleEdge edge) {
		this.edgeList.add(edge);
	}
	
	public List<MFragInstance> getMFragInstanceList() {
		return mFragInstanceList;
	}
	
	//INFORMATIONS ABOUT THE GENERATION OF THE SSBN 
	
	public List<SSBNWarning> getWarningList() {
		return warningList;
	}
	
	public void setWarningList(List<SSBNWarning> e){
		this.warningList = e ; 
	}
	
	public ILogManager getLogManager() {
		return logManager;
	}




	public KnowledgeBase getKnowledgeBase() {
		return knowledgeBase;
	}

	public void setKnowledgeBase(KnowledgeBase knowledgeBase) {
		this.knowledgeBase = knowledgeBase;
	}
	
	/**
	 * Removes a specified collection of nodes from this network, regarding each
	 * node's dependency (if any node references the nodes given by the argument, it
	 * removes that dependency)
	 * It does not remove finding nodes or query nodes, yet.
	 * @param nodesToRemove
	 */
	public boolean removeAll(Collection<INode> nodesToRemove) {
		boolean ret = false;
		for (INode nodeToRemove : nodesToRemove) {
			for (INode parent : nodeToRemove.getParentNodes()) {
				parent.removeChildNode(nodeToRemove);
			}
			for (INode child : nodeToRemove.getChildNodes()) {
				child.removeParentNode(nodeToRemove);
			}
		}
		ret = this.getSsbnNodeList().removeAll(nodesToRemove) || ret;
		ret = this.getSimpleSsbnNodeList().removeAll(nodesToRemove) || ret;
		return ret;
	}
}
