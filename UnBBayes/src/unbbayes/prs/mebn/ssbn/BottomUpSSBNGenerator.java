/**
 * 
 */
package unbbayes.prs.mebn.ssbn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

import unbbayes.prs.Edge;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.DomainMFrag;
import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.GenerativeInputNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.entity.StateLink;
import unbbayes.prs.mebn.kb.KBFacade;
import unbbayes.prs.mebn.kb.powerloom.PowerLoomKB;
import unbbayes.prs.mebn.ssbn.exception.InvalidContextNodeFormula;
import unbbayes.prs.mebn.ssbn.exception.OVInstanceFaultException;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;
import unbbayes.util.Debug;

/**
 * @author Shou Matsumoto
 *
 */
public class BottomUpSSBNGenerator implements ISSBNGenerator {
	
	private ResourceBundle resource = 
		ResourceBundle.getBundle("unbbayes.prs.mebn.ssbn.resources.Resources");
	
	private KBFacade kb = null;
	
	private MFrag mfrag = null;
	
	/**
	 * 
	 */
	public BottomUpSSBNGenerator() {
		// TODO Auto-generated constructor stub
	}
	
	/*
	 * Calls ContextNodeAvaliator's method to check context node's validation.
	 */
	private void contextNodeEvaluation (SSBNNode queryNode) {
		
		Debug.setDebug(true); 
		
		if (queryNode == null) {
			return;
		}
		
		DomainMFrag mFrag = queryNode.getResident().getMFrag();
		
		// We assume if MFrag is already set to use Default, then some context has failed previously and there's no need to evaluate again.		
		if (mFrag.isUsingDefaultCPT()) {
			return;
		}
		
		ContextNodeAvaliator avaliator = new ContextNodeAvaliator(PowerLoomKB.getInstanceKB(), kb); 
		
		List<OrdinaryVariable> ovList = queryNode.getResident().getOrdinaryVariableList(); 
		
		Collection<ContextNode> contextNodeList = mFrag.getContextByOVCombination(ovList);
		
		List<OVInstance> ovInstances = new ArrayList<OVInstance>(); 
		ovInstances.addAll(queryNode.getArguments()); 
		
		Debug.println(""); 
		Debug.println("Evaluating... "); 
		Debug.println(""); 
		
		for(ContextNode context: contextNodeList){
			Debug.println("Context Node: " + context.getLabel()); 
			try{
				if(!avaliator.evaluateContextNode(context, ovInstances)){
					mFrag.setAsUsingDefaultCPT(true); 
					Debug.println("Result = FALSE. Use default distribution ");
					return;  
				}else{
					Debug.println("Result = TRUE.");
//					return; 
				}
			}
			catch(OVInstanceFaultException e){
				try {
					Debug.println("OVInstance Fault. Try evaluate a search. "); 
					List<String> result = avaliator.evalutateSearchContextNode(context, ovInstances);
					if(result.isEmpty()){
						
						OrdinaryVariable rigthTerm = context.getFreeVariable(); 
						result = kb.getEntityByType(rigthTerm.getValueType().getName());
						
						Debug.println("No information in Knowlege Base"); 
						Debug.print("Result = "); 
						for(String entity: result){
							Debug.print(entity + " "); 
						}
						Debug.println(""); 
//						return;  
					}else{
						Debug.print("Result = "); 
						for(String entity: result){
							Debug.print(entity + " "); 
						}
						Debug.println("");
//						return; 
					}
				} catch (InvalidContextNodeFormula ie) {
					Debug.println("Invalid Context Node: the formula don't is accept."); 
					// TODO Auto-generated catch block
//					ie.printStackTrace();
				} 
			}
			Debug.println(""); 
//			return; 
		}
	}
	
