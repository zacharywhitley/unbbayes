package unbbayes.gui.umpst.implementation;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import unbbayes.controller.umpst.IconController;
import unbbayes.gui.umpst.IUMPSTPanel;
import unbbayes.gui.umpst.MainPanel;
import unbbayes.gui.umpst.TableButton;
import unbbayes.gui.umpst.UmpstModule;
import unbbayes.model.umpst.entity.AttributeModel;
import unbbayes.model.umpst.entity.EntityModel;
import unbbayes.model.umpst.entity.RelationshipModel;
import unbbayes.model.umpst.implementation.CauseVariableModel;
import unbbayes.model.umpst.implementation.OrdinaryVariableModel;
import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.model.umpst.rule.RuleModel;

/**
 * @author Diego Marques
 *
 */
public class CauseEditPanel extends IUMPSTPanel{
	
	private static final long serialVersionUID = 1L;
	private int ID = 0;
	private final int SIZE_COLUMNS_ICON = 25;
	private final int SIZE_COLUMNS_TEXT = 300;
	private int NUM_COLUMNS_ICON = 4;
	private final int NUM_COLUMNS_TEXT = 1;
	
	private String variableRelationshipName;
	private String variableInstEntity1;
	private String variableInstEntity2;	
	
	private ImplementationMainPropertiesEditionPane mainPropertiesEditionPane;	
	private RuleModel rule;
	
	private JButton buttonRel;
	private JButton buttonAt;
	private JButton buttonAddUpdate;
	private JButton buttonSetRel;
	
	private JPanel titleLabel;
	private JSplitPane variableBox;
	private JSplitPane selectPane;
	private JSplitPane relationshipBox;
	private JSplitPane argumentBox;
	private JSplitPane attributeBox;
	private JScrollPane variablePane;
	private JScrollPane cvTable;
	private IconController iconController;
	
	private RelationshipModel relationshipSelected;
	private List<RelationshipModel> relatedRelationshipList;
	private AttributeModel attributeSelected;
	private CauseVariableModel causeVariable;
	private CauseVariableModel causeVariableEdited;
	
