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
		this.setInferenceAlgorithm(new JunctionTreeMPEAlgorithm());
		this.setName("JunctionTreeMPE");
	}

}
