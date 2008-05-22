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
package unbbayes.datamining.gui.evaluation.batchEvaluation.controllers;

import java.awt.Cursor;
import java.io.File;
import java.util.ResourceBundle;

import unbbayes.controller.JavaHelperController;
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
			JavaHelperController.getInstance().openHelp(view);
		} catch (Exception evt) {
			view.setStatusBar(resource.getString("helpError") + " " +
					evt.getMessage() + " " + this.getClass().getName());
		}
	}

}
