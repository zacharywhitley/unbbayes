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
package unbbayes.prs.id;

import java.awt.Color;
import java.util.ResourceBundle;

import unbbayes.prs.Node;
import unbbayes.prs.bn.IProbabilityFunction;
import unbbayes.prs.bn.IRandomVariable;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.ProbabilisticTable;
import unbbayes.prs.bn.cpt.IProbabilityFunctionAdapter;

/**
 *  This class represents the utility node.
 *
 *@author Michael Onishi 
 *@author Rommel Carvalho
 */
public class UtilityNode extends Node implements IRandomVariable, IProbabilityFunctionAdapter, java.io.Serializable {

	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;
	
    private PotentialTable utilTable;

    private static Color color = Color.cyan;
    
    /** Load resource file from this package */
  	private static ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
  			unbbayes.prs.bn.resources.BnResources.class.getName());
  	
  	private int internalIdentificator = Integer.MIN_VALUE;

  	/**
     * Constructs a UtilityNode with an initialized table and 
     * an incremented DrawElement.
     */
    public UtilityNode() {
    	
	  	//by young
		setColor(Color.cyan);
		
        utilTable = new UtilityTable();
        states.add(resource.getString("utilityName"));
    }
    
    
    public int getType() {
    	return UTILITY_NODE_TYPE;    	
    }

    /**
     * N�o faz nada ao se tentar inserir um estado, pois
     * vari�veis de utilidade s� aceitam 1 estado.
     */
    public void appendState(String state) { 
    	if (getStatesSize() < 1) {
    		super.appendState(state);
    	}
    }

    /**
     * N�o faz nada ao se tentar inserir um estado, pois
     * vari�veis de utilidade s� aceitam 1 estado.
     */
    public void removeLastState() { }

    /**
     *  Gets the tabelaPot attribute of the TVU object
     *
     *@return    The tabelaPot value
     */
    public PotentialTable getProbabilityFunction() {
        return this.utilTable;
    }

    
    /**
     *  Get the node's color.
     *	@return The node's color.
     */
    //by young
     
    public static Color getStaticColor() {
        return color;
    } 
    
    /**
     *  Set the node's color.
     *
     *@param rgb The node's RGB color.
     */
    //by young
     
    public static void setStaticColor(int rgb) {
        color = new Color(rgb);
    }


	/**
	 * @return the internalIdentificator
	 */
	public int getInternalIdentificator() {
		return internalIdentificator;
	}


	/**
	 * @param internalIdentificator the internalIdentificator to set
	 */
	public void setInternalIdentificator(int internalIdentificator) {
		this.internalIdentificator = internalIdentificator;
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.cpt.IProbabilityFunctionAdapter#loadProbabilityFunction(unbbayes.prs.bn.IProbabilityFunction)
	 */
	public void loadProbabilityFunction(IProbabilityFunction probabilityFunction) {
		if (probabilityFunction instanceof UtilityTable) {
			// use a clone, because we are going to use cpt.removeVariable(variable) to marginalize out
			PotentialTable util = ((UtilityTable) probabilityFunction).getTemporaryClone();	// cpt to read
			// the cpt to be overwritten
			PotentialTable myTable = this.getProbabilityFunction();
			
			// copy the content of util to a temporary vector, but trim size to myTable.tableSize()
			float[] temp = new float[myTable.tableSize()];
			System.arraycopy(util.getValues(), 0, temp, 0, myTable.tableSize());
			
			// overwrite my table
			myTable.setValues(temp);	
		}
	}


	@Override
	public UtilityNode getClone() {
		UtilityNode clone = new UtilityNode();
		super.clone(clone);
		clone.loadProbabilityFunction(this.getProbabilityFunction());
//		clone.tabelaPot = (ProbabilisticTable) this.tabelaPot.clone();
		return clone;
	}
    
}