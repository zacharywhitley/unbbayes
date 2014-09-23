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
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

import unbbayes.prs.INode;
import unbbayes.prs.Network;
import unbbayes.prs.Node;
import unbbayes.prs.id.DecisionNode;
import unbbayes.prs.id.UtilityTable;
import unbbayes.util.Debug;


/**
 * Class representing a clique in a junction tree
 *
 *@author Michael
 *@author Rommel
 *@version  06/27/2001
 */
public class Clique implements IRandomVariable, java.io.Serializable {

	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;	
	
	/** Load resource file from this package */
  	private static ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
  			unbbayes.prs.bn.resources.BnResources.class.getName());

    /**
     *  It identifies the clique uniquely if the network is connected. If disconnected, then the uniqueness is not guaranteed.
     */
    private int index;
    
    private int internalIdentificator = Integer.MIN_VALUE;

    /**
     *  Referencia para o clique pai.
     */
    private Clique parent;

    /**
     *  Lista de nodes filhos.
     */
    private List<Clique> children;

    /**
     *  Tabela de Potencial Associada ao Clique.
     */
    private PotentialTable potentialTable;
    
    /**
     *  Tabela de Utilidade Associada ao Clique.
     */
    private PotentialTable utilityTable;    

    /**
     *  Lista de Nos Clusterizados.
     */
    private List<Node> nodes;

    /**
     * List of probabilistic nodes related to Clique.
     */
    private List<Node> associatedNodes;

    /**
     *  Lista de Nos de Utilidade associados ao Clique.
     */
    private List<Node> associatedUtilNodes;    


    /**
     * Creates a new clique. Initializes array of children, array of cluster
     * nodes, and associated nodes. The association status is set to false.
     */
    public Clique() {
      this(new ProbabilisticTable(), new UtilityTable());
    }
    
    /**
     * Constructor initializing fields.
     * @param cliqueProb : potential table representing clique potentials (probability).
     * Specify this parameter if you want to use special instance of clique potential for this clique.
     * @see #Clique(PotentialTable, PotentialTable)
     */
    public Clique(PotentialTable cliqueProb) {
    	 this(cliqueProb, new UtilityTable());
    }
    
    /**
     * Constructor initializing fields.
     * @param cliqueProb : potential table representing clique potentials (probability).
     * Specify this parameter if you want to use special instance of clique potential for this clique.
     * @param cliqueUtility : potential table representing clique utility values.
     * Specify this parameter if you want to use special instance of utility table for this clique.
     */
    public Clique(PotentialTable cliqueProbability, PotentialTable cliqueUtility) {
    	children = new ArrayList<Clique>(2);
    	nodes = new ArrayList<Node>(2);
    	associatedNodes = new ArrayList<Node>(0);
    	associatedUtilNodes = new ArrayList<Node>(0);
    	potentialTable = cliqueProbability;
    	if (potentialTable == null) {
    		potentialTable = new ProbabilisticTable();
    	}
    	utilityTable = cliqueUtility;
    	if (utilityTable == null) {
    		utilityTable = new UtilityTable();
    	}
    }
    

    /**
     * Normalizes a clique in a junction tree.
     *
     *@return       normalization ratio
     */
    public float normalize() throws Exception {
        boolean fixo[] = new boolean[nodes.size()];
        ArrayList<Node> decisoes = new ArrayList<Node>();
        for (int i = 0; i < nodes.size(); i++) {        	
            if (nodes.get(i).getType() == Node.DECISION_NODE_TYPE) {
                decisoes.add(nodes.get(i));
                fixo[i] = true;
            }
        }

        if (decisoes.size() == 0) {
            return potentialTable.normalize();
        }

        int index[] = new int[decisoes.size()];
        for (int i = 0; i < index.length; i++) {
            index[i] = nodes.indexOf(decisoes.get(i));
        }
        normalizeID(0, decisoes, fixo, index, new int[nodes.size()]);
        /** @todo retornar a constante de normalizacao correta */
        return 0;
    }

    


    private void normalizeID (int control,
    		ArrayList<Node> decisoes,
                             boolean fixo[],
                             int index[],
                             int coord[]) throws Exception {

        if (control == decisoes.size()) {
            float soma = sum(0, fixo, coord);
            if (soma == 0.0) {
            	if (Debug.isDebugMode()) {
            		for (int k = 0; k < decisoes.size(); k++) {
            			Debug.println(decisoes.get(k) + " - " + decisoes.get(k).getStateAt(coord[index[k]]));
            		}
            	}
            	return;
//                throw new Exception(resource.getString("InconsistencyUnderflowException"));
            }
            div(0, fixo, coord, soma);            
            return;
        }

        DecisionNode node = (DecisionNode) decisoes.get(control);
        
        if (node.hasEvidence()) {
        	coord[index[control]] = node.getEvidence();
            normalizeID(control+1, decisoes, fixo, index, coord);
        	return;        	
        }
                
        for (int i = 0; i < node.getStatesSize(); i++) {        	
            coord[index[control]] = i;
			normalizeID(control+1, decisoes, fixo, index, coord);
        }
    }

    private float sum(int control, boolean fixo[], int coord[]) {
        if (control == nodes.size()) {
            return potentialTable.getValue(coord);
        }

        if (fixo[control]) {
            return sum(control+1, fixo, coord);
        }

        Node node = nodes.get(control);
        float retorno = 0;
        for (int i = 0; i < node.getStatesSize(); i++) {
            coord[control] = i;
            retorno += sum(control+1, fixo, coord);
        }
        return retorno;
    }

    private void div(int control, boolean fixo[], int coord[], float soma) {
        if (control == nodes.size()) {
            int cLinear = potentialTable.getLinearCoord(coord);
            potentialTable.setValue(cLinear, potentialTable.getValue(cLinear) / soma);
            return;
        }

        if (fixo[control]) {
            div(control+1, fixo, coord, soma);
            return;
        }

        Node node = nodes.get(control);
        for (int i = 0; i < node.getStatesSize(); i++) {
            coord[control] = i;
            div(control+1, fixo, coord, soma);
        }
    }


    /**
     * The parent of this clique
     *
     *@param  parent : the parent clique to set
     */
    public void setParent(Clique parent) {
        this.parent = parent;
    }


    /**
     * @return an index associated with this clique. This value may also be used as a multple-value flag
     * @see #getAssociatedProbabilisticNodes()
     * @see #getAssociatedUtilityNodes()
     */
    public int getIndex() {
        return index;
    }

    /**
     * @param index or flat indicating the status of association
     * @see #getAssociatedProbabilisticNodes()
     * @see #getAssociatedUtilityNodes()
     */
    public void setIndex(int indice) {
        this.index = indice;
    }


    /**
     *@return    size of the array of children cliques.
     *@deprecated : call {@link #getChildren()} instead
     */
    public int getChildrenSize() {
        return children.size();
    }

    /**
     * Add a child clique to this clique.
     *@param  a clique to add into the end of the array.
     */
    public void addChild(Clique child) {
        children.add(child);
    }
    
    /**
     * Remove the specified children
     * @param c the clique to remove from the children's list
     */
    public void removeChild(Clique c) {
    	children.remove(c);
    }

    /**
     * @param index: index of a child clique
     *@return    child clique in a given index
     *@deprecated call {@link #getChildren()} instead
     */
    public Clique getChildAt(int index) {
        return children.get(index);
    }


    /**
     *@return    list of vetor cluster nodes.
     *@deprecated use {@link #getNodesList()} instead
     */
    @Deprecated
    public ArrayList<Node> getNodes() {
    	if (nodes instanceof ArrayList) {
    		return (ArrayList) nodes;
    	} else {
    		return new ArrayList<Node>(nodes);
    	}
    }
    
    /**
     * This method substitutes {@link #getNodes()}
     *@return    list of vetor cluster nodes.
     */
    public List<Node> getNodesList() {
    	return nodes;
    }
    
    /**
     * @param nodes: list of vetor cluster nodes.
     */
    public void setNodesList(List<Node> nodes){
    	this.nodes = nodes;
    }


    /**
     *@return    list of associated probabilistic nodes (probabilistic nodes linked to this clique)
     *@deprecated use {@link #getAssociatedProbabilisticNodesList()} instead
     */
    @Deprecated
	public ArrayList<Node> getAssociatedProbabilisticNodes() {
        if (associatedNodes instanceof ArrayList) {
        	return (ArrayList) associatedNodes;
        } else {
        	return new ArrayList<Node>(associatedNodes);
        }
    }
    
	/**
	 *@return    list of associated probabilistic nodes (probabilistic nodes linked to this clique)
	 */
    public List<Node> getAssociatedProbabilisticNodesList() {
    	return associatedNodes;
    }
    
    /**
     * @param associatedNodes : list of associated probabilistic nodes (probabilistic nodes linked to this clique)
     */
    public void setAssociatedProbabilisticNodesList(List<Node> associatedNodes) {
    	this.associatedNodes = associatedNodes;
    }

    /**
     *@return   list of associated utility nodes (utility nodes linked to this clique)
     *@deprecated use {@link #getAssociatedUtilityNodesList()} instead
     */
    @Deprecated
    public ArrayList<Node> getAssociatedUtilityNodes() {
        if (associatedUtilNodes instanceof ArrayList) {
        	return (ArrayList) associatedUtilNodes;
        } else {
        	return new ArrayList<Node>(associatedUtilNodes);
        }
    }
    
    /**
     * Substitutes {@link #getAssociatedUtilityNodes()}
     *@return   list of associated utility nodes (utility nodes linked to this clique)
     */
    public List<Node> getAssociatedUtilityNodesList() {
    	return associatedUtilNodes;
    }
    
    /**
     * @param associatedUtilNodes : list of associated utility nodes (utility nodes linked to this clique)
     */
    public void setAssociatedUtilityNodesList(List<Node> associatedUtilNodes) {
    	this.associatedUtilNodes = associatedUtilNodes;
    }

    /**
     *@return  the potential table
     */
    public PotentialTable getProbabilityFunction() {
        return potentialTable;
    }
    
    /**
	 * @param probabilityFunction the probabilityFunction to set
	 */
	protected void setProbabilityFunction(PotentialTable probabilityFunction) {
		this.potentialTable = probabilityFunction;
	}
    

    /**
     *@return    obtains the utility function of this clique.
     */
    public PotentialTable getUtilityTable() {
        return utilityTable;
    }
    
	/**
	 * Returns the parent.
	 * @return Clique
	 */
	public Clique getParent() {
		return parent;
	}
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("C{");
		for (int j = nodes.size()-1; j>=0;j--) {
			sb.append(nodes.get(j) + " ");				
		}
		sb.append("}");
		return sb.toString();
	}


	/**
	 * @return the children
	 */
	public List<Clique> getChildren() {
		return children;
	}


	/**
	 * @param children the children to set
	 */
	public void setChildren(List<Clique> children) {
		this.children = children;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.IRandomVariable#getInternalIdentificator()
	 */
	public int getInternalIdentificator() {
		return this.internalIdentificator;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.IRandomVariable#setInternalIdentificator(int)
	 */
	public void setInternalIdentificator(int internalIdentificator) {
		this.internalIdentificator = internalIdentificator;
	}

	/**
	 * Generates a clone of this clique. It does not generate associated separators.
	 * @param net: nodes in this network will be used to fill {@link Clique#getNodesList()}
	 * @return a cloned clique
	 */
	public Clique clone(Network net) {
		Clique newClique = new Clique();
		newClique.setInternalIdentificator(this.getInternalIdentificator());
		
//		boolean hasInvalidNode = false;	// this will be true if a clique contains a node not in new network.
		// add nodes to clique
		for (Node node : this.getNodes()) {
			Node newNode = null;	
			if (net != null) {
				// extract associated node, because they are related by name
				newNode = net.getNode(node.getName()); 
			}
			if (newNode == null) {
				if (node instanceof ProbabilisticNode) {
					// create the new node on the fly
					newNode = (ProbabilisticNode) ((ProbabilisticNode) node).clone();
				} else {
					throw new IllegalStateException("Unable to clone: " + node + ", " + this);
				}
			}
			newClique.getNodes().add(newNode);
		}
		
		// add nodes to the list "associatedProbabilisticNodes"
		for (Node node : this.getAssociatedProbabilisticNodes()) {
			Node newNode = null;
			if (net != null) {
				// extract associated node, because they are related by name
				newNode = net.getNode(node.getName());
			}
			if (newNode == null) {
				if (node instanceof ProbabilisticNode) {
					// create the new node on the fly
					newNode = (ProbabilisticNode) ((ProbabilisticNode) node).clone();
				} else {
					throw new IllegalStateException("Unable to clone: " + node + ", " + this);
				}
			}
			// this.getNodes() and this.getAssociatedProbabilisticNodes() may be different... Copy both separately. 
			newClique.getAssociatedProbabilisticNodes().add(newNode);
		}
		
		newClique.setIndex(this.getIndex());
		
		// copy clique potential variables
		PotentialTable origPotential = this.getProbabilityFunction();
		PotentialTable copyPotential = newClique.getProbabilityFunction();
		for (int i = 0; i < origPotential.getVariablesSize(); i++) {
			Node newNode = null;
			if (net != null) {
				newNode = net.getNode(origPotential.getVariableAt(i).getName());
			}
			if (newNode == null) {
				if (origPotential.getVariableAt(i) instanceof ProbabilisticNode) {
					// create the new node on the fly
					newNode = (ProbabilisticNode) ((ProbabilisticNode) origPotential.getVariableAt(i)).clone();
				} else {
					throw new IllegalStateException("Unable to clone: " + origPotential.getVariableAt(i) + ", " + this);
				}
			}
			copyPotential.addVariable(newNode);
		}
		
		// copy the values of clique potential
		copyPotential.setValues(origPotential.getValues());
		copyPotential.copyData();
		
		return newClique;
	}
	
	/**
	 * Just delegates to {@link #clone(Network)} passing null as argument.
	 * @see java.lang.Object#clone()
	 * @see #clone(Network)
	 */
	public Object clone() throws CloneNotSupportedException {
		return this.clone(null);
	}
	

	/**
	 * Will join this clique to another clique.
	 * It is assumed that clique potentials are already globally consistent 
	 * (i.e. marginalizing out the clique table will result in separator table).
	 * For example, if this clique has nodes [X,Y], and the other clique has nodes [Y,Z],
	 * then this method will change this clique to [X,Y,Z].
	 * {@link #getProbabilityFunction()} will also be expanded and filled with joint probability of [X,Y,Z] obtained from the original two cliques.
	 * Content of separators are kept untouched.
	 * Content of related nodes are also kept untouched.
	 * TODO also join utility table {@link #getUtilityTable()}
	 * @param cliqueToJoin : clique to be joined to this clique.
	 * @param separator : separator between this clique and cliqueToJoin. No consistency check will be made.
	 * @see {@link #clone(Network)}
	 * @see #getProbabilityFunction()
	 * @see PotentialTable#directOpTab(PotentialTable, int)
	 */
	public void join(Clique cliqueToJoin) {
		// basic assertion
		if (cliqueToJoin == null || cliqueToJoin.getNodesList() == null) {
			return;
		}
		
		if (this.getUtilityTable() != null && this.getUtilityTable().variableCount() > 0) {
			throw new UnsupportedOperationException("Current version is unable to join cliques in influence diagrams (i.e. those using utility tables)");
		}
		
		// get nodes in cliqueToJoin that is not in this clique.
		// I'm using a list in order to keep same ordering
		List<INode> disjointNodes = new ArrayList<INode>(cliqueToJoin.getNodesList());	// clone nodes in cliqueToJoin
		disjointNodes.removeAll(this.getNodesList());
		
		// get the separator potentials by summing-out the nodes in disjointNodes
		if (cliqueToJoin.getProbabilityFunction() != null) {
			PotentialTable separatorTable = cliqueToJoin.getProbabilityFunction().getTemporaryClone();
			if (separatorTable != null) {
				for (INode nodeToSumOut : disjointNodes) {
					separatorTable.removeVariable(nodeToSumOut);
				}
				
				// also add to clique table
				PotentialTable cliqueTable = this.getProbabilityFunction();
				if (cliqueTable != null) {
					for (INode nodeToAdd : disjointNodes) {
						cliqueTable.addVariable(nodeToAdd);
					}
					
					// absorb cliqueToJoin: multiply by potentials in cliqueToJoin, and divide by separatorTable
					cliqueTable.opTab(separatorTable, PotentialTable.DIVISION_OPERATOR);	// divide first, to avoid underflow (because division by 0<x<=1 will increase value)
					cliqueTable.opTab(cliqueToJoin.getProbabilityFunction(), PotentialTable.PRODUCT_OPERATOR);
				}
			}
		}
		
		// add disjointNodes in this clique
		this.getNodesList().addAll((List) disjointNodes);
		
	}



}