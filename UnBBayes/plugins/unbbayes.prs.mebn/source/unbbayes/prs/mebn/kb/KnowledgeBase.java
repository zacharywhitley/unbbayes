/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package unbbayes.prs.mebn.kb;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import unbbayes.io.exception.UBIOException;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.RandomVariableFinding;
import unbbayes.prs.mebn.ResidentNode;
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
	 * It clear the findings of the knowledge base; 
	 */
	public void clearFindings();
	
	/**
	 * Fill the generative Knowledge Base with the terms of the mebn. 
	 * 
	 * @param mebn Multi Entity Bayesian Network that contains the terms
	 */
	public void createGenerativeKnowledgeBase(
			MultiEntityBayesianNetwork mebn); 
	
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
	public void createRandomVariableDefinition(ResidentNode resident);

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
	 * @throws UBIOException 
	 */
	public void loadModule(File file, boolean findingModule) throws UBIOException;

	/**
	 * Evaluate a simple formula. A simple formula is a formula that doesn't have 
	 * variables not instanciated and the result is a boolean value (true/false). <p>
	 * 
	 * Examples: <br>
	 * <i>IsOwnStarship(!ST0).</i><br>
	 * <i>Exists(!ST0)</i><br>
	 * <i>Z1! = StarshipZone(!SR2)</i><br>
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
	 * This method is responsible for evaluating a single search formula. A search
	 * formula is a formula that returns a list of entities that satisfies a
	 * restriction. A single search formula is a formula with only one search 
	 * variable <p>
	 * 
	 * Example:<br>
	 * <i>z = StarshipZone(!ST0)</i> return the zones that
	 * satisfies <i>StarshipZone(!ST0)</i>.
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
	public List<String> evaluateSingleSearchContextNodeFormula(ContextNode context,
			List<OVInstance> ovInstances) throws OVInstanceFaultException;
	
	/**
	 * This method is responsible for evaluating a search formula. A search
	 * formula is a formula that returns a list of entities that satisfies a
	 * restriction. <p>
	 * 
	 * Example:<br>
	 * <i>z = StarshipZone(!ST0)</i> return the zones that
	 * satisfies <i>StarshipZone(!ST0)</i>.
	 * 
	 * @param context
	 *            the context node that has the formula.
	 *            
	 * @param ovInstances
	 *            the list of OVInstance. 
	 * @return 
	 *        The values for each fault variable that satisfies the context node, 
	 *        or null if not exists valuese that satisfies the context node. 
	 */
	public SearchResult evaluateSearchContextNodeFormula(ContextNode context,
			List<OVInstance> ovInstances);
	
	
	/**
	 * Try to recover the entity values that satisfies all the context nodes.
	 * (evaluate the free variables) 
	 * 
	 * @param contextList 
	 * @param ovInstances Values for the know variables
	 * @return The values for each fault variable that satisfies all the 
	 *         context nodes. 
	 */
	public Map<OrdinaryVariable, List<String>> evaluateMultipleSearchContextNodeFormula(
			List<ContextNode> contextList, List<OVInstance> ovInstances);
	
	
	
    /*-------------------------------------------------------------------------*/
	/* Facade Methods                                                          */
	/*-------------------------------------------------------------------------*/
	
	/** 
	 * Verify it exists the entity in the base
	 */
	public boolean existEntity(String name);

	/**
	 * Verify if exists a findings in the knowledge base. Return the finding if its
	 * exists (a finding is a state for the randon variables with the arguments). 
	 * Return null otherside. 
	 * @param randonVariable The resident variable to be tested 
	 * @param listArguments  The arguments (in the some order that the arguments of the node)
	 * @return
	 */
    public StateLink searchFinding(ResidentNode randonVariable, Collection<OVInstance> listArguments); 

	/**
	 * Return all the entities of one type. 
	 * @param type
	 * @return
	 */
	public List<String> getEntityByType(String type);
	

	
	/**
	 * Searches the KB for findings of a resident node and adds those findings as
	 * new RandomVariableFindings
	 * @param resident resident node which name is going to be searched inside kb
	 * @see RandomVariableFinding
	 */
	public void fillFindings(ResidentNode resident); 
	
	
}
