/**
 * 
 */
package unbbayes.gui.umpst.implementation;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import unbbayes.controller.umpst.IconController;
import unbbayes.gui.umpst.IUMPSTPanel;
import unbbayes.gui.umpst.MainPanel;
import unbbayes.gui.umpst.TableButton;
import unbbayes.gui.umpst.UmpstModule;
import unbbayes.model.umpst.entity.EntityModel;
import unbbayes.model.umpst.implementation.OrdinaryVariableModel;
import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.model.umpst.rule.RuleModel;

/**
 * Instance elements from ImplementationMainPropertiesPane to build
 * Ordinary variable panel
 * @author Diego Marques
 */
public class OrdinaryVariableEditPanel extends IUMPSTPanel{
	
	private int ID = 0;
	private final int SIZE_COLUMNS_ICON = 25;
	private final int SIZE_COLUMNS_TEXT = 300;
	private int NUM_COLUMNS_ICON = 4;
	
	private ImplementationMainPropertiesEditionPane mainPropertiesEditionPane;
	private RuleModel rule;
	public static OrdinaryVariableModel ordinaryVariableSelected;
	private OrdinaryVariableModel ordinaryVariable;
	private UMPSTProject umpstProject;

	private JPanel titlePanel;
	private JScrollPane variablePane;
	private JSplitPane panelSet;
	private JSplitPane ordinaryVariablePanel;
	
	private String[] entitiesNames = null;
	private String variableEdited = null;
	private String entitySelected = null;
	private String ordinaryVariableRow;
	private EntityModel entityObject = null;
	
	private IconController iconController = new IconController().getInstance();
	
	
	/**
	 * 
	 */
	public OrdinaryVariableEditPanel(UmpstModule janelaPai, UMPSTProject umpstProject,
				RuleModel rule) {
		
		super(janelaPai);
		this.setUmpstProject(umpstProject);
		this.umpstProject = umpstProject;
		this.rule = rule;
//		this.setLayout(new GridLayout(1,1));
		this.setLayout(new FlowLayout());
		setID();
		
		mainPropertiesEditionPane =  new ImplementationMainPropertiesEditionPane(
				janelaPai, umpstProject, rule);		
		titlePanel = mainPropertiesEditionPane.createTitleLabel("Ordinary Variables");
		variablePane = createOrdinaryVariableTableAndEdit(rule.getOrdinaryVariableList());
		
		panelSet = new JSplitPane(JSplitPane.VERTICAL_SPLIT); // panel to edit ov
		ordinaryVariablePanel = createOrdinaryVariablePanel();
		createSubEditPane();
		
		
		JSplitPane mainPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		mainPane.setPreferredSize(new Dimension(480, 380));
		mainPane.add(createEditPane());
		mainPane.add(createAddUpdateButton());
		this.add(mainPane);
//		buttonListerner();
	}
	
	public JSplitPane createEditPane() {
		JSplitPane editPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		editPane.add(titlePanel);
		editPane.add(panelSet);		
		return editPane;
	}
	
