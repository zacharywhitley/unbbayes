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
package unbbayes.datamining.evaluation.batchEvaluation.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.ResourceBundle;

import unbbayes.datamining.gui.evaluation.EvaluationIDs;

/**
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 11/08/2007
 */
public class Evaluations implements Serializable {

	private static final long serialVersionUID = 1L;
	private transient ResourceBundle resource;
	private int numColumns;
	private ArrayList<Object[]> dataTable;
	private String[] columnNames;
	private String[] evaluationNames = EvaluationIDs.getEvaluationNames();
	
	public Evaluations(ResourceBundle resource) {
		this.resource = resource;
		buildDataTable();
	}
	
	public void buildDataTable() {
		dataTable = new ArrayList<Object[]>();
		
		String[] columnNames = {
				/* Active */
				resource.getString("activeTableHeader"),

				/* Evaluations name */
				resource.getString("evaluationNameTableHeader"),
		};

		this.columnNames = columnNames;
		numColumns = columnNames.length;
	}
	
	public void loadScript(ArrayList<Object[]> dataTable) {
		this.dataTable = dataTable;
	}

	public String getColumnName(int col) {
		return columnNames[col].toString();
	}
	
	public int getColumnCount() {
		return numColumns;
	}
	public int getRowCount() {
		return dataTable.size();
	}

	public void addRow(Object[] rowData) {
		dataTable.add(rowData);
	}

	public void removeEvaluation(int row) {
		if (row >= 0) {
			dataTable.remove(row);
		}
	}

	public Object getValueAt(int row, int col) {
		return dataTable.get(row)[col];
	}

	public void setValueAt(Object value, int row, int col) {
		dataTable.get(row)[col] = value;
	}
	
	public boolean getActive(int i) {
		return (Boolean) dataTable.get(i)[0];
	}
	
	public String getEvaluationName(int i) {
		return (String) dataTable.get(i)[1];
	}
	
	public int getEvaluationID(int i) {
		return EvaluationIDs.getEvaluationID((String) dataTable.get(i)[1]);
	}
	
	public ArrayList<Object[]> getDataTable() {
		return dataTable;
	}

	public String[] getEvaluationNames() {
		if (evaluationNames == null) {
			evaluationNames = EvaluationIDs.getEvaluationNames();
		}
		
		return evaluationNames.clone();
	}

	public boolean isBuildROC() {
		for (int i = 0; i < dataTable.size(); i++) {
			if (getActive(i) && 
					EvaluationIDs.ROC_POINTS_NAME.equals(dataTable.get(i)[1])) {
				return true;
			}
		}
		
		return false;
	}

	public boolean isComputeAUC() {
		for (int i = 0; i < dataTable.size(); i++) {
			if (getActive(i) && 
					EvaluationIDs.AUC_NAME.equals(dataTable.get(i)[1])) {
				return true;
			}
		}
		
		return false;
	}

}

