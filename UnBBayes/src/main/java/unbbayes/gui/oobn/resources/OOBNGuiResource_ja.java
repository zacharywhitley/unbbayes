package unbbayes.gui.oobn.resources;

import java.util.ListResourceBundle;

/**
 * <p>Title: UnBBayes</p>
 * <p>Description: Resources file for unbbayes.gui.oobn package. Localization = japanese.</p>
 * <p>Copyleft: LGPL 2008</p>
 * <p>Company: UnB</p>
 * @author Shou Matsumoto
 * @version 1.0
 * @since 12/12/2009
 */
public class OOBNGuiResource_ja extends ListResourceBundle {
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
		{"OOBNPopupMenuMessage","ノードのタイプを変更します"},
		{"changeNodeToPrivate","プライベートノードに変更します"},
		{"changeNodeToOutput","出力（Output）ノードに変更します"},
		{"changeNodeToInput","入力（Input）ノードに変更します"},
		{"OOBNPopupMenuTooltipMessage","ノードタイプを選択してください"},
		{"openClassFromFile","ファイルからクラスを読み込みます"},
		

		{"ErrorLoadingClass","クラスを読み込む途中でエラーが起こりました"},
		
		{"editionToolTip","編集モードへ移行"},
		{"removeToolTip","プロジェクトからクラスを削除します"},
		{"newToolTip","プロジェクトに新規クラス作成"},
		{"newFromFileToolTip","ファイルからクラスを読み込みます"},
		{"status","ステータス:"},
		{"newOOBNClass","新規OOBNクラス"},
		{"renameClass", "クラス名変更"},
		{"oobnFileFilter","Net (.net), OOBN用Net (.oobn)"},
		{"NoClassSelected","OOBNクラスが選択されていません"},
		{"compilationError" , "コンピレーションエラー"},
		{"DuplicatedClassName","プロジェクトに同じ名のクラスが既に存在しています"},
		
		{"CannotDragNDrop", "ドラッグ出来ません"},
		{"dragNDropToAddInstance", "インスタンス作成には、ここからクラスをドラッグアンドドロップして下さい"},

		{"compileToolTip","選択中のクラスでOOBNをコンパイル"},
		{"statusReadyLabel", "準備完了"},
		

		{"classNavigationPanelLabel", "クラスリスト"},
		

		{"leftClickToChangeNodeType", "右クリックでノードタイプを変更出来ます"},
		

		{"changeNodeType", "選択中のノードタイプ変更"},
		
		{"saveTitle", "選択中のクラスを保存します"},
		
		{"unsupportedGraphFormat" , "このモジュール、およびプラグインは、この形式には対応していません"},
		
	};

}
