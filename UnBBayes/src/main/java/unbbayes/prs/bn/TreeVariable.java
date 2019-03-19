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

import unbbayes.prs.INode;
import unbbayes.prs.Node;

/**
 * Abstract class for variables that will be shown in the tree of nodes and states with 
 * their probabilities in the compilation panel.
 */
public abstract class TreeVariable extends Node implements java.io.Serializable {

    // Clique associated to this variable.
    protected IRandomVariable cliqueAssociado;

    /** Store the marginal list (they add to 1). 
     * @deprecated use {@link #getMarginalAt(int)} and {@link #setMarginalAt(int, float)} to access these values.*/
    protected float[] marginalList;
    
    private float[] marginalCopy;

    private int evidence = -1;
//    private boolean hasLikelihood = false;

	private float[] likelihood = null;
	
	private List<INode> likelihoodParents = new ArrayList<INode>();

    /**
     * @deprecated use {@link #updateMarginal()} instead
     */
    protected abstract void marginal();
    
    /**
     * This method has to be overwritten in order to 
     * show the correct marginal list in the tree.
     * @deprecated the way marginal is updated depends on the underlying inference algorithm, so this method should not be here.
     */
    public void updateMarginal() {
    	this.marginal();
    }
    
    public void initMarginalList() {
    	marginalList = new float[getStatesSize()];
    }
    
    //by young
	public boolean isMarginalList() 
	{  				
		if( marginalList != null )
			return true;
		
		return false;		
	}
   //by young end
	
    public void copyMarginal() {
    	int size = marginalList.length;
    	marginalCopy = new float[size];
    	System.arraycopy(marginalList, 0, marginalCopy, 0, size);
    }
    
    public void restoreMarginal() {
    	if (marginalCopy == null) {
    		return;
    	}
    	int size = marginalList.length;
    	System.arraycopy(marginalCopy, 0, marginalList, 0, size);
    }
    
    /**
     *  Return the marginal probability associated to a specific index/state.
     *
     *@param index the state of the node.
     *@return the marginal probability associated to the state defined by index.
     */
    public float getMarginalAt(int index) {
	//by young
    	if( marginalList != null )
    		return marginalList[index];
    	return -0.0f;
    //by young end
    }

    public void setMarginalAt(int index, float value) {
    	if (marginalList == null) {
    		initMarginalList();
    	}
        marginalList[index] = value;
    }
    
    public void setMarginalProbabilities(float marginalProbabilities[]) {
    	System.arraycopy( marginalProbabilities, 0, marginalList, 0, marginalProbabilities.length);
//        for (int i = 0; i < getStatesSize(); i++) {
//            setMarginalAt(i, marginalProbabilities[i]);
//        }
    }

    /**
     * Reset the value of the evidence, which by default is -1 when there is no evidence.
     */
    public void resetEvidence() {
        evidence = -1;
        this.resetLikelihood();
    }


    /**
     * Returns true if there is evidence associated to the node, false otherwise.
     *
     * @return true if there is evidence associated to the node, false otherwise.
     */
    public boolean hasEvidence() {
        return (evidence != -1);
    }


    /**
     * Returns the index associated to the node that is the evidence of this node.
     * @return the index associated to the node that is the evidence of this node.
     */
    public int getEvidence() {
        return evidence;
    }
    
    public void resetLikelihood() {
//    	if (hasLikelihood) {
//    		evidence = -1;
//    		hasLikelihood = false;
//    	}
    	if (hasLikelihood()) {
    		evidence = -1;
    	}
    	likelihood = null;
    	if (this.getLikelihoodParents() != null) {
    		this.getLikelihoodParents().clear();
    	}
    }
    
    /**
     * @return true if there is a likelihood evidence set to this node.
     * False otherwise.
     */
    public boolean hasLikelihood() {
//    	return hasLikelihood;
    	return likelihood != null;
    }


    /**
     * Add the state associated to the given index as the evidence.
     *
     * @param stateIndex the index of the state to be set as evidence.
     */
    public void addFinding(int stateIndex) {
    	this.addFinding(stateIndex, false);
    }
    
    /**
     * Add the state associated to the given index as the evidence.
     *
     * @param stateIndex the index of the state to be set as evidence.
     * @param isNegative: if set to true, the evidence will have the meaning
     * "NOT in the state identified by index stateIndex"
     */
    public void addFinding(int stateIndex, boolean isNegative) {
//        float[] likelihood = new float[getStatesSize()];
//        likelihood[stateIndex] = 1;
//        setMarginalProbabilities(likelihood);
        evidence = stateIndex;
        for (int i = 0; i < getStatesSize(); i++) {
        	// if not isNegative, set marginal to 1 if stateindex == i; 0 otherwise.
        	// if isNegative, set marginal to 0 if stateindex == i; 1 otherwise.
			setMarginalAt(i, ((i==stateIndex)?(isNegative?0:1):(isNegative?1:0)) );
		}
    }
    
    /**
     * This is the same of {@link #addLikeliHood(float[], null)}
     * @param likelihood
     * @see #addLikeliHood(float[], List)
     */
    public void addLikeliHood(float likelihood[]) {
    	this.addLikeliHood(likelihood, null);
    }

