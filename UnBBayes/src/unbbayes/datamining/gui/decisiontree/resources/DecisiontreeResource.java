package unbbayes.datamining.gui.decisiontree.resources;

import java.util.*;
import javax.swing.KeyStroke;

public class DecisiontreeResource extends ListResourceBundle { 
	
	public Object[][] getContents() { 
		return contents;
	}
	static final Object[][] contents = {
	{"class","Class = "},
	{"attributes","Attributes"},  
	{"open","Open ..."},
	{"build","Build Decision Tree"},
	{"file","File"},
	{"help","Help"},
	{"about","About ..."},
	{"openTooltip","Open a file"},
	{"buildTooltip","Build a decision tree"},
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
	{"buildMnemonic",new Character('B')},
	{"openAccelerator", KeyStroke.getKeyStroke(79, java.awt.event.KeyEvent.CTRL_MASK, false)},
	{"buildAccelerator", KeyStroke.getKeyStroke(66, java.awt.event.KeyEvent.CTRL_MASK, false)},
	}; 
} 
