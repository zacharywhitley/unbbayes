package unbbayes.datamining.gui.naivebayes.resources;

import java.util.*;

public class NaivebayesResource_pt extends ListResourceBundle { 
	
	public Object[][] getContents() { 
		return contents;
	}
	static final Object[][] contents = {
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
	}; 
}