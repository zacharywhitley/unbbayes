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
import unbbayes.learning.io.LearningDataSetIO;
import unbbayes.prs.Graph;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.util.extension.UnBBayesModule;
import unbbayes.util.extension.UnBBayesModuleBuilder;

/**
 * This class converts the BAN Learning tool to a UnBBayes module
 * 
 * @author Shou Matsumoto
 * 
 */
public class BANModule extends UnBBayesModule implements UnBBayesModuleBuilder {

	private BaseIO io;

	private ProbabilisticNetwork pn;

	private File netFileFromUnBBayesFrame = null;
	
	private ResourceBundle resource = null;

	/**
	 * Default constructor
	 */
	public BANModule() {
		super();
		
		this.resource = unbbayes.util.ResourceController.newInstance().getBundle(
				unbbayes.gui.resources.GuiResources.class.getName());
		
		this.setName("BAN");
		
		// setting up the i/o classes used by UnBBayesFrame
		// This is done in order to notify UnBBayes' core that TXT files must be delegated to this modul,
		this.io = new OwnerAwareFileExtensionIODelegator(this);
		
		// filling the content of OwnerAwareFileExtensionIODelegator with LearningDataSetIO (a txt-aware IO).
		((OwnerAwareFileExtensionIODelegator)this.io).getDelegators().clear();
		((OwnerAwareFileExtensionIODelegator)this.io).getDelegators().add(new LearningDataSetIO());

		this.setVisible(false); // this module should not be visible
	}
	
	/**
	 * @param title
	 */
	public BANModule(String title) {
		this();
		this.setTitle(title);
		this.setName(title);
	}

	/**
	 * It just triggers a series of file choosers in order to collect the
	 * necessary files to start learning.
	 * Please, note that at this point {@link #getUnbbayesFrame()} must
	 * return a non-null value;
	 * 
	 * @param dataSetFile :
	 *            the set of data to use as a starting point.
	 *            If set to null, a filechooser will be used to collect it from
	 *            user.
	 * @return : the last file used by the system
	 */
	protected File triggerFileChooser(File dataSetFile) {
		
		// initial test
		if (dataSetFile == null) {
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
				dataSetFile = chooser.getSelectedFile();
				// store file history
				fileHistoryController.setCurrentDirectory(chooser.getCurrentDirectory());
			} else {
				// user has cancelled
				return null;	
			}
		}

		// trigger learning
		try {
			// I'm not sure why the original code was using a magic number of 0...
			new ConstructionController(dataSetFile, getUnbbayesFrame().getController(), 0, true);
		} catch (InvalidParentException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(getUnbbayesFrame(), 
					e.getMessage(), 
					resource.getString("error"), 
					JOptionPane.ERROR_MESSAGE); 
		}

		return dataSetFile;
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
