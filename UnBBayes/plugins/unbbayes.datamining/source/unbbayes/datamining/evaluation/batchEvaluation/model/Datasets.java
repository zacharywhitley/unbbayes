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

/**
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 11/08/2007
 */
public class Datasets implements Serializable {

	private static final long serialVersionUID = 1L;
	private transient ResourceBundle resource;
	private ArrayList<ArrayList<String>> attributes;
	private int numColumns;
	private ArrayList<Object[]> dataTable;
	private String[] columnNames;
	
	public Datasets(ResourceBundle resource) {
		this.resource = resource;
		buildDataTable();
	}
	
	public void buildDataTable() {
		dataTable = new ArrayList<Object[]>();
		attributes = new ArrayList<ArrayList<String>>();
		
		String[] columnNames = {
				/* Active */
				resource.getString("activeTableHeader"),

				/* Finished */
				resource.getString("finishedTableHeader"),
				
				/* Datasets name */
				resource.getString("datasetNameTableHeader"),
				
				/* Class */
				resource.getString("classTableHeader"),
				
				/* Counter */
				resource.getString("counterTableHeader"),
				
				/* File */
				resource.getString("fileTableHeader"),
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

	public void removeDataset(int row) {
		if (row >= 0) {
			dataTable.remove(row);
			attributes.remove(row);
		}
	}

	public Object getValueAt(int row, int col) {
		return dataTable.get(row)[col];
	}

	public void setValueAt(Object value, int row, int col) {
		dataTable.get(row)[col] = value;
	}
	
	public boolean isActive(int i) {
		return (Boolean) dataTable.get(i)[0];
	}
	
	public String getDatasetName(int i) {
		return (String) dataTable.get(i)[2];
	}
	
	public String getDatasetFullName(int i) {
		return (String) dataTable.get(i)[5];
	}
	
	public int getClassIndex(int i) {
		int internalIndex = findAttribute((String) dataTable.get(i)[3], i);
		
		/* The internal attributes are mapped this way:
		 * 0: ""
		 * 1: First attribute name
		 * 2: Second attribute name
		 * and so on.
		 * Thus we must subtract 1 of the internalIndex representation.
		 */
		--internalIndex;
		
		return internalIndex;
	}
	
	public int getCounterIndex(int i) {
		int internalIndex = findAttribute((String) dataTable.get(i)[4], i);
		
		/* The internal attributes are mapped this way:
		 * 0: ""
		 * 1: First attribute name
		 * 2: Second attribute name
		 * and so on.
		 * Thus we must subtract 1 of the internalIndex representation.
		 */
		--internalIndex;
		
		return internalIndex;
	}
	
	private int findAttribute(String attributeName, int row) {
		ArrayList<String> attributes = this.attributes.get(row);
		int numAttributes = attributes.size();
		
		for (int att = 0; att < numAttributes; att ++) {
			if (attributes.get(att).equals(attributeName)) {
				return att;
			}
		}
		
		return -1;
	}

	public String getFilePath(int i) {
		return (String) dataTable.get(i)[5];
	}
	
	public ArrayList<Object[]> getDataTable() {
		return dataTable;
	}

	public String[] getAttributes(int row) {
		ArrayList<String> attributes;
		attributes = this.attributes.get(row);
		int numAttributes = attributes.size();
		String[] result = new String[numAttributes];
		
		for (int att = 0; att < numAttributes; att++) {
			result[att] = new String((String) attributes.get(att));
		}
		
		return result;
	}

	public void addAttributes(ArrayList<String> newAttributes) {
		attributes.add(newAttributes);
	}
	
	public int getNumActiveData() {
		int numData = 0;
		int dataTableSize = dataTable.size();
		
		for (int i = 0; i < dataTableSize; i++) {
			if (isActive(i)) {
				++numData;
			}
		}
		
		return numData;
	}
	
}

