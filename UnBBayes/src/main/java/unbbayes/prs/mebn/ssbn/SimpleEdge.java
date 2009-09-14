package unbbayes.prs.mebn.ssbn;

public class SimpleEdge {

	private SimpleSSBNNode originNode; 
	
	private SimpleSSBNNode destinationNode;

	/**
	 * Create a edge bethween two nodes. 
	 * 
	 * @param _originNode       The parent
	 * @param _destinationNode  The child
	 */
	public SimpleEdge(SimpleSSBNNode _originNode, SimpleSSBNNode _destinationNode){
		this.originNode = _originNode; 
		this.destinationNode = _destinationNode; 
	}
	
	public SimpleSSBNNode getOriginNode() {
		return originNode;
	}

	public SimpleSSBNNode getDestinationNode() {
		return destinationNode;
	}
	
}
