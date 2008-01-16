package unbbayes.datamining.gui.naivebayes.resources;

import java.util.ListResourceBundle;

public class NaiveBayesResource extends ListResourceBundle {

	public Object[][] getContents() {
		return contents;
	}
	static final Object[][] contents = {
	// Naive Bayes Main
	{"numericOption","Numeric attributes options:"},
	{"selectDB","Select data base:"},
	{"selectClass","Select class:"},
	{"cancel","Cancel"},
	{"help","Help"},
	{"select","Browse ..."},
	{"method","Discretization method:"},
	{"num","Number of discretized states:"},
	{"frequency","Frequency"},
	{"range","Range"},
	{"errorDB","Error in data base: "},
	{"error","Error "},
	{"fileNotFound","File not found: "},
	{"errorOpen","Error opening file: "},
	{"selectMnemonic",new Character('B')},
	{"cancelMnemonic",new Character('C')},
	{"helpMnemonic",new Character('H')},
	{"helpTopicsMnemonic",new Character('E')},
	{"fileMnemonic",new Character('F')},
	{"openMnemonic",new Character('O')},
	{"exitMnemonic",new Character('X')},
	{"learningMnemonic",new Character('L')},
	{"learnNaiveBayesMnemonic",new Character('N')},
	{"saveNetworkMnemonic",new Character('S')},
	{"fileMenu","File"},
	{"openMenu","Open ..."},
	{"exit","Exit"},
	{"helpTopicsMenu","Help Topics"},
	{"learningMenu","Learning"},
	{"learnNaiveBayes","Learn Naive Bayes "},
	{"saveNetworkMenu","Save Network ..."},
	{"openFileTooltip","Open a file"},
	{"saveFileTooltip","Save a file"},
	{"learnDataTooltip","Learn data"},
	{"helpFileTooltip","Call help file"},
	{"welcome","Welcome"},
	{"attributes2","Attributes"},
	{"inference","Inference"},
	{"fileExtensionException","File extension not known."},
	{"numericAttributesException","Can't handle numeric attributes - Discretization needed"},
	{"openFile","File opened successfully"},
	{"error2","Error= "},
	{"learnSuccessful","Naive Bayes learning successful"},
	{"exception","Exception "},
	{"errorWritingFileException","Error writing file "},
        {"saveModel","Model saved successfully"},
	};
}