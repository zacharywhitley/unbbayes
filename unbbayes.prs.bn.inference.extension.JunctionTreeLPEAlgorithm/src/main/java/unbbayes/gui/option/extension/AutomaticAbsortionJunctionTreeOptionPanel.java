/**
 * 
 */
package unbbayes.gui.option.extension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import unbbayes.gui.option.JunctionTreeOptionPanel;
import unbbayes.prs.INode;
import unbbayes.prs.Node;
import unbbayes.prs.bn.JunctionTreeAlgorithm;
import unbbayes.prs.bn.TreeVariable;
import unbbayes.prs.bn.inference.extension.AssetAwareInferenceAlgorithm;
import unbbayes.prs.bn.inference.extension.IAssetNetAlgorithm;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithm;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithmListener;

/**
 * This is a plugin extension (and a option panel) for
 * junction tree algorithm which automatically absorbs
 * evidences.
 * @author Shou Matsumoto
 *
 */
public class AutomaticAbsortionJunctionTreeOptionPanel extends JunctionTreeOptionPanel {

	private static final long serialVersionUID = -185054328252707270L;

	/**
	 * Default constructor
	 */
	public AutomaticAbsortionJunctionTreeOptionPanel() {
		super();
		// Instantiate an AssetAwareInferenceAlgorithm which works like a JunctionTreeAlgorithm
		JunctionTreeAlgorithm delegator = new JunctionTreeAlgorithm();
		delegator.setLikelihoodExtractor(AssetAwareInferenceAlgorithm.DEFAULT_JEFFREYRULE_LIKELIHOOD_EXTRACTOR);
		delegator.setOptionPanel(this);
		AssetAwareInferenceAlgorithm alg = (AssetAwareInferenceAlgorithm)AssetAwareInferenceAlgorithm.getInstance(delegator);
		// clear listener
		alg.removeInferencceAlgorithmListener(null);
		// listener which will automatically absorb hard evidences
		alg.addInferencceAlgorithmListener(new IInferenceAlgorithmListener() {
			/** Backup mediator and disable it temporary, so that we disallow algorithm to access GUI*/
			public void onBeforeRun(IInferenceAlgorithm algorithm) {}
			public void onBeforeReset(IInferenceAlgorithm algorithm) {}
			/** nodes with hard evidences will be deleted */
			public void onBeforePropagate(IInferenceAlgorithm algorithm) {
				if (algorithm instanceof IAssetNetAlgorithm) {
					IAssetNetAlgorithm assetNetAlgorithm = (IAssetNetAlgorithm) algorithm;
					Map<INode, List<Float>> evidences = new HashMap<INode, List<Float>>();	// this map will store the evidences
					for (Node node : algorithm.getNetwork().getNodes()) {
						if (node instanceof TreeVariable) {
							TreeVariable variable = (TreeVariable) node;
							if (!variable.hasLikelihood() && variable.hasEvidence()) {
								List<Float> prob = new ArrayList<Float>(variable.getStatesSize());
								// TODO suport negative evidence
								int state = variable.getEvidence();
								for (int i = 0; i < variable.getStatesSize(); i++) {
									prob.add((state==i)?1f:0f);
								}
								evidences.put(variable, prob );
								variable.resetEvidence();	// clear evidence
							}
						}
					}
					// absorb and delete nodes with hard evidence
					assetNetAlgorithm.setAsPermanentEvidence(evidences, true);
				}
			}
			/** Restore mediator, so that we re-enable GUI access from algorithm */
			public void onAfterRun(IInferenceAlgorithm algorithm) {}
			public void onAfterReset(IInferenceAlgorithm algorithm) {}
			/** Update CPTs regarding current clique potentials */
			public void onAfterPropagate(IInferenceAlgorithm algorithm) {
				if (algorithm instanceof AssetAwareInferenceAlgorithm) {
					IInferenceAlgorithm probabilityDelegator = ((AssetAwareInferenceAlgorithm)algorithm).getProbabilityPropagationDelegator();
					if (probabilityDelegator instanceof JunctionTreeAlgorithm) {
						((JunctionTreeAlgorithm)probabilityDelegator).updateCPTBasedOnCliques();
					}
				}
			}
		});
		// add listener which will delete all virtual nodes
		alg.addInferencceAlgorithmListener(JunctionTreeAlgorithm.CLEAR_VIRTUAL_NODES_ALGORITHM_LISTENER);
		// make sure the algorithm responsible for probabilities are also using the same listener (not mandatory, but just to make sure)
		alg.getProbabilityPropagationDelegator().removeInferencceAlgorithmListener(null);	// clear listener
		alg.getProbabilityPropagationDelegator().addInferencceAlgorithmListener(JunctionTreeAlgorithm.CLEAR_VIRTUAL_NODES_ALGORITHM_LISTENER);
		
		// do not think about assets, although we are using AssetAwareInferenceAlgorithm
		alg.setToUpdateAssets(false);
		alg.setToChangeGUI(false);	// do not allow algorithm to add extra panels into GUI
		// other auxiliary config
		alg.setToAllowZeroAssets(true);
		alg.setToCalculateMarginalsOfAssetNodes(false);	
		alg.setToLogAssets(true);
		this.setInferenceAlgorithm(alg);
		this.setName("Junction tree with absortion");
		alg.setName(this.getName());
		
		this.removeAll();
		this.initComponents();
		this.initListeners();
		
		// by default, mark the option "delete virtual nodes" to true
		getRemoveVirtualNodeCheckBox().setSelected(true);
		getRemoveVirtualNodeCheckBox().setEnabled(false);
		getUseSoftEvidenceCheckBox().setSelected(true);
		getUseSoftEvidenceCheckBox().setEnabled(false);
	}
	
	public void commitChanges() {
		// the checkbox for soft evidence...
		if (getInferenceAlgorithm() instanceof AssetAwareInferenceAlgorithm) {
			JunctionTreeAlgorithm junctionTreeAlgorithm = (JunctionTreeAlgorithm) ((AssetAwareInferenceAlgorithm) getInferenceAlgorithm()).getProbabilityPropagationDelegator();
			if (this.getUseSoftEvidenceCheckBox().isSelected()) {
				junctionTreeAlgorithm.setLikelihoodExtractor(this.getSoftEvidenceLikelihoodExtractor());
				if (getMediator() != null) {
					getMediator().getScreen().getEvidenceTree().setLikelihoodEvidenceDialogBuilder(getSoftEvidenceDialogBuilder());
				}

			} else {
				junctionTreeAlgorithm.setLikelihoodExtractor(this.getLikelihoodEvidenceLikelihoodExtractor()); 
				if (getMediator() != null) {
					getMediator().getScreen().getEvidenceTree().setLikelihoodEvidenceDialogBuilder(getLikelihoodEvidenceDialogBuilder());
				}
			}
		}
	}

}
