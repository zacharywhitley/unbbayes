/**
 * 
 */
package edu.gmu.ace.scicast;

import java.io.Serializable;
import java.util.Map;

/**
 * This is a common interface for objects
 * containing statistics about the probabilistic network managed by
 * {@link MarkovEngineInterface}
 * @author Shou Matsumoto
 *
 */
public interface NetStatistics extends Serializable {

	/**
	 * This is a mapping from quantity of possible states of a node to quantity of nodes
	 * having such number of states. This is useful for creating a report of how many
	 * nodes the network contain, organized by quantity of states in the nodes.
	 * <br/>
	 * <br/>
	 * Example of such report:
	 * <br/>
	 * <br/>
	 * 2 states : 104 nodes;
	 * 3 states : 50 nodes;
	 * 4 states : 21 nodes;
	 * <br/>
	 * <br/>
	 * By summing the values, we can obtain the total quantity of nodes.
	 * @return the numberOfStatesToNumberOfNodesMap
	 */
	public Map<Integer, Integer> getNumberOfStatesToNumberOfNodesMap();

	/**
	 * This is a mapping from quantity of possible states of a node to quantity of nodes
	 * having such number of states. This is useful for creating a report of how many
	 * nodes the network contain, organized by quantity of states in the nodes.
	 * <br/>
	 * <br/>
	 * Example of such report:
	 * <br/>
	 * <br/>
	 * 2 states : 104 nodes;
	 * 3 states : 50 nodes;
	 * 4 states : 21 nodes;
	 * <br/>
	 * <br/>
	 * By summing the values, we can obtain the total quantity of nodes.
	 * @param numberOfStatesToNumberOfNodesMap the numberOfStatesToNumberOfNodesMap to set
	 */
	public void setNumberOfStatesToNumberOfNodesMap( Map<Integer, Integer> numberOfStatesToNumberOfNodesMap);
	
	/**
	 * Sum of clique table sizes.
	 * @param sumOfCliqueSizes the sumOfCliqueSizes to set
	 */
	public void setSumOfCliqueTableSizes(int sumOfCliqueSizes);

	/**
	 * Sum of clique table sizes.
	 * @return the sumOfCliqueSizes
	 */
	public int getSumOfCliqueTableSizes();

	/**
	 * Sum of clique table sizes without counting the cliques whose all nodes 
	 * were resolved. This is because depending on how the implementation of {@link MarkovEngineInterface}
	 * is implemented (i.e. whether cliques containing only resolved questions are deleted or not),  
	 * the clique may still stay there in order 
	 * to keep track of assets of a clique whose all nodes were resolved.
	 * @param sumOfCliqueSizesWithoutResolvedCliques the sumOfCliqueSizesWithoutResolvedCliques to set
	 */
	public void setSumOfCliqueTableSizesWithoutResolvedCliques(
			int sumOfCliqueSizesWithoutResolvedCliques) ;

	/**
	 * Sum of clique table sizes without counting the cliques whose all nodes 
	 * were resolved. This is because depending on how the implementation of {@link MarkovEngineInterface}
	 * is implemented (i.e. whether cliques containing only resolved questions are deleted or not),  
	 * the clique may still stay there in order 
	 * to keep track of assets of a clique whose all nodes were resolved.
	 * @return the sumOfCliqueSizesWithoutResolvedCliques
	 */
	public int getSumOfCliqueTableSizesWithoutResolvedCliques();
	
	/**
	 * Sum of clique tables and separator tables sizes.
	 * @param sumOfCliqueAndSeparatorTableSizes the sumOfCliqueAndSeparatorTableSizes to set
	 */
	public void setSumOfCliqueAndSeparatorTableSizes(
			int sumOfCliqueAndSeparatorTableSizes);

	/**
	 * Sum of clique tables and separator tables sizes.
	 * @return the sumOfCliqueAndSeparatorTableSizes
	 */
	public int getSumOfCliqueAndSeparatorTableSizes();

