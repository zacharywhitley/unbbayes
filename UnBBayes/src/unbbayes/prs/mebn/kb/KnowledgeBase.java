package unbbayes.prs.mebn.kb;

import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.entity.ObjectEntity;

public interface KnowledgeBase {

	public void executeConceptDefinition(ObjectEntity entity);
	
	public void executeRandonVariableDefinition(DomainResidentNode resident);
	
	//TODO alterar apos definir como sera um finding... 
	public void executeEntityFinding(String entityFinding);
	
	//TODO alterar apos definir como sera um finding... 
	public void executeRandonVariableFinding(String randonVariableFinding);
	
	public boolean executeContextFormula(ContextNode context);
	
}
