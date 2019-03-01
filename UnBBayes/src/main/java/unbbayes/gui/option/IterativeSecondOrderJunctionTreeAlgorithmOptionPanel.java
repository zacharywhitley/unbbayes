package unbbayes.gui.option;

import unbbayes.prs.bn.IterativeSecondOrderJunctionTreeAlgorithm;

/**
 * This is the default option panel for {@link IterativeSecondOrderJunctionTreeAlgorithm}
 * @author Shou Matsumoto
 */
public class IterativeSecondOrderJunctionTreeAlgorithmOptionPanel extends JunctionTreeOptionPanel {

	/**
	 * Default constructor is kept public in order to allow plugin
	 * framework to instantiate it
	 */
	public IterativeSecondOrderJunctionTreeAlgorithmOptionPanel() {
		super();
		this.removeAll();
		IterativeSecondOrderJunctionTreeAlgorithm alg = new IterativeSecondOrderJunctionTreeAlgorithm();
		alg.setOptionPanel(this);
		this.setInferenceAlgorithm(alg);
		this.initComponents();
		this.initListeners();
	}



}
