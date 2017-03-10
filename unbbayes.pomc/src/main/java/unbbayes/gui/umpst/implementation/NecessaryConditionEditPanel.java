/**
 * 
 */
package unbbayes.gui.umpst.implementation;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import unbbayes.controller.umpst.FormulaTreeControllerUMP;
import unbbayes.controller.umpst.IconController;
import unbbayes.gui.umpst.IUMPSTPanel;
import unbbayes.gui.umpst.MainPanel;
import unbbayes.gui.umpst.TableButton;
import unbbayes.gui.umpst.UmpstModule;
import unbbayes.model.umpst.entity.RelationshipModel;
import unbbayes.model.umpst.group.GroupModel;
import unbbayes.model.umpst.implementation.NecessaryConditionVariableModel;
import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.model.umpst.rule.RuleModel;

/**
 * This class has main edition pane for context nodes.
 * @author Diego Marques
 */
public class NecessaryConditionEditPanel extends IUMPSTPanel{
	
	private int ID = 0;
	private JPanel titleLabel;
	private final int SIZE_COLUMNS_ICON = 25;
	private final int SIZE_COLUMNS_TEXT = 300;
	private int NUM_COLUMNS_ICON = 4;
	private final int NUM_COLUMNS_TEXT = 1;
	private IconController iconController;
	
	private UMPSTProject umpstProject;
	private RuleModel rule;
	private NecessaryConditionVariableModel ncVariableModel; 
	
	// All relationships of the model implemented
	private List<RelationshipModel> relationshipListModel; 
	
	private ImplementationMainPropertiesEditionPane mainPropertiesEditionPane;
	private FormulaEditionPane formulaEditionPane;
	private FormulaTreeControllerUMP formulaTreeController;
	private FormulaViewTreePane formulaViewTreePane;
	private FormulaVariablePane formulaVariablePane;
	private JScrollPane editionList;
	
	private JPanel cnEditionPane;
	private JPanel rvListPane;

	public NecessaryConditionEditPanel(UmpstModule janelaPai, UMPSTProject umpstProject,
		RuleModel rule) {
		
		super(janelaPai);
		this.umpstProject = umpstProject;
		this.rule = rule;		
		this.setLayout(new FlowLayout());		
		setID();		
		
//		cnEditionPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
//		rvListPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		cnEditionPane = new JPanel(new BorderLayout());
		rvListPane = new JPanel(new BorderLayout());
		
		iconController = IconController.getInstance();
		relationshipListModel = new ArrayList<RelationshipModel>();
		setAllRelationshipModel();
		
		String key = getID(); // see last id in rule
		ncVariableModel = new NecessaryConditionVariableModel(key, null);
		
		mainPropertiesEditionPane = new ImplementationMainPropertiesEditionPane(
				getFatherPanel(), getUmpstProject(), rule);
		formulaEditionPane = new FormulaEditionPane(this, umpstProject, rule, ncVariableModel, false);
		formulaVariablePane = new FormulaVariablePane(umpstProject, rule, formulaEditionPane);
		editionList = createNCVariableTableAndEdit(rule.getNecessaryConditionList());
		
		titleLabel = mainPropertiesEditionPane.createTitleLabel("Necessary Condition");
		
		this.add(createMainPanel());
	}
	
	/*
	 * Done to keep all relationships present in model and that can be used
	 * in some necessary condition rule.
	 */
	public void setAllRelationshipModel() {
		Set<String> keys = umpstProject.getMapGroups().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);
		for (String keyAux : sortedKeys){
			GroupModel group = umpstProject.getMapGroups().get(keyAux);
			for (int i = 0; i < group.getBacktrackingRelationship().size(); i++) {
				relationshipListModel.add(group.
						getBacktrackingRelationship().get(i));				
			}
		}
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
				String key = getID();
																	
