package unbbayes.datamining.gui.neuralnetwork.resources;

import java.util.ListResourceBundle;

public class NeuralNetworkResource extends ListResourceBundle{

  public Object[][] getContents() {
    return contents;
  }

  static final Object[][] contents = {
    // NeuralNetworkMain e NeuralNetworkController
    {"openFile2","Open file"},
    {"txtFiles","Txt Files"},
    {"arffFiles","Arff Files"},
    {"saveModel2","Save model"},
    {"openModel2","Open model"},
    {"model","Model"},

    {"openFileToolTip","Open file"},
    {"saveModelToolTip","Save model"},
    {"openModelToolTip","Open model"},
    {"helpFileTooltip","Call help topics"},
    {"learnDataTooltip","Learn data"},
    {"advancedOptionsToolTip","Advanced options"},
    {"welcome","Welcome"},
    {"fileMenu","File"},
    {"openMenu","Open..."},
    {"openModelMenu","Open Model..."},
    {"saveModelMenu","Save Model..."},
    {"exitMenu","Exit"},
    {"optionsMenu","Options"},
    {"learnMenu","Learn"},
    {"helpMenu","Help"},
    {"helpTopicsMenu","Help Topics"},
    {"advancedOptionsMenu","Advanced Options..."},
    {"settingsPanel","Settings"},
    {"chartPanel","Trainning Chart"},
    {"inferencePanel","Inference"},
    {"exception","Exception"},
    {"openFileSuccess","File opened successfully"},
    {"errorDB","Error in data base"},
    {"fileNotFound","File not found"},
    {"errorOpen","Error opening file"},
    {"error","Error"},
    {"saveModelSuccess","Model saved successfully"},
    {"errorWritingFileException","Error writing file"},
    {"modelOpenSuccess","Model opened successfully"},
    {"advancedOptionsTitle","Advanced Options"},

    //TrainingPanel
    {"fillToolTip","Fill"},
    {"printToolTip","Print"},
    {"gridToolTip","Add and remove the grid"},
    {"resetButtonToolTip","Reset to default size"},
    {"chartTitle","Mean Square Error X Epoch"},
    {"YAxisTitle","Mean Square Error"},
    {"XAxisTitle","Epochs"},
    {"printingFailed","Printing failed:"},

    //OptionsPanel & AdvancedOptionsPanel
    {"activationFunctionLabel","Activation Function"},
    {"momentumLabel","Momentum"},
    {"learningRateLabel","Learning Rate"},
    {"sigmoid","Sigmoid"},
    {"tanh","Tanh"},

    {"hiddenLayerSize","Hidden Layer Size"},
    {"activationFunctionSteep","Activation Function Steep"},
    {"learningStopCondition","Learning Stop Condition"},
    {"numericInput","Numeric Input"},
    {"normalizationAlgorithm","Normalization Algorithm"},
    {"learningRateDecay","Learning Rate Decay"},
    {"auto","Auto"},
    {"limitOfEpochs","Limit of Epochs"},
    {"relativeError","Relative Error of the Mean Square Error (%)"},
    {"normalizeNumericInput","Normalize Numeric Input"},
    {"linearNormalization","Linear Normalization"},
    {"mean0StandardDeviation1","Mean 0 and Standard Deviation 1"},

    //InferencePanel
    {"expandToolTip","Expand tree"},
    {"collapseToolTip","Collapse tree"},
    {"inference","Inference"},
    {"class","Class"}
  };
}
