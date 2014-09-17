package unbbayes.gui.umpst.group;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import unbbayes.controller.umpst.Controller;
import unbbayes.controller.umpst.IconController;
import unbbayes.gui.umpst.IUMPSTPanel;
import unbbayes.gui.umpst.MainPropertiesEditionPane;
import unbbayes.gui.umpst.TableButton;
import unbbayes.gui.umpst.TableObject;
import unbbayes.gui.umpst.UmpstModule;
import unbbayes.model.umpst.entity.AttributeModel;
import unbbayes.model.umpst.entity.EntityModel;
import unbbayes.model.umpst.entity.RelationshipModel;
import unbbayes.model.umpst.goal.HypothesisModel;
import unbbayes.model.umpst.group.GroupModel;
import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.util.CommonDataUtil;

public class GroupsEditionPanel extends IUMPSTPanel {

	private GridBagConstraints constraint  = new GridBagConstraints();
	private JLabel titulo                  = new JLabel();

	private JButton buttonSave 	     ;
	private JButton buttonCancel     ;
	private JButton buttonBackEntities     ;
	private JButton buttonBackAtributes    ;
	private JButton buttonBackRelationship ;

	private GroupModel group;

	private static final long serialVersionUID = 1L;

	private Object[][] dataBacktracking = {};
	private Object[][] dataFrame = {};

	private MainPropertiesEditionPane mainPropertiesEditionPane ; 
	
	private Controller controller; 

	/** Load resource file from this package */
	private static ResourceBundle resource = 
			unbbayes.util.ResourceController.newInstance().getBundle(
					unbbayes.gui.umpst.resources.Resources.class.getName());

	private IconController iconController = IconController.getInstance();

	public GroupsEditionPanel(UmpstModule janelaPai,
			UMPSTProject umpstProject, 
			GroupModel group){

		super(janelaPai);

		this.setUmpstProject(umpstProject);

		this.group = group;

		this.setLayout(new GridLayout(1,1));

		createButtons(); 
		
		controller = Controller.getInstance(umpstProject); 

		JSplitPane splitPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				createPanelText(),
				getBacktrackingPanel()); 

		splitPanel.setDividerLocation(320); 

		this.add(splitPanel); 

		listeners();

