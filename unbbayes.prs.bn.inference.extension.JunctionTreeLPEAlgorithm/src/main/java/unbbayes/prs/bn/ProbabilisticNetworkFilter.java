/**
 * 
 */
package unbbayes.prs.bn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import unbbayes.gui.HierarchicTree;
import unbbayes.io.NetworkCompilationLogManager;
import unbbayes.prs.Edge;
import unbbayes.prs.INode;
import unbbayes.prs.Node;
import unbbayes.prs.exception.InvalidParentException;

/**
 * This is just a ProbabilisticNetwork which ignores some nodes
 * @author Shou Matsumoto
 *
 */
public class ProbabilisticNetworkFilter extends ProbabilisticNetwork {

	private final ProbabilisticNetwork adaptedNetwork;
	private final Collection<INode> filteredNodes;

	/**
	 * @param adaptedNetwork : the network to be filtered
	 * @param filteredNodes : nodes to be ignored (filtered) in the filtered network
	 */
	public ProbabilisticNetworkFilter(ProbabilisticNetwork adaptedNetwork, Collection<INode> filteredNodes) {
		super(adaptedNetwork.getId());
		this.adaptedNetwork = adaptedNetwork;
		this.filteredNodes = filteredNodes;
	}

	/**
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return adaptedNetwork.hashCode();
	}

	/**
	 * @return
	 * @see unbbayes.prs.Network#getEdges()
	 */
	public List<Edge> getEdges() {
		List<Edge> ret = new ArrayList<Edge>();
		for (Edge edge : adaptedNetwork.getEdges()) {
			if (!filteredNodes.contains(edge.getOriginNode())
					&& !filteredNodes.contains(edge.getOriginNode())) {
				ret.add(edge);
			}
		}
		return ret;
	}

	/**
	 * @return
	 * @see unbbayes.prs.Network#getNodes()
	 */
	public ArrayList<Node> getNodes() {
		ArrayList<Node>  ret = new ArrayList<Node>(adaptedNetwork.getNodes());
		ret.removeAll(filteredNodes);
		return ret;
	}

	/**
	 * @return
	 * @see unbbayes.prs.Network#getNodeCount()
	 */
	public int getNodeCount() {
		return getNodes().size();
	}

	/**
	 * @param index
	 * @return
	 * @see unbbayes.prs.Network#getNodeAt(int)
	 */
	public Node getNodeAt(int index) {
		return getNodes().get(index);
	}

	/**
	 * @param obj
	 * @return
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		return super.equals(obj) || adaptedNetwork.equals(obj);
	}

	/**
	 * @param name
	 * @return
	 * @see unbbayes.prs.Network#getNode(java.lang.String)
	 */
	public Node getNode(String name) {
		Node node = adaptedNetwork.getNode(name);
		if (filteredNodes.contains(node)) {
			return null;
		} else {
			return node;
		}
	}

	/**
	 * @return
	 * @see unbbayes.prs.bn.SingleEntityNetwork#getDescriptionNodes()
	 */
	public ArrayList<Node> getDescriptionNodes() {
		ArrayList<Node> ret = new ArrayList<Node>(adaptedNetwork.getDescriptionNodes());
		ret.removeAll(filteredNodes);
		return ret;
	}

	/**
	 * @throws Exception
	 * @deprecated
	 * @see unbbayes.prs.bn.ProbabilisticNetwork#compile()
	 */
	public void compile() throws Exception {
		adaptedNetwork.compile();
	}

	/**
	 * @param name
	 * @return
	 * @see unbbayes.prs.Network#getNodeIndex(java.lang.String)
	 */
	public int getNodeIndex(String name) {
		if (this.getNode(name) == null) {
			return -1;
		}
		return adaptedNetwork.getNodeIndex(name);
	}

	/**
	 * @return
	 * @see unbbayes.prs.bn.SingleEntityNetwork#getExplanationNodes()
	 */
	public ArrayList<Node> getExplanationNodes() {
		ArrayList<Node>  ret = new ArrayList<Node>(adaptedNetwork.getExplanationNodes());
		ret.removeAll(filteredNodes);
		return ret;
	}

