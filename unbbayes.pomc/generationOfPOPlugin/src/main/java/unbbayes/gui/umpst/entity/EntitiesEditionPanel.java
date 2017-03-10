package unbbayes.gui.umpst.entity;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
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
import unbbayes.gui.umpst.MainPropertiesEditionPane;
import unbbayes.gui.umpst.TableButton;
import unbbayes.gui.umpst.TableObject;
import unbbayes.gui.umpst.UmpstModule;
import unbbayes.gui.umpst.selection.AttributeSelectionPane;
import unbbayes.gui.umpst.selection.GoalSelectionPane;
import unbbayes.gui.umpst.selection.HypothesisSelectionPane;
import unbbayes.gui.umpst.selection.interfaces.GoalAddition;
import unbbayes.gui.umpst.selection.interfaces.HypothesisAddition;
import unbbayes.model.umpst.ObjectModel;
import unbbayes.model.umpst.entity.AttributeModel;
import unbbayes.model.umpst.entity.EntityModel;
import unbbayes.model.umpst.entity.RelationshipModel;
import unbbayes.model.umpst.goal.GoalModel;
import unbbayes.model.umpst.goal.HypothesisModel;
import unbbayes.model.umpst.group.GroupModel;
import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.model.umpst.rule.RuleModel;


/**
 * Panel for Entities Edition
 * 
 * @author Laecio Santos (laecio@gmail.com)
 * @author Rafael Mezzomo 
 * @author Shou Matusumoto
 *
 */
