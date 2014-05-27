package unbbayes.gui.umpst.entity;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import unbbayes.controller.umpst.IconController;
import unbbayes.gui.umpst.IUMPSTPanel;
import unbbayes.gui.umpst.MainPropertiesEditionPane;
import unbbayes.gui.umpst.UmpstModule;
import unbbayes.model.umpst.entity.AttributeModel;
import unbbayes.model.umpst.entity.EntityModel;
import unbbayes.model.umpst.entity.RelationshipModel;
import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.util.CommonDataUtil;


public class RelationshipEditionPanel extends IUMPSTPanel {

	private static final long serialVersionUID = 1L;

	private GridBagConstraints constraint     = new GridBagConstraints();
	private JLabel titulo                     = new JLabel();

	private JButton buttonSave 	     ;
	private JButton buttonCancel     ;

	private RelationshipModel relationshipModel;

	private JList<EntityModel> listEntity,
	listUsedEntitiesRelationship; 

	private DefaultListModel<EntityModel> listEntityModel = new DefaultListModel<EntityModel>();
	private DefaultListModel<EntityModel> listUsedEntitiesModel = new DefaultListModel<EntityModel>();

	private JList<AttributeModel> listAtribute,
	listUsedAttributes;

	private DefaultListModel<AttributeModel> listAtributeModel = new DefaultListModel<AttributeModel>();
	private DefaultListModel<AttributeModel> listUsedAtributeModel = new DefaultListModel<AttributeModel>();

	private MainPropertiesEditionPane mainPropertiesEditionPane ; 

	/** Load resource file from this package */
	private static ResourceBundle resource = 
			unbbayes.util.ResourceController.newInstance().getBundle(
					unbbayes.gui.umpst.resources.Resources.class.getName());

	private IconController iconController = IconController.getInstance();


	public RelationshipEditionPanel(UmpstModule fatherWindow,
			UMPSTProject umpstProject, 
			RelationshipModel relationship){

		super(fatherWindow);
		this.setUmpstProject(umpstProject);

		this.relationshipModel = relationship;

		this.setLayout(new GridBagLayout());
		constraint.fill = GridBagConstraints.HORIZONTAL;

		this.setLayout(new GridLayout(1,1));

		createButtons(); 

		JSplitPane leftPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, 
				createTextPanel(),
				createBacktrackingEntity()); 

		leftPanel.setDividerLocation(300); 

