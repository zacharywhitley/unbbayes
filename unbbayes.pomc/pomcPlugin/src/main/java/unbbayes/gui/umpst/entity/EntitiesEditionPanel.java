package unbbayes.gui.umpst.entity;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import unbbayes.controller.umpst.Controller;
import unbbayes.controller.umpst.IconController;
import unbbayes.gui.umpst.IUMPSTPanel;
import unbbayes.gui.umpst.MainPanel;
import unbbayes.gui.umpst.MainPropertiesEditionPane;
import unbbayes.gui.umpst.TableButton;
import unbbayes.gui.umpst.UmpstModule;
import unbbayes.gui.umpst.selection.AttributeSelectionPane;
import unbbayes.model.umpst.entities.AttributeModel;
import unbbayes.model.umpst.entities.EntityModel;
import unbbayes.model.umpst.entities.RelationshipModel;
import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.model.umpst.requirements.GoalModel;
import unbbayes.model.umpst.requirements.HypothesisModel;
import unbbayes.model.umpst.rules.RuleModel;


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

	private GridBagConstraints constraint     = 
			new GridBagConstraints();

	private JButton buttonSave;
	private JButton buttonCancel;
	private JButton buttonAttribute;
	private JButton buttonFrameGoal;
	private JButton buttonFrameHypothesis;
	private JButton buttonReuseAttribute; 

	private EntityModel entity;

	private MainPropertiesEditionPane mainPropertiesEditionPane ; 

	private JList list; 
	private JList listAux;
	
	private DefaultListModel listModel = new DefaultListModel();
	private DefaultListModel listModelAux = new DefaultListModel();

	private JList listHypothesis,listHypothesisAux; 
	private DefaultListModel listHypothesisModel = new DefaultListModel();
	private DefaultListModel listHypothesisModelAux = new DefaultListModel();
	
	private Controller controller; 
	
	UmpstModule fatherModule; 

	/** Load resource file from this package */
	private static ResourceBundle resource = 
			unbbayes.util.ResourceController.newInstance().getBundle(
					unbbayes.gui.umpst.resources.Resources.class.getName());

	private IconController iconController = IconController.getInstance();

	public EntitiesEditionPanel(UmpstModule _fatherModule,
			UMPSTProject _umpstProject, 
			EntityModel _entity){

		super(_fatherModule);
		
		fatherModule = _fatherModule; 
		
		controller = Controller.getInstance(_umpstProject); 

		this.setUmpstProject(_umpstProject);

		this.entity = _entity;

		this.setLayout(new GridLayout(1,1));

		createButtons(); 

		createPanelText();

		JSplitPane leftPane = new JSplitPane(
				JSplitPane.VERTICAL_SPLIT,
				createPanelText(),
				createBacktrackingPanel()); 

		JSplitPane rightPane = new JSplitPane(
				JSplitPane.VERTICAL_SPLIT,
				createTraceabilityTable(),
				createAtributeTable()); 


		JSplitPane mainPane = new JSplitPane(
				JSplitPane.HORIZONTAL_SPLIT,
				leftPane,
				rightPane);

		this.add(mainPane);

		leftPane.setDividerLocation(280); 
		rightPane.setDividerLocation(280); 
		mainPane.setDividerLocation(500); 

		createListeners();
	}

	public JSplitPane createBacktrackingPanel(){

		JSplitPane panel = new JSplitPane(
				JSplitPane.VERTICAL_SPLIT,
				createBacktrackingGoalPanel(),
				createBacktrackingHypothesis());

		return panel; 

	}

	public JPanel createPanelText(){

		JPanel panel = new JPanel();

		String title            = resource.getString("ttEntity");

		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		mainPropertiesEditionPane = 
				new MainPropertiesEditionPane(
						buttonCancel, 
						buttonSave, 
						title, 
						"Goals Details",
						null,
						null); 

		if (entity != null){
			mainPropertiesEditionPane.setTitleText(entity.getName());
			mainPropertiesEditionPane.setCommentsText(entity.getComments());
			mainPropertiesEditionPane.setAuthorText(entity.getAuthor());
			mainPropertiesEditionPane.setDateText(entity.getDate());
		}

		return mainPropertiesEditionPane.getPanel(); 

	}

	public void createButtons(){

		buttonSave 	    = new JButton(iconController.getSaveObjectIcon());

		buttonSave.setText(resource.getString("btnSave"));

		if( entity == null){
			buttonSave.setToolTipText(resource.getString("hpSaveEntity"));

		} else {
			buttonSave.setToolTipText(resource.getString("hpUpdateEntity"));
		}

		buttonCancel     = new JButton(iconController.getReturnIcon());
		buttonCancel.setText(resource.getString("btnReturn")); 
		buttonCancel.setToolTipText(resource.getString("hpReturnMainPanel"));

		buttonAttribute = new JButton(iconController.getListAddIcon());
		buttonAttribute.setToolTipText(resource.getString("hpAddAttribute")); 

		buttonReuseAttribute = new JButton(iconController.getReuseAttributeIcon());
		buttonReuseAttribute.setToolTipText(resource.getString("hpReuseAttribute"));
		
		buttonFrameGoal	= new JButton (iconController.getCicleGoalIcon());
		buttonFrameHypothesis	= new JButton (iconController.getCicleHypothesisIcon());

	}

	public void createListeners(){

		buttonSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				//Creating new entity
				if( entity == null){
					try {

						if (mainPropertiesEditionPane.getTitleText().equals("")){
							JOptionPane.showMessageDialog(null, resource.getString("erEntityDescriptionEmpty"));
						}
						else{
							
							EntityModel newEntity = controller.createNewEntity(
									mainPropertiesEditionPane.getTitleText(), 
									mainPropertiesEditionPane.getCommentsText(), 
									mainPropertiesEditionPane.getAuthorText(), 
									mainPropertiesEditionPane.getDateText()); 
		
//							updateMapSearch(entityAdd);
							//updateBacktracking(entityAdd);
							updateTableEntities();

							changePanel(new EntitiesEditionPanel(getFatherPanel(),getUmpstProject(),newEntity));	
						}

					} catch (Exception e1) {
						JOptionPane.showMessageDialog(null, 
								"Error while creating entity", 
								"UnBBayes", 
								JOptionPane.WARNING_MESSAGE);
						UmpstModule pai = getFatherPanel();
						changePanel(pai.getMenuPanel());	
						e1.printStackTrace();

					}
				}
				
				//Changing old entity
				else{
					if( JOptionPane.showConfirmDialog(null, 
							"Do you want to update this entity?", 
							"UnBBayes", 
							JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION ){
						
						try{

							/**Cleaning Search Map*/
//							Set<EntityModel> aux = new HashSet<EntityModel>();
//							EntityModel entityBeta;
//							String[] strAux = entity.getEntityName().split(" ");
//
//							for (int i = 0; i < strAux.length; i++) {
//								if(getUmpstProject().getMapSearchEntity().get(strAux[i])!=null){
//									getUmpstProject().getMapSearchEntity().get(strAux[i]).getEntitiesRelated().remove(entity);
//									aux = getUmpstProject().getMapSearchEntity().get(strAux[i]).getEntitiesRelated();
//									for (Iterator<EntityModel> it = aux.iterator(); it.hasNext(); ) {
//										entityBeta = it.next();
//									}
//								}
//							}


							/************/

							entity.setName(mainPropertiesEditionPane.getTitleText());
							entity.setComments(mainPropertiesEditionPane.getCommentsText());
							entity.setAuthor(mainPropertiesEditionPane.getAuthorText());
							entity.setDate(mainPropertiesEditionPane.getDateText());

							updateTableEntities();

							JOptionPane.showMessageDialog(null, 
									"entity successfully updated", 
									"UnBBayes", 
									JOptionPane.INFORMATION_MESSAGE);


						}
						catch (Exception e2) {
							JOptionPane.showMessageDialog(null,
									"Error while ulpating entity", 
									"UnBBayes", 
									JOptionPane.WARNING_MESSAGE);
							UmpstModule pai = getFatherPanel();
							changePanel(pai.getMenuPanel());	
						}
					}
				}
			}
		});

		buttonFrameHypothesis.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				createFrameHypo();

			}
		});

		buttonFrameGoal.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				createFrameGoal();				
			}
		});

		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				UmpstModule pai = getFatherPanel();
				changePanel(pai.getMenuPanel());	
			}
		});

		buttonAttribute.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				changePanel(new AtributeEditionPanel(getFatherPanel(),getUmpstProject(), entity, null, null));

			}
		});
		
		buttonReuseAttribute.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createAttributeSelectionPanel(); 

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
	
	public void createAttributeSelectionPanel(){
		AttributeSelectionPane hypothesisSelectionPane = new AttributeSelectionPane(vinculateAtribute(), this); 
		hypothesisSelectionPane.setLocationRelativeTo(fatherModule); 
		hypothesisSelectionPane.pack();
		hypothesisSelectionPane.setVisible(true);
	}
	
	public void updateTableEntities(){
		String[] columnNames = {"ID","Entity","",""};	    

		Object[][] data = new Object[getUmpstProject().getMapEntity().size()][4];
		Integer i=0;

		Set<String> keys = getUmpstProject().getMapEntity().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);

		for (String key: sortedKeys){
			data[i][0] = getUmpstProject().getMapEntity().get(key).getId();
			data[i][1] = getUmpstProject().getMapEntity().get(key).getName();			
			data[i][2] = "";
			data[i][3] = "";
			i++;
		}

		UmpstModule pai = getFatherPanel();
