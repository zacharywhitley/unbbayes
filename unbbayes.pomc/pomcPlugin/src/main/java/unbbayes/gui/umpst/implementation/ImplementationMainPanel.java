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
import java.util.Set;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import unbbayes.gui.umpst.IUMPSTPanel;
import unbbayes.gui.umpst.TableObject;
import unbbayes.gui.umpst.UmpstModule;
import unbbayes.model.umpst.project.UMPSTProject;

/**
 * Define Implementation tab properties. 
 * @author Diego Marques
 */
public class ImplementationMainPanel extends IUMPSTPanel{
	
	private JSplitPane splitPane;
	private TableImplementation implementationPanel;
	private ImplementationSearchPanel implementationSearch;
	private JTextArea descriptionArea;
	private JPanel descriptionPanel;
	
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
		
		splitPane =  new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				descriptionPanel, implementationPanel);
		splitPane.setDividerLocation(200);
		splitPane.setPreferredSize(new Dimension(800, 600));
		splitPane.setBackground(new Color(0x4169AA));
		
		this.add(splitPane);	
		
	}

//	/**
//	 * Set the panel size and its properties
//	 * @return splitPane
//	 */
//	public JSplitPane getSplitPane() {
//		if (splitPane == null) {			
//			splitPane =  new JSplitPane(JSplitPane.VERTICAL_SPLIT,
//					createRuleDescriptionPanel(), getImplementationTable());
//			splitPane.setDividerLocation(200);
//			splitPane.setPreferredSize(new Dimension(800, 600));
//			splitPane.setBackground(new Color(0x4169AA));
//		}
//		return splitPane;
//	}
	
//	/**
//	 * @return the implementationSearch
//	 */
//	public ImplementationSearchPanel getImplementationSearchPanel() {
//		if (implementationSearch == null) {
//			implementationSearch = new ImplementationSearchPanel(getFatherPanel(), 
//					getUmpstProject());
////			implementationSearch.setBackground(new Color(0xffffff));
//		}
//		return implementationSearch;
//	}
//	
//	public JSplitPane createSelectRulePanel() {
//		JSplitPane rulePanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
//		rulePanel.add(getImplementationTable());
//		rulePanel.add(createRuleDescriptionPanel());
//		return rulePanel;
//	}
	
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
		splitPane.remove(implementationPanel);
		implementationPanel = new TableImplementation(getFatherPanel(), getUmpstProject());
		splitPane.add(implementationPanel);
		splitPane.revalidate();
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
