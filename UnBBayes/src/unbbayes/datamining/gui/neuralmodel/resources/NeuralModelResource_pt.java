package unbbayes.datamining.gui.neuralmodel.resources;

import java.util.*;

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