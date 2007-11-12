package unbbayes.prs.mebn.ssbn;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import unbbayes.io.mebn.PrOwlIO;
import unbbayes.io.mebn.exceptions.IOMebnException;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.DomainMFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.entity.Type;
import unbbayes.prs.mebn.kb.KBFacade;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.kb.powerloom.PowerLoomFacade;
import unbbayes.prs.mebn.kb.powerloom.PowerLoomKB;
import unbbayes.prs.mebn.ssbn.exception.InvalidContextNodeFormula;
import unbbayes.prs.mebn.ssbn.exception.OVInstanceFaultException;
import unbbayes.util.Debug;

/**
 * Class that contains methods for evaluate the context nodes of a MFrag. 
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 */
public class ContextNodeAvaliator {

	private KnowledgeBase kb; 
    private KBFacade kbFacade; 
	
	public ContextNodeAvaliator(KnowledgeBase kb, KBFacade kbFacade){
		
		this.kb = kb; 
		this.kbFacade = kbFacade; 
		
	}

	/**
	 * Evaluate a context node. 
	 * 
	 * @param node
	 * @param ovInstances
	 * @return
	 * @throws OVInstanceFaultException
	 */
	public boolean evaluateContextNode(ContextNode node, List<OVInstance> ovInstances) throws OVInstanceFaultException{
		
		List<OrdinaryVariable> ovFaultList = node.getOVFaultForOVInstanceSet(ovInstances); 
		
		if(!ovFaultList.isEmpty()){
			throw new OVInstanceFaultException(ovFaultList); 
		}else{
			return kb.evaluateSimpleFormula(node, ovInstances);
		}
		
	}
	
	/**
	 * Evaluate a search context node. A search context node is a node that return
	 * instances of the Knowledge Base that satisfies a restriction. 
	 * 
	 * Ex.: z = StarshipZone(st). 
	 * -> return all the z's. 
	 * 
	 * @param context
	 * @param ovInstances
	 * @return
	 * @throws InvalidContextNodeFormula
	 */
	public List<String> evalutateSearchContextNode(ContextNode context, List<OVInstance> ovInstances) throws InvalidContextNodeFormula{
		
			List<String> entitiesResult = kb.evaluateComplexContextFormula(context, ovInstances); 
			return entitiesResult;
	}
	
}
