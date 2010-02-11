/**
 * 
 */
package unbbayes.learning.gui.extension;

import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import unbbayes.controller.FileHistoryController;
import unbbayes.gui.SimpleFileFilter;
import unbbayes.gui.UnBBayesFrame;
import unbbayes.io.BaseIO;
import unbbayes.io.OwnerAwareFileExtensionIODelegator;
import unbbayes.learning.ConstructionController;
import unbbayes.prs.Graph;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.util.extension.UnBBayesModule;
import unbbayes.util.extension.UnBBayesModuleBuilder;

/**
 * This class converts the Learning tool to a UnBBayes module
 * 
 * @author Shou Matsumoto
 * 
 */
public class LearningModule extends UnBBayesModule implements
		UnBBayesModuleBuilder {

	private BaseIO io;

	private ProbabilisticNetwork pn;

	private File netFileFromUnBBayesFrame = null;
	
	private ResourceBundle resource = null;

	/**
	 * Default constructor
	 */
	public LearningModule() {
		super();
		
		this.resource = unbbayes.util.ResourceController.newInstance().getBundle(
				unbbayes.gui.resources.GuiResources.class.getName());
		
		this.setName(this.resource.getString("learningItem"));
		
		// setting up the i/o classes used by UnBBayesFrame in order to load a
		// file from the main pane
		this.io = new OwnerAwareFileExtensionIODelegator(this);

		this.setVisible(false); // this module should not be visible
	}

	/**
	 * It just triggers a series of file choosers in order to collect the
	 * necessary files to start learning.
	 * Please, note that at this point {@link #getUnbbayesFrame()} must
	 * return a non-null value;
	 * 
	 * @param netFile :
	 *            the network file (containing BN) to use as a starting point.
	 *            If set to null, a filechooser will be used to collect it from
	 *            user.
	 * @return : the last file used by the system
	 */
	protected File triggerFileChooser(File netFile) {
		
		// initial test
		if (netFile == null) {
			// retrieve/store file history
			FileHistoryController fileHistoryController = FileHistoryController.getInstance();

			// choose the data file
			String[] nets = new String[] { "txt" };
			JFileChooser chooser = new JFileChooser(fileHistoryController.getCurrentDirectory());
			chooser.setMultiSelectionEnabled(false);
			chooser.addChoosableFileFilter(new SimpleFileFilter(nets,
					resource.getString("textFileFilter")));
			
			int option = chooser.showOpenDialog(getUnbbayesFrame());
			if (option == JFileChooser.APPROVE_OPTION) {
				// user has chosen a data set
				netFile = chooser.getSelectedFile();
				// store file history
				fileHistoryController.setCurrentDirectory(chooser.getCurrentDirectory());
			} else {
				// user has cancelled
				return null;	
			}
		}

		// trigger learning
		new ConstructionController(netFile, getUnbbayesFrame().getController());

		return netFile;
	}

	/**
	 * @param title
	 */
	public LearningModule(String title) {
		this();
		this.setTitle(title);
		this.setName(title);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see unbbayes.util.extension.UnBBayesModule#getModuleName()
	 */
	public String getModuleName() {
		return this.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see unbbayes.util.extension.UnBBayesModule#openFile(java.io.File)
	 */
	public UnBBayesModule openFile(File file) throws IOException {
		this.setNetFileFromUnBBayesFrame(file);
		// after some time, #setUnBBayesFrame will be called and file choosers
		// will be triggered.
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see unbbayes.util.extension.UnBBayesModuleBuilder#buildUnBBayesModule()
	 */
	public UnBBayesModule buildUnBBayesModule() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see unbbayes.gui.IPersistenceAwareWindow#getIO()
	 */
	public BaseIO getIO() {
		return this.io;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see unbbayes.gui.IPersistenceAwareWindow#getPersistingGraph()
	 */
	public Graph getPersistingGraph() {
		return this.pn;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see unbbayes.util.extension.UnBBayesModule#setUnbbayesFrame(unbbayes.gui.UnBBayesFrame)
	 */
	public void setUnbbayesFrame(UnBBayesFrame unbbayesFrame) {
		super.setUnbbayesFrame(unbbayesFrame);
		try {
			// this is a workaround to trigger file choosers only when we know
			// UnBBayesFrame is available.
			if (this.getUnbbayesFrame() != null) {
				// if openFile was called once, getNetFileFromUnBBayesFrame will
				// return non-null value.
				triggerFileChooser(this.getNetFileFromUnBBayesFrame());
			}
		} catch (Throwable t) {
			t.printStackTrace();
			JOptionPane.showMessageDialog(getUnbbayesFrame(), 
					t.getMessage(), 
					resource.getString("error"), 
					JOptionPane.ERROR_MESSAGE); 
		}
		
		// free
		unbbayesFrame.getDesktop().remove(this);
		this.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#setVisible(boolean)
	 */
	public void setVisible(boolean flag) {
		// this is allways invisible
		super.setVisible(false);
	}

	/**
	 * @return the netFileFromUnBBayesFrame
	 */
	public File getNetFileFromUnBBayesFrame() {
		return netFileFromUnBBayesFrame;
	}

	/**
	 * @param netFileFromUnBBayesFrame
	 *            the netFileFromUnBBayesFrame to set
	 */
	public void setNetFileFromUnBBayesFrame(File netFileFromUnBBayesFrame) {
		this.netFileFromUnBBayesFrame = netFileFromUnBBayesFrame;
	}

	/**
	 * @return the resource
	 */
	public ResourceBundle getResource() {
		return resource;
	}

	/**
	 * @param resource the resource to set
	 */
	public void setResource(ResourceBundle resource) {
		this.resource = resource;
	}
	
}
