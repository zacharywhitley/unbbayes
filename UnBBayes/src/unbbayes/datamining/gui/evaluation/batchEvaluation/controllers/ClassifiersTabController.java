package unbbayes.datamining.gui.evaluation.batchEvaluation.controllers;

import java.util.ResourceBundle;

import javax.swing.JTable;

import unbbayes.datamining.evaluation.batchEvaluation.BatchEvaluation;
import unbbayes.datamining.evaluation.batchEvaluation.model.Classifiers;
import unbbayes.datamining.gui.evaluation.batchEvaluation.BatchEvaluationMain;
import unbbayes.datamining.gui.evaluation.batchEvaluation.ClassifiersTab;

/**
 * 
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 02/08/2007
 */
public class ClassifiersTabController {

	private static final long serialVersionUID = 1L;
	private ResourceBundle resource;
	private BatchEvaluationMain mainView;
	private ClassifiersTab view;
	private BatchEvaluation model;
	private Classifiers data;

	public ClassifiersTabController(ClassifiersTab view, BatchEvaluation model) {
		this.view = view;
		this.model = model;
		mainView = view.getMainView();
		resource = mainView.getResourceBundle();
		data = model.getClassifierData();
	}

	public void insertData() {
		Object[] dataTableEntry = new Object[] {
				new Boolean(true),
				data.getClassifierNames()[0]
		};
		String[] classifierNames = data.getClassifierNames();
		view.addRow(dataTableEntry, classifierNames);
	}

	public void editData(JTable classifierTable) {
		// TODO Auto-generated method stub
		
	}

	public void updateData() {
		data = model.getClassifierData();
		view.updateData();
	}
	
	public ResourceBundle getResourceBundle() {
		return resource;
	}

	public Classifiers getData() {
		return data;
	}

	public ClassifiersTabController getController() {
		return this;
	}

}
