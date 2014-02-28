package unbbayes.gui.umpst.goal;


import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import unbbayes.gui.umpst.IUMPSTPanel;
import unbbayes.gui.umpst.MainPropertiesEditionPane;
import unbbayes.gui.umpst.UmpstModule;
import unbbayes.model.umpst.entities.EntityModel;
import unbbayes.model.umpst.groups.GroupModel;
import unbbayes.model.umpst.project.SearchModelGoal;
import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.model.umpst.requirements.GoalModel;


public class SubgoalsEditionPanel extends IUMPSTPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;



	private JButton buttonAdd 	     = new JButton();
	private JButton buttonCancel     = new JButton("Cancel");
	private JButton buttonHypothesis = new JButton("add Hypothesis");
	private JButton buttonSubgoal    = new JButton("add SubGoal");
	private JButton buttonBack       = new JButton("return");
	
	private MainPropertiesEditionPane mainPropertiesEditionPane ; 

	private GoalModel goal;
	private GoalModel goalFather;

	private GridBagConstraints constraints     = new GridBagConstraints();

	public SubgoalsEditionPanel(UmpstModule janelaPai,
			UMPSTProject umpstProject, 
			GoalModel goal, 
			GoalModel goalFather){
		
		super(janelaPai);

		this.setUmpstProject(umpstProject);

		this.goal = goal;
		this.goalFather = goalFather;
		
		buttonHypothesis.setToolTipText("Add new Hyphotesis");
		buttonSubgoal.setToolTipText("Add new Subgoal");
		buttonBack.setToolTipText("Return to previous goal ");
		buttonCancel.setToolTipText("Return to main panel");
		
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

		if( goal == null){
			buttonAdd.setText(" Add ");
		} else {
			buttonAdd.setText(" Update ");

		}

	}

	public JPanel createPanelText(){

		String title = ""; 
		
		if( goal == null){
			title = "Add new Subgoal";
		} else {
			title = "Update Subgoal";

		}
		
		// CREATE FORM 
		mainPropertiesEditionPane = 
				new MainPropertiesEditionPane(buttonBack, 
						buttonAdd, 
						title, 
						"Subgoals Details",
						null,
						null); 

		if (goal != null){
			mainPropertiesEditionPane.setTitleText(goal.getName());
			mainPropertiesEditionPane.setCommentsText(goal.getComments());
			mainPropertiesEditionPane.setAuthorText(goal.getAuthor());
			mainPropertiesEditionPane.setDateText(goal.getDate());
		}

		return mainPropertiesEditionPane.getPanel(); 
		
//		if (goalFather!=null){
//			c.gridx = 0; c.gridy = 6;c.gridwidth=1;
//			panel.add( new JLabel("Father Name: "), c);
//			c.gridx = 1; c.gridy = 6;c.gridwidth=2;
//			panel.add( new JLabel(goalFather.getGoalName()), c);
//		}
	}


	public void createListeners(){

		buttonAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( goal == null){

					try {
						if (mainPropertiesEditionPane.getTitleText().equals("")){
							JOptionPane.showMessageDialog(null, "Subgoals details are empty!");
						}
						else{
							GoalModel goalAdd = updateMapGoal();					    
							updateMapSearch(goalAdd);
							updateTableGoals(goalAdd);
//							JOptionPane.showMessageDialog(null, "Subgoal successfully added",null, JOptionPane.INFORMATION_MESSAGE);
						}

					} catch (Exception e1) {
						JOptionPane.showMessageDialog(null, "Error while creating subgoal", "UnBBayes", JOptionPane.WARNING_MESSAGE);
						UmpstModule pai = getFatherPanel();
						changePanel(pai.getMenuPanel());	

					}
				}
				else{
						try{
							/**Cleaning Search Map*/
							Set<GoalModel> aux = new HashSet<GoalModel>();
							GoalModel goalBeta;
							String[] strAux = goal.getName().split(" ");

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

							goal.setName(mainPropertiesEditionPane.getTitleText());
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
		});

		buttonBack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				UmpstModule pai = getFatherPanel();
				changePanel(pai.getMenuPanel().getRequirementsPane().getGoalsPanel().getGoalsAdd(goalFather));	

			}
		});

		buttonCancel.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				UmpstModule pai = getFatherPanel();
				changePanel(pai.getMenuPanel());
			}
		});

		buttonHypothesis.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				changePanel(new HypothesisEditionPanel(getFatherPanel(),getUmpstProject(),goal,null,null));

			}
		});

		buttonSubgoal.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				changePanel(new SubgoalsEditionPanel(getFatherPanel(),getUmpstProject(),null,goal));

			}
		});

	}

	public GoalModel updateMapGoal(){
		String idAux = "";
		Set<String> keys = getUmpstProject().getMapGoal().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);
		int tamanho = getUmpstProject().getMapGoal().size()+1;
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

		getUmpstProject().getMapGoal().put(goalAdd.getId(), goalAdd);	

		return goalAdd;
	}


	public void updateTableGoals(GoalModel goalUpdate){
		String[] columnNames = {"ID","Goal","","",""};



		Object[][] data = new Object[getUmpstProject().getMapGoal().size()][5];
		Integer i=0;

		Set<String> keys = getUmpstProject().getMapGoal().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);

		for (String key: sortedKeys){
			data[i][0] = getUmpstProject().getMapGoal().get(key).getId();
			data[i][1] = getUmpstProject().getMapGoal().get(key).getName();			
			data[i][2] = "";
			data[i][3] = "";
			data[i][4] = "";
			i++;
		}

		UmpstModule pai = getFatherPanel();
		changePanel(pai.getMenuPanel().getRequirementsPane().getGoalsPanel().getGoalsAdd(goalFather));

		TableGoals goalsTable = pai.getMenuPanel().getRequirementsPane().getGoalsTable();
		JTable table = goalsTable.createTable(columnNames,data);

		goalsTable.getScrollPanePergunta().setViewportView(table);
		goalsTable.getScrollPanePergunta().updateUI();
		goalsTable.getScrollPanePergunta().repaint();
		goalsTable.updateUI();
		goalsTable.repaint();
	}

	public JPanel createHypothesisTable(){

		TableHypothesis hypoTable = new TableHypothesis(getFatherPanel(),getUmpstProject(),goal);
		JTable table = hypoTable.createTable();
		JScrollPane scrollPane = new JScrollPane(table);


		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder("List of Hypothesis"));

		GridBagConstraints c = new GridBagConstraints();
		if(goal!=null){
			c.gridx = 0; c.gridy = 0; c.gridwidth=1;
			panel.add(buttonHypothesis,c);
		}
		c.fill = GridBagConstraints.BOTH;
		c.gridx=0;c.gridy=1;c.weightx=0.9;c.weighty=0.9;c.gridwidth=6;

		panel.add(scrollPane,c);
		add(panel,constraints);
		
		return panel;


	}

	public JPanel createSubgoalsTable(){

		TableSubGoals subgoalsTable = new TableSubGoals(getFatherPanel(),getUmpstProject(),goal);
		JTable table = subgoalsTable.createTable();
		JScrollPane scrollPane = new JScrollPane(table);

		//table.setBorder(BorderFactory.createTitledBorder("List of Subgoals"));

		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder("List of Subgoals"));

		GridBagConstraints c = new GridBagConstraints();

		if (goal!=null){
			c.gridx = 0; c.gridy = 0; c.gridwidth=1;
			panel.add(buttonSubgoal,c);
		}

		c.fill = GridBagConstraints.BOTH;
		c.gridx=0;c.gridy=1;c.weightx=0.9;c.weighty=0.9;c.gridwidth=6;

		panel.add(scrollPane,c);
		add(panel,constraints);
		
		return panel; 

	}

	public void updateMapSearch(GoalModel goalAdd){
		/**Upating searchPanel*/

		String[] strAux = {};
		strAux = goalAdd.getName().split(" ");
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

		/************/		    

	}

	public JScrollPane createTraceabilityTable() {
		Object[][] data = new Object[30][2];

		String[] columnNames = {"Name","Type"};

		int i = 0;

		if ( (goal!=null)&&(goal.getFowardTrackingEntity() !=null) ){
			EntityModel entity;
			Set<EntityModel> aux = goal.getFowardTrackingEntity();

			for (Iterator<EntityModel> it = aux.iterator(); it.hasNext(); ) {
				entity = it.next();
				data[i][0] = entity.getName();
				data[i][1] = "Entity";
				i++;
			}
		}

		if ((goal!=null)&&(goal.getSubgoals()!=null)){
			Set<String> keys = goal.getSubgoals().keySet();
			TreeSet<String> sortedKeys = new TreeSet<String>(keys);

			for (String key: sortedKeys){
				data[i][0] = goal.getSubgoals().get(key).getName();
				data[i][1] = "Goals";
				i++;
			}
		}
		if ((goal!=null)&&(goal.getMapHypothesis()!=null)){
			Set<String> keys = goal.getMapHypothesis().keySet();
			TreeSet<String> sortedKeys = new TreeSet<String>(keys);	

			for (String key: sortedKeys){
				data[i][0] = goal.getMapHypothesis().get(key).getName();
				data[i][1] = "Hypothesis";
				i++;
			}    	
		}

		if ( (goal!=null)&&(goal.getFowardTrackingGroups() !=null) ){
			GroupModel group;
			Set<GroupModel> aux = goal.getFowardTrackingGroups();

			for (Iterator<GroupModel> it = aux.iterator(); it.hasNext(); ) {
				group = it.next();
				data[i][0] = group.getName();
				data[i][1] = "Group";
				data[i][2] = "Direct";
				i++;
			}
		}


		DefaultTableModel tableModel = new DefaultTableModel(data, columnNames);

		JTable table = new JTable(tableModel);
		table.setEnabled(false); 
		table.setGridColor(Color.WHITE);

		JScrollPane scrollPane = new JScrollPane(table);

		scrollPane.setBorder(BorderFactory.createTitledBorder("This Goal Traceability"));

		add(scrollPane,constraints);
		
		return scrollPane;

	}




}