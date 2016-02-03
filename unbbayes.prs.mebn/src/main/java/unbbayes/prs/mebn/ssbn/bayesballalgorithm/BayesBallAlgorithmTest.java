package unbbayes.prs.mebn.ssbn.bayesballalgorithm;

import java.util.ArrayList;
import java.util.List;

/** 
 * This is a test implementation of Bayes Ball algorithm for validate the 
 * necessary structure for implement it. 
 * 
 * It has simple use cases for test if the algorithm really is in accordance 
 * with the D-Separation criterion. 
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 */
public class BayesBallAlgorithmTest {
	
	public static void main(String[] args){
		
		BayesBallAlgorithmTest bb = new BayesBallAlgorithmTest(); 
		 
		List<Node> targetNodes = createNetwork07(); 
	
		System.out.println("Algorithm started");
		
		List<Node> dConnectedNodes = bb.runAlgorithm(targetNodes.get(0)); 
		
		System.out.println("");
		System.out.println("");
		System.out.println("------------ RESULT -----------");
		if (dConnectedNodes == null){
			System.out.println("Returned null");
		}else{
			for(Node n: dConnectedNodes){
				System.out.println(n);
			}
		}
		
		System.out.println("--------------------------------");
		System.out.println("");
		
		System.out.println("Algorithm finished");
		
	}

	
	/**
	 * Return list o D-Connected nodes to a target node. 
	 * 
	 * @param network
	 * @param targetNode
	 * @return List of nodes d-connected to the target node. 
	 *         Return null for arguments invalid 
	 */
	public List<Node> runAlgorithm(Node targetNode){
		
		//All nodes 
		List<BayesBallNode> nodeList = new ArrayList<BayesBallNode>();  //all nodes visualized
		
		//Scheduled nodes 
		List<BayesBallNode> unvaluedNodeList = new ArrayList<BayesBallNode>(); 

		//Verify arguments
		if(targetNode == null){
			return null; 
		}
		
		BayesBallNode targetBayesBallNode = new BayesBallNode(targetNode); 
		targetBayesBallNode.setReceivedBallFromChild(true);

		unvaluedNodeList.add(targetBayesBallNode); 
		nodeList.add(targetBayesBallNode); 

		while (unvaluedNodeList.size() > 0){
			
			System.out.println("-------------- NOVA ITERACAO -----------------");
			// This version of Bayes Ball algorithm treat only probabilistic nodes 
			for(BayesBallNode node: unvaluedNodeList){
				
				node.setVisited(true);

				System.out.println("Evaluating node: " + node);
				//Receiving ball from child 
				//  Not finding 
				//  -> pass ball to parents 
				//  -> pass ball to children 
				//  Finding 
				//  -> block 
				if (node.isReceivedBallFromChild()){
					System.out.println("Is Received ball from child");
					if (!node.isObserved()){
						if(!node.isEvaluatedTop()){
							evaluateTop(nodeList, node);
						}
						if(!node.isEvaluatedBottom()){
							evaluateBottom(nodeList, node);
						}
					}
					//Receiving ball from parent 
					//  Not finding 
					//  -> pass ball to children 
					//  Finding 
					//  -> pass ball to parents 
				}
				
				if (node.isReceivedBallFromParent()){
					System.out.println("Is Received ball from parent");
					if (!node.isObserved()){
						if(!node.isEvaluatedBottom()){
							evaluateBottom(nodeList, node);
						}
					} else{
						if(!node.isEvaluatedTop()){
							evaluateTop(nodeList, node);
						}
					}
				}
				
			}
			
			//Schedule nodes to be evaluated in the next iteration 
			unvaluedNodeList.clear(); 
			for(BayesBallNode node: nodeList){
				if(!node.isVisited()){
					unvaluedNodeList.add(node); 
				}
			}
		}

		List<Node> dConnectedNodeList = new ArrayList<Node>(); 
		
		for(BayesBallNode node: nodeList){
			dConnectedNodeList.add(node.getNode());
		}
		
		return dConnectedNodeList; 
		
	}

	private void evaluateBottom(List<BayesBallNode> nodeList,
			BayesBallNode node) {
		System.out.println("Evaluate bottom");
		node.setEvaluatedBottom(true);
		for(Node childrenNode: node.getNode().getChildrenNodeList()){
			BayesBallNode newNode = new BayesBallNode(childrenNode);
			if(nodeList.contains(newNode)){
				int index = nodeList.indexOf(newNode);
				newNode = nodeList.get(index); 
				newNode.setVisited(false);
				newNode.setReceivedBallFromParent(true);
			}else{
				newNode.setReceivedBallFromParent(true);
				nodeList.add(newNode); 
			}
		}
	}

