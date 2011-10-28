/**
 * 
 */
package unbbayes.gui.option.extension;

import unbbayes.prs.bn.inference.extension.JunctionTreeLPEAlgorithm;

/**
 * @author Shou Matsumoto
 *
 */
public class JunctionTreeLPEOptionPanel extends JunctionTreeMPEOptionPanel {

	/**
	 * 
	 */
	public JunctionTreeLPEOptionPanel() {
		JunctionTreeLPEAlgorithm algToSet = new JunctionTreeLPEAlgorithm();
		algToSet.setMediator(this.getMediator());
		this.setInferenceAlgorithm(algToSet);
		this.setName("JunctionTreeLPE");
	}
	
	

}
