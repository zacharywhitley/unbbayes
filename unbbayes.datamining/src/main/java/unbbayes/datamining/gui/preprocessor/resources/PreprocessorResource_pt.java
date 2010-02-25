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
package unbbayes.datamining.gui.preprocessor.resources;

import java.util.ListResourceBundle;

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
	{"fileTestTrainingCreated","Arquivos de treinamento e avaliação criados com sucesso"},
	{"sampleCreated","Amostra criada com sucesso"},
	{"preprocessorTitle","InitializePreprocessors - "},
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