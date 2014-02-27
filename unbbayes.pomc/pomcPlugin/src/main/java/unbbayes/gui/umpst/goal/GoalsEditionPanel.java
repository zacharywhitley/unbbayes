package unbbayes.gui.umpst.goal;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;

import javax.help.HelpSet;
import javax.help.JHelp;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.MaskFormatter;

import unbbayes.controller.umpst.IconController;
import unbbayes.gui.umpst.IUMPSTPanel;
import unbbayes.gui.umpst.MainPropertiesEditionPane;
import unbbayes.gui.umpst.UmpstModule;
import unbbayes.gui.umpst.selection.HypothesisSelectionPane;
import unbbayes.gui.umpst.selection.SubGoalSelectionPane;
import unbbayes.model.umpst.entities.AttributeModel;
import unbbayes.model.umpst.entities.EntityModel;
import unbbayes.model.umpst.entities.RelationshipModel;
import unbbayes.model.umpst.groups.GroupModel;
import unbbayes.model.umpst.project.SearchModelGoal;
import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.model.umpst.requirements.GoalModel;
import unbbayes.model.umpst.requirements.HypothesisModel;
import unbbayes.model.umpst.rules.RuleModel;
import unbbayes.util.CommonDataUtil;

/**
 * Panel for Goals Edition
 */
public class GoalsEditionPanel extends IUMPSTPanel {

	private static final long serialVersionUID = 1L;

	private JButton buttonSave 	     ;
	private JButton buttonCancel     ;

	private JButton buttonHypothesis ;

	private JButton buttonSubgoal    ;
	private JButton buttonReuseSubgoal;

	private JButton buttonReuseHipothesis;

	private MainPropertiesEditionPane mainPropertiesEditionPane ; 

	private GoalModel goal;
	private GoalModel goalFather;

	private MaskFormatter maskFormatter;

	private SubGoalSelectionPane subgoalSelectionPane; 

	private HypothesisSelectionPane hypothesisSelectionPane; 

	UmpstModule janelaPai; 

	/** Load resource file from this package */
	private static ResourceBundle resource = 
			unbbayes.util.ResourceController.newInstance().getBundle(
					unbbayes.gui.umpst.resources.Resources.class.getName());

	private IconController iconController = IconController.getInstance();


	public GoalsEditionPanel(UmpstModule fatherModule, 
			UMPSTProject umpstProject, 
			GoalModel goal, 
			GoalModel goalFather){

		super(fatherModule);

		setUmpstProject(umpstProject);
		
		this.goal = goal;
		
		this.goalFather = goalFather;
		
		if(goal != null){
			if(goal.getGoalFather() != null){
				this.goalFather = goal.getGoalFather(); 
			}
		}
		
		this.janelaPai = fatherModule; 

		this.setLayout(new GridLayout(1,1));

		createButtons(); 

		JSplitPane leftPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				createPanelText(),
				createSubgoalsTable()); 