	/**
	 * @param edge
	 * @see unbbayes.prs.Network#removeEdge(unbbayes.prs.Edge)
	 */
	public void removeEdge(Edge edge) {
		adaptedNetwork.removeEdge(edge);
	}

	/**
	 * @param node
	 * @see unbbayes.prs.Network#addNode(unbbayes.prs.Node)
	 */
	public void addNode(Node node) {
		adaptedNetwork.addNode(node);
	}

	/**
	 * @return
	 * @see unbbayes.prs.bn.ProbabilisticNetwork#getJunctionTreeBuilder()
	 */
	public IJunctionTreeBuilder getJunctionTreeBuilder() {
		return adaptedNetwork.getJunctionTreeBuilder();
	}

	/**
	 * @param edge
	 * @throws InvalidParentException
	 * @see unbbayes.prs.Network#addEdge(unbbayes.prs.Edge)
	 */
	public void addEdge(Edge edge) throws InvalidParentException {
		adaptedNetwork.addEdge(edge);
	}

	/**
	 * @param junctionTreeBuilder
	 * @see unbbayes.prs.bn.ProbabilisticNetwork#setJunctionTreeBuilder(unbbayes.prs.bn.IJunctionTreeBuilder)
	 */
	public void setJunctionTreeBuilder(IJunctionTreeBuilder junctionTreeBuilder) {
		adaptedNetwork.setJunctionTreeBuilder(junctionTreeBuilder);
	}

	/**
	 * @param element
	 * @see unbbayes.prs.Network#removeNode(unbbayes.prs.Node)
	 */
	public void removeNode(Node element) {
		adaptedNetwork.removeNode(element);
	}

	/**
	 * @throws Exception
	 * @deprecated
	 * @see unbbayes.prs.bn.SingleEntityNetwork#verifyCycles()
	 */
	public void verifyCycles() throws Exception {
		adaptedNetwork.verifyCycles();
	}

	/**
	 * @return
	 * @see unbbayes.prs.bn.SingleEntityNetwork#hasCycle()
	 */
	public boolean hasCycle() {
		return adaptedNetwork.hasCycle();
	}

	/**
	 * @param no1
	 * @param no2
	 * @return
	 * @see unbbayes.prs.Network#hasEdge(unbbayes.prs.Node, unbbayes.prs.Node)
	 */
	public int hasEdge(Node no1, Node no2) {
		if (filteredNodes.contains(no1) || filteredNodes.contains(no2)) {
			return -1;
		}
		return adaptedNetwork.hasEdge(no1, no2);
	}

	/**
	 * @param no1
	 * @param no2
	 * @param vetArcos
	 * @return
	 * @see unbbayes.prs.Network#hasEdge(unbbayes.prs.Node, unbbayes.prs.Node, java.util.List)
	 */
	public int hasEdge(Node no1, Node no2, List<Edge> vetArcos) {
		if (filteredNodes.contains(no1) || filteredNodes.contains(no2)) {
			return -1;
		}
		return adaptedNetwork.hasEdge(no1, no2, vetArcos);
	}

	/**
	 * @throws Exception
	 * @deprecated
	 * @see unbbayes.prs.bn.SingleEntityNetwork#verifyConectivity()
	 */
	public void verifyConectivity() throws Exception {
		adaptedNetwork.verifyConectivity();
	}

	/**
	 * @param no1
	 * @param no2
	 * @return
	 * @see unbbayes.prs.Network#getEdge(unbbayes.prs.Node, unbbayes.prs.Node)
	 */
	public Edge getEdge(Node no1, Node no2) {
		if (filteredNodes.contains(no1) || filteredNodes.contains(no2)) {
			return null;
		}
		return adaptedNetwork.getEdge(no1, no2);
	}

	/**
	 * @return
	 * @see unbbayes.prs.bn.SingleEntityNetwork#isConnected()
	 */
	public boolean isConnected() {
		return adaptedNetwork.isConnected();
	}

	/**
	 * @param name
	 * @see unbbayes.prs.Network#setName(java.lang.String)
	 */
	public void setName(String name) {
		adaptedNetwork.setName(name);
	}

	/**
	 * @return
	 * @see unbbayes.prs.Network#getName()
	 */
	public String getName() {
		return adaptedNetwork.getName();
	}

