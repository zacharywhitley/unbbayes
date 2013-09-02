/**
 * 
 */
package edu.gmu.ace.scicast.io;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import unbbayes.io.BaseIO;
import unbbayes.io.exception.LoadException;
import unbbayes.prs.Graph;
import unbbayes.prs.INode;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.builder.IProbabilisticNetworkBuilder;
import unbbayes.prs.builder.impl.DefaultProbabilisticNetworkBuilder;
import au.com.bytecode.opencsv.CSVReader;

/**
 * It creates a BN from a DAGGRE csv file.
 * @author Shou Matsumoto
 *
 */
public class DAGGRECSVToBNConverter implements BaseIO {
	private static final String[] FILEEXT = {"csv"};
	private String name = "DAGGRE CSV";
	
	private int defaultNodePosition = 600;
	private int nodePositionRandomness = 570;

	private IDAGGRECSVNodeCreationListener commandToBeCalledWhenNodeIsCreated = null;
	
	private IProbabilisticNetworkBuilder networkBuilder = DefaultProbabilisticNetworkBuilder.newInstance();
	
	/**
	 * Default constructor.
	 * This class creates a BN from a DAGGRE csv file.
	 * The same csv file can be used for reading (soft) evidences.
	 */
	public DAGGRECSVToBNConverter() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Requirements (from Dr. Charles Twardy's e-mail ctwardy@c4i.gmu.edu)
	 * I have no idea how (if?) this file represents edits to multiple-choice questions.  Just assume everything is binary for a first test.
	 * The columns are: QuestionID, Timestamp, UserID, new Probability.  All probabilities started at uniform. 
	 * Output should be a set of probabilities for each question, the number of edits processed, and a runtime.  
	 * The first test should use only disconnected nodes.  So 100+ nodes and no arcs.
	 * The second test should arbitrarily divide them into 20ish groups of five.
	 * To control for background procesess, do 3-5 runs each and take the minimum time.
	 * 
	 * The file contains at least 196 nodes and 3148 users.
	 * @param input : the csv file in DAGGRE format.
	 * @see unbbayes.io.BaseIO#load(java.io.File)
	 */
	public Graph load(File input) throws LoadException, IOException {
		
		ProbabilisticNetwork ret = new ProbabilisticNetwork(input.getName());
		
		// last getCommandToBeCalledWhenNodeIsCreated().getNumberOfNewNodesToBeGeneratedBeforeCall() nodes created
		List<INode> lastNodes = new ArrayList<INode>();	
		
		// called when new node is created
		IDAGGRECSVNodeCreationListener newNodeCommand = getCommandToBeCalledWhenNodeIsCreated();
	   
		CSVReader reader = new CSVReader(new FileReader(input));	// classes of open csv
		String [] nextLine;	// the line read
		while ((nextLine = reader.readNext()) != null) {
	        // nextLine[] is an array of values from the line
	    	Integer id = Integer.valueOf(nextLine[0]);
	        if (ret.getNodeIndex(id.toString()) < 0) {	// if network does not contain node with name == id
	        	ProbabilisticNode node = new ProbabilisticNode();
	        	// change the position of the node in a random fashion
	        	node.setPosition(
	        			getDefaultNodePosition() + ((Math.random()<.5)?(Math.random()*getNodePositionRandomness()):-(Math.random()*getNodePositionRandomness())), 
	        			getDefaultNodePosition() + ((Math.random()<.5)?(Math.random()*getNodePositionRandomness()):-(Math.random()*getNodePositionRandomness()))
        			);
	        	node.setName(id.toString());	// name is primary id
	        	ret.addNode(node);	// add node to network
	        	// assume boolean node
	        	node.appendState("false");
	        	node.appendState("true");
	        	node.getProbabilityFunction().addVariable(node);
	        	// assume uniform dist on startup
	        	node.getProbabilityFunction().setValue(0,.5f);
	        	node.getProbabilityFunction().setValue(1,.5f);
	        	if (newNodeCommand != null) {
	        		lastNodes.add(node);
	        		if (lastNodes.size() >= newNodeCommand.getNumberOfNewNodesToBeGeneratedBeforeCall()) {
	        			newNodeCommand.doCommand(ret, lastNodes);
	        			lastNodes.clear();
	        		}
	        	}
	        }
	    }
		if (newNodeCommand != null && !lastNodes.isEmpty()) {
			// the quantity of lines in the csv was not a multiple of newNodeCommand.getNumberOfNewNodesToBeGeneratedBeforeCall().
			// call for the last few nodes.
			newNodeCommand.doCommand(ret, lastNodes);
			lastNodes.clear();
		}
	    
		return ret;
	}

	/**
	 * Not implemented yet
	 * @see unbbayes.io.BaseIO#save(java.io.File, unbbayes.prs.Graph)
	 */
	public void save(File output, Graph net) throws IOException {
		throw new IOException("Save operation not supported by this version.");
	}

	/**
	 * @see unbbayes.io.BaseIO#supports(java.io.File, boolean)
	 */
	public boolean supports(File file, boolean isLoadOnly) {
		if (!isLoadOnly) {
			// only load is possible
			return false;
		}
		return file.getName().endsWith(".csv");
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.BaseIO#getSupportedFileExtensions(boolean)
	 */
	public String[] getSupportedFileExtensions(boolean isLoadOnly) {
		if (!isLoadOnly) {
			return new String[0];	// avoid returning null
		}
		return FILEEXT;
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.BaseIO#getSupportedFilesDescription(boolean)
	 */
	public String getSupportedFilesDescription(boolean isLoadOnly) {
		if (!isLoadOnly) {
			return "";	// avoid returning null
		}
		return "CSV file in DAGGRE format";
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.BaseIO#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.BaseIO#getName()
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @return the commandToBeCalledWhenNodeIsCreated
	 */
	public IDAGGRECSVNodeCreationListener getCommandToBeCalledWhenNodeIsCreated() {
		return commandToBeCalledWhenNodeIsCreated;
	}

	/**
	 * @param commandToBeCalledWhenNodeIsCreated the commandToBeCalledWhenNodeIsCreated to set
	 */
	public void setNodeCreationListener(IDAGGRECSVNodeCreationListener nodeCreationListener) {
		this.commandToBeCalledWhenNodeIsCreated = nodeCreationListener;
	}

	/**
	 * @return the networkBuilder
	 */
	public IProbabilisticNetworkBuilder getNetworkBuilder() {
		return networkBuilder;
	}

	/**
	 * @param networkBuilder the networkBuilder to set
	 */
	public void setNetworkBuilder(IProbabilisticNetworkBuilder networkBuilder) {
		this.networkBuilder = networkBuilder;
	}

	/**
	 * @return the defaultNodePosition
	 */
	public int getDefaultNodePosition() {
		return defaultNodePosition;
	}

	/**
	 * @param defaultNodePosition the defaultNodePosition to set
	 */
	public void setDefaultNodePosition(int defaultNodePosition) {
		this.defaultNodePosition = defaultNodePosition;
	}

	/**
	 * @return the nodePositionRandomness
	 */
	public int getNodePositionRandomness() {
		return nodePositionRandomness;
	}

	/**
	 * @param nodePositionRandomness the nodePositionRandomness to set
	 */
	public void setNodePositionRandomness(int nodePositionRandomness) {
		this.nodePositionRandomness = nodePositionRandomness;
	}

}
