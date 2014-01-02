/**
 * 
 */
package unbbayes.gui.mebn.extension.ssbn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import unbbayes.gui.NetworkWindow;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.prs.mebn.Argument;
import unbbayes.prs.mebn.IMultiEntityNode;
import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.exception.MEBNException;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.ssbn.ISSBNGenerator;
import unbbayes.prs.mebn.ssbn.LiteralEntityInstance;
import unbbayes.prs.mebn.ssbn.OVInstance;
import unbbayes.prs.mebn.ssbn.Query;
import unbbayes.prs.mebn.ssbn.SSBN;
import unbbayes.prs.mebn.ssbn.exception.ImplementationRestrictionException;
import unbbayes.prs.mebn.ssbn.exception.OVInstanceFaultException;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;
import unbbayes.prs.mebn.ssbn.laskeyalgorithm.LaskeyAlgorithmParameters;
import unbbayes.prs.mebn.ssbn.laskeyalgorithm.LaskeySSBNGenerator;
import unbbayes.util.Debug;

/**
 * @author Shou Matsumoto
 *
 */
public class FullSSBNGenerator extends LaskeySSBNGenerator {
	
	/**
	 * This is a default instance of {@link LaskeyAlgorithmParameters} to be used by {@link FullSSBNGenerator}.
	 * This instance basically returns "true" for everything, except for {@link LaskeyAlgorithmParameters#DO_PRUNE}
	 * (will return "false" in such case).
	 */
	public static final LaskeyAlgorithmParameters DEFAULT_ALGORITHM_PARAM = new LaskeyAlgorithmParameters() {
		public String getParameterValue(int parameter) {
			if (parameter == LaskeyAlgorithmParameters.DO_PRUNE) {
				return "false";
			}
			return "true";
		}
	};

	/**
	 * default constructor is kept protected in order to facilitate inheritance
	 */
	protected FullSSBNGenerator() {
		// initialize with default parameters
		super(DEFAULT_ALGORITHM_PARAM);
	}
	
	/**
	 * Default constructor method.
	 * @return an instance of {@link ISSBNGenerator}
	 */
	public static ISSBNGenerator getInstance() {
		return new FullSSBNGenerator();
	}

	/**
	 * Query all leaf nodes, no matter what is the provided list of query.
	 * @see unbbayes.prs.mebn.ssbn.laskeyalgorithm.LaskeySSBNGenerator#generateSSBN(java.util.List, unbbayes.prs.mebn.kb.KnowledgeBase)
	 */
	public SSBN generateSSBN(List<Query> queryList, KnowledgeBase knowledgeBase)
			throws SSBNNodeGeneralException,
			ImplementationRestrictionException, MEBNException,
			OVInstanceFaultException, InvalidParentException {
		
		// this is the MEBN project we are working with
		MultiEntityBayesianNetwork mebn = null;
		// extract instance either from controller, or from a query
		if (getMediator() != null) {
			mebn = (MultiEntityBayesianNetwork) getMediator().getNetwork();
		} else if (queryList != null && !queryList.isEmpty()) {
			// extract MEBN from the 1st object in the list of query
			mebn = queryList.get(0).getMebn();
		}
		
		return super.generateSSBN(
				getCombinatorialArgumentQueryList(			// query permutation of leaf nodes
						getLeafMultiEntityNodes(mebn),		// the leaf nodes in mebn
						knowledgeBase						// use the provided database
					),
				knowledgeBase								// use the same instance of database
			);
	}

