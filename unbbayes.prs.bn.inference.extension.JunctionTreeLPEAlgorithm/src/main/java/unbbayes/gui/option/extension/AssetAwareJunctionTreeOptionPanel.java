/**
 * 
 */
package unbbayes.gui.option.extension;

import javax.swing.JOptionPane;

import unbbayes.controller.INetworkMediator;
import unbbayes.gui.option.JunctionTreeOptionPanel;
import unbbayes.prs.bn.JunctionTreeAlgorithm;
import unbbayes.prs.bn.inference.extension.AssetAwareInferenceAlgorithm;

/**
 * @author Shou Matsumoto
 *
 */
public class AssetAwareJunctionTreeOptionPanel extends JunctionTreeOptionPanel {

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
	
}
