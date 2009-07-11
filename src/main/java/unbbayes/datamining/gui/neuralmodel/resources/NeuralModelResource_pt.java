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
package unbbayes.datamining.gui.neuralmodel.resources;

import java.util.ListResourceBundle;

public class NeuralModelResource_pt extends ListResourceBundle{

  public Object[][] getContents() {
    return contents;
  }
  static final Object[][] contents = {
    // NeuralModelMain e NeuralModelController
    {"openFileToolTip","Abrir arquivo"},
    {"saveModelToolTip","Salvar modelo"},
    {"learnDataTooltip","Aprende os dados"},
    {"helpFileTooltip","Tópicos de ajuda"},
    {"welcome","Bem vindo"},
    {"openModelToolTip","Abrir modelo"},
    {"attributes","Atributos"},
    {"rules","Regras"},
    {"classify","Classificar"},
    {"errorDB","Erro na base de dados"},
    {"fileNotFound","Arquivo não encontrado"},
    {"errorOpen","Erro ao abrir o arquivo"},
    {"error","Error"},
    {"openFile","Arquivo aberto com sucesso"},
    {"openFile2","Abrir arquivo"},
    {"numericAttributesException","Este programa não manipula atributos numéricos - Discretização necessária"},
    {"saveModel","Modelo salvo com sucesso"},
    {"saveModel2","Salvar modelo"},
    {"errorWritingFileException","Erro na escrita do arquivo "},
    {"openModel2","Abrir modelo"},
    {"modelOpenedSuccessfully","Modelo aberto com sucesso"},
    {"exception","Exceção"},
    {"model","Modelo"},
    {"printException","Erro de impressão: "},
    {"tabbedPaneAttributes","Atributos"},
    {"tabbedPaneRules","Regras"},
    {"tabbedPanelClassify","Inferência"},

    //Menu
    {"fileMenu","Arquivo"},
    {"openMenu","Abrir..."},
    {"openModelMenu","Abrir Modelo..."},
    {"saveModelMenu","Salvar Modelo..."},
    {"exitMenu","Sair"},
    {"learnMenu","Aprendizagem"},
    {"learnModelMenu","Construir Modelo"},
    {"helpMenu","Ajuda"},
    {"helpTopicsMenu","Tópicos de Ajuda..."},

    //RulesPanel & OptionsPanel
    {"minimumSupport","Suporte mínimo:"},
    {"minimumConfidence","Confiança mínima:"},
    {"maximumOrder","Ordem Máxima:"},
    {"if","SE"},
    {"then","ENTÃO"},
    {"and","E"},
    {"index","Índice"},
    {"confidence","Confiança"},
    {"support","Suporte"},
    {"cases","Casos"},
    {"printTableToolTip","Imprimir tabela"},
    {"previewTableToolTip","Visualizar impressão da tabela"},

    //InferencePanel
    {"expandToolTip","Expandir árvore"},
    {"collapseToolTip","Contrair árvore"},
    {"inference","Inferência"},
    {"class","Classe"},
    {"rule","Regra"}
  };
}