	private void evaluateTop(List<BayesBallNode> nodeList, BayesBallNode node) {
		node.setEvaluatedTop(true);
		System.out.println("Evaluate top");
		for(Node parentNode: node.getNode().getParentNodeList()){
			BayesBallNode newNode = new BayesBallNode(parentNode);
			if(nodeList.contains(newNode)){
				int index = nodeList.indexOf(newNode);
				newNode = nodeList.get(index); 
				newNode.setVisited(false);
				newNode.setReceivedBallFromChild(true);
			}else{
				newNode.setReceivedBallFromChild(true);
				nodeList.add(newNode); 
			}
		}
	}

	private class BayesBallNode{

		private boolean evaluatedTop; 
		private boolean evaluatedBottom; 
		private boolean observed; 

		private boolean receivedBallFromChild; 
		private boolean receivedBallFromParent; 

		private boolean visited; 

		private Node node; 

		private BayesBallNode(Node _node){
			evaluatedTop = false; 
			evaluatedBottom = false; 
			observed = false; 
			this.node = _node; 

			if(_node.isFinding){
				this.observed = true; 
			}

			this.receivedBallFromChild = false; 
			this.receivedBallFromParent = false; 

		}

		public boolean isEvaluatedTop() {
			return evaluatedTop;
		}

		public void setEvaluatedTop(boolean evaluatedTop) {
			this.evaluatedTop = evaluatedTop;
		}

		public boolean isEvaluatedBottom() {
			return evaluatedBottom;
		}

		public void setEvaluatedBottom(boolean evaluatedBottom) {
			this.evaluatedBottom = evaluatedBottom;
		}

		public boolean isObserved() {
			return observed;
		}

		public boolean isVisited() {
			return visited;
		}

		public void setVisited(boolean visited) {
			this.visited = visited;
		}	

		public boolean isReceivedBallFromChild() {
			return receivedBallFromChild;
		}

		public void setReceivedBallFromChild(boolean receivedBallFromChild) {
			this.receivedBallFromChild = receivedBallFromChild;

		}
		
		public void setReceivedBallFromParent(boolean receivedBallFromParent) {
			this.receivedBallFromParent = receivedBallFromParent;

		}
		
		public boolean isReceivedBallFromParent() {
			return receivedBallFromParent;
		}

		public Node getNode() {
			return node;
		}
		
		public String toString(){
			return node.toString(); 
		}
		
		public boolean equals(Object obj){

			if (!(obj instanceof BayesBallNode)) return false; 

			if (((BayesBallNode)obj).getNode().getName().equals(this.getNode().getName())){
				return true; 
			}else{
				return false; 
			}
		}
		
	}

	private class Node{
		private String name; 

		private List<Node> parentNodeList; 
		private List<Node> childrenNodeList; 
		private boolean isFinding; 

		public Node(String _name){
			this.name = _name; 
			parentNodeList = new ArrayList<Node>(); 
			childrenNodeList = new ArrayList<Node>();
			this.isFinding = false; 
		}

		public void addParent(Node _parentNode){
			this.parentNodeList.add(_parentNode); 
			_parentNode.childrenNodeList.add(this); 
		}

		public void setHasEvidence(){
			this.isFinding = true; 
		}

		public boolean equals(Object obj){

			if (!(obj instanceof Node)) return false; 

			if (((Node)obj).getName().equals(this.name)){
				return true; 
			}else{
				return false; 
			}
		}
		
		public String toString(){
			return this.getName(); 
		}

		public String getName() {
			return name;
		}

		public List<Node> getParentNodeList() {
			return parentNodeList;
		}

		public List<Node> getChildrenNodeList() {
			return childrenNodeList;
		}
	}
	
	

	//--------------------------------------------------------------------------
	//                                TESTE CASES                              -
	//--------------------------------------------------------------------------
	
	/**
	 * Network only with the target node. 
	 */
	private static List<Node> createNetwork01(){
		
		BayesBallAlgorithmTest bb = new BayesBallAlgorithmTest(); 
		List<Node> listQueryNodes = new ArrayList<Node>(); 
		
		Node targetNode = bb.new Node("Node_01"); 
		listQueryNodes.add(targetNode); 
		
		return listQueryNodes;
		
	}
	
	/**
	 * Network with the target and one parent. The parent is d-connected by 
	 * a serial connection. 
	 */
	private static List<Node> createNetwork02(){
		
		BayesBallAlgorithmTest bb = new BayesBallAlgorithmTest(); 
		List<Node> listQueryNodes = new ArrayList<Node>(); 
		
		Node targetNode = bb.new Node("Node_01"); 
		listQueryNodes.add(targetNode);
		
		Node parentNode = bb.new Node("Node_02"); 
		targetNode.addParent(parentNode);
		
		return listQueryNodes;
		
	}
	
