package unbbayes.gui.umpst.implementation;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
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
import unbbayes.model.umpst.implementation.EffectVariableModel;
import unbbayes.model.umpst.implementation.EffectVariableModel;
import unbbayes.model.umpst.implementation.OrdinaryVariableModel;
import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.model.umpst.rule.RuleModel;

/**
 * @author Diego Marques
 *
 */
public class EffectEditPanel extends IUMPSTPanel{
	
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
	private AttributeModel attributeSelected;
	private EffectVariableModel effectVariable;
	private EffectVariableModel effectVariableEdited;
	
	private ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
			unbbayes.gui.umpst.resources.Resources.class.getName());
	
	private boolean wasAttributeBox;
	private boolean wasRelationshipBox;
	
	public EffectEditPanel(UmpstModule janelaPai, UMPSTProject umpstProject, RuleModel rule) {
		
		super(janelaPai);
		this.setUmpstProject(umpstProject);
		this.rule = rule;		
		this.setLayout(new FlowLayout());
		setID();
		
		iconController = IconController.getInstance();
		mainPropertiesEditionPane = new ImplementationMainPropertiesEditionPane(
				getFatherPanel(), getUmpstProject(), rule);
		
		titleLabel = mainPropertiesEditionPane.createTitleLabel("Effects");
		
		createRelationshipBox(); // add JComboBox at relationshipBox
		createArgumentBox();
		relationshipBox.add(argumentBox);
		variableBox = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		variableBox.add(relationshipBox);
		setWasRelationshipBox(true);
		setWasAttributeBox(false);
		
		variablePane = createEffectVariableTableAndEdit(rule.getEffectVariableList());
		
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
		relationshipBox = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		// Relationship Box
		int relationshipListSize = rule.getRelationshipList().size();
		String[] relationshipNames = new String[relationshipListSize];
		for (int i = 0; i < relationshipListSize; i++) {
			relationshipNames[i] = rule.getRelationshipList().get(i).getName();
		}
		
		final JComboBox<String> relationshipNameBox = new JComboBox<String>(relationshipNames);
		if(getEffectVariableEdited() != null) {
			if (getEffectVariableEdited().getRelationship() != null) {			
				relationshipNameBox.setSelectedItem(getEffectVariableEdited().getRelationship());
			}
		}
		relationshipNameBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index = relationshipNameBox.getSelectedIndex();
				setAttributeSelected(null);
				setRelationshipSelected(rule.getRelationshipList().get(index));
				setVariableRelationshipName(rule.getRelationshipList().get(index).getName());
				
				UmpstModule janelaPai = getFatherPanel();
				ImplementationEditPanel ipanel = janelaPai.getMenuPanel().getImplementationPane().getImplementationTable().
						getImplementationEditPanel();
				ipanel.getEffectEditPane().updateArgumentBox();	
				changePanel(ipanel);
			}
		});
		if (getRelationshipSelected() == null) {
			setRelationshipSelected(rule.getRelationshipList().get(0));
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
			if(getEffectVariableEdited() != null) {
				if (getEffectVariableEdited().getArgumentList() != null) {
					argumentComboBox.setSelectedItem(getEffectVariableEdited().getArgumentList().get(0));
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
			if(getEffectVariableEdited() != null) {
				if (getEffectVariableEdited().getArgumentList() != null) {
					argumentComboBox.setSelectedItem(getEffectVariableEdited().getArgumentList().get(0));
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
			if((getEffectVariableEdited() != null) &&
					(getEffectVariableEdited().getArgumentList().size() == 2)) {
				argumentComboBox2.setSelectedItem(getEffectVariableEdited().getArgumentList().get(1));
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
		if (getEffectVariableEdited() != null) {
			if (getEffectVariableEdited().getAttribute() != null) {
				attributeComboxBox.setSelectedItem(getEffectVariableEdited().getAttributeModel().getName());
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
				ipanel.getEffectEditPane().updateRelationshipBox();
				changePanel(ipanel);
			}						
		});
		
		buttonAt.addActionListener(new ActionListener() {		
			public void actionPerformed(ActionEvent e) {
//				UmpstModule janelaPai = getFatherPanel();
//				ImplementationEditPanel ipanel = janelaPai.getMenuPanel().getImplementationPane().getImplementationTable().
//						getImplementationEditPanel();				
//				ipanel.getEffectEditPane().updateAttributeBox();
//				changePanel(ipanel);
			}
		});
	}	
	
	public JScrollPane createEffectVariableTableAndEdit(List<EffectVariableModel> effectVariableList) {
		Object[] columnNames = {"ID", "Cause Variable", "", ""};		
		Object[][] data = new Object[effectVariableList.size()][NUM_COLUMNS_ICON];
		
		String sentence = null;
		for (int i = 0; i < effectVariableList.size(); i++) {
			if (effectVariableList.get(i).getRelationship() != null) {
				sentence = effectVariableList.get(i).getRelationship() + "(";
				for (int j = 0; j < effectVariableList.get(i).getArgumentList().size(); j++) {				
					sentence = sentence + effectVariableList.get(i).getArgumentList().get(j) + ", ";
				}
				int index = sentence.lastIndexOf(", ");
				sentence = sentence.substring(0, index);
				sentence = sentence + ")";
			} else if (effectVariableList.get(i).getAttribute() != null) {
				sentence = effectVariableList.get(i).getAttribute();
			}
			
			String effectVariableId = effectVariableList.get(i).getId();
			data[i][0] = effectVariableId;
			data[i][1] = sentence;
			data[i][2] = "";
			data[i][3] = "";
		}		
		return createEffectVariableTableAndEdit(data, columnNames);
//		return null;
	}
	
	public JScrollPane createEffectVariableTableAndEdit(final Object[][] data, Object[] columnNames) {		
		
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
				while((i < rule.getEffectVariableList().size()) && (!flag)) {
					if(variableRow.equals(rule.getEffectVariableList().get(i).getId())) {
						flag = true;
					} else {
						i++;
					}
				}
				
				UmpstModule janelaPai = getFatherPanel();
				ImplementationEditPanel ipanel = janelaPai.getMenuPanel().getImplementationPane().getImplementationTable().
						getImplementationEditPanel();
				
				// Set cause variable edited and reload panel
				effectVariableEdited = rule.getEffectVariableList().get(i);
				if ((effectVariableEdited.getRelationship() != null) &&
						(effectVariableEdited.getAttribute() == null)) {
					setRelationshipSelected(effectVariableEdited.getRelationshipModel());
					setAttributeSelected(null);
					ipanel.getEffectEditPane().updateRelationshipBoxEdited();
				} else if ((effectVariableEdited.getRelationship() == null) &&
						(effectVariableEdited.getAttribute() != null)) {
					setRelationshipSelected(null);
					setAttributeSelected(effectVariableEdited.getAttributeModel());
					ipanel.getEffectEditPane().updateAttributeBox();
				}
				setEffectVariableEdited(effectVariableEdited);
				changePanel(ipanel);
			}
		});
		
		buttonDelete.addHandler(new TableButton.TableButtonPressedHandler() {
			public void onButtonPress(int row, int column) {				
				String variableRow = data[row][0].toString();				
				int i = 0;
				boolean flag = false;
				while((i < rule.getEffectVariableList().size()) && (!flag)) {
					if(variableRow.equals(rule.getEffectVariableList().get(i).getId())) {
						flag = true;
						rule.removeEventVariableObject(rule.getEffectVariableList().get(i));
						rule.getEffectVariableList().remove(i);
					} else {
						i++;
					}
				}				
				updateEffectVariableTable();				
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
							if (getEffectVariableEdited() != null) {
								effectVariable = new EffectVariableModel(effectVariableEdited.getId());								
							} else {
								String key = Integer.toString(ID);
								effectVariable = new EffectVariableModel(key);
							}
							effectVariable.setRelationshipModel(getRelationshipSelected());
							effectVariable.setRelationship(relationship);
							effectVariable.getArgumentList().add(arg1);
							effectVariable.getArgumentList().add(arg2);
							if (!mainPropertiesEditionPane.isVariableDuplicated(effectVariable)) {
								if (getEffectVariableEdited() != null) {
									int i = 0;
									while (i < rule.getEffectVariableList().size()) {				
										if (rule.getEffectVariableList().get(i).getId().equals(effectVariable.getId())) {
											rule.getEffectVariableList().get(i).setRelationshipModel(effectVariable.getRelationshipModel());
											rule.getEffectVariableList().get(i).setRelationship(effectVariable.getRelationship());
											rule.getEffectVariableList().get(i).setArgumentList(effectVariable.getArgumentList());
											
											rule.changeEventVariableObject(effectVariable);
											break;
										}
										i++;
									}
									setEffectVariableEdited(null);
								} else {
									rule.getEffectVariableList().add(effectVariable);
									rule.getEventVariableObjectList().add(effectVariable);
									ID++;
								}
							} else {
								System.err.println("Variable duplicated.");
							}							
							updateEffectVariableTable();
						}
					} else if (getRelationshipSelected().getEntityList().size() == 1) {
						String relationship = getVariableRelationshipName();
						String arg1 = getVariableInstEntity1();
						
						if(relationship == null || arg1 == null) {
							System.err.println("Error. Select relationship and set its arguments.");
						} else {
							if (getEffectVariableEdited() != null) {
								effectVariable = new EffectVariableModel(effectVariableEdited.getId());								
							} else {
								String key = Integer.toString(ID);
								effectVariable = new EffectVariableModel(key);
							}
							effectVariable.setRelationshipModel(getRelationshipSelected());
							effectVariable.setRelationship(relationship);
							effectVariable.getArgumentList().add(arg1);
							if (!mainPropertiesEditionPane.isVariableDuplicated(effectVariable)) {
								if (getEffectVariableEdited() != null) {
									int i = 0;
									while (i < rule.getEffectVariableList().size()) {				
										if (rule.getEffectVariableList().get(i).getId().equals(effectVariable.getId())) {
											rule.getEffectVariableList().get(i).setRelationshipModel(effectVariable.getRelationshipModel());
											rule.getEffectVariableList().get(i).setRelationship(effectVariable.getRelationship());
											rule.getEffectVariableList().get(i).setArgumentList(effectVariable.getArgumentList());
											
											rule.changeEventVariableObject(effectVariable);
											break;
										}
										i++;
									}
									setEffectVariableEdited(null);
								} else {
									rule.getEffectVariableList().add(effectVariable);
									rule.getEventVariableObjectList().add(effectVariable);
									ID++;
								}
							} else {
								System.err.println("Variable duplicated.");
							}
							updateEffectVariableTable();
						}
					} else {
						System.err.println("Error add relationship.");
					}
				} else if ((getRelationshipSelected() == null) && (getAttributeSelected() != null)) {
					String key = Integer.toString(ID);
					effectVariable = new EffectVariableModel(key);
					effectVariable.setAttribute(getAttributeSelected().getName());
					effectVariable.setAttributeModel(getAttributeSelected());
					if (!mainPropertiesEditionPane.isVariableDuplicated(effectVariable)) {
						rule.getEffectVariableList().add(effectVariable);
						ID++;
					}
					updateEffectVariableTable();
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
	
	public void updateEffectVariableTable() {
		UmpstModule janelaPai = getFatherPanel();
		ImplementationEditPanel ipanel = janelaPai.getMenuPanel().getImplementationPane().
				getImplementationTable().getImplementationEditPanel();
		
		ipanel.getEffectEditPane().getVariablePane().setViewportView(
				createEffectVariableTableAndEdit(rule.getEffectVariableList()));
		ipanel.getEffectEditPane().getVariablePane().updateUI();
		ipanel.getEffectEditPane().getVariablePane().repaint();
		
		changePanel(ipanel);
	}
	
	/**
	 * Set ID according to the last necessary condition ID created.
	 */
	public void setID() {
		int greaterID = -1;
		boolean beginID = true; // created to set ID = 0
		for (int i = 0; i < rule.getEffectVariableList().size(); i++) {
			if (greaterID < Integer.parseInt(rule.getEffectVariableList().get(i).getId())) {
				greaterID = Integer.parseInt(rule.getEffectVariableList().get(i).getId());
				beginID = false;
			}
		}
		if (!beginID) {
			ID = greaterID+1;
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
	 * @return the effectVariable
	 */
	public EffectVariableModel getEffectVariable() {
		return effectVariable;
	}

	/**
	 * @param effectVariable the effectVariable to set
	 */
	public void setEffectVariable(EffectVariableModel effectVariable) {
		this.effectVariable = effectVariable;
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
	 * @return the effectVariableEdited
	 */
	public EffectVariableModel getEffectVariableEdited() {
		return effectVariableEdited;
	}

	/**
	 * @param effectVariableEdited the effectVariableEdited to set
	 */
	public void setEffectVariableEdited(EffectVariableModel effectVariableEdited) {
		this.effectVariableEdited = effectVariableEdited;
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
}