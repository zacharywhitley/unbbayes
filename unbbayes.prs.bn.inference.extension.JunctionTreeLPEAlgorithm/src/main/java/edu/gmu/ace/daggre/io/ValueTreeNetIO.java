package edu.gmu.ace.daggre.io;


import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StreamTokenizer;
import java.util.HashMap;
import java.util.Map;

import unbbayes.io.NetIO;
import unbbayes.io.exception.LoadException;
import unbbayes.prs.Edge;
import unbbayes.prs.Network;
import unbbayes.prs.Node;
import unbbayes.prs.bn.IRandomVariable;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.SingleEntityNetwork;
import unbbayes.prs.bn.valueTree.IValueTree;
import unbbayes.prs.bn.valueTree.IValueTreeNode;
import unbbayes.prs.bn.valueTree.ValueTreeNode;
import unbbayes.prs.bn.valueTree.ValueTreeProbabilisticNode;
import unbbayes.prs.builder.IProbabilisticNetworkBuilder;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.util.Debug;

/**
 * This is an I/O class for NET format which
 * is also able to store/load value trees.
 * @author Shou Matsumoto
 * @see IValueTreeNode
 * @see IValueTree
 */
public class ValueTreeNetIO extends NetIO {

	/**
	 * Name of property in {@link Network#getProperty(String)} which stores instances of {@link IValueTreeNode} loded by this class
	 */
	public static final String VALUE_TREE_MAP_PROPERTY_NAME = ValueTreeNetIO.class.getName()+"_Map";

//	/**
//	 * This is a temporary representation for converting the NET representation (which specifies parents for each node)
//	 * to value tree (which is easier to generate by specifying children for each node).
//	 * @author Shou Matsumoto
//	 * @see ValueTreeNetIO#load(File, SingleEntityNetwork, IProbabilisticNetworkBuilder)
//	 * @see ValueTreeNetIO#loadPotentialDeclaration(StreamTokenizer, SingleEntityNetwork)
//	 */
//	protected class ValueTreeMapper {
//		private Map<String, IValueTreeNode> instantiatedValueTreeNodes = new HashMap<String, IValueTreeNode>();
//		
//
//		/**
//		 * @return the instantiatedValueTreeNodes
//		 */
//		public Map<String, IValueTreeNode> getInstantiatedValueTreeNodes() {
//			return instantiatedValueTreeNodes;
//		}
//
//		/**
//		 * @param instantiatedValueTreeNodes the instantiatedValueTreeNodes to set
//		 */
//		public void setInstantiatedValueTreeNodes(
//				Map<String, IValueTreeNode> instantiatedValueTreeNodes) {
//			this.instantiatedValueTreeNodes = instantiatedValueTreeNodes;
//		}
//		
//	}
	

	/**
	 * Default constructor is kept public in order to allow
	 * plugin infrastructure to load it.
	 */
	public ValueTreeNetIO() {}
	
	
	
