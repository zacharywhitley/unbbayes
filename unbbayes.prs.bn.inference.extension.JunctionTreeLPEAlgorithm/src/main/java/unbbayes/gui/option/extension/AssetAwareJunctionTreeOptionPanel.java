/**
 * 
 */
package unbbayes.gui.option.extension;

import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

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

	private JLabel mainLabel;
	private JTextField assetQuantityTextField;
	private ComponentListener deleteDummyNodesListener = new ComponentListener() {
		public void componentShown(ComponentEvent e) {
			getClearVirtualNodesActionListener().actionPerformed(new ActionEvent(e.getSource(), e.getID(), e.getComponent().toString()));
		}
		public void componentResized(ComponentEvent e) {}
		public void componentMoved(ComponentEvent e) {}
		public void componentHidden(ComponentEvent e) {}
	};


	/**
	 * Default constructor must be public
	 */
	public AssetAwareJunctionTreeOptionPanel() {
		super();
		// Instantiate an AssetAwareInferenceAlgorithm which works like a JunctionTreeAlgorithm
		JunctionTreeAlgorithm delegator = new JunctionTreeAlgorithm();
		delegator.setOptionPanel(this);
		this.setInferenceAlgorithm(AssetAwareInferenceAlgorithm.getInstance(delegator));
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
			this.setAssetQuantityTextField(new JTextField(String.valueOf(((AssetAwareInferenceAlgorithm) this.getInferenceAlgorithm()).getDefaultInitialAssetQuantity())));
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
			((AssetAwareInferenceAlgorithm) this.getInferenceAlgorithm()).setDefaultInitialAssetQuantity(Float.parseFloat(this.getAssetQuantityTextField().getText()));
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
		if (this.getMediator() != null && this.getDeleteDummyNodesListener() != null) {
			// remove listener from old module
			this.getMediator().getScreen().getNetWindowEdition().removeComponentListener(getDeleteDummyNodesListener());
		}
		// add again if the check box for removing virtual nodes is checked
		if (this.getRemoveVirtualNodeCheckBox().isSelected()) {
			if (this.getMediator() != null && this.getDeleteDummyNodesListener() != null) {
				this.getMediator().getScreen().getNetWindowEdition().addComponentListener(getDeleteDummyNodesListener());
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
			this.getAssetQuantityTextField().setText(String.valueOf(((AssetAwareInferenceAlgorithm) this.getInferenceAlgorithm()).getDefaultInitialAssetQuantity()));
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


	/**
	 * @param deleteDummyNodesListener the deleteDummyNodesListener to set
	 */
	public void setDeleteDummyNodesListener(ComponentListener deleteDummyNodesListener) {
		this.deleteDummyNodesListener = deleteDummyNodesListener;
	}


	/**
	 * @return the deleteDummyNodesListener
	 */
	public ComponentListener getDeleteDummyNodesListener() {
		return deleteDummyNodesListener;
	}
	
	
	
}
