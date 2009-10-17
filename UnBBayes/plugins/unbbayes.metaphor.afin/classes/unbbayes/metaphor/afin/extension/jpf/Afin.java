/**
 * 
 */
package unbbayes.metaphor.afin.extension.jpf;

import unbbayes.io.BaseIO;
import unbbayes.io.FileExtensionIODelegator;
import unbbayes.metaphor.afin.AFINMetaphorMainPanel;
import unbbayes.prs.Graph;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.util.extension.UnBBayesModule;

/**
 * Sample class that converts Afin metaphor into a plugin for UnBBayes core.
 * @author Shou Matsumoto
 *
 */
public class Afin extends UnBBayesModule {

	private BaseIO io = FileExtensionIODelegator.newInstance();
	private AFINMetaphorMainPanel mainPanel;
	
	public Afin() {
		super("Metaphor");
		this.mainPanel = new AFINMetaphorMainPanel();
		this.add(this.mainPanel);
		this.setVisible(false);
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
	public String[] getSupportedFileExtensions() {
		String[] ret = {"net", "xml"};
		return ret;
	}

	/* (non-Javadoc)
	 * @see unbbayes.gui.IPersistenceAwareWindow#getSupportedFilesDescription()
	 */
	public String getSupportedFilesDescription() {
		return "Net (.net), XMLBIF (.xml)";
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
	 * @see unbbayes.gui.IPersistenceAwareWindow#setPersistingGraph(unbbayes.prs.Graph)
	 */
	public void setPersistingGraph(Graph graph) {
		this.getMainPanel().setNet((ProbabilisticNetwork)graph);
	}

}
