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
package unbbayes.prs.msbn;

import java.util.ArrayList;
import java.util.List;

import unbbayes.prs.Edge;
import unbbayes.prs.Node;
import unbbayes.prs.bn.JunctionTree;
import unbbayes.prs.bn.SingleEntityNetwork;
import unbbayes.util.SetToolkit;

/**
 * SubNetwork of a multi-sectioned network.
 * @author Michael S. Onishi
 */
public class SubNetwork extends SingleEntityNetwork {

	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;		
	
	private char[] visited;
	
	/**
	 * Child subnetworks. 
	 */
	protected List<SubNetwork> adjacents;
	
	/**
	 * Parent subnetwork.
	 */
	protected SubNetwork parent;
	
	/**
	 * Creates a new subnetwork with the specified id.
	 * @param id	The subnetwork's id.		
	 * @see unbbayes.prs.bn.SingleEntityNetwork#Network(String)
	 */
	public SubNetwork(String id) {
		super(id);
		adjacents = new ArrayList<SubNetwork>();		
	}
	
	/**
	 * Returns the number of adjacents.
	 * @return the number of adjacents.
	 */
	public int getAdjacentsSize() {
		return adjacents.size();		
	}
	
	/**
	 * Returns the adjacent subnetwork in the specified index.
	 * @param index		the index of the adjacent subnetwork.
	 * @return SubNetwork	the adjacent subnetwork in the specified index.
	 */
	public SubNetwork getAdjacentAt(int index) {
		return (SubNetwork) adjacents.get(index);		
	}
	
	/**
	 * Adds the specified subnetwork in the adjacents list.
	 * @param net	
	 */
	public void addAdjacent(SubNetwork net) {
		adjacents.add(net);			
	}
	
	/**
	 * Sets the parent of this subnetwork
	 * @param the parent of this subnetwork
	 */
	public void setParent(SubNetwork parent) {
		this.parent = parent;		
	}
	
	/**
	 *	Local version of the moralize method.
	 */
	protected void localMoralize() {
		super.moralize();				
	}
	
	/**
	 * Compiles this subnetwork in a junction tree structure.
	 * @throws Exception	If a junction tree cannot be constructed.
	 */
	protected void compileJunctionTree() throws Exception {
		super.compileJT(new JunctionTree());
	}
	
	/**
	 * Returns the junctiontree.
	 * @return the junctiontree.
	 */
	protected JunctionTree getJunctionTree() {
		return junctionTree;
	}
	
	protected void initTriangulation() {
		copiaNos = SetToolkit.clone(nodeList);
		oe = new ArrayList<Node>();
		oe.ensureCapacity(copiaNos.size()); 
	}

	/**
	 * Verify the local consistency.
	 * @see unbbayes.prs.bn.SingleEntityNetwork#verifyConsistency()
	 */
	protected void verifyConsistency() throws Exception {
		super.verifyConsistency();		
	}
	
	/**
	 * Makes the path from this subnetwork to the specified subnetwork
	 * @param net		The final subnetwork of the path to create.
	 * @return List	The path from this subnetwork to the specified subnetwork
	 */
	protected List makePath(SubNetwork net) {
		List<SubNetwork> path = new ArrayList<SubNetwork>();
		makePath(net, path);
		return path;			
	}
	
	private boolean makePath(SubNetwork net, List<SubNetwork> path) {
		if (path.contains(this)) {
			return false;
		}
		
		path.add(this);
		
		if (this.equals(net)) {
			return true;
		}
		
		for (int i = adjacents.size()-1; i>=0; i--) {
			SubNetwork netAux = (SubNetwork) adjacents.get(i);
			if (netAux.makePath(net, path)) {
				return true;				
			}			
		}
		
		if (parent.makePath(net, path)) {
			return true;			
		}	
		
		path.remove(this);
		return false;
	}
	
	/**
	 * Minimum weight triangulation.
	 * First, the not d-sepnodes are eliminated, and after, the d-sepnodes. 
	 *
	 * @param adj		  Adjacent subnetwork to make the d-sepnodes list and eliminate in the correct order.
	 * @return boolean   true if any edge was inserted, false otherwise.
	 */	
	protected boolean elimine(SubNetwork adj) {
		System.out.println("Elimine");
		
		oe.clear();
		ArrayList<Node> inter = SetToolkit.intersection(copiaNos, adj.copiaNos);				
		ArrayList<Node> auxNos = SetToolkit.clone(copiaNos);
		
		int sizeAnt = auxNos.size();
				
		auxNos.removeAll(inter);
		
//		assert inter.size() + auxNos.size() == sizeAnt;
		
		boolean inseriu = false;
		while (minimumWeightElimination(auxNos)) {
			inseriu = true;
		}
		
		while (minimumWeightElimination(inter)) {
			inseriu = true;			
		}
		
		makeAdjacents();
		
		return inseriu;
	}
	
