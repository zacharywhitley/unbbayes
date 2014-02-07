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

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import unbbayes.prs.INode;
import unbbayes.prs.Node;
import unbbayes.prs.bn.cpt.IProbabilityFunctionAdapter;
import unbbayes.util.Debug;
import unbbayes.util.SetToolkit;

/**
 * Represents probabilistic variable.
 * 
 * @author Michael Onishi
 * @author Rommel Carvalho
 */
public class ProbabilisticNode extends TreeVariable implements IRandomVariable, IProbabilityFunctionAdapter,
		java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8362313890037632119L;

	private ProbabilisticTable tabelaPot;

	private static Color descriptionColor = Color.yellow;

	private static Color explanationColor = Color.green;

	/** Load resource file from this package */
	private static ResourceBundle resource = unbbayes.util.ResourceController
			.newInstance().getBundle(
					unbbayes.prs.bn.resources.BnResources.class.getName());

	private int internalIdentificator = Integer.MIN_VALUE;

	/**
	 * Constructs a ProbabilisticNode with an initialized table and an
	 * incremented DrawElement.
	 */
	public ProbabilisticNode() {
		tabelaPot = new ProbabilisticTable();
		// by young
		setColor(descriptionColor);
	}

	public int getType() {
		return PROBABILISTIC_NODE_TYPE;
	}

	/**
	 * Copies the main features to a new node.
	 * 
	 * @param radius
	 *            : the radius of the node. This is used in order to calculate
	 *            the new position of the new node, so that it does not overlap
	 *            the old node's postition.
	 * @return copy of the node
	 */
	public ProbabilisticNode clone(double radius) {
		// TODO Check whether we can eliminate radius
		ProbabilisticNode no = new ProbabilisticNode();

		for (int i = 0; i < getStatesSize(); i++) {
			no.appendState(getStateAt(i));
		}
		ProbabilisticNode.setDescriptionColor(ProbabilisticNode
				.getDescriptionColor().getRGB());
		ProbabilisticNode.setExplanationColor(ProbabilisticNode
				.getExplanationColor().getRGB());
		no.setPosition(this.getPosition().getX() + 1.3 * radius, this
				.getPosition().getY() + 1.3 * radius);
		no.setName(resource.getString("copyName") + this.getName());
		no.setDescription(resource.getString("copyName")
				+ this.getDescription());
		no.tabelaPot = (ProbabilisticTable) this.tabelaPot.clone();
		return no;
	}

	public Object clone() {
		ProbabilisticNode cloned = new ProbabilisticNode();
		cloned.tabelaPot = (ProbabilisticTable) this.tabelaPot.clone();
		ProbabilisticNode.setDescriptionColor(ProbabilisticNode
				.getDescriptionColor().getRGB());
		ProbabilisticNode.setExplanationColor(ProbabilisticNode
				.getExplanationColor().getRGB());
		cloned.setDescription(this.getDescription());
		cloned.setName(this.getName());
		cloned.setPosition(this.getPosition().getX(), this.getPosition().getY());
		cloned.setParents(SetToolkit.clone(parents));
		cloned.setChildren(SetToolkit.clone(this.getChildren()));
		cloned.setStates(SetToolkit.clone(states));
		cloned.setAdjacents(SetToolkit.clone(this.getAdjacents()));
		cloned.setSelected(this.isSelected());
		cloned.setExplanationDescription(this.getExplanationDescription());
		cloned.setPhrasesMap(this.getPhrasesMap());
		cloned.setInformationType(this.getInformationType());

		if (isMarginalList()) {
			float[] marginais = new float[super.marginalList.length];
			System.arraycopy(super.marginalList, 0, marginais, 0,
					marginais.length);
			cloned.marginalList = marginais;
			cloned.setInternalIdentificator(this.getInternalIdentificator());
		}

		// cloned.copyMarginal();
		return cloned;
	}

	/**
	 * Performs a clone which copies only basic attributes related to itself
	 * only (i.e. it does not copy CPT, parents, children, adjacents, and
	 * position)
	 * 
	 * @return a new instance
	 */
	public ProbabilisticNode basicClone() {
		ProbabilisticNode cloned = new ProbabilisticNode();
		cloned.setDescription(this.getDescription());
		cloned.setName(this.getName());
		// cloned.setPosition(this.getPosition().getX(),
		// this.getPosition().getY());
		cloned.setStates(SetToolkit.clone(states));
		if (super.marginalList != null) {
			float[] marginais = new float[super.marginalList.length];
			System.arraycopy(super.marginalList, 0, marginais, 0,
					marginais.length);
			cloned.marginalList = marginais;
			cloned.copyMarginal();
		}
		if (this.hasEvidence()) {
			cloned.addFinding(this.getEvidence());
		}
		cloned.setInternalIdentificator(this.getInternalIdentificator());
		// TODO copy likelihood
		return cloned;
	}

	/**
	 * Returns the probabilistic table of this variable.
	 * 
	 * @return the CPT (potential table)
	 */
	public PotentialTable getProbabilityFunction() {
		return tabelaPot;
	}

	/**
	 * Calculates the margin of this node.
	 */
	protected void marginal() {
		initMarginalList();
		if (cliqueAssociado == null) {
			try {
				Debug.println(
						getClass(),
						"Attempted to calculate marginal probability of node "
								+ this
								+ " from a junction tree, but junction tree was not properly initialized.");
			} catch (Throwable t) {
				t.printStackTrace();
			}
			return;
		}
		PotentialTable auxTab = (PotentialTable) ((PotentialTable) cliqueAssociado
				.getProbabilityFunction()).getTemporaryClone();
		int index = auxTab.indexOfVariable(this);
		int size = cliqueAssociado.getProbabilityFunction().variableCount();
		for (int i = 0; i < size; i++) {
			if (i != index) {
				auxTab.removeVariable(cliqueAssociado.getProbabilityFunction()
						.getVariableAt(i));
			}
		}

		int tableSize = auxTab.tableSize();
		if (marginalList.length <= 0 && tableSize > 0) {
			// TODO use resource files instead
			throw new IllegalStateException(
					"Inconsistent quantity of marginal states. This may be caused by a node which cannot be handled by the selected algorithm.");
		} else {
			for (int i = 0; i < tableSize; i++) {
				marginalList[i] = auxTab.getValue(i);
			}
		}
	}

	/**
	 * Inserts a new state and updates the affected tables. Overrides Node's
	 * superclass method.
	 * 
	 * @param state
	 *            : a new state to be added.
	 */
	public void appendState(String state) {
		updateState(state, true);
	}

	// by young2010
	/**
	 * Remove all, but one, states by removing the last state until there is
	 * just one left.
	 */
	public void removeAllStates() {
		while (states.size() > 1) {
			removeLastState();
		}
	}
	
	/**
	 * Removes the newest state and updates the affected tables. Overwrites a
	 * Node's superclass method.
	 */
	public void removeLastState() {
		if (states.size() > 1) {
			// super.removeLastState();
			updateState(null, false);
		}
	}

	/**
	 * This method can be used to update the affected tables when inserting and
	 * removing new states.
	 * 
	 * @param state
	 *            state to be inserted / removed.
	 * @param isInsertion
	 *            true for insertion and false for remotion.
	 */
	private void updateState(String state, boolean isInsertion) {
		int d = getStatesSize();
		if (d > 0) {
			while (d <= tabelaPot.tableSize()) {
				if (isInsertion) {
					tabelaPot.addValueAt(d++, 0);
				} else {
					tabelaPot.removeValueAt(--d);
				}
				d += getStatesSize();
			}
		} else if (isInsertion && d == 0) {
			// special case: the table started from 0 elements and we are adding a new state to node
			tabelaPot.addValueAt(d++, 0);
		}

		List<Node> clones[] = new ArrayList[getChildren().size()];
		int indexes[] = new int[getChildren().size()];
		for (int i = 0; i < getChildren().size(); i++) {
			if (getChildren().get(i).getType() == Node.DECISION_NODE_TYPE
					|| getChildren().get(i).getType() == Node.CONTINUOUS_NODE_TYPE) {
				continue;
			}

			IRandomVariable randomVar = (IRandomVariable) getChildren().get(i);
			PotentialTable auxTab = (PotentialTable) randomVar.getProbabilityFunction();

			clones[i] = auxTab.cloneVariables();
			indexes[i] = auxTab.indexOfVariable(this);
		}

		for (int i = 0; i < getChildren().size(); i++) {
			if (getChildren().get(i).getType() == Node.DECISION_NODE_TYPE
					|| getChildren().get(i).getType() == Node.CONTINUOUS_NODE_TYPE) {
				continue;
			}
			PotentialTable auxTab = (PotentialTable) ((IRandomVariable) getChildren().get(i)).getProbabilityFunction();
			int l = indexes[i];
			List<Node> auxList = clones[i];
			for (int k = auxList.size() - 1; k >= l; k--) {
				// TODO fix the marginalization which is happening here
				// ISumOperation operationBkp = null;
				// if (isInsertion) {
				// if (auxTab instanceof ProbabilisticTable) {
				// // backup old marginalization operation and overwrite with
				// something different
				// ProbabilisticTable pt = (ProbabilisticTable) auxTab;
				// operationBkp = pt.getSumOperation();
				// pt.setSumOperation(new ISumOperation() {
				// public float operate(float arg1, float arg2) {
				// // TODO Auto-generated method stub
				// return 0;
				// }
				// });
				// }
				// // remove var, so that it is added again after the state is
				// updated
				// auxTab.removeVariable(auxList.get(k));
				// //re-enable marginalization of
				// auxTab.removeVariable(auxList.get(k));
				// if (isInsertion) {
				// if (auxTab instanceof ProbabilisticTable) {
				// // restore backed up operation
				// ProbabilisticTable pt = (ProbabilisticTable) auxTab;
				// pt.setSumOperation(operationBkp);
				// }
				// }
				// } else {
				// remove var, so that it is added again after the state is
				// updated
				Node node = auxList.get(k);
				auxTab.removeVariable(node,true);
				// }
			}
		}

		if (isInsertion) {
			super.appendState(state);
		} else {
			super.removeLastState();
		}

		for (int i = 0; i < getChildren().size(); i++) {
			if (getChildren().get(i).getType() == Node.DECISION_NODE_TYPE
					|| getChildren().get(i).getType() == Node.CONTINUOUS_NODE_TYPE) {
				continue;
			}
			IProbabilityFunction auxTab = ((IRandomVariable) getChildren().get(i)).getProbabilityFunction();
			int l = indexes[i];
			List<Node> auxList = clones[i];
			for (int k = l; k < auxList.size(); k++) {
				auxTab.addVariable(auxList.get(k));
			}
		}
	}

	/**
	 * Returns node's color.
	 * 
	 * @return color of the probabilistic node.
	 */
	public static Color getDescriptionColor() {
		return descriptionColor;
	}

	/**
	 * Changes the description node's color.
	 * 
	 * @param c
	 *            RGB value of the new color.
	 */
	public static void setDescriptionColor(int c) {
		descriptionColor = new Color(c);
	}

	/**
	 * Changes the explanation's node's color.
	 * 
	 * @param c
	 *            RGB value of the new color.
	 */
	public static void setExplanationColor(int c) {
		explanationColor = new Color(c);
	}

	/**
	 * Gets the explanationColor.
	 * 
	 * @return Returns a Color
	 */
	public static Color getExplanationColor() {
		return explanationColor;
	}

	/**
	 * @return the internalIdentificator
	 */
	public int getInternalIdentificator() {
		return internalIdentificator;
	}

	/**
	 * @param internalIdentificator
	 *            the internalIdentificator to set
	 */
	public void setInternalIdentificator(int internalIdentificator) {
		this.internalIdentificator = internalIdentificator;
	}

	/**
	 * Removes literally all states.
	 * This is different from {@link #removeAllStates()}, because {@link #removeAllStates()} keeps at least 1 state active.
	 * @see unbbayes.prs.Node#removeStates()
	 */
	public void removeStates() {
		while (states.size() > 0) {
			removeStateAt(0);
		}
	}

	/**
	 * This will remove all states that are at and after the given index.
	 * TODO implement a version that removes 1 state, shifts posterior states, and
	 * still keeps CPT consistent.
	 * @see unbbayes.prs.Node#removeStateAt(int)
	 * @deprecated use {@link #removeLastState()} or {@link #removeStates()}
	 */
	public void removeStateAt(int index) {
		while (getStatesSize() > index) {
			updateState(null, false);
			states.remove(index);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.cpt.IProbabilityFunctionAdapter#loadProbabilityFunction(unbbayes.prs.bn.IProbabilityFunction)
	 */
	public void loadProbabilityFunction(IProbabilityFunction probabilityFunction) {
		if (probabilityFunction instanceof PotentialTable) {
			// use a clone, because we are going to use cpt.removeVariable(variable) to marginalize out
			PotentialTable cpt = ((PotentialTable) probabilityFunction).getTemporaryClone();	// cpt to read
			// the cpt to be overwritten
			PotentialTable myCPT = this.getProbabilityFunction();
			
			// the table must specify the conditional probability of the same variable
			if (this.equals(cpt.getVariableAt(0))) {	// the 1st variable of CPT indicates who the CPT specifies
				// check which variables to marginalize out: they are the ones present in my table, but not in 
				for (int i = 0; i < cpt.getVariablesSize(); i++) {
					Node var = (Node) cpt.getVariableAt(i);
					if (this.getProbabilityFunction().getVariableIndex(var) < 0) {
						// not found. Remove and marginalize
						cpt.removeVariable(var, true);
						i--;
					}
				}
				
				
				// if I can do a quick copy, just do it
				if (cpt.tableSize() == myCPT.tableSize()) {
					// fast copy of values
					myCPT.setValues(cpt.getValues());
				} else {
					// reset content of my cpt to 1 (identity number in multiplication), and then do a matrix multiplication
					for (int i = 0; i < myCPT.tableSize(); i++) {
						myCPT.setValue(i, 1f);
					}
					myCPT.opTab(cpt, PotentialTable.PRODUCT_OPERATOR);
				}
				
			} else {
				// truncate the values of the argument (so that it fits to myCPT.tableSize())
				System.arraycopy(cpt.getValues(), 0, myCPT.dataPT.data, 0, myCPT.tableSize());
			}
			
		}
	}

}
