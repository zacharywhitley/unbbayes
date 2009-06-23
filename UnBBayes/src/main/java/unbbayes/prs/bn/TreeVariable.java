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

import unbbayes.prs.Node;

/**
 * Abstract class for variables that will be shown in the tree of nodes and states with 
 * their probabilities in the compilation panel.
 */
public abstract class TreeVariable extends Node implements java.io.Serializable {

    // Clique que a vari�vel est� associada.
    protected ITabledVariable cliqueAssociado;

    // Armazena marginais e evid�ncias.
    protected float[] marginalList;
    
    private float[] marginalCopy;

    private int evidence = -1;

    /**
     * Tem que ser sobrescrito para atualizar as marginais
     * que ser�o visualizadas na �rvore da interface.
     */
    protected abstract void marginal();
    
    public void initMarginalList() {
    	marginalList = new float[getStatesSize()];
    }
    
    void copyMarginal() {
    	int size = marginalList.length;
    	marginalCopy = new float[size];
    	System.arraycopy(marginalList, 0, marginalCopy, 0, size);
    }
    
    void restoreMarginal() {
    	int size = marginalList.length;
    	System.arraycopy(marginalCopy, 0, marginalList, 0, size);
    }
    
    /**
     *  Retorna o valor da marginal de determinado �ndice.
     *
     *@param index returna a marginal do estado especificado pelo par�metro <code>index</code>
     *@return    valor da marginal de determinado �ndice.
     */
    public float getMarginalAt(int index) {
        return marginalList[index];
    }

    void setMarginalAt(int index, float value) {
        marginalList[index] = value;
    }

    /**
     * Limpa o flag de evid�ncia.
     */
    void resetEvidence() {
        evidence = -1;
    }


    /**
     * Retorna true se esta vari�vel cont�m alguma evid�ncia e false caso contr�rio.
     *
     * @return true se esta vari�vel cont�m alguma evid�ncia e false caso contr�rio.
     */
    public boolean hasEvidence() {
        return (evidence != -1);
    }


    public int getEvidence() {
        return evidence;
    }


    /**
     * Adiciona um finding (evid�ncia) no estado especificado.
     *
     * @param stateNo �ndice do estado a ser adicionado o finding.
     */
    public void addFinding(int stateNo) {
        float[] likelihood = new float[getStatesSize()];
        evidence = stateNo;
        likelihood[stateNo] = 1;
        addLikeliHood(likelihood);
    }

    /**
     * Adicina um likelihood nesta vari�vel
     *
     * @param valores array contendo o likelihood de cada estado da vari�vel.
     */
    public void addLikeliHood(float valores[]) {
        for (int i = 0; i < getStatesSize(); i++) {
            setMarginalAt(i, valores[i]);
        }
    }

    /**
     *  Retorna o clique associado a esta variavel
     *
     *@return    clique associado
     */
    protected ITabledVariable getAssociatedClique() {
        return this.cliqueAssociado;
    }

    /**
     *  Associa esta variavel ao clique do parametro.
     *
     *@param  clique  clique associado a esta variavel.
     */
    protected void setAssociatedClique(ITabledVariable clique) {
        this.cliqueAssociado = clique;
    }
	

	protected void updateEvidences() {
		if (evidence != -1) {						
			PotentialTable auxTab = cliqueAssociado.getPotentialTable();
			int index = auxTab.indexOfVariable(this);
			auxTab.computeFactors();
			updateRecursive(auxTab, 0, 0, index, 0);			
		}
	}
	
	private void updateRecursive(PotentialTable tab, int c, int linear, int index, int state) {
    	if (c >= tab.variableList.size()) {
    		tab.dataPT.data[linear] *= marginalList[state];
    		return;    		    		
    	}
    	
    	if (index == c) {
    		for (int i = tab.variableList.get(c).getStatesSize() - 1; i >= 0; i--) {    		    		
	    		updateRecursive(tab, c+1, linear + i*tab.factorsPT[c] , index, i);
    		}
    	} else {
	    	for (int i = tab.variableList.get(c).getStatesSize() - 1; i >= 0; i--) {    		    		
	    		updateRecursive(tab, c+1, linear + i*tab.factorsPT[c] , index, state);
    		}
    	}
    }
	
}