package unbbayes.datamining.gui.neuralmodel.resources;

import java.util.*;

public class NeuralModelResource extends ListResourceBundle{

  public Object[][] getContents() {
    return contents;
  }

  static final Object[][] contents = {
    // NeuralModelMain e NeuralModelController
    {"openFileToolTip","Open file"},
    {"saveModelToolTip","Save model"},
    {"learnDataTooltip","Learn data"},
    {"helpFileTooltip","Call help file"},
    {"welcome","Welcome"},
    {"openModelToolTip","Open model"},
    {"attributes","Attributes"},
    {"rules","Rules"},
    {"classify","Classify"},
    {"errorDB","Error in data base"},
    {"fileNotFound","File not found"},
    {"errorOpen","Error opening file"},
    {"error","Error"},
    {"openFile","File opened successfully"},
    {"openFile2","Open file"},
    {"numericAttributesException","Can't handle numeric attributes - Discretization needed"},
    {"saveModel","Model saved successfully"},
    {"saveModel2","Save model"},
    {"errorWritingFileException","Error writing file"},
    {"openModel2","Open model"},
    {"modelOpenedSuccessfully","Model opened successfully"},
    {"exception","Exception"},
    {"model","Model"},
    {"printException","Print error: "},
    {"tabbedPaneAttributes","Attributes"},
    {"tabbedPaneRules","Rules"},
    {"tabbedPanelClassify","Inference"},

    //Menu
    {"fileMenu","File"},
    {"openMenu","Open..."},
    {"openModelMenu","Open Model..."},
    {"saveModelMenu","Save Model..."},
    {"exitMenu","Exit"},
    {"learnMenu","Learning"},
    {"learnModelMenu","Learn Model"},
    {"helpMenu","Help"},
    {"helpTopicsMenu","Help Topics..."},

    //RulesPanel & OptionsPanel
    {"minimumSupport","Minimum support:"},
    {"minimumConfidence","Minimum confidence:"},
    {"maximumOrder","Maximum order:"},
    {"if","IF"},
    {"then","THEN"},
    {"and","AND"},
    {"index","Index"},
    {"confidence","Confidence"},
    {"support","Support"},
    {"cases","Cases"},
    {"printTableToolTip","Print table"},
    {"previewTableToolTip","Preview"},

    //InferencePanel
    {"expandToolTip","Expand tree"},
    {"collapseToolTip","Collapse tree"},
    {"inference","Inference"},
    {"class","Class"},
    {"rule","Rule"}
  };
}