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
package unbbayes.datamining.gui.neuralnetwork.resources;

import java.util.ListResourceBundle;

public class NeuralNetworkResource_pt extends ListResourceBundle{

  public Object[][] getContents() {
    return contents;
  }

  static final Object[][] contents = {
    // NeuralNetworkMain e NeuralNetworkController
    {"openFile2","Abrir arquivo"},
    {"txtFiles","Arquivo txt"},
    {"arffFiles","Arquivo arff"},
    {"saveModel2","Salvar modelo"},
    {"openModel2","Abrir modelo"},
    {"model","Modelo"},

    {"openFileToolTip","Abrir arquivo de treinamento"},
    {"saveModelToolTip","Salvar modelo"},
    {"openModelToolTip","Abrir modelo"},
    {"helpFileTooltip","Tópicos de ajuda"},
    {"learnDataTooltip","Aprender dados"},
    {"advancedOptionsToolTip","Opções avançadas"},
    {"welcome","Bem vindo"},
    {"fileMenu","Arquivo"},
    {"openMenu","Abrir..."},
    {"openModelMenu","Abrir Modelo..."},
    {"saveModelMenu","Salvar Modelo..."},
    {"exitMenu","Sair"},
    {"optionsMenu","Opções"},
    {"learnMenu","Treinar modelo"},
    {"helpMenu","Ajuda"},
    {"helpTopicsMenu","Tópicos de ajuda"},
    {"advancedOptionsMenu","Opções avançadas..."},
    {"settingsPanel","Configurações"},
    {"chartPanel","Gráfico de treinamento"},
    {"inferencePanel","Inferência"},
    {"exception","Exceção"},
    {"openFileSuccess","Arquivo aberto com sucesso"},
    {"errorDB","Erro na base de dados"},
    {"fileNotFound","Arquivo não encontrado"},
    {"errorOpen","Erro ao abrir o arquivo"},
    {"error","Error"},
    {"saveModelSuccess","Modelo salvo com sucesso"},
    {"errorWritingFileException","Erro na escrita do arquivo "},
    {"modelOpenSuccess","Modelo aberto com sucesso"},
    {"advancedOptionsTitle","Opções avançadas"},

    //TrainingPanel
    {"fillToolTip","Expandir"},
    {"printToolTip","Imprimir"},
    {"gridToolTip","Adicionar ou remover grade"},
    {"resetButtonToolTip","Restaurar tamanho original"},
    {"chartTitle","Erro Quadrado Médio X Época"},
    {"YAxisTitle","Erro Quadrado Médio"},
    {"XAxisTitle","Épocas"},
    {"printingFailed","Falha na impressão "},

    //OptionsPanel & AdvancedOptionsPanel
    {"activationFunctionLabel","Função de Ativação"},
    {"momentumLabel","Momentum"},
    {"learningRateLabel","Taxa de Aprendizagem"},
    {"sigmoid","Sigmoide"},
    {"tanh","Tanh"},

    {"hiddenLayerSize","Tamanho da Camada Oculta"},
    {"activationFunctionSteep","Inclinação da Função de Ativação"},
    {"learningStopCondition","Critérios de Parada da Aprendizagem"},
    {"numericInput","Entradas Numéricas"},
    {"normalizationAlgorithm","Algoritmo de Normalização"},
    {"learningRateDecay","Decaimento da Taxa de Aprendizagem"},
    {"auto","Auto"},
    {"limitOfEpochs","Limite de épocas"},
    {"relativeError","Erro Relativo do Erro Quadrado Médio (%)"},
    {"normalizeNumericInput","Normalizar Entradas Numúricas"},
    {"linearNormalization","Normalização Linear"},
    {"mean0StandardDeviation1","Média 0 e Desvio Padrão 1"},

    //InferencePanel
    {"expandToolTip","Expandir árvore"},
    {"collapseToolTip","Contrair árvore"},
    {"inference","Inferência"},
    {"class","Classe"}
  };
}
