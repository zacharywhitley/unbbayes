package unbbayes.datamining.gui.ban.resources;

import java.util.*;

public class BanResource_pt extends ListResourceBundle {

	public Object[][] getContents() {
		return contents;
	}
	static final Object[][] contents = {
	// Naive Bayes Main
	{"numericOption","Op��es para atributos num�ricos:"},
	{"selectDB","Selecione a base de dados:"},
	{"selectClass","Selecione a classe:"},
	{"cancel","Cancelar"},
	{"help","Ajuda"},
	{"select","Selecionar ..."},
	{"method","M�todo de discretiza��o:"},
	{"num","N�mero de dados discretos:"},
	{"frequency","Frequ�ncia"},
	{"range","Alcance"},
	{"errorDB","Erro na base de dados: "},
	{"error","Error"},
	{"fileNotFound","Arquivo n�o encontrado: "},
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
	{"helpTopicsMenu","T�picos de ajuda"},
	{"learningMenu","Aprendizagem"},
	{"learnNaiveBayes","Construir BAN "},
	{"saveNetworkMenu","Salvar rede ..."},
	{"openFileTooltip","Abre um arquivo"},
	{"saveFileTooltip","Salva uma rede"},
	{"learnDataTooltip","Aprende os dados"},
	{"helpFileTooltip","Chama arquivo de ajuda"},
	{"welcome","Bem Vindo"},
	{"attributes2","Atributos"},
	{"inference","Infer�ncia"},
	{"fileExtensionException","Extens�o de arquivo n�o conhecida."},
	{"numericAttributesException","Este programa n�o manipula atributos num�ricos - Discretiza��o necess�ria"},
	{"openFile","Arquivo aberto com sucesso"},
	{"error2","Erro= "},
	{"learnSuccessful","Aprendizagem BAN com sucesso"},
	{"exception","Exce��o "},
	{"errorWritingFileException","Erro na escrita do arquivo "},
        {"saveModel","Modelo salvo com sucesso"},
	};
}