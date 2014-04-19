/**
 * 
 */
package unbbayes.util.extension.bn.inference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import unbbayes.prs.Edge;
import unbbayes.prs.Graph;
import unbbayes.prs.INode;
import unbbayes.prs.Node;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.util.Debug;

/**
 * This class factorizes nodes in a network when
 * the type of Independence of Causal Influence (ICI) is noisy-max.
 * It will use temporal factorization, which is efficient for junction tree propagation.
 * @author Shou Matsumoto
 * @see ICIFactorizationJunctionTreeAlgorithm
 * @see NoisyMaxCPTConverter
 */
public class NoisyMaxTemporalFactorizationHandler implements ICINodeFactorizationHandler {

	private Map<INode, PotentialTable> tableBackups = null;
//	private Map<INode, List<INode>> parentsBackup;
	private Map<INode, Graph> networkBackup;
	private Map<INode, List<INode>> leakBackup;
	private Map<INode, List<INode>> divorcingNodesBackup;

	/**
	 * Default constructor
	 */
	public NoisyMaxTemporalFactorizationHandler() {}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.ICINodeFactorizationHandler#isICICompatible(unbbayes.prs.INode, unbbayes.prs.Graph)
	 */
	public boolean isICICompatible(INode node, Graph net) {
		// basic assertions
		if (node == null || net == null) {
			return false;
		}
		// TODO Auto-generated method stub
		return (node instanceof ProbabilisticNode) && (node.getParentNodes().size() >= 2);
	}