	/**
	 * This method simply intercepts the method of the superclass so that if %valueTreeRoot is found, then
	 * it generates a root of value tree, and if %valueTreeNode is found, then it generates a value tree node instead.
	 * @see unbbayes.io.NetIO#loadNodeDeclaration(java.io.StreamTokenizer, unbbayes.prs.bn.SingleEntityNetwork, unbbayes.prs.builder.IProbabilisticNetworkBuilder)
	 */
	protected void loadNodeDeclaration (StreamTokenizer st, SingleEntityNetwork net, IProbabilisticNetworkBuilder networkBuilder)
										throws IOException , LoadException{
		if (st.sval.equals("node")
			|| st.sval.equals("decision")
			|| st.sval.equals("utility")) {
			Node auxNode = null;
			if (st.sval.equals("node")) {
				auxNode = networkBuilder.getProbabilisticNodeBuilder().buildNode();
			} else if (st.sval.equals("decision")) {
				auxNode = networkBuilder.getDecisionNodeBuilder().buildNode();
			} else { // utility
				auxNode = networkBuilder.getUtilityNodeBuilder().buildNode();
			}

			getNext(st);
			auxNode.setName(st.sval.startsWith(getDefaultNodeNamePrefix())?st.sval.substring(getDefaultNodeNamePrefix().length()):st.sval);
			getNext(st);
			if (st.sval.equals("{")) {
				getNext(st);
				boolean isValueTreeNode = false;
				boolean isValueTreeRoot = false;
				while (!st.sval.equals("}")) {
					if (st.sval.equals("%valueTreeRoot")) {
						// this is a root of the value tree
						isValueTreeRoot = true;
						readTillEOL(st);
						getNext(st);
					} else if (st.sval.equals("%valueTreeNode")) {
						// this is a node in the value tree
						isValueTreeNode = true;
						readTillEOL(st);
						getNext(st);
					} else {
						// this is another type of node
						this.loadNodeDeclarationBody(st, auxNode);
					}
				}
				// do not add node if it was a value tree node
				if (!isValueTreeNode) {
					if (isValueTreeRoot) {
						// re-instantiate node as a IValueTreeProbabili
						ValueTreeProbabilisticNode vtNode = new ValueTreeProbabilisticNode();
						// copy information of auxNode to vtNode
						vtNode.setName(auxNode.getName());
						vtNode.setDescription(auxNode.getDescription());
						vtNode.setPosition(auxNode.getPosition().getX(), auxNode.getPosition().getY());
						for (int i = 0; i < auxNode.getStatesSize(); i++) {
							// copy the states too
							vtNode.appendState(auxNode.getStateAt(i));
						}
						// vtNode will be the one to be added to network
						auxNode = vtNode;
					}
					net.addNode(auxNode);
				} else {
					// this is a node in value tree
				}
			} else {
				throw new LoadException(
						" l."
						+ ((st.lineno() < this.lineno)?this.lineno:st.lineno()));
			}
		}
	}
	

