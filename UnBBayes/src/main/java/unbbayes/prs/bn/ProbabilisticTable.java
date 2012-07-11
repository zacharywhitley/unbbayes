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

import java.util.ResourceBundle;

import unbbayes.prs.Node;
import unbbayes.prs.id.DecisionNode;

/**
 * Probabilistic Potential Table
 * @author Michael
 */
public class ProbabilisticTable extends PotentialTable implements java.io.Serializable {

	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;	
	
	/** Load resource file from this package */
  	private static ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
  			unbbayes.prs.bn.resources.BnResources.class.getName());

	public ProbabilisticTable() {
	}

	/**
	 *  Remove variable from table. This method can also be used for general marginalization.
	 *
	 *@param  variavel : variable to be removed from table.
	 */
	public void removeVariable(unbbayes.prs.INode variavel) {
		computeFactors();
		int index = variableList.indexOf(variavel);
		if (variavel.getType() == Node.DECISION_NODE_TYPE) {
			DecisionNode decision = (DecisionNode) variavel;
			int statesSize = variavel.getStatesSize();
			if (decision.hasEvidence()) {
				finding(variableList.size()-1, index, new int[variableList.size()], decision.getEvidence());
			} else {
//				sum(variaveis.size()-1, index, 0, 0);
				sum(index);
				for (int i = dataPT.size-1; i >= 0; i--) {
					dataPT.data[i] = dataPT.data[i] / statesSize;
				}
			}
		} else {
//		  sum(variaveis.size()-1, index, 0, 0);
		  sum(index);
		}
		notifyModification();
		variableList.remove(index);
	}

	/**
	 * Remove the variable of the table. 
	 * 
	 * Note: 
	 * Substitute the previous method removeVariable(Node variable)
	 *
	 * @param variable  Variable to be removed
	 * @param normalize True if is to normalize the cpt after the node remotion
	 */	
	public void removeVariable(unbbayes.prs.INode variable, boolean normalize){
		int index = variableList.indexOf(variable);
		if (index < 0) {
			// variable not found. Ignore it.
			return;
		}
		computeFactors();
		
		if (variable.getType() == Node.DECISION_NODE_TYPE) {
			DecisionNode decision = (DecisionNode) variable;
			int statesSize = variable.getStatesSize();
			if (decision.hasEvidence()) {
				finding(variableList.size()-1, index, new int[variableList.size()], decision.getEvidence());
			} else {
				sum(index);
				if(normalize){
					for (int i = dataPT.size-1; i >= 0; i--) {
						dataPT.data[i] = dataPT.data[i] / statesSize;
					}
				}
			}
		} else if (variableList.size() <= 1) {
			// we are removing the only probabilistic node in this potential table, so we need neither to sum-out nor to normalize.
			dataPT.size = 0;
		} else {
			sum(index);
			if(normalize){
				int statesSize = variable.getStatesSize();
				for (int i = dataPT.size-1; i >= 0; i--) {
					dataPT.data[i] = dataPT.data[i] / statesSize;
				}
			}
		}
		notifyModification();
		variableList.remove(index);
	}
	
	/**
	 *  Check the consistency of the property of this table.
	 *
	 * @throws Exception if table sums up to 100% for the states
	 *				   given parent's states.
	 */
	public void verifyConsistency() throws Exception {
		Node auxNo = variableList.get(0);
		int noLin = auxNo.getStatesSize();

		/* Check if the node represents a numeric attribute */
		if (noLin == 0) {
			/* 
			 * The node represents a numeric attribute which has no potential
			 * table. Just Return.
			 */
			return;
		}
		
		int noCol = 1;
		int sizeVariaveis = variableList.size();
		for (int k = 1; k < sizeVariaveis; k++) {
			auxNo = variableList.get(k);
			noCol *= auxNo.getStatesSize();
		}

		float soma;
		for (int j = 0; j < noCol; j++) {
			soma = 0;
			for (int i = 0; i < noLin; i++) {
				soma += dataPT.data[j * noLin + i] * 100;
			}

			if (Math.abs(soma - 100.0) > 0.01) {
				throw new Exception(resource.getString("variableTableName") + variableList.get(0) + resource.getString("inconsistencyName") + soma + "%\n");
			}
		}
	}
	

	/**
	 * Returns a new instance of a ProbabilisticTable. Implements the abstract method from PotentialTable.
	 * @return a new instance of a ProbabilisticTable.
	 */
	public PotentialTable newInstance() {
		return new ProbabilisticTable();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return variableList+"@"+ super.toString();
	}
	
	
}