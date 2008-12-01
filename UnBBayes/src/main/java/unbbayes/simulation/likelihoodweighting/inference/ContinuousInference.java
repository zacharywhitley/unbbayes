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
package unbbayes.simulation.likelihoodweighting.inference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import unbbayes.io.XMLBIFIO;
import unbbayes.io.exception.LoadException;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ITabledVariable;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.TreeVariable;
import unbbayes.prs.bn.continuous.ContinuousNode;
import unbbayes.util.SortUtil;

/**
 * 
 * Class that implements the Monte Carlo simulation.
 * It uses forward sampling to calculate a RV's probability mass function. Based on its pmf, it then 
 * calculates the cumulative density function. Finally, a random number between 0 and 1 is generated 
 * and the sampled state is defined by the state the random number relates based on its cdf. 
 * 
 * @author Danilo Custodio
 * @author Rommel Carvalho
 *
 */
public class ContinuousInference {
	
	protected ProbabilisticNetwork pn;
	protected ProbabilisticNetwork clonedPN;
	protected List<Node> nodeOrderQueue;
	private List<Node> discreteNodeList;
	private List<Node> continuousNodeList;
	
	/**
	 * Return the order the nodes are in the sampled matrix.
	 * @return The order the nodes are in the sampled matrix.
	 */
	public List<Node> getNodeOrderQueue() {
		return nodeOrderQueue;
	}
	
	public ContinuousInference(ProbabilisticNetwork pn){		
		this.pn = pn;
		this.clonedPN = clonePN(pn);
		discreteNodeList = new ArrayList<Node>();
		continuousNodeList = new ArrayList<Node>();
		for (Node node : pn.getNodes()) {
			if (node.getType() == Node.CONTINUOUS_NODE_TYPE) {
				continuousNodeList.add(node);
			} else {
				discreteNodeList.add(node);
			}
		} 
	}
	
	public void start(){
		nodeOrderQueue = new ArrayList<Node>();		
		createOrderQueue();
	}
	
	/**
	 * Creates the queue of the nodes that are going to be analyzed.
	 */
	protected void createOrderQueue(){
		// Keeps track of the nodes that have already been added to the queue (nodeAddedList[nodeIndex]=true).
		boolean[] nodeAddedList = new boolean[continuousNodeList.size()];
		initOrderQueue(nodeAddedList);											
		for(int i = 0; i < nodeOrderQueue.size(); i++){
			// All children of continuous nodes are also continuous.
			Node node = nodeOrderQueue.get(i);
			addToOrderQueue(node.getChildren(), nodeAddedList);			
		}		
	}
	
	/**
	 * Initializes the queue with the nodes that are root. In other words. 
	 * It will put in the queue the nodes that do not have parents.
	 * @param nodeAddedList Keeps track of the nodes that have already been added to the queue (nodeAddedList[nodeIndex]=true).
	 */
	protected void initOrderQueue(boolean[] nodeAddedList){
		for (int i = 0; i < continuousNodeList.size(); i++) {
			Node node = continuousNodeList.get(i);
			if(node.getParents().size() == 0 ) {
				nodeAddedList[i]= true;					
				nodeOrderQueue.add(node);
			}
		}			
	}
	
	/**
	 * Take the children of a node that have already been added to the queue. Analyze them
	 * one by one and add the child that is not in the queue yet. 
	 * @param children Children of a node that is already in the queue.
	 * @param nodeAddedList Nodes that have already been added to the queue.
	 */
	protected void addToOrderQueue(ArrayList<Node> children, boolean[] nodeAddedList){
		for(int i = 0 ; i < children.size(); i++){
			Node n1 = children.get(i);
			for(int j = 0 ; j < continuousNodeList.size(); j++){
				Node n2 = continuousNodeList.get(j);
				if(n1.getName().equals(n2.getName())){
					if(!nodeAddedList[j]){
						nodeOrderQueue.add(n1);						
						nodeAddedList[j] = true;						
						break;						
					}										
				}				
			}	
		}	
	}
	

