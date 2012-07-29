/**
 * 
 */
package unbbayes.prs.bn.inference.extension;

import unbbayes.prs.Node;
import unbbayes.prs.bn.JeffreyRuleLikelihoodExtractor;
import unbbayes.prs.bn.JunctionTreeAlgorithm;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.cpt.IArbitraryConditionalProbabilityExtractor;
import unbbayes.prs.bn.cpt.impl.InCliqueConditionalProbabilityExtractor;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithm;

/**
 * This class is equivalent to {@link BruteForceAssetAwareInferenceAlgorithm},
 * but the joint probability is calculated by using the cpts.
 * <br/>
 * E.g. <br/><br/>
 * P(A,B,C) = P(C|Pa(C))*P(B|Pa(B))*P(A|Pa(A)).
 * @author Shou Matsumoto
 *
 */
public class CPTBruteForceAssetAwareInferenceAlgorithm extends
		BruteForceAssetAwareInferenceAlgorithm {

	/** Default constructor is not private in order to allow inheritance */
	protected CPTBruteForceAssetAwareInferenceAlgorithm() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * This class is equivalent to {@link BruteForceAssetAwareInferenceAlgorithm},
	 * but the joint probability is calculated by using the cpts.
	 * <br/>
	 * E.g. <br/><br/>
	 * P(A,B,C) = P(C|Pa(C))*P(B|Pa(B))*P(A|Pa(A)).
	 */
	public static CPTBruteForceAssetAwareInferenceAlgorithm getInstance(IInferenceAlgorithm probabilityDelegator) {
		return (CPTBruteForceAssetAwareInferenceAlgorithm) CPTBruteForceAssetAwareInferenceAlgorithm.getInstance(probabilityDelegator, 1000);
	}
	
	
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.inference.extension.BruteForceAssetAwareInferenceAlgorithm#updateMarginalsFromJointProbability()
	 */
	@Override
	protected void updateMarginalsFromJointProbability() {
		// TODO Auto-generated method stub
		super.updateMarginalsFromJointProbability();
	}

	/**
	 * Default instantiation method.
	 */
	public static IInferenceAlgorithm getInstance(IInferenceAlgorithm probabilityDelegator, float initQValues) {
		CPTBruteForceAssetAwareInferenceAlgorithm ret = new CPTBruteForceAssetAwareInferenceAlgorithm();
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
		ret.setDefaultInitialAssetTableValue(initQValues);
		ret.initializeJointAssets();
		ret.initializeJointProbabilities();
		return ret;
	}
	

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.inference.extension.BruteForceAssetAwareInferenceAlgorithm#updateJointProbability(boolean)
	 */
	@Override
	protected void updateJointProbability(boolean isToUpdateAssets) {
		if (getRelatedProbabilisticNetwork() == null || getRelatedProbabilisticNetwork().getJunctionTree() == null) {
			// nothing to update
			System.err.println("No network found");
			return;
		}
		

		// only update cpts if everything went ok
		this.updateCPT();
		
		// this is the joint probability table
		JointPotentialTable jProbTable = this.getJointProbabilityTable();
		if (jProbTable == null) {
			throw new IllegalStateException("Joint probability table is not initialized");
		}

		// update current joint probability using CPTs
		for (int i = 0; i < jProbTable.tableSize(); i++) {
			
			// obtain the states of nodes related to this cell, so that we can filter clique potentials
			int[] jointTableStates = jProbTable.getMultidimensionalCoord(i);
			
			// calculate Product(Clique)/Product(Separator) regarding the states related to this cell
			double product = 1f;	// 1 is the identity value of a product
			
			// iterate on CPTs
			for (Node node : getRelatedProbabilisticNetwork().getNodes()) {
				if (!(node instanceof ProbabilisticNode)) {
					continue;
				}
				ProbabilisticNode mainNode = (ProbabilisticNode) node;
				
				// get CPT
				PotentialTable cpt = mainNode.getProbabilityFunction();
				
				// multiply only the cells of cpt which matches the states of joint table
				for (int indexOfCliqueTable = 0; indexOfCliqueTable < cpt.tableSize(); indexOfCliqueTable++) {
					
					// check if states of clique table matches the states of joint table 
					boolean matches = true;
					int[] cliquePotStates = cpt.getMultidimensionalCoord(indexOfCliqueTable);
					for (int cliqueVarIndex = 0; cliqueVarIndex < cpt.getVariablesSize(); cliqueVarIndex++) {
						if (cliquePotStates[cliqueVarIndex] != jointTableStates[jProbTable.indexOfVariable((Node) cpt.getVariableAt(cliqueVarIndex))]) {
							matches = false;
							break;
						}
					}
					if (matches) {
						product *= cpt.getValue(indexOfCliqueTable);
					}
				}
			}
			
			// update joint probability
			jProbTable.setValue(i, (float)product);
			
			// update joint q-values
			if (isToUpdateAssets) {
				double value = getJointQTable().getValue(i) * jProbTable.getValue(i) / jProbTable.getCopiedValue(i);
				if (!isToAllowZeroAssets() && value <= (isToUseQValues()?1f:0f)) {	// note: 0 assets == 1 q-value
					throw new ZeroAssetsException("Cell " + i + " in asset table went to " + value);
				}
				// new q = old q * new prob / old prob
				getJointQTable().setValue(i, (float)value ); // tables have same indexes
			}
		}
		
		
	}

	/**
	 * Updates the values of the cpts 
	 * of each node, in order to reflect
	 * the current clique potentials.
	 * This method is needed, because this class
	 * uses CPT to calculate joint prob.
	 */
	protected void updateCPT() {
		if (getRelatedProbabilisticNetwork() == null || getRelatedProbabilisticNetwork().getNodes() == null) {
			return;
		}
		
		
		IArbitraryConditionalProbabilityExtractor conditionalProbabilityExtractor = null;
		if (this.getProbabilityPropagationDelegator() instanceof JunctionTreeAlgorithm) {
			// if possible, reuse from JT algorithm
			JunctionTreeAlgorithm junctionTreeAlgorithm = (JunctionTreeAlgorithm) this.getProbabilityPropagationDelegator();
			if (junctionTreeAlgorithm.getLikelihoodExtractor() instanceof JeffreyRuleLikelihoodExtractor) {
				JeffreyRuleLikelihoodExtractor jeffreyRuleLikelihoodExtractor = (JeffreyRuleLikelihoodExtractor) junctionTreeAlgorithm.getLikelihoodExtractor();
				conditionalProbabilityExtractor = jeffreyRuleLikelihoodExtractor.getConditionalProbabilityExtractor();
			}
		}
		if (conditionalProbabilityExtractor == null) {
			conditionalProbabilityExtractor = InCliqueConditionalProbabilityExtractor.newInstance();
		}
		for (Node node : getRelatedProbabilisticNetwork().getNodes()) {
			if (node instanceof ProbabilisticNode) {
				ProbabilisticNode mainNode = (ProbabilisticNode) node;
				PotentialTable newCPT = (PotentialTable) conditionalProbabilityExtractor.buildCondicionalProbability(mainNode, node.getParentNodes(), getRelatedProbabilisticNetwork(), this.getProbabilityPropagationDelegator());
				PotentialTable oldCPT = mainNode.getProbabilityFunction();
				// TODO check if the ordering of the parents are OK
				oldCPT.setValues(newCPT.getValues());
			}
		}
	}
	
	 
	

}
