/**
 *  UnBBayes
 *  Copyright (C) 2002, 2009 Universidade de Brasilia - http://www.unb.br
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
package unbbayes.util.dseparation.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;


import unbbayes.prs.Graph;
import unbbayes.prs.INode;
import unbbayes.util.Debug;
import unbbayes.util.dseparation.IDSeparationUtility;

/**
 * @author Shou Matsumoto
 * Checks d-separation criterion using moral separation (m-separation) criterion,
 * proposed by Lauritzen, Dawid, Larsen and Leimer (1990), which is proven to be
 * equivalent to d-separation criteria.
 * 
 * Basic steps (given sets X, Y and the separator S):
 * 
 * 1 - generate a graph containing only X, Y, Z and their ancestors.
 * 2 - turn the graph moral (connect/marry nodes having a common child)
 * 3 - eliminate edge orientation
 * 4 - if every single path connecting X to Y is blocked by S, then S d-separates X to Y
 * 
 */
public class MSeparationUtility implements IDSeparationUtility {

	
  	
	
	/**
	 * Default constructor.
	 * It's protected in order to simplify inheritance.
	 */
	protected MSeparationUtility() {
		super();		
	}
	
	/**
	 * Default constructor method.
	 * @return a new instance of MSeparationutility
	 */
	public static MSeparationUtility newInstance() {
		MSeparationUtility ret = new MSeparationUtility();
		return ret;
	}
	
	/**
  	 * Builds a map containing a temporally reference to all adjacent nodes, given a key node.
  	 * It is used to represent a undirected graph.
  	 * 
  	 * Mapping model:
  	 * 		node (key) --<<mapped to>>--> set of all adjacent nodes obtained by {@link Node#getAdjacents()}
  	 * 
  	 * It also guarantees that the map is "closed" (destination nodes are also keys). 
  	 * I.E. if "key" is mapped to "node", then "node" must be a key in this map too.
  	 * 
  	 * It does not go down/up recursively (so that it guarantees only the nodes within
  	 * the parameter is mapped).
  	 * 
  	 * @param allKeys
  	 * 
  	 * @see {@link Node#getAdjacents()}
  	 * @see {@link Node#makeAdjacents()}
  	 */
	public Map<INode, Set<INode>> buildClosedAdjacentNodeMap(Set<INode> allKeys) {
		Map<INode, Set<INode>> ret = new HashMap<INode, Set<INode>>();
		
		for (INode key : allKeys) {
			
			// build up the adjacent set
			Set<INode> adjacents = new HashSet<INode>();
			for (INode node : key.getAdjacentNodes()) {
				// make sure only those nodes within allKeys are mapped
				if (allKeys.contains(node)) {
					adjacents.add(node);
				}
			}
			ret.put(key, adjacents);			
		}
		return ret;
	}
	
	/**
	 * If a node has a common child, make them adjacent (marry them - this is something moral).
	 * 
  	 * It also guarantees that the map is "closed" (destination nodes are also keys). 
  	 * I.E. if "key" is mapped to "node", then "node" must be a key in this map too.
  	 * 
	 * @param closedAdjacentNodeMap : OBS. in/out parameter. 
	 * @return same as closedAdjacentNodeMap
	 */
	public Map<INode, Set<INode>> makeItMoral(Map<INode, Set<INode>> closedAdjacentNodeMap) {
		
		// for each key (all nodes inside this "closed" map), marry the parents
		for (INode key : closedAdjacentNodeMap.keySet()) {
			
			// obtain all parents (Note: they might not be mapped by closedAdjacentNodeMap)
			List<INode> parents = key.getParentNodes();
			
			// marries the parents 2-by-2
			for (int i = 0; i < parents.size() - 1; i++) {
				// extracts one of the parents and its mapping
				INode parent1 = parents.get(i);
				Set<INode> setForParent1 = closedAdjacentNodeMap.get(parent1);
				if (setForParent1 == null) {
					// if this parent is not mapped, no need to marry it (we must retain the map "closed")
					continue;
				}
				// get the other pair
				for (int j = i + 1; j < parents.size(); j++) {
					// extracts the other the parent and its mapping
					INode parent2 = parents.get(j);
					Set<INode> setForParent2 = closedAdjacentNodeMap.get(parent2);
					if (setForParent2 == null) {
						// if this parent is not mapped, no need to marry
						continue;
					}
					// we are sure now that both parents are mapped, so the map remains "closed"
					// make them adjacent each other
					setForParent1.add(parent2);
					setForParent2.add(parent1);
				}
			}
		}
		
		return closedAdjacentNodeMap;
	}
	
