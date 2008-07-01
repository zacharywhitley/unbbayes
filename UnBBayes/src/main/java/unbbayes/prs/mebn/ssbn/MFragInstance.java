package unbbayes.prs.mebn.ssbn;

import java.util.HashMap;
import java.util.Map;

import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.OrdinaryVariable;

public class MFragInstance {

	private MFrag mFragOrigin; 
	
	private boolean useDefaultDistribution; 

	//Ordinary variables
	
	private Map<OrdinaryVariable, LiteralEntityInstance> ordinaryVariableEvaluationState;
	
	private Map<ContextNode, ContextNodeEvaluationState> contextNodeEvaluationState; 
	
	
	public enum ContextNodeEvaluationState{
		EVALUATION_OK, 
		EVALUATION_FAIL, 
		EVALUATION_SEARCH, 
		NOT_EVALUATED_YET
	}
	
	public MFragInstance(MFrag mFragOrigin){
		this.mFragOrigin = mFragOrigin; 
		
		contextNodeEvaluationState = new HashMap<ContextNode, ContextNodeEvaluationState>(); 
		for(ContextNode contextNode: mFragOrigin.getContextNodeList()){
			contextNodeEvaluationState.put(contextNode, ContextNodeEvaluationState.NOT_EVALUATED_YET); 
		}
		
		ordinaryVariableEvaluationState = new HashMap<OrdinaryVariable, LiteralEntityInstance>(); 
		for(OrdinaryVariable ordinaryVariable: mFragOrigin.getOrdinaryVariableList()){
			ordinaryVariableEvaluationState.put(ordinaryVariable, null); 
		}
	
	}
	
	public void setContextNodeEvaluationState(ContextNode context, ContextNodeEvaluationState state){
		contextNodeEvaluationState.put(context, state); 
	}

	public void setOrdinaryVariableEvaluationState(OrdinaryVariable ov, LiteralEntityInstance instance){
		ordinaryVariableEvaluationState.put(ov, instance); 
	}
	
	public boolean isUsingDefaultDistribution() {
		return useDefaultDistribution;
	}

	public void setUseDefaultDistribution(boolean useDefaultDistribution) {
		this.useDefaultDistribution = useDefaultDistribution;
	}
	
}
