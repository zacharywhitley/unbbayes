/**
 * 
 */
package unbbayes.prs.bn.inference.extension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import unbbayes.prs.INode;
import unbbayes.prs.Node;
import unbbayes.prs.bn.AssetNetwork;
import unbbayes.prs.bn.Clique;
import unbbayes.prs.bn.JeffreyRuleLikelihoodExtractor;
import unbbayes.prs.bn.JunctionTreeAlgorithm;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.ProbabilisticTable;
import unbbayes.prs.bn.Separator;
import unbbayes.prs.bn.TreeVariable;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.util.Debug;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithm;

/**
 * This class uses a joint probability table
 * and joint asset table
 * in order to provide the same functionality
 * of {@link AssetAwareInferenceAlgorithm}.
 * That is, instead of using a junction tree for assets, it
 * calculates all possible combination of states.
 * Joint probabilities are calculated
 * as Product(clique_potentials) / Product(separator_potentials)
 * of the junction tree in {@link #getRelatedProbabilisticNetwork()}
 * @author Shou Matsumoto
 *
 */
public class BruteForceAssetAwareInferenceAlgorithm extends
		AssetAwareInferenceAlgorithm {

	public static final String JOINT_ASSET_TABLE_PROP = BruteForceAssetAwareInferenceAlgorithm.class.getName()+".JOINT_ASSET_TABLE_PROP";

	public static final String JOINT_PROBABILITY_TABLE_PROP = BruteForceAssetAwareInferenceAlgorithm.class.getName()+".JOINT_PROBABILITY_TABLE_PROP";
//	private JointPotentialTable jointProbabilityTable;



	/**
	 * Default constructor is not private, to allow inheritance
	 */
	protected BruteForceAssetAwareInferenceAlgorithm() {
		// use q-values by default
		this.setToUseQValues(true);
	}
	
	public static IInferenceAlgorithm getInstance(IInferenceAlgorithm probabilityDelegator) {
		return getInstance(probabilityDelegator, 100f);
	}
	
	

	/**
	 * Default instantiation method.
	 */
	public static IInferenceAlgorithm getInstance(IInferenceAlgorithm probabilityDelegator, float initQValues) {
		BruteForceAssetAwareInferenceAlgorithm ret = new BruteForceAssetAwareInferenceAlgorithm();
		// for some reason, polymorphism is not working so properly...
		if (probabilityDelegator instanceof JunctionTreeAlgorithm) {
			// call explicitly
			ret.setProbabilityPropagationDelegator((JunctionTreeAlgorithm)probabilityDelegator);
		} else {
			ret.setProbabilityPropagationDelegator(probabilityDelegator);
		}
		try {
			ret.setAssetPropagationDelegator(AssetPropagationInferenceAlgorithm.getInstance((ProbabilisticNetwork) probabilityDelegator.getNetwork()));
		} catch (Exception e) {
			throw new IllegalArgumentException("The network managed by " + probabilityDelegator + " must be an instance of " + ProbabilisticNetwork.class.getName(), e);
		}
		ret.setToUseQValues(true);
		ret.setDefaultInitialAssetTableValue(initQValues);
		ret.initializeJointAssets();
		ret.initializeJointProbabilities();
		return ret;
	}
	
	
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.inference.extension.AssetAwareInferenceAlgorithm#newInstance(unbbayes.util.extension.bn.inference.IInferenceAlgorithm)
	 */
	protected IInferenceAlgorithm newInstance(IInferenceAlgorithm probabilityDelegator) {
		return this.newInstance(probabilityDelegator, getDefaultInitialAssetTableValue());
	}
	
	/**
	 * This is used in {@link #clone()} to create a new instance of this class
	 * @param probabilityDelegator
	 * @param defaultInitialQValue
	 * @return
	 * @see #getInstance(IInferenceAlgorithm, float)
	 */
	protected IInferenceAlgorithm newInstance( IInferenceAlgorithm probabilityDelegator, float defaultInitialQValue) {
		return getInstance(probabilityDelegator, defaultInitialQValue);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.inference.extension.AssetAwareInferenceAlgorithm#clone(boolean)
	 */
	public IInferenceAlgorithm clone(boolean isToCloneAssets)
			throws CloneNotSupportedException {
		// clone Bayes network
		ProbabilisticNetwork newNet = this.cloneProbabilisticNetwork(getRelatedProbabilisticNetwork());
		
		// clone JT algorithm using the cloned BN
		JunctionTreeAlgorithm jtAlgorithm = new JunctionTreeAlgorithm(newNet);
		if (getProbabilityPropagationDelegator() instanceof JunctionTreeAlgorithm) {
			// reuse same likelihood extractor
			JunctionTreeAlgorithm origJTAlgorithm = (JunctionTreeAlgorithm) getProbabilityPropagationDelegator();
			jtAlgorithm.setLikelihoodExtractor(origJTAlgorithm.getLikelihoodExtractor());
		} else {
			// unknown type of BN inference algorithm...
			Debug.println(getClass(), "Unknown type of BN inference algorithm. Cannot extract likelihood extractor. Using default: JeffreyRuleLikelihoodExtractor.");
			jtAlgorithm.setLikelihoodExtractor(JeffreyRuleLikelihoodExtractor.newInstance());
		}
		
		// clone this algorithm
		BruteForceAssetAwareInferenceAlgorithm ret = (BruteForceAssetAwareInferenceAlgorithm) newInstance(jtAlgorithm, getDefaultInitialAssetTableValue());
		// copy settings
		ret.setDefaultInitialAssetTableValue(getDefaultInitialAssetTableValue());
		ret.setToAllowZeroAssets(this.isToAllowZeroAssets());
		ret.setToCalculateMarginalsOfAssetNodes(this.isToCalculateMarginalsOfAssetNodes());
		ret.setToLogAssets(this.isToLogAssets());
		ret.setToNormalizeDisconnectedNets(this.isToNormalizeDisconnectedNets());
		ret.setToPropagateForGlobalConsistency(this.isToPropagateForGlobalConsistency());
		ret.setToUpdateAssets(this.isToUpdateAssets());
		ret.setToUpdateOnlyEditClique(this.isToUpdateOnlyEditClique());
		ret.setToUpdateSeparators(this.isToUpdateSeparators());
		ret.setToUseQValues(this.isToUseQValues());
		
		// copy current converter which converts q values to assets and vice-versa
		ret.setqToAssetConverter(this.getqToAssetConverter());
		
		// clone joint prob table
		ret.setJointProbabilityTable((JointPotentialTable) this.getJointProbabilityTable().clone());
		
		// return now if we do not need to clone assets
		if (!isToCloneAssets) {
			ret.setAssetNetwork(null);
			return ret;
		}
		
		// clone asset net. just for backward compatibility
		AssetNetwork newAssetNet = null;
		try {
			newAssetNet = ret.createAssetNetFromProbabilisticNet(newNet);
		} catch (InvalidParentException e) {
			throw new RuntimeException("Could not clone asset network of user " + getAssetNetwork(), e);
		}
		
		// mapping between original cliques to copied cliques.
		Map<Clique, Clique> oldCliqueToNewCliqueMap = new HashMap<Clique, Clique>();
		
		if (getAssetNetwork().getJunctionTree() != null && getAssetNetwork().getJunctionTree().getCliques() != null) {
			// copy asset tables of cliques. Since cliques are stored in a list, the ordering of the copied cliques is supposedly the same
			for (int i = 0; i < getAssetNetwork().getJunctionTree().getCliques().size(); i++) {
				(newAssetNet.getJunctionTree().getCliques().get(i).getProbabilityFunction()).setValues(
						( getAssetNetwork().getJunctionTree().getCliques().get(i).getProbabilityFunction()).getValues()
				);
				// store asset cliques in the map, so that we can use it for the separators later
				oldCliqueToNewCliqueMap.put(getAssetNetwork().getJunctionTree().getCliques().get(i), newAssetNet.getJunctionTree().getCliques().get(i));
			}
		}
		
		if (getAssetNetwork().getJunctionTree() != null && getAssetNetwork().getJunctionTree().getSeparators() != null) {
			// copy asset tables of separators. Use oldCliqueToNewCliqueMap, because separators are not stored in a list
			for (Separator oldSep : getAssetNetwork().getJunctionTree().getSeparators()) {
				Separator newSep = newAssetNet.getJunctionTree().getSeparator(
						oldCliqueToNewCliqueMap.get(oldSep.getClique1()),
						oldCliqueToNewCliqueMap.get(oldSep.getClique2())
				);
				if (newSep == null) {
					throw new RuntimeException("Could not access copy of separator " + oldSep + " correctly while copying asset tables of separators.");
				}
				// tables are supposedly with same size
				(newSep.getProbabilityFunction()).setValues(( oldSep.getProbabilityFunction()).getValues());
			}
		}
		
		// link algorithm to the new asset net
		ret.setAssetNetwork(newAssetNet);
		
		// clone joint asset table.
		ret.setJointQTable((JointPotentialTable) this.getJointQTable().clone());
		
		return ret;
	}


	


	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.inference.extension.AssetAwareInferenceAlgorithm#calculateExplanation(java.util.List)
	 */
	public float calculateExplanation(  List<Map<INode, Integer>> inputOutpuArgumentForExplanation) {
		if (inputOutpuArgumentForExplanation == null) {
			inputOutpuArgumentForExplanation = new ArrayList<Map<INode,Integer>>();
		}
		
		// value to be returned
		float minValue = Float.MAX_VALUE;
		
		// find joint asset with smallest value
		JointPotentialTable table = getJointQTable();
		for (int i = 0; i < table.tableSize(); i++) {
			if (table.getValue(i) > 0f && table.getValue(i) <= minValue) {
				if (table.getValue(i) < minValue) {
					// if strictly small, reset explanation
					inputOutpuArgumentForExplanation.clear();
					minValue = table.getValue(i);
				}
				// extract states having this (min) value
				int[] states = table.getMultidimensionalCoord(i);
				// convert states to Map<INode, Integer>
				Map<INode, Integer> explanation = new HashMap<INode, Integer>();
				for (int j = 0; j < table.variableCount(); j++) {
					explanation.put(table.getVariableAt(j), states[j]);
				}
				inputOutpuArgumentForExplanation.add(explanation);
			}
		}
		
		// if LPE was not found, it means that joint table was all 0 (so, min is also 0)
		if (inputOutpuArgumentForExplanation.isEmpty()) {
			return 0f;
		}
		
		return minValue;
	}



	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.inference.extension.AssetAwareInferenceAlgorithm#setAssetNetwork(unbbayes.prs.bn.AssetNetwork)
	 */
	@Override
	public void setAssetNetwork(AssetNetwork network)
			throws IllegalArgumentException {
		super.setAssetNetwork(network);
		if (this.getJointQTable() == null) {
			this.initializeJointAssets();
		}
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.inference.extension.AssetAwareInferenceAlgorithm#runMinPropagation(java.util.Map)
	 */
	@Override
	public void runMinPropagation(Map<INode, Integer> conditions) {
		if (conditions == null) {
			conditions = new HashMap<INode, Integer>();
		}
		// fill conditions with current evidences, if present
		for (Node node : getAssetNetwork().getNodes()) {
			if (node instanceof TreeVariable) {
				TreeVariable treeVariable = (TreeVariable) node;
				if (treeVariable.hasEvidence()) {
					// fill conditions with extra values, but the priority is the content of conditions.
					if (!conditions.containsKey(treeVariable)) {
						conditions.put(treeVariable, treeVariable.getEvidence());
					}
				}
			}
		}
		JointPotentialTable table = getJointQTable();
		table.copyData();
		if (conditions != null && !conditions.isEmpty()) {
			// set to 0 all cells that does not match conditions
			for (int i = 0; i < table.tableSize(); i++) {
				int[] states = table.getMultidimensionalCoord(i);
				boolean matches = true;
				for (INode key : conditions.keySet()) {
					int condition = conditions.get(key);
					boolean isNegativeCondition = false;	// when true, this is a "not" evidence (i.e. all states but the condition must be considered).
					if (condition < 0) {
						isNegativeCondition = true;
						condition = -(condition + 1);	// -1 == not 0, -2 = not 1, -3 = not 2 and so on
					}
					if ( ( isNegativeCondition && states[table.indexOfVariable((Node) key)] == condition )
							|| ( !isNegativeCondition && states[table.indexOfVariable((Node) key)] != condition )) {
						matches = false;
						break;
					}
				}
				if (!matches) {
					table.setValue(i, (float)0.0);
				}
			}
		}
		return;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.inference.extension.AssetAwareInferenceAlgorithm#undoMinPropagation()
	 */
	public void undoMinPropagation() {
		// no need to propagate lpe on joint table, so no need to undo
		getJointQTable().restoreData();
		return;
	}



	/**
	 * This is a {@link ProbabilisticTable} which represents joint
	 * probabilities or joint assets.
	 * The difference to {@link ProbabilisticTable} is that
	 * data copied by {@link #copyData()} (which is restorable by calling
	 * {@link #restoreData()}) can be accessed.
	 * @author Shou Matsumoto
	 */
	public class JointPotentialTable extends ProbabilisticTable {
		private static final long serialVersionUID = -4355963169676077807L;
		public JointPotentialTable() {}
		
		/** This is used in {@link #clone()} */
		public PotentialTable newInstance() { return new JointPotentialTable(); }
		
	}
	
//	/**
//	 * Runs {@link PotentialTable#copyData()} for
//	 * each clique potentials and separator potentials
//	 * of {@link #getRelatedProbabilisticNetwork()}
//	 */
//	protected void copyTableData() {
//		if (getRelatedProbabilisticNetwork() == null || getRelatedProbabilisticNetwork().getJunctionTree() == null) {
//			Debug.println(getClass(), "Attempted to copy clique/separator tables of a network which was not initialized yet.");
//			return;
//		}
//		// iterate on cliques
//		if ( getRelatedProbabilisticNetwork().getJunctionTree().getCliques() != null) {
//			for (Clique clique : getRelatedProbabilisticNetwork().getJunctionTree().getCliques()) {
//				clique.getProbabilityFunction().copyData();
//			}
//		}
//		// iterate on separators
//		if ( getRelatedProbabilisticNetwork().getJunctionTree().getSeparators() != null) {
//			for (Separator separator : getRelatedProbabilisticNetwork().getJunctionTree().getSeparators()) {
//				separator.getProbabilityFunction().copyData();
//			}
//		}
//	}

	/**
	 * Initializes joint probability table and joint q table
	 * @see unbbayes.prs.bn.inference.extension.AssetAwareInferenceAlgorithm#run()
	 */
	public void run() throws IllegalStateException {
		super.run();
		
		// initialize joint probability table and asset table
		this.initializeJointAssets();
		this.initializeJointProbabilities();
		
		// clone will fail if we associate nodes with joint clique (clique with joint table)
//		this.associateNodesWithJointTable();
	}
	
	
//	protected void associateNodesWithJointTable() {
//		Clique jointClique = new Clique(getJointProbabilityTable());
//		for (Node node : getRelatedProbabilisticNetwork().getNodes()) {
//			if (node instanceof ProbabilisticNode) {
//				if (!jointClique.getAssociatedProbabilisticNodes().contains(node)) {
//					jointClique.getAssociatedProbabilisticNodes().add(node);
//				}
//				if (!jointClique.getNodes().contains(node)) {
//					jointClique.getNodes().add(node);
//				}
//				ProbabilisticNode probNode = (ProbabilisticNode) node;
//				probNode.setAssociatedClique(jointClique);
//			}
//		}
//	}

	protected void initializeJointProbabilities() {
		JointPotentialTable jProbTab = new JointPotentialTable();
		for (Node node : getRelatedProbabilisticNetwork().getNodes()) {
			jProbTab.addVariable(node);
		}
		setJointProbabilityTable(jProbTab);
		
		// update only the content of joint probability table
		this.updateJointProbability(false);
	}

	protected void initializeJointAssets() {
		if (getAssetNetwork() == null) {
			return;
		}
		// init joint asset table
		JointPotentialTable jAstTab = new JointPotentialTable();
		for (Node node : getAssetNetwork().getNodes()) {
			// variables in asset table must come from asset network instead of from prob network
			jAstTab.addVariable(node);
		}
		// init content of asset table
		for (int i = 0; i < jAstTab.tableSize(); i++) {
			jAstTab.setValue(i, getDefaultInitialAssetTableValue());
		}
		jAstTab.copyData();	// backup initial values of asset table
		setJointQTable(jAstTab);
	}

	
	protected void updateJointProbability(boolean isToUpdateAssets) {
		if (getRelatedProbabilisticNetwork() == null || getRelatedProbabilisticNetwork().getJunctionTree() == null) {
			// nothing to update
			Debug.println(getClass(), "No network found");
			return;
		}
		// this is the joint probability table
		JointPotentialTable jProbTable = this.getJointProbabilityTable();
		if (jProbTable == null) {
			throw new IllegalStateException("Joint probability table is not initialized");
		}
		
		// update current joint probability using clique/separator potentials
		for (int i = 0; i < jProbTable.tableSize(); i++) {
			
			// obtain the states of nodes related to this cell, so that we can filter clique potentials
			int[] jointTableStates = jProbTable.getMultidimensionalCoord(i);
			
			// calculate Product(Clique)/Product(Separator) regarding the states related to this cell
			float product = 1f;	// 1 is the identity value of a product
			
			// iterate on cliques
			for (Clique clique : getRelatedProbabilisticNetwork().getJunctionTree().getCliques()) {
				
				// get clique potential
				PotentialTable cliqueTable = clique.getProbabilityFunction();
				
				// multiply only the cells of clique potential which matches the states of joint table
				for (int indexOfCliqueTable = 0; indexOfCliqueTable < cliqueTable.tableSize(); indexOfCliqueTable++) {
					
					// check if states of clique table matches the states of joint table 
					boolean matches = true;
					int[] cliquePotStates = cliqueTable.getMultidimensionalCoord(indexOfCliqueTable);
					for (int cliqueVarIndex = 0; cliqueVarIndex < cliqueTable.getVariablesSize(); cliqueVarIndex++) {
						if (cliquePotStates[cliqueVarIndex] != jointTableStates[jProbTable.indexOfVariable((Node) cliqueTable.getVariableAt(cliqueVarIndex))]) {
							matches = false;
							break;
						}
					}
					if (matches) {
						product *= cliqueTable.getValue(indexOfCliqueTable);
					}
				}
			}
			// iterate on separators
			for (Separator sep : getRelatedProbabilisticNetwork().getJunctionTree().getSeparators()) {
				// get separator potential
				PotentialTable sepTable = sep.getProbabilityFunction();
				
				// multiply only the cells of separator potential which matches the states of joint table
				for (int indexOfSepTable = 0; indexOfSepTable < sepTable.tableSize(); indexOfSepTable++) {
					
					// check if states of separator table matches the states of joint table 
					boolean matches = true;
					int[] sepPotStates = sepTable.getMultidimensionalCoord(indexOfSepTable);
					for (int sepVarIndex = 0; sepVarIndex < sepTable.getVariablesSize(); sepVarIndex++) {
						if (sepPotStates[sepVarIndex] != jointTableStates[jProbTable.indexOfVariable((Node) sepTable.getVariableAt(sepVarIndex))]) {
							matches = false;
							break;
						}
					}
					if (matches) {
						float value = sepTable.getValue(indexOfSepTable);
						if (value == 0.0f ) {
							if (product > 0.0f) {
								throw new IllegalStateException("Inconsistency or underflow detected in separator " + sepTable);
							} else {
								// consider the contribution of this separator as 0.0
							}
						} else {
							product /= value;
						}
					}
				}
			}
			
			// update joint probability
			jProbTable.setValue(i, product);
			
			// update joint q-values
			if (isToUpdateAssets && getJointQTable().getValue(i) > 0.0) {
				float value = getJointQTable().getValue(i) * jProbTable.getValue(i) / jProbTable.getCopiedValue(i);
				if (!isToAllowZeroAssets() && value <= (isToUseQValues()?1f:0f) ) { // note: 0 assets == 1 q-value
					throw new ZeroAssetsException("Cell " + i + " in asset table went to " + value);
				}
				// new q = old q * new prob / old prob
				getJointQTable().setValue(i, value ); // tables have same indexes
			}
		}
		
		// backup the updated joint tables
//		jProbTable.copyData();
//		if (isToUpdateAssets) {
//			getJointQTable().copyData();
//		}
	}
	
	/**
	 * Obtains the marginal probability of a node from a joint probability table
	 * @param node
	 * @return marginal probability
	 */
	protected float[] getMarginalProbabilityFromJointTable( ProbabilisticNode node) {
		float[] ret = new float[node.getStatesSize()];
		Map<ProbabilisticNode, Integer> nodesAndStatesToConsider = new HashMap<ProbabilisticNode, Integer>();
		for (int i = 0; i < node.getStatesSize(); i++) {
			nodesAndStatesToConsider.put((ProbabilisticNode) node, i);
			ret[i] = this.getJointProbability(nodesAndStatesToConsider);
		}
		return ret;
	}

	/**
	 * Updates the content of joint tables
	 * @param isToUpdateAssets : if false, it will not update the joint q table (i.e. updates only joint probability table)
	 * @see unbbayes.prs.bn.inference.extension.AssetAwareInferenceAlgorithm#propagate()
	 */
	public void propagate() {
		// backup old joint values
		getJointProbabilityTable().copyData();	
		getJointQTable().copyData();
		
		// backup old clique/sep potentials
		for (Clique clique : getRelatedProbabilisticNetwork().getJunctionTree().getCliques()) {
			clique.getProbabilityFunction().copyData();
		}
		for (Separator separator : getRelatedProbabilisticNetwork().getJunctionTree().getSeparators()) {
			separator.getProbabilityFunction().copyData();
		}
		
		// do not update assets on superclass' propagation
		boolean backup = this.isToUpdateAssets();
		this.setToUpdateAssets(false);	
		super.propagate();
		this.setToUpdateAssets(backup);
		
		// propagate assets on joint table
		try {
			this.updateJointProbability(this.isToUpdateAssets());
		} catch (ZeroAssetsException e) {
			// undo propagation
			getJointProbabilityTable().restoreData();
			getJointQTable().restoreData();
			// need to restore marginals, because super.propagate() has changed it
			this.updateMarginalsFromJointProbability();
			throw e;
		}
		
		// update marginal probability of nodes based on joint table instead of clique table
		this.updateMarginalsFromJointProbability();
		if (this.isToUpdateAssets() && this.isToPropagateForGlobalConsistency()) {
			// run min propagation on joint asset table if needed (this will only propagate findings)
			this.runMinPropagation(null);
		}
	}
	
	/**
	 * Forces {@link TreeVariable#getMarginalAt(int)} to
	 * have values which reflects {@link #getJointProbabilityTable()}
	 */
	public void updateMarginalsFromJointProbability() {
		// update marginals accordingly to joint probability
		for (Node node : getRelatedProbabilisticNetwork().getNodes()) {
			if (node instanceof ProbabilisticNode) {
				ProbabilisticNode probNode = (ProbabilisticNode) node;
				probNode.setMarginalProbabilities(this.getMarginalProbabilityFromJointTable(probNode));
			}
		}
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.inference.extension.AssetAwareInferenceAlgorithm#getJointProbability(java.util.Map)
	 */
	public float getJointProbability(
			Map<ProbabilisticNode, Integer> nodesAndStatesToConsider) {
		// just add joint probabilities which matches nodesAndStatesToConsider
		JointPotentialTable table = getJointProbabilityTable();
		float sum = 0f;
		for (int i = 0; i < table.tableSize(); i++) {
			int[] states = table.getMultidimensionalCoord(i);
			boolean matches = true;
			for (ProbabilisticNode node : nodesAndStatesToConsider.keySet()) {
				if (nodesAndStatesToConsider.get(node) != states[table.getVariableIndex(node)]) {
					matches = false;
					break;
				}
			}
			if (matches) {
				sum += table.getValue(i);
			}
		}
		return sum;
	}
	
	

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.inference.extension.AssetAwareInferenceAlgorithm#revertLastProbabilityUpdate()
	 */
	@Override
	public void revertLastProbabilityUpdate() {
		super.revertLastProbabilityUpdate();
		this.getJointProbabilityTable().restoreData();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.inference.extension.AssetAwareInferenceAlgorithm#addAssets(float)
	 */
	public void addAssets(float delta) {
		super.addAssets(delta);
		/*
		 * If we want to add something into a logarithm value, we must multiply some ratio.
		 * Assuming that asset = b logX (q)  (note: X is some base), then X^(asset/b) = q
		 * So, X^((asset+delta)/b) = X^(asset/b + delta/b) = X^(asset/b) * X^(delta/b)  = q * X^(delta/b)
		 * So, we need to multiply X^(delta/b) in order to update q when we add delta into asset.
		 */
		float ratio = (float) Math.pow(getqToAssetConverter().getCurrentLogBase(), delta / getqToAssetConverter().getCurrentCurrencyConstant() );
		JointPotentialTable assetTable = getJointQTable();
		for (int i = 0; i < assetTable.tableSize(); i++) {
			if (isToUseQValues()) {
				assetTable.setValue(i, (assetTable.getValue(i) * ratio) );
			} else {
				assetTable.setValue(i, (assetTable.getValue(i) + delta) );
			}
		}
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.inference.extension.AssetAwareInferenceAlgorithm#calculateExpectedAssets()
	 */
	public double calculateExpectedAssets() {
		// this is the value to be returned by this method
		double ret = 0 ;
		
		// simple sum of product of joints
		JointPotentialTable probabilityTable = getJointProbabilityTable();
		JointPotentialTable qTable = getJointQTable();
		
		// generate a dummy clique for prob
		Clique dummyProbClique = null;
		if (getExpectedAssetCellListeners() != null && !getExpectedAssetCellListeners().isEmpty()) {
			dummyProbClique = new Clique(probabilityTable);
		}
		// generate a dummy clique for prob
		Clique dummyAssetClique = null;
		if (getExpectedAssetCellListeners() != null && !getExpectedAssetCellListeners().isEmpty()) {
			dummyAssetClique = new Clique(qTable);
		}
		
		for (int j = 0; j < probabilityTable.tableSize(); j++) {
			if (probabilityTable.getValue(j) <= 0f) {
				continue;
			}
			double value = probabilityTable.getValue(j) 
			* getqToAssetConverter().getScoreFromQValues(qTable.getValue(j));
			ret +=  value;
			this.notifyExpectedAssetCellListener(dummyProbClique, dummyAssetClique, j, j, value);
		}			
		
		return ret;
	}

	/**
	 * @param jointProbabilityTable the jointProbabilityTable to set
	 */
	public void setJointProbabilityTable(JointPotentialTable jointProbabilityTable) {
//		this.jointProbabilityTable = jointProbabilityTable;
		getRelatedProbabilisticNetwork().getProperties().put(JOINT_PROBABILITY_TABLE_PROP, jointProbabilityTable);
	}

	/**
	 * @return the jointProbabilityTable
	 */
	public JointPotentialTable getJointProbabilityTable() {
//		return jointProbabilityTable;
		if (getRelatedProbabilisticNetwork() != null) {
			return (JointPotentialTable) getRelatedProbabilisticNetwork().getProperty(JOINT_PROBABILITY_TABLE_PROP);
		}
		return null;
	}

	/**
	 * @param jointAssetTable the jointAssetTable to set
	 */
	public void setJointQTable(JointPotentialTable jointAssetTable) {
		getAssetNetwork().getProperties().put(JOINT_ASSET_TABLE_PROP, jointAssetTable);
	}

	/**
	 * @return the jointAssetTable
	 */
	public JointPotentialTable getJointQTable() {
		if (getAssetNetwork() != null) {
			return (JointPotentialTable) getAssetNetwork().getProperty(JOINT_ASSET_TABLE_PROP);
		}
		return null;
	}

	
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.inference.extension.AssetAwareInferenceAlgorithm#setToUseQValues(boolean)
	 */
	public void setToUseQValues(boolean isToUseQValues) {
		if (!isToUseQValues) {
			throw new UnsupportedOperationException("Brute force algorithm will always use q-values instead of assets.");
		}
		super.setToUseQValues(true);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.inference.extension.AssetAwareInferenceAlgorithm#isToUseQValues()
	 */
	public boolean isToUseQValues() {
		// brute force algorithm always use q-values
		return true;
	}
	
	public void setAsPermanentEvidence(INode node, int state, boolean isToDeleteNode) {
		super.setAsPermanentEvidence(node, state, isToDeleteNode);
		this.updateJointProbability(false);
		// copy joint probability
		this.getJointProbabilityTable().copyData();
		// update joint assets
		Map<INode, Integer> conditions = new HashMap<INode, Integer>();
		conditions.put(node, state);
		this.runMinPropagation(conditions);// this is equivalent to set finding node=state 
		// copy joint assets
		this.getJointQTable().copyData();
	}

	
}
