package unbbayes.datamining.gui.neuralnetwork.resources;

import java.util.*;

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
    {"helpFileTooltip","T�picos de ajuda"},
    {"learnDataTooltip","Aprender dados"},
    {"advancedOptionsToolTip","Op��es avan�adas"},
    {"welcome","Bem vindo"},
    {"fileMenu","Arquivo"},
    {"openMenu","Abrir..."},
    {"openModelMenu","Abrir Modelo..."},
    {"saveModelMenu","Salvar Modelo..."},
    {"exitMenu","Sair"},
    {"optionsMenu","Op��es"},
    {"learnMenu","Treinar modelo"},
    {"helpMenu","Ajuda"},
    {"helpTopicsMenu","T�picos de ajuda"},
    {"advancedOptionsMenu","Op��es avan�adas..."},
    {"settingsPanel","Configura��es"},
    {"chartPanel","Gr�fico de treinamento"},
    {"inferencePanel","Infer�ncia"},
    {"exception","Exce��o"},
    {"openFileSuccess","Arquivo aberto com sucesso"},
    {"errorDB","Erro na base de dados"},
    {"fileNotFound","Arquivo n�o encontrado"},
    {"errorOpen","Erro ao abrir o arquivo"},
    {"error","Error"},
    {"saveModelSuccess","Modelo salvo com sucesso"},
    {"errorWritingFileException","Erro na escrita do arquivo "},
    {"modelOpenSuccess","Modelo aberto com sucesso"},
    {"advancedOptionsTitle","Op��es avan�adas"},

    //TrainingPanel
    {"fillToolTip","Expandir"},
    {"printToolTip","Imprimir"},
    {"gridToolTip","Adicionar ou remover grade"},
    {"resetButtonToolTip","Restaurar tamanho original"},
    {"chartTitle","Erro Quadrado M�dio X �poca"},
    {"YAxisTitle","Erro Quadrado M�dio"},
    {"XAxisTitle","�pocas"},
    {"printingFailed","Falha na impress�o "},

    //OptionsPanel & AdvancedOptionsPanel
    {"activationFunctionLabel","Fun��o de Ativa��o"},
    {"momentumLabel","Momentum"},
    {"learningRateLabel","Taxa de Aprendizagem"},
    {"sigmoid","Sigmoide"},
    {"tanh","Tanh"},

    {"hiddenLayerSize","Tamanho da Camada Oculta"},
    {"activationFunctionSteep","Inclina��o da Fun��o de Ativa��o"},
    {"learningStopCondition","Crit�rios de Parada da Aprendizagem"},
    {"numericInput","Entradas Num�ricas"},
    {"normalizationAlgorithm","Algoritmo de Normaliza��o"},
    {"learningRateDecay","Decaimento da Taxa de Aprendizagem"},
    {"auto","Auto"},
    {"limitOfEpochs","Limite de �pocas"},
    {"relativeError","Erro Relativo do Erro Quadrado M�dio (%)"},
    {"normalizeNumericInput","Normalizar Entradas Num�ricas"},
    {"linearNormalization","Normaliza��o Linear"},
    {"mean0StandardDeviation1","M�dia 0 e Desvio Padr�o 1"},

    //InferencePanel
    {"expandToolTip","Expandir �rvore"},
    {"collapseToolTip","Contrair �rvore"},
    {"inference","Infer�ncia"},
    {"class","Classe"}
  };
}
