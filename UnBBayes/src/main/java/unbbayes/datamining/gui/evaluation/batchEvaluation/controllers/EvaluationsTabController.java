package unbbayes.datamining.gui.evaluation.batchEvaluation.controllers;

import java.util.ResourceBundle;

import javax.swing.JTable;

import unbbayes.datamining.evaluation.batchEvaluation.BatchEvaluation;
import unbbayes.datamining.evaluation.batchEvaluation.model.Evaluations;
import unbbayes.datamining.gui.evaluation.batchEvaluation.BatchEvaluationMain;
import unbbayes.datamining.gui.evaluation.batchEvaluation.EvaluationsTab;

/**
 * 
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 02/08/2007
 */
public class EvaluationsTabController {

	private static final long serialVersionUID = 1L;
	private ResourceBundle resource;
	private BatchEvaluationMain mainView;
	private EvaluationsTab view;
	private BatchEvaluation model;
	private Evaluations data;

	public EvaluationsTabController(EvaluationsTab view, BatchEvaluation model) {
		this.view = view;
		this.model = model;
		mainView = view.getMainView();
		resource = mainView.getResourceBundle();
		data = model.getEvaluationData();
	}

	public void insertData() {
		Object[] dataTableEntry = new Object[] {
				new Boolean(true),
				data.getEvaluationNames()[0]
		};
		String[] evaluationNames = data.getEvaluationNames();
		view.addRow(dataTableEntry, evaluationNames);
	}

	public void editData(JTable evaluationTable) {
		// TODO Auto-generated method stub
		
	}

	public void updateData() {
		data = model.getEvaluationData();
		view.updateData();
	}
	
	public ResourceBundle getResourceBundle() {
		return resource;
	}

	public Evaluations getData() {
		return data;
	}

	public EvaluationsTabController getController() {
		return this;
	}

}
