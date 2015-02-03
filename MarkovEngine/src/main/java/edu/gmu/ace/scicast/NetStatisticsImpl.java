/**
 * 
 */
package edu.gmu.ace.scicast;

import java.util.HashMap;
import java.util.Map;

/**
 * This is the default implementation of {@link NetStatistics}
 * @author Shou Matsumoto
 */
public class NetStatisticsImpl implements NetStatistics {

	private static final long serialVersionUID = -106466719737571173L;

	private Map<Integer, Integer> numberOfStatesToNumberOfNodesMap = new HashMap<Integer, Integer>(0);
	
	private int sumOfCliqueTableSizes = 0;
	private int sumOfCliqueAndSeparatorTableSizes = 0;
	private int sumOfCliqueTableSizesWithoutResolvedCliques = 0;
	private int sumOfCliqueAndSeparatorTableSizesWithoutResolvedCliques = 0;
	private int degreeOfFreedom = 0;
	private int numberOfCliques = 0;
	private int numberOfSeparators = 0;
	private int maxCliqueTableSize = 0;
	private int numberOfNonEmptyCliques = 0;
	private int maxNumParents = 0;
	private int numArcs = 0;
	private boolean isRunningApproximation = false;
	
	
	
	/**
	 * Default constructor is remaining public for
	 * some level of compatibility with beans design pattern.
	 */
	public NetStatisticsImpl() {
		// TODO Auto-generated constructor stub
	}

	
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
	public Map<Integer, Integer> getNumberOfStatesToNumberOfNodesMap() {
		return numberOfStatesToNumberOfNodesMap;
	}

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
	public void setNumberOfStatesToNumberOfNodesMap(
			Map<Integer, Integer> numberOfStatesToNumberOfNodesMap) {
		this.numberOfStatesToNumberOfNodesMap = numberOfStatesToNumberOfNodesMap;
	}

	/**
	 * Sum of clique table sizes.
	 * @param sumOfCliqueTableSizes the sumOfCliqueTableSizes to set
	 */
	public void setSumOfCliqueTableSizes(int sumOfCliqueSizes) {
		this.sumOfCliqueTableSizes = sumOfCliqueSizes;
	}

	/**
	 * Sum of clique table sizes.
	 * @return the sumOfCliqueTableSizes
	 */
	public int getSumOfCliqueTableSizes() {
		return sumOfCliqueTableSizes;
	}

	/**
	 * Sum of clique table sizes without counting the cliques whose all nodes 
	 * were resolved. This is because depending on how the implementation of {@link MarkovEngineInterface}
	 * is implemented (i.e. whether cliques containing only resolved questions are deleted or not),  
	 * the clique may still stay there in order 
	 * to keep track of assets of a clique whose all nodes were resolved.
	 * @param sumOfCliqueTableSizesWithoutResolvedCliques the sumOfCliqueTableSizesWithoutResolvedCliques to set
	 */
	public void setSumOfCliqueTableSizesWithoutResolvedCliques(
			int sumOfCliqueSizesWithoutResolvedCliques) {
		this.sumOfCliqueTableSizesWithoutResolvedCliques = sumOfCliqueSizesWithoutResolvedCliques;
	}

	/**
	 * Sum of clique table sizes without counting the cliques whose all nodes 
	 * were resolved. This is because depending on how the implementation of {@link MarkovEngineInterface}
	 * is implemented (i.e. whether cliques containing only resolved questions are deleted or not),  
	 * the clique may still stay there in order 
	 * to keep track of assets of a clique whose all nodes were resolved.
	 * @return the sumOfCliqueTableSizesWithoutResolvedCliques
	 */
	public int getSumOfCliqueTableSizesWithoutResolvedCliques() {
		return sumOfCliqueTableSizesWithoutResolvedCliques;
	}

	/**
	 * Sum of clique tables and separator tables sizes.
	 * @param sumOfCliqueAndSeparatorTableSizes the sumOfCliqueAndSeparatorTableSizes to set
	 */
	public void setSumOfCliqueAndSeparatorTableSizes(
			int sumOfCliqueAndSeparatorTableSizes) {
		this.sumOfCliqueAndSeparatorTableSizes = sumOfCliqueAndSeparatorTableSizes;
	}