	/**
	 * Depth-first eliminate
	 * @param caller
	 */
	protected void elimineProfundidade(SubNetwork caller) {
		System.out.println("Elimine profundidade");
		
		for (int i = adjacents.size()-1; i >= 0; i--) {
			SubNetwork ai = (SubNetwork) adjacents.get(i);			
			if (elimine(ai)) {
				updateArcsAux(ai);
			}
			ai.elimineProfundidade(this);
		}
		
		if (caller != null) {			
			if (elimine(caller)) {				
				updateArcsAux(caller);
			}
		}	
	}
	
	/**
	 * Distribute the edges inserted in this subnetwork to the adjacents.
	 */
	protected void distributeArcs() {
		System.out.println("DistributeArcs");
		
		for (int i = adjacents.size()-1; i>=0; i--) {
			SubNetwork net = (SubNetwork) adjacents.get(i);
			updateArcsBilateral(net);
			net.distributeArcs();
		}
	}
	
	private void updateArcsAux(SubNetwork net) {
		ArrayList<Node> dsepset = SetToolkit.intersection(nodeList, net.nodeList);
		for (int i = arcosMarkov.size()-1; i>=0; i--) {
			Edge e = (Edge) arcosMarkov.get(i);
			if (dsepset.contains(e.getOriginNode()) 
				&& dsepset.contains(e.getDestinationNode())) {
				
				Node a = net.getNode(e.getOriginNode().getName());
				Node b = net.getNode(e.getDestinationNode().getName()); 
				
//				assert(a != null && b != null);
				if (! a.getAdjacents().contains(b) && ! b.getAdjacents().contains(a)) {
					a.getAdjacents().add(b);
					b.getAdjacents().add(a);
					Edge newEdge = new Edge(a,b);
					net.arcosMarkov.add(newEdge);

					System.out.println(newEdge);
				}
			}
		}
	}
	
	private void updateArcsBilateral(SubNetwork net) {
		System.out.println("updateArcs");
		updateArcsAux(net);
		net.updateArcsAux(this);		
	}
	
	/**
	 * Initialize the visited array for the cycle detection.
	 */
	protected void initVisited() {
		visited = new char[nodeList.size()];	
	}
	
	/**
	 * Method used to verify cycles in the union graph of sub-networks.
	 * @throws Exception	If the union graph has cycle.
	 */
	protected void distributedCycle() throws Exception {
		for (int i = nodeList.size()-1; i>=0; i--) {
			dfsCycle(i, null);
		}
	}
	
	
	/**
     * Depth first search to verify cycle.
     */
    private void dfsCycle(int nodeIndex, Node caller) throws Exception {
    	if (visited[nodeIndex] != 0) {
    		if (visited[nodeIndex] == 1) {
    			// Back edge. Has cycle!
                throw new Exception("CicleNetException");
    		}
    		return;    		
    	}
    	
    	Node node = nodeList.get(nodeIndex);    	
    	visited[nodeIndex] = 1;
    	
    	for (int i = node.getChildren().size()-1; i >= 0; i--) {
    		int newIndex = getNodeIndex(node.getChildren().get(i).getName());
    		dfsCycle(newIndex, node);
    	}
    	
    	if (parent != null) {    		
	    	int index = parent.getNodeIndex(node.getName());
	    	if (index != -1) {
	    		Node next = parent.getNodeAt(index);
	    		if (caller == null || (caller != null && ! next.equals(caller))) {
	    			parent.dfsCycle(index, node);
	    		}
	    	}
    	}
    	
    	for (int i = adjacents.size()-1; i>=0; i--) {
    		SubNetwork net = (SubNetwork) adjacents.get(i);
    		int index = net.getNodeIndex(node.getName());
    		if (index != -1) {
    			Node next = net.getNodeAt(index);
	    		if (caller == null || (caller != null && ! next.equals(caller))) {
    				net.dfsCycle(index, node);
	    		}
    		}
    	}
    	
    	visited[nodeIndex] = 2;
    }
    
	/**
	 * @see unbbayes.prs.bn.SingleEntityNetwork#updateMarginais()
	 */
	protected void updateMarginais() {
		super.updateMarginais();
	}
	
	/*
	public void normalize() {
		for (int i = 0 ; i < junctionTree.getCliques().size(); i++) {
			Clique c1 = (Clique) junctionTree.getCliques().get(i);
			try {
				System.out.println(c1.normalize());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	*/
}