package unbbayes.datamining.gui.decisiontree.resources;

import java.util.*;

import javax.swing.*;

public class DecisiontreeResource_pt extends ListResourceBundle {

	public Object[][] getContents() {
		return contents;
	}
	static final Object[][] contents = {
	// Decision Tree Main
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
	{"helpTopicsMnemonic",new Character('T')},
	{"learnMnemonic",new Character('P')},
	{"saveModelMnemonic",new Character('S')},
	{"openModelMnemonic",new Character('M')},
        {"openAccelerator", KeyStroke.getKeyStroke(65, java.awt.event.KeyEvent.CTRL_MASK, false)},
	{"buildAccelerator", KeyStroke.getKeyStroke(67, java.awt.event.KeyEvent.CTRL_MASK, false)},
	{"helpTopics","T�picos de ajuda"},
	{"numericAttributesException","Este programa n�o manipula atributos num�ricos - Discretiza��o necess�ria"},
	{"saveModel","Salvar Modelo ..."},
	{"learn","Aprendizagem"},
	{"openModel","Abrir Modelo ..."},
	{"callHelpFile","Chama arquivo de ajuda"},
	{"openAModel","Abre um modelo"},
	{"saveAModel","Salva um modelo"},
	{"inference","Infer�ncia"},
        {"attributes","Atributos"},
	{"error1","Erro= "},
	{"openFile","Abrir arquivo"},
	{"fileExtensionNotKnown"," Extens�o de arquivo n�o conhecida."},
	{"fileOpenedSuccessfully","Arquivo aberto com sucesso"},
	{"exception","Exce��o "},
	{"id3Learn","Aprendizagem ID3 feita com sucesso"},
	{"welcome","Bem Vindo"},
	{"openModel2","Abrir Modelo"},
	{"modelOpenedSuccessfully","Modelo aberto com sucesso"},
	{"errorWritingFile","Erro na escrita do arquivo "},
	//Induction Panel
	{"messages","Mensagens"},
	{"selectedNode","N� selecionado: "},
	{"leaf","\tFolha alcan�ada: "},
	{"back","Voltar"},
	{"backMnemonic",new Character('V')},
	{"return","\nUsu�rio retorna um n�vel na �rvore\n�ltima op��o foi "},
	};
}