	public void createSubEditPane() {
		panelSet.add(ordinaryVariablePanel);
		panelSet.add(variablePane);
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
				String variableEdited = getVariableEdited();
				String entitySelected = getEntitySelected();
				EntityModel entityObject = getEntityObject();
				if(variableEdited == null || entitySelected == null) {
					System.err.println("Error. Select entity or edit variable!");
				} else {					
					String key = Integer.toString(ID);
					ordinaryVariable = new OrdinaryVariableModel(key, variableEdited,
							entitySelected, entityObject);
					
					rule.getOrdinaryVariableList().add(ordinaryVariable);
					updateOrdinaryVariableTable();
					ID++;
					//TODO ordinary variable is linked to rule and not to project					
				//	umpstProject.getMapOrdinaryVariable().put(key, ordinaryVariable);
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
	
	public void updateOrdinaryVariableTable() {
		UmpstModule janelaPai = getFatherPanel();
		ImplementationEditPanel ipanel = janelaPai.getMenuPanel().getImplementationPane().
				getImplementationTable().getImplementationEditPanel();
		
		ipanel.getOrdVariablePane().getVariablePane().setViewportView(
				createOrdinaryVariableTableAndEdit(rule.getOrdinaryVariableList()));
		ipanel.getOrdVariablePane().getVariablePane().updateUI();
		ipanel.getOrdVariablePane().getVariablePane().repaint();
		
		changePanel(ipanel);
		
	}
	
	// Edit variable and set type entity
	public JSplitPane createOrdinaryVariablePanel() {
		final List<EntityModel> entityList = rule.getEntityList();
		
		final JTextField variable = new JTextField(SIZE_COLUMNS_TEXT);
		
		// If needs to edit variable that was created	
		ordinaryVariableSelected = getOrdinaryVariableSelected();
		if (ordinaryVariableSelected  != null) {
			
			String key = ordinaryVariableSelected.getVariable();
//			System.out.println(key);
//			int index = Integer.parseInt(key);
//			
//			for (int i = 0; i < rule.getOrdinaryVariableList().size(); i++) {
//				System.out.println(rule.getOrdinaryVariableList().get(i).getId());
//			}
			
//			String text = rule.getOrdinaryVariableList().get(index).getVariable();
//			variable.setText(text);			
		}
		
		variable.setEditable(true);
		variable.addCaretListener(new CaretListener() {			
			public void caretUpdate(CaretEvent e) {
				setVariableEdited(variable.getText());
			}
		});		
		
		String[] entitiesNames = new String[entityList.size()];
		for (int i = 0; i < entityList.size(); i++) {
			entitiesNames[i] = entityList.get(i).getName();
		}	
		final JComboBox<String> typeEntitiesBox = new JComboBox<String>(entitiesNames);
		typeEntitiesBox.setSelectedIndex(0);
		typeEntitiesBox.addActionListener(new ActionListener() {			
			public void actionPerformed(ActionEvent e) {
				int index = typeEntitiesBox.getSelectedIndex();
				setEntitySelected(entityList.get(index).getName());
				setEntityObject(entityList.get(index));
			}
		});
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, variable,
				typeEntitiesBox);
		splitPane.setPreferredSize(new Dimension(380, 25));
		splitPane.setDividerLocation(200);
		
		return splitPane;
	}
	
	// Set data object of ordinary variable	
	public JScrollPane createOrdinaryVariableTableAndEdit(List<OrdinaryVariableModel> ordinaryVariableList) {
		Object[] columnNames = {"ID", "Ordinary Variable", "", ""};		
		
		Object[][] data = new Object[ordinaryVariableList.size()][NUM_COLUMNS_ICON];		
		for (int i = 0; i < ordinaryVariableList.size(); i++) {
			String sentence = "(" + ordinaryVariableList.get(i).getVariable() + ", " +
					ordinaryVariableList.get(i).getTypeEntity() + ")";
			
			String ordinaryVariableId = ordinaryVariableList.get(i).getId();	
			
			data[i][0] = ordinaryVariableId;
			data[i][1] = sentence;
			data[i][2] = "";
			data[i][3] = "";
		}		
		return createOrdinaryVariableTableAndEdit(data, columnNames);		
	}
	
	public JScrollPane createOrdinaryVariableTableAndEdit(final Object[][] data, Object[] columnNames) {		
		
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
				ordinaryVariableRow = data[row][0].toString();
				
				int i = 0;
				boolean flag = false;
				while((i < rule.getOrdinaryVariableList().size()) && (!flag)) {
					if(ordinaryVariableRow.equals(rule.getOrdinaryVariableList().
							get(i).getId())) {
						flag = true;
					} else {
						i++;
					}
				}				
				setOrdinaryVariableSelected(rule.getOrdinaryVariableList().get(i));
				
				UmpstModule janelaPai = getFatherPanel();
				ImplementationEditPanel ipanel = janelaPai.getMenuPanel().getImplementationPane().
						getImplementationTable().getImplementationEditPanel();				
				ipanel.getOrdVariablePane().getVariablePane().updateUI();				
				ipanel.getOrdVariablePane().getVariablePane().repaint();
				changePanel(ipanel);
			}
		});
		
		buttonDelete.addHandler(new TableButton.TableButtonPressedHandler() {			
			

			public void onButtonPress(int row, int column) {
				ordinaryVariableRow = data[row][0].toString();				
				
//					OrdinaryVariableModel ordinaryVariable = rule.getOrdinaryVariableList().get(index);
//					controller.removeOrdinaryVariableFromOrdinaryVariableList(ordinaryVariable, rule);
				int i = 0;
				boolean flag = false;
				while((i < rule.getOrdinaryVariableList().size()) && (!flag)) {
					if(ordinaryVariableRow.equals(rule.getOrdinaryVariableList().
							get(i).getId())) {
						flag = true;
						rule.getOrdinaryVariableList().remove(i);
					} else {
						i++;
					}
				}
				
				UmpstModule janelaPai = getFatherPanel();
				ImplementationEditPanel ipanel = janelaPai.getMenuPanel().getImplementationPane().
						getImplementationTable().getImplementationEditPanel();
				
				ipanel.getOrdVariablePane().getVariablePane().setViewportView(
						createOrdinaryVariableTableAndEdit(rule.getOrdinaryVariableList()));
				ipanel.getOrdVariablePane().getVariablePane().updateUI();
				ipanel.getOrdVariablePane().getVariablePane().repaint();
				changePanel(ipanel);
			}
		});
		
		variableEditTable.setPreferredScrollableViewportSize(variableEditTable.getPreferredSize());
