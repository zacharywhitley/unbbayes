package unbbayes.prs.mebn.ssbn;

import java.util.List;

import unbbayes.prs.mebn.MFrag;

public class MFragInstance {

	private MFrag mFragOrigin; 
	
	private boolean useDefaultDistribution; 

	//Ordinary variables
	private List<LiteralEntityInstance>[] ordinaryVariableEvaluationState; 
	
	//Context nodes
	private ContextNodeEvaluationState[] contextNodeEvaluationState; 
	
	public enum ContextNodeEvaluationState{
		EVALUATION_OK, 
		EVALUATION_FAIL, 
		EVALUATION_SEARCH, 
		NOT_EVALUATED_YET
	}
	
	public MFragInstance(MFrag mFragOrigin){
		this.mFragOrigin = mFragOrigin; 
		
		contextNodeEvaluationState = new ContextNodeEvaluationState[mFragOrigin.getContextNodeList().size()]; 
		for(ContextNodeEvaluationState c: contextNodeEvaluationState){
			c = ContextNodeEvaluationState.NOT_EVALUATED_YET; 
		}
	
	}

	public boolean isUsingDefaultDistribution() {
		return useDefaultDistribution;
	}

	public void setUseDefaultDistribution(boolean useDefaultDistribution) {
		this.useDefaultDistribution = useDefaultDistribution;
	}
	
}
