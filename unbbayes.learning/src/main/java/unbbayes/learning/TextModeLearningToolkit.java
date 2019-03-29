
package unbbayes.learning;

import java.util.ArrayList;
import java.util.List;

import unbbayes.controller.MainController;
import unbbayes.prs.Edge;
import unbbayes.prs.Node;
import unbbayes.prs.bn.LearningNode;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.ProbabilisticTable;
import unbbayes.prs.bn.cpt.impl.NormalizeTableFunction;
import unbbayes.util.Debug;

/**
 * @author Shou Matsumoto; Edited by Young
 * @author Bo
 *
 */
public class TextModeLearningToolkit extends LearningToolkit {

    private int classVariableIndex = -1;
    
 
	private String[] paradigmAlgorithmMetricParam = {
			AlgorithmController.PARADIGMS.Ponctuation.name(),	// scoring paradigm
			AlgorithmController.SCORING_ALGORITHMS.K2.name(),	// K2 algorithm
			AlgorithmController.METRICS.MDL.name(),				// MDL metric
			"1"													// default value of parameter is 1
		};

	public TextModeLearningToolkit(ProbabilisticNetwork net, List<LearningNode> variables){
    	
		if (net != null) {
			for (Node n : variables) { 
				Node original = net.getNode(n.getName());
				n.removeStates();
				for (int i = 0; i < original.getStatesSize(); i++){
					String state = original.getStateAt(i);
					n.appendState(state);
				}
			}
		}
    	
    	setEmptyNet(net);
    	this.setVariables(variables);
    }
    
    public void setData(List<LearningNode> dv, int[][] matrix, int[] vector, long caseNumber, boolean compacted){
    	for (LearningNode n :  dv){
    		this.data_variables.put(n.getName(), n);	
    	}
    	    	
		this.compacted = compacted;
		this.dataBase = matrix;
		this.vector = vector;
		this.caseNumber = caseNumber;
	}
     
    /**
     * This method normalizes the Conditional Probability Table (CPT) of all nodes in a probabilistic network.
     * <br/>
     * <br/>
     * TODO create this method in {@link MainController}, and this method should simply delegate to it.
     * 
     * @param net : the network whose 
     * @see #ProbabilisticController(ArrayList, int[][], int[], long, MainController, boolean)
     * @see NormalizeTableFunction
     */
	public void normalizeCPTs(ProbabilisticNetwork net) {
		
		if (net == null || net.getNodes() == null) {
			return;	// ignore null networks
		}
		
		// use this table normalizer in order to normalize table
		NormalizeTableFunction normalizer = new NormalizeTableFunction();
		
		// iterate on all nodes
		for (Node node : net.getNodes()) {
			if (node == null) {
				continue;	// ignore invalid nodes
			}
			if (node instanceof ProbabilisticNode) {
				// extract the CPT of this node
				PotentialTable table = ((ProbabilisticNode) node).getProbabilityFunction();
				if (table != null 
						&& (table instanceof ProbabilisticTable)) { // ignore nodes with unknown or invalid CPTs
					// this should normalize the CPT
					normalizer.applyFunction((ProbabilisticTable) table);
				}
				
			}
		}
		
	}