	/**
	 * Performs temporal factorization of noisy max, by also including leak nodes. 
	 * For instance, A<-[B,C,D,E] will become:
	 * <pre>
	 *  /--LB<-B   /--LC<-C  /--LD<-D
	 * v          v         v
	 * A<--------D1<-------D2<---LE<--E
	 * </pre>
	 * Where LX are leak nodes for nodes X; 
	 * and {D1,D2} are the nodes that divorces.
	 * @see unbbayes.util.extension.bn.inference.ICINodeFactorizationHandler#treatICI(unbbayes.prs.INode, unbbayes.prs.Graph)
	 * @param node : it is expected to be a {@link ProbabilisticNode}
	 * @param net : it is expected to be a {@link ProbabilisticNetwork}
	 * @throws InvalidParentException 
	 */
	public void treatICI(INode node, Graph net)  {
		// basic assertions
		if (node == null || net == null) {
			return;
		}
		
		// make sure we are working with probabilistic nodes
		ProbabilisticNode mainNode;
		try {
			mainNode = (ProbabilisticNode) node;
		} catch (ClassCastException e) {
			throw new IllegalArgumentException("Node is expected to be a probabilistic node.",e);
		}
		// make sure we are working with probabilistic network
		ProbabilisticNetwork network;
		try {
			network = (ProbabilisticNetwork) net;
		} catch (ClassCastException e) {
			throw new IllegalArgumentException("Network is expected to be a probabilistic network.",e);
		}
		
		// backup what network was used for this node
		getNetworkBackup().put(mainNode, network);
		
		
		// get the CPT
		PotentialTable table = mainNode.getProbabilityFunction();
		
		// get parents. Use a clone, because the original list will be changed (we'll remove parents from main node)
		ArrayList<INode> originalParents = new ArrayList<INode>(mainNode.getParentNodes());
//		getParentsBackup().put(mainNode, originalParents);
		
		// create leak nodes
		List<INode> leakNodes = new ArrayList<INode>(originalParents.size());	// leakNodes and originalParents shall correspond by indexes
		
		for (INode parent : originalParents) {
			// decide a position to insert node
			Double x = null;
			Double y = null;
			if (parent instanceof Node) {
				// below the parent
				x = ((Node) parent).getPosition().getX(); 
				// place node somewhere in between child and parent, but closer to parent
				y = .4*mainNode.getPosition().getY() + .6*((Node) parent).getPosition().getY() ;
			}
			
			ProbabilisticNode leak = addNewNodeToNet("Leak_"+parent.getName(), x, y, mainNode,network);
			
			// connect leak nodes with parents
			try {
				network.addEdge(new Edge((Node) parent,leak));
			} catch (InvalidParentException e) {
				throw new RuntimeException(e);
			}
			
			// fill CPT of leak node by using values in original node's cpt whose all nodes except parent node is at state 0
			int[] coord = table.getMultidimensionalCoord(0);	// states of all nodes are at state zero
			
			// iterate on states of parents (this is how many columns the cpt of leak node has)
			int indexOfParent = table.indexOfVariable((Node) parent);
			for (int leakTableIndex = 0; coord[indexOfParent] < parent.getStatesSize(); coord[indexOfParent]++) {
				// iterate on rows of CPT (this is associated with states of leak table, which is equal to states of main node as well)
				for (coord[0] = 0; coord[0] < mainNode.getStatesSize(); coord[0]++, leakTableIndex++ ) {
					leak.getProbabilityFunction().setValue(leakTableIndex, table.getValue(coord));
				}
			}
			
			leakNodes.add(leak);
		}
		
		// keep backup of leak nodes, so that we can delete them later
		getLeakBackup().put(mainNode, leakNodes);
		
		// backup original CPT, because we'll change it
		getTableBackups().put(mainNode, (PotentialTable) table.clone());
		
		// disconnect main node from parents. This will change cpt of main node as well
		for (INode parent : originalParents) {
			Edge edge = network.getEdge((Node) parent, mainNode);
			if (edge != null) {
				network.removeEdge(edge);
			}
		}
		
		// Create divorcing nodes. We need numParents-2 divorcing nodes
		List<INode> divorcingNodes = new ArrayList<INode>(leakNodes.size()-2);
		for (int leakIndex = 0; leakIndex < leakNodes.size(); leakIndex++) {
			// extract the current leak node to be connected
			Node leak = (Node) leakNodes.get(leakIndex);
			
			// candidate of a child for the current leak node
			Node child = mainNode;
			
			// first leak is connected directly to main node
			if (leakIndex == 0) {
				try {
					network.addEdge(new Edge((Node) leak, mainNode));
				} catch (InvalidParentException e) {
					throw new RuntimeException(e);
				}
			} else if (leakIndex + 1 >= leakNodes.size()) {
				// last leak is connected directly to last divorcing node
				if (!divorcingNodes.isEmpty()) {
					child = (Node)divorcingNodes.get(divorcingNodes.size()-1);
				}
				try {
					network.addEdge(new Edge( (Node)leak, child));
				} catch (InvalidParentException e) {
					throw new RuntimeException(e);
				}
			} else {
				// create new divorcing node 
				ProbabilisticNode divorcingNode 
					= addNewNodeToNet(
							"Partition_"+leak.getName(), 
							leak.getPosition().getX(), 
							mainNode.getPosition().getY() + Math.abs(mainNode.getPosition().getY() - leak.getPosition().getY()), 	// place divorce node at the other side of the main node
							mainNode, 
							network
						);
				
				// connect new divorcing node to last divorcing node (if there is no divorcing node, connect to mainNode)
				if (!divorcingNodes.isEmpty()) {
					child = (Node)divorcingNodes.get(divorcingNodes.size()-1);
				}
				try {
					network.addEdge(new Edge(divorcingNode, child));
				} catch (InvalidParentException e) {
					throw new RuntimeException(e);
				}
				
				// connect leak node to new divorcing node
				try {
					// Note: cpt will be filled only after all parents were created
					network.addEdge(new Edge(leak, divorcingNode));
				} catch (InvalidParentException e) {
					throw new RuntimeException(e);
				}
				
				divorcingNodes.add(divorcingNode);
			}
			
		}
		
		// backup divorcing nodes, because we may need to delete them later
		getDivorcingNodesBackup().put(mainNode, divorcingNodes);
		
		// make sure the cpt of the main node is 0% for all cells. 
		for (int i = 0; i < table.tableSize(); i++) {
			// This will facilitate a lot, because we'll only need to set a few cells to 100% and keep all others to 0%.
			table.setValue(i, 0f);
		}
		
		// fill CPT of main node with deterministic max function. Iterate on columns (related to states of parents) of CPT
		for (int index1stCellInColumn = 0; index1stCellInColumn < table.tableSize(); index1stCellInColumn += mainNode.getStatesSize()) { 
			// index1stCellInColumn points to 1st cell of each column
			
			// extract what are the states of each parent at this column of CPT
			int[] statesOfCurrentColumn = table.getMultidimensionalCoord(index1stCellInColumn);
			if (statesOfCurrentColumn[0] != 0) {
				throw new RuntimeException("We are supposed to be iterating on the 1st row of the curren column, but was row " + statesOfCurrentColumn[0]);
			}
			
			// in max function, the cell of maximum state will get 100%, and all others will be 0%.
			int maxState = 0;
			for (int state : statesOfCurrentColumn) {
				if (state > maxState) {
					maxState = state;
				}
			}
			
			// point to the row of maximum state
			statesOfCurrentColumn[0] = maxState;
			
			// note: all rows were initialized to 0%, so we only need to overwrite 1 of them to 100%
			table.setValue(statesOfCurrentColumn, 1f);
		}
		
		
		// cpt of all divorcing nodes and main node are the same (deterministic max with 2 parents, all nodes with same number of states)
		for (INode divorcing : divorcingNodes) {
			if (divorcing instanceof ProbabilisticNode) {
				// just copy the table of main node to this node
				((ProbabilisticNode) divorcing).getProbabilityFunction().setValues(table.getValues());
			}
		}
		
	}

