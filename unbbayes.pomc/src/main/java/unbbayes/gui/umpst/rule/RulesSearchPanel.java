package unbbayes.gui.umpst.rule;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

import unbbayes.controller.umpst.IconController;
import unbbayes.gui.umpst.IUMPSTPanel;
import unbbayes.gui.umpst.UmpstModule;
import unbbayes.model.umpst.goal.GoalModel;
import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.model.umpst.rule.RuleModel;

public class RulesSearchPanel extends IUMPSTPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JLabel labelRule;
	private JButton buttonSearch;
	private JButton buttonAddRule,buttonCancel;
	private JTextField textRule;
	

	/** Load resource file from this package */
	private static ResourceBundle resource = 
			unbbayes.util.ResourceController.newInstance().getBundle(
					unbbayes.gui.umpst.resources.Resources.class.getName());
	
	
	public RulesSearchPanel(UmpstModule janelaPai,UMPSTProject umpstProject){
		super(janelaPai);
		
		this.setUmpstProject(umpstProject);
		
		this.setLayout(new BorderLayout());
		//GridBagConstraints constraints = new  GridBagConstraints();
		
		JPanel panel = new JPanel();
		
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBackground(new Color(0x4169AA));
		
		panel.add(getLabelRule());
		panel.add(Box.createRigidArea(new Dimension(0,5)));
		panel.add(getTextRule());
		panel.add(Box.createRigidArea(new Dimension(0,5)));
		panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

		JPanel buttonPane = new JPanel ();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		buttonPane.add(Box.createHorizontalGlue());
		buttonPane.add(getButtonSearch());
		buttonPane.add(getButtonCancel());

		buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPane.add(getButtonAddRule());

		
		this.add(panel, BorderLayout.CENTER);
		this.add(buttonPane, BorderLayout.PAGE_END);
		
		}

	/**
	 * @return the buttonAddRule
	 */
	public JButton getButtonAddRule() {
		
		if (buttonAddRule == null){
			buttonAddRule = new JButton (IconController.getInstance().getAddIconP());
			buttonAddRule.setToolTipText(resource.getString("hpAddRule"));
			buttonAddRule.setForeground(Color.blue);
			buttonAddRule.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					changePanel(getRulesAdd(null));
				}
			});
		}
		
		return buttonAddRule;
	} 
	
	
	public RulesEditionPanel getRulesAdd(RuleModel rule){
		
		RulesEditionPanel ret = new RulesEditionPanel(getFatherPanel(),getUmpstProject(),rule);
		
		return ret;
		
	}
	
	
	/**
	 * @return the labelRule
	 */
	public JLabel getLabelRule() {
		
		if(labelRule == null){
			labelRule = new JLabel("Search for a rule: ");
			labelRule.setForeground(Color.white);
		}
		
		return labelRule;
	}


	/**
	 * @return the buttonSearch
	 */
	public JButton getButtonSearch() {
		
		if(buttonSearch == null){
			buttonSearch = new JButton(IconController.getInstance().getSearchIcon());
			buttonSearch.setToolTipText(resource.getString("hpSearchRule"));
			buttonSearch.setForeground(Color.blue);
		}
	
			
		buttonSearch.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				if (!textRule.getText().equals("")){
					updateTableRules();
				}
				else{
					JOptionPane.showMessageDialog(null, "Seach is empty!");
				}

			}
		});
		
		return buttonSearch;
	}

	/**
	 * @return the buttonCancel
	 */
	public JButton getButtonCancel() {
		
		if (buttonCancel == null){
			buttonCancel = new JButton (IconController.getInstance().getEditClear());
			buttonCancel.setToolTipText(resource.getString("hpCleanSearch"));
			buttonCancel.setForeground(Color.blue);
			buttonCancel.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					textRule.setText("");
					returnTableRules();
				}
			});
		}
		
		return buttonCancel;
	} 

	/**
	 * @return the textRule
	 */
	public JTextField getTextRule() {
		
		if (textRule == null){
			textRule = new JTextField(10);
		}
		
		textRule.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				if (!textRule.getText().equals("")){
					updateTableRules();
				}
				else{
					JOptionPane.showMessageDialog(null, "Seach is empty!");
				}

			}
		});
		
		return textRule;
	}
	
	public void updateTableRules(){
    	String[] columnNames = {"ID","Rule","",""};
    	
		Pattern pattern = Pattern.compile(textRule.getText()); 
		Matcher m; 
		
		List<RuleModel> result = new ArrayList<RuleModel>(); 
		
		for(RuleModel rule: getUmpstProject().getMapRules().values()){
			m = pattern.matcher(rule.getName()); 
			if (m.find()){
				result.add(rule); 
			}
		}
		
		Object[][] data = new Object[result.size()][5];
    	
		Integer i=0;
		
    	for (RuleModel rule : result ) {
    	  
    	 	data[i][0] = rule.getId();
			data[i][1] = rule.getName();			
			data[i][2] = "";
			data[i][3] = "";
			i++;
    	}
    	
	    
   
	    UmpstModule pai = getFatherPanel();
	    changePanel(pai.getMenuPanel());
	    
	    TableRules rulesTable = pai.getMenuPanel().getRulesPane().getRulesTable();
	    JTable table = rulesTable.createTable(columnNames,data);
	    
	    rulesTable.getScrollPanePergunta().setViewportView(table);
	    rulesTable.getScrollPanePergunta().updateUI();
	    rulesTable.getScrollPanePergunta().repaint();
	    rulesTable.updateUI();
	    rulesTable.repaint();
    }
	
	   public void returnTableRules(){
	    	String[] columnNames = {"ID","Rule","",""};
	    	
			Object[][] data = new Object[getUmpstProject().getMapRules().size()][4];
			Integer i=0;
		    
			Set<String> keys = getUmpstProject().getMapRules().keySet();
			TreeSet<String> sortedKeys = new TreeSet<String>(keys);
			
			for (String key: sortedKeys){
				data[i][0] = getUmpstProject().getMapRules().get(key).getId();
				data[i][1] = getUmpstProject().getMapRules().get(key).getName();			
				data[i][2] = "";
				data[i][3] = "";
				i++;
			}
	   
		    UmpstModule pai = getFatherPanel();
		    changePanel(pai.getMenuPanel());
		    
		    TableRules rulesTable = pai.getMenuPanel().getRulesPane().getRulesTable();
		    JTable table = rulesTable.createTable(columnNames,data);
		    
		    rulesTable.getScrollPanePergunta().setViewportView(table);
		    rulesTable.getScrollPanePergunta().updateUI();
		    rulesTable.getScrollPanePergunta().repaint();
		    rulesTable.updateUI();
		    rulesTable.repaint();
	    }
	
	
	

}
