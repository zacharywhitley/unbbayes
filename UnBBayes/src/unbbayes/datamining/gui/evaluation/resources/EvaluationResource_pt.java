package unbbayes.datamining.gui.evaluation.resources;

import java.util.*;

public class EvaluationResource_pt extends ListResourceBundle {

	public Object[][] getContents() {
		return contents;
	}

        static final Object[][] contents = {
	// EvaluationMain
	{"selectProgram","Selecione Programa"},
	{"file","Arquivo"},
	{"help","Ajuda"},
	{"helpTopics","T�picos de ajuda"},
	{"status","Status"},
	{"welcome","Bem vindo"},
	{"exit","Sair"},
	{"openModel","Abrir um modelo"},
	{"openModelDialog","Abrir Modelo ..."},
	{"error2","Erro= "},
	{"errorDB","Erro na base de dados: "},
        {"error","Erro "},
	{"fileNotFound","Arquivo n�o encontrado: "},
	{"errorOpen","Erro ao abrir arquivo: "},
	{"modelOpened","Modelo aberto com sucesso"},
	{"model","Modelo "},
	{"fileExtensionNotKnown","Extens�o de arquivo n�o conhecida"},
        {"numericAttributesException","Este programa n�o manipula atributos num�ricos - Discretiza��o necess�ria"},
	{"fileMnemonic",new Character('A')},
	{"helpMnemonic",new Character('U')},
	{"helpTopicsMnemonic",new Character('T')},
	{"fileExitMnemonic",new Character('R')},
	{"openModelMnemonic",new Character('M')},
	};
}