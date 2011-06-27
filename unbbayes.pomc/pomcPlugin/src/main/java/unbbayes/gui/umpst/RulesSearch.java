package unbbayes.gui.umpst;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.model.umpst.rules.RulesModel;

public class RulesSearch extends IUMPSTPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JLabel labelRule;
	
	private JButton buttonSearch;
	private JButton buttonAddRule,buttonCancel;

	private JTextField textRule;
	

	
	
	
	public RulesSearch(UmpstModule janelaPai){
		super(janelaPai);
		
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

		buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
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
			buttonAddRule = new JButton ("add new rule");
			buttonAddRule.setForeground(Color.blue);
			buttonAddRule.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					changePanel(getRulesAdd(null));
				}
			});
		}
		
		return buttonAddRule;
	} 
	
	
	public RulesAdd getRulesAdd(RulesModel rule){
		
		RulesAdd ret = new RulesAdd(getFatherPanel(),rule);
		
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
			buttonSearch = new JButton("Search: ");
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
			buttonCancel = new JButton ("cancel search");
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
		
		return textRule;
	}
	
	public void updateTableRules(){
    	String[] columnNames = {"ID","Rule","",""};
    	
    	
    	
		Set<RulesModel> aux = UMPSTProject.getInstance().getMapSearchRules().get(textRule.getText()).getRulesRelated();
		RulesModel rule;
		Object[][] data = new Object[UMPSTProject.getInstance().getMapSearchRules().get(textRule.getText()).getRulesRelated().size()][4];

		Integer i=0;
		
	   
    	for (Iterator<RulesModel> it = aux.iterator(); it.hasNext(); ) {
    	     rule = it.next();  // No downcasting required.
    	     
    	 	data[i][0] = rule.getId();
			data[i][1] = rule.getRulesName();			
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
	    	
	    	
		    
			Object[][] data = new Object[UMPSTProject.getInstance().getMapRules().size()][4];
			Integer i=0;
		    
			Set<String> keys = UMPSTProject.getInstance().getMapRules().keySet();
			TreeSet<String> sortedKeys = new TreeSet<String>(keys);
			
			for (String key: sortedKeys){
				data[i][0] = UMPSTProject.getInstance().getMapRules().get(key).getId();
				data[i][1] = UMPSTProject.getInstance().getMapRules().get(key).getRulesName();			
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
