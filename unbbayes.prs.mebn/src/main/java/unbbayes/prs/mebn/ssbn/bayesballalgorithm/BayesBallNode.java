package unbbayes.prs.mebn.ssbn.bayesballalgorithm;

import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.ssbn.SimpleSSBNNode;

/**
 * Node used in the BayesBall algorithm. 
 * 
 * Add to SimpleSSBNNode the necessary information for run BayesBall algorithm. 
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 */
public class BayesBallNode extends SimpleSSBNNode {

	private boolean evaluatedTop; 
	private boolean evaluatedBottom; 
	private boolean observed; 

	private boolean receivedBallFromChild; 
	private boolean receivedBallFromParent; 

	private boolean visited; 
	
//	//Maybe put this code into SimpleSSBNNode... 
//	private List<MFragInstance> inputMFragList; 
	
	
	private BayesBallNode(ResidentNode residentNode){
		super(residentNode); 
		
		evaluatedTop = false; 
		evaluatedBottom = false; 
		observed = false; 

		this.receivedBallFromChild = false; 
		this.receivedBallFromParent = false; 
		this.visited = false; 
		
//		this.inputMFragList = new ArrayList<MFragInstance>(); 
		
	}
	
	public static BayesBallNode getInstance(ResidentNode residentNode){
		return new BayesBallNode(residentNode); 
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
	
	public void setObserved(boolean observed){
		this.observed = observed; 
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
	
	/** 
	 * Verify if other object is the same of this. 
	 * 
	 * Return true if: 
	 * 1) obj is a BayesBallNode
	 * 2) obj have the same resident node 
	 * 3) obj have the same argument values for each ordinary variable  
	 */
	public boolean equals(Object obj){

		if (!(obj instanceof BayesBallNode)) return false; 

		BayesBallNode otherObject = (BayesBallNode)obj; 
		
		if (otherObject.getResidentNode().equals(this.getResidentNode())){
			for(int i = 0; i < this.getEntityArray().length; i++){
				if(!this.getEntityArray()[i].equals(otherObject.getEntityArray()[i])){
					return false; 
				}
			}
			return true; 
		}else{
			return false; 
		}
		
	}
	
}
