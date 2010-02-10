/**
 * 
 */
package unbbayes.learning.incrementalLearning.resources;

import java.util.ListResourceBundle;

/**
 * <p>Title: UnBBayes</p>
 * <p>Description: Resources file for unbbayes.learning.incrementalLearning package. Localization = portuguese.</p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: UnB</p>
 * @author Shou Matsumoto
 * @version 1.0
 * @since 10/02/2010
 */
public class Resources_pt extends ListResourceBundle {

	/** 
	 *  Override getContents and provide an array, where each item in the array is a pair
	 *	of objects. The first element of each pair is a String key,
	 *	and the second is the value associated with that key.
	 *
	 * @return The resources' contents
	 */
	public Object[][] getContents() {
		return contents;
	}
 
	/**
	 * The resources
	 */
	static final Object[][] contents =
	{	
		{"title","Aprendizagem Incremental"},
		{"selectFile","Selecione um arquivo para iniciar aprendizagem."},
		{"openFile","Abrir arquivo"},
		{"chooseNetworkFile","Selecione o arquivo de rede"},
		{"chooseFrontierSet","Selecione o conjunto de fronteira."},
		{"chooseTrainingSet","Selecione o conjunto de treinamento."},

	};

}