    /**
     * Add likelihood to the variable.
     *
     * @param likelihood : the likelihood ratio.
     * @param dependencies : if the likelihood is a function of other variables, add
     * such variables to this list. A copy of the list will be stored in this {@link TreeVariable}. Hence,
     * you should access {@link #getLikelihoodParents()} to modify its content, or {@link #resetLikelihood()}
     * to clear its content.
     * @see #setLikelihoodParents(List)
     */
    public void addLikeliHood(float likelihood[], final List<INode> dependencies) {
    	this.likelihood = likelihood;
    	if (dependencies != null) {
    		this.setLikelihoodParents(new ArrayList<INode>(dependencies));
    	} else {
    		this.setLikelihoodParents(new ArrayList<INode>());
    	}
    	if (hasLikelihood()) {
    		evidence = 0;
    	}
    	
//    	hasLikelihood = true;
//    	 Does it matter which state is set as evidence?
//    	 For now we are choosing the one with the highest probability.
//    	float largestProb = likelihood[0];
////    	float sumLikelihood = 0;
//    	evidence = 0;
//        for (int i = 0; i < getStatesSize(); i++) {
//            setMarginalAt(i, likelihood[i]);
//            if (likelihood[i] > largestProb) {
//            	largestProb = likelihood[i];
//            	evidence = i;
//            }
////            sumLikelihood += likelihood[i];
//        }
//        
////        for (int i = 0; i < getStatesSize(); i++) {
////            setMarginalAt(i, likelihood[i]/largestProb);
////            setMarginalAt(i, likelihood[i]/sumLikelihood);
////        }
        
    }

    /**
     *  Returns the clique associated to this variable.
     *
     *@return the clique associated to this variable.
     */
    public IRandomVariable getAssociatedClique() {
        return this.cliqueAssociado;
    }

    /**
     *  Associate this variable to the given clique.
     *
     *@param  clique the clique to associate to this variable.
     */
    public void setAssociatedClique(IRandomVariable clique) {
        this.cliqueAssociado = clique;
    }
	
    /**
     * It currently delegates to {@link PotentialTable#updateEvidences(float[], int)}
     */
	protected void updateEvidences() {
		if (evidence != -1) {						
			PotentialTable auxTab = (PotentialTable)cliqueAssociado.getProbabilityFunction();
			int index = auxTab.indexOfVariable(this);
			// the following 2 lines were migrated to PotentialTable.updateEvidences
//			auxTab.computeFactors();
//			updateRecursive(auxTab, 0, 0, index, 0);			
			auxTab.updateEvidences(marginalList, index);
		}
	}
	
	// the following method was migrated to PotentialTable
//	private void updateRecursive(PotentialTable tab, int c, int linear, int index, int state) {
//    	if (c >= tab.variableList.size()) {
//    		tab.dataPT.data[linear] *= marginalList[state];
//    		return;    		    		
//    	}
//    	
//    	if (index == c) {
//    		for (int i = tab.variableList.get(c).getStatesSize() - 1; i >= 0; i--) {    		    		
//	    		updateRecursive(tab, c+1, linear + i*tab.factorsPT[c] , index, i);
//    		}
//    	} else {
//	    	for (int i = tab.variableList.get(c).getStatesSize() - 1; i >= 0; i--) {    		    		
//	    		updateRecursive(tab, c+1, linear + i*tab.factorsPT[c] , index, state);
//    		}
//    	}
//    }

	/**
	 * @return the likelihood of a likelihood evidence
	 */
	public float[] getLikelihood() {
		return likelihood;
	}

//	/**
//	 * This is the same of {@link #addLikeliHood(float[])}
//	 * @param likelihood the likelihood to set
//	 */
//	public void setLikelihood(float[] likelihood) {
//		this.addLikeliHood(likelihood);
//	}

	/**
	 * This list is related to {@link #getLikelihood()}. If the likelihood
	 * is conditional (i.e. if it depends to a state of other variables),
	 * then this list must store such variables, in the order compatible
	 * with {@link #getLikelihood()}.
	 * @see JunctionTreeAlgorithm#addVirtualNode(List)
	 * @see JeffreyRuleLikelihoodExtractor
	 * @see  #addLikeliHood(float[], List)
	 * @return the likelihoodParents
	 */
	public List<INode> getLikelihoodParents() {
		return likelihoodParents;
	}

	/**
	 * This list is related to {@link #getLikelihood()}. If the likelihood
	 * is conditional (i.e. if it depends to a state of other variables),
	 * then this list must store such variables, in the order compatible
	 * with {@link #getLikelihood()}.
	 * @see JunctionTreeAlgorithm#addVirtualNode(List)
	 * @see JeffreyRuleLikelihoodExtractor
	 *  @see  #addLikeliHood(float[], List)
	 * @param likelihoodParents the likelihoodParents to set. Setting this to null will call {@link List#clear()}
	 */
	protected void setLikelihoodParents(List<INode> likelihoodParents) {
		if (likelihoodParents != null) {
			this.likelihoodParents = likelihoodParents;
		} else {
			// this is trying to set to null
			if (this.likelihoodParents != null) {
				this.likelihoodParents.clear();
			} else {
				this.likelihoodParents = new ArrayList<INode>();
			} 
		}
		
	}
	
}