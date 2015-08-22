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

import unbbayes.prs.INode;
import unbbayes.prs.Node;
import unbbayes.prs.id.DecisionNode;
import unbbayes.util.FloatCollection;

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
  	
  	// TODO use multipleton instead of singleton, if performance is not a problem
  	private static PotentialTable temporarySingletonClone1 = null;
  	private static PotentialTable temporarySingletonClone2 = null;
  	/** if true, will use {@link #temporarySingletonClone1}. If false, will use {@link #temporarySingletonClone2} */
  	private static boolean temporarySingletonCloneSwitch = true;	

	private static boolean isToUseSingletonInstanceAsTemporaryClone = true;

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
		Node currentNode = variableList.get(0);
		int numerOfLinesInCPT = currentNode.getStatesSize();

		/* Check if the node represents a numeric attribute */
		if (numerOfLinesInCPT == 0) {
			/* 
			 * The node represents a numeric attribute which has no potential
			 * table. Just Return.
			 */
			return;
		}
		
		int numberOfColumnsInCPT = 1;
		int numberOfVariables = variableList.size();
		for (int k = 1; k < numberOfVariables; k++) {
			currentNode = variableList.get(k);
			numberOfColumnsInCPT *= currentNode.getStatesSize();
		}

		float sum;
		for (int j = 0; j < numberOfColumnsInCPT; j++) {
			sum = 0f;
			for (int i = 0; i < numerOfLinesInCPT; i++) {
				sum += dataPT.data[j * numerOfLinesInCPT + i] * 100;
			}
			if (Math.abs(sum - 100.0) > 0.01) {
				throw new Exception(resource.getString("variableTableName") + variableList.get(0) + resource.getString("inconsistencyName") + sum + "%\n");
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
	
	/**
	 * CAUTION: this method is over fit to {@link unbbayes.prs.bn.JunctionTree#absorb(Clique, Clique)}
	 * in order to reduce the amount of garbage.
	 * Returns a copy of the data from the table,
	 * but some optimizations may be performed by implementations (subclasses), assuming that
	 * the copy will only be alive temporary.
	 * @return A copy of the data from the table.
	 * CAUTION: by default, it will return a singleton instance if
	 * {@link #isToUseSingletonInstanceAsTemporaryClone()} is true.
	 * In the default case, this method is not thread safe (except of singleton instantiation).
	 * @see unbbayes.prs.bn.PotentialTable#getTemporaryClone()
	 * @see unbbayes.prs.bn.JunctionTree#absorb(Clique, Clique)
	 */
	public PotentialTable getTemporaryClone() {
		if (isToUseSingletonInstanceAsTemporaryClone) {
			// use lazily instantiated singleton object
			
			PotentialTable auxTab;
			
			// TODO stop leaving it overly fit to {@link unbbayes.prs.bn.JunctionTree#absorb(Clique, Clique)}.
			// 2 consecutive calls will not return the same object, because {@link unbbayes.prs.bn.JunctionTree#absorb(Clique, Clique)} makes 2 calls assuming they are different objects
			if (temporarySingletonCloneSwitch) {
				if (temporarySingletonClone1 == null) {
					// instantiate new object
					synchronized(PotentialTable.class) {	// double checking avoids race condition
						if (temporarySingletonClone1 == null) {
							temporarySingletonClone1 = newInstance();
						}
					}
				}
				auxTab = temporarySingletonClone1;
			} else {
				if (temporarySingletonClone2 == null) {
					// instantiate new object
					synchronized(PotentialTable.class) {	// double checking avoids race condition
						if (temporarySingletonClone2 == null) {
							temporarySingletonClone2 = newInstance();
						}
					}
				}
				auxTab = temporarySingletonClone2;
			}
			// swap switch at each call
			temporarySingletonCloneSwitch = !temporarySingletonCloneSwitch;
			
			// TODO see how much the synchronization methods would reduce performance. If small, then use synchronization
			
			
			// TODO check which is faster - just do clear + addAll or to use set(index,value) if size is OK
			auxTab.variableList.clear();
			auxTab.variableList.addAll(variableList);
			
			// perform fast  copy of content
			auxTab.dataPT.size = dataPT.size;
			if (auxTab.dataPT.data.length < dataPT.size) {
				// guarantee the size matches
				auxTab.dataPT.data = new float[dataPT.size];
			}
			System.arraycopy(dataPT.data, 0, auxTab.dataPT.data, 0, dataPT.size);
			
			// also copy factors (which is used to calculate indexes when summing out variables)
			if (factorsPT != null) {
				if (auxTab.factorsPT == null || auxTab.factorsPT.length < factorsPT.length) {
					// guarantee the size matches
					auxTab.factorsPT = new int[factorsPT.length];
				}
				System.arraycopy(factorsPT, 0, auxTab.factorsPT, 0, factorsPT.length);
			}
			
			// indicate that the singleton table was modified.
			auxTab.notifyModification();
			
			auxTab.setSumOperation(this.getSumOperation());
			
			
			return auxTab;
		} else {
			// TODO return from some kind of object pool
			return (PotentialTable) this.clone();
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return variableList+"@"+ super.toString();
	}

	/**
	 * This method just calls {@link #removeVariable(INode, boolean)},
	 * and then reallocates {@link FloatCollection#size} of space for {@link FloatCollection#data},
	 * because {@link #removeVariable(INode, boolean)} only changes the value of {@link FloatCollection#size}
	 * without reallocating.
	 * After that, this method expects the garbage collector to actually free the memory.
	 * @see unbbayes.prs.bn.PotentialTable#purgeVariable(unbbayes.prs.INode, boolean)
	 */
	public void purgeVariable(INode variable, boolean normalize) {
		this.removeVariable(variable, normalize);
		// reallocate with length = dataPT.size
		float[] aux = dataPT.data;
		dataPT.data = new float[dataPT.size];
		System.arraycopy(aux, 0, dataPT.data, 0, dataPT.size);
	}

	/**
	 * If true, {@link #getTemporaryClone()} will return singleton instances.
	 * This is true by default.
	 * @return the isToUseSingletonInstanceAsTemporaryClone
	 */
	public static boolean isToUseSingletonInstanceAsTemporaryClone() {
		return isToUseSingletonInstanceAsTemporaryClone;
	}

	/**
	 * If true, {@link #getTemporaryClone()} will return singleton instances
	 * This is true by default.
	 * @param isToUseSingletonInstanceAsTemporaryClone the isToUseSingletonInstanceAsTemporaryClone to set
	 */
	public static void setToUseSingletonInstanceAsTemporaryClone(
			boolean isToUseSingletonInstanceAsTemporaryClone) {
		ProbabilisticTable.isToUseSingletonInstanceAsTemporaryClone = isToUseSingletonInstanceAsTemporaryClone;
	}
	
	
}