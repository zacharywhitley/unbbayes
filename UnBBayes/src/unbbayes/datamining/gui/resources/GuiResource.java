package unbbayes.datamining.gui.resources;

import java.util.*;

public class GuiResource extends ListResourceBundle {

	public Object[][] getContents() {
		return contents;
	}
	static final Object[][] contents = {
	// Invoker Main
	{"id3Classifier","ID3 Classifier"},
	{"naiveBayesClassifier","Naive Bayes Classifier"},
	{"evaluation","Evaluation"},
	{"language","Language"},
	{"lookAndFeel","Look and Feel"},
	{"window","Window"},
	{"help","Help"},
	{"helpTopics","Help Topics"},
	{"cascade","Cascade"},
	{"tile","Tile"},
	{"english","English"},
	{"portuguese","Portuguese"},
	{"globalOptions","Global Options"},
	{"preferences","Preferences..."},
	{"id3Mnemonic",new Character('I')},
	{"naiveBayesMnemonic",new Character('N')},
	{"evaluationMnemonic",new Character('E')},
	{"languageMnemonic",new Character('L')},
	{"lafMnemonic",new Character('F')},
	{"windowMnemonic",new Character('W')},
	{"helpMnemonic",new Character('H')},
	{"helpTopicsMnemonic",new Character('T')},
	{"preferencesMnemonic",new Character('P')},
	{"globalOptionsMnemonic",new Character('G')},
	{"portugueseMnemonic",new Character('P')},
	{"englishMnemonic",new Character('E')},
	{"selectMnemonic",new Character('S')},
	{"preprocessorMnemonic",new Character('P')},
	{"cascadeMnemonic",new Character('C')},
	{"tileMnemonic",new Character('T')},
	{"selectProgram","Select Program"},
	{"instancesPreprocessor","Instances Preprocessor"},
	{"unsupportedLookAndFeelException","LookAndFeel not supported: "},
	{"classNotFoundException","LookAndFeel class not found: "},
	{"instanciationException","Can't load LookAndFeel: "},
	{"illegalAccessException","LookAndFeel can't be used: "},
	// Global Options
	{"globalOptions","Global Options"},
	{"defaultLanguage","Default Language"},
	{"defaultLookAndFeel","Default Look and Feel"},
	{"maximumNumber","Maximum number of states allowed"},
	{"cancel","Cancel"},
	// Attribute Panel
	{"selectClass","Select Class ="},
	{"class","Class = "},
	{"attributes","Attributes = "},
	};
}