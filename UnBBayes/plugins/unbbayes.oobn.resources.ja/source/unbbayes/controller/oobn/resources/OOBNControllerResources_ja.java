package unbbayes.controller.oobn.resources;

import java.util.ListResourceBundle;

/**
 * <p>Title: UnBBayes</p>
 * <p>Description: Resources file for unbbayes.controller.oobn package. Localization = japanese.</p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: UnB</p>
 * @author Shou Matsumoto
 * @version 1.0
 * @since 16/11/2008
 */
public class OOBNControllerResources_ja extends ListResourceBundle {
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
	{	{"imageFileFilter","PNG (.png), JPEG (.jpg), GIF (.gif), BMP (.bmp)"},
		{"likelihoodName","尤度"},
		{"likelihoodException","零しか発見されませんでした"},
		{"statusEvidenceProbabilistic","全体的な可能性、および根拠: "},
		{"statusEvidenceException","アンダーフロー、或いは不適格な根拠が発見されました"},
		{"statusError","エラー"},
		{"printLogToolTip","コンパイルログを出力します"},
		{"previewLogToolTip","印刷プレビュー"},
		{"okButtonLabel","了承"},
		{"closeButtonLabel","閉じる"},
		{"statusTotalTime","総合時間: "},
		{"statusSeconds"," 秒"},
		{"stateProbabilisticName","状態 "},
		{"stateDecisionName","アクション "},
		{"stateUtilityName","ユーティリティー "},
		{"firstStateProbabilisticName","状態 0"},
		{"firstStateDecisionName","アクション 0"},
		{"nodeName","ノード: "},
		
		//status
		{"statusLoadingKB","知識データベースの読み込み中..."},
		{"statusSavingKB","知識データベースの書き込み中..."},
		{"statusGeneratingSSBN","SSBN作成中..."},
		{"statusReady","準備完了"},
		{"statusEdittingClass","次のクラスを編集中: "},
		
		
		//MainController
		{"NewPNName","新規 BN"},
		{"NewMSBNName","新規 MSBN"},
		{"NewOOBNName","新規 OOBN"},
		
		{"probabilisticNodeName","C"},
		{"decisionNodeName","D"},
		{"utilityNodeName","U"},
		{"contextNodeName","CX"},
		{"residentNodeName","RX"},
		{"inputNodeName","IX"},
		{"ordinaryVariableName", "OX"}, 
		{"entityName", "EX"}, 			
		
		{"domainMFragName","DMFrag"},	
		{"findingMFragName","FMFrag"},				
		
		{"copiedNodeName","複製"},
		{"askTitle","ネットのタイトルを入力してください"},
		{"informationText","情報"},
		{"printException","印刷エラー: "},
		{"loadNetException","ネットファイルの読み込みに失敗しました"},
		{"cancelOption","キャンセル"},
		{"printerStatus","プリンターの状況"},
		{"initializingPrinter","プリンター準備中..."},
		{"printingPage","プリント中 "},
		{"previewButtonLabel","プレビュー"},
		{"nextButtonLabel","次"},
		{"fitToPageButtonLabel","自動調節"},
		{"loading","読み込み中 "},
		{"cancel","キャンセル"},
		{"of"," ／ "},
		{"numberFormatError","実数値をお願いします."},
		

		{"JAXBExceptionFound", "不正な文法です..."},
		
		/* Numeric attribute node */
		{"mean", "平均値"},
		{"stdDev", "標準偏差"}, 
			
		/* Java helper */
		{"helperDialogTitle", "ヘルプ"},
		
		//Network Controller
		{"logDialogTitle", "ログ"}, 
		
		//Result Dialog
		{"ResultDialog", "結果"}, 
		
		/* load/save */
		{"saveSucess", "ファイルがセーブされました"},
		{"bnDontExists", "アクティブなベイジアンネットワークが存在しません"},
		{"msbnDontExists", "アクティブなMSBNが存在しません"},
		{"sucess", "成功"}, 
		{"error", "エラー"},
		{"loadHasError", "ファイルの読み込みは完了しましたが、エラーが発生しました"},
		{"withoutPosfixe", "ファイルタイプが入力されてません"},

		/* Likelihood Weighting Inference */
		{"sampleSizeInputMessage", "サンプルサイズの入力をお願いします (試験実行回数)"},
		{"sampleSizeInputTitle", "サンプルサイズ"}, 
		{"sampleSizeInputError", "サンプルサイズには零より上の整数を入力して下さい"},
		{"likelihoodWeightingNotApplicableError", "尤度操作はベイジアンネットワークでしか使用出来ませんので、別のアルゴリズムを選択して下さい"},
		
		
		// OOBN controller's error messages
		
		{"OOBNClassCycle", "自らを含むOOBNクラスは使用出来ません"},
		
	};
}