	/**
	 * Sum of clique tables and separator table sizes, without counting the cliques whose all nodes 
	 * were resolved. This is because depending on how the implementation of {@link MarkovEngineInterface}
	 * is implemented (i.e. whether cliques containing only resolved questions are deleted or not),  
	 * the clique may still stay there in order 
	 * to keep track of assets of a clique whose all nodes were resolved.
	 * @param sumOfCliqueAndSeparatorTableSizesWithoutResolvedCliques the sumOfCliqueAndSeparatorTableSizesWithoutResolvedCliques to set
	 */
	public void setSumOfCliqueAndSeparatorTableSizesWithoutResolvedCliques(
			int sumOfCliqueAndSeparatorTableSizesWithoutResolvedCliques) ;

	/**
	 * Sum of clique tables and separator table sizes, without counting the cliques whose all nodes 
	 * were resolved. This is because depending on how the implementation of {@link MarkovEngineInterface}
	 * is implemented (i.e. whether cliques containing only resolved questions are deleted or not),  
	 * the clique may still stay there in order 
	 * to keep track of assets of a clique whose all nodes were resolved.
	 * @return the sumOfCliqueAndSeparatorTableSizesWithoutResolvedCliques
	 */
	public int getSumOfCliqueAndSeparatorTableSizesWithoutResolvedCliques() ;
	
	/**
	 * Number of degrees of freedom in the probability representation.
	 * In a clique-based structure, this is calculated as: 
	 * for each clique, get the clique table size 
	 * (i.e. multiply the number of variable values) 
	 * and subtract one; sum over cliques; do the same for
	 * the separators; subtract that sum from the clique sum.
	 * @param degreeOfFreedom the degreeOfFreedom to set
	 */
	public void setDegreeOfFreedom(int degreeOfFreedom);


	/**
	 * Number of degrees of freedom in the probability representation.
	 * In a clique-based structure, this is calculated as: 
	 * for each clique, get the clique table size 
	 * (i.e. multiply the number of variable values) 
	 * and subtract one; sum over cliques; do the same for
	 * the separators; subtract that sum from the clique sum.
	 * @return the degreeOfFreedom
	 */
	public int getDegreeOfFreedom();
	
	/**
	 * Total quantity of cliques (including cliques whose all nodes were resolved).
	 * @return the numberOfCliques
	 */
	public int getNumberOfCliques();


	/**
	 * Total quantity of cliques (including cliques whose all nodes were resolved).
	 * @param numberOfCliques the numberOfCliques to set
	 */
	public void setNumberOfCliques(int numberOfCliques);
	
	
	/**
	 * Total quantity of separators
	 * @param numberOfSeparators the numberOfSeparators to set
	 */
	public void setNumberOfSeparators(int numberOfSeparators);


	/**
	 * Total quantity of separators
	 * @return the numberOfSeparators
	 */
	public int getNumberOfSeparators();
	
	/**
	 * Maximum clique table size (size of largest clique table)
	 * @param maxCliqueTableSize the maxCliqueTableSize to set
	 */
	public void setMaxCliqueTableSize(int maxCliqueTableSize);


	/**
	 * Maximum clique table size (size of largest clique table)
	 * @return the maxCliqueTableSize
	 */
	public int getMaxCliqueTableSize();
	
	/**
	 * Total quantity of cliques (not including cliques whose all nodes were resolved).
	 * @param numberOfNonEmptyCliques the numberOfNonEmptyCliques to set
	 */
	public void setNumberOfNonEmptyCliques(int numberOfNonEmptyCliques);


	/**
	 * Total quantity of cliques (not including cliques whose all nodes were resolved).
	 * @return the numberOfNonEmptyCliques
	 */
	public int getNumberOfNonEmptyCliques();
	
	/**
	 * Maximum number of parents per node.
	 * @param maxNumParents the maxNumParents to set
	 */
	public void setMaxNumParents(int maxNumParents);


	/**
	 * Maximum number of parents per node.
	 * @return the maxNumParents
	 */
	public int getMaxNumParents();
	
	/**
	 * @return the total quantity of arcs
	 */
	public int getNumArcs();
	
	/**
	 * @param numArcs the total quantity of arcs
	 */
	public void setNumArcs(int numArcs);
	
	
	
}
