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
   


	private PotentialTable utilityTable;
    private ArrayList<Node> nos;

    private Clique clique1;

    private Clique clique2;
    
    private Separator() {
    	nos = new ArrayList<Node>();
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
    	this(clique1, clique2, true);        
    }
    
    /**
     *  Constructor for the Separator.
     *  
     * @param c1
     * @param c2
     * @param updateCliques
     */
    public Separator(Clique clique1, Clique clique2, boolean updateCliques) {
    	this();    	
        this.clique1 = clique1;
        this.clique2 = clique2;
        if (updateCliques) {
	        clique2.setParent(clique1);
	        clique1.addChild(clique2);
        }
    }

    /**
     * Constructor initializing fields
     * @param assetClique1
     * @param assetClique2
     * @param table : {@link #getProbabilityFunction()} will be initialized to this object
     */
    public Separator(Clique assetClique1, Clique assetClique2,
			PotentialTable table) {
		this(assetClique1, assetClique2);
		if (table != null) {
			tabelaPot = table;
		}
	}

	/**
     *@param  nodeList list of clusterized nodes
     */
    public void setNodes(ArrayList<Node> nodeList) {
        this.nos = nodeList;
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
        return nos;
    }


	public boolean equals(Object obj) {
		if (obj != null) {
			return this.toString().equals(obj.toString());
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
		for (int j = nos.size()-1; j>=0;j--) {
			sb.append(nos.get(j) + " ");				
		}
		sb.append("}");
		sb.append(this.getClique1());
		sb.append("-");
		sb.append(this.getClique2());
		return sb.toString();
	}
}

