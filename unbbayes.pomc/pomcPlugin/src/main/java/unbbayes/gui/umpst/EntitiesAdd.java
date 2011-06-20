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
import java.util.Set;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
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
import javax.swing.table.DefaultTableModel;

import unbbayes.model.umpst.entities.EntityModel;
import unbbayes.model.umpst.entities.RelationshipModel;
import unbbayes.model.umpst.project.SearchModelEntity;
import unbbayes.model.umpst.project.SearchModelGoal;
import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.model.umpst.requirements.GoalModel;
import unbbayes.model.umpst.rules.RulesModel;


public class EntitiesAdd extends IUMPSTPanel {
	
	private ImageIcon iconAtribute = createImageIcon("images/hypo.png");

	
	private GridBagConstraints constraint     = new GridBagConstraints();
	private JLabel titulo            = new JLabel();
	
	private JButton buttonAdd 	     = new JButton();
	private JButton buttonCancel     = new JButton("Cancel");
	private JButton buttonAtribute = new JButton("atribute");
 
	
	private JTextField dateText,authorText;
	private JTextField entityText;
	private JTextArea commentsText;
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
		constraint.gridx=0;constraint.gridy=0;constraint.weightx=0.5;constraint.weighty=0.5;
		panelText();
		
		
		
		GridBagConstraints c     = new GridBagConstraints();
		JPanel panelBacktracking = new JPanel();
		panelBacktracking.setLayout(new GridBagLayout());
		c.fill = GridBagConstraints.BOTH;
		c.gridx=0;c.gridy=0;c.weightx=0.5;c.weighty=0.5;
		panelBacktracking.add(getBacktrackingPanel(),c);
		c.gridx=0;c.gridy=1;c.weightx=0.5;c.weighty=0.5;
		panelBacktracking.add(getBackrackingPanelHypothesis(),c);
		
		constraint.gridx=0;constraint.gridy=1;constraint.weightx=0.5;constraint.weighty=0.5;
		add(panelBacktracking,constraint);
		
		
		
		constraint.gridx=1;constraint.gridy=1;constraint.weightx=0.5;constraint.weighty=0.5;
		createAtributeTable();
		constraint.gridx=1;constraint.gridy=0;constraint.weightx=0.5;constraint.weighty=0.5;
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
		c.gridx = 0; c.gridy = 5;c.gridwidth = 1;
		panel.add( new JLabel("Comments: "), c);
		c.gridx = 0; c.gridy = 3;c.gridwidth = 1;
		panel.add( new JLabel("Author Name: "), c);
		c.gridx = 0; c.gridy = 4;c.gridwidth = 1;
		panel.add( new JLabel("Date: "), c);
		

		GridBagConstraints d = new GridBagConstraints();
		d.gridx = 0; d.gridy = 0;
		d.fill = GridBagConstraints.PAGE_START;
		d.gridwidth = 3;
		d.insets = new Insets(0, 0, 0, 0);
		titulo.setFont(new Font("Arial", Font.BOLD, 32));
		titulo.setBackground(new Color(0x4169AA));
		panel.add( titulo, d);
				
		entityText = new JTextField(20);
		commentsText = new JTextArea(5,21);
		authorText = new JTextField(20);
		dateText = new JTextField(20);
 

		c.gridx = 1; c.gridy = 2;c.gridwidth = 2;
		panel.add( entityText, c);
		
		c.gridx = 1; c.gridy = 3;c.gridwidth = 2;
		panel.add( commentsText, c);
		
		c.gridx = 1; c.gridy = 4;c.gridwidth = 2;
		panel.add( authorText, c);
		
		c.gridx = 1; c.gridy = 5;c.gridwidth = 2;
		panel.add( dateText, c);
		
