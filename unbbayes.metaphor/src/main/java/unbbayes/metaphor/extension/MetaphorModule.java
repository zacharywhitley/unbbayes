/**
 * 
 */
package unbbayes.metaphor.extension;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ResourceBundle;

import unbbayes.io.BaseIO;
import unbbayes.io.FileExtensionIODelegator;
import unbbayes.io.NetIO;
import unbbayes.io.XMLBIFIO;
import unbbayes.io.exception.LoadException;
import unbbayes.io.exception.UBIOException;
import unbbayes.metaphor.MetaphorFrame;
import unbbayes.metaphor.resources.Resources;
import unbbayes.prs.Graph;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.util.ResourceController;
import unbbayes.util.extension.UnBBayesModule;
import unbbayes.util.extension.UnBBayesModuleBuilder;

/**
 * Sample class that converts Medical Metaphor project into a plugin for UnBBayes core.
 * @author Shou Matsumoto
 *
 */
public class MetaphorModule extends UnBBayesModule implements UnBBayesModuleBuilder {

	private static final long serialVersionUID = -765155649460722440L;
	
	private BaseIO io;
	private MetaphorFrame frame;
	
	private ResourceBundle resource = ResourceController.newInstance().getBundle(Resources.class.getName());
	
	public MetaphorModule() {
		super();
		
		this.setTitle(this.resource.getString("frameTitle"));
		
		this.setFrame(new MetaphorFrame());
		this.getFrame().setVisible(false);
		
		this.add(this.getFrame().getContentPane());
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
		if (this.getFrame() != null) {
			this.getFrame().getContentPane().setVisible(flag);
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
		try {
			return this.getFrame().getMainPanel().getNet();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see unbbayes.gui.IPersistenceAwareWindow#getSavingMessage()
	 */
	public String getSavingMessage() {
		return this.resource.getString("saveFile");
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
		return this.resource.getString("supportedFileDescription");
	}




	/*
	 * (non-Javadoc)
	 * @see unbbayes.util.extension.UnBBayesModule#getModuleName()
	 */
	@Override
	public String getModuleName() {
		return this.resource.getString("moduleName");
	}


	/*
	 * (non-Javadoc)
	 * @see unbbayes.util.extension.UnBBayesModule#openFile(java.io.File)
	 */
	@Override
	public UnBBayesModule openFile(File file) throws IOException {
		
		try {
			this.getFrame().getMainPanel().setNet((ProbabilisticNetwork)(this.getIO().load(file)));
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



	/**
	 * @return the frame
	 */
	public MetaphorFrame getFrame() {
		return frame;
	}



	/**
	 * @param frame the frame to set
	 */
	public void setFrame(MetaphorFrame frame) {
		this.frame = frame;
	}


}
