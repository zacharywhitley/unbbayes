/**
 * 
 */
package unbbayes.io;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import unbbayes.prs.Node;
import unbbayes.prs.bn.AssetNetwork;
import unbbayes.prs.bn.AssetNode;
import unbbayes.prs.bn.JeffreyRuleLikelihoodExtractor;
import unbbayes.prs.bn.JunctionTreeAlgorithm;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.TreeVariable;
import unbbayes.prs.bn.inference.extension.AssetAwareInferenceAlgorithm;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.util.Debug;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithm;
import au.com.bytecode.opencsv.CSVReader;

/**
 * Based on a Bayesian network and a csv file in DAGGRE format,
 * 
 * @author Shou Matsumoto
 *
 */
public class DAGGREQuestionReader  {

	private boolean isToPropagate = true;
	private boolean isToCreateUserAssetNet = true;

	/**
	 * @param name
	 */
	public DAGGREQuestionReader() {
		super();
	}

	/**
	 * Method implemented from Dr. Charles Twardy's (ctwardy@c4i.gmu.edu) requirements:
	 * Assume everything is binary for a first test.
	 * The columns of the csv file are: QuestionID, Timestamp, UserID, new Probability.  
	 * All probabilities started at uniform. 
	 * Output should be a set of probabilities for each question, the number of edits processed, and a runtime.  
	 * Note: the runtime can be obtained by calling the system time info from the caller
	 * @param csv : csv file to be read
	 * @param net : bayesian network to be considered as a basis for the edits. Each question in csv represents a node in this network.
	 * It is assumed that algorithm compiled this network prior to this method.
	 * @param algorithm: algorithm to be used in order to update probabilities and assets (q-values).
	 * @param usersMap : a map from user id to asset network (datastructure representing the q-tables of a given user). This is an
	 * input/output argument, and this map will be filled with the users read from the csv file.
	 * @return number of edits processed
	 * @throws IOException
	 * @throws InvalidParentException 
	 */
	public long load(File csv, ProbabilisticNetwork net, AssetAwareInferenceAlgorithm algorithm, Map<Integer, AssetNetwork> usersMap) throws IOException, InvalidParentException {
		
		// initial assertion
		if (csv == null || !csv.exists() || net == null || net.getNodeCount() == 0) {
			return 0;
		}
		if (usersMap == null) {
			// userMap is not going to be an output parameter, but let's at least reuse the map's name
			usersMap = new HashMap<Integer, AssetNetwork>();
		}

		// algorithm to be used for updating probabilities
		IInferenceAlgorithm junctionTreeAlgorithm = algorithm.getProbabilityPropagationDelegator();
		
		if (junctionTreeAlgorithm instanceof JunctionTreeAlgorithm) {
			// force soft evidence by using jeffrey rule in likelihood evidence w/ virtual nodes.
			((JunctionTreeAlgorithm)junctionTreeAlgorithm).setLikelihoodExtractor(JeffreyRuleLikelihoodExtractor.newInstance() );
		} else {
			throw new IllegalArgumentException("This method only works for junction tree algorithm. However, " + algorithm.getName() + " is not using Junction Tree algorithm.");
		}
			
		
		// A soft evidence is represented as a list of conditions (assumed nodes) and an array of float representing new conditional probability.
		// Non-edited probabilities must remain in the values before the edit. Thus, we must extract what are the current probability values.
		// this object extracts conditional probability of any nodes in same clique (it assumes prob network was compiled using junction tree algorithm)
//		IArbitraryConditionalProbabilityExtractor conditionalProbabilityExtractor = InCliqueConditionalProbabilityExtractor.newInstance();	
		
		// iterate on csv
		CSVReader reader = new CSVReader(new FileReader(csv));	// classes of open csv
		String [] nextLine;	// the line read
		long lineCounter = 0;	// how many lines were read
		for (lineCounter = 0; (nextLine = reader.readNext()) != null ; lineCounter++) {
	        
			// nextLine[] is an array of values from the line
			// extract columns of interest
	    	Integer id = Integer.valueOf(nextLine[0]);		// question ID
	        Integer userID = Integer.valueOf(nextLine[2]);	// user id
	        float probTrue = Float.valueOf(nextLine[3]);	// edit (i.e. probability of the question to be true)
	        // TODO what if probTrue is not the probability of question to be true?
	        
	        // check consistency of probTrue
	        if (probTrue <= 0 || probTrue >= 1) {
	        	throw new IllegalStateException("User " + userID + " bet " + probTrue + " on question " + id);
//	        	Debug.println(getClass(), "Warning: user " + userID + " bet " + probTrue + " on question " + id);
//	        	//ignore this edit
//	        	lineCounter--;
//	        	continue;	
	        }
	        
	        // extract node (the node user is making a bet) related to question id
	        Node node = net.getNode(id.toString());
	        if (!(node instanceof TreeVariable)) {
	        	// cannot use likelihood/soft evidence for this node
	        	throw new IllegalStateException(node + " is not a TreeVariable. We cannot add likelihood/soft evidence to this node.");
//	        	Debug.println(getClass(), node + " is not a TreeVariable. We cannot add likelihood/soft evidence to this node.");
//	        	//ignore this edit
//	        	lineCounter--;
//	        	continue;	
	        }
	        
	        // extract user (or user's asset's q network)
	        AssetNetwork userAssetNet = usersMap.get(userID);
	        if (userAssetNet == null) {
	        	if (isToCreateUserAssetNet()) {
	        		// create user's asset network with cliques and store to user map
	        		userAssetNet = algorithm.createAssetNetFromProbabilisticNet(net);
	        	} else {
	        		// store only asset network without cliques
	        		userAssetNet = AssetNetwork.getInstance(algorithm.getRelatedProbabilisticNetwork());
	        	}
	        	usersMap.put(userID, userAssetNet);
	        }
	        
	        if (isToPropagate()){
	        	// set this user as current user
	        	if (isToCreateUserAssetNet()) {
	        		if (userAssetNet != null) {
	        			algorithm.setAssetNetwork(userAssetNet);
	        		} else {
	        			Debug.println(getClass(), "Could not find/generate asset net for user " + userID);
	        		}
	        	}
	        	
	        	// at this moment, node is a TreeVariable
	        	TreeVariable betNode = (TreeVariable) node;
	        	
	        	// if we were doing conditional bet, we should extract what is P(betNode | assumptions) prior to edit, 
	        	// because any probability not related to the bet should remain the same value prior to bet, but we need
	        	// to know what was such conditional probability in order to use jeffreys rule.
	        	// Note: we do not need to use this on unconditional bet
	        	// TODO conditional bet (uncomment the following code if you want conditional bet)
//			PotentialTable potential = (PotentialTable) conditionalProbabilityExtractor.buildCondicionalProbability(
//					betNode, 
//					Collections.EMPTY_LIST, 
//					net, 
//					junctionTreeAlgorithm
//				);
	        	
	        	// If you are doing conditional probability, change the desired cells of "potential" here.
	        	// e.g. potential.setValue(0, 0.55f); potential.setValue(1, 0.45f);
	        	
	        	// we are doing soft evidence, but the JeffreyRuleLikelihoodExtractor can convert soft evidence to likelihood evidence
	        	// thus, we are adding the values of soft evidence in the likelihood evidence's vector 
	        	// (JeffreyRuleLikelihoodExtractor will then use Jeffrey rule to change its contents again)
	        	// The likelihood evidence vector is the user input CPT converted to a uni-dimensional array
//			float likelihood[] = new float[potential.tableSize()];
//			for (int i = 0; i < likelihood.length; i++) {
//				likelihood[i] = potential.getValue(i);
//			}
	        	// for conditional bet, comment the following lines and uncomment the above lines
	        	float likelihood[] = new float[betNode.getStatesSize()];
	        	likelihood[0] = 1-probTrue;		// assuming that index 0 is false
	        	likelihood[1] = probTrue; 		// assuming that index 1 is true
	        	// TODO check if this index order is always valid
	        	
	        	// add likelihood ratio given (empty) parents (conditions assumed in the bet - empty now)
	        	betNode.addLikeliHood(likelihood, Collections.EMPTY_LIST);
	        	
	        	// propagate soft evidence
	        	algorithm.propagate();
	        } else {
	        	// just update marginals
	        	// at this moment, node is a TreeVariable
	        	TreeVariable betNode = (TreeVariable) node;
	        	// previous marginals
	        	float prevTrue = betNode.getMarginalAt(1);
	        	betNode.setMarginalAt(0, 1-probTrue);
	        	betNode.setMarginalAt(1, probTrue);
	        	
	        	// extract asset node and update only its marginal
	        	Node assetNode = userAssetNet.getNode(id.toString());
	        	if (assetNode != null && (assetNode instanceof AssetNode)) {
	        		// update marginal of asset node using the ratio of change in probability
					AssetNode asset = (AssetNode) assetNode;
	        		asset.setMarginalAt(0, asset.getMarginalAt(0) * ((1-prevTrue) / (1-probTrue)));
	        		asset.setMarginalAt(1, asset.getMarginalAt(1) * (prevTrue / probTrue));
	        	}
	        }
//	        System.out.println(lineCounter + "," + node.getName() + " ; [" + ((TreeVariable) node).getMarginalAt(0) + " , "+ ((TreeVariable) node).getMarginalAt(1) + "]");
//	        if (((TreeVariable) node).getMarginalAt(0) > .5) {
//	        	System.out.println(lineCounter + " ; [" + ((TreeVariable) node).getMarginalAt(0) + " , "+ ((TreeVariable) node).getMarginalAt(1) + "]");
//	        }
	    }
		return lineCounter;
	}