//		changePanel(pai.getMenuPanel());

		EntitiesTable entitiesTable = pai.getMenuPanel().getEntitiesPane().getEntitiesTable();
		JTable table = entitiesTable.createTable(columnNames,data);

		entitiesTable.getScrollPanePergunta().setViewportView(table);
		entitiesTable.getScrollPanePergunta().updateUI();
		entitiesTable.getScrollPanePergunta().repaint();
		entitiesTable.updateUI();
		entitiesTable.repaint();
	}

//	public void updateMapSearch(EntityModel entityAdd){
//		/**Upating searchPanel*/
//
//		String[] strAux = {};
//		strAux = entityAdd.getEntityName().split(" ");
//		Set<EntityModel> entitySetSearch = new HashSet<EntityModel>();
//
//		for (int i = 0; i < strAux.length; i++) {
//			if(!strAux[i].equals(" ")){
//				if(getUmpstProject().getMapSearchEntity().get(strAux[i])==null){
//					entitySetSearch.add(entityAdd);
//					SearchModelEntity searchModel = new SearchModelEntity(strAux[i], entitySetSearch);
//					getUmpstProject().getMapSearchEntity().put(searchModel.getKeyWord(), searchModel);
//				}
//				else{
//					getUmpstProject().getMapSearchEntity().get(strAux[i]).getEntitiesRelated().add(entityAdd);
//				}
//			}
//		}
//
//		/************/		    
//
//	}


	public JPanel createBacktrackingGoalPanel(){

		JPanel panel = new JPanel();

		JScrollPane scrollPane = new JScrollPane();
		
		if(( entity != null ) && (entity.getBacktrackingGoalList().size() > 0) ){
			
			if(entity.getBacktrackingGoalList().size() > 0){
				dataBacktracking = new Object[entity.getBacktrackingGoalList().size()][3];

				for (int i = 0; i < entity.getBacktrackingGoalList().size(); i++) {
					dataBacktracking[i][0] = entity.getBacktrackingGoalList().get(i);
					dataBacktracking[i][1] = "Goal";
					dataBacktracking[i][2] = "";
				}
			}
			
			String[] columns = {"Name","Type",""};
			
			DefaultTableModel model = new DefaultTableModel(dataBacktracking, 
					columns);
			
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
					
					GoalModel goal = (GoalModel)dataBacktracking[row][0];
					
					controller.removeGoalFromEntityBackTrackingList(entity, goal); 
					
					UmpstModule pai = getFatherPanel();
					changePanel(pai.getMenuPanel().getEntitiesPane().getEntitiesPanel().createEntitiesPanel(entity));
				}
			});
			
			scrollPane = new JScrollPane(table);

		}
		
		if (entity != null){

			panel = new JPanel();
			panel.setLayout(new GridBagLayout());

			GridBagConstraints c = new GridBagConstraints();

			if (entity!=null){
				c.gridx = 1; c.gridy = 0; c.gridwidth=1;
				panel.add(buttonFrameGoal,c);
				buttonFrameGoal.setToolTipText("Add backtracking from goals");
			}

			c.fill = GridBagConstraints.BOTH;
			c.gridx=0;c.gridy=1;c.weightx=0.9;c.weighty=0.9;c.gridwidth=6;

			panel.add(scrollPane,c);
			
		}

		return panel;

	}

	public JPanel createBacktrackingHypothesis(){

		JPanel panel = new JPanel();

		JScrollPane scrollPane = new JScrollPane();
		
		if(( entity != null ) && (entity.getBacktrackingHypothesis().size() > 0)){
			
			if(entity.getBacktrackingHypothesis().size() > 0){
				dataBacktracking = new Object[entity.getBacktrackingHypothesis().size()][3];

				for (int i = 0; i < entity.getBacktrackingHypothesis().size(); i++) {
					dataBacktracking[i][0] = entity.getBacktrackingHypothesis().get(i);
					dataBacktracking[i][1] = "Hypothesis";
					dataBacktracking[i][2] = "";
				}
			}
			
			String[] columns = {"Name","Type",""};
			
			DefaultTableModel model = new DefaultTableModel(dataBacktracking, 
					columns);
			
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
					
					HypothesisModel hypothesis = (HypothesisModel)dataBacktracking[row][0];
					
					controller.removeHypothesisFromEntityBackTrackingList(entity, hypothesis); 
					
					UmpstModule pai = getFatherPanel();
					changePanel(pai.getMenuPanel().getEntitiesPane().getEntitiesPanel().createEntitiesPanel(entity));
				}
			});
			
			scrollPane = new JScrollPane(table);

		}
		
		if (entity != null){

			panel = new JPanel();
			panel.setLayout(new GridBagLayout());

			GridBagConstraints c = new GridBagConstraints();

			if (entity!=null){
				c.gridx = 1; c.gridy = 0; c.gridwidth=1;
				panel.add(buttonFrameHypothesis,c);
				buttonFrameHypothesis.setToolTipText("Add hypothesis from goals");
			}

			c.fill = GridBagConstraints.BOTH;
			c.gridx=0;c.gridy=1;c.weightx=0.9;c.weighty=0.9;c.gridwidth=6;

			panel.add(scrollPane,c);
			
		}

		return panel;

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
			c.gridx = 0; c.gridy = 0; c.gridwidth=1;
			panel.add(buttonAttribute, c);

			c.gridx = 1; c.gridy = 0; c.gridwidth=1;
			panel.add(buttonReuseAttribute,c);
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

			Set<RuleModel> aux = entity.getFowardTrackingRules();
			RuleModel rule;
			for (Iterator<RuleModel> it = aux.iterator(); it.hasNext(); ) {
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
				data[i][0] = entity.getMapAtributes().get(key).getName();
				data[i][1] = "Atribute";
				i++;
			}
		}

		if ((entity!=null)&&(entity.getFowardTrackingRules()!=null)){

			Set<RuleModel> aux = entity.getFowardTrackingRules();
			RuleModel rule;
			for (Iterator<RuleModel> it = aux.iterator(); it.hasNext(); ) {
				rule = it.next();
				data[i][0] = rule.getName();
				data[i][1] = "Rule";
				i++;
			}

		}

		if ((entity!=null)&&(entity.getFowardTrackingRelationship()!=null)){
			Set<RelationshipModel> aux = entity.getFowardTrackingRelationship();
			RelationshipModel relationship;
			for (Iterator<RelationshipModel> it = aux.iterator(); it.hasNext(); ) {
				relationship = it.next();
				data[i][0] = relationship.getName();
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


	public String[] vinculateAtribute(){

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
							allOtherAtributes[i] = entityAux.getMapAtributes().get(keyAtribute).getName();
							i++;
						}
					}

				}

			}
		} 

		return allOtherAtributes;

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
					if (getUmpstProject().getMapEntity().get(key).getMapAtributes().get(keyAux).getName().equals(atributeRelated)){
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
		changePanel(pai.getMenuPanel().getEntitiesPane().getEntitiesPanel().createEntitiesPanel(entity));    			
	}

	public void createFrameGoal(){

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
			dataFrame[i][1] = getUmpstProject().getMapGoal().get(key).getName();			
			dataFrame[i][2] = "";
			i++;
		}


		DefaultTableModel model = new DefaultTableModel(dataFrame,columnNames);
		JTable table = new JTable(model);

		TableButton buttonEdit = new TableButton( new TableButton.TableButtonCustomizer()
		{
			public void customize(JButton button, int row, int column)
			{
				button.setIcon(IconController.getInstance().getAddIconP());

			}
		});

		TableColumn buttonColumn1 = table.getColumnModel().getColumn(columnNames.length-1);
		buttonColumn1.setMaxWidth(28);
		buttonColumn1.setCellRenderer(buttonEdit);
		buttonColumn1.setCellEditor(buttonEdit);

		buttonEdit.addHandler(new TableButton.TableButtonPressedHandler() {	
			public void onButtonPress(int row, int column) {

				String key = dataFrame[row][0].toString();
				
				GoalModel goal = (GoalModel)getUmpstProject().getMapGoal().get(key); 
				
				controller.addGoalToEntityBackTrackingList(entity, goal);
				
				UmpstModule pai = getFatherPanel();
				changePanel(pai.getMenuPanel().getEntitiesPane().getEntitiesPanel().createEntitiesPanel(entity));

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

		for (String key: sortedKeys){
			dataFrameHypo[i][0] = (HypothesisModel)getUmpstProject().getMapHypothesis().get(key);			
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

				String key = dataFrame[row][0].toString();
				
				HypothesisModel hypothesis = (HypothesisModel)getUmpstProject().getMapHypothesis().get(key); 
				
				controller.addHypothesisToEntityBackTrackingList(entity, hypothesis);
								UmpstModule pai = getFatherPanel();
				changePanel(pai.getMenuPanel().getEntitiesPane().getEntitiesPanel().createEntitiesPanel(entity));

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