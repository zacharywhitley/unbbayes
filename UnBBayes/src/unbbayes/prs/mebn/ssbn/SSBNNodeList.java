/**
 * 
 */
package unbbayes.prs.mebn.ssbn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import unbbayes.prs.mebn.OrdinaryVariable;

/**
 * @author Shou Matsumoto
 *
 */
public class SSBNNodeList implements Collection<SSBNNode> {

	
	private List<SSBNNode> nodes = null;
	
	/**
	 * 
	 */
	public SSBNNodeList() {
		nodes = new ArrayList<SSBNNode>();
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#add(java.lang.Object)
	 */
	public boolean add(SSBNNode arg0) {
		// TODO Auto-generated method stub
		return nodes.add(arg0);
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#addAll(java.util.Collection)
	 */
	public boolean addAll(Collection<? extends SSBNNode> arg0) {
		// TODO Auto-generated method stub
		return nodes.addAll(arg0);
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#clear()
	 */
	public void clear() {
		// TODO Auto-generated method stub
		nodes.clear();
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#contains(java.lang.Object)
	 */
	public boolean contains(Object arg0) {
		// TODO Auto-generated method stub
		return nodes.contains(arg0);
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#containsAll(java.util.Collection)
	 */
	public boolean containsAll(Collection<?> arg0) {
		// TODO Auto-generated method stub
		return nodes.containsAll(arg0);
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#isEmpty()
	 */
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return nodes.isEmpty();
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#iterator()
	 */
	public Iterator<SSBNNode> iterator() {
		// TODO Auto-generated method stub
		return nodes.iterator();
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#remove(java.lang.Object)
	 */
	public boolean remove(Object arg0) {
		// TODO Auto-generated method stub
		return nodes.remove(arg0);
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#removeAll(java.util.Collection)
	 */
	public boolean removeAll(Collection<?> arg0) {
		// TODO Auto-generated method stub
		return nodes.removeAll(arg0);
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#retainAll(java.util.Collection)
	 */
	public boolean retainAll(Collection<?> arg0) {
		// TODO Auto-generated method stub
		return nodes.retainAll(arg0);
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#size()
	 */
	public int size() {
		// TODO Auto-generated method stub
		return nodes.size();
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#toArray()
	 */
	public Object[] toArray() {
		// TODO Auto-generated method stub
		return nodes.toArray();
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#toArray(T[])
	 */
	public <T> T[] toArray(T[] arg0) {
		// TODO Auto-generated method stub
		return nodes.toArray(arg0);
	}
	
	
	
	// Exported methods
	
	/**
	 * @param instanceName: Entity Instance name (e.g. !ST, !Z0, etc)
	 * @return nodes containing all those instance names. The size would be 0 if theres no such node.
	 */
	public Collection<SSBNNode> getNodeByArgument(String...instanceName) {
		Collection<SSBNNode> ret = new ArrayList<SSBNNode>();
		for (SSBNNode node : this.nodes) {
			if (node.hasAllOVs(false, instanceName)) {
				ret.add(node);
			}
		}
		return ret;
	}
	
	
	/**
	 * @param ovs: Ordinal Variables
	 * @return nodes containing all those instance names. The size would be 0 if theres no such node.
	 */
	public Collection<SSBNNode> getNodeByArgument(Collection<OrdinaryVariable> ov) {
		Collection<SSBNNode> ret = new ArrayList<SSBNNode>();
		for (SSBNNode node : this.nodes) {
			if (node.hasAllOVs(ov)) {
				ret.add(node);
			}
		}
		return ret;
	}
	
	/**
	 * 
	 * @param name name of a node to find
	 * @return all nodes w/ specified name. Size = 0 if no node was found.
	 */
	public Collection<SSBNNode> getNode(String name) {
		Collection<SSBNNode> ret = new ArrayList<SSBNNode>();
		for (SSBNNode node : this.nodes) {
			if (node.getName().compareToIgnoreCase(name) == 0) {
				ret.add(node);
			}
		}
		return ret;
	}
	
	
	

}
