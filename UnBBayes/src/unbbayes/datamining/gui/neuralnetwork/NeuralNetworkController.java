package unbbayes.datamining.gui.neuralnetwork;


import java.awt.*;
import java.io.*;
//import java.util.*;
//import java.awt.print.*;
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
    float momentum;
    int hiddenSize;
    String trainningTime;  // não usado por enquanto
    int activationFunction;

    mainScreen.setCursor(new Cursor(Cursor.WAIT_CURSOR));
    if(instanceSet != null){
      learningRate = mainScreen.optionsPanel.getLearningRate();
      momentum = mainScreen.optionsPanel.getMomentum();
      hiddenSize = mainScreen.optionsPanel.getHiddenLayerSize();
      trainningTime = mainScreen.optionsPanel.getTrainningTime();
      activationFunction = mainScreen.optionsPanel.getSelectedActivationFunction();
      bpn = new NeuralNetwork(learningRate, momentum, hiddenSize, activationFunction, 0);
      bpn.buildClassifier(instanceSet);
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
    mainScreen.setTitle("Neural Network - " + selectedFile.getName());
    mainScreen.attributePanel.setInstances(instanceSet);
    mainScreen.attributePanel.enableComboBox(true);
  }






  public boolean saveModel() throws Exception{
    mainScreen.setCursor(new Cursor(Cursor.WAIT_CURSOR));
    String[] bpnString = {"bpn"};   //artificial neural network
    boolean success = false;
    fileChooser = new JFileChooser(FileController.getInstance().getCurrentDirectory());
//    fileChooser.setDialogTitle(resource.getString("saveModel2"));
    fileChooser.setMultiSelectionEnabled(false);
//    fileChooser.setFileView(new FileIcon(mainScreen));
    fileChooser.addChoosableFileFilter(new SimpleFileFilter(bpnString, "Neural Network (*.bpn)"));
    int returnVal = fileChooser.showSaveDialog(mainScreen);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      File selectedFile = fileChooser.getSelectedFile();
      String fileName = selectedFile.getName();
      if (!fileName.regionMatches(true, fileName.length() - 4, ".bpn", 0, 4)) {
        selectedFile = new File(selectedFile.getAbsolutePath() + ".bpn");
      }
      ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(selectedFile));
//      out.writeObject(cnm);           precisa arumar para salvar o modelo.
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
//      cnm = null;
//      cnm = (CombinatorialNeuralModel) in.readObject();
//      mainScreen.tabbedPaneRules.removeAll();
//      mainScreen.rulesPanel = new RulesPanel(this);
//      mainScreen.rulesPanel.setRulesPanel(cnm, cnm.getConfidence(), cnm.getSupport());
//      mainScreen.tabbedPaneRules.add(mainScreen.rulesPanel, BorderLayout.CENTER);
//      mainScreen.inferencePanel.setNetwork(cnm);
      mainScreen.setTitle("Neural Network - " + /*resource.getString*/("model") + " " + selectedFile.getName());
      FileController.getInstance().setCurrentDirectory(fileChooser.getCurrentDirectory());
      file = selectedFile;
      success = true;
    }
    mainScreen.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    return success;
  }

}