	/**
	 * Sum of clique tables and separator tables sizes.
	 * @return the sumOfCliqueAndSeparatorTableSizes
	 */
	public int getSumOfCliqueAndSeparatorTableSizes() {
		return sumOfCliqueAndSeparatorTableSizes;
	}

	/**
	 * Sum of clique tables and separator table sizes, without counting the cliques whose all nodes 
	 * were resolved. This is because depending on how the implementation of {@link MarkovEngineInterface}
	 * is implemented (i.e. whether cliques containing only resolved questions are deleted or not),  
	 * the clique may still stay there in order 
	 * to keep track of assets of a clique whose all nodes were resolved.
	 * @param sumOfCliqueAndSeparatorTableSizesWithoutResolvedCliques the sumOfCliqueAndSeparatorTableSizesWithoutResolvedCliques to set
	 */
	public void setSumOfCliqueAndSeparatorTableSizesWithoutResolvedCliques(
			int sumOfCliqueAndSeparatorTableSizesWithoutResolvedCliques) {
		this.sumOfCliqueAndSeparatorTableSizesWithoutResolvedCliques = sumOfCliqueAndSeparatorTableSizesWithoutResolvedCliques;
	}

	/**
	 * Sum of clique tables and separator table sizes, without counting the cliques whose all nodes 
	 * were resolved. This is because depending on how the implementation of {@link MarkovEngineInterface}
	 * is implemented (i.e. whether cliques containing only resolved questions are deleted or not),  
	 * the clique may still stay there in order 
	 * to keep track of assets of a clique whose all nodes were resolved.
	 * @return the sumOfCliqueAndSeparatorTableSizesWithoutResolvedCliques
	 */
	public int getSumOfCliqueAndSeparatorTableSizesWithoutResolvedCliques() {
		return sumOfCliqueAndSeparatorTableSizesWithoutResolvedCliques;
	}


	/**
	 * Number of degrees of freedom in the probability representation.
	 * In a clique-based structure, this is calculated as: 
	 * for each clique, get the clique table size 
	 * (i.e. multiply the number of variable values) 
	 * and subtract one; sum over cliques; do the same for
	 * the separators; subtract that sum from the clique sum.
	 * @param degreeOfFreedom the degreeOfFreedom to set
	 */
	public void setDegreeOfFreedom(int degreeOfFreedom) {
		this.degreeOfFreedom = degreeOfFreedom;
	}


	/**
	 * Number of degrees of freedom in the probability representation.
	 * In a clique-based structure, this is calculated as: 
	 * for each clique, get the clique table size 
	 * (i.e. multiply the number of variable values) 
	 * and subtract one; sum over cliques; do the same for
	 * the separators; subtract that sum from the clique sum.
	 * @return the degreeOfFreedom
	 */
	public int getDegreeOfFreedom() {
		return degreeOfFreedom;
	}


	/**
	 * Total quantity of cliques (including cliques whose all nodes were resolved).
	 * @return the numberOfCliques
	 */
	public int getNumberOfCliques() {
		return numberOfCliques;
	}


	/**
	 * Total quantity of cliques (including cliques whose all nodes were resolved).
	 * @param numberOfCliques the numberOfCliques to set
	 */
	public void setNumberOfCliques(int numberOfCliques) {
		this.numberOfCliques = numberOfCliques;
	}


	/**
	 * Total quantity of separators
	 * @param numberOfSeparators the numberOfSeparators to set
	 */
	public void setNumberOfSeparators(int numberOfSeparators) {
		this.numberOfSeparators = numberOfSeparators;
	}


	/**
	 * Total quantity of separators
	 * @return the numberOfSeparators
	 */
	public int getNumberOfSeparators() {
		return numberOfSeparators;
	}


	/**
	 * Maximum clique table size (size of largest clique table)
	 * @param maxCliqueTableSize the maxCliqueTableSize to set
	 */
	public void setMaxCliqueTableSize(int maxCliqueTableSize) {
		this.maxCliqueTableSize = maxCliqueTableSize;
	}


