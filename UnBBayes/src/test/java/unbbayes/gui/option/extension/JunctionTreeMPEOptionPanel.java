/**
 * 
 */
package unbbayes.gui.option.extension;

import unbbayes.gui.option.JunctionTreeOptionPanel;
import unbbayes.prs.bn.inference.extension.JunctionTreeMPEAlgorithm;

/**
 * @author Shou Matsumoto
 *
 */
public class JunctionTreeMPEOptionPanel extends JunctionTreeOptionPanel {

	/**
	 * Default constructor of plug-ins must be public.
	 */
	public JunctionTreeMPEOptionPanel() {
		super();
		JunctionTreeMPEAlgorithm algorithmToSet = new JunctionTreeMPEAlgorithm();
		algorithmToSet.setOptionPanel(this);
		this.setInferenceAlgorithm(algorithmToSet);
		this.setName("JunctionTreeMPE");
	}

	
	
}
