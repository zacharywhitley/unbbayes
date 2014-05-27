package unbbayes.gui.umpst.rule;

import java.awt.BorderLayout;
import java.awt.Color;
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

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
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
import unbbayes.model.umpst.group.GroupModel;
import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.model.umpst.rule.RuleModel;
import unbbayes.util.CommonDataUtil;

public class RulesEditionPanel extends IUMPSTPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private GridBagConstraints constraints     = new GridBagConstraints();

	private JButton buttonSave 	          ;
	private JButton buttonCancel          ;

	private JButton buttonBackEntities    ;
	private JButton buttonBackAtributes   ;
	private JButton buttonBackRelationship;
	private JButton buttonBackRules       ;

	private RuleModel rule;

	private JButton buttonCopy, 
	buttonDelete;

	private JComboBox ruleTypeText;

	private JList list;

	private Object[][] dataBacktracking = {};
	private Object[][] dataFrame = {};

	private MainPropertiesEditionPane mainPropertiesEditionPane ; 

	private Controller controller; 

	/** Load resource file from this package */
	private static ResourceBundle resource = 
			unbbayes.util.ResourceController.newInstance().getBundle(
					unbbayes.gui.umpst.resources.Resources.class.getName());

	private IconController iconController = IconController.getInstance();

	public RulesEditionPanel(UmpstModule fatherWindow,
			UMPSTProject umpstProject, 
			RuleModel rule){

		super(fatherWindow);

		this.setUmpstProject(umpstProject);

		this.rule = rule;

		this.setLayout(new GridLayout(1,1));

		controller = Controller.getInstance(umpstProject); 

		createButtons(); 

		JSplitPane splitPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				createPanelText(),
				createBacktrackingPanel()); 

		splitPanel.setDividerLocation(320); 

		JSplitPane mainSplitPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
				splitPanel,
				createTraceabilityTable()); 

		mainSplitPanel.setDividerLocation(600); 

		createListeners();

		this.add(mainSplitPanel); 

	}

	public JPanel createPanelText(){

		String title            = resource.getString("ttRule");

		String[] possibleStates = {
				resource.getString("tpDeterministic"),
				resource.getString("tpNoDeterministic") 
		};

		ruleTypeText = new JComboBox(possibleStates); 

		mainPropertiesEditionPane = 
				new MainPropertiesEditionPane(
						buttonCancel, 
						buttonSave, 
						title, 
						resource.getString("ttRuleDetails"),
						new JLabel(resource.getString("ttType")),
						ruleTypeText, false); 

		if (rule != null){
			mainPropertiesEditionPane.setTitleText(rule.getName());
			mainPropertiesEditionPane.setCommentsText(rule.getComments());
			mainPropertiesEditionPane.setAuthorText(rule.getAuthor());
			mainPropertiesEditionPane.setDateText(rule.getDate());
			ruleTypeText.setSelectedItem((String)rule.getRuleType());
		}

		return mainPropertiesEditionPane.getPanel();
	}

	private void createButtons(){

		buttonSave 	           = new JButton(iconController.getSaveObjectIcon());
		buttonSave.setText(resource.getString("btnSave"));

		if( rule == null){
			buttonSave.setToolTipText(resource.getString("hpSaveRule"));

		} else {
			buttonSave.setToolTipText(resource.getString("hpUpdateRule"));
		}

		buttonCancel     = new JButton(iconController.getReturnIcon());
		buttonCancel.setText(resource.getString("btnReturn")); 

		buttonCancel.setToolTipText(resource.getString("hpReturnMainPanel"));

		buttonBackEntities     = new JButton(iconController.getCicleEntityIcon());
		buttonBackAtributes    = new JButton(iconController.getCicleAttributeIcon());
		buttonBackRelationship = new JButton(iconController.getCicleRelationshipIcon());
		buttonBackRules        = new JButton(iconController.getCicleRuleIcon());

	}

	private void createListeners(){

		buttonSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( rule == null){

					try {
						if (mainPropertiesEditionPane.getTitleText().equals("")){
							JOptionPane.showMessageDialog(null, 
									"Rule's name is empty");
						}
						else{
							RuleModel newRule = updateMapRules();		
							updateTableRules();

							changePanel(new RulesEditionPanel(getFatherPanel(),getUmpstProject(),newRule));
						}

					} catch (Exception e1) {
						JOptionPane.showMessageDialog(null, 
								"Error while creating rule", 
								"UnBBayes", 
								JOptionPane.WARNING_MESSAGE);
						UmpstModule pai = getFatherPanel();
						changePanel(pai.getMenuPanel());	
						e1.printStackTrace();

					}
				}
				else{
					if( JOptionPane.showConfirmDialog(null, 
							"Do you want to update this Rule?", 
							"UnBBayes", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION ){

						try{
							/**Cleaning Search Map*/
							Set<RuleModel> aux = new HashSet<RuleModel>();
							RuleModel rulesBeta;
							String[] strAux=rule.getName().split(" ");

							/************/

							rule.setName(mainPropertiesEditionPane.getTitleText());
							//							rule.setRuleType(typeText.getText());
							rule.setComments(mainPropertiesEditionPane.getCommentsText());
							rule.setAuthor(mainPropertiesEditionPane.getAuthorText());
							rule.setDate(mainPropertiesEditionPane.getDateText());

							updateTableRules();

							JOptionPane.showMessageDialog(null, 
									"Rule successfully updated",
									null, 
									JOptionPane.INFORMATION_MESSAGE);	

						}
						catch (Exception e2) {
							JOptionPane.showMessageDialog(null,
									"Error while updating rule", 
									"UnBBayes", 
									JOptionPane.WARNING_MESSAGE);
							UmpstModule pai = getFatherPanel();
							changePanel(pai.getMenuPanel());
							e2.printStackTrace();
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

		buttonBackEntities.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				createFrameEntities();				
			}
		});

		buttonBackAtributes.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				createFrameAtributes();				
			}
		});

		buttonBackRules.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				createFrameRules();				
			}
		});
		
		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				UmpstModule pai = getFatherPanel();
				changePanel(pai.getMenuPanel());	
			}
		});
		


	}

	public JScrollPane createTraceabilityTable(){

		int i = 0;
		int qtdeElements = 0; 

		String[] columns = {"Name", "Type"};

		Object[][] data; 

		if(rule != null){

			qtdeElements = rule.getFatherRuleList().size() 
					     + rule.getGroupList().size(); 

			data = new Object[qtdeElements][2];

			if (qtdeElements < 30){
				data = new Object[30][3];
			}

			for(RuleModel r: rule.getFatherRuleList()){
				data[i][0] = r.getName();
				data[i][1] = "Rule";
				i++;
			}

			for(GroupModel g: rule.getGroupList()){
				data[i][0] = g.getName();
				data[i][1] = "Group";
				i++;
			}

		}else{
			data = new Object[30][2];
		}

		DefaultTableModel model = new DefaultTableModel(data, columns);
		JTable table = new JTable(model);
		table.setGridColor(Color.WHITE); 
		table.setEnabled(false); 

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setBorder(BorderFactory.createTitledBorder("This Rule Traceability"));

		return scrollPane;

	}

	public RuleModel updateMapRules(){
		String idAux = "";
		Set<String> keys = getUmpstProject().getMapRules().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);
		int tamanho = getUmpstProject().getMapRules().size()+1;
		int maior = 0;
		String idAux2 = "";
		int intAux;

		if ( getUmpstProject().getMapRules().size()!=0){
			for (String key: sortedKeys){
				idAux= getUmpstProject().getMapRules().get(key).getId();
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

		RuleModel rulesAdd = new RuleModel(idAux,
				mainPropertiesEditionPane.getTitleText(),
				(String)ruleTypeText.getSelectedItem(), 
				mainPropertiesEditionPane.getCommentsText(), 
				mainPropertiesEditionPane.getAuthorText(), 
				mainPropertiesEditionPane.getDateText());

		CommonDataUtil.getInstance().setAuthorName(mainPropertiesEditionPane.getAuthorText()); 

		getUmpstProject().getMapRules().put(rulesAdd.getId(), rulesAdd);	

		return rulesAdd;
	}


	public void updateTableRules(){
		String[] columnNames = {"ID","Rule","",""};

		Object[][] data = new Object[getUmpstProject().getMapRules().size()][4];
		Integer i=0;

		Set<String> keys = getUmpstProject().getMapRules().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);

		for (String key: sortedKeys){
			data[i][0] = getUmpstProject().getMapRules().get(key).getId();
			data[i][1] = getUmpstProject().getMapRules().get(key).getName();			
			data[i][2] = "";
			data[i][3] = "";
			i++;
		}

		UmpstModule pai = getFatherPanel();
		//	    changePanel(pai.getMenuPanel());

		TableRules rulesTable = pai.getMenuPanel().getRulesPane().getRulesTable();
		JTable table = rulesTable.createTable(columnNames,data);

		rulesTable.getScrollPanePergunta().setViewportView(table);
		rulesTable.getScrollPanePergunta().updateUI();
		rulesTable.getScrollPanePergunta().repaint();
		rulesTable.updateUI();
		rulesTable.repaint();
	}

	//    public void createTrackingPanel(){
	//		Box box = Box.createHorizontalBox();
	//		Set<String> keys = getUmpstProject().getMapEntity().keySet();
	//		TreeSet<String> sortedKeys = new TreeSet<String>(keys);
	//		
	//		for (String key: sortedKeys){
	//			listModel.addElement(getUmpstProject().getMapEntity().get(key).getEntityName());
	//		}
	//		
	//		/**This IF is responsable to update the first JList with all requirements elemente MINUS those 
	//		 * who are already registred as backtracking.
	//		 * */
	//		if (rule!=null){
	//			listAux = rule.getBacktracking();
	//			for (int i = 0; i < listAux.getModel().getSize();i++) {
	//				listModelAux.addElement((listAux.getModel().getElementAt(i)));
	//				if (listModel.contains(listAux.getModel().getElementAt(i))){
	//					listModel.remove(listModel.indexOf(listAux.getModel().getElementAt(i)));
	//				}
	//			}
	//			
	//		}
	//		
	//		list = new JList(listModel); //data has type Object[]
	//		list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
	//		list.setLayoutOrientation(JList.VERTICAL_WRAP);
	//		list.setVisibleRowCount(-1);
	//
	//		JScrollPane listScroller = new JScrollPane(list);
	//		listScroller.setMinimumSize(new Dimension(300,200));
	//				
	//		box.add(listScroller);
	//		
	//		buttonCopy = new JButton("copy >>");
	//		box.add(buttonCopy);
	//		buttonCopy.addActionListener(
	//				new ActionListener() {
	//					
	//					public void actionPerformed(ActionEvent e) {
	//						listModelAux.addElement(list.getSelectedValue());	
	//						listModel.removeElement(list.getSelectedValue());
	//
	//					}
	//				}
	//		
	//		);
	//		
	//		buttonDelete = new JButton("<< delete");
	//		box.add(buttonDelete);
	//		buttonDelete.addActionListener(
	//				new ActionListener() {
	//					
	//					public void actionPerformed(ActionEvent e) {
	//						listModel.addElement(listAux.getSelectedValue());	
	//						listModelAux.removeElement(listAux.getSelectedValue());
	//					}
	//				}
	//		
	//		);	
	//		
	//		listAux = new JList(listModelAux);
	//
	//		listAux.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
	//		listAux.setLayoutOrientation(JList.VERTICAL_WRAP);
	//		listAux.setVisibleRowCount(-1);
	//	
	//		JScrollPane listScrollerAux = new JScrollPane(listAux);
	//		listScrollerAux.setMinimumSize(new Dimension(300,200));
	//		box.add(listScrollerAux);
	//
	//		box.setBorder(BorderFactory.createTitledBorder("Adding backtracking(Entity)"));
	//		add(box,constraints);
	//		
	//	}

	public JPanel createBacktrackingPanel(){

		JPanel panel = new JPanel();
		JScrollPane scrollPane = new JScrollPane();

		final DefaultListModel<EntityModel> listModelEntity            = new DefaultListModel<EntityModel>();
		final DefaultListModel<AttributeModel> listModelAttribute      = new DefaultListModel<AttributeModel>();
		final DefaultListModel<RelationshipModel> listModelRelationhip = new DefaultListModel<RelationshipModel>();
		final DefaultListModel<RuleModel> listModelRule = new DefaultListModel<RuleModel>();
		
		final JList listEntity             ;
		final JList listAtribute           ;
		final JList listRelationship       ;
		final JList listRule               ;
		
		if(rule!=null){

			for (EntityModel entity: rule.getEntityList()) {
				listModelEntity.addElement(entity);
			}

			for (AttributeModel attribute: rule.getAttributeList()) {
				listModelAttribute.addElement(attribute);
			}

			for (RelationshipModel relationship: rule.getRelationshipList()) {
				listModelRelationhip.addElement(relationship);
			}

			for (RuleModel ruleChildren: rule.getChildrenRuleList()) {
				listModelRule.addElement(ruleChildren);
			}
			
			listEntity             = new JList(listModelEntity);
			listAtribute           = new JList(listModelAttribute);
			listRelationship       = new JList(listModelRelationhip);
			listRule               = new JList(listModelRule); 

			dataBacktracking = new Object[listEntity.getModel().getSize() + 
			                              listAtribute.getModel().getSize() + 
			                              listRelationship.getModel().getSize()+ 
			                              listRule.getModel().getSize()][3];

			int i;
			for (i = 0; i < listEntity.getModel().getSize(); i++) {
				dataBacktracking[i][0] = listEntity.getModel().getElementAt(i);
				dataBacktracking[i][1] = "Entity";
				dataBacktracking[i][2] = "";

			}
			int j;
			for (j = 0; j < listAtribute.getModel().getSize(); j++) {
				dataBacktracking[j+i][0] = listAtribute.getModel().getElementAt(j);
				dataBacktracking[j+i][1] = "Atribute";
				dataBacktracking[j+i][2] = "";

			}
			int k;
			for (k = 0; k < listRelationship.getModel().getSize(); k++) {
				dataBacktracking[k+j+i][0] = listRelationship.getModel().getElementAt(k);
				dataBacktracking[k+j+i][1] = "Relationship";
				dataBacktracking[k+j+i][2] = "";
			}

			int w;
			for (w = 0; w < listRule.getModel().getSize(); w++) {
				dataBacktracking[k+j+i+w][0] = listRule.getModel().getElementAt(w);
				dataBacktracking[k+j+i+w][1] = "Rule";
				dataBacktracking[k+j+i+w][2] = "";
			}
			
			String[] columns = {"Name","Type",""};
			DefaultTableModel model = new DefaultTableModel(dataBacktracking,columns);
			JTable table = new JTable(model);

			TableButton buttonDel = new TableButton( new TableButton.TableButtonCustomizer()
			{
				public void customize(JButton button, int row, int column)
				{
					button.setIcon(iconController.getDeleteIcon());

				}
			});

			TableColumn buttonColumn1 = table.getColumnModel().getColumn(columns.length-1);

			buttonColumn1.setMaxWidth(28);
			buttonColumn1.setCellRenderer(buttonDel);
			buttonColumn1.setCellEditor(buttonDel);

			buttonDel.addHandler(new TableButton.TableButtonPressedHandler() {	
				public void onButtonPress(int row, int column) {
					if (row < listEntity.getModel().getSize()){
						String key = dataBacktracking[row][0].toString();
						EntityModel entityRemoved = 
								listModelEntity.remove(listModelEntity.indexOf(key));
						rule.removeBacktrackingEntity(entityRemoved);
					}
					else{
						if (row < (listEntity.getModel().getSize()+listAtribute.getModel().getSize())){
							String keyAtr = dataBacktracking[row][0].toString();
							AttributeModel attributeRemoved = 
									listModelAttribute.remove(listModelAttribute.indexOf(keyAtr));
							rule.removeBacktrackingAttibute(attributeRemoved);
						}
						else{
							if (row < (listEntity.getModel().getSize()+
									listAtribute.getModel().getSize()) +
									listRelationship.getModel().getSize()){
								String keyAtr = dataBacktracking[row][0].toString();
								RelationshipModel relationshipRemoved = 
										listModelRelationhip.remove(listModelRelationhip.indexOf(keyAtr));
								rule.removeBacktrackingRelationship(relationshipRemoved);
							}else{
								String keyAtr = dataBacktracking[row][0].toString();
								RuleModel ruleRemoved = listModelRule.remove(listModelRelationhip.indexOf(keyAtr));
								controller.removeRuleFromRuleBackTrackingList(ruleRemoved, rule); 
							}
						}
					}
					UmpstModule father = getFatherPanel();
					changePanel(father.getMenuPanel().getRulesPane().getRulesPanel().getRulesAdd(rule));
				}
			});

			panel = new JPanel();
			panel.setLayout(new GridBagLayout());

			GridBagConstraints c = new GridBagConstraints();

			if (rule!=null){
				c.gridx = 0; c.gridy = 0; c.gridwidth=1;
				panel.add(buttonBackEntities,c);
				buttonBackEntities.setToolTipText("Add backtracking from entities");

				c.gridx = 1; c.gridy = 0; c.gridwidth=1;
				panel.add(buttonBackAtributes,c);
				buttonBackAtributes.setToolTipText("Add backtracking from atributes");

				c.gridx = 2; c.gridy = 0; c.gridwidth=1;
				panel.add(buttonBackRelationship,c);
				buttonBackRelationship.setToolTipText("Add backtracking from relationship");

				c.gridx = 3; c.gridy = 0; c.gridwidth=1;
				panel.add(new JPanel(),c);

				c.gridx = 4; c.gridy = 0; c.gridwidth=1;
				panel.add(buttonBackRules,c);
				buttonBackRelationship.setToolTipText("Add backtracking from rules");
			}

			c.fill = GridBagConstraints.BOTH;
			c.gridx=0;c.gridy=1;c.weightx=0.9;c.weighty=0.9;c.gridwidth=6;

			scrollPane = new JScrollPane(table);

			panel.add(scrollPane,c);
		}

		panel.setBorder(BorderFactory.createTitledBorder("List of backtrackings"));

		return panel;

	}

	public void createFrameEntities(){

		final JFrame frame = new JFrame("Entities");
		JPanel panel = new JPanel();

		panel.setLayout(new BorderLayout());
		//		GridBagConstraints c = new GridBagConstraints();

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

		TableButton buttonEdit = new TableButton( new TableButton.TableButtonCustomizer()
		{
			public void customize(JButton button, int row, int column)
			{
				button.setIcon(iconController.getAddIconP());

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

				controller.addEntityToRuleBackTrackingList(entity, rule); 

				UmpstModule father = getFatherPanel();
				changePanel(father.getMenuPanel().getRulesPane().getRulesPanel().getRulesAdd(rule));

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

		frame.setLocationRelativeTo(buttonBackEntities);
		//		frame.setLocationByPlatform(true); 
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frame.setSize(300,200);
		frame.setVisible(true);

	}	

	public void createFrameAtributes(){

		final JFrame frame = new JFrame("Attributes");
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

		TableButton buttonEdit = new TableButton( new TableButton.TableButtonCustomizer()
		{
			public void customize(JButton button, int row, int column)
			{
				button.setIcon(iconController.getAddIconP());

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

				AttributeModel attribute = getUmpstProject().getMapAtribute().get(key); 

				controller.addAttributeToRuleBackTrackingList(attribute, rule); 

				UmpstModule father = getFatherPanel();
				changePanel(father.getMenuPanel().getRulesPane().getRulesPanel().getRulesAdd(rule));

			}
		});

		JScrollPane scroll = new JScrollPane(table);

		panel.add(scroll,BorderLayout.CENTER);

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

		panel.setPreferredSize(new Dimension(400,200));

		frame.add(panel);

		frame.setLocationRelativeTo(buttonBackAtributes);
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frame.setSize(300,200);
		frame.setVisible(true);

	}	

	public void createFrameRules(){

		final JFrame frame = new JFrame("Rules");
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		String[] columnNames = {"ID","Rule",""};

		dataFrame = new Object[getUmpstProject().getMapRules().size()][3];

		Integer i=0;

		Set<String> keys = getUmpstProject().getMapRules().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);

		for (String key: sortedKeys){
			dataFrame[i][0] = getUmpstProject().getMapRules().get(key).getId();
			dataFrame[i][1] = getUmpstProject().getMapRules().get(key).getName();			
			dataFrame[i][2] = "";
			i++;
		}

		DefaultTableModel model = new DefaultTableModel(dataFrame,columnNames);
		JTable table = new JTable(model);

		TableButton buttonEdit = new TableButton( new TableButton.TableButtonCustomizer()
		{
			public void customize(JButton button, int row, int column)
			{
				button.setIcon(iconController.getAddIconP());

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

				RuleModel ruleChildren = getUmpstProject().getMapRules().get(key); 

				controller.addRuleToRuleBackTrackingList(ruleChildren, rule); 

				UmpstModule father = getFatherPanel();
				changePanel(father.getMenuPanel().getRulesPane().getRulesPanel().getRulesAdd(rule));

			}
		});

		JScrollPane scroll = new JScrollPane(table);

		panel.add(scroll,BorderLayout.CENTER);

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

		panel.setPreferredSize(new Dimension(800,200));

		frame.add(panel);

		frame.setLocationRelativeTo(buttonBackAtributes);
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frame.setSize(800,200);
		frame.setVisible(true);

	}	
	
	public void createFrameRelationship(){

		final JFrame frame = new JFrame("Relationships");
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		//		GridBagConstraints c = new GridBagConstraints();

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
				button.setIcon(iconController.getAddIconP());

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

				RelationshipModel relationship = getUmpstProject().getMapRelationship().get(key); 

				controller.addRelationshipToRuleBackTrackingList(relationship, rule); 

				UmpstModule father = getFatherPanel();
				changePanel(father.getMenuPanel().getRulesPane().getRulesPanel().getRulesAdd(rule));

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

		//		frame.setLocationByPlatform(true); 
		frame.setLocationRelativeTo(buttonBackRelationship);
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frame.setSize(300,200);
		frame.setVisible(true);

	}	

}