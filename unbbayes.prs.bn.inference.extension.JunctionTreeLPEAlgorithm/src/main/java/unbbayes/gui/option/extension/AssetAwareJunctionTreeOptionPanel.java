/**
 * 
 */
package unbbayes.gui.option.extension;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import unbbayes.controller.INetworkMediator;
import unbbayes.gui.option.JunctionTreeOptionPanel;
import unbbayes.prs.bn.JunctionTreeAlgorithm;
import unbbayes.prs.bn.inference.extension.AssetAwareInferenceAlgorithm;
import unbbayes.util.Debug;

/**
 * @author Shou Matsumoto
 *
 */
public class AssetAwareJunctionTreeOptionPanel extends JunctionTreeOptionPanel {

	private static final long serialVersionUID = -528991137535805602L;
	
	private JLabel mainLabel;
	private JTextField assetQuantityTextField;
	
	/**
	 * Default constructor must be public
	 */
	public AssetAwareJunctionTreeOptionPanel() {
		super();
		// Instantiate an AssetAwareInferenceAlgorithm which works like a JunctionTreeAlgorithm
		JunctionTreeAlgorithm delegator = new JunctionTreeAlgorithm();
		delegator.setOptionPanel(this);
		AssetAwareInferenceAlgorithm alg = (AssetAwareInferenceAlgorithm)AssetAwareInferenceAlgorithm.getInstance(delegator);
		alg.setToAllowZeroAssets(true);
		alg.setToCalculateMarginalsOfAssetNodes(true);	// force GUI to display marginal assets (min-assets by default)
		alg.setToLogAssets(true);	// enable log
		alg.setToUseQValues(true);
		this.setInferenceAlgorithm(alg);
		this.setName("Asset Propagation Algorithm");
		// the following are already called by super()
//		this.initComponents();
//		this.initListeners();
	}


	/**
	 * This method initializes all JComponents
	 */
	protected void initComponents() {
//		this.setLayout(new FlowLayout());
		super.initComponents();
		this.setMainLabel(new JLabel("Insert initial asset quantity for each node:"));
		this.add(this.getMainLabel());
		
		try {
			this.setAssetQuantityTextField(new JTextField(String.valueOf(((AssetAwareInferenceAlgorithm) this.getInferenceAlgorithm()).getDefaultInitialAssetTableValue())));
		} catch (Exception e) {
			Debug.println(getClass(), e.getMessage(), e);
			this.setAssetQuantityTextField(new JTextField("1000"));
		}
		
		this.add(this.getAssetQuantityTextField());
	}
	
