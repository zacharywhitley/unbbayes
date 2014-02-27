package unbbayes.gui.umpst.group;


import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import unbbayes.gui.umpst.IUMPSTPanel;
import unbbayes.gui.umpst.MainPanel;
import unbbayes.gui.umpst.MainPropertiesEditionPane;
import unbbayes.gui.umpst.TableButton;
import unbbayes.gui.umpst.UmpstModule;
import unbbayes.model.umpst.groups.GroupModel;
import unbbayes.model.umpst.project.SearchModelGroup;
import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.util.CommonDataUtil;


public class GroupsEditionPanel extends IUMPSTPanel {

	private GridBagConstraints constraint     = new GridBagConstraints();
	private JLabel titulo            = new JLabel();

	private JButton buttonAdd 	     = new JButton();
	private JButton buttonCancel     = new JButton("Cancel");
	private JButton buttonBackEntities = new JButton("Add entity backtracking ");
	private JButton buttonBackAtributes = new JButton("Add atribute backtracking");
	private JButton buttonBackRelationship = new JButton("Add relationship backtracking");

	private GroupModel group;

	private static final long serialVersionUID = 1L;

	private JList list,listAux, listAtributeAux, listRelationshipAux; 
	private DefaultListModel listModel = new DefaultListModel();
	private DefaultListModel listModelAux = new DefaultListModel();
	private DefaultListModel listModelAtrAux = new DefaultListModel();
	private DefaultListModel listModelRltAux = new DefaultListModel();

	private Object[][] dataBacktracking = {};
	private Object[][] dataFrame = {};

	private MainPropertiesEditionPane mainPropertiesEditionPane ; 

	/** Load resource file from this package */
	private static ResourceBundle resource = 
			unbbayes.util.ResourceController.newInstance().getBundle(
					unbbayes.gui.umpst.resources.Resources.class.getName());

	public GroupsEditionPanel(UmpstModule janelaPai,
			UMPSTProject umpstProject, 
			GroupModel group){
		super(janelaPai);

		this.setUmpstProject(umpstProject);

		this.group = group;

		this.setLayout(new GridLayout(1,1));

		JSplitPane splitPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				createPanelText(),
				getBacktrackingPanel()); 

		splitPanel.setDividerLocation(300); 

		this.add(splitPanel); 

		listeners();

