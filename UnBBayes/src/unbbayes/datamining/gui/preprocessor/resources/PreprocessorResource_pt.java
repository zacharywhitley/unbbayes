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
		{"saveFile","Salvar arquivo"},
		{"saveTrainingFile","Salvar Arquivo de Treinamento"},
		{"saveTestFile","Salvar Arquivo de Avaliação"},
		{"saveSample","Salvar Amostra"},
		{"status","Status"},
		{"welcome","Bem vindo"},
		{"open","Abrir ..."},
		{"save","Salvar ..."},
		{"fileTestTraining","Criar Arquivos de Treinamento e Avaliação..."},
		{"sample","Criar Amostra..."},
		{"exit","Sair"},
		{"preprocess","Pré processamento"},
	{"errorException","Erro= "},
	{"fileOpened","Arquivo aberto com sucesso"},
	{"fileTestTrainingCreated","Arquivos de treinamento e avalia��o criados com sucesso"},
	{"sampleCreated","Amostra criada com sucesso"},
	{"preprocessorTitle","Preprocessor - "},
	{"errorDB","Erro na base de dados: "},
	{"fileNotFound","Arquivo não encontrado: "},
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
	{"instancesEditor","Editor de instâncias para atributos selecionados"},
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