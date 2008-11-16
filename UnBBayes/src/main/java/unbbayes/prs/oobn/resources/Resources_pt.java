/**
 * 
 */
package unbbayes.prs.oobn.resources;

import java.util.ListResourceBundle;

/**
 * @author Shou Matsumoto
 *
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
	{	{"OOBNExceptionMessage","Exceção geral de OOBN"},
		//{"compileToolTip","Compila a OOBN usando a classe atual"},
		
		{"DuplicateOOBNClassExceptionMessage","Foi encontrado uma classe duplicada"},
		

		{"InputNodeHasNoParents", "Nós de Input não devem possuir pais"},
		{"InstanceOutputNodeHasNoParents", "Nós de Output em instâncias não devem possuir pais"},
		{"InstanceInputNodeHasNoMultipleParents", "Nós de Input em instâncias não devem possuir múltiplos pais"},
		{"NoNodeIsParentOf2InstanceInput","Um nó não deve ser pai de duas ou mais instâncias de Input"},
		{"PleaseAddParentToInstanceInputNodes","Insira um arco a um nó de Input"},
		{"PleaseAddChildToInstanceOutputNodes", "Insira um arco a um nó de Output"},
	};
}