//			variableTable.setFillsViewportHeight(true);
		variableEditTable.updateUI();
		variableEditTable.repaint();
		
		JScrollPane scrollpane = new JScrollPane(variableEditTable);
		scrollpane.setPreferredSize(new Dimension(80, 80));
				
		return scrollpane;
	}
	
	/**
	 * Set ID according to the last necessary condition ID created.
	 */
	public void setID() {
		int greaterID = -1;
		boolean beginID = true; // created to set ID = 0
		for (int i = 0; i < rule.getOrdinaryVariableList().size(); i++) {
			if (greaterID < Integer.parseInt(rule.getOrdinaryVariableList().get(i).getId())) {
				greaterID = Integer.parseInt(rule.getOrdinaryVariableList().get(i).getId());
				beginID = false;
			}
		}
		if (!beginID) {
			ID = greaterID+1;
		}
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
	 * @return the variableEdited
	 */
	public String getVariableEdited() {
		return variableEdited;
	}

	/**
	 * @param variableEdited the variableEdited to set
	 */
	public void setVariableEdited(String variableEdited) {
		this.variableEdited = variableEdited;
	}

	/**
	 * @return the entitySelected
	 */
	public String getEntitySelected() {
		return entitySelected;
	}

	/**
	 * @param entitySelected the entitySelected to set
	 */
	public void setEntitySelected(String entitySelected) {
		this.entitySelected = entitySelected;
	}

	/**
	 * @return the ordinaryVariableSelected
	 */
	public OrdinaryVariableModel getOrdinaryVariableSelected() {
		return ordinaryVariableSelected;
	}

	/**
	 * @param ordinaryVariableSelected the ordinaryVariableSelected to set
	 */
	public void setOrdinaryVariableSelected(OrdinaryVariableModel ordinaryVariableSelected) {
		this.ordinaryVariableSelected = ordinaryVariableSelected;
	}

	/**
	 * @return the entityObject
	 */
	public EntityModel getEntityObject() {
		return entityObject;
	}

	/**
	 * @param entityObject the entityObject to set
	 */
	public void setEntityObject(EntityModel entityObject) {
		this.entityObject = entityObject;
	}
}