				boolean duplicated = false;
				boolean edited = false;
				int indexToEdit = 0;
				for (int i = 0; i < rule.getNecessaryConditionList().size(); i++) {
					if ((rule.getNecessaryConditionList().get(i).getFormula().
							equals(ncVariableModel.getFormula())) && !(rule.getNecessaryConditionList().get(i).getId().equals(
									ncVariableModel.getId()))) {
						System.err.println("Error. Condtion duplicated.");
						duplicated = true;
						break;
					} else if (rule.getNecessaryConditionList().get(i).getId().equals(
							ncVariableModel.getId())) {
						edited = true;
						break;
					}
					indexToEdit++;
				}
				if (!duplicated && !edited) {
					NecessaryConditionVariableModel ncVariableAdded = new NecessaryConditionVariableModel(key, null);				
					ncVariableAdded.setFormula(ncVariableModel.getFormula());
					ncVariableAdded.setFormulaTree(ncVariableModel.getFormulaTree());
					
					rule.getNecessaryConditionList().add(ncVariableAdded);
					ID++;
					updateNCVariableTable();
				} else if (edited) {
					rule.getNecessaryConditionList().get(indexToEdit).setFormula(ncVariableModel.getFormula());
					rule.getNecessaryConditionList().get(indexToEdit).setFormulaTree(ncVariableModel.getFormulaTree());
					updateNCVariableTable();
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
	
	public JScrollPane createNCVariableTableAndEdit(List<NecessaryConditionVariableModel> necessaryConditionList) {
		Object[] columnNames = {"ID", "Necessary Condition", "", ""};
		Object[][] data = new Object[necessaryConditionList.size()][NUM_COLUMNS_ICON];
		
		String formula = null;
		for (int i = 0; i < necessaryConditionList.size(); i++) {
			if (necessaryConditionList.get(i).getFormula() != null) {
				formula = necessaryConditionList.get(i).getFormula();
			}
			String ncId = necessaryConditionList.get(i).getId();
			data[i][0] = ncId;
			data[i][1] = formula;
			data[i][2] = "";
			data[i][3] = "";
		}		
		return createNCVariableTableAndEdit(data, columnNames);
	}
	
	public JScrollPane createNCVariableTableAndEdit(final Object[][] data, Object[] columnNames) {
		
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
				while((i < rule.getNecessaryConditionList().size()) && (!flag)) {
					if(variableRow.equals(rule.getNecessaryConditionList().get(i).getId())) {				
						flag = true;
					} else {
						i++;
					}
				}
				
				UmpstModule janelaPai = getFatherPanel();
				ImplementationEditPanel ipanel = janelaPai.getMenuPanel().getImplementationPane().getImplementationTable().
						getImplementationEditPanel();
				
//				NecessaryConditionVariableModel ncVariableEdited = new NecessaryConditionVariableModel(
//						rule.getNecessaryConditionList().get(i).getId(), null);
//				ncVariableEdited.setFormula(rule.getNecessaryConditionList().get(i).getFormula());
//				ncVariableEdited.setFormulaTree(rule.getNecessaryConditionList().get(i).getFormulaTree());	
				
				ncVariableModel.setId(rule.getNecessaryConditionList().get(i).getId());
				ncVariableModel.setFormula(rule.getNecessaryConditionList().get(i).getFormula());
				ncVariableModel.setFormulaTree(rule.getNecessaryConditionList().get(i).getFormulaTree());
				
				ipanel.getNecConditionPane().updateEditionFormulaPane();	
				changePanel(ipanel);
			}
		});
		
		buttonDelete.addHandler(new TableButton.TableButtonPressedHandler() {
			public void onButtonPress(int row, int column) {				
				String variableRow = data[row][0].toString();				
				int i = 0;
				boolean flag = false;
				while((i < rule.getNecessaryConditionList().size()) && (!flag)) {
					if(variableRow.equals(rule.getNecessaryConditionList().get(i).getId())) {
						flag = true;
						rule.getNecessaryConditionList().remove(i);
					} else {
						i++;
					}
				}				
				updateNCVariableTable();				
			}
		});
		
		variableEditTable.setPreferredScrollableViewportSize(variableEditTable.getPreferredSize());
		variableEditTable.updateUI();
		variableEditTable.repaint();
		
		JScrollPane scrollpane = new JScrollPane(variableEditTable);
		scrollpane.setPreferredSize(new Dimension(80, 80));
		
		return scrollpane;
	}
	
	public void updateEditionFormulaPane() {
		cnEditionPane.remove(formulaEditionPane);
		rvListPane.remove(formulaVariablePane);
		
		formulaEditionPane = new FormulaEditionPane(this, umpstProject, rule, ncVariableModel, true);
		formulaVariablePane = new FormulaVariablePane(umpstProject, rule, formulaEditionPane);
		
		cnEditionPane.add(formulaEditionPane);
		rvListPane.add(formulaVariablePane);
		cnEditionPane.revalidate();
		rvListPane.revalidate();
	}
	
	public void updateNewFormulaPane() {		
		cnEditionPane.remove(formulaEditionPane);
		rvListPane.remove(formulaVariablePane);
		
		String key = getID();
		ncVariableModel = new NecessaryConditionVariableModel(key, null);
		formulaEditionPane = new FormulaEditionPane(this, umpstProject, rule, ncVariableModel, false);
		formulaVariablePane = new FormulaVariablePane(umpstProject, rule, formulaEditionPane);
		
		cnEditionPane.add(formulaEditionPane);
		rvListPane.add(formulaVariablePane);
		cnEditionPane.revalidate();
		rvListPane.revalidate();
	}
	
	public void updateNCVariableTable() {
		UmpstModule janelaPai = getFatherPanel();
		ImplementationEditPanel ipanel = janelaPai.getMenuPanel().getImplementationPane().
				getImplementationTable().getImplementationEditPanel();		
		
		ipanel.getNecConditionPane().getEditionList().setViewportView(
				createNCVariableTableAndEdit(rule.getNecessaryConditionList()));
		ipanel.getNecConditionPane().getEditionList().updateUI();
		ipanel.getNecConditionPane().getEditionList().repaint();
		ipanel.getNecConditionPane().updateNewFormulaPane();
		
		changePanel(ipanel);
	}
	
	public JSplitPane createMainPanel() {
		JSplitPane mainPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		mainPane.setPreferredSize(new Dimension(480, 380));
		mainPane.add(createEditPanel()); // title and formula edition pane
		mainPane.add(createTableListAndAddUpdateButton()); // list of context nodes and add/update button
		return mainPane;
	}
	
	public JSplitPane createEditPanel() {
		JSplitPane editPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		editPane.add(titleLabel);
		editPane.add(createVariableEditionPane()); // select relationship or attribute and list items added
		return editPane;
	}
	
	public JSplitPane createVariableEditionPane() {
		JSplitPane variableEditionPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		variableEditionPane.add(createContextNodeEditionPane());
		variableEditionPane.add(createRVListPane());
		return variableEditionPane;
	}
	
	public JPanel createContextNodeEditionPane() {	    
		cnEditionPane.add(formulaEditionPane);
		return cnEditionPane;
	}
	
	public JPanel createRVListPane() {		
		rvListPane.add(formulaVariablePane); // Selection button RV or OV
		return rvListPane;
	}
	
	public JSplitPane createTableListAndAddUpdateButton() {
		JSplitPane tlAndAddUpdatePane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		tlAndAddUpdatePane.add(editionList);
		tlAndAddUpdatePane.add(createAddUpdateButton());
		return tlAndAddUpdatePane;
	}
	
	/**
	 * Set ID according to the last necessary condition ID created.
	 */
	public void setID() {
		int greaterID = -1;
		boolean beginID = true; // created to set ID = 0
		for (int i = 0; i < rule.getNecessaryConditionList().size(); i++) {
			if (greaterID < Integer.parseInt(rule.getNecessaryConditionList().get(i).getId())) {
				greaterID = Integer.parseInt(rule.getNecessaryConditionList().get(i).getId());
				beginID = false;
			}
		}
		if (!beginID) {
			ID = greaterID+1;
		}
	}
	
	/**
	 * Returns ID as String type. Was built to centralize the global variable.
	 * @return the ID
	 */
	public String getID() {
		return Integer.toString(ID);
	}

	/**
	 * @return the ncVariableModel
	 */
	public NecessaryConditionVariableModel getNcVariableModel() {
		return ncVariableModel;
	}

	/**
	 * @param ncVariableModel the ncVariableModel to set
	 */
	public void setNcVariableModel(NecessaryConditionVariableModel ncVariableModel) {
		this.ncVariableModel = ncVariableModel;
	}

	/**
	 * All relationships of the model implemented
	 * @return the relationshipListModel
	 */
	public List<RelationshipModel> getRelationshipListModel() {
		return relationshipListModel;
	}

	/**
	 * All relationships of the model implemented
	 * @param relationshipListModel the relationshipListModel to set
	 */
	public void setRelationshipListModel(List<RelationshipModel> relationshipListModel) {
		this.relationshipListModel = relationshipListModel;
	}

	/**
	 * @return the editionList
	 */
	public JScrollPane getEditionList() {
		return editionList;
	}

	/**
	 * @param editionList the editionList to set
	 */
	public void setEditionList(JScrollPane editionList) {
		this.editionList = editionList;
	}

	/**
	 * @return the formulaEditionPane
	 */
	public FormulaEditionPane getFormulaEditionPane() {
		return formulaEditionPane;
	}

	/**
	 * @param formulaEditionPane the formulaEditionPane to set
	 */
	public void setFormulaEditionPane(FormulaEditionPane formulaEditionPane) {
		this.formulaEditionPane = formulaEditionPane;
	}
	
}
