/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package unbbayes.datamining.gui.evaluation.batchEvaluation;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import net.miginfocom.layout.CC;
import net.miginfocom.swing.MigLayout;
import unbbayes.datamining.evaluation.batchEvaluation.BatchEvaluation;
import unbbayes.datamining.evaluation.batchEvaluation.model.Preprocessors;
import unbbayes.datamining.gui.UtilsGUI;
import unbbayes.datamining.gui.UtilsGUI.CheckBoxEditor;
import unbbayes.datamining.gui.UtilsGUI.EachRowEditor;
import unbbayes.datamining.gui.UtilsGUI.EachRowRenderer;
import unbbayes.datamining.gui.evaluation.batchEvaluation.controllers.PreprocessorsTabController;

/**
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 07/08/2007
 */
public class PreprocessorsTab {
	
	private String tabTitle;
	private ResourceBundle resource;
	private BatchEvaluationMain mainView;
	private PreprocessorsTabController controller;
	private UtilsGUI utilsGUI;
	private EachRowEditor rowEditorPreprocessor;
	private EachRowRenderer rowRendererPreprocessor;
	private Preprocessors data;
	private JTable table;

	public PreprocessorsTab(BatchEvaluationMain mainView, BatchEvaluation model) {
		this.mainView = mainView;
		utilsGUI = new UtilsGUI();
		resource = mainView.getResourceBundle();
		tabTitle = resource.getString("preprocessorsTabTitle");
		controller = new PreprocessorsTabController(this, model);
		data = controller.getData();
	}

	/**
	 * Return the This tab's title.
	 * @return This tab's title.
	 */
	public String getTabTitle() {
		return tabTitle;
	}
	
	/**
	* Build and return the panel created by this class.
	* @return This tab panel.
	 * @throws Exception 
	*/
	protected JPanel getTabPanel() throws Exception {
		/* Build the principal panel */
		JPanel panel = new JPanel(new MigLayout());

		/* Get the InitializePreprocessors table */
		table = gettable();

		/* Build scroll pane for the data table */
		JScrollPane dataTableScroll = new JScrollPane(table);
		dataTableScroll.setPreferredSize(new Dimension(1000, 150));
		panel.add(dataTableScroll, BorderLayout.NORTH);
		
		/* Add a separator line */
		panel.add(new JSeparator(), "growx, wrap, gaptop 10");

		/* Add insert button */
		String newButtonText = resource.getString("newButtonText");
		JButton newButton = new JButton(newButtonText);
		newButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						controller.insertData(data.getRowCount());
					}
				}
			);

		/* Add delete button */
		String deleteButtonText = resource.getString("deleteButtonText");
		JButton deleteButton = new JButton(deleteButtonText);
		deleteButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						removeRow();
					}
				}
			);

		/* Add config button */
		String configButtonText = resource.getString("configurationButtonText");
		JButton configButton = new JButton(configButtonText);
		configButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						controller.preprocessorConfig(table.getSelectedRow());
					}
				}
			);

//		/* Add details button */
//		String detailsButtonText = resource.getString("detailsButtonText");
//		JButton detailsButton = new JButton(detailsButtonText);
//		detailsButton.addActionListener(
//				new ActionListener() {
//					public void actionPerformed(ActionEvent e) {
//						controller.showDetailsPreprocessor(table);
//					}
//				}
//			);
	
		/* Add buttons to the panel */
		panel.add(newButton,     new CC().spanX(4).split(4).tag("other"));
		panel.add(deleteButton,  new CC().tag("other"));
		panel.add(configButton,  new CC().tag("other"));
