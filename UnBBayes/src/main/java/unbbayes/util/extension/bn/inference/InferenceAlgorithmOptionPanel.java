/**
 * 
 */
package unbbayes.util.extension.bn.inference;

import javax.swing.JPanel;

import unbbayes.controller.INetworkMediator;

/**
 * JPanel containing some forms that a user 
 * can fill up. By extending this panel, we can initialize some 
 * attributes for inference algorithm.
 * This panel is usually implemented
 * in a way that any change on the fields reflects 
 * directly the attributes of the algorithm.
 * 
 * @author Shou Matsumoto
 *
 */
public abstract class InferenceAlgorithmOptionPanel extends JPanel {
	
	private static final long serialVersionUID = 1377907003286358357L;
	
	private INetworkMediator mediator;

	/**
	 * Default constructor for plugin support
	 */
	public InferenceAlgorithmOptionPanel() {
		super();
	}

	/**
	 * Obtains the algorithm to run.
	 * @return
	 */
	public abstract IInferenceAlgorithm getInferenceAlgorithm();
	
	/**
	 * Commit the changes of the parameters given by the user.
	 */
	public abstract void commitChanges();
	
	/**
	 * Revert the changes of the parameters given by the user.
	 */
	public abstract void revertChanges();
	
	/**
	 * Obtains the main controller (mediator) of BN module.
	 * @return the mediator (main controller)
	 */
	public INetworkMediator getMediator() {
		return this.mediator;
	}
	
	/**
	 * The main controller (mediator) of BN module.
	 * @param mediator 
	 */
	public void setMediator(INetworkMediator mediator){
		this.mediator = mediator;
	}
	
}