	/**
	 * @param name : name of new node. Numbers will be appended if name is not unique in network
	 * @param x : position of new node. If null, will be placed near mainNode.
	 * @param y : position of new node. If null, will be placed near mainNode.
	 * @param mainNode : the states of new node will be the same of this node.
	 * @param network : the new node will be added to this network. It is also used to check
	 * uniqueness of name.
	 * @return  a new probabilistic node with specified name, position.
	 */
	protected ProbabilisticNode addNewNodeToNet(String name, Double x, Double y, ProbabilisticNode mainNode, ProbabilisticNetwork network) {
		
		ProbabilisticNode newNode = new ProbabilisticNode();
		
		Random random = new Random();
		if (x == null) {
			// randomly place it close to main node
			x =  mainNode.getPosition().getX() + (random.nextBoolean()?-1:1)*random.nextDouble()*200; 
		}
		if (y == null) {
			// randomly place it close to main node
			y =  mainNode.getPosition().getY() + (random.nextBoolean()?-1:1)*random.nextDouble()*200;
		}
		random = null;	// do not use it anymore
		
		newNode.setPosition(x,y);
		
		// node has same states of mainNode (child)
		for (int i = 0; i < mainNode.getStatesSize(); i++) {
			newNode.appendState(mainNode.getStateAt(i));
		}
		
		// make sure name is unique
		if (network.getNodeIndex(name) >= 0) {
			// find an suffix that does not exist in network yet
			int i = 1;
			for (; network.getNodeIndex(name+"_"+i) >= 0; i++) {}
			name += "_"+i;
		}
		newNode.setName(name);
		newNode.setDescription(name);
		
		// initialize potential table too
		newNode.getProbabilityFunction().addVariable(newNode);
		
		// don't forget to include node into network
		network.addNode(newNode);
		
		return newNode;
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.ICINodeFactorizationHandler#undo()
	 */
	public void undo() {
		
		// all nodes and nets changed by this object were supposedly stored in getNetworkBackup()
		for (Entry<INode, Graph> entry : getNetworkBackup().entrySet()) {
			
			// delete all divorcing nodes
			List<INode> nodes = getDivorcingNodesBackup().get(entry.getKey());
			if (nodes != null) {
				for (INode node : nodes) {
					// this will also remove the edges to/from this node
					try {
						entry.getValue().removeNode((Node) node);
					} catch (ClassCastException e) {
						Debug.println(getClass(), "Could not delete node " + node, e);
					}
				}
			}
			
			// delete all leak nodes
			nodes = getLeakBackup().get(entry.getKey());
			if (nodes != null) {
				for (INode node : nodes) {
					// this will also remove the edges to/from this node
					try {
						entry.getValue().removeNode((Node) node);
					} catch (ClassCastException e) {
						Debug.println(getClass(), "Could not delete node " + node, e);
					}
				}
			}
			
			// extract cpt of main node in order to reconnect parents and restore probability
			PotentialTable backupTable = getTableBackups().get(entry.getKey());
			
			if (backupTable != null) {
				// reconnect parents and main node (parents must be connected in the same order)
				for (int i = 1; i < backupTable.getVariablesSize(); i++) {	// start from 1, because 0 is the main node itself
					
					Node parent = (Node) backupTable.getVariableAt(i);
//					if (entry.getValue() instanceof Network) {
//						// make sure the instance is the same of the one in current network
//						parent = ((Network) entry.getValue()).getNode(parent.getName());
//					}
					try {
						entry.getValue().addEdge(new Edge((Node)parent , (Node)entry.getKey()));
					} catch (Exception e) {
						Debug.println(getClass(), "Could not reconnect " + parent + " and " + entry.getKey(), e);
					}
				}
				
				// restore content of CPT of main node
				if (entry.getKey() instanceof ProbabilisticNode) {
					((ProbabilisticNode) entry.getKey()).getProbabilityFunction().setValues(backupTable.getValues());
				}
			}
			
		}
		
	}



	/**
	 * @return the backups of the cpts of {@link #getNode()}
	 * used in {@link #treatICI(INode, Graph)}.
	 * This will also lazily instantiate map if null.
	 */
	public Map<INode, PotentialTable> getTableBackups() {
		if (tableBackups == null) {
			tableBackups = new HashMap<INode, PotentialTable>();
		}
		return tableBackups;
	}

	/**
	 * @param tableBackups: the backups of the cpts of {@link #getNode()}
	 * used in {@link #treatICI(INode, Graph)}.
	 */
	protected void setTableBackups(Map<INode, PotentialTable> tableBackups) {
		if (tableBackups == null) {
			tableBackups = new HashMap<INode, PotentialTable>();
		}
		this.tableBackups = tableBackups;
	}

//	/**
//	 * @return backup of what nodes were direct parents of {@link #getNode()}
//	 */
//	public Map<INode, List<INode>> getParentsBackup() {
//		if (parentsBackup == null) {
//			parentsBackup = new HashMap<INode, List<INode>>();
//		}
//		return parentsBackup;
//	}
//
//	/**
//	 * @param parentsBackup: backup of what nodes were direct parents of {@link #getNode()}
//	 */
//	protected void setParentsBackup(Map<INode, List<INode>> parentsBackup) {
//		this.parentsBackup = parentsBackup;
//	}

	/**
	 * @return a backup of what network was used in {@link #treatICI(INode, Graph)}
	 */
	public Map<INode, Graph> getNetworkBackup() {
		if (networkBackup == null) {
			networkBackup = new HashMap<INode, Graph>();
		}
		return networkBackup;
	}

	/**
	 * @param networkBackup: a backup of what network was used in {@link #treatICI(INode, Graph)}
	 */
	protected void setNetworkBackup(HashMap<INode, Graph> networkBackup) {
		this.networkBackup = networkBackup;
	}

	/**
	 * @return the leak nodes generated at {@link #treatICI(INode, Graph)}.
	 * Will lazily instantiate it if null.
	 */
	public Map<INode, List<INode>> getLeakBackup() {
		if (leakBackup == null) {
			leakBackup = new HashMap<INode, List<INode>>();
		}
		return leakBackup;
	}

	/**
	 * @param leakBackup the leak nodes generated at {@link #treatICI(INode, Graph)}.
	 * {@link #getLeakBackup()} will lazily instantiate it if null.
	 */
	protected void setLeakBackup(Map<INode, List<INode>> leakBackup) {
		this.leakBackup = leakBackup;
	}

	/**
	 * @return the divorcing nodes generated at {@link #treatICI(INode, Graph)}.
	 * Will lazily instantiate it if null.
	 */
	public Map<INode, List<INode>> getDivorcingNodesBackup() {
		if (divorcingNodesBackup == null) {
			divorcingNodesBackup = new HashMap<INode, List<INode>>();
		}
		return divorcingNodesBackup;
	}

	/**
	 * @param divorcingNodesBackup the divorcing nodes generated at {@link #treatICI(INode, Graph)}.
	 * {@link #getDivorcingNodesBackup()} will lazily instantiate it if null.
	 */
	protected void setDivorcingNodesBackup(Map<INode, List<INode>> divorcingNodesBackup) {
		this.divorcingNodesBackup = divorcingNodesBackup;
	}

}
