package unbbayes.prs.mebn.kb.powerloom;

import unbbayes.prs.mebn.kb.KnowledgeBase;
import edu.isi.powerloom.*;
import edu.isi.powerloom.logic.*;
import edu.isi.stella.Module;
import edu.isi.stella.javalib.*;
import edu.isi.stella.Stella_Object;

public class PowerLoomKB implements KnowledgeBase{

	public PowerLoomKB(){
	    PLI.initialize();
	}
	
	public void executeConceptDefinition(Object conceptDefinition){
		
	}
	
	public void executeRandonVariableDefinition(Object randonVariableDefinition){
		
	}
	
	public void executeEntityFinding(Object entityFinding){
		
	}
	
	public void executeRandonVariableFinding(Object randonVariableFinding){
		
	}
	
	public boolean executeContextFormula(Object contextFormula){
		return false;
	}
	
}
