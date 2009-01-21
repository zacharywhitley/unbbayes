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
package unbbayes.evaluation.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import com.ibm.icu.text.NumberFormat;
import com.sun.tools.corba.se.idl.constExpr.EvaluationException;

import unbbayes.evaluation.Evaluation.EvidenceEvaluation;
import unbbayes.gui.table.NumberEditor;
import unbbayes.gui.table.PercentEditor;
import unbbayes.gui.table.RadioButtonCellEditor;
import unbbayes.gui.table.RadioButtonCellRenderer;

public class EvaluationPane extends JPanel {

	private static final long serialVersionUID = 1L;
	
	private JPanel inputPane;
	private JTable inputTable;
	private JLabel sampleSizeLabel;
	private JFormattedTextField sampleSizeTextField;
	private JButton runButton;
	
	private JPanel outputPane;
	private JTable outputTable;
	private JLabel pccLabel;
	private JLabel pccValueLabel;

	public EvaluationPane() {
		super(new GridLayout(2, 0));
		
		setUpInputPane();
		
		setUpOutputPane();
		
		add(inputPane);
		add(outputPane);

	}
	
	private void setUpOutputPane() {
		outputPane = new JPanel(new BorderLayout());
		
		setUpPccPane();
		
		setUpOutputTable();
	}
	
	private void setUpPccPane() {
		JPanel pccPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		pccLabel = new JLabel("Probability of Correct Classification:");
		pccValueLabel = new JLabel("");
		
		pccPane.add(pccLabel);
		pccPane.add(pccValueLabel);
		
		outputPane.add(pccPane, BorderLayout.NORTH);
	}
	
	private void setUpInputPane() {
		inputPane = new JPanel(new BorderLayout());
		
		setUpInputTable();
		
		setUpSampleSizePane();
		
	}
	
	private void setUpSampleSizePane() {
		JPanel sampleSizePane = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		sampleSizeLabel = new JLabel("Sample Size:");
		sampleSizeTextField = new JFormattedTextField(NumberFormat.getIntegerInstance());
		sampleSizeTextField.setColumns(10);
		
		runButton = new JButton("Run");
		
		sampleSizePane.add(sampleSizeLabel);
		sampleSizePane.add(sampleSizeTextField);
		sampleSizePane.add(runButton);
		
		inputPane.add(sampleSizePane, BorderLayout.SOUTH);
	}

	private void setUpInputTable() {
		TableModel dm = new EvaluationInputTableModel();
		
		inputTable = new JTable(dm);
		inputTable.setPreferredScrollableViewportSize(new Dimension(500, 70));

		// Create the scroll pane and add the table to it.
		JScrollPane scrollPane = new JScrollPane(inputTable);

		inputTable.getColumn("Target").setCellRenderer(
				new RadioButtonCellRenderer());
		inputTable.getColumn("Target").setCellEditor(
				new RadioButtonCellEditor());

		inputTable.setDefaultEditor(Float.class, new NumberEditor());
		
		inputPane.add(scrollPane, BorderLayout.CENTER);
	}

	private void setUpOutputTable() {
		TableModel dm = new EvaluationOutputTableModel();

		outputTable = new JTable(dm);
		outputTable.setPreferredScrollableViewportSize(new Dimension(500, 70));

		// Create the scroll pane and add the table to it.
		JScrollPane scrollPane = new JScrollPane(outputTable);
		
		
		outputTable.getColumn("Marginal PCC (%)").setCellEditor(
				new PercentEditor());
		outputTable.getColumn("Marginal Improvement (%)").setCellEditor(
				new PercentEditor());
		outputTable.getColumn("Individual PCC (%)").setCellEditor(
				new PercentEditor());
		outputTable.getColumn("Cost").setCellEditor(
				new NumberEditor());
		outputTable.getColumn("Individual Cost Rate").setCellEditor(
				new NumberEditor());

		outputPane.add(scrollPane, BorderLayout.CENTER);
	}
	
	public void setRunButtonActionListener(ActionListener actionListener) {
		runButton.addActionListener(actionListener);
		
	}
	
	public Integer getSampleSizeValue() {
		return ((Long)sampleSizeTextField.getValue()).intValue();
	}
	
	public List<String> getTargetNodeNameList() {
		List<String> targetNodeNameList = new ArrayList<String>();
		
		TableModel dm = inputTable.getModel();
		
		for (int i = 0; i < dm.getRowCount(); i++) {
			if ((Boolean)dm.getValueAt(i, 1)) {
				targetNodeNameList.add((String)dm.getValueAt(i, 0));
			}
		}
		
		return targetNodeNameList;
	}

	public List<String> getEvidenceNodeNameList() {
		List<String> evidenceNodeNameList = new ArrayList<String>();
		
		TableModel dm = inputTable.getModel();
		
		for (int i = 0; i < dm.getRowCount(); i++) {
			if ((Boolean)dm.getValueAt(i, 2)) {
				evidenceNodeNameList.add((String)dm.getValueAt(i, 0));
			}
		}
		
		return evidenceNodeNameList;
	}
	
	public float getCost(String nodeName) {
		
		TableModel dm = inputTable.getModel();
		
		for (int i = 0; i < dm.getRowCount(); i++) {
			// If it is an evidence node and it has the name given
			if ((Boolean)dm.getValueAt(i, 2) && dm.getValueAt(i, 0).equals(nodeName)) {
				// Return its cost
				return (Float)dm.getValueAt(i, 3);
			}
		}
		
		return 0.0f;
	}
	
