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
	{"build","Construir Árvore de Decisão"},
	{"file","Arquivo"},
	{"help","Ajuda"},
	{"about","Sobre ..."},
	{"openTooltip","Abre um arquivo"},
	{"buildTooltip","Constrói uma árvore de decisão"},
	{"exit","Sair"},
	{"nullPointerException","Arquivo inválido: "},
	{"fileNotFoundException","Arquivo não encontrado: "},
	{"ioException1","Problema lendo "},
	{"ioException2"," como um arquivo arff."},
	{"result1","Processando arquivo:\t"},
	{"result2","Número de instâncias:\t"},
	{"result3","Número de atributos:\t"},
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
	{"helpTopics","Tópicos de ajuda"},
	{"numericAttributesException","Este programa não manipula atributos numéricos - Discretização necessária"},
	{"saveModel","Salvar Modelo ..."},
	{"learn","Aprendizagem"},
	{"openModel","Abrir Modelo ..."},
	{"callHelpFile","Chama arquivo de ajuda"},
	{"openAModel","Abre um modelo"},
	{"saveAModel","Salva um modelo"},
	{"inference","Inferência"},
        {"attributes","Atributos"},
	{"error1","Erro= "},
	{"openFile","Abrir arquivo"},
	{"fileExtensionNotKnown"," Extensão de arquivo não conhecida."},
	{"fileOpenedSuccessfully","Arquivo aberto com sucesso"},
	{"exception","Exceção "},
	{"id3Learn","Aprendizagem ID3 feita com sucesso"},
	{"welcome","Bem Vindo"},
	{"openModel2","Abrir Modelo"},
	{"modelOpenedSuccessfully","Modelo aberto com sucesso"},
	{"errorWritingFile","Erro na escrita do arquivo "},
	//Induction Panel
	{"messages","Mensagens"},
	{"selectedNode","Nó selecionado: "},
	{"leaf","\tFolha alcançada: "},
	{"back","Voltar"},
	{"backMnemonic",new Character('V')},
	{"return","\nUsuário retorna um nível na árvore\nÚltima opção foi "},
	};
}