	/**
	 * @param leafMultiEntityNodes : nodes to be queried.
	 * @param database : database where we can retrieve instances of entities.
	 * @return a list of instances of {@link Query} that were generated from the list of nodes provided in the arguments.
	 * Its arguments are filled with a permutation/combination of all possible entity instances that could be retrieved
	 * from a database.
	 * For example, if we are querying A(x,y) and B(z); and from the database we know that x = {a,b}, y = {c,d,e}, and z = {f};
	 * then this method will return queries for A(a,c), A(a,d), A(a,e), A(b,c), A(b,d), A(b,e), and B(f).
	 */
	protected List<Query> getCombinatorialArgumentQueryList( List<IMultiEntityNode> leafMultiEntityNodes, KnowledgeBase database) {
		
		// basic assertions
		if (database == null) {
			throw new NullPointerException("An instance of KnowledgeBase must be provided.");
		}
		
		// list to be returned by this method
		List<Query> ret = new ArrayList<Query>();
		
		if (leafMultiEntityNodes != null) {
			for (IMultiEntityNode multiEntityNode : leafMultiEntityNodes) {
				// we assume that there is no duplicate node in leafMultiEntityNodes
				
				// the current implementation only allows to query resident nodes
				ResidentNode residentNode = null;
				if (multiEntityNode instanceof ResidentNode) {
					residentNode = (ResidentNode) multiEntityNode;
				} else {
					throw new IllegalArgumentException("Current implementation only allows queries in resident nodes, but found " + multiEntityNode);
				}
				
				// all possible instances for each argument of the resident node. The database will be used to fill values
				Map<Argument, List<OVInstance>> possibleSSBNArgumentsMap = new HashMap<Argument, List<OVInstance>>();
				
				// for each argument of the resident node, get its possible instances
				for (Argument argument : residentNode.getArgumentList()) {
					
					// database.getEntityByType can be used to obtain entity instances if the argument is a name of a type of an ordinary variable
					List<String> instanceNames = database.getEntityByType(argument.getOVariable().getValueType().getName());	// it is assumed that there is no redundancy
					
					// this list will contain instances of ordinary variables, and will be used to fill map possibleSSBNArgumentsMap
					List<OVInstance> instanceList = new ArrayList<OVInstance>(instanceNames.size());
					
					// fill instanceList
					for (String instanceName : instanceNames) {
						instanceList.add(
								OVInstance.getInstance(
										argument.getOVariable(), 
										LiteralEntityInstance.getInstance(instanceName, argument.getOVariable().getValueType())
									)
							);
					}
					
					possibleSSBNArgumentsMap.put(argument, instanceList);
					
				}	// end of "for" loop
				
				// create a query for each possible combination of instances in possibleSSBNArgumentsMap
				
				// calculate how many combinations there are
				// for example, if resident node is A(x,y) and x = {a,b} and y = {c,d,e}, then possible instances of A are 
				// 6 combinations: A(a,c), A(a,d), A(a,e), A(b,c), A(b,d), A(b,e)
				int numCombinations = 1;
				for (List<OVInstance> instancesPerArgument : possibleSSBNArgumentsMap.values()) {
					// the number of combination of instances is simply the product of how many instances there are for each argument
					numCombinations *= instancesPerArgument.size();
				}
				
				// If resident node is A(x,y) and x = {a,b} and y = {c,d,e}, access in the following order:
				// A(a,c), A(b,c), A(a,d), A(b,d), A(a,e), A(b,e).
				for (int linearIndex = 0; linearIndex < numCombinations; linearIndex++) {
					// list indicating what arguments have which values
					List<OVInstance> ovInstanceList = new ArrayList<OVInstance>();
					// The following variable will be used in order to convert linearizedCombinationIndex to indexes of possible values of arguments.
					// In other words, will be used to convert linear index to multi-dimensional index.
					// For example, if resident node is A(x,y) and x = {a,b} and y = {c,d,e}, then combinations are {A(a,c), A(b,c), A(a,d), A(b,d), A(a,e), A(b,e)}.
					// Consequently, the combination at index 4 (i.e. A(a,e)) has value of x at index 4%2=0, and value of y at index 4/2%3=2.
					// In general, the combination at index i has value of the n-th argument at index i/(PRODUCT(<sizes of previous n-1 arguments>)) % <size of n-th argument>
					int productOfPreviousInstancesSize = 1;	// this will be the "PRODUCT(<sizes of previous n-1 arguments>)"
					// keep original ordering of arguments (i.e. arguments in same order of resident node), just in case Query requires such ordering
					for (Argument argument : residentNode.getArgumentList()) {
						// extract possible values of current argument
						List<OVInstance> possibleInstances = possibleSSBNArgumentsMap.get(argument);
						ovInstanceList.add(possibleInstances.get((linearIndex / productOfPreviousInstancesSize) % possibleInstances.size()));
						productOfPreviousInstancesSize *= possibleInstances.size();
					}
					ret.add(new Query(residentNode, ovInstanceList));
				} // end of for linearIndex
				
			}	// end of for each leafMultiEntityNodes
		} // end of if leafMultiEntityNodes != null
		
		return ret;
	}