		JSplitPane rightPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				createTraceabilityTable(),
				createHypothesisTable()); 


		JSplitPane mainPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				leftPane,
				rightPane);

		this.add(mainPane);

		leftPane.setDividerLocation(280); 
		rightPane.setDividerLocation(280); 

		createListeners();
	}

	public JPanel createPanelText(){

		String title = ""; 

		if( goal == null){
			if (goalFather!=null){
				title = "New Subgoal";
			}
			else{
				title = "New Goal";
			}

		} else {
			title = "Update Goal";
		}

		// CREATE FORM 
		mainPropertiesEditionPane = 
				new MainPropertiesEditionPane(buttonCancel, 
						buttonSave, 
						title, 
						"Goals Details",
						null,
						null); 

		if (goal != null){
			mainPropertiesEditionPane.setTitleText(goal.getGoalName());
			mainPropertiesEditionPane.setCommentsText(goal.getComments());
			mainPropertiesEditionPane.setAuthorText(goal.getAuthor());
			mainPropertiesEditionPane.setDateText(goal.getDate());
		}

		return mainPropertiesEditionPane.getPanel(); 

	}

	private void createButtons() {

		buttonSave 	     = new JButton(iconController.getSaveObjectIcon());
		buttonSave.setText(resource.getString("btnSave"));
		
		if( goal == null){
			buttonSave.setToolTipText(resource.getString("HpSaveGoal"));

		} else {
			buttonSave.setToolTipText(resource.getString("HpUpdateGoal"));
		}
		
		buttonCancel     = new JButton(iconController.getReturnIcon());
		buttonCancel.setText(resource.getString("btnReturn")); 
		buttonCancel.setToolTipText(resource.getString("HpReturnMainPanel"));
		
		buttonHypothesis = new JButton(iconController.getListAddIcon());
		buttonHypothesis.setToolTipText(resource.getString("HpAddHyphotesis"));

		buttonSubgoal    = new JButton(iconController.getListAddIcon());
		buttonSubgoal.setToolTipText(resource.getString("HpAddSubgoal"));

		buttonReuseSubgoal = new JButton(iconController.getReuseAttributeIcon());
		buttonReuseSubgoal.setToolTipText(resource.getString("HpReuseSubgoal"));

		buttonReuseHipothesis = new JButton(iconController.getReuseAttributeIcon()); 
		buttonReuseSubgoal.setToolTipText(resource.getString("HpReuseHypothesis"));

	}


	public void createListeners(){

		buttonSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				// -> New Goal
				if( goal == null){
					if (mainPropertiesEditionPane.getTitleText().equals("")){
						JOptionPane.showMessageDialog(null, resource.getString("ErGoalDescriptionEmpty"));
					}
					else{
						GoalModel goalAdd = updateMapGoal();					    
						updateMapSearch(goalAdd);
						
						TableGoals tableGoals = updateTableGoals(goalAdd);
						
						UmpstModule pai = getFatherPanel();
						changePanel(pai.getMenuPanel().getRequirementsPane().getGoalsPanel().getGoalsAdd(goalAdd));	
						//						JOptionPane.showMessageDialog(null, "Goal successfully added",null, JOptionPane.INFORMATION_MESSAGE);
					}
				}
				// -> Update Goal
				else{
					if( JOptionPane.showConfirmDialog(null, "Do you want to update this Goal?", "UnBBayes", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION ){

						try{
							/**Cleaning Search Map*/
							Set<GoalModel> aux = new HashSet<GoalModel>();
							GoalModel goalBeta;
							String[] strAux=goal.getGoalName().split(" ");

							for (int i = 0; i < strAux.length; i++) {
								if(getUmpstProject().getMapSearchGoal().get(strAux[i])!=null){
									getUmpstProject().getMapSearchGoal().get(strAux[i]).getGoalsRelated().remove(goal);
									aux = getUmpstProject().getMapSearchGoal().get(strAux[i]).getGoalsRelated();
									for (Iterator<GoalModel> it = aux.iterator(); it.hasNext(); ) {
										goalBeta = it.next();
									}
								}
							}
							/************/

							goal.setGoalName(mainPropertiesEditionPane.getTitleText());
							goal.setComments(mainPropertiesEditionPane.getCommentsText());
							goal.setAuthor(mainPropertiesEditionPane.getAuthorText());
							goal.setDate(mainPropertiesEditionPane.getDateText());


							updateMapSearch(goal);
							updateTableGoals(goal);

							//							JOptionPane.showMessageDialog(null, "Goal successfully updated",null, JOptionPane.INFORMATION_MESSAGE);	

						}
						catch (Exception e2) {
							JOptionPane.showMessageDialog(null,"Error while updating goal", "UnBBayes", JOptionPane.WARNING_MESSAGE);
							UmpstModule pai = getFatherPanel();
							changePanel(pai.getMenuPanel());	
						}
					}
				}
			}
		});

		buttonReuseSubgoal.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createReutilizeGoalPanel();
			}
		});

		buttonReuseHipothesis.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createReutilizeHipothesysPanel();
			}
		});

		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				UmpstModule pai = getFatherPanel();
				if(goalFather == null){
					TableGoals tableGoals = updateTableGoals(goal);
					changeToTableGoals(tableGoals); 
				} else{
					changePanel(pai.getMenuPanel().getRequirementsPane().getGoalsPanel().getGoalsAdd(goalFather));	
				}
			}
		});

		buttonHypothesis.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				changePanel(new HypothesisEditionPanel(getFatherPanel(),getUmpstProject(),goal,null,null));

			}
		});

		buttonSubgoal.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//				changePanel(new SubgoalsAdd(getFatherPanel(),getUmpstProject(),null,goal));
				changePanel(new GoalsEditionPanel(getFatherPanel(),getUmpstProject(),null,goal));
			}
		});

	}

	private void createReutilizeGoalPanel() {
		subgoalSelectionPane = new SubGoalSelectionPane(getOthersGoalsList(), this); 
		subgoalSelectionPane.setLocationRelativeTo(janelaPai); 
		subgoalSelectionPane.pack();
		subgoalSelectionPane.setVisible(true);
	}

	private void createReutilizeHipothesysPanel() {
		hypothesisSelectionPane = new HypothesisSelectionPane(getOthersHypothesisList(), this); 
		hypothesisSelectionPane.setLocationRelativeTo(janelaPai); 
		hypothesisSelectionPane.pack();
		hypothesisSelectionPane.setVisible(true);
	}

	public GoalModel updateMapGoal(){
		
		String idAux = "";
		Set<String> keys = getUmpstProject().getMapGoal().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);
		int tamanho = getUmpstProject().getMapGoal().size() + 1;
		int maior = 0;
		String idAux2 = "";
		int intAux;

		if (goalFather==null){

			if ( getUmpstProject().getMapGoal().size()!=0){
				for (String key: sortedKeys){
					//tamanho = tamanho - getUmpstProject().getMapGoal().get(key).getSubgoals().size();
					
					idAux= getUmpstProject().getMapGoal().get(key).getId();
					
					if (idAux.contains(".")){
						intAux = idAux.indexOf(".");
						idAux2 = idAux.substring(0, intAux);
						if (maior < Integer.parseInt(idAux2)){
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

		}
		else{
			if (goalFather.getSubgoals()!=null){
				idAux = goalFather.getId()+"."+ (goalFather.getSubgoals().size()+1);

			}
			else{
				idAux = goalFather.getId()+".1";

			}
		}


		GoalModel goalAdd = new GoalModel(idAux,
				mainPropertiesEditionPane.getTitleText(),
				mainPropertiesEditionPane.getCommentsText(), 
				mainPropertiesEditionPane.getAuthorText(), 
				mainPropertiesEditionPane.getDateText(),
				goalFather,
				null,
				null,
				null,
				null,
				null);

		if (goalFather!=null){

			GoalModel aux = goalAdd.getGoalFather();
			while (aux!=null){
				aux.getSubgoals().put(goalAdd.getId(),goalAdd);
				if (aux.getGoalFather()!=null){
					aux = aux.getGoalFather();
				}
				else{
					aux=null;
				}
			}

			//goalFather.getSubgoals().put(goalAdd.getId(), goalAdd);
		}


		CommonDataUtil.getInstance().setAuthorName(mainPropertiesEditionPane.getAuthorText()); 

		getUmpstProject().getMapGoal().put(goalAdd.getId(), goalAdd);	

		return goalAdd;
	}


	public TableGoals updateTableGoals(GoalModel goalUpdate){
		String[] columnNames = {"ID","Goal","","",""};

		Object[][] data = new Object[getUmpstProject().getMapGoal().size()][5];
		Integer i=0;

		Set<String> keys = getUmpstProject().getMapGoal().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);

		for (String key: sortedKeys){
			data[i][0] = getUmpstProject().getMapGoal().get(key).getId();
			data[i][1] = getUmpstProject().getMapGoal().get(key).getGoalName();			
			data[i][2] = "";
			data[i][3] = "";
			data[i][4] = "";
			i++;
		}

		UmpstModule pai = getFatherPanel();
		TableGoals goalsTable = pai.getMenuPanel().getRequirementsPane().getGoalsTable();
		goalsTable.createTable(columnNames,data);
		
		return goalsTable; 
	}
	
	public void changeToTableGoals(TableGoals goalsTable){

		UmpstModule pai = getFatherPanel();
		changePanel(pai.getMenuPanel());

		goalsTable.getScrollPanePergunta().setViewportView(goalsTable.getTable());
		goalsTable.getScrollPanePergunta().updateUI();
		goalsTable.getScrollPanePergunta().repaint();
		goalsTable.updateUI();
		goalsTable.repaint();
	}

	public JPanel createHypothesisTable(){

		TableHypothesis hypoTable = new TableHypothesis(getFatherPanel(), 
				getUmpstProject(),
				goal);

		JTable table = hypoTable.createTable();

		JScrollPane scrollPane = new JScrollPane(table);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); 
		scrollPane.setHorizontalScrollBarPolicy(
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder("List of Hypothesis"));

		if(goal!=null){

			JPanel panelIcons = new JPanel();
			panelIcons.setLayout(new GridLayout(0,8)); 

			panelIcons.add(buttonHypothesis);
			panelIcons.add(buttonReuseHipothesis); 
			panelIcons.add(new JLabel()); 
			panelIcons.add(new JLabel()); 
			panelIcons.add(new JLabel()); 
			panelIcons.add(new JLabel()); 
			panelIcons.add(new JLabel()); 

			JButton btnHelp = new JButton(iconController.getHelpIcon()); 

			panelIcons.add(btnHelp); 

			btnHelp.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					try {
						HelpSet set =  new HelpSet(null, getClass().getResource("/help/UMPHelp/ump.hs"));
						//						set.setHomeID("UMP_Example");

						//						HelpBroker hb = set.createHelpBroker();
						//						DisplayHelpFromSource display = new CSH.DisplayHelpFromSource( hb );

						JHelp help = new JHelp(set);
						JFrame f = new JFrame();
						f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
						f.setContentPane(help);
						f.pack();
						f.setLocationRelativeTo(getFatherPanel()); 
						//						f.setTitle(resource.getString("helperDialogTitle"));
						f.setVisible(true);
					} catch (Exception evt) {
						evt.printStackTrace();
					}
				}
			});

			panel.add(panelIcons, BorderLayout.PAGE_START); 

		}

		panel.add(scrollPane,BorderLayout.CENTER);

		return panel; 
	}

	public JPanel createSubgoalsTable(){

		TableSubGoals subgoalsTable = new TableSubGoals(getFatherPanel(),
				getUmpstProject(),
				goal);
		
		JTable table = subgoalsTable.createTable();
		JScrollPane scrollPane = new JScrollPane(table);

		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); 
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder("List of Subgoals"));
		
		if (goal!=null){

			JPanel panelIcons = new JPanel();
			panelIcons.setLayout(new GridLayout(0,8)); 

			panelIcons.add(buttonSubgoal);
			panelIcons.add(buttonReuseSubgoal); 
			panelIcons.add(new JLabel()); 
			panelIcons.add(new JLabel()); 
			panelIcons.add(new JLabel()); 
			panelIcons.add(new JLabel()); 
			panelIcons.add(new JLabel()); 
			panelIcons.add(new JButton(iconController.getHelpIcon())); 

			panel.add(panelIcons,BorderLayout.PAGE_START);
		}

		panel.add(scrollPane, BorderLayout.CENTER);

		return panel; 

	}

	public void updateMapSearch(GoalModel goalAdd){
		/**Upating searchPanel*/

		String[] strAux = {};
		
		strAux = goalAdd.getGoalName().split(" ");
		
		Set<GoalModel> goalSetSearch = new HashSet<GoalModel>();

		for (int i = 0; i < strAux.length; i++) {
			if(!strAux[i].equals(" ")){
				if(getUmpstProject().getMapSearchGoal().get(strAux[i])==null){
					goalSetSearch.add(goalAdd);
					SearchModelGoal searchModel = new SearchModelGoal(strAux[i], goalSetSearch);
					getUmpstProject().getMapSearchGoal().put(searchModel.getKeyWord(), searchModel);
				}
				else{
					getUmpstProject().getMapSearchGoal().get(strAux[i]).getGoalsRelated().add(goalAdd);
				}
			}
		}	    
	}

	public JScrollPane  createTraceabilityTable() {

		int i = 0;

		//All this magic only for calculate the number of lines... 

		if ( (goal != null ) && (goal.getFowardTrackingEntity() !=null) ){

			EntityModel entity;
			Set<EntityModel> aux = goal.getFowardTrackingEntity();

			for (Iterator<EntityModel> it = aux.iterator(); it.hasNext(); ) {

				entity = it.next();
				if (entity.getFowardTrackingRules()!=null){
					Set<RuleModel> auxRules = entity.getFowardTrackingRules();
					RuleModel rule;
					for (Iterator<RuleModel> itRules = auxRules.iterator(); itRules.hasNext(); ) {
						rule = itRules.next();
						i++;
					}
				}

				if (entity.getMapAtributes()!=null){
					Set<String> keysAtribute = entity.getMapAtributes().keySet();
					TreeSet<String> sortedKeysAtribute = new TreeSet<String>(keysAtribute);
					AttributeModel atribute;
					for(String keyAtribute : sortedKeysAtribute){
						i++;
					}
				}

				if (entity.getFowardTrackingRelationship()!=null){
					Set<RelationshipModel> auxRelationship = entity.getFowardTrackingRelationship();
					RelationshipModel relationship;
					for (Iterator<RelationshipModel> itRelationship = auxRelationship.iterator(); itRelationship.hasNext(); ) {
						relationship = itRelationship.next();
						i++;
					}
				}

				if (entity.getFowardTrackingGroups()!=null){
					Set<GroupModel> auxGroups = entity.getFowardTrackingGroups();
					GroupModel group;
					for (Iterator<GroupModel> itGroups = auxGroups.iterator(); itGroups.hasNext(); ) {
						group = itGroups.next();
						i++;
					}
				}
				i++;
			}
		}

		if ((goal!=null)&&(goal.getSubgoals()!=null)){
			Set<String> keys = goal.getSubgoals().keySet();
			TreeSet<String> sortedKeys = new TreeSet<String>(keys);

			for (String key: sortedKeys){

				i++;
			}
		}
		if ((goal!=null)&&(goal.getMapHypothesis()!=null)){
			Set<String> keys = goal.getMapHypothesis().keySet();
			TreeSet<String> sortedKeys = new TreeSet<String>(keys);	

			for (String key: sortedKeys){

				i++;
			}    	
		}

		if ( (goal!=null)&&(goal.getFowardTrackingGroups() !=null) ){
			GroupModel group;
			Set<GroupModel> aux = goal.getFowardTrackingGroups();

			for (Iterator<GroupModel> it = aux.iterator(); it.hasNext(); ) {
				group = it.next();

				i++;
			}
		}


		//Allocate the array for table
		Object[][] data = new Object[i+1][3];

		if (i < 30){
			data = new Object[30][3];
		}

		String[] columnNames = {
				"Type",
				"Traceability",
				"Name"};
		i=0;


		if ( (goal!=null)&&(goal.getFowardTrackingEntity() !=null) ){
			EntityModel entity;
			Set<EntityModel> aux = goal.getFowardTrackingEntity();

			for (Iterator<EntityModel> it = aux.iterator(); it.hasNext(); ) {
				entity = it.next();

				data[i][0] = "Entity";
				data[i][1] = "Direct";
				data[i][2] = entity.getEntityName();

				i++;
				if (entity.getFowardTrackingRules()!=null){
					Set<RuleModel> auxRules = entity.getFowardTrackingRules();
					RuleModel rule;
					for (Iterator<RuleModel> itRules = auxRules.iterator(); itRules.hasNext(); ) {
						rule = itRules.next();

						data[i][0] = "Rule";
						data[i][1] = "Indirect";
						data[i][2] = rule.getRulesName();
						i++;
					}
				}

				if (entity.getMapAtributes()!=null){
					Set<String> keysAtribute = entity.getMapAtributes().keySet();
					TreeSet<String> sortedKeysAtribute = new TreeSet<String>(keysAtribute);
					AttributeModel atribute;
					for(String keyAtribute : sortedKeysAtribute){

						data[i][0] = "Atribute";
						data[i][1] = "Indirect";
						data[i][2] = entity.getMapAtributes().get(keyAtribute).getAtributeName();
						i++;
					}
				}

				if (entity.getFowardTrackingRelationship()!=null){
					Set<RelationshipModel> auxRelationship = entity.getFowardTrackingRelationship();
					RelationshipModel relationship;
					for (Iterator<RelationshipModel> itRelationship = auxRelationship.iterator(); itRelationship.hasNext(); ) {
						relationship = itRelationship.next();

						data[i][0] = "Relationship";
						data[i][1] = "Indirect";
						data[i][2] = relationship.getRelationshipName();
						i++;
					}
				}
				if (entity.getFowardTrackingGroups()!=null){
					Set<GroupModel> auxGroups = entity.getFowardTrackingGroups();
					GroupModel group;
					for (Iterator<GroupModel> itGroups = auxGroups.iterator(); itGroups.hasNext(); ) {
						group = itGroups.next();

						data[i][0] = "Group";
						data[i][1] = "Indirect";
						data[i][2] = group.getGroupName();
						i++;
					}
				}

			}
		}


		if ((goal!=null)&&(goal.getSubgoals()!=null)){
			Set<String> keys = goal.getSubgoals().keySet();
			TreeSet<String> sortedKeys = new TreeSet<String>(keys);

			for (String key: sortedKeys){

				data[i][0] = "Goals";
				data[i][1] = "Direct";
				data[i][2] = goal.getSubgoals().get(key).getGoalName();
				i++;
			}
		}
		if ((goal!=null)&&(goal.getMapHypothesis()!=null)){
			Set<String> keys = goal.getMapHypothesis().keySet();
			TreeSet<String> sortedKeys = new TreeSet<String>(keys);	

			for (String key: sortedKeys){

				data[i][0] = "Hypothesis";
				data[i][1] = "Direct";
				data[i][2] = goal.getMapHypothesis().get(key).getHypothesisName();
				i++;
			}    	
		}

		if ( (goal!=null)&&(goal.getFowardTrackingGroups() !=null) ){
			GroupModel group;
			Set<GroupModel> aux = goal.getFowardTrackingGroups();

			for (Iterator<GroupModel> it = aux.iterator(); it.hasNext(); ) {
				group = it.next();

				data[i][0] = "Group";
				data[i][1] = "Direct";
				data[i][2] = group.getGroupName();

				i++;
			}
		}


		DefaultTableModel tableModel = new DefaultTableModel(data, columnNames);
		JTable table = new JTable(tableModel);
		table.setGridColor(Color.WHITE); 
		table.setEnabled(false); 

		table.getColumnModel().getColumn(0).setMaxWidth(100); 
		table.getColumnModel().getColumn(1).setMaxWidth(50); 
		table.getColumnModel().getColumn(2).setMinWidth(1000); 

		JScrollPane scrollPane = new JScrollPane(table);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); 
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

		scrollPane.setBorder(BorderFactory.createTitledBorder("This Goal Traceability"));

		return scrollPane; 

	}


	public String[] getOthersHypothesisList(){

		Set<String> keys = getUmpstProject().getMapGoal().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);	

		Set<String> keysHypo;
		TreeSet<String> sortedKeysHypo;
		GoalModel goalAux;
		int i=0;
		/**This is only to found the number of other hypothesis existents in order to create 
		 *     	    String[] allOtherHypothesis = new String[i];
		 * */
		for (String key: sortedKeys){
			if(getUmpstProject().getMapGoal().get(key)!=goal){
				if(getUmpstProject().getMapGoal().get(key).getMapHypothesis()!=null){

					goalAux = getUmpstProject().getMapGoal().get(key);
					keysHypo = goalAux.getMapHypothesis().keySet();
					sortedKeysHypo = new TreeSet<String>(keysHypo);	

					for (String keyHypo : sortedKeysHypo){
						/**Testing if the hypothesis is already in this goal*/
						if ( goal.getMapHypothesis().get(goalAux.getMapHypothesis().get(keyHypo).getId())==null )
							i++;
					}

				}
			}
		}   

		String[] allOtherHypothesis = new String[i];

		i=0;
		for (String key: sortedKeys){
			if(getUmpstProject().getMapGoal().get(key)!=goal){
				if(getUmpstProject().getMapGoal().get(key).getMapHypothesis()!=null){

					goalAux = getUmpstProject().getMapGoal().get(key);
					keysHypo = goalAux.getMapHypothesis().keySet();
					sortedKeysHypo = new TreeSet<String>(keysHypo);	

					for (String keyHypo : sortedKeysHypo){
						if ( goal.getMapHypothesis().get(goalAux.getMapHypothesis().get(keyHypo).getId()) == null ){
							allOtherHypothesis[i] = goalAux.getMapHypothesis().get(keyHypo).getHypothesisName();
							i++;
						}
					}

				}
			}
		} 
		return allOtherHypothesis;

	}

	public void addVinculateHypothesis(String hypothesisRelated){

		Set<String> keys = getUmpstProject().getMapGoal().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);	

		Set<String> keysHypo;
		TreeSet<String> sortedKeysHypo;
		GoalModel goalAux;
		int i=0;
		Boolean achou = false;

		for (String key: sortedKeys){
			if(getUmpstProject().getMapGoal().get(key).getMapHypothesis()!=null){	
				keysHypo = getUmpstProject().getMapGoal().get(key).getMapHypothesis().keySet();
				sortedKeysHypo = new TreeSet<String>(keysHypo);
				for(String keyAux : sortedKeysHypo){
					if (getUmpstProject().getMapGoal().get(key).getMapHypothesis().get(keyAux).getHypothesisName()==hypothesisRelated){
						updateMapHypothesis(getUmpstProject().getMapGoal().get(key).getMapHypothesis().get(keyAux));
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

	public void updateMapHypothesis(HypothesisModel hypothesisVinculated){

		/**Toda vez deve atualizar que agora essa hipotese tem outro pai e o goal relacionado agora tem outra hipotese*/
		getUmpstProject().getMapHypothesis().get(hypothesisVinculated.getId()).getGoalRelated().add(goal);
		goal.getMapHypothesis().put(hypothesisVinculated.getId(), hypothesisVinculated);

		if (hypothesisVinculated.getMapSubHypothesis()!=null){
			Set<String> keys = hypothesisVinculated.getMapSubHypothesis().keySet();
			TreeSet<String> sortedKeys = new TreeSet<String>(keys);	
			HypothesisModel hypothesis;
			for (String key: sortedKeys){
				hypothesis = hypothesisVinculated.getMapSubHypothesis().get(key);

				getUmpstProject().getMapHypothesis().get(hypothesis.getId()).getGoalRelated().add(goal);
				goal.getMapHypothesis().put(hypothesis.getId(),hypothesis);

			}

		}
		//PRECISO ATUALIZAR O GOAL RELATED DA HIPOTESE QUE ESTA NO MAPA GERAL

		UmpstModule pai = getFatherPanel();
		changePanel(pai.getMenuPanel().getRequirementsPane().getGoalsPanel().getGoalsAdd(goal));    			
	}


	private String[] getOthersGoalsList(){

		Set<String> keys = getUmpstProject().getMapGoal().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);	

		int i=0;

		/**This is only to found the number of other hypothesis existents in order to create 
		 *     	    String[] allOtherHypothesis = new String[i];
		 * */
		for (String key: sortedKeys){
			if(getUmpstProject().getMapGoal().get(key)!=goal){

				if(goal.getSubgoals().size()>0){

					if (goal.getSubgoals().get(getUmpstProject().getMapGoal().get(key).getId())==null){
						i++;
					}

				}
				else{
					i++;	

				}
			}
		}   

		String[] allOtherGoals = new String[i];

		i=0;

		for (String key: sortedKeys){
			if(getUmpstProject().getMapGoal().get(key)!=goal){

				if(goal.getSubgoals().size()>0){
					if (goal.getSubgoals().get(getUmpstProject().getMapGoal().get(key).getId())==null){
						allOtherGoals[i] = getUmpstProject().getMapGoal().get(key).getGoalName();
						i++;
					}
				}
				else{
					allOtherGoals[i] = getUmpstProject().getMapGoal().get(key).getGoalName();
					i++;
				}
			}
		}

		return allOtherGoals;

	}

	public void addVinculateGoal(String goalRelated){

		Set<String> keys = getUmpstProject().getMapGoal().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);	

		for (String key: sortedKeys){
			if(getUmpstProject().getMapGoal().get(key).getGoalName().equals(goalRelated)){	
				updateMapGoalVinculate(getUmpstProject().getMapGoal().get(key));
				break;
			}
		}  

	}


	public void updateMapGoalVinculate(GoalModel goalVinculated){

		/**Toda vez deve atualizar que agora essa hipotese tem outro pai e o goal relacionado agora tem outra hipotese*/
		getUmpstProject().getMapGoal().get(goalVinculated.getId()).getGoalsRelated().add(goal);
		goal.getSubgoals().put(goalVinculated.getId(), goalVinculated);

		UmpstModule pai = getFatherPanel();
		changePanel(pai.getMenuPanel().getRequirementsPane().getGoalsPanel().getGoalsAdd(goal));    			
	}


}