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
package unbbayes.prs.bn;


import java.util.ArrayList;

import unbbayes.prs.Node;
import unbbayes.prs.id.UtilityTable;

/**
 * It represents a separator between cliques in a junction tree.
 *
 *@author     Michael
 *@author     Rommel
 */
public class Separator implements IRandomVariable, java.io.Serializable {

	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;
	
    private PotentialTable tabelaPot;
   
    private int internalIdentificator = Integer.MIN_VALUE;

	private PotentialTable utilityTable;
    private ArrayList<Node> nodes;

    private Clique clique1;

    private Clique clique2;
    
    private Separator() {
    	nodes = new ArrayList<Node>(1);
        tabelaPot = new ProbabilisticTable();
        utilityTable = new UtilityTable();
    }
    
    /**
     *  Constructor for the Separator. It updates the cliques, 
     * adding clique2 to the child list of clique1 and setting 
     * clique1 as the parent of clique2. 
     *
     * @param clique1 the origin clique
     * @param clique2 the destination clique
     */
    public Separator(Clique clique1, Clique clique2) {
    	this(clique1, clique2, new ProbabilisticTable(), new UtilityTable(), true);     
    }
    
    /**
     *  Constructor for the Separator.
     *  
     * @param c1
     * @param c2
     * @param updateCliques
     */
    public Separator(Clique clique1, Clique clique2, boolean updateCliques) {
    	this(clique1, clique2, new ProbabilisticTable(), new UtilityTable(), updateCliques);
    }

    /**
     * Constructor initializing fields
     * @param clique1
     * @param clique2
     * @param table : {@link #getProbabilityFunction()} will be initialized to this object
     */
    public Separator(Clique clique1, Clique clique2, PotentialTable table) {
    	this(clique1, clique2, table, new UtilityTable(), true);
    }
   
    /**
     * Constructor initializing fields
     * @param assetClique1
     * @param assetClique2
     * @param probTable : probability table
     * @param utilTable : utility table
     */
    public Separator(Clique assetClique1, Clique assetClique2,
			PotentialTable probTable, PotentialTable utilTable) {
		this(assetClique1, assetClique2, probTable, utilTable, true);
	}
    
    /**
     * Constructor initializing fields
     * @param clique1
     * @param clique2
     * @param probTable : probability table
     * @param utilTable : utility table
     * @param updateCliques
     */
    public Separator(Clique clique1, Clique clique2,
    		PotentialTable probTable, PotentialTable utilTable, boolean updateCliques) {
    	this();
    	this.clique1 = clique1;
    	this.clique2 = clique2;
    	if (probTable != null) {
    		tabelaPot = probTable;
    	}
    	if (utilTable != null) {
    		utilityTable = utilTable;
    	}
    	if (updateCliques) {
	        clique2.setParent(clique1);
	        clique1.addChild(clique2);
        }
    }

	/**
     *@param  nodeList list of clusterized nodes
     */
    public void setNodes(ArrayList<Node> nodeList) {
        this.nodes = nodeList;
    }


    /**
     *@return    potential table associated with the separator.
     */
    public PotentialTable getProbabilityFunction() {
        return tabelaPot;
    }
    
    /**
	 * @param probabilityFunction the probabilityFunction to set
	 */
	protected void setProbabilityFunction(PotentialTable probabilityFunction) {
		this.tabelaPot = probabilityFunction;
	}

    /**
     *@return    utility table associated with the separator
     */
    public PotentialTable getUtilityTable() {
        return utilityTable;
    }


    /**
     *@return    List of clusterized nodes.
     */
    public ArrayList<Node> getNodes() {
        return nodes;
    }


	public boolean equals(Object obj) {
//		if (obj != null) {
//			return this.toString().equals(obj.toString());
//		}
		if (obj != null && obj instanceof IRandomVariable) {
			return this.getInternalIdentificator() == ((IRandomVariable)obj).getInternalIdentificator();
		}
		
		return super.equals(obj);
	}

	/**
     *  Returns the first (origin) clique
     *
     *@return    node 1
     */
    public Clique getClique1() {
        return clique1;
    }


