package unbbayes.prs.mebn.ssbn;

public class SSBNWarning {

	private String warning; 
	private SSBNNode nodeCause; 
	private Exception exception; 
	private String detalhatedExplanation; 
	
	public SSBNWarning(String warning, Exception e, SSBNNode nodeCause, String detalhatedExplanation){
		this.warning = warning; 
		this.exception = e; 
		this.nodeCause = nodeCause;
		this.detalhatedExplanation = detalhatedExplanation; 
	}
	
	public SSBNWarning(Exception e, SSBNNode nodeCause){
		this(null, e, nodeCause, null); 
	}
	
	public String getWarning() {
		return warning;
	}

	public SSBNNode getNodeCause() {
		return nodeCause;
	}

	public String getDetalhatedExplanation() {
		return detalhatedExplanation;
	}
	
}