	/**
	 * This method initializes all listeners of the components
	 * created in {@link #initComponents()}
	 */
	protected void initListeners() {
		super.initListeners();
		
		// listener to remove all virtual nodes when returning to edit mode
		setClearVirtualNodesAncestorListener(new AncestorListener() {
			public void ancestorRemoved(AncestorEvent event) {}
			public void ancestorMoved(AncestorEvent event) {}
			/**
			 * This is called when the card layout sets the edit mode pane visible
			 */
			public void ancestorAdded(AncestorEvent event) {
				// extract junction tree algorithm, so that we can remove virtual nodes generated by such algorithm
				
				JunctionTreeAlgorithm junctionTreeAlgorithm = null;	// this will store the extracted junction tree algorithm
				
				if (getInferenceAlgorithm() instanceof AssetAwareInferenceAlgorithm) {
					// if this is AssetAwareInferenceAlgorithm, it has 2 inference algorithm: 1 for probs, 1 for assets. Use the one for probs/
					AssetAwareInferenceAlgorithm assetAwareInferenceAlgorithm = (AssetAwareInferenceAlgorithm) getInferenceAlgorithm();
					if (assetAwareInferenceAlgorithm.getProbabilityPropagationDelegator() instanceof JunctionTreeAlgorithm) {
						junctionTreeAlgorithm = (JunctionTreeAlgorithm) assetAwareInferenceAlgorithm.getProbabilityPropagationDelegator();
					}
				} else if (getInferenceAlgorithm() instanceof JunctionTreeAlgorithm) {
					// if for some reason this option dialog is using a JunctionTreeAlgorithm directly, use it
					junctionTreeAlgorithm = (JunctionTreeAlgorithm) getInferenceAlgorithm();
				}
				
				try {
					// actually clear virtual nodes. If junctionTreeAlgorithm could not be extracted, it will fall into the following "catch" clause
					junctionTreeAlgorithm.clearVirtualNodes();
				} catch (Exception t) {
					t.printStackTrace();
					try {
						// TODO use resources
						JOptionPane.showMessageDialog(AssetAwareJunctionTreeOptionPanel.this, "Could not clear virtual nodes: " + t.getMessage());
					} catch (Throwable t2) {
						t2.printStackTrace();
					}
				}
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.InferenceAlgorithmOptionPanel#setMediator(unbbayes.controller.INetworkMediator)
	 */
	public void setMediator(INetworkMediator mediator) {
		super.setMediator(mediator);
		try {
			this.getInferenceAlgorithm().setMediator(mediator);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(AssetAwareJunctionTreeOptionPanel.this, 
					e.getMessage(), 
					AssetAwareJunctionTreeOptionPanel.this.getName(), 
					JOptionPane.ERROR_MESSAGE); 
		}
	}


	/* (non-Javadoc)
	 * @see unbbayes.gui.option.JunctionTreeOptionPanel#commitChanges()
	 */
	public void commitChanges() {
		super.commitChanges();
		try {
			((AssetAwareInferenceAlgorithm) this.getInferenceAlgorithm()).setDefaultInitialAssetTableValue(Float.parseFloat(this.getAssetQuantityTextField().getText()));
			((AssetAwareInferenceAlgorithm) this.getInferenceAlgorithm()).setToAllowZeroAssets(true);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Could not commit change: " + e.getMessage());
		}
		
		// the checkbox for soft evidence...
		if (getInferenceAlgorithm() instanceof AssetAwareInferenceAlgorithm) {
			AssetAwareInferenceAlgorithm assetAware = (AssetAwareInferenceAlgorithm) getInferenceAlgorithm();
			if (assetAware.getProbabilityPropagationDelegator() instanceof JunctionTreeAlgorithm) {
				JunctionTreeAlgorithm junctionTreeAlgorithm = (JunctionTreeAlgorithm) assetAware.getProbabilityPropagationDelegator();
				if (this.getUseSoftEvidenceCheckBox().isSelected()) {
					junctionTreeAlgorithm.setLikelihoodExtractor(this.getSoftEvidenceLikelihoodExtractor());
					if (getMediator() != null) {
						getMediator().getScreen().getEvidenceTree().setLikelihoodEvidenceDialogBuilder(getSoftEvidenceDialogBuilder());
					}
				} else{
					junctionTreeAlgorithm.setLikelihoodExtractor(this.getLikelihoodEvidenceLikelihoodExtractor()); 
					if (getMediator() != null) {
						getMediator().getScreen().getEvidenceTree().setLikelihoodEvidenceDialogBuilder(getLikelihoodEvidenceDialogBuilder());
					}
				}
			}
		}
		// remove listener that removes all virtual nodes from network
		if (this.getMediator() != null && this.getClearVirtualNodesAncestorListener() != null) {
			// remove listener from old module
			this.getMediator().getScreen().getNetWindowEdition().removeAncestorListener(this.getClearVirtualNodesAncestorListener());
		}
		// add again if the check box for removing virtual nodes is checked
		if (this.getRemoveVirtualNodeCheckBox().isSelected()) {
			if (this.getMediator() != null && this.getClearVirtualNodesAncestorListener() != null) {
				this.getMediator().getScreen().getNetWindowEdition().addAncestorListener(this.getClearVirtualNodesAncestorListener());
			}
		}
	}


	/* (non-Javadoc)
	 * @see unbbayes.gui.option.JunctionTreeOptionPanel#revertChanges()
	 */
	@Override
	public void revertChanges() {
		super.revertChanges();
		try {
			this.getAssetQuantityTextField().setText(String.valueOf(((AssetAwareInferenceAlgorithm) this.getInferenceAlgorithm()).getDefaultInitialAssetTableValue()));
		} catch (Exception e) {
			Debug.println(getClass(), e.getMessage(), e);
			this.getAssetQuantityTextField().setText("1000");
			this.repaint();
			this.getAssetQuantityTextField().repaint();
		}
	}


	/**
	 * @return the mainLabel
	 */
	public JLabel getMainLabel() {
		return mainLabel;
	}


	/**
	 * @param mainLabel the mainLabel to set
	 */
	public void setMainLabel(JLabel mainLabel) {
		this.mainLabel = mainLabel;
	}


	/**
	 * @return the assetQuantityTextField
	 */
	public JTextField getAssetQuantityTextField() {
		return assetQuantityTextField;
	}


	/**
	 * @param assetQuantityTextField the assetQuantityTextField to set
	 */
	public void setAssetQuantityTextField(JTextField assetQuantityTextField) {
		this.assetQuantityTextField = assetQuantityTextField;
	}
	
}
