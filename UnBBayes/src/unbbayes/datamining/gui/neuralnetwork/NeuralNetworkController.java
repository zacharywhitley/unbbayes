package unbbayes.datamining.gui.neuralnetwork;

import java.io.*;
import java.awt.*;
import javax.swing.*;
import unbbayes.controller.*;
import unbbayes.datamining.classifiers.*;
import unbbayes.datamining.datamanipulation.*;
import unbbayes.gui.*;

public class NeuralNetworkController {

  private NeuralNetwork bpn = null;
//  private ResourceBundle resource;
  private NeuralNetworkMain mainScreen;
  private JFileChooser fileChooser;
  private InstanceSet instanceSet;
  private File file;
  private int hiddenLayerSize;

  public NeuralNetworkController() {
//    resource = ResourceBundle.getBundle("unbbayes.datamining.gui.neuralmodel.resources.NeuralModelResource");
    mainScreen = new NeuralNetworkMain();
    mainScreen.setController(this);
  }

  /**
   * Used to get the internal frame of the neural network.
   *
   * @return the internal frame.
   * @see {@link JInternalFrame}
   */
  public JInternalFrame getMainFrame(){
    return mainScreen;
  }

  public void help() throws Exception{
    FileController.getInstance().openHelp(mainScreen);
  }

  public void learn() throws Exception{
    float learningRate;
    boolean learningRateDecay;
    float momentum;
    int hiddenSize;
    int trainningTime;
    int activationFunction;
    float activationFunctionSteep;
    float minimumErrorVariation;

    mainScreen.setCursor(new Cursor(Cursor.WAIT_CURSOR));
    if(instanceSet != null){
      learningRate = mainScreen.optionsPanel.getLearningRate();
      learningRateDecay = mainScreen.advancedOptionsPanel.getLearningRateDecayEnabled();
      momentum = mainScreen.optionsPanel.getMomentum();
      hiddenSize = mainScreen.advancedOptionsPanel.getHiddenLayerSize();
      activationFunction = mainScreen.optionsPanel.getSelectedActivationFunction();
      trainningTime = mainScreen.advancedOptionsPanel.getTrainningTime();
      activationFunctionSteep = (float)mainScreen.advancedOptionsPanel.getActivationFunctionSteep();
      minimumErrorVariation = (float)mainScreen.advancedOptionsPanel.getMinimumErrorVariation();

      bpn = new NeuralNetwork(learningRate, learningRateDecay, momentum, hiddenSize, activationFunction, trainningTime, activationFunctionSteep, minimumErrorVariation);
      bpn.setQuadraticErrorOutput(mainScreen.chartPanel);
      bpn.buildClassifier(instanceSet);
      mainScreen.inferencePanel.setNetwork(bpn);
    }
    mainScreen.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  }

  public boolean openFile() throws Exception{
    mainScreen.setCursor(new Cursor(Cursor.WAIT_CURSOR));
    String[] arff = {"ARFF"};
    String[] txt = {"TXT"};
    boolean fileOpenSuccess = false;
    fileChooser = new JFileChooser(FileController.getInstance().getCurrentDirectory());
    fileChooser.setDialogTitle(/*resource.getString*/("openFile2"));
    fileChooser.setMultiSelectionEnabled(false);
    fileChooser.setFileView(new FileIcon(mainScreen));
    fileChooser.addChoosableFileFilter(new SimpleFileFilter(txt, "TxtFiles (*.txt)"));
    fileChooser.addChoosableFileFilter(new SimpleFileFilter(arff, "ArffFiles (*.arff)"));
    int returnValue = fileChooser.showOpenDialog(mainScreen);
    if (returnValue == JFileChooser.APPROVE_OPTION) {
      File selectedFile = fileChooser.getSelectedFile();
      openSelectedFile(selectedFile);
      file = selectedFile;
      FileController.getInstance().setCurrentDirectory(fileChooser.getCurrentDirectory());
      fileOpenSuccess = true;
    }
    mainScreen.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    return fileOpenSuccess;
  }

  private void openSelectedFile(File selectedFile) throws Exception{
    instanceSet = FileController.getInstance().getInstanceSet(selectedFile, mainScreen);

    boolean numericAttributes = instanceSet.checkNumericAttributes();
    if (numericAttributes == true){
      throw new Exception(/*resource.getString*/("numericAttributesException"));
    }
    mainScreen.setTitle("Backpropagation Neural Network - " + selectedFile.getName());
    mainScreen.attributePanel.setInstances(instanceSet);
    mainScreen.attributePanel.enableComboBox(true);
    hiddenLayerSize = (instanceSet.numAttributes() + instanceSet.numClasses()) / 2;
    if(hiddenLayerSize < 3){
      hiddenLayerSize = 3;
    }
  }

  public int getHiddenLayerSize(){
    return hiddenLayerSize;
  }

  public boolean saveModel() throws Exception{
    mainScreen.setCursor(new Cursor(Cursor.WAIT_CURSOR));
    String[] bpnString = {"bpn"};   //backpropagation neural network
    boolean success = false;
    fileChooser = new JFileChooser(FileController.getInstance().getCurrentDirectory());
//    fileChooser.setDialogTitle(resource.getString("saveModel2"));
    fileChooser.setMultiSelectionEnabled(false);
    fileChooser.setFileView(new FileIcon(mainScreen));
    fileChooser.addChoosableFileFilter(new SimpleFileFilter(bpnString, "Neural Network (*.bpn)"));
    int returnVal = fileChooser.showSaveDialog(mainScreen);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      File selectedFile = fileChooser.getSelectedFile();
      String fileName = selectedFile.getName();
      if (!fileName.regionMatches(true, fileName.length() - 4, ".bpn", 0, 4)) {
        selectedFile = new File(selectedFile.getAbsolutePath() + ".bpn");
      }
      ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(selectedFile));
      out.writeObject(bpn);
      FileController.getInstance().setCurrentDirectory(fileChooser.getCurrentDirectory());
      success = true;
    }
    mainScreen.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    return success;
  }

  public boolean openModel() throws Exception{
    mainScreen.setCursor(new Cursor(Cursor.WAIT_CURSOR));
    String[] neuralNetworkString = {"bpn"};
    boolean success = false;
    fileChooser = new JFileChooser(FileController.getInstance().getCurrentDirectory());
//    fileChooser.setDialogTitle(resource.getString("openModel2"));
    fileChooser.setMultiSelectionEnabled(false);
    fileChooser.setFileView(new FileIcon(mainScreen));
    fileChooser.addChoosableFileFilter(new SimpleFileFilter(neuralNetworkString, "Neural Network (*.bpn)"));
    int returnVal = fileChooser.showOpenDialog(mainScreen);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      File selectedFile = fileChooser.getSelectedFile();
      ObjectInputStream in = new ObjectInputStream(new FileInputStream(selectedFile));
      bpn = null;
      bpn = (NeuralNetwork)in.readObject();
//      mainScreen.rulesPanel = new RulesPanel(this);
//      mainScreen.rulesPanel.setRulesPanel(cnm, cnm.getConfidence(), cnm.getSupport());
//      mainScreen.tabbedPaneRules.add(mainScreen.rulesPanel, BorderLayout.CENTER);
      mainScreen.inferencePanel.setNetwork(bpn);
      mainScreen.setTitle("Backpropagation Neural Network - " + /*resource.getString*/("model") + " " + selectedFile.getName());
      FileController.getInstance().setCurrentDirectory(fileChooser.getCurrentDirectory());
      file = selectedFile;
      success = true;
    }
    mainScreen.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    return success;
  }
}