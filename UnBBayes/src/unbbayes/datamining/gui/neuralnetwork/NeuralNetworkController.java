package unbbayes.datamining.gui.neuralnetwork;

import java.io.*;
import java.awt.*;
import javax.swing.*;
import java.util.*;
import unbbayes.controller.*;
import unbbayes.datamining.classifiers.*;
import unbbayes.datamining.datamanipulation.*;
import unbbayes.gui.*;

/**
 *  Class that implements the neural network framwork controller
 *
 *  @author Rafael Moraes Noivo
 *  @version $1.0 $ (02/16/2003)
 */
public class NeuralNetworkController {
  private ResourceBundle resource;
  private NeuralNetwork bpn = null;
  private NeuralNetworkMain mainScreen;
  private JFileChooser fileChooser;
  private InstanceSet instanceSet;
  private File file;
  private int hiddenLayerSize;

  public NeuralNetworkController() {
    resource = ResourceBundle.getBundle("unbbayes.datamining.gui.neuralnetwork.resources.NeuralNetworkResource");
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

  /**
   * Used to call the help.
   *
   * @throws Exception If the help files are not found
   */
  public void help() throws Exception{
    FileController.getInstance().openHelp(mainScreen);
  }

  /**
   * Used to start the learn process
   *
   * @throws Exception If any erros occur during training
   */
  public void learn() throws Exception{
    float learningRate;
    boolean learningRateDecay;
    float momentum;
    int hiddenSize;
    int trainningTime;
    int numerialInputNormalization;
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
      numerialInputNormalization = mainScreen.advancedOptionsPanel.getNumericalInputNormalization();
      activationFunctionSteep = (float)mainScreen.advancedOptionsPanel.getActivationFunctionSteep();
      minimumErrorVariation = (float)mainScreen.advancedOptionsPanel.getMinimumErrorVariation();

      bpn = new NeuralNetwork(learningRate, learningRateDecay, momentum, hiddenSize, activationFunction, trainningTime, numerialInputNormalization, activationFunctionSteep, minimumErrorVariation);
      bpn.setMeanSquaredErrorOutput(mainScreen.chartPanel);
      bpn.buildClassifier(instanceSet);
      mainScreen.inferencePanel.setNetwork(bpn);
    }
    mainScreen.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  }

  /**
   * Used to open a new training file
   *
   * @return True if the file was successfully opened and false otherway.
   * @throws Exception If any problem occur during file oppening
   */
  public boolean openFile() throws Exception{
    mainScreen.setCursor(new Cursor(Cursor.WAIT_CURSOR));
    String[] arff = {"ARFF"};
    String[] txt = {"TXT"};
    boolean fileOpenSuccess = false;
    fileChooser = new JFileChooser(FileController.getInstance().getCurrentDirectory());
    fileChooser.setDialogTitle(resource.getString("openFile2"));
    fileChooser.setMultiSelectionEnabled(false);
    fileChooser.setFileView(new FileIcon(mainScreen));
    fileChooser.addChoosableFileFilter(new SimpleFileFilter(txt, resource.getString("txtFiles") + " (*.txt)"));
    fileChooser.addChoosableFileFilter(new SimpleFileFilter(arff, resource.getString("arffFiles") + " (*.arff)"));
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
    mainScreen.setTitle("Backpropagation Neural Network - " + selectedFile.getName());
    mainScreen.attributePanel.setInstances(instanceSet);
    mainScreen.attributePanel.enableComboBox(true);
    hiddenLayerSize = (instanceSet.numAttributes() + 1 /*class attribute*/) / 2;
    if(hiddenLayerSize < 3){
      hiddenLayerSize = 3;
    }
  }

  /**
   * Returns the hidden layer size
   * @return The hidden layer size
   */
  public int getHiddenLayerSize(){
    return hiddenLayerSize;
  }

  /**
   * Used to save the generated model
   *
   * @return True if the model was saved successfully and false other way
   * @throws Exception If any problem occur during saving.
   */
  public boolean saveModel() throws Exception{
    mainScreen.setCursor(new Cursor(Cursor.WAIT_CURSOR));
    String[] bpnString = {"bpn"};   //backpropagation neural network
    boolean success = false;
    fileChooser = new JFileChooser(FileController.getInstance().getCurrentDirectory());
    fileChooser.setDialogTitle(resource.getString("saveModel2"));
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

  /**
   * Used to open a saved model
   *
   * @return True if the model was opened successfully and false otherway.
   * @throws Exception If any problem occur during the openning processe.
   */
  public boolean openModel() throws Exception{
    mainScreen.setCursor(new Cursor(Cursor.WAIT_CURSOR));
    String[] neuralNetworkString = {"bpn"};
    boolean success = false;
    fileChooser = new JFileChooser(FileController.getInstance().getCurrentDirectory());
    fileChooser.setDialogTitle(resource.getString("openModel2"));
    fileChooser.setMultiSelectionEnabled(false);
    fileChooser.setFileView(new FileIcon(mainScreen));
    fileChooser.addChoosableFileFilter(new SimpleFileFilter(neuralNetworkString, "Neural Network (*.bpn)"));
    int returnVal = fileChooser.showOpenDialog(mainScreen);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      File selectedFile = fileChooser.getSelectedFile();
      ObjectInputStream in = new ObjectInputStream(new FileInputStream(selectedFile));
      bpn = null;
      bpn = (NeuralNetwork)in.readObject();
      mainScreen.inferencePanel.setNetwork(bpn);
      mainScreen.setTitle("Backpropagation Neural Network - " + resource.getString("model") + " " + selectedFile.getName());
      FileController.getInstance().setCurrentDirectory(fileChooser.getCurrentDirectory());
      file = selectedFile;
      success = true;
    }
    mainScreen.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    return success;
  }
}