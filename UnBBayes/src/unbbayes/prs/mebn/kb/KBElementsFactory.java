package unbbayes.prs.mebn.kb;

import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.entity.ObjectEntity;

public interface KBElementsFactory {

	public Object createConceptDefinition(ObjectEntity entity);
	
	public Object createRandonVariableDefinition(DomainResidentNode resident);
	
	public Object createEntityFinding();
	
	public Object createRandonVariableFinding();
	
	public Object createContextFormula(ContextNode context);
	
}
