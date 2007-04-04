package unbbayes.prs.mebn.kb;

public interface KnowledgeBase {

	public void executeConceptDefinition(Object conceptDefinition);
	
	public void executeRandonVariableDefinition(Object randonVariableDefinition);
	
	public void executeEntityFinding(Object entityFinding);
	
	public void executeRandonVariableFinding(Object randonVariableFinding);
	
	public boolean executeContextFormula(Object contextFormula);
	
}
