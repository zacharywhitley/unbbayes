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
		{"helpTopics","T�picos de Ajuda"},
		{"openFile","Abrir arquivo"},
		{"saveFile","Salvar arquivo"},
		{"saveTrainingFile","Salvar Arquivo de Treinamento"},
		{"saveTestFile","Salvar Arquivo de Avalia��o"},
		{"saveSample","Salvar Amostra"},
		{"status","Status"},
		{"welcome","Bem vindo"},
		{"open","Abrir ..."},
		{"save","Salvar ..."},
		{"fileTestTraining","Criar Arquivos de Treinamento e Avalia��o..."},
		{"sample","Criar Amostra..."},
		{"exit","Sair"},
		{"preprocess","Pr� processamento"},
	{"errorException","Erro= "},
	{"fileOpened","Arquivo aberto com sucesso"},
	{"fileTestTrainingCreated","Arquivos de treinamento e avalia��o criados com sucesso"},
	{"sampleCreated","Amostra criada com sucesso"},
	{"preprocessorTitle","Preprocessor - "},
	{"errorDB","Erro na base de dados: "},
	{"fileNotFound","Arquivo n�o encontrado: "},
	{"errorOpen","Erro ao abrir arquivo: "},
	{"error","Erro"},
	{"fileMnemonic",new Character('A')},
		{"helpMnemonic",new Character('U')},
		{"helpTopicsMnemonic",new Character('T')},
		{"openMnemonic",new Character('A')},
		{"saveMnemonic",new Character('S')},
		{"fileTestTrainingMnemonic",new Character('C')},
		{"sampleMnemonic",new Character('P')},
		{"fileExitMnemonic",new Character('R')},
		// PreprocessPanel
		{"continuousAttributes","Atributos cont�nuos"},
	{"attributeInfo","Informa��o sobre os atributos"},
	{"fileAttributes","Atributos no arquivo"},
	{"file","Arquivo"},
	{"attributes","Atributos: "},
	{"instances","Instancias: "},
	{"none","Nenhum"},
	{"relation","Rela��o: "},
	{"name","Nome: "},
	{"distinct","Distintos: "},
	{"missing","Valores faltantes: "},
	{"type","Tipo: "},
	{"discretizeAttribute","Discretizar atributo ..."},
	{"instancesEditor","Editor de instancias para atributos selecionados"},
	{"selectedAttributes","S� os atributos selecionados ser�o utilizados pelo editor de inst�ncias"},
	{"label","Valor"},
	{"count","Contagem"},
	{"statistic","Estat�stica"},
	{"value","Valor"},
	{"minimum","M�nimo"},
	{"maximum","M�ximo"},
	{"mean","M�dia"},
	{"stdDev","Desvio Padr�o"},
	{"nominal","Nominal"},
	{"numeric","Num�rico"},
	{"noAttributeSelected","Nenhum atributo selecionado"},
		// AttributeSelectionPanel
		{"name2","Nome"},
	{"selectedAttributes","S� os atributos selecionados ser�o utilizados pelo editor de inst�ncias"},
		};
}