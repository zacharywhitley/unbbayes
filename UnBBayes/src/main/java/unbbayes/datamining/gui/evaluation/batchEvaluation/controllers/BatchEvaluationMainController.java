package unbbayes.datamining.gui.evaluation.batchEvaluation.controllers;

import java.awt.Cursor;
import java.io.File;
import java.util.ResourceBundle;

import unbbayes.controller.FileController;
import unbbayes.datamining.datamanipulation.FileUtils;
import unbbayes.datamining.evaluation.batchEvaluation.BatchEvaluation;
import unbbayes.datamining.gui.evaluation.batchEvaluation.BatchEvaluationMain;

/**
 * 
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 02/08/2007
 */
public class BatchEvaluationMainController {

	private static final long serialVersionUID = 1L;
	private ResourceBundle resource;
	private BatchEvaluationMain view;
	private BatchEvaluation model;

	public BatchEvaluationMainController(BatchEvaluationMain view) {
		this.view = view;
		resource = view.getResourceBundle();
		model = new BatchEvaluation();
	}

	public void fileExit() {
		view.dispose();
	}

	public void openScript() {
		String[] returnStatus = new String[1];
		File file = FileUtils.openBevFile(view, returnStatus);
		view.setStatusBar(returnStatus[0]);
		if (file == null) {
			return;
		}
		String returnValue = model.openScript(file);
		if (returnValue == null) {
			view.setStatusBar(returnStatus[0]);
		} else {
			view.setStatusBar(returnValue);
		}
		
		view.getDatasetsTabController().updateData();
		view.getPreprocessorsTabController().updateData();
		view.getClassifiersTabController().updateData();
		view.getEvaluationsTabController().updateData();
	}

	public void saveScript() {
		String[] returnStatus = new String[1];
		File file = FileUtils.saveBevFile(view, returnStatus);
		if (file == null) {
			return;
		}
		String returnValue = model.saveScript(file);
		if (returnValue == null) {
			view.setStatusBar(returnStatus[0]);
		} else {
			view.setStatusBar(returnValue);
		}
	}

	public void runScript() {
		view.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		Thread t = new Thread(new Runnable() {
			public void run() {
				try {
					view.disableFunctions();
					view.setStatusBar(resource.getString("runScriptRunning"));
					model.runScript(view.getLogsTabController());
					view.setStatusBar(resource.getString("runScriptSuccess"));
				} catch (Exception e) {
					view.setStatusBar(resource.getString("runScriptError") +
							" " + e.getMessage() + ": " + this.getClass().getName());
					e.printStackTrace();
				}
				view.enableFunctions();
			}
		});

		t.start();
		view.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}

	public ResourceBundle getResourceBundle() {
		return resource;
	}

	public BatchEvaluation getModel() {
		if (model == null) {
			model = new BatchEvaluation();
		}
		
		return model;
	}

	public void help() {
		try {
			FileController.getInstance().openHelp(view);
		} catch (Exception evt) {
			view.setStatusBar(resource.getString("helpError") + " " +
					evt.getMessage() + " " + this.getClass().getName());
		}
	}

}