public class EntitiesEditionPanel extends IUMPSTPanel 
                                  implements GoalAddition, HypothesisAddition {

	private static final long serialVersionUID = 1L;

	private Object[][] dataFrame ;
	private Object[][] dataFrameHypo ;
	private Object[][] dataBacktracking ;

	private JButton buttonSave;
	private JButton buttonCancel;
	private JButton buttonAttribute;
	private JButton buttonFrameGoal;
	private JButton buttonFrameHypothesis;
	private JButton buttonReuseAttribute; 

	private EntityModel entity;

	private MainPropertiesEditionPane mainPropertiesEditionPane ; 

	private Controller controller; 

	UmpstModule fatherModule; 
	
	private final EntitiesEditionPanel entitiesEditionPanel; 

	/** Load resource file from this package */
	private static ResourceBundle resource = 
			unbbayes.util.ResourceController.newInstance().getBundle(
					unbbayes.gui.umpst.resources.Resources.class.getName());

	private IconController iconController = IconController.getInstance();

	public EntitiesEditionPanel(UmpstModule _fatherModule,
			UMPSTProject _umpstProject, 
			EntityModel _entity){

		super(_fatherModule);
		
		entitiesEditionPanel = this; 

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
				createGoalBacktrackingPanel()); 

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

	public JSplitPane createGoalBacktrackingPanel(){

		JSplitPane splitPanel = new JSplitPane(
				JSplitPane.VERTICAL_SPLIT,
				createBacktrackingGoalPanel(),
				createBacktrackingHypothesis());

		splitPanel.setDividerLocation(150); 

		return splitPanel; 

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
						null, true); 

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
		buttonFrameGoal.setToolTipText(resource.getString("hpAddBackGoal"));
		
		buttonFrameHypothesis	= new JButton (iconController.getCicleHypothesisIcon());
		buttonFrameHypothesis.setToolTipText(resource.getString("hpAddBackHypothesis"));

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

							updateTableEntities();

							changePanel(new EntitiesEditionPanel(getFatherPanel(),getUmpstProject(),newEntity));	
						}

					} catch (Exception e1) {
						JOptionPane.showMessageDialog(null, 
								resource.getString("erCreatingEntity"), 
								resource.getString("ttPanelError"),
								JOptionPane.WARNING_MESSAGE);
						UmpstModule pai = getFatherPanel();
						changePanel(pai.getMenuPanel());	
						e1.printStackTrace();

					}
				}

				//Changing old entity
				else{
					if( JOptionPane.showConfirmDialog(null, 
							resource.getString("qtUpdateEntity"), 
							resource.getString("ttPanelQuestion"), 
							JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION ){

						try{

							entity.setName(mainPropertiesEditionPane.getTitleText());
							entity.setComments(mainPropertiesEditionPane.getCommentsText());
							entity.setAuthor(mainPropertiesEditionPane.getAuthorText());
							entity.setDate(mainPropertiesEditionPane.getDateText());

							updateTableEntities();

							JOptionPane.showMessageDialog(null, 
									resource.getString("msEntitySuccessfullUpdated"), 
									resource.getString("ttPanelSucessfull"), 
									JOptionPane.INFORMATION_MESSAGE);
						}
						catch (Exception e2) {
							JOptionPane.showMessageDialog(null,
									resource.getString("erUpdatingEntity"), 
									resource.getString("ttPanelError"), 
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
				Collection<HypothesisModel> hypothesisSet = getUmpstProject().getMapHypothesis().values(); 
				
				HypothesisSelectionPane hypothesisSelectionPane = 
						new HypothesisSelectionPane(hypothesisSet, entitiesEditionPanel); 
				hypothesisSelectionPane.setLocationRelativeTo(entitiesEditionPanel); 
				hypothesisSelectionPane.pack(); 
				hypothesisSelectionPane.setVisible(true); 
			}
		});

		buttonFrameGoal.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				Collection<GoalModel> goalSet = getUmpstProject().getMapGoal().values(); 
				
				GoalSelectionPane goalSelectionPane = new GoalSelectionPane(goalSet, entitiesEditionPanel); 
				goalSelectionPane.setLocationRelativeTo(entitiesEditionPanel); 
				goalSelectionPane.pack(); 
				goalSelectionPane.setVisible(true); 
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

		TableEntities entitiesTable = pai.getMenuPanel().getEntitiesPane().getEntitiesTable();
		JTable table = entitiesTable.createTable(columnNames,data);

		entitiesTable.getScrollPaneEntitiesTable().setViewportView(table);
		entitiesTable.getScrollPaneEntitiesTable().updateUI();
		entitiesTable.getScrollPaneEntitiesTable().repaint();
		entitiesTable.updateUI();
		entitiesTable.repaint();
	}

	public JPanel createBacktrackingGoalPanel(){

		JPanel panel = new JPanel();
		
		final int COLUMN_BUTTON_DEL = 0; 
		final int COLUMN_ID         = 1; 
		final int COLUMN_NAME       = 2; 

		JScrollPane scrollPane = new JScrollPane();

		if(( entity != null ) && (entity.getBacktrackingGoalList().size() > 0) ){

			if(entity.getBacktrackingGoalList().size() > 0){
				dataBacktracking = new Object[entity.getBacktrackingGoalList().size()][3];

				for (int i = 0; i < entity.getBacktrackingGoalList().size(); i++) {
					dataBacktracking[i][COLUMN_BUTTON_DEL] = "";
					dataBacktracking[i][COLUMN_ID] = entity.getBacktrackingGoalList().get(i).getId();
					dataBacktracking[i][COLUMN_NAME] = entity.getBacktrackingGoalList().get(i);
				}
			}

			String[] columns = {"","ID","Name"};

			DefaultTableModel model = new DefaultTableModel(dataBacktracking, 
					columns);

			JTable table = new JTable(model);

			TableButton buttonDel = new TableButton( new TableButton.TableButtonCustomizer(){
				public void customize(JButton button, int row, int column){
					button.setIcon(IconController.getInstance().getDeleteIcon() );
				}
			});

			TableColumn buttonColumn1 = table.getColumnModel().getColumn(COLUMN_BUTTON_DEL);

			buttonColumn1.setMaxWidth(TableObject.SIZE_COLUMN_BUTTON);
			buttonColumn1.setCellRenderer(buttonDel);
			buttonColumn1.setCellEditor(buttonDel);

			buttonDel.addHandler(new TableButton.TableButtonPressedHandler() {	
				public void onButtonPress(int row, int column) {

					GoalModel goal = (GoalModel)dataBacktracking[row][COLUMN_NAME];

					controller.removeGoalFromEntityBackTrackingList(entity, goal); 

					UmpstModule pai = getFatherPanel();
					changePanel(pai.getMenuPanel().getEntitiesPane().getEntitiesPanel().createEntitiesPanel(entity));
				}
			});

			table.getColumnModel().getColumn(COLUMN_ID).setMaxWidth(TableObject.SIZE_COLUMN_INDEX); 
			
			scrollPane = new JScrollPane(table);
		}

		//Add painel only if have one entity created. 
		
		if (entity != null){

			panel = new JPanel();
			panel.setLayout(new GridBagLayout());

			GridBagConstraints c = new GridBagConstraints();

			c.gridx = 1; c.gridy = 0; c.gridwidth=1;
			panel.add(buttonFrameGoal,c);

			c.fill = GridBagConstraints.BOTH;
			c.gridx=0;c.gridy=1;c.weightx=0.9;c.weighty=0.9;c.gridwidth=6;

			panel.add(scrollPane,c);
		}

		return panel;
	}

	public JPanel createBacktrackingHypothesis(){

		JPanel panel = new JPanel();

		final int COLUMN_BUTTON_DEL = 0; 
		final int COLUMN_ID         = 1; 
		final int COLUMN_NAME       = 2; 
		
		JScrollPane scrollPane = new JScrollPane();

		if(( entity != null ) && (entity.getBacktrackingHypothesis().size() > 0)){

			if(entity.getBacktrackingHypothesis().size() > 0){
				dataBacktracking = new Object[entity.getBacktrackingHypothesis().size()][3];

				for (int i = 0; i < entity.getBacktrackingHypothesis().size(); i++) {
					dataBacktracking[i][COLUMN_BUTTON_DEL] = "";
					dataBacktracking[i][COLUMN_ID] = entity.getBacktrackingHypothesis().get(i).getId();
					dataBacktracking[i][COLUMN_NAME] = entity.getBacktrackingHypothesis().get(i);
				}
			}

			String[] columns = {" ","ID","Name"};

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

			TableColumn buttonColumn1 = table.getColumnModel().getColumn(COLUMN_BUTTON_DEL);

			buttonColumn1.setMaxWidth(TableObject.SIZE_COLUMN_BUTTON);
			buttonColumn1.setCellRenderer(buttonDel);
			buttonColumn1.setCellEditor(buttonDel);

			buttonDel.addHandler(new TableButton.TableButtonPressedHandler() {	
				public void onButtonPress(int row, int column) {

					HypothesisModel hypothesis = (HypothesisModel)dataBacktracking[row][COLUMN_NAME];

					controller.removeHypothesisFromEntityBackTrackingList(entity, hypothesis); 

					UmpstModule pai = getFatherPanel();
					changePanel(pai.getMenuPanel().getEntitiesPane().getEntitiesPanel().createEntitiesPanel(entity));
				}
			});

			table.getColumnModel().getColumn(COLUMN_ID).setMaxWidth(TableObject.SIZE_COLUMN_INDEX); 
			
			scrollPane = new JScrollPane(table);

		}

		if (entity != null){

			panel = new JPanel();
			panel.setLayout(new GridBagLayout());

			GridBagConstraints c = new GridBagConstraints();

     		c.gridx = 1; c.gridy = 0; c.gridwidth=1;
			panel.add(buttonFrameHypothesis,c);
	
			c.fill = GridBagConstraints.BOTH;
			c.gridx=0;c.gridy=1;c.weightx=0.9;c.weighty=0.9;c.gridwidth=6;

			panel.add(scrollPane,c);

		}

		return panel;

	}

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
		panel.setBorder(BorderFactory.createTitledBorder(resource.getString("ttListAttibutes")));

		return panel; 

	}


	public JScrollPane createTraceabilityTable(){

		int i = 0;

		String[] columns = {resource.getString("ttColType"), 
				            resource.getString("ttColDescription")};

		List<ObjectModel> listObjectModel = new ArrayList<ObjectModel>(); 

		if (entity != null){
			
			//ATRIBUTOS
			if ( entity.getMapAtributes()!=null ){

				Set<String> keys = entity.getMapAtributes().keySet();
				TreeSet<String> sortedString = new TreeSet<String>(keys);

				for (String key : sortedString){
					AttributeModel attributeModel = entity.getMapAtributes().get(key); 
					listObjectModel.add(attributeModel); 
				}
			}

			if (entity.getFowardTrackingRules() != null){

				Set<RuleModel> aux = entity.getFowardTrackingRules();
				
				for(RuleModel ruleModel: aux){
					listObjectModel.add(ruleModel); 
				}
			}

			if (entity.getFowardTrackingRelationship() != null){
				
				Set<RelationshipModel> aux = entity.getFowardTrackingRelationship();
				for( RelationshipModel relationshipModel: aux){
					listObjectModel.add(relationshipModel); 
				}
			}

			if (entity.getFowardTrackingGroups() != null){
				Set<GroupModel> aux = entity.getFowardTrackingGroups();
				for(GroupModel groupModel: aux){
					listObjectModel.add(groupModel); 
				}
			}
		}
		
		String[][] data; 
		
		if(listObjectModel.size() > 0){
			data = new String[listObjectModel.size()][2]; 
			for(ObjectModel objectModel: listObjectModel){
				data[i][0] = objectModel.getType(); 
				data[i][1] = objectModel.getName(); 
				i++; 
			}
		}else{
			data = new String[30][2];
		}

		DefaultTableModel model = new DefaultTableModel(data, columns);
		
		JTable table = new JTable(model);
		table.setGridColor(Color.WHITE); 
		table.setEnabled(false); 
		
		table.getColumnModel().getColumn(0).setMaxWidth(100); 

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setBorder(BorderFactory.createTitledBorder(resource.getString("ttEntityTraceability")));

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

		TableButton buttonAdd = new TableButton( new TableButton.TableButtonCustomizer(){
			public void customize(JButton button, int row, int column){
				button.setIcon(IconController.getInstance().getAddIconP());
			}
		});

		TableColumn buttonColumn1 = table.getColumnModel().getColumn(columnNames.length-1);
		buttonColumn1.setMaxWidth(TableObject.SIZE_COLUMN_BUTTON);
		buttonColumn1.setCellRenderer(buttonAdd);
		buttonColumn1.setCellEditor(buttonAdd);

		buttonAdd.addHandler(new TableButton.TableButtonPressedHandler() {	
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

	public void createFrameHypothesis(){

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

		TableButton buttonEdit = new TableButton( new TableButton.TableButtonCustomizer(){
			public void customize(JButton button, int row, int column){
				button.setIcon(IconController.getInstance().getAddIcon());
			}
		});

		TableColumn buttonColumn1 = table.getColumnModel().getColumn(0);
		
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

	public void addGoalList(List<GoalModel> listGoals) {
	
		GoalModel goal = listGoals.get(0); 
		controller.addGoalToEntityBackTrackingList(entity, goal);

		UmpstModule pai = getFatherPanel();
		changePanel(pai.getMenuPanel().getEntitiesPane().getEntitiesPanel().createEntitiesPanel(entity));
	
	}

	public void addHypothesisList(List<HypothesisModel> list) {
		HypothesisModel hypothesis = list.get(0); 
		controller.addHypothesisToEntityBackTrackingList(entity, hypothesis);

		UmpstModule pai = getFatherPanel();
		changePanel(pai.getMenuPanel().getEntitiesPane().getEntitiesPanel().createEntitiesPanel(entity));
	}

}