	/**
	 * Learns the Bayes net structure and parameters from the arguments of this toolkit (see arguments in constructor).
	 * TODO this method does not check for cycles.
	 * @param topology : arcs in this network will be included in new network. Set to null in order to ignore this parameter
	 * @param learnNewArcs : if true, structure learning will be used to learn new arcs. If false, then all arcs
	 * will be used from topology net. 
	 * @return a new instance of {@link ProbabilisticNetwork}
	 * @see TextModeLearningToolkit
	 * @see #getFrequencies(LearningNode, List)
	 * @see #getProbability(float[][], LearningNode)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ProbabilisticNetwork buildNet(boolean learnNewArcs) {
		
		// perform structure learning if it was asked
		if (learnNewArcs) {
			// the class varible to be used in augmented learning
			int classVarIndex = getClassVariableIndex();
			if (classVarIndex < 0) {
				// perform non-augmented structure learning
				new AlgorithmController(
						(List)getVariables(), getDataBase(), getVector(), getCaseNumber(), getParadigmAlgorithmMetricParam(), isCompacted());
			} else {	// class variable was specified
				
				// Bayes net augmented structure learning (BAN)
				new AlgorithmController(
						(List)getVariables(), getDataBase(), getVector(), getCaseNumber(), getParadigmAlgorithmMetricParam(), isCompacted(), 
						classVarIndex);
				
			}
			
		}
		
		ProbabilisticNetwork topology = getEmptyNet(); 
		
    	if (!learnNewArcs) {
    		// disconsider/clear all new arcs that were learned
    		for (Node var : getVariables()) {
				var.getChildNodes().clear();
				var.getParentNodes().clear();
				var.clearAdjacents();
			}
    	}
    	
    	// just create new instance
    	setLearnedNet(this.instantiateProbabilisticNetwork(getVariables()));
    	
    	if (topology != null) {
    		
    		List<ProbabilisticNode> newNodes = new ArrayList<>(topology.getNodeCount());
    		
    		// reuse arcs. TODO avoid cycles
    		for (Node childInTopology : topology.getNodes()) {
    			if (childInTopology == null) {
    				Debug.println(getClass(), "Found a null node in the list of nodes of network " 
    									+ topology + ". Ignoring...");
    				continue;	// only consider nodes that are present in topology
    			}
    			Node childInNet = getLearnedNet().getNode(childInTopology.getName());
    			if (childInNet == null) {
    				// create the node if it was not present in the learned net already
    				childInNet = new ProbabilisticNode();
    				childInNet.setName(childInTopology.getName());
    				childInNet.setDescription(childInTopology.getDescription());
    				((ProbabilisticNode)childInNet).getProbabilityFunction().addVariable(childInNet);
    				// making sure the new node has same states of its original
    				for (int stateIndex = 0; stateIndex < childInTopology.getStatesSize(); stateIndex++) {
    					childInNet.appendState(childInTopology.getStateAt(stateIndex));
					}
    				getLearnedNet().addNode(childInNet);
    				// make sure nodes are not placed all over position (0,0) in canvas.
    				adjustPosition(getLearnedNet().getNodeIndex(childInNet.getName()), getLearnedNet());
    				newNodes.add((ProbabilisticNode) childInNet);
    			}
    			
    			// reuse (x,y) position
    			childInNet.setPosition(childInTopology.getPosition().getX(), childInTopology.getPosition().getY());
    			
    			for (Node parentInTopology : childInTopology.getParents()) {
    				Node parentInNet = getLearnedNet().getNode(parentInTopology.getName());
    				if (parentInNet == null) {
        				// create the node if it was not present in the learned net already
    					parentInNet = new ProbabilisticNode();
    					parentInNet.setName(parentInTopology.getName());
    					parentInNet.setDescription(parentInTopology.getDescription());
        				((ProbabilisticNode)parentInNet).getProbabilityFunction().addVariable(parentInNet);
        				// making sure the new node has same states of its original
        				for (int stateIndex = 0; stateIndex < parentInTopology.getStatesSize(); stateIndex++) {
							parentInNet.appendState(parentInTopology.getStateAt(stateIndex));
						}
        				getLearnedNet().addNode(parentInNet);
        				// make sure nodes are not placed all over position (0,0) in canvas.
        				adjustPosition(getLearnedNet().getNodeIndex(parentInNet.getName()), getLearnedNet());
        				newNodes.add((ProbabilisticNode) parentInNet);
        			}
    				if (getLearnedNet().hasEdge(parentInNet, childInNet) >= 0) {
    					continue;	// ignore arcs that already exist
    				}
    				parentInNet.getChildren().add(childInNet); 
    				childInNet.getParents().add(parentInNet);
    				Edge edge = new Edge(parentInNet, childInNet);
    				// the following will avoid making automatic changes to sizes of CPTs, 
    				// compared to getLearnedNet().addEdge(edge);
    				getLearnedNet().getEdges().add(edge);	
    			} 
    			
    		}	// end of for each child in topology
    		
    		// populate potential tables of newly created nodes from their original CPTs
    		for (ProbabilisticNode newNode : newNodes) {
    			Node originalNode = topology.getNode(newNode.getName());
    			if (originalNode == null
    					|| !(originalNode instanceof ProbabilisticNode)) {
    				throw new RuntimeException("Failed to retrieve node " + newNode 
    						+ " from topology network, even though node is supposedly created. This can be a bug in the algorithm.");
    			}
    			if (originalNode.getStatesSize() != newNode.getStatesSize()) {
    				throw new RuntimeException("Node in topology had " + originalNode.getStatesSize() 
    				+ " states, while a new node created in the algorithm has " + newNode.getStatesSize() 
    				+ " states. This is probably a bug in the algorithm.");
    			}
    			// TODO do we need to check if the ordering of states also matches?
    			
    			// will reuse the CPT from original (topology) network.
    			PotentialTable originalTable = ((ProbabilisticNode)originalNode).getProbabilityFunction();
    			PotentialTable newTable = newNode.getProbabilityFunction();
    			// make sure the variables are in the same order
    			for (int varIndex = 0; varIndex < newNode.getStatesSize(); varIndex++) {
    				if (!originalTable.getVariableAt(varIndex).equals(newTable.getVariableAt(varIndex))) {
    					throw new RuntimeException(
    							"Indexes of variables in new table does not match with original table. This can be a bug in the libraries.");
    				}
    			}
    			// since parents of new nodes are added in same ordering of original network, we can clone CPT
    			newTable.setValues(originalTable.getValues());
    		}
    	} else {
			// adjust position of nodes without using specific topology
    		ProbabilisticNetwork net = getLearnedNet();
    		for (int nodeIndex = 0; nodeIndex < net.getNodeCount(); nodeIndex++) {
    			adjustPosition(nodeIndex, net);
			}
    	}
    	 
		return getLearnedNet();
	}
	
	/**
	 * Simply moves a node to some (x,y) position in the canvas
	 * based on an algorithm proportional to the index of the node in the network.
	 * This can be used to automatically place a node to some position in the canvas,
	 * in order to avoid placing all nodes in same (x,y) position.
	 * @param nodeIndex : index of the node in the network
	 * @param net : net containing the node to move.
	 */
	protected void adjustPosition(int nodeIndex, ProbabilisticNetwork net) {
		Node n = net.getNodeAt(nodeIndex);
		if (n == null) {
			throw new IndexOutOfBoundsException(nodeIndex + " is not a valid node index in network " + net);
		}
		double x = 120*nodeIndex;
		double y = ( ( (nodeIndex % 4) * 100 ) + 10) ;
		n.setPosition( x, y);
	}