	/**
	 * @return
	 * @see unbbayes.prs.Network#getId()
	 */
	public String getId() {
		return adaptedNetwork.getId();
	}

	/**
	 * @return
	 * @see unbbayes.prs.Network#toString()
	 */
	public String toString() {
		return adaptedNetwork.toString();
	}

	/**
	 * @return
	 * @see unbbayes.prs.Network#getNodeIndexes()
	 */
	public Map<String, Integer> getNodeIndexes() {
		Map<String, Integer> ret = new HashMap<String, Integer>(adaptedNetwork.getNodeIndexes());
		for (INode filtered : filteredNodes) {
			ret.remove(filtered.getName());
		}
		return ret;
	}

	/**
	 * @param nodeIndexes
	 * @see unbbayes.prs.Network#setNodeIndexes(java.util.Map)
	 */
	public void setNodeIndexes(Map<String, Integer> nodeIndexes) {
		adaptedNetwork.setNodeIndexes(nodeIndexes);
	}

	/**
	 * @param name
	 * @param value
	 * @see unbbayes.prs.Network#addProperty(java.lang.String, java.lang.Object)
	 */
	public void addProperty(String name, Object value) {
		adaptedNetwork.addProperty(name, value);
	}

	/**
	 * @param name
	 * @see unbbayes.prs.Network#removeProperty(java.lang.String)
	 */
	public void removeProperty(String name) {
		adaptedNetwork.removeProperty(name);
	}

	/**
	 * 
	 * @see unbbayes.prs.Network#clearProperty()
	 */
	public void clearProperty() {
		adaptedNetwork.clearProperty();
	}

	/**
	 * @param name
	 * @return
	 * @see unbbayes.prs.Network#getProperty(java.lang.String)
	 */
	public Object getProperty(String name) {
		return adaptedNetwork.getProperty(name);
	}

	/**
	 * @return
	 * @see unbbayes.prs.Network#getProperties()
	 */
	public Map<String, Object> getProperties() {
		return adaptedNetwork.getProperties();
	}

	/**
	 * @param properties
	 * @see unbbayes.prs.Network#setProperties(java.util.Map)
	 */
	public void setProperties(Map<String, Object> properties) {
		adaptedNetwork.setProperties(properties);
	}

	/**
	 * @param createLog
	 * @see unbbayes.prs.bn.SingleEntityNetwork#setCreateLog(boolean)
	 */
	public void setCreateLog(boolean createLog) {
		adaptedNetwork.setCreateLog(createLog);
	}

	/**
	 * 
	 * @see unbbayes.prs.bn.SingleEntityNetwork#resetEvidences()
	 */
	public void resetEvidences() {
		adaptedNetwork.resetEvidences();
	}

	/**
	 * 
	 * @see unbbayes.prs.bn.SingleEntityNetwork#resetLikelihoods()
	 */
	public void resetLikelihoods() {
		adaptedNetwork.resetLikelihoods();
	}

	/**
	 * @return
	 * @see unbbayes.prs.bn.SingleEntityNetwork#isID()
	 */
	public boolean isID() {
		return adaptedNetwork.isID();
	}

	/**
	 * @return
	 * @see unbbayes.prs.bn.SingleEntityNetwork#isHybridBN()
	 */
	public boolean isHybridBN() {
		return adaptedNetwork.isHybridBN();
	}

	/**
	 * @return
	 * @see unbbayes.prs.bn.SingleEntityNetwork#isBN()
	 */
	public boolean isBN() {
		return adaptedNetwork.isBN();
	}

	/**
	 * @return
	 * @see unbbayes.prs.bn.SingleEntityNetwork#getHierarchicTree()
	 */
	public HierarchicTree getHierarchicTree() {
		return adaptedNetwork.getHierarchicTree();
	}

	/**
	 * @param hierarchicTree
	 * @see unbbayes.prs.bn.SingleEntityNetwork#setHierarchicTree(unbbayes.gui.HierarchicTree)
	 */
	public void setHierarchicTree(HierarchicTree hierarchicTree) {
		adaptedNetwork.setHierarchicTree(hierarchicTree);
	}

