/**
 * 
 */
package unbbayes.gui.option.extension;

import unbbayes.prs.bn.inference.extension.JunctionTreeLPEComplementAlgorithm;

/**
 * @author Shou Matsumoto
 *
 */
public class JunctionTreeLPEComplementOptionPanel extends JunctionTreeMPEOptionPanel {

	/**
	 * 
	 */
	public JunctionTreeLPEComplementOptionPanel() {
		JunctionTreeLPEComplementAlgorithm algToSet = new JunctionTreeLPEComplementAlgorithm();
		algToSet.setOptionPanel(this);
		this.setInferenceAlgorithm(algToSet);
		this.setName("JunctionTreeComplementLPE");
	}
	
	

}
