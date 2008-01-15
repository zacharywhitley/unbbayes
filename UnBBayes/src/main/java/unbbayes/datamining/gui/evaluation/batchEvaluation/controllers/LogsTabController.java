package unbbayes.datamining.gui.evaluation.batchEvaluation.controllers;

import java.util.ResourceBundle;

import unbbayes.datamining.evaluation.batchEvaluation.BatchEvaluation;
import unbbayes.datamining.evaluation.batchEvaluation.model.Logs;
import unbbayes.datamining.gui.evaluation.batchEvaluation.BatchEvaluationMain;
import unbbayes.datamining.gui.evaluation.batchEvaluation.LogsTab;

/**
 * 
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 02/08/2007
 */
public class LogsTabController {

	private static final long serialVersionUID = 1L;
	private ResourceBundle resource;
	private BatchEvaluationMain mainView;
	private LogsTab view;
	private BatchEvaluation model;
	private Logs data;

	public LogsTabController(LogsTab view, BatchEvaluation model) {
		this.view = view;
		this.model = model;
		mainView = view.getMainView();
		resource = mainView.getResourceBundle();
		data = model.getLogData();
	}

	public void clearData() {
		view.clearData();
	}

	public void insertData(String log) {
		view.insertData(log);
	}

	public ResourceBundle getResourceBundle() {
		return resource;
	}

	public Logs getData() {
		return data;
	}

	public LogsTabController getController() {
		return this;
	}

}