	public void setPccValue(float pccValue) {
		pccValueLabel.setText("" + pccValue);
	}
	
	public void addOutputValues(List<EvidenceEvaluation> evidenceEvaluationList) throws EvaluationException {
		((EvaluationOutputTableModel)outputTable.getModel()).addValues(evidenceEvaluationList);
		outputTable.revalidate();
	}

	private class EvaluationOutputTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		private String[] columnNames = { "Node", "Marginal PCC (%)",
				"Marginal Improvement (%)", "Individual PCC (%)", "Cost",
				"Individual Cost Rate" };

		private Object[][] data  = new Object[0][6];

		public void addValues(List<EvidenceEvaluation> evidenceEvaluationList) throws EvaluationException {
			data = new Object[evidenceEvaluationList.size()][getColumnCount()];
			EvidenceEvaluation evidenceEvaluation;
			for (int i = 0; i < evidenceEvaluationList.size(); i++) {
				evidenceEvaluation = evidenceEvaluationList.get(i);
				data[i][0] = evidenceEvaluation.getName();
				data[i][1] = evidenceEvaluation.getMarginalPCC();
				data[i][2] = evidenceEvaluation.getMarginalImprovement();
				data[i][3] = evidenceEvaluation.getIndividualPCC();
				data[i][4] = evidenceEvaluation.getCost();
				data[i][5] = evidenceEvaluation.getCostRate();
			}
		}

		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			return data.length;
		}

		public String getColumnName(int col) {
			return columnNames[col];
		}

		public Object getValueAt(int row, int col) {
			return data[row][col];
		}

		/**
		 * JTable uses this method to determine the default renderer/ editor for
		 * each cell. If we didn't implement this method, then the last column
		 * would contain text ("true"/"false"), rather than a check box.
		 */
		public Class getColumnClass(int c) {
			if (c == 0) {
				return String.class;
			} else {
				return Float.class;
			}
		}

		public boolean isCellEditable(int row, int col) {
			return false;
		}

	}

	public void addInputValues(List<String> nodeNameList) {
		((EvaluationInputTableModel)inputTable.getModel()).addValues(nodeNameList);
		inputTable.revalidate();
	}
	
	private class EvaluationInputTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		private String[] columnNames = { "Node", "Target", "Evidence", "Cost" };

		private Object[][] data = new Object[0][4];
		
		public void addValues(List<String> nodeNameList) {
			data = new Object[nodeNameList.size()][getColumnCount()];
			for (int i = 0; i < nodeNameList.size(); i++) {
				data[i][0] = nodeNameList.get(i);
				data[i][1] = Boolean.FALSE;
				data[i][2] = Boolean.FALSE;
				data[i][3] = new Float(100.00);
			}
		}

		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			return data.length;
		}

		public String getColumnName(int col) {
			return columnNames[col];
		}

		public Object getValueAt(int row, int col) {
			return data[row][col];
		}

		public Class getColumnClass(int c) {
			if (c == 0) {
				return String.class;
			} else if (c == 1 || c == 2) {
				return Boolean.class;
			} else {
				return Float.class;
			}
		}

		public boolean isCellEditable(int row, int col) {
			// Just the node name is not editable
			return (col != 0);
		}

		public void setValueAt(Object value, int row, int col) {
			data[row][col] = value;
			fireTableCellUpdated(row, col);

			// If selected as target
			if (col == 1 && value.equals(Boolean.TRUE)) {
				// Make sure there is just one target selected
				resetNonSelectedValues(row, col);
				// Make sure it is not selected as evidence
				setValueAt(Boolean.FALSE, row, col + 1);
				fireTableCellUpdated(row, col + 1);
				// If selected as evidence
			} else if (col == 2 && value.equals(Boolean.TRUE)) {
				// Make sure it is not selected as target
				setValueAt(Boolean.FALSE, row, col - 1);
				fireTableCellUpdated(row, col - 1);
			}
		}

		/**
		 * This will give the behavior of a ButtonGroup
		 */
		private void resetNonSelectedValues(int newRow, int col) {
			for (int row = 0; row < data.length; row++) {
				if (getValueAt(row, col).equals(Boolean.TRUE) && row != newRow) {
					setValueAt(Boolean.FALSE, row, col);
					fireTableCellUpdated(row, col);
				}
			}
		}

	}

	/**
	 * Create the GUI and show it. For thread safety, this method should be
	 * invoked from the event-dispatching thread.
	 */
	private static void createAndShowGUI() {
		// Create and set up the window.
		JFrame frame = new JFrame("TableRenderDemo");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Create and set up the content pane.
		EvaluationPane newContentPane = new EvaluationPane();
		newContentPane.setOpaque(true); // content panes must be opaque
		frame.setContentPane(newContentPane);

		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {

		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});

		// JFrame frame = new JFrame("Evaluation Test");
		//
		// EvaluationPane pane = new EvaluationPane();
		//
		// List<String> nodeNameList = new ArrayList<String>();
		// nodeNameList.add("Node1");
		// nodeNameList.add("Node2");
		// nodeNameList.add("Node3");
		// nodeNameList.add("Node4");
		//
		// frame.add(pane);
		// frame.setSize(800, 450);
		// frame.setVisible(true);
		// frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}

}
