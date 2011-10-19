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
		this.setInferenceAlgorithm(new JunctionTreeLPEAlgorithm());
		this.setName("JunctionTreeLPE");
	}
	
	

}