		JSplitPane splitPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				leftPanel,
				createRelationshipTable()); 

		splitPanel.setDividerLocation(500); 

		this.add(splitPanel); 

		createListeners();

		titulo.setText("Relationship");

	}

	public JPanel createTextPanel(){

		String title            = resource.getString("ttRelationship");

		mainPropertiesEditionPane = 
				new MainPropertiesEditionPane(
						buttonCancel, 
						buttonSave, 
						title, 
						"Atribute Details",
						null,
						null, 
						true); 

		if (relationshipModel != null){
			mainPropertiesEditionPane.setTitleText(relationshipModel.getName());
			mainPropertiesEditionPane.setCommentsText(relationshipModel.getComments());
			mainPropertiesEditionPane.setAuthorText(relationshipModel.getAuthor());
			mainPropertiesEditionPane.setDateText(relationshipModel.getDate());
		}

		return mainPropertiesEditionPane.getPanel();

	}


	public void createButtons(){
		buttonSave 	     = new JButton(iconController.getSaveObjectIcon());
		buttonSave.setText(resource.getString("btnSave"));

		if( relationshipModel == null){
			buttonSave.setToolTipText(
					resource.getString("hpSaveRelationship"));

		} else {
			buttonSave.setToolTipText(
					resource.getString("hpUpdateRelationship"));
		}

		buttonCancel     = new JButton(iconController.getReturnIcon());
		buttonCancel.setText(resource.getString("btnReturn")); 

		buttonCancel.setToolTipText(resource.getString("hpReturnMainPanel"));

	}

	public void createListeners(){

		buttonSave.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				// Creating new relationship 

				if( relationshipModel == null ){
					try {

						if(mainPropertiesEditionPane.getTitleText().equals("")){
							JOptionPane.showMessageDialog(null, "Relationship name is empty!");
						}
						else{
							RelationshipModel relationshipModel = updateMapRelationship();
							updateBacktracking(relationshipModel);

							JOptionPane.showMessageDialog(null, 
									"Relationship successfully added",
									null, 
									JOptionPane.INFORMATION_MESSAGE);

							UmpstModule father = getFatherPanel();
							changePanel(father.getMenuPanel());
						}

					} catch (Exception e1) {
						JOptionPane.showMessageDialog(null, "Error while creating relationship", "UnBBayes", JOptionPane.WARNING_MESSAGE);
						UmpstModule pai = getFatherPanel();
						changePanel(pai.getMenuPanel());	
						e1.printStackTrace(); 

					}
				}

				// Update existing relationship

				else{
					if( JOptionPane.showConfirmDialog(null, "Do you want to update this relationship?", "UnBBayes", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION ){
						//RelationshipText.getText(),commentsText.getText(), authorText.getText(), dateText.getText(),null,null,null);
						try{
							relationshipModel.setName(mainPropertiesEditionPane.getTitleText());
							relationshipModel.setComments(mainPropertiesEditionPane.getCommentsText());
							relationshipModel.setAuthor(mainPropertiesEditionPane.getAuthorText());
							relationshipModel.setDate(mainPropertiesEditionPane.getDateText());

							updateBacktracking(relationshipModel);

							JOptionPane.showMessageDialog(null, 
									"Relationship successfully updated", 
									"UnBBayes", 
									JOptionPane.INFORMATION_MESSAGE);
							UmpstModule pai = getFatherPanel();
							changePanel(pai.getMenuPanel());	

						}
						catch (Exception e2) {
							JOptionPane.showMessageDialog(null,
									"Error while ulpating relationship", 
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

		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				UmpstModule pai = getFatherPanel();
				changePanel(pai.getMenuPanel());	
			}
		});


	}


	public RelationshipModel updateMapRelationship(){

		String idAux = "";
		int tamanho = getUmpstProject().getMapRelationship().size()+1;

		if ( getUmpstProject().getMapRelationship().size() != 0){
			idAux = tamanho+"";
		}
		else{
			idAux = "1";
		}


		RelationshipModel relationshipModel = new RelationshipModel(idAux,
				mainPropertiesEditionPane.getTitleText(),
				mainPropertiesEditionPane.getCommentsText(), 
				mainPropertiesEditionPane.getAuthorText(), 
				mainPropertiesEditionPane.getDateText(),
				null,
				null,
				null, 
				null,
				null,
				null);

		CommonDataUtil.getInstance().setAuthorName(mainPropertiesEditionPane.getAuthorText()); 


		getUmpstProject().getMapRelationship().put(relationshipModel.getId(), relationshipModel);	

		return relationshipModel;
	}


	public  JPanel createBacktrackingEntity(){

		JPanel panel = new JPanel(new GridLayout(1,3));
		JButton buttonCopy, buttonRemove;

		//Build the entity selection list. The entities are inserted in 
		//alphabetic order. 

		Set<String> keys = getUmpstProject().getMapEntity().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);

		for (String key: sortedKeys){
			listEntityModel.addElement(getUmpstProject().getMapEntity().get(key));
		}


		//This IF is responsable to update the first JList with all requirements 
		//elements MINUS those who are already registered as backtracking.

		if (relationshipModel!=null){
			for (EntityModel entity: relationshipModel.getBacktrackingEntityList()) {
				listUsedEntitiesModel.addElement(entity);
				if (listEntityModel.contains(entity)){
					listEntityModel.removeElement(entity);
				}
			}
		}

		listEntity = new JList(listEntityModel); //data has type Object[]
		listEntity.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listEntity.setLayoutOrientation(JList.VERTICAL);
		listEntity.setVisibleRowCount(-1);

		JScrollPane listHypothesisScroller = new JScrollPane(listEntity);

		panel.add(listHypothesisScroller);

		JPanel panelButtons = new JPanel(new GridLayout(6,1)); 

		panelButtons.add(new JLabel()); 
		panelButtons.add(new JLabel()); 

		buttonCopy = new JButton(resource.getString("btnCopy"));
		buttonCopy.setIcon(IconController.getInstance().getRigthDoubleArrowIcon());
		buttonCopy.setBackground(Color.WHITE); 

		panelButtons.add(buttonCopy);
		buttonCopy.addActionListener(
				new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						listUsedEntitiesModel.addElement(listEntity.getSelectedValue());	
						//						listEntityModel.removeElement(listEntity.getSelectedValue());

					}
				}

				);

		buttonRemove = new JButton(resource.getString("btnRemove"));
		buttonRemove.setIcon(IconController.getInstance().getLeftDoubleArrowIcon());
		buttonRemove.setBackground(Color.WHITE); 

		panelButtons.add(buttonRemove);

		buttonRemove.addActionListener(
				new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						//						listEntityModel.addElement(listUsedEntitiesRelationship.getSelectedValue());
						listUsedEntitiesModel.removeElement(listUsedEntitiesRelationship.getSelectedValue());
					}
				}

				);	

		panelButtons.add(new JLabel()); 
		panelButtons.add(new JLabel()); 

		panel.add(panelButtons); 

		listUsedEntitiesRelationship = new JList(listUsedEntitiesModel);

		listUsedEntitiesRelationship.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listUsedEntitiesRelationship.setLayoutOrientation(JList.VERTICAL);
		listUsedEntitiesRelationship.setVisibleRowCount(-1);

		JScrollPane listHypothesisScrollerAux = new JScrollPane(listUsedEntitiesRelationship);
		panel.add(listHypothesisScrollerAux);

		panel.setBorder(BorderFactory.createTitledBorder("Envolved Entities"));

		return panel;

	}


	public void updateBacktracking(RelationshipModel relationship){

		String keyWord = "";

		Set<String> keys = getUmpstProject().getMapEntity().keySet();

		TreeSet<String> sortedKeysMapEntity = new TreeSet<String>(keys);

		// Add each entitie to the backtracking list of the relationship and 
		// update the forward tracking list of the entitie adding this 
		// relationship. 

		if (listUsedEntitiesRelationship != null){

			for (int i = 0; i < listUsedEntitiesRelationship.getModel().getSize();i++) {
				keyWord = listUsedEntitiesRelationship.getModel().getElementAt(i).toString();
				for (String key: sortedKeysMapEntity){
					System.out.println("Key = " + key);
					EntityModel entityModel = getUmpstProject().getMapEntity().get(key); 
					if(entityModel == null) System.out.println("Entidade igual a null");
					if (keyWord.equals( entityModel.getName()) ){
						getUmpstProject().getMapEntity().get(key).getFowardTrackingRelationship().add(relationship);
					}			

				}
			}

			for(int i = 0; i < listUsedEntitiesModel.getSize(); i++){
				relationship.getBacktrackingEntityList().add(listUsedEntitiesModel.getElementAt(i)); 	
			}

		}

		if (listUsedAttributes != null){
			for (int i = 0; i < listUsedAttributes.getModel().getSize();i++) {
				keyWord = listUsedAttributes.getModel().getElementAt(i).toString();
				for (String key: sortedKeysMapEntity){
					if ( getUmpstProject().getMapHypothesis().get(key)!=null){
						if (keyWord.equals( getUmpstProject().getMapHypothesis().get(key).getName()) ){
							getUmpstProject().getMapAtribute().get(key).getFowardTrackingRelationship().add(relationship);
						}	
					}

				}
			}

		}

	}


	public JScrollPane createRelationshipTable(){

		TableRelationship relationshipTable = new TableRelationship(getFatherPanel(),getUmpstProject());
		JTable table = relationshipTable.createTable();
		JScrollPane scrollPane = new JScrollPane(table);

		scrollPane.setBorder(BorderFactory.createTitledBorder("Table of Relationship"));

		return scrollPane;

	}

}