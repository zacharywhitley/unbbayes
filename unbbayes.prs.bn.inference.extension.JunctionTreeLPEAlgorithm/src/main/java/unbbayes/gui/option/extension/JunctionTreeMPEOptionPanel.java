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

	private static final long serialVersionUID = 7309121020731882224L;

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
