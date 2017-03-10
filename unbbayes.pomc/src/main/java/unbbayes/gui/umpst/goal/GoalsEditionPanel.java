package unbbayes.gui.umpst.goal;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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

import unbbayes.controller.umpst.Controller;
import unbbayes.controller.umpst.IconController;
import unbbayes.gui.umpst.FastHelpJFrame;
import unbbayes.gui.umpst.IUMPSTPanel;
import unbbayes.gui.umpst.MainPropertiesEditionPane;
import unbbayes.gui.umpst.UmpstModule;
import unbbayes.gui.umpst.selection.HypothesisSelectionPane;
import unbbayes.gui.umpst.selection.SubGoalSelectionPane;
import unbbayes.gui.umpst.selection.interfaces.HypothesisAddition;
import unbbayes.model.umpst.entity.AttributeModel;
import unbbayes.model.umpst.entity.EntityModel;
import unbbayes.model.umpst.entity.RelationshipModel;
import unbbayes.model.umpst.goal.GoalModel;
import unbbayes.model.umpst.goal.HypothesisModel;
import unbbayes.model.umpst.group.GroupModel;
import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.model.umpst.rule.RuleModel;
import unbbayes.util.CommonDataUtil;

/**
 * Panel for Goals Edition
 */
public class GoalsEditionPanel extends IUMPSTPanel implements HypothesisAddition{

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

	private SubGoalSelectionPane subgoalSelectionPane; 

	private HypothesisSelectionPane hypothesisSelectionPane; 

	UmpstModule janelaPai; 

	private Controller controller; 

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

