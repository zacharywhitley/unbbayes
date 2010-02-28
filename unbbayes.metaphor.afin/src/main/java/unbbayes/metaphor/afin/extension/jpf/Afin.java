/**
 * 
 */
package unbbayes.metaphor.afin.extension.jpf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import unbbayes.controller.exception.InvalidFileNameException;
import unbbayes.io.BaseIO;
import unbbayes.io.FileExtensionIODelegator;
import unbbayes.io.NetIO;
import unbbayes.io.XMLBIFIO;
import unbbayes.io.exception.LoadException;
import unbbayes.io.exception.UBIOException;
import unbbayes.metaphor.afin.AFINMetaphorMainPanel;
import unbbayes.prs.Graph;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.util.extension.UnBBayesModule;
import unbbayes.util.extension.UnBBayesModuleBuilder;

/**
 * Sample class that converts Afin metaphor into a plugin for UnBBayes core.
 * @author Shou Matsumoto
 *
 */
public class Afin extends UnBBayesModule implements UnBBayesModuleBuilder {

	private BaseIO io;
	private AFINMetaphorMainPanel mainPanel;
	
	public Afin() {
		super("Metaphor");
		this.mainPanel = new AFINMetaphorMainPanel();
		this.add(this.mainPanel);
		this.setVisible(false);
		FileExtensionIODelegator delegator = FileExtensionIODelegator.newInstance();
		delegator.setDelegators(new ArrayList<BaseIO>());
		delegator.getDelegators().add(new NetIO());
		delegator.getDelegators().add(new XMLBIFIO());
		this.setIO(delegator);
	}
	
	

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean flag) {
		super.setVisible(flag);
		if (this.mainPanel != null) {
			this.mainPanel.setVisible(flag);
		}
	}



	/* (non-Javadoc)
	 * @see unbbayes.gui.IPersistenceAwareWindow#getIO()
	 */
	public BaseIO getIO() {
		return this.io;
	}
	
	public void setIO(BaseIO io) {
		this.io = io;
	}


	/* (non-Javadoc)
	 * @see unbbayes.gui.IPersistenceAwareWindow#getPersistingGraph()
	 */
	public Graph getPersistingGraph() {
		return this.getMainPanel().getNet();
	}

	/* (non-Javadoc)
	 * @see unbbayes.gui.IPersistenceAwareWindow#getSavingMessage()
	 */
	public String getSavingMessage() {
		return "Save Afin";
	}

	/* (non-Javadoc)
	 * @see unbbayes.gui.IPersistenceAwareWindow#getSupportedFileExtensions()
	 */
	public String[] getSupportedFileExtensions(boolean isLoadOnly) {
		String[] ret = {"net", "xml"};
		return ret;
	}

	/* (non-Javadoc)
	 * @see unbbayes.gui.IPersistenceAwareWindow#getSupportedFilesDescription()
	 */
	public String getSupportedFilesDescription(boolean isLoadOnly) {
		return "Metaphor Files (.net, .xml)";
	}

	/**
	 * @return the mainPanel
	 */
	public AFINMetaphorMainPanel getMainPanel() {
		return mainPanel;
	}

	/**
	 * @param mainPanel the mainPanel to set
	 */
	public void setMainPanel(AFINMetaphorMainPanel mainPanel) {
		this.mainPanel = mainPanel;
	}



	/*
	 * (non-Javadoc)
	 * @see unbbayes.util.extension.UnBBayesModule#getModuleName()
	 */
	@Override
	public String getModuleName() {
		return "Metaphor";
	}


	/*
	 * (non-Javadoc)
	 * @see unbbayes.util.extension.UnBBayesModule#openFile(java.io.File)
	 */
	@Override
	public UnBBayesModule openFile(File file) throws IOException {
		
		try {
			this.getMainPanel().setNet((ProbabilisticNetwork)(this.getIO().load(file)));
		} catch (LoadException e) {
			throw new UBIOException(e);
		}
		
		return this;
		
	}



	/**
	 * A extension of IOException in order to support cause's stack tracing.
	 * @author Shou Matsumoto
	 *
	 */
	public class InvalidFileNameException extends IOException {
		private static final long serialVersionUID = -2604565168902414428L;
		public InvalidFileNameException (String msg, Throwable t) {
			super(msg);
			this.initCause(t);
		}
	}



	/*
	 * (non-Javadoc)
	 * @see unbbayes.util.extension.UnBBayesModuleBuilder#buildUnBBayesModule()
	 */
	public UnBBayesModule buildUnBBayesModule() {
		return this;
	}


}
