package unbbayes.gui.umpst.entity;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ResourceBundle;
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
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import unbbayes.controller.umpst.IconController;
import unbbayes.gui.umpst.IUMPSTPanel;
import unbbayes.gui.umpst.MainPanel;
import unbbayes.gui.umpst.MainPropertiesEditionPane;
import unbbayes.gui.umpst.TableButton;
import unbbayes.gui.umpst.UmpstModule;
import unbbayes.gui.umpst.TableButton.TableButtonCustomizer;
import unbbayes.gui.umpst.TableButton.TableButtonPressedHandler;
import unbbayes.model.umpst.entities.AttributeModel;
import unbbayes.model.umpst.entities.EntityModel;
import unbbayes.model.umpst.entities.RelationshipModel;
import unbbayes.model.umpst.project.SearchModelEntity;
import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.model.umpst.rules.RulesModel;
import unbbayes.prs.bn.cpt.impl.resources.Resources;
import unbbayes.util.CommonDataUtil;


/**
 * Panel for Entities Edition
 * 
 * @author Laecio Santos (laecio@gmail.com)
 * @author Rafael Mezzomo 
 * @author Shou Matusumoto
 *
 */
public class EntitiesEditionPanel extends IUMPSTPanel {
	
	private static final long serialVersionUID = 1L;
	
	private Object[][] dataFrame ;
	private Object[][] dataFrameHypo ;
	private Object[][] dataBacktracking ;
	private Object[][] dataBacktrackingHypo ;
	
	private JComboBox atributeVinculationList = new JComboBox();
	
	private GridBagConstraints constraint     = new GridBagConstraints();

	
	private JButton buttonAdd 	     = new JButton();
	private JButton buttonCancel     = new JButton("Cancel");
	private JButton buttonAtribute = new JButton("atribute");
	private JButton buttonFrame	= new JButton ("backtracking goal");
	private JButton buttonFrameHypo = new JButton("backtracking hypothesis");
	
	private EntityModel entity;
	
	private MainPropertiesEditionPane mainPropertiesEditionPane ; 
	
	private JList list,listAux; 
    private DefaultListModel listModel = new DefaultListModel();
	private DefaultListModel listModelAux = new DefaultListModel();
	
	private JList listHypothesis,listHypothesisAux; 
    private DefaultListModel listHypothesisModel = new DefaultListModel();
	private DefaultListModel listHypothesisModelAux = new DefaultListModel();
	
	/** Load resource file from this package */
  	private static ResourceBundle resource = 
  			unbbayes.util.ResourceController.newInstance().getBundle(
  			unbbayes.gui.umpst.resources.Resources.class.getName());
	
	public EntitiesEditionPanel(UmpstModule _fatherModule,
			UMPSTProject _umpstProject, 
			EntityModel _entity){
		
		super(_fatherModule);
		
		this.setUmpstProject(_umpstProject);
		
		this.entity = _entity;
		
		this.setLayout(new GridLayout(1,1));
		
		createPanelText();
		
		JSplitPane leftPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				createPanelText(),
				createBacktrackingPanel()); 