	/**
	 * Network with the target and one parent. The parent is d-connected by 
	 * a serial connection. 
	 */
	private static List<Node> createNetwork03(){
		
		BayesBallAlgorithmTest bb = new BayesBallAlgorithmTest(); 
		
		List<Node> listQueryNodes = new ArrayList<Node>(); 
		Node targetNode = bb.new Node("Node_01"); 
		listQueryNodes.add(targetNode);
		
		Node parentNode = bb.new Node("Node_02"); 
		targetNode.addParent(parentNode);
		
		return listQueryNodes;
		
	}
	
	/**
	 * Example Fig. 3 
	 * 
	 * 1 -> 2 
	 * 3 -> 2, 6
	 * 5 -> 4, 6
	 * 
	 * Findings: 2, 5
	 * Query: 6 
	 * 
	 */
	private static List<Node> createNetwork04(){
		
		BayesBallAlgorithmTest bb = new BayesBallAlgorithmTest(); 
		
		Node node01 = bb.new Node("Node_01");
		Node node02 = bb.new Node("Node_02");
		Node node03 = bb.new Node("Node_03");
		Node node04 = bb.new Node("Node_04");
		Node node05 = bb.new Node("Node_05");
		Node node06 = bb.new Node("Node_06"); 
		
		node02.addParent(node01);
		node02.addParent(node03);
		node06.addParent(node03);
		node04.addParent(node05);
		node06.addParent(node05);
		
		node01.setHasEvidence();
		node03.setHasEvidence();
		
		List<Node> listQueryNodes = new ArrayList<Node>(); 
		listQueryNodes.add(node06);
		
		return listQueryNodes;
		
	}	
	
	/**
	 * Example Fig. 4a 
	 * 
	 * 1 -> 2
	 * 1 -> 4
	 * 2 -> 5
	 * 3 -> 2
	 * 3 -> 6
	 * 5 -> 4
	 * 6 -> 5
	 * 
	 * Findings: 1, 3
	 * Query: 6 
	 * 
	 */
	private static List<Node> createNetwork05(){
		
		BayesBallAlgorithmTest bb = new BayesBallAlgorithmTest(); 
		
		Node node01 = bb.new Node("Node_01");
		Node node02 = bb.new Node("Node_02");
		Node node03 = bb.new Node("Node_03");
		Node node04 = bb.new Node("Node_04");
		Node node05 = bb.new Node("Node_05");
		Node node06 = bb.new Node("Node_06"); 
		
		node02.addParent(node01);
		node04.addParent(node01);
		node05.addParent(node02);
		node02.addParent(node03);
		node06.addParent(node03);
		node04.addParent(node05);
		node05.addParent(node06);
		
		node01.setHasEvidence();
		node03.setHasEvidence();
		
		List<Node> listQueryNodes = new ArrayList<Node>(); 
		listQueryNodes.add(node06);
		
		return listQueryNodes;
		
	}		
	
	/**
	 * Example Fig. 4c
	 * 
	 * 1 -> 2
	 * 2 -> 5
	 * 3 -> 2
	 * 5 -> 4
	 * 6 -> 5
	 * 
	 * Findings: 2, 4
	 * Query: 6 
	 * 
	 */
	private static List<Node> createNetwork06(){
		
		BayesBallAlgorithmTest bb = new BayesBallAlgorithmTest(); 
		
		Node node01 = bb.new Node("Node_01");
		Node node02 = bb.new Node("Node_02");
		Node node03 = bb.new Node("Node_03");
		Node node04 = bb.new Node("Node_04");
		Node node05 = bb.new Node("Node_05");
		Node node06 = bb.new Node("Node_06"); 
		
		node02.addParent(node01);
		node05.addParent(node02);
		node02.addParent(node03);
		node04.addParent(node05);
		node05.addParent(node06);
		
		node02.setHasEvidence();
		node04.setHasEvidence();
		
		List<Node> listQueryNodes = new ArrayList<Node>(); 
		listQueryNodes.add(node03);
		
		return listQueryNodes;
		
	}	
	