    /**
     *  Returns the second (destination) clique.
     *
     *@return    node 2
     */
    public Clique getClique2() {
        return clique2;
    }

    
    /**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("S{");
		for (int j = nodes.size()-1; j>=0;j--) {
			sb.append(nodes.get(j) + " ");				
		}
		sb.append("}");
		sb.append(this.getClique1());
		sb.append("-");
		sb.append(this.getClique2());
		return sb.toString();
	}
	
	/**
	 * Checks whether this separator is complete in the moralized Bayes net structure.
	 * That is, checks if nodes in this separator are fully connected after moralization
	 * (i.e. all pairs of variables are either connected by arcs, or shares at least one common child in the Bayes net).
	 * Connections and common children will be retrieved by referencing {@link Node#getParentNodes()}
	 * or {@link Node#getChildNodes()}.
	 * <br/> <br/>
	 * Please, notice that nodes in this separator will be retrieved from {@link #getNodes()},
	 * not from the separator table {@link #getProbabilityFunction()},
	 * so be careful if your implementation doesn't keep {@link #getNodes()} and {@link IProbabilityFunction#getVariableAt(int)}
	 * consistent.
	 * @return true if this separator is empty, or has only 1 variable, or all variables
	 * are fully connected in a moralized network. Returns false otherwise.
	 * @see Node#isParentOf(Node)
	 * @see Node#getChildren()
	 */
	public boolean isComplete() {
		// retrieve the nodes in this separator
		ArrayList<Node> nodesInSeparator = getNodes();
		
		// if empty or has only 1 node, this is complete
		if (nodesInSeparator == null || nodesInSeparator.size() <= 1) {
			return true;
		}
		
		// At this point of code, there is at least 1 pair of nodes in this separator.
		// Check if all nodes in this separator are pairwise connected, or shares the same children
		int separatorSize = nodesInSeparator.size();	// how many nodes there are in this separator
		for (int i = 0; i < separatorSize-1; i++) {
			Node node1 = nodesInSeparator.get(i); // extract one of the pair of nodes being verified
			for (int j = i+1; j < separatorSize; j++) {
				Node node2 = nodesInSeparator.get(j); // extract the other node in the pair being verified
				// If we found at least 1 pair of nodes not connected and not sharing same children, then separator is not complete.
				// First, check if there is any connection. 
				if ( node1.isParentOf(node2)				// there is an arc node1->node2
						|| node2.isParentOf(node1) ) { 	// there is an arc node2->node1
					continue;	// this pair was fine, so check other pairs of nodes
				}
				
				// There is no direct connection, but we should check if there is a common children (to see if they would be connected if graph is moralized)
				boolean hasCommonChild = false;
				for (Node child : node1.getChildren()) {
					if (node2.isParentOf(child)) {
						hasCommonChild = true;
						break;	// we need just 1 common child in order to have node1 and node2 connected in moralized net.
					}
				}
				if (hasCommonChild) {
					continue;	// this pair was fine, so check other pairs of nodes
				}
				
				// if program reached this point, then current pair is not connected, and doesn't share common child.
				return false;	// we can return immediately, because 1 disconnected pair makes this separator not fully connected (in the moralized net).
			}
		}
		
		// if program reached this line, all nodes in this separator are either connected or shares same children
		return true;	// so, this separator is complete
	}

	/**
	 * Internal identificators of separators
	 * are using negative values, in order to distinguish
	 * from cliques
	 * @return the internalIdentificator
	 */
	public int getInternalIdentificator() {
		return internalIdentificator;
	}

	/**
	 * Internal identificators of separators
	 * are using negative values, in order to distinguish
	 * from cliques
	 * @param internalIdentificator the internalIdentificator to set
	 */
	public void setInternalIdentificator(int internalIdentificator) {
		this.internalIdentificator = internalIdentificator;
	}

//	/**
//	 * This method simply fills all entries in {@link #getProbabilityFunction()}
//	 * with 1, which is the null value in multiplication and division
//	 * (these two operations are the ones used in junction tree propagation
//	 * for global consistency between tables in cliques and separators)
//	 * @see JunctionTree#initBelief(Separator)
//	 * @see JunctionTree#initConsistency()
//	 * @see PotentialTable#setValue(int, float)
//	 */
//	public void fillTableWith1() {
//		PotentialTable table = this.getProbabilityFunction();
//		if (table != null) {
//			table.fillTable(1f);
//		}
//	}
}

