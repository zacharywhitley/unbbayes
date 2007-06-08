package unbbayes.datamining.gui.naivebayes.resources;

import java.util.*;

public class NaiveBayesResource_pt extends ListResourceBundle {

	public Object[][] getContents() {
		return contents;
	}
	static final Object[][] contents = {
	// Naive Bayes Main
	{"numericOption","Opções para atributos numéricos:"},
	{"selectDB","Selecione a base de dados:"},
	{"selectClass","Selecione a classe:"},
	{"cancel","Cancelar"},
	{"help","Ajuda"},
	{"select","Selecionar ..."},
	{"method","Método de discretização:"},
	{"num","Número de dados discretos:"},
	{"frequency","Freqüência"},
	{"range","Alcance"},
	{"errorDB","Erro na base de dados: "},
	{"error","Error"},
	{"fileNotFound","Arquivo não encontrado: "},
	{"errorOpen","Erro ao abrir o arquivo: "},
	{"selectMnemonic",new Character('S')},
	{"cancelMnemonic",new Character('C')},
	{"helpMnemonic",new Character('U')},
	{"helpTopicsMnemonic",new Character('T')},
	{"fileMnemonic",new Character('A')},
	{"openMnemonic",new Character('A')},
	{"exitMnemonic",new Character('R')},
	{"learningMnemonic",new Character('P')},
	{"learnNaiveBayesMnemonic",new Character('C')},
	{"saveNetworkMnemonic",new Character('S')},
	{"fileMenu","Arquivo"},
	{"openMenu","Abrir ..."},
	{"exit","Sair"},
	{"helpTopicsMenu","Tópicos de ajuda"},
	{"learningMenu","Aprendizagem"},
	{"learnNaiveBayes","Construir Naive Bayes "},
	{"saveNetworkMenu","Salvar rede ..."},
	{"openFileTooltip","Abre um arquivo"},
	{"saveFileTooltip","Salva uma rede"},
	{"learnDataTooltip","Aprende os dados"},
	{"helpFileTooltip","Chama arquivo de ajuda"},
	{"welcome","Bem Vindo"},
	{"attributes2","Atributos"},
	{"inference","Inferência"},
	{"fileExtensionException","Extensão de arquivo não conhecida."},
	{"numericAttributesException","Este programa não manipula atributos numéricos - Discretização necessária"},
	{"openFile","Arquivo aberto com sucesso"},
	{"error2","Erro= "},
	{"learnSuccessful","Aprendizagem Naive Bayes com sucesso"},
	{"exception","Exceção "},
	{"errorWritingFileException","Erro na escrita do arquivo "},
        {"saveModel","Modelo salvo com sucesso"},
	};
}