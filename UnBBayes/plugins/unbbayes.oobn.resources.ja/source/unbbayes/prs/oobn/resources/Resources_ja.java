/**
 * 
 */
package unbbayes.prs.oobn.resources;

import java.util.ListResourceBundle;

/**
 * @author Shou Matsumoto
 *
 */
public class Resources_ja extends ListResourceBundle {

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
	{	{"OOBNExceptionMessage","基本OOBNエラー"},
		//{"compileToolTip","Compile OOBN using current class"},
		

		{"DuplicateOOBNClassExceptionMessage","同じOOBNクラスが二つ発見されました"},
		
		{"InputNodeHasNoParents", "入力ノードに親は付けられません"},
		{"InstanceOutputNodeHasNoParents", "インスタンスの出力ノードに親は付けられません"},
		{"InstanceInputNodeHasNoMultipleParents", "インスタンスの入力ノードには多数の親を付けられません"},
		{"NoNodeIsParentOf2InstanceInput","同じノードは多数のインスタンス入力ノードに繋げられません"},
		{"PleaseAddParentToInstanceInputNodes","入力ノードと繋げて下さい"},
		{"PleaseAddChildToInstanceOutputNodes", "出力ノードと繋げて下さい"},
		{"InstanceInputTypeCompatibilityFailed", "ノードタイプが不正です。ノードのステートの数と名前をチェックして下さい"},
		{"ClassCycleFound", "サイクルが発見されました: どれかのクラスが自らを包容しています"},
	};

}
