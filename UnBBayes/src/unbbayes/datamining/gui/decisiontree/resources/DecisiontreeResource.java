package unbbayes.datamining.gui.decisiontree.resources;

import java.util.*;
import javax.swing.KeyStroke;

public class DecisiontreeResource extends ListResourceBundle { 
	
	public Object[][] getContents() { 
		return contents;
	}
	static final Object[][] contents = {
	// Attribute Panel
	{"class","Class = "},
	{"attributes","Attributes"},  
	// Decision Tree Main
	{"open","Open ..."},
	{"build","Learn Decision Tree"},
	{"file","File"},
	{"help","Help"},
	{"about","About ..."},
	{"openTooltip","Open a file"},
	{"buildTooltip","Learn a decision tree"},
	{"exit","Exit"},
	{"nullPointerException","File invalid: "},
	{"fileNotFoundException","File not found: "},
	{"ioException1","Problem opening "},
	{"ioException2"," as a arff file."},
	{"result1","Processing file:\t"},
	{"result2","Instances number:\t"},
	{"result3","Attributes number:\t"},
	{"result4","Attributes:\t"},
	{"result5","Class:\t\t"}, 
	{"fileMnemonic",new Character('F')},
	{"openMnemonic",new Character('O')},
	{"helpMnemonic",new Character('H')},
	{"aboutMnemonic",new Character('A')},
	{"exitMnemonic",new Character('X')},
	{"buildMnemonic",new Character('D')},
	{"helpTopicsMnemonic",new Character('E')},
	{"learnMnemonic",new Character('L')},
	{"openAccelerator", KeyStroke.getKeyStroke(79, java.awt.event.KeyEvent.CTRL_MASK, false)},
	{"buildAccelerator", KeyStroke.getKeyStroke(66, java.awt.event.KeyEvent.CTRL_MASK, false)},
	{"helpTopics","Help Topics"},
	{"numericAttributes","Can't manipulate numeric attributes - Discretization needed"},
	{"saveModel","Save Model ..."},
	{"learn","Learning"},
	{"openModel","Open Model ..."},
	{"callHelpFile","Call help file"},
	{"openAModel","Open a model"},
	{"saveAModel","Save a model"},
	{"inference","Inference"},
	{"error1","Error= "},
	{"openFile","Open File"},
	{"fileExtensionNotKnown"," File extension not known."},
	{"fileOpenedSuccessfully","File opened successfully"},
	{"exception","Exception "},
	{"id3Learn","ID3 learning successful"},
	{"welcome","Welcome"},
	{"openModel2","Open Model"},
	{"modelOpenedSuccessfully","Model opened successfully"},
	{"errorWritingFile","Error writing file "},
	//Induction Panel
	{"messages","Messages"},	
	{"selectedNode","Selected node: "},
	{"leaf","\tReached leaf: "},
	{"back","Back"},
	{"backMnemonic",new Character('B')},
	{"return","\nUser returns a level in the tree\nLast option was "},
	}; 
} 
