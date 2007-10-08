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
import unbbayes.prs.mebn.ssbn.OVInstance;

/**
 * 
 * @author Laecio Lima dos Santso
 * @version 1.0 (09/24/07)
 */
public interface KnowledgeBase {

	/**
	 * Insert the entity into KB. 
	 */
	public void createEntityDefinition(ObjectEntity entity);
	
	/**
	 * Insert the randon variable and your states into KB. 
	 * @param resident
	 */
	public void createRandonVariableDefinition(DomainResidentNode resident);
	
	/**
	 * Insert the entity finding into KB. 
	 * @param resident
	 */
	public void insertEntityInstance(ObjectEntityInstance entityFinding);
	
	/**
	 * Insert the randon variable and your states into KB. 
	 * @param randonVariableFinding
	 */
	public void insertRandonVariableFinding(RandonVariableFinding randonVariableFinding); 

	/**
	 * Insert the randon variable and your states into KB. 
	 * 
	 * Notas: 
	 * - As variaveis ordinarias já devem estar setadas com as entidades
	 * - Retorno: True, False, Absurd, lista...
	 * 
	 * @param resident
	 */
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
	
	/** 
	 * A simple formula is a formula that don't have variables and the result is
	 * a boolean falue (true/false). 
	 * ex: IsOwnStarship(Enterprise). 
	 * 
	 * @param context
	 * @param ovInstances
	 * @return
	 */
    public Boolean evaluateSimpleFormula(ContextNode context, List<OVInstance> ovInstances); 
	
    /**
     * Complex formulas are formulas that return a list of entities that satisfies 
     * a restriction. Ex: z = StarshipZone(!ST0) return the zones that satisfies
     * StarshipZone(!ST0).  
     * @param context Node that have the formula
     * @param ovInstances List of OVInstance. Don't have a ovInstance for the 
     *                    parameters that will be search (z in the example). 
     * @return list of the names of entities that satisfies (a empty list if
     *                     don't have entities).  
     */
    public  List<String> evaluateComplexFormula(ContextNode context, List<OVInstance> ovInstances); 
	
}
