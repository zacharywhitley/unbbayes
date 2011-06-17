package unbbayes.gui.umpst;


import java.awt.Color;
import java.awt.Font;
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
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import unbbayes.model.umpst.entities.EntityModel;
import unbbayes.model.umpst.groups.GroupsModel;
import unbbayes.model.umpst.project.SearchModelGoal;
import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.model.umpst.requirements.GoalModel;
import unbbayes.model.umpst.requirements.HypothesisModel;
import unbbayes.model.umpst.rules.RulesModel;


public class GoalsAdd extends IUMPSTPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	private  JComboBox hypothesisVinculationList;

	
	private GridBagConstraints constraints     = new GridBagConstraints();
	private JLabel titulo            = new JLabel();
	
	private JButton buttonAdd 	     = new JButton();
	private JButton buttonCancel     = new JButton("Cancel");
	private JButton buttonHypothesis = new JButton("add Hypothesis");
	private JButton buttonSubgoal    = new JButton("add SubGoal");
	private JButton buttonBack		 = new JButton("Return");
	
	private JTextField dateText,authorText;
	private JTextField goalText,commentsText;
	private GoalModel goal;
	private GoalModel goalFather;
	
	

	public GoalsAdd(UmpstModule janelaPai, GoalModel goal, GoalModel goalFather){
		super(janelaPai);
		
		
		this.goal = goal;
		this.goalFather = goalFather;
		this.setLayout(new GridBagLayout());
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridx=0; constraints.gridy = 0; constraints.weightx=0.2;constraints.weighty=0.2;	
		panelText();
		
		constraints.gridx=1; constraints.gridy=0; constraints.weightx=0.3;constraints.weighty=0.2;
		createTraceabilityTable();
		constraints.gridx=0; constraints.gridy=1; constraints.weightx=0.5;constraints.weighty=0.4;
		createSubgoalsTable();
		constraints.gridx=1; constraints.gridy=1; constraints.weightx=0.5;constraints.weighty=0.4;
		createHypothesisTable();
		
		
	
		
		listeners();

		if( goal == null){
			if (goalFather!=null){
				titulo.setText("Add new Sub-Goal");
			}
			else{
				titulo.setText("Add new Goal");
			}
			buttonAdd.setText(" Add ");
		} else {
			titulo.setText("Update Goal");
			buttonAdd.setText(" Update ");
			goalText.setText(goal.getGoalName());
			commentsText.setText(goal.getComments());
			authorText.setText(goal.getAuthor());
			dateText.setText(goal.getDate());
			
		}
		
	}

	public void panelText(){
		
		GridBagConstraints c = new GridBagConstraints();
		JPanel panel = new JPanel();
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
		panel.add( new JLabel("Goal Description: "), c);
		c.gridx = 0; c.gridy = 3;c.gridwidth=1;
		panel.add( new JLabel("Comments: "), c);
		c.gridx = 0; c.gridy = 4;c.gridwidth=1;
		panel.add( new JLabel("Author Name: "), c);
		c.gridx = 0; c.gridy = 5;c.gridwidth=1;
		panel.add( new JLabel("Date: "), c);
		
		if (goalFather!=null){
			c.gridx = 0; c.gridy = 6;c.gridwidth=1;
			panel.add( new JLabel("Father Name: "), c);
			c.gridx = 1; c.gridy = 6;c.gridwidth=2;
			panel.add( new JLabel(goalFather.getGoalName()), c);
		}
		
		
		
	
			
		goalText = new JTextField(20);
		commentsText = new JTextField(20);
		authorText = new JTextField(20);
		dateText = new JTextField(20);
 

		c.gridx = 1; c.gridy = 2;c.gridwidth=2;
		panel.add( goalText, c);
		
		c.gridx = 1; c.gridy = 3;c.gridwidth=2;
		panel.add( commentsText, c);c.gridwidth=2;
		
		c.gridx = 1; c.gridy = 4;c.gridwidth=2;
		panel.add( authorText, c);c.gridwidth=2;
		
		c.gridx = 1; c.gridy = 5;c.gridwidth=2;
		panel.add( dateText, c);c.gridwidth=2;

		
			
		c.gridx = 0; c.gridy = 7; c.gridwidth = 1;
		panel.add( buttonCancel, c);
		
		
		c.gridx = 2; c.gridy = 7;c.gridwidth=1;
		panel.add( buttonAdd, c);
		
		
		
	

		
		buttonHypothesis.setToolTipText("Add new Hyphotesis");
		buttonSubgoal.setToolTipText("Add new Subgoal");
		buttonCancel.setToolTipText("Return to main panel");
		buttonBack.setToolTipText("Return to previous goal");
		
		/*c.gridx=0; c.gridy = 9; c.gridwidth=4; c.gridheight = 4;c.fill = GridBagConstraints.BOTH;
		panel.add(createTraceabilityTable(),c);*/
		
		panel.setBorder(BorderFactory.createTitledBorder("Goals Details"));
		add(panel,constraints);
	}
	
	
	public void listeners(){
		
		buttonAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( goal == null){
					
					try {
						if (goalText.getText().equals("")){
							JOptionPane.showMessageDialog(null, "Goals details are empty!");
						}
						else{
						    GoalModel goalAdd = updateMapGoal();					    
						    updateMapSearch(goalAdd);
							updateTableGoals(goalAdd);
						  	JOptionPane.showMessageDialog(null, "Goal successfully added",null, JOptionPane.INFORMATION_MESSAGE);
						}
						
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(null, "Error while creating goal", "UnBBayes", JOptionPane.WARNING_MESSAGE);
						UmpstModule pai = getFatherPanel();
						alterarJanelaAtual(pai.getMenuPanel());	
					
					}
				}
				else{
					if( JOptionPane.showConfirmDialog(null, "Do you want to update this Goal?", "UnBBayes", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION ){
						
						try{
							/**Cleaning Search Map*/
							Set<GoalModel> aux = new HashSet<GoalModel>();
							GoalModel goalBeta;
							String[] strAux=goal.getGoalName().split(" ");

						    for (int i = 0; i < strAux.length; i++) {
					    		if(UMPSTProject.getInstance().getMapSearchGoal().get(strAux[i])!=null){
					    			UMPSTProject.getInstance().getMapSearchGoal().get(strAux[i]).getGoalsRelated().remove(goal);
					    			aux = UMPSTProject.getInstance().getMapSearchGoal().get(strAux[i]).getGoalsRelated();
					    	    	for (Iterator<GoalModel> it = aux.iterator(); it.hasNext(); ) {
					    	    		goalBeta = it.next();
					    	   		}
					    		}
					    		
						    	
						    }
						    /************/
							
							goal.setGoalName(goalText.getText());
							goal.setComments(commentsText.getText());
							goal.setAuthor(authorText.getText());
							goal.setDate(dateText.getText());
							
						
							updateMapSearch(goal);
							updateTableGoals(goal);
					
							
							JOptionPane.showMessageDialog(null, "Goal successfully updated",null, JOptionPane.INFORMATION_MESSAGE);	
						
						}
						catch (Exception e2) {
							JOptionPane.showMessageDialog(null,"Error while updating goal", "UnBBayes", JOptionPane.WARNING_MESSAGE);
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
				updateTableGoals(goal);
				//alterarJanelaAtual(pai.getMenuPanel());	
			}
		});
		buttonBack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				UmpstModule pai = getFatherPanel();
				alterarJanelaAtual(pai.getMenuPanel().getRequirementsPane().getGoalsPanel().getGoalsAdd(goalFather)	);	
				
			}
		});
		
		buttonHypothesis.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				alterarJanelaAtual(new HypothesisAdd(getFatherPanel(),goal,null,null));

			}
		});
		
		buttonSubgoal.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				alterarJanelaAtual(new SubgoalsAdd(getFatherPanel(),null,goal));

			}
		});
		

		goalText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				commentsText.requestFocus();
			}
		});
		
		commentsText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				authorText.requestFocus();
			}
		});
		
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
    
    public GoalModel updateMapGoal(){
    	String idAux = "";
    	Set<String> keys = UMPSTProject.getInstance().getMapGoal().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);
		int tamanho = UMPSTProject.getInstance().getMapGoal().size()+1;
		int maior = 0;
		String idAux2 = "";
		int intAux;
		
		if (goalFather==null){
			
			if ( UMPSTProject.getInstance().getMapGoal().size()!=0){
				for (String key: sortedKeys){
					//tamanho = tamanho - UMPSTProject.getInstance().getMapGoal().get(key).getSubgoals().size();
					idAux= UMPSTProject.getInstance().getMapGoal().get(key).getId();
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

		
		GoalModel goalAdd = new GoalModel(idAux,goalText.getText(),commentsText.getText(), authorText.getText(), 
				dateText.getText(),goalFather,null,null,null,null);
		
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
		
	    UMPSTProject.getInstance().getMapGoal().put(goalAdd.getId(), goalAdd);	
	    
	    return goalAdd;
    }
    
    
    public void updateTableGoals(GoalModel goalUpdate){
    	String[] columnNames = {"ID","Goal","","",""};
    	
    	
	    
		Object[][] data = new Object[UMPSTProject.getInstance().getMapGoal().size()][5];
		Integer i=0;
	    
		Set<String> keys = UMPSTProject.getInstance().getMapGoal().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);
		
		for (String key: sortedKeys){
			data[i][0] = UMPSTProject.getInstance().getMapGoal().get(key).getId();
			data[i][1] = UMPSTProject.getInstance().getMapGoal().get(key).getGoalName();			
			data[i][2] = "";
			data[i][3] = "";
			data[i][4] = "";
			i++;
		}
   
	    UmpstModule pai = getFatherPanel();
	    alterarJanelaAtual(pai.getMenuPanel());
	    
	    TableGoals goalsTable = pai.getMenuPanel().getRequirementsPane().getGoalsTable();
	    JTable table = goalsTable.createTable(columnNames,data);
	    
	    goalsTable.getScrollPanePergunta().setViewportView(table);
	    goalsTable.getScrollPanePergunta().updateUI();
	    goalsTable.getScrollPanePergunta().repaint();
	    goalsTable.updateUI();
	    goalsTable.repaint();
    }
    
    public void createHypothesisTable(){
    	
    	 TableHypothesis hypoTable = new TableHypothesis(getFatherPanel(),goal);
 	    JTable table = hypoTable.createTable();
 	    JScrollPane scrollPane = new JScrollPane(table);

 	    
 	    JPanel panel = new JPanel();
 	    panel.setLayout(new GridBagLayout());
 	    panel.setBorder(BorderFactory.createTitledBorder("List of Hypothesis"));
 	    
 	    GridBagConstraints c = new GridBagConstraints();
 		if(goal!=null){
 			
			c.gridx = 0; c.gridy = 0;c.gridwidth=1;
			panel.add(vinculateHypothesis() , c);
 			
 			c.gridx = 1; c.gridy = 0; c.gridwidth=1;
 			panel.add(buttonHypothesis,c);
 		}
 	    c.fill = GridBagConstraints.BOTH;
 	    c.gridx=0;c.gridy=1;c.weightx=0.9;c.weighty=0.9;c.gridwidth=6;
 	    
 	    panel.add(scrollPane,c);
 	    add(panel,constraints);

    	
    }
    
    public void createSubgoalsTable(){
    	
		/*Integer i=0;
		Integer j=0;
	    
		Set<String> keys = UMPSTProject.getInstance().getMapGoal().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);
		
		for (String key: sortedKeys){
			
			if(UMPSTProject.getInstance().getMapGoal().get(key).getId().startsWith(goal.getId()+".")){
	
				i++;
			}
			
		
		}
    	Object[][] data = new Object[i][5];

		for (String key: sortedKeys){
			
			if(UMPSTProject.getInstance().getMapGoal().get(key).getId().startsWith(goal.getId()+".")){
				data[j][0] = UMPSTProject.getInstance().getMapGoal().get(key).getId();
				data[j][1] = UMPSTProject.getInstance().getMapGoal().get(key).getGoalName();			
				data[j][2] = "";
				data[j][3] = "";
				data[j][4] = "";
				j++;
			}
			
		
		}*/
    	
	    /*
		Object[][] data = new Object[goal.getSubgoals().size()][5];
		Integer i=0;
	    
		Set<String> keys = goal.getSubgoals().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);
		
		for (String key: sortedKeys){
			data[i][0] = goal.getSubgoals().get(key).getId();
			data[i][1] = goal.getSubgoals().get(key).getGoalName();			
			data[i][2] = "";
			data[i][3] = "";
			data[i][4] = "";
			i++;
		}*/
   
	    
	    TableSubGoals subgoalsTable = new TableSubGoals(getFatherPanel(),goal);
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

    }

    public void updateMapSearch(GoalModel goalAdd){
	    /**Upating searchPanel*/
	    
	    String[] strAux = {};
	    strAux = goalAdd.getGoalName().split(" ");
	    Set<GoalModel> goalSetSearch = new HashSet<GoalModel>();

	    
	    for (int i = 0; i < strAux.length; i++) {
	    	if(!strAux[i].equals(" ")){
	    		if(UMPSTProject.getInstance().getMapSearchGoal().get(strAux[i])==null){
	    			goalSetSearch.add(goalAdd);
	    			SearchModelGoal searchModel = new SearchModelGoal(strAux[i], goalSetSearch);
	    			UMPSTProject.getInstance().getMapSearchGoal().put(searchModel.getKeyWord(), searchModel);
	    		}
	    		else{
	    			UMPSTProject.getInstance().getMapSearchGoal().get(strAux[i]).getGoalsRelated().add(goalAdd);
	    		}
	    	}
	    }
	    
		/************/		    

    }
    
    public void  createTraceabilityTable() {
		
		int i = 0;

    	
    	if ( (goal!=null)&&(goal.getFowardTrackingEntity() !=null) ){
			EntityModel entity;
			Set<EntityModel> aux = goal.getFowardTrackingEntity();
			
	    	for (Iterator<EntityModel> it = aux.iterator(); it.hasNext(); ) {
	    		entity = it.next();
	    		if (entity.getFowardTrackingRules()!=null){
	    			Set<RulesModel> auxRules = entity.getFowardTrackingRules();
	    			RulesModel rule;
	    	    	for (Iterator<RulesModel> itRules = auxRules.iterator(); it.hasNext(); ) {
	    	    		rule = itRules.next();
	    	    		i++;
	    	    	}
	    		}
	    		if (entity.getFowardTrackingGroups()!=null){
	    			Set<GroupsModel> auxGroups = entity.getFowardTrackingGroups();
	    			GroupsModel group;
	    	    	for (Iterator<GroupsModel> itGroups = auxGroups.iterator(); it.hasNext(); ) {
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
			GroupsModel group;
			Set<GroupsModel> aux = goal.getFowardTrackingGroups();
			
	    	for (Iterator<GroupsModel> it = aux.iterator(); it.hasNext(); ) {
	    		group = it.next();
	 
	    		i++;
	    	}
		}
    	
    	
    	Object[][] data = new Object[i+1][3];
    	
		String[] columnNames = {"Name","Type","Traceability"};
		i=0;
		

    	if ( (goal!=null)&&(goal.getFowardTrackingEntity() !=null) ){
			EntityModel entity;
			Set<EntityModel> aux = goal.getFowardTrackingEntity();
			
	    	for (Iterator<EntityModel> it = aux.iterator(); it.hasNext(); ) {
	    		entity = it.next();
	    		if (entity.getFowardTrackingRules()!=null){
	    			Set<RulesModel> auxRules = entity.getFowardTrackingRules();
	    			RulesModel rule;
	    	    	for (Iterator<RulesModel> itRules = auxRules.iterator(); it.hasNext(); ) {
	    	    		rule = itRules.next();
	    	    		data[i][0] = rule.getRulesName();
	    	    		data[i][1] = "Rule";
	    	    		data[i][2] = "Indirect";
	    	    		i++;
	    	    	}
	    		}
	    		if (entity.getFowardTrackingGroups()!=null){
	    			Set<GroupsModel> auxGroups = entity.getFowardTrackingGroups();
	    			GroupsModel group;
	    	    	for (Iterator<GroupsModel> itGroups = auxGroups.iterator(); it.hasNext(); ) {
	    	    		group = itGroups.next();
	    	    		data[i][0] = group.getGroupName();
	    	    		data[i][1] = "Group";
	    	    		data[i][2] = "Indirect";
	    	    		i++;
	    	    	}
	    		}
	    		data[i][0] = entity.getEntityName();
	    		data[i][1] = "Entity";
	    		data[i][2] = "Direct";

	    		i++;
	    	}
		}
		
		

		if ((goal!=null)&&(goal.getSubgoals()!=null)){
			Set<String> keys = goal.getSubgoals().keySet();
			TreeSet<String> sortedKeys = new TreeSet<String>(keys);
			
			for (String key: sortedKeys){
				data[i][0] = goal.getSubgoals().get(key).getGoalName();
				data[i][1] = "Goals";
	    		data[i][2] = "Direct";
				i++;
			}
		}
    	if ((goal!=null)&&(goal.getMapHypothesis()!=null)){
    		Set<String> keys = goal.getMapHypothesis().keySet();
			TreeSet<String> sortedKeys = new TreeSet<String>(keys);	
			
			for (String key: sortedKeys){
				data[i][0] = goal.getMapHypothesis().get(key).getHypothesisName();
				data[i][1] = "Hypothesis";
	    		data[i][2] = "Direct";
				i++;
			}    	
		}
    	
    	if ( (goal!=null)&&(goal.getFowardTrackingGroups() !=null) ){
			GroupsModel group;
			Set<GroupsModel> aux = goal.getFowardTrackingGroups();
			
	    	for (Iterator<GroupsModel> it = aux.iterator(); it.hasNext(); ) {
	    		group = it.next();
	    		data[i][0] = group.getGroupName();
	    		data[i][1] = "Group";
	    		data[i][2] = "Direct";
	    		i++;
	    	}
		}

		
		DefaultTableModel tableModel = new DefaultTableModel(data, columnNames);
		JTable table = new JTable(tableModel);

		JScrollPane scrollPane = new JScrollPane(table);
		
		scrollPane.setBorder(BorderFactory.createTitledBorder("This Goal Traceability"));
		
		add(scrollPane,constraints);
		
       }
    
    	
    	public JComboBox vinculateHypothesis(){

    	    Set<String> keys = UMPSTProject.getInstance().getMapGoal().keySet();
			TreeSet<String> sortedKeys = new TreeSet<String>(keys);	
			
			Set<String> keysHypo;
			TreeSet<String> sortedKeysHypo;
			GoalModel goalAux;
			int i=0;
			/**This is only to found the number of other hypothesis existents in order to create 
			 *     	    String[] allOtherHypothesis = new String[i];
			 * */
			for (String key: sortedKeys){
				if(UMPSTProject.getInstance().getMapGoal().get(key)!=goal){
					if(UMPSTProject.getInstance().getMapGoal().get(key).getMapHypothesis()!=null){
						
						goalAux = UMPSTProject.getInstance().getMapGoal().get(key);
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
				if(UMPSTProject.getInstance().getMapGoal().get(key)!=goal){
					if(UMPSTProject.getInstance().getMapGoal().get(key).getMapHypothesis()!=null){
						
						goalAux = UMPSTProject.getInstance().getMapGoal().get(key);
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
    	    
			
    		hypothesisVinculationList = new JComboBox(allOtherHypothesis);
    		hypothesisVinculationList.addActionListener(new ActionListener() {
    				
    				public void actionPerformed(ActionEvent e) {
    					//JOptionPane.showMessageDialog(null, "selecionou "+petList.getSelectedIndex());
    					addVinculateHypothesis((String) hypothesisVinculationList.getSelectedItem());
    				}
    			});
    		
    		return hypothesisVinculationList;
    		
    	}
    	
    	public void addVinculateHypothesis(String hypothesisRelated){
    		
    		 Set<String> keys = UMPSTProject.getInstance().getMapGoal().keySet();
 			TreeSet<String> sortedKeys = new TreeSet<String>(keys);	
 			
 			Set<String> keysHypo;
 			TreeSet<String> sortedKeysHypo;
 			GoalModel goalAux;
 			int i=0;
 			Boolean achou = false;
 		
 			for (String key: sortedKeys){
 				if(UMPSTProject.getInstance().getMapGoal().get(key).getMapHypothesis()!=null){	
					keysHypo = UMPSTProject.getInstance().getMapGoal().get(key).getMapHypothesis().keySet();
					sortedKeysHypo = new TreeSet<String>(keysHypo);
					for(String keyAux : sortedKeysHypo){
						if (UMPSTProject.getInstance().getMapGoal().get(key).getMapHypothesis().get(keyAux).getHypothesisName()==hypothesisRelated){
							updateMapHypothesis(UMPSTProject.getInstance().getMapGoal().get(key).getMapHypothesis().get(keyAux));
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
    	    	
    			goal.getMapHypothesis().put(hypothesisVinculated.getId(), hypothesisVinculated);
    			if (hypothesisVinculated.getMapSubHypothesis()!=null){
    				 Set<String> keys = hypothesisVinculated.getMapSubHypothesis().keySet();
    		 		 TreeSet<String> sortedKeys = new TreeSet<String>(keys);	
    		 		 HypothesisModel hypothesis;
		 			for (String key: sortedKeys){
		 				hypothesis = hypothesisVinculated.getMapSubHypothesis().get(key);
		 				goal.getMapHypothesis().put(hypothesis.getId(),hypothesis);
		 			}

    			}
    			//PRECISO ATUALIZAR O GOAL RELATED DA HIPOTESE QUE ESTA NO MAPA GERAL
    			
    			UmpstModule pai = getFatherPanel();
    		    alterarJanelaAtual(pai.getMenuPanel().getRequirementsPane().getGoalsPanel().getGoalsAdd(goal));    			
    	}
    		
}