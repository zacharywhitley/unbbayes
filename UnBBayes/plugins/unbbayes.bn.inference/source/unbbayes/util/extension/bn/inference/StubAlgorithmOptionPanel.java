/**
 * 
 */
package unbbayes.util.extension.bn.inference;

import unbbayes.gui.option.GibbsSamplingOptionPanel;

/**
 * This is a stub for testing purpose.
 * Do not use it.
 * @author Shou Matsumoto
 *
 */
public class StubAlgorithmOptionPanel extends GibbsSamplingOptionPanel {

	
	
	private static final long serialVersionUID = 2436368845782870026L;

	public StubAlgorithmOptionPanel() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see unbbayes.gui.option.GibbsSamplingOptionPanel#commitChanges()
	 */
	@Override
	public void commitChanges() {
		super.commitChanges();
		System.out.println("Committing changes on stub");
	}

	/* (non-Javadoc)
	 * @see unbbayes.gui.option.GibbsSamplingOptionPanel#getInferenceAlgorithm()
	 */
	@Override
	public IInferenceAlgorithm getInferenceAlgorithm() {
		return new StubInferenceAlgorithm();
	}

	
	
}