	/**
	 * @see unbbayes.io.NetIO#loadPotentialDeclaration(java.io.StreamTokenizer, unbbayes.prs.bn.SingleEntityNetwork)
	 */
	protected void loadPotentialDeclaration(StreamTokenizer st, SingleEntityNetwork net) 
											throws IOException , LoadException {
		
		if (st.sval.equals("potential")) {
			
			PotentialTable auxPotentialTable = null;
			
			getNext(st);	
			// node name regarding prefix
			String childNodeName = st.sval.startsWith(getDefaultNodeNamePrefix())?st.sval.substring(getDefaultNodeNamePrefix().length()):st.sval;
			Node childNode = net.getNode(childNodeName);
			if (childNode != null) {
				if ((childNode instanceof IRandomVariable)) {
					auxPotentialTable = (PotentialTable)((IRandomVariable) childNode).getProbabilityFunction();
					if (!(childNode instanceof ValueTreeProbabilisticNode)) {
						// do not re-add variable if this is a root of value tree, because the constructor already does it.
						auxPotentialTable.addVariable(childNode);
					}
				}
			}  // if node was not found in network, it is probably a member of some value tree, so include it as a child of some ValueTreeProbabilisticNode

			getNext(st);
			if (st.sval.equals("|")) {
				getNext(st);	// parent names
			}

			Node parentNode;
			Edge auxArc;
			IValueTreeNode valueTreeChild = null;	// equivalent of childNode if it is a value tree node
			while (!st.sval.startsWith("{")) {
				String parentNodeName = st.sval.startsWith(getDefaultNodeNamePrefix())?st.sval.substring(getDefaultNodeNamePrefix().length()):st.sval;
				parentNode = net.getNode(parentNodeName);
				if (childNode != null) {
					// this is a normal BN node, and dependency is represented as an arc
					auxArc = new Edge(parentNode, childNode);
					try {
						net.addEdge(auxArc);
					} catch (InvalidParentException e) {
						throw new LoadException(e.getMessage());
					}
				} else  {
					// it is a value tree node
					if (parentNode != null) {
						if (parentNode instanceof ValueTreeProbabilisticNode) {
							// it is a child of the root
							// extract the value tree where we'll add the node
							IValueTree valueTree = ((ValueTreeProbabilisticNode)parentNode).getValueTree();
							Map<String, IValueTreeNode> map = ((Map)net.getProperty(VALUE_TREE_MAP_PROPERTY_NAME));
							if (map == null || !map.containsKey(childNodeName)) {
								// create the new node and add as child of root (null)
//								valueTreeChild = valueTree.addNode(childNodeName, null, 1f); 
								valueTreeChild = ValueTreeNode.getInstance(childNodeName, valueTree); 
								if (map == null) {
									// instantiate map if it was not present yet
									map = new HashMap<String, IValueTreeNode>();
								}
								// update mapping by inserting the newly created node
								map.put(childNodeName, valueTreeChild);
								net.getProperties().put(VALUE_TREE_MAP_PROPERTY_NAME, map);
							} else {
								// reuse the node which was instantiated previously
								valueTreeChild = map.get(childNodeName);
								valueTreeChild.setParent(null);
								valueTreeChild.setFaction(1f);
//								valueTreeChild.setValueTree(valueTree);
//								valueTree.addNode(valueTreeChild);
//								// this node was handled now, so no need to keep it in the map anymore
//								map.remove(childNodeName);
							}
							// propagate to children that the value tree is this
							this.setValueTreeRecursively(valueTreeChild,valueTree);
						} else {
							throw new IOException(parentNode + " is a parent of " + childNodeName + ", but " + childNodeName + " is an unknown type of node.");
						}
					} else {
						// it is a child of another value tree node
						// check mapping of instantiated, but not initialized tree value nodes
						Map<String, IValueTreeNode> map = ((Map)net.getProperty(VALUE_TREE_MAP_PROPERTY_NAME));
						if (map == null) {
							// instantiate mapping if it is not present yet
							map = new HashMap<String, IValueTreeNode>();
							net.getProperties().put(VALUE_TREE_MAP_PROPERTY_NAME, map);
						}
						IValueTreeNode valueTreeParent = map.get(parentNodeName);
						if (valueTreeParent == null) {
							// instantiate parent if it was not yet instantiated
							valueTreeParent = ValueTreeNode.getInstance(parentNodeName, null);
							map.put(parentNodeName, valueTreeParent);
						}
						valueTreeChild = map.get(childNodeName);
						if (valueTreeChild == null) {
							// instantiate child if it was not yet instantiated
							// also set the value tree to be the same of its parents
							valueTreeChild = ValueTreeNode.getInstance(childNodeName, valueTreeParent.getValueTree());
							map.put(childNodeName, valueTreeChild);
						}
						// connect child and parent
						valueTreeParent.getChildren().add(valueTreeChild);
						valueTreeChild.setParent(valueTreeParent);
						
					}
				}
				getNext(st);
			}
			
			/*
			 * Invert the parents in the table, to
			 * mantain consistency in the program.
			 * Internal pre-requisite.
			 */
			if (childNode != null && childNode instanceof IRandomVariable) {
				int sizeVetor = auxPotentialTable.variableCount() / 2;
				for (int k = 1; k <= sizeVetor; k++) {
					Object temp = auxPotentialTable.getVariableAt(k);
					auxPotentialTable.setVariableAt(
						k,
						auxPotentialTable.getVariableAt(
							auxPotentialTable.variableCount() - k));
					auxPotentialTable.setVariableAt(
						auxPotentialTable.variableCount() - k,
						(Node) temp);
				}
			}
			
			if (st.sval.length() == 1) {
				getNext(st);
			}
			
			if (st.sval.endsWith("}")) {
				// there were nothing declared
				Debug.println(this.getClass(), "Empty potential declaration found for " + childNode.getName());
			}

			while (!st.sval.endsWith("}")) {
				if (st.sval.equals("data")) {
					getNext(st);	// extract "normal"
					if (st.sval.equals("normal")) {
						// this is a continuous node
						this.loadPotentialDataContinuous(st, childNode);
					} else if (childNode == null) {
						// this is a value tree node (not a root), so simply fill faction
						this.loadPotentialDataValueTree(st, valueTreeChild);
					} else {
						// this is a ordinal node
						this.loadPotentialDataOrdinal(st, childNode);
					}
					
				} else {
					throw new LoadException(
							 " line "
							+ ((st.lineno() < this.lineno)?this.lineno:st.lineno()));
				}
			}
		}
	}
	
