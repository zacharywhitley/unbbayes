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

import unbbayes.datamining.evaluation.batchEvaluation.PreprocessorParameters;
import unbbayes.datamining.preprocessor.imbalanceddataset.PreprocessorIDs;

/**
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 11/08/2007
 */
public class Preprocessors implements Serializable {

	private static final long serialVersionUID = 1L;
	private transient ResourceBundle resource;
	private int numColumns;
	private ArrayList<Object[]> dataTable;
	private String[] columnNames;
	private String[] preprocessorNames = PreprocessorIDs.getPreprocessorNames();
	private ArrayList<PreprocessorParameters> preprocessors;
	private int activeColumn = 0;
	private int preprocessorIDColumn = 1;
	
	public Preprocessors(ResourceBundle resource) {
		this.resource = resource;
		buildDataTable();
		preprocessors = new ArrayList<PreprocessorParameters>();
	}
	
	public void buildDataTable() {
		dataTable = new ArrayList<Object[]>();
		
		String[] columnNames = {
				/* Active */
				resource.getString("activeTableHeader"),

				/* InitializePreprocessors name */
				resource.getString("preprocessorNameTableHeader"),

//				/* Config */
//				resource.getString("configButtonTableHeader"),
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
		String preprocessorName = getPreprocessorName(getLastRow());
		addPreprocessor(getPreprocessorID(preprocessorName));
	}

	private int getPreprocessorID(String preprocessorName) {
		return PreprocessorIDs.getPreprocessorID(preprocessorName);
	}
	
	private int getLastRow() {
		return dataTable.size() - 1;
	}

	public void removePreprocessor(int row) {
		if (row >= 0) {
			dataTable.remove(row);
		}
	}

	public Object getValueAt(int row, int col) {
		return dataTable.get(row)[col];
	}

	public void setValueAt(Object value, int row, int col) {
		if (row >= dataTable.size()) {
			@SuppressWarnings("unused") int pau = 1;
			return;
		}
		dataTable.get(row)[col] = value;
		if (col == preprocessorIDColumn) {
			setPreprocessorID(getLastRow(), getPreprocessorID((String) value));
		}
		if (col == activeColumn ) {
			setActive(getLastRow(), (Boolean) value);
		}
	}
	
	private void setActive(int pos, boolean active) {
		preprocessors.get(pos).setActive(active);
	}

	public boolean isActive(int i) {
		return (Boolean) dataTable.get(i)[0];
	}
	
	public String getPreprocessorName(int row) {
		return (String) dataTable.get(row)[preprocessorIDColumn];
	}
	
	public ArrayList<Object[]> getDataTable() {
		return dataTable;
	}

	public String[] getPreprocessorNames() {
		if (preprocessorNames == null) {
			preprocessorNames = PreprocessorIDs.getPreprocessorNames();
		}
		
		return preprocessorNames.clone();
	}
	
	private void addPreprocessor(int preprocessorID) {
		preprocessors.add(new PreprocessorParameters(preprocessorID));
	}

	private void setPreprocessorID(int pos, int preprocessorID) {
		preprocessors.get(pos).setPreprocessorID(preprocessorID);
	}

	public PreprocessorParameters getPreprocessor(int pos) {
		return preprocessors.get(pos);
	}

	public PreprocessorParameters[] getActivePreprocessors() {
		int numPreprocessors = getNumActivePreprocessors();
		PreprocessorParameters[] preprocessors;
		preprocessors = new PreprocessorParameters[numPreprocessors];
		
		for (int i = 0; i < dataTable.size(); i++) {
			if (isActive(i)) {
				preprocessors[i] = getPreprocessor(i);
			}
		}
		
		return preprocessors;
	}

	public int getNumActivePreprocessors() {
		int numPreprocessors = 0;
		
		for (int i = 0; i < getRowCount(); i++) {
			if (isActive(i)) {
				++numPreprocessors;
			}
		}
		return numPreprocessors;
	}

}

