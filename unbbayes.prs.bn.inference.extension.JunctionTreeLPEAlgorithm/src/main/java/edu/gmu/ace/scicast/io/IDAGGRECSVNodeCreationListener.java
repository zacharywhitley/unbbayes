/**
 * 
 */
package edu.gmu.ace.daggre.io;

import java.util.List;

import unbbayes.prs.Graph;
import unbbayes.prs.INode;

/**
 * This listener will be called by {@link DAGGRECSVToBNConverter#load(java.io.File)}
 * for each {@link #getNumberOfNewNodesToBeGeneratedBeforeCall()} quantities
 * of new nodes created.
 * @author Shou Matsumoto
 *
 */
public interface IDAGGRECSVNodeCreationListener {

	/**
	 * @param nodesQuantity : Quantity of new nodes to be created before calling {@link #doCommand(Graph, List)}
	 */
	public void setNumberOfNewNodesToBeGeneratedBeforeCall(long nodesQuantity);
	
	/**
	 * @return Quantity of new nodes to be created before calling {@link #doCommand(Graph, List)}
	 */
	public long getNumberOfNewNodesToBeGeneratedBeforeCall();
	
	/**
	 * This method will be called by {@link DAGGRECSVToBNConverter#load(java.io.File)}
	 * for each {@link #getNumberOfNewNodesToBeGeneratedBeforeCall()} new nodes
	 * created.
	 * @param graph	:	graph being updated
	 * @param lastNodesCreated : last {@link #getNumberOfNewNodesToBeGeneratedBeforeCall()}
	 * nodes created by by {@link DAGGRECSVToBNConverter#load(java.io.File)}.
	 */
	public void doCommand(Graph graph, List<INode> lastNodesCreated);
	
}
