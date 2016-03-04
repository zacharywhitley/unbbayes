package unbbayes.prs.mebn.ssbn.bayesballalgorithm;

import java.util.ArrayList;
import java.util.List;

import unbbayes.controller.INetworkMediator;
import unbbayes.controller.mebn.IMEBNMediator;
import unbbayes.io.log.ISSBNLogManager;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.prs.mebn.exception.MEBNException;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.ssbn.BuilderLocalDistributionImpl;
import unbbayes.prs.mebn.ssbn.IBuilderLocalDistribution;
import unbbayes.prs.mebn.ssbn.IBuilderStructure;
import unbbayes.prs.mebn.ssbn.IMediatorAwareSSBNGenerator;
import unbbayes.prs.mebn.ssbn.OVInstance;
import unbbayes.prs.mebn.ssbn.Query;
import unbbayes.prs.mebn.ssbn.SSBN;
import unbbayes.prs.mebn.ssbn.exception.ImplementationRestrictionException;
import unbbayes.prs.mebn.ssbn.exception.OVInstanceFaultException;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;
import unbbayes.prs.mebn.ssbn.pruner.IPruneStructure;
import unbbayes.prs.mebn.ssbn.pruner.IPruner;
import unbbayes.prs.mebn.ssbn.pruner.impl.BarrenNodePruner;
import unbbayes.prs.mebn.ssbn.pruner.impl.PruneStructureImpl;
import unbbayes.prs.mebn.ssbn.util.PositionAdjustmentUtils;
import unbbayes.util.Debug;

/**
 * Generate Situation Specific Bayesian Network based on Bayes Ball algorithm 
 * for verify D-Connected nodes. 
 * 
 * Bayes-Ball Algorithm based on paper "Bayes-Ball: The Rational Pastime (for 
 * Determining Irrelevant and Requisite Information in Belief Networks and 
 * Influence Diagrams)", by Ross D. Schachter (1998). Adapted to work with 
 * MEBN and SSBN generation. 
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 */
public class BayesBallSSBNGenerator implements IMediatorAwareSSBNGenerator{

	private boolean isLogEnabled = true;
	private INetworkMediator mediator;
	
	private KnowledgeBase kb; 
	private SSBN ssbn; 

	private ISSBNLogManager logManager; 
	
	/**
	 * Generate a SSBN using a adaptation of the Bayes Ball algorithm. 
	 */
	public SSBN generateSSBN(List<Query> queryList, KnowledgeBase kb)
			throws SSBNNodeGeneralException,
				   ImplementationRestrictionException, 
				   MEBNException,
				   OVInstanceFaultException, 
				   InvalidParentException {
		
		this.kb = kb; 
		
		System.out.println("");
		System.out.println("---------------------------------");
		System.out.println("       Bayes Ball Algorithm      ");
		System.out.println("---------------------------------");
		System.out.println("");		
		
		//Step 1: Initialization 
		
		ssbn = new SSBN();
		ssbn.setKnowledgeBase(kb); 
		
		logManager = ssbn.getLogManager();
		
		for(Query query: queryList){
			System.out.println("Query: " + query);
			BayesBallNode ssbnNode = BayesBallNode.getInstance(query.getResidentNode()); 
			query.setSSBNNode(ssbnNode); 
			
			for(OVInstance argument : query.getArguments()){
				ssbnNode.setEntityForOv(argument.getOv(), argument.getEntity()); 	
			}
			
			ssbnNode.setFinished(false); 

			ssbn.addQueryToQueryList(query);                                                          
		}
				
		//Step 2: 
		IBuilderStructure structureBuilder = BayesBallStructureBuilder.newInstance(); 
		structureBuilder.buildStructure(ssbn); 
		
		//Step 3: 
		List<IPruner> pruners = new ArrayList<IPruner>();
		
//		pruners.add(DSeparationPruner.newInstance());
		pruners.add(BarrenNodePruner.newInstance());
		
		IPruneStructure pruneStructure = PruneStructureImpl.newInstance(pruners); 
		pruneStructure.pruneStructure(ssbn);
		
		//Step 4: 
		IBuilderLocalDistribution localDistributionBuilder = BuilderLocalDistributionImpl.newInstance(); 
		localDistributionBuilder.buildLocalDistribution(ssbn);
				
		System.out.println("Local distribution generated!");
		
		// Adjust nodes positions
		try {
			PositionAdjustmentUtils.adjustPositionProbabilisticNetwork(
					(ProbabilisticNetwork)ssbn.getNetwork());
		} catch (Exception e) {
			Debug.println(getClass(), e.getMessage(), e);
		}
		
		if (ssbn.getNetwork().getNodeCount() != ssbn.getNetwork().getNodeIndexes().keySet().size()) {
			// Inconsistency on the quantity of indexed nodes and actual nodes
			// force synchronization
			ssbn.getNetwork().getNodeIndexes().clear();
			for (int i = 0; i < ssbn.getNetwork().getNodes().size(); i++) {
				ssbn.getNetwork().getNodeIndexes().put(ssbn.getNetwork().getNodes().get(i).getName(), i);
			}
		}
		
		try {
			ssbn.compileAndInitializeSSBN();
		} catch (Exception e) {
			throw new MEBNException(e);
		}
		
		System.out.println("Network compiled");
		
		// show on display
		if (this.getMediator() instanceof IMEBNMediator) {
			((IMEBNMediator)this.getMediator()).setSpecificSituationBayesianNetwork(
					(ProbabilisticNetwork)(ssbn.getNetwork()));
			((IMEBNMediator)this.getMediator()).setToTurnToSSBNMode(true);	// if this is false, ((IMEBNMediator)this.getMediator()).turnToSSBNMode() will not work
			((IMEBNMediator)this.getMediator()).turnToSSBNMode();
			((IMEBNMediator)this.getMediator()).getScreen().getEvidenceTree().updateTree(true);;
		}
		
		return ssbn;
	}

	public void setMediator(INetworkMediator mediator) {
		this.mediator = mediator;
	}
	
	public INetworkMediator getMediator() {
		return mediator;
	}

	public void setLogEnabled(boolean isLogEnabled) {
		this.isLogEnabled = isLogEnabled;
	}

	public boolean isLogEnabled() {
		return this.isLogEnabled;
	}

	public int getLastIterationCount() {
		// not a iterative method, so it is just 1 iteration
		return 1;
	}

}
