/**
 * 
 */
package unbbayes.gui.umpst.implementation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import unbbayes.controller.umpst.GenerateMTheoryController;
import unbbayes.gui.umpst.IUMPSTPanel;
import unbbayes.gui.umpst.UmpstModule;
import unbbayes.model.umpst.project.UMPSTProject;

/**
 * Define Implementation tab properties. 
 * @author Diego Marques
 */
public class ImplementationMainPanel extends IUMPSTPanel{
	
	private JSplitPane splitPane;
	private JSplitPane btnPane;
	private TableImplementation implementationPanel;
	private ImplementationSearchPanel implementationSearch;
	private JTextArea descriptionArea;
	private JPanel descriptionPanel;
	private JPanel generatePanel;
	
	private GenerateMTheoryController generateMtheoryController;
	
	private String ruleDescriptionText = " ";
	
	/**
	 * Constructor of main implementation panel.
	 * @param janelaPai
	 * @param umpstProject
	 */
	public ImplementationMainPanel(UmpstModule janelaPai, UMPSTProject umpstProject) {
		super(janelaPai);
		this.setUmpstProject(umpstProject);
		this.setLayout(new FlowLayout());
		
		descriptionPanel = createRuleDescriptionPanel();
		implementationPanel = new TableImplementation(getFatherPanel(), getUmpstProject());
		
		createAddUpdateButton();
		btnPane =  new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				implementationPanel, generatePanel);
		btnPane.setDividerLocation(260);
		
		splitPane =  new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				descriptionPanel, btnPane);		
		
		splitPane.setDividerLocation(170);
		splitPane.setPreferredSize(new Dimension(800, 600));
		splitPane.setBackground(new Color(0x4169AA));
		
		this.add(splitPane);	
		
	}
	
	public void createAddUpdateButton() {
		generatePanel = new JPanel();
		generatePanel.setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.PAGE_START;
		
		JButton btnGenerate = new JButton("Generate Model");
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		generatePanel.add(btnGenerate, c);
		
		btnGenerate.addActionListener(new ActionListener() {			
			public void actionPerformed(ActionEvent e) {
				
				generateMtheoryController = new GenerateMTheoryController(getUmpstProject());
			}
		});
	}

	public JPanel createRuleDescriptionPanel() {
		descriptionPanel = new JPanel(new BorderLayout());		
		descriptionPanel.setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.CENTER;
		
		JLabel titleLabel = new JLabel();
		titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
		titleLabel.setHorizontalAlignment(JLabel.CENTER);
		titleLabel.setText("Rule");
		
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		descriptionPanel.add(titleLabel, c);		
		
		descriptionArea = new JTextArea(5, 50);
		JScrollPane scrollPane = new JScrollPane(descriptionArea); 
		descriptionArea.setEditable(false);
		
		descriptionArea.setText(ruleDescriptionText);
		descriptionArea.setLineWrap(true);
		descriptionArea.setBorder(BorderFactory.createEtchedBorder());		
		
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		descriptionPanel.add(descriptionArea, c);
		
		
		return descriptionPanel;
	}
	
	/** 
	 * @return the implementationPanel
	 */
	public TableImplementation getImplementationTable() {
		if (implementationPanel == null) {
			implementationPanel = new TableImplementation(getFatherPanel(), getUmpstProject());
		}		
		return implementationPanel;
	}
	
	public void updateDescriptionArea() {		
		descriptionArea.setText(ruleDescriptionText);
	}
	
	/**
	 * Update table of rules (implementationPanel)
	 */
	public void updateSplitPane() {
		btnPane.remove(implementationPanel);
		implementationPanel = new TableImplementation(getFatherPanel(), getUmpstProject());
		btnPane.add(implementationPanel);
		btnPane.revalidate();
		btnPane.setDividerLocation(260);
	}

	/**
	 * @return the ruleDescriptionText
	 */
	public String getRuleDescriptionText() {
		return ruleDescriptionText;
	}

	/**
	 * @param ruleDescriptionText the ruleDescriptionText to set
	 */
	public void setRuleDescriptionText(String ruleDescriptionText) {
		this.ruleDescriptionText = ruleDescriptionText;
	}
}