	/**
	 * execute parameter learning 
	 */
	public void runParameterLearning(List<LearningNode> data_variables, int[][] matrix, int[] vector, long caseNumber) {
		setData(data_variables, matrix, vector, caseNumber, false);
		forceParameterLearning();
	}
 
	/**
	 * Forces execution of parameter learning of {@link #getVariables()}.
	 */
	public void forceParameterLearning() {

        int length = getVariables().size();
        
		for(int i = 0; i < length ; i++) {
        	LearningNode variable  = (LearningNode)getVariables().get(i); 
        	float[][] pre_arrayNijk =  dataBaseForEachRV.get(variable);
        	
        	if (variable.getName().equalsIgnoreCase("asia")){
        		System.out.println();
        	}
        	
        	if (pre_arrayNijk != null) {
        		System.out.println("");
        	}
        	float[][] arrayNijk = getFrequencies(variable, variable.getParents()); // <- get array for data                         
        	PotentialTable table = variable.getProbabilidades();				   // <- get table to update it according to new added parents 
        	// probably there was garbage. Clean up
        	while (table.getVariablesSize() > 1) {
        		// remove parents. Parents will be re-added later.
        		table.removeVariable(table.getVariableAt(table.getVariablesSize()-1), false);
        	}
        	int parentsLength = variable.getTamanhoPais();
for2:       for (int j = 0; j < parentsLength; j++) {
            	Node pai = variable.getPais().get(j);
            	for (int k = 0; k < table.variableCount(); k++) {
            		if (pai == table.getVariableAt(k)) {
            			continue for2;
            		}            		
            	}
                table.addVariable(pai);											// <- Updated table for new parents
            }
           
            if (pre_arrayNijk != null) {										// <- Add previous data to new data
            	
            	System.out.println(java.util.Arrays.toString(pre_arrayNijk[0]) + " " + java.util.Arrays.toString(arrayNijk[0]));
            	addArrays(variable, arrayNijk, pre_arrayNijk);
            	System.out.println(java.util.Arrays.toString(arrayNijk[0])); 
            }
            
        	getProbability(arrayNijk, variable); 								// <- Calculate new probabilities
        }      
		
		this.normalizeCPTs(getLearnedNet()); // normalize the CPT of all nodes in the network 
	}

	public void addArrays(LearningNode variable, float[][] first, float[][] second) { 
		int ri = getStateSize(variable);
        int nijLength  = getTotalParentStateSize(variable.getPais());
		 for(int i = 0; i < nijLength;i++){ 
             for(int j = 0; j < ri ; j++){ 
            	 first[j][i] = first[j][i] + second[j][i]; 
             }
		 }
	} 

