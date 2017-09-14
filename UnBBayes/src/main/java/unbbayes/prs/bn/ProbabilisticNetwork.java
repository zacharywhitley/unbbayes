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
import java.util.Collections;
import java.util.List;

import unbbayes.prs.INode;
import unbbayes.prs.Node;
import unbbayes.util.SetToolkit;

/**
 *  It represents a probabilistic network
 *
 *@author     michael
 *@author     rommel
 */
public class ProbabilisticNetwork
	extends SingleEntityNetwork
	implements java.io.Serializable {

	  /** Serialization runtime version number */
	  private static final long serialVersionUID = 0;
	
	private IJunctionTreeBuilder junctionTreeBuilder = new DefaultJunctionTreeBuilder();
	
	/**
	 * Creates a new probabilistic network. Clears log file and initializes the vector of node elimination.
	 * @see #getNodeEliminationOrder()
	 */
	public ProbabilisticNetwork(String id) {
		super(id);							
		nodeEliminationOrder = new ArrayList<Node>();
		firstInitialization = true;
	}
	

	/**
	 *  Do the triangularization process
	 *  @deprecated use {@link JunctionTreeAlgorithm#triangulate(ProbabilisticNetwork)}
	 */
	private void triangula() {		
		Node aux;
		ArrayList<Node> auxNos;

		if (createLog) {
			logManager.append(resource.getString("triangulateLabel"));
		}
		auxNos = SetToolkit.clone(nodeList);
		removeUtilityNodes(auxNos);
		copiaNos = SetToolkit.clone(auxNos);
		int sizeDecisao = decisionNodes.size();
		for (int i = 0; i < sizeDecisao; i++) {
			aux = decisionNodes.get(i);
			auxNos.remove(aux);
			auxNos.removeAll(aux.getParents());
		}

		nodeEliminationOrder = new ArrayList<Node>(copiaNos.size());

		while (minimumWeightElimination(auxNos))
			;

		//        int index;
		for (int i = decisionNodes.size() - 1; i >= 0; i--) {
			aux = decisionNodes.get(i);
			nodeEliminationOrder.add(aux);
			int sizeAdjacentes = aux.getAdjacents().size();
			for (int j = 0; j < sizeAdjacentes; j++) {
				Node v = aux.getAdjacents().get(j);
				v.getAdjacents().remove(aux);
			}
			if (createLog) {
				logManager.append(
					"\t" + nodeEliminationOrder.size() + " " + aux.getName() + "\n");
			}

			auxNos = SetToolkit.clone(aux.getParents());
			auxNos.removeAll(decisionNodes);
			auxNos.removeAll(nodeEliminationOrder);
			for (int j = 0; j < i; j++) {
				Node decision = decisionNodes.get(j);
				auxNos.removeAll(decision.getParents());
			}

			while (minimumWeightElimination(auxNos)) 
				;
		}
		
		makeAdjacents();
	}

	private void removeUtilityNodes(ArrayList<Node> nodes) {
		for (int i = nodes.size() - 1; i >= 0; i--) {
			if (nodes.get(i).getType() == Node.UTILITY_NODE_TYPE) {
				nodes.remove(i);
			}
		}
	}


	/**
	 * It does all the steps to compile a network using junction tree method.
	 * The steps are: 
	 * 1. consistency check; 
	 * 2. Moralization; 
	 * 3. Triangulation; 
	 * 4. Compilation of Junction Tree
	 * 
	 * @deprecated use {@link JunctionTreeAlgorithm#run()}
	 */
	public void compile() throws Exception {
		// TODO remove double dispatch (JunctionTreeAlgorithm is calling this method) by migrating this code to JunctionTreeAlgorithm class
		if (nodeList.size() == 0) {
			throw new Exception(resource.getString("EmptyNetException"));
		}
		if (createLog) {
			logManager.reset();
		}
		verifyConsistency();
		moralize();
		triangula();		
		
		if (getJunctionTreeBuilder() == null) {
			// initialize it if not initialized yet
			setJunctionTreeBuilder(new DefaultJunctionTreeBuilder());
		}
		
		compileJT(this.getJunctionTreeBuilder().buildJunctionTree(this));
	}


	/**
	 * This is used in {@link #compile()} to generate instances of {@link JunctionTree}
	 * @return the junctionTreeBuilder
	 */
	public IJunctionTreeBuilder getJunctionTreeBuilder() {
		return junctionTreeBuilder;
	}


	/**
	 * This is used in {@link #compile()} to generate instances of {@link JunctionTree}
	 * @param junctionTreeBuilder the junctionTreeBuilder to set
	 */
	public void setJunctionTreeBuilder(IJunctionTreeBuilder junctionTreeBuilder) {
		this.junctionTreeBuilder = junctionTreeBuilder;
	}


	/* (non-Javadoc)
	 * @see unbbayes.prs.Network#removeNode(unbbayes.prs.Node)
	 */
	public void removeNode(Node nodeToRemove) {
		this.removeNode(nodeToRemove, true);
	}
	
	/**
	 * Removes a node from network.
	 * @param nodeToRemove
	 * @param isToDeleteCliqueAndSeparator : if true, it will remove the node from cliques and separators too
	 */
	public void removeNode(Node nodeToRemove, boolean isToDeleteCliqueAndSeparator) {
		/*
		 * NOTE: this method assumes that the junction tree algorithm is implemented in a way
		 * which it ignores separators containing 0 nodes (i.e. the empty separator still represents
		 * a link between cliques, but such link is used only for accessing cliques in a 
		 * hierarchic ordering, and it is not supposed to propagate evidences - e.g. absorb will do nothing).
		 */
		if (isToDeleteCliqueAndSeparator && !this.isID() && !this.isHybridBN()) {
			// attempt to remove probabilistic nodes from junction tree as well
			// this only makes sense if we are attempting to remove nodes from a compiled BN
			// (nonsense if you are deleting nodes in edit mode)
			if (getJunctionTree() != null) {
				// remove variable from separators
				if (getJunctionTree().getSeparators() != null) {
					for (Separator separator : getJunctionTree().getSeparatorsContainingAllNodes(Collections.singletonList((INode)nodeToRemove), Integer.MAX_VALUE)) {
						if (separator.getNodes().contains(nodeToRemove)) {
							PotentialTable sepTable = separator.getProbabilityFunction();
							sepTable.purgeVariable(nodeToRemove, false);
							sepTable.normalize();
							separator.getNodes().remove(nodeToRemove);
						}
					}
				}
				// remove variable from cliques
				if (getJunctionTree().getCliques() != null) {
					for (Clique clique : getJunctionTree().getCliquesContainingAllNodes(Collections.singletonList((INode)nodeToRemove), Integer.MAX_VALUE)) {
						PotentialTable cliqueTable = clique.getProbabilityFunction();
						cliqueTable.purgeVariable(nodeToRemove, false);
						cliqueTable.normalize();
						clique.getAssociatedProbabilisticNodesList().remove(nodeToRemove);
						clique.getNodesList().remove(nodeToRemove);
					}
				}
			}
		}
		if (getNodesCopy() != null) {
			getNodesCopy().remove(nodeToRemove);
		}
		super.removeNode(nodeToRemove);
	}

}