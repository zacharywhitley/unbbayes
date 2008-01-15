package unbbayes.prs.mebn.ssbn;

import unbbayes.prs.mebn.ContextNode;

public class ContextNodeEvaluationResult {

	public static int FALSE        = 0;
	public static int TRUE         = 1; 
	public static int ENTITIES     = 2; 
	public static int ALL_ENTITIES = 3; 
	public static int UNAVALIABLE  = 4; 
	
	private ContextNode contextNode; 
	private int typeResult; 
	private Object objectResult; 

	public ContextNodeEvaluationResult(ContextNode contextNode, int typeResult, Object objectResult){
		this.contextNode = contextNode; 
		this.typeResult = typeResult; 
		this.objectResult = objectResult; 
	}
	
	public ContextNodeEvaluationResult(ContextNode contextNode, int typeResult){
		this.contextNode = contextNode; 
		this.typeResult = typeResult; 
		this.objectResult = null; 
	}

	public ContextNode getContextNode() {
		return contextNode;
	}

	public Object getObjectResult() {
		return objectResult;
	}

	public int getTypeResult() {
		return typeResult;
	}
	
}
