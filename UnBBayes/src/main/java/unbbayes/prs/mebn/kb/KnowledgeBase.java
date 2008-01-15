package unbbayes.prs.mebn.kb;

import java.io.File;
import java.util.Collection;
import java.util.List;

import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.RandomVariableFinding;
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.entity.ObjectEntityInstance;
import unbbayes.prs.mebn.entity.StateLink;
import unbbayes.prs.mebn.ssbn.OVInstance;
import unbbayes.prs.mebn.ssbn.exception.OVInstanceFaultException;

/**
 * This interface defines all methods necessary for a KB in UnBBayes, so it can
 * evaluate context node formulas, in other words, FOL sentences.
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 * @author Rommel Novaes Carvalho (rommel.carvalho@gmail.com)
 * @version 1.0 (2007/12/26)
 */
public interface KnowledgeBase {

	/**
	 * It clears all knowledge base.
	 */
	public void clearKnowledgeBase();

	/**
	 * It inserts the entity into KB as a definition.
	 * 
	 * @param entity
	 *            the entity to insert as a definition.
	 */
	public void createEntityDefinition(ObjectEntity entity);

	/**
	 * It inserts the resident random variable and its states into KB as a
	 * definition.
	 * 
	 * @param resident
	 *            the resident node to insert as a definition.
	 */
	public void createRandomVariableDefinition(DomainResidentNode resident);

	/**
	 * The method is responsible for inserting the entity as a finding into KB.
	 * 
	 * @param entityInstance
	 *            the entity to insert as finding.
	 */
	public void insertEntityInstance(ObjectEntityInstance entityInstance);

	/**
	 * The method is responsible for inserting the random variable and its
	 * states as a finding into KB.
	 * 
	 * @param randomVariableFinding
	 *            the random variable to insert as finding.
	 */
	public void insertRandomVariableFinding(
			RandomVariableFinding randomVariableFinding);

	/**
	 * The method is responsible for saving in the given file the concepts
	 * designed in the given MEBN.
	 * 
	 * @param mebn
	 *            the MEBN where the concepts are designed.
	 * @param file
	 *            the file to save the concepts.
	 */
	public void saveGenerativeMTheory(MultiEntityBayesianNetwork mebn, File file);

	/**
	 * The method is responsible for saving in the given file all findings
	 * present in the given MEBN.
	 * 
	 * @param mebn
	 *            the MEBN where the findings are defined.
	 * @param file
	 *            the file to save the findings.
	 */
	public void saveFindings(MultiEntityBayesianNetwork mebn, File file);

	/**
	 * The method is responsible for loading the module defined in the given
	 * file.
	 * 
	 * @param file
	 *            the file that contains the module's definition to be loaded.
	 */
	public void loadModule(File file);

	/**
	 * This method is responsible for evaluating a simple formula. A simple
	 * formula is a formula that does not have variables and the result is a
	 * boolean value (true/false). Example: IsOwnStarship(!ST0).
	 * 
	 * @param context
	 *            the context node that has the formula.
	 * @param ovInstances
	 *            the list of ovInstances that have to have all the ov's used in
	 *            the context node's formula.
	 * @return the result of the evaluation (true or false)
	 */
	public Boolean evaluateContextNodeFormula(ContextNode context,
			List<OVInstance> ovInstances);

	/**
	 * This method is responsible for evaluating a complex formula. A complex
	 * formula is a formula that returns a list of entities that satisfies a
	 * restriction. Example: z = StarshipZone(!ST0) return the zones that
	 * satisfies StarshipZone(!ST0).
	 * 
	 * @param context
	 *            the context node that has the formula.
	 * @param ovInstances
	 *            the list of OVInstance. It does not have an ovInstance for the
	 *            parameter that will be searched for (z in the example above).
	 * @return 
	 *            the list of entities' names that satisfies the restriction (an
	 *            empty list if it does not find any entity).
	 * @throws OVInstanceFaultException 
	 *            For this implementation, only is permited
	 *            one search variable. For all others variables of the context formula, 
	 *            one ov instance should be present in ovInstances list, otherside
	 *            this exception will be throw.  
	 */
	public List<String> evaluateSearchContextNodeFormula(ContextNode context,
			List<OVInstance> ovInstances) throws OVInstanceFaultException;
	
	
	
	
	
	
    /*-------------------------------------------------------------------------*/
	/* Facade Methods                                                          */
	/*-------------------------------------------------------------------------*/
	
	/** 
	 * Verifica se existe a entidade na base, retornando o seu tipo em caso positivo, 
	 * ou null caso contrario. 
	 */
	public boolean existEntity(String name);

	/**
	 * Verifica se existe o finding na base. Caso positivo, retorna o valor (ou seja, 
	 * o estado para a variável ordinária, dados os argumentos), caso negativo, 
	 * retorna null.  
	 * @param nameRV
	 * @param listArguments
	 * @return
	 */
    public StateLink searchFinding(DomainResidentNode randonVariable, Collection<OVInstance> listArguments); 

	/**
	 * Return all the entities of one type. 
	 * @param type
	 * @return
	 */
	public List<String> getEntityByType(String type);
	

}