		Controller controller = Controller.getInstance(umpstProject); 

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
				createSubgoalsPanel()); 

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


		if (goalFather!=null){
			title = resource.getString("ttSubGoal");
		}
		else{
			title = resource.getString("ttGoal");
		}

		// CREATE FORM 
		mainPropertiesEditionPane = 
				new MainPropertiesEditionPane(buttonCancel, 
						buttonSave, 
						title, 
						"Goals Details",
						null,
						null, false); 

		if (goal != null){
			mainPropertiesEditionPane.setTitleText(goal.getName());
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
						GoalModel goalAdd = createGoal();		

						TableGoals tableGoals = updateTableGoals();

						UmpstModule pai = getFatherPanel();
						
						changePanel(pai.getMenuPanel().getGoalsPane().getGoalsPanel().getGoalsAdd(goalAdd));
					}
				}
				// -> Update Goal
				else{
					try{
						/**Cleaning Search Map*/
						Set<GoalModel> aux = new HashSet<GoalModel>();
						GoalModel goalBeta;
						String[] strAux=goal.getName().split(" ");

						/************/

						goal.setName(mainPropertiesEditionPane.getTitleText());
						goal.setComments(mainPropertiesEditionPane.getCommentsText());
						goal.setAuthor(mainPropertiesEditionPane.getAuthorText());
						goal.setDate(mainPropertiesEditionPane.getDateText());

						updateTableGoals();

					}
					catch (Exception e2) {
						JOptionPane.showMessageDialog(null,
								resource.getString("erSaveGoal"), 
								"UnBBayes", 
								JOptionPane.WARNING_MESSAGE);
						UmpstModule pai = getFatherPanel();
						changePanel(pai.getMenuPanel());	
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
					TableGoals tableGoals = updateTableGoals();
					changeToTableGoals(tableGoals); 
				} else{
					changePanel(pai.getMenuPanel().getGoalsPane().getGoalsPanel().getGoalsAdd(goalFather));	
				}
			}
		});

		buttonHypothesis.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				changePanel(new HypothesisEditionPanel(getFatherPanel(),getUmpstProject(),goal,null,null));
			}
		});
		
		buttonHypothesis.addMouseListener(new MouseAdapter(){
		    public void mouseClicked(MouseEvent e) {
		        if (e.getButton() == 3) { 
		        	FastHelpJFrame fastHelp = new FastHelpJFrame("AddHypothesis", buttonHypothesis); 
		        	fastHelp.showHelp();	
		        }
		    }
		}); 

		buttonSubgoal.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
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

	public GoalModel createGoal(){

		Set<String> keys = getUmpstProject().getMapGoal().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);
		
		int max = 0;
		String idAux = "";
		String idAux2 = "";
		int intAux;

		if (goalFather==null){

			if ( getUmpstProject().getMapGoal().size()!=0){
				for (String key: sortedKeys){

					idAux= getUmpstProject().getMapGoal().get(key).getId();

					if (idAux.contains(".")){
						intAux = idAux.indexOf(".");
						idAux2 = idAux.substring(0, intAux);
						if (max < Integer.parseInt(idAux2)){
							max = Integer.parseInt(idAux2);
						}
					}
					else{
						if (max< Integer.parseInt(idAux)){
							max = Integer.parseInt(idAux);
						}
					}

				}
				max++;
				idAux = max+"";
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
				goalFather);

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
		}

		CommonDataUtil.getInstance().setAuthorName(mainPropertiesEditionPane.getAuthorText()); 

		getUmpstProject().getMapGoal().put(goalAdd.getId(), goalAdd);	

		return goalAdd;
	}


	public TableGoals updateTableGoals(){
		UmpstModule pai = getFatherPanel();
		TableGoals goalsTable = pai.getMenuPanel().getGoalsPane().getGoalsTable();
		goalsTable.createTable();

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
		panel.setBorder(BorderFactory.createTitledBorder(resource.getString("ttListHypothesis")));

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

	public JPanel createSubgoalsPanel(){

		TableSubGoals subgoalsTable = new TableSubGoals(getFatherPanel(),
				getUmpstProject(),
				goal);

		JTable table = subgoalsTable.createTable();
		JScrollPane scrollPane = new JScrollPane(table);

		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); 
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(resource.getString("ttListSubgoals")));

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

	public JScrollPane  createTraceabilityTable() {

		int i = 0;

		//All this magic only for calculate the number of lines... 

		//--------- Traceability table --------------
		//Entities (F) 
		//   Rules
		//   Attributes
		//   Relationship
		//   Groups
		//Subgoals    (-)
		//Hypothesis  (-)
		//Groups      

		
		//Only calculate the size of the array for insert the elements... 
		
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
		final Object[][] data;

		if (i < 30){
			data = new Object[30][2];
		}else{
			data = new Object[i+1][2];
		}
		
		String[] columnNames = {resource.getString("ttColType"), 
	            resource.getString("ttColDescription")};
		
		//Insert the elements
		i=0;

		if ( (goal!=null)&&(goal.getFowardTrackingEntity() !=null) ){
			EntityModel entity;
			Set<EntityModel> aux = goal.getFowardTrackingEntity();

			for (Iterator<EntityModel> it = aux.iterator(); it.hasNext(); ) {
				entity = it.next();

				data[i][0] = "Entity";
				data[i][1] = entity;

				i++;
				if (entity.getFowardTrackingRules()!=null){
					Set<RuleModel> auxRules = entity.getFowardTrackingRules();
					RuleModel rule;
					for (Iterator<RuleModel> itRules = auxRules.iterator(); itRules.hasNext(); ) {
						rule = itRules.next();

						data[i][0] = "Rule";
						data[i][1] = rule;
						i++;
					}
				}

				if (entity.getMapAtributes()!=null){
					Set<String> keysAtribute = entity.getMapAtributes().keySet();
					TreeSet<String> sortedKeysAtribute = new TreeSet<String>(keysAtribute);
					AttributeModel atribute;
					for(String keyAtribute : sortedKeysAtribute){

						data[i][0] = "Atribute";
						data[i][1] = entity.getMapAtributes().get(keyAtribute);
						i++;
					}
				}

				if (entity.getFowardTrackingRelationship()!=null){
					Set<RelationshipModel> auxRelationship = entity.getFowardTrackingRelationship();
					RelationshipModel relationship;
					for (Iterator<RelationshipModel> itRelationship = auxRelationship.iterator(); itRelationship.hasNext(); ) {
						relationship = itRelationship.next();

						data[i][0] = "Relationship";
						data[i][1] = relationship;
						i++;
					}
				}
				if (entity.getFowardTrackingGroups()!=null){
					Set<GroupModel> auxGroups = entity.getFowardTrackingGroups();
					GroupModel group;
					for (Iterator<GroupModel> itGroups = auxGroups.iterator(); itGroups.hasNext(); ) {
						group = itGroups.next();

						data[i][0] = "Group";
						data[i][1] = group;
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
				data[i][1] = goal.getSubgoals().get(key);
				i++;
			}
		}
		if ((goal!=null)&&(goal.getMapHypothesis()!=null)){
			Set<String> keys = goal.getMapHypothesis().keySet();
			TreeSet<String> sortedKeys = new TreeSet<String>(keys);	

			for (String key: sortedKeys){

				data[i][0] = "Hypothesis";
				data[i][1] = goal.getMapHypothesis().get(key);
				i++;
			}    	
		}

		if ( (goal!=null)&&(goal.getFowardTrackingGroups() !=null) ){
			GroupModel group;
			Set<GroupModel> aux = goal.getFowardTrackingGroups();

			for (Iterator<GroupModel> it = aux.iterator(); it.hasNext(); ) {
				group = it.next();

				data[i][0] = "Group";
				data[i][1] = group;

				i++;
			}
		}

		DefaultTableModel tableModel = new DefaultTableModel(data, columnNames);
		JTable table = new JTable(tableModel);
		table.setGridColor(Color.WHITE); 
		table.setEnabled(false); 

		table.getColumnModel().getColumn(0).setMaxWidth(100); 
		table.getColumnModel().getColumn(1).setMinWidth(1000); 

		table.setBackground(Color.WHITE); 
		JScrollPane scrollPane = new JScrollPane(table);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); 
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

		scrollPane.setBorder(BorderFactory.createTitledBorder(resource.getString("ttGoalTraceability")));

		return scrollPane; 

	}


	public Collection<HypothesisModel> getOthersHypothesisList(){

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

		List<HypothesisModel> listHypothesis = new ArrayList<HypothesisModel>(); 

		for (String key: sortedKeys){
			if(getUmpstProject().getMapGoal().get(key)!=goal){
				if(getUmpstProject().getMapGoal().get(key).getMapHypothesis()!=null){

					goalAux = getUmpstProject().getMapGoal().get(key);
					keysHypo = goalAux.getMapHypothesis().keySet();
					sortedKeysHypo = new TreeSet<String>(keysHypo);	

					for (String keyHypo : sortedKeysHypo){
						if ( goal.getMapHypothesis().get(goalAux.getMapHypothesis().get(keyHypo).getId()) == null ){
							listHypothesis.add(goalAux.getMapHypothesis().get(keyHypo));
						}
					}
				}
			}
		} 

		return listHypothesis;

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
					if (getUmpstProject().getMapGoal().get(key).getMapHypothesis().get(keyAux).getName()==hypothesisRelated){
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
		changePanel(pai.getMenuPanel().getGoalsPane().getGoalsPanel().getGoalsAdd(goal));    			
	}


	private String[] getOthersGoalsList(){

		Set<String> keys = getUmpstProject().getMapGoal().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);	

		int i=0;

		/**This is only to found the number of other goals existents in order to create 
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
					if (goal.getSubgoals().get(getUmpstProject().getMapGoal().get(key).getId()) == null){
						allOtherGoals[i] = getUmpstProject().getMapGoal().get(key).getName();
						i++;
					}
				}
				else{
					allOtherGoals[i] = getUmpstProject().getMapGoal().get(key).getName();
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
			if(getUmpstProject().getMapGoal().get(key).getName().equals(goalRelated)){	
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
		changePanel(pai.getMenuPanel().getGoalsPane().getGoalsPanel().getGoalsAdd(goal));    			
	}

	public void addHypothesisList(List<HypothesisModel> list) {
		HypothesisModel hypothesisModel = list.get(0); 
		addVinculateHypothesis(hypothesisModel.getName()); 
	}


}