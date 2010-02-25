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
package unbbayes.datamining.gui.evaluation.batchEvaluation.resources;

import java.util.ListResourceBundle;

public class BatchEvaluationResource extends ListResourceBundle {

	public Object[][] getContents() {
		return contents;
	}

	static final Object[][] contents = {
		/*********************************************************************/
		/* BatchEvaluationMain */
		{"mainTitle", "Batch Evaluation"},
		{"selectProgram", "Select Program"},
		{"file", "File"},
		{"openScript", "Open script"},
		{"saveScript", "Save script"},
		{"runScript", "Run script"},
		{"help", "Help"},
		{"helpTopics", "Help Topics"},
		{"status", "Status"},
		{"welcome", "Welcome"},
		{"exit", "Exit"},
		
		{"fileMnemonic", new Character('F')},
		{"helpMnemonic", new Character('H')},
		{"helpTopicsMnemonic", new Character('T')},
		{"fileExitMnemonic", new Character('E')},
		{"openScriptMnemonic", new Character('O')},
		{"saveScriptMnemonic", new Character('S')},
		{"runScriptMnemonic", new Character('R')},

		/* Open and save script dialog window */
		{"openScriptDialog", "Open script"},
		{"saveScriptDialog", "Save script"},
		{"runScriptDialog", "Run script"},
		{"openScriptSuccessDialog", "Script opened successfuly"},
		{"saveScriptSuccessDialog", "Script saved successfuly"},
		{"canceledDialog", "Canceled by user"},
		{"scriptFilterText", "BatchEvaluationScriptFiles (*.bes)"},
		
		/* Error messages */
		{"runScriptError", "Error running script!"},
		
		/* Success messages */
		{"runScriptRunning", "Running script. Please wait!"},
		{"runScriptSuccess", "Script finalized successfuly!"},
		
		
		
		/*********************************************************************/
		/* Datasets tab */
		{"datasetsTabTitle", "Choose datasets"},
		{"newButtonText", "New"},
		{"deleteButtonText", "Delete"},
		{"editButtonText", "Edit"},
		{"detailsButtonText", "Details"},
		
		{"openDatasetDialog", "Add dataset file"},
		{"openDatasetSuccessDialog", "Datasets added successfuly"},

		
		
		/*********************************************************************/
		/* InitializePreprocessors tab */
		{"preprocessorsTabTitle", "Choose preprocessors"},
		{"configurationButtonText", "Config"},
		{"preprocessorsOptionsTitle", "Set preprocessor options"},
		
		
		
		/*********************************************************************/
		/* InitializePreprocessors Config */
		{"ratio", "Ratio"},
		{"cluster", "Cluster"},
		{"oversamplingThreshold", "Oversampling threshold"},
		{"positiveThreshold", "Positive threshold"},
		{"negativeThreshold", "Negative threshold"},
		{"cleanType", "Cleaning options"},
		{"cleanActivated", "Activated"},
		{"cleanDeactivated", "Deactivated"},
		{"cleanBoth", "Both"},
		{"start", "Start"},
		{"end", "End"},
		{"step", "Step"},
		
		
		
		/*********************************************************************/
		/* Classifiers tab */
		{"classifiersTabTitle", "Choose classifiers"},
		
		
		
		/*********************************************************************/
		/* Evaluations tab */
		{"evaluationsTabTitle", "Choose evaluations"},
		
		
		
		/*********************************************************************/
		/* Log tab */
		{"logTabTitle", "Log"},
		{"copyButtonText", "Copy"},
		{"clearButtonText", "Clear"},
	};
}