package unbbayes.datamining.gui.naivebayes.resources;

import java.util.*;

public class NaivebayesResource extends ListResourceBundle { 
	
	public Object[][] getContents() { 
		return contents;
	}
	static final Object[][] contents = {
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
	{"error","Error"},
	{"fileNotFound","File not found: "},
	{"errorOpen","Error opening file: "},
	{"selectMnemonic",new Character('B')},
	{"cancelMnemonic",new Character('C')},
	{"helpMnemonic",new Character('H')},
	}; 
}