	protected void simulate() {
		for(int i = 0; i < nodeOrderQueue.size(); i++) {
			
			Node node = nodeOrderQueue.get(i);
			List<Node> discreteParentList = new ArrayList<Node>();
			List<Node> continuousParentList = new ArrayList<Node>();
			for (Node parentNode : clonedPN.getNode(node.getName()).getParents()) {
				if (parentNode.getType() == Node.PROBABILISTIC_NODE_TYPE) {
					discreteParentList.add(parentNode);
				} else if (parentNode.getType() == Node.CONTINUOUS_NODE_TYPE) {
					continuousParentList.add(parentNode);
				}
			}
			SortUtil.sortNodeListByName(discreteParentList);
			SortUtil.sortNodeListByName(continuousParentList);
			
			// The max of possible networks to be compiled to get its posterior is the
			// number of discrete parents this continuous node has.
			// But there might be two parent nodes in the same PN, in that case the network used
			// will be the same.
			ProbabilisticNetwork[] pnList = new ProbabilisticNetwork[discreteParentList.size()];
			List<Node> nodeInNetwork;
			boolean nodeVisitedBefore;
			for (int j = 0; j < discreteParentList.size(); j++) {
				nodeVisitedBefore = false;
				for (int k = 0; k < j && !nodeVisitedBefore; k++) {
					for (Node n : pnList[k].getNodes()) {
						if (discreteParentList.get(j).getName().equals(n.getName())) {
							nodeVisitedBefore = true;
							pnList[j] = pnList[k];
							break;
						}
					}
				}
				if (!nodeVisitedBefore) {
					nodeInNetwork = new ArrayList<Node>();
					addAdjacentNodes(clonedPN.getNode(discreteParentList.get(j).getName()), nodeInNetwork);
					pnList[j] = clonedPN;
					for (Node nodeToRemove : clonedPN.getNodes()) {
						if (!nodeInNetwork.contains(nodeToRemove)) {
							clonedPN.removeNode(nodeToRemove);
						}
					}
					clonedPN = clonePN(this.pn);
				}
			}
			
			// Now we have the posterior of all parents of the current continuous node.
			// TODO get multi coord to get probability and multiply to come up with alpha from C. Weighted Gaussian Sum!
		}
	}
	
	protected void addAdjacentNodes(Node node, List<Node> nodeInNetwork) {
		nodeInNetwork.add(node);
		for (Node child : node.getChildren()) {
			if (child instanceof ProbabilisticNode && !nodeInNetwork.contains(child)) {
				addAdjacentNodes(child, nodeInNetwork);
			}
		}
		for (Node parent : node.getParents()) {
			if (parent instanceof ProbabilisticNode && !nodeInNetwork.contains(parent)) {
				addAdjacentNodes(parent, nodeInNetwork);
			}
		}
	}

	/**
	 * Return the indexes (sampling order) in the queue for the parents of a given node. 
	 * @param node The node to retrieve the parents for finding the indexes.
	 * @return List of indexes (sampling order) of a node's parents in the queue.
	 */
	protected List<Integer> getParentsIndexesInQueue(ProbabilisticNode node){
		List<Integer> indexes = new ArrayList<Integer>();
		ArrayList<Node> parents = node.getParents();		
		for(int i = 0 ; i < parents.size();i++){
			Node parentNode = parents.get(i);
			indexes.add(getIndexInQueue(parentNode));						
		}	
		return indexes;		
	}
	
	/**
	 * Retrieves the node's index in the queue.  
	 * @param node
	 * @return
	 */
	protected Integer getIndexInQueue(Node node){
		for(int i = 0 ; i <nodeOrderQueue.size();i++){
			if(node.getName().equals(nodeOrderQueue.get(i).getName())){				
				return i;				
			}			
		}	
		return null;	
	}
	
	/**
	 * As I am not sure if the clone methods are corrected. I decided to clone the network by
	 * saving it in a file and loading again as another network.
	 * @param network
	 * @return
	 */
	protected ProbabilisticNetwork clonePN(ProbabilisticNetwork network) {
		ProbabilisticNetwork clone = null;
		
		XMLBIFIO io = new XMLBIFIO();
		try {
			File file = new File("clone.xml");
			io.save(file, network);
			clone = io.load(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LoadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return clone;
	}
	
}
