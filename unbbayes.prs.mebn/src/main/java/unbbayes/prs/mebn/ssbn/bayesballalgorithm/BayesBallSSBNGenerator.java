package unbbayes.prs.mebn.ssbn.bayesballalgorithm;

import java.util.ArrayList;
import java.util.List;

import unbbayes.io.log.ISSBNLogManager;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.entity.Entity;
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.entity.exception.TypeException;
import unbbayes.prs.mebn.exception.MEBNException;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.ssbn.BuilderLocalDistributionImpl;
import unbbayes.prs.mebn.ssbn.IBuilderLocalDistribution;
import unbbayes.prs.mebn.ssbn.IBuilderStructure;
import unbbayes.prs.mebn.ssbn.ISSBNGenerator;
import unbbayes.prs.mebn.ssbn.OVInstance;
import unbbayes.prs.mebn.ssbn.Query;
import unbbayes.prs.mebn.ssbn.SSBN;
import unbbayes.prs.mebn.ssbn.SimpleSSBNNode;
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
public class BayesBallSSBNGenerator implements ISSBNGenerator{

	private boolean isLogEnabled = true;
	
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
		
		long initialTime = System.currentTimeMillis(); 
		
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
			
		long inicializationTime = System.currentTimeMillis(); 
		long numberNodesAfterInitialization = ssbn.getSimpleSsbnNodeList().size(); 
		
		//Step 2: 
		IBuilderStructure structureBuilder = BayesBallStructureBuilder.newInstance(); 
		structureBuilder.buildStructure(ssbn); 
		
		long numberNodesAfterBuilder = ssbn.getSimpleSsbnNodeList().size(); 
		
		long buildStructureTime = System.currentTimeMillis(); 
		
		//Step 3: 
		List<IPruner> pruners = new ArrayList<IPruner>();
		
//		pruners.add(DSeparationPruner.newInstance());
		pruners.add(BarrenNodePruner.newInstance());
		
		IPruneStructure pruneStructure = PruneStructureImpl.newInstance(pruners); 
		pruneStructure.pruneStructure(ssbn);
		
		long numberNodesAfterPrune = ssbn.getSimpleSsbnNodeList().size(); 
		long pruneTime = System.currentTimeMillis(); 
		
		//TODO Remove this temporary solution. 
		//Recover from kb individuals of ObjectEntity in case of this individuals are
		//not filled yed (when the user uses a database knowledge base, for instance. 
		Debug.println("\n");
		Debug.println("Fill possible values for Object Entities:"); 
		
		List<ObjectEntity> evaluatedObjectEntityList = new ArrayList<ObjectEntity>(); 
		
		for(SimpleSSBNNode node: ssbn.getSimpleSsbnNodeList()){
			ResidentNode resident = node.getResidentNode(); 
			for (Entity state : resident.getPossibleValueList()) {
				if (state instanceof ObjectEntity) {
					ObjectEntity objectEntityState = (ObjectEntity)state; 
					if(!evaluatedObjectEntityList.contains(objectEntityState)){
						objectEntityState.removeAllInstances();
						evaluatedObjectEntityList.add(objectEntityState); 
					}
					if(objectEntityState.getInstanceList().size() == 0){
						Debug.println("Fill possible values for entity " + objectEntityState);
						List<String> listIndividuals = kb.getEntityByType(objectEntityState.getType().getName()); 
						for(String individual: listIndividuals){
							try {
								Debug.println("Individual: " + individual);
								objectEntityState.addInstance(individual);
							} catch (TypeException e) {
								e.printStackTrace();
							} 
						}
					}
				}
			}
		}
		
		Debug.println("\n");
		
		//Step 4: 
		//In this step we translate the BayesBallSSBNnode (SimpleSSBNNode) to SSBNNode.
		//This is a good time for expand the states of resident nodes, because we already prune 
		//all unnecessary nodes, so all nodes in the SSBN really will need of the possible value list. 
		IBuilderLocalDistribution localDistributionBuilder = BuilderLocalDistributionImpl.newInstance(); 
		localDistributionBuilder.buildLocalDistribution(ssbn);
				
		long buildCPTTime = System.currentTimeMillis(); 
		
		//Free space... 
		Runtime rt = Runtime.getRuntime();
		System.out.println("Free memory: " + rt.freeMemory()); 
		ssbn.removeSpaceAfterGenerationOfNetwork(); 
		rt.gc();
		System.out.println("Free memory after garbage collection: " + rt.freeMemory()); 
		
		System.out.println("Local distribution generated!");
		
		try {
			ssbn.compileAndInitializeSSBN();
		} catch (Exception e) {
			throw new MEBNException(e);
		}
		
		long compilationTime = System.currentTimeMillis(); 
		
		System.out.println("Network compiled");
		
		long finalTime = System.currentTimeMillis();
		
		
		System.out.println("-------------------------------------------------------------------------------");
		System.out.println("Benchmark Overview");
		System.out.println("Inicialization  Time : " + (inicializationTime  - initialTime ) + "ms");
		System.out.println("Build Structure Time : " + (buildStructureTime  - inicializationTime ) + "ms");
		System.out.println("Prune Structure Time : " + (pruneTime  - buildStructureTime ) + "ms");
		System.out.println("Build CPT Time : " + (buildCPTTime  - pruneTime ) + "ms");
		System.out.println("Compilation Time : " + (compilationTime  - buildCPTTime ) + "ms");
		System.out.println("Final Time : " + (finalTime   - initialTime ) + "ms");
		System.out.println(" ");
		System.out.println("Nodes After Initialization: " + numberNodesAfterInitialization);
		System.out.println("Nodes After Builder: " + numberNodesAfterBuilder);
		System.out.println("Nodes After Prune: " + numberNodesAfterPrune);
		System.out.println("-------------------------------------------------------------------------------");
		
		
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
		
		return ssbn;
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
