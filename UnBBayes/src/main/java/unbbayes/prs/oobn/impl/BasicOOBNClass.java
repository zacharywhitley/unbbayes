/**
 * 
 */
package unbbayes.prs.oobn.impl;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import unbbayes.gui.oobn.node.OOBNNodeGraphicalWrapper;
import unbbayes.prs.Edge;
import unbbayes.prs.Network;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ITabledVariable;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.SingleEntityNetwork;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.prs.oobn.IOOBNClass;
import unbbayes.prs.oobn.IOOBNNode;
import unbbayes.prs.oobn.exception.OOBNException;
import unbbayes.util.Debug;

/**
 * @author Shou Matsumoto
 *
 */
public class BasicOOBNClass extends SingleEntityNetwork implements IOOBNClass {

	
	/**
	 * @param name
	 */
	protected BasicOOBNClass(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Constructor method for BasicOOBNClass,
	 * a simplified implementation of IOOBNClass
	 * @param name: name/title of the oobn class
	 * @return a new instance of a oobn class
	 */
	public static BasicOOBNClass newInstance(String name) {
		return new BasicOOBNClass(name);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.oobn.IOOBNClass#getAllNodes()
	 */
	public Set<IOOBNNode> getAllNodes() {
		Set<IOOBNNode> ret = new HashSet<IOOBNNode>();
		try {
			for (Node node : this.getNodes()) {
				if (node instanceof OOBNNodeGraphicalWrapper) {
					ret.add(((OOBNNodeGraphicalWrapper)node).getWrappedNode());
				}
			}
		} catch (Exception e) {
			throw new java.lang.IllegalArgumentException(e); 
		}
		return ret;
		
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		try {
			return super.equals(obj) || this.getName().equals(((BasicOOBNClass)obj).getName());
		} catch (Exception e) {
			// if conversion is throwing an exception, we assume they are not "compatible",
			// so, they are not "equal"
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Network#toString()
	 */
	@Override
	public String toString() {
		return this.getName();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.oobn.IOOBNClass#getClassName()
	 */
	public String getClassName() {
		return this.getName();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.oobn.IOOBNClass#setClassName(java.lang.String)
	 */
	public void setClassName(String name) throws OOBNException {
		// TODO implement name consistency check
		Debug.println(this.getClass(), "Name consistency check is not implemented yet.");
		if (name.contains("!")) {
			throw new OOBNException();
		}
		this.setName(name);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.oobn.IOOBNClass#getNetwork()
	 */
	public Network getNetwork() {
		return this;
	}

	/* (non-Javadoc)
	 * @see java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer.DataFlavor)
	 */
	public IOOBNClass getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException {
		if (this.isDataFlavorSupported(flavor)) {
			// the transfer data is this class
			Debug.println(this.getClass(), "Returning " + this.getClassName() + " as transfer data");
			return this;
		}
		Debug.println(this.getClass(), "Flavor is not suported; " + flavor.toString());
		return null;
	}

	/* (non-Javadoc)
	 * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
	 */
	public DataFlavor[] getTransferDataFlavors() {
		try{
			DataFlavor [] ret = {new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType,this.getClassName())};
			return  ret;
		} catch (Exception e) {
			Debug.println(this.getClass(), "It was not possible to initialize Transferable DataFlavor", e);
		}
		return new DataFlavor[0];
	}

	/* (non-Javadoc)
	 * @see java.awt.datatransfer.Transferable#isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
	 */
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		// suggest to support any type
		return true;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.oobn.IOOBNClass#getInputNodes()
	 */
	public Set<IOOBNNode> getInputNodes() {
		Set<IOOBNNode> ret = new HashSet<IOOBNNode>();
		for (IOOBNNode node : this.getAllNodes()) {
			if (node.getType() == node.TYPE_INPUT) {
				ret.add(node);
			}
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.oobn.IOOBNClass#getOutputNodes()
	 */
	public Set<IOOBNNode> getOutputNodes() {
		Set<IOOBNNode> ret = new HashSet<IOOBNNode>();
		for (IOOBNNode node : this.getAllNodes()) {
			if (node.getType() == node.TYPE_OUTPUT) {
				ret.add(node);
			}
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Network#removeNode(unbbayes.prs.Node)
	 */
	@Override
	public void removeNode(Node element) {
		
		Debug.println(this.getClass(), "Removing node " + element.getName());
		
		// obtain wrapped IOOBNNode
		IOOBNNode removingNode = null;
		for (IOOBNNode node : this.getAllNodes()) {
			// I'm comparing element.equals instead of node.equals because I expect element
			// is a graphical wrapper of node (the graphical wrapper can treat IOOBNNode, bug
			// IOOBNNode cannot treat the graphical wrapper)
			if (element.equals(node)) {
				removingNode = node;
				break;
			}
		}
		
		
		
		// Update wrapped IOOBNNode
		if (removingNode != null) {
			
			// update children
			for (IOOBNNode child : removingNode.getOOBNChildren()) {
				child.getOOBNParents().remove(removingNode);
				Debug.println(this.getClass(), "Removing parent " + removingNode.getName() + " from " + child.getName());
			}
			// update parents
			for (IOOBNNode parent : removingNode.getOOBNParents()) {
				parent.getOOBNChildren().remove(removingNode);

				Debug.println(this.getClass(), "Removing child " + removingNode.getName() + " from " + parent.getName());
			}
		}
		
		// update the graphical wrapper as well
		super.removeNode(element);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Network#addEdge(unbbayes.prs.Edge)
	 */
	@Override
	public void addEdge(Edge edge) throws InvalidParentException {
		
		// I'm extending this method in order to make add child and add parent as
		// only one method (and make it transactional).
		
		edge.getDestinationNode().addParent(edge.getOriginNode());
	    edgeList.add(edge);
	    if (edge.getDestinationNode() instanceof ITabledVariable) {
			ITabledVariable v2 = (ITabledVariable) edge.getDestinationNode();
			PotentialTable auxTab = v2.getPotentialTable();
			auxTab.addVariable(edge.getOriginNode());
		}
	}

	

	
	
	
	
	
	
	

}
