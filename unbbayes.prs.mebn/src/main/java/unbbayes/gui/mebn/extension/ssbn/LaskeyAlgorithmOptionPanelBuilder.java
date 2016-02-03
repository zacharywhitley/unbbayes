/**
 * 
 */
package unbbayes.gui.mebn.extension.ssbn;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import unbbayes.gui.mebn.OptionsDialog;
import unbbayes.prs.mebn.ssbn.ISSBNGenerator;
import unbbayes.prs.mebn.ssbn.bayesballalgorithm.BayesBallSSBNGenerator;
import unbbayes.prs.mebn.ssbn.laskeyalgorithm.LaskeyAlgorithmParameters;
import unbbayes.prs.mebn.ssbn.laskeyalgorithm.LaskeySSBNGenerator;
import unbbayes.prs.mebn.ssbn.pruner.IPruner;
import unbbayes.prs.mebn.ssbn.pruner.impl.BarrenNodePruner;
import unbbayes.prs.mebn.ssbn.pruner.impl.DSeparationPruner;
import unbbayes.prs.mebn.ssbn.pruner.impl.PruneStructureImpl;
import unbbayes.util.Debug;

/**
 * This class is a panel used by {@link OptionsDialog} in order
 * to fill informations for laskey's SSBN generation algorithm.
 * This class is a part of the plugin infrastructure for MEBN module.
 * @author Shou Matsumoto
 *
 */
public class LaskeyAlgorithmOptionPanelBuilder extends JScrollPane implements ISSBNOptionPanelBuilder {

