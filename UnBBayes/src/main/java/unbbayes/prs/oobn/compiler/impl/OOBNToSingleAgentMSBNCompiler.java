/**
 * 
 */
package unbbayes.prs.oobn.compiler.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import unbbayes.gui.oobn.node.OOBNNodeGraphicalWrapper;
import unbbayes.prs.Edge;
import unbbayes.prs.Network;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ITabledVariable;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.msbn.AbstractMSBN;
import unbbayes.prs.msbn.SingleAgentMSBN;
import unbbayes.prs.msbn.SubNetwork;
import unbbayes.prs.oobn.IOOBNClass;
import unbbayes.prs.oobn.IOOBNNode;
import unbbayes.prs.oobn.IObjectOrientedBayesianNetwork;
import unbbayes.prs.oobn.compiler.IDisconnectedNetworkToMultipleSubnetworkConverter;
import unbbayes.prs.oobn.compiler.IOOBNCompiler;
import unbbayes.prs.oobn.impl.DefaultOOBNNode;
import unbbayes.util.Debug;

/**
 * @author Shou Matsumoto
 *
 */
public class OOBNToSingleAgentMSBNCompiler implements IOOBNCompiler {

	/**
	 * 
	 */
	private OOBNToSingleAgentMSBNCompiler() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Default construction method
	 * @return a new instance
	 */
	public static OOBNToSingleAgentMSBNCompiler newInstance() {
		return new OOBNToSingleAgentMSBNCompiler();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.oobn.compiler.IOOBNCompiler#compile(unbbayes.prs.oobn.IObjectOrientedBayesianNetwork, unbbayes.prs.oobn.IOOBNClass)
	 */
	public Network compile(IObjectOrientedBayesianNetwork oobn,
			IOOBNClass mainClass) {
				
		AbstractMSBN msbn = new SingleAgentMSBN(oobn.getTitle());	// MSBN to be returned
					
		// create an instance of mainClass
		IOOBNNode mainInstance = DefaultOOBNNode.newInstance();
		mainInstance.setParentClass(mainClass);
		mainInstance.setName(mainClass.getClassName());
		mainInstance.setType(IOOBNNode.TYPE_INSTANCE);
		
		// generate the networks using the main instance, no prefix and no mappings to upper input nodes (since there is no upper)
		Collection<Network> generatedNetworks = this.generateNetworksRecursively(mainInstance, "", new HashMap<String, ProbabilisticNode>());
		
		// obtain network fragmenter
		IDisconnectedNetworkToMultipleSubnetworkConverter fragmenter = DisconnectedNetworkToMultipleSubnetworkConverterImpl.newInstance();
		
		try {
			// add each class to MSBN (treating only those referenced by mainClass/mainInstance)
			for (Network newNetwork : generatedNetworks) {
				// if network is disconnected, create multiple sub-networks in order to make them connected
				for (SubNetwork subnetwork : fragmenter.generateSubnetworks(newNetwork)) {
					msbn.addNetwork(subnetwork);
				}			
			}
		} catch (Exception e) {
			String message  = "Could not convert " + oobn.getTitle() 
			 				+ " using " + mainClass.getNetwork().getName()
			 				+ " as main class";
//			Debug.println(this.getClass(), message, e);
			throw new RuntimeException(message , e);
		}
		
		
		
		return msbn;
	}
	
	/**
	 * This method visits in-depth the instances called by thisInstance and generates subnetworks from them, recursively.
	 * The initial call must set at least thisInstances' name and original class; also set prefix as "" and 
	 * inputInstanceToParentCloneMap as empty (not null).
	 * 
	 * @param thisInstance: instance node to be converted to a network
	 * @param prefix: name prefix. Should be used as classpath and should contain separator ("_")
	 * @param inputInstanceToParentCloneMap: maps a inputInstance.getOriginalNode().getName() to unique inputInstance.getParents(0)
	 * @return collection of generated networks
	 */
	protected Collection<Network> generateNetworksRecursively(IOOBNNode thisInstance, String prefix, Map<String,ProbabilisticNode> inputInstanceToParentCloneMap) {
		
		Collection<Network> ret = new ArrayList<Network>();
		
		Collection<IOOBNNode> usedInstances = new ArrayList<IOOBNNode>();	// traces what instances/classes are being called by this class
		
//		Collection<IOOBNNode> usedInputInstances = new ArrayList<IOOBNNode>(); // traces what instance input nodes are used within this class
		
		// traces what instance input nodes are used by this class (by name) and maps it to its parent's clone (which is added to new Network)
		Map<String, ProbabilisticNode>  newInputInstanceToParentCloneMap = new HashMap<String, ProbabilisticNode>();	
		
		
		// traces the generated clone node and its original (original[key] -> clone[object])
		Map<String, ProbabilisticNode> originalNameToCloneMap = new HashMap<String, ProbabilisticNode>();
		
		SubNetwork newNetwork = null;	// new network to add
		if (thisInstance.getName() != null && !thisInstance.getName().isEmpty()) {
			// if instance name was defined, use it as the new network's name
			newNetwork = new SubNetwork(thisInstance.getName());
		} else {
			// if no instance name was defined, use the class name (concat with prefix).
			try {
				newNetwork = new SubNetwork(prefix + thisInstance.getParentClass().getClassName());
			} catch (Exception e) {
				// if we cannot obtain even the classname, use "Main" as default name
				newNetwork = new SubNetwork("Main");
			}
		}
		
		// start filling nodes using the class' nodes
		for (IOOBNNode node : thisInstance.getParentClass().getAllNodes()) {
			if ( node.getType() == node.TYPE_INSTANCE_INPUT ) {
				// instance and instance input are not added to compiled network
				// but I'd like to trace it in order to know what instance inputs are being instanciated
//				usedInputInstances.add(node);
				// we do not map it to the instance input map now because we do not have the clone nodes yet
				continue;
			}
			if ( node.getType() == node.TYPE_INSTANCE ) {
				// instance and instance input are not added to compiled network
				// but i'd like to trace it in order to know what classes are called by this
				usedInstances.add(node);
				continue;
			}
			
			// if this code is reached, the node must be output (normal and instance), normal input or private
			
			// obtain the graphical node representation and clone it
			ProbabilisticNode probabilisticNode = (ProbabilisticNode)((ProbabilisticNode)thisInstance.getParentClass().getNetwork().getNode(node.getName())).clone();
			
			// map the original node to its clone node
			if (originalNameToCloneMap.put(node.getName(), probabilisticNode) != null) {
				Debug.println(this.getClass(), "There were more than one mapping from " + node.getName() + " to " + probabilisticNode.getName());
			}
			
			// if node is a input, convert its name to its caller's node (parent of the input node)
			// this is the process of merging (unifying) the input node with its parent (we may call this as instantiation of input node)
			if (node.getType() == node.TYPE_INPUT) {
				
				// the map contains key which is the instance input node's name, which must be prefix + node.getName()
				ProbabilisticNode parentClone = inputInstanceToParentCloneMap.get(prefix + node.getName());
				
				if (parentClone != null) {
					
					// since MSBN verifies node equality using names, setting the input name as the parent'name
					// should  be equivalent to set them as equal nodes (only at MSBN)
					probabilisticNode.setName(parentClone.getName());
					
					// by setting the CPT as uniform, the UnBBayes MSBN implementation behaves as the node has no potential at all
					// and thus, it behaves like it's using the CPT of a node defined in another subnetwork (another network section)
					// which in our case is in the upper instance node. This is what we want.
					float linearValue = 1f / probabilisticNode.getStatesSize();
					PotentialTable cpt = probabilisticNode.getPotentialTable();
					for (int i = 0; i < cpt.tableSize(); i++) {
						cpt.setValue(i, linearValue);	
					}
				} else {
					// no parent was found for the upper instance of this input node. 
					// This means that the input node must behave as it is an private node
					// simply add prefix to its name.
					probabilisticNode.setName(prefix + probabilisticNode.getName());
				}				
				
			} else {
				// this is either output or private node
				// simply add prefix to its name
				probabilisticNode.setName(prefix + probabilisticNode.getName());
				
				// since it may be a parent of an instance input node, let's register it
				for (IOOBNNode child : node.getOOBNChildren()) {
					if (child.getType() == child.TYPE_INSTANCE_INPUT) {
						// we use the prefix + name in order to support multiple nested namespace levels
						newInputInstanceToParentCloneMap.put(prefix + child.getName(), probabilisticNode);
						// despite we assume a node is parent of only one instance input, it may be changed later,
						// so, I'm not braking the operation
					}
				}
			}
			
			// add it to new network
			newNetwork.addNode(probabilisticNode);
		}
		
		// by now, we finished adding nodes and registering the input instantiation (using newinputInstanceToParentCloneMap)
		
		// since we've got the nodes, start filling edges
		// by running original network's edges and getting the clones using a map, we can do it
		for (Edge edge : thisInstance.getParentClass().getNetwork().getEdges()) {
			
			IOOBNNode originalParent = ((OOBNNodeGraphicalWrapper)edge.getOriginNode()).getWrappedNode();
			IOOBNNode originalChild = ((OOBNNodeGraphicalWrapper)edge.getDestinationNode()).getWrappedNode();
			
			ProbabilisticNode cloneParent = originalNameToCloneMap.get(originalParent.getName());
			ProbabilisticNode cloneChild = originalNameToCloneMap.get(originalChild.getName());
			
			// if both nodes are present in the new network as well, then a new edge must be created for new network
			if ( ( cloneParent != null ) && ( cloneChild != null ) ) {
				Edge newEdge = new Edge(cloneParent, cloneChild);
				// I do not want to use newNetwork.addEdge since that method adds the node as well.
				// so, I'm just forcing plane add.
				// This is dangerous, since getEdges might return a copy instead of a reference.
				newNetwork.getEdges().add(newEdge);
			}
			
		}
		
		// by now, we've got nodes and edges converted correctly,.
		
		
		// Unfortunately, the parents and CPT are still mapped to the original one. 
		// Let's use originalToCloneMap to re-map them.		
		for (Node cloneNode : newNetwork.getNodes()) {
			
			// stores the original parents and children (as new lists, since we want to clear them after this)
			List<Node> originalParents = new ArrayList<Node>(cloneNode.getParents());
			List<Node> originalChildren = new ArrayList<Node>(cloneNode.getChildren());
			
			// clear the adjacent nodes
			cloneNode.clearAdjacents();
			
			// clear the parents and children from the clone node
			cloneNode.getParents().clear();
			cloneNode.getChildren().clear();
			
			// Obtain CPT in order to re-map CPT variables
			PotentialTable cpt = null;
			if (cloneNode instanceof ProbabilisticNode) {
				cpt = ((ProbabilisticNode)cloneNode).getPotentialTable();
				// re-map the node itself before re-mapping parents
				cpt.setVariableAt(0, cloneNode);	// we assume the node itself was the 1st variable
			}
			
			// fill parents and children using the clones previously mapped by originalToCloneMap
			for (Node parent : originalParents) {
				Node nodeToAdd = originalNameToCloneMap.get(parent.getName());
				if (nodeToAdd != null) {
					cloneNode.getParents().add(nodeToAdd);
					
					// CPT is still pointing to original nodes, we want to remap them too
					if (cpt != null) {						
						cpt.setVariableAt(cpt.indexOfVariable(parent), nodeToAdd);
					}
				}
			}
			
			for (Node child : originalChildren) {
				Node nodeToAdd = originalNameToCloneMap.get(child.getName());
				if (nodeToAdd != null) {
					cloneNode.getChildren().add(originalNameToCloneMap.get(child.getName()));	
					
					// I assume no CPT remapping is needed for children
				}
			}
			
			// regenerate adjacent nodes
			cloneNode.makeAdjacents();
			
		}
		
		// By now, we've got correct nodes, edges and parent/children mapping
		
		
		
		
		
				
		// register the added network to the return (list of generated network)
		ret.add(newNetwork);
		
		// do recursively (and add them to the list of generated network)
		for (IOOBNNode instance : usedInstances) {
			ret.addAll(this.generateNetworksRecursively( instance
						  							   , prefix + instance.getName() + "_"
						  							   , newInputInstanceToParentCloneMap ) );
		}
		
		
		return ret;
	}
}
