package unbbayes.prs.mebn.kb;

import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.entity.ObjectEntity;

/**
 * Methods for work with the knowledge base, add knowledge and 
 * doing queries. 
 * 
 * @author Laecio Lima dos Santos
 *
 */
public class KnowledgeBaseFacade {

	private KnowledgeBase kb; 
	private KBElementsFactory factory; 
	
	public KnowledgeBaseFacade(KnowledgeBase _kb, KBElementsFactory _factory){
		kb = _kb; 
		factory = _factory; 
	}
	
	public void insertConceptDefinition(ObjectEntity entity){
		kb.executeConceptDefinition(factory.createConceptDefinition(entity)); 
	}
	
	public void insertRandonVariableDefinition(DomainResidentNode resident){
		kb.executeRandonVariableDefinition(factory.createRandonVariableDefinition(resident)); 
	}
	
	public void insertEntityFinding(){
		kb.executeEntityFinding(factory.createEntityFinding()); 
	}
	
	public void insertRandonVariableFinding(){
		kb.executeRandonVariableFinding(factory.createRandonVariableFinding()); 
	}
	
	public boolean querieContextFormula(ContextNode context){
		return kb.executeContextFormula(factory.createContextFormula(context));
	}
	
}