	/**
	 * @param mebn: MEBN project where we are going to look for nodes.
	 * @return : nodes (resident nodes) that do not have children.
	 */
	protected List<IMultiEntityNode> getLeafMultiEntityNodes(MultiEntityBayesianNetwork mebn) {
		// basic assertion
		if (mebn == null) {
			throw new IllegalArgumentException("Instance of MultiEntityBayesianNetwork must be provided (either from controller or from the query).");
		}
		
		// use a set, so that we don't have redundancies
		Set<IMultiEntityNode> ret = new HashSet<IMultiEntityNode>();
		
		List<ResidentNode> residentNodes = mebn.getDomainResidentNodes();
		if (residentNodes == null) {
			// there is nothing to return, so return the empty list
			return Collections.emptyList();
		}
		
		// obtain resident nodes that do not have children
		for (ResidentNode residentNode : residentNodes) {
			// check if there is no children in the same mfrag
			if (residentNode.getResidentNodeChildList() == null || residentNode.getResidentNodeChildList().isEmpty()) {
				// check if there is no children in different mfrag (i.e. whether there are children of input nodes)
				boolean hasInputChild = false;
				if (residentNode.getInputInstanceFromList() != null) {
					for (InputNode inputNode : residentNode.getInputInstanceFromList()) {
						if (inputNode.getResidentNodeChildList() != null && !inputNode.getResidentNodeChildList().isEmpty()) {
							hasInputChild = true;
							break;
						}
					}
				}
				if (!hasInputChild) {
					// there is no children at all, so include in the list to return
					ret.add(residentNode);
				}
			}
		}
		
		return new ArrayList<IMultiEntityNode>(ret);
	}
	
	/**
	 * Overwrite superclass so that it won't compile the network (to a Junction tree), because a network generated
	 * by this SSBN generator is likely to be too complex to be compiled/propagated by a junction tree algorithm 
	 * in a reasonable amount of time.
	 * @see unbbayes.prs.mebn.ssbn.laskeyalgorithm.LaskeySSBNGenerator#compileAndInitializeSSBN(unbbayes.prs.mebn.ssbn.SSBN)
	 */
	protected void compileAndInitializeSSBN(SSBN ssbn) throws Exception {
		Debug.println(getClass(), "Not compiling this SSBN to a Junction Tree, because networks generated by this SSBN generator are likely to be huge/complex.");
	}
	
	/**
	 * Pop-up new window for the BN (like if it were a new BN project), instead of using the default SSBN window.
	 * @see unbbayes.prs.mebn.ssbn.laskeyalgorithm.LaskeySSBNGenerator#showSSBN(unbbayes.prs.mebn.ssbn.SSBN)
	 */
	protected void showSSBN(SSBN ssbn) {
		if (this.getMediator() == null) {
			// if there is no mediator (i.e. a controller), we cannot show this SSBN to GUI
			return;
		}
		NetworkWindow window = new NetworkWindow(ssbn.getNetwork());
		this.getMediator().getScreen().getUnbbayesFrame().addWindow(window);
		window.setVisible(true);
	}

}
