package unbbayes.datamining.gui.resources;

import java.util.*;

public class GuiResource_pt extends ListResourceBundle {

	public Object[][] getContents() {
		return contents;
	}
	static final Object[][] contents = {
	// Invoker Main
	{"id3Classifier","Classificador ID3"},
	{"naiveBayesClassifier","Classificador Naive Bayes"},
	{"evaluation","Avaliação"},
	{"language","Linguagem"},
	{"lookAndFeel","Look and Feel"},
	{"window","Janela"},
	{"help","Ajuda"},
	{"helpTopics","Tópicos de ajuda"},
	{"cascade","Cascade"},
	{"tile","Tile"},
	{"english","Inglês"},
	{"portuguese","Português"},
	{"globalOptions","Opções globais"},
	{"preferences","Preferências..."},
	{"id3Mnemonic",new Character('I')},
	{"naiveBayesMnemonic",new Character('N')},
	{"evaluationMnemonic",new Character('A')},
	{"languageMnemonic",new Character('L')},
	{"lafMnemonic",new Character('F')},
	{"windowMnemonic",new Character('J')},
	{"helpMnemonic",new Character('A')},
	{"helpTopicsMnemonic",new Character('T')},
	{"preferencesMnemonic",new Character('P')},
	{"globalOptionsMnemonic",new Character('G')},
	{"portugueseMnemonic",new Character('P')},
	{"englishMnemonic",new Character('I')},
	{"selectMnemonic",new Character('S')},
	{"preprocessorMnemonic",new Character('P')},
	{"cascadeMnemonic",new Character('C')},
	{"tileMnemonic",new Character('T')},
	{"selectProgram","Selecione Programa"},
	{"instancesPreprocessor","Preprocessor de Instâncias"},
	{"unsupportedLookAndFeelException","Não suporta esse LookAndFeel: "},
	{"classNotFoundException","A classe do LookAndFeel não foi encontrada: "},
	{"instanciationException","Não foi possível carregar esse LookAndFeel: "},
	{"illegalAccessException","Esse LookAndFeel não pode ser usado: "},
	// Global Options
	{"defaultLanguage","Linguagem Default"},
	{"defaultLookAndFeel","Look and Feel Default"},
	{"maximumNumber","Número Máximo de estados permitidos"},
	{"cancel","Cancelar"},
	// Attribute Panel
	{"selectClass","Selecione Classe ="},
	{"class","Classe = "},
	{"attributes","Attributos = "},
	};
}