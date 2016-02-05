/**
 * 
 */
package unbbayes.gui.umpst.implementation;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import unbbayes.controller.umpst.IconController;
import unbbayes.gui.umpst.IUMPSTPanel;
import unbbayes.gui.umpst.UmpstModule;
import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.model.umpst.rule.RuleModel;

/**
 * Set Define rule panel and its properties.
 * @author Diego Marques
 */
public class ImplementationEditPanel extends IUMPSTPanel {
		
	private OrdinaryVariableEditPanel ordVariablePane;
	private NecessaryConditionEditPanel necConditionPane;
	private CauseEditPanel causeEditPane;
	private EffectEditPanel effectEditPane;
	
	private ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
			unbbayes.gui.umpst.resources.Resources.class.getName());	

	/**
	 * @param ruleAux 
	 * @param umpstProject 
	 * @param umpstModule 
	 */
	public ImplementationEditPanel(UmpstModule janelaPai, UMPSTProject umpstProject,
			RuleModel rule) {
		
		super(janelaPai);
		this.setUmpstProject(umpstProject);
		this.setLayout(new FlowLayout());
		
		IconController iconController = IconController.getInstance();
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, 
				createDescriptionText(rule), createRuleTabsPanel(iconController, rule));
		splitPane.setPreferredSize(new Dimension(800, 600));
		
		this.add(splitPane);
	}

	public JPanel createDescriptionText(RuleModel rule) {		
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		
		GridBagConstraints p = new GridBagConstraints();
		p.anchor = GridBagConstraints.FIRST_LINE_START;
		
		p.gridx = 0;
		p.gridy = 1;
		p.gridwidth = 1;
		panel.add(new JLabel("Description:"), p);
		
		JTextArea descriptionText = new JTextArea();
		descriptionText.setEditable(false);
		descriptionText.setPreferredSize(new Dimension(500, 80));
		
		descriptionText.setText(rule.getName());
		descriptionText.setLineWrap(true);
		descriptionText.setBorder(BorderFactory.createEtchedBorder());		
		
		JScrollPane scrollPane = new JScrollPane(descriptionText);		
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(new Dimension(50, 50));
		
		p.gridx = 0;
		p.gridy = 2;
		p.gridwidth = 1;
		p.ipadx = 580;
//		p.ipady = 50;		
		p.fill = GridBagConstraints.HORIZONTAL;
		panel.add(scrollPane, p);

		return panel;		
	}
	
	public JPanel createRuleTabsPanel(IconController iconController, RuleModel rule) {
		
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		
		// Set position
		GridBagConstraints p = new GridBagConstraints();
		p.anchor = GridBagConstraints.FIRST_LINE_START;
		
		p.gridx = 0;
		p.gridy = 4;
		p.gridwidth = 1;
		panel.add(new JLabel("Define Rule:"), p);
		
		p.gridx = 0;
		p.gridy = 5;
		p.gridwidth = 2;
		p.ipadx = 200;
		p.ipady = 100;
		
		final JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		
		// Ordinary Variable Tab		
		ordVariablePane = new OrdinaryVariableEditPanel(getFatherPanel(),
				getUmpstProject(), rule);
		ordVariablePane.setPreferredSize(new Dimension(500, 300));
//		tabbedPane.addTab(resource.getString("ttOrdinaryVariable"),
		tabbedPane.addTab("Ordinary Variable",
				iconController.getImplementingIcon(),
				ordVariablePane,
				resource.getString("hpOrdinaryVariableTab"));
		tabbedPane.setMnemonicAt(0, KeyEvent.VK_0);
		
		// Necessary Conditions Tab
		necConditionPane = new NecessaryConditionEditPanel(getFatherPanel(),
				getUmpstProject(), rule);
		necConditionPane.setPreferredSize(new Dimension(500, 300));
//		tabbedPane.addTab(resource.getString("ttOrdinaryVariable"),
		tabbedPane.addTab("Necessary Conditions",
				iconController.getImplementingIcon(),
				necConditionPane,
				resource.getString("hpNecessaryConditionTab"));
		tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);		
		
		// Cause Tab
		causeEditPane = new CauseEditPanel(getFatherPanel(),
				getUmpstProject(), rule);
		causeEditPane.setPreferredSize(new Dimension(500, 300));
//				tabbedPane.addTab(resource.getString("ttOrdinaryVariable"),
		tabbedPane.addTab("Causes",
				iconController.getImplementingIcon(),
				causeEditPane,
				resource.getString("hpCauseTab"));
		tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);		
		
		// Effect Tab
		effectEditPane = new EffectEditPanel(getFatherPanel(),
				getUmpstProject(), rule);
		effectEditPane.setPreferredSize(new Dimension(500, 300));
//						tabbedPane.addTab(resource.getString("ttOrdinaryVariable"),
		tabbedPane.addTab("Effects",
				iconController.getImplementingIcon(),
				effectEditPane,
				resource.getString("hpEffectTab"));
		tabbedPane.setMnemonicAt(0, KeyEvent.VK_2);		
		panel.add(tabbedPane, p);

		return panel;
	}

	/**
	 * @return the ordVariablePane
	 */
	public OrdinaryVariableEditPanel getOrdVariablePane() {
		return ordVariablePane;
	}

	/**
	 * @param ordVariablePane the ordVariablePane to set
	 */
	public void setOrdVariablePane(OrdinaryVariableEditPanel ordVariablePane) {
		this.ordVariablePane = ordVariablePane;
	}	

	/**
	 * @return the causePane
	 */
	public CauseEditPanel getCauseEditPane() {
		return causeEditPane;
	}

	/**
	 * @param causePane the causePane to set
	 */
	public void setCauseEditPane(CauseEditPanel causePane) {
		this.causeEditPane = causePane;
	}

	/**
	 * @return the effectEditPane
	 */
	public EffectEditPanel getEffectEditPane() {
		return effectEditPane;
	}

	/**
	 * @param effectEditPane the effectEditPane to set
	 */
	public void setEffectEditPane(EffectEditPanel effectEditPane) {
		this.effectEditPane = effectEditPane;
	}

	/**
	 * @return the necConditionPane
	 */
	public NecessaryConditionEditPanel getNecConditionPane() {
		return necConditionPane;
	}

	/**
	 * @param necConditionPane the necConditionPane to set
	 */
	public void setNecConditionPane(NecessaryConditionEditPanel necConditionPane) {
		this.necConditionPane = necConditionPane;
	}
	
}