	/**
	 * Recursively call {@link IValueTreeNode#setValueTree(IValueTree)} to the
	 * provided node and its children
	 * @param valueTreeNode
	 * @param valueTreeToSet
	 */
	private void setValueTreeRecursively(IValueTreeNode valueTreeNode, IValueTree valueTreeToSet) {
		if (valueTreeNode == null) {
			return;
		}
		valueTreeNode.setValueTree(valueTreeToSet);
		// do recursive call to children
		if (valueTreeNode.getChildren() != null) {
			for (IValueTreeNode child : valueTreeNode.getChildren()) {
				setValueTreeRecursively(child, valueTreeToSet);
			}
		}
	}



	/**
	 * Loads potential declaration's content assuming it is declaring
	 * a value tree node
	 * @param st
	 * @param valueTreeChild
	 * @throws LoadException
	 * @throws IOException
	 */
	protected void loadPotentialDataValueTree(StreamTokenizer st, IValueTreeNode valueTreeChild) throws LoadException , IOException {
		
		// the 1st value in the table is supposedly the faction
		if (st.sval.equals("%")) {
			// ignore comments
			readTillEOL(st);
		} else {
			valueTreeChild.setFaction(Float.parseFloat(st.sval));
		}
		// read until end of table declaration
		getNext(st);
		while (!st.sval.equals("}")) {
			if (st.sval.equals("%")) {
				readTillEOL(st);
			} else {
				// ignore
			}
			getNext(st);
		}
	}



