package unbbayes.util;

import java.util.ArrayList;
import java.util.List;

import unbbayes.prs.Network;
import unbbayes.prs.Node;

public class GraphLayoutUtil {
	
	protected Network network;
	protected List<List<Node>> graphLevelMatrix;
	
	public GraphLayoutUtil(Network network) {
		this.network = network;
		graphLevelMatrix = new ArrayList<List<Node>>();
		createGraphLevelMatrix();
	}
	
	public void doLayout() {
		double x;
		double y;
		for (int i = 0; i < graphLevelMatrix.size(); i++) {
			List<Node> nodeList = graphLevelMatrix.get(i);
			y = i * 150 + 20;
			for (int j = 0; j < nodeList.size(); j++) {
				x = j * 300 + 50;
				nodeList.get(j).setPosition(x, y);
			}
		}
	}

	/**
	 * Creates the queue of the nodes that are going to be analyzed.
	 */
	protected void createGraphLevelMatrix() {
		// Keeps track of the nodes that have already been added to the queue (nodeAddedList[nodeIndex]=true). 
		boolean[] nodeAddedList = new boolean[network.getNodeCount()];
		initGraphLevelMatrix(nodeAddedList);
		// For every root node, add its children
		int level = 0;
		boolean wasNodeAdded = false;
		do {
			wasNodeAdded = false;
			List<Node> currentLevelList = graphLevelMatrix.get(level++);
			List<Node> nextLevelList = new ArrayList<Node>();
			for(int i = 0; i < currentLevelList.size(); i++) {
				Node node = currentLevelList.get(i);
				if (addChildrenToNextLevel(node.getChildren(), nextLevelList, nodeAddedList)) {
					wasNodeAdded = true;			
				}
			}
			graphLevelMatrix.add(nextLevelList);
		} while (wasNodeAdded);
	}

	/**
	 * Initializes the queue with the nodes that are root. In other words. 
	 * It will put in the queue the nodes that do not have parents.
	 * @param nodeAddedList Keeps track of the nodes that have already been added to the queue (nodeAddedList[nodeIndex]=true).
	 */
	protected void initGraphLevelMatrix(boolean[] nodeAddedList) {
		// Add root nodes
		List<Node> nodeList = new ArrayList<Node>();
		for(int i = 0 ; i < network.getNodeCount(); i++){
			if(network.getNodeAt(i).getParents().size() == 0 ){
				nodeAddedList[i]= true;					
				nodeList.add(network.getNodeAt(i));
			}
		}
		graphLevelMatrix.add(nodeList);
	}

	/**
	 * Take the children of a node that have already been added to the queue. Analyze them
	 * one by one and add the child that is not in the queue yet. 
	 * @param children Children of a node that is already in the queue.
	 * @param nodeAddedList Nodes that have already been added to the queue.
	 */
	protected boolean addChildrenToNextLevel(ArrayList<Node> children, List<Node> nodeList, boolean[] nodeAddedList) {
		boolean wasNodeAdded = false;
		for (int i = 0 ; i < children.size(); i++) {
			Node n1 = children.get(i);
			for (int j = 0 ; j < network.getNodeCount(); j++) {
				Node n2 = network.getNodeAt(j);
				if (n1.getName().equals(n2.getName())) {
					if (!nodeAddedList[j]) {
						nodeList.add(n1);						
						nodeAddedList[j] = true;
						wasNodeAdded = true;
						break;						
					}										
				}				
			}	
		}
		return wasNodeAdded;
	}

}
