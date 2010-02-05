/**
 * 
 */
package unbbayes.simulation.montecarlo.gui.extension;

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

import unbbayes.controller.IconController;
import unbbayes.io.BaseIO;
import unbbayes.io.FileExtensionIODelegator;
import unbbayes.io.NetIO;
import unbbayes.io.XMLBIFIO;
import unbbayes.prs.Graph;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.simulation.montecarlo.controller.MCMainController;
import unbbayes.simulation.montecarlo.sampling.MatrixMonteCarloSampling;
import unbbayes.util.Debug;
import unbbayes.util.extension.UnBBayesModule;
import unbbayes.util.extension.UnBBayesModuleBuilder;

/**
 * This class converts the Monte carlo sampling tool to a UnBBayes module plugin.
 * @author Shou Matsumoto
 *
 */
public class MonteCarloModule extends UnBBayesModule implements UnBBayesModuleBuilder {

	private static final long serialVersionUID = 8591919431839488343L;

	private String name = "Logic Sampling";
	
	private BaseIO io;
	
	private MCMainController lastBuiltMcMainController;
	
	/** Load resource file from this package */
  	private static ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
  			unbbayes.simulation.montecarlo.resources.MCResources.class.getName());
  	
  	private JScrollPane scrollPane;
  	private JLabel buttonPanelLabel;
  	private JButton button;
  	private ActionListener buttonActionListener;
	
	public MonteCarloModule() {
		super();
		
		this.setLayout(new FlowLayout(FlowLayout.CENTER, 10,5));
		
		// setting up the i/o classes used by UnBBayesFrame in order to load a file from the main pane
		this.io = new MonteCarloFileExtensionIODelegator(this);
		
		scrollPane = new JScrollPane();
		scrollPane.setBorder(new TitledBorder(this.resource.getString("mcTitle")));	
		
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
		
		// the button must trigger a monte carlo sampling
		
		buttonActionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					lastBuiltMcMainController = new MCMainController(new MatrixMonteCarloSampling());
				} catch (Exception exc) {
					Debug.println(this.getClass(), "Exception at MCMainController", exc);
				} catch (Error err) {
					err.printStackTrace();
				}
			}
		};
		button.addActionListener(buttonActionListener);
		
	}
	


	/* (non-Javadoc)
	 * @see unbbayes.util.extension.UnBBayesModuleBuilder#buildUnBBayesModule()
	 */
	public UnBBayesModule buildUnBBayesModule() {
		return this;
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
			this.lastBuiltMcMainController = new MCMainController(new MatrixMonteCarloSampling(), false);
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

	/**
	 * This is just a {@link FileExtensionIODelegator} with some customization
	 * (names, content of delegators and its descriptions)
	 * @author Shou Matsumoto
	 *
	 */
	public class MonteCarloFileExtensionIODelegator extends FileExtensionIODelegator {

		private MonteCarloModule owner;
		
		/**
		 * Default constructor
		 * @param the {@link MonteCarloModule} owning this IO class. This is used generally
		 * in order to extract module names.
		 */
		public MonteCarloFileExtensionIODelegator(MonteCarloModule module) {
			super();
			this.owner = module;
			// customizing the content of the IO
			List<BaseIO> delegators = new ArrayList<BaseIO>();
			delegators.add(new NetIO());
			delegators.add(new XMLBIFIO());
			this.setDelegators(delegators);
		}
		
		/*
		 * (non-Javadoc)
		 * @see unbbayes.io.FileExtensionIODelegator#getName()
		 */
		public String getName() {
			return this.owner.getName();
		}

		/*
		 * (non-Javadoc)
		 * @see unbbayes.io.FileExtensionIODelegator#getSupportedFilesDescription(boolean)
		 */
		public String getSupportedFilesDescription(boolean isLoadOnly) {
			String ret = this.getName() + " - " + super.getSupportedFilesDescription(isLoadOnly);
			return ret;
		}
	}


	/**
	 * 
	 * The last MCMainController used by this module
	 * @return
	 */
	protected MCMainController getLastBuiltMcMainController() {
		return lastBuiltMcMainController;
	}

	/**
	 * The last MCMainController used by this module
	 * @param lastBuiltMcMainController
	 */
	protected void setLastBuiltMcMainController(
			MCMainController lastBuiltMcMainController) {
		this.lastBuiltMcMainController = lastBuiltMcMainController;
	}

	/**
	 * The main content pane of this internal frame
	 * @return
	 */
	protected JScrollPane getScrollPane() {
		return scrollPane;
	}


	/**
	 * 
	 * @return the label asking user to press the "open file"
	 * button
	 */
	protected JLabel getButtonPanelLabel() {
		return buttonPanelLabel;
	}


	/**
	 * 
	 * @return the "open file" button
	 */
	protected JButton getButton() {
		return button;
	}


	/**
	 * Getts the current action listener for the "open file"
	 * button
	 * @return
	 */
	protected ActionListener getButtonActionListener() {
		return buttonActionListener;
	}


	/**
	 * Sets the action listener for the main button (the
	 * "open file" button)
	 * @param buttonActionListener
	 */
	protected void setButtonActionListener(ActionListener buttonActionListener) {
		this.getButton().removeActionListener(this.buttonActionListener);
		this.buttonActionListener = buttonActionListener;
		this.getButton().addActionListener(this.buttonActionListener);
	}
}
