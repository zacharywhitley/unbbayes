/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package unbbayes.datamining.gui.evaluation.batchEvaluation.resources;

import java.util.ListResourceBundle;

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