//		panel.add(detailsButton, new CC().tag("other"));
		return panel;
	}
	
	public void loadPreprocessorData() {
		/* Start editor and renderer for class and counter combobox */
		rowEditorPreprocessor = utilsGUI.new EachRowEditor(table);
		rowRendererPreprocessor = utilsGUI.new EachRowRenderer();

		data = controller.getData();
		DefaultTableModel PreprocessorModel;
		PreprocessorModel = (DefaultTableModel) table.getModel();
		updateColumnModel();
		PreprocessorModel.fireTableDataChanged();
	}

	public void updateColumnModel() {
		String[] preprocessorNames;
		int numRows = table.getRowCount();
		for (int row = 0; row < numRows; row++) {
			preprocessorNames = data.getPreprocessorNames();
			changeColumnModel(preprocessorNames, row);
		}
	}

	private void changeColumnModel(String[] preprocessorNames, int row) {
		TableColumnModel columnModel = table.getColumnModel();
		
		/* Set the cell renderer and editor for the current column */
		rowEditorPreprocessor.setEditorAt(row, utilsGUI.new ComboBoxEditor(preprocessorNames));
		rowRendererPreprocessor.add(row, utilsGUI.new ComboBoxRenderer(preprocessorNames));
		columnModel.getColumn(1).setCellEditor(rowEditorPreprocessor);
		columnModel.getColumn(1).setCellRenderer(rowRendererPreprocessor);
	}
	
	public void addRow(Object[] dataTableEntry, String[] preprocessorNames) {
		/* Insert the InitializePreprocessors info into the table */
		DefaultTableModel PreprocessorModel;
		PreprocessorModel = (DefaultTableModel) table.getModel();
		PreprocessorModel.addRow(dataTableEntry);
		int row = table.getRowCount() - 1;

		/* Set the cell renderer and editor for the current column */
		changeColumnModel(preprocessorNames, row);
		
		/* Move focus to the inserted row */
		table.changeSelection(row, 0, false, false);
		table.requestFocusInWindow();
	}
	
	public void removeRow() {
		if (table.getRowCount() > 0) {
			DefaultTableModel PreprocessorModel;
			PreprocessorModel = (DefaultTableModel) table.getModel();
			int row = table.getSelectedRow();
			PreprocessorModel.removeRow(row);
			
			/* Remove editors and renderers */
			rowEditorPreprocessor.remove(row);
			rowRendererPreprocessor.remove(row);
			
			int rowFocus = table.getRowCount() - 1;
			table.changeSelection(rowFocus, 0, false, false);
			table.requestFocusInWindow();
		}
	}

	private JTable gettable() {
		/* Set the InitializePreprocessors table header */
		
		/* First, we build the recipient table */
		DefaultTableModel PreprocessorModel = new DefaultTableModel() {
			private static final long serialVersionUID = 1L;

			public String getColumnName(int col) {
				return data.getColumnName(col);
			}
			
			public int getColumnCount() {
				return data.getColumnCount();
			}

			public int getRowCount() {
				return data.getRowCount();
			}

			public void addRow(Object[] rowData) {
				data.addRow(rowData);
				super.fireTableDataChanged();
			}

			public void removeRow(int row) {
				if (row >= 0) {
					data.removePreprocessor(row);
					super.fireTableDataChanged();
				}
			}

			public Object getValueAt(int row, int col) {
				return data.getValueAt(row, col);
			}

			public void setValueAt(Object value, int row, int col) {
				data.setValueAt(value, row, col);
				fireTableCellUpdated(row, col);
			}

			public boolean isCellEditable(int row, int col) {
				return true;
			}
			
		};

		JTable table = new JTable(PreprocessorModel);

		/* Start editor and renderer for class and counter combobox */
		rowEditorPreprocessor = utilsGUI.new EachRowEditor(table);
		rowRendererPreprocessor = utilsGUI.new EachRowRenderer();

		/* Set the editor used in this table */
		TableColumnModel columnModel = table.getColumnModel();
		columnModel.getColumn(0).setCellEditor(new CheckBoxEditor());
		columnModel.getColumn(0).setCellRenderer(utilsGUI.new CheckBoxRenderer());
		
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		int width;

		/* Set "Active" column width */
		width = 50;
		table.getColumnModel().getColumn(0).setPreferredWidth(width);

		/* Set "InitializePreprocessors name" column width */
		width = 400;
		table.getColumnModel().getColumn(1).setPreferredWidth(width);

//		/* Set "Config" column width */
//		width = 50;
//		table.getColumnModel().getColumn(2).setPreferredWidth(width);
//
		/* Deny reordering of rows */
		table.getTableHeader().setReorderingAllowed(false);
		
		/* Set the row height just enough to show all info of comboboxes */
		table.setRowHeight(table.getRowHeight() + 5);
		
		return table;
	}

	public PreprocessorsTabController getController() {
		return controller;
	}

	public void updateData() {
		/* Start editor and renderer for class and counter combobox */
		rowEditorPreprocessor = utilsGUI.new EachRowEditor(table);
		rowRendererPreprocessor = utilsGUI.new EachRowRenderer();

		data = controller.getData();
		DefaultTableModel datasetModel;
		datasetModel = (DefaultTableModel) table.getModel();
		updateColumnModel();
		datasetModel.fireTableDataChanged();
	}

	public BatchEvaluationMain getMainView() {
		return mainView;
	}

	public String getSelectedPreprocessorName() {
		return (String) table.getValueAt(table.getSelectedRow(), 1);
	}

}

