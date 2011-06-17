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

import unbbayes.model.umpst.entities.EntityModel;
import unbbayes.model.umpst.groups.GroupsModel;
import unbbayes.model.umpst.project.SearchModelEntity;
import unbbayes.model.umpst.project.SearchModelGoal;
import unbbayes.model.umpst.project.SearchModelGroup;
import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.model.umpst.requirements.GoalModel;


public class GroupsAdd extends IUMPSTPanel {
	
	private ImageIcon iconAtribute = createImageIcon("images/hypo.png");

	
	private GridBagConstraints constraint     = new GridBagConstraints();
	private JLabel titulo            = new JLabel();
	
	private JButton buttonAdd 	     = new JButton();
	private JButton buttonCancel     = new JButton("Cancel");
 
	
	private JTextField dateText,authorText;
	private JTextField entityText,commentsText;
	private GroupsModel group;

	private static final long serialVersionUID = 1L;
	
	private JList listEntities,listEntitiesAux; 
    private DefaultListModel listEntityModel = new DefaultListModel();
	private DefaultListModel listEntityModelAux = new DefaultListModel();
	
	private JList listHypothesis,listHypothesisAux; 
    private DefaultListModel listHypothesisModel = new DefaultListModel();
	private DefaultListModel listHypothesisModelAux = new DefaultListModel();
	
	
	public GroupsAdd(UmpstModule janelaPai, GroupsModel group){
		super(janelaPai);
		
		this.group = group;
		this.setLayout(new GridBagLayout());
		constraint.fill = GridBagConstraints.BOTH;
		constraint.gridx=0;constraint.gridy=0;constraint.weightx=0.5;constraint.weighty=0.4;
		panelText();
		constraint.gridx=0;constraint.gridy=1;constraint.weightx=0.5;constraint.weighty=0.3;
		getTrackingPanel();
		constraint.gridx=0;constraint.gridy=2;constraint.weightx=0.5;constraint.weighty=0.3;
		getTrackingPanelHypothesis();
		listeners();

		if( group == null){
			titulo.setText("Add new group");
			buttonAdd.setText(" Add ");
		} else {
			titulo.setText("Update Group");
			buttonAdd.setText(" Update ");
			entityText.setText(group.getGroupName());
			commentsText.setText(group.getComments());
			authorText.setText(group.getAuthor());
			dateText.setText(group.getDate());
		}
		
	}

