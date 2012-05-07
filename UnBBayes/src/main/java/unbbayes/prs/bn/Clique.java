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
import java.util.List;
import java.util.ResourceBundle;

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
     *  Identifica unicamente o no.
     */
    private int index;

    /**
     *  Referencia para o clique pai.
     */
    private Clique parent;

    /**
     *  Lista de nos filhos.
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
    private ArrayList<Node> nos;

    /**
     *  Lista de N�s Probabilisticos associados ao Clique.
     */
    private ArrayList<Node> nosAssociados;

    /**
     *  Lista de Nos de Utilidade associados ao Clique.
     */
    private ArrayList<Node> associatedUtilNodes;    


    /**
     * Creates a new clique. Initializes array of children, array of cluster
     * nodes, and associated nodes. The association status is set to false.
     */
    public Clique() {
        children = new ArrayList<Clique>();
        nos = new ArrayList<Node>();
        nosAssociados = new ArrayList<Node>();
        associatedUtilNodes = new ArrayList<Node>();
        potentialTable = new ProbabilisticTable();
        utilityTable = new UtilityTable();
    }
    

    /**
     * Normalizes a clique in a junction tree.
     *
     *@return       normalization ratio
     */
    public float normalize() throws Exception {
        boolean fixo[] = new boolean[nos.size()];
        ArrayList<Node> decisoes = new ArrayList<Node>();
        for (int i = 0; i < nos.size(); i++) {        	
            if (nos.get(i).getType() == Node.DECISION_NODE_TYPE) {
                decisoes.add(nos.get(i));
                fixo[i] = true;
            }
        }

        if (decisoes.size() == 0) {
            return potentialTable.normalize();
        }

        int index[] = new int[decisoes.size()];
        for (int i = 0; i < index.length; i++) {
            index[i] = nos.indexOf(decisoes.get(i));
        }
        normalizeID(0, decisoes, fixo, index, new int[nos.size()]);
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
        if (control == nos.size()) {
            return potentialTable.getValue(coord);
        }

        if (fixo[control]) {
            return sum(control+1, fixo, coord);
        }

        Node node = nos.get(control);
        float retorno = 0;
        for (int i = 0; i < node.getStatesSize(); i++) {
            coord[control] = i;
            retorno += sum(control+1, fixo, coord);
        }
        return retorno;
    }

    private void div(int control, boolean fixo[], int coord[], float soma) {
        if (control == nos.size()) {
            int cLinear = potentialTable.getLinearCoord(coord);
            potentialTable.setValue(cLinear, potentialTable.getValue(cLinear) / soma);
            return;
        }

        if (fixo[control]) {
            div(control+1, fixo, coord, soma);
            return;
        }

        Node node = nos.get(control);
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
     */
    public Clique getChildAt(int index) {
        return children.get(index);
    }


    /**
     *@return    list of vetor cluster nodes.
     */
    public ArrayList<Node> getNodes() {
        return nos;
    }


    /**
     *@return    list of associated probabilistic nodes (probabilistic nodes linked to this clique)
     */
    public ArrayList<Node> getAssociatedProbabilisticNodes() {
        return nosAssociados;
    }

    /**
     *@return   list of associated utility nodes (utility nodes linked to this clique)
     */
    public ArrayList<Node> getAssociatedUtilityNodes() {
        return associatedUtilNodes;
    }

    /**
     *@return  the potential table
     */
    public PotentialTable getProbabilityFunction() {
        return potentialTable;
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
		for (int j = nos.size()-1; j>=0;j--) {
			sb.append(nos.get(j) + " ");				
		}
		return sb.toString();
	}

}