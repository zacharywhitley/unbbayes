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
import javax.swing.JComboBox;
import javax.swing.JFrame;
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
import javax.swing.table.TableColumn;

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
	private JButton buttonBackEntities = new JButton("Add entity backtracking ");
	private JButton buttonBackAtributes = new JButton("Add atribute backtracking");
	private JButton buttonBackRelationship = new JButton("Add relationship backtracking");

	
	private JTextField dateText,authorText;
	private JTextField ruleText,typeText;
	private JTextArea commentsText;
	private RulesModel rule;
	private JButton buttonCopy, buttonDelete;
	private JComboBox ruleTypeText;
	

	private JList list,listAux, listAtributeAux, listRelationshipAux; 
	private DefaultListModel listModel = new DefaultListModel();
	private DefaultListModel listModelAux = new DefaultListModel();
	private DefaultListModel listModelAtrAux = new DefaultListModel();
	private DefaultListModel listModelRltAux = new DefaultListModel();

	private Object[][] dataBacktracking = {};
	private Object[][] dataFrame = {};

	
	public RulesAdd(UmpstModule janelaPai,UMPSTProject umpstProject, RulesModel rule){
		super(janelaPai);
		
		this.setUmpstProject(umpstProject);
		
		this.rule = rule;
		this.setLayout(new GridBagLayout());
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridx=0; constraints.gridy = 0; constraints.weightx=0.4;constraints.weighty=0.4;	
		panelText();
		constraints.gridx=0; constraints.gridy = 1; constraints.weightx=0.6;constraints.weighty=0.6;	
		add(getBacktrackingPanel(),constraints);
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
		commentsText.setLineWrap(true); 
		commentsText.setWrapStyleWord(true);
		commentsText.setBorder(BorderFactory.createEtchedBorder());
		
		authorText = new JTextField(20);
		dateText = new JTextField(20);
 
		//String[] rulesTypes = {"","Deterministic","Stochastic"};
		//ruleTypeText = new JComboBox(rulesTypes);

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
		
		
		panel.setBorder(BorderFactory.createTitledBorder("Rule Details"));
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
						    //updateBacktracking(ruleAdd);
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
					    		if(getUmpstProject().getMapSearchRules().get(strAux[i])!=null){
					    			getUmpstProject().getMapSearchRules().get(strAux[i]).getRulesRelated().remove(rule);
					    			aux = getUmpstProject().getMapSearchRules().get(strAux[i]).getRulesRelated();
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
							//updateBacktracking(rule);
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

		buttonBackRelationship.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				createFrameRelationship();				
			}
		});
		
		buttonBackEntities.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				createFrame();				
			}
		});
		
		buttonBackAtributes.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				createFrameAtributes();				
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
    	Set<String> keys = getUmpstProject().getMapRules().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);
		int tamanho = getUmpstProject().getMapRules().size()+1;
		int maior = 0;
		String idAux2 = "";
		int intAux;
		
			
		if ( getUmpstProject().getMapRules().size()!=0){
			for (String key: sortedKeys){
				idAux= getUmpstProject().getMapRules().get(key).getId();
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
				commentsText.getText(), authorText.getText(), dateText.getText(),null,null, null, null);
		
		
		
	    getUmpstProject().getMapRules().put(rulesAdd.getId(), rulesAdd);	
	    
	    return rulesAdd;
    }
    
    
    public void updateTableRules(){
    	String[] columnNames = {"ID","Rule","",""};
    	
    	
	    
		Object[][] data = new Object[getUmpstProject().getMapRules().size()][4];
		Integer i=0;
	    
		Set<String> keys = getUmpstProject().getMapRules().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);
		
		for (String key: sortedKeys){
			data[i][0] = getUmpstProject().getMapRules().get(key).getId();
			data[i][1] = getUmpstProject().getMapRules().get(key).getRulesName();			
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
	    		if(getUmpstProject().getMapSearchRules().get(strAux[i])==null){
	    			ruleSetSearch.add(ruleAdd);
	    			SearchModelRules searchModel = new SearchModelRules(strAux[i], ruleSetSearch);
	    			getUmpstProject().getMapSearchRules().put(searchModel.getKeyWord(), searchModel);
	    		}
	    		else{
	    			getUmpstProject().getMapSearchRules().get(strAux[i]).getRulesRelated().add(ruleAdd);
	    		}
	    	}
	    }
	    
		/************/		    

    }
    
    public void getTrackingPanel(){
		Box box = Box.createHorizontalBox();
		Set<String> keys = getUmpstProject().getMapEntity().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);
		
		for (String key: sortedKeys){
			listModel.addElement(getUmpstProject().getMapEntity().get(key).getEntityName());
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
    
    public JPanel getBacktrackingPanel(){
		
		JPanel panel = new JPanel();
		JScrollPane scrollPane = new JScrollPane();
		if(rule!=null){
			listAux = rule.getBacktracking();
			for (int i = 0; i < listAux.getModel().getSize();i++) {
				listModelAux.addElement((listAux.getModel().getElementAt(i)));
			}
			
			listAtributeAux = rule.getBacktrackingAtribute();

			for (int i = 0; i < listAtributeAux.getModel().getSize();i++) {
				listModelAtrAux.addElement((listAtributeAux.getModel().getElementAt(i)));
			}
			
			listRelationshipAux = rule.getBacktrackingRelationship();

			for (int i = 0; i < listRelationshipAux.getModel().getSize();i++) {
				listModelRltAux.addElement((listRelationshipAux.getModel().getElementAt(i)));
			}
			
			listAux = new JList(listModelAux);
			listAtributeAux= new JList(listModelAtrAux);
			listRelationshipAux = new JList(listModelRltAux);
			
			dataBacktracking = new Object[listAux.getModel().getSize()+listAtributeAux.getModel().getSize()+listRelationshipAux.getModel().getSize()][3];
			
			int i;
			for (i = 0; i < listAux.getModel().getSize(); i++) {
				dataBacktracking[i][0] = listAux.getModel().getElementAt(i);
				dataBacktracking[i][1] = "Entity";
				dataBacktracking[i][2] = "";
	
			}
			int j;
			for (j = 0; j < listAtributeAux.getModel().getSize(); j++) {
				dataBacktracking[j+i][0] = listAtributeAux.getModel().getElementAt(j);
				dataBacktracking[j+i][1] = "Atribute";
				dataBacktracking[j+i][2] = "";
	
			}
			int k;
			for (k = 0; k < listRelationshipAux.getModel().getSize(); k++) {
				dataBacktracking[k+j+i][0] = listRelationshipAux.getModel().getElementAt(k);
				dataBacktracking[k+j+i][1] = "Relationship";
				dataBacktracking[k+j+i][2] = "";
	
			}
			
			
			
			String[] columns = {"Name","Type",""};
			DefaultTableModel model = new DefaultTableModel(dataBacktracking,columns);
			JTable table = new JTable(model);
			
			TableButton buttonDel = new TableButton( new TableButton.TableButtonCustomizer()
			{
				public void customize(JButton button, int row, int column)
				{
					button.setIcon(new ImageIcon("images/del.gif") );

				}
			});

			TableColumn buttonColumn1 = table.getColumnModel().getColumn(columns.length-1);
			
			buttonColumn1.setMaxWidth(28);
			buttonColumn1.setCellRenderer(buttonDel);
			buttonColumn1.setCellEditor(buttonDel);
			
			buttonDel.addHandler(new TableButton.TableButtonPressedHandler() {	
				public void onButtonPress(int row, int column) {
					if (row<listAux.getModel().getSize()){
						String key = dataBacktracking[row][0].toString();
						listModelAux.remove(listModelAux.indexOf(key));
						listAux = new JList(listModelAux);
						rule.setBacktracking(listAux);
					}
					else{
						if (row<(listAux.getModel().getSize()+listAtributeAux.getModel().getSize())){
							String keyAtr = dataBacktracking[row][0].toString();
							listModelAtrAux.remove(listModelAtrAux.indexOf(keyAtr));
							listAtributeAux = new JList(listModelAtrAux);
							rule.setBacktrackingAtribute(listAtributeAux);
						}
						else{
							String keyAtr = dataBacktracking[row][0].toString();
							listModelRltAux.remove(listModelRltAux.indexOf(keyAtr));
							listRelationshipAux = new JList(listModelRltAux);
							rule.setBacktrackingRelationship(listRelationshipAux);
						}
					}
					UmpstModule father = getFatherPanel();
				    changePanel(father.getMenuPanel().getRulesPane().getRulesPanel().getRulesAdd(rule));
				}
			});
			
			panel = new JPanel();
		    panel.setLayout(new GridBagLayout());
		    
		    GridBagConstraints c = new GridBagConstraints();
			
		    if (rule!=null){
		    	c.gridx = 0; c.gridy = 0; c.gridwidth=1;
		    	panel.add(buttonBackEntities,c);
		    	buttonBackEntities.setToolTipText("Add backtracking from entities");
		    	
		    	c.gridx = 1; c.gridy = 0; c.gridwidth=1;
		    	panel.add(buttonBackAtributes,c);
		    	buttonBackAtributes.setToolTipText("Add backtracking from atributes");
		    	
		    	c.gridx = 2; c.gridy = 0; c.gridwidth=1;
		    	panel.add(buttonBackRelationship,c);
		    	buttonBackRelationship.setToolTipText("Add backtracking from relationship");
		    }
			
		    c.fill = GridBagConstraints.BOTH;
		    c.gridx=0;c.gridy=1;c.weightx=0.9;c.weighty=0.9;c.gridwidth=6;
			
			 scrollPane = new JScrollPane(table);
			 
			 panel.add(scrollPane,c);
		}
		
		return panel;
		//add(box,constraint);
		
	}
	
	/*public void updateBacktracking(RulesModel rule){
		String keyWord = "";
		Set<String> keys = getUmpstProject().getMapEntity().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);
		
		
		
		if (listAux !=null){
			for (int i = 0; i < listAux.getModel().getSize();i++) {
				keyWord = listAux.getModel().getElementAt(i).toString();
				for (String key: sortedKeys){
					if (keyWord.equals( getUmpstProject().getMapEntity().get(key).getEntityName()) ){
						getUmpstProject().getMapEntity().get(key).getFowardTrackingRules().add(rule);
					}			
				
				}
			}
			rule.setBacktracking(listAux);

		}
		
	}*/
	
    public void createFrame(){
		
		JFrame frame = new JFrame("Adding Backtracking from entities");
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		
		String[] columnNames = {"ID","Entity",""};
    	
		dataFrame = new Object[getUmpstProject().getMapEntity().size()][3];

	    
		Integer i=0;
	    
		Set<String> keys = getUmpstProject().getMapEntity().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);
		
		for (String key: sortedKeys){
			dataFrame[i][0] = getUmpstProject().getMapEntity().get(key).getId();
			dataFrame[i][1] = getUmpstProject().getMapEntity().get(key).getEntityName();			
			dataFrame[i][2] = "";
			i++;
		}
		
		
		
		DefaultTableModel model = new DefaultTableModel(dataFrame,columnNames);
		JTable table = new JTable(model);
		
		TableButton buttonEdit = new TableButton( new TableButton.TableButtonCustomizer()
		{
			public void customize(JButton button, int row, int column)
			{
				button.setIcon(new ImageIcon("images/add.gif") );

			}
		});

		TableColumn buttonColumn1 = table.getColumnModel().getColumn(columnNames.length-1);
		buttonColumn1.setMaxWidth(28);
		buttonColumn1.setCellRenderer(buttonEdit);
		buttonColumn1.setCellEditor(buttonEdit);
		
		buttonEdit.addHandler(new TableButton.TableButtonPressedHandler() {	
			public void onButtonPress(int row, int column) {
				
				String key = dataFrame[row][1].toString();
				list = rule.getBacktracking();
				
				listModel.addElement(key);
				list = new JList(listModel);
				rule.setBacktracking(list);
				
				Set<String> keys = getUmpstProject().getMapEntity().keySet();
				TreeSet<String> sortedKeys = new TreeSet<String>(keys);
				for (String keyAux : sortedKeys){
					if (getUmpstProject().getMapEntity().get(keyAux).getEntityName().equals(key)){
						getUmpstProject().getMapEntity().get(keyAux).getFowardTrackingRules().add(rule);
					}
				}
					
				
				UmpstModule father = getFatherPanel();
			    changePanel(father.getMenuPanel().getRulesPane().getRulesPanel().getRulesAdd(rule));
				
			}
		});
		
		
		
		JScrollPane scroll = new JScrollPane(table);

		c.gridx=0;c.gridy=0;c.weightx=0.5;c.weighty=0.5;  c.fill = GridBagConstraints.BOTH;
		panel.add(scroll,c);
		panel.setPreferredSize(new Dimension(400,200));
		
		frame.add(panel);
		
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frame.setSize(300,200);
		frame.setVisible(true);
		
	}	

	public void createFrameAtributes(){
		
		JFrame frame = new JFrame("Adding Backtracking from atributes");
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		
		String[] columnNames = {"ID","Atribute",""};
		
		dataFrame = new Object[getUmpstProject().getMapAtribute().size()][3];
	
	    
		Integer i=0;
	    
		Set<String> keys = getUmpstProject().getMapAtribute().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);
		
		for (String key: sortedKeys){
			dataFrame[i][0] = getUmpstProject().getMapAtribute().get(key).getId();
			dataFrame[i][1] = getUmpstProject().getMapAtribute().get(key).getAtributeName();			
			dataFrame[i][2] = "";
			i++;
		}
		
		
		
		DefaultTableModel model = new DefaultTableModel(dataFrame,columnNames);
		JTable table = new JTable(model);
		
		TableButton buttonEdit = new TableButton( new TableButton.TableButtonCustomizer()
		{
			public void customize(JButton button, int row, int column)
			{
				button.setIcon(new ImageIcon("images/add.gif") );
	
			}
		});
	
		TableColumn buttonColumn1 = table.getColumnModel().getColumn(columnNames.length-1);
		buttonColumn1.setMaxWidth(28);
		buttonColumn1.setCellRenderer(buttonEdit);
		buttonColumn1.setCellEditor(buttonEdit);
		
		buttonEdit.addHandler(new TableButton.TableButtonPressedHandler() {	
			public void onButtonPress(int row, int column) {
				
				String key = dataFrame[row][1].toString();
				list = rule.getBacktrackingAtribute();
				
				listModel.addElement(key);
				list = new JList(listModel);
				rule.setBacktrackingAtribute(list);
				
				Set<String> keys = getUmpstProject().getMapAtribute().keySet();
				TreeSet<String> sortedKeys = new TreeSet<String>(keys);
				for (String keyAux : sortedKeys){
					if (getUmpstProject().getMapAtribute().get(keyAux).getAtributeName().equals(key)){
						getUmpstProject().getMapAtribute().get(keyAux).getFowardTrackingRules().add(rule);
					}
				}
					
				
				UmpstModule father = getFatherPanel();
			    changePanel(father.getMenuPanel().getRulesPane().getRulesPanel().getRulesAdd(rule));
				
			}
		});
		
		
		
		JScrollPane scroll = new JScrollPane(table);
	
		c.gridx=0;c.gridy=0;c.weightx=0.5;c.weighty=0.5;  c.fill = GridBagConstraints.BOTH;
		panel.add(scroll,c);
		panel.setPreferredSize(new Dimension(400,200));
		
		frame.add(panel);
		
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frame.setSize(300,200);
		frame.setVisible(true);
		
	}	

	public void createFrameRelationship(){
		
		JFrame frame = new JFrame("Adding Backtracking from relationship");
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		
		String[] columnNames = {"ID","Relationship",""};
		
		dataFrame = new Object[getUmpstProject().getMapRelationship().size()][3];
	
	    
		Integer i=0;
	    
		Set<String> keys = getUmpstProject().getMapRelationship().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);
		
		for (String key: sortedKeys){
			dataFrame[i][0] = getUmpstProject().getMapRelationship().get(key).getId();
			dataFrame[i][1] = getUmpstProject().getMapRelationship().get(key).getRelationshipName();			
			dataFrame[i][2] = "";
			i++;
		}
		
		
		
		DefaultTableModel model = new DefaultTableModel(dataFrame,columnNames);
		JTable table = new JTable(model);
		
		TableButton buttonEdit = new TableButton( new TableButton.TableButtonCustomizer()
		{
			public void customize(JButton button, int row, int column)
			{
				button.setIcon(new ImageIcon("images/add.gif") );
	
			}
		});
	
		TableColumn buttonColumn1 = table.getColumnModel().getColumn(columnNames.length-1);
		buttonColumn1.setMaxWidth(28);
		buttonColumn1.setCellRenderer(buttonEdit);
		buttonColumn1.setCellEditor(buttonEdit);
		
		buttonEdit.addHandler(new TableButton.TableButtonPressedHandler() {	
			public void onButtonPress(int row, int column) {
				
				String key = dataFrame[row][1].toString();
				list = rule.getBacktrackingRelationship();
				
				listModel.addElement(key);
				list = new JList(listModel);
				rule.setBacktrackingRelationship(list);
				
				Set<String> keys = getUmpstProject().getMapRelationship().keySet();
				TreeSet<String> sortedKeys = new TreeSet<String>(keys);
				for (String keyAux : sortedKeys){
					if (getUmpstProject().getMapRelationship().get(keyAux).getRelationshipName().equals(key)){
						getUmpstProject().getMapRelationship().get(keyAux).getFowardtrackingRules().add(rule);
					}
				}
					
				
				UmpstModule father = getFatherPanel();
			    changePanel(father.getMenuPanel().getRulesPane().getRulesPanel().getRulesAdd(rule));
				
			}
		});
		
		
		
		JScrollPane scroll = new JScrollPane(table);
	
		c.gridx=0;c.gridy=0;c.weightx=0.5;c.weighty=0.5;  c.fill = GridBagConstraints.BOTH;
		panel.add(scroll,c);
		panel.setPreferredSize(new Dimension(400,200));
		
		frame.add(panel);
		
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frame.setSize(300,200);
		frame.setVisible(true);
		
	}	
    	  	
}