	/*
	 * The recursive part of SSBN bottom-up generation algorithm
	 * currentNode is the node currently analized
	 * seen is all nodes analized previously (doesnt contain currentNode)
	 * net is the resulting ProbabilisticNetwork
	 */
	private SSBNNode generateRecursive(SSBNNode currentNode , SSBNNodeList seen, ProbabilisticNetwork net) throws SSBNNodeGeneralException {
		
		Debug.println("Passo: " + currentNode.getName()); 
		
		// check for cycle
		if (seen.contains(currentNode)) {
			throw new SSBNNodeGeneralException(this.resource.getString("CycleFound"));
		}
		
		// check if querynode has a known value or it should be a probabilistic node (query the kb)
		
		// Extract arguments
		StateLink exactValue = kb.searchFinding(currentNode.getResident(), currentNode.getArguments()); 
		
		// Treat returned value
		if (exactValue != null) {
			// there were an exact match
			currentNode.setNodeAsFinding(exactValue.getState());
			return currentNode;
		}
		
		// if the program reached this line, the node doesn't have a finding
		seen.add(currentNode);	// mark this as already seen  (treated) node
		
		
		// evaluates querynode's mfrag's context nodes (if not OK, sets MFrag's flag to use default CPT)	
		contextNodeEvaluation(currentNode);

		
		// extract parents to treat them recursively
		List<SSBNNode> parents = new ArrayList<SSBNNode>();
		
		// Extract resident nodes
		for (DomainResidentNode parent : currentNode.getResident().getResidentNodeFatherList()) {
			
            SSBNNode ssbnnode = SSBNNode.getInstance(parent, new ProbabilisticNode());
			
			//TODO arguments of the parent node. Control by MFrag? st = ST0 always???
			
            for(OVInstance instance : currentNode.getArguments()){
            	if(parent.containsArgument(instance.getOv())){
            		ssbnnode.addArgument(instance); 
            	}
            }
            
            parents.add(ssbnnode);
            
			
		}
		
		// Extract input nodes
		for (GenerativeInputNode input : currentNode.getResident().getInputNodeFatherList()) {
			DomainResidentNode resident = (DomainResidentNode)input.getResidentNodePointer().getResidentNode(); 
			
			//other MFrag... analyse names of ordinaryVariables... 
            SSBNNode ssbnnode = SSBNNode.getInstance(resident, new ProbabilisticNode());
			
            for(OVInstance instance: currentNode.getArguments()){
            	OrdinaryVariable ov = instance.getOv(); 
            	int index = input.getResidentNodePointer().getOrdinaryVariableIndex(ov);
            	if(index > 0){
            	   OrdinaryVariable destination = resident.getOrdinaryVariableList().get(index);
            	   ssbnnode.addArgument(destination, instance.getEntity().getInstanceName());
            	}
            }
            
            parents.add(ssbnnode);
			
		}
		
		
		// recursive calling for parents
		for (SSBNNode ssbnnode : parents) {
			
//			TODO a reference for the father is necessary (context node avaliation)
            
			this.generateRecursive(ssbnnode, seen, net);	// algorithm's core
			
			currentNode.addParent(ssbnnode, true);
			if (ssbnnode.getProbNode() != null) {	// if this parent is not a finding
				net.addEdge(new Edge(currentNode.getProbNode(),ssbnnode.getProbNode()));
			}
			
		}
		
		Debug.println("Rede: "); 
		for(Edge edge: net.getEdges()){
			Debug.println(edge.getOriginNode().getName() + " -> " + edge.getDestinationNode().getName()); 
		}
		
		// TODO finish this method

		return currentNode;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.ssbn.SSBNGenerator#generateSSBN(unbbayes.prs.mebn.ssbn.Query)
	 */
	public ProbabilisticNetwork generateSSBN(Query query) throws SSBNNodeGeneralException {
		
		// As the query starts, let's clear the flags used by the previous query
		query.getMebn().clearMFragsIsUsingDefaultCPTFlag();
		
		// some data extraction
		
		SSBNNode querynode = query.getQueryNode();
		this.kb = query.getKb();

		// initialization
		
		ProbabilisticNetwork net = new ProbabilisticNetwork(query.getMebn().getName());
		
		// call recursive
		
		this.generateRecursive(querynode, new SSBNNodeList(), net);
		
		return net;
	}

}
