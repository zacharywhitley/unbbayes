package unbbayes.datamining.gui.naivebayes.resources;

import java.util.*;

public class NaivebayesResource_pt extends ListResourceBundle { 
	
	public Object[][] getContents() { 
		return contents;
	}
	static final Object[][] contents = {
	{"numericOption","Opções para atributos numéricos:"},
	{"selectDB","Selecione a base de dados:"},
	{"selectClass","Selecione a classe:"},
	{"cancel","Cancelar"},
	{"help","Ajuda"},
	{"select","Selecionar ..."},
	{"method","Método de discretização:"},
	{"num","Número de dados discretos:"},
	{"frequency","Frequência"},
	{"range","Alcance"},
	{"errorDB","Erro na base de dados: "},
	{"error","Error"},
	{"fileNotFound","Arquivo não encontrado: "},
	{"errorOpen","Erro ao abrir o arquivo: "},
	{"selectMnemonic",new Character('S')},
	{"cancelMnemonic",new Character('C')},
	{"helpMnemonic",new Character('U')},
	}; 
}