	/**
	 * Simply creates a new instance of a network, but it reuses list of nodes.
	 * {@link Edge} will be rebuild.
	 * @param nodeList : nodes to include in network
	 * @param considerArcs : if true, arcs will be considered. If false, arcs will be ignored
	 * @return new instance of {@link ProbabilisticNetwork}
	 */
	public ProbabilisticNetwork instantiateProbabilisticNetwork(
			List<LearningNode> nodeList) {
		ProbabilisticNetwork net = new ProbabilisticNetwork("NewNetwork");
		boolean direction = true;
		int length  = nodeList.size(); 
		for (int i = 0; i < length; i++) {
			Node child = nodeList.get(i);
			net.addNode(child);
			
			// potential table must contain the node itself by default
			PotentialTable table = ((ProbabilisticNode)child).getProbabilityFunction();
			if (table.getVariableIndex(child) < 0) {	// avoid adding duplicates
				table.addVariable(child);
			}
			
			for (int j = 0; j < child.getParents().size(); j++) {
				Node parent = (Node)child.getParents().get(j);
				parent.getChildren().add(child);
				for(int k = 0 ; k < parent.getParents().size() && direction; k++){
					Node tempNode = (Node)parent.getParents().get(k);
					if(tempNode == child){
						parent.getParents().remove(k);
						direction = false;
					}                      		
				}                 
				Edge edge = new Edge(parent, child);
				edge.setDirection(direction);                	
				direction = true;
				net.getEdges().add(edge);
			}
			
		}    
		 
		return net;
	} 
	
	/**
	 * @return the variables
	 */
	public List<LearningNode> getVariables() {
		return global_variables;
	}
 
	/**
	 * @param variables the variables to set
	 */
	public void setVariables(List<LearningNode> variables) {
		this.global_variables = variables;
	} 

	/**
	 * @return the dataBase
	 */
	public int[][] getDataBase() {
		return this.dataBase;
	} 

	/**
	 * @param the dataBase to set
	 */
	public void setDataBase(int[][] dataBase) {
		this.dataBase = dataBase;
	} 

	/**
	 * @return the vector
	 */
	public int[] getVector() {
		return this.vector;
	} 

	/**
	 * @param vector the vector to set
	 */
	public void setVector(int[] vector) {
		this.vector = vector;
	}
  
	/**
	 * @return the caseNumber
	 */
	public long getCaseNumber() {
		return this.caseNumber;
	}
 
	/**
	 * @param caseNumber the caseNumber to set
	 */
	public void setCaseNumber(long caseNumber) {
		this.caseNumber = caseNumber;
	}
 
	/**
	 * @return the compacted
	 */
	public boolean isCompacted() {
		return this.compacted;
	}
 
	/**
	 * @param compacted the compacted to set
	 */
	public void setCompacted(boolean compacted) {
		this.compacted = compacted;
	}

	/**
	 * @return 
	 * index in {@link #getVariables()}
	 * used in order to force the specified variable to be a class variable.
	 * A class variable is used in augmented structure learning for 
	 * efficiency.
	 * Negative values indicate that class variables are disabled.
	 * @see #buildNet(boolean)
	 * @see AlgorithmController
	 */
	public int getClassVariableIndex() {
		return classVariableIndex;
	}

	/**
	 * @param classVariableIndex 
	 * set this value to some index in {@link #getVariables()}
	 * in order to force the specified variable to be a class variable.
	 * A class variable is used in augmented structure learning for 
	 * efficiency.
	 * Set it to negative in order to disable class variable.
	 * @see #buildNet(boolean)
	 * @see AlgorithmController
	 */
	public void setClassVariableIndex(int classVariableIndex) {
		this.classVariableIndex = classVariableIndex;
	}

	/**
	 * @return
	 * the pamp (paradigm - algorithm - metric- parameter value) array
	 * to be passed to {@link AlgorithmController}
	 * at {@link #buildNet(boolean)}
	 */
	public String[] getParadigmAlgorithmMetricParam() {
		return paradigmAlgorithmMetricParam;
	}

	/**
	 * @param paradigmAlgorithmMetricParam 
	 * the pamp (paradigm - algorithm - metric- parameter value) array
	 * to be passed to {@link AlgorithmController}
	 * at {@link #buildNet(boolean)}
	 */
	public void setParadigmAlgorithmMetricParam(
			String[] paradigmAlgorithmMetricParam) {
		this.paradigmAlgorithmMetricParam = paradigmAlgorithmMetricParam;
	} 
}