		JSplitPane rightPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				createTraceabilityTable(),
				createAtributeTable()); 


		JSplitPane mainPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				leftPane,
				rightPane);

		this.add(mainPane);

		leftPane.setDividerLocation(280); 
		rightPane.setDividerLocation(280); 
		mainPane.setDividerLocation(500); 
	
		
		createListeners();

		if( _entity == null){
			buttonAdd.setText(" Add ");
		} else {
			buttonAdd.setText(" Update ");
			mainPropertiesEditionPane.setTitleText(_entity.getEntityName());
			mainPropertiesEditionPane.setCommentsText(_entity.getComments());
			mainPropertiesEditionPane.setAuthorText(_entity.getAuthor());
			mainPropertiesEditionPane.setDateText(_entity.getDate());
		}
		
	}

	public JSplitPane createBacktrackingPanel(){

		JSplitPane panel = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				getBacktrackingPanel(),
				getBacktrackingHypothesis());
		
		return panel; 
		
	}
	
	public JPanel createPanelText(){
		
		JPanel panel = new JPanel();
		
		String title            = resource.getString("ttEntity");
		
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		mainPropertiesEditionPane = 
				new MainPropertiesEditionPane(buttonCancel, 
						buttonAdd, 
						title, 
						"Goals Details",
						null,
						null); 

		if (entity != null){
			mainPropertiesEditionPane.setTitleText(entity.getEntityName());
			mainPropertiesEditionPane.setCommentsText(entity.getComments());
			mainPropertiesEditionPane.setAuthorText(entity.getAuthor());
			mainPropertiesEditionPane.setDateText(entity.getDate());
		}

		return mainPropertiesEditionPane.getPanel(); 
	
	}
	
	
	public void createListeners(){
		
		buttonAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( entity == null){
					try {
						
						if (mainPropertiesEditionPane.getTitleText().equals("")){
							JOptionPane.showMessageDialog(null, resource.getString("erEntityDescriptionEmpty"));
						}
						else{
							EntityModel entityAdd = updateMaEntity();					    
						    updateMapSearch(entityAdd);
						    //updateBacktracking(entityAdd);
							updateTableEntities();
							
							UmpstModule pai = getFatherPanel();
							changePanel(new EntitiesEditionPanel(getFatherPanel(),getUmpstProject(),entityAdd));	
						}
					
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(null, "Error while creating entity", "UnBBayes", JOptionPane.WARNING_MESSAGE);
						UmpstModule pai = getFatherPanel();
						changePanel(pai.getMenuPanel());	
						e1.printStackTrace();
					
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
					    		if(getUmpstProject().getMapSearchEntity().get(strAux[i])!=null){
					    			getUmpstProject().getMapSearchEntity().get(strAux[i]).getEntitiesRelated().remove(entity);
					    			aux = getUmpstProject().getMapSearchEntity().get(strAux[i]).getEntitiesRelated();
					    	    	for (Iterator<EntityModel> it = aux.iterator(); it.hasNext(); ) {
					    	    		entityBeta = it.next();
					    	   		}
					    		}
						    }
						    
						    
						    /************/
							
							entity.setEntityName(mainPropertiesEditionPane.getTitleText());
							entity.setComments(mainPropertiesEditionPane.getCommentsText());
							entity.setAuthor(mainPropertiesEditionPane.getAuthorText());
							entity.setDate(mainPropertiesEditionPane.getDateText());
							
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
				addVinculateAtribute((String) atributeVinculationList.getSelectedItem());
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
				changePanel(new AtributeEditionPanel(getFatherPanel(),getUmpstProject(), entity, null, null));

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

    	int tamanho = getUmpstProject().getMapEntity().size()+1;

    	if ( getUmpstProject().getMapEntity().size()!=0){
    		idAux = tamanho+"";
    	}
    	else{
    		idAux = "1";
    	}


    	EntityModel newEntity = new EntityModel(
    			idAux,
    			mainPropertiesEditionPane.getTitleText(),
    			mainPropertiesEditionPane.getCommentsText(), 
    			mainPropertiesEditionPane.getAuthorText(), 
    			mainPropertiesEditionPane.getDateText());


    	getUmpstProject().getMapEntity().put(newEntity.getId(), newEntity);	

    	CommonDataUtil.getInstance().setAuthorName(mainPropertiesEditionPane.getAuthorText()); 

    	return newEntity;
    }
    
    
    public void updateTableEntities(){
    	String[] columnNames = {"ID","Entity","",""};	    
	    
		Object[][] data = new Object[getUmpstProject().getMapEntity().size()][4];
		Integer i=0;
	    
		Set<String> keys = getUmpstProject().getMapEntity().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);
		
		for (String key: sortedKeys){
			data[i][0] = getUmpstProject().getMapEntity().get(key).getId();
			data[i][1] = getUmpstProject().getMapEntity().get(key).getEntityName();			
			data[i][2] = "";
			data[i][3] = "";
			i++;
		}
   
	    UmpstModule pai = getFatherPanel();
	    changePanel(pai.getMenuPanel());
	    
	    EntitiesTable entitiesTable = pai.getMenuPanel().getEntitiesPane().getEntitiesTable();
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
	    		if(getUmpstProject().getMapSearchEntity().get(strAux[i])==null){
	    			entitySetSearch.add(entityAdd);
	    			SearchModelEntity searchModel = new SearchModelEntity(strAux[i], entitySetSearch);
	    			getUmpstProject().getMapSearchEntity().put(searchModel.getKeyWord(), searchModel);
	    		}
	    		else{
	    			getUmpstProject().getMapSearchEntity().get(strAux[i]).getEntitiesRelated().add(entityAdd);
	    		}
	    	}
	    }
	    
		/************/		    

    }


	public JPanel getBacktrackingPanel(){
				
		JPanel panel = new JPanel();
	
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
					button.setIcon(IconController.getInstance().getDeleteIcon() );

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
				    changePanel(pai.getMenuPanel().getEntitiesPane().getEntitiesPanel().getEntitiesPanel(entity));
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
					button.setIcon(IconController.getInstance().getDeleteIcon() );

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
				    changePanel(pai.getMenuPanel().getEntitiesPane().getEntitiesPanel().getEntitiesPanel(entity));
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
	
	
	
	
	/**public void updateBacktracking(EntityModel entity){
		String keyWord = "";
		Set<String> keys = getUmpstProject().getMapGoal().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);
		
		
		
		if (list !=null){
			for (int i = 0; i < list.getModel().getSize();i++) {
				keyWord = list.getModel().getElementAt(i).toString();
				for (String key: sortedKeys){
					if (keyWord.equals( getUmpstProject().getMapGoal().get(key).getGoalName()) ){
						getUmpstProject().getMapGoal().get(key).getFowardTrackingEntity().add(entity);
					}			
				
				}
			}
			entity.setBacktracking(list);

		}
		
		if (listHypothesis !=null){
			for (int i = 0; i < listHypothesis.getModel().getSize();i++) {
				keyWord = listHypothesis.getModel().getElementAt(i).toString();
				for (String key: sortedKeys){
					if ( getUmpstProject().getMapHypothesis().get(key)!=null){
						if (keyWord.equals( getUmpstProject().getMapHypothesis().get(key).getHypothesisName()) ){
							getUmpstProject().getMapHypothesis().get(key).getFowardTrackingEntity().add(entity);
						}	
					}
				
				}
			}
			entity.setBacktrackingHypothesis(listHypothesis);

		}
		
	}*/
	
	public JPanel createAtributeTable(){
    	
	    TableAtribute atributesTable = new TableAtribute(getFatherPanel(),getUmpstProject(),entity);
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

	   
	    return panel; 

    }
	
	
	public JScrollPane createTraceabilityTable(){
		
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
		
		if (i < 30){
			data = new Object[30][3];
		}
		
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
		table.setGridColor(Color.WHITE); 
		table.setEnabled(false); 
		
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setBorder(BorderFactory.createTitledBorder("This entity traceability"));
		
		return scrollPane;
		
	}
	
	
 	
	public JComboBox vinculateAtribute(){

	    Set<String> keys = getUmpstProject().getMapEntity().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);	
		
		Set<String> keysAtribute;
		TreeSet<String> sortedKeysAtribute;
		EntityModel entityAux;
		int i=0;
		/**This is only to found the number of other hypothesis existents in order to create 
		 *     	    String[] allOtherHypothesis = new String[i];
		 * */
		for (String key: sortedKeys){
			if(getUmpstProject().getMapEntity().get(key)!=entity){
				if(getUmpstProject().getMapEntity().get(key).getMapAtributes()!=null){
					
					entityAux = getUmpstProject().getMapEntity().get(key);
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
				if(getUmpstProject().getMapEntity().get(key)!=entity){
					if(getUmpstProject().getMapEntity().get(key).getMapAtributes()!=null){
						
						entityAux = getUmpstProject().getMapEntity().get(key);
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
	
	public void addVinculateAtribute(String atributeRelated){
		
		 Set<String> keys = getUmpstProject().getMapEntity().keySet();
			TreeSet<String> sortedKeys = new TreeSet<String>(keys);	
			
			Set<String> keysAtribute;
			TreeSet<String> sortedKeysAtribute;
			int i=0;
			Boolean achou = false;
		
			for (String key: sortedKeys){
				if(getUmpstProject().getMapEntity().get(key).getMapAtributes()!=null){	
				keysAtribute = getUmpstProject().getMapEntity().get(key).getMapAtributes().keySet();
				sortedKeysAtribute = new TreeSet<String>(keysAtribute);
				for(String keyAux : sortedKeysAtribute){
					if (getUmpstProject().getMapEntity().get(key).getMapAtributes().get(keyAux).getAtributeName().equals(atributeRelated)){
						updateMapAtribute(getUmpstProject().getMapEntity().get(key).getMapAtributes().get(keyAux));
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
	
	 public void updateMapAtribute(AttributeModel atributeVinculated){
	    	
		 	/**Toda vez deve atualizar que agora essa hipotese tem outro pai e o goal relacionado agora tem outra hipotese*/
		 	getUmpstProject().getMapAtribute().get(atributeVinculated.getId()).getEntityRelated().add(entity);
			entity.getMapAtributes().put(atributeVinculated.getId(), atributeVinculated);
			
			if (atributeVinculated.getMapSubAtributes()!=null){
				 Set<String> keys = atributeVinculated.getMapSubAtributes().keySet();
		 		 TreeSet<String> sortedKeys = new TreeSet<String>(keys);	
		 		 AttributeModel atribute;
	 			for (String key: sortedKeys){
	 				atribute = atributeVinculated.getMapSubAtributes().get(key);
	 				
	    		 	getUmpstProject().getMapAtribute().get(atribute.getId()).getEntityRelated().add(entity);
	 				entity.getMapAtributes().put(atribute.getId(),atribute);

	 			}

			}
			//PRECISO ATUALIZAR O GOAL RELATED DA HIPOTESE QUE ESTA NO MAPA GERAL
			
			UmpstModule pai = getFatherPanel();
		    changePanel(pai.getMenuPanel().getEntitiesPane().getEntitiesPanel().getEntitiesPanel(entity));    			
	}
	 
	public void createFrame(){
		
		JFrame frame = new JFrame("Adding Backtracking from goals");
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		
		String[] columnNames = {"ID","Goal",""};
    	
		dataFrame = new Object[getUmpstProject().getMapGoal().size()][3];

	    
		Integer i=0;
	    
		Set<String> keys = getUmpstProject().getMapGoal().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);
		
		for (String key: sortedKeys){
			dataFrame[i][0] = getUmpstProject().getMapGoal().get(key).getId();
			dataFrame[i][1] = getUmpstProject().getMapGoal().get(key).getGoalName();			
			dataFrame[i][2] = "";
			i++;
		}
		
		
		
		DefaultTableModel model = new DefaultTableModel(dataFrame,columnNames);
		JTable table = new JTable(model);
		
		TableButton buttonEdit = new TableButton( new TableButton.TableButtonCustomizer()
		{
			public void customize(JButton button, int row, int column)
			{
				button.setIcon(IconController.getInstance().getEditIcon());

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
				
				Set<String> keys = getUmpstProject().getMapGoal().keySet();
				TreeSet<String> sortedKeys = new TreeSet<String>(keys);
				for (String keyAux : sortedKeys){
					if (getUmpstProject().getMapGoal().get(keyAux).getGoalName().equals(key)){
						getUmpstProject().getMapGoal().get(keyAux).getFowardTrackingEntity().add(entity);
					}
				}
					
				
				
				UmpstModule pai = getFatherPanel();
			    changePanel(pai.getMenuPanel().getEntitiesPane().getEntitiesPanel().getEntitiesPanel(entity));
				
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
		
		JFrame frame = new JFrame("Adding backtracking from hypothesis");
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		
		String[] columnNames = {"Hypothesis",""};
    	
		dataFrameHypo = new Object[getUmpstProject().getMapHypothesis().size()][2];

	    
		Integer i=0;
	    
		Set<String> keys = getUmpstProject().getMapHypothesis().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);
		
		/**for (String KeyA : sortedKeys){
			listHypothesisModel.addElement(getUmpstProject().getMapHypothesis().get(KeyA).getHypothesisName());
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
			
		}*/
		

		
		for (String key: sortedKeys){
			dataFrameHypo[i][0] = getUmpstProject().getMapHypothesis().get(key).getHypothesisName();			
			dataFrameHypo[i][1] = "";
			i++;
		}
		
		
		
		DefaultTableModel model = new DefaultTableModel(dataFrameHypo,columnNames);
		JTable table = new JTable(model);
		
		TableButton buttonEdit = new TableButton( new TableButton.TableButtonCustomizer()
		{
			public void customize(JButton button, int row, int column)
			{
				button.setIcon(IconController.getInstance().getAddIcon());

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
				
				listHypothesisModel.addElement(key);
				listHypothesis = new JList(listHypothesisModel);
				entity.setBacktrackingHypothesis(listHypothesis);
				
				Set<String> keys = getUmpstProject().getMapHypothesis().keySet();
				TreeSet<String> sortedKeys = new TreeSet<String>(keys);
				for (String keyAux : sortedKeys){
					if (getUmpstProject().getMapHypothesis().get(keyAux).getHypothesisName().equals(key)){
						getUmpstProject().getMapHypothesis().get(keyAux).getFowardTrackingEntity().add(entity);
					}
				}
				
				/**listHypothesisModel.clear();
				listHypothesis = entity.getBacktrackingHypothesis();
				for (int j = 0; j < listHypothesis.getModel().getSize();j++) {
					listHypothesisModel.addElement((listHypothesis.getModel().getElementAt(j)));
				}
				
				listHypothesisModel.addElement(key);

				listHypothesis = new JList(listHypothesisModel);
				entity.setBacktrackingHypothesis(listHypothesis);
				
				Set<String> keys = getUmpstProject().getMapHypothesis().keySet();
				TreeSet<String> sortedKeys = new TreeSet<String>(keys);
				for (String keyAux : sortedKeys){
					if (getUmpstProject().getMapHypothesis().get(keyAux).getHypothesisName().equals(key)){
						getUmpstProject().getMapHypothesis().get(keyAux).getFowardTrackingEntity().add(entity);
					}
				}*/
				
				UmpstModule pai = getFatherPanel();
			    changePanel(pai.getMenuPanel().getEntitiesPane().getEntitiesPanel().getEntitiesPanel(entity));
				
			    

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