/**
 * 
 */
package unbbayes.prs.medg.ssid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import unbbayes.controller.INetworkMediator;
import unbbayes.controller.NetworkController;
import unbbayes.controller.mebn.IMEBNMediator;
import unbbayes.gui.NetworkWindow;
import unbbayes.prs.Edge;
import unbbayes.prs.Graph;
import unbbayes.prs.Network;
import unbbayes.prs.Node;
import unbbayes.prs.bn.IProbabilityFunction;
import unbbayes.prs.bn.IRandomVariable;
import unbbayes.prs.bn.JunctionTreeAlgorithm;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.SingleEntityNetwork;
import unbbayes.prs.bn.TreeVariable;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.prs.id.DecisionNode;
import unbbayes.prs.id.UtilityNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.exception.MEBNException;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.ssbn.IMediatorAwareSSBNGenerator;
import unbbayes.prs.mebn.ssbn.ISSBNGenerator;
import unbbayes.prs.mebn.ssbn.LiteralEntityInstance;
import unbbayes.prs.mebn.ssbn.OVInstance;
import unbbayes.prs.mebn.ssbn.Query;
import unbbayes.prs.mebn.ssbn.SSBN;
import unbbayes.prs.mebn.ssbn.SSBN.State;
import unbbayes.prs.mebn.ssbn.SSBNNode;
import unbbayes.prs.mebn.ssbn.SimpleSSBNNode;
import unbbayes.prs.mebn.ssbn.exception.ImplementationRestrictionException;
import unbbayes.prs.mebn.ssbn.exception.OVInstanceFaultException;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;
import unbbayes.prs.mebn.ssbn.laskeyalgorithm.LaskeyAlgorithmParameters;
import unbbayes.prs.mebn.ssbn.laskeyalgorithm.LaskeySSBNGenerator;
import unbbayes.prs.medg.MultiEntityDecisionNode;
import unbbayes.prs.medg.MultiEntityUtilityNode;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithm;

/**
 * This class delegates most of the operations to some {@link ISSBNGenerator},
 * and then substitutes probabilistic nodes to decision and utility nodes
 * when these were originated from {@link MultiEntityDecisionNode} or
 * {@link MultiEntityUtilityNode}.
 * @author Shou Matsumoto
 *
 */
public class DelegationSSIDGenerator implements IMediatorAwareSSBNGenerator, ISSBNGenerator {
	
	private INetworkMediator mediator = null;
	private ISSBNGenerator delegator;
	private IInferenceAlgorithm inferenceAlgorithm;
	
	private Logger log = Logger.getLogger(getClass());
	private boolean isToPreprocess = true;
	private boolean isToPostProcessDecisionNodes = false;

	/**
	 * Default constructor with no parameters is kept protected to allow inheritance.
	 */
	protected DelegationSSIDGenerator() {}
	
	/**
	 * Constructor method with no argument.
	 * {@link #setDelegator(ISSBNGenerator)} will be set to {@link LaskeySSBNGenerator} with SSBN compilation disabled
	 * @return new instance of generator which will do pre-processing, delegate construction to delegator, and then perform post processing.
	 */
	public static ISSBNGenerator getInstance() {
		LaskeyAlgorithmParameters parameters = new LaskeyAlgorithmParameters(); 
		parameters.setParameterValue(LaskeyAlgorithmParameters.DO_INITIALIZATION, "true");
		parameters.setParameterValue(LaskeyAlgorithmParameters.DO_BUILDER, "true"); 
		parameters.setParameterValue(LaskeyAlgorithmParameters.DO_PRUNE, "true"); 
		parameters.setParameterValue(LaskeyAlgorithmParameters.DO_CPT_GENERATION, "true"); 
		
//		// disable compilation of SSBN, because it will be converted to SSID and then compiled as influence diagram
//		parameters.setParameterValue(LaskeyAlgorithmParameters.COMPILE_AND_INITIALIZE_SSBN, "false");
		
		return DelegationSSIDGenerator.getInstance(new LaskeySSBNGenerator(parameters));
	}
	
