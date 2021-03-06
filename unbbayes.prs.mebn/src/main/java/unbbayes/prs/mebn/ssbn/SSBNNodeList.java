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
	
	public SSBNNodeList() {
		nodes = new ArrayList<SSBNNode>();
	}

	// Exported methods
	
	/**
	 * @param instanceName: Entity Instance name (e.g. !ST, !Z0, etc)
	 * @return nodes containing all those instance names. The size would be 0 if theres no such node.
	 */
	public Collection<SSBNNode> getNodeByArgument(String...instanceName) {
		Collection<SSBNNode> ret = new ArrayList<SSBNNode>();
		if (instanceName == null) {
			return ret;
		}
		if (instanceName.length <= 0) {
			return ret;
		}
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
		if (ov == null) {
			return ret;
		}
		if (ov.size() <= 0) {
			return ret;
		}
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
		if (name == null) {
			return ret;
		}
		if (name.length() <= 0) {
			return ret;
		}
		for (SSBNNode node : this.nodes) {
			if (node.getName().equalsIgnoreCase(name)) {
				ret.add(node);
			}
		}
		return ret;
	}
	
	/* (non-Javadoc)
	 * @see java.util.Collection#add(java.lang.Object)
	 */
	public boolean add(SSBNNode arg0) {
		return nodes.add(arg0);
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#addAll(java.util.Collection)
	 */
	public boolean addAll(Collection<? extends SSBNNode> arg0) {
		return nodes.addAll(arg0);
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#clear()
	 */
	public void clear() {
		nodes.clear();
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#contains(java.lang.Object)
	 */
	public boolean contains(Object arg0) {
		return nodes.contains(arg0);
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#containsAll(java.util.Collection)
	 */
	public boolean containsAll(Collection<?> arg0) {
		return nodes.containsAll(arg0);
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#isEmpty()
	 */
	public boolean isEmpty(){
		return nodes.isEmpty();
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#iterator()
	 */
	public Iterator<SSBNNode> iterator() {
		return nodes.iterator();
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#remove(java.lang.Object)
	 */
	public boolean remove(Object arg0) {
		return nodes.remove(arg0);
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#removeAll(java.util.Collection)
	 */
	public boolean removeAll(Collection<?> arg0) {
		return nodes.removeAll(arg0);
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#retainAll(java.util.Collection)
	 */
	public boolean retainAll(Collection<?> arg0) {
		return nodes.retainAll(arg0);
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#size()
	 */
	public int size() {
		return nodes.size();
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#toArray()
	 */
	public Object[] toArray() {
		return nodes.toArray();
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#toArray(T[])
	 */
	public <T> T[] toArray(T[] arg0) {
		return nodes.toArray(arg0);
	}

}
