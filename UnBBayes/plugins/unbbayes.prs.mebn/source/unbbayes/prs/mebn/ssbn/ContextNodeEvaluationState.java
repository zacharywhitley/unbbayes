package unbbayes.prs.mebn.ssbn;

public enum ContextNodeEvaluationState{
	EVALUATION_OK,      // Evaluation OK - Context node evaluated true with all the arguments filled
	EVALUATION_FAIL, 	// Evaluation fail - Context node evaluated false with all the arguments filled
	EVALUATION_SEARCH, 	// Evaluation search - Context node result in a list of ord. variables that fill the argument
	NOT_EVALUATED_YET	// Not evaluated yet- Context node not evaluated yet. 
}
