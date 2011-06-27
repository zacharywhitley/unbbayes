package unbbayes.gui.umpst;


import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import unbbayes.model.umpst.entities.EntityModel;
import unbbayes.model.umpst.project.SearchModelRules;
import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.model.umpst.rules.RulesModel;


public class RulesAdd extends IUMPSTPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private GridBagConstraints constraints     = new GridBagConstraints();
	private JLabel titulo            = new JLabel();
	
	private JButton buttonAdd 	     = new JButton();
	private JButton buttonCancel     = new JButton("Cancel");
	
	private JTextField dateText,authorText;
	private JTextField ruleText,typeText;
	private JTextArea commentsText;
	private RulesModel rule;
	private JButton buttonCopy, buttonDelete;

	private TitledBorder bordaDadosComuns = new TitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
	

	private JList list,listAux; 
	private DefaultListModel listModel = new DefaultListModel();
	private DefaultListModel listModelAux = new DefaultListModel();

	public RulesAdd(UmpstModule janelaPai, RulesModel rule){
		super(janelaPai);
		
		
		this.rule = rule;
		this.setLayout(new GridBagLayout());
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridx=0; constraints.gridy = 0; constraints.weightx=0.5;constraints.weighty=0.6;	
		panelText();
		constraints.gridx=0; constraints.gridy = 1; constraints.weightx=0.5;constraints.weighty=0.4;	
		getTrackingPanel();
		/*
		constraints.gridx=0; constraints.gridy=1;
		createTraceabilityTable();*/
		listeners();

		if( rule == null){
			titulo.setText("Add new Rule");
			buttonAdd.setText(" Add ");
		} else {
			titulo.setText("Update Rule");
			buttonAdd.setText(" Update ");
			ruleText.setText(rule.getRulesName());
			typeText.setText(rule.getRuleType());
			commentsText.setText(rule.getComments());
			authorText.setText(rule.getAuthor());
			dateText.setText(rule.getDate());
			
		}
		
	}

	public void panelText(){
		
		GridBagConstraints c = new GridBagConstraints();
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		panel.setLayout(new GridBagLayout());
		
		GridBagConstraints d = new GridBagConstraints();
		d.gridx = 0; d.gridy = 0;
		d.fill = GridBagConstraints.PAGE_START;
		
		d.gridwidth = 2;
		d.insets = new Insets(0, 0, 0, 0);
		titulo.setFont(new Font("Arial", Font.BOLD, 32));
		titulo.setBackground(new Color(0x4169AA));
		panel.add( titulo, d);

		c.gridx = 0; c.gridy = 2; c.gridwidth=1;
		panel.add( new JLabel("Rule Description: "), c);
		c.gridx = 0; c.gridy = 3; c.gridwidth=1;
		panel.add(new JLabel("Rule Type"),c);
		c.gridx = 0; c.gridy = 4;c.gridwidth=1;
		panel.add( new JLabel("Author Name: "), c);
		c.gridx = 0; c.gridy = 5;c.gridwidth=1;
		panel.add( new JLabel("Date: "), c);
		c.gridx = 0; c.gridy = 6;c.gridwidth=1;
		panel.add( new JLabel("Comments: "), c);
		
		
			
		ruleText = new JTextField(20);
		typeText = new JTextField(20);
		commentsText = new JTextArea(5,21);
		authorText = new JTextField(20);
		dateText = new JTextField(20);
 

		c.gridx = 1; c.gridy = 2;c.gridwidth=2;
		panel.add( ruleText, c);
		
		c.gridx = 1; c.gridy = 3;c.gridwidth=2;
		panel.add( typeText, c);
		
		c.gridx = 1; c.gridy = 4;c.gridwidth=2;
		panel.add( authorText, c);c.gridwidth=2;
		
		c.gridx = 1; c.gridy = 5;c.gridwidth=2;
		panel.add( dateText, c);c.gridwidth=2;

		c.gridx = 1; c.gridy = 6;c.gridwidth=2;
		panel.add( commentsText, c);c.gridwidth=2;
		
			
		c.gridx = 0; c.gridy = 7; c.gridwidth = 1;
		panel.add( buttonCancel, c);
		
		c.gridx = 1; c.gridy = 7;c.gridwidth=1;
		panel.add( buttonAdd, c);
		
		
		panel.setBorder(BorderFactory.createTitledBorder("Rule's Details"));
		add(panel,constraints);
	}
	
	
	public void listeners(){
		
		buttonAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( rule == null){
					
					try {
						if (ruleText.getText().equals("")){
							JOptionPane.showMessageDialog(null, "Rule's name is empty");
						}
						else{
						    RulesModel ruleAdd = updateMapRules();					    
						    updateMapSearch(ruleAdd);
						    updateBacktracking(ruleAdd);
							updateTableRules();
						  	JOptionPane.showMessageDialog(null, "Rule successfully added",null, JOptionPane.INFORMATION_MESSAGE);
						}
						
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(null, "Error while creating rule", "UnBBayes", JOptionPane.WARNING_MESSAGE);
						UmpstModule pai = getFatherPanel();
						changePanel(pai.getMenuPanel());	
					
					}
				}
				else{
					if( JOptionPane.showConfirmDialog(null, "Do you want to update this Rule?", "UnBBayes", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION ){
						
						try{
							/**Cleaning Search Map*/
							Set<RulesModel> aux = new HashSet<RulesModel>();
							RulesModel rulesBeta;
							String[] strAux=rule.getRulesName().split(" ");

						    for (int i = 0; i < strAux.length; i++) {
					    		if(UMPSTProject.getInstance().getMapSearchRules().get(strAux[i])!=null){
					    			UMPSTProject.getInstance().getMapSearchRules().get(strAux[i]).getRulesRelated().remove(rule);
					    			aux = UMPSTProject.getInstance().getMapSearchRules().get(strAux[i]).getRulesRelated();
					    	    	for (Iterator<RulesModel> it = aux.iterator(); it.hasNext(); ) {
					    	    		rulesBeta = it.next();
					    	   		}
					    		}
					    		
						    	
						    }
						    /************/
							
							rule.setRulesName(ruleText.getText());
							rule.setRuleType(typeText.getText());
							rule.setComments(commentsText.getText());
							rule.setAuthor(authorText.getText());
							rule.setDate(dateText.getText());
							
						
							updateMapSearch(rule);
							updateBacktracking(rule);
							updateTableRules();
					
							
							JOptionPane.showMessageDialog(null, "Rule successfully updated",null, JOptionPane.INFORMATION_MESSAGE);	
						
						}
						catch (Exception e2) {
							JOptionPane.showMessageDialog(null,"Error while updating rule", "UnBBayes", JOptionPane.WARNING_MESSAGE);
							UmpstModule pai = getFatherPanel();
							changePanel(pai.getMenuPanel());	
						}
					}
				}
			}
		});

		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				UmpstModule pai = getFatherPanel();
				changePanel(pai.getMenuPanel());	
			}
		});
		
	
		

		ruleText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				commentsText.requestFocus();
			}
		});
		
		/*commentsText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				authorText.requestFocus();
			}
		});*/
		
		authorText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				dateText.requestFocus();
			}
		});
		
		dateText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				buttonAdd.requestFocus();
			}
		});
		
	}
	
	
	/** Returns an ImageIcon, or null if the path was invalid. */
    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = MainPanel.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }
    
    public RulesModel updateMapRules(){
    	String idAux = "";
    	Set<String> keys = UMPSTProject.getInstance().getMapRules().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);
		int tamanho = UMPSTProject.getInstance().getMapRules().size()+1;
		int maior = 0;
		String idAux2 = "";
		int intAux;
		
			
		if ( UMPSTProject.getInstance().getMapRules().size()!=0){
			for (String key: sortedKeys){
				idAux= UMPSTProject.getInstance().getMapRules().get(key).getId();
				if (idAux.contains(".")){
					intAux = idAux.indexOf(".");
					idAux2 = idAux.substring(0, intAux);
					if (maior<Integer.parseInt(idAux2)){
						maior = Integer.parseInt(idAux2);
					}
				}
				else{
					if (maior< Integer.parseInt(idAux)){
						maior = Integer.parseInt(idAux);
					}
				}
			}
			maior++;
			idAux = maior+"";
		}
		else{
			idAux = 1+"";
		}
			
		
	

		
		RulesModel rulesAdd = new RulesModel(idAux,ruleText.getText(),typeText.getText(), 
				commentsText.getText(), authorText.getText(), dateText.getText(),null,null);
		
		
		
	    UMPSTProject.getInstance().getMapRules().put(rulesAdd.getId(), rulesAdd);	
	    
	    return rulesAdd;
    }
    
    
    public void updateTableRules(){
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

    public void updateMapSearch(RulesModel ruleAdd){
	    /**Upating searchPanel*/
	    
	    String[] strAux = {};
	    strAux = ruleAdd.getRulesName().split(" ");
	    Set<RulesModel> ruleSetSearch = new HashSet<RulesModel>();

	    
	    for (int i = 0; i < strAux.length; i++) {
	    	if(!strAux[i].equals(" ")){
	    		if(UMPSTProject.getInstance().getMapSearchRules().get(strAux[i])==null){
	    			ruleSetSearch.add(ruleAdd);
	    			SearchModelRules searchModel = new SearchModelRules(strAux[i], ruleSetSearch);
	    			UMPSTProject.getInstance().getMapSearchRules().put(searchModel.getKeyWord(), searchModel);
	    		}
	    		else{
	    			UMPSTProject.getInstance().getMapSearchRules().get(strAux[i]).getRulesRelated().add(ruleAdd);
	    		}
	    	}
	    }
	    
		/************/		    

    }
    
    public void getTrackingPanel(){
		Box box = Box.createHorizontalBox();
		Set<String> keys = UMPSTProject.getInstance().getMapEntity().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);
		
		for (String key: sortedKeys){
			listModel.addElement(UMPSTProject.getInstance().getMapEntity().get(key).getEntityName());
		}
		
		
		/**This IF is responsable to update the first JList with all requirements elemente MINUS those 
		 * who are already registred as backtracking.
		 * */
		if (rule!=null){
			listAux = rule.getBacktracking();
			for (int i = 0; i < listAux.getModel().getSize();i++) {
				listModelAux.addElement((listAux.getModel().getElementAt(i)));
				if (listModel.contains(listAux.getModel().getElementAt(i))){
					listModel.remove(listModel.indexOf(listAux.getModel().getElementAt(i)));
				}
			}
			
		}
		
		list = new JList(listModel); //data has type Object[]
		list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		list.setLayoutOrientation(JList.VERTICAL_WRAP);
		list.setVisibleRowCount(-1);
		
		
		
		

		JScrollPane listScroller = new JScrollPane(list);
		listScroller.setMinimumSize(new Dimension(300,200));
				
		box.add(listScroller);
	
	
		
		buttonCopy = new JButton("copy >>");
		box.add(buttonCopy);
		buttonCopy.addActionListener(
				new ActionListener() {
					
					public void actionPerformed(ActionEvent e) {
						listModelAux.addElement(list.getSelectedValue());	
						listModel.removeElement(list.getSelectedValue());

					}
				}
		
		);
		
		buttonDelete = new JButton("<< delete");
		box.add(buttonDelete);
		buttonDelete.addActionListener(
				new ActionListener() {
					
					public void actionPerformed(ActionEvent e) {
						listModel.addElement(listAux.getSelectedValue());	
						listModelAux.removeElement(listAux.getSelectedValue());
					}
				}
		
		);	
		
		listAux = new JList(listModelAux);

		listAux.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		listAux.setLayoutOrientation(JList.VERTICAL_WRAP);
		listAux.setVisibleRowCount(-1);
	
		JScrollPane listScrollerAux = new JScrollPane(listAux);
		listScrollerAux.setMinimumSize(new Dimension(300,200));
		box.add(listScrollerAux);

		box.setBorder(BorderFactory.createTitledBorder("Adding backtracking(Entity)"));
		add(box,constraints);
		
	}
	
	public void updateBacktracking(RulesModel rule){
		String keyWord = "";
		Set<String> keys = UMPSTProject.getInstance().getMapEntity().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);
		
		
		
		if (listAux !=null){
			for (int i = 0; i < listAux.getModel().getSize();i++) {
				keyWord = listAux.getModel().getElementAt(i).toString();
				for (String key: sortedKeys){
					if (keyWord.equals( UMPSTProject.getInstance().getMapEntity().get(key).getEntityName()) ){
						UMPSTProject.getInstance().getMapEntity().get(key).getFowardTrackingRules().add(rule);
					}			
				
				}
			}
			rule.setBacktracking(listAux);

		}
		
	}

    	  	
}