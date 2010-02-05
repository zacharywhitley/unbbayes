/**
 * 
 */
package unbbayes.simulation.likelihoodweighting.inference.gui.extension;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.border.TitledBorder;

import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.simulation.likelihoodweighting.sampling.LikelihoodWeightingSampling;
import unbbayes.simulation.montecarlo.controller.MCMainController;
import unbbayes.simulation.montecarlo.gui.extension.MonteCarloModule;
import unbbayes.util.Debug;
import unbbayes.util.extension.UnBBayesModule;

/**
 * @author Shou Matsumoto
 *
 */
public class LikelihoodWeightingSamplingModule extends MonteCarloModule {

	private static final long serialVersionUID = 1L;


	/**
	 * Default constructor.
	 */
	public LikelihoodWeightingSamplingModule() {
		super();
		
		// reusing the FileExtensionIODelegator's customization. It uses the current name as descriptor
		this.setName("LikelihoodWeighting");
		this.setIO(new MonteCarloFileExtensionIODelegator(this));
		
		this.getScrollPane().setBorder(new TitledBorder("LikelihoodWeighting"));
		this.getButton().removeActionListener(this.getButtonActionListener());
		this.setButtonActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				try {
					setLastBuiltMcMainController(new MCMainController(new LikelihoodWeightingSampling()));
				} catch (Exception exc) {
					Debug.println(this.getClass(), "Exception at MCMainController", exc);
				} catch (Error err) {
					err.printStackTrace();
				}
			}
		});
	}

	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.simulation.montecarlo.gui.extension.MonteCarloModule#openFile(java.io.File)
	 */
	public UnBBayesModule openFile(File file) throws IOException {
		try {
			this.setLastBuiltMcMainController(new MCMainController(new LikelihoodWeightingSampling(), false));
			this.getLastBuiltMcMainController().setPn((ProbabilisticNetwork)this.getIO().load(file));
		} catch (ClassCastException e) {
			throw new IllegalArgumentException("The loaded file must be a ProbabilisticNetwork", e);
		}
		
		// updating startup parameters
		this.getLastBuiltMcMainController().startupParameters();
		
		return null;
	}

}