    /** Resource file from this package. Resource is not static, to enable hotplug */
  	private ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
  			unbbayes.gui.mebn.resources.Resources.class.getName());
	
	private ISSBNGenerator ssbnGenerator;
	private LaskeyAlgorithmParameters parameters;
	private JPanel mainPanel;

	private JCheckBox initializationCheckBox;

	private JCheckBox buildCheckBox;

	private JCheckBox pruneCheckBox;

	private JCheckBox cptGenerationCheckBox;

	private JPanel pruneConfigurationPanel;

	private JCheckBox barrenNodePrunerCheckBox;

	private JCheckBox dseparatedNodePrunerCheckBox;

	
	// the pruners were modeled as attributes in order to reuse them when necessary
	private PruneStructureImpl pruneStructure;
	
	// these are constant values for compatible pruners. I'm avoiding static ones because I'm not sure if it supports asynchronous access
	private final IPruner BARREN_NODE_PRUNER = BarrenNodePruner.newInstance();
	private final IPruner DSEPARATION_PRUNER = DSeparationPruner.newInstance();

	private JPanel checkBoxPanel;

	private JCheckBox userInteractionCheckBox;

	private JPanel recursiveLimitPanel;

	private JTextField recursivityLimitTextField;

	/**
	 * Default constructor is mandatory for plugin compatibility
	 */
	public LaskeyAlgorithmOptionPanelBuilder() {
		super();
		
		// start invisible
		this.setVisible(false);
		
		// initialize algorithm
		this.buildAlgorithm();
		
		// initialize panel
		this.buildPanel();
		
	}
	
	/**
	 * This method initializes the ssbn generation algorithm using default parameters.
	 * The default constructor calls this method.
	 */
	protected void buildAlgorithm() {
		// initialize laskey algorithm using default parameter values
		LaskeyAlgorithmParameters param = new LaskeyAlgorithmParameters();
		param.setParameterValue(LaskeyAlgorithmParameters.DO_INITIALIZATION, "true");
		param.setParameterValue(LaskeyAlgorithmParameters.DO_BUILDER, "true"); 
		param.setParameterValue(LaskeyAlgorithmParameters.DO_PRUNE, "true"); 
		param.setParameterValue(LaskeyAlgorithmParameters.DO_CPT_GENERATION, "true"); 
		this.setParameters(param); 
		
//		setSSBNGenerator(new LaskeySSBNGenerator(param));
		setSSBNGenerator(new BayesBallSSBNGenerator()); 
		
		// assure the initialization of prune structure, using default pruners
		List<IPruner> pruners = new ArrayList<IPruner>();
		pruners.add(BARREN_NODE_PRUNER);	// barren node pruning is enabled by default
		pruners.add(DSEPARATION_PRUNER);	// d-separated node pruning is enabled by default
		this.setPruneStructure((PruneStructureImpl)PruneStructureImpl.newInstance(pruners));
//		((LaskeySSBNGenerator)getSSBNGenerator()).setPruneStructure(this.getPruneStructure());
	}
	
	

	/**
	 * This method fills up the content of this pane.
	 * The default constructor calls this method.
	 */
	protected void buildPanel() {
		// initialize pane
		mainPanel = new JPanel(new BorderLayout(20,10));
		mainPanel.setBorder(new TitledBorder(this.getResource().getString("mainPanelBorderTitle")));

		checkBoxPanel = new JPanel(new GridLayout(0,1,10,10));
		
		// initialize check boxes
		initializationCheckBox = new JCheckBox(this.resource.getString("initializationCheckBoxLabel"),
				Boolean.parseBoolean(parameters.getParameterValue(LaskeyAlgorithmParameters.DO_INITIALIZATION)));
		buildCheckBox = new JCheckBox(this.resource.getString("buildCheckBoxLabel"),
				Boolean.parseBoolean(parameters.getParameterValue(LaskeyAlgorithmParameters.DO_BUILDER)));
		pruneCheckBox = new JCheckBox(this.resource.getString("pruneCheckBoxLabel"),
				Boolean.parseBoolean(parameters.getParameterValue(LaskeyAlgorithmParameters.DO_PRUNE)));
		cptGenerationCheckBox = new JCheckBox(this.resource.getString("cptGenerationCheckBoxLabel"),
				Boolean.parseBoolean(parameters.getParameterValue(LaskeyAlgorithmParameters.DO_CPT_GENERATION)));
		userInteractionCheckBox  = new JCheckBox(this.resource.getString("userInteractionCheckBoxLabel"),
				Boolean.parseBoolean(parameters.getParameterValue(LaskeyAlgorithmParameters.USE_USER_INTERATION)));
		
		// pruning configurations
		pruneConfigurationPanel = new JPanel(new GridLayout(0,1,10,10));
		pruneConfigurationPanel.setBorder(new TitledBorder(this.resource.getString("pruneConfigurationBorderTitle")));
		
		// fill checkboxes for pruning configurations
		barrenNodePrunerCheckBox = new JCheckBox(this.resource.getString("barrenNodePrunerCheckBoxLabel"),true);
		dseparatedNodePrunerCheckBox = new JCheckBox(this.resource.getString("dseparatedNodePrunerCheckBoxLabel"),true);
		pruneConfigurationPanel.add(barrenNodePrunerCheckBox);
		pruneConfigurationPanel.add(dseparatedNodePrunerCheckBox);
		
		// hide pruning configuration if it is not selected
		if (!pruneCheckBox.isSelected()) {
			pruneConfigurationPanel.setEnabled(false);
		}
		
		// configure recursive limit
		recursiveLimitPanel = new JPanel(new BorderLayout());
		recursiveLimitPanel.setBorder(new TitledBorder(this.getResource().getString("recursiveLimitBorderTitle")));
		
		recursivityLimitTextField = new JTextField(getParameters().getParameterValue(LaskeyAlgorithmParameters.NUMBER_NODES_LIMIT),7);
		recursiveLimitPanel.add(recursivityLimitTextField);
		
		// add listeners
		pruneCheckBox.addChangeListener(new ChangeListener() {
			// enables/disables pruneConfigurationPanel depending on the state of pruneCheckBox
			public void stateChanged(ChangeEvent e) {
				if (pruneCheckBox.isSelected()) {
					pruneConfigurationPanel.setEnabled(true);
					barrenNodePrunerCheckBox.setEnabled(true);
					dseparatedNodePrunerCheckBox.setEnabled(true);
					Debug.println(this.getClass(), "Prune configuration enabled");
				} else {
					pruneConfigurationPanel.setEnabled(false);
					barrenNodePrunerCheckBox.setEnabled(false);
					dseparatedNodePrunerCheckBox.setEnabled(false);

					Debug.println(this.getClass(), "Prune configuration disabled");
				}
				mainPanel.updateUI();
				mainPanel.repaint();
			}
		});
		
		// add components to main panel
//		checkBoxPanel.add(initializationCheckBox);
//		checkBoxPanel.add(buildCheckBox);
//		checkBoxPanel.add(cptGenerationCheckBox);
//		checkBoxPanel.add(userInteractionCheckBox);
		checkBoxPanel.add(pruneCheckBox);
		
		mainPanel.add(checkBoxPanel, BorderLayout.CENTER);
		mainPanel.add(pruneConfigurationPanel, BorderLayout.SOUTH);
		mainPanel.add(recursiveLimitPanel, BorderLayout.NORTH);
		
		this.setViewportView(mainPanel);
	}

	/* (non-Javadoc)
	 * @see unbbayes.gui.mebn.extension.ssbn.ISSBNOptionPanelBuilder#commitChanges()
	 */
	public void commitChanges() {
		
		// commit parameters
		
		this.getParameters().setParameterValue(LaskeyAlgorithmParameters.DO_INITIALIZATION, 
				String.valueOf(this.getInitializationCheckBox().isSelected()));
		this.getParameters().setParameterValue(LaskeyAlgorithmParameters.DO_BUILDER, 
				String.valueOf(this.getBuildCheckBox().isSelected()));
		this.getParameters().setParameterValue(LaskeyAlgorithmParameters.DO_PRUNE, 
				String.valueOf(this.getPruneCheckBox().isSelected()));
		this.getParameters().setParameterValue(LaskeyAlgorithmParameters.DO_CPT_GENERATION, 
				String.valueOf(this.getCptGenerationCheckBox().isSelected()));
		this.getParameters().setParameterValue(LaskeyAlgorithmParameters.USE_USER_INTERATION, 
				String.valueOf(this.getUserInteractionCheckBox().isSelected()));
		
		// recursive limit panel
		this.getParameters().setParameterValue(LaskeyAlgorithmParameters.USE_USER_INTERATION, 
				getRecursivityLimitTextField().getText());
		
		// commit pruners
		
		// reset pruner
		this.getPruneStructure().getListOfPruners().clear();
		
		// refill pruners
		if (this.getBarrenNodePrunerCheckBox().isSelected()) {
			this.getPruneStructure().getListOfPruners().add(BARREN_NODE_PRUNER);
		}
		if (this.getDseparatedNodePrunerCheckBox().isSelected()) {
			this.getPruneStructure().getListOfPruners().add(DSEPARATION_PRUNER);
		}

		// update view
		this.updateUI();
		this.repaint();
	}

	/* (non-Javadoc)
	 * @see unbbayes.gui.mebn.extension.ssbn.ISSBNOptionPanelBuilder#discardChanges()
	 */
	public void discardChanges() {
		
		// revert changes on general checkboxes
		this.getInitializationCheckBox().setSelected(Boolean.parseBoolean(
				this.getParameters().getParameterValue(LaskeyAlgorithmParameters.DO_INITIALIZATION)));
		this.getBuildCheckBox().setSelected(Boolean.parseBoolean(
				this.getParameters().getParameterValue(LaskeyAlgorithmParameters.DO_BUILDER)));
		this.getPruneCheckBox().setSelected(Boolean.parseBoolean(
				this.getParameters().getParameterValue(LaskeyAlgorithmParameters.DO_PRUNE)));
		this.getCptGenerationCheckBox().setSelected(Boolean.parseBoolean(
				this.getParameters().getParameterValue(LaskeyAlgorithmParameters.DO_CPT_GENERATION)));
		this.getUserInteractionCheckBox().setSelected(Boolean.parseBoolean(
				this.getParameters().getParameterValue(LaskeyAlgorithmParameters.USE_USER_INTERATION)));
		
		// recursive limit panel
		this.getRecursivityLimitTextField().setText(
				this.getParameters().getParameterValue(LaskeyAlgorithmParameters.NUMBER_NODES_LIMIT));
		
		// revert changes on pruning checkboxes
		this.getBarrenNodePrunerCheckBox().setSelected(
				this.getPruneStructure().getListOfPruners().contains(BARREN_NODE_PRUNER));
		this.getDseparatedNodePrunerCheckBox().setSelected(
				this.getPruneStructure().getListOfPruners().contains(BARREN_NODE_PRUNER));
		
		// update view
		this.updateUI();
		this.repaint();
	}

	/* (non-Javadoc)
	 * @see unbbayes.gui.mebn.extension.ssbn.ISSBNOptionPanelBuilder#getSSBNGenerator()
	 */
	public ISSBNGenerator getSSBNGenerator() {
		return this.ssbnGenerator;
	}

	/* (non-Javadoc)
	 * @see unbbayes.gui.mebn.extension.ssbn.ISSBNOptionPanelBuilder#setSSBNGenerator(unbbayes.prs.mebn.ssbn.ISSBNGenerator)
	 */
	public void setSSBNGenerator(ISSBNGenerator ssbnGenerator) {
		this.ssbnGenerator = ssbnGenerator;
	}

	/* (non-Javadoc)
	 * @see unbbayes.gui.mebn.extension.IPanelBuilder#getPanel()
	 */
	public JComponent getPanel() {
		this.setVisible(true);
		return this;
	}


	/**
	 * @return the parameters
	 */
	public LaskeyAlgorithmParameters getParameters() {
		return parameters;
	}

	/**
	 * @param parameters the parameters to set
	 */
	public void setParameters(LaskeyAlgorithmParameters parameters) {
		this.parameters = parameters;
	}

	/**
	 * @return the mainPanel
	 */
	public JPanel getMainPanel() {
		return mainPanel;
	}

	/**
	 * @param mainPanel the mainPanel to set
	 */
	public void setMainPanel(JPanel mainPanel) {
		this.mainPanel = mainPanel;
	}


	/**
	 * @return the initializationCheckBox
	 */
	public JCheckBox getInitializationCheckBox() {
		return initializationCheckBox;
	}

	/**
	 * @param initializationCheckBox the initializationCheckBox to set
	 */
	public void setInitializationCheckBox(JCheckBox initializationCheckBox) {
		this.initializationCheckBox = initializationCheckBox;
	}

	/**
	 * @return the buildCheckBox
	 */
	public JCheckBox getBuildCheckBox() {
		return buildCheckBox;
	}

	/**
	 * @param buildCheckBox the buildCheckBox to set
	 */
	public void setBuildCheckBox(JCheckBox buildCheckBox) {
		this.buildCheckBox = buildCheckBox;
	}

	/**
	 * @return the pruneCheckBox
	 */
	public JCheckBox getPruneCheckBox() {
		return pruneCheckBox;
	}

	/**
	 * @param pruneCheckBox the pruneCheckBox to set
	 */
	public void setPruneCheckBox(JCheckBox pruneCheckBox) {
		this.pruneCheckBox = pruneCheckBox;
	}

	/**
	 * @return the cptGenerationCheckBox
	 */
	public JCheckBox getCptGenerationCheckBox() {
		return cptGenerationCheckBox;
	}

	/**
	 * @param cptGenerationCheckBox the cptGenerationCheckBox to set
	 */
	public void setCptGenerationCheckBox(JCheckBox cptGenerationCheckBox) {
		this.cptGenerationCheckBox = cptGenerationCheckBox;
	}

	/**
	 * @return the pruneConfigurationPanel
	 */
	public JPanel getPruneConfigurationPanel() {
		return pruneConfigurationPanel;
	}

	/**
	 * @param pruneConfigurationPanel the pruneConfigurationPanel to set
	 */
	public void setPruneConfigurationPanel(JPanel pruneConfigurationPanel) {
		this.pruneConfigurationPanel = pruneConfigurationPanel;
	}

	public JCheckBox getBarrenNodePrunerCheckBox() {
		return barrenNodePrunerCheckBox;
	}

	public void setBarrenNodePrunerCheckBox(JCheckBox barrenNodePrunerCheckBox) {
		this.barrenNodePrunerCheckBox = barrenNodePrunerCheckBox;
	}

	public JCheckBox getDseparatedNodePrunerCheckBox() {
		return dseparatedNodePrunerCheckBox;
	}

	public void setDseparatedNodePrunerCheckBox(
			JCheckBox dseparatedNodePrunerCheckBox) {
		this.dseparatedNodePrunerCheckBox = dseparatedNodePrunerCheckBox;
	}

	public ResourceBundle getResource() {
		return resource;
	}

	public void setResource(ResourceBundle resource) {
		this.resource = resource;
	}

	public PruneStructureImpl getPruneStructure() {
		return pruneStructure;
	}

	public void setPruneStructure(PruneStructureImpl pruneStructure) {
		this.pruneStructure = pruneStructure;
	}

	public JPanel getCheckBoxPanel() {
		return checkBoxPanel;
	}

	public void setCheckBoxPanel(JPanel checkBoxPanel) {
		this.checkBoxPanel = checkBoxPanel;
	}

	public JCheckBox getUserInteractionCheckBox() {
		return userInteractionCheckBox;
	}

	public void setUserInteractionCheckBox(JCheckBox userInteractionCheckBox) {
		this.userInteractionCheckBox = userInteractionCheckBox;
	}

	public JPanel getRecursiveLimitPanel() {
		return recursiveLimitPanel;
	}

	public void setRecursiveLimitPanel(JPanel recursiveLimitPanel) {
		this.recursiveLimitPanel = recursiveLimitPanel;
	}

	public JTextField getRecursivityLimitTextField() {
		return recursivityLimitTextField;
	}

	public void setRecursivityLimitTextField(JTextField recursivityLimitTextField) {
		this.recursivityLimitTextField = recursivityLimitTextField;
	}

}
