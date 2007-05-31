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
	{"helpTopics","Tópicos de ajuda"},
	{"status","Status"},
	{"welcome","Bem vindo"},
	{"exit","Sair"},
	{"openModel","Abrir um modelo"},
	{"openModelDialog","Abrir Modelo ..."},
	{"error2","Erro= "},
	{"errorDB","Erro na base de dados: "},
        {"error","Erro "},
	{"fileNotFound","Arquivo não encontrado: "},
	{"errorOpen","Erro ao abrir arquivo: "},
	{"modelOpened","Modelo aberto com sucesso"},
	{"model","Modelo "},
	{"fileExtensionNotKnown","Extens�o de arquivo não conhecida"},
        {"numericAttributesException","Este programa não manipula atributos numéricos - Discretização necessária"},
	{"fileMnemonic",new Character('A')},
	{"helpMnemonic",new Character('U')},
	{"helpTopicsMnemonic",new Character('T')},
	{"fileExitMnemonic",new Character('R')},
	{"openModelMnemonic",new Character('M')},
	};
}