	/**
	 * Maximum clique table size (size of largest clique table)
	 * @return the maxCliqueTableSize
	 */
	public int getMaxCliqueTableSize() {
		return maxCliqueTableSize;
	}


	/**
	 * Total quantity of cliques (not including cliques whose all nodes were resolved).
	 * @param numberOfNonEmptyCliques the numberOfNonEmptyCliques to set
	 */
	public void setNumberOfNonEmptyCliques(int numberOfNonEmptyCliques) {
		this.numberOfNonEmptyCliques = numberOfNonEmptyCliques;
	}


	/**
	 * Total quantity of cliques (not including cliques whose all nodes were resolved).
	 * @return the numberOfNonEmptyCliques
	 */
	public int getNumberOfNonEmptyCliques() {
		return numberOfNonEmptyCliques;
	}


	/**
	 * Use this method if you'd only like to get a string representation of this report.
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		// the string to return
		String ret = "Quantity of nodes organized by quantity of states:\n";
		
		// append quantity of nodes peer state
		for (Integer numStates : getNumberOfStatesToNumberOfNodesMap().keySet()) {
			ret += "-nodes with " + numStates + " possible states: " + getNumberOfStatesToNumberOfNodesMap().get(numStates) + "\n";
		}
		
		ret += "\n Total quantity of arcs: " + getNumArcs() + "\n\n";
		

		// append info related to clique table sizes
		ret += "Sum of clique table sizes: " + getSumOfCliqueTableSizes() + "\n";
		ret += "Sum of clique table sizes without counting the cliques whose all nodes were resolved: " 
			+ getSumOfCliqueTableSizesWithoutResolvedCliques() + "\n\n";

		// append info related to clique table sizes, with separators table size too
		ret += "Sum of clique and separator table sizes: " + getSumOfCliqueAndSeparatorTableSizes() + "\n";
		ret += "Sum of clique and separator table sizes, without counting the \"resolved\" cliques: " 
			+ getSumOfCliqueAndSeparatorTableSizesWithoutResolvedCliques() + "\n\n";

		// append info related to degree of freedom
		ret += "Number of degrees of freedom in the probability representation: " + getDegreeOfFreedom() + "\n\n";

		// append info related to quantity of cliques and separators
		ret += "Total quantity of cliques (including cliques whose all nodes were resolved): " + getNumberOfCliques() + "\n";
		ret += "Total quantity of separators: " + getNumberOfSeparators() + "\n";
		ret += "Total quantity of cliques (not including cliques whose all nodes were resolved): " + getNumberOfNonEmptyCliques() + "\n\n";

		// append info related to maximum clique table size
		ret += "Maximum clique table size (size of largest clique table): " + getMaxCliqueTableSize() + "\n\n";
		
		// maximum number of parents per node
		ret += "Maximum number of parents per node: " + getMaxNumParents() + "\n\n";
		
		ret += "Using approximation: " + this.isRunningApproximation();
		
		return ret;
	}


	/**
	 * Maximum number of parents per node.
	 * @param maxNumParents the maxNumParents to set
	 */
	public void setMaxNumParents(int maxNumParents) {
		this.maxNumParents = maxNumParents;
	}


	/**
	 * Maximum number of parents per node.
	 * @return the maxNumParents
	 */
	public int getMaxNumParents() {
		return maxNumParents;
	}


	/**
	 * @return the numArcs
	 */
	public int getNumArcs() {
		return this.numArcs;
	}


	/**
	 * @param numArcs the numArcs to set
	 */
	public void setNumArcs(int numArcs) {
		this.numArcs = numArcs;
	}


	/*
	 * (non-Javadoc)
	 * @see edu.gmu.ace.scicast.NetStatistics#isRunningApproximation()
	 */
	public boolean isRunningApproximation() {
		return isRunningApproximation;
	}


	/*
	 * (non-Javadoc)
	 * @see edu.gmu.ace.scicast.NetStatistics#setRunningApproximation(boolean)
	 */
	public void setRunningApproximation(boolean isRunningApproximation) {
		this.isRunningApproximation = isRunningApproximation;
	}


}
