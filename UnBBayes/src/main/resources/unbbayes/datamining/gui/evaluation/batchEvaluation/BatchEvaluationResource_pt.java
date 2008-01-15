package unbbayes.datamining.gui.evaluation.batchEvaluation;

import java.util.*;

public class BatchEvaluationResource_pt extends ListResourceBundle {

	public Object[][] getContents() {
		return contents;
	}

	static final Object[][] contents = {
		/*********************************************************************/
		/* BatchEvaluationMain */
		{"mainTitle", "Avaliação em Lote"},
		{"selectProgram", "Selecione Programa"},
		{"file", "Arquivo"},
		{"openScript", "Abrir script"},
		{"saveScript", "Salvar script"},
		{"runScript", "Rodar script"},
		{"help", "Ajuda"},
		{"helpTopics", "Tópicos de ajuda"},
		{"status", "Status"},
		{"welcome", "Bem vindo"},
		{"exit", "Sair"},
		
		{"fileMnemonic", new Character('A')},
		{"helpMnemonic", new Character('J')},
		{"helpTopicsMnemonic", new Character('T')},
		{"fileExitMnemonic", new Character('R')},
		{"openScriptMnemonic", new Character('B')},
		{"saveScriptMnemonic", new Character('S')},
		{"runScriptMnemonic", new Character('R')},
		
		/* Open and save script dialog window */
		{"openScriptDialog", "Abrir script"},
		{"saveScriptDialog", "Salvar script"},
		{"runScriptDialog", "Rodar script"},
		{"openScriptSuccessDialog", "Script aberto com sucesso"},
		{"saveScriptSuccessDialog", "Script salvo com sucesso"},
		{"canceledDialog", "Cancelado pelo usuário"},
		{"scriptFilterText", "ArquivosScriptAvaliaçaoEmLote (*.bes)"},
		
		/* Error messages */
		{"runScriptError", "Erro ao rodar script!"},
		
		/* Success messages */
		{"runScriptRunning", "Rodando script. Aguarde por favor!"},
		{"runScriptSuccess", "Script finalizado com sucesso!"},
		
		

		/*********************************************************************/
		/* Datasets tab */
		{"datasetsTabTitle", "Escolha as bases de dados"},
		{"newButtonText", "Novo"},
		{"deleteButtonText", "Remover"},
		{"editButtonText", "Editar"},
		{"detailsButtonText", "Detalhes"},
		
		{"openDatasetDialog", "Inserir base de dados"},
		{"openDatasetSuccessDialog", "Base de dados inserida com sucesso"},


		
		/*********************************************************************/
		/* InitializePreprocessors tab */
		{"preprocessorsTabTitle", "Escolha os pré-processadores"},
		{"configurationButtonText", "Configurar"},
		{"preprocessorsOptionsTitle", "Opções do preprocessor"},
		
		
		
		/*********************************************************************/
		/* InitializePreprocessors Config */
		{"ratio", "Razão entre positivo/negativo"},
		{"cluster", "Cluster"},
		{"oversamplingThreshold", "Limiar para Oversampling"},
		{"positiveThreshold", "Limiar Positivo"},
		{"negativeThreshold", "Limiar Negativo"},
		{"cleanType", "Opções de Limpeza"},
		{"cleanActivated", "Ativado"},
		{"cleanDeactivated", "Desativado"},
		{"cleanBoth", "Ambos"},
		{"start", "Início"},
		{"end", "Fim"},
		{"step", "Passo"},
		
		
		
		/*********************************************************************/
		/* Classifiers tab */
		{"classifiersTabTitle", "Escolha os classificadores"},
		
		
		
		/*********************************************************************/
		/* Evaluations tab */
		{"evaluationsTabTitle", "Escolha as avaliações"},
		
		
		
		/*********************************************************************/
		/* Log tab */
		{"logTabTitle", "Log"},
		{"copyButtonText", "Copiar"},
		{"clearButtonText", "Limpar"},
	};
}