	/**
	 * If true, {@link #load(File, ProbabilisticNetwork, AssetAwareInferenceAlgorithm, Map)} will
	 * call {@link AssetAwareInferenceAlgorithm#propagate()}.
	 * @param isToPropagate the isToPropagate to set
	 */
	public void setToPropagate(boolean isToPropagate) {
		this.isToPropagate = isToPropagate;
	}

	/**
	 * If true, {@link #load(File, ProbabilisticNetwork, AssetAwareInferenceAlgorithm, Map)} will
	 * call {@link AssetAwareInferenceAlgorithm#propagate()}.
	 * @return the isToPropagate
	 */
	public boolean isToPropagate() {
		return isToPropagate;
	}

	/**
	 * If true, the map parameter of {@link #load(File, ProbabilisticNetwork, AssetAwareInferenceAlgorithm, Map)}
	 * will be filled with new values (i.e. new user asset nets).
	 * @param isToCreateUserAssetNet the isToCreateUserAssetNet to set
	 */
	public void setToCreateUserAssetNet(boolean isToCreateUserAssetNet) {
		this.isToCreateUserAssetNet = isToCreateUserAssetNet;
	}

	/**
	 * If true, the map parameter of {@link #load(File, ProbabilisticNetwork, AssetAwareInferenceAlgorithm, Map)}
	 * will be filled with new values (i.e. new user asset nets).
	 * @return the isToCreateUserAssetNet
	 */
	public boolean isToCreateUserAssetNet() {
		return isToCreateUserAssetNet;
	}
	

}
