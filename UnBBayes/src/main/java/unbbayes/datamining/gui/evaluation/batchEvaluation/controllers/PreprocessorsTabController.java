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
