/**
 * 
 */
package unbbayes.learning.incrementalLearning.gui.extension;


import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import unbbayes.aprendizagem.ConstructionController;
import unbbayes.aprendizagem.ProbabilisticController;
import unbbayes.controller.IconController;
import unbbayes.io.BaseIO;
import unbbayes.io.OwnerAwareFileExtensionIODelegator;
import unbbayes.learning.incrementalLearning.controller.ILController;
import unbbayes.learning.incrementalLearning.io.ILIO;
import unbbayes.prs.Graph;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.util.Debug;
import unbbayes.util.extension.UnBBayesModule;
import unbbayes.util.extension.UnBBayesModuleBuilder;

/**
 * This class converts the Incremental Learning tool to a UnBBayes module plugin.
 * This code is very similar to unbbayes.learning.incrementalLearning.gui.ILBridge
 * @author Shou Matsumoto
 *
 */
public class IncrementalLearningModule extends UnBBayesModule implements
		UnBBayesModuleBuilder {


	private BaseIO io;
	private ILIO incrementalLearningIO;
	
	private JScrollPane scrollPane;
  	private JLabel buttonPanelLabel;
  	private JButton button;
  	private ActionListener buttonActionListener;
  	
  	private ProbabilisticNetwork pn;
  	
  	/** Load resource file from this package */
  	private static ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
  			unbbayes.learning.incrementalLearning.resources.Resources.class.getName());
	
	/**
	 * Default constructor
	 */
	public IncrementalLearningModule() {
		super();
		this.setName("Incremental Learning");
		
		this.setLayout(new FlowLayout(FlowLayout.CENTER, 10,5));
		
		// setting up the i/o classes used by UnBBayesFrame in order to load a file from the main pane
		this.io = new OwnerAwareFileExtensionIODelegator(this);
		this.incrementalLearningIO = new ILIO();
		
		scrollPane = new JScrollPane();
		scrollPane.setBorder(new TitledBorder(this.resource.getString("title")));	
		
		JPanel contentPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10,5));

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10,5));
		buttonPanelLabel = new JLabel(this.resource.getString("selectFile"));
		buttonPanel.add(buttonPanelLabel);
		
		button = new JButton(this.resource.getString("openFile"),
				IconController.getInstance().getNetFileIcon());
		buttonPanel.add(button);
		
		contentPanel.add(buttonPanel);
		
		scrollPane.setViewportView(contentPanel);
		this.setContentPane(scrollPane);
		
		// the button must trigger a incremental learning
		
		buttonActionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					triggerFileChooser(null);
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		};
		button.addActionListener(buttonActionListener);
	}
	
	/**
	 * It just triggers a series of file choosers in order to collect the necessary
	 * files to start incremental learning
	 * @param netFile : the network file (containing BN) to use as a starting point. If set to null,
	 * a filechooser will be used to collect it from user.
	 * @return : the last file used by the system
	 */
	protected File triggerFileChooser(File netFile) {
		// initial test
		if (netFile == null) {
			/* choose the network file*/ 
			netFile = this.incrementalLearningIO.chooseFile(
						this.getIO().getSupportedFileExtensions(true), 
						this.resource.getString("chooseNetworkFile")
					);
		}
        
		/* Obtaining the network from netFile*/
        this.pn = this.incrementalLearningIO.getNet(netFile, this.getIO());        
        
        /* Choose another file, containing enough statistic informations */                 
        netFile = this.incrementalLearningIO.chooseFile(new String[] { "obj" }, this.resource.getString("chooseFrontierSet"));
        
        List ssList = new ArrayList();;
        if (netFile != null) {
            ssList = this.incrementalLearningIO.getSuficStatistics(netFile);
        }
        
        netFile = this.incrementalLearningIO.chooseFile(new String[] { "txt" }, this.resource.getString("chooseTrainingSet"));
        ConstructionController constructionController= new ConstructionController(netFile,pn);        
        
        try {
        	Thread.sleep(2000);
		} catch (Throwable e) {
			Debug.println(this.getClass(), "Pre WAIT interrupted", e);
		}        
        ILController ilc = new ILController(pn,ssList,constructionController.getVariables());
        try {
        	Thread.sleep(2000);
		} catch (Throwable e) {
			Debug.println(this.getClass(), "Post WAIT interrupted", e);
		}
        
        /*Gives the probability of each node*/        
        new ProbabilisticController(ilc.getListaVariaveis(),constructionController.getMatrix(),
        							constructionController.getVector(),constructionController.getCaseNumber(),
        							this.getUnbbayesFrame().getController(), constructionController.isCompacted());        
        
        //paramRecalc();
        netFile = this.incrementalLearningIO.getFile();
        this.incrementalLearningIO.makeNetFile(netFile, io, pn);
        netFile = this.incrementalLearningIO.getFile();
        this.incrementalLearningIO.makeContFile(ssList, netFile);
        
        return netFile;
	}


	/**
	 * @param title
	 */
	public IncrementalLearningModule(String title) {
		this();
		this.setTitle(title);
		this.setName(title);
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.UnBBayesModule#getModuleName()
	 */
	public String getModuleName() {
		return this.getName();
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.UnBBayesModule#openFile(java.io.File)
	 */
	public UnBBayesModule openFile(File file) throws IOException {
		this.triggerFileChooser(file);
		return null;	// show nothing as a internal frame
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.UnBBayesModuleBuilder#buildUnBBayesModule()
	 */
	public UnBBayesModule buildUnBBayesModule() {
		return this;
	}

	/* (non-Javadoc)
	 * @see unbbayes.gui.IPersistenceAwareWindow#getIO()
	 */
	public BaseIO getIO() {
		return this.io;
	}

	/* (non-Javadoc)
	 * @see unbbayes.gui.IPersistenceAwareWindow#getPersistingGraph()
	 */
	public Graph getPersistingGraph() {
		return this.pn;
	}
	

	
}