	/**
	 * Does the {@link #getRoutes(INode, INode, Map)} recursively.
	 * This is implemented here because I don't want public or protected methods to be recursive...
	 * Don't ask me why.
	 * Make sure from != to
	 * @param processedPath (input): registers the already visited nodes, in order to prevent cycle. Not null.
	 * @param nodesNotToContain : nodes that a returned path should not contain. The algorithm will ignore 
	 * It is concatenated to the return list, as a prefix.
	 */
	private Set<List<INode>> getRoutesRec(INode from, INode to, Map<INode, Set<INode>> closedAdjacentNodeMap, List<INode> processedPath, Set<INode> nodesNotToContain) {
		
		Set<List<INode>> ret = new HashSet<List<INode>>(); // Initialize the return value
		
		
		// mark the current node as "evaluated", but don't use processedPath directly, since we don't want it to be an output parameter
		List<INode> processingPath = new ArrayList<INode>(processedPath);
		processingPath.add(from);
		
		// initialize the set of adjacent nodes
		Set<INode> adjacentSet = null;
		if (closedAdjacentNodeMap != null) {
			// if we have a pre-defined adjacency map, use it
			adjacentSet = closedAdjacentNodeMap.get(from);
		} else {
			// since we don't have an adjacency map, assume as a directed graph (use only children).
			adjacentSet = new HashSet<INode>(from.getChildNodes());
		}
		
		
		for (INode adjacent : adjacentSet) {
			
			if (processingPath.contains(adjacent)) {
				// this is a cicle. Ignore this sub-path
				continue;
			}
			if (nodesNotToContain.contains(adjacent)) {
				// since no path should contain nodes within nodesNotToContain, we should ignore such adjacent nodes
				continue;
			}
			if (adjacent.equals(to)) {
				// path found!
				List<INode> path = new ArrayList<INode>(processingPath);
				path.add(adjacent);
				ret.add(path);
				continue;
			}
			
			// recursive call
			ret.addAll(this.getRoutesRec(adjacent, to, closedAdjacentNodeMap, processingPath, nodesNotToContain));
		}
		
		return ret;
	}
	
	/**
	 * Obtains a set of path/routes between two nodes, including themselves.
	 * Cycles are not counted as new routes.
	 * @param from : a node to start from
	 * @param to : a destination node
	 * @param closedAdjacentNodeMap : a map indicating all node's adjacency. 
	 * @param nodesNotToContain : nodes that a returned path should not contain. The algorithm will ignore
	 * a path if it contains any node within it.
	 * If set to null, this method will start using {@link INode#getChildren()} to build a directed path.
	 * @return : a set of all path from "from" to "to". The path is represented as a list containing all
	 * nodes included in the path. Since it is a list, it stores the visit order as well.
	 */
	public Set<List<INode>> getRoutes(INode from, INode to, Map<INode, Set<INode>> closedAdjacentNodeMap, Set<INode> nodesNotToContain) {
				
		// treating special case: auto-relationship ("from" is adjacent to itself and from == to)
		if (from.equals(to)) {
			
			Set<List<INode>> ret = new HashSet<List<INode>>();	// returning set
			
			// initialize the set of adjacent nodes
			Set<INode> adjacentSet = null;
			if (closedAdjacentNodeMap != null) {
				// if we have a pre-defined adjacency map, use it
				adjacentSet = closedAdjacentNodeMap.get(from);
			} else {
				// since we don't have an adjacency map, assume as a directed graph (use only children).
				adjacentSet = new HashSet<INode>(from.getChildNodes());
			}
			
			// if "from" was adjacent to itself, we shall consider from -> from a correct path
			if (adjacentSet.contains(to)) {
				List<INode> autoRelationPath = new ArrayList<INode>();
				autoRelationPath.add(from);
				autoRelationPath.add(to);				
				ret.add(autoRelationPath);
			} else {
				// just return an empty set below
			}
			
			return ret;	// we shall not start recursive call when from == to, ever.
		}
		
		// the method should not throw null pointer exception because of nodes to be ignored
		// so, initialize it
		if (nodesNotToContain == null) {
			nodesNotToContain = new HashSet<INode>();
		}
		
		// normal case: recursive call considering the "current path" as a empty list of nodes
		return this.getRoutesRec(from, to, closedAdjacentNodeMap, new ArrayList<INode>(), nodesNotToContain);		
	}
	
	
	/**
	 * Obtains a set of path/routes between two nodes, including themselves.
	 * Cycles are not counted as new routes.
	 * This is equals to {@link #getRoutesRec(INode, INode, Map, List, null)}
	 * @param from : a node to start from
	 * @param to : a destination node
	 * @param closedAdjacentNodeMap : a map indicating all node's adjacency. 
	 * a path if it contains any node within it.
	 * If set to null, this method will start using {@link INode#getChildren()} to build a directed path.
	 * @return : a set of all path from "from" to "to". The path is represented as a list containing all
	 * nodes included in the path. Since it is a list, it stores the visit order as well.
	 */
	public Set<List<INode>> getRoutes(INode from, INode to, Map<INode, Set<INode>> closedAdjacentNodeMap) {
		return this.getRoutes(from, to, closedAdjacentNodeMap, null);
	}
	
