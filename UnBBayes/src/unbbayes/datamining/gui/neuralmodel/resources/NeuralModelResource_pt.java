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
    {"helpFileTooltip","T�picos de ajuda"},
    {"welcome","Bem vindo"},
    {"openModelToolTip","Abrir modelo"},
    {"attributes","Atributos"},
    {"rules","Regras"},
    {"classify","Classificar"},
    {"errorDB","Erro na base de dados"},
    {"fileNotFound","Arquivo n�o encontrado"},
    {"errorOpen","Erro ao abrir o arquivo"},
    {"error","Error"},
    {"openFile","Arquivo aberto com sucesso"},
    {"openFile2","Abrir arquivo"},
    {"numericAttributesException","Este programa n�o manipula atributos num�ricos - Discretiza��o necess�ria"},
    {"saveModel","Modelo salvo com sucesso"},
    {"saveModel2","Salvar modelo"},
    {"errorWritingFileException","Erro na escrita do arquivo "},
    {"openModel2","Abrir modelo"},
    {"modelOpenedSuccessfully","Modelo aberto com sucesso"},
    {"exception","Exce��o"},
    {"model","Modelo"},
    {"printException","Erro de impress�o: "},


    //RulesPanel & OptionsPanel
    {"minimumSupport","Suporte m�nimo:"},
    {"minimumConfidence","Confian�a m�nima:"},
    {"maximumOrder","Ordem M�xima:"},
    {"if","SE"},
    {"then","ENT�O"},
    {"and","E"},
    {"index","�ndice"},
    {"confidence","Confian�a"},
    {"support","Suporte"},
    {"cases","Casos"},
    {"printTableToolTip","Imprimir tabela"},
    {"previewTableToolTip","Visualizar impress�o da tabela"},

    //InferencePanel
    {"expandToolTip","Expandir �rvore"},
    {"collapseToolTip","Contrair �rvore"},
    {"inference","Infer�ncia"},
    {"class","Classe"},
    {"rule","Regra"}
  };
}