	private ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
			unbbayes.gui.umpst.resources.Resources.class.getName());
	
	private boolean wasAttributeBox;
	private boolean wasRelationshipBox;
	
	public CauseEditPanel(UmpstModule janelaPai, UMPSTProject umpstProject, RuleModel rule) {
		
		super(janelaPai);
		this.setUmpstProject(umpstProject);
		this.rule = rule;		
		this.setLayout(new FlowLayout());
		setID();
		
		iconController = IconController.getInstance();
		mainPropertiesEditionPane = new ImplementationMainPropertiesEditionPane(
				getFatherPanel(), getUmpstProject(), rule);
		
		titleLabel = mainPropertiesEditionPane.createTitleLabel("Causes");
		
		// Used to keep relationship related to the rule
		relatedRelationshipList = new ArrayList<RelationshipModel>();
		createRelationshipBox(); // add JComboBox at relationshipBox
		
		// TODO Change attribute list according to new attributes present in children rule		
		createArgumentBox();
		relationshipBox.add(argumentBox);
		variableBox = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		variableBox.add(relationshipBox);
		setWasRelationshipBox(true);
		setWasAttributeBox(false);
		
		variablePane = createCauseVariableTableAndEdit(rule.getCauseVariableList());	
		
		cvTable = new JScrollPane();
		
		createRAButton();
		this.add(createMainPanel());
		RAButtonListerner();
	}

	public JSplitPane createMainPanel() {
		JSplitPane mainPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		mainPane.setPreferredSize(new Dimension(480, 380));
		mainPane.add(createEditPanel()); // title and select and list pane
		mainPane.add(createAddUpdateButton()); // button to add or update variable
		return mainPane;
	}
	
	public JSplitPane createEditPanel() {
		JSplitPane editPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		editPane.add(titleLabel);
		editPane.add(createSelectAndListPanel()); // select relationship or attribute and list items added
		return editPane;
	}
	
	public JSplitPane createSelectAndListPanel() {		
		JSplitPane selectAndListPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		selectAndListPane.add(createSelectPanel()); // select relationship or attribute
		selectAndListPane.add(variablePane);
		return selectAndListPane;		
	}
	
	/**
	 * Create attribute or relationship selection box
	 * @return selectPane
	 */
	public JSplitPane createSelectPanel() {
		selectPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);		
		
		// Relationship and Attribute buttons
		JPanel btnRAPane = new JPanel(new FlowLayout());
		btnRAPane.setLayout(new GridBagLayout());
		
		JPanel buttonRA = new JPanel();
		buttonRA.setLayout(new GridLayout(1,2));		
		buttonRA.add(buttonRel);
		buttonRA.add(buttonAt);
		
		// Add buttons
		btnRAPane.add(buttonRA);
		
		selectPane.add(btnRAPane);
		selectPane.add(variableBox);
		return selectPane;
	}
	
	public void updateAttributeBox() {
		if (getWasRelationshipBox()) {
			variableBox.remove(relationshipBox);
		} else {
			variableBox.remove(attributeBox);			
		}
		createAttributeBox();
		variableBox.add(attributeBox);
		variableBox.repaint();
		setWasAttributeBox(true);
		setWasRelationshipBox(false);
	}
	
	public void updateRelationshipBox() {
		if (getWasAttributeBox()) {
			variableBox.remove(attributeBox);
			variableBox.add(relationshipBox);
			variableBox.repaint();
		} else {
			updateArgumentBox();
		}
		setWasAttributeBox(false);
		setWasRelationshipBox(true);
	}
	
	public void updateArgumentBox() {
		relationshipBox.remove(argumentBox);
		createArgumentBox();
		relationshipBox.add(argumentBox);
		relationshipBox.repaint();
	}
	
	public void updateRelationshipBoxEdited() {
		if (getWasAttributeBox()) {
			variableBox.remove(attributeBox);
		} else {
			variableBox.remove(relationshipBox);
		}
		createRelationshipBox();
		createArgumentBox();
		relationshipBox.add(argumentBox);
		variableBox.add(relationshipBox);
		variableBox.repaint();
		setWasRelationshipBox(true);
		setWasAttributeBox(false);
	}
	
	/**
	 * Create relationshipBox splitPane with its arguments 
	 */
	public void createRelationshipBox() {
		relationshipBox = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);		
		
		// Relationship Box	
		int relationshipListSize = rule.getRelationshipList().size();
		if (hasChildrenRule()) {
			for (int i = 0; i < rule.getChildrenRuleList().size(); i++) {
				relationshipListSize = relationshipListSize + 
						rule.getChildrenRuleList().get(i).getRelationshipList().size();
			}
		}		
		
		int count = 0; // It is used to put additional amount of relationship
		final String[] relationshipNames = new String[relationshipListSize];
		for (int i = 0; i < rule.getRelationshipList().size(); i++) {
			relationshipNames[i] = rule.getRelationshipList().get(i).getName();
			
			// Add to related relationship list
			relatedRelationshipList.add(rule.getRelationshipList().get(i));
			count++;
		}
		
		// Put relationships from children rules inside array of relationshipNames
		if (hasChildrenRule()) {			
			for (int i = 0; i < rule.getChildrenRuleList().size(); i++) {				
				for (int j = 0; j < rule.getChildrenRuleList().get(i).
						getRelationshipList().size(); j++) {
					relationshipNames[count] = rule.getChildrenRuleList().get(i).
							getRelationshipList().get(j).getName();
					
					// Add to related relationship list
					relatedRelationshipList.add(rule.getChildrenRuleList().get(i).
							getRelationshipList().get(j));
					count++;
				}
			}
		}
		
		final JComboBox<String> relationshipNameBox = new JComboBox<String>(relationshipNames);
		if(getCauseVariableEdited() != null) {
			if (getCauseVariableEdited().getRelationship() != null) {			
				relationshipNameBox.setSelectedItem(getCauseVariableEdited().getRelationship());
			}
		}
		relationshipNameBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index = relationshipNameBox.getSelectedIndex();
				setAttributeSelected(null);				
				setRelationshipSelected(relatedRelationshipList.get(index));
				setVariableRelationshipName(relationshipNames[index]);
				
				UmpstModule janelaPai = getFatherPanel();
				ImplementationEditPanel ipanel = janelaPai.getMenuPanel().getImplementationPane().getImplementationTable().
						getImplementationEditPanel();
				ipanel.getCauseEditPane().updateArgumentBox();	
				changePanel(ipanel);
			}
		});
		if (getRelationshipSelected() == null) {
			setRelationshipSelected(relatedRelationshipList.get(0));
		}
		relationshipBox.add(relationshipNameBox);
	}
	
	/**
	 * Get relationshipBox pane and set arguments related to relationship selected.
	 */
	public void createArgumentBox() {
		argumentBox = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		
		// Instance of entities related to relationship
		final ArrayList<ArrayList<String>> boxArray = new ArrayList<ArrayList<String>>();
		List<EntityModel> entityList = getRelationshipSelected().getEntityList();
		List<OrdinaryVariableModel> ordVariableList = rule.getOrdinaryVariableList();
		
		for (int i = 0; i < entityList.size(); i++) {
			final ArrayList<String> argumentArray = new ArrayList<String>();			
			for (int j = 0; j < ordVariableList.size(); j++) {
				if (entityList.get(i).getName().equals(ordVariableList.get(j).getTypeEntity())) {						
						argumentArray.add(ordVariableList.get(j).getVariable());
				}
			}
			boxArray.add(argumentArray);
		}
		
		// Entity instance box
		if(boxArray.size() == 1) {
			final String[] elementBox = new String[boxArray.get(0).size()];
			for (int i = 0; i < boxArray.get(0).size(); i++) {
				elementBox[i] = boxArray.get(0).get(i);
			}
			
			final JComboBox<String> argumentComboBox = new JComboBox<String>(elementBox);
			if(getCauseVariableEdited() != null) {
				if (getCauseVariableEdited().getArgumentList() != null) {
					argumentComboBox.setSelectedItem(getCauseVariableEdited().getArgumentList().get(0));
				}
			}
			argumentComboBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int index = argumentComboBox.getSelectedIndex();
					setVariableInstEntity1(elementBox[index]);
				}
			});
			argumentBox.add(argumentComboBox);
		} else if (boxArray.size() == 2){
			final String[] elementBox = new String[boxArray.get(0).size()];
			for (int i = 0; i < boxArray.get(0).size(); i++) {
				elementBox[i] = boxArray.get(0).get(i);
			}
			
			final JComboBox<String> argumentComboBox = new JComboBox<String>(elementBox);
			if(getCauseVariableEdited() != null) {
				if (getCauseVariableEdited().getArgumentList() != null) {
					argumentComboBox.setSelectedItem(getCauseVariableEdited().getArgumentList().get(0));
				}
			}
			argumentComboBox.addActionListener(new ActionListener() {			
				public void actionPerformed(ActionEvent e) {
					int index = argumentComboBox.getSelectedIndex();
					setVariableInstEntity1(elementBox[index]);
				}
			});
			argumentBox.add(argumentComboBox);
			
			final String[] elementBox2 = new String[boxArray.get(1).size()];
			for (int i = 0; i < boxArray.get(1).size(); i++) {
				elementBox2[i] = boxArray.get(1).get(i);
			}			
			
			final JComboBox<String> argumentComboBox2 = new JComboBox<String>(elementBox2);
			if((getCauseVariableEdited() != null) &&
					(getCauseVariableEdited().getArgumentList().size() == 2)) {
				argumentComboBox2.setSelectedItem(getCauseVariableEdited().getArgumentList().get(1));
			}
			argumentComboBox2.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int index = argumentComboBox2.getSelectedIndex();
					setVariableInstEntity2(elementBox2[index]);
				}
			});
			argumentBox.add(argumentComboBox2);
		}  else {
			System.err.println("Error Relationship. Argument error.");
		}
	}
	
	public void createRAButton() {		
		buttonAddUpdate = new JButton("Add/Update");
		buttonAddUpdate.setPreferredSize(new Dimension(50, 30));
		
		buttonRel = new JButton("R");
		buttonAt = new JButton("A");
		
		buttonSetRel = new JButton();
	}
	
	/**
	 * Create attributebox.
	 */
	public void createAttributeBox() {
		attributeBox = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		
		final String[] elementBox = new String[rule.getAttributeList().size()];
		for (int i = 0; i < rule.getAttributeList().size(); i++) {
			elementBox[i] = rule.getAttributeList().get(i).getName();
		}
		
		final JComboBox<String> attributeComboxBox = new JComboBox<String>(elementBox);
		if (getCauseVariableEdited() != null) {
			if (getCauseVariableEdited().getAttribute() != null) {
				attributeComboxBox.setSelectedItem(getCauseVariableEdited().getAttributeModel().getName());
			}
		}
		attributeComboxBox.addActionListener(new ActionListener() {			
			public void actionPerformed(ActionEvent e) {
				int index = attributeComboxBox.getSelectedIndex();
				setRelationshipSelected(null);
				setAttributeSelected(rule.getAttributeList().get(index));
			}
		});
		attributeBox.add(attributeComboxBox);
	}

	private void RAButtonListerner() {	
		buttonRel.addActionListener(new ActionListener() {			
			public void actionPerformed(ActionEvent e) {
				UmpstModule janelaPai = getFatherPanel();
				ImplementationEditPanel ipanel = janelaPai.getMenuPanel().getImplementationPane().getImplementationTable().
						getImplementationEditPanel();	
				ipanel.getCauseEditPane().updateRelationshipBox();
				changePanel(ipanel);
			}						
		});
		
		buttonAt.addActionListener(new ActionListener() {		
			public void actionPerformed(ActionEvent e) {
//				UmpstModule janelaPai = getFatherPanel();
//				ImplementationEditPanel ipanel = janelaPai.getMenuPanel().getImplementationPane().getImplementationTable().
//						getImplementationEditPanel();				
//				ipanel.getCauseEditPane().updateAttributeBox();
//				changePanel(ipanel);
			}
		});
	}	
	
	public JScrollPane createCauseVariableTableAndEdit(List<CauseVariableModel> causeVariableList) {
		Object[] columnNames = {"ID", "Cause Variable", "", ""};		
		Object[][] data = new Object[causeVariableList.size()][NUM_COLUMNS_ICON];
		
		String sentence = null;
		for (int i = 0; i < causeVariableList.size(); i++) {
			if (causeVariableList.get(i).getRelationship() != null) {
				sentence = causeVariableList.get(i).getRelationship() + "(";
				for (int j = 0; j < causeVariableList.get(i).getArgumentList().size(); j++) {				
					sentence = sentence + causeVariableList.get(i).getArgumentList().get(j) + ", ";
				}
				int index = sentence.lastIndexOf(", ");
				sentence = sentence.substring(0, index);
				sentence = sentence + ")";
			} else if (causeVariableList.get(i).getAttribute() != null) {
				sentence = causeVariableList.get(i).getAttribute();
			}
			
			String causeVariableId = causeVariableList.get(i).getId();
			data[i][0] = causeVariableId;
			data[i][1] = sentence;
			data[i][2] = "";
			data[i][3] = "";
		}
		return createCauseVariableTableAndEdit(data, columnNames);
	}
	
	public JScrollPane createCauseVariableTableAndEdit(final Object[][] data, Object[] columnNames) {		
		
		// Ordinary variable table
		DefaultTableModel tableModel = new DefaultTableModel(data, columnNames);
		JTable variableEditTable = new JTable(tableModel);
		variableEditTable.setTableHeader(null);
		variableEditTable.setPreferredSize(new Dimension(400, 150));
		
		TableButton buttonEdit = new TableButton(new TableButton.TableButtonCustomizer() {			
			public void customize(JButton button, int row, int column) {
				button.setIcon(iconController.getEditUMPIcon());
			}
		});
		
		TableButton buttonDelete = new TableButton(new TableButton.TableButtonCustomizer() {			
			public void customize(JButton button, int row, int column) {
				button.setIcon(iconController.getDeleteIcon());				
			}
		});
		
		TableColumn columnSentence = variableEditTable.getColumnModel().getColumn(
				columnNames.length-3);
		columnSentence.setMinWidth(SIZE_COLUMNS_TEXT);
		
		TableColumn columnEdit = variableEditTable.getColumnModel().getColumn(
				columnNames.length-2);
		columnEdit.setMaxWidth(SIZE_COLUMNS_ICON);
		columnEdit.setCellRenderer(buttonEdit);
		columnEdit.setCellEditor(buttonEdit);
		
		TableColumn columnDelete = variableEditTable.getColumnModel().getColumn(
				columnNames.length-1);
		columnDelete.setMaxWidth(SIZE_COLUMNS_ICON);
		columnDelete.setCellRenderer(buttonDelete);
		columnDelete.setCellEditor(buttonDelete);
		
		buttonEdit.addHandler(new TableButton.TableButtonPressedHandler() {			
			public void onButtonPress(int row, int column) {
				String variableRow = data[row][0].toString();				
				int i = 0;
				boolean flag = false;
				while((i < rule.getCauseVariableList().size()) && (!flag)) {
					if(variableRow.equals(rule.getCauseVariableList().get(i).getId())) {
						flag = true;
					} else {
						i++;
					}
				}
				
				UmpstModule janelaPai = getFatherPanel();
				ImplementationEditPanel ipanel = janelaPai.getMenuPanel().getImplementationPane().getImplementationTable().
						getImplementationEditPanel();
				
				// Set cause variable edited and reload panel
				causeVariableEdited = rule.getCauseVariableList().get(i);
				if ((causeVariableEdited.getRelationship() != null) &&
						(causeVariableEdited.getAttribute() == null)) {
					setRelationshipSelected(causeVariableEdited.getRelationshipModel());
					setAttributeSelected(null);
					ipanel.getCauseEditPane().updateRelationshipBoxEdited();
				} else if ((causeVariableEdited.getRelationship() == null) &&
						(causeVariableEdited.getAttribute() != null)) {
					setRelationshipSelected(null);
					setAttributeSelected(causeVariableEdited.getAttributeModel());
					ipanel.getCauseEditPane().updateAttributeBox();
				}
				setCauseVariableEdited(causeVariableEdited);
				changePanel(ipanel);
			}
		});
		
		buttonDelete.addHandler(new TableButton.TableButtonPressedHandler() {
			public void onButtonPress(int row, int column) {				
				String variableRow = data[row][0].toString();				
				int i = 0;
				boolean flag = false;
				while((i < rule.getCauseVariableList().size()) && (!flag)) {
					if(variableRow.equals(rule.getCauseVariableList().get(i).getId())) {
						flag = true;						
						rule.removeEventVariableObject(rule.getCauseVariableList().get(i));
						rule.getCauseVariableList().remove(i);
					} else {
						i++;
					}
				}				
				updateCauseVariableTable();				
			}
		});
		
		variableEditTable.setPreferredScrollableViewportSize(variableEditTable.getPreferredSize());
		variableEditTable.updateUI();
		variableEditTable.repaint();
		
		JScrollPane scrollpane = new JScrollPane(variableEditTable);
//		scrollpane.remove();
		scrollpane.setPreferredSize(new Dimension(80, 80));
				
		return scrollpane;
	}
	
	public JPanel createAddUpdateButton() {
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.PAGE_END;
		
		JButton buttonCancel = new JButton("Cancel");
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		buttonPanel.add(buttonCancel, c);
		
		JButton buttonAddUpdate = new JButton("Add/Update");
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 1;
		buttonPanel.add(buttonAddUpdate, c);
		
		buttonAddUpdate.addActionListener(new ActionListener() {			
			public void actionPerformed(ActionEvent e) {				
				// Just edit relationship
				if ((getRelationshipSelected() != null) && (getAttributeSelected() == null)) {					
					if (getRelationshipSelected().getEntityList().size() == 2) {
						String relationship = getVariableRelationshipName();
						String arg1 = getVariableInstEntity1();
						String arg2 = getVariableInstEntity2();						
						
						if(relationship == null || arg1 == null || arg2 == null) {
							System.err.println("Error. Select relationship and set its arguments.");
						} else {						
							if (getCauseVariableEdited() != null) {
								causeVariable = new CauseVariableModel(causeVariableEdited.getId());								
							} else {
								String key = Integer.toString(ID);
								causeVariable = new CauseVariableModel(key);
							}
							causeVariable.setRelationshipModel(getRelationshipSelected());
							causeVariable.setRelationship(relationship);
							causeVariable.getArgumentList().add(arg1);
							causeVariable.getArgumentList().add(arg2);
							if (!mainPropertiesEditionPane.isVariableDuplicated(causeVariable)) {
								if (getCauseVariableEdited() != null) {
									int i = 0;
									while (i < rule.getCauseVariableList().size()) {				
										if (rule.getCauseVariableList().get(i).getId().equals(causeVariable.getId())) {
											rule.getCauseVariableList().get(i).setRelationshipModel(causeVariable.getRelationshipModel());
											rule.getCauseVariableList().get(i).setRelationship(causeVariable.getRelationship());
											rule.getCauseVariableList().get(i).setArgumentList(causeVariable.getArgumentList());
											
											rule.changeEventVariableObject(causeVariable);
											break;
										}
										i++;
									}
									setCauseVariableEdited(null);
								} else {
									rule.getCauseVariableList().add(causeVariable);
									rule.getEventVariableObjectList().add(causeVariable);
									ID++;
								}
							} else {
								System.err.println("Variable duplicated.");
							}
							updateCauseVariableTable();
						}
					} else if (getRelationshipSelected().getEntityList().size() == 1) {
						String relationship = getVariableRelationshipName();
						String arg1 = getVariableInstEntity1();
						
						if(relationship == null || arg1 == null) {
							System.err.println("Error. Select relationship and set its arguments.");
						} else {
							if (getCauseVariableEdited() != null) {
								causeVariable = new CauseVariableModel(causeVariableEdited.getId());								
							} else {
								String key = Integer.toString(ID);
								causeVariable = new CauseVariableModel(key);
							}
							causeVariable.setRelationshipModel(getRelationshipSelected());
							causeVariable.setRelationship(relationship);
							causeVariable.getArgumentList().add(arg1);
							if (!mainPropertiesEditionPane.isVariableDuplicated(causeVariable)) {
								if (getCauseVariableEdited() != null) {
									int i = 0;
									while (i < rule.getCauseVariableList().size()) {				
										if (rule.getCauseVariableList().get(i).getId().equals(causeVariable.getId())) {
											rule.getCauseVariableList().get(i).setRelationshipModel(causeVariable.getRelationshipModel());
											rule.getCauseVariableList().get(i).setRelationship(causeVariable.getRelationship());
											rule.getCauseVariableList().get(i).setArgumentList(causeVariable.getArgumentList());
											
											rule.changeEventVariableObject(causeVariable);
											break;
										}
										i++;
									}
									setCauseVariableEdited(null);
								} else {
									rule.getCauseVariableList().add(causeVariable);
									rule.getEventVariableObjectList().add(causeVariable);
									ID++;
								}
							} else {
								System.err.println("Variable duplicated.");
							}
							updateCauseVariableTable();
						}
					} else {
						System.err.println("Error add relationship.");
					}
				} else if ((getRelationshipSelected() == null) && (getAttributeSelected() != null)) {
					String key = Integer.toString(ID);
					causeVariable = new CauseVariableModel(key);
					causeVariable.setAttribute(getAttributeSelected().getName());
					causeVariable.setAttributeModel(getAttributeSelected());
					if (!mainPropertiesEditionPane.isVariableDuplicated(causeVariable)) {
						rule.getCauseVariableList().add(causeVariable);
						ID++;
					}
					updateCauseVariableTable();
				} else {
					System.err.println("Error add variable.");
				}
			}
		});
		
		buttonCancel.addActionListener(new ActionListener() {			
			public void actionPerformed(ActionEvent e) {
				UmpstModule janelaPai = getFatherPanel();
				MainPanel ipanel = janelaPai.getMenuPanel();						
				changePanel(ipanel);
			}
		});
		return buttonPanel;
	}
	
	public void updateCauseVariableTable() {
		UmpstModule janelaPai = getFatherPanel();
		ImplementationEditPanel ipanel = janelaPai.getMenuPanel().getImplementationPane().
				getImplementationTable().getImplementationEditPanel();
		
		ipanel.getCauseEditPane().getVariablePane().setViewportView(
				createCauseVariableTableAndEdit(rule.getCauseVariableList()));
		ipanel.getCauseEditPane().getVariablePane().updateUI();
		ipanel.getCauseEditPane().getVariablePane().repaint();
		
		changePanel(ipanel);
	}
	
	/**
	 * Set ID according to the last necessary condition ID created.
	 */
	public void setID() {
		int greaterID = -1;
		boolean beginID = true; // created to set ID = 0
		for (int i = 0; i < rule.getCauseVariableList().size(); i++) {
			if (greaterID < Integer.parseInt(rule.getCauseVariableList().get(i).getId())) {
				greaterID = Integer.parseInt(rule.getCauseVariableList().get(i).getId());
				beginID = false;
			}
		}
		if (!beginID) {
			ID = greaterID+1;
		}
	}
	
	private boolean hasChildrenRule() {
		if (rule.getChildrenRuleList() != null) {
			return true;
		} else {
			return false;
		}		
	}
	
	/**
	 * @return the selectPane
	 */
	public JSplitPane getSelectPane() {
		return selectPane;
	}

	/**
	 * @param selectPane the selectPane to set
	 */
	public void setSelectPane(JSplitPane selectPane) {
		this.selectPane = selectPane;
	}
	
	/**
	 * @return the relationshipSelected
	 */
	public RelationshipModel getRelationshipSelected() {
		return relationshipSelected;
	}

	/**
	 * @param relationshipSelected the relationshipSelected to set
	 */
	public void setRelationshipSelected(RelationshipModel relationshipSelected) {
		this.relationshipSelected = relationshipSelected;
	}
	
	/**
	 * @return the variableRelationshipName
	 */
	public String getVariableRelationshipName() {
		return variableRelationshipName;
	}

	/**
	 * @param variableRelationshipName the variableRelationshipName to set
	 */
	public void setVariableRelationshipName(String variableRelationshipName) {
		this.variableRelationshipName = variableRelationshipName;
	}

	/**
	 * @return the variableInstEntity1
	 */
	public String getVariableInstEntity1() {
		return variableInstEntity1;
	}

	/**
	 * @param variableInstEntity1 the variableInstEntity1 to set
	 */
	public void setVariableInstEntity1(String variableInstEntity1) {
		this.variableInstEntity1 = variableInstEntity1;
	}

	/**
	 * @return the variableInstEntity2
	 */
	public String getVariableInstEntity2() {
		return variableInstEntity2;
	}

	/**
	 * @param variableInstEntity2 the variableInstEntity2 to set
	 */
	public void setVariableInstEntity2(String variableInstEntity2) {
		this.variableInstEntity2 = variableInstEntity2;
	}

	/**
	 * @return the attributeSelected
	 */
	public AttributeModel getAttributeSelected() {
		return attributeSelected;
	}

	/**
	 * @param attributeSelected the attributeSelected to set
	 */
	public void setAttributeSelected(AttributeModel attributeSelected) {
		this.attributeSelected = attributeSelected;
	}

	/**
	 * @return the causeVariable
	 */
	public CauseVariableModel getCauseVariable() {
		return causeVariable;
	}

	/**
	 * @param causeVariable the causeVariable to set
	 */
	public void setCauseVariable(CauseVariableModel causeVariable) {
		this.causeVariable = causeVariable;
	}

	/**
	 * @return the variablePane
	 */
	public JScrollPane getVariablePane() {
		return variablePane;
	}

	/**
	 * @param variablePane the variablePane to set
	 */
	public void setVariablePane(JScrollPane variablePane) {
		this.variablePane = variablePane;
	}	

	/**
	 * @return the causeVariableEdited
	 */
	public CauseVariableModel getCauseVariableEdited() {
		return causeVariableEdited;
	}

	/**
	 * @param causeVariableEdited the causeVariableEdited to set
	 */
	public void setCauseVariableEdited(CauseVariableModel causeVariableEdited) {
		this.causeVariableEdited = causeVariableEdited;
	}

	/**
	 * @return the wasAttributeBox
	 */
	public boolean getWasAttributeBox() {
		return wasAttributeBox;
	}

	/**
	 * @param wasAttributeBox the wasAttributeBox to set
	 */
	public void setWasAttributeBox(boolean wasAttributeBox) {
		this.wasAttributeBox = wasAttributeBox;
	}

	/**
	 * @return the wasRelationshipBox
	 */
	public boolean getWasRelationshipBox() {
		return wasRelationshipBox;
	}

	/**
	 * @param wasRelationshipBox the wasRelationshipBox to set
	 */
	public void setWasRelationshipBox(boolean wasRelationshipBox) {
		this.wasRelationshipBox = wasRelationshipBox;
	}

	/**
	 * @return the relatedRelationshipList
	 */
	public List<RelationshipModel> getRelatedRelationshipList() {
		return relatedRelationshipList;
	}

	/**
	 * @param relatedRelationshipList the relatedRelationshipList to set
	 */
	public void setRelatedRelationshipList(List<RelationshipModel> relatedRelationshipList) {
		this.relatedRelationshipList = relatedRelationshipList;
	}
}