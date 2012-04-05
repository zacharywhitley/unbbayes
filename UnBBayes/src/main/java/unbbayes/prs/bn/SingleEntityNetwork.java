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
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import unbbayes.gui.HierarchicTree;
import unbbayes.io.NetworkCompilationLogManager;
import unbbayes.io.log.TextLogManager;
import unbbayes.prs.Edge;
import unbbayes.prs.Network;
import unbbayes.prs.Node;
import unbbayes.prs.id.DecisionNode;
import unbbayes.util.SetToolkit;

/**
 *  Class that represents a generic network.
 *
 *@author Rommel Carvalho
 *@author Michael Onishi
 *@version 2006/09/11
 */
public class SingleEntityNetwork extends Network implements java.io.Serializable {

	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;	
	
	/** Load resource file from this package */
  	protected static ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
  			unbbayes.prs.bn.resources.BnResources.class.getName());
  	
  	protected HierarchicTree hierarchicTree;
  	
  	protected boolean firstInitialization;
  	
  	/**
  	 * Decision nodes used during the transformation process
	 */
	protected ArrayList<Node> decisionNodes;
	
	protected double radius;

    /**
     * Processes compilation log
	 */
	protected NetworkCompilationLogManager logManager;

	/**
	 * A list of edges used during transformation process
	 */
	protected List<Edge> arcosMarkov;
	
	/**
	 * Indicates whether log should be created or not
	 */
	protected boolean createLog;
           
    /**
     * Order of node elminination
	 */
	protected ArrayList<Node> nodeEliminationOrder;

	/**
	 * Copy of nodes without utility nodes. This is used during transformation
	 * process.
	 */
	protected ArrayList<Node> copiaNos;
	
	protected List<Edge> copiaArcos;

	/**
	 * Points to junction tree related to the graph.
	 * @deprecated use {@link #getJunctionTree()} or {@link #setJunctionTree(JunctionTree)} instead.
	 */
	protected IJunctionTree junctionTree;	
    
    /**
     * Creates a new graph with no nodes or edges.
     */
    public SingleEntityNetwork(String name) {
    	super(name);
        arcosMarkov = new ArrayList<Edge>();
        logManager = new NetworkCompilationLogManager(); 
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
		DefaultTreeModel model = new DefaultTreeModel(root);
        hierarchicTree = new HierarchicTree(model);
    }


    public ArrayList<Node> getDescriptionNodes()
    {   ArrayList<Node> descriptionNodes = new ArrayList<Node>();
        int size = nodeList.size();
        for (int i=0;i<size;i++)
        {   Node node = getNodeAt(i);
            if ((node.getType() == Node.PROBABILISTIC_NODE_TYPE) && (node.getInformationType() == Node.DESCRIPTION_TYPE))
            {   descriptionNodes.add(node);
            }
        }
        return descriptionNodes;
    }

    public ArrayList<Node> getExplanationNodes()
    {   ArrayList<Node> explanationNodes = new ArrayList<Node>();
        int size = nodeList.size();
        for (int i=0;i<size;i++)
        {   Node node = getNodeAt(i);
            if ((node.getType() == Node.PROBABILISTIC_NODE_TYPE) && (node.getInformationType() == Node.EXPLANATION_TYPE))
            {   explanationNodes.add(node);
            }
        }
        return explanationNodes;
    }

    /**
     *  Build the adjacent list of each node in nodeList (then adjacent list 
     *  contains all nodes that is or father or child of the origin node)_.
     */
    protected void makeAdjacentsListForNodeListElements() {
        this.clearAdjacents();
        for (int qnos = 0; qnos < nodeList.size(); qnos++) {
            nodeList.get(qnos).makeAdjacents();
        }
    }
    
    
    protected void makeAdjacents() {
    	clearAdjacents();
    	for (int z = arcosMarkov.size() - 1; z >= 0; z--) {
			Edge auxArco = arcosMarkov.get(z);
			auxArco.getOriginNode().getAdjacents().add(
				auxArco.getDestinationNode());
			auxArco.getDestinationNode().getAdjacents().add(
				auxArco.getOriginNode());
		}
	
		for (int z = copiaArcos.size() - 1; z >= 0; z--) {
			Edge auxArco = copiaArcos.get(z);
			if (auxArco.getDestinationNode().getType()
				== Node.UTILITY_NODE_TYPE) {
				copiaArcos.remove(z);
			} else {
				auxArco.getOriginNode().getAdjacents().add(
					auxArco.getDestinationNode());
				auxArco.getDestinationNode().getAdjacents().add(
					auxArco.getOriginNode());
			}
		}
//		arcosMarkov = SetToolkit.union(arcosMarkov, edges);    	
    }


    /**
     * For each node in the graph, destroys the list of their adjacent nodes.
     */
    protected void clearAdjacents() {
    	int size = nodeList.size();
        for (int qnos = 0; qnos < size; qnos++) {
            nodeList.get(qnos).clearAdjacents();
        }
    }


    /**
     *  Verify if this network has cycle.
     *
     *@throws Exception If this network has a cycle.
     *@deprecated use {@link #hasCycle()} instead.
     */
    public void verifyCycles() throws Exception {
    	int nodeSize = nodeList.size();
    	char[] visited = new char[nodeSize];
    	int[] pi = new int[nodeSize];
    	
    	for (int i = 0; i < nodeSize; i++) {
    		dfsCycle(i, visited, pi);
    	}
    }
    
    /**
     * @return true if network has a cycle. False otherwise.
     */
    public boolean hasCycle() {
    	// TODO migrate the verifyCycles code to here and remove verifyCycles.
    	try {
			this.verifyCycles();
    		return false;
		} catch (Exception e) {
			// TODO: stop using exception-driven methods (exceptions should not be used as test conditions)
			return true;
		}
    }
    
    /**
     * Depth first search to verify cycle.
     */
    private void dfsCycle(int nodeIndex, char[] visited, int[] pi) throws Exception {
    	if (visited[nodeIndex] != 0) { 			
 			// Back edge. Has cycle!
    		if (visited[nodeIndex] == 1) {
                throw new Exception(resource.getString("CicleNetException") 
                					 + " " + createPath(nodeIndex, nodeIndex, pi, true));
    		}
    		return;    		
    	}
    	
    	visited[nodeIndex] = 1;    	
    	Node node = nodeList.get(nodeIndex);    	
    	for (int i = node.getChildren().size()-1; i >= 0; i--) {
    		int newIndex = getNodeIndex(node.getChildren().get(i).getName());
    		pi[newIndex] = nodeIndex; 
    		dfsCycle(newIndex, visited, pi);
    	}
    	visited[nodeIndex] = 2;
    }
    
    /**
     * Auxiliary method for dfsCycle() to construct the path of the cycle detected. 
     */
    private String createPath(int currentIndex, int nodeIndex, int[] pi, boolean first) {
    	if (currentIndex == nodeIndex && ! first) {
			return nodeList.get(currentIndex).getName();
    	}
    	return createPath(pi[currentIndex], nodeIndex, pi, false) + " " + nodeList.get(currentIndex).getName();
    }
    

    /**
     * It verifies if the network is connected
     *
     *  @throws Exception if network is disconnected.
     *  @deprecated use {@link #isConnected()} instead.
     */
    public void verifyConectivity() throws Exception {
        List<Node> visitados = new ArrayList<Node>(nodeList.size());
        if (nodeList.size() <= 1) {
            return;
        }
        makeAdjacentsListForNodeListElements();
        dfsConnectivity(nodeList.get(0), visitados);
        clearAdjacents();
        if (visitados.size() != nodeList.size()) {
            throw new Exception(resource.getString("DisconectedNetException"));
        }
    }
    
    /**
     * @return true if network is fully connected (all nodes has at least one path to each other). False otherwise.
     */
    public boolean isConnected() {
    	try {
    		// TODO migrate code of verifyConectivity to here and remove verifyConectivity method.
    		this.verifyConectivity();
			return true;
		} catch (Exception e) {
			// TODO: stop using exception-driven methods (exceptions should not be used as test conditions)
			return false;
		}
    }

    /**
     * Depth first search to verify connectivity.
     */
    private void dfsConnectivity(Node no, List<Node> visitados) {
        visitados.add(no);
        for (int i = 0; i < no.getAdjacents().size(); i++) {
            Node aux = no.getAdjacents().get(i);
            if (! visitados.contains(aux)) {
                dfsConnectivity(aux, visitados);
            }
        }
    }

	/**
	 *  Performs moralization of the network.
	 */
	protected void moralize() {
		Node auxNo;
		Node auxPai1;
		Node auxPai2;
		Edge auxArco;
		
		clearAdjacents();
	
		if (createLog) {
			logManager.append(resource.getString("moralizeLabel"));
		}
		arcosMarkov.clear();
		copiaArcos = (ArrayList<Edge>)SetToolkit.clone(edgeList);
	
		// remove the list of edges for information
		int sizeArcos = copiaArcos.size() - 1;
		for (int i = sizeArcos; i >= 0; i--) {
			auxArco = copiaArcos.get(i);
			if (auxArco.getDestinationNode().getType()
				== Node.DECISION_NODE_TYPE) {
				copiaArcos.remove(i);
			}
		}
	
		int sizeNos = nodeList.size();
		for (int n = 0; n < sizeNos; n++) {
			auxNo = nodeList.get(n);
			if (!(auxNo.getType() == Node.DECISION_NODE_TYPE)
				&& auxNo.getParents().size() > 1) {
				int sizePais = auxNo.getParents().size();
				for (int j = 0; j < sizePais - 1; j++) {
					auxPai1 = auxNo.getParents().get(j);
					for (int k = j + 1; k < sizePais; k++) {
						auxPai2 = auxNo.getParents().get(k);
						if ((hasEdge(auxPai1, auxPai2, copiaArcos) == -1)
							&& (hasEdge(auxPai1, auxPai2, arcosMarkov) == -1)) {
							auxArco = new Edge(auxPai1, auxPai2);
							if (createLog) {
								logManager.append(
									auxPai1.getName()
										+ " - "
										+ auxPai2.getName()
										+ "\n");
							}
							arcosMarkov.add(auxArco);
						}
					}
				}
			}
		}
		
		makeAdjacents();
		
		if (createLog) {
			logManager.append("\n");
		}
	}

	/**
	 * Sets the createLog.
	 * @param createLog The createLog to set
	 */
	public void setCreateLog(boolean createLog) {
		this.createLog = createLog;
	}

	/**
	 * Builds junction tree from graph.
	 */
	protected void compileJT(IJunctionTree jt) throws Exception {
		int menor;
		Clique auxClique;
		Separator auxSep;
	
		resetEvidences();
		
		junctionTree = jt;
		
		this.cliques();
		this.arvoreForte();
		this.sortCliqueNodes();
		this.associateCliques();
		junctionTree.initBeliefs();
	
		int sizeNos = copiaNos.size();
		for (int c = 0; c < sizeNos; c++) {
			Node auxNode = copiaNos.get(c);
			menor = Integer.MAX_VALUE;
			if (auxNode.getType() == Node.PROBABILISTIC_NODE_TYPE) {
				int sizeSeparadores = junctionTree.getSeparatorsSize();
				for (int c2 = 0; c2 < sizeSeparadores; c2++) {
					auxSep = (Separator) junctionTree.getSeparatorAt(c2);
					if (auxSep.getNodes().contains(auxNode)
						&& (auxSep.getProbabilityFunction().tableSize() < menor)) {
						((ProbabilisticNode) auxNode).setAssociatedClique(
							auxSep);
						menor = auxSep.getProbabilityFunction().tableSize();
					}
				}
			}
	
			if (menor == Integer.MAX_VALUE) {
				int sizeCliques = junctionTree.getCliques().size();
				for (int c2 = 0; c2 < sizeCliques; c2++) {
					auxClique = (Clique) junctionTree.getCliques().get(c2);
					if (auxClique.getNodes().contains(auxNode)
						&& (auxClique.getProbabilityFunction().tableSize() < menor)) {
						// the following code was changed because typechecking should be done in class level instead of checking int values
						//						if (auxNode.getType()
//							== Node.PROBABILISTIC_NODE_TYPE) {
						if (auxNode instanceof ProbabilisticNode) {
							((ProbabilisticNode) auxNode).setAssociatedClique(
								auxClique);
						} else {
							((DecisionNode) auxNode).setAssociatedClique(
								auxClique);
							break;
						}
						menor = auxClique.getProbabilityFunction().tableSize();
					}
				}
			}
		}
	
		updateMarginais();
	
		if (createLog) {
			Thread t = new Thread(new Runnable() {
				public void run() {
					makeLog();
					System.out.println("**Log ended**");
				}
			});
			t.setPriority(Thread.MIN_PRIORITY);
			t.start();
		}
	}

	public void resetEvidences() {
		for (Node node : this.getNodesCopy()) {
			if (node instanceof TreeVariable) {
				((TreeVariable)node).resetEvidence();
				// OBS utility nodes are not tree variables
			}
		}
	}
	
	public void resetLikelihoods() {
		for (Node node : this.getNodesCopy()) {
			if (node instanceof TreeVariable) {
				((TreeVariable)node).resetLikelihood();
				// OBS utility nodes are not tree variables
			}
		}
	}

	/**
	 * Returns true if this network is a Influence Diagram
	 * @return Returns true if this network is a Influence Diagram, false otherwise.
	 */
	public boolean isID() {
		for (int i = 0; i < nodeList.size(); i++) {
			if (nodeList.get(i).getType() == Node.DECISION_NODE_TYPE
				|| nodeList.get(i).getType() == Node.UTILITY_NODE_TYPE) {
	
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns true if this network is hybrid, in other words, if it has at least 
	 * one continuous node.
	 * @return Returns true if this network is hybrid (continuous and discrete nodes).
	 */
	public boolean isHybridBN() {
		for (int i = 0; i < nodeList.size(); i++) {
			if (nodeList.get(i).getType() == Node.CONTINUOUS_NODE_TYPE) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns true if this network has only discrete probabilistic nodes, i.e. simple BN.
	 * @return Returns true if this network has only discrete probabilistic nodes, i.e. simple BN.
	 */
	public boolean isBN() {
		for (int i = 0; i < nodeList.size(); i++) {
			if (nodeList.get(i).getType() != Node.PROBABILISTIC_NODE_TYPE) {
				return false;
			}
		}
		return true;
	}

	protected void updateMarginais() {
		for (int i = 0; i < copiaNos.size(); i++) {
			TreeVariable node = (TreeVariable) copiaNos.get(i);
			/* Check if the node represents a numeric attribute */
			if (node.getStatesSize() == 0) {
				/* 
				 * The node represents a numeric attribute which has no
				 * potential table. Just skip it.
				 */
				continue;
			}
			node.marginal();
		}
	}

	/**
	 * Identifies cliques
	 */
	protected void cliques() {
		int i;
		int j;
		Node auxNo;
		Node auxNo2;
		int e;
		Clique auxClique;
		Clique auxClique2;
		List<Clique> listaCliques = new ArrayList<Clique>();
	
		int sizeNos = copiaNos.size();
		for (i = 0; i < sizeNos; i++) {
			auxNo = copiaNos.get(i);
			e = nodeEliminationOrder.indexOf(auxNo);
			auxClique = new Clique();
			auxClique.getNodes().add(auxNo);
	
			int sizeAdjacentes = auxNo.getAdjacents().size();
			for (j = 0; j < sizeAdjacentes; j++) {
				auxNo2 = auxNo.getAdjacents().get(j);
				if (nodeEliminationOrder.indexOf(auxNo2) > e) {
					auxClique.getNodes().add(auxNo2);
				}
			}
			listaCliques.add(auxClique);
		}
	
		boolean haTroca = true;
		while (haTroca) {
			haTroca = false;
			for (i = 0; i < listaCliques.size() - 1; i++) {
				auxClique = listaCliques.get(i);
				auxClique2 = listaCliques.get(i + 1);
				if (auxClique.getNodes().size() > auxClique2.getNodes().size()) {
					listaCliques.set(i + 1, auxClique);
					listaCliques.set(i, auxClique2);
					haTroca = true;
				}
			}
		}
	
		int sizeCliques = listaCliques.size();
	
		for1 : for (i = 0; i < sizeCliques; i++) {
			auxClique = listaCliques.get(i);
			for (j = i + 1; j < sizeCliques; j++) {
				auxClique2 = listaCliques.get(j);
	
				if (auxClique2.getNodes().containsAll(auxClique.getNodes())) {
					continue for1;
				}
			}
			junctionTree.getCliques().add(auxClique);
		}
		listaCliques.clear();
	}

	/**
	 * Order nodes (of cliques) and separators accourding to the elimination order.
	 */
	protected void sortCliqueNodes() {
		List listaCliques = junctionTree.getCliques();
		boolean isID = isID();
		for (int k = 0; k < listaCliques.size(); k++) {
			Clique clique = (Clique) listaCliques.get(k);
			ArrayList<Node> nosClique = clique.getNodes();
			boolean haTroca = true;
			while (haTroca) {
				haTroca = false;
				for (int i = 0; i < nosClique.size() - 1; i++) {
					Node node1 = nosClique.get(i);
					Node node2 = nosClique.get(i + 1);
					if (isID) {
						if (nodeEliminationOrder.indexOf(node1) > nodeEliminationOrder.indexOf(node2)) {
							nosClique.set(i + 1, node1);
							nosClique.set(i, node2);
							haTroca = true;
						}
					} else { 
						if (node1.getName().compareToIgnoreCase(node2.getName()) > 0 ) {
							nosClique.set(i + 1, node1);
							nosClique.set(i, node2);
							haTroca = true;
						}	
					}
				}
			}
		}
	
		for (int k = junctionTree.getSeparatorsSize() - 1; k >= 0; k--) {
			Separator separator = (Separator) junctionTree.getSeparatorAt(k);
			ArrayList<Node> nosSeparator = separator.getNodes();
			boolean haTroca = true;
			while (haTroca) {
				haTroca = false;
				for (int i = 0; i < nosSeparator.size() - 1; i++) {
					Node node1 = nosSeparator.get(i);
					Node node2 = nosSeparator.get(i + 1);
					if (node1.getName().compareToIgnoreCase(node2.getName()) > 0 ) {
						nosSeparator.set(i + 1, node1);
						nosSeparator.set(i, node2);
						haTroca = true;
					}
					/*
					if (oe.indexOf(node1) > oe.indexOf(node2)) {
						nosSeparator.set(i + 1, node1);
						nosSeparator.set(i, node2);
						haTroca = true;
					}
					*/
				}
			}
		}
	}

	protected void makeLog() {
		long in = System.currentTimeMillis();
		try {
			logManager.finishLog(junctionTree, nodeList);
			if (id != null) {
				logManager.writeToDisk(id + ".txt", false);
			} else {
				logManager.writeToDisk(TextLogManager.DEFAULT_FILENAME, false);				
			}
		} catch (java.io.IOException ioe) {
			System.err.println(ioe.getMessage());
		}
		System.out.println(
			"GERACAO DO ARQUIVO LOG em "
				+ (System.currentTimeMillis() - in)
				+ " ms");
	}

	/**
	 * Associates the nodes to a unique clique, with less space for states, which contains its family.
	 */
	protected void associateCliques() {
		int min;
		Node auxNo;
		IProbabilityFunction auxTabPot, auxUtilTab;
		Clique auxClique;
		Clique cliqueMin = null;
	
		for (int i = junctionTree.getCliques().size() - 1; i >= 0; i--) {
			auxClique = (Clique) junctionTree.getCliques().get(i);
			auxTabPot = auxClique.getProbabilityFunction();
			auxUtilTab = auxClique.getUtilityTable();
	
			int sizeNos = auxClique.getNodes().size();
			for (int c = 0; c < sizeNos; c++) {
				auxTabPot.addVariable(auxClique.getNodes().get(c));
				auxUtilTab.addVariable(auxClique.getNodes().get(c));
			}
		}
	
		for (int k = junctionTree.getSeparatorsSize() - 1; k >= 0; k--) {
			Separator auxSep = (Separator) junctionTree.getSeparatorAt(k);
			auxTabPot = auxSep.getProbabilityFunction();
			auxUtilTab = auxSep.getUtilityTable();
			int sizeNos = auxSep.getNodes().size();
			for (int c = 0; c < sizeNos; c++) {
				auxTabPot.addVariable(auxSep.getNodes().get(c));
				auxUtilTab.addVariable(auxSep.getNodes().get(c));
			}
		}
	
		int sizeNos = nodeList.size();
		for (int n = 0; n < sizeNos; n++) {
			if (nodeList.get(n).getType() == Node.DECISION_NODE_TYPE) {
				continue;
			}
	
			min = Integer.MAX_VALUE;
			auxNo = nodeList.get(n);
	
			int sizeCliques = junctionTree.getCliques().size();
			for (int c = 0; c < sizeCliques; c++) {
				auxClique = (Clique) junctionTree.getCliques().get(c);
	
				if (auxClique.getProbabilityFunction().tableSize() < min
					&& auxClique.getNodes().containsAll(auxNo.getParents())) {
					if (auxNo.getType() == Node.PROBABILISTIC_NODE_TYPE
						&& !auxClique.getNodes().contains(auxNo)) {
						continue;
					}
					cliqueMin = auxClique;
					min = cliqueMin.getProbabilityFunction().tableSize();
				}
			}
			// perform class check instead of int check
			if (auxNo instanceof ProbabilisticNode) {
				cliqueMin.getAssociatedProbabilisticNodes().add(auxNo);
			} else {
				cliqueMin.getAssociatedUtilityNodes().add(auxNo);
			}
			// 
//			if (auxNo.getType() == Node.PROBABILISTIC_NODE_TYPE) {
//				cliqueMin.getAssociatedProbabilisticNodes().add(auxNo);
//			} else if () {
//				cliqueMin.getAssociatedUtilityNodes().add(auxNo);
//			}
		}
	}

	/**
	 *  Builds the junction tree  - Frank Jensen
	 */
	protected void arvoreForte() {
		int ndx;
		Clique auxClique;
		Clique auxClique2;
		ArrayList<Node> uni;
		ArrayList<Node> inter;
		ArrayList<Node> auxList;
		ArrayList<Node> listaNos;
		Separator sep;
		ArrayList<Node> alpha = new ArrayList<Node>();
	
		for (int i = nodeEliminationOrder.size() - 1; i >= 0; i--) {
			alpha.add(nodeEliminationOrder.get(i));
		}
	
		if (copiaNos.size() > 1) {
			int sizeCliques = junctionTree.getCliques().size();
			for (int i = 0; i < sizeCliques; i++) {
				auxClique = (Clique) junctionTree.getCliques().get(i);
				listaNos = SetToolkit.clone(auxClique.getNodes());
				if (listaNos.size() <= 1) {
					break;
				}
				//calculate index
				while ((ndx = getCliqueIndex(listaNos, alpha)) <= 0
					&& listaNos.size() > 1);
				if (ndx < 0) {
					ndx = 0;
				}
				auxClique.setIndex(ndx);
				listaNos.clear();
			}
			alpha.clear();
	
			Comparator<Clique> comparador = new Comparator<Clique>() {
				public int compare(Clique o1, Clique o2) {
					Clique c1 = o1;
					Clique c2 = o2;
					if (c1.getIndex() > c2.getIndex()) {
						return 1;
					}
					if (c1.getIndex() < c2.getIndex()) {
						return -1;
					}
					return 0;
				}
			};
	
			Collections.sort(junctionTree.getCliques(), comparador);
	
			auxClique = (Clique) junctionTree.getCliques().get(0);
			uni = SetToolkit.clone(auxClique.getNodes());
	
			int sizeCliques1 = junctionTree.getCliques().size();
			for (int i = 1; i < sizeCliques1; i++) {
				auxClique = (Clique) junctionTree.getCliques().get(i);
				inter = SetToolkit.intersection(auxClique.getNodes(), uni);
	
				for (int j = 0; j < i; j++) {
					auxClique2 = (Clique) junctionTree.getCliques().get(j);
	
					if (!auxClique2.getNodes().containsAll(inter)) {
						continue;
					}
	
					sep = new Separator(auxClique2, auxClique);
					sep.setNodes(inter);
					junctionTree.addSeparator(sep);
	
					auxList = SetToolkit.union(auxClique.getNodes(), uni);
					uni.clear();
					uni = auxList;
					break;
				}
			}
		}
	}

	/**
	 * Sub-method of strong-tree method
	 */
	protected int getCliqueIndex(ArrayList<Node> listaNos, ArrayList<Node> alpha) {
		int ndx;
		int mx;
		Node auxNo;
		Node noMax = null;
		ArrayList<Node> auxList = null;
		ArrayList<Node> vizinhos;
	
		// gets the maximum index node using alpha order (inverse of elimination order)
		mx = -1;
		int sizeNos = listaNos.size();
		for (int i = 0; i < sizeNos; i++) {
			auxNo = listaNos.get(i);
			ndx = alpha.indexOf(auxNo);
			if (mx < ndx) {
				mx = ndx;
				noMax = auxNo;
			}
		}
	
		// remove node from clique
		listaNos.remove(noMax);
	
		// Build list of neighbors of clique
		auxNo = listaNos.get(0);
		vizinhos = SetToolkit.clone(auxNo.getAdjacents());
		int sizeNos1 = listaNos.size();
		for (int i = 1; i < sizeNos1; i++) {
			auxNo = listaNos.get(i);
			auxList = SetToolkit.intersection(vizinhos, auxNo.getAdjacents());
			vizinhos.clear();
			vizinhos = auxList;
		}
		vizinhos.remove(noMax);
	
		ndx = 0;
		int sizeVizinhos = vizinhos.size();
		for (int i = 0; i < sizeVizinhos; i++) {
			auxNo = vizinhos.get(i);
			if (listaNos.contains(auxNo) || (alpha.indexOf(auxNo) > mx)) {
				continue;
			}
			ndx = mx;
			break;
		}
	
		return ndx;
	}

	/**
	 * Sub-routine for {@link ProbabilisticNetwork#triangula()}.
	 * It eliminates the nodes in the graph by using minimum weight heuristics.
	 * First, it eliminates nodes whose adjacent nodes are pairwise connected.
	 * After that, if there are more nodes in the graph, it eliminates them using the
	 * minimum weight heuristic.
	 *
	 * @param  auxNos  collection of nodes.
	 * @deprecated use {@link JunctionTreeAlgorithm#minimumWeightElimination(List, ProbabilisticNetwork)}
	 */
	protected boolean minimumWeightElimination(ArrayList<Node> auxNos) {
		boolean algum;
		
		algum = true;
		while (algum) {
			algum = false;
	
			for (int i = auxNos.size() - 1; i >= 0; i--) {
				Node auxNo = auxNos.get(i);
	
				if (cordas(auxNo)) {
					//N�ｽo tem cordas necess�ｽrias:teste pr�ｽximo.
					continue;
				}
	
				for (int j = auxNo.getAdjacents().size() - 1; j >= 0; j--) {
					Node v = auxNo.getAdjacents().get(j);
					//boolean removed = v.getAdjacents().remove(auxNo);				
					//assert removed;
					v.getAdjacents().remove(auxNo);
				}
				auxNos.remove(auxNo);
				algum = true;
				nodeEliminationOrder.add(auxNo);
				if (createLog) {
					logManager.append(
						"\t" + nodeEliminationOrder.size() + " " + auxNo.getName() + "\n");
				}
			}
		}
	
		if (auxNos.size() > 0) {
			Node auxNo = weight(auxNos); //auxNo: clique de peso m�ｽnimo.
			nodeEliminationOrder.add(auxNo);
			if (createLog) {
				logManager.append(
					"\t" + nodeEliminationOrder.size() + " " + auxNo.getName() + "\n");
			}
			elimine(auxNo, auxNos); //Elimine no e reduza grafo.
			return true;
		}
		
		return false;
	}

	/**
	 *  SUB-FUN�ｽ�ｽO do procedimento triangula que elimina n�ｽ e reduz o grafo. Inclui
	 *  cordas necess�ｽrias para eliminar n�ｽ. Retira-o e aos adjacentes.
	 *
	 *@param  no      n�ｽ a ser eliminado
	 *@param  auxNos  lista de n�ｽs
	 */
	private void elimine(Node no, ArrayList<Node> auxNos) {	
		for (int i = no.getAdjacents().size()-1; i > 0; i--) {
			Node auxNo1 = no.getAdjacents().get(i);
	
			for (int j = i - 1; j >= 0; j--) {
				Node auxNo2 = no.getAdjacents().get(j);
				if (! auxNo2.getAdjacents().contains(auxNo1)) {
					Edge auxArco = new Edge(auxNo1, auxNo2);
					if (createLog) {
						logManager.append(
							auxNo1.getName()
								+ resource.getString("linkedName")
								+ auxNo2.getName()
								+ "\n");
					}
					arcosMarkov.add(auxArco);
					auxNo1.getAdjacents().add(auxNo2);
					auxNo2.getAdjacents().add(auxNo1);			
					
					System.out.println(auxArco);
				}
			}
		}
	
		for (int i = no.getAdjacents().size() - 1; i >= 0; i--) {
			Node auxNo1 = no.getAdjacents().get(i);
			//boolean removed = auxNo1.getAdjacents().remove(no);
			//assert removed;
			auxNo1.getAdjacents().remove(no);
		}
		auxNos.remove(no);
	}

	/**
	 *  SUB-FUN�ｽ�ｽO do m�ｽtodo pesoMinimo que utiliza a her�ｽstica do peso m�ｽnimo.
	 *
	 * @param  auxNos  n�ｽs.
	 * @return         n�ｽ cujo conjunto formado por adjacentes possui peso m�ｽnimo.
	 */
	private Node weight(ArrayList<Node> auxNos) {
		Node v;
		Node auxNo;
		double p;
	
		Node noMin = null;
		double pmin = Double.MAX_VALUE;

		for (int i = auxNos.size()-1; i >= 0; i--) {
			auxNo = auxNos.get(i);
			p = Math.log(auxNo.getStatesSize());
	
			for (int j = auxNo.getAdjacents().size()-1; j >= 0; j--) {
				v = auxNo.getAdjacents().get(j);
				p += Math.log(v.getStatesSize());
			}
			if (p < pmin) {
				pmin = p;
				noMin = auxNo;
			}
		}
		
//		assert noMin != null;
		return noMin;
	}

	/**
	 *  SUB-FUN�ｽ�ｽO do m�ｽtodo triangula.
	 *
	 *@param  no  n�ｽ.
	 *@return     true - caso haja necessidade de inserir corda para poder eliminar
	 *      o n�ｽ. false - caso contr�ｽrio.
	 */
	private boolean cordas(Node no) {
		if (no.getAdjacents().size() < 2) {
			return false;
		}
	
		for (int i = no.getAdjacents().size()-1; i > 0; i--) {
			Node auxNo1 = no.getAdjacents().get(i);
	
			for (int j = i - 1; j >=0; j--) {
				Node auxNo2 = no.getAdjacents().get(j);
				if (! auxNo2.getAdjacents().contains(auxNo1)) {
					return true;
				}
			}
		}
		return false;
	}
	
	
	/**
	 * Verifies integrity as a coherent directed acyclic graph.
	 * The output specifies what kinds of errors were detected, if any.
	 * TODO stop using exception-driven code (exceptions should not be used as test conditions)
	 */
	protected void verifyConsistency() throws Exception {
		if (nodeList.size() != 0) {
			
			nodeIndexes.clear();
			for (int i = nodeList.size()-1; i>=0; i--) {
				nodeIndexes.put(nodeList.get(i).getName(), new Integer(i));				
			}
			
			boolean erro = false;

			StringBuffer sb = new StringBuffer();

			try {
				verifyUtility();
			} catch (Exception e) {
				erro = true;
				sb.append(e.getMessage());
			}
			try {
				verifyCycles();
			} catch (Exception e) {
				erro = true;
				sb.append('\n' + e.getMessage());
			}
			// disconnected networks should be OK now if we are using instances of JunctionTreeAlgorithm (because it normalizes each disconnected clique after propagation)
//			try {
//				verifyConectivity();
//			} catch (Exception e) {
//				erro = true;
//				sb.append('\n' + e.getMessage());
//			}
			try {
				verifyPotentialTables();
			} catch (Exception e) {
				erro = true;
				sb.append('\n' + e.getMessage());
			}
			try {
				sortDecisions();
			} catch (Exception e) {
				erro = true;
				sb.append('\n' + e.getMessage());
			}

			if (erro) {
				throw new Exception(sb.toString());
			}
		}
	}

	public HierarchicTree getHierarchicTree() {
		return hierarchicTree;
	}

	public void setHierarchicTree(HierarchicTree hierarchicTree) {
		this.hierarchicTree = hierarchicTree;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public double getRadius() {
		return radius;
	}

	/**
	 * Sub-method of {@link #verifyConsistency()}.
	 * This method verifies consistency of conditional probabilistic tables.
	 * @deprecated use {@link JunctionTreeAlgorithm#verifyPotentialTables(unbbayes.prs.Graph)} instead
	 */
	protected void verifyPotentialTables() throws Exception {
		ProbabilisticTable auxTabPot;
		int c;
		Node auxNo;
		ProbabilisticNode auxVP;
	
		int sizeNos = nodeList.size();
		for (c = 0; c < sizeNos; c++) {
			auxNo = nodeList.get(c);
			if (auxNo.getType() == Node.PROBABILISTIC_NODE_TYPE) {
				auxVP = (ProbabilisticNode) auxNo;
				auxTabPot = (ProbabilisticTable) auxVP.getProbabilityFunction();
				auxTabPot.verifyConsistency();
			}
		}
	}
	
	/**
	 * Sub method of {@link #verifyConsistency()}.
	 * This method asserts no utility nodes have children.
	 * @deprecated use {@link JunctionTreeAlgorithm#verifyUtility(ProbabilisticNetwork)} instead
	 */
	protected void verifyUtility() throws Exception {
		Node aux;
	
		int sizeNos = nodeList.size();
		for (int i = 0; i < sizeNos; i++) {
			aux = (Node) nodeList.get(i);
			if (aux.getType() == Node.UTILITY_NODE_TYPE
				&& aux.getChildren().size() != 0) {
				throw new Exception(
					resource.getString("variableName")
						+ aux
						+ resource.getString("hasChildName"));
			}
		}
	}

	/**
	 * Sub method of {@link #verifyConsistency()}.
	 * This method checks if decision nodes follow a linear (total) order.
	 * That is, if there is a directed path through decision nodes.
	 */
	protected void sortDecisions() throws Exception {
		clearAdjacents();
		decisionNodes = new ArrayList<Node>();
		int sizeNos = nodeList.size();
		for (int i = 0; i < sizeNos; i++) {
			if (nodeList.get(i).getType() == Node.DECISION_NODE_TYPE) {
				decisionNodes.add(nodeList.get(i));
			}
		}
	
		ArrayList<Node> fila = new ArrayList<Node>();
		fila.ensureCapacity(nodeList.size()); 
		Node aux, aux2, aux3;
	
		int sizeDecisao = decisionNodes.size();
		for (int i = 0; i < sizeDecisao; i++) {
			boolean visitados[] = new boolean[nodeList.size()];
			aux = (Node) decisionNodes.get(i);
			fila.clear();
			fila.add(aux);
	
			while (fila.size() != 0) {
				aux2 = fila.remove(0);
				visitados[nodeList.indexOf(aux2)] = true;
	
				int sizeFilhos = aux2.getChildren().size();
				for (int k = 0; k < sizeFilhos; k++) {
					aux3 = (Node) aux2.getChildren().get(k);
					if (!visitados[nodeList.indexOf(aux3)]) {
						if (aux3.getType() == Node.DECISION_NODE_TYPE
							&& !aux.getAdjacents().contains(aux3)) {
							aux.getAdjacents().add(aux3);
						}
						fila.add(aux3);
					}
				}
			}
		}
	
		boolean haTroca = true;
		while (haTroca) {
			haTroca = false;
			for (int i = 0; i < decisionNodes.size() - 1; i++) {
				Node node1 = decisionNodes.get(i);
				Node node2 = decisionNodes.get(i + 1);
				if (node1.getAdjacents().size()
					< node2.getAdjacents().size()) {
					decisionNodes.set(i + 1, node1);
					decisionNodes.set(i, node2);
					haTroca = true;
				}
			}
		}
	
//		int sizeDecisao1 = decisionNodes.size();
//		for (int i = 0; i < sizeDecisao1; i++) {
//			System.out.print(decisionNodes.get(i) + " ");
//		}
//		System.out.println();
	
		for (int i = 0; i < decisionNodes.size(); i++) {
			aux = decisionNodes.get(i);
			//            System.out.print(aux.getAdjacents().size() + " ");
			if (aux.getAdjacents().size() != decisionNodes.size() - i - 1) {
				throw new Exception(
					resource.getString("DecisionOrderException"));
			}
		}
	
		clearAdjacents();
	}

	/**
	 * Return a copy of the nodes (without utility nodes).
	 *
	 * @return A copy of the nodes (without utility nodes).
	 */
	public ArrayList<Node> getNodesCopy() {
		if (copiaNos == null) {
			copiaNos = (ArrayList<Node>)nodeList.clone();
		}
		return copiaNos;
	}
	
	/**
	 * Reset the copy of the nodes. It is usually due to changes 
	 * in the network.
	 */
	public void resetNodesCopy() {
		copiaNos = (ArrayList<Node>)nodeList.clone();
	}

	public String getLog() {
		return logManager.getLog();
	}

	/**
	 * Gets the createLog.
	 * @return Returns a boolean
	 */
	public boolean isCreateLog() {
		return createLog;
	}

	/**
	 * Calls the junction tree method in order to update evidences.
	 *  @throws Exception : the message will contain any consistency error.
	 */
	public void updateEvidences() throws Exception {
		int sizeNos = this.getNodesCopy().size();
		for (int c = 0; c < sizeNos; c++) {
			TreeVariable node = (TreeVariable) copiaNos.get(c);
			node.updateEvidences();
		}

		try {
			junctionTree.consistency();
		} catch (Exception e) {
			initialize();
			throw e;
		}
		updateMarginais();
		resetLikelihoods();
	}
	
	/**
	 * Initialize the believes of a junction tree.
	 */
	public void initialize() throws Exception {
		resetEvidences();
		junctionTree.initBeliefs();
		if (firstInitialization) {
			updateMarginais();
			copyMarginal();
			firstInitialization = false;
		} else {
			restoreMarginais();
		}
	}

	protected void copyMarginal() {
		for (int i = 0; i < copiaNos.size(); i++) {
			TreeVariable node = (TreeVariable) copiaNos.get(i);
			node.copyMarginal();
		}
	}

	protected void restoreMarginais() {
		for (int i = 0; i < copiaNos.size(); i++) {
			TreeVariable node = (TreeVariable) copiaNos.get(i);
			node.restoreMarginal();
		}
	}

	/**
	 * Sets the firstInitialization.
	 * @param firstInitialization The firstInitialization to set
	 */
	public void setFirstInitialization(boolean firstInitialization) {
		this.firstInitialization = firstInitialization;
	}

	/**
	 * @return the total estimated probability of the related
	 * junction tree (PET stands for "Probabilidade Estimada Total", which means
	 *  total estimated probability)
	 */
	public float PET() {
		return junctionTree.getN();
	}


	/**
	 * @return the junctionTree
	 */
	public IJunctionTree getJunctionTree() {
		return junctionTree;
	}


	/**
	 * @param junctionTree the junctionTree to set
	 */
	public  void setJunctionTree(IJunctionTree junctionTree) {
		this.junctionTree = junctionTree;
	}


	/**
	 * @return the logManager
	 */
	public NetworkCompilationLogManager getLogManager() {
		return logManager;
	}


	/**
	 * @param logManager the logManager to set
	 */
	public void setLogManager(NetworkCompilationLogManager logManager) {
		this.logManager = logManager;
	}


	/**
	 * @return the nodeEliminationOrder
	 */
	public ArrayList<Node> getNodeEliminationOrder() {
		return nodeEliminationOrder;
	}


	/**
	 * @param nodeEliminationOrder the nodeEliminationOrder to set
	 */
	public void setNodeEliminationOrder(ArrayList<Node> nodeEliminationOrder) {
		this.nodeEliminationOrder = nodeEliminationOrder;
	}
}