	public void panelText(){
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		c.gridx = 0; c.gridy = 2;c.gridwidth = 1;
		panel.add( new JLabel("Entity Description: "), c);
		c.gridx = 0; c.gridy = 3;c.gridwidth = 1;
		panel.add( new JLabel("Comments: "), c);
		c.gridx = 0; c.gridy = 4;c.gridwidth = 1;
		panel.add( new JLabel("Author Name: "), c);
		c.gridx = 0; c.gridy = 5;c.gridwidth = 1;
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
		commentsText = new JTextField(20);
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
		
		
		c.gridx = 0; c.gridy = 6; c.gridwidth = 1;
		panel.add( buttonCancel, c);
	
		
		c.gridx = 1; c.gridy = 6; c.gridwidth = 1;
		panel.add(buttonAdd,c);
		
		panel.setBorder(BorderFactory.createTitledBorder("Rule's details"));
		
		add(panel,constraint);
	
	}
	
	
	
	
	public void listeners(){
		
		buttonAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( group == null){
					try {
						GroupsModel groupAdd = updateMapGroups();					    
					    updateMapSearch(groupAdd);
					    updateBacktracking(groupAdd);
						updateTableGroups();
						JOptionPane.showMessageDialog(null, "group successfully added",null, JOptionPane.INFORMATION_MESSAGE);
						
					
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(null, "Error while creating group", "UnBBayes", JOptionPane.WARNING_MESSAGE);
						UmpstModule pai = getFatherPanel();
						alterarJanelaAtual(pai.getMenuPanel());	
					
					}
				}
				else{
					if( JOptionPane.showConfirmDialog(null, "Do you want to update this group?", "UnBBayes", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION ){
						//EntityModel group = new EntityModel(entityText.getText(),commentsText.getText(), authorText.getText(), dateText.getText(),null);
						try{
							
							/**Cleaning Search Map*/
							Set<GroupsModel> aux = new HashSet<GroupsModel>();
							GroupsModel groupBeta;
							String[] strAux = group.getGroupName().split(" ");

						    for (int i = 0; i < strAux.length; i++) {
					    		if(UMPSTProject.getInstance().getMapSearchGroups().get(strAux[i])!=null){
					    			UMPSTProject.getInstance().getMapSearchGroups().get(strAux[i]).getRelatedGroups().remove(group);
					    			aux = UMPSTProject.getInstance().getMapSearchGroups().get(strAux[i]).getRelatedGroups();
					    	    	for (Iterator<GroupsModel> it = aux.iterator(); it.hasNext(); ) {
					    	    		groupBeta = it.next();
					    	   		}
					    		}
					    		
						    	
						    }
						    /************/
							
							group.setGroupName(entityText.getText());
							group.setComments(commentsText.getText());
							group.setAuthor(authorText.getText());
							group.setDate(dateText.getText());
							
							updateMapSearch(group);
							updateBacktracking(group);
							updateTableGroups();
							
							JOptionPane.showMessageDialog(null, "group successfully updated", "UnBBayes", JOptionPane.INFORMATION_MESSAGE);
						
							
						}
						catch (Exception e2) {
							JOptionPane.showMessageDialog(null,"Error while ulpating group", "UnBBayes", JOptionPane.WARNING_MESSAGE);
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
		
	
		
		
		entityText.addActionListener(new ActionListener() {
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
    
    public GroupsModel updateMapGroups(){
    	String idAux = "";
		int intAux = 0;
		int tamanho = UMPSTProject.getInstance().getMapGroups().size()+1;
		
		
					
			if ( UMPSTProject.getInstance().getMapGroups().size()!=0){
				idAux = tamanho+"";
			}
			else{
				idAux = "1";
			}
	
		
		GroupsModel groupAdd = new GroupsModel(idAux,entityText.getText(),commentsText.getText(), authorText.getText(), 
				dateText.getText(),null,null);
		
		
	    UMPSTProject.getInstance().getMapGroups().put(groupAdd.getId(), groupAdd);	
	    
	    return groupAdd;
    }
    
    
    public void updateTableGroups(){
    	String[] columnNames = {"ID","Group","",""};	    
	    
		Object[][] data = new Object[UMPSTProject.getInstance().getMapGroups().size()][4];
		Integer i=0;
	    
		Set<String> keys = UMPSTProject.getInstance().getMapGroups().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);
		
		for (String key: sortedKeys){
			data[i][0] = UMPSTProject.getInstance().getMapGroups().get(key).getId();
			data[i][1] = UMPSTProject.getInstance().getMapGroups().get(key).getGroupName();			
			data[i][2] = "";
			data[i][3] = "";
			i++;
		}
   
	    UmpstModule pai = getFatherPanel();
	    alterarJanelaAtual(pai.getMenuPanel());
	    
	    TableGroups groupTable = pai.getMenuPanel().getGroupsPane().getGroupsTable();
	    JTable table = groupTable.createTable(columnNames,data);
	    
	    groupTable.getScrollPanePergunta().setViewportView(table);
	    groupTable.getScrollPanePergunta().updateUI();
	    groupTable.getScrollPanePergunta().repaint();
	    groupTable.updateUI();
	    groupTable.repaint();
    }

    public void updateMapSearch(GroupsModel groupAdd){
	    /**Upating searchPanel*/
	    
	    String[] strAux = {};
	    strAux = groupAdd.getGroupName().split(" ");
	    Set<GroupsModel> groupSetSearch = new HashSet<GroupsModel>();

	    
	    for (int i = 0; i < strAux.length; i++) {
	    	if(!strAux[i].equals(" ")){
	    		if(UMPSTProject.getInstance().getMapSearchGroups().get(strAux[i])==null){
	    			groupSetSearch.add(groupAdd);
	    			SearchModelGroup searchModel = new SearchModelGroup(strAux[i], groupSetSearch);
	    			UMPSTProject.getInstance().getMapSearchGroups().put(searchModel.getKeyWord(), searchModel);
	    		}
	    		else{
	    			UMPSTProject.getInstance().getMapSearchGroups().get(strAux[i]).getRelatedGroups().add(groupAdd);
	    		}
	    	}
	    }
	    
		/************/		    

    }


	public  void getTrackingPanel(){
		JButton buttonCopy, buttonDelete;

		Box box = Box.createHorizontalBox();
		JPanel panelTracking = new JPanel();

	
		
		Set<String> keys = UMPSTProject.getInstance().getMapGoal().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);
		
		for (String key: sortedKeys){
			listEntityModel.addElement(UMPSTProject.getInstance().getMapGoal().get(key).getGoalName());
		}
		
		
		/**This IF is responsable to update the first JList with all requirements elemente MINUS those 
		 * who are already registred as backtracking.
		 * */
		if (group!=null){
			listEntitiesAux = group.getBacktrackingGoal();
			for (int i = 0; i < listEntitiesAux.getModel().getSize();i++) {
				listEntityModelAux.addElement((listEntitiesAux.getModel().getElementAt(i)));
				if (listEntityModel.contains(listEntitiesAux.getModel().getElementAt(i))){
					listEntityModel.remove(listEntityModel.indexOf(listEntitiesAux.getModel().getElementAt(i)));
				}
			}
			
		}
		
		listEntities = new JList(listEntityModel); //data has type Object[]
		listEntities.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		listEntities.setLayoutOrientation(JList.VERTICAL_WRAP);
		listEntities.setVisibleRowCount(-1);
		
		
		
		

		JScrollPane listScroller = new JScrollPane(listEntities);
		listScroller.setMinimumSize(new Dimension(300,200));
				
		box.add(listScroller);
	
	
		
		buttonCopy = new JButton("copy >>");
		box.add(buttonCopy);
		buttonCopy.addActionListener(
				new ActionListener() {
					
					public void actionPerformed(ActionEvent e) {
						listEntityModelAux.addElement(listEntities.getSelectedValue());	
						listEntityModel.removeElement(listEntities.getSelectedValue());

					}
				}
		
		);
		
		buttonDelete = new JButton("<< delete");
		box.add(buttonDelete);
		buttonDelete.addActionListener(
				new ActionListener() {
					
					public void actionPerformed(ActionEvent e) {
						listEntityModel.addElement(listEntitiesAux.getSelectedValue());	
						listEntityModelAux.removeElement(listEntitiesAux.getSelectedValue());
					}
				}
		
		);	
		
		listEntitiesAux = new JList(listEntityModelAux);

		listEntitiesAux.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		listEntitiesAux.setLayoutOrientation(JList.VERTICAL_WRAP);
		listEntitiesAux.setVisibleRowCount(-1);
	
		JScrollPane listScrollerAux = new JScrollPane(listEntitiesAux);
		listScrollerAux.setMinimumSize(new Dimension(300,200));
		box.add(listScrollerAux);
				
		box.setBorder(BorderFactory.createTitledBorder("Adding backtracking from Goals"));
		
		add(box,constraint);
		
	}
	
	public  void getTrackingPanelHypothesis(){
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
		if (group!=null){
			listHypothesisAux = group.getBacktrackingHypothesis();
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
		listHypothesisScroller.setMinimumSize(new Dimension(300,200));
				
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
		listHypothesisScrollerAux.setMinimumSize(new Dimension(300,200));
		box.add(listHypothesisScrollerAux);
				
		box.setBorder(BorderFactory.createTitledBorder("Adding backtracking from Hypothesis"));
		
		add(box,constraint);
		
	}
	
	
	
	public void updateBacktracking(GroupsModel group){
		String keyWord = "";
		Set<String> keys = UMPSTProject.getInstance().getMapGoal().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);
		
		
		
		if (listEntitiesAux !=null){
			for (int i = 0; i < listEntitiesAux.getModel().getSize();i++) {
				keyWord = listEntitiesAux.getModel().getElementAt(i).toString();
				for (String key: sortedKeys){
					if (keyWord.equals( UMPSTProject.getInstance().getMapGoal().get(key).getGoalName()) ){
						UMPSTProject.getInstance().getMapGoal().get(key).getFowardTrackingGroups().add(group);
					}			
				
				}
			}
			group.setBacktrackingGoal(listEntitiesAux);

		}
		
		if (listHypothesisAux !=null){
			for (int i = 0; i < listHypothesisAux.getModel().getSize();i++) {
				keyWord = listHypothesisAux.getModel().getElementAt(i).toString();
				for (String key: sortedKeys){
					if (keyWord.equals( UMPSTProject.getInstance().getMapHypothesis().get(key).getHypothesisName()) ){
						UMPSTProject.getInstance().getMapHypothesis().get(key).getFowardTrackingGroups().add(group);
					}			
				
				}
			}
			group.setBacktrackingHypothesis(listHypothesisAux);

		}
		
	}
	
	
}