	private static List<Node> createNetwork07(){
		
		BayesBallAlgorithmTest bb = new BayesBallAlgorithmTest(); 
		
		Node a1 = bb.new Node("A1");
		Node a2 = bb.new Node("A2");
		Node a2x1 = bb.new Node("A2-X1");
		Node a2x2 = bb.new Node("A2-X2");
		Node a2d1 = bb.new Node("A2-D1");
		Node a2d2 = bb.new Node("A2-D2");
		Node a4 = bb.new Node("A4"); 
		Node a5 = bb.new Node("A5"); 
		Node a10f = bb.new Node("A10-F");
		Node a10d1x1 = bb.new Node("A10-D1-X1");
		Node a10d2x2 = bb.new Node("A10-D2-X2");
		Node a11 = bb.new Node("A11"); 
		Node a12 = bb.new Node("A12"); 
		Node a8f = bb.new Node("A8-F"); 
		Node a8d1x1 = bb.new Node("A8-D1-X1"); 
		Node a8d2 = bb.new Node("A8-D2"); 
		Node a9x1 = bb.new Node("A9-X1"); 
		Node a9x2 = bb.new Node("A9-X2"); 
		Node a6f = bb.new Node("A6-F"); 
		Node a7x1 = bb.new Node("A7-X1"); 
		Node a7x2 = bb.new Node("A7-X2");
		Node a6d1x1 = bb.new Node("A6-D1-X1");
		Node a6d2x2 = bb.new Node("A6-D2-X2");
		Node a3 = bb.new Node("A3");
		Node a3d1 = bb.new Node("A3-D1");
		Node a3d2 = bb.new Node("A3-D2");
		Node a3d3 = bb.new Node("A3-D3");
		Node a3d4 = bb.new Node("A3-D4");
		Node a3x1 = bb.new Node("A3-X1");
		Node a3x2 = bb.new Node("A3-X2");
		
		Node a13f = bb.new Node("A13-F");
		Node a13d1x1 = bb.new Node("A13-D1-X1");
		
		Node d1 = bb.new Node("D1");
		Node d2 = bb.new Node("D2");
		Node d3 = bb.new Node("D3");
		Node d4 = bb.new Node("D4");
		Node d5 = bb.new Node("D5");
		Node d8 = bb.new Node("D8");
		Node d9 = bb.new Node("D9");
		Node d10 = bb.new Node("D10");
		Node d11 = bb.new Node("D11");
		Node d12 = bb.new Node("D12");
		Node d13 = bb.new Node("D13");
		Node d14 = bb.new Node("D14");
		
		Node d6f = bb.new Node("D6-F");
		
		Node d10cont = bb.new Node("D10cont");
		Node d15 = bb.new Node("D15");
		
		Node d3a1 = bb.new Node("D3-A1");
		Node d4a1 = bb.new Node("D4-A1");
		Node d6a1 = bb.new Node("D6-A1");
		Node d2x1 = bb.new Node("D2-X1");
		Node d7x1 = bb.new Node("D7-X1");
		Node d8x1 = bb.new Node("D8-X1");
		Node d8x2 = bb.new Node("D8-X2");
		Node d8x3 = bb.new Node("D8-X3");
		Node d8x4 = bb.new Node("D8-X4");
		
		Node q = bb.new Node("Q"); 
		
		d15.addParent(d6f);
		d15.addParent(d10cont);
		d7x1.addParent(d6f);
		d6f.addParent(d4);
		d6f.addParent(d6a1);
		d5.addParent(d4);
		d5.addParent(d2x1);
		d4.addParent(d3);
		d4.addParent(d4a1);
		d3.addParent(d3a1);
		d3.addParent(d2);
		d14.addParent(d10cont);
		d14.addParent(d12);
		d13.addParent(d12);
		d12.addParent(d8x4);
		d12.addParent(d10);
		d11.addParent(d10);
		d11.addParent(d8x3);
		d10.addParent(d9);
		d10.addParent(d8x2);
		d9.addParent(d8);
		d9.addParent(d8x1);
		d2.addParent(d1);
		d10cont.addParent(d1);
		d8.addParent(d1);
		d1.addParent(q);
		a13d1x1.addParent(a13f);
		q.addParent(a13f);
		q.addParent(a4);
		q.addParent(a1);
		a4.addParent(a2d2);
		a2d2.addParent(a2x2);
		a2d2.addParent(a2d1);
		a2d1.addParent(a2);
		a2d1.addParent(a2x1);
		a1.addParent(a2);
		a3d3.addParent(a3d2);
		a3d4.addParent(a3d2);
		a3d2.addParent(a3d1);
		a3d2.addParent(a3x2);
		a3d1.addParent(a3x1);
		a3d1.addParent(a3);
		a1.addParent(a3);
		a1.addParent(a5);
		a5.addParent(a10f);
		a5.addParent(a11);
		a5.addParent(a8f);
		a5.addParent(a8d2);
		a5.addParent(a6f);
		a10f.addParent(a11);
		a10f.addParent(a12);
		a10d1x1.addParent(a10f);
		a10d2x2.addParent(a10f);
		a8f.addParent(a9x1);
		a8f.addParent(a9x2);
		a6f.addParent(a7x1);
		a6f.addParent(a7x2);
		a8d1x1.addParent(a8f);
		a8d2.addParent(a8f);		
		a6d1x1.addParent(a6f);
		a6d2x2.addParent(a6f);		

		a10f.setHasEvidence();
		a8f.setHasEvidence();
		a6f.setHasEvidence();
		a13f.setHasEvidence();
		d6f.setHasEvidence();
		
		List<Node> listQueryNodes = new ArrayList<Node>(); 
		listQueryNodes.add(q);
		
		return listQueryNodes;
		
	}	

}



