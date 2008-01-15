package unbbayes.datamining.evaluation.batchEvaluation;

import java.util.*;

/** Resources file for datamanipulation package. Localization = portuguese.
 *
 *  @author M�rio Henrique Paes Vieira (mariohpv@bol.com.br)
 *  @version $1.0 $ (16/02/2002)
 */
public class BatchEvaluationResource_pt extends ListResourceBundle {

	/**
	 * Override getContents and provide an array, where each item in the array
	 * is a pair of objects. The first element of each pair is a String key,
	 * and the second is the value associated with that key.
	 */
	public Object[][] getContents() {
		return contents;
	}

	/** The resources */
	static final Object[][] contents = {
		
		/**********************************************************************
		 * Datasets
		 *********************************************************************/
		{"activeTableHeader", "Ativo"},
		{"finishedTableHeader", "Finalizado"},
		{"datasetNameTableHeader", "Base de dados"},
		{"classTableHeader", "Classe"},
		{"counterTableHeader", "Contador"},
		{"fileTableHeader", "Arquivo"},
		
		
		/**********************************************************************
		 * InitializePreprocessors
		 *********************************************************************/
		{"preprocessorNameTableHeader", "Preprocessador"},
		{"configButtonTableHeader", ""},
		
		
		/**********************************************************************
		 * Classifiers
		 *********************************************************************/
		{"classifierNameTableHeader", "Classificador"},
		
		
		/**********************************************************************
		 * Evaluations
		 *********************************************************************/
		{"evaluationNameTableHeader", "Avaliação"},
		
		
		/**********************************************************************
		 * RunScript
		 *********************************************************************/
		{"runScriptFinished", "finalizado!"},
	};
}