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
package unbbayes.prs;

import java.util.List;

/** 
 * Interface for a graph constituting of Nodes and Edges
 */
public interface Graph {

		/**
		 *  This is a list of edges in this graph
		 *
		 *@return    edgeList .
		 */
		public List<Edge> getEdges();

		/**
		 *  The nodes in this graph.
		 *@return   nodes
		 */
		public List<Node> getNodes();

		/**
		 *  This is the quantity of nodes in this graph.
		 *  @see List#size()
		 *@return    quantity of nodes in this graph.
		 */
		public int getNodeCount();


		/**
		 *  Removes the specified edge from graph
		 *
		 *@param edge : edge/arc to be removed.
		 */
		public void removeEdge(Edge edge) ;

		/**
		 *  Add node to graph.
		 *  This method is supposedly safer than calling
		 *  {@link #getNodes()} and then {@link List#add(Object)}.
		 * @param  node  : node to be inserted.
		 */
		public void addNode(Node node);

		/**
		 *  Inserts edge/arc to graph
		 *@param  arc : edge/arc to be inserted
		 */
		public void addEdge(Edge arc) throws Exception;

		/**
		 *  Remove n� do grafo.
		 *
		 *@param  elemento  no a ser removido.
		 */
		public void removeNode(Node elemento);

		/**
		 * Checks whether an arc/edge connecting two nodes exists.
		 * @param  node1 : origin.
		 * @param  node2  : destination.
		 * @return position of the inserted arc in {@link #getEdges()}, or a negative
		 * value if edge is not present.
		 */
		public int hasEdge(Node node1, Node node2);
		
		/**
		 * This method can be used to represent a generic attribute.
		 * @param name
		 * @param value
		 * @see #removeProperty(String)
		 * @see #clearProperty()
		 * @see #getProperty(String)
		 */
		public void addProperty(String name, Object value);
		
		/**
		 * Remove a generic property by name.
		 * @param name
		 * @see #addProperty(String, Object)
		 * @see #clearProperty()
		 * @see #getProperty(String)
		 */
		public void removeProperty(String name);
		
		/**
		 * Clear all the generic attributes.
		 * @see #addProperty(String, Object)
		 * @see #removeProperty(String)
		 * @see #getProperty(String)
		 */
		public void clearProperty();
		
		/**
		 * This method can be used to represent a generic attribute.
		 * The behavior when name == null is unspecified.
		 * @param name
		 * @return
		 * @see #addProperty(String, Object)
		 * @see #removeProperty(String)
		 * @see #clearProperty()
		 */
		public Object getProperty(String name);

}
