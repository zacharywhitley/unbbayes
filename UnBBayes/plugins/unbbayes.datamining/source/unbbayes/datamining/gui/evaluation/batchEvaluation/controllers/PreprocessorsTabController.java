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

import java.util.ResourceBundle;

import unbbayes.datamining.evaluation.batchEvaluation.BatchEvaluation;
import unbbayes.datamining.evaluation.batchEvaluation.model.Preprocessors;
import unbbayes.datamining.gui.evaluation.batchEvaluation.BatchEvaluationMain;
import unbbayes.datamining.gui.evaluation.batchEvaluation.PreprocessorsParameters;
import unbbayes.datamining.gui.evaluation.batchEvaluation.PreprocessorsTab;

/**
 * 
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 02/08/2007
 */
public class PreprocessorsTabController {

	private static final long serialVersionUID = 1L;
	private ResourceBundle resource;
	private BatchEvaluationMain mainView;
	private PreprocessorsTab view;
	private BatchEvaluation model;
	private Preprocessors data;

	public PreprocessorsTabController(PreprocessorsTab view, BatchEvaluation model) {
		this.view = view;
		this.model = model;
		mainView = view.getMainView();
		resource = mainView.getResourceBundle();
		data = model.getPreprocessorData();
	}

	public void insertData(final int row) {
		Object[] dataTableEntry = new Object[] {
				new Boolean(true),
				data.getPreprocessorNames()[0],
		};
		String[] preprocessorNames = data.getPreprocessorNames();
		view.addRow(dataTableEntry, preprocessorNames);
	}

	public void preprocessorConfig(int row) {
		PreprocessorsParameters preprocessorsConfig;
		preprocessorsConfig = new PreprocessorsParameters(this, mainView);
//		ArrayList<Integer> parameters;
		preprocessorsConfig.run(data.getPreprocessor(row));
//		if (parameters != null) {
//			data.setPreprocessor(row, parameters);
//		}
	}

	public void updateData() {
		data = model.getPreprocessorData();
		view.updateData();
	}
	
	public ResourceBundle getResourceBundle() {
		return resource;
	}

	public Preprocessors getData() {
		return data;
	}

	public PreprocessorsTabController getController() {
		return this;
	}

}