	/**
	 * Constructor method
	 * @param delegator : construction of situation-specific network structure will be delegated to this
	 * @return new instance of generator which will do pre-processing, delegate construction to delegator, and then perform post processing.
	 */
	public static ISSBNGenerator getInstance(ISSBNGenerator delegator) {
		DelegationSSIDGenerator ret = new DelegationSSIDGenerator();
		ret.setDelegator(delegator);
		return ret;
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.mebn.ssbn.ISSBNGenerator#generateSSBN(java.util.List, unbbayes.prs.mebn.kb.KnowledgeBase)
	 */
	public SSBN generateSSBN(List<Query> listQueries, 
			KnowledgeBase kb) throws SSBNNodeGeneralException, 
			                         ImplementationRestrictionException, 
			                         MEBNException, 
			                         OVInstanceFaultException, 
			                         InvalidParentException {
		this.preprocess(listQueries, kb);
		SSBN ssbn = this.getDelegator().generateSSBN(listQueries, kb);
		ssbn = this.postprocess(listQueries, kb, ssbn);
		return ssbn;
	}
	

	/**
	 * Performs any necessary pre-processing: populate list of queries with utility nodes
	 * @param listQueries : queries passed to {@link #generateSSBN(List, KnowledgeBase)}. 
	 * This will be overwritten/appended, so this is an input and output argument.
	 * @param knowledgeBase : {@link KnowledgeBase} passed to {@link #generateSSBN(List, KnowledgeBase)}
	 */
	protected void preprocess(List<Query> listQueries, KnowledgeBase knowledgeBase) {
		
		if (!isToPreprocess()) {
			getLog().debug("Skipping preprocessing, because isToPreprocess flag is " + isToPreprocess());
			return;
		}
		
		// basic assertions
		if (listQueries == null || listQueries.isEmpty()) {
			getLog().warn("Preprocessing skipped because no query was provided.");
			return;
		}
		if (knowledgeBase == null) {
			getLog().warn("Preprocessing skipped because null knowledge base was provided.");
			return;
		}

		// Extract MTheory. Assume all the queries are referent to the same MTeory
		MultiEntityBayesianNetwork mebn = listQueries.get(0).getMebn(); 
		
		// Use hash to keep track of what queries are already present in list
		HashSet equivalentQueryTracker = new HashSet();
		for (Query query : listQueries) {
			// use generateStringIdentifier because we don't want to rely on Query#equals or Query#toString or Query#hash
			equivalentQueryTracker.add(this.generateIdentifier(query));
		}
		
		// add utility nodes to the list of initial nodes
		for(MFrag mfrag: mebn.getMFragList()){
			// we are creating one utility node per each possible combination of arguments in a continuous resident node
			for (Node node : mfrag.getNodes()) {
				if (node instanceof MultiEntityUtilityNode) {
					
					MultiEntityUtilityNode medgUtilityNode = (MultiEntityUtilityNode) node;
					if (medgUtilityNode.getOrdinaryVariableList() == null 
							|| medgUtilityNode.getOrdinaryVariableList().isEmpty()) {
						continue;	// go to next node
					}
					
					// prepare all possible combinations of arguments. The ovs are synchronized by index
					boolean hasAtLeastOneCombination = true;	// true if all Lists in combinator were filled
					List<List<LiteralEntityInstance>> combinator = new ArrayList<List<LiteralEntityInstance>>();
					for (OrdinaryVariable ov : medgUtilityNode.getOrdinaryVariableList()) {
						List<LiteralEntityInstance> contentOfCombinator = new ArrayList<LiteralEntityInstance>();
						// get all values for ov
						List<String> ovValues = knowledgeBase.getEntityByType(ov.getValueType().getName());
						if (ovValues.isEmpty()) {
							// this iterator cannot be filled, because ov has no possible values
							hasAtLeastOneCombination = false;
							break;
						}
						// add all possible values for ov
						for (String ovValue : ovValues) {
							contentOfCombinator.add(LiteralEntityInstance.getInstance(ovValue, ov.getValueType()));
						}
						combinator.add(contentOfCombinator);
					}
					
					// do not add node if there is some ov with no possible value (that means, no combination can be created)
					if (!hasAtLeastOneCombination) {
						continue;	// go to next continuous node
					}
					
					// list of indexes of combinator
					List<Integer> indexes = new ArrayList<Integer>(combinator.size());
					for (int i = 0; i < combinator.size(); i++) {
						// Note that at this point, each list in combinator has at least 1 element, because hasAtLeastOneCombination == true (so, 0 is a valid index)
						indexes.add(0);
					}
					
					/*
					 * iterate on combinator using indexes. 
					 * E.g.:	combinator = {{a,b},{1,2}}; 
					 * 			then indexes must iterate like:
					 * 			{0,0}	(means (a,1));
					 * 			{1,0}	(means (b,1));
					 * 			{0,1}	(means (a,2));
					 * 			{1,1}	(means (b,2));
					 * 	
					 */
					while (hasAtLeastOneCombination) {
						
						// create a query for each possible combination of combinator
						
						// fill arguments of query
						List<OVInstance> arguments = new ArrayList<OVInstance>();
						for (int i = 0; i < medgUtilityNode.getOrdinaryVariableList().size(); i++) {
							arguments.add(OVInstance.getInstance(medgUtilityNode.getOrdinaryVariableList().get(i), combinator.get(i).get(indexes.get(i))));
						}
						Query query = new Query(medgUtilityNode, arguments);
						
						// if equivalent query is not already present in list, include it
						Object identifier = generateIdentifier(query);
						if (!equivalentQueryTracker.contains(identifier)) {
							listQueries.add(query);
							equivalentQueryTracker.add(identifier);
						}
						
						// update indexes and check condition to end loop (it ends when all possible combinations were visited)
						for (int i = 0; i < combinator.size(); i++) {	
							// OBS. combinator, indexes and continuousResidentNode.getOrdinaryVariableList() have same size and are synchronized.
							Integer newIndex = indexes.get(i) + 1;	// obtain current step + 1
							// reset index if there is no mode element in this list
							if (newIndex >= combinator.get(i).size()) {
								// but if this is the last list, there is no more possible combination
								if (i >= combinator.size() - 1) {
									// there is no more combination
									hasAtLeastOneCombination = false;
									break;
								} else {
									newIndex = 0;	// reset
								}
							} 
							indexes.set(i, newIndex);	// update step 
							if (newIndex > 0) {
								// if we did not reset this index, we do not need to update the following indexes
								break;
							}
						}
						
					}	// while only ends when combinator was completely iterated
				}	// if node is a continuous resident node
			}	// for each node in mfrag
		}	// for each mfrag
		
	}
	
	/**
	 * @param query : query to be used to generate identifier
	 * @return Generates string which will be equal if a query represents same instance of situation-specific node.
	 * It is just a string containing name of resident node and list of arguments
	 */
	protected Object generateIdentifier(Query query) {
		
		// the following is almost the same of query.toString() of MEBN plug-in, 
		// but we should not rely on query.toString() to be the same for different implementations/plug-ins and future versions
		// so it was re-implemented
		
		String string = "";
		
		if (query.getResidentNode() == null) {
			string += "<NULL RESIDENT>"; 
		} else {
			string += query.getResidentNode().getName(); 
		}
		
		string += "(";
		if (query.getArguments() != null) {
			for (OVInstance argument: query.getArguments()) {
				if (argument == null) {
					continue;
				}
				String ovName = "<NULL OV>";
				if (argument.getOv() != null) {
					ovName = argument.getOv().getName();
				}
				String instanceName = "<NULL INSTANCE>";
				if (argument.getEntity() != null) {
					instanceName = argument.getEntity().getInstanceName();
				}
				string += ovName + "=" + instanceName + " "; 
			}
		}
		string += ")";
		
		return string;
	}

	/**
	 * Performs any necessary post processing: setup of {@link #getMediator()}, propagation of findings, handling of decision nodes, etc.
	 * @param listQueries : queries used in {@link #generateSSBN(List, KnowledgeBase)}
	 * @param kb : kb used in {@link #generateSSBN(List, KnowledgeBase)}
	 * @param ssbn : ssbn generated by {@link #generateSSBN(List, KnowledgeBase)} of {@link #getDelegator()}
	 * @return {@link SSID} generated from post-processing ssbn
	 * @throws SSBNNodeGeneralException when failed to set up findings at {@link #setUpFindings(SSBN, IInferenceAlgorithm)}
	 * @throws InvalidParentException  from {@link #convertToInfluenceDiagram(SSBN)}
	 * @see #convertToInfluenceDiagram(SSBN)
	 * @see #postProcessDecisionNodes(SSID)
	 * @see #setUpFindings(SSBN, IInferenceAlgorithm)
	 */
	protected SSBN postprocess(List<Query> listQueries, KnowledgeBase kb, SSBN ssbn) throws SSBNNodeGeneralException, InvalidParentException {
		
		// substitute some probabilistic nodes with decision and utility nodes
		ssbn = this.convertToInfluenceDiagram(ssbn);
		
		// make sure ordering of decision nodes are plausible
		this.postProcessDecisionNodes(ssbn);
		
		// this is the actual ID (i.e. SSID) to compile
		Network influenceDiagram = ssbn.getNetwork();
		
		// If it is a SingleEntityNetwork, make sure all variables are properly initialized
		if (influenceDiagram instanceof SingleEntityNetwork) {
			SingleEntityNetwork singleEntityNetwork = (SingleEntityNetwork) influenceDiagram;
			singleEntityNetwork.resetNodesCopy();
			singleEntityNetwork.resetEvidences();
		}
		
		// actually compile network
		IInferenceAlgorithm algorithm = getSituationSpecificInferenceAlgorithm();
		algorithm.setNetwork(influenceDiagram);
		algorithm.run();
		
		if (algorithm.getNetwork() != null && (algorithm.getNetwork() instanceof Network)) {
			// algorithm may create new network. SSID must be linked to the new network
			ssbn.setNetwork((Network) algorithm.getNetwork());	
		}
		
		// check if GUI is enabled (it is enabled if there is a mediator -- class making interface between this class and GUI)
		NetworkWindow idModule = null;	// this is going to hold the ID module (NetworkWindow is a UnBBayesModule)
		if (getMediator() != null) {
			
			// Since we are calling this algorithm from GUI, set up the whole BN module instead of just the inference algorithm
			idModule = new NetworkWindow(ssbn.getNetwork());	// Instantiate a new BN module
			
			// extract controller from BN module
			NetworkController controller = idModule.getController();
			try {
				algorithm.setMediator(controller);
			} catch (Exception e) {
				// algorithm should work without mediator, so let's ignore and try going on
				e.printStackTrace();
			}
			controller.setInferenceAlgorithm(algorithm);
			
			// Make sure the tree (JTree in the left side of compilation panel) is updated with the network changes, if there is any.
			controller.getScreen().getEvidenceTree().resetTree();
			
			// change BN module to the "compiled" view instead of "edit" view
			idModule.changeToPNCompilationPane();
		
			// add/show popup
			getMediator().getScreen().getUnbbayesFrame().addWindow(idModule);
			idModule.setVisible(true);
			idModule.updateUI();
			getMediator().getScreen().getUnbbayesFrame().repaint();
			idModule.repaint();
		}
		
		// fill findings and propagate. This should be performed after GUI is instantiated (if GUI is instantiated at all)
		this.setUpFindings(ssbn, algorithm);
		
		// if GUI was instantiated, we need to pop-up GUI window and update screen now (because evidences were propagated)
		if (idModule != null && getMediator() != null) {
			if (getMediator() instanceof IMEBNMediator) {
				// the following code forces GUI to understand that it should not display an SSBN in its card layout panel
				// this is because the network is displayed as a popup		
				((IMEBNMediator)getMediator()).setToTurnToSSBNMode(false);
				((IMEBNMediator)getMediator()).setSpecificSituationBayesianNetwork(null);	// free any previous ssbn/ssid
			}
			// add/show popup
			getMediator().getScreen().getUnbbayesFrame().addWindow(idModule);
			idModule.setVisible(true);
			idModule.updateUI();
			getMediator().getScreen().getUnbbayesFrame().repaint();
			idModule.repaint();
		}
		
		return ssbn;
	}
	

	/**
	 * Nodes that were generated from {@link MultiEntityDecisionNode} and {@link MultiEntityUtilityNode}
	 * will be converted to {@link DecisionNode} and {@link UtilityNode}.
	 * This method will NOT generate new instances of {@link SSID} or {@link SSIDNode}.
	 * It will only make changes to {@link SSBN#getNetwork()} directly.
	 * @param ssbn : ssbn whose {@link SSBN#getNetwork()} will be modified.
	 * @return the same instance of ssbn input argument
	 * @throws InvalidParentException this method adds parents to nodes, and this exception will be thrown if such inclusion is invalid
	 * @see #postprocess(List, KnowledgeBase, SSBN)
	 */
	protected SSBN convertToInfluenceDiagram(SSBN ssbn) throws InvalidParentException {
		
		// extract network to modify
		Network net = ssbn.getNetwork();
		
		// make a mapping from node in net to node in ssbn
		Map<Node, SSBNNode> ssbnNodeMap = new HashMap<Node, SSBNNode>();
		for (SSBNNode ssbnNode : ssbn.getSsbnNodeList()) {
			ssbnNodeMap.put(ssbnNode.getProbNode(), ssbnNode);
		}
		
		// only deal with nodes that were actually included to network (i.e. ignore nodes in SSBN which were omitted in final Network)
		// iterate on a new/cloned list, because original list of nodes can be modified in the iteration
		for (ProbabilisticNode oldNode : new ArrayList<ProbabilisticNode>((List)net.getNodes())) {	
			SSBNNode ssbnNode = ssbnNodeMap.get(oldNode);
			Node newNode = null;
			PotentialTable utilityTableToFill = null;
			if (ssbnNode.getResident() instanceof MultiEntityDecisionNode) {
				// substitute with decision node;
				newNode = new DecisionNode();
			} else if (ssbnNode.getResident() instanceof MultiEntityUtilityNode) {
				// substitute with utility node;
				newNode = new UtilityNode();
				// make sure the utility table will be copied later
				utilityTableToFill = ((UtilityNode)newNode).getProbabilityFunction();
				// utility node is modeled as random variable, so needs an internal identifier
				((UtilityNode)newNode).setInternalIdentificator(oldNode.getInternalIdentificator());
			}	// else keep unchanged (null)
			
			if (newNode != null) {
				
				newNode.setLabel(oldNode.getLabel());
				newNode.setPosition(oldNode.getPosition().getX(), oldNode.getPosition().getY());
				newNode.setName(oldNode.getName());
				newNode.setDescription(oldNode.getDescription());
				newNode.removeStates();
				for (int stateIndex = 0; stateIndex < oldNode.getStatesSize(); stateIndex++) {
					newNode.appendState(oldNode.getStateAt(stateIndex));
				}
				
				// handle parents
				for (Node parent : oldNode.getParents()) {
					// substitute arcs from parents
					Edge oldEdge = net.getEdge(parent, oldNode);
					Edge newEdge = new Edge(parent, newNode);
					int oldEdgeIndex = net.getEdges().indexOf(oldEdge);
					Edge substitutedEdge = net.getEdges().set(oldEdgeIndex, newEdge);
					if (substitutedEdge != oldEdge) {
						throw new RuntimeException("Edge to substitute was " + oldEdge + ", but actually deleted " + substitutedEdge);
					}
					
					// add parent
					newNode.addParent(parent);
					// add parent to table;
					if (utilityTableToFill != null) {
						utilityTableToFill.addVariable(parent);
					}
				}
				
				// handle children
				for (Node child : oldNode.getChildren()) {

					// substitute arcs to children
					Edge oldEdge = net.getEdge(oldNode, child);
					Edge newEdge = new Edge(newNode, child);
					int oldEdgeIndex = net.getEdges().indexOf(oldEdge);
					Edge substitutedEdge = net.getEdges().set(oldEdgeIndex, newEdge);
					if (substitutedEdge != oldEdge) {
						throw new RuntimeException("Edge to substitute was " + oldEdge + ", but actually deleted " + substitutedEdge);
					}
					
					// add child
					newNode.addChild(child);
				
					// set children's parent
					// TODO consider case when child.getParents() returns immutable list or does not change original list
					List<Node> parentsOfChild = child.getParents();
					int oldNodeIndex = parentsOfChild.indexOf(oldNode);
					Node substitutedNode = parentsOfChild.set(oldNodeIndex, newNode);
					if (substitutedNode != oldNode) {
						throw new RuntimeException("Parent of child " + child + " to substitute was " + oldNode + ", but actually deleted " + substitutedNode);
					}
					
					// update children's table (replace variable)
					if (child instanceof IRandomVariable) {
						// extract the probability/utililty function of child
						IProbabilityFunction childFunction = ((IRandomVariable) child).getProbabilityFunction();
						// find index of old node in function
						boolean found = false;	// just to make sure we found old node in child's table
						for (int index = 0; index < childFunction.variableCount(); index++) {
							if (childFunction.getVariableAt(index).equals(oldNode)) {
								// found old node. Substitute
								childFunction.setVariableAt(index, newNode);
								found = true;
								break;
							}
						}
						if (!found) {
							throw new RuntimeException("Old parent " + oldNode + " not found in table of child " + child);
						}
					}
				}
				
				// copy table content (i.e. numbers in cells)
				if (utilityTableToFill != null) {
					
					PotentialTable tableToCopy = oldNode.getProbabilityFunction();
					
					// sanity checks
					if (utilityTableToFill.tableSize() != tableToCopy.tableSize()) {
						throw new IllegalStateException("Table size inconsistency at node " + oldNode 
								+ ". Original table's size = " + tableToCopy.tableSize() + ", cloned table's size = " + utilityTableToFill.tableSize());
					}
					if (utilityTableToFill.variableCount() != tableToCopy.variableCount()) {
						throw new IllegalStateException("Inconsistency of number of variables in utility table of " + oldNode 
								+ ". Vars in original table = " + tableToCopy.variableCount() + ", vars in cloned table = " + utilityTableToFill.variableCount());
					}
					for (int varIndex = 0; varIndex < tableToCopy.variableCount(); varIndex++) {
						// compare variables and orderings by name (because they may be different)
						if (!tableToCopy.getVariableAt(varIndex).getName().equalsIgnoreCase(utilityTableToFill.getVariableAt(varIndex).getName())) {
							throw new IllegalStateException("Inconsistency of variable order in utility table of " + oldNode 
									+ " at index " + varIndex
									+ ". Var in original table = " + tableToCopy.getVariableAt(varIndex) + ", var in cloned table = " + utilityTableToFill.getVariableAt(varIndex));
						}
					}
					
					utilityTableToFill.setValues(tableToCopy.getValues());
				}
			}
		}
		
		// just return the same instance of input argument
		return ssbn;
	}


	/**
	 * This method may reorder decision nodes or create new arcs between decision nodes if the underlying ID algorithm
	 * requires such ordering.
	 * This method will NOT add links/arcs/parents to {@link SSBNNode}.
	 * It will only make changes to {@link SSBN#getNetwork()}.
	 * @param ssid : {@link SSBN#getNetwork()} of this instance will be modified.
	 * @see #postprocess(List, KnowledgeBase, SSBN)
	 */
	protected void postProcessDecisionNodes(SSBN ssid) {
		if (!isToPostProcessDecisionNodes) {
			getLog().debug("Skipping post processing of decision nodes, because isToPostProcessDecisionNodes flag is " + isToPostProcessDecisionNodes);
			return;
		}
		
		if (ssid == null) {
			getLog().warn("No SSID was specified. Skip post processing of decision nodes.");
			return;
		}
		
		// extract network to process
		ProbabilisticNetwork net = ssid.getProbabilisticNetwork();
		if (net == null) {
			getLog().warn("No probabilistic network associated with SSID " + ssid + ". Skip post processing of decision nodes.");
			return;
		}
		
		// TODO eventually migrate following code to core (JunctionTreeAlgorithm)
		
		
		// check that reordering is needed. Use junction tree algorithm's method for checking ordering of decision nodes
		JunctionTreeAlgorithm decisionOrderChecker = new JunctionTreeAlgorithm(net);
		decisionOrderChecker.setDecisionTotalOrderRequired(true);
		// the following throws exception if total ordering of nodes was not possible to establish
		try {
			decisionOrderChecker.sortDecisionNodes(net);
			// if this ran fine, then decision nodes are linearly ordered
			getLog().debug("Decision nodes are already ordered. Skip post processing of decision nodes in network " +  net);
			return;
		} catch (Exception e) {
			getLog().debug("Reordering decision nodes in SSID " + ssid, e);
		}
		
		// convert to net containing only decision nodes;
		
		// connect disconnected decision nodes;
		
		// for each decision node, connect parents and children;
		
		
	}

	/**
	 * initializes and propagates findings.
	 * It basically translates MEBN findings to DMP findings.
	 * It is desired that the network is already compiled.
	 * CAUTION: the {@link ProbabilisticNetwork} linked to ssbn may not
	 * be the same of the {@link ProbabilisticNetwork} treated by the algorithm.
	 * @param ssbn	: the network generated by the MEBN portion
	 * @param algorithm	: the algorithm responsible for compiling the ssbn
	 * @throws SSBNNodeGeneralException 
	 * @see {@link #postprocess(List, KnowledgeBase, SSBN)}
	 */
	protected void setUpFindings(SSBN ssbn, IInferenceAlgorithm algorithm) throws SSBNNodeGeneralException {
		// initial assertion
		if (ssbn == null || algorithm == null) {
			throw new SSBNNodeGeneralException(new IllegalArgumentException(this.getClass() + ": SSBN == " + ssbn + "; algorithm == " + algorithm)); // NOPMD by Shou Matsumoto on 15/10/16 10:13
		}

		// TODO check if a reset is necessary
//		algorithm.reset();
		
		// findings of discrete nodes may be directly inserted to the network treated by the algorithm
		Graph g = algorithm.getNetwork();	// net to add findings
		if (g == null) {
			throw new SSBNNodeGeneralException(new IllegalStateException("No network is associated with the underlying ID compilation algorithm." 
					+ ssbn + " and " + algorithm + " should be compiled before this method."));
		}
		
		if (!(g instanceof ProbabilisticNetwork)) {
			throw new SSBNNodeGeneralException(new ClassCastException("The underlying ID compiler is associated with an instance of " + g.getClass().getName()+
					", which is not compatible with " + ProbabilisticNetwork.class.getName()));
		}

		ProbabilisticNetwork net = (ProbabilisticNetwork) g;
		boolean isToPropagate = false;	// this will become true if at least 1 valid finding was found
		if (ssbn.getFindingList() != null) {
			for(SimpleSSBNNode ssbnFindingNode: ssbn.getFindingList()){
				//Not all findings nodes are at the network. 
				if(ssbnFindingNode.getProbNode()!= null){ 
					// extract node and finding state from the network managed by the algorithm
					TreeVariable node = (TreeVariable)net.getNode(ssbnFindingNode.getProbNode().getName());
					String stateName = ssbnFindingNode.getState().getName(); 
					
					// add discrete findings directly to the network managed by the inference algorithm
					// unfortunately, the network managed by the algorithm and the one linked to the SSBN may not be the same (because algorithm may instantiate another network)
					boolean isStateInNode = false; 	// indicates if node contains the specified state (true if evidence is to a valid state)
					for(int i = 0; i < node.getStatesSize(); i++){
						// check if the name of the state in the SSID node is the same in the node in actual network managed by the algorithm
						if(node.getStateAt(i).equals(stateName)){
							node.addFinding(i);
							isStateInNode = true; 
							isToPropagate = true;
							break; 
						}
					}
					if(!isStateInNode){
						throw new SSBNNodeGeneralException(node + " has no state for finding: " + stateName); 
					}
					
				}
			}
		}
		ssbn.setState(State.WITH_FINDINGS); 
		
		
		// update evidences
		if (isToPropagate) {
			algorithm.propagate();
		}
		ssbn.setState(State.FINDINGS_PROPAGATED); 
	
	
	}



	
	
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.mebn.ssbn.ISSBNGenerator#getLastIterationCount()
	 */
	public int getLastIterationCount() {
		getLog().trace("Detected invocation of getLastIterationCount. Returning default value = 1 ");
		return 1;
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.mebn.ssbn.ISSBNGenerator#setLogEnabled(boolean)
	 */
	public void setLogEnabled(boolean isEnabled) {
		// to disable, override log4j properties
		if (!isEnabled) {
			getLog().setLevel((Level)Level.OFF);
		} else {
			// if it's enabled, just keep property read from log4j. 
			// But if it's off, turn it on.
			if (Level.OFF.isGreaterOrEqual(getLog().getLevel())) {
				// trace is considered finer than debug
				getLog().setLevel((Level)Level.TRACE);
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.mebn.ssbn.ISSBNGenerator#isLogEnabled()
	 */
	public boolean isLogEnabled() {
		if (getLog() == null) {
			return false;
		}
		// just check if it is not off
		return !Level.OFF.isGreaterOrEqual(getLog().getLevel());
	}
	

	/**
	 * @param inferenceAlgorithm : inference algorithm to be used to compile situation specific instances (i.e. SSID).
	 * It is a {@link JunctionTreeAlgorithm} by default if set to null.
	 * @see #getSituationSpecificInferenceAlgorithm()
	 * @see #setUpFindings(SSBN, IInferenceAlgorithm)
	 */
	public void setSituationSpecificInferenceAlgorithm(IInferenceAlgorithm inferenceAlgorithm) {
		this.inferenceAlgorithm = inferenceAlgorithm;
		getLog().trace("SSID inference algorithm set to " + inferenceAlgorithm);
	}

	/**
	 * @return : inference algorithm to be used to compile situation specific instances (i.e. SSID).
	 * It is a {@link JunctionTreeAlgorithm} by default if set to null.
	 * @see #setSituationSpecificInferenceAlgorithm(IInferenceAlgorithm)
	 * @see #setUpFindings(SSBN, IInferenceAlgorithm)
	 */
	public IInferenceAlgorithm getSituationSpecificInferenceAlgorithm() {
		if (inferenceAlgorithm == null) {
			inferenceAlgorithm = new JunctionTreeAlgorithm();
		}
		return inferenceAlgorithm;
	}


	/**
	 * @return the mediator
	 */
	public INetworkMediator getMediator() {
		return this.mediator;
	}

	/**
	 * @param mediator the mediator to set
	 */
	public void setMediator(INetworkMediator mediator) {
		getLog().trace("Mediator changed to " + mediator);
		this.mediator = mediator;
	}
	


	/**
	 * @return creation of situation specific networks will be delegated to this instance after preprocessing and before post-processing.
	 * 
	 */
	public ISSBNGenerator getDelegator() {
		return delegator;
	}

	/**
	 * @param delegator : creation of situation specific networks will be delegated to this instance after preprocessing and before post-processing
	 */
	public void setDelegator(ISSBNGenerator delegator) {
		this.delegator = delegator;
	}

	/**
	 * @return the log
	 */
	public Logger getLog() {
		if (log == null) {
			log = Logger.getLogger(getClass());
		}
		return log;
	}

	/**
	 * @param log the log to set
	 */
	public void setLog(Logger log) {
		this.log = log;
	}

	/**
	 * This flag should be set to false if {@link #preprocess(List, KnowledgeBase)} should be skipped.
	 * @return the isToPreprocess
	 */
	public boolean isToPreprocess() {
		return isToPreprocess;
	}

	/**
	 * This flag should be set to false if {@link #preprocess(List, KnowledgeBase)} should be skipped.
	 * @param isToPreprocess the isToPreprocess to set
	 */
	public void setToPreprocess(boolean isToPreprocess) {
		this.isToPreprocess = isToPreprocess;
	}

	/**
	 * @return if false, {@link #postProcessDecisionNodes(SSBN)} will be skipped.
	 */
	public boolean isToPostProcessDecisionNodes() {
		return isToPostProcessDecisionNodes;
	}

	/**
	 * @param isToPostProcessDecisionNodes : if false, {@link #postProcessDecisionNodes(SSBN)} will be skipped.
	 */
	public void setToPostProcessDecisionNodes(boolean isToPostProcessDecisionNodes) {
		this.isToPostProcessDecisionNodes = isToPostProcessDecisionNodes;
	}
	
	
	

}
