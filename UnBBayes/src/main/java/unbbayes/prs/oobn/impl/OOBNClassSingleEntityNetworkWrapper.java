/**
 * 
 */
package unbbayes.prs.oobn.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import unbbayes.gui.HierarchicTree;
import unbbayes.prs.Edge;
import unbbayes.prs.Network;
import unbbayes.prs.Node;
import unbbayes.prs.bn.SingleEntityNetwork;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.prs.oobn.IOOBNClass;
import unbbayes.prs.oobn.IOOBNNode;
import unbbayes.prs.oobn.exception.OOBNException;

/**
 * @author Shou Matsumoto
 *
 */
public class OOBNClassSingleEntityNetworkWrapper extends DefaultOOBNClass {

	private SingleEntityNetwork wrapped = null;
	
	/**
	 * 
	 */
	protected OOBNClassSingleEntityNetworkWrapper(SingleEntityNetwork wrapped) {
		// TODO Auto-generated constructor stub
		super(wrapped.getName());
		this.wrapped = wrapped;
	}

	public static OOBNClassSingleEntityNetworkWrapper newInstance (SingleEntityNetwork wrapped) {
		return new OOBNClassSingleEntityNetworkWrapper(wrapped);
	}


	/* (non-Javadoc)
	 * @see unbbayes.prs.oobn.impl.DefaultOOBNClass#getNetwork()
	 */
	@Override
	public Network getNetwork() {
		return this.getWrapped();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.oobn.impl.DefaultOOBNClass#setClassName(java.lang.String)
	 */
	@Override
	public void setClassName(String name) throws OOBNException {
		this.getWrapped().setName(name);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.oobn.impl.DefaultOOBNClass#toString()
	 */
	@Override
	public String toString() {
		return this.wrapped.toString();
	}
	
	

	/* (non-Javadoc)
	 * @see unbbayes.prs.oobn.impl.DefaultOOBNClass#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		try{
			if (this.getWrapped() == null) {
				return (this == obj) || obj == null;
			}
			return (this == obj) || this.getWrapped().equals(obj);
		} catch (Exception e) {
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.oobn.impl.DefaultOOBNClass#getClassName()
	 */
	@Override
	public String getClassName() {
		return this.getWrapped().getName();
	}

	/**
	 * @return the wrapped
	 */
	public SingleEntityNetwork getWrapped() {
		return wrapped;
	}

	/**
	 * @param wrapped the wrapped to set
	 */
	public void setWrapped(SingleEntityNetwork wrapped) {
		this.wrapped = wrapped;
	}

	/**
	 * @param edge
	 * @throws InvalidParentException
	 * @see unbbayes.prs.Network#addEdge(unbbayes.prs.Edge)
	 */
	public void addEdge(Edge edge) throws InvalidParentException {
		wrapped.addEdge(edge);
	}

	/**
	 * @param no
	 * @see unbbayes.prs.Network#addNode(unbbayes.prs.Node)
	 */
	public void addNode(Node no) {
		wrapped.addNode(no);
	}

	/**
	 * @return
	 * @see unbbayes.prs.bn.SingleEntityNetwork#getDescriptionNodes()
	 */
	public ArrayList<Node> getDescriptionNodes() {
		return wrapped.getDescriptionNodes();
	}

	/**
	 * @param no1
	 * @param no2
	 * @return
	 * @see unbbayes.prs.Network#getEdge(unbbayes.prs.Node, unbbayes.prs.Node)
	 */
	public Edge getEdge(Node no1, Node no2) {
		return wrapped.getEdge(no1, no2);
	}

	/**
	 * @return
	 * @see unbbayes.prs.Network#getEdges()
	 */
	public List<Edge> getEdges() {
		return wrapped.getEdges();
	}

	/**
	 * @return
	 * @see unbbayes.prs.bn.SingleEntityNetwork#getExplanationNodes()
	 */
	public ArrayList<Node> getExplanationNodes() {
		return wrapped.getExplanationNodes();
	}

	/**
	 * @return
	 * @see unbbayes.prs.bn.SingleEntityNetwork#getHierarchicTree()
	 */
	public HierarchicTree getHierarchicTree() {
		return wrapped.getHierarchicTree();
	}

	/**
	 * @return
	 * @see unbbayes.prs.Network#getId()
	 */
	public String getId() {
		return wrapped.getId();
	}

	/**
	 * @return
	 * @see unbbayes.prs.bn.SingleEntityNetwork#getLog()
	 */
	public String getLog() {
		return wrapped.getLog();
	}

	/**
	 * @return
	 * @see unbbayes.prs.Network#getName()
	 */
	public String getName() {
		return wrapped.getName();
	}

	/**
	 * @param name
	 * @return
	 * @see unbbayes.prs.Network#getNode(java.lang.String)
	 */
	public Node getNode(String name) {
		return wrapped.getNode(name);
	}

	/**
	 * @param index
	 * @return
	 * @see unbbayes.prs.Network#getNodeAt(int)
	 */
	public Node getNodeAt(int index) {
		return wrapped.getNodeAt(index);
	}

	/**
	 * @return
	 * @see unbbayes.prs.Network#getNodeCount()
	 */
	public int getNodeCount() {
		return wrapped.getNodeCount();
	}

	/**
	 * @param name
	 * @return
	 * @see unbbayes.prs.Network#getNodeIndex(java.lang.String)
	 */
	public int getNodeIndex(String name) {
		return wrapped.getNodeIndex(name);
	}

	/**
	 * @return
	 * @see unbbayes.prs.Network#getNodes()
	 */
	public ArrayList<Node> getNodes() {
		return wrapped.getNodes();
	}

	/**
	 * @return
	 * @see unbbayes.prs.bn.SingleEntityNetwork#getNodesCopy()
	 */
	public ArrayList<Node> getNodesCopy() {
		return wrapped.getNodesCopy();
	}

	/**
	 * @return
	 * @see unbbayes.prs.bn.SingleEntityNetwork#getRadius()
	 */
	public double getRadius() {
		return wrapped.getRadius();
	}

	/**
	 * @param no1
	 * @param no2
	 * @return
	 * @see unbbayes.prs.Network#hasEdge(unbbayes.prs.Node, unbbayes.prs.Node)
	 */
	public int hasEdge(Node no1, Node no2) {
		return wrapped.hasEdge(no1, no2);
	}

	/**
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return wrapped.hashCode();
	}

	/**
	 * @throws Exception
	 * @see unbbayes.prs.bn.SingleEntityNetwork#initialize()
	 */
	public void initialize() throws Exception {
		wrapped.initialize();
	}

	/**
	 * @return
	 * @see unbbayes.prs.bn.SingleEntityNetwork#isCreateLog()
	 */
	public boolean isCreateLog() {
		return wrapped.isCreateLog();
	}

	/**
	 * @return
	 * @see unbbayes.prs.bn.SingleEntityNetwork#isID()
	 */
	public boolean isID() {
		return wrapped.isID();
	}

	/**
	 * @return
	 * @see unbbayes.prs.bn.SingleEntityNetwork#PET()
	 */
	public float PET() {
		return wrapped.PET();
	}

	/**
	 * @param arco
	 * @see unbbayes.prs.Network#removeEdge(unbbayes.prs.Edge)
	 */
	public void removeEdge(Edge arco) {
		wrapped.removeEdge(arco);
	}

	/**
	 * @param elemento
	 * @see unbbayes.prs.Network#removeNode(unbbayes.prs.Node)
	 */
	public void removeNode(Node elemento) {
		wrapped.removeNode(elemento);
	}

	/**
	 * 
	 * @see unbbayes.prs.bn.SingleEntityNetwork#resetEvidences()
	 */
	public void resetEvidences() {
		wrapped.resetEvidences();
	}

	/**
	 * @param createLog
	 * @see unbbayes.prs.bn.SingleEntityNetwork#setCreateLog(boolean)
	 */
	public void setCreateLog(boolean createLog) {
		wrapped.setCreateLog(createLog);
	}

	/**
	 * @param firstInitialization
	 * @see unbbayes.prs.bn.SingleEntityNetwork#setFirstInitialization(boolean)
	 */
	public void setFirstInitialization(boolean firstInitialization) {
		wrapped.setFirstInitialization(firstInitialization);
	}

	/**
	 * @param hierarchicTree
	 * @see unbbayes.prs.bn.SingleEntityNetwork#setHierarchicTree(unbbayes.gui.HierarchicTree)
	 */
	public void setHierarchicTree(HierarchicTree hierarchicTree) {
		wrapped.setHierarchicTree(hierarchicTree);
	}

	/**
	 * @param name
	 * @see unbbayes.prs.Network#setName(java.lang.String)
	 */
	public void setName(String name) {
		wrapped.setName(name);
	}

	/**
	 * @param radius
	 * @see unbbayes.prs.bn.SingleEntityNetwork#setRadius(double)
	 */
	public void setRadius(double radius) {
		wrapped.setRadius(radius);
	}

	/**
	 * @throws Exception
	 * @see unbbayes.prs.bn.SingleEntityNetwork#updateEvidences()
	 */
	public void updateEvidences() throws Exception {
		wrapped.updateEvidences();
	}
	
	
	
	
	

}
