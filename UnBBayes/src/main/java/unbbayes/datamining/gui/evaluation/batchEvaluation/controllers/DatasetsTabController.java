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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ResourceBundle;

import unbbayes.datamining.datamanipulation.FileUtils;
import unbbayes.datamining.evaluation.batchEvaluation.BatchEvaluation;
import unbbayes.datamining.evaluation.batchEvaluation.model.Datasets;
import unbbayes.datamining.gui.evaluation.batchEvaluation.BatchEvaluationMain;
import unbbayes.datamining.gui.evaluation.batchEvaluation.DatasetsTab;

/**
 * 
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 02/08/2007
 */
public class DatasetsTabController {

	private static final long serialVersionUID = 1L;
	private ResourceBundle resource;
	private BatchEvaluationMain mainView;
	private DatasetsTab view;
	private BatchEvaluation model;
	private Datasets data;

	public DatasetsTabController(DatasetsTab view, BatchEvaluation model) {
		this.view = view;
		this.model = model;
		mainView = view.getMainView();
		resource = mainView.getResourceBundle();
		data = model.getDatasetData();
	}

	private File openData() {
		String[] returnStatus = new String[1];
		File file = FileUtils.openArffFile(mainView, returnStatus);
		mainView.setStatusBar(returnStatus[0]);

		return file;
	}

	public void addData() {
		/* Show open file dialog and check if user canceled the operation */
		File file = openData();
		if (file == null) {
			/* Operation canceled by the user. Just return */
			return;
		}

		/* Get file info */
		String filePath = file.getAbsolutePath();
		String fileName = file.getName();

		/*
		 * 0: ArrayList<String> - All data' names; 1: String - Likely
		 * counter attribute name; 2: String - Relation's name.
		 */
		ArrayList<Object> headerInfo = null;

		/* Get header info of the selected file */
		try {
			headerInfo = FileUtils.getFileHeaderInfo(file);
		} catch (IOException e) {
			e.printStackTrace();
		}

		/* Get a list of data' names */
		@SuppressWarnings("unchecked")
		ArrayList<String> aux = (ArrayList<String>) headerInfo.get(0);
		int numAttributes = aux.size();
		String[] attributesName = new String[numAttributes + 1];
		attributesName[0] = "";
		for (int att = 0; att < numAttributes; att++) {
			attributesName[att + 1] = aux.get(att);
		}
		numAttributes = attributesName.length;

		/* Get the likely counter name */
		String likelyCounterName = (String) headerInfo.get(1);

		/* Get the likely class name */
		String likelyClassName;
		if (likelyCounterName != null
				&& likelyCounterName.equals(attributesName[numAttributes - 1])) {
			likelyClassName = new String(attributesName[numAttributes - 2]);
		} else {
			likelyClassName = new String(attributesName[numAttributes - 1]);
		}

		/* Get relation's name of the selected file */
		String relationName = (String) headerInfo.get(2);
		if (relationName != null) {
			fileName = relationName;
		}
		Object[] dataTableEntry = new Object[] { new Boolean(true),
				new Boolean(false), fileName, likelyClassName,
				likelyCounterName, filePath };
		view.addData(dataTableEntry, attributesName);

		/* Store the data */
		ArrayList<String> newAttributes;
		newAttributes = new ArrayList<String>(numAttributes);
		for (int att = 0; att < numAttributes; att++) {
			newAttributes.add(attributesName[att]);
		}
		data.addAttributes(newAttributes);
	}

	public void updateData() {
		data = model.getDatasetData();
		view.updateData();
	}
	
	public void setFinished(int i, boolean finished) {
		data.setValueAt(finished, i, 1);
		updateData();
	}
	
	public ResourceBundle getResourceBundle() {
		return resource;
	}

	public Datasets getData() {
		return data;
	}

	public DatasetsTabController getController() {
		return this;
	}

}
