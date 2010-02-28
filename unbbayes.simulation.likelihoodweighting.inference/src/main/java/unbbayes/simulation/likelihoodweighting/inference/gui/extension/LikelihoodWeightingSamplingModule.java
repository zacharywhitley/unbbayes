/**
 * 
 */
package unbbayes.simulation.likelihoodweighting.inference.gui.extension;

import java.io.File;
import java.io.IOException;

import unbbayes.gui.UnBBayesFrame;
import unbbayes.io.BaseIO;
import unbbayes.io.OwnerAwareFileExtensionIODelegator;
import unbbayes.prs.Graph;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.simulation.likelihoodweighting.sampling.LikelihoodWeightingSampling;
import unbbayes.simulation.montecarlo.controller.MCMainController;
import unbbayes.util.extension.UnBBayesModule;
import unbbayes.util.extension.UnBBayesModuleBuilder;

/**
 * This class converts the Likelihood Weighting sampling tool to a UnBBayes module plugin.
 * @author Shou Matsumoto
 *
 */
public class LikelihoodWeightingSamplingModule extends UnBBayesModule implements UnBBayesModuleBuilder {
	
	private static final long serialVersionUID = 1L;

	private String name = "Likelihood Weighting";
	
	private BaseIO io;

	private MCMainController lastBuiltMcMainController;
	
	/**
	 * Default constructor.
	 */
	public LikelihoodWeightingSamplingModule() {
		super();
		
		// setting up the i/o classes used by UnBBayesFrame in order to load a file from the main pane
		this.io = new OwnerAwareFileExtensionIODelegator(this);
		
		// let this frame to be invisible
		this.setVisible(false);
	}
	 
	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#setVisible(boolean)
	 */
	public void setVisible(boolean visible){
		// this is a workarount to assure this is always invisible
		super.setVisible(false);
	};

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.UnBBayesModuleBuilder#buildUnBBayesModule()
	 */
	public UnBBayesModule buildUnBBayesModule() {
		return this;
	}
	
	
	
	/* (non-Javadoc)
	 * @see unbbayes.util.extension.UnBBayesModule#setUnbbayesFrame(unbbayes.gui.UnBBayesFrame)
	 */
	public void setUnbbayesFrame(UnBBayesFrame unbbayesFrame) {
		super.setUnbbayesFrame(unbbayesFrame);
		// this is a workaround in order to start process only when the UnBBayesFrame is set (that means
		// this module has been added as its internal frame, since this method is called on UnBBayesFrame#add(JUnBBayesModule))
		try {
			lastBuiltMcMainController = new MCMainController(new LikelihoodWeightingSampling());
		} catch (Throwable t) {
			t.printStackTrace();
		}

		unbbayesFrame.getDesktop().remove(this);
		this.dispose();
	}



	/*
	 * (non-Javadoc)
	 * @see unbbayes.util.extension.UnBBayesModuleBuilder#getName()
	 */
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.util.extension.UnBBayesModuleBuilder#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.util.extension.UnBBayesModule#getModuleName()
	 */
	public String getModuleName() {
		return this.getName();
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.util.extension.UnBBayesModule#openFile(java.io.File)
	 */
	public UnBBayesModule openFile(File file) throws IOException {
		try {
			this.lastBuiltMcMainController = new MCMainController(new LikelihoodWeightingSampling(), false);
			this.lastBuiltMcMainController.setPn((ProbabilisticNetwork)this.getIO().load(file));
		} catch (ClassCastException e) {
			throw new IllegalArgumentException("The loaded file must be a ProbabilisticNetwork", e);
		}
		
		// updating startup parameters
		this.lastBuiltMcMainController.startupParameters();
		
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.gui.IPersistenceAwareWindow#getPersistingGraph()
	 */
	public Graph getPersistingGraph() {
		return this.lastBuiltMcMainController.getPn();
	}
	
	

	/*
	 * (non-Javadoc)
	 * @see unbbayes.gui.IPersistenceAwareWindow#getIO()
	 */
	public BaseIO getIO() {
		return io;
	}

	/**
	 * @param io the io to set
	 */
	public void setIO(BaseIO io) {
		this.io = io;
	}

}