		if( group == null){
			titulo.setText("Add new group");
			buttonAdd.setText(" Add ");
		} else {
			titulo.setText("Update Group");
			buttonAdd.setText(" Update ");
			mainPropertiesEditionPane.setTitleText(group.getGroupName());
			mainPropertiesEditionPane.setCommentsText(group.getComments());
			mainPropertiesEditionPane.setAuthorText(group.getAuthor());
			mainPropertiesEditionPane.setDateText(group.getDate());
		}

	}

	public JPanel createPanelText(){

		String title            = resource.getString("ttGroup");

		mainPropertiesEditionPane = 
				new MainPropertiesEditionPane(
						buttonCancel, 
						buttonAdd, 
						title, 
						resource.getString("ttGroupDetails"),
						null,
						null); 

		if (group != null){
			mainPropertiesEditionPane.setTitleText(group.getGroupName());
			mainPropertiesEditionPane.setCommentsText(group.getComments());
			mainPropertiesEditionPane.setAuthorText(group.getAuthor());
			mainPropertiesEditionPane.setDateText(group.getDate());
		}

		return mainPropertiesEditionPane.getPanel();
	}



	public void listeners(){

		buttonAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( group == null){
					try {
						if (mainPropertiesEditionPane.getTitleText().equals("")){
							JOptionPane.showMessageDialog(null, "Group's name is empty!");
						}
						else{
							GroupModel groupAdd = updateMapGroups();					    
							updateMapSearch(groupAdd);
							updateTableGroups();
							JOptionPane.showMessageDialog(null, 
									"group successfully added",
									null, 
									JOptionPane.INFORMATION_MESSAGE);
						}

					} catch (Exception e1) {
						JOptionPane.showMessageDialog(null, "Error while creating group", "UnBBayes", JOptionPane.WARNING_MESSAGE);
						UmpstModule pai = getFatherPanel();
						changePanel(pai.getMenuPanel());	

					}
				}
				else{
					if( JOptionPane.showConfirmDialog(null, "Do you want to update this group?", "UnBBayes", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION ){
						//EntityModel group = new EntityModel(groupText.getText(),commentsText.getText(), authorText.getText(), dateText.getText(),null);
						try{

							/**Cleaning Search Map*/
							Set<GroupModel> aux = new HashSet<GroupModel>();
							GroupModel groupBeta;
							String[] strAux = group.getGroupName().split(" ");

							for (int i = 0; i < strAux.length; i++) {
								if(getUmpstProject().getMapSearchGroups().get(strAux[i])!=null){
									getUmpstProject().getMapSearchGroups().get(strAux[i]).getRelatedGroups().remove(group);
									aux = getUmpstProject().getMapSearchGroups().get(strAux[i]).getRelatedGroups();
									for (Iterator<GroupModel> it = aux.iterator(); it.hasNext(); ) {
										groupBeta = it.next();
									}
								}


							}
							/************/

							group.setGroupName(mainPropertiesEditionPane.getTitleText());
							group.setComments(mainPropertiesEditionPane.getCommentsText());
							group.setAuthor(mainPropertiesEditionPane.getAuthorText());
							group.setDate(mainPropertiesEditionPane.getDateText());

							updateMapSearch(group);
							updateTableGroups();

							JOptionPane.showMessageDialog(null, "group successfully updated", "UnBBayes", JOptionPane.INFORMATION_MESSAGE);


						}
						catch (Exception e2) {
							JOptionPane.showMessageDialog(null,"Error while ulpating group", "UnBBayes", JOptionPane.WARNING_MESSAGE);
							UmpstModule pai = getFatherPanel();
							changePanel(pai.getMenuPanel());	
						}
					}
				}
			}
		});

		buttonBackRelationship.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				createFrameRelationship();				
			}
		});

		buttonBackAtributes.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				createFrameAtributes();				
			}
		});

		buttonBackEntities.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				createFrame();				
			}
		});

		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				UmpstModule pai = getFatherPanel();
				changePanel(pai.getMenuPanel());	
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

	public GroupModel updateMapGroups(){
		String idAux = "";

		int tamanho = getUmpstProject().getMapGroups().size()+1;

		if ( getUmpstProject().getMapGroups().size()!=0){
			idAux = tamanho+"";
		}
		else{
			idAux = "1";
		}


		GroupModel groupAdd = new GroupModel(idAux,
				mainPropertiesEditionPane.getTitleText(),
				mainPropertiesEditionPane.getCommentsText(), 
				mainPropertiesEditionPane.getAuthorText(), 
				mainPropertiesEditionPane.getDateText(),
				null,null, null, null, null, null);

		CommonDataUtil.getInstance().setAuthorName(mainPropertiesEditionPane.getAuthorText()); 

		getUmpstProject().getMapGroups().put(groupAdd.getId(), groupAdd);	

		return groupAdd;
	}


	public void updateTableGroups(){
		String[] columnNames = {"ID","Group","",""};	    

		Object[][] data = new Object[getUmpstProject().getMapGroups().size()][4];
		Integer i=0;

		Set<String> keys = getUmpstProject().getMapGroups().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);

		for (String key: sortedKeys){
			data[i][0] = getUmpstProject().getMapGroups().get(key).getId();
			data[i][1] = getUmpstProject().getMapGroups().get(key).getGroupName();			
			data[i][2] = "";
			data[i][3] = "";
			i++;
		}

		UmpstModule pai = getFatherPanel();
		changePanel(pai.getMenuPanel());

		TableGroups groupTable = pai.getMenuPanel().getGroupsPane().getGroupsTable();
		JTable table = groupTable.createTable(columnNames,data);

		groupTable.getScrollPanePergunta().setViewportView(table);
		groupTable.getScrollPanePergunta().updateUI();
		groupTable.getScrollPanePergunta().repaint();
		groupTable.updateUI();
		groupTable.repaint();
	}

	public void updateMapSearch(GroupModel groupAdd){
		/**Upating searchPanel*/

		String[] strAux = {};
		strAux = groupAdd.getGroupName().split(" ");
		Set<GroupModel> groupSetSearch = new HashSet<GroupModel>();


		for (int i = 0; i < strAux.length; i++) {
			if(!strAux[i].equals(" ")){
				if(getUmpstProject().getMapSearchGroups().get(strAux[i])==null){
					groupSetSearch.add(groupAdd);
					SearchModelGroup searchModel = new SearchModelGroup(strAux[i], groupSetSearch);
					getUmpstProject().getMapSearchGroups().put(searchModel.getKeyWord(), searchModel);
				}
				else{
					getUmpstProject().getMapSearchGroups().get(strAux[i]).getRelatedGroups().add(groupAdd);
				}
			}
		}

		/************/		    

	}



	public JPanel getBacktrackingPanel(){

		JPanel panel = new JPanel();
		JScrollPane scrollPane = new JScrollPane();
		if(group!=null){
			listAux = group.getBacktrackingEntities();
			for (int i = 0; i < listAux.getModel().getSize();i++) {
				listModelAux.addElement((listAux.getModel().getElementAt(i)));
			}

			listAtributeAux = group.getBacktrackingAtributes();

			for (int i = 0; i < listAtributeAux.getModel().getSize();i++) {
				listModelAtrAux.addElement((listAtributeAux.getModel().getElementAt(i)));
			}

			listRelationshipAux = group.getBacktrackingRelationship();

			for (int i = 0; i < listRelationshipAux.getModel().getSize();i++) {
				listModelRltAux.addElement((listRelationshipAux.getModel().getElementAt(i)));
			}

			listAux = new JList(listModelAux);
			listAtributeAux= new JList(listModelAtrAux);
			listRelationshipAux = new JList(listModelRltAux);

			dataBacktracking = new Object[listAux.getModel().getSize()+listAtributeAux.getModel().getSize()+listRelationshipAux.getModel().getSize()][3];

			int i;
			for (i = 0; i < listAux.getModel().getSize(); i++) {
				dataBacktracking[i][0] = listAux.getModel().getElementAt(i);
				dataBacktracking[i][1] = "Entity";
				dataBacktracking[i][2] = "";

			}
			int j;
			for (j = 0; j < listAtributeAux.getModel().getSize(); j++) {
				dataBacktracking[j+i][0] = listAtributeAux.getModel().getElementAt(j);
				dataBacktracking[j+i][1] = "Atribute";
				dataBacktracking[j+i][2] = "";

			}
			int k;
			for (k = 0; k < listRelationshipAux.getModel().getSize(); k++) {
				dataBacktracking[k+j+i][0] = listRelationshipAux.getModel().getElementAt(k);
				dataBacktracking[k+j+i][1] = "Relationship";
				dataBacktracking[k+j+i][2] = "";

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
					if (row<listAux.getModel().getSize()){
						String key = dataBacktracking[row][0].toString();
						listModelAux.remove(listModelAux.indexOf(key));
						listAux = new JList(listModelAux);
						group.setBacktrackingEntities(listAux);
					}
					else{
						if (row<(listAux.getModel().getSize()+listAtributeAux.getModel().getSize())){
							String keyAtr = dataBacktracking[row][0].toString();
							listModelAtrAux.remove(listModelAtrAux.indexOf(keyAtr));
							listAtributeAux = new JList(listModelAtrAux);
							group.setBacktrackingAtributes(listAtributeAux);
						}
						else{
							String keyAtr = dataBacktracking[row][0].toString();
							listModelRltAux.remove(listModelRltAux.indexOf(keyAtr));
							listRelationshipAux = new JList(listModelRltAux);
							group.setBacktrackingRelationship(listRelationshipAux);
						}
					}
					UmpstModule father = getFatherPanel();
					changePanel(father.getMenuPanel().getGroupsPane().getGroupsPanel().getGroupsAdd(group));
				}
			});

			panel = new JPanel();
			panel.setLayout(new GridBagLayout());

			GridBagConstraints c = new GridBagConstraints();

			if (group!=null){
				c.gridx = 0; c.gridy = 0; c.gridwidth=1;
				panel.add(buttonBackEntities,c);
				buttonBackEntities.setToolTipText("Add backtracking from entities");

				c.gridx = 1; c.gridy = 0; c.gridwidth=1;
				panel.add(buttonBackAtributes,c);
				buttonBackAtributes.setToolTipText("Add backtracking from atributes");

				c.gridx = 2; c.gridy = 0; c.gridwidth=1;
				panel.add(buttonBackRelationship,c);
				buttonBackRelationship.setToolTipText("Add backtracking from relationship");
			}

			c.fill = GridBagConstraints.BOTH;
			c.gridx=0;c.gridy=1;c.weightx=0.9;c.weighty=0.9;c.gridwidth=6;

			scrollPane = new JScrollPane(table);

			panel.add(scrollPane,c);
		}

		return panel;
		//add(box,constraint);

	}

	/*public void updateBacktracking(groupsModel group){
		String keyWord = "";
		Set<String> keys = getUmpstProject().getMapEntity().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);



		if (listAux !=null){
			for (int i = 0; i < listAux.getModel().getSize();i++) {
				keyWord = listAux.getModel().getElementAt(i).toString();
				for (String key: sortedKeys){
					if (keyWord.equals( getUmpstProject().getMapEntity().get(key).getEntityName()) ){
						getUmpstProject().getMapEntity().get(key).getFowardTrackinggroups().add(group);
					}			

				}
			}
			group.setBacktracking(listAux);

		}

	}*/

	public void createFrame(){

		JFrame frame = new JFrame("Adding Backtracking from entities");
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();


		String[] columnNames = {"ID","Entity",""};

		dataFrame = new Object[getUmpstProject().getMapEntity().size()][3];


		Integer i=0;

		Set<String> keys = getUmpstProject().getMapEntity().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);

		for (String key: sortedKeys){
			dataFrame[i][0] = getUmpstProject().getMapEntity().get(key).getId();
			dataFrame[i][1] = getUmpstProject().getMapEntity().get(key).getEntityName();			
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
				list = group.getBacktrackingEntities();

				listModel.addElement(key);
				list = new JList(listModel);
				group.setBacktrackingEntities(list);

				Set<String> keys = getUmpstProject().getMapEntity().keySet();
				TreeSet<String> sortedKeys = new TreeSet<String>(keys);
				for (String keyAux : sortedKeys){
					if (getUmpstProject().getMapEntity().get(keyAux).getEntityName().equals(key)){
						getUmpstProject().getMapEntity().get(keyAux).getFowardTrackingGroups().add(group);
					}
				}


				UmpstModule father = getFatherPanel();
				changePanel(father.getMenuPanel().getGroupsPane().getGroupsPanel().getGroupsAdd(group));

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

	public void createFrameAtributes(){

		JFrame frame = new JFrame("Adding Backtracking from atributes");
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();


		String[] columnNames = {"ID","Atribute",""};

		dataFrame = new Object[getUmpstProject().getMapAtribute().size()][3];


		Integer i=0;

		Set<String> keys = getUmpstProject().getMapAtribute().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);

		for (String key: sortedKeys){
			dataFrame[i][0] = getUmpstProject().getMapAtribute().get(key).getId();
			dataFrame[i][1] = getUmpstProject().getMapAtribute().get(key).getAtributeName();			
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
				list = group.getBacktrackingAtributes();

				listModel.addElement(key);
				list = new JList(listModel);
				group.setBacktrackingAtributes(list);

				Set<String> keys = getUmpstProject().getMapAtribute().keySet();
				TreeSet<String> sortedKeys = new TreeSet<String>(keys);
				for (String keyAux : sortedKeys){
					if (getUmpstProject().getMapAtribute().get(keyAux).getAtributeName().equals(key)){
						getUmpstProject().getMapAtribute().get(keyAux).getFowardTrackingGroups().add(group);
					}
				}


				UmpstModule father = getFatherPanel();
				changePanel(father.getMenuPanel().getGroupsPane().getGroupsPanel().getGroupsAdd(group));

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

	public void createFrameRelationship(){

		JFrame frame = new JFrame("Adding Backtracking from relationship");
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();


		String[] columnNames = {"ID","Relationship",""};

		dataFrame = new Object[getUmpstProject().getMapRelationship().size()][3];


		Integer i=0;

		Set<String> keys = getUmpstProject().getMapRelationship().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);

		for (String key: sortedKeys){
			dataFrame[i][0] = getUmpstProject().getMapRelationship().get(key).getId();
			dataFrame[i][1] = getUmpstProject().getMapRelationship().get(key).getRelationshipName();			
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
				list = group.getBacktrackingRelationship();

				listModel.addElement(key);
				list = new JList(listModel);
				group.setBacktrackingRelationship(list);

				Set<String> keys = getUmpstProject().getMapRelationship().keySet();
				TreeSet<String> sortedKeys = new TreeSet<String>(keys);
				for (String keyAux : sortedKeys){
					if (getUmpstProject().getMapRelationship().get(keyAux).getRelationshipName().equals(key)){
						getUmpstProject().getMapRelationship().get(keyAux).getFowardtrackingGroups().add(group);
					}
				}


				UmpstModule father = getFatherPanel();
				changePanel(father.getMenuPanel().getGroupsPane().getGroupsPanel().getGroupsAdd(group));

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