package unbbayes.datamining.gui.decisiontree.resources;

import java.util.*;
import javax.swing.KeyStroke;

public class DecisiontreeResource_pt extends ListResourceBundle { 
	
	public Object[][] getContents() { 
		return contents;
	}
	static final Object[][] contents = {
	{"class","Classe = "},
	{"attributes","Atributos"}, 
	{"open","Abrir ..."},
	{"build","Construir �rvore de Decis�o"},
	{"file","Arquivo"},
	{"help","Ajuda"},
	{"about","Sobre ..."},
	{"openTooltip","Abre um arquivo"},
	{"buildTooltip","Constr�i uma �rvore de decis�o"},
	{"exit","Sair"},
	{"nullPointerException","Arquivo inv�lido: "},
	{"fileNotFoundException","Arquivo n�o encontrado: "},
	{"ioException1","Problema lendo "},
	{"ioException2"," como um arquivo arff."},
	{"result1","Processando arquivo:\t"},
	{"result2","N�mero de inst�ncias:\t"},
	{"result3","N�mero de atributos:\t"},
	{"result4","Atributos:\t"},
	{"result5","Classe:\t\t"},
	{"fileMnemonic",new Character('A')},
	{"openMnemonic",new Character('A')},
	{"helpMnemonic",new Character('U')},
	{"aboutMnemonic",new Character('B')},
	{"exitMnemonic",new Character('R')},
	{"buildMnemonic",new Character('C')},
	{"openAccelerator", KeyStroke.getKeyStroke(65, java.awt.event.KeyEvent.CTRL_MASK, false)},
	{"buildAccelerator", KeyStroke.getKeyStroke(67, java.awt.event.KeyEvent.CTRL_MASK, false)},
	}; 
} 
