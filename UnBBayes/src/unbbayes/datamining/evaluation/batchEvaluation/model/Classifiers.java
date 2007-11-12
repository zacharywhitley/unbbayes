package unbbayes.datamining.evaluation.batchEvaluation.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.ResourceBundle;

import unbbayes.datamining.classifiers.ClassifierIDs;


/**
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 11/08/2007
 */
public class Classifiers implements Serializable {

	private static final long serialVersionUID = 1L;
	private transient ResourceBundle resource;
	private int numColumns;
	private ArrayList<Object[]> dataTable;
	private String[] columnNames;
	private String[] classifierNames = ClassifierIDs.getClassifierNames();
	
	public Classifiers(ResourceBundle resource) {
		this.resource = resource;
		buildDataTable();
	}
	
	public void buildDataTable() {
		dataTable = new ArrayList<Object[]>();
		
		String[] columnNames = {
				/* Active */
				resource.getString("activeTableHeader"),

				/* Classifier name */
				resource.getString("classifierNameTableHeader"),
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

	public void removeClassifier(int row) {
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
	
	public String getClassifierName(int i) {
		return (String) dataTable.get(i)[1];
	}
	
	public ArrayList<Object[]> getDataTable() {
		return dataTable;
	}

	public String[] getClassifierNames() {
		if (classifierNames == null) {
			classifierNames = ClassifierIDs.getClassifierNames();
		}
		
		return classifierNames.clone();
	}

}

