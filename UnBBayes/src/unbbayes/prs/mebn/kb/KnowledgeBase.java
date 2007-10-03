package unbbayes.prs.mebn.kb;

import java.io.File;
import java.util.List;

import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.RandonVariableFinding;
import unbbayes.prs.mebn.entity.Entity;
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.entity.ObjectEntityInstance;
import unbbayes.prs.mebn.entity.Type;

/**
 * 
 * @author Laecio Lima dos Santso
 * @version 1.0 (09/24/07)
 */
public interface KnowledgeBase {

	/**
	 * Insert the entity into KB. 
	 */
	//createEntityDefinition
	public void executeConceptDefinition(ObjectEntity entity);
	
	/**
	 * Insert the randon variable and your states into KB. 
	 * @param resident
	 */
	//createRandonVariableDefinition
	public void executeRandonVariableDefinition(DomainResidentNode resident);
	
	/**
	 * Insert the entity finding into KB. 
	 * @param resident
	 */
	//insertEntityInstance
	public void executeEntityFinding(ObjectEntityInstance entityFinding);
	
	/**
	 * Insert the randon variable and your states into KB. 
	 * @param randonVariableFinding
	 */
	//insertRandonVariableInstance
	public void executeRandonVariableFinding(RandonVariableFinding randonVariableFinding); 

	/**
	 * Insert the randon variable and your states into KB. 
	 * 
	 * Notas: 
	 * - As variaveis ordinarias já devem estar setadas com as entidades
	 * - Retorno: True, False, Absurd, lista...
	 * 
	 * @param resident
	 */
	//queryContextFormula
	public boolean executeContextFormula(ContextNode context);
	
	
	/**
	 * Salvar a modelagem na base de conhecimento
	 */
	public void saveGenerativeMTheory(MultiEntityBayesianNetwork mebn, File file); 
	
	/**
	 * Salvar os findings 
	 */
	public void saveFindings(MultiEntityBayesianNetwork mebn, File file); 
	
	
	public void loadModule(File file); 
	
	
	
	
	
	
	
	
	
	
	
	/* Outras classes */
	/* O painel de query deve permitir que o usuário entre com entidades não 
	 * instanciadas por findings anteriormente?... Caso afirmativo, criar no painel
	 * uma forma de entrar com o finding pelo nome (texto). 
	 * 
	 * A MEBN deve ter métodos para a partir dos nomes, obter os objetos desejados. 
	 */
	
}
