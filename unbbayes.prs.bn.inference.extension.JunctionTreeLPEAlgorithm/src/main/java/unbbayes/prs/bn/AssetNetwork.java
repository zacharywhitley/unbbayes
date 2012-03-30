/**
 * 
 */
package unbbayes.prs.bn;

import unbbayes.prs.Edge;
import unbbayes.prs.Node;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.util.Debug;


/**
 * This class represents a network of assets instead of probabilities.
 * It is not conceptually a probabilistic network, but it is extending 
 * {@link ProbabilisticNetwork} just for class compatibility (i.e.
 * just to make sure algorithms like {@link unbbayes.prs.bn.inference.extension.JunctionTreeLPEAlgorithm}
 * works for assets as well).
 * 
 * @author Shou Matsumoto
 *
 */
public class AssetNetwork extends ProbabilisticNetwork {
	
	private ProbabilisticNetwork relatedNetwork;

	/**
	 * Default constructor is protected to allow inheritance.
	 * Use {@link #getInstance()} to instantiate.
	 */
	protected AssetNetwork() {
		super("Asset Network");
	}
	
	/**
	 * Default constructor method initializing network.
	 * This method calls {@link #setRelatedNetwork(ProbabilisticNetwork)}, which instantiates asset nodes and edges.
	 * @param relatedNetwork : probabilistic network containing the probability distribution of this asset network
	 * @param nodesToIgnore 
	 * @return a network of assets having the same structure of relatedNetwork
	 * @throws InvalidParentException : if relatedNetwork contains invalid edges
	 */
	public static AssetNetwork getInstance(ProbabilisticNetwork relatedNetwork) throws InvalidParentException {
		AssetNetwork ret = new AssetNetwork();
		ret.setRelatedNetwork(relatedNetwork);
		ret.setName("Asssets of " + relatedNetwork.getName());
		return ret;
	}

	/**
	 * @return the relatedNetwork
	 */
	public ProbabilisticNetwork getRelatedNetwork() {
		return relatedNetwork;
	}

	/**
	 * This method sets the relatedNetwork (probabilistic network w/ the probabilities of the assets of this network)
	 * and instantiates asset nodes and edges according to nodes/edges in relatedNetwork. It only considers instances of {@link ProbabilisticNode}
	 * in the relatedNetwork. It does not copy cliques/separators, because some algorithms may not use cliques/separators.
	 * @param relatedNetwork the relatedNetwork to set
	 * @param nodesToIgnore 
	 * @throws InvalidParentException if relatedNetwork contains invalid edges
	 */
	public void setRelatedNetwork(ProbabilisticNetwork relatedNetwork) throws InvalidParentException {
		if (this.relatedNetwork == null
				|| !this.relatedNetwork.equals(relatedNetwork)) {
//			// stores which node has originated the corresponding asset node
//			Map<Node, AssetNode> nodeToAssetMap = new HashMap<Node, AssetNode>();
			
			// clear old nodes and edges
			this.clearNodes();
			this.getNodeIndexes().clear();
			this.clearEdges();
			
			// copy nodes
			for (Node aux : relatedNetwork.getNodes()) {
				if (aux instanceof ProbabilisticNode) {
					ProbabilisticNode node = (ProbabilisticNode) aux;
					// only consider probabilistic nodes
					AssetNode assetNode = AssetNode.getInstance();
					assetNode.setName(node.getName());
					assetNode.setPosition(node.getPosition().getX(), node.getPosition().getY());
					// copy states
					for (int i = 0; i < node.getStatesSize(); i++) {
						assetNode.appendState(node.getStateAt(i));
					}
					this.addNode(assetNode);
//					nodeToAssetMap.put(node, assetNode);
				}
			}
			
			// copy edges
			for (Edge edge : relatedNetwork.getEdges()) {
//				if (nodeToAssetMap.containsKey(edge.getOriginNode()) && nodeToAssetMap.containsKey(edge.getDestinationNode())) {
//					// both nodes in the edge are mapped
//					Edge newEdge = new Edge(nodeToAssetMap.get(edge.getOriginNode()), nodeToAssetMap.get(edge.getDestinationNode()));
//					this.addEdge(newEdge);
//				}
				// the asset node and original node has the same name, so we can obtain the asset node by searching by name
				Node assetNodeOrig = this.getNode(edge.getOriginNode().getName());	
				Node assetNodeDest = this.getNode(edge.getDestinationNode().getName());
				if (assetNodeOrig != null && assetNodeDest != null) {
					Edge newEdge = new Edge(assetNodeOrig, assetNodeDest);
					this.addEdge(newEdge);
				} else {
					try {
						Debug.println(getClass(), "Could not create edge: " + assetNodeOrig + " -> " + assetNodeDest);
					} catch (Throwable t) {
						t.printStackTrace();
					}
				}
			}
		}
		this.relatedNetwork = relatedNetwork;
		
	}

	/**
	 * This method will always return false, because AssetNetwork will never contain
	 * utility nodes or decision nodes.
	 */
	public boolean isID() {
		return false;
	}


}