	/** 
	 * Simply clears the property {@link #VALUE_TREE_MAP_PROPERTY_NAME} of net after execution.
	 * @see unbbayes.io.NetIO#load(java.io.File, unbbayes.prs.bn.SingleEntityNetwork, unbbayes.prs.builder.IProbabilisticNetworkBuilder)
	 */
	protected void load(File input, SingleEntityNetwork net, IProbabilisticNetworkBuilder networkBuilder) throws IOException, LoadException {
		super.load(input, net, networkBuilder);
		// add all value tree nodes to its respective value trees
		Map<String, IValueTreeNode> map = (Map<String, IValueTreeNode>) net.getProperty(VALUE_TREE_MAP_PROPERTY_NAME);
		if (map != null) {
			// at this point, it is assumed that all values are pointing to the respective value tree
			for (IValueTreeNode valueTreeNode : map.values()) {
				// include it to the value tree, because at this point it is only pointing to a value tree, but the value tree is not pointing to it
				valueTreeNode.getValueTree().addNode(valueTreeNode);
			}
		}
		// clear the mapping of value tree nodes after load is finished.
		net.getProperties().remove(VALUE_TREE_MAP_PROPERTY_NAME);
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.NetIO#saveNodeDeclaration(java.io.PrintStream, unbbayes.prs.Node, unbbayes.prs.bn.SingleEntityNetwork)
	 */
	protected void saveNodeDeclaration(PrintStream stream, Node node, SingleEntityNetwork net) {
		super.saveNodeDeclaration(stream, node, net);
		
		// also save value tree nodes as normal nodes if this node is a value tree node
		if (node instanceof ValueTreeProbabilisticNode) {
			for (IValueTreeNode valueTreeNode : (((ValueTreeProbabilisticNode) node).getValueTree()).getNodes()) {
				stream.print("node");

				stream.println(" " + getDefaultNodeNamePrefix() + valueTreeNode.getName());
				stream.println("{");
				
				stream.println("     label = \"" + valueTreeNode.getName() +"\";");
				stream.println("     position = (" + (int)node.getPosition().getX() + " " + (int)node.getPosition().getY()  + ");");
				stream.println("     states = (\"faction\" \"stub\");");
				
				// mark as %valueTreeNode
				stream.println("     %valueTreeNode ");
				
				stream.println("}");
				stream.println();
				
			}
			
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.NetIO#saveNodeDeclarationBody(java.io.PrintStream, unbbayes.prs.Node, unbbayes.prs.bn.SingleEntityNetwork)
	 */
	protected void saveNodeDeclarationBody(PrintStream stream, Node node, SingleEntityNetwork net) {
		super.saveNodeDeclarationBody(stream, node, net);
		
		// also mark this node as %valueTreeRoot if this is a root of a value tree
		if (node instanceof ValueTreeProbabilisticNode) {
		      stream.println("     %valueTreeRoot ");
		}
	
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.NetIO#savePotentialDeclaration(java.io.PrintStream, unbbayes.prs.Node, unbbayes.prs.bn.SingleEntityNetwork)
	 */
	protected void savePotentialDeclaration(PrintStream stream, Node node, SingleEntityNetwork net) {
		super.savePotentialDeclaration(stream, node, net);
		
		// also print dummy potentials containing faction of value tree nodes
		if (node instanceof ValueTreeProbabilisticNode) {
			IValueTree valueTree = ((ValueTreeProbabilisticNode) node).getValueTree();
			for (IValueTreeNode valueTreeNode : valueTree.get1stLevelNodes()) {
				saveValueTreeDummyPotentialRecursively(stream, valueTreeNode);
			}
		}
		
	}

	/**
	 * Prints a dummy potential distribution for value tree node and its descendants.
	 * @param stream
	 * @param valueTreeNode
	 */
	private void saveValueTreeDummyPotentialRecursively(PrintStream stream, IValueTreeNode valueTreeNode) {
		// ignore null nodes
		if (valueTreeNode == null) {
			return;
		}
		
		// print a dummy potential distribution for value tree node
		
		stream.print("potential (" + getDefaultNodeNamePrefix() + valueTreeNode.getName());
		// the parent is the root node
		stream.print(" |");
		
		int parentStateSize = 1;	// this will hold how many states the parent has
		if (valueTreeNode.getParent() == null) {
			// this is a child of the root node
			stream.print(" " + getDefaultNodeNamePrefix() + valueTreeNode.getValueTree().getRoot().getName());
			parentStateSize = valueTreeNode.getValueTree().getRoot().getStatesSize();
		} else {
			// simply print the parent node
			stream.print(" " + getDefaultNodeNamePrefix() + valueTreeNode.getParent().getName());
			parentStateSize = 2;	// value tree will be stored as a dummy node with 2 values
		}
		
		stream.println(")");
		stream.println("{");
		

		stream.print(" data = (");
		
		// simply print prob distribution where 1st element is faction and 2nd is its complement
		for (int i = 0; i < parentStateSize; i++) {
			stream.println("( " + valueTreeNode.getFaction() + " " + (1-valueTreeNode.getFaction()) + " )");
		}
		
		stream.println(");");
		stream.println("}");
		stream.println();
		
		// recursively call children
		if (valueTreeNode.getChildren() != null) {
			for (IValueTreeNode child : valueTreeNode.getChildren()) {
				this.saveValueTreeDummyPotentialRecursively(stream, child);
			}
		}
	}
	

}