	/**
	 * Obtains a set of path/routes between two nodes, including themselves.
	 * This is the same as calling {@link #getRoutes(INode, INode, Map)} setting the Map as null.
	 * Cycles are not counted as new routes.
	 * @param from : a node to start from
	 * @param to : a destination node
	 * If set to null, this method will start using {@link INode#getChildren()} to build a directed path.
	 * @return : a set of all path from "from" to "to". The path is represented as a list containing all
	 * nodes included in the path. Since it is a list, it stores the visit order as well.
	 * @see #getRoutes(INode, INode, Map)
	 */
	public Set<List<INode>> getRoutes (INode from, INode to) {
		return this.getRoutes(from, to, null, null);
	}
	
	
	/**
	 * Obtains recursively all ancesters of a given node.
	 * I just don't want a public method to be recursive... Don't ask me why.
	 * @param node
	 * @return set of nodes
	 */
	private Set<INode> getAllAncestorsRec(INode node) {
		Set<INode> ret = new HashSet<INode>();		
		try {
			for (INode parent : node.getParentNodes()) {
				ret.add(parent);
				ret.addAll(this.getAllAncestorsRec(parent));
			}
		} catch (NullPointerException npe) {
			// we can assume that there is no parent...
			Debug.println(this.getClass(), npe.getMessage(), npe);
		}		
		return ret;
	}
	
	/**
	 * Method to get all ancestors of a given set of nodes.
	 * @param nodes : nodes to be analyzed.
	 * @return : a set of nodes
	 */
	public Set<INode> getAllAncestors(Set<INode> nodes) {
		Set<INode> ret = new HashSet<INode>();	
		for (INode node : nodes) {
			// searches recursivelly
			ret.addAll(this.getAllAncestorsRec(node));	
		}
		return ret;
	}
	
	/**
	 * Checks if container contains at least one element in contents.
	 * @param container
	 * @param content
	 * @return true if exists a element in contents which is in container. False otherwise.
	 */
	private boolean containsAtLeastOneOf(Collection container, Collection contents) {
		for (Object content : contents) {
			if (container.contains(content)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if the nodes within "from" are m-separated from nodes from "to", given a set
	 * of "separators".
	 * 
	 * Lauritzen, Dawid, Larsen and Leimer (1990), says that m-separation is equivalent to d-separation.
	 * 
	 * @param graph : NOT USED BY THIS IMPLEMENTATION (since it uses only the parents/children of the nodes)
	 * @param from : set 1 of nodes which m-separation is going to be tested
	 * @param to : set 2 of nodes which m-separation is going to be tested
	 * @param separators : set of separators.
	 * @return : true if "from" is m-separated with "to" given "separators". False otherwise.
	 */
	public boolean isDSeparated(Graph graph, Set<INode> from, Set<INode> to, Set<INode> separators) {
		
		// initial check
		if (from.isEmpty()) {
			return false;
		}
		if (to.isEmpty()) {
			return false;
		}
		
		// start m-separation algorithm
		
		// step 1 - use only nodes within "from", "to", "separators" and their ancestors.
		Set<INode> usedNodes = new HashSet<INode>();	// we assume a Set will prevent adding same nodes
		usedNodes.addAll(from);
		usedNodes.addAll(to);
		usedNodes.addAll(separators);
		usedNodes.addAll(this.getAllAncestors(from));
		usedNodes.addAll(this.getAllAncestors(to));
		usedNodes.addAll(this.getAllAncestors(separators));
		
		// step 2 & 3 - create a non-oriented representation of this graph (containing only usedNodes)
		Map<INode, Set<INode>> mapRepresentingNonOrientedGraph = this.buildClosedAdjacentNodeMap(usedNodes);
		
		// step 2 & 3 - make it moral: propose parents' marriage...
		mapRepresentingNonOrientedGraph = this.makeItMoral(mapRepresentingNonOrientedGraph);
		
		// step 4 - if each path between "from" and "to" are blocked by any "separator", the graph is m-separated
		// TODO optimize this, since this is becoming a very heavy procedure...
		for (INode nodeFrom : from) {
			for (INode nodeTo : to) {
				if (this.getRoutes(nodeFrom, nodeTo, mapRepresentingNonOrientedGraph, separators).size() > 0) {
					return false;
				}
			}
		}
		
		// all paths between "from" and "to" are blocked by "separators", so, it is m-separated
		return true;
	}
	
	

}
