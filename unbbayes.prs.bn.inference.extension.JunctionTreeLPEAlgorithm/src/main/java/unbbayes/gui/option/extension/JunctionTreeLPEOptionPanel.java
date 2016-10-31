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

	private static final long serialVersionUID = -7418822697405220796L;

	/**
	 * 
	 */
	public JunctionTreeLPEOptionPanel() {
		JunctionTreeLPEAlgorithm algToSet = new JunctionTreeLPEAlgorithm();
		algToSet.setOptionPanel(this);
		this.setInferenceAlgorithm(algToSet);
		this.setName("JunctionTreeLPE");
	}
	
	

}
