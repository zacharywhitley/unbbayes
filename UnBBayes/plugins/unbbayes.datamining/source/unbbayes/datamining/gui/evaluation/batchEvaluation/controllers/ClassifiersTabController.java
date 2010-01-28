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