	/**
	 * @param radius
	 * @see unbbayes.prs.bn.SingleEntityNetwork#setRadius(double)
	 */
	public void setRadius(double radius) {
		adaptedNetwork.setRadius(radius);
	}

	/**
	 * @return
	 * @see unbbayes.prs.bn.SingleEntityNetwork#getRadius()
	 */
	public double getRadius() {
		return adaptedNetwork.getRadius();
	}

	/**
	 * @return
	 * @see unbbayes.prs.bn.SingleEntityNetwork#getNodesCopy()
	 */
	public ArrayList<Node> getNodesCopy() {
		ArrayList<Node> ret = new ArrayList<Node>(adaptedNetwork.getNodesCopy());
		ret.removeAll(filteredNodes);
		return ret;
	}

	/**
	 * 
	 * @see unbbayes.prs.bn.SingleEntityNetwork#resetNodesCopy()
	 */
	public void resetNodesCopy() {
		adaptedNetwork.resetNodesCopy();
	}

	/**
	 * @return
	 * @see unbbayes.prs.bn.SingleEntityNetwork#getLog()
	 */
	public String getLog() {
		return adaptedNetwork.getLog();
	}

	/**
	 * @return
	 * @see unbbayes.prs.bn.SingleEntityNetwork#isCreateLog()
	 */
	public boolean isCreateLog() {
		return adaptedNetwork.isCreateLog();
	}

	/**
	 * @throws Exception
	 * @see unbbayes.prs.bn.SingleEntityNetwork#updateEvidences()
	 */
	public void updateEvidences() throws Exception {
		adaptedNetwork.updateEvidences();
	}

	/**
	 * @throws Exception
	 * @see unbbayes.prs.bn.SingleEntityNetwork#initialize()
	 */
	public void initialize() throws Exception {
		adaptedNetwork.initialize();
	}

	/**
	 * @param firstInitialization
	 * @see unbbayes.prs.bn.SingleEntityNetwork#setFirstInitialization(boolean)
	 */
	public void setFirstInitialization(boolean firstInitialization) {
		adaptedNetwork.setFirstInitialization(firstInitialization);
	}

	/**
	 * @return
	 * @see unbbayes.prs.bn.SingleEntityNetwork#PET()
	 */
	public float PET() {
		return adaptedNetwork.PET();
	}

	/**
	 * @return
	 * @see unbbayes.prs.bn.SingleEntityNetwork#getJunctionTree()
	 */
	public IJunctionTree getJunctionTree() {
		return adaptedNetwork.getJunctionTree();
	}

	/**
	 * @param junctionTree
	 * @see unbbayes.prs.bn.SingleEntityNetwork#setJunctionTree(unbbayes.prs.bn.IJunctionTree)
	 */
	public void setJunctionTree(IJunctionTree junctionTree) {
		adaptedNetwork.setJunctionTree(junctionTree);
	}

	/**
	 * @return
	 * @see unbbayes.prs.bn.SingleEntityNetwork#getLogManager()
	 */
	public NetworkCompilationLogManager getLogManager() {
		return adaptedNetwork.getLogManager();
	}

	/**
	 * @param logManager
	 * @see unbbayes.prs.bn.SingleEntityNetwork#setLogManager(unbbayes.io.NetworkCompilationLogManager)
	 */
	public void setLogManager(NetworkCompilationLogManager logManager) {
		adaptedNetwork.setLogManager(logManager);
	}

	/**
	 * @return
	 * @see unbbayes.prs.bn.SingleEntityNetwork#getNodeEliminationOrder()
	 */
	public ArrayList<Node> getNodeEliminationOrder() {
		ArrayList<Node> ret = new ArrayList<Node>(adaptedNetwork.getNodeEliminationOrder());
		ret.removeAll(filteredNodes);
		return ret;
	}

	/**
	 * @param nodeEliminationOrder
	 * @see unbbayes.prs.bn.SingleEntityNetwork#setNodeEliminationOrder(java.util.ArrayList)
	 */
	public void setNodeEliminationOrder(ArrayList<Node> nodeEliminationOrder) {
		adaptedNetwork.setNodeEliminationOrder(nodeEliminationOrder);
	}

}