		titulo.setText("Group");

	}

	public JPanel createPanelText(){

		String title            = resource.getString("ttGroup");

		mainPropertiesEditionPane = 
				new MainPropertiesEditionPane(
						buttonCancel, 
						buttonSave, 
						title, 
						resource.getString("ttGroupDetails"),
						null,
						null, true); 

		if (group != null){
			mainPropertiesEditionPane.setTitleText(group.getName());
			mainPropertiesEditionPane.setCommentsText(group.getComments());
			mainPropertiesEditionPane.setAuthorText(group.getAuthor());
			mainPropertiesEditionPane.setDateText(group.getDate());
		}

		return mainPropertiesEditionPane.getPanel();
	}

	private void createButtons() {

		buttonSave	     = new JButton(iconController.getSaveObjectIcon());
		buttonSave.setText(resource.getString("btnSave"));

		if( group == null){
			buttonSave.setToolTipText(resource.getString("hpSaveGroup"));

		} else {
			buttonSave.setToolTipText(resource.getString("hpUpdateGroup"));
		}

		buttonCancel     = new JButton(iconController.getReturnIcon());
		buttonCancel.setText(resource.getString("btnReturn")); 
		buttonCancel.setToolTipText(resource.getString("HpReturnMainPanel"));

		buttonBackEntities = new JButton(iconController.getCicleEntityIcon());
		buttonBackEntities.setToolTipText(resource.getString("hpAddBackEntity"));

		buttonBackAtributes = new JButton(iconController.getCicleAttributeIcon());
		buttonBackAtributes.setToolTipText(resource.getString("hpAddBackAttribute"));

		buttonBackRelationship = new JButton(iconController.getCicleRelationshipIcon());
		buttonBackAtributes.setToolTipText(resource.getString("hpAddBackRelationship"));

	}

	public void listeners(){

		buttonSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( group == null){
					try {
						if (mainPropertiesEditionPane.getTitleText().equals("")){
							JOptionPane.showMessageDialog(null, "Group's name is empty!");
						}
						else{
							GroupModel newGroup = updateMapGroups();		
							updateTableGroups();
							changePanel(new GroupsEditionPanel(getFatherPanel(),getUmpstProject(),newGroup));
						}

					} catch (Exception e1) {
						JOptionPane.showMessageDialog(null, "Error while creating group", "UnBBayes", JOptionPane.WARNING_MESSAGE);
						UmpstModule pai = getFatherPanel();
						changePanel(pai.getMenuPanel());	

					}
				}
				else{
					if( JOptionPane.showConfirmDialog(null, "Do you want to update this group?", "UnBBayes", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION ){
						try{

							/**Cleaning Search Map*/
							Set<GroupModel> aux = new HashSet<GroupModel>();
							GroupModel groupBeta;
							String[] strAux = group.getName().split(" ");

							/************/

							group.setName(mainPropertiesEditionPane.getTitleText());
							group.setComments(mainPropertiesEditionPane.getCommentsText());
							group.setAuthor(mainPropertiesEditionPane.getAuthorText());
							group.setDate(mainPropertiesEditionPane.getDateText());

							updateTableGroups();

							JOptionPane.showMessageDialog(null, "Group successfully updated", "UnBBayes", JOptionPane.INFORMATION_MESSAGE);


						}
						catch (Exception e2) {
							JOptionPane.showMessageDialog(null,"Error while ulpdating group", "UnBBayes", JOptionPane.WARNING_MESSAGE);
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
				createFrameEntity();				
			}
		});

		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				UmpstModule pai = getFatherPanel();
				changePanel(pai.getMenuPanel());	
			}
		});


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
				mainPropertiesEditionPane.getDateText());

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
			data[i][1] = getUmpstProject().getMapGroups().get(key).getName();			
			data[i][2] = "";
			data[i][3] = "";
			i++;
		}

		UmpstModule pai = getFatherPanel();

		TableGroups groupTable = pai.getMenuPanel().getGroupsPane().getGroupsTable();
		JTable table = groupTable.createTable(columnNames,data);

		groupTable.getScrollPanePergunta().setViewportView(table);
		groupTable.getScrollPanePergunta().updateUI();
		groupTable.getScrollPanePergunta().repaint();
		groupTable.updateUI();
		groupTable.repaint();
	}

	public JPanel getBacktrackingPanel(){

		JPanel panel = new JPanel();
		JScrollPane scrollPane = new JScrollPane();

		if(group!=null){

			DefaultListModel listModel = new DefaultListModel(); 

			//Entities
			for (EntityModel entity: group.getBacktrackingEntities()) {
				listModel.addElement(entity);
			}

			//Attribute
			for (AttributeModel attribute: group.getBacktrackingAtributes()) {
				listModel.addElement(attribute); 
			}

			//Relationship
			for (RelationshipModel relationship: group.getBacktrackingRelationship()) {
				listModel.addElement(relationship); 
			}

			JList list = new JList(listModel); 

			dataBacktracking = new Object[listModel.getSize()][3];

			int i = 0;

			for (Object obj: listModel.toArray()) {
				if(obj instanceof EntityModel){
					dataBacktracking[i][0] = (EntityModel) obj;
					dataBacktracking[i][1] = "Entity";
					dataBacktracking[i][2] = "";
				}
				if(obj instanceof AttributeModel){
					dataBacktracking[i][0] = (AttributeModel) obj;
					dataBacktracking[i][1] = "Atribute";
					dataBacktracking[i][2] = "";
				}
				if(obj instanceof RelationshipModel){
					dataBacktracking[i][0] = (RelationshipModel) obj;
					dataBacktracking[i][1] = "Relationship";
					dataBacktracking[i][2] = "";
				}
				i++; 
			}

			String[] columns = {"Name","Type",""};
			DefaultTableModel model = new DefaultTableModel(dataBacktracking,columns);
			JTable table = new JTable(model);

			TableButton buttonDel = new TableButton( new TableButton.TableButtonCustomizer()
			{
				public void customize(JButton button, int row, int column){
					button.setIcon(iconController.getDeleteIcon());
				}
			});

			TableColumn buttonColumn1 = table.getColumnModel().getColumn(columns.length-1);

			buttonColumn1.setMaxWidth(TableObject.SIZE_COLUMN_BUTTON);
			buttonColumn1.setCellRenderer(buttonDel);
			buttonColumn1.setCellEditor(buttonDel);

			buttonDel.addHandler(new TableButton.TableButtonPressedHandler() {	
				public void onButtonPress(int row, int column) {
					
					EntityModel entity = (EntityModel)dataBacktracking[row][0];

					controller.removeEntityFromGroupBackTrackingList(entity, group); 
					
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
	}

	public void createFrameEntity(){

		final JFrame frame = new JFrame("Adding Backtracking from entities");
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		String[] columnNames = {"ID","Entity",""};

		dataFrame = new Object[getUmpstProject().getMapEntity().size()][3];

		Integer i=0;

		Set<String> keys = getUmpstProject().getMapEntity().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);

		for (String key: sortedKeys){
			dataFrame[i][0] = getUmpstProject().getMapEntity().get(key).getId();
			dataFrame[i][1] = getUmpstProject().getMapEntity().get(key).getName();			
			dataFrame[i][2] = "";
			i++;
		}

		DefaultTableModel model = new DefaultTableModel(dataFrame,columnNames);
		JTable table = new JTable(model);

		TableButton buttonEdit = new TableButton( new TableButton.TableButtonCustomizer(){
			public void customize(JButton button, int row, int column){
				button.setIcon(iconController.getAddIconP() );
			}
		});

		TableColumn buttonColumn1 = table.getColumnModel().getColumn(columnNames.length-1);
		buttonColumn1.setMaxWidth(TableObject.SIZE_COLUMN_BUTTON);
		buttonColumn1.setCellRenderer(buttonEdit);
		buttonColumn1.setCellEditor(buttonEdit);
		
		TableColumn indexColumn = table.getColumnModel().getColumn(0);
		indexColumn.setMaxWidth(TableObject.SIZE_COLUMN_INDEX);

		buttonEdit.addHandler(new TableButton.TableButtonPressedHandler() {	
			public void onButtonPress(int row, int column) {

				String key = dataFrame[row][0].toString();

				EntityModel entity = getUmpstProject().getMapEntity().get(key); 
				
				group.getBacktrackingEntities().add(entity);
				entity.getFowardTrackingGroups().add(group); 

				UmpstModule father = getFatherPanel();
				changePanel(father.getMenuPanel().getGroupsPane().getGroupsPanel().getGroupsAdd(group));

			}
		});

		JScrollPane scroll = new JScrollPane(table);

		panel.add(scroll,BorderLayout.CENTER);
		panel.setPreferredSize(new Dimension(400,200));

		JButton btnClose = new JButton(resource.getString("closeButton")); 
		btnClose.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				frame.dispose();
			}
		});

		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.setLayout(new GridLayout());
		toolBar.add(new JLabel());
		toolBar.add(new JLabel());
		toolBar.add(btnClose);
		
		panel.add(toolBar, BorderLayout.PAGE_END); 
		
		frame.add(panel);

		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frame.setLocationRelativeTo(buttonBackEntities);
		frame.setSize(300,200);
		frame.setVisible(true);

	}	

	public void createFrameAtributes(){

		final JFrame frame = new JFrame("Adding Backtracking from atributes");
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		String[] columnNames = {"ID","Atribute",""};

		dataFrame = new Object[getUmpstProject().getMapAtribute().size()][3];

		Integer i=0;

		Set<String> keys = getUmpstProject().getMapAtribute().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);

		for (String key: sortedKeys){
			dataFrame[i][0] = getUmpstProject().getMapAtribute().get(key).getId();
			dataFrame[i][1] = getUmpstProject().getMapAtribute().get(key).getName();			
			dataFrame[i][2] = "";
			i++;
		}

		DefaultTableModel model = new DefaultTableModel(dataFrame,columnNames);
		JTable table = new JTable(model);

		TableButton buttonEdit = new TableButton( new TableButton.TableButtonCustomizer(){
			public void customize(JButton button, int row, int column){
				button.setIcon(iconController.getAddIconP() );
			}
		});

		TableColumn buttonColumn1 = table.getColumnModel().getColumn(columnNames.length-1);
		buttonColumn1.setMaxWidth((TableObject.SIZE_COLUMN_BUTTON));
		buttonColumn1.setCellRenderer(buttonEdit);
		buttonColumn1.setCellEditor(buttonEdit);
		
		TableColumn indexColumn = table.getColumnModel().getColumn(0);
		indexColumn.setMaxWidth(TableObject.SIZE_COLUMN_INDEX);

		buttonEdit.addHandler(new TableButton.TableButtonPressedHandler() {	
			public void onButtonPress(int row, int column) {

				String key = dataFrame[row][0].toString();

				group.getBacktrackingAtributes().add(getUmpstProject().getMapAtribute().get(key));

				Set<String> keys = getUmpstProject().getMapAtribute().keySet();
				TreeSet<String> sortedKeys = new TreeSet<String>(keys);
				for (String keyAux : sortedKeys){
					if (getUmpstProject().getMapAtribute().get(keyAux).getName().equals(key)){
						getUmpstProject().getMapAtribute().get(keyAux).getFowardTrackingGroups().add(group);
					}
				}

				UmpstModule father = getFatherPanel();
				changePanel(father.getMenuPanel().getGroupsPane().getGroupsPanel().getGroupsAdd(group));
			}
		});


		JScrollPane scroll = new JScrollPane(table);

		panel.add(scroll,BorderLayout.CENTER);
		panel.setPreferredSize(new Dimension(400,200));

		JButton btnClose = new JButton(resource.getString("closeButton")); 
		btnClose.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				frame.dispose();
			}
		});

		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.setLayout(new GridLayout());
		toolBar.add(new JLabel());
		toolBar.add(new JLabel());
		toolBar.add(btnClose);
		
		panel.add(toolBar, BorderLayout.PAGE_END); 
		
		
		frame.add(panel);

		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frame.setLocationRelativeTo(buttonBackAtributes);
		frame.setSize(300,200);
		frame.setVisible(true);

	}	

	public void createFrameRelationship(){

		final JFrame frame = new JFrame("Adding Backtracking from relationship");
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		String[] columnNames = {"ID","Relationship",""};

		dataFrame = new Object[getUmpstProject().getMapRelationship().size()][3];

		Integer i=0;

		Set<String> keys = getUmpstProject().getMapRelationship().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);

		for (String key: sortedKeys){
			dataFrame[i][0] = getUmpstProject().getMapRelationship().get(key).getId();
			dataFrame[i][1] = getUmpstProject().getMapRelationship().get(key).getName();			
			dataFrame[i][2] = "";
			i++;
		}

		DefaultTableModel model = new DefaultTableModel(dataFrame,columnNames);
		JTable table = new JTable(model);

		TableButton buttonEdit = new TableButton( new TableButton.TableButtonCustomizer(){
			public void customize(JButton button, int row, int column){
				button.setIcon(iconController.getAddIconP() );
			}
		});

		TableColumn buttonColumn1 = table.getColumnModel().getColumn(columnNames.length-1);
		buttonColumn1.setMaxWidth(TableObject.SIZE_COLUMN_BUTTON);
		buttonColumn1.setCellRenderer(buttonEdit);
		buttonColumn1.setCellEditor(buttonEdit);

		TableColumn indexColumn = table.getColumnModel().getColumn(0);
		indexColumn.setMaxWidth(TableObject.SIZE_COLUMN_INDEX);
		
		
		buttonEdit.addHandler(new TableButton.TableButtonPressedHandler() {	
			public void onButtonPress(int row, int column) {

				String key = dataFrame[row][0].toString();

				group.getBacktrackingRelationship().add(getUmpstProject().getMapRelationship().get(key));

				Set<String> keys = getUmpstProject().getMapRelationship().keySet();
				TreeSet<String> sortedKeys = new TreeSet<String>(keys);
				for (String keyAux : sortedKeys){
					if (getUmpstProject().getMapRelationship().get(keyAux).getName().equals(key)){
						getUmpstProject().getMapRelationship().get(keyAux).getFowardtrackingGroups().add(group);
					}
				}
				UmpstModule father = getFatherPanel();
				changePanel(father.getMenuPanel().getGroupsPane().getGroupsPanel().getGroupsAdd(group));
			}
		});

		JScrollPane scroll = new JScrollPane(table);

//		c.gridx=0;c.gridy=0;c.weightx=0.5;c.weighty=0.5;  c.fill = GridBagConstraints.BOTH;
		panel.add(scroll,BorderLayout.CENTER);
		panel.setPreferredSize(new Dimension(400,200));

		JButton btnClose = new JButton(resource.getString("closeButton")); 
		btnClose.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				frame.dispose();
			}
		});

		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.setLayout(new GridLayout());
		toolBar.add(new JLabel());
		toolBar.add(new JLabel());
//		toolBar.add(btnSelect);
		toolBar.add(btnClose);
		
		panel.add(toolBar, BorderLayout.PAGE_END); 
		
		frame.add(panel);
		frame.setLocationRelativeTo(buttonBackRelationship);
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frame.setSize(300,200);
		frame.setVisible(true);

	}	


}