package unbbayes.datamining.gui.preprocessor.resources;

import java.util.*;

public class PreprocessorResource_pt extends ListResourceBundle {

	public Object[][] getContents() {
		return contents;
	}
	static final Object[][] contents = {
	// PreprocessorMain
        {"selectProgram","Selecione Programa"},
        {"help","Ajuda"},
	{"file","Arquivo"},
        {"helpTopics","Tópicos de Ajuda"},
        {"openFile","Abrir arquivo"},
        {"status","Status"},
        {"welcome","Bem vindo"},
        {"open","Abrir ..."},
        {"exit","Sair"},
        {"preprocess","Pré processamento"},
	{"errorException","Erro= "},
	{"fileOpened","Arquivo aberto com sucesso"},
	{"preprocessorTitle","Preprocessor - "},
	{"errorDB","Erro na base de dados: "},
	{"fileNotFound","Arquivo não encontrado: "},
	{"errorOpen","Erro ao abrir arquivo: "},
	{"error","Erro"},
	{"fileMnemonic",new Character('A')},
        {"helpMnemonic",new Character('U')},
        {"helpTopicsMnemonic",new Character('T')},
        {"openMnemonic",new Character('A')},
        {"fileExitMnemonic",new Character('R')},
        // PreprocessPanel
        {"continuousAttributes","Atributos contínuos"},
	{"attributeInfo","Informação sobre os atributos"},
	{"fileAttributes","Atributos no arquivo"},
	{"file","Arquivo"},
	{"attributes","Atributos: "},
	{"instances","Instancias: "},
	{"none","Nenhum"},
	{"relation","Relação: "},
	{"name","Nome: "},
	{"distinct","Distintos: "},
	{"missing","Valores faltantes: "},
	{"type","Tipo: "},
	{"discretizeAttribute","Discretizar atributo ..."},
	{"instancesEditor","Editor de instancias para atributos selecionados"},
	{"selectedAttributes","Só os atributos selecionados serão utilizados pelo editor de instâncias"},
	{"label","Valor"},
	{"count","Contagem"},
	{"statistic","Estatística"},
	{"value","Valor"},
	{"minimum","Mínimo"},
	{"maximum","Máximo"},
	{"mean","Média"},
	{"stdDev","Desvio Padrão"},
	{"nominal","Nominal"},
	{"numeric","Numérico"},
	{"noAttributeSelected","Nenhum atributo selecionado"},
        // AttributeSelectionPanel
        {"name2","Nome"},
	{"selectedAttributes","Só os atributos selecionados serão utilizados pelo editor de instâncias"},
        };
}