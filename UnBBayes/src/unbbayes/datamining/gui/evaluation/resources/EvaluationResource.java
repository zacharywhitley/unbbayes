package unbbayes.datamining.gui.evaluation.resources;

import java.util.*;

public class EvaluationResource extends ListResourceBundle {

	public Object[][] getContents() {
		return contents;
	}

        static final Object[][] contents = {
	// EvaluationMain
	{"selectProgram","Select Program"},
	{"file","File"},
	{"help","Help"},
	{"helpTopics","Help Topics"},
	{"status","Status"},
	{"welcome","Welcome"},
	{"exit","Exit"},
	{"openModel","Open a model"},
	{"openModelDialog","Open Model ..."},
	{"error2","Error= "},
	{"errorDB","Error in data base: "},
        {"error","Error "},
	{"fileNotFound","File not found: "},
	{"errorOpen","Error opening file: "},
	{"modelOpened","Model opened successfully"},
	{"model","Model "},
	{"fileExtensionNotKnown","File extension not known"},
        {"numericAttributesException","Can't handle numeric attributes - Discretization needed"},
	{"fileMnemonic",new Character('F')},
	{"helpMnemonic",new Character('H')},
	{"helpTopicsMnemonic",new Character('E')},
	{"fileExitMnemonic",new Character('X')},
	{"openModelMnemonic",new Character('M')},
	};
}