		Box box = Box.createHorizontalBox();
		box.add(buttonCancel);
		//box.add(buttonAtribute);
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
						    updateBacktracking(entityAdd);
							updateTableEntities();
							JOptionPane.showMessageDialog(null, "entity successfully added",null, JOptionPane.INFORMATION_MESSAGE);
						}
					
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(null, "Error while creating entity", "UnBBayes", JOptionPane.WARNING_MESSAGE);
						UmpstModule pai = getFatherPanel();
						alterarJanelaAtual(pai.getMenuPanel());	
					
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
							updateBacktracking(entity);
							updateTableEntities();
							
							JOptionPane.showMessageDialog(null, "entity successfully updated", "UnBBayes", JOptionPane.INFORMATION_MESSAGE);
						
							
						}
						catch (Exception e2) {
							JOptionPane.showMessageDialog(null,"Error while ulpating entity", "UnBBayes", JOptionPane.WARNING_MESSAGE);
							UmpstModule pai = getFatherPanel();
							alterarJanelaAtual(pai.getMenuPanel());	
						}
					}
				}
			}
		});

		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				UmpstModule pai = getFatherPanel();
				alterarJanelaAtual(pai.getMenuPanel());	
			}
		});
		
		buttonAtribute.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				alterarJanelaAtual(new AtributeAdd(getFatherPanel(), entity, null, null));

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
        java.net.URL imgURL = MenuPanel.class.getResource(path);
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
				dateText.getText(),null,null,null,null,null,null,null);
		
		
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
	    alterarJanelaAtual(pai.getMenuPanel());
	    
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


	public Box getBacktrackingPanel(){
		JButton buttonCopy, buttonDelete;

		Box box = Box.createHorizontalBox();

	
		
		Set<String> keys = UMPSTProject.getInstance().getMapGoal().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);
		
		for (String key: sortedKeys){
			listModel.addElement(UMPSTProject.getInstance().getMapGoal().get(key).getGoalName());
		}
		
		
		/**This IF is responsable to update the first JList with all requirements elemente MINUS those 
		 * who are already registred as backtracking.
		 * */
		if (entity!=null){
			listAux = entity.getBacktracking();
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
		listScroller.setMinimumSize(new Dimension(100,150));
				
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
		listScrollerAux.setMinimumSize(new Dimension(100,150));
		box.add(listScrollerAux);
				
		box.setBorder(BorderFactory.createTitledBorder("Adding backtracking from Goals"));
		
		return box;
		//add(box,constraint);
		
	}
	
	public  Box getBackrackingPanelHypothesis(){
		Box box = Box.createHorizontalBox();
		JButton buttonCopy, buttonDelete;

		
		Set<String> keys = UMPSTProject.getInstance().getMapHypothesis().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);
		
		for (String key: sortedKeys){
			listHypothesisModel.addElement(UMPSTProject.getInstance().getMapHypothesis().get(key).getHypothesisName());
		}
		
		
		/**This IF is responsable to update the first JList with all requirements elemente MINUS those 
		 * who are already registred as backtracking.
		 * */
		if (entity!=null){
			listHypothesisAux = entity.getBacktrackingHypothesis();
			for (int i = 0; i < listHypothesisAux.getModel().getSize();i++) {
				listHypothesisModelAux.addElement((listHypothesisAux.getModel().getElementAt(i)));
				if (listHypothesisModel.contains(listHypothesisAux.getModel().getElementAt(i))){
					listHypothesisModel.remove(listHypothesisModel.indexOf(listHypothesisAux.getModel().getElementAt(i)));
				}
			}
			
		}
		
		listHypothesis = new JList(listHypothesisModel); //data has type Object[]
		listHypothesis.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		listHypothesis.setLayoutOrientation(JList.VERTICAL_WRAP);
		listHypothesis.setVisibleRowCount(-1);
		
		
		
		

		JScrollPane listHypothesisScroller = new JScrollPane(listHypothesis);
		listHypothesisScroller.setMinimumSize(new Dimension(100,150));
				
		box.add(listHypothesisScroller);
	
	
		
		buttonCopy = new JButton("copy >>");
		box.add(buttonCopy);
		buttonCopy.addActionListener(
				new ActionListener() {
					
					public void actionPerformed(ActionEvent e) {
						listHypothesisModelAux.addElement(listHypothesis.getSelectedValue());	
						listHypothesisModel.removeElement(listHypothesis.getSelectedValue());

					}
				}
		
		);
		
		buttonDelete = new JButton("<< delete");
		box.add(buttonDelete);
		buttonDelete.addActionListener(
				new ActionListener() {
					
					public void actionPerformed(ActionEvent e) {
						listHypothesisModel.addElement(listHypothesisAux.getSelectedValue());	
						listHypothesisModelAux.removeElement(listHypothesisAux.getSelectedValue());
					}
				}
		
		);	
		
		listHypothesisAux = new JList(listHypothesisModelAux);

		listHypothesisAux.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		listHypothesisAux.setLayoutOrientation(JList.VERTICAL_WRAP);
		listHypothesisAux.setVisibleRowCount(-1);
	
		JScrollPane listHypothesisScrollerAux = new JScrollPane(listHypothesisAux);
		listHypothesisScrollerAux.setMinimumSize(new Dimension(100,150));
		box.add(listHypothesisScrollerAux);
				
		box.setBorder(BorderFactory.createTitledBorder("Adding backtracking from Hypothesis"));
		
		return box;
		//add(box,constraint);
		
	}
	
	
	
	public void updateBacktracking(EntityModel entity){
		String keyWord = "";
		Set<String> keys = UMPSTProject.getInstance().getMapGoal().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);
		
		
		
		if (listAux !=null){
			for (int i = 0; i < listAux.getModel().getSize();i++) {
				keyWord = listAux.getModel().getElementAt(i).toString();
				for (String key: sortedKeys){
					if (keyWord.equals( UMPSTProject.getInstance().getMapGoal().get(key).getGoalName()) ){
						UMPSTProject.getInstance().getMapGoal().get(key).getFowardTrackingEntity().add(entity);
					}			
				
				}
			}
			entity.setBacktracking(listAux);

		}
		
		if (listHypothesisAux !=null){
			for (int i = 0; i < listHypothesisAux.getModel().getSize();i++) {
				keyWord = listHypothesisAux.getModel().getElementAt(i).toString();
				for (String key: sortedKeys){
					if ( UMPSTProject.getInstance().getMapHypothesis().get(key)!=null){
						if (keyWord.equals( UMPSTProject.getInstance().getMapHypothesis().get(key).getHypothesisName()) ){
							UMPSTProject.getInstance().getMapHypothesis().get(key).getFowardTrackingEntity().add(entity);
						}	
					}
				
				}
			}
			entity.setBacktrackingHypothesis(listHypothesisAux);

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
	
	
}