package unbbayes.gui.umpst;


import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import unbbayes.model.umpst.entities.AtributeModel;
import unbbayes.model.umpst.entities.EntityModel;
import unbbayes.model.umpst.entities.RelationshipModel;
import unbbayes.model.umpst.project.SearchModelEntity;
import unbbayes.model.umpst.project.SearchModelGoal;
import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.model.umpst.requirements.GoalModel;
import unbbayes.model.umpst.requirements.HypothesisModel;
import unbbayes.model.umpst.rules.RulesModel;


public class EntitiesAdd extends IUMPSTPanel {
	
	private Object[][] dataFrame ;
	private Object[][] dataFrameHypo ;
	private Object[][] dataBacktracking ;
	private Object[][] dataBacktrackingHypo ;
	
	private JComboBox atributeVinculationList = new JComboBox();
	
	private GridBagConstraints constraint     = new GridBagConstraints();
	private JLabel titulo            = new JLabel();
	
	private JButton buttonAdd 	     = new JButton();
	private JButton buttonCancel     = new JButton("Cancel");
	private JButton buttonAtribute = new JButton("atribute");
	private JButton buttonFrame	= new JButton ("frame");
	private JButton buttonFrameHypo = new JButton("frameHypo");
	
	private JTextField dateText,authorText;
	private JTextField entityText;
	private JTextField commentsText;
	private EntityModel entity;

	private static final long serialVersionUID = 1L;
	
	private JList list,listAux; 
    private DefaultListModel listModel = new DefaultListModel();
	private DefaultListModel listModelAux = new DefaultListModel();
	
	private JList listHypothesis,listHypothesisAux; 
    private DefaultListModel listHypothesisModel = new DefaultListModel();
	private DefaultListModel listHypothesisModelAux = new DefaultListModel();
	
	
	public EntitiesAdd(UmpstModule janelaPai, EntityModel entity){
		super(janelaPai);
		
		this.entity = entity;
		this.setLayout(new GridBagLayout());
		constraint.fill = GridBagConstraints.BOTH;
		constraint.gridx=0;constraint.gridy=0;constraint.weightx=0.4;constraint.weighty=0.4;
		panelText();
		
		
		
		GridBagConstraints c     = new GridBagConstraints();
		JPanel panelBacktracking = new JPanel();
		panelBacktracking.setLayout(new GridBagLayout());
		c.fill = GridBagConstraints.BOTH;
		c.gridx=0;c.gridy=0;c.weightx=0.5;c.weighty=0.5;
		panelBacktracking.add(getBacktrackingPanel(),c);
		c.gridx=0;c.gridy=1;c.weightx=0.5;c.weighty=0.5;
		panelBacktracking.add(getBacktrackingHypothesis(),c);
		
		constraint.gridx=0;constraint.gridy=1;constraint.weightx=0.5;constraint.weighty=0.6;
		add(panelBacktracking,constraint);
		
		
		
		constraint.gridx=1;constraint.gridy=1;constraint.weightx=0.5;constraint.weighty=0.6;
		createAtributeTable();
		constraint.gridx=1;constraint.gridy=0;constraint.weightx=0.6;constraint.weighty=0.6;
		createTraceabilityTable();
		listeners();

		if( entity == null){
			titulo.setText("Add new entity");
			buttonAdd.setText(" Add ");
		} else {
			titulo.setText("Update Entity");
			buttonAdd.setText(" Update ");
			entityText.setText(entity.getEntityName());
			commentsText.setText(entity.getComments());
			authorText.setText(entity.getAuthor());
			dateText.setText(entity.getDate());
		}
		
	}

	public void panelText(){
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		c.gridx = 0; c.gridy = 2;c.gridwidth = 1;
		panel.add( new JLabel("Entity Description: "), c);
		c.gridx = 0; c.gridy = 3;c.gridwidth = 1;
		panel.add( new JLabel("Author Name: "), c);
		c.gridx = 0; c.gridy = 4;c.gridwidth = 1;
		panel.add( new JLabel("Date: "), c);
		c.gridx = 0; c.gridy = 5;c.gridwidth = 1;
		panel.add( new JLabel("Comments: "), c);
		

		GridBagConstraints d = new GridBagConstraints();
		d.gridx = 0; d.gridy = 0;
		d.fill = GridBagConstraints.PAGE_START;
		d.gridwidth = 3;
		d.insets = new Insets(0, 0, 0, 0);
		titulo.setFont(new Font("Arial", Font.BOLD, 32));
		titulo.setBackground(new Color(0x4169AA));
		panel.add( titulo, d);
				
		entityText = new JTextField(20);
		commentsText = new JTextField(20);
		authorText = new JTextField(20);
		dateText = new JTextField(20);
 

		c.gridx = 1; c.gridy = 2;c.gridwidth = 2;
		panel.add( entityText, c);
		
		c.gridx = 1; c.gridy = 3;c.gridwidth = 2;
		panel.add( authorText, c);
		
		c.gridx = 1; c.gridy = 4;c.gridwidth = 2;
		panel.add( dateText, c);
		
		c.gridx = 1; c.gridy = 5;c.gridwidth = 2;
		panel.add( commentsText, c);
		
		Box box = Box.createHorizontalBox();
		box.add(buttonCancel);
		//box.add(buttonAtribute);
		//box.add(buttonFrame);
		//box.add(buttonFrameHypo);
		box.add(buttonAdd);
		
		buttonAdd.setToolTipText("Save this entity");
		buttonCancel.setToolTipText("Cancel and return to main panel");
		
		c.gridx = 2; c.gridy = 6; c.gridwidth = 2;
		panel.add(box,c);
		/*
		panel.add( buttonCancel, c);
		c.gridx = 1; c.gridy = 6; c.gridwidth = 1;
		panel.add( buttonAtribute, c);
		buttonAtribute.setToolTipText("Add new Atribute");
		c.gridx = 2; c.gridy = 6; c.gridwidth = 1;
		panel.add(buttonAdd,c);*/
		
		panel.setBorder(BorderFactory.createTitledBorder("Rule's details"));
		
		add(panel,constraint);
	
	}
	
	
	
	
	public void listeners(){
		
		buttonAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( entity == null){
					try {
						
						if (entityText.getText().equals("")){
							JOptionPane.showMessageDialog(null, "Entity details are empty!");
						}
						else{
						
						
							EntityModel entityAdd = updateMaEntity();					    
						    updateMapSearch(entityAdd);
						    //updateBacktracking(entityAdd);
							updateTableEntities();
							JOptionPane.showMessageDialog(null, "entity successfully added",null, JOptionPane.INFORMATION_MESSAGE);
						}
					
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(null, "Error while creating entity", "UnBBayes", JOptionPane.WARNING_MESSAGE);
						UmpstModule pai = getFatherPanel();
						changePanel(pai.getMenuPanel());	
					
					}
				}
				else{
					if( JOptionPane.showConfirmDialog(null, "Do you want to update this entity?", "UnBBayes", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION ){
						//EntityModel entity = new EntityModel(entityText.getText(),commentsText.getText(), authorText.getText(), dateText.getText(),null);
						
						
						
						try{
							

							/**Cleaning Search Map*/
							Set<EntityModel> aux = new HashSet<EntityModel>();
							EntityModel entityBeta;
							String[] strAux = entity.getEntityName().split(" ");

						    for (int i = 0; i < strAux.length; i++) {
					    		if(UMPSTProject.getInstance().getMapSearchEntity().get(strAux[i])!=null){
					    			UMPSTProject.getInstance().getMapSearchEntity().get(strAux[i]).getEntitiesRelated().remove(entity);
					    			aux = UMPSTProject.getInstance().getMapSearchEntity().get(strAux[i]).getEntitiesRelated();
					    	    	for (Iterator<EntityModel> it = aux.iterator(); it.hasNext(); ) {
					    	    		entityBeta = it.next();
					    	   		}
					    		}
					    		
						    	
						    }
						    /************/
							
							entity.setEntityName(entityText.getText());
							entity.setComments(commentsText.getText());
							entity.setAuthor(authorText.getText());
							entity.setDate(dateText.getText());
							
							updateMapSearch(entity);
							//updateBacktracking(entity);
							updateTableEntities();
							
							JOptionPane.showMessageDialog(null, "entity successfully updated", "UnBBayes", JOptionPane.INFORMATION_MESSAGE);
						
							
						}
						catch (Exception e2) {
							JOptionPane.showMessageDialog(null,"Error while ulpating entity", "UnBBayes", JOptionPane.WARNING_MESSAGE);
							UmpstModule pai = getFatherPanel();
							changePanel(pai.getMenuPanel());	
						}
					}
				}
			}
		});
		
		buttonFrameHypo.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				createFrameHypo();
				
			}
		});
		
		buttonFrame.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				createFrame();				
			}
		});

		atributeVinculationList.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				//JOptionPane.showMessageDialog(null, "selecionou "+petList.getSelectedIndex());
				addVinculateHypothesis((String) atributeVinculationList.getSelectedItem());
			}
		});
		
		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				UmpstModule pai = getFatherPanel();
				changePanel(pai.getMenuPanel());	
			}
		});
		
		buttonAtribute.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				changePanel(new AtributeAdd(getFatherPanel(), entity, null, null));

			}
		});
		
		
		entityText.addActionListener(new ActionListener() {
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
    
    public EntityModel updateMaEntity(){
    	String idAux = "";
		int intAux = 0;
		int tamanho = UMPSTProject.getInstance().getMapEntity().size()+1;
		
		
					
			if ( UMPSTProject.getInstance().getMapEntity().size()!=0){
				idAux = tamanho+"";
			}
			else{
				idAux = "1";
			}
	
		
		EntityModel entityAdd = new EntityModel(idAux,entityText.getText(),commentsText.getText(), authorText.getText(), 
				dateText.getText(),null,null,null,null,null,null);
		
		
	    UMPSTProject.getInstance().getMapEntity().put(entityAdd.getId(), entityAdd);	
	    
	    return entityAdd;
    }
    
    
    public void updateTableEntities(){
    	String[] columnNames = {"ID","Entity","",""};	    
	    
		Object[][] data = new Object[UMPSTProject.getInstance().getMapEntity().size()][4];
		Integer i=0;
	    
		Set<String> keys = UMPSTProject.getInstance().getMapEntity().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);
		
		for (String key: sortedKeys){
			data[i][0] = UMPSTProject.getInstance().getMapEntity().get(key).getId();
			data[i][1] = UMPSTProject.getInstance().getMapEntity().get(key).getEntityName();			
			data[i][2] = "";
			data[i][3] = "";
			i++;
		}
   
	    UmpstModule pai = getFatherPanel();
	    changePanel(pai.getMenuPanel());
	    
	    TableEntities entitiesTable = pai.getMenuPanel().getEntitiesPane().getEntitiesTable();
	    JTable table = entitiesTable.createTable(columnNames,data);
	    
	    entitiesTable.getScrollPanePergunta().setViewportView(table);
	    entitiesTable.getScrollPanePergunta().updateUI();
	    entitiesTable.getScrollPanePergunta().repaint();
	    entitiesTable.updateUI();
	    entitiesTable.repaint();
    }

    public void updateMapSearch(EntityModel entityAdd){
	    /**Upating searchPanel*/
	    
	    String[] strAux = {};
	    strAux = entityAdd.getEntityName().split(" ");
	    Set<EntityModel> entitySetSearch = new HashSet<EntityModel>();

	    
	    for (int i = 0; i < strAux.length; i++) {
	    	if(!strAux[i].equals(" ")){
	    		if(UMPSTProject.getInstance().getMapSearchEntity().get(strAux[i])==null){
	    			entitySetSearch.add(entityAdd);
	    			SearchModelEntity searchModel = new SearchModelEntity(strAux[i], entitySetSearch);
	    			UMPSTProject.getInstance().getMapSearchEntity().put(searchModel.getKeyWord(), searchModel);
	    		}
	    		else{
	    			UMPSTProject.getInstance().getMapSearchEntity().get(strAux[i]).getEntitiesRelated().add(entityAdd);
	    		}
	    	}
	    }
	    
		/************/		    

    }


	public JPanel getBacktrackingPanel(){
		
		Set<String> keys = UMPSTProject.getInstance().getMapGoal().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);
		
		JPanel panel = new JPanel();
	
		/*for (String key: sortedKeys){
			listModel.addElement(UMPSTProject.getInstance().getMapGoal().get(key).getGoalName());
		}
		
		
		if (entity!=null){
			listAux = entity.getBacktracking();
			for (int i = 0; i < listAux.getModel().getSize();i++) {
				listModelAux.addElement((listAux.getModel().getElementAt(i)));
				if (listModel.contains(listAux.getModel().getElementAt(i))){
					listModel.remove(listModel.indexOf(listAux.getModel().getElementAt(i)));
				}
			}
			
		}*/
		
		//list = new JList(listModel); //data has type Object[]
		JScrollPane scrollPane = new JScrollPane();
		if(entity!=null){
			listAux = entity.getBacktracking();
			for (int i = 0; i < listAux.getModel().getSize();i++) {
				listModelAux.addElement((listAux.getModel().getElementAt(i)));
			}
			listAux = new JList(listModelAux);
			dataBacktracking = new Object[listAux.getModel().getSize()][3];
	
			for (int i = 0; i < listAux.getModel().getSize(); i++) {
				dataBacktracking[i][0] = listAux.getModel().getElementAt(i);
				dataBacktracking[i][1] = "Goal";
				dataBacktracking[i][2] = "";
	
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
					String key = dataBacktracking[row][0].toString();
					listModelAux.remove(listModelAux.indexOf(key));
					listAux = new JList(listModelAux);
					entity.setBacktracking(listAux);
					
					UmpstModule pai = getFatherPanel();
				    changePanel(pai.getMenuPanel().getEntitiesPane().getEntitiesPanel().getEntitiesMainPanel(entity));
				}
			});
			
			panel = new JPanel();
		    panel.setLayout(new GridBagLayout());
		    
		    GridBagConstraints c = new GridBagConstraints();
			
		    if (entity!=null){
		    	c.gridx = 1; c.gridy = 0; c.gridwidth=1;
		    	panel.add(buttonFrame,c);
		    	buttonFrame.setToolTipText("Add backtracking from goals");
		    }
			
		    c.fill = GridBagConstraints.BOTH;
		    c.gridx=0;c.gridy=1;c.weightx=0.9;c.weighty=0.9;c.gridwidth=6;
			
			 scrollPane = new JScrollPane(table);
			 
			 panel.add(scrollPane,c);
		}
		
		return panel;
		//add(box,constraint);
		
	}
	
public JPanel getBacktrackingHypothesis(){
		
		JPanel panel = new JPanel();
	
		JScrollPane scrollPane = new JScrollPane();
		if(entity!=null){
			listHypothesisAux = entity.getBacktrackingHypothesis();
			for (int i = 0; i < listHypothesisAux.getModel().getSize();i++) {
				listHypothesisModelAux.addElement((listHypothesisAux.getModel().getElementAt(i)));
			}
			listHypothesisAux = new JList(listHypothesisModelAux);
			dataBacktrackingHypo = new Object[listHypothesisAux.getModel().getSize()][3];
	
			for (int j = 0; j < listHypothesisAux.getModel().getSize(); j++) {
				dataBacktrackingHypo[j][0] = listHypothesisAux.getModel().getElementAt(j);
				dataBacktrackingHypo[j][1] = "Hypothesis";
				dataBacktrackingHypo[j][2] = "";
	
			}
			String[] columns = {"Name","Type",""};
			DefaultTableModel model = new DefaultTableModel(dataBacktrackingHypo,columns);
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
					String key = dataBacktrackingHypo[row][0].toString();
					listHypothesisModelAux.remove(listHypothesisModelAux.indexOf(key));
					listHypothesisAux = new JList(listHypothesisModelAux);
					entity.setBacktrackingHypothesis(listHypothesisAux);
					
					UmpstModule pai = getFatherPanel();
				    changePanel(pai.getMenuPanel().getEntitiesPane().getEntitiesPanel().getEntitiesMainPanel(entity));
				}
			});
			
			
			panel = new JPanel();
		    panel.setLayout(new GridBagLayout());
		    
		    GridBagConstraints c = new GridBagConstraints();
			
		    if (entity!=null){
		    	c.gridx = 1; c.gridy = 0; c.gridwidth=1;
		    	panel.add(buttonFrameHypo,c);
		    	buttonFrameHypo.setToolTipText("Add backtracking from hypothesis");
		    	
		    }
			
		    c.fill = GridBagConstraints.BOTH;
		    c.gridx=0;c.gridy=1;c.weightx=0.9;c.weighty=0.9;c.gridwidth=6;
			scrollPane = new JScrollPane(table);
			panel.add(scrollPane,c);
		}
		
		return panel;
		//add(box,constraint);
		
	}
	
	
	
	
	public void updateBacktracking(EntityModel entity){
		String keyWord = "";
		Set<String> keys = UMPSTProject.getInstance().getMapGoal().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);
		
		
		
		if (list !=null){
			for (int i = 0; i < list.getModel().getSize();i++) {
				keyWord = list.getModel().getElementAt(i).toString();
				for (String key: sortedKeys){
					if (keyWord.equals( UMPSTProject.getInstance().getMapGoal().get(key).getGoalName()) ){
						UMPSTProject.getInstance().getMapGoal().get(key).getFowardTrackingEntity().add(entity);
					}			
				
				}
			}
			entity.setBacktracking(list);

		}
		
		if (listHypothesis !=null){
			for (int i = 0; i < listHypothesis.getModel().getSize();i++) {
				keyWord = listHypothesis.getModel().getElementAt(i).toString();
				for (String key: sortedKeys){
					if ( UMPSTProject.getInstance().getMapHypothesis().get(key)!=null){
						if (keyWord.equals( UMPSTProject.getInstance().getMapHypothesis().get(key).getHypothesisName()) ){
							UMPSTProject.getInstance().getMapHypothesis().get(key).getFowardTrackingEntity().add(entity);
						}	
					}
				
				}
			}
			entity.setBacktrackingHypothesis(listHypothesis);

		}
		
	}
	
	public void createAtributeTable(){
    	
	    TableAtribute atributesTable = new TableAtribute(getFatherPanel(),entity);
	    JTable table = atributesTable.createTable();
	    JScrollPane scrollPane = new JScrollPane(table);

	    
	    JPanel panel = new JPanel();
	    panel.setLayout(new GridBagLayout());
	    
	    GridBagConstraints c = new GridBagConstraints();
		
	    if (entity!=null){
	    	c.gridx = 1; c.gridy = 0; c.gridwidth=1;
	    	panel.add(buttonAtribute,c);
	    	buttonAtribute.setToolTipText("Add new atribute to entity");
	    	
	    	c.gridx = 0; c.gridy = 0; c.gridwidth=1;
	    	panel.add(vinculateAtribute(),c);
	    	
	    }
		
	    c.fill = GridBagConstraints.BOTH;
	    c.gridx=0;c.gridy=1;c.weightx=0.9;c.weighty=0.9;c.gridwidth=6;
	    
	    panel.add(scrollPane,c);
	    panel.setBorder(BorderFactory.createTitledBorder("List of Atributes"));

	   
	    add(panel,constraint);

    }
	
	
	public void createTraceabilityTable(){
		
		int i = 0;
		String[] columns = {"Name", "Type"};
		
		if ( (entity!=null) && (entity.getMapAtributes()!=null) ){
		
			Set<String> keys = entity.getMapAtributes().keySet();
			TreeSet<String> sortedString = new TreeSet<String>(keys);
			
			for (String key : sortedString){
				i++;
			}
		}
		
		if ((entity!=null)&&(entity.getFowardTrackingRules()!=null)){
			
			Set<RulesModel> aux = entity.getFowardTrackingRules();
			RulesModel rule;
	    	for (Iterator<RulesModel> it = aux.iterator(); it.hasNext(); ) {
	    		rule = it.next();
	    		i++;
	    	}

		}
		
		if ((entity!=null)&&(entity.getFowardTrackingRelationship()!=null)){
			Set<RelationshipModel> aux = entity.getFowardTrackingRelationship();
			RelationshipModel relationship;
	    	for (Iterator<RelationshipModel> it = aux.iterator(); it.hasNext(); ) {
	    		relationship = it.next();
	    		i++;
	    	}

			
		}
		
		Object[][] data = new Object[i+1][2];
		i=0;
		
		if ( (entity!=null) && (entity.getMapAtributes()!=null) ){
			
			Set<String> keys = entity.getMapAtributes().keySet();
			TreeSet<String> sortedString = new TreeSet<String>(keys);
			
			for (String key : sortedString){
				data[i][0] = entity.getMapAtributes().get(key).getAtributeName();
				data[i][1] = "Atribute";
				i++;
			}
		}
		
		if ((entity!=null)&&(entity.getFowardTrackingRules()!=null)){
			
			Set<RulesModel> aux = entity.getFowardTrackingRules();
			RulesModel rule;
	    	for (Iterator<RulesModel> it = aux.iterator(); it.hasNext(); ) {
	    		rule = it.next();
	    		data[i][0] = rule.getRulesName();
	    		data[i][1] = "Rule";
	    		i++;
	    	}

		}
		
		if ((entity!=null)&&(entity.getFowardTrackingRelationship()!=null)){
			Set<RelationshipModel> aux = entity.getFowardTrackingRelationship();
			RelationshipModel relationship;
	    	for (Iterator<RelationshipModel> it = aux.iterator(); it.hasNext(); ) {
	    		relationship = it.next();
	    		data[i][0] = relationship.getRelationshipName();
	    		data[i][1] = "Relationship";
	    		i++;
	    	}

			
		}
		
		
		DefaultTableModel model = new DefaultTableModel(data, columns);
		JTable table = new JTable(model);
		
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setBorder(BorderFactory.createTitledBorder("This entity traceability"));
		
		add(scrollPane,constraint);
		
		
		
	}
	
	
 	
	public JComboBox vinculateAtribute(){

	    Set<String> keys = UMPSTProject.getInstance().getMapEntity().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);	
		
		Set<String> keysAtribute;
		TreeSet<String> sortedKeysAtribute;
		EntityModel entityAux;
		int i=0;
		/**This is only to found the number of other hypothesis existents in order to create 
		 *     	    String[] allOtherHypothesis = new String[i];
		 * */
		for (String key: sortedKeys){
			if(UMPSTProject.getInstance().getMapEntity().get(key)!=entity){
				if(UMPSTProject.getInstance().getMapEntity().get(key).getMapAtributes()!=null){
					
					entityAux = UMPSTProject.getInstance().getMapEntity().get(key);
					keysAtribute = entityAux.getMapAtributes().keySet();
					sortedKeysAtribute = new TreeSet<String>(keysAtribute);	
					
					for (String keyHypo : sortedKeysAtribute){
						/**Testing if the hypothesis is already in this goal*/
						if ( entity.getMapAtributes().get(entityAux.getMapAtributes().get(keyHypo).getId())==null )
							i++;
					}

				}
			}
		}   
		
	    String[] allOtherAtributes = new String[i];

		 i=0;
		 
		 
		 for (String key: sortedKeys){
				if(UMPSTProject.getInstance().getMapEntity().get(key)!=entity){
					if(UMPSTProject.getInstance().getMapEntity().get(key).getMapAtributes()!=null){
						
						entityAux = UMPSTProject.getInstance().getMapEntity().get(key);
						keysAtribute = entityAux.getMapAtributes().keySet();
						sortedKeysAtribute = new TreeSet<String>(keysAtribute);	
						
						for (String keyAtribute : sortedKeysAtribute){
							/**Testing if the hypothesis is already in this goal*/
							if ( entity.getMapAtributes().get(entityAux.getMapAtributes().get(keyAtribute).getId())==null ){
								allOtherAtributes[i] = entityAux.getMapAtributes().get(keyAtribute).getAtributeName();
								i++;
							}
						}

					}
				
				}
			} 
		 
		
	    
		
		atributeVinculationList = new JComboBox(allOtherAtributes);
		
		
		return atributeVinculationList;
		
	}
	
	public void addVinculateHypothesis(String hypothesisRelated){
		
		 Set<String> keys = UMPSTProject.getInstance().getMapEntity().keySet();
			TreeSet<String> sortedKeys = new TreeSet<String>(keys);	
			
			Set<String> keysAtribute;
			TreeSet<String> sortedKeysAtribute;
			int i=0;
			Boolean achou = false;
		
			for (String key: sortedKeys){
				if(UMPSTProject.getInstance().getMapEntity().get(key).getMapAtributes()!=null){	
				keysAtribute = UMPSTProject.getInstance().getMapEntity().get(key).getMapAtributes().keySet();
				sortedKeysAtribute = new TreeSet<String>(keysAtribute);
				for(String keyAux : sortedKeysAtribute){
					if (UMPSTProject.getInstance().getMapEntity().get(key).getMapAtributes().get(keyAux).getAtributeName().equals(hypothesisRelated)){
						updateMapAtribute(UMPSTProject.getInstance().getMapEntity().get(key).getMapAtributes().get(keyAux));
						achou=true;
						break;
					}
				}
				}
				if (achou){
					break;
				}
			}  
		
	}
	
	 public void updateMapAtribute(AtributeModel atributeVinculated){
	    	
		 	/**Toda vez deve atualizar que agora essa hipotese tem outro pai e o goal relacionado agora tem outra hipotese*/
		 	UMPSTProject.getInstance().getMapAtribute().get(atributeVinculated.getId()).getEntityRelated().add(entity);
			entity.getMapAtributes().put(atributeVinculated.getId(), atributeVinculated);
			
			if (atributeVinculated.getMapSubAtributes()!=null){
				 Set<String> keys = atributeVinculated.getMapSubAtributes().keySet();
		 		 TreeSet<String> sortedKeys = new TreeSet<String>(keys);	
		 		 AtributeModel atribute;
	 			for (String key: sortedKeys){
	 				atribute = atributeVinculated.getMapSubAtributes().get(key);
	 				
	    		 	UMPSTProject.getInstance().getMapAtribute().get(atribute.getId()).getEntityRelated().add(entity);
	 				entity.getMapAtributes().put(atribute.getId(),atribute);

	 			}

			}
			//PRECISO ATUALIZAR O GOAL RELATED DA HIPOTESE QUE ESTA NO MAPA GERAL
			
			UmpstModule pai = getFatherPanel();
		    changePanel(pai.getMenuPanel().getEntitiesPane().getEntitiesPanel().getEntitiesMainPanel(entity));    			
	}
	 
	public void createFrame(){
		
		JFrame frame = new JFrame("Geeeeeeeente");
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		
		String[] columnNames = {"ID","Goal",""};
    	
		dataFrame = new Object[UMPSTProject.getInstance().getMapGoal().size()][3];

	    
		Integer i=0;
	    
		Set<String> keys = UMPSTProject.getInstance().getMapGoal().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);
		
		for (String key: sortedKeys){
			dataFrame[i][0] = UMPSTProject.getInstance().getMapGoal().get(key).getId();
			dataFrame[i][1] = UMPSTProject.getInstance().getMapGoal().get(key).getGoalName();			
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
				list = entity.getBacktracking();
				
				listModel.addElement(key);
				list = new JList(listModel);
				entity.setBacktracking(list);
				
				Set<String> keys = UMPSTProject.getInstance().getMapGoal().keySet();
				TreeSet<String> sortedKeys = new TreeSet<String>(keys);
				for (String keyAux : sortedKeys){
					if (UMPSTProject.getInstance().getMapGoal().get(keyAux).getGoalName().equals(key)){
						UMPSTProject.getInstance().getMapGoal().get(keyAux).getFowardTrackingEntity().add(entity);
					}
				}
					
				
				
				UmpstModule pai = getFatherPanel();
			    changePanel(pai.getMenuPanel().getEntitiesPane().getEntitiesPanel().getEntitiesMainPanel(entity));
				
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
	
public void createFrameHypo(){
		
		JFrame frame = new JFrame("List of all Hypothesis");
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		
		String[] columnNames = {"Hypothesis",""};
    	
		dataFrameHypo = new Object[UMPSTProject.getInstance().getMapHypothesis().size()][2];

	    
		Integer i=0;
	    
		Set<String> keys = UMPSTProject.getInstance().getMapHypothesis().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);
		
		for (String KeyA : sortedKeys){
			listHypothesisModel.addElement(UMPSTProject.getInstance().getMapHypothesis().get(KeyA).getHypothesisName());
		}
		
		if (entity!=null){
			listHypothesis = entity.getBacktrackingHypothesis();
			for (int j = 0; j < listHypothesis.getModel().getSize();j++) {
				listHypothesisModel.addElement((listHypothesis.getModel().getElementAt(i)));
				if (listHypothesisModel.contains(listHypothesis.getModel().getElementAt(i))){
					listHypothesisModel.remove(listHypothesisModel.indexOf(listHypothesis.getModel().getElementAt(j)));
				}
			}
			
		}
		
		for (int n = 0; n < listHypothesisModel.getSize(); n++) {
			dataFrameHypo[n][0] = listHypothesisModel.getElementAt(n);
			dataFrameHypo[n][1] = "";
			
		}
		
		listHypothesisModel.clear();
		listHypothesis = null;
		
		/*
		for (String key: sortedKeys){
			dataFrameHypo[i][0] = UMPSTProject.getInstance().getMapHypothesis().get(key).getId();
			dataFrameHypo[i][1] = UMPSTProject.getInstance().getMapHypothesis().get(key).getHypothesisName();			
			dataFrameHypo[i][2] = "";
			i++;
		}*/
		
		
		
		DefaultTableModel model = new DefaultTableModel(dataFrameHypo,columnNames);
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
				
				String key = dataFrameHypo[row][0].toString();
				listHypothesis = entity.getBacktrackingHypothesis();
				
				for (int j = 0; j < listHypothesis.getModel().getSize();j++) {
					listHypothesisModel.addElement((listHypothesis.getModel().getElementAt(j)));
				}
				
				//listHypothesisModel = listHypothesis.getModel();
				listHypothesisModel.addElement(key);
				listHypothesis = new JList(listHypothesisModel);
				entity.setBacktrackingHypothesis(listHypothesis);
				//updateBacktracking(entity);
				
				Set<String> keys = UMPSTProject.getInstance().getMapHypothesis().keySet();
				TreeSet<String> sortedKeys = new TreeSet<String>(keys);
				for (String keyAux : sortedKeys){
					if (UMPSTProject.getInstance().getMapHypothesis().get(keyAux).getHypothesisName().equals(key)){
						UMPSTProject.getInstance().getMapHypothesis().get(keyAux).getFowardTrackingEntity().add(entity);
					}
				}
				
				UmpstModule pai = getFatherPanel();
			    changePanel(pai.getMenuPanel().getEntitiesPane().getEntitiesPanel().getEntitiesMainPanel(entity));
				
			    

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