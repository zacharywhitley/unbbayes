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
package unbbayes.datamining.evaluation.batchEvaluation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ResourceBundle;

import unbbayes.datamining.evaluation.batchEvaluation.model.Classifiers;
import unbbayes.datamining.evaluation.batchEvaluation.model.Datasets;
import unbbayes.datamining.evaluation.batchEvaluation.model.Evaluations;
import unbbayes.datamining.evaluation.batchEvaluation.model.Logs;
import unbbayes.datamining.evaluation.batchEvaluation.model.Preprocessors;
import unbbayes.datamining.gui.evaluation.batchEvaluation.controllers.LogsTabController;

/**
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 11/08/2007
 */
public class BatchEvaluation {

	private transient ResourceBundle resource;
	private Datasets datasetData;
	private Preprocessors preprocessorData;
	private Classifiers classifierData;
	private Evaluations evaluationData;
	private Logs logData;
	
	public BatchEvaluation() {
		resource = ResourceBundle.getBundle(
				unbbayes.datamining.evaluation.batchEvaluation.resources.BatchEvaluationResource.class.getName());
		datasetData = new Datasets(resource);
		preprocessorData = new Preprocessors(resource);
		classifierData = new Classifiers(resource);
		evaluationData = new Evaluations(resource);
		logData = new Logs(resource);
	}
	
	public String openScript(File file) {
		FileInputStream scriptStream;
		ObjectInputStream scriptStreamIn;
		
		try {
			scriptStream = new FileInputStream(file.getAbsoluteFile());
			scriptStreamIn = new ObjectInputStream(scriptStream);
	
			/*  Open each object */
			datasetData = (Datasets) scriptStreamIn.readObject();
			preprocessorData = (Preprocessors) scriptStreamIn.readObject();
			classifierData = (Classifiers) scriptStreamIn.readObject();
			evaluationData = (Evaluations) scriptStreamIn.readObject();
			logData = (Logs) scriptStreamIn.readObject();
		} catch (FileNotFoundException e) {
			return e.getLocalizedMessage();
		} catch (IOException e) {
			return e.getLocalizedMessage();
		} catch (ClassNotFoundException e) {
			return e.getLocalizedMessage();
		}
		
		return null;
	}

	public String saveScript(File file) {
		FileOutputStream scriptStream;
		ObjectOutputStream scriptStreamOut;
		
		try {
			scriptStream = new FileOutputStream(file.getAbsoluteFile());
			scriptStreamOut = new ObjectOutputStream(scriptStream);
	
			/*  Save each object */
			scriptStreamOut.writeObject(datasetData);
			scriptStreamOut.writeObject(preprocessorData);
			scriptStreamOut.writeObject(classifierData);
			scriptStreamOut.writeObject(evaluationData);
			scriptStreamOut.writeObject(logData);
		} catch (FileNotFoundException e) {
			return e.getLocalizedMessage();
		} catch (IOException e) {
			return e.getLocalizedMessage();
		}
		
		return null;
	}

	public void runScript(LogsTabController logsWindowController)
	throws Exception {
		RunScript runScript;
		runScript = new RunScript(datasetData, preprocessorData, evaluationData,
				logsWindowController, resource);
		runScript.run();
	}

	public Datasets getDatasetData() {
		return datasetData;
	}

	public Preprocessors getPreprocessorData() {
		return preprocessorData;
	}

	public Classifiers getClassifierData() {
		return classifierData;
	}

	public Evaluations getEvaluationData() {
		return evaluationData;
	}

	public Logs getLogData